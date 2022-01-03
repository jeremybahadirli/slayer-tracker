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
import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import com.slayertracker.controller.Controller;
import com.slayertracker.groups.Assignment;
import com.slayertracker.groups.Variant;
import com.slayertracker.records.AssignmentRecord;
import com.slayertracker.records.Record;
import com.slayertracker.records.RecordMap;
import com.slayertracker.views.SlayerTrackerPanel;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.RuneLite;
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

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import static net.runelite.api.Skill.SLAYER;

// TERMINOLOGY
// Assignment: An assignment given by a Slayer Master, ie "Trolls", "Fire giants"
// Variant: A subset of the Assignment which is fought together, ie "Ice trolls", "Fire giant (Catacombs)"
// Group: A group of monsters that is tracked together by the plugin. Both of the above are Groups
//        "Trolls" Assignment is a Group encompassing all trolls
//        "Ice trolls" Variant is a Group encompassing the trolls fought on the Fremennik Isles
// Record: The data tracked by the plugin for each Group. A Record contains the Player's kc, xp, etc. for a Group
// Target Name: The exact in-game name of a monster in a Group, ie "Ice troll male", "Ice troll female"
// Interactor: An individual on-assignment monster which is interacting with the player

// TODO
// Test logging out during interaction
// Last kill of task - kc and xp are counted but gp is not.
// Add all Variants (Category:Slayer monster)

// For each task,
// Add task weight for each master
// Add config entry to choose master
// record LastTask - Banshees 100
// if currentTask != lastTask,
// re-average in currentTask to slayer task average length
// lastTask = currentTask

// - TASK(time, xgp, weight) - All calculations based thereon
// - goodness = xgp / time
// - order all tasks by goodness, select only top good tasks allowing for point surplus
// - Block highest weighted below cutoff
// - Skip all others below cutoff

// Do some test calculations on the above to get xgp/time gain calibrated

@Slf4j
@PluginDescriptor(
        name = "Slayer Tracker"
)
public class SlayerTrackerPlugin extends Plugin {
    @Inject
    private Client client;
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
    private static final RecordMap<Assignment, AssignmentRecord> assignmentRecords = new RecordMap<>();
    private final Set<NPC> xpShareInteractors = new HashSet<>();
    private int cachedXp = -1;

    private Controller controller;

    @Override
    protected void startUp() {
        // Create side panel and controller
        SlayerTrackerPanel slayerTrackerPanel = new SlayerTrackerPanel(assignmentRecords, config, itemManager);
        controller = new Controller(assignmentRecords, slayerTrackerPanel);
        assignmentRecords.addPcl(controller);

        // Create button for side panel
        BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/slayer_icon.png");
        NavigationButton navButton = NavigationButton.builder()
                .panel(slayerTrackerPanel)
                .tooltip("Slayer Tracker")
                .icon(icon)
                .priority(5)
                .build();
        clientToolbar.addNavigation(navButton);

        // Create data folder on disk if it doesn't exist
        DATA_FOLDER.mkdirs();

        // Create gson instance for groups serialization.
        // Type adapters register SlayerTrackerPlugin as
        // a property change listener for records loaded from disk
        gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(AssignmentRecord.class, (InstanceCreator<Record>) type -> new AssignmentRecord(controller))
                .registerTypeAdapter(Record.class, (InstanceCreator<Record>) type -> new Record(controller))
                .create();

        // If already logged in on plugin startup, store current Slayer xp for xp drop calculation
        // If not logged in, Player will receive xp drop on login, so we will store it then
        if (client.getGameState() == GameState.LOGGED_IN) {
            cachedXp = client.getSkillExperience(SLAYER);
        }
    }

    @Override
    protected void shutDown() {
        saveRecordsToDisk();
    }

    @Subscribe
    public void onClientShutdown(ClientShutdown event) {
        // Ask client to allow us to save our groups to disk before shutting down.
        event.waitFor(executor.submit(this::saveRecordsToDisk));
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        switch (event.getGameState()) {
            case LOGGING_IN:
                // Set current assignment from Slayer Plugin config file
                Assignment.getAssignmentByName(
                                configManager.getRSProfileConfiguration(SlayerConfig.GROUP_NAME, SlayerConfig.TASK_NAME_KEY))
                        .ifPresent(assignment -> this.currentAssignment = assignment);
                // Set groups file name before loading the records
                // This will be remembered for saving after logout as well
                dataFileName = configManager.getRSProfileKey().split("\\.")[1] + ".json";
                loadRecordsFromDisk();
                break;
            case LOGIN_SCREEN:
                // If assignment records is not null, this is
                // upon logout, rather than client startup
                if (!assignmentRecords.isEmpty()) {
                    saveRecordsToDisk();
                }
                xpShareInteractors.clear();
                cachedXp = -1;
                break;
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        // If Slayer Plugin task changes
        if (event.getGroup().equals(SlayerConfig.GROUP_NAME) && event.getKey().equals(SlayerConfig.TASK_NAME_KEY)) {
            // Set current assignment to null, or the new value if it is valid
            currentAssignment = null;
            Assignment.getAssignmentByName(event.getNewValue()).ifPresent(assignment ->
                    this.currentAssignment = assignment);

            // Clear interactors and start times for all records
            assignmentRecords.values().forEach(assignmentRecord -> {
                assignmentRecord.getInteractors().clear();
                assignmentRecord.setStartInstant(null);
                assignmentRecord.getVariantRecords().values().forEach(variantRecord -> {
                    variantRecord.getInteractors().clear();
                    variantRecord.setStartInstant(null);
                });
            });
        }
    }

    private final Predicate<NPC> isNotInteracting = interactor ->
            !client.getNpcs().contains(interactor)
                    || client.getLocalPlayer().getInteracting() != interactor // For null case, != rather than !.equals
                    && (interactor.getInteracting() == null
                    || !interactor.getInteracting().equals(client.getLocalPlayer()));

    @Subscribe
    public void onGameTick(GameTick event) {
        if (currentAssignment == null) {
            return;
        }

        final Instant now = Instant.now();
        assignmentRecords.values().forEach(assignmentRecord -> {
            assignmentRecord.getInteractors().stream()
                    .filter(isNotInteracting)
                    .forEach(interactor -> {
                        assignmentRecord.addToHours(Duration.between(assignmentRecord.getStartInstant(), now));
                        assignmentRecord.setStartInstant(now);
                    });

            assignmentRecord.getVariantRecords().values().forEach(variantRecord ->
                    variantRecord.getInteractors().stream()
                            .filter(isNotInteracting)
                            .forEach(interactor -> {
                                variantRecord.addToHours(Duration.between(variantRecord.getStartInstant(), now));
                                variantRecord.setStartInstant(now);
                            }));
        });

        assignmentRecords.values().forEach(assignmentRecord -> {
            assignmentRecord.getInteractors().removeIf(isNotInteracting);
            assignmentRecord.getVariantRecords().values().forEach(variantRecord ->
                    variantRecord.getInteractors().removeIf(isNotInteracting));
        });
    }

    @Subscribe
    public void onInteractingChanged(InteractingChanged event) {
        if (currentAssignment == null) {
            return;
        }

        // Determine whether this is a Player-NPC interaction
        // Assign the source or target as appropriate to npc
        final NPC npc;
        if (event.getSource() == client.getLocalPlayer() && event.getTarget() instanceof NPC) {
            npc = (NPC) event.getTarget();
        } else if (event.getSource() instanceof NPC && event.getTarget() == client.getLocalPlayer()) {
            npc = (NPC) event.getSource();
        } else {
            return;
        }

        if (!isOnAssignment.test(currentAssignment, npc)) {
            return;
        }

        final Instant now = Instant.now();

        // If Assignment Record for this npc doesn't exist, create one
        assignmentRecords.putIfAbsent(currentAssignment, new AssignmentRecord(controller));
        AssignmentRecord assignmentRecord = assignmentRecords.get(currentAssignment);
        // If this was the first interactor in the record, set start instant to now
        if (assignmentRecord.getInteractors().isEmpty()) {
            assignmentRecord.setStartInstant(now);
        }
        // Add the npc to the record's interactors
        assignmentRecord.getInteractors().add(npc);

        // Do the same as above for the Variant, if one exists
        currentAssignment.getVariantMatchingNpc(npc).ifPresent(variant -> {
            assignmentRecord.getVariantRecords().putIfAbsent(variant, new Record(controller));
            Record variantRecord = assignmentRecord.getVariantRecords().get(variant);
            if (variantRecord.getInteractors().isEmpty()) {
                variantRecord.setStartInstant(now);
            }
            variantRecord.getInteractors().add(npc);
        });
    }

    @Subscribe
    public void onActorDeath(ActorDeath event) {
        if (currentAssignment == null || !assignmentRecords.containsKey(currentAssignment)) {
            return;
        }

        Actor actor = event.getActor();
        if (!(actor instanceof NPC)) {
            return;
        }

        NPC npc = (NPC) actor;

        if (assignmentRecords.get(currentAssignment).getInteractors().stream().anyMatch(interactor -> interactor.equals(npc))) {
            xpShareInteractors.add(npc);
            assignmentRecords.get(currentAssignment).incrementKc();
        }

        currentAssignment.getVariantMatchingNpc(npc).ifPresent(variant -> {
            if (assignmentRecords.get(currentAssignment).getVariantRecords().get(variant).getInteractors().stream().anyMatch(interactor -> interactor.equals(npc))) {
                assignmentRecords.get(currentAssignment).getVariantRecords().get(variant).incrementKc();
            }
        });
    }

    @Subscribe
    private void onNpcLootReceived(NpcLootReceived event) {
        if (currentAssignment == null) {
            return;
        }

        NPC npc = event.getNpc();
        if (!isOnAssignment.test(currentAssignment, npc)) {
            return;
        }

        final int lootGe = event.getItems().stream().mapToInt(itemStack ->
                        itemManager.getItemPrice(itemStack.getId()) * itemStack.getQuantity())
                .sum();

        final int lootHa = event.getItems().stream().mapToInt(itemStack -> {
                    // Since coins have 0 HA value, use (1*) the coin item stack quantity
                    if (itemStack.getId() == ItemID.COINS_995) {
                        return itemStack.getQuantity();
                    } else {
                        return itemManager.getItemComposition(itemStack.getId()).getHaPrice() * itemStack.getQuantity();
                    }
                })
                .sum();


        assignmentRecords.get(currentAssignment).addToGe(lootGe);
        assignmentRecords.get(currentAssignment).addToHa(lootHa);

        currentAssignment.getVariantMatchingNpc(npc).ifPresent(variant -> {
            assignmentRecords.get(currentAssignment).getVariantRecords().get(variant).addToGe(lootGe);
            assignmentRecords.get(currentAssignment).getVariantRecords().get(variant).addToHa(lootHa);
        });
    }

    @Subscribe
    private void onStatChanged(StatChanged event) {
        if (currentAssignment == null || event.getSkill() != SLAYER) {
            return;
        }

        int newSlayerXp = event.getXp();
        if (newSlayerXp <= cachedXp) {
            return;
        }

        if (cachedXp == -1) {
            // This xp drop is the initial xp sent on login
            cachedXp = newSlayerXp;
            return;
        }

        final int slayerXpDrop = newSlayerXp - cachedXp;
        cachedXp = newSlayerXp;

        divideXp(slayerXpDrop, xpShareInteractors);
    }

    // If variant has defined custom Slayer XP value, use that
    // Otherwise, use its HP
    private final Function<NPC, Integer> getSlayerXp = npc ->
            currentAssignment.getVariantMatchingNpc(npc).map(Variant::getSlayerXp)
                    .orElse(npcManager.getHealth(npc.getId()));

    private void divideXp(int slayerXpDrop, Set<NPC> xpShareInteractors) {
        // Recursively allocate xp to each killed monster in the queue
        // this will allow for safe rounding to whole xp amounts

        if (xpShareInteractors.isEmpty()) {
            return;
        }

        final int hpTotal = xpShareInteractors.stream().mapToInt(getSlayerXp::apply).sum();

        NPC npc = xpShareInteractors.iterator().next();
        final int thisNpcsXpShare = slayerXpDrop * getSlayerXp.apply(npc) / hpTotal;

        assignmentRecords.get(currentAssignment).addToXp(thisNpcsXpShare);

        currentAssignment.getVariantMatchingNpc(npc).ifPresent(variant ->
                assignmentRecords.get(currentAssignment).getVariantRecords().get(variant).addToXp(thisNpcsXpShare));

        slayerXpDrop -= thisNpcsXpShare;
        xpShareInteractors.remove(npc);
        divideXp(slayerXpDrop, xpShareInteractors);
    }

    private void loadRecordsFromDisk() {
        try {
            // Ensure directory exists, then define groups file
            File dataFile = new File(DATA_FOLDER, dataFileName);

            // If groups file doesn't exist, create it
            if (!dataFile.exists()) {
                Writer writer = new FileWriter(dataFile);
                writer.write("{}");
                writer.close();
            }

            // Deserialize json from groups file, as HashMap<Assignment, AssignmentRecord>
            RecordMap<Assignment, AssignmentRecord> dataFromDisk = gson.fromJson(new FileReader(dataFile), new TypeToken<RecordMap<Assignment, AssignmentRecord>>() {
            }.getType());
            assignmentRecords.clear();
            assignmentRecords.putAll(dataFromDisk);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveRecordsToDisk() {
        try {
            File dataFile = new File(DATA_FOLDER, dataFileName);
            Writer writer = new FileWriter(dataFile);
            log.info("SAVING DATA TO DISK");
            gson.toJson(assignmentRecords, writer);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final BiPredicate<Assignment, NPC> isOnAssignment = (assignment, npc) ->
            // True if formatted NPC name contains any of the Assignment's target names,
            // AND NPC has an Attack OR Pick action
            assignment.getTargetNames().stream().anyMatch(
                    npc.getTransformedComposition().getName().replace('\u00A0', ' ').toLowerCase()::contains)
                    && (ArrayUtils.contains(npc.getTransformedComposition().getActions(), "Attack")
                    || ArrayUtils.contains(npc.getTransformedComposition().getActions(), "Pick"));

    private void clearData() {
        log.warn("CLEARING ALL DATA");
        assignmentRecords.clear();
    }

    ////////////////////////////
    // DEBUG+BOILERPLATE ONLY //
    ////////////////////////////

    @Subscribe
    private void onCommandExecuted(CommandExecuted event) {
        switch (event.getCommand()) {
            case "ttt":
                // Print record to log - "::ttt"
                logInfo();
                break;
            case "ppp":
                log.info(assignmentRecords.values().toString());
                break;
            case "XXX":
                // Delete all records - "::XXX"
                clearData();
                break;
        }
    }

    private void logInfo() {
        log.info("ASSIGNMENT RECORDS");
        log.info(assignmentRecords.toString());
        assignmentRecords.values().forEach(assignmentRecord -> {
            log.info("INTERACTORS/START_TIME");
            log.info(assignmentRecord.getInteractors().toString());
            log.info(assignmentRecord.getStartInstant().toString());
            log.info("VARIANT RECORDS");
            log.info(assignmentRecord.getVariantRecords().toString());
            assignmentRecord.getVariantRecords().values().forEach(variantRecord -> {
                log.info("INTERACTORS/START_TIME");
                log.info(variantRecord.getInteractors().toString());
                log.info(variantRecord.getStartInstant().toString());
            });
        });
        log.info("XP SHARE INTERACTORS");
        log.info(String.valueOf(xpShareInteractors));
    }

    @Provides
    SlayerTrackerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(SlayerTrackerConfig.class);
    }
}