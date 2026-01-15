/*
 * Copyright (c) 2026, Jeremy Bahadirli <https://github.com/jeremybahadirli>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.slayertracker.tracker;

import com.slayertracker.RecordingModeController;
import com.slayertracker.groups.Assignment;
import com.slayertracker.groups.Variant;
import com.slayertracker.persistence.ProfileContext;
import com.slayertracker.persistence.RecordRepository;
import com.slayertracker.records.AssignmentRecord;
import com.slayertracker.records.CustomRecord;
import com.slayertracker.records.Record;
import com.slayertracker.state.TrackerState;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.NPCManager;
import net.runelite.client.plugins.slayer.SlayerConfig;
import net.runelite.client.plugins.slayer.SlayerPluginService;
import net.runelite.client.util.Text;

@Slf4j
@Singleton
public class TrackerService
{
	private final Client client;
	private final ConfigManager configManager;
	private final ItemManager itemManager;
	private final NPCManager npcManager;
	private final SlayerPluginService slayerPluginService;
	private final TrackerState state;
	private final RecordRepository recordRepository;
	private final ProfileContext profileContext;

	@Setter
	private RecordingModeController recordingModeController;

	final Pattern BOSSKILL_MESSAGE_PATTERN = Pattern.compile("Your (.+) (?:kill|success) count is: ?<col=[0-9a-f]{6}>([0-9,]+)</col>");
	final int QUEUE_PRUNE_TICKS = 20;

	@Inject
	public TrackerService(TrackerState state,
						  RecordRepository recordRepository,
						  ProfileContext profileContext,
						  Client client,
						  ConfigManager configManager,
						  ItemManager itemManager,
						  NPCManager npcManager,
						  SlayerPluginService slayerPluginService)
	{
		this.state = state;
		this.recordRepository = recordRepository;
		this.profileContext = profileContext;
		this.client = client;
		this.configManager = configManager;
		this.itemManager = itemManager;
		this.npcManager = npcManager;
		this.slayerPluginService = slayerPluginService;
	}

	public void handlePluginStart()
	{
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			state.setCachedXp(client.getSkillExperience(Skill.SLAYER));
		}
	}

	public void handleLogin() throws Exception
	{
		Optional<String> fileName = profileContext.getProfileFileName();
		state.setProfileFileName(fileName.orElse(null));
		if (fileName.isPresent())
		{
			state.getAssignmentRecords().putAll(recordRepository.load(fileName.get()));
		}

		refreshCurrentAssignmentFromConfig();
	}

	public void handleLogout() throws Exception
	{
		saveRecords();
		logDequeChange("endedInteractions", "clear-logout", state.getEndedInteractions());
		logDequeChange("killEvents", "clear-logout", state.getKillEvents());
		logDequeChange("xpDropEvents", "clear-logout", state.getXpDropEvents());
		logDequeChange("taskAmountChanges", "clear-logout", state.getTaskAmountChanges());
		state.clear();
	}

	public void handleVarbitChanged(VarbitChanged event)
	{
		if (event.getVarpId() != VarPlayerID.SLAYER_COUNT)
		{
			return;
		}
		else if (event.getValue() == client.getVarpValue(VarPlayerID.SLAYER_COUNT_ORIGINAL))
		{
			return;
		}
		else if (client.getTickCount() == 0)
		{
			state.setRemainingAmount(event.getValue());
			return;
		}

		// Remaining amount decrement
		triggerContinuousRecording();
		final int amountDelta = state.getRemainingAmount() - event.getValue();
		if (amountDelta > 0)
		{
			addTaskAmountChange(new TrackerState.TaskAmountChange(amountDelta, client.getTickCount()));
//			runQueueCycle();
		}
		state.setRemainingAmount(event.getValue());
	}

	public void handleSlayerTaskChange()
	{
		refreshCurrentAssignmentFromConfig();

		state.getAssignmentRecords().values().forEach(ar -> {
			clearInteractingNpcs(ar, "taskChange-assignment");
			ar.getVariantRecords().values().forEach(variantRecord -> clearInteractingNpcs(variantRecord, "taskChange-variant"));
			ar.getCustomRecords().forEach(customRecord -> clearInteractingNpcs(customRecord, "taskChange-custom"));
		});
		clearEndedInteractions("taskChange");
	}

	public void handleInteractingChanged(InteractingChanged event)
	{
		final Actor source = event.getSource();
		final Actor target = event.getTarget();

		if (source == client.getLocalPlayer()
			|| target == client.getLocalPlayer()
			|| state.getCurrentAssignmentRecord().getInteractingNpcs().stream().anyMatch(npc -> npc.equals(source) || npc.equals(target)))
		{
			handleInteractingEnd();
		}

		final NPC npc;
		if (source == client.getLocalPlayer() && target instanceof NPC)
		{
			npc = (NPC) target;
		}
		else if (target == client.getLocalPlayer() && source instanceof NPC)
		{
			npc = (NPC) source;
		}
		else
		{
			return;
		}

		if (slayerPluginService.getTargets().contains(npc))
		{
			handleTargetInteractingStart(npc);
		}
	}

	private void handleTargetInteractingStart(NPC npc)
	{
		triggerContinuousRecording();

		state.getEndedInteractions().removeIf(endedInteraction -> {
			boolean match = endedInteraction.getNpc() == npc;
			if (match)
			{
				logDequeChange("endedInteractions", "removeIf-interactingStart", endedInteraction);
			}
			return match;
		});

		Predicate<Record> shouldSetCombatInstant = r -> (!isContinuousRecordingMode()
			|| getContinuousRecordingStartInstant().isAfter(r.getCombatInstant()))
			&& r.getInteractingNpcs().isEmpty();

		final Instant now = Instant.now();

		AssignmentRecord assignmentRecord = state.getAssignmentRecords().computeIfAbsent(state.getCurrentAssignment(), r -> new AssignmentRecord(state));
		if (shouldSetCombatInstant.test(assignmentRecord))
		{
			assignmentRecord.setCombatInstant(now);
		}
		addNpcToInteracting(assignmentRecord, npc, "interacting-start-assignment");
		state.getCurrentAssignment().getVariantMatchingNpc(npc).ifPresent(variant -> {
			Record variantRecord = assignmentRecord.getVariantRecords().computeIfAbsent(variant, r -> new Record(state));
			if (shouldSetCombatInstant.test(variantRecord))
			{
				variantRecord.setCombatInstant(now);
			}
			addNpcToInteracting(variantRecord, npc, "interacting-start-variant");
		});
		assignmentRecord.getCustomRecords().stream()
			.filter(CustomRecord::isRecording)
			.forEach(customRecord -> {
				if (shouldSetCombatInstant.test(customRecord))
				{
					customRecord.setCombatInstant(now);
				}
				addNpcToInteracting(customRecord, npc, "interacting-start-custom");
			});
	}

	private void handleInteractingEnd()
	{
		final Instant now = Instant.now();
		final int currentTick = client.getTickCount();

		Set<NPC> endedInteractionNpcs = new HashSet<>();
		state.getAssignmentRecords().values().forEach(ar -> {
			updateInteractingNpcs(ar, now, endedInteractionNpcs);
			ar.getVariantRecords().values().forEach(vr -> updateInteractingNpcs(vr, now, endedInteractionNpcs));
			ar.getCustomRecords().forEach(cr -> updateInteractingNpcs(cr, now, endedInteractionNpcs));
		});

		if (endedInteractionNpcs.isEmpty())
		{
			return;
		}

		for (NPC npc : endedInteractionNpcs)
		{
			TrackerState.EndedInteraction existingEntry = state.getEndedInteractions()
				.stream()
				.filter(endedInteraction -> endedInteraction.getNpc() == npc)
				.findFirst()
				.orElse(null);

			if (existingEntry != null)
			{
				removeEndedInteraction(existingEntry, "update");
				existingEntry.updateTick(currentTick);
				if (npc.isDead())
				{
					existingEntry.markDead();
				}
				state.getEndedInteractions().addLast(existingEntry);
				logDequeChange("endedInteractions", "addLast-" + "update", existingEntry);
				continue;
			}
			TrackerState.EndedInteraction endedInteraction = new TrackerState.EndedInteraction(npc, currentTick, npc.isDead());
			state.getEndedInteractions().addLast(endedInteraction);
			logDequeChange("endedInteractions", "addLast-" + "new", endedInteraction);
		}
		//		runQueueCycle();
	}

	private void updateInteractingNpcs(Record record, Instant now, Set<NPC> endedInteractionNpcs)
	{
		final Predicate<NPC> isNotInteracting = npc ->
			client.getTopLevelWorldView().npcs().stream().noneMatch(worldNpc -> worldNpc.equals(npc))
				|| npc.isDead()
				|| (client.getLocalPlayer().getInteracting() != npc
				&& npc.getInteracting() != client.getLocalPlayer());

		record.getInteractingNpcs().removeIf(npc -> {
			if (isNotInteracting.test(npc))
			{
				logRecordInteraction("remove", record, npc, "interacting-end");
				record.addToHours(Duration.between(record.getCombatInstant(), now));
				record.setCombatInstant(now);
				endedInteractionNpcs.add(npc);
				return true;
			}
			return false;
		});
	}

	private void populateKillEventsFromInteractions(int currentTick)
	{
		AssignmentRecord currentAssignmentRecord = state.getCurrentAssignmentRecord();
		if (currentAssignmentRecord == null)
		{
			return;
		}

		List<NPC> killCandidates = new ArrayList<>();

		for (Iterator<TrackerState.EndedInteraction> endedInteractionIterator = state.getEndedInteractions().iterator(); endedInteractionIterator.hasNext(); )
		{
			TrackerState.EndedInteraction endedInteraction = endedInteractionIterator.next();
			// We can mark endedInteraction.isDead() in special conditions (bosses)
			if (endedInteraction.isDead() || endedInteraction.getNpc().isDead())
			{
				killCandidates.add(endedInteraction.getNpc());
				logDequeChange("endedInteractions", "iterator-remove-populateKillEvents", endedInteraction);
				endedInteractionIterator.remove();
			}
		}

		for (NPC npc : killCandidates)
		{
			if (state.getKillEvents().stream().anyMatch(killEvent -> killEvent.getNpc() == npc))
			{
				continue;
			}
			addKillEvent(new TrackerState.KillEvent(npc, state.getCurrentAssignment(), currentTick), "fromInteraction");
		}
	}

	public void handleChatMessage(ChatMessage event)
	{

		Matcher m = BOSSKILL_MESSAGE_PATTERN.matcher(event.getMessage());
		if (m.find())
		{
			String bossName = Text.removeTags(m.group(1));
			log(event, bossName);
		}
	}

	public void handleStatChanged(StatChanged event)
	{
		if (event.getSkill() != Skill.SLAYER)
		{
			return;
		}

		int newSlayerXp = event.getXp();

		if (newSlayerXp <= state.getCachedXp())
		{
			return;
		}

		if (state.getCachedXp() == -1)
		{
			state.setCachedXp(newSlayerXp);
			return;
		}

		final int slayerXpDrop = newSlayerXp - state.getCachedXp();
		addXpDropEvent(new TrackerState.XpDropEvent(slayerXpDrop, client.getTickCount()));
		state.setCachedXp(newSlayerXp);
//		runQueueCycle();
	}

	// Runs on amount change, interaction end, and stat change
	private void runQueueCycle()
	{
		final int currentTick = client.getTickCount();
		pruneEndedInteractions(currentTick);
		// TODO killevent created late
		populateKillEventsFromInteractions(currentTick);
		pruneKillEvents(currentTick);
		pruneXpDropEvents(currentTick);
		pruneTaskAmountChanges(currentTick);

		Iterator<TrackerState.KillEvent> killEventIterator = state.getKillEvents().iterator();

		// Iterate through unlogged task amount changes. For each, pull an unlogged kill event and increment KCs
		for (Iterator<TrackerState.TaskAmountChange> taskAmountChangeIterator = state.getTaskAmountChanges().iterator(); taskAmountChangeIterator.hasNext(); )
		{
			TrackerState.TaskAmountChange taskAmountChange = taskAmountChangeIterator.next();

			while (taskAmountChange.getUnloggedAmount() > 0)
			{
				TrackerState.KillEvent killEvent = nextUnloggedKillEvent(killEventIterator);
				if (killEvent == null)
				{
					break;
				}

				// Increment if record exists. Record should have been created on interacting start;
				// if not, do nothing to avoid record with hours = 0;
				Assignment assignment = killEvent.getAssignment();
				AssignmentRecord assignmentRecord = state.getAssignmentRecords().get(assignment);
				if (assignmentRecord != null)
				{
					assignmentRecord.incrementKc();
					assignment.getVariantMatchingNpc(killEvent.getNpc()).ifPresent(variant -> {
						Record variantRecord = assignmentRecord.getVariantRecords().get(variant);
						if (variantRecord != null)
						{
							variantRecord.incrementKc();
						}
					});
					assignmentRecord.getCustomRecords().stream()
						.filter(CustomRecord::isRecording)
						.forEach(Record::incrementKc);
				}

				killEvent.markKcLogged();
				if (killEvent.isCompleted())
				{
					log("kill-kill-completed", "npc", killEvent.getNpc(), "assignment", assignment, "taskAmountChange", taskAmountChange);
					onKillEventCompleted(killEvent);
					logDequeChange("killEvents", "iterator-remove-kc", killEvent);
					killEventIterator.remove();
				}
				taskAmountChange.consume(1);
			}

			if (taskAmountChange.isConsumed())
			{
				logDequeChange("taskAmountChanges", "iterator-remove-consumed", taskAmountChange);
				taskAmountChangeIterator.remove();
			}
			else // Ran out of KillEvents
			{
				break;
			}
		}

		List<TrackerState.KillEvent> xpEligibleKillEvents = state.getKillEvents().stream()
			.filter(killEvent -> killEvent.isKcLogged() && !killEvent.isXpLogged())
			.toList();

		int xpToAllocate = state.getXpDropEvents().stream()
			.mapToInt(TrackerState.XpDropEvent::xp)
			.sum();

		if (!xpEligibleKillEvents.isEmpty() && xpToAllocate > 0)
		{
			clearXpDropEvents("allocation");
			Map<TrackerState.KillEvent, Integer> killEventXpAllocations = calculateXpAllocations(xpToAllocate, xpEligibleKillEvents);
			applyXpAllocations(killEventXpAllocations);
			xpEligibleKillEvents.forEach(ke -> {
				ke.markXpLogged();
				if (ke.isCompleted())
				{
					log("xp-kill-completed", "npc", ke.getNpc(), "assignment", ke.getAssignment(), "taskAmountChange", null);
				}
			});
			state.getKillEvents().removeIf(killEvent -> {
				if (killEvent.isCompleted())
				{
					onKillEventCompleted(killEvent);
					logDequeChange("killEvents", "removeIf-completed-xp", killEvent);
					return true;
				}
				return false;
			});
		}
	}

	private Map<TrackerState.KillEvent, Integer> calculateXpAllocations(int xpToAllocate, List<TrackerState.KillEvent> killEvents)
	{
		Map<TrackerState.KillEvent, Integer> killEventXpAllocations = new HashMap<>();

		final int npcXpTotal = killEvents.stream().mapToInt(this::getSlayerXpForKillEvent).sum();
		if (npcXpTotal <= 0)
		{
			log("xp-allocation-npcXpTotal=0", killEvents);
			return killEventXpAllocations;
		}

		int remainingXp = xpToAllocate;
		for (int i = 0; i < killEvents.size(); i++)
		{
			TrackerState.KillEvent killEvent = killEvents.get(i);
			int killEventXpAllocation;
			if (i == killEvents.size() - 1)
			{
				killEventXpAllocation = remainingXp;
			}
			else
			{
				double killEventXpRatio = (double) getSlayerXpForKillEvent(killEvent) / npcXpTotal;
				killEventXpAllocation = Math.min((int) Math.round(xpToAllocate * killEventXpRatio), remainingXp);
				remainingXp -= killEventXpAllocation;
			}
			killEventXpAllocations.put(killEvent, killEventXpAllocation);
		}

		return killEventXpAllocations;
	}

	private int getSlayerXpForKillEvent(TrackerState.KillEvent killEvent)
	{
		Assignment assignment = killEvent.getAssignment();
		NPC npc = killEvent.getNpc();

		// Health-based xp amount isn't a property of Variant because it requires an NPC object to calculate
		return assignment.getVariantMatchingNpc(npc)
			.flatMap(Variant::getSlayerXp)
			.orElseGet(() -> {
				// NPCComposition is null on NPC despawn. Use NPC id for now.
				// TODO change NPCPredicates to take
				Integer health = npcManager.getHealth(npc.getId());
				if (health == null)
				{
					log("xp-allocation-missing-health", npc, npc.getId());
					return 0;
				}
				return health;
			});
	}

	private void applyXpAllocations(Map<TrackerState.KillEvent, Integer> killEventXpAllocations)
	{
		if (killEventXpAllocations.isEmpty())
		{
			log("xp-allocation-couldn't-apply-no-killEventXpAllocations");
			return;
		}

		killEventXpAllocations.forEach((killEvent, killEventXpAllocation) -> {
			if (killEventXpAllocation <= 0)
			{
				log("xp-allocation-killEventXpAllocation<=0", killEvent, killEventXpAllocation);
				return;
			}

			Assignment assignment = killEvent.getAssignment();
			if (assignment == null)
			{
				log("xp-allocation-missing-assignment", killEvent);
				return;
			}

			// Increment if record exists. Record should have been created on interacting start;
			// if not, do nothing to avoid record with hours = 0;
			AssignmentRecord assignmentRecord = state.getAssignmentRecords().get(assignment);
			if (assignmentRecord != null)
			{
				assignmentRecord.addToXp(killEventXpAllocation);
				assignment.getVariantMatchingNpc(killEvent.getNpc()).ifPresent(variant -> {
					Record variantRecord = assignmentRecord.getVariantRecords().get(variant);
					if (variantRecord != null)
					{
						variantRecord.addToXp(killEventXpAllocation);
					}
				});
				assignmentRecord.getCustomRecords().stream()
					.filter(CustomRecord::isRecording)
					.forEach(customRecord -> customRecord.addToXp(killEventXpAllocation));
			}
		});
	}

	public void handleNpcLootReceived(NpcLootReceived event)
	{
		NPC npc = event.getNpc();

//		final int currentTick = client.getTickCount();
//		pruneKillEvents(currentTick);
//		runQueueCycle(); // TODO Is this necessary? Should ensure all KillEvents are kc-logged.

		TrackerState.KillEvent killEvent = findEligibleKillEventForNpc(npc);
		if (killEvent == null)
		{
			return;
		}

		final int lootGe = event.getItems().stream().mapToInt(itemStack ->
			itemManager.getItemPrice(itemStack.getId()) * itemStack.getQuantity()
		).sum();

		final int lootHa = event.getItems().stream().mapToInt(itemStack -> {
			if (itemStack.getId() == ItemID.COINS)
			{
				return itemStack.getQuantity();
			}
			else
			{
				return itemManager.getItemComposition(itemStack.getId()).getHaPrice() * itemStack.getQuantity();
			}
		}).sum();

		// Increment if record exists. Record should have been created on interacting start;
		// if not, do nothing to avoid record with hours = 0;
		Assignment assignment = killEvent.getAssignment();
		AssignmentRecord assignmentRecord = state.getAssignmentRecords().get(assignment);
		if (assignmentRecord != null)
		{
			assignmentRecord.addToGe(lootGe);
			assignmentRecord.addToHa(lootHa);
			assignment.getVariantMatchingNpc(npc).ifPresent(variant -> {
				Record variantRecord = assignmentRecord.getVariantRecords().get(variant);
				if (variantRecord != null)
				{
					variantRecord.addToGe(lootGe);
					variantRecord.addToHa(lootHa);
				}
			});
			assignmentRecord.getCustomRecords().stream()
				.filter(CustomRecord::isRecording)
				.forEach(customRecord -> {
					customRecord.addToGe(lootGe);
					customRecord.addToHa(lootHa);
				});
		}

		killEvent.markLootLogged();
		if (killEvent.isCompleted())
		{
			log("loot-kill-completed", "npc", npc, "event", event, "killEvent", killEvent);
			onKillEventCompleted(killEvent);
			removeKillEvent(killEvent, "loot");
		}
	}

	// TODO Prune/log when npc==null
	// TODO Prune when killEvent contains NPC
	private void pruneEndedInteractions(int currentTick)
	{
		while (!state.getEndedInteractions().isEmpty())
		{
			TrackerState.EndedInteraction endedInteraction = state.getEndedInteractions().peekFirst();

			boolean expired = currentTick - endedInteraction.getLastInteractedTick() > QUEUE_PRUNE_TICKS;

			if (expired)
			{
				log("EndedInteraction expired:", endedInteraction.getNpc());
				removeFirstEndedInteraction("prune-expired");
				continue;
			}
			break;
		}
	}

	private void pruneKillEvents(int currentTick)
	{
		while (!state.getKillEvents().isEmpty())
		{
			TrackerState.KillEvent killEvent = state.getKillEvents().peekFirst();

			NPC npc = killEvent.getNpc();
			boolean isNpcNull = npc == null;
			boolean isNpcCompositionNull = !isNpcNull && npc.getTransformedComposition() == null;
			boolean expired = currentTick - killEvent.getTick() > QUEUE_PRUNE_TICKS;
			boolean completed = killEvent.isCompleted();

			if (isNpcNull)
			{
				log("KillEvent NPC is null:", killEvent);
			}
			if (isNpcCompositionNull)
			{
				log("KillEvent NPCComposition is null:", killEvent);
			}
			if (expired)
			{
				log("KillEvent expired:", killEvent);
			}
			if (completed)
			{
				onKillEventCompleted(killEvent);
				logDequeChange("killEvents", "prune-remove-completed", killEvent);
			}
			if (expired || completed)
			{
				removeFirstKillEvent(expired ? "prune-expired" : "prune-completed");
				continue;
			}
			break;
		}
	}

	private void pruneXpDropEvents(int currentTick)
	{
		while (!state.getXpDropEvents().isEmpty())
		{
			TrackerState.XpDropEvent xpDropEvent = state.getXpDropEvents().peekFirst();

			boolean expired = currentTick - xpDropEvent.tick() > QUEUE_PRUNE_TICKS;

			if (expired)
			{
				log("xpDropEvent expired:", xpDropEvent);
				removeFirstXpDropEvent("prune-expired");
				continue;
			}
			break;
		}
	}

	private void pruneTaskAmountChanges(int currentTick)
	{
		while (!state.getTaskAmountChanges().isEmpty())
		{
			TrackerState.TaskAmountChange taskAmountChange = state.getTaskAmountChanges().peekFirst();

			boolean expired = currentTick - taskAmountChange.getTick() > QUEUE_PRUNE_TICKS;
			boolean consumed = taskAmountChange.isConsumed();

			if (expired)
			{
				log("taskAmountChanges expired:", taskAmountChange);
			}
			if (expired || consumed)
			{
				removeFirstTaskAmountChange(expired ? "prune-expired" : "prune-consumed");
				continue;
			}
			break;
		}
	}

	private TrackerState.KillEvent nextUnloggedKillEvent(Iterator<TrackerState.KillEvent> iterator)
	{
		while (iterator.hasNext())
		{
			TrackerState.KillEvent killEvent = iterator.next();
			if (!killEvent.isKcLogged())
			{
				return killEvent;
			}
		}
		return null;
	}

	private TrackerState.KillEvent findEligibleKillEventForNpc(NPC npc)
	{
		for (TrackerState.KillEvent killEvent : state.getKillEvents())
		{
			if (killEvent.getNpc() == npc)
			{
				if (!killEvent.isKcLogged())
				{
					log("loot-KillEvent associated with NPC was not kc-logged:", killEvent);
					return null;
				}
				return killEvent;
			}
		}
		return null;
	}

	private void onKillEventCompleted(TrackerState.KillEvent killEvent)
	{
		final Instant now = Instant.now();
		NPC npc = killEvent.getNpc();

		state.getAssignmentRecords().values().forEach(ar -> {
			removeNpcFromRecord(ar, npc, now);
			ar.getVariantRecords().values().forEach(vr -> removeNpcFromRecord(vr, npc, now));
			ar.getCustomRecords().forEach(cr -> removeNpcFromRecord(cr, npc, now));
		});
		state.getEndedInteractions().removeIf(endedInteraction -> {
			boolean match = endedInteraction.getNpc() == npc;
			if (match)
			{
				logDequeChange("endedInteractions", "removeIf-onKillEventCompleted", endedInteraction);
			}
			return match;
		});
	}

	private void removeNpcFromRecord(Record record, NPC npc, Instant now)
	{
		boolean removed = record.getInteractingNpcs().remove(npc);
		if (removed)
		{
			logRecordInteraction("remove", record, npc, "killEvent-completed");
		}
		if (removed && record.getInteractingNpcs().isEmpty())
		{
			record.addToHours(Duration.between(record.getCombatInstant(), now));
			record.setCombatInstant(now);
		}
	}

	private void addNpcToInteracting(Record record, NPC npc, String reason)
	{
		if (record.getInteractingNpcs().add(npc))
		{
			logRecordInteraction("add", record, npc, reason);
		}
	}

	private void clearInteractingNpcs(Record record, String reason)
	{
		if (!record.getInteractingNpcs().isEmpty())
		{
			logRecordInteraction("clear", record, null, reason);
			record.getInteractingNpcs().clear();
		}
	}

	private void logRecordInteraction(String action, Record record, NPC npc, String reason)
	{
		log("record-interactingNpcs-" + action, reason, record, npc);
	}

	private void removeEndedInteraction(TrackerState.EndedInteraction endedInteraction, String reason)
	{
		state.getEndedInteractions().remove(endedInteraction);
		logDequeChange("endedInteractions", "remove-" + reason, endedInteraction);
	}

	private void removeFirstEndedInteraction(String reason)
	{
		TrackerState.EndedInteraction removed = state.getEndedInteractions().removeFirst();
		logDequeChange("endedInteractions", "removeFirst-" + reason, removed);
	}

	private void clearEndedInteractions(String reason)
	{
		if (!state.getEndedInteractions().isEmpty())
		{
			logDequeChange("endedInteractions", "clear-" + reason, state.getEndedInteractions());
			state.getEndedInteractions().clear();
		}
	}

	private void addKillEvent(TrackerState.KillEvent killEvent, String reason)
	{
		state.getKillEvents().addLast(killEvent);
		logDequeChange("killEvents", "addLast-" + reason, killEvent);
	}

	private void removeKillEvent(TrackerState.KillEvent killEvent, String reason)
	{
		state.getKillEvents().remove(killEvent);
		logDequeChange("killEvents", "remove-" + reason, killEvent);
	}

	private void removeFirstKillEvent(String reason)
	{
		TrackerState.KillEvent removed = state.getKillEvents().removeFirst();
		logDequeChange("killEvents", "removeFirst-" + reason, removed);
	}

	private void addXpDropEvent(TrackerState.XpDropEvent xpDropEvent)
	{
		state.getXpDropEvents().addLast(xpDropEvent);
		logDequeChange("xpDropEvents", "addLast", xpDropEvent);
	}

	private void clearXpDropEvents(String reason)
	{
		if (!state.getXpDropEvents().isEmpty())
		{
			logDequeChange("xpDropEvents", "clear-" + reason, state.getXpDropEvents());
			state.getXpDropEvents().clear();
		}
	}

	private void removeFirstXpDropEvent(String reason)
	{
		TrackerState.XpDropEvent removed = state.getXpDropEvents().removeFirst();
		logDequeChange("xpDropEvents", "removeFirst-" + reason, removed);
	}

	private void addTaskAmountChange(TrackerState.TaskAmountChange taskAmountChange)
	{
		state.getTaskAmountChanges().addLast(taskAmountChange);
		logDequeChange("taskAmountChanges", "addLast", taskAmountChange);
	}

	private void removeFirstTaskAmountChange(String reason)
	{
		TrackerState.TaskAmountChange removed = state.getTaskAmountChanges().removeFirst();
		logDequeChange("taskAmountChanges", "removeFirst-" + reason, removed);
	}

	private void logDequeChange(String dequeName, String action, Object value)
	{
		log("deque-" + dequeName + "-" + action, value);
	}

	public void saveRecords() throws Exception
	{
		if (state.getProfileFileName() == null)
		{
			return;
		}
		recordRepository.save(state.getAssignmentRecords(), state.getProfileFileName());
	}

	private void refreshCurrentAssignmentFromConfig()
	{
		Assignment.getAssignmentByName(
				configManager.getRSProfileConfiguration(SlayerConfig.GROUP_NAME, SlayerConfig.TASK_NAME_KEY))
			.ifPresentOrElse(assignment -> {
				state.setCurrentAssignment(assignment);
				state.setRemainingAmount(client.getVarpValue(VarPlayerID.SLAYER_COUNT));
			}, () -> {
				state.setCurrentAssignment(null);
				state.setRemainingAmount(0);
			});
	}

	private boolean isContinuousRecordingMode()
	{
		return recordingModeController != null && recordingModeController.isContinuousRecordingMode();
	}

	private Instant getContinuousRecordingStartInstant()
	{
		return recordingModeController == null ? Instant.EPOCH : recordingModeController.getContinuousRecordingStartInstant();
	}

	private void triggerContinuousRecording()
	{
		if (recordingModeController != null)
		{
			recordingModeController.setContinuousRecording(isContinuousRecordingMode());
		}
	}

	public void log(Object... objects)
	{
		Object[] out = new Object[objects.length + 1];
		out[0] = client.getTickCount();
		System.arraycopy(objects, 0, out, 1, objects.length);

		log.info("{}", java.util.Arrays.toString(out));
	}

	public void handleGameTick(GameTick event)
	{
		runQueueCycle();
	}
}
