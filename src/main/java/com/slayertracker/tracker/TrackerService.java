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
import com.slayertracker.views.RecordingModePanel;
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
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
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

	@Getter
	@Setter
	private RecordingModeController recordingModeController;

	private static final Pattern BRACELET_OF_SLAUGHTER_ACTIVATE_PATTERN = Pattern.compile(
		"Your bracelet of slaughter prevents your slayer count from decreasing."
	);
	private static final Pattern EXPEDITIOUS_BRACELET_ACTIVATE_PATTERN = Pattern.compile(
		"Your expeditious bracelet helps you progress your slayer (?:task )?faster."
	);

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
		if (event.getVarpId() == VarPlayerID.SLAYER_COUNT)
		{
			handleSlayerCountVarbitChanged(event);
		}
		switch (event.getVarbitId())
		{
			case VarbitID.WESTERN_DIARY_ELITE_COMPLETE:
				state.getPlayerUnlockState().setWesternDiary(true);
				break;
			case VarbitID.KOUREND_DIARY_ELITE_COMPLETE:
				state.getPlayerUnlockState().setKourendDiary(true);
				break;
			case VarbitID.SLAYER_LONGER_ABERRANTSPECTRES:
				state.getPlayerUnlockState().addExtendedAssignment(Assignment.ABERRANT_SPECTRES);
				break;
			case VarbitID.SLAYER_LONGER_ABYSSALDEMONS:
				state.getPlayerUnlockState().addExtendedAssignment(Assignment.ABYSSAL_DEMONS);
				break;
			case VarbitID.SLAYER_LONGER_ANKOU:
				state.getPlayerUnlockState().addExtendedAssignment(Assignment.ANKOU);
				break;
			case VarbitID.SLAYER_LONGER_AQUANITES:
				state.getPlayerUnlockState().addExtendedAssignment(Assignment.AQUANITES);
				break;
			case VarbitID.SLAYER_LONGER_ARAXYTES:
				state.getPlayerUnlockState().addExtendedAssignment(Assignment.ARAXYTES);
				break;
			case VarbitID.SLAYER_LONGER_AVIANSIES:
				state.getPlayerUnlockState().addExtendedAssignment(Assignment.AVIANSIES);
				break;
			case VarbitID.SLAYER_LONGER_BASILISK:
				state.getPlayerUnlockState().addExtendedAssignment(Assignment.BASILISKS);
				break;
			case VarbitID.SLAYER_LONGER_BLACKDEMONS:
				state.getPlayerUnlockState().addExtendedAssignment(Assignment.BLACK_DEMONS);
				break;
			case VarbitID.SLAYER_LONGER_BLACKDRAGONS:
				state.getPlayerUnlockState().addExtendedAssignment(Assignment.BLACK_DRAGONS);
				break;
			case VarbitID.SLAYER_LONGER_BLOODVELD:
				state.getPlayerUnlockState().addExtendedAssignment(Assignment.BLOODVELD);
				break;
			case VarbitID.SLAYER_LONGER_CAVEHORRORS:
				state.getPlayerUnlockState().addExtendedAssignment(Assignment.CAVE_HORRORS);
				break;
			case VarbitID.SLAYER_LONGER_CAVEKRAKEN:
				state.getPlayerUnlockState().addExtendedAssignment(Assignment.CAVE_KRAKEN);
				break;
			case VarbitID.SLAYER_LONGER_CUSTODIANS:
				state.getPlayerUnlockState().addExtendedAssignment(Assignment.CUSTODIAN_STALKERS);
				break;
			case VarbitID.SLAYER_LONGER_DARKBEASTS:
				state.getPlayerUnlockState().addExtendedAssignment(Assignment.DARK_BEASTS);
				break;
			case VarbitID.SLAYER_LONGER_DUSTDEVILS:
				state.getPlayerUnlockState().addExtendedAssignment(Assignment.DUST_DEVILS);
				break;
			case VarbitID.SLAYER_LONGER_FOSSILWYVERNS:
				state.getPlayerUnlockState().addExtendedAssignment(Assignment.FOSSIL_ISLAND_WYVERNS);
				break;
			case VarbitID.SLAYER_LONGER_GARGOYLES:
				state.getPlayerUnlockState().addExtendedAssignment(Assignment.GARGOYLES);
				break;
			case VarbitID.SLAYER_LONGER_GREATERDEMONS:
				state.getPlayerUnlockState().addExtendedAssignment(Assignment.GREATER_DEMONS);
				break;
			case VarbitID.SLAYER_LONGER_METALDRAGONS:
				state.getPlayerUnlockState().addExtendedAssignment(Assignment.METAL_DRAGONS);
				break;
			case VarbitID.SLAYER_LONGER_NECHRYAEL:
				state.getPlayerUnlockState().addExtendedAssignment(Assignment.NECHRYAEL);
				break;
			case VarbitID.SLAYER_LONGER_REVENANTS:
				state.getPlayerUnlockState().addExtendedAssignment(Assignment.REVENANTS);
				break;
			case VarbitID.SLAYER_LONGER_SCABARITES:
				state.getPlayerUnlockState().addExtendedAssignment(Assignment.MINIONS_OF_SCABARAS);
				break;
			case VarbitID.SLAYER_LONGER_SKELETALWYVERNS:
				state.getPlayerUnlockState().addExtendedAssignment(Assignment.SKELETAL_WYVERNS);
				break;
			case VarbitID.SLAYER_LONGER_SPIRITUALGWD:
				state.getPlayerUnlockState().addExtendedAssignment(Assignment.SPIRITUAL_CREATURES);
				break;
			case VarbitID.SLAYER_LONGER_SUQAH:
				state.getPlayerUnlockState().addExtendedAssignment(Assignment.SUQAHS);
				break;
			case VarbitID.SLAYER_LONGER_VAMPYRES:
				state.getPlayerUnlockState().addExtendedAssignment(Assignment.VAMPYRES);
				break;
			case VarbitID.SLAYER_LONGER_WYRMS:
				state.getPlayerUnlockState().addExtendedAssignment(Assignment.WYRMS);
				break;
		}
	}

	private void handleSlayerCountVarbitChanged(VarbitChanged event)
	{
		if (client.getTickCount() == 0)
		{
			state.setRemainingAmount(event.getValue());
			return;
		}
		if (event.getValue() == client.getVarpValue(VarPlayerID.SLAYER_COUNT_ORIGINAL))
		{
			return;
		}
		final int amountDelta = state.getRemainingAmount() - event.getValue();
		if (amountDelta > 0)
		{
			state.getTaskAmountChanges().addLast(new TrackerState.AmountProc(amountDelta, client.getTickCount()));
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
	}

	// TODO consider ONLY using players current interacting. What systems rely on interactingNpcs?
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
		state.getEndedInteractions().removeIf(endedInteraction -> endedInteraction.getNpc() == npc);


		final Instant now = Instant.now();

		AssignmentRecord assignmentRecord = state.getAssignmentRecords().computeIfAbsent(state.getCurrentAssignment(), r -> new AssignmentRecord(state));
		if (!recordingModeController.isRecording())
		{
			log("set combat instant @S");
			assignmentRecord.setCombatInstant(now);
		}
		assignmentRecord.getInteractingNpcs().add(npc);
		state.getCurrentAssignment().getVariantMatchingNpc(npc).ifPresent(variant -> {
			Record variantRecord = assignmentRecord.getVariantRecords().computeIfAbsent(variant, r -> new Record(state));
			if (!recordingModeController.isRecording())
			{
				variantRecord.setCombatInstant(now);
			}
			variantRecord.getInteractingNpcs().add(npc);
		});
		assignmentRecord.getCustomRecords().stream()
			.filter(CustomRecord::isRecording)
			.forEach(customRecord -> {
				if (!recordingModeController.isRecording())
				{
					customRecord.setCombatInstant(now);
				}
				customRecord.getInteractingNpcs().add(npc);
			});

		recordingModeController.setRecording(true);
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

		for (NPC npc : endedInteractionNpcs)
		{
			TrackerState.EndedInteraction entry = state.getEndedInteractions()
				.stream()
				.filter(e -> e.getNpc() == npc)
				.findFirst()
				.orElseGet(() ->
				{
					TrackerState.EndedInteraction created =
						new TrackerState.EndedInteraction(npc, currentTick, npc.isDead());
					state.getEndedInteractions().addLast(created);
					return created;
				});

			// Ensure this entry is at the end of queue
			state.getEndedInteractions().remove(entry);
			state.getEndedInteractions().addLast(entry);
			entry.updateTick(currentTick);

			// Interactible check for weakness threshold NPCs (gargoyles)
			// Possible to be killed without dying
			if (isNpcDead(npc, state.getCurrentAssignment()))
			{
				entry.markDead();
			}
		}

		if (recordingModeController.getRecordingMode() == RecordingModePanel.RecordingMode.IN_COMBAT
			&& state.getCurrentAssignmentRecord().getInteractingNpcs().isEmpty())
		{
			recordingModeController.setRecording(false);
		}
	}

	public void pauseRecordingIfIdle()
	{
		if (recordingModeController == null)
		{
			return;
		}

		Actor localPlayer = client.getLocalPlayer();
		if (localPlayer != null && localPlayer.getInteracting() == null)
		{
			recordingModeController.setRecording(false);
		}
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
				System.out.println(recordingModeController.isRecording());
				if (recordingModeController.isRecording())
				{
					Duration duration = Duration.between(record.getCombatInstant(), now);
					log("Added duration: ", duration);
					record.addToHours(duration);
				}
				log("set combat instant @E");
				record.setCombatInstant(now);
				endedInteractionNpcs.add(npc);
				return true;
			}
			return false;
		});
	}

	public void handleChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM)
		{
			return;
		}

		String message = Text.removeTags(event.getMessage());
		Matcher slaughterMatcher = BRACELET_OF_SLAUGHTER_ACTIVATE_PATTERN.matcher(message);
		Matcher expeditiousMatcher = EXPEDITIOUS_BRACELET_ACTIVATE_PATTERN.matcher(message);

		if (slaughterMatcher.find())
		{
			state.getTaskAmountChanges().addLast(new TrackerState.AmountProc(1, client.getTickCount()));
		}
		else if (expeditiousMatcher.find())
		{
			Iterator<TrackerState.AmountProc> taskAmountChangeIterator = state.getTaskAmountChanges().iterator();
			if (taskAmountChangeIterator.hasNext())
			{
				TrackerState.AmountProc taskAmountChange = taskAmountChangeIterator.next();
				taskAmountChange.consume(1);
			}
			else
			{
				state.getExpeditiousProcs().addLast(new TrackerState.AmountProc(1, client.getTickCount()));
			}
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
		TrackerState.XpDropEvent xpDropEvent = new TrackerState.XpDropEvent(slayerXpDrop, client.getTickCount());
		state.getXpDropEvents().addLast(xpDropEvent);
		state.setCachedXp(newSlayerXp);
	}

	public void handleGameTick()
	{
		runQueueCycle();
	}

	private void runQueueCycle()
	{
		final int currentTick = client.getTickCount();
		pruneEndedInteractions(currentTick);
		populateKillEventsFromInteractions(currentTick);
		pruneKillEvents(currentTick);
		pruneXpDropEvents(currentTick);
		pruneTaskAmountChanges(currentTick);
		pruneExpeditiousProcs(currentTick);

		consumeTaskAmountChangesFromExpeditiousProcs();
		recordKc();
		recordXp();

		state.getKillEvents().removeIf(killEvent -> {
			if (killEvent.isCompleted())
			{
				onKillEventCompleted(killEvent);
				return true;
			}
			return false;
		});
	}

	private void pruneEndedInteractions(int currentTick)
	{
		while (!state.getEndedInteractions().isEmpty())
		{
			TrackerState.EndedInteraction endedInteraction = state.getEndedInteractions().peekFirst();

			boolean expired = currentTick - endedInteraction.getLastInteractedTick() > QUEUE_PRUNE_TICKS;

			if (expired)
			{
				log("EndedInteraction expired:", endedInteraction, endedInteraction.getNpc().isDead());
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

			NPC npc = killEvent.getNpc();
			boolean isNpcNull = npc == null;
			boolean expired = currentTick - killEvent.getTick() > QUEUE_PRUNE_TICKS;
			boolean completed = killEvent.isCompleted();

			if (isNpcNull)
			{
				log("KillEvent NPC is null:", killEvent);
			}
			if (expired)
			{
				log("KillEvent expired:", killEvent);
			}
			if (completed)
			{
				onKillEventCompleted(killEvent);
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

			boolean expired = currentTick - xpDropEvent.getTick() > QUEUE_PRUNE_TICKS;

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
			TrackerState.AmountProc taskAmountChange = state.getTaskAmountChanges().peekFirst();

			boolean expired = currentTick - taskAmountChange.getTick() > QUEUE_PRUNE_TICKS;
			boolean consumed = taskAmountChange.isConsumed();

			if (expired)
			{
				log("taskAmountChange expired:", taskAmountChange);
			}
			if (expired || consumed)
			{
				state.getTaskAmountChanges().removeFirst();
				continue;
			}
			break;
		}
	}

	private void pruneExpeditiousProcs(int currentTick)
	{
		while (!state.getExpeditiousProcs().isEmpty())
		{
			TrackerState.AmountProc expeditiousProc = state.getExpeditiousProcs().peekFirst();

			boolean expired = currentTick - expeditiousProc.getTick() > QUEUE_PRUNE_TICKS;
			boolean consumed = expeditiousProc.isConsumed();

			if (expired)
			{
				log("expeditiousProc expired:", expeditiousProc);
			}
			if (expired || consumed)
			{
				state.getExpeditiousProcs().removeFirst();
				continue;
			}
			break;
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

		for (Iterator<TrackerState.EndedInteraction> endedInteractionIterator = state.getEndedInteractions().iterator(); endedInteractionIterator.hasNext(); )
		{
			TrackerState.EndedInteraction endedInteraction = endedInteractionIterator.next();
			if (endedInteraction.isDead() || isNpcDead(endedInteraction.getNpc(), state.getCurrentAssignment()))
			{
				killCandidates.add(endedInteraction.getNpc());
				endedInteractionIterator.remove();
			}
		}

		for (NPC npc : killCandidates)
		{
			if (state.getKillEvents().stream().noneMatch(killEvent -> killEvent.getNpc() == npc))
			{
				state.getKillEvents().addLast(new TrackerState.KillEvent(npc, state.getCurrentAssignment(), currentTick));
			}
		}
	}

	private boolean isNpcDead(NPC npc, Assignment assignment)
	{
		final int health = calculateHealth(npc);
		return npc.isDead() || (health >= 0 && health <= assignment.getWeaknessThreshold());
	}

	// https://github.com/runelite/runelite/blob/c5289dea5790219da84542bb98b22e55d65d61fd/runelite-client/src/main/java/net/runelite/client/plugins/slayer/TargetWeaknessOverlay.java#L101
	private int calculateHealth(NPC target)
	{
		if (target == null || target.getName() == null)
		{
			return -1;
		}

		final int healthScale = target.getHealthScale();
		final int healthRatio = target.getHealthRatio();
		final Integer maxHealth = npcManager.getHealth(target.getId());

		if (healthRatio < 0 || healthScale <= 0 || maxHealth == null)
		{
			return -1;
		}

		//noinspection IntegerDivisionInFloatingPointContext
		return (int) ((maxHealth * healthRatio / healthScale) + 0.5f);
	}

	private void consumeTaskAmountChangesFromExpeditiousProcs()
	{
		Iterator<TrackerState.AmountProc> taskAmountChangeIterator = state.getTaskAmountChanges().iterator();
		for (Iterator<TrackerState.AmountProc> expeditiousProcIterator = state.getExpeditiousProcs().iterator(); expeditiousProcIterator.hasNext(); )
		{
			TrackerState.AmountProc expeditiousProc = expeditiousProcIterator.next();
			while (expeditiousProc.getUnloggedAmount() > 0)
			{
				log("consuming from expeditious proc");
				TrackerState.AmountProc taskAmountChange = taskAmountChangeIterator.next();
				taskAmountChange.consume(1);
				expeditiousProc.consume(1);
				if (taskAmountChange.isConsumed())
				{
					taskAmountChangeIterator.remove();
				}
				if (expeditiousProc.isConsumed())
				{
					expeditiousProcIterator.remove();
					log("expeditious proc consumed");
				}
			}
		}
	}

	private void recordXp()
	{
		List<TrackerState.KillEvent> xpEligibleKillEvents = state.getKillEvents().stream()
			.filter(killEvent -> killEvent.isKcLogged() && !killEvent.isXpLogged())
			.collect(Collectors.toList());

		int xpToAllocate = state.getXpDropEvents().stream()
			.mapToInt(TrackerState.XpDropEvent::getXp)
			.sum();

		if (!xpEligibleKillEvents.isEmpty() && xpToAllocate > 0)
		{
			state.getXpDropEvents().clear();
			Map<TrackerState.KillEvent, Integer> killEventXpAllocations = calculateXpAllocations(xpToAllocate, xpEligibleKillEvents);
			applyXpAllocations(killEventXpAllocations);
			xpEligibleKillEvents.forEach(ke -> {
				ke.markXpLogged();
				if (ke.isCompleted())
				{
					log("xp-kill-completed", "npc", ke.getNpc(), "assignment", ke.getAssignment(), "taskAmountChange", null);
				}
			});
		}
	}

	private void recordKc()
	{
		// Iterate through unlogged amount procs (task amount/expeditious bracelet procs).
		// For each, pull an unlogged kill event and increment KCs
		Iterator<TrackerState.KillEvent> killEventIterator = state.getKillEvents().iterator();
		for (Iterator<TrackerState.AmountProc> taskAmountChangeIterator = state.getTaskAmountChanges().iterator(); taskAmountChangeIterator.hasNext(); )
		{
			TrackerState.AmountProc taskAmountChange = taskAmountChangeIterator.next();

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
					onKillEventCompleted(killEvent);
					killEventIterator.remove();
				}
				taskAmountChange.consume(1);
			}

			if (taskAmountChange.isConsumed())
			{
				taskAmountChangeIterator.remove();
			}
			else
			{
				break; // No more KillEvents to consume amount procs
			}
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
				// NPCComposition is null on NPC despawn. Use NPC id.
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
			onKillEventCompleted(killEvent);
			state.getKillEvents().remove(killEvent);
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
		NPC npc = killEvent.getNpc();
		state.getEndedInteractions().removeIf(endedInteraction -> endedInteraction.getNpc() == npc);
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

	public void log(Object... objects)
	{
		Object[] out = new Object[objects.length + 1];
		out[0] = client.getTickCount();
		System.arraycopy(objects, 0, out, 1, objects.length);

		log.info("{}", java.util.Arrays.toString(out));
	}
}
