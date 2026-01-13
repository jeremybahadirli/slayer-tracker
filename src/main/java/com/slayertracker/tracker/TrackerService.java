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
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Skill;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.CommandExecuted;
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
			state.getTaskAmountChanges().addLast(new TrackerState.TaskAmountChange(amountDelta, client.getTickCount()));
			runQueueCycle();
		}
		state.setRemainingAmount(event.getValue());
	}

	public void handleSlayerTaskChange()
	{
		refreshCurrentAssignmentFromConfig();

		state.getAssignmentRecords().values().forEach(ar -> {
			ar.getInteractingNpcs().clear();
			ar.getVariantRecords().values().forEach(variantRecord -> variantRecord.getInteractingNpcs().clear());
			ar.getCustomRecords().forEach(customRecord -> customRecord.getInteractingNpcs().clear());
		});
		state.getEndedInteractions().clear();
	}

	public void handleInteractingChanged(InteractingChanged event)
	{
		if (event.getSource() != client.getLocalPlayer() && event.getTarget() != client.getLocalPlayer())
		{
			return;
		}

		NPC npc = null;
		if (event.getSource() instanceof NPC)
		{
			npc = (NPC) event.getSource();
		}
		else if (event.getTarget() instanceof NPC)
		{
			npc = (NPC) event.getTarget();
		}

		if (npc != null && slayerPluginService.getTargets().contains(npc))
		{
			handleTargetInteractingStart(npc);
		}
		else if (npc == null)
		{
			handleInteractingEnd();
		}
	}

	private void handleTargetInteractingStart(NPC npc)
	{
		triggerContinuousRecording();

		state.getEndedInteractions().removeIf(endedInteraction -> endedInteraction.getNpc() == npc);

		Predicate<Record> shouldSetCombatInstant = r -> (!isContinuousRecordingMode()
			|| getContinuousRecordingStartInstant().isAfter(r.getCombatInstant()))
			&& r.getInteractingNpcs().isEmpty();

		final Instant now = Instant.now();

		AssignmentRecord assignmentRecord = state.getAssignmentRecords().computeIfAbsent(state.getCurrentAssignment(), r -> new AssignmentRecord(state));
		if (shouldSetCombatInstant.test(assignmentRecord))
		{
			assignmentRecord.setCombatInstant(now);
		}
		assignmentRecord.getInteractingNpcs().add(npc);
		state.getCurrentAssignment().getVariantMatchingNpc(npc).ifPresent(variant -> {
			Record variantRecord = assignmentRecord.getVariantRecords().computeIfAbsent(variant, r -> new Record(state));
			if (shouldSetCombatInstant.test(variantRecord))
			{
				variantRecord.setCombatInstant(now);
			}
			variantRecord.getInteractingNpcs().add(npc);
		});
		assignmentRecord.getCustomRecords().stream()
			.filter(CustomRecord::isRecording)
			.forEach(customRecord -> {
				if (shouldSetCombatInstant.test(customRecord))
				{
					customRecord.setCombatInstant(now);
				}
				customRecord.getInteractingNpcs().add(npc);
			});
	}

	private void handleInteractingEnd()
	{
		final Predicate<NPC> isNotInteracting = npc ->
			client.getTopLevelWorldView().npcs().stream().noneMatch(worldNpc -> worldNpc.equals(npc))
				|| (client.getLocalPlayer().getInteracting() != npc
				&& npc.getInteracting() != client.getLocalPlayer());

		final Instant now = Instant.now();
		final int currentTick = client.getTickCount();

		Set<NPC> endedInteractionNpcs = new HashSet<>();
		state.getAssignmentRecords().values().forEach(ar -> {
			updateInteractingNpcs(ar, isNotInteracting, now, endedInteractionNpcs);
			ar.getVariantRecords().values().forEach(vr -> updateInteractingNpcs(vr, isNotInteracting, now, endedInteractionNpcs));
			ar.getCustomRecords().forEach(cr -> updateInteractingNpcs(cr, isNotInteracting, now, endedInteractionNpcs));
		});

		updateEndedInteractions(endedInteractionNpcs, currentTick);
		runQueueCycle();
	}

	private void updateInteractingNpcs(Record record, Predicate<NPC> isNotInteracting, Instant now, Set<NPC> endedInteractionNpcs)
	{
		boolean recordHadActiveInteractors = !record.getInteractingNpcs().isEmpty();

		record.getInteractingNpcs().removeIf(npc -> {
			if (isNotInteracting.test(npc))
			{
				endedInteractionNpcs.add(npc);
				return true;
			}
			return false;
		});

		boolean allInteractionsEnded = recordHadActiveInteractors && record.getInteractingNpcs().isEmpty();
		if (allInteractionsEnded)
		{
			record.addToHours(Duration.between(record.getCombatInstant(), now));
			record.setCombatInstant(now);
		}
	}

	private void updateEndedInteractions(Set<NPC> endedInteractionNpcs, int currentTick)
	{
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
				state.getEndedInteractions().remove(existingEntry);
				existingEntry.updateTick(currentTick);
				if (npc.isDead())
				{
					existingEntry.markDead();
				}
				state.getEndedInteractions().addLast(existingEntry);
				continue;
			}

			state.getEndedInteractions().addLast(new TrackerState.EndedInteraction(npc, currentTick, npc.isDead()));
		}
	}

	private void populateKillEventsFromInteractions(int currentTick)
	{
		AssignmentRecord currentAssignmentRecord = state.getCurrentAssignmentRecord();
		if (currentAssignmentRecord == null)
		{
			return;
		}

		List<NPC> killCandidates = new ArrayList<>();

		// Populate from interacting NPCs
		currentAssignmentRecord.getInteractingNpcs().stream()
			.filter(NPC::isDead)
			.forEach(killCandidates::add);

		// Populate from ended interactions
		Iterator<TrackerState.EndedInteraction> endedInteractionIterator = state.getEndedInteractions().iterator();
		while (endedInteractionIterator.hasNext())
		{
			TrackerState.EndedInteraction endedInteraction = endedInteractionIterator.next();
			// We can mark endedInteraction.isDead() in special conditions (bosses)
			if (endedInteraction.isDead() || endedInteraction.getNpc().isDead())
			{
				killCandidates.add(endedInteraction.getNpc());
				endedInteractionIterator.remove();
			}
		}

		for (NPC npc : killCandidates)
		{
			if (state.getKillEvents().stream().anyMatch(recentKill -> recentKill.getNpc() == npc))
			{
				continue;
			}
			state.getKillEvents().addLast(new TrackerState.KillEvent(npc, state.getCurrentAssignment(), currentTick));
			log("killEvent created: ", npc);
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
		state.getXpDropEvents().addLast(new TrackerState.XpDropEvent(slayerXpDrop, client.getTickCount()));
		state.setCachedXp(newSlayerXp);
		runQueueCycle();
	}

	// Runs on amount change, interaction end, and stat change
	private void runQueueCycle()
	{
		final int currentTick = client.getTickCount();
		pruneEndedInteractions(currentTick);
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
					killEventIterator.remove();
				}
				taskAmountChange.consume(1);
			}

			if (taskAmountChange.isConsumed())
			{
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
			state.getXpDropEvents().clear();
			Map<TrackerState.KillEvent, Integer> killEventXpAllocations = calculateXpAllocations(xpToAllocate, xpEligibleKillEvents);
			applyXpAllocations(killEventXpAllocations);
			xpEligibleKillEvents.forEach(ke -> {
				ke.markXpLogged();
				if (ke.isCompleted()) {
					log("xp-kill-completed", "npc", ke.getNpc(), "assignment", ke.getAssignment(), "taskAmountChange", null);
				}
			});
			state.getKillEvents().removeIf(TrackerState.KillEvent::isCompleted);
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
				NPCComposition composition = npc.getTransformedComposition();
				if (composition == null)
				{
					log("xp-allocation-missing-NPCComposition", npc);
					return 0;
				}
				Integer health = npcManager.getHealth(composition.getId());
				if (health == null)
				{
					log("xp-allocation-missing-health", npc, composition.getId());
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
		runQueueCycle(); // TODO Is this necessary? Should ensure all KillEvents are kc-logged.

		TrackerState.KillEvent killEvent = findEligibleKillEventForNpc(npc);
		if (killEvent == null)
		{
			log("loot-no-kill-found", "npc", npc, "event", event,
				"oldestKillTick", state.getKillEvents().isEmpty() ? -1 : state.getKillEvents().peekFirst().getTick(),
				"recentKills", state.getKillEvents().size());
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
			state.getKillEvents().remove(killEvent);
		}
	}

	private void pruneEndedInteractions(int currentTick)
	{
		while (!state.getEndedInteractions().isEmpty())
		{
			TrackerState.EndedInteraction endedInteraction = state.getEndedInteractions().peekFirst();

			boolean expired = currentTick - endedInteraction.getLastInteractedTick() > QUEUE_PRUNE_TICKS;

			if (expired)
			{
				log("EndedInteraction expired:", endedInteraction.getNpc());
				state.getEndedInteractions().removeFirst();
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

			boolean expired = currentTick - killEvent.getTick() > QUEUE_PRUNE_TICKS;
			boolean completed = killEvent.isCompleted();

			if (expired)
			{
				log("KillEvent expired:", killEvent);
			}
			if (expired || completed)
			{
				state.getKillEvents().removeFirst();
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
				state.getXpDropEvents().removeFirst();
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
				state.getTaskAmountChanges().removeFirst();
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
		for (Iterator<TrackerState.KillEvent> it = state.getKillEvents().descendingIterator(); it.hasNext(); )
		{
			TrackerState.KillEvent killEvent = it.next();
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
}
