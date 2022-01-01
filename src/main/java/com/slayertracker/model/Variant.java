package com.slayertracker.model;

import lombok.Getter;

@Getter
public enum Variant {
    //<editor-fold desc="Enums">
    // Aberrant spectres
    ABERRANT_SPECTRE("Aberrant spectre", "Aberrant spectre", "Abhorrent spectre"),
    DEVIANT_SPECTRE("Deviant spectre", "Deviant spectre", "Repugnant spectre"),
    // Dust devils
    DUST_DEVIL_WEAK("Smoke Dungeon", new int[]{93}),
    DUST_DEVIL_STRONG("Catacombs of Kourend", new int[]{110}),
    // Fire giants
    FIRE_GIANT_WEAK("level-86", new int[]{86}),
    FIRE_GIANT_STRONG("Catacombs fo Kourend", new int[]{104, 109}),
    // Jad
    JAD("Jad", 25250, "TzTok-Jad"), // PLACEHOLDER must add to Assignment
    FIGHT_CAVE_OTHERS("Other", "Tz-Kih", "Tz-Kek", "Tok-Xil", "Yt-MekKok", "Ket-Zek", "Yt-HurKot"),
    // Kalphite
    KALPHITE_WORKER("Worker", "Kalphite Worker"),
    KALPHITE_SOLDIER("Soldier", "Kalphite Soldier"),
    KALPHITE_GUARDIAN("Guardian", "Kalphite Guardian"),
    KALPHITE_QUEEN("Queen", "Kalphite Queen"),
    // Trolls
    ICE_TROLL("Ice troll", "Ice troll runt", "Ice troll grunt", "Ice troll male", "Ice troll female"),
    MOUNTAIN_TROLL("Mountain troll"),
    // Tzhaar
    TZHAAR_KET("Tzhaar-Ket"),
    TZHAAR_XIL("Tzhaar-Xil"),
    TZHAAR_MEJ("Tzhaar-Mej"),
    TZHAAR_HUR("Tzhaar-Hur");
    //</editor-fold>

    private final String name;
    private final String[] targetNames;
    private final int[] combatLevels;
    private final int slayerXp;

    Variant(String nameAndTargetName) {
        this.name = nameAndTargetName;
        this.targetNames = new String[]{nameAndTargetName};
        this.combatLevels = new int[0];
        this.slayerXp = -1;
    }

    Variant(String name, String... targetNames) {
        this.name = name;
        this.targetNames = targetNames;
        this.combatLevels = new int[0];
        this.slayerXp = -1;
    }

    Variant(String name, int[] combatLevels) {
        this.name = name;
        this.targetNames = new String[]{};
        this.combatLevels = combatLevels;
        this.slayerXp = -1;
    }

    Variant(String displayAndTargetName, int slayerXp) {
        this.name = displayAndTargetName;
        this.targetNames = new String[]{displayAndTargetName};
        this.combatLevels = new int[0];
        this.slayerXp = slayerXp;
    }

    Variant(String name, int slayerXp, String... targetNames) {
        this.name = name;
        this.targetNames = targetNames;
        this.combatLevels = new int[0];
        this.slayerXp = slayerXp;
    }

    Variant(String name, int slayerXp, int[] combatLevels) {
        this.name = name;
        this.targetNames = new String[]{};
        this.combatLevels = combatLevels;
        this.slayerXp = slayerXp;
    }
}