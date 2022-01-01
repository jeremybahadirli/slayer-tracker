package com.slayertracker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(SlayerTrackerConfig.GROUP_NAME)
public interface SlayerTrackerConfig extends Config {
    String GROUP_NAME = "slayertracker";

    @ConfigItem(
            keyName = "lootUnit",
            name = "Loot Unit",
            description = "Display loot value as Grand Exchange or High Alchemy price."
    )
    default SlayerTrackerLootUnit lootUnit() {
        return SlayerTrackerLootUnit.GRAND_EXCHANGE;
    }

    enum SlayerTrackerLootUnit {
        GRAND_EXCHANGE, HIGH_ALCHEMY
    }
}