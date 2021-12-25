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

    @ConfigItem(
            keyName = "lootUnit",
            name = "Loot Unit",
            description = "Display loot value as Grand Exchange or High Alchemy price."
    )
    default SlayerTrackerLootUnit lootUnit()
    {
        return SlayerTrackerLootUnit.GRAND_EXCHANGE;
    }

    enum SlayerTrackerLootUnit {
        GRAND_EXCHANGE, HIGH_ALCHEMY
    }
}