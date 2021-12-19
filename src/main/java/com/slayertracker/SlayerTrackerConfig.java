package com.slayertracker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(SlayerTrackerConfig.GROUP_NAME)
public interface SlayerTrackerConfig extends Config {
    String GROUP_NAME = "slayertracker";

    // Key names for stored task values
    String KC_KEY = "kc_";
    String TIME_KEY = "time_";
    String XP_KEY = "xp_";
    String HA_KEY = "ha_";
    String GE_KEY = "ge_";
    String TRACKED_TASKS_KEY = "tracked_tasks";
}
