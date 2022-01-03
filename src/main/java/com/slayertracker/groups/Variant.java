/*
 * Copyright (c) 2021, Jeremy Bahadirli <https://github.com/jeremybahadirli>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.slayertracker.model;

import lombok.Getter;

@Getter
public enum Variant {
    //<editor-fold desc="Enums">
    // Aberrant spectres
    ABERRANT_SPECTRE("Aberrant spectre", "Aberrant spectre", "Abhorrent spectre"),
    DEVIANT_SPECTRE("Deviant spectre", "Deviant spectre", "Repugnant spectre"),
    // Bloodvelds
    BLOODVELD_WEAK("(level-76)", new int[]{76}),
    BLOODVELD_STRONG("God Wars Dungeon (level-81)", new int[]{81}),
    MUTATED_BLOODVELD("Mutated Bloodveld", "Mutated Bloodveld"),
    // Dust devils
    DUST_DEVIL_WEAK("Smoke Dungeon", new int[]{93}),
    DUST_DEVIL_STRONG("Catacombs of Kourend", new int[]{110}),
    // Fire giants
    FIRE_GIANT_WEAK("level-86", new int[]{86}),
    FIRE_GIANT_STRONG("Catacombs of Kourend", new int[]{104, 109}),
    // Hellhounds
    CERBERUS("Cerberus", "Cerberus"),
    HELLHOUND("Hellhound", "Hellhound"),
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
    // Turoth
    TUROTH_SMALL("Small", new int[]{83, 85}),
    TUROTH_LARGE("Large", new int[]{87, 89}),
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