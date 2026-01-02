/*
 * Copyright (c) 2017, Tyler <https://github.com/tylerthardy>
 * Copyright (c) 2018, Shaun Dreclin <shaundreclin@gmail.com>
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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.gameval.ItemID;

@Getter
public enum Assignment
{
	//<editor-fold desc="Enums">
	ABERRANT_SPECTRES(
		"Aberrant spectres",
		ItemID.SLAYERGUIDE_ABERRANTSPECTER,
		Variant.of("ABERRANT_SPECTRE",
			"Aberrant spectre",
			Match.byNpcNames("Aberrant spectre", "Abhorrent spectre")
		),
		Variant.of("DEVIANT_SPECTRE",
			"Deviant spectre",
			Match.byNpcNames("Deviant spectre", "Repugnant spectre")
		)
	),
	ABYSSAL_DEMONS(
		"Abyssal demons",
		ItemID.SLAYERGUIDE_ABYSSALDEMON,
		Variant.of("ABYSSAL_DEMON", "Abyssal demon", Match.byNpcNames("Abyssal demon")),
		Variant.of("SIRE", "Abyssal sire", Match.byNpcNames("Abyssal sire"))
	),
	ABYSSAL_SIRE("The Abyssal Sire", ItemID.ABYSSALSIRE_PET),
	ALCHEMICAL_HYDRA("The Alchemical Hydra", ItemID.HYDRAPET),
	ANKOU(
		"Ankou",
		ItemID.ANKOU_HEAD,
		Variant.of("ANKOU_NORMAL", "Normal", Match.byCombatLevels(75, 82, 86)),
		Variant.of("ANKOU_CATACOMBS", "Catacombs", Match.byCombatLevels(95)),
		Variant.of("ANKOU_WILDY_CAVE", "Wildy Slayer Cave", Match.byCombatLevels(98))
	),
	ARAXXOR("Araxxor", ItemID.ARAXXORPET),
	ARAXYTES("Araxytes", ItemID.POH_ARAXYTE_HEAD),
	AVIANSIES(
		"Aviansies",
		ItemID.ARCEUUS_CORPSE_AVIANSIE_INITIAL,
		Variant.of("AVIANSIE_NORMAL", "Aviansie", Match.byNpcNames("Aviansie")),
		Variant.of("KREEARRA", "Kree'arra", Match.byCombatLevels(580, 159, 149, 143))
	),
	BANDITS("Bandits", ItemID.PICKPOCKET_GUIDE_DESERT_BANDIT),
	BANSHEES(
		"Banshees",
		ItemID.SLAYERGUIDE_BANSHEE,
		Variant.of("BANSHEE_SLAYER_TOWER", "Slayer Tower", Match.byCombatLevels(23)),
		Variant.of("BANSHEE_CATACOMBS", "Catacombs", Match.byCombatLevels(89))
	),
	BARROWS_BROTHERS("Barrows Brothers", ItemID.BARROWS_KARIL_HEAD),
	BASILISKS(
		"Basilisks",
		ItemID.SLAYERGUIDE_BASILISK,
		Variant.of("BASILISK_NORMAL", "Basilisk", Match.byNpcNames("Basilisk")),
		Variant.of("BASILISK_KNIGHT", "Basilisk Knight", Match.byNpcNames("Basilisk Knight"))
	),
	BATS(
		"Bats",
		ItemID.RAIDS_BAT2_COOKED,
		Variant.of("BAT_NORMAL", "Bat", Match.byNpcNames("Bat", "Giant bat")),
		Variant.of("BAT_ALBINO", "Albino bat", Match.byNpcNames("Albino bat")),
		Variant.of("DEATHWING", "Deathwing", Match.byNpcNames("Deathwing"))
	),
	BEARS(
		"Bears",
		ItemID.ARCEUUS_CORPSE_BEAR_INITIAL,
		Variant.of("BEAR_NORMAL", "Normal", Match.byNpcNames("Bear cub", "Black bear", "Grizzly bear", "Grizzly bear cub")),
		Variant.of("CALLISTO", "Callisto", Match.byNpcNames("Callisto"))
	),
	BIRDS("Birds", ItemID.FEATHER),
	BLACK_DEMONS(
		"Black demons",
		ItemID.BLACK_DEMON_MASK,
		Variant.of("BLACK_DEMON", "Black demon", Match.byNpcNames("Black demon")),
		Variant.of("BLACK_DEMON_GORILLA", "Demonic gorilla", Match.byNpcNames("Demonic gorilla"))
	),
	BLACK_DRAGONS(
		"Black dragons",
		ItemID.DRAGONMASK_BLACK,
		Variant.of("BLACK_DRAG_NORMAL", "Black dragon", Match.byNpcNames("Black dragon")),
		Variant.of("BLACK_DRAG_BABY", "Baby black dragon", Match.byNpcNames("Baby black dragon")),
		Variant.of("BLACK_DRAG_BRUTAL", "Brutal black dragon", Match.byNpcNames("Brutal black dragon"))
	),
	BLACK_KNIGHTS("Black Knights", ItemID.BLACK_FULL_HELM),
	BLOODVELD(
		"Bloodveld",
		ItemID.SLAYERGUIDE_BLOODVELD,
		Variant.of("BLOODVELD_WEAK", "(level-76)", Match.byCombatLevels(76)),
		Variant.of("BLOODVELD_STRONG", "God Wars Dungeon (level-81)", Match.byCombatLevels(81)),
		Variant.of("MUTATED_BLOODVELD", "Mutated Bloodveld", Match.byNpcNames("Mutated Bloodveld"))
	),
	BLUE_DRAGONS(
		"Blue dragons",
		ItemID.DRAGONMASK_BLUE,
		Variant.of("BLUE_DRAG_NORMAL", "Blue dragon", Match.byNpcNames("Blue dragon")),
		Variant.of("BLUE_DRAG_BABY", "Baby blue dragon", Match.byNpcNames("Baby blue dragon")),
		Variant.of("BLUE_DRAG_BRUTAL", "Brutal blue dragon", Match.byNpcNames("Brutal blue dragon")),
		Variant.of("VORKATH", "Vorkath", Match.byNpcNames("Vorkath"))
	),
	BRINE_RATS("Brine rats", ItemID.OLAF2_BRINE_RAT_INV),
	CALLISTO("Callisto", ItemID.CALLISTO_PET),
	CATABLEPON("Catablepon", ItemID.SOS_HALF_SKULL2),
	CAVE_BUGS("Cave bugs", ItemID.SWAMP_CAVE_BUG),
	CAVE_CRAWLERS(
		"Cave crawlers",
		ItemID.SLAYERGUIDE_CAVECRAWLER,
		Variant.of("SWAMP", "Swamp", Match.byCombatLevels(23))),
	CAVE_HORRORS("Cave horrors", ItemID.SLAYERGUIDE_HARMLESS_CAVE_HORROR),
	CAVE_KRAKEN(
		"Cave kraken",
		ItemID.CERT_EADGAR_FADE_TO_BLACK_INV,
		Variant.of("CAVE_KRAKEN", "Cave kraken", Match.byNpcNames("Cave kraken")),
		Variant.of("KRAKEN", "Kraken", Match.byNpcNames("Kraken"))
	),
	CAVE_SLIMES("Cave slimes", ItemID.SWAMP_CAVE_SLIME),
	CERBERUS("Cerberus", ItemID.HELL_PET),
	CHAOS_DRUIDS(
		"Chaos druids",
		ItemID.ELDERCHAOS_HOOD,
		Variant.of("CHAOS_DRUIDS", "Chaos druid", Match.byNpcNames("Chaos druid")),
		Variant.of("CHAOS_DRUIDS_ELDER", "Elder", Match.byNpcNames("Elder chaos druid"))
	),
	CHAOS_ELEMENTAL("The Chaos Elemental", ItemID.CHAOSELEPET),
	CHAOS_FANATIC("The Chaos Fanatic", ItemID.STAFF_OF_ZAROS),
	COCKATRICE("Cockatrice", ItemID.SLAYERGUIDE_COCKATRICE),
	COWS("Cows", ItemID.COW_MASK),

	CRABS("Crabs", ItemID.HUNDRED_PIRATE_CRAB_SHELL_GAUNTLET),

	CRAWLING_HANDS("Crawling hands", ItemID.SLAYERGUIDE_CRAWLINGHAND),
	CRAZY_ARCHAEOLOGIST("Crazy Archaeologists", ItemID.FEDORA),
	CROCODILES("Crocodiles", ItemID.GREEN_SALAMANDER),

	CUSTODIAN_STALKERS("Custodian Stalkers", ItemID.SLAYERGUIDE_CUSTODIAN_STALKER_MATURE),

	DAGANNOTH(
		"Dagannoth",
		ItemID.POH_DAGGANOTH,
		Variant.of("DAGANNOTH_NORMAL", "(level-74/92)", Match.byCombatLevels(74, 92)),
		Variant.of("DAGANNOTH_WATERBIRTH", "Waterbirth", Match.byCombatLevels(42, 70, 88, 90)),
		Variant.of("DAGANNOTH_KINGS", "Kings", Match.byNpcNames("Dagannoth Rex", "Dagannoth Prime", "Dagannoth Supreme"))
	),
	DAGANNOTH_KINGS("Dagannoth Kings", ItemID.PRIMEPET),
	DARK_BEASTS("Dark beasts", ItemID.SLAYERGUIDE_DARK_BEAST),
	DARK_WARRIORS(
		"Dark warriors",
		ItemID.BLACK_MED_HELM,
		Variant.of("DARK_WARRIOR_FORTRESS", "Fortress", Match.byCombatLevels(8, 145)),
		Variant.of("DARK_WARRIOR_KOUREND", "Kourend", Match.byCombatLevels(37, 51, 62))
	),
	DERANGED_ARCHAEOLOGIST("Deranged Archaeologist", ItemID.FOSSIL_SWAMP_DIARY),
	DOGS("Dogs", ItemID.POH_GUARD_DOG),
	DRAKES("Drakes", ItemID.SLAYERGUIDE_DRAKE),

	DUKE_SUCELLUS("Duke Sucellus", ItemID.DUKESUCELLUSPET),

	DUST_DEVILS(
		"Dust devils",
		ItemID.SLAYERGUIDE_DUSTDEVIL,
		Variant.of("DUST_DEVIL_WEAK", "Smoke Dungeon", Match.byCombatLevels(93)),
		Variant.of("DUST_DEVIL_STRONG", "Catacombs of Kourend", Match.byCombatLevels(110))
	),
	DWARVES("Dwarves", ItemID.GRIM_WEAR_HELMET),
	EARTH_WARRIORS("Earth warriors", ItemID.BRONZE_FULL_HELM_TRIM),

	ELVES("Elves", ItemID.PICKPOCKET_GUIDE_WOODELF),

	ENTS("Ents", ItemID.POH_TREE_2),
	FEVER_SPIDERS("Fever spiders", ItemID.SLAYERGUIDE_FEVER_SPIDER),
	FIRE_GIANTS(
		"Fire giants",
		ItemID.RTBRANDAPET,
		Variant.of("FIRE_GIANT_WEAK", "(level-86)", Match.byCombatLevels(86)),
		Variant.of("FIRE_GIANT_STRONG", "Catacombs of Kourend", Match.byCombatLevels(104, 109))
	),
	FLESH_CRAWLERS("Fleshcrawlers", ItemID.ARCEUUS_CORPSE_SCORPION_INITIAL),
	FOSSIL_ISLAND_WYVERNS("Fossil island wyverns", ItemID.SLAYERGUIDE_FOSSILWYVERN),
	GARGOYLES(
		"Gargoyles",
		ItemID.SLAYERGUIDE_GARGOYLE,
		Variant.of("GARGOYLES", "Gargoyle", Match.byNpcNames("Gargoyle")),
		Variant.of("GROTESQUE_GUARDIANS", "Grotesque guardians", 1350, Match.byNpcNames("Dusk"))
	),
	GENERAL_GRAARDOR("General Graardor", ItemID.BANDOSPET),
	GHOSTS("Ghosts", ItemID.AMULET_OF_GHOSTSPEAK),
	GHOULS("Ghouls", ItemID.TRICK_OR_TREAT_HEAD),
	GIANT_MOLE("The Giant Mole", ItemID.MOLEPET),
	GOBLINS("Goblins", ItemID.ARCEUUS_CORPSE_GOBLIN_INITIAL),
	GREATER_DEMONS(
		"Greater demons",
		ItemID.GREATER_DEMON_MASK,
		Variant.of("GREATER_DEMON_NORMAL", "Normal", Match.byCombatLevels(92)),
		Variant.of("GREATER_DEMON_CATACOMBS", "Catacombs", Match.byCombatLevels(100, 101, 113)),
		Variant.of("GREATER_DEMON_WILDY_CAVE", "Wildy Slayer Cave", Match.byCombatLevels(104)),
		Variant.of("KRIL", "K'ril Tsutsaroth", Match.byCombatLevels(145, 650)),
		Variant.of("SKOTIZO", "Skotizo", Match.byNpcNames("Skotizo"))
	),
	GREEN_DRAGONS(
		"Green dragons",
		ItemID.DRAGONMASK_GREEN,
		Variant.of("GREEN_DRAG_NORMAL", "Green dragon", Match.byNpcNames("Green dragon")),
		Variant.of("GREEN_DRAG_BABY", "Baby green dragon", Match.byNpcNames("Baby green dragon")),
		Variant.of("GREEN_DRAG_BRUTAL", "Brutal green dragon", Match.byNpcNames("Brutal green dragon"))
	),
	GROTESQUE_GUARDIANS("The Grotesque Guardians", ItemID.DUSKPET),
	HARPIE_BUG_SWARMS("Harpie bug swarms", ItemID.SLAYERGUIDE_SWARM),
	HELLHOUNDS(
		"Hellhounds",
		ItemID.POH_HELLHOUND,
		Variant.of("HELLHOUND", "Hellhound", Match.byNpcNames("Hellhound")),
		Variant.of("CERBERUS", "Cerberus", Match.byNpcNames("Cerberus"))
	),
	HILL_GIANTS("Hill giants", ItemID.ARCEUUS_CORPSE_GIANT_INITIAL),
	HOBGOBLINS("Hobgoblins", ItemID.POH_HOBGOBLIN),
	HYDRAS(
		"Hydras",
		ItemID.SLAYERGUIDE_HYDRA,
		Variant.of("HYDRA", "Hydra", Match.byNpcNames("Hydra")),
		Variant.of("ALCHEMICAL", "Alchemical Hydra", Match.byNpcNames("Alchemical Hydra"))
	),
	ICEFIENDS("Icefiends", ItemID.FD_ICEDIAMOND),
	ICE_GIANTS(
		"Ice giants",
		ItemID.RTELDRICPET,
		Variant.of("ICE_GIANT_NORMAL", "Normal", Match.byCombatLevels(53)),
		Variant.of("ICE_GIANT_WILDY_CAVE", "Wildy Slayer Cave", Match.byCombatLevels(67))
	),
	ICE_WARRIORS(
		"Ice warriors",
		ItemID.MITHRIL_FULL_HELM_TRIM,
		Variant.of("ICE_WARRIOR", "Ice warrior", Match.byNpcNames("Ice warrior")),
		Variant.of("ICELORD", "Icelord", Match.byNpcNames("Icelord"))
	),
	INFERNAL_MAGES("Infernal mages", ItemID.SLAYERGUIDE_INFERNALMAGE),
	JAD(
		"TzTok-Jad",
		ItemID.JAD_PET,
		Variant.of("JAD", "Jad", 25250, Match.byNpcNames("TzTok-Jad")),
		Variant.of("FIGHT_CAVE_OTHERS", "Other", Match.byNpcNames("Tz-Kih", "Tz-Kek", "Tok-Xil", "Yt-MekKok", "Ket-Zek", "Yt-HurKot"))
	),
	JELLIES(
		"Jellies",
		ItemID.SLAYERGUIDE_JELLY,
		Variant.of("JELLY", "Jelly", Match.byNpcNames("Jelly")),
		Variant.of("WARPED_JELLY", "Warped jelly", Match.byNpcNames("Warped jelly"))
	),
	JUNGLE_HORROR("Jungle horrors", ItemID.ARCEUUS_CORPSE_HORROR_INITIAL),
	KALPHITE(
		"Kalphites",
		ItemID.POH_KALPHITE_SOLDIER,
		Variant.of("KALPHITE_WORKER", "Worker", Match.byNpcNames("Kalphite Worker")),
		Variant.of("KALPHITE_SOLDIER", "Soldier", Match.byNpcNames("Kalphite Soldier")),
		Variant.of("KALPHITE_GUARDIAN", "Guardian", Match.byNpcNames("Kalphite Guardian")),
		Variant.of("KALPHITE_QUEEN", "Queen", Match.byNpcNames("Kalphite Queen"))
	),
	KALPHITE_QUEEN("The Kalphite Queen", ItemID.KQPET_WALKING),
	KILLERWATTS("Killerwatts", ItemID.SLAYERGUIDE_KILLERWATT),
	KING_BLACK_DRAGON("The King Black Dragon", ItemID.KBDPET),
	KRAKEN("The Cave Kraken Boss", ItemID.KRAKENPET),
	KREEARRA("Kree'arra", ItemID.ARMADYLPET),
	KRIL_TSUTSAROTH("K'ril Tsutsaroth", ItemID.ZAMORAKPET),
	KURASK("Kurask", ItemID.SLAYERGUIDE_KURASK),
	LAVA_DRAGONS("Lava Dragons", ItemID.LAVA_SCALE),
	LESSER_DEMONS(
		"Lesser demons",
		ItemID.LESSER_DEMON_MASK,
		Variant.of("LESSER_DEMON_NORMAL", "Normal", Match.byCombatLevels(82)),
		Variant.of("LESSER_DEMON_WILDY_CATACOMBS", "Catacombs/Wildy Cave", Match.byCombatLevels(87, 94))
	),

	LESSER_NAGUA("Lesser Nagua", ItemID.SLAYERGUIDE_LESSER_NAGUA),

	LIZARDMEN(
		"Lizardmen",
		ItemID.LIZARDMAN_FANG,
		Variant.of("LIZARDMAN", "Lizardman", Match.byNpcNames("Lizardman")),
		Variant.of("LIZARDMAN_BRUTE", "Lizardman brute", Match.byNpcNames("Lizardman brute")),
		Variant.of("LIZARDMAN_SHAMAN", "Lizardman shaman", Match.byNpcNames("Lizardman shaman"))
	),
	LIZARDS(
		"Lizards",
		ItemID.SLAYERGUIDE_LIZARD,
		Variant.of("LIZARD_DESERT", "Desert", Match.byNpcNames("Lizard", "Desert lizard", "Small lizard")),
		Variant.of("LIZARD_SULFUR", "Sulfur", Match.byNpcNames("Sulfur lizard"))
	),
	MAGIC_AXES("Magic axes", ItemID.IRON_BATTLEAXE),
	MAMMOTHS("Mammoths", ItemID.BARBASSAULT_ATT_HORN_01),

	METAL_DRAGONS("Metal dragons", ItemID.POH_STEEL_DRAGON),

	MINIONS_OF_SCABARAS("Minions of scabaras", ItemID.NTK_SCARAB_GOLD),
	MINOTAURS("Minotaurs", ItemID.ARCEUUS_CORPSE_MINOTAUR_INITIAL),
	MOGRES("Mogres", ItemID.SLAYERGUIDE_MOGRE),
	MOLANISKS("Molanisks", ItemID.SLAYERGUIDE_MOLANISK),
	MONKEYS(
		"Monkeys",
		ItemID.ARCEUUS_CORPSE_MONKEY_INITIAL,
		Variant.of("MONKEY_NORMAL", "Normal", Match.byNpcNames("Monkey")),
		Variant.of("MONKEY_APE_ATOLL", "Ape Atoll", Match.byNpcNames("Monkey guard", "Monkey archer", "Monkey zombie", "Padulah"))
	),
	MOSS_GIANTS("Moss giants", ItemID.MOSSY_KEY),
	MUTATED_ZYGOMITES(
		"Mutated zygomites",
		ItemID.SLAYER_ZYGOMITE_OBJECT,
		Variant.of("ZYGOMITE_MUTATED", "Mutated zygomite", Match.byCombatLevels(74, 86)),
		Variant.of("ZYGOMITE_ANCIENT", "Ancient zygomite", Match.byCombatLevels(109))
	),
	NECHRYAEL("Nechryael", ItemID.SLAYERGUIDE_NECHRYAEL),
	OGRES("Ogres", ItemID.ARCEUUS_CORPSE_OGRE_INITIAL),
	OTHERWORLDLY_BEING("Otherworldly beings", ItemID.SECRET_GHOST_HAT),

	PHANTOM_MUSPAH("The Phantom Muspah", ItemID.MUSPAHPET),

	PIRATES("Pirates", ItemID.BREW_RED_PIRATE_HAT),
	PYREFIENDS(
		"Pyrefiends",
		ItemID.SLAYERGUIDE_PYRFIEND,
		Variant.of("PYREFIEND", "Pyrefiend", Match.byNpcNames("Pyrefiend")),
		Variant.of("PYRELORD", "Pyrelord", Match.byNpcNames("Pyrelord"))
	),
	RATS("Rats", ItemID.RATS_TAIL),
	RED_DRAGONS(
		"Red dragons",
		ItemID.POH_DRAGON,
		Variant.of("RED_DRAGON_NORMAL", "Red dragon", Match.byNpcNames("Red dragon")),
		Variant.of("RED_DRAGON_BABY", "Baby red dragon", Match.byNpcNames("Baby red dragon")),
		Variant.of("RED_DRAGON_BRUTAL", "Brutal red dragon", Match.byNpcNames("Brutal red dragon"))
	),
	REVENANTS("Revenants", ItemID.WILD_CAVE_BRACELET_CHARGED),
	ROCKSLUGS("Rockslugs", ItemID.SLAYERGUIDE_ROCKSLUG),
	ROGUES("Rogues", ItemID.ROGUESDEN_HELM),
	SARACHNIS("Sarachnis", ItemID.SARACHNISPET),
	SCORPIA("Scorpia", ItemID.SCORPIA_PET),
	SCORPIONS("Scorpions", ItemID.ARCEUUS_CORPSE_SCORPION_INITIAL),
	SEA_SNAKES("Sea snakes", ItemID.HUNDRED_ILM_SNAKE_CORPSE),
	SHADES("Shades", ItemID.BLACKROBETOP),
	SHADOW_WARRIORS("Shadow warriors", ItemID.BLACK_FULL_HELM),
	SKELETAL_WYVERNS("Skeletal wyverns", ItemID.SLAYERGUIDE_SKELETALWYVERN),
	SKELETONS(
		"Skeletons",
		ItemID.POH_SKELETON_GUARD,
		Variant.of("SKELETON", "Skeleton", Match.byNpcNames("Skeleton")),
		Variant.of("VETION", "Vet'ion", Match.byCombatLevels(454))
	),
	SMOKE_DEVILS(
		"Smoke devils",
		ItemID.CERT_GUIDE_ICON_DUMMY,
		Variant.of("SMOKE_DEVIL_NORMAL", "Smoke devil", Match.byCombatLevels(160, 280)),
		Variant.of("THERMONUCLEAR", "Thermonuclear", Match.byCombatLevels(301))
	),
	SOURHOGS("Sourhogs", ItemID.PORCINE_SOURHOG_TROPHY),
	SPIDERS("Spiders", ItemID.POH_SPIDER),
	SPIRITUAL_CREATURES("Spiritual creatures", ItemID.DRAGON_BOOTS),
	SUQAHS("Suqahs", ItemID.SUQKA_TOOTH),
	TERROR_DOGS("Terror dogs", ItemID.SLAYERGUIDE_TERRORDOG),

	THE_LEVIATHAN("The Leviathan", ItemID.LEVIATHANPET),
	THE_WHISPERER("The Whisperer", ItemID.WHISPERERPET),

	THERMONUCLEAR_SMOKE_DEVIL("The Thermonuclear Smoke Devil", ItemID.SMOKEPET),
	TROLLS(
		"Trolls",
		ItemID.POH_TROLL,
		Variant.of("ICE_TROLL", "Ice troll", Match.byNpcNames("Ice troll", "Ice troll runt", "Ice troll grunt", "Ice troll male", "Ice troll female")),
		Variant.of("MOUNTAIN_TROLL", "Mountain troll", Match.byNpcNames("Mountain troll"))
	),
	TUROTH(
		"Turoth",
		ItemID.SLAYERGUIDE_TUROTH,
		Variant.of("TUROTH_SMALL", "Small", Match.byCombatLevels(83, 85)),
		Variant.of("TUROTH_LARGE", "Large", Match.byCombatLevels(87, 89))
	),
	TZHAAR(
		"Tzhaar",
		ItemID.ARCEUUS_CORPSE_TZHAAR_INITIAL,
		Variant.of("TZHAAR_KET", "Tzhaar-Ket", Match.byNpcNames("Tzhaar-Ket")),
		Variant.of("TZHAAR_XIL", "Tzhaar-Xil", Match.byNpcNames("Tzhaar-Xil")),
		Variant.of("TZHAAR_MEJ", "Tzhaar-Mej", Match.byNpcNames("Tzhaar-Mej")),
		Variant.of("TZHAAR_HUR", "Tzhaar-Hur", Match.byNpcNames("Tzhaar-Hur")),
		Variant.of("JAD", "Jad", 25250, Match.byNpcNames("TzTok-Jad"))
	),
	VAMPYRES("Vampyres", ItemID.STAKE),

	VARDORVIS("Vardorvis", ItemID.VARDORVISPET),

	VENENATIS("Venenatis", ItemID.VENENATIS_PET),
	VETION("Vet'ion", ItemID.VETION_PET),
	VORKATH("Vorkath", ItemID.VORKATHPET),
	WALL_BEASTS("Wall beasts", ItemID.SWAMP_WALLBEAST),

	WARPED_CREATURES("Warped Creatures", ItemID.POG_SLAYER_DUMMY_WARPED_TERRORBIRD),

	WATERFIENDS("Waterfiends", ItemID.WATER_ORB),
	WEREWOLVES("Werewolves", ItemID.DAGGER_WOLFBANE),
	WOLVES("Wolves", ItemID.GREY_WOLF_FUR),
	WYRMS("Wyrms", ItemID.SLAYERGUIDE_WYRM),
	ZILYANA("Commander Zilyana", ItemID.SARADOMINPET),
	ZOMBIES("Zombies", ItemID.TRICK_OR_TREAT_HEAD),
	ZUK("TzKal-Zuk", ItemID.INFERNOPET_ZUK),
	ZULRAH("Zulrah", ItemID.SNAKEPET);
	//</editor-fold>

	private static final Map<String, Assignment> assignment;

	private final String name;
	private final int itemSpriteId;
	private final Variant[] variants;

	static
	{
		ImmutableMap.Builder<String, Assignment> builder = new ImmutableMap.Builder<>();

		for (Assignment assignment : values())
		{
			builder.put(assignment.getName().toLowerCase(), assignment);
		}

		assignment = builder.build();
	}

	Assignment(String name, int itemSpriteId, Variant... variants)
	{
		Preconditions.checkArgument(itemSpriteId >= 0);
		this.name = name;
		this.itemSpriteId = itemSpriteId;
		this.variants = variants;
	}

	public static Optional<Assignment> getAssignmentByName(String name)
	{
		if (name == null)
		{
			return Optional.empty();
		}

		String nameLowerCase = name.toLowerCase();
		if (assignment.containsKey(nameLowerCase))
		{
			return Optional.of(assignment.get(nameLowerCase));
		}
		else
		{
			return Optional.empty();
		}
	}

	public Optional<Variant> getVariantById(String id)
	{
		if (id == null)
		{
			return Optional.empty();
		}

		return Arrays.stream(variants)
			.filter(variant -> variant.getId().equalsIgnoreCase(id))
			.findFirst();
	}

	public Optional<Variant> getVariantMatchingNpc(NPC npc)
	{
		final NPCComposition composition = npc.getTransformedComposition();
		if (composition == null)
		{
			return Optional.empty();
		}

		return Arrays.stream(this.getVariants())
			.filter(variant -> variant.matches(npc))
			.findAny();
	}
}