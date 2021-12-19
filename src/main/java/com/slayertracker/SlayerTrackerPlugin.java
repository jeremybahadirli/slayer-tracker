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
import net.runelite.client.game.ItemStack;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.slayer.SlayerConfig;
import org.apache.commons.lang3.ArrayUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static net.runelite.api.Skill.SLAYER;

@Slf4j
@PluginDescriptor(
        name = "Slayer Tracker"
)
public class SlayerTrackerPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private SlayerTrackerConfig config;

    @Inject
    private ConfigManager configManager;

    @Inject
    private ItemManager itemManager;

    private Task task;
    private final Set<String> targetNames = new HashSet<>();
    private final Set<NPC> interactors = new HashSet<>();
    private Instant startTime;
    private int cachedXp = -1;

    @Override
    protected void startUp() throws Exception {
        if (client.getGameState() == GameState.LOGGED_IN) {
            cachedXp = client.getSkillExperience(SLAYER);
        }
    }

    @Override
    protected void shutDown() throws Exception {
        task = null;
        targetNames.clear();
        interactors.clear();
        startTime = null;
        cachedXp = -1;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {

        switch (gameStateChanged.getGameState()) {
            case HOPPING:
            case LOGGING_IN:
                task = null;
                targetNames.clear();
                interactors.clear();
                startTime = null;
                cachedXp = -1;
                break;
            case LOGGED_IN:
                updateTaskByName(getSlayerConfigTaskName());
                // clearConfig(); // Uncomment to clear all config entries on login
                break;
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        updateInteractors();
        if (startTime == null && !interactors.isEmpty()) {
            startTiming();
        } else if (startTime != null && interactors.isEmpty()) {
            stopTiming();
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (event.getGroup().equals(SlayerConfig.GROUP_NAME) && event.getKey().equals(SlayerConfig.TASK_NAME_KEY)) {
            updateTaskByName(event.getNewValue());
        }
    }

    @Subscribe
    private void onNpcLootReceived(NpcLootReceived npcLootReceived) {
        if (!isTask(npcLootReceived.getNpc())) {
            return;
        }

        int ge = 0;
        int ha = 0;
        for (ItemStack itemStack : npcLootReceived.getItems()) {
            ge += itemManager.getItemPrice(itemStack.getId()) * itemStack.getQuantity();
            ha += itemManager.getItemComposition(itemStack.getId()).getHaPrice() * itemStack.getQuantity();
        }

        int oldGe = getTaskGe(task);
        configManager.setRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.GE_KEY + task, oldGe + ge);

        int oldHa = getTaskHa(task);
        configManager.setRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.HA_KEY + task, oldHa + ha);
    }

    @Subscribe
    public void onActorDeath(ActorDeath actorDeath) {
        Actor actor = actorDeath.getActor();
        if (!(actor instanceof NPC)) {
            return;
        }

        NPC npc = (NPC) actor;
        if (!interactors.contains(npc)) {
            return;
        }

        // Monster KC in config ++
        int oldKc = getTaskKc(task);
        configManager.setRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.KC_KEY + task, ++oldKc);

        // Update combat duration, even though
        // still in combat with task.
        interactors.remove(npc);
        if (!interactors.isEmpty()) {
            stopTiming();
            startTiming();
        }
    }

    @Subscribe
    private void onStatChanged(StatChanged statChanged) {
        if (statChanged.getSkill() != SLAYER) {
            return;
        }

        int slayerExp = statChanged.getXp();

        if (slayerExp <= cachedXp) {
            return;
        }

        if (cachedXp == -1) {
            // this is the initial xp sent on login
            cachedXp = slayerExp;
            return;
        }

        final int delta = slayerExp - cachedXp;
        cachedXp = slayerExp;

        int oldXp = getTaskXp(task);
        configManager.setRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.XP_KEY + task, oldXp + delta);
    }

    // TODO
    // Config File
    // Side Panel (ugh)

    @Subscribe
    public void onInteractingChanged(InteractingChanged event) {
        final Actor source = event.getSource();
        final Actor target = event.getTarget();
        final NPC npc;

        // Determine whether this is a Player-NPC interaction.
        // Assign the source or target as appropriate to npc
        if (source == client.getLocalPlayer() && target instanceof NPC) {
            npc = (NPC) target;
        } else if (source instanceof NPC && target == client.getLocalPlayer()) {
            npc = (NPC) source;
        } else {
            return;
        }

        // If npc is on task, add to the set of task
        // npcs interacting with the Player
        if (isTask(npc)) {
            interactors.add(npc);
        }
    }

    private boolean isTask(NPC npc) {
        // If targetNames is empty, no slayer task is assigned
        if (targetNames.isEmpty()) {
            return false;
        }

        // Verify NPC is still valid
        final NPCComposition composition = npc.getTransformedComposition();
        if (composition == null) {
            return false;
        }

        // Format non-breaking space, convert to lower case for comparison
        final String name = composition.getName()
                .replace('\u00A0', ' ')
                .toLowerCase();

        // Iterate over names of monsters on current task
        // If names match
        for (String target : targetNames) {
            if (name.contains(target)) {
                if (ArrayUtils.contains(composition.getActions(), "Attack")
                        // Pick action is for zygomite-fungi
                        || ArrayUtils.contains(composition.getActions(), "Pick")) {
                    return true;
                }
            }
        }
        return false;
    }

    private void updateInteractors() {
        // Remove an Interactor from the set if:
        //
        // Interactor no longer exists in the client, OR
        // Player's interaction is not pointed toward it, AND
        // it's not interacting OR its interaction is not pointed toward the player
        interactors.removeIf(interactor ->
                !client.getNpcs().contains(interactor)
                        || interactor != client.getLocalPlayer().getInteracting()
                        && (interactor.getInteracting() == null || !interactor.getInteracting().equals(client.getLocalPlayer()))
        );
    }

    public void startTiming() {
        startTime = Instant.now();
    }

    public void stopTiming() {
        Duration duration = Duration.between(startTime, Instant.now());
        startTime = null;
        Duration newTime = duration.plus(getTaskDuration(task));
        configManager.setRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.TIME_KEY + task, newTime.toString());
    }

    private void updateTaskByName(String taskName) {
        task = Task.getTask(taskName);

        // If no task exists, clear targetNames and return.
        if (taskName.equals("")) {
            targetNames.clear();
            return;
        }

        // Add Task's secondary target names, as lower case
        Arrays.stream(task.getTargetNames())
                .map(String::toLowerCase)
                .forEach(targetNames::add);
        // Add Task's primary name, as singular
        targetNames.add(taskName.replaceAll("s$", ""));
    }

    private String getSlayerConfigTaskName() {
        String taskName = configManager.getRSProfileConfiguration(SlayerConfig.GROUP_NAME, SlayerConfig.TASK_NAME_KEY);
        return taskName == null ? "" : taskName.toLowerCase();
    }

    public Duration getTaskDuration(Task task) {
        String durationString = configManager.getRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.TIME_KEY + task);
        if (durationString != null) {
            return Duration.parse(durationString);
        } else {
            return Duration.ZERO;
        }
    }

    public int getTaskKc(Task task) {
        Integer kc = configManager.getRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.KC_KEY + task, int.class);
        return kc == null ? 0 : kc;
    }

    private int getTaskXp(Task task) {
        Integer xp = configManager.getRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.XP_KEY + task, int.class);
        return xp == null ? 0 : xp;
    }

    private int getTaskGe(Task task) {
        Integer ge = configManager.getRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.GE_KEY + task, int.class);
        return ge == null ? 0 : ge;
    }

    private int getTaskHa(Task task) {
        Integer ha = configManager.getRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.HA_KEY + task, int.class);
        return ha == null ? 0 : ha;
    }

    @Subscribe
    private void onCommandExecuted(CommandExecuted commandExecuted) {
        switch (commandExecuted.getCommand()) {
            case "ttt":
                if (commandExecuted.getArguments().length == 0) {
                    logInfo(task);
                } else {
                    logInfo(Task.getTask(commandExecuted.getArguments()[0]));
                }
                break;
            case "ddd":
                if (commandExecuted.getArguments().length == 0) {
                    removeTask(task);
                } else {
                    removeTask(Task.getTask(commandExecuted.getArguments()[0]));
                }
                break;
        }
    }

    private void logInfo(Task task) {
        log.info("tn: " + task.getName());
        log.info("tm: " + getTaskDuration(task));
        log.info("kc: " + getTaskKc(task));
        log.info("xp: " + getTaskXp(task));
        log.info("ge: " + getTaskGe(task));
        log.info("ha: " + getTaskHa(task));
        float hours = getTaskDuration(task).getSeconds() / 3600f;
        int kcPerHour = Math.round(getTaskKc(task) / hours);
        int xpPerHour = Math.round(getTaskXp(task) / hours);
        int gePerHour = Math.round(getTaskGe(task) / hours);
        int haPerHour = Math.round(getTaskHa(task) / hours);
        log.info("kc/h: " + kcPerHour);
        log.info("xp/h: " + xpPerHour);
        log.info("ge/h: " + gePerHour);
        log.info("ha/h: " + haPerHour);
    }

    private void clearConfig() {
        log.warn("CLEARING ALL CONFIG ENTRIES");
        for (String key : configManager.getConfigurationKeys("slayertracker")) {
            String[] splitString = key.split("\\.");
            configManager.unsetRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, splitString[splitString.length-1]);
            configManager.unsetConfiguration(SlayerTrackerConfig.GROUP_NAME, splitString[splitString.length-1]);
        }
    }

    private void removeTask(Task task) {
        configManager.unsetRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.TIME_KEY + task);
        configManager.unsetRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.KC_KEY + task);
        configManager.unsetRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.XP_KEY + task);
        configManager.unsetRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.GE_KEY + task);
        configManager.unsetRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.HA_KEY + task);
    }

    @Provides
    SlayerTrackerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(SlayerTrackerConfig.class);
    }
}