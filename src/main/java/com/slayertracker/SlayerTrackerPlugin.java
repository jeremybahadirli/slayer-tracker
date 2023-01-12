/*
 * Copyright (c) 2022, Jeremy Bahadirli <https://github.com/jeremybahadirli>
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
package com.slayertracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import com.slayertracker.groups.Assignment;
import com.slayertracker.groups.Variant;
import com.slayertracker.records.AssignmentRecord;
import com.slayertracker.records.CustomRecord;
import com.slayertracker.records.CustomRecordSet;
import com.slayertracker.records.Record;
import com.slayertracker.records.RecordMap;
import com.slayertracker.views.SlayerTrackerPanel;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemID;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ClientShutdown;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.NPCManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.slayer.SlayerConfig;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.lang3.ArrayUtils;

/*
 * DEVELOPMENT STARTED 12/05/21
 *
 * TERMINOLOGY
 * Assignment:		An assignment given by a Slayer Master, ie "Trolls", "Fire giants".
 * Variant:			A subset of the Assignment which is fought together, ie "Ice trolls", "Fire giant (Catacombs)".
 * Group:			A group of monsters that is tracked together by the plugin. Both of the above are Groups.
 *						"Trolls" Assignment is a Group encompassing all trolls.
 *						"Ice trolls" Variant is a Group encompassing the trolls fought on the Fremennik Isles.
 * Record:			The data tracked by the plugin for each Group. A Record contains the Player's kc, xp, etc. for a Group.
 * Target Name:		The exact in-game name of a monster in a Group, ie "Ice troll male", "Ice troll female".
 * Interactor:		An individual on-assignment monster which is interacting with the player.
 * Combat Instant: 	The time at which interaction began for a record.
 * 						If there are multiple interactors for a given record,
 * 						Combat Instant is reset each time interaction with an individual ends (kill or otherwise).
 * TODO
 * Initial Release (minus Analysis):
 * Test logging out during interaction
 * Last kill of task - kc and xp are counted but gp is not.
 * RecordManager?
 *
 * Analysis:
 * Add task weight and average quantity for each task, with extensions as necessary
 * To config, add:
 * 		Slayer Master combo box
 *		Active task extension checkboxes
 * To Side Panel, add Slayer Master view combo box, with option for "All"
 */

@Slf4j
@PluginDescriptor(
	name = "Slayer Tracker"
)
public class SlayerTrackerPlugin extends Plugin implements PropertyChangeListener
{
	@Inject
	private Client client;
	@Inject
	private ClientThread clientThread;
	@Inject
	private ConfigManager configManager;
	@Inject
	private SlayerTrackerConfig config;
	@Inject
	private ItemManager itemManager;
	@Inject
	private NPCManager npcManager;
	@Inject
	private ClientToolbar clientToolbar;
	@Inject
	private ScheduledExecutorService executor;

	public static final String DATA_FOLDER_NAME = "slayer-tracker";
	public static final File DATA_FOLDER = new File(RuneLite.RUNELITE_DIR, DATA_FOLDER_NAME);
	private String dataFileName;
	private Gson gson;

	private Assignment currentAssignment;
	private final RecordMap<Assignment, AssignmentRecord> assignmentRecords = new RecordMap<>(this);
	private final Set<NPC> xpShareInteractors = new HashSet<>();
	private int cachedXp = -1;

	private SlayerTrackerPanel slayerTrackerPanel;

	private boolean loggingIn = false;

	@Override
	protected void startUp()
	{
		// Create side panel
		slayerTrackerPanel = new SlayerTrackerPanel(assignmentRecords, config, itemManager);

		// Create button for side panel
		BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/slayer_icon.png");
		NavigationButton navButton = NavigationButton.builder()
			.panel(slayerTrackerPanel)
			.tooltip("Slayer Tracker")
			.icon(icon)
			.priority(5)
			.build();
		clientToolbar.addNavigation(navButton);

		// GSON serializes record data to JSON for disk storage
		gson = new GsonBuilder()
			// Only serialize fields with @Expose
			.excludeFieldsWithoutExposeAnnotation()
			// Save as human-readable JSON (newlines/tabs)
			.setPrettyPrinting()
			// When reconstructing records from JSON, apply property change listeners
			.registerTypeAdapter(AssignmentRecord.class, (InstanceCreator<Record>) type -> new AssignmentRecord(this))
			.registerTypeAdapter(RecordMap.class, (InstanceCreator<RecordMap<?, ? extends Record>>) type -> new RecordMap<>(this))
			.registerTypeAdapter(CustomRecordSet.class, (InstanceCreator<CustomRecordSet<CustomRecord>>) type -> new CustomRecordSet<>(this))
			// GSON doesn't recognize Instant, so serialize/deserialize as a long
			.registerTypeAdapter(Instant.class, (JsonSerializer<Instant>) (instant, type, context) ->
				new JsonPrimitive(instant.getEpochSecond()))
			.registerTypeAdapter(Instant.class, (JsonDeserializer<Instant>) (json, type, context) ->
				Instant.ofEpochSecond(json.getAsLong()))
			.create();

		// If already logged in on plugin startup, store current Slayer xp for xp drop calculation
		// If not logged in, Player will receive xp drop on login, so we will store it then
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			cachedXp = client.getSkillExperience(Skill.SLAYER);
		}
	}

	@Override
	protected void shutDown()
	{
		saveRecordsToDisk();
	}

	@Subscribe
	public void onClientShutdown(ClientShutdown event)
	{
		// Ask client to allow us to save our groups to disk before shutting down.
		event.waitFor(executor.submit(this::saveRecordsToDisk));
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		switch (event.getGameState())
		{
			case LOGGING_IN:
				loggingIn = true;
				break;
			case LOGGED_IN:
				// LOGGED_IN can execute while loading chunks in game, so
				// only proceed if LOGGING_IN ran prior to this
				if (!loggingIn)
				{
					return;
				}
				loggingIn = false;

				// Set current assignment from Slayer Plugin config file
				Assignment.getAssignmentByName(
						configManager.getRSProfileConfiguration(SlayerConfig.GROUP_NAME, SlayerConfig.TASK_NAME_KEY))
					.ifPresent(assignment -> this.currentAssignment = assignment);

				// Set data file name
				// This will be remembered for saving after logout
				dataFileName = configManager.getRSProfileKey().split("\\.")[1] + ".json";
				loadRecordsFromDisk();
				break;
			case LOGIN_SCREEN:
				saveRecordsToDisk();

				// xpShareInteractors could theoretically retain an NPC
				// if the player logs out at exactly the right instant
				xpShareInteractors.clear();
				// Reset slayer xp
				cachedXp = -1;
				slayerTrackerPanel.getRecordingModePanel().setContinuousRecording(false);
				break;
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		switch (event.getGroup())
		{
			case SlayerTrackerConfig.GROUP_NAME:
				// User has changed loot unit in the settings. Update the Side Panel
				if (event.getKey().equals(SlayerTrackerConfig.LOOT_UNIT_KEY))
				{
					clientThread.invokeLater(() ->
						SwingUtilities.invokeLater(() ->
							slayerTrackerPanel.update()));
				}
				break;
			case SlayerConfig.GROUP_NAME:
				// Slayer Plugin task name has changed
				if (event.getKey().equals(SlayerConfig.TASK_NAME_KEY))
				{
					// Set current assignment to null, or the new value if it is valid
					currentAssignment = null;
					Assignment.getAssignmentByName(event.getNewValue()).ifPresent(assignment ->
						this.currentAssignment = assignment);

					// Clear interactors for all records, as no more active interactors will be on-task
					assignmentRecords.values().forEach(assignmentRecord -> {
						assignmentRecord.getInteractors().clear();
						assignmentRecord.getVariantRecords().values().forEach(variantRecord -> variantRecord.getInteractors().clear());
						assignmentRecord.getCustomRecords().forEach(customRecord -> customRecord.getInteractors().clear());
					});
				}
				break;
		}
	}

	// True if no Player-NPC interaction exists with the given NPC
	private final Predicate<NPC> isNotInteracting = interactor ->
		!client.getNpcs().contains(interactor)
			|| client.getLocalPlayer() == null
			|| client.getLocalPlayer().getInteracting() != interactor
			&& interactor.getInteracting() != client.getLocalPlayer();

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (currentAssignment == null)
		{
			return;
		}

		// When an interactor is no longer interacting, we have just ended combat (kill or ran away).
		// Log the duration from the last combat instant to the record.
		// In case there are other simultaneous interactors, set the last combat instant to now,
		// so the next interaction time is logged appropriately.
		final Instant now = Instant.now();
		assignmentRecords.values().forEach(assignmentRecord -> {
			assignmentRecord.getInteractors().stream()
				.filter(isNotInteracting)
				.forEach(interactor -> {
					assignmentRecord.addToHours(Duration.between(assignmentRecord.getCombatInstant(), now));
					assignmentRecord.setCombatInstant(now);
				});

			assignmentRecord.getVariantRecords().values().forEach(variantRecord ->
				variantRecord.getInteractors().stream()
					.filter(isNotInteracting)
					.forEach(interactor -> {
						variantRecord.addToHours(Duration.between(variantRecord.getCombatInstant(), now));
						variantRecord.setCombatInstant(now);
					}));

			assignmentRecord.getCustomRecords().forEach(customRecord ->
				customRecord.getInteractors().stream()
					.filter(isNotInteracting)
					.forEach(interactor -> {
						customRecord.addToHours(Duration.between(customRecord.getCombatInstant(), now));
						customRecord.setCombatInstant(now);
					}));
		});

		// Remove no-longer-interacting interactors from records' interactor sets
		assignmentRecords.values().forEach(assignmentRecord -> {
			assignmentRecord.getInteractors().removeIf(isNotInteracting);

			assignmentRecord.getVariantRecords().values().forEach(variantRecord ->
				variantRecord.getInteractors().removeIf(isNotInteracting));

			assignmentRecord.getCustomRecords().forEach(customRecord ->
				customRecord.getInteractors().removeIf(isNotInteracting));
		});
	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged event)
	{
		if (currentAssignment == null)
		{
			return;
		}

		// Determine whether this is a Player-NPC interaction
		// Assign the source or target as appropriate to NPC
		final NPC npc;
		if (event.getSource() == client.getLocalPlayer() && event.getTarget() instanceof NPC)
		{
			npc = (NPC) event.getTarget();
		}
		else if (event.getSource() instanceof NPC && event.getTarget() == client.getLocalPlayer())
		{
			npc = (NPC) event.getSource();
		}
		else
		{
			return;
		}

		if (!isOnAssignment.test(currentAssignment, npc))
		{
			return;
		}

		final Instant now = Instant.now();

		slayerTrackerPanel.getRecordingModePanel().setContinuousRecording(slayerTrackerPanel.getRecordingModePanel().isContinuousRecordingMode());

		// If Assignment Record for this NPC doesn't exist, create one
		assignmentRecords.putIfAbsent(currentAssignment, new AssignmentRecord(this));
		AssignmentRecord assignmentRecord = assignmentRecords.get(currentAssignment);

		// If this is the first interactor in the record and recording mode is In Combat, set last combat instant to now
		if ((!slayerTrackerPanel.getRecordingModePanel().isContinuousRecordingMode()
			|| slayerTrackerPanel.getRecordingModePanel().getContinuousRecordingStartInstant().isAfter(assignmentRecord.getCombatInstant()))
			&& assignmentRecord.getInteractors().isEmpty())
		{
			assignmentRecord.setCombatInstant(now);
		}
		// Add the NPC to the record's interactors
		assignmentRecord.getInteractors().add(npc);

		// Do the same as above for the Variant, if one exists
		currentAssignment.getVariantMatchingNpc(npc).ifPresent(variant -> {
			assignmentRecord.getVariantRecords().putIfAbsent(variant, new Record());
			Record variantRecord = assignmentRecord.getVariantRecords().get(variant);
			if ((!slayerTrackerPanel.getRecordingModePanel().isContinuousRecordingMode()
				|| slayerTrackerPanel.getRecordingModePanel().getContinuousRecordingStartInstant().isAfter(variantRecord.getCombatInstant()))
				&& variantRecord.getInteractors().isEmpty())
			{
				variantRecord.setCombatInstant(now);
			}
			variantRecord.getInteractors().add(npc);
		});

		// Do the same as above for any recording Custom records
		assignmentRecord.getCustomRecords().stream()
			.filter(CustomRecord::isRecording)
			.forEach(customRecord -> {
				if ((!slayerTrackerPanel.getRecordingModePanel().isContinuousRecordingMode()
					|| slayerTrackerPanel.getRecordingModePanel().getContinuousRecordingStartInstant().isAfter(customRecord.getCombatInstant()))
					&& customRecord.getInteractors().isEmpty())
				{
					customRecord.setCombatInstant(now);
				}
				customRecord.getInteractors().add(npc);
			});
	}

	@Subscribe
	public void onActorDeath(ActorDeath event)
	{
		if (currentAssignment == null || !assignmentRecords.containsKey(currentAssignment))
		{
			return;
		}

		// Only proceed if the dead actor was an NPC
		if (!(event.getActor() instanceof NPC))
		{
			return;
		}

		NPC npc = (NPC) event.getActor();

		// Only proceed if the dead NPC is in the assignment record's interactors
		if (assignmentRecords.get(currentAssignment).getInteractors().stream().noneMatch(interactor -> interactor.equals(npc)))
		{
			return;
		}

		slayerTrackerPanel.getRecordingModePanel().setContinuousRecording(slayerTrackerPanel.getRecordingModePanel().isContinuousRecordingMode());

		// Add NPC to set of those who will be allotted xp from the next xp drop
		xpShareInteractors.add(npc);

		// Increment kc in assignment record
		AssignmentRecord assignmentRecord = assignmentRecords.get(currentAssignment);
		assignmentRecord.incrementKc();

		// Increment kc in any variant record which contains this NPC in its interactors
		currentAssignment.getVariantMatchingNpc(npc).ifPresent(variant -> {
			Record variantRecord = assignmentRecord.getVariantRecords().get(variant);
			if (variantRecord != null && variantRecord.getInteractors().stream().anyMatch(interactor -> interactor.equals(npc)))
			{
				variantRecord.incrementKc();
			}
		});

		// Increment kc in any recording custom records
		assignmentRecord.getCustomRecords().stream()
			.filter(CustomRecord::isRecording)
			.forEach(Record::incrementKc);
	}

	@Subscribe
	private void onNpcLootReceived(NpcLootReceived event)
	{
		if (currentAssignment == null || !assignmentRecords.containsKey(currentAssignment))
		{
			return;
		}

		NPC npc = event.getNpc();
		if (!isOnAssignment.test(currentAssignment, npc))
		{
			return;
		}

		// Sum the GE item price of each dropped item
		final int lootGe = event.getItems().stream().mapToInt(itemStack ->
				itemManager.getItemPrice(itemStack.getId()) * itemStack.getQuantity())
			.sum();

		// Sum the HA item price of each dropped item
		final int lootHa = event.getItems().stream().mapToInt(itemStack -> {
				// Since coins have 0 HA value instead of 1, use the coin item stack quantity as its value
				if (itemStack.getId() == ItemID.COINS_995)
				{
					return itemStack.getQuantity();
				}
				else
				{
					return itemManager.getItemComposition(itemStack.getId()).getHaPrice() * itemStack.getQuantity();
				}
			})
			.sum();

		// Add GE/HA values to assignment record
		AssignmentRecord assignmentRecord = assignmentRecords.get(currentAssignment);
		assignmentRecord.addToGe(lootGe);
		assignmentRecord.addToHa(lootHa);

		// Add GE/HA values to the variant record, if one exists
		currentAssignment.getVariantMatchingNpc(npc).ifPresent(variant -> {
			Record variantRecord = assignmentRecord.getVariantRecords().get(variant);
			if (variantRecord != null)
			{
				variantRecord.addToGe(lootGe);
				variantRecord.addToHa(lootHa);
			}
		});

		// Add GE/HA values to any recording custom records
		assignmentRecord.getCustomRecords().stream()
			.filter(CustomRecord::isRecording)
			.forEach(customRecord -> {
				customRecord.addToGe(lootGe);
				customRecord.addToHa(lootHa);
			});
	}

	@Subscribe
	private void onStatChanged(StatChanged event)
	{
		if (event.getSkill() != Skill.SLAYER)
		{
			return;
		}

		int newSlayerXp = event.getXp();
		if (newSlayerXp <= cachedXp)
		{
			return;
		}

		// If cached xp is its initial value of -1, this is the initial slayer xp
		// given on login. Store that as the cached xp for future xp drop calculations
		if (cachedXp == -1)
		{
			cachedXp = newSlayerXp;
			return;
		}

		// These checks need to be below the above initial login xp drop check
		if (currentAssignment == null || !assignmentRecords.containsKey(currentAssignment))
		{
			return;
		}

		// This xp drop is the player's new xp minus the stored xp
		final int slayerXpDrop = newSlayerXp - cachedXp;
		cachedXp = newSlayerXp;

		divideXp(slayerXpDrop, xpShareInteractors);
	}

	// Determines the slayer xp value of a given NPC
	// If variant has defined custom Slayer XP value, use that, otherwise use NPC's HP
	private final Function<NPC, Integer> getSlayerXp = npc ->
		currentAssignment.getVariantMatchingNpc(npc).map(Variant::getSlayerXp)
			.orElse(npcManager.getHealth(npc.getId()));

	private void divideXp(int slayerXpDrop, Set<NPC> xpShareInteractors)
	{
		// Recursively allocate xp to each monster in the queue for this xp drop
		// This method will allow for safe rounding to whole xp amounts

		// Final case to break recursion
		if (xpShareInteractors.isEmpty())
		{
			return;
		}

		// The sum of the slayer xp value of each NPC remaining in the queue
		final int npcXpTotal = xpShareInteractors.stream().mapToInt(getSlayerXp::apply).sum();

		// Choose the next NPC to allocate xp to
		NPC npc = xpShareInteractors.iterator().next();

		// Amount allocated is equal to the proportion of this NPC's xp value to the total value of the queue,
		// times the xp received, rounded to an integer. Then, remove the amount allocated from the total
		// Calculating in this way allows for distributing xp from the actual amount received,
		// without fear of error in the total caused by the rounding of the individual NPCs
		final int thisNpcsXpShare = slayerXpDrop * getSlayerXp.apply(npc) / npcXpTotal;
		slayerXpDrop -= thisNpcsXpShare;

		// Add the xp share to the assignment record
		AssignmentRecord assignmentRecord = assignmentRecords.get(currentAssignment);
		assignmentRecord.addToXp(thisNpcsXpShare);

		// Add the xp share to the variant record if one exists
		currentAssignment.getVariantMatchingNpc(npc).ifPresent(variant -> {
			Record variantRecord = assignmentRecord.getVariantRecords().get(variant);
			if (variantRecord != null)
			{
				variantRecord.addToXp(thisNpcsXpShare);
			}
		});

		// Add the xp share to any recording custom records
		assignmentRecord.getCustomRecords().stream()
			.filter(CustomRecord::isRecording)
			.forEach(customRecord ->
				customRecord.addToXp(thisNpcsXpShare));

		// Remove this NPC from the queue and recursively repeat with every NPC in the queue
		xpShareInteractors.remove(npc);
		divideXp(slayerXpDrop, xpShareInteractors);
	}

	private void loadRecordsFromDisk()
	{
		try
		{
			if (dataFileName == null)
			{
				return;
			}
			File dataFile = getDataFile();

			// If data file doesn't exist, create one with an empty assignment RecordMap
			if (!dataFile.exists())
			{
				Writer writer = new FileWriter(dataFile);
				writer.write("{}");
				writer.close();
			}

			// Deserialize json from data file, as HashMap<Assignment, AssignmentRecord>
			// then copy it into assignmentRecords
			// Must copy it in, because the ui has already received this RecordMap instance
			HashMap<Assignment, AssignmentRecord> dataFromDisk = gson.fromJson(new FileReader(dataFile), new TypeToken<HashMap<Assignment, AssignmentRecord>>()
			{
			}.getType());
			assignmentRecords.clear();
			assignmentRecords.putAll(dataFromDisk);
		}
		catch (Exception e)
		{
			slayerTrackerPanel.displayFileError();
			e.printStackTrace();
		}
	}

	private void saveRecordsToDisk()
	{
		try
		{
			if (dataFileName == null)
			{
				return;
			}
			File dataFile = getDataFile();

			// Serialize assignmentRecords to json and write to the data file
			Writer writer = new FileWriter(dataFile);
			gson.toJson(assignmentRecords, writer);
			writer.flush();
			writer.close();
		}
		catch (Exception e)
		{
			// If data folder could not be created, display user-facing error in the Side Panel
			slayerTrackerPanel.displayFileError();
			e.printStackTrace();
		}
	}

	private File getDataFile() throws IOException
	{
		// Throw exception if data folder could not be created
		if (!DATA_FOLDER.exists() && !DATA_FOLDER.mkdirs())
		{
			throw new IOException("Could not create data folder: .runelite/slayer-tracker");
		}
		return new File(DATA_FOLDER, dataFileName);
	}

	// True if formatted NPC name contains any of the Assignment's target names,
	// AND NPC has an Attack OR Pick action (pick action is for Zygomite)
	private static final BiPredicate<Assignment, NPC> isOnAssignment = (assignment, npc) ->
		npc.getTransformedComposition() != null
			&& assignment.getTargetNames().stream()
			.anyMatch(npc.getTransformedComposition().getName().replace('\u00A0', ' ').toLowerCase()::contains)
			&& (ArrayUtils.contains(npc.getTransformedComposition().getActions(), "Attack")
			|| ArrayUtils.contains(npc.getTransformedComposition().getActions(), "Pick"));

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		// Side panel update is triggered by an updated value in an assignment record,
		// or an assignment, variant, or custom record map
		clientThread.invokeLater(() ->
			SwingUtilities.invokeLater(() ->
				slayerTrackerPanel.update()));
	}

	@Provides
	SlayerTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SlayerTrackerConfig.class);
	}
}