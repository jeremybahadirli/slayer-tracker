package com.slayertracker;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
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
import org.apache.commons.lang3.tuple.MutablePair;

import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;

import static net.runelite.api.Skill.SLAYER;

@Slf4j
@PluginDescriptor(
        name = "Slayer Tracker"
)
public class SlayerTrackerPlugin extends Plugin {
    //<editor-fold desc="Injectors">
    @Inject
    private Client client;

    @Inject
    private ConfigManager configManager;

    @Inject
    private ItemManager itemManager;

    @Inject
    private NPCManager npcManager;

    @Inject
    private ClientToolbar clientToolbar;

    //</editor-fold>

    // *TERMINOLOGY*
    // Assignment: The assignment given by a Slayer Master, ie "Trolls", "Fire giants"
    // Variant: A subset of the Assignment which is commonly fought as a group, ie "Ice trolls", "Fire giant (level-104/109)"
    // Record: General term for either of the above as it's tracked by the plugin
    // Target Name: The exact in-game name of an NPC in an Assignment/Variant, ie "Ice troll male", "Ice troll female"
    // Interactor: An individual on-assignment NPC which is interacting with the player

    private Assignment assignment;
    private final HashMap<Enum<?>, MutablePair<Set<NPC>, Instant>> recordToInteractorsAndTime = new HashMap<>();
    private final Set<NPC> xpShareInteractors = new HashSet<>();
    private int cachedXp = -1;

    private final Predicate<NPC> isNotInteracting = interactor ->
            !client.getNpcs().contains(interactor)
                    || interactor != client.getLocalPlayer().getInteracting()
                    && (interactor.getInteracting() == null
                    || !interactor.getInteracting().equals(client.getLocalPlayer()));

    @Override
    protected void startUp() {
        if (client.getGameState() == GameState.LOGGED_IN) {
            cachedXp = client.getSkillExperience(SLAYER);
        }

        SlayerTrackerPanel panel = new SlayerTrackerPanel(itemManager);
        final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "slayer_icon.png");

        NavigationButton navButton = NavigationButton.builder()
                .panel(panel)
                .tooltip("Slayer Tracker")
                .icon(icon)
                .priority(90)
                .build();

        clientToolbar.addNavigation(navButton);
    }

    @Override
    protected void shutDown() {
        clearAssignmentAndRecords();
        xpShareInteractors.clear();
        cachedXp = -1;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        switch (event.getGameState()) {
            case HOPPING:
            case LOGGING_IN:
                clearAssignmentAndRecords();
                Assignment.getAssignmentByName(getSlayerConfigAssignmentName()).ifPresent(
                        assignment -> this.assignment = assignment);
                xpShareInteractors.clear();
                cachedXp = -1;
                break;
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (event.getGroup().equals(SlayerConfig.GROUP_NAME) && event.getKey().equals(SlayerConfig.TASK_NAME_KEY)) {
            clearAssignmentAndRecords();
            Assignment.getAssignmentByName(getSlayerConfigAssignmentName()).ifPresent(
                    assignment -> this.assignment = assignment);
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (assignment == null) {
            return;
        }

        final Instant now = Instant.now();
        recordToInteractorsAndTime.forEach((record, interactorsAndTime) ->
                interactorsAndTime.left.stream()
                        .filter(isNotInteracting)
                        .forEach(interactor -> {
                            setRecordDuration(record, getRecordDuration(record).plus(Duration.between(interactorsAndTime.right, now)));
                            interactorsAndTime.right = now;
                        })
        );

        recordToInteractorsAndTime.values().forEach(interactorsAndTime ->
                interactorsAndTime.left.removeIf(isNotInteracting));

        recordToInteractorsAndTime.values().removeIf(interactorsAndTime -> interactorsAndTime.left.isEmpty());
    }

    @Subscribe
    public void onInteractingChanged(InteractingChanged event) {
        if (assignment == null) {
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

        if (!isOnAssignment(assignment, npc)) {
            return;
        }

        final Instant now = Instant.now();
        // If it doesn't exist, create a record for the Assignment, and add the npc
        recordToInteractorsAndTime.putIfAbsent(assignment, new MutablePair<>(new HashSet<>(), now));
        recordToInteractorsAndTime.get(assignment).left.add(npc);

        // Do the same if Variant exists
        assignment.getVariantMatchingNpc(npc).ifPresent(variant -> {
            recordToInteractorsAndTime.putIfAbsent(variant, new MutablePair<>(new HashSet<>(), now));
            recordToInteractorsAndTime.get(variant).left.add(npc);
        });
    }

    @Subscribe
    public void onActorDeath(ActorDeath event) {
        if (assignment == null) {
            return;
        }

        Actor actor = event.getActor();
        if (!(actor instanceof NPC)) {
            return;
        }

        NPC npc = (NPC) actor;

        recordToInteractorsAndTime.forEach((record, interactorsAndTime) -> {
            if (interactorsAndTime.left.stream().anyMatch(interactor -> interactor.equals(npc))) {
                xpShareInteractors.add(npc);
                setRecordKc(record, getRecordKc(record) + 1);
            }
        });
    }

    @Subscribe
    private void onNpcLootReceived(NpcLootReceived event) {
        if (assignment == null) {
            return;
        }

        NPC npc = event.getNpc();
        if (!isOnAssignment(assignment, npc)) {
            return;
        }

        final int ge = event.getItems().stream().mapToInt(itemStack ->
                        itemManager.getItemPrice(itemStack.getId()) * itemStack.getQuantity())
                .sum();

        final int ha = event.getItems().stream().mapToInt(itemStack ->
                        itemManager.getItemComposition(itemStack.getId()).getHaPrice() * itemStack.getQuantity())
                .sum();

        setRecordGe(assignment, getRecordGe(assignment) + ge);
        setRecordHa(assignment, getRecordHa(assignment) + ha);

        assignment.getVariantMatchingNpc(npc).ifPresent(variant -> {
            setRecordGe(variant, getRecordGe(variant) + ge);
            setRecordHa(variant, getRecordHa(variant) + ha);
        });
    }

    @Subscribe
    private void onStatChanged(StatChanged event) {
        if (assignment == null || event.getSkill() != SLAYER) {
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

        divideXp(assignment, slayerXpDrop, xpShareInteractors);
    }

    // Recursively allocate xp to each killed monster in the queue
    // this will allow for safe rounding to whole xp amounts
    private void divideXp(Assignment assignment, int slayerXpDrop, Set<NPC> xpShareInteractors) {
        if (xpShareInteractors.isEmpty()) {
            return;
        }

        final int hpTotal = xpShareInteractors.stream().mapToInt(npc ->
                        npcManager.getHealth(npc.getId()))
                .sum();

        NPC npc = xpShareInteractors.iterator().next();
        final int thisNpcsXpShare = slayerXpDrop * npcManager.getHealth(npc.getId()) / hpTotal;

        setRecordXp(assignment, getRecordXp(assignment) + thisNpcsXpShare);

        assignment.getVariantMatchingNpc(npc).ifPresent(variant ->
                setRecordXp(variant, getRecordXp(variant) + thisNpcsXpShare)
        );

        slayerXpDrop -= thisNpcsXpShare;
        xpShareInteractors.remove(npc);
        divideXp(assignment, slayerXpDrop, xpShareInteractors);
    }

    // TODO
    // Config File
    // Side Panel (ugh)
    // Test Logging Out and Final Monster on task getting XP - perhaps don't clear xpInteractorQueue
    // Add all Variants (Category:Slayer monster)
    // Write time record on shutdown

    private static boolean isOnAssignment(Assignment assignment, NPC npc) {
        final NPCComposition composition = npc.getTransformedComposition();
        if (composition == null) {
            return false;
        }

        // Format non-breaking space, convert to lower case for comparison
        final String npcNameFormatted = composition.getName()
                .replace('\u00A0', ' ')
                .toLowerCase();

        return assignment.getTargetNames().stream().anyMatch(npcNameFormatted::contains)
                && (ArrayUtils.contains(composition.getActions(), "Attack")
                || ArrayUtils.contains(composition.getActions(), "Pick"));
    }

    //<editor-fold desc="Debug">
    @Subscribe
    private void onCommandExecuted(CommandExecuted event) {
        switch (event.getCommand()) {
            case "ttt":
                // Print record to log - "::ttt" or "::ttt TROLLS"
                if (event.getArguments().length == 0) {
                    logInfo(assignment);
                } else {
                    Assignment.getAssignmentByName(event.getArguments()[0]).ifPresent(this::logInfo);
                }
                break;
            case "ddd":
                // Delete record - "::ddd FIRE_GIANT_WEAK"
                Assignment.getAssignmentByName(event.getArguments()[0]).ifPresent(this::removeRecord);
                break;
            case "XXX":
                // Delete all records - "::XXX"
                clearConfig();
                break;
        }
    }

    private void logInfo(Enum<?> record) {
        if (record == null) {
            return;
        }

        try {
            Arrays.stream(((Assignment) record).getVariants()).forEach(this::logInfo);
        } catch (ClassCastException ignored) {
        }

        float hours = getRecordDuration(record).getSeconds() / 3600f;
        log.info("");
        log.info("nm: " + record);
        log.info("tm: " + getRecordDuration(record));
        log.info("kc: " + getRecordKc(record));
        log.info("xp: " + getRecordXp(record));
        log.info("ge: " + getRecordGe(record));
        log.info("ha: " + getRecordHa(record));
        log.info("kc/h: " + Math.round(getRecordKc(record) / hours));
        log.info("xp/h: " + Math.round(getRecordXp(record) / hours));
        log.info("ge/h: " + Math.round(getRecordGe(record) / hours));
        log.info("ha/h: " + Math.round(getRecordHa(record) / hours));
        log.info(String.valueOf(recordToInteractorsAndTime));
        log.info(String.valueOf(xpShareInteractors));
    }
    //</editor-fold>

    private void clearAssignmentAndRecords() {
        assignment = null;
        recordToInteractorsAndTime.clear();
    }

    private void clearConfig() {
        log.warn("CLEARING ALL CONFIG ENTRIES");

        configManager.getConfigurationKeys("slayertracker").forEach(key -> {
            String[] splitString = key.split("\\.");
            configManager.unsetRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, splitString[splitString.length - 1]);
            configManager.unsetConfiguration(SlayerTrackerConfig.GROUP_NAME, splitString[splitString.length - 1]);
        });
    }

    private void removeRecord(Enum<?> record) {
        log.warn("REMOVING RECORD: " + record);
        configManager.unsetRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.TIME_KEY + record);
        configManager.unsetRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.KC_KEY + record);
        configManager.unsetRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.XP_KEY + record);
        configManager.unsetRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.GE_KEY + record);
        configManager.unsetRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.HA_KEY + record);
    }

    //<editor-fold desc="Config Getters/Setters">
    private String getSlayerConfigAssignmentName() {
        String assignmentName = configManager.getRSProfileConfiguration(SlayerConfig.GROUP_NAME, SlayerConfig.TASK_NAME_KEY);
        return assignmentName == null ? "" : assignmentName.toLowerCase();
    }

    public Duration getRecordDuration(Enum<?> record) {
        String durationString = configManager.getRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.TIME_KEY + record);
        if (durationString != null) {
            return Duration.parse(durationString);
        } else {
            return Duration.ZERO;
        }
    }

    public void setRecordDuration(Enum<?> record, Duration duration) {
        if (record != null) {
            configManager.setRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.TIME_KEY + record, duration.toString());
        }
    }

    public int getRecordKc(Enum<?> record) {
        Integer kc = configManager.getRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.KC_KEY + record, int.class);
        return kc == null ? 0 : kc;
    }

    public void setRecordKc(Enum<?> record, int kc) {
        if (record != null) {
            configManager.setRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.KC_KEY + record, kc);
        }
    }

    private int getRecordXp(Enum<?> record) {
        Integer xp = configManager.getRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.XP_KEY + record, int.class);
        return xp == null ? 0 : xp;
    }

    public void setRecordXp(Enum<?> record, int xp) {
        if (record != null) {
            configManager.setRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.XP_KEY + record, xp);
        }
    }

    private int getRecordGe(Enum<?> record) {
        Integer ge = configManager.getRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.GE_KEY + record, int.class);
        return ge == null ? 0 : ge;
    }

    private void setRecordGe(Enum<?> record, int ge) {
        if (record != null) {
            configManager.setRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.GE_KEY + record, ge);
        }
    }

    private int getRecordHa(Enum<?> record) {
        Integer ha = configManager.getRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.HA_KEY + record, int.class);
        return ha == null ? 0 : ha;
    }

    private void setRecordHa(Enum<?> record, int ha) {
        if (record != null) {
            configManager.setRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.HA_KEY + record, ha);
        }
    }
    //</editor-fold>

    @Provides
    SlayerTrackerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(SlayerTrackerConfig.class);
    }
}