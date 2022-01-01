package com.slayertracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;

import javax.inject.Inject;
import javax.swing.*;

import com.slayertracker.model.*;
import com.slayertracker.view.SlayerTrackerPanel;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
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

import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.*;

import static net.runelite.api.Skill.SLAYER;

@Slf4j
@PluginDescriptor(
        name = "Slayer Tracker"
)
public class SlayerTrackerPlugin extends Plugin implements PropertyChangeListener {
    //<editor-fold desc="Injectors">
    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private ConfigManager configManager;

    @Inject
    private ItemManager itemManager;

    @Inject
    private NPCManager npcManager;

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private ScheduledExecutorService executor;
    //</editor-fold>

    // *TERMINOLOGY*
    // Assignment: An assignment given by a Slayer Master, ie "Trolls", "Fire giants"
    // Variant: A subset of the Assignment which is commonly fought as a group, ie "Ice trolls", "Fire giant (level-104/109)"
    // Record: Either of the above as it's tracked by the plugin
    // Target Name: The exact in-game name of an NPC in an Assignment/Variant, ie "Ice troll male", "Ice troll female"
    // Interactor: An individual on-assignment NPC which is interacting with the player

    public static final String DATA_FOLDER_NAME = "slayer-tracker";
    public static final String DATA_FILE_NAME = "data.json";
    public static final File DATA_FOLDER;

    private Assignment currentAssignment;
    private HashMap<Assignment, AssignmentRecord> assignmentRecords = new HashMap<>();
    private final Set<NPC> xpShareInteractors = new HashSet<>();
    private int cachedXp = -1;

    public static Gson gson;

    static {
        DATA_FOLDER = new File(RuneLite.RUNELITE_DIR, DATA_FOLDER_NAME);
        DATA_FOLDER.mkdirs();
    }

    private SlayerTrackerPanel panel;

    @Override
    protected void startUp() {
        // Typically on game launch, prior to logging in

        gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(AssignmentRecord.class, (InstanceCreator<Record>) type -> new AssignmentRecord(this))
                .registerTypeAdapter(VariantRecord.class, (InstanceCreator<Record>) type -> new VariantRecord(this))
                .create();

        if (client.getGameState() == GameState.LOGGED_IN) {
            cachedXp = client.getSkillExperience(SLAYER);
        }

        panel = new SlayerTrackerPanel(itemManager);
        final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/slayer_icon.png");

        NavigationButton navButton = NavigationButton.builder()
                .panel(panel)
                .tooltip("Slayer Tracker")
                .icon(icon)
                .priority(5)
                .build();

        clientToolbar.addNavigation(navButton);
    }

    @Override
    protected void shutDown() {
        saveAssignmentRecordsToDisk();

        currentAssignment = null;
        xpShareInteractors.clear();
        cachedXp = -1;
    }

    @Subscribe
    public void onClientShutdown(ClientShutdown event) {
        event.waitFor(executor.submit(this::saveAssignmentRecordsToDisk));
    }

    private void loadAssignmentRecordsFromDisk() {
        try {
            // Ensure directory exists, then define data file
            DATA_FOLDER.mkdirs();
            File dataFile = new File(DATA_FOLDER, DATA_FILE_NAME);

            // If data file doesn't exist, create it
            if (!dataFile.exists()) {
                Writer writer = new FileWriter(dataFile);
                writer.write("");
                writer.close();
            }

            // Deserialize json from data file, as HashMap<Assignment, AssignmentRecord>
            assignmentRecords = gson.fromJson(new FileReader(dataFile), new TypeToken<HashMap<Assignment, AssignmentRecord>>() {}.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        updatePanel();
    }

    private void saveAssignmentRecordsToDisk() {
        try {
            File dataFile = new File(DATA_FOLDER, DATA_FILE_NAME);
            Writer writer = new FileWriter(dataFile);
            gson.toJson(assignmentRecords, writer);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        switch (event.getGameState()) {
            case HOPPING:
                log.info("HOPPING");

                // Save data to config
                saveAssignmentRecordsToDisk();
                xpShareInteractors.clear();

                updatePanel();
                break;
            case LOGGED_IN:
                log.info("LOGGED_IN (DOING NOTHING)");
                break;
            case LOGGING_IN:
                log.info("LOGGING_IN");

                // Set assignment from Slayer plugin config file
                Assignment.getAssignmentByName(getSlayerConfigAssignmentName()).ifPresent(assignment ->
                        this.currentAssignment = assignment);

                loadAssignmentRecordsFromDisk();

                // Build side panel
                updatePanel();
                break;
            case LOGIN_SCREEN:
                log.info("LOGIN SCREEN");

                // Clear data as if first boot
                xpShareInteractors.clear();
                cachedXp = -1;

                updatePanel();
                break;
        }
    }

    private void updatePanel() {
        clientThread.invokeLater(() ->
                SwingUtilities.invokeLater(() ->
                        panel.build(assignmentRecords)));
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (event.getGroup().equals(SlayerConfig.GROUP_NAME) && event.getKey().equals(SlayerConfig.TASK_NAME_KEY)) {
            currentAssignment = null;
            Assignment.getAssignmentByName(getSlayerConfigAssignmentName()).ifPresent(assignment ->
                    this.currentAssignment = assignment);

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
                    || interactor != client.getLocalPlayer().getInteracting()
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
                        log.info(assignmentRecord + "-DR: " + assignmentRecord.getHours());
                    });

            assignmentRecord.getVariantRecords().values().forEach(variantRecord ->
                    variantRecord.getInteractors().stream()
                            .filter(isNotInteracting)
                            .forEach(interactor -> {
                                variantRecord.addToHours(Duration.between(variantRecord.getStartInstant(), now));
                                variantRecord.setStartInstant(now);
                                log.info(variantRecord + "-DR: " + variantRecord.getHours());
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
        assignmentRecords.putIfAbsent(currentAssignment, new AssignmentRecord(currentAssignment, this));
        AssignmentRecord assignmentRecord = assignmentRecords.get(currentAssignment);
        // If this was the first interactor in the record, set start instant to now
        if (assignmentRecord.getInteractors().isEmpty()) {
            assignmentRecord.setStartInstant(now);
        }
        // Add the npc to the record's interactors
        assignmentRecord.getInteractors().add(npc);

        // Do the same as above for the Variant, if one exists
        currentAssignment.getVariantMatchingNpc(npc).ifPresent(variant -> {
            assignmentRecord.getVariantRecords().putIfAbsent(variant, new VariantRecord(variant, this));
            VariantRecord variantRecord = assignmentRecord.getVariantRecords().get(variant);
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
                    // Since coins have 0 HA value, use 1 * the coin item stack quantity
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
            // this is the initial xp sent on login
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

    // Recursively allocate xp to each killed monster in the queue
    // this will allow for safe rounding to whole xp amounts
    private void divideXp(int slayerXpDrop, Set<NPC> xpShareInteractors) {
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

    // TODO
    // Test Logging Out and Final Monster on task getting XP - perhaps don't clear xpInteractorQueue
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

    // True if formatted NPC name contains any of the Assignment's target names,
    // AND NPC has an Attack OR Pick action
    private static final BiPredicate<Assignment, NPC> isOnAssignment = (assignment, npc) ->
            assignment.getTargetNames().stream()
                    .anyMatch(npc.getTransformedComposition()
                            .getName()
                            .replace('\u00A0', ' ')
                            .toLowerCase()::contains)
                    && (ArrayUtils.contains(npc.getTransformedComposition().getActions(), "Attack")
                    || ArrayUtils.contains(npc.getTransformedComposition().getActions(), "Pick"));

    @Subscribe
    private void onCommandExecuted(CommandExecuted event) {
        switch (event.getCommand()) {
            case "ttt":
                // Print record to log - "::ttt"
                logInfo();
                break;
            case "ppp":
                saveAssignmentRecordsToDisk();
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
        assignmentRecords.forEach((type, assignmentRecord) -> {
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

    private void clearData() {
        log.warn("CLEARING ALL DATA");
        assignmentRecords.clear();
        updatePanel();
    }

    private String getSlayerConfigAssignmentName() {
        String assignmentName = configManager.getRSProfileConfiguration(SlayerConfig.GROUP_NAME, SlayerConfig.TASK_NAME_KEY);
        return assignmentName == null ? "" : assignmentName.toLowerCase();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        clientThread.invokeLater(() ->
                SwingUtilities.invokeLater(() ->
                        panel.build(assignmentRecords)));
    }

    @Provides
    SlayerTrackerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(SlayerTrackerConfig.class);
    }
}