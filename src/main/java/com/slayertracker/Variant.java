package com.slayertracker;

import lombok.Getter;

@Getter
enum Variant {
    //<editor-fold desc="Enums">
    // Aberrant spectres
    ABERRANT_SPECTRE("Aberrant spectre", "Aberrant spectre", "Abhorrent spectre"),
    DEVIANT_SPECTRE("Deviant spectre", "Deviant spectre", "Repugnant spectre"),
    // Fire giants
    FIRE_GIANT_WEAK("Level-86", new int[]{86}),
    FIRE_GIANT_STRONG("Level-104/109", new int[]{104, 109}),
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
    TZHAAR_HUR("Tzhaar-Hur"),

    SUQAH_NORM("Suqah"),

    JAD("Jad",25250,"TzTok-Jad"); // PLACEHOLDER must add to Assignment
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