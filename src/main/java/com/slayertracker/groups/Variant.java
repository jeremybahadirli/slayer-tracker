/*
 * Copyright (c) 2022, Jeremy Bahadirli <https://github.com/jeremybahadirli>
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
package com.slayertracker.groups;

import lombok.Getter;

@Getter
public enum Variant implements Group
{
	//<editor-fold desc="Enums">
	// Aberrant spectres
	ABERRANT_SPECTRE("Aberrant spectre", "Aberrant spectre", "Abhorrent spectre"),
	DEVIANT_SPECTRE("Deviant spectre", "Deviant spectre", "Repugnant spectre"),
	// Abyssal demons
	ABYSSAL_DEMON("Abyssal demon"),
	SIRE("Abyssal sire"),
	// Ankous
	ANKOU_NORMAL("Normal", new int[]{75, 82, 86}),
	ANKOU_CATACOMBS("Catacombs", new int[]{95}),
	ANKOU_WILDY_CAVE("Wildy Slayer Cave", new int[]{98}),
	// Aviansies
	AVIANSIE_NORMAL("Aviansie"),
	KREEARRA("Kree'arra", new int[]{580, 159, 149, 143}),
	// Banshees
	BANSHEE_SLAYER_TOWER("Slayer Tower", new int[]{23}),
	BANSHEE_CATACOMBS("Catacombs", new int[]{89}),
	// Basilisks
	BASILISK_NORMAL("Basilisk"),
	BASILISK_KNIGHT("Basilisk Knight", "Basilisk Knight"),
	// Bats
	BAT_NORMAL("Bat", "Bat", "Giant bat"),
	BAT_ALBINO("Albino bat"),
	DEATHWING("Deathwing"),
	// Bears
	BEAR_NORMAL("Normal", "Bear cub", "Black bear", "Grizzly bear", "Grizzly bear cub"),
	CALLISTO("Callisto"),
	// Black demons
	BLACK_DEMON("Black demon"),
	BLACK_DEMON_GORILLA("Demonic gorilla"),
	// Black dragons
	BLACK_DRAG_NORMAL("Black dragon"),
	BLACK_DRAG_BABY("Baby black dragon"),
	BLACK_DRAG_BRUTAL("Brutal black dragon"),
	// Bloodvelds
	BLOODVELD_WEAK("(level-76)", new int[]{76}),
	BLOODVELD_STRONG("God Wars Dungeon (level-81)", new int[]{81}),
	MUTATED_BLOODVELD("Mutated Bloodveld"),
	// Blue dragons
	BLUE_DRAG_NORMAL("Blue dragon"),
	BLUE_DRAG_BABY("Baby blue dragon"),
	BLUE_DRAG_BRUTAL("Brutal blue dragon"),
	VORKATH("Vorkath"),
	// Bronze dragon
	BRONZE_DRAG_NORMAL("Normal", new int[]{131}),
	BRONZE_DRAG_CATACOMBS("Catacombs", new int[]{143}),
	// Cave kraken
	CAVE_KRAKEN("Cave kraken"),
	KRAKEN("Kraken"),
	// Chaos druids
	CHAOS_DRUIDS("Chaos druid"),
	CHAOS_DRUIDS_ELDER("Elder", "Elder chaos druid"),
	// Dagannoth
	DAGANNOTH_NORMAL("(level-74/92)", new int[]{74, 92}),
	DAGANNOTH_WATERBIRTH("Waterbirth", new int[]{42, 70, 88, 90}),
	DAGANNOTH_KINGS("Kings", "Dagannoth Rex", "Dagannoth Prime", "Dagannoth Supreme"),
	// Dark warriors
	DARK_WARRIOR_FORTRESS("Fortress", new int[]{8, 145}),
	DARK_WARRIOR_KOUREND("Kourend", new int[]{37, 51, 62}),
	// Dust devils
	DUST_DEVIL_WEAK("Smoke Dungeon", new int[]{93}),
	DUST_DEVIL_STRONG("Catacombs of Kourend", new int[]{110}),
	// Fire giants
	FIRE_GIANT_WEAK("(level-86)", new int[]{86}),
	FIRE_GIANT_STRONG("Catacombs of Kourend", new int[]{104, 109}),
	// Gargoyles
	GARGOYLES("Gargoyle"),
	GROTESQUE_GUARDIANS("Grotesque guardians", 1350, "Dusk"),
	// Greater demons
	GREATER_DEMON_NORMAL("Normal", new int[]{92}),
	GREATER_DEMON_CATACOMBS("Catacombs", new int[]{100, 101, 113}),
	GREATER_DEMON_WILDY_CAVE("Wildy Slayer Cave", new int[]{104}),
	KRIL("K'ril Tsutsaroth", new int[]{145, 650}),
	SKOTIZO("Skotizo"),
	// Green dragons
	GREEN_DRAG_NORMAL("Green dragon"),
	GREEN_DRAG_BABY("Baby green dragon"),
	GREEN_DRAG_BRUTAL("Brutal green dragon"),
	// Hellhounds
	CERBERUS("Cerberus", "Cerberus"),
	HELLHOUND("Hellhound", "Hellhound"),
	// Hydras
	HYDRA("Hydra"),
	ALCHEMICAL("Alchemical Hydra"),
	// Ice giants
	ICE_GIANT_NORMAL("Normal", new int[]{53}),
	ICE_GIANT_WILDY_CAVE("Wildy Slayer Cave", new int[]{67}),
	// Ice warriors
	ICE_WARRIOR("Ice warrior"),
	ICELORD("Icelord"),
	// Iron dragons
	IRON_DRAG_NORMAL("Normal", new int[]{189}),
	IRON_DRAG_CATACOMBS("Catacombs", new int[]{215}),
	// Jad
	JAD("Jad", 25250, "TzTok-Jad"),
	FIGHT_CAVE_OTHERS("Other", "Tz-Kih", "Tz-Kek", "Tok-Xil", "Yt-MekKok", "Ket-Zek", "Yt-HurKot"),
	// Jellies
	JELLY("Jelly"),
	WARPED_JELLY("Warped jelly"),
	// Kalphite
	KALPHITE_WORKER("Worker", "Kalphite Worker"),
	KALPHITE_SOLDIER("Soldier", "Kalphite Soldier"),
	KALPHITE_GUARDIAN("Guardian", "Kalphite Guardian"),
	KALPHITE_QUEEN("Queen", "Kalphite Queen"),
	// Lesser demons
	LESSER_DEMON_NORMAL("Normal", new int[]{82}),
	LESSER_DEMON_WILDY_CATACOMBS("Catacombs/Wildy Cave", new int[]{87, 94}),
	// Lizardmen
	LIZARDMAN("Lizardman"),
	LIZARDMAN_BRUTE("Lizardman brute"),
	LIZARDMAN_SHAMAN("Lizardman shaman"),
	// Lizards
	LIZARD_DESERT("Desert", "Lizard", "Desert lizard", "Small lizard"),
	LIZARD_SULFUR("Sulfur", "Sulfur lizard"),
	// Monkeys
	MONKEY_NORMAL("Normal", "Monkey"),
	MONKEY_APE_ATOLL("Ape Atoll", "Monkey guard", "Monkey archer", "Monkey zombie", "Padulah"),
	// Pyrefiend
	PYREFIEND("Pyrefiend"),
	PYRELORD("Pyrelord"),
	// Red dragons
	RED_DRAGON_NORMAL("Red dragon"),
	RED_DRAGON_BABY("Baby red dragon"),
	RED_DRAGON_BRUTAL("Brutal red dragon"),
	// Skeletons
	SKELETON("Skeleton"),
	VETION("Vet'ion", new int[]{454}),
	// Smoke devils
	SMOKE_DEVIL_NORMAL("Smoke devil", new int[]{160, 280}),
	THERMONUCLEAR("Thermonuclear", new int[]{301}),
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
	TZHAAR_HUR("Tzhaar-Hur"),
	// Zygomite
	ZYGOMITE_MUTATED("Mutated zygomite", new int[]{74, 86}),
	ZYGOMITE_ANCIENT("Ancient zygomite", new int[]{109});
	//</editor-fold>

	private final String name;
	private final String[] targetNames;
	private final int[] combatLevels;
	private final int slayerXp;

	Variant(String nameAndTargetName)
	{
		this.name = nameAndTargetName;
		this.targetNames = new String[]{nameAndTargetName};
		this.combatLevels = new int[0];
		this.slayerXp = -1;
	}

	Variant(String name, String... targetNames)
	{
		this.name = name;
		this.targetNames = targetNames;
		this.combatLevels = new int[0];
		this.slayerXp = -1;
	}

	Variant(String name, int[] combatLevels)
	{
		this.name = name;
		this.targetNames = new String[]{};
		this.combatLevels = combatLevels;
		this.slayerXp = -1;
	}

	Variant(String displayAndTargetName, int slayerXp)
	{
		this.name = displayAndTargetName;
		this.targetNames = new String[]{displayAndTargetName};
		this.combatLevels = new int[0];
		this.slayerXp = slayerXp;
	}

	Variant(String name, int slayerXp, String... targetNames)
	{
		this.name = name;
		this.targetNames = targetNames;
		this.combatLevels = new int[0];
		this.slayerXp = slayerXp;
	}

	Variant(String name, int slayerXp, int[] combatLevels)
	{
		this.name = name;
		this.targetNames = new String[]{};
		this.combatLevels = combatLevels;
		this.slayerXp = slayerXp;
	}
}