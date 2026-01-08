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
import java.util.Optional;
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
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.events.ConfigChanged;
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

	public void onPluginStart()
	{
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			state.setCachedXp(client.getSkillExperience(Skill.SLAYER));
		}
	}

	public void handleLogin() throws Exception
	{
		refreshCurrentAssignmentFromConfig();
		state.getAssignmentRecords().clear();

		Optional<String> fileName = profileContext.getProfileFileName();
		state.setProfileFileName(fileName.orElse(null));
		if (fileName.isPresent())
		{
			state.getAssignmentRecords().putAll(recordRepository.load(fileName.get()));
		}
	}

	public void handleLogout() throws Exception
	{
		saveRecords();
		reset();
	}

	public void onSlayerConfigChanged(ConfigChanged event)
	{
		if (!SlayerConfig.GROUP_NAME.equals(event.getGroup()) || !SlayerConfig.TASK_NAME_KEY.equals(event.getKey()))
		{
			return;
		}

		// Slayer task changed

		Assignment.getAssignmentByName(event.getNewValue()).ifPresentOrElse((a) -> {
			state.setCurrentAssignment(a);
			state.clearQueues();
		}, () -> state.setCurrentAssignment(null));

		state.getAssignmentRecords().values().forEach(ar -> {
			ar.getInteractors().clear();
			ar.getVariantRecords().values().forEach(variantRecord -> variantRecord.getInteractors().clear());
			ar.getCustomRecords().forEach(customRecord -> customRecord.getInteractors().clear());
		});
	}

	public void onGameTick()
	{
		if (state.getCurrentAssignment() == null)
		{
			return;
		}

		final Predicate<NPC> isNotInteracting = interactor ->
			client.getTopLevelWorldView().npcs().stream().noneMatch(npc -> npc.equals(interactor))
				|| client.getLocalPlayer() == null
				|| (client.getLocalPlayer().getInteracting() != interactor
				&& interactor.getInteracting() != client.getLocalPlayer());

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

		state.getAssignmentRecords().values().forEach(ar -> {
			ar.getInteractors().removeIf(isNotInteracting);
			ar.getVariantRecords().values().forEach(variantRecord ->
				variantRecord.getInteractors().removeIf(isNotInteracting));
			ar.getCustomRecords().forEach(customRecord ->
				customRecord.getInteractors().removeIf(isNotInteracting));
		});
	}

	public void onInteractingChanged(InteractingChanged event)
	{
		if (state.getCurrentAssignment() == null)
		{
			return;
		}

		final NPC npc = getNpcFromInteraction(event);
		if (npc == null || !slayerPluginService.getTargets().contains(npc))
		{
			return;
		}

		triggerContinuousRecording();

		state.getAssignmentRecords().putIfAbsent(state.getCurrentAssignment(), new AssignmentRecord(state));
		AssignmentRecord assignmentRecord = state.getAssignmentRecords().get(state.getCurrentAssignment());

		Predicate<Record> shouldSetCombatInstant = r -> (!isContinuousRecordingMode()
			|| getContinuousRecordingStartInstant().isAfter(r.getCombatInstant()))
			&& r.getInteractors().isEmpty();

		final Instant now = Instant.now();
		if (shouldSetCombatInstant.test(assignmentRecord))
		{
			assignmentRecord.setCombatInstant(now);
		}
		assignmentRecord.getInteractors().add(npc);

		state.getCurrentAssignment().getVariantMatchingNpc(npc).ifPresent(variant -> {
			Record variantRecord = assignmentRecord.getVariantRecords().computeIfAbsent(variant, v -> new Record(state));
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

	public void onChatMessage(ChatMessage event)
	{
		final Pattern BOSSKILL_MESSAGE_PATTERN = Pattern.compile("Your (.+) (?:kill|success) count is: ?<col=[0-9a-f]{6}>([0-9,]+)</col>");

		Matcher m = BOSSKILL_MESSAGE_PATTERN.matcher(event.getMessage());
		if (m.find())
		{
			String bossName = Text.removeTags(m.group(1));

			System.out.println("CME " + client.getTickCount());
		}

	}

	public void onActorDeath(ActorDeath event)
	{
		System.out.println("ADE " + client.getTickCount());
		if (state.getCurrentAssignment() == null || !state.getAssignmentRecords().containsKey(state.getCurrentAssignment()))
		{
			return;
		}

		if (!(event.getActor() instanceof NPC))
		{
			return;
		}

		NPC npc = (NPC) event.getActor();

		if (state.getAssignmentRecords().get(state.getCurrentAssignment()).getInteractors().stream().noneMatch(interactor -> interactor.equals(npc)))
		{
			return;
		}

		triggerContinuousRecording();

		state.getKcNpcQueue().add(npc);
		state.getXpNpcQueue().add(npc);
		state.getLootNpcQueue().put(npc, state.getCurrentAssignment());
	}

	// Well probably need to check all interactors to see if they have "noActorDeath" property and set a flag, assuming
	// tick misorder.
	// If death queue contains NPCs, process death queue, otherwise add KC/XP to queue to be processed on chatmessage for bosses,
	// assuming tick misorder.
	// Actually, just manually program all XP values.
	public void onStatChanged(StatChanged event)
	{
		if (event.getSkill() != Skill.SLAYER)
		{
			return;
		}
		System.out.println("SCE " + client.getTickCount());

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
		state.setCachedXp(newSlayerXp);

		processDeathQueue();
		divideXp(slayerXpDrop);
	}

	public void onNpcLootReceived(NpcLootReceived event)
	{
		System.out.println("NLE " + client.getTickCount());
		NPC npc = event.getNpc();
		if (!state.getLootNpcQueue().containsKey(npc))
		{
			return;
		}

		Assignment assignment = state.getLootNpcQueue().get(npc);

		final int lootGe = event.getItems().stream().mapToInt(itemStack ->
				itemManager.getItemPrice(itemStack.getId()) * itemStack.getQuantity())
			.sum();

		final int lootHa = event.getItems().stream().mapToInt(itemStack -> {
				if (itemStack.getId() == net.runelite.api.gameval.ItemID.COINS)
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

		state.getLootNpcQueue().remove(npc);
	}

	public void saveRecords() throws Exception
	{
		System.out.println("Saving records" + state.getProfileFileName());
		if (state.getProfileFileName() == null)
		{
			return;
		}
		recordRepository.save(state.getAssignmentRecords(), state.getProfileFileName());
	}

	public void refreshCurrentAssignmentFromConfig()
	{
		Assignment.getAssignmentByName(
				configManager.getRSProfileConfiguration(SlayerConfig.GROUP_NAME, SlayerConfig.TASK_NAME_KEY))
			.ifPresent(state::setCurrentAssignment);
	}

	private void reset()
	{
		state.clear();
	}

	private void processDeathQueue()
	{
		for (NPC npc : state.getKcNpcQueue())
		{
			AssignmentRecord assignmentRecord = state.getAssignmentRecords().get(state.getCurrentAssignment());
			assignmentRecord.incrementKc();

			state.getCurrentAssignment().getVariantMatchingNpc(npc).ifPresent(variant -> {
				Record variantRecord = assignmentRecord.getVariantRecords().get(variant);
				if (variantRecord != null && variantRecord.getInteractors().stream().anyMatch(interactor -> interactor.equals(npc)))
				{
					variantRecord.incrementKc();
				}
			});

			assignmentRecord.getCustomRecords().stream()
				.filter(CustomRecord::isRecording)
				.forEach(Record::incrementKc);

		}
		state.getKcNpcQueue().clear();
	}

	private void divideXp(int xpToAllocate)
	{
		if (state.getXpNpcQueue().isEmpty())
		{
			return;
		}

		final Function<NPC, Integer> getSlayerXp = npc ->
			state.getCurrentAssignment().getVariantMatchingNpc(npc)
				.flatMap(Variant::getSlayerXp)
				.orElse(npcManager.getHealth(npc.getId()));

		final int npcXpTotal = state.getXpNpcQueue().stream().mapToInt(getSlayerXp::apply).sum();

		NPC npc = state.getXpNpcQueue().iterator().next();

		final int thisNpcsXpShare = xpToAllocate * (getSlayerXp.apply(npc) / npcXpTotal);
		xpToAllocate -= thisNpcsXpShare;

		AssignmentRecord assignmentRecord = state.getAssignmentRecords().get(state.getCurrentAssignment());
		assignmentRecord.addToXp(thisNpcsXpShare);

		state.getCurrentAssignment().getVariantMatchingNpc(npc).ifPresent(variant -> {
			Record variantRecord = assignmentRecord.getVariantRecords().get(variant);
			if (variantRecord != null)
			{
				variantRecord.addToXp(thisNpcsXpShare);
			}
		});

		assignmentRecord.getCustomRecords().stream()
			.filter(CustomRecord::isRecording)
			.forEach(customRecord ->
				customRecord.addToXp(thisNpcsXpShare));

		state.getXpNpcQueue().remove(npc);
		divideXp(xpToAllocate);
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

	private NPC getNpcFromInteraction(InteractingChanged event)
	{
		if (event.getSource() == client.getLocalPlayer() && event.getTarget() instanceof NPC)
		{
			return (NPC) event.getTarget();
		}
		else if (event.getSource() instanceof NPC && event.getTarget() == client.getLocalPlayer())
		{
			return (NPC) event.getSource();
		}
		return null;
	}
}
