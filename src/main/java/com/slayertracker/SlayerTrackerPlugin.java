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

    private String taskName;

    private final Set<String> targetNames = new HashSet<>();
    private final Set<NPC> interactors = new HashSet<>();

    private int cachedXp = -1;

    private Instant startTime;

    @Override
    protected void startUp() throws Exception {
        if (client.getGameState() == GameState.LOGGED_IN) {
            cachedXp = client.getSkillExperience(SLAYER);
        }
    }

    @Override
    protected void shutDown() throws Exception {
        taskName = null;
        startTime = null;
        targetNames.clear();
        interactors.clear();
        cachedXp = -1;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {

        switch (gameStateChanged.getGameState()) {
            case HOPPING:
            case LOGGING_IN:
                taskName = null;
                startTime = null;
                targetNames.clear();
                interactors.clear();
                cachedXp = -1;
                break;
            case LOGGED_IN:
                updateTask(getTaskName());
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
            updateTask(event.getNewValue());
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

        int oldGe = getTaskGe(taskName);
        configManager.setRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.GE_KEY + taskName, oldGe + ge);

        int oldHa = getTaskHa(taskName);
        configManager.setRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.HA_KEY + taskName, oldHa + ha);
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
        int oldKc = getTaskKc(taskName);
        configManager.setRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.KC_KEY + taskName, ++oldKc);

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

        int oldXp = getTaskXp(taskName);
        configManager.setRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.XP_KEY + taskName, oldXp + delta);
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
        // Players interaction is not pointed toward it, AND
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
        Duration newTime = duration.plus(getTaskDuration(taskName));
        configManager.setRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.TIME_KEY + taskName, newTime.toString());
    }

    private void updateTask(String taskName) {
        this.taskName = taskName;

        // If no task exists, clear targetNames and return.
        if (taskName.equals("")) {
            targetNames.clear();
            return;
        }

        // Add Task's secondary target names, as lower case
        Arrays.stream(Task.getTask(taskName).getTargetNames())
                .map(String::toLowerCase)
                .forEach(targetNames::add);
        // Add Task's primary name, as singular
        targetNames.add(taskName.replaceAll("s$", ""));
    }

    private void removeTask(String taskName) {
        configManager.unsetRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.TIME_KEY + taskName);
        configManager.unsetRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.KC_KEY + taskName);
        configManager.unsetRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.XP_KEY + taskName);
        configManager.unsetRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.GE_KEY + taskName);
        configManager.unsetRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.HA_KEY + taskName);
    }

    public Duration getTaskDuration(String taskName) {
        String durationString = configManager.getRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.TIME_KEY + taskName);
        if (durationString != null) {
            return Duration.parse(durationString);
        } else {
            return Duration.ZERO;
        }
    }

    private String getTaskName() {
        String taskName = configManager.getRSProfileConfiguration(SlayerConfig.GROUP_NAME, SlayerConfig.TASK_NAME_KEY);
        return taskName == null ? "" : taskName.toLowerCase();
    }

    public int getTaskKc(String taskName) {
        Integer kc = configManager.getRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.KC_KEY + taskName, int.class);
        return kc == null ? 0 : kc;
    }

    private int getTaskXp(String taskName) {
        Integer xp = configManager.getRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.XP_KEY + taskName, int.class);
        return xp == null ? 0 : xp;
    }

    private int getTaskGe(String taskName) {
        Integer ge = configManager.getRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.GE_KEY + taskName, int.class);
        return ge == null ? 0 : ge;
    }

    private int getTaskHa(String taskName) {
        Integer ha = configManager.getRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.HA_KEY + taskName, int.class);
        return ha == null ? 0 : ha;
    }

    @Subscribe
    private void onCommandExecuted(CommandExecuted commandExecuted) {
        switch (commandExecuted.getCommand()) {
            case "ttt":
                if (commandExecuted.getArguments().length < 1) {
                    logInfo(taskName);
                } else {
                    logInfo(String.join(" ", commandExecuted.getArguments()).toLowerCase());
                }
                break;
            case "ddd":
                removeTask(String.join(" ", commandExecuted.getArguments()).toLowerCase());
                break;
        }
    }

    private void logInfo(String taskName) {
        log.info("tn: " + taskName);
        log.info("tm: " + getTaskDuration(taskName));
        log.info("kc: " + getTaskKc(taskName));
        log.info("xp: " + getTaskXp(taskName));
        log.info("ge: " + getTaskGe(taskName));
        log.info("ha: " + getTaskHa(taskName));
        float hours = getTaskDuration(taskName).getSeconds() / 3600f;
        int kcPerHour = Math.round(getTaskKc(taskName) / hours);
        int xpPerHour = Math.round(getTaskXp(taskName) / hours);
        int gePerHour = Math.round(getTaskGe(taskName) / hours);
        int haPerHour = Math.round(getTaskHa(taskName) / hours);
        log.info("kc/h: " + kcPerHour);
        log.info("xp/h: " + xpPerHour);
        log.info("ge/h: " + gePerHour);
        log.info("ha/h: " + haPerHour);
    }

    @Provides
    SlayerTrackerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(SlayerTrackerConfig.class);
    }
}