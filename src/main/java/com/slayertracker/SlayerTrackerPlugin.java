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
import net.runelite.client.game.NPCManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.slayer.SlayerConfig;
import org.apache.commons.lang3.ArrayUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

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

    @Inject
    private NPCManager npcManager;

    private Task task;
    private final Set<String> targetNames = new HashSet<>();
    private final HashMap<Enum<?>, HashSet<NPC>> subTaskToInteractorSet = new HashMap<>();
    private final HashMap<Enum<?>, Instant> subTaskToInteractionStartTime = new HashMap<>();
    private final Set<NPC> xpNpcQueue = new HashSet<>();
    private int cachedXp = -1;

    @Override
    protected void startUp() throws Exception {
        // When plugin is started while already logged in,
        // cache the current Slayer xp
        if (client.getGameState() == GameState.LOGGED_IN) {
            cachedXp = client.getSkillExperience(SLAYER);
        }
    }

    @Override
    protected void shutDown() throws Exception {
        task = null;
        targetNames.clear();
        subTaskToInteractorSet.clear();
        subTaskToInteractionStartTime.clear();
        cachedXp = -1;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {

        switch (gameStateChanged.getGameState()) {
            case HOPPING:
            case LOGGING_IN:
                task = null;
                targetNames.clear();
                subTaskToInteractorSet.clear();
                subTaskToInteractionStartTime.clear();
                cachedXp = -1;
                break;
            case LOGGED_IN:
                updateTaskByName(getSlayerConfigTaskName()); // TODO try moving to LOGGING_IN because this repeats
                break;
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        updateInteractors();
        // TODO Perhaps we can move this out of onGameTick now
        // TODO to when these HashMaps are originally manipulated
        for (Enum<?> subTask : subTaskToInteractorSet.keySet()) {
            if (!subTaskToInteractionStartTime.containsKey(subTask)) {
                startTiming(subTask);
            }
        }

        for (Iterator<Enum<?>> iterator = subTaskToInteractionStartTime.keySet().iterator(); iterator.hasNext(); ) {
            Enum<?> subTask = iterator.next();
            if (!subTaskToInteractorSet.containsKey(subTask)) {
                recordTime(subTask);
                iterator.remove();
            }
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
        NPC npc = npcLootReceived.getNpc();
        if (!isTask(npc)) {    // Must use isTask instead of interactors.contains() because npc
            return;            // is removed from interactors by the time loot drops
        }

        int ge = 0;
        int ha = 0;
        for (ItemStack itemStack : npcLootReceived.getItems()) {
            ge += itemManager.getItemPrice(itemStack.getId()) * itemStack.getQuantity();
            ha += itemManager.getItemComposition(itemStack.getId()).getHaPrice() * itemStack.getQuantity();
        }

        setTaskGe(task, getTaskGe(task) + ge);
        setTaskHa(task, getTaskHa(task) + ha);

        // Only proceed if npc is a subTask
        SubTask subTask = getSubTask(npc);
        if (subTask == null) {
            return;
        }

        setTaskGe(subTask, getTaskGe(subTask) + ge);
        setTaskHa(subTask, getTaskHa(subTask) + ha);
    }

    @Subscribe
    public void onActorDeath(ActorDeath actorDeath) {
        // Only proceed if dead actor was an NPC
        Actor actor = actorDeath.getActor();
        if (!(actor instanceof NPC)) {
            return;
        }

        NPC npc = (NPC) actor;
        for (Enum<?> key : subTaskToInteractorSet.keySet()) {       // Iterate over interactor sets, ie FIRE_GIANTS, FIRE_GIANT_WEAK
            if (subTaskToInteractorSet.get(key).contains(npc)) {    // If an interactor set contains the dead npc:
                xpNpcQueue.add(npc);                                // Add it to the set of npcs to share next xp drop
                setTaskKc(key, getTaskKc(key) + 1);                 // Increment kc for that interactor set
                subTaskToInteractorSet.get(key).remove(npc);        // Remove npc from interactor set
                if (!subTaskToInteractorSet.get(key).isEmpty()) {   // If interactor set hasn't emptied,
                    recordTime(key);                                // Log kill time, and continue timing
                    startTiming(key);
                }
            }
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

        divideXp(delta, xpNpcQueue);
    }

    // Recursively allocate xp to each killed monster in the queue
    // this will allow for safe rounding to whole xp amounts
    private void divideXp(int delta, Set<NPC> xpNpcQueue) {
        if (xpNpcQueue.isEmpty()) {
            return;
        }

        int hpTotal = 0;
        for (NPC npc : xpNpcQueue) {
            hpTotal += npcManager.getHealth(npc.getId());
        }

        NPC npc = xpNpcQueue.iterator().next();
        int xpShare = delta * npcManager.getHealth(npc.getId()) / hpTotal;

        setTaskXp(task, getTaskXp(task) + xpShare);

        SubTask subTask = getSubTask(npc);
        if (subTask != null) {
            setTaskXp(subTask, getTaskXp(subTask) + xpShare);
        }

        delta -= xpShare;
        xpNpcQueue.remove(npc);
        divideXp(delta, xpNpcQueue);
    }

    // TODO
    // Config File
    // Side Panel (ugh)

    @Subscribe
    public void onInteractingChanged(InteractingChanged event) {
        final Actor source = event.getSource();
        final Actor target = event.getTarget();
        final NPC npc;

        // Determine whether this is a Player-NPC interaction
        // Assign the source or target as appropriate to npc
        if (source == client.getLocalPlayer() && target instanceof NPC) {
            npc = (NPC) target;
        } else if (source instanceof NPC && target == client.getLocalPlayer()) {
            npc = (NPC) source;
        } else {
            return;
        }

        // Only proceed if npc is on task
        if (!isTask(npc)) {
            return;
        }

        // If it doesn't exist, create a set of interactors for the task, than add the npc
        if (!subTaskToInteractorSet.containsKey(task)) {
            subTaskToInteractorSet.put(task, new HashSet<>());
        }
        subTaskToInteractorSet.get(task).add(npc);

        // Only proceed if npc is also a subtask
        SubTask subTask = getSubTask(npc);
        if (subTask == null) {
            return;
        }

        // Same as above, but for subtask
        if (!subTaskToInteractorSet.containsKey(subTask)) {
            subTaskToInteractorSet.put(subTask, new HashSet<>());
        }
        subTaskToInteractorSet.get(subTask).add(npc);
    }

    private boolean isTask(NPC npc) {
        // If targetNames is empty, no slayer task is assigned
        if (targetNames.isEmpty()) {
            return false;
        }

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

    private SubTask getSubTask(NPC npc) {
        if (task == null) {
            return null;
        }

        final NPCComposition composition = npc.getTransformedComposition();
        if (composition == null) {
            return null;
        }

        for (SubTask subTask : task.getSubTasks()) {
            for (int lvl : subTask.getCombatLevels()) {             // Find subtask with matching combat level
                if (npc.getCombatLevel() == lvl) {
                    return subTask;
                }
            }
            for (String targetName : subTask.getTargetNames()) {    // Find subtask with matching name
                if (npc.getName().equals(targetName)) {
                    return subTask;
                }
            }
        }
        return null;
    }

    private void updateInteractors() {
        // Remove an Interactor from the set if:
        //
        // Interactor no longer exists in the client, OR
        // Player's interaction is not pointed toward it, AND
        // it's not interacting OR its interaction is not pointed toward the player
        for (Enum<?> key : subTaskToInteractorSet.keySet()) {
            subTaskToInteractorSet.get(key).removeIf(interactor ->
                    !client.getNpcs().contains(interactor)
                            || interactor != client.getLocalPlayer().getInteracting()
                            && (interactor.getInteracting() == null || !interactor.getInteracting().equals(client.getLocalPlayer()))
            );
        }
        // Remove empty interactor sets from the dictionary if empty
        subTaskToInteractorSet.values().removeIf(HashSet::isEmpty);
    }

    public void startTiming(Enum<?> task) {
        subTaskToInteractionStartTime.put(task, Instant.now());
    }

    public void recordTime(Enum<?> task) {
        Duration duration = Duration.between(subTaskToInteractionStartTime.get(task), Instant.now());
        setTaskDuration(task, getTaskDuration(task).plus(duration));
    }

    private void updateTaskByName(String taskName) {
        if (taskName.equals("")) {
            task = null;
            targetNames.clear();
            subTaskToInteractorSet.clear();
            return;
        }

        task = Task.getTask(taskName);

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
            case "XXX":
                clearConfig();
                break;
            case "i":
                log.info(String.valueOf(subTaskToInteractorSet));
                break;
            case "s":
                log.info(String.valueOf(xpNpcQueue));
        }
    }

    private void logInfo(Enum<?> task) {
        try {
            for (SubTask subTask : ((Task) task).getSubTasks()) {
                logInfo(subTask);
            }
        } catch (ClassCastException e) {
        }

        float hours = getTaskDuration(task).getSeconds() / 3600f;
        log.info("");
        log.info("nm: " + task);
        log.info("tm: " + getTaskDuration(task));
        log.info("kc: " + getTaskKc(task));
        log.info("xp: " + getTaskXp(task));
        log.info("ge: " + getTaskGe(task));
        log.info("ha: " + getTaskHa(task));
        log.info("kc/h: " + Math.round(getTaskKc(task) / hours));
        log.info("xp/h: " + Math.round(getTaskXp(task) / hours));
        log.info("ge/h: " + Math.round(getTaskGe(task) / hours));
        log.info("ha/h: " + Math.round(getTaskHa(task) / hours));
        log.info(String.valueOf(subTaskToInteractorSet));
        log.info(String.valueOf(subTaskToInteractionStartTime));
    }

    private void clearConfig() {
        log.warn("CLEARING ALL CONFIG ENTRIES");
        for (String key : configManager.getConfigurationKeys("slayertracker")) {
            String[] splitString = key.split("\\.");
            configManager.unsetRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, splitString[splitString.length - 1]);
            configManager.unsetConfiguration(SlayerTrackerConfig.GROUP_NAME, splitString[splitString.length - 1]);
        }
    }

    private void removeTask(Task task) {
        log.warn("REMOVING TASK: " + task);
        configManager.unsetRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.TIME_KEY + task);
        configManager.unsetRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.KC_KEY + task);
        configManager.unsetRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.XP_KEY + task);
        configManager.unsetRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.GE_KEY + task);
        configManager.unsetRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.HA_KEY + task);
    }

    // GETTERS AND SETTERS FOR CONFIG FILE

    public Duration getTaskDuration(Enum<?> task) {
        String durationString = configManager.getRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.TIME_KEY + task);
        if (durationString != null) {
            return Duration.parse(durationString);
        } else {
            return Duration.ZERO;
        }
    }

    public void setTaskDuration(Enum<?> task, Duration duration) {
        if (task != null) {
            configManager.setRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.TIME_KEY + task, duration.toString());
        }
    }

    public int getTaskKc(Enum<?> task) {
        Integer kc = configManager.getRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.KC_KEY + task, int.class);
        return kc == null ? 0 : kc;
    }

    public void setTaskKc(Enum<?> task, int kc) {
        if (task != null) {
            configManager.setRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.KC_KEY + task, kc);
        }
    }

    private int getTaskXp(Enum<?> task) {
        Integer xp = configManager.getRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.XP_KEY + task, int.class);
        return xp == null ? 0 : xp;
    }

    public void setTaskXp(Enum<?> task, int xp) {
        if (task != null) {
            configManager.setRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.XP_KEY + task, xp);
        }
    }

    private int getTaskGe(Enum<?> task) {
        Integer ge = configManager.getRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.GE_KEY + task, int.class);
        return ge == null ? 0 : ge;
    }

    private void setTaskGe(Enum<?> task, int ge) {
        if (task != null) {
            configManager.setRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.GE_KEY + task, ge);
        }
    }

    private int getTaskHa(Enum<?> task) {
        Integer ha = configManager.getRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.HA_KEY + task, int.class);
        return ha == null ? 0 : ha;
    }

    private void setTaskHa(Enum<?> task, int ha) {
        if (task != null) {
            configManager.setRSProfileConfiguration(SlayerTrackerConfig.GROUP_NAME, SlayerTrackerConfig.HA_KEY + task, ha);
        }
    }

    @Provides
    SlayerTrackerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(SlayerTrackerConfig.class);
    }
}