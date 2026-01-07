/*
 * Copyright (c) 2017, Tyler <https://github.com/tylerthardy>
 * Copyright (c) 2018, Shaun Dreclin <shaundreclin@gmail.com>
 * Copyright (c) 2026, Jeremy Bahadirli <https://github.com/jeremybahadirli>
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
import java.util.Arrays;
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
		Variant.of("Aberrant spectre", NpcPredicates.byName("Aberrant spectre", "Abhorrent spectre")),
		Variant.of("Deviant spectre", NpcPredicates.byName("Deviant spectre", "Repugnant spectre"))
	),
	ABYSSAL_DEMONS(
		"Abyssal demons",
		ItemID.SLAYERGUIDE_ABYSSALDEMON,
		Variant.of("Abyssal demon", NpcPredicates.byName("Abyssal demon")),
		Variant.of("Abyssal sire", NpcPredicates.byName("Abyssal sire"))
	),
	ABYSSAL_SIRE("The Abyssal Sire", ItemID.ABYSSALSIRE_PET),
	ALCHEMICAL_HYDRA("The Alchemical Hydra", ItemID.HYDRAPET),
	ANKOU(
		"Ankou",
		ItemID.ANKOU_HEAD,
		Variant.of("Normal", NpcPredicates.byCombatLevel(75, 82, 86)),
		Variant.of("Catacombs", NpcPredicates.byCombatLevel(95)),
		Variant.of("Wildy Slayer Cave", NpcPredicates.byCombatLevel(98))
	),
	ARAXXOR("Araxxor", ItemID.ARAXXORPET),
	ARAXYTES("Araxytes", ItemID.POH_ARAXYTE_HEAD),
	AVIANSIES(
		"Aviansies",
		ItemID.ARCEUUS_CORPSE_AVIANSIE_INITIAL,
		Variant.of("Aviansie", NpcPredicates.byName("Aviansie")),
		Variant.of("Kree'arra", NpcPredicates.byCombatLevel(580, 159, 149, 143))
	),
	BANDITS("Bandits", ItemID.PICKPOCKET_GUIDE_DESERT_BANDIT),
	BANSHEES(
		"Banshees",
		ItemID.SLAYERGUIDE_BANSHEE,
		Variant.of("Slayer Tower", NpcPredicates.byCombatLevel(23)),
		Variant.of("Catacombs", NpcPredicates.byCombatLevel(89))
	),
	BARROWS_BROTHERS("Barrows Brothers", ItemID.BARROWS_KARIL_HEAD),
	BASILISKS(
		"Basilisks",
		ItemID.SLAYERGUIDE_BASILISK,
		Variant.of("Basilisk", NpcPredicates.byName("Basilisk")),
		Variant.of("Basilisk Knight", NpcPredicates.byName("Basilisk Knight"))
	),
	BATS(
		"Bats",
		ItemID.RAIDS_BAT2_COOKED,
		Variant.of("Bat", NpcPredicates.byName("Bat", "Giant bat")),
		Variant.of("Albino bat", NpcPredicates.byName("Albino bat")),
		Variant.of("Deathwing", NpcPredicates.byName("Deathwing"))
	),
	BEARS(
		"Bears",
		ItemID.ARCEUUS_CORPSE_BEAR_INITIAL,
//		Variant.of("Normal", NpcPredicate.byName("Bear cub", "Black bear", "Grizzly bear", "Grizzly bear cub")),
		Variant.of("Normal", NpcPredicates.byNameContaining("Bear")),
		Variant.of("Callisto", NpcPredicates.byName("Callisto"))
	),
	BIRDS("Birds", ItemID.FEATHER),
	BLACK_DEMONS(
		"Black demons",
		ItemID.BLACK_DEMON_MASK,
		Variant.of("Black demon", NpcPredicates.byName("Black demon")),
		Variant.of("Demonic gorilla", NpcPredicates.byName("Demonic gorilla"))
	),
	BLACK_DRAGONS(
		"Black dragons",
		ItemID.DRAGONMASK_BLACK,
		Variant.of("Black dragon", NpcPredicates.byName("Black dragon")),
		Variant.of("Baby black dragon", NpcPredicates.byName("Baby black dragon")),
		Variant.of("Brutal black dragon", NpcPredicates.byName("Brutal black dragon"))
	),
	BLACK_KNIGHTS("Black Knights", ItemID.BLACK_FULL_HELM),
	BLOODVELD(
		"Bloodveld",
		ItemID.SLAYERGUIDE_BLOODVELD,
		Variant.of("(level-76)", NpcPredicates.byCombatLevel(76)),
		Variant.of("God Wars Dungeon (level-81)", NpcPredicates.byCombatLevel(81)),
		Variant.of("Mutated Bloodveld", NpcPredicates.byName("Mutated Bloodveld"))
	),
	BLUE_DRAGONS(
		"Blue dragons",
		ItemID.DRAGONMASK_BLUE,
		Variant.of("Blue dragon", NpcPredicates.byName("Blue dragon")),
		Variant.of("Baby blue dragon", NpcPredicates.byName("Baby blue dragon")),
		Variant.of("Brutal blue dragon", NpcPredicates.byName("Brutal blue dragon")),
		Variant.of("Vorkath", NpcPredicates.byName("Vorkath"))
	),
	BRINE_RATS("Brine rats", ItemID.OLAF2_BRINE_RAT_INV),
	CALLISTO("Callisto", ItemID.CALLISTO_PET),
	CATABLEPON("Catablepon", ItemID.SOS_HALF_SKULL2),
	CAVE_BUGS("Cave bugs", ItemID.SWAMP_CAVE_BUG),
	CAVE_CRAWLERS(
		"Cave crawlers",
		ItemID.SLAYERGUIDE_CAVECRAWLER,
		Variant.of("Swamp", NpcPredicates.byCombatLevel(23))),
	CAVE_HORRORS("Cave horrors", ItemID.SLAYERGUIDE_HARMLESS_CAVE_HORROR),
	CAVE_KRAKEN(
		"Cave kraken",
		ItemID.CERT_EADGAR_FADE_TO_BLACK_INV,
		Variant.of("Cave kraken", NpcPredicates.byName("Cave kraken")),
		Variant.of("Kraken", NpcPredicates.byName("Kraken"))
	),
	CAVE_SLIMES("Cave slimes", ItemID.SWAMP_CAVE_SLIME),
	CERBERUS("Cerberus", ItemID.HELL_PET),
	CHAOS_DRUIDS(
		"Chaos druids",
		ItemID.ELDERCHAOS_HOOD,
		Variant.of("Chaos druid", NpcPredicates.byName("Chaos druid")),
		Variant.of("Elder", NpcPredicates.byName("Elder chaos druid"))
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
		Variant.of("(level-74/92)", NpcPredicates.byCombatLevel(74, 92)),
		Variant.of("Waterbirth", NpcPredicates.byCombatLevel(42, 70, 88, 90)),
		Variant.of("Kings", NpcPredicates.byName("Dagannoth Rex", "Dagannoth Prime", "Dagannoth Supreme"))
	),
	DAGANNOTH_KINGS("Dagannoth Kings", ItemID.PRIMEPET),
	DARK_BEASTS("Dark beasts", ItemID.SLAYERGUIDE_DARK_BEAST),
	DARK_WARRIORS(
		"Dark warriors",
		ItemID.BLACK_MED_HELM,
		Variant.of("Fortress", NpcPredicates.byCombatLevel(8, 145)),
		Variant.of("Kourend", NpcPredicates.byCombatLevel(37, 51, 62))
	),
	DERANGED_ARCHAEOLOGIST("Deranged Archaeologist", ItemID.FOSSIL_SWAMP_DIARY),
	DOGS("Dogs", ItemID.POH_GUARD_DOG),
	DRAKES("Drakes", ItemID.SLAYERGUIDE_DRAKE),

	DUKE_SUCELLUS("Duke Sucellus", ItemID.DUKESUCELLUSPET),

	DUST_DEVILS(
		"Dust devils",
		ItemID.SLAYERGUIDE_DUSTDEVIL,
		Variant.of("Smoke Dungeon", NpcPredicates.byCombatLevel(93)),
		Variant.of("Catacombs of Kourend", NpcPredicates.byCombatLevel(110))
	),
	DWARVES("Dwarves", ItemID.GRIM_WEAR_HELMET),
	EARTH_WARRIORS("Earth warriors", ItemID.BRONZE_FULL_HELM_TRIM),

	ELVES("Elves", ItemID.PICKPOCKET_GUIDE_WOODELF,
		Variant.of(
			"Guard",
			NpcPredicates.byName("Iorwerth Archer", "Elf Archer", "Iorwerth Warrior", "Elf Warrior", "Mourner", "Reanimated Elf")
				.or(NpcPredicates.byName("Guard").and(NpcPredicates.byCombatLevel(108)))
		)
	),

	ENTS("Ents", ItemID.POH_TREE_2),
	FEVER_SPIDERS("Fever spiders", ItemID.SLAYERGUIDE_FEVER_SPIDER),
	FIRE_GIANTS(
		"Fire giants",
		ItemID.RTBRANDAPET,
		Variant.of("(level-86)", NpcPredicates.byCombatLevel(86)),
		Variant.of("Catacombs of Kourend", NpcPredicates.byCombatLevel(104, 109))
	),
	FLESH_CRAWLERS("Fleshcrawlers", ItemID.ARCEUUS_CORPSE_SCORPION_INITIAL),
	FOSSIL_ISLAND_WYVERNS("Fossil island wyverns", ItemID.SLAYERGUIDE_FOSSILWYVERN),
	GARGOYLES(
		"Gargoyles",
		ItemID.SLAYERGUIDE_GARGOYLE,
		Variant.of("Gargoyle", NpcPredicates.byName("Gargoyle")),
		Variant.of("Grotesque guardians", 1350, NpcPredicates.byName("Dusk"))
	),
	GENERAL_GRAARDOR("General Graardor", ItemID.BANDOSPET),
	GHOSTS("Ghosts", ItemID.AMULET_OF_GHOSTSPEAK),
	GHOULS("Ghouls", ItemID.TRICK_OR_TREAT_HEAD),
	GIANT_MOLE("The Giant Mole", ItemID.MOLEPET),
	GOBLINS("Goblins", ItemID.ARCEUUS_CORPSE_GOBLIN_INITIAL),
	GREATER_DEMONS(
		"Greater demons",
		ItemID.GREATER_DEMON_MASK,
		Variant.of("Normal", NpcPredicates.byCombatLevel(92)),
		Variant.of("Catacombs", NpcPredicates.byCombatLevel(100, 101, 113)),
		Variant.of("Wildy Slayer Cave", NpcPredicates.byCombatLevel(104)),
		Variant.of("K'ril Tsutsaroth", NpcPredicates.byCombatLevel(145, 650)),
		Variant.of("Skotizo", NpcPredicates.byName("Skotizo"))
	),
	GREEN_DRAGONS(
		"Green dragons",
		ItemID.DRAGONMASK_GREEN,
		Variant.of("Green dragon", NpcPredicates.byName("Green dragon")),
		Variant.of("Baby green dragon", NpcPredicates.byName("Baby green dragon")),
		Variant.of("Brutal green dragon", NpcPredicates.byName("Brutal green dragon"))
	),
	GROTESQUE_GUARDIANS("The Grotesque Guardians", ItemID.DUSKPET),
	HARPIE_BUG_SWARMS("Harpie bug swarms", ItemID.SLAYERGUIDE_SWARM),
	HELLHOUNDS(
		"Hellhounds",
		ItemID.POH_HELLHOUND,
		Variant.of("Hellhound", NpcPredicates.byName("Hellhound")),
		Variant.of("Cerberus", NpcPredicates.byName("Cerberus"))
	),
	HILL_GIANTS("Hill giants", ItemID.ARCEUUS_CORPSE_GIANT_INITIAL),
	HOBGOBLINS("Hobgoblins", ItemID.POH_HOBGOBLIN),
	HYDRAS(
		"Hydras",
		ItemID.SLAYERGUIDE_HYDRA,
		Variant.of("Hydra", NpcPredicates.byName("Hydra")),
		Variant.of("Alchemical Hydra", NpcPredicates.byName("Alchemical Hydra"))
	),
	ICEFIENDS("Icefiends", ItemID.FD_ICEDIAMOND),
	ICE_GIANTS(
		"Ice giants",
		ItemID.RTELDRICPET,
		Variant.of("Normal", NpcPredicates.byCombatLevel(53)),
		Variant.of("Wildy Slayer Cave", NpcPredicates.byCombatLevel(67))
	),
	ICE_WARRIORS(
		"Ice warriors",
		ItemID.MITHRIL_FULL_HELM_TRIM,
		Variant.of("Ice warrior", NpcPredicates.byName("Ice warrior")),
		Variant.of("Icelord", NpcPredicates.byName("Icelord"))
	),
	INFERNAL_MAGES("Infernal mages", ItemID.SLAYERGUIDE_INFERNALMAGE),
	JAD(
		"TzTok-Jad",
		ItemID.JAD_PET,
		Variant.of("Jad", 25250, NpcPredicates.byName("TzTok-Jad")),
		Variant.of("Other", NpcPredicates.byName("Tz-Kih", "Tz-Kek", "Tok-Xil", "Yt-MekKok", "Ket-Zek", "Yt-HurKot"))
	),
	JELLIES(
		"Jellies",
		ItemID.SLAYERGUIDE_JELLY,
		Variant.of("Jelly", NpcPredicates.byName("Jelly")),
		Variant.of("Warped jelly", NpcPredicates.byName("Warped jelly"))
	),
	JUNGLE_HORROR("Jungle horrors", ItemID.ARCEUUS_CORPSE_HORROR_INITIAL),
	KALPHITE(
		"Kalphites",
		ItemID.POH_KALPHITE_SOLDIER,
		Variant.of("Worker", NpcPredicates.byName("Kalphite Worker")),
		Variant.of("Soldier", NpcPredicates.byName("Kalphite Soldier")),
		Variant.of("Guardian", NpcPredicates.byName("Kalphite Guardian")),
		Variant.of("Queen", NpcPredicates.byName("Kalphite Queen"))
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
		Variant.of("Normal", NpcPredicates.byCombatLevel(82)),
		Variant.of("Catacombs/Wildy Cave", NpcPredicates.byCombatLevel(87, 94))
	),

	LESSER_NAGUA("Lesser Nagua", ItemID.SLAYERGUIDE_LESSER_NAGUA),

	LIZARDMEN(
		"Lizardmen",
		ItemID.LIZARDMAN_FANG,
		Variant.of("Lizardman", NpcPredicates.byName("Lizardman")),
		Variant.of("Lizardman brute", NpcPredicates.byName("Lizardman brute")),
		Variant.of("Lizardman shaman", NpcPredicates.byName("Lizardman shaman"))
	),
	LIZARDS(
		"Lizards",
		ItemID.SLAYERGUIDE_LIZARD,
		Variant.of("Desert", NpcPredicates.byName("Lizard", "Desert lizard", "Small lizard")),
		Variant.of("Sulfur", NpcPredicates.byName("Sulfur lizard"))
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
		Variant.of("Normal", NpcPredicates.byName("Monkey")),
		Variant.of("Ape Atoll", NpcPredicates.byName("Monkey guard", "Monkey archer", "Monkey zombie", "Padulah"))
	),
	MOSS_GIANTS("Moss giants", ItemID.MOSSY_KEY),
	MUTATED_ZYGOMITES(
		"Mutated zygomites",
		ItemID.SLAYER_ZYGOMITE_OBJECT,
		Variant.of("Mutated zygomite", NpcPredicates.byCombatLevel(74, 86)),
		Variant.of("Ancient zygomite", NpcPredicates.byCombatLevel(109))
	),
	NECHRYAEL("Nechryael", ItemID.SLAYERGUIDE_NECHRYAEL),
	OGRES("Ogres", ItemID.ARCEUUS_CORPSE_OGRE_INITIAL),
	OTHERWORLDLY_BEING("Otherworldly beings", ItemID.SECRET_GHOST_HAT),

	PHANTOM_MUSPAH("The Phantom Muspah", ItemID.MUSPAHPET),

	PIRATES("Pirates", ItemID.BREW_RED_PIRATE_HAT),
	PYREFIENDS(
		"Pyrefiends",
		ItemID.SLAYERGUIDE_PYRFIEND,
		Variant.of("Pyrefiend", NpcPredicates.byName("Pyrefiend")),
		Variant.of("Pyrelord", NpcPredicates.byName("Pyrelord"))
	),
	RATS("Rats", ItemID.RATS_TAIL),
	RED_DRAGONS(
		"Red dragons",
		ItemID.POH_DRAGON,
		Variant.of("Red dragon", NpcPredicates.byName("Red dragon")),
		Variant.of("Baby red dragon", NpcPredicates.byName("Baby red dragon")),
		Variant.of("Brutal red dragon", NpcPredicates.byName("Brutal red dragon"))
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
		Variant.of("Skeleton", NpcPredicates.byName("Skeleton")),
		Variant.of("Vet'ion", NpcPredicates.byCombatLevel(454))
	),
	SMOKE_DEVILS(
		"Smoke devils",
		ItemID.CERT_GUIDE_ICON_DUMMY,
		Variant.of("Smoke devil", NpcPredicates.byCombatLevel(160, 280)),
		Variant.of("Thermonuclear", NpcPredicates.byCombatLevel(301))
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
		Variant.of("Ice troll", NpcPredicates.byName("Ice troll", "Ice troll runt", "Ice troll grunt", "Ice troll male", "Ice troll female")),
		Variant.of("Mountain troll", NpcPredicates.byName("Mountain troll"))
	),
	TUROTH(
		"Turoth",
		ItemID.SLAYERGUIDE_TUROTH,
		Variant.of("Small", NpcPredicates.byCombatLevel(83, 85)),
		Variant.of("Large", NpcPredicates.byCombatLevel(87, 89))
	),
	TZHAAR(
		"Tzhaar",
		ItemID.ARCEUUS_CORPSE_TZHAAR_INITIAL,
		Variant.of("Tzhaar-Ket", NpcPredicates.byName("Tzhaar-Ket")),
		Variant.of("Tzhaar-Xil", NpcPredicates.byName("Tzhaar-Xil")),
		Variant.of("Tzhaar-Mej", NpcPredicates.byName("Tzhaar-Mej")),
		Variant.of("Tzhaar-Hur", NpcPredicates.byName("Tzhaar-Hur")),
		Variant.of("Jad", 25250, NpcPredicates.byName("TzTok-Jad"))
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
	WOLVES("Wolves", ItemID.GREY_WOLF_FUR,
		Variant.of("White Wolf", NpcPredicates.byName("White Wolf"))
	),
	WYRMS("Wyrms", ItemID.SLAYERGUIDE_WYRM),
	ZILYANA("Commander Zilyana", ItemID.SARADOMINPET),
	ZOMBIES("Zombies", ItemID.TRICK_OR_TREAT_HEAD),
	ZUK("TzKal-Zuk", ItemID.INFERNOPET_ZUK),
	ZULRAH("Zulrah", ItemID.SNAKEPET);
	//</editor-fold>

	private final String name;
	private final int itemSpriteId;
	private final Variant[] variants;

	Assignment(String name, int itemSpriteId, Variant... variants)
	{
		Preconditions.checkArgument(itemSpriteId >= 0);
		this.name = name;
		this.itemSpriteId = itemSpriteId;
		this.variants = Arrays.stream(variants)
			.map(variant -> Variant.scopeToAssignment(this.name(), variant))
			.toArray(Variant[]::new);
	}

	public static Optional<Assignment> getAssignmentByName(String name)
	{
		if (name == null)
		{
			return Optional.empty();
		}

		return Arrays.stream(values())
			.filter(assignment -> assignment.getName().equalsIgnoreCase(name))
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
