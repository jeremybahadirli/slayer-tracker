/*
 * Copyright (c) 2017, Tyler <https://github.com/tylerthardy>
 * Copyright (c) 2018, Shaun Dreclin <shaundreclin@gmail.com>
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
package com.slayertracker;

import lombok.Getter;

@Getter
enum Variant {
    //<editor-fold desc="Enums">
    // Aberrant spectres
    ABERRANT_SPECTRE("Aberrant spectre", "Aberrant spectre", "Abhorrent spectre"),
    DEVIANT_SPECTRE("Deviant spectre", "Deviant spectre", "Repugnant spectre"),
    // Fire giants
    FIRE_GIANT_WEAK("Level-86", 86),
    FIRE_GIANT_STRONG("Level-104/109", 104, 109),
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

    private final String displayName;
    private final String[] targetNames;
    private final int[] combatLevels;

    Variant(String displayAndTargetName) {
        this.displayName = displayAndTargetName;
        this.targetNames = new String[]{displayAndTargetName};
        this.combatLevels = new int[0];
    }

    Variant(String displayName, String... targetNames) {
        this.displayName = displayName;
        this.targetNames = targetNames;
        this.combatLevels = new int[0];
    }

    Variant(String displayName, int... combatLevels) {
        this.displayName = displayName;
        this.targetNames = new String[]{};
        this.combatLevels = combatLevels;
    }
}