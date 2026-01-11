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
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Function;
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
import net.runelite.api.Skill;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.api.gameval.ItemID;
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
		state.getAssignmentRecords().clear();

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
		reset();
	}

	public void handleSlayerTaskChange()
	{
		refreshCurrentAssignmentFromConfig();

		state.getAssignmentRecords().values().forEach(ar -> {
			ar.getInteractors().clear();
			ar.getVariantRecords().values().forEach(variantRecord -> variantRecord.getInteractors().clear());
			ar.getCustomRecords().forEach(customRecord -> customRecord.getInteractors().clear());
		});
	}

	public void handleTaskAmountChange()
	{
		if (slayerPluginService.getRemainingAmount() == slayerPluginService.getInitialAmount())
		{
			return;
		}
		triggerContinuousRecording();
		handleKills(state.getRemainingAmount() - slayerPluginService.getRemainingAmount());
		state.setRemainingAmount(slayerPluginService.getRemainingAmount());
	}

	public void handleInteractingChanged(InteractingChanged event)
	{
		if (state.getCurrentAssignment() == null
			|| (event.getSource() != client.getLocalPlayer() && event.getTarget() != client.getLocalPlayer()))
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

		Predicate<Record> shouldSetCombatInstant = r -> (!isContinuousRecordingMode()
			|| getContinuousRecordingStartInstant().isAfter(r.getCombatInstant()))
			&& r.getInteractors().isEmpty();

		final Instant now = Instant.now();

		AssignmentRecord assignmentRecord = state.getAssignmentRecords().computeIfAbsent(state.getCurrentAssignment(), r -> new AssignmentRecord(state));
		if (shouldSetCombatInstant.test(assignmentRecord))
		{
			assignmentRecord.setCombatInstant(now);
		}
		assignmentRecord.getInteractors().add(npc);

		state.getCurrentAssignment().getVariantMatchingNpc(npc).ifPresent(variant -> {
			Record variantRecord = assignmentRecord.getVariantRecords().computeIfAbsent(variant, r -> new Record(state));
			if (shouldSetCombatInstant.test(variantRecord))
			{
				variantRecord.setCombatInstant(now);
			}
			variantRecord.getInteractors().add(npc);
		});

		assignmentRecord.getCustomRecords().stream()
			.filter(CustomRecord::isRecording)
			.forEach(customRecord -> {
				if (shouldSetCombatInstant.test(customRecord))
				{
					customRecord.setCombatInstant(now);
				}
				customRecord.getInteractors().add(npc);
			});
	}

	private void handleInteractingEnd()
	{
		final Predicate<NPC> isNotInteracting = interactor ->
			client.getTopLevelWorldView().npcs().stream().noneMatch(npc -> npc.equals(interactor))
				|| client.getLocalPlayer() == null
				|| interactor.isDead()
				|| (client.getLocalPlayer().getInteracting() != interactor
				&& interactor.getInteracting() != client.getLocalPlayer());

		// Change to only iterate over current assignment record? Must then purge interactors when assignment ends.
		final Instant now = Instant.now();
		state.getAssignmentRecords().values().forEach(ar -> {
			ar.getInteractors().stream()
				.filter(isNotInteracting)
				.forEach(interactor -> {
					ar.addToHours(Duration.between(ar.getCombatInstant(), now));
					ar.setCombatInstant(now);
				});

			ar.getVariantRecords().values().forEach(vr ->
				vr.getInteractors().stream()
					.filter(isNotInteracting)
					.forEach(interactor -> {
						vr.addToHours(Duration.between(vr.getCombatInstant(), now));
						vr.setCombatInstant(now);
					}));

			ar.getCustomRecords().forEach(cr ->
				cr.getInteractors().stream()
					.filter(isNotInteracting)
					.forEach(interactor -> {
						cr.addToHours(Duration.between(cr.getCombatInstant(), now));
						cr.setCombatInstant(now);
					}));
		});

		state.getCurrentAssignmentRecord().getInteractors().stream()
			.filter(NPC::isDead)
			.forEach(npc -> {
				if (isNpcAlreadyTracked(npc))
				{
					return;
				}
				state.getRecentKills().addLast(new TrackerState.KillEvent(npc, state.getCurrentAssignment(), client.getTickCount()));
			});

		state.getAssignmentRecords().values().forEach(ar -> {
			ar.getInteractors().removeIf(isNotInteracting);
			ar.getVariantRecords().values().forEach(variantRecord ->
				variantRecord.getInteractors().removeIf(isNotInteracting));
			ar.getCustomRecords().forEach(customRecord ->
				customRecord.getInteractors().removeIf(isNotInteracting));
		});
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

		if (state.getCurrentAssignment() == null || !state.getAssignmentRecords().containsKey(state.getCurrentAssignment()))
		{
			return;
		}

		final int slayerXpDrop = newSlayerXp - state.getCachedXp();
		state.getSlayerXpDrops().addLast(new TrackerState.XpDrop(slayerXpDrop, client.getTickCount()));
		state.setCachedXp(newSlayerXp);
	}

	private void handleKills(int kills)
	{
		if (kills <= 0)
		{
			return;
		}

		final int currentTick = client.getTickCount();
		pruneRecentKills(currentTick);
		pruneSlayerXpDrops(currentTick);

		Queue<NPC> xpNpcQueue = new ArrayDeque<>();
		int killsRemaining = kills;
		for (Iterator<TrackerState.KillEvent> it = state.getRecentKills().iterator(); killsRemaining > 0 && it.hasNext(); )
		{
			TrackerState.KillEvent kill = it.next();
			if (kill.isKcLogged())
			{
				continue;
			}

			NPC npc = kill.getNpc();

			xpNpcQueue.add(npc);

			Assignment assignment = kill.getAssignment();
			AssignmentRecord assignmentRecord = state.getAssignmentRecords().get(assignment);
			assignmentRecord.incrementKc();

			assignment.getVariantMatchingNpc(npc).ifPresent(variant -> {
				Record variantRecord = assignmentRecord.getVariantRecords().get(variant);
				variantRecord.incrementKc();
			});

			assignmentRecord.getCustomRecords().stream()
				.filter(CustomRecord::isRecording)
				.forEach(Record::incrementKc);

			kill.markKcLogged();
			if (kill.isCompleted())
			{
				it.remove();
			}
			killsRemaining--;
		}

		int xpToAllocate = state.getSlayerXpDrops().stream()
			.mapToInt(TrackerState.XpDrop::xp)
			.sum();
		state.getSlayerXpDrops().clear();
		processXpQueue(xpToAllocate, xpNpcQueue);
	}

	private void processXpQueue(int xpToAllocate, Queue<NPC> xpNpcQueue)
	{
		if (xpNpcQueue.isEmpty() || xpToAllocate <= 0)
		{
			return;
		}

		final Function<NPC, Integer> getSlayerXp = npc ->
			state.getCurrentAssignment().getVariantMatchingNpc(npc)
				.flatMap(Variant::getSlayerXp)
				.orElse(npcManager.getHealth(Objects.requireNonNull(npc.getTransformedComposition()).getId()));

		final int npcXpTotal = xpNpcQueue.stream().mapToInt(getSlayerXp::apply).sum();

		NPC npc = xpNpcQueue.remove();
		final double thisNpcXpRatio = (double) xpToAllocate / npcXpTotal;
		final int thisNpcsXpShare = Math.toIntExact(Math.round(xpToAllocate * thisNpcXpRatio));
		xpToAllocate -= thisNpcsXpShare;

		AssignmentRecord assignmentRecord = state.getCurrentAssignmentRecord();
		assignmentRecord.addToXp(thisNpcsXpShare);

		state.getCurrentAssignment().getVariantMatchingNpc(npc).ifPresent(variant -> {
			Record variantRecord = assignmentRecord.getVariantRecords().get(variant);
			variantRecord.addToXp(thisNpcsXpShare);
		});

		assignmentRecord.getCustomRecords().stream()
			.filter(CustomRecord::isRecording)
			.forEach(customRecord ->
				customRecord.addToXp(thisNpcsXpShare));

		processXpQueue(xpToAllocate, xpNpcQueue);
	}

	public void handleNpcLootReceived(NpcLootReceived event)
	{
		NPC npc = event.getNpc();

		final int currentTick = client.getTickCount();
		pruneRecentKills(currentTick);
		TrackerState.KillEvent killEvent = findKillForNpc(npc);
		if (killEvent == null)
		{
			return;
		}

		killEvent.markLootLogged();

		Assignment assignment = killEvent.getAssignment();

		final int lootGe = event.getItems().stream().mapToInt(itemStack ->
				itemManager.getItemPrice(itemStack.getId()) * itemStack.getQuantity())
			.sum();

		final int lootHa = event.getItems().stream().mapToInt(itemStack -> {
				if (itemStack.getId() == ItemID.COINS)
				{
					return itemStack.getQuantity();
				}
				else
				{
					return itemManager.getItemComposition(itemStack.getId()).getHaPrice() * itemStack.getQuantity();
				}
			})
			.sum();

		AssignmentRecord assignmentRecord = state.getAssignmentRecords().get(assignment);
		assignmentRecord.addToGe(lootGe);
		assignmentRecord.addToHa(lootHa);

		assignment.getVariantMatchingNpc(npc).ifPresent(variant -> {
			Record variantRecord = assignmentRecord.getVariantRecords().get(variant);
			variantRecord.addToGe(lootGe);
			variantRecord.addToHa(lootHa);
		});

		assignmentRecord.getCustomRecords().stream()
			.filter(CustomRecord::isRecording)
			.forEach(customRecord -> {
				customRecord.addToGe(lootGe);
				customRecord.addToHa(lootHa);
			});

		if (killEvent.isCompleted())
		{
			state.getRecentKills().remove(killEvent);
		}
	}

	private void pruneRecentKills(int currentTick)
	{
		while (!state.getRecentKills().isEmpty())
		{
			TrackerState.KillEvent kill = state.getRecentKills().peekFirst();

			boolean expired = currentTick - kill.getTick() > QUEUE_PRUNE_TICKS;
			boolean completed = kill.isCompleted();

			if (expired)
			{
				log("Kill expired:", kill);
			}

			if (!expired && !completed)
			{
				break;
			}

			state.getRecentKills().removeFirst();
		}
	}

	private void pruneSlayerXpDrops(int currentTick)
	{
		while (!state.getSlayerXpDrops().isEmpty())
		{
			TrackerState.XpDrop drop = state.getSlayerXpDrops().peekFirst();

			boolean expired = currentTick - drop.tick() > QUEUE_PRUNE_TICKS;

			if (expired)
			{
				log("Xp drop expired:", drop);
			}

			if (!expired)
			{
				break;
			}

			state.getSlayerXpDrops().removeFirst();
		}
	}

	private TrackerState.KillEvent findKillForNpc(NPC npc)
	{
		for (Iterator<TrackerState.KillEvent> it = state.getRecentKills().descendingIterator(); it.hasNext(); )
		{
			TrackerState.KillEvent kill = it.next();
			if (kill.getNpc() == npc)
			{
				return kill;
			}
		}
		return null;
	}

	private boolean isNpcAlreadyTracked(NPC npc)
	{
		return state.getRecentKills().stream().anyMatch(kill -> kill.getNpc() == npc);
	}

	public void handleCommandExecuted(CommandExecuted event)
	{
		if (event.getCommand().equals("t") && state != null)
		{
			log("recent kills: ", state.getRecentKills());
			log("slayer xp drops: ", state.getSlayerXpDrops());
			log("current assignment: ", state.getCurrentAssignment());
			log("current assignment record: ", state.getCurrentAssignmentRecord());
			log("targets: ", slayerPluginService.getTargets());
			log("remaining amount: ", slayerPluginService.getRemainingAmount());
		}
	}

	public void saveRecords() throws Exception
	{
		if (state.getProfileFileName() == null)
		{
			return;
		}
		recordRepository.save(state.getAssignmentRecords(), state.getProfileFileName());
	}

	// Slayer config updates sooner than SlayerPluginService
	private void refreshCurrentAssignmentFromConfig()
	{
		Assignment.getAssignmentByName(
				configManager.getRSProfileConfiguration(SlayerConfig.GROUP_NAME, SlayerConfig.TASK_NAME_KEY))
			.ifPresentOrElse(assignment -> {
				state.setCurrentAssignment(assignment);
				state.setRemainingAmount(Integer.parseInt(configManager.getRSProfileConfiguration(SlayerConfig.GROUP_NAME, SlayerConfig.AMOUNT_KEY)));
			}, () -> {
				state.setCurrentAssignment(null);
				state.setRemainingAmount(0);
			});
	}

	private void reset()
	{
		state.clear();
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

	private void log(Object... objects)
	{
		Object[] out = new Object[objects.length + 1];
		out[0] = client.getTickCount();
		System.arraycopy(objects, 0, out, 1, objects.length);

		log.info("{}", java.util.Arrays.toString(out));
	}
}
