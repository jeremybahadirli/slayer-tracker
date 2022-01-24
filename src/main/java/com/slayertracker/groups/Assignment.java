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
import static com.slayertracker.groups.Variant.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import net.runelite.api.ItemID;
import static net.runelite.api.ItemID.*;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;

@Getter
public enum Assignment implements Group
{
	//<editor-fold desc="Enums">
	ABERRANT_SPECTRES("Aberrant spectres", new Variant[]{Variant.ABERRANT_SPECTRE, DEVIANT_SPECTRE}, ItemID.ABERRANT_SPECTRE, "Spectre"),
	ABYSSAL_DEMONS("Abyssal demons", new Variant[]{Variant.ABYSSAL_DEMON, SIRE}, ItemID.ABYSSAL_DEMON),
	ABYSSAL_SIRE("Abyssal Sire", ABYSSAL_ORPHAN),
	ADAMANT_DRAGONS("Adamant dragons", ADAMANT_DRAGON_MASK),
	ALCHEMICAL_HYDRA("Alchemical Hydra", IKKLE_HYDRA),
	ANKOU("Ankou", new Variant[]{ANKOU_NORMAL, ANKOU_CATACOMBS, ANKOU_WILDY_CAVE}, ANKOU_MASK),
	AVIANSIES("Aviansies", new Variant[]{AVIANSIE_NORMAL, Variant.KREEARRA}, ENSOULED_AVIANSIE_HEAD),
	BANDITS("Bandits", ItemID.BANDIT, "Bandit", "Black Heather", "Donny the Lad", "Speedy Keith"),
	BANSHEES("Banshees", new Variant[]{BANSHEE_SLAYER_TOWER, BANSHEE_CATACOMBS}, BANSHEE),
	BARROWS_BROTHERS("Barrows Brothers", KARILS_COIF),
	BASILISKS("Basilisks", new Variant[]{BASILISK_NORMAL, Variant.BASILISK_KNIGHT}, BASILISK),
	BATS("Bats", new Variant[]{BAT_NORMAL, BAT_ALBINO, DEATHWING}, GIRAL_BAT_2, "Death wing"),
	BEARS("Bears", new Variant[]{BEAR_NORMAL, Variant.CALLISTO}, ENSOULED_BEAR_HEAD),
	BIRDS("Birds", FEATHER, "Chicken", "Rooster", "Terrorbird", "Seagull", "Vulture"),
	BLACK_DEMONS("Black demons", new Variant[]{BLACK_DEMON, BLACK_DEMON_GORILLA}, BLACK_DEMON_MASK),
	BLACK_DRAGONS("Black dragons", new Variant[]{BLACK_DRAG_NORMAL, BLACK_DRAG_BABY, BLACK_DRAG_BRUTAL}, BLACK_DRAGON_MASK, "Baby black dragon"),
	BLACK_KNIGHTS("Black Knights", BLACK_FULL_HELM, "Black Knight"),
	BLOODVELD("Bloodveld", new Variant[]{BLOODVELD_WEAK, BLOODVELD_STRONG, MUTATED_BLOODVELD}, ItemID.BLOODVELD),
	BLUE_DRAGONS("Blue dragons", new Variant[]{BLUE_DRAG_NORMAL, BLUE_DRAG_BABY, BLUE_DRAG_BRUTAL, Variant.VORKATH}, BLUE_DRAGON_MASK, "Baby blue dragon"),
	BRINE_RATS("Brine rats", BRINE_RAT),
	BRONZE_DRAGONS("Bronze dragons", new Variant[]{BRONZE_DRAG_NORMAL, BRONZE_DRAG_CATACOMBS}, BRONZE_DRAGON_MASK),
	CALLISTO("Callisto", CALLISTO_CUB),
	CATABLEPON("Catablepon", LEFT_SKULL_HALF),
	CAVE_BUGS("Cave bugs", SWAMP_CAVE_BUG),
	CAVE_CRAWLERS("Cave crawlers", CAVE_CRAWLER, "Chasm crawler"),
	CAVE_HORRORS("Cave horrors", CAVE_HORROR, "Cave abomination"),
	CAVE_KRAKEN("Cave kraken", new Variant[]{Variant.CAVE_KRAKEN, Variant.KRAKEN}, ItemID.CAVE_KRAKEN),
	CAVE_SLIMES("Cave slimes", SWAMP_CAVE_SLIME),
	CERBERUS("Cerberus", HELLPUPPY),
	CHAOS_DRUIDS("Chaos druids", new Variant[]{Variant.CHAOS_DRUIDS, CHAOS_DRUIDS_ELDER}, ELDER_CHAOS_HOOD, "Elder Chaos druid", "Chaos druid"),
	CHAOS_ELEMENTAL("Chaos Elemental", PET_CHAOS_ELEMENTAL),
	CHAOS_FANATIC("Chaos Fanatic", ANCIENT_STAFF),
	COCKATRICE("Cockatrice", ItemID.COCKATRICE, "Cockathrice"),
	COWS("Cows", COW_MASK),
	CRAWLING_HANDS("Crawling hands", CRAWLING_HAND, "Crushing hand"),
	CRAZY_ARCHAEOLOGIST("Crazy Archaeologists", FEDORA, "Crazy Archaeologist"),
	CROCODILES("Crocodiles", SWAMP_LIZARD),
	DAGANNOTH("Dagannoth", new Variant[]{DAGANNOTH_NORMAL, DAGANNOTH_WATERBIRTH, Variant.DAGANNOTH_KINGS}, ItemID.DAGANNOTH),
	DAGANNOTH_KINGS("Dagannoth Kings", PET_DAGANNOTH_PRIME),
	DARK_BEASTS("Dark beasts", DARK_BEAST, "Night beast"),
	DARK_WARRIORS("Dark warriors", new Variant[]{DARK_WARRIOR_FORTRESS, DARK_WARRIOR_KOUREND}, BLACK_MED_HELM, "Dark warrior"),
	DERANGED_ARCHAEOLOGIST("Deranged Archaeologist", ARCHAEOLOGISTS_DIARY),
	DOGS("Dogs", GUARD_DOG, "Jackal"),
	DRAKES("Drakes", DRAKE),
	DUST_DEVILS("Dust devils", new Variant[]{DUST_DEVIL_WEAK, DUST_DEVIL_STRONG}, DUST_DEVIL, "Choke devil"),
	DWARVES("Dwarves", DWARVEN_HELMET, "Dwarf", "Black Guard"),
	EARTH_WARRIORS("Earth warriors", BRONZE_FULL_HELM_T),
	ELVES("Elves", ELF, "Elf", "Iorwerth Warrior", "Iorwerth Archer"),
	ENTS("Ents", NICE_TREE, "Ent"),
	FEVER_SPIDERS("Fever spiders", FEVER_SPIDER),
	FIRE_GIANTS("Fire giants", new Variant[]{FIRE_GIANT_WEAK, FIRE_GIANT_STRONG}, FIRE_BATTLESTAFF),
	FLESH_CRAWLERS("Fleshcrawlers", ENSOULED_SCORPION_HEAD, "Flesh crawler"),
	FOSSIL_ISLAND_WYVERNS("Fossil island wyverns", FOSSIL_ISLAND_WYVERN, "Ancient wyvern", "Long-tailed wyvern", "Spitting wyvern", "Taloned wyvern"),
	GARGOYLES("Gargoyles", new Variant[]{Variant.GARGOYLES, Variant.GROTESQUE_GUARDIANS}, GARGOYLE),
	GENERAL_GRAARDOR("General Graardor", PET_GENERAL_GRAARDOR),
	GHOSTS("Ghosts", GHOSTSPEAK_AMULET, "Death wing", "Tortured soul"),
	GHOULS("Ghouls", ZOMBIE_HEAD),
	GIANT_MOLE("Giant Mole", BABY_MOLE),
	GOBLINS("Goblins", ENSOULED_GOBLIN_HEAD),
	GREATER_DEMONS("Greater demons", new Variant[]{GREATER_DEMON_NORMAL, GREATER_DEMON_CATACOMBS, GREATER_DEMON_WILDY_CAVE, KRIL, SKOTIZO}, GREATER_DEMON_MASK),
	GREEN_DRAGONS("Green dragons", new Variant[]{GREEN_DRAG_NORMAL, GREEN_DRAG_BABY, GREEN_DRAG_BRUTAL}, GREEN_DRAGON_MASK, "Baby green dragon", "Elvarg"),
	GROTESQUE_GUARDIANS("Grotesque Guardians", MIDNIGHT, "Dusk", "Dawn"),
	HARPIE_BUG_SWARMS("Harpie bug swarms", SWARM),
	HELLHOUNDS("Hellhounds", new Variant[]{Variant.CERBERUS, Variant.HELLHOUND}, ItemID.HELLHOUND, "Cerberus"),
	HILL_GIANTS("Hill giants", ENSOULED_GIANT_HEAD, "Cyclops"),
	HOBGOBLINS("Hobgoblins", HOBGOBLIN_GUARD),
	HYDRAS("Hydras", new Variant[]{Variant.HYDRA, ALCHEMICAL}, ItemID.HYDRA),
	ICEFIENDS("Icefiends", ICE_DIAMOND),
	ICE_GIANTS("Ice giants", new Variant[]{ICE_GIANT_NORMAL, ICE_GIANT_WILDY_CAVE}, ICE_DIAMOND),
	ICE_WARRIORS("Ice warriors", new Variant[]{ICE_WARRIOR, ICELORD}, MITHRIL_FULL_HELM_T, "Icelord"),
	INFERNAL_MAGES("Infernal mages", INFERNAL_MAGE, "Malevolent mage"),
	IRON_DRAGONS("Iron dragons", new Variant[]{IRON_DRAG_NORMAL, IRON_DRAG_CATACOMBS}, IRON_DRAGON_MASK),
	JAD("TzTok-Jad", new Variant[]{Variant.JAD, FIGHT_CAVE_OTHERS}, TZREKJAD),
	JELLIES("Jellies", new Variant[]{Variant.JELLY, WARPED_JELLY}, ItemID.JELLY, "Jelly"),
	JUNGLE_HORROR("Jungle horrors", ENSOULED_HORROR_HEAD),
	KALPHITE("Kalphite", new Variant[]{KALPHITE_WORKER, Variant.KALPHITE_SOLDIER, KALPHITE_GUARDIAN, Variant.KALPHITE_QUEEN}, ItemID.KALPHITE_SOLDIER),
	KALPHITE_QUEEN("Kalphite Queen", KALPHITE_PRINCESS),
	KILLERWATTS("Killerwatts", KILLERWATT),
	KING_BLACK_DRAGON("King Black Dragon", PRINCE_BLACK_DRAGON),
	KRAKEN("Cave Kraken Boss", PET_KRAKEN, "Kraken"),
	KREEARRA("Kree'arra", PET_KREEARRA),
	KRIL_TSUTSAROTH("K'ril Tsutsaroth", PET_KRIL_TSUTSAROTH),
	KURASK("Kurask", ItemID.KURASK),
	LAVA_DRAGONS("Lava Dragons", LAVA_SCALE, "Lava dragon"),
	LESSER_DEMONS("Lesser demons", new Variant[]{LESSER_DEMON_NORMAL, LESSER_DEMON_WILDY_CATACOMBS}, LESSER_DEMON_MASK),
	LIZARDMEN("Lizardmen", new Variant[]{LIZARDMAN, LIZARDMAN_BRUTE, LIZARDMAN_SHAMAN}, LIZARDMAN_FANG, "Lizardman"),
	LIZARDS("Lizards", new Variant[]{LIZARD_DESERT, LIZARD_SULFUR}, DESERT_LIZARD, "Desert lizard", "Sulphur lizard", "Small lizard", "Lizard"),
	MAGIC_AXES("Magic axes", IRON_BATTLEAXE, "Magic axe"),
	MAMMOTHS("Mammoths", ATTACKER_HORN, "Mammoth"),
	MINIONS_OF_SCABARAS("Minions of scabaras", GOLDEN_SCARAB, "Scarab swarm", "Locust rider", "Scarab mage"),
	MINOTAURS("Minotaurs", ENSOULED_MINOTAUR_HEAD),
	MITHRIL_DRAGONS("Mithril dragons", MITHRIL_DRAGON_MASK),
	MOGRES("Mogres", MOGRE),
	MOLANISKS("Molanisks", MOLANISK),
	MONKEYS("Monkeys", new Variant[]{MONKEY_NORMAL, MONKEY_APE_ATOLL}, ENSOULED_MONKEY_HEAD, "Tortured gorilla"),
	MOSS_GIANTS("Moss giants", HILL_GIANT_CLUB),
	MUTATED_ZYGOMITES("Mutated zygomites", new Variant[]{ZYGOMITE_MUTATED, ZYGOMITE_ANCIENT}, MUTATED_ZYGOMITE, "Zygomite", "Fungi"),
	NECHRYAEL("Nechryael", ItemID.NECHRYAEL, "Nechryarch"),
	OGRES("Ogres", ENSOULED_OGRE_HEAD),
	OTHERWORLDLY_BEING("Otherworldly beings", GHOSTLY_HOOD),
	PIRATES("Pirates", PIRATE_HAT, "Pirate"),
	PYREFIENDS("Pyrefiends", new Variant[]{Variant.PYREFIEND, PYRELORD}, ItemID.PYREFIEND, "Flaming pyrelord"),
	RATS("Rats", RATS_TAIL),
	RED_DRAGONS("Red dragons", new Variant[]{RED_DRAGON_NORMAL, RED_DRAGON_BABY, RED_DRAGON_BRUTAL}, BABY_RED_DRAGON, "Baby red dragon"),
	REVENANTS("Revenants", BRACELET_OF_ETHEREUM, "Revenant imp", "Revenant goblin", "Revenant pyrefiend", "Revenant hobgoblin", "Revenant cyclops", "Revenant hellhound", "Revenant demon", "Revenant ork", "Revenant dark beast", "Revenant knight", "Revenant dragon"),
	ROCKSLUGS("Rockslugs", ROCKSLUG),
	ROGUES("Rogues", ROGUE_MASK, "Rogue"),
	RUNE_DRAGONS("Rune dragons", RUNE_DRAGON_MASK),
	SARACHNIS("Sarachnis", SRARACHA),
	SCORPIA("Scorpia", SCORPIAS_OFFSPRING),
	SCORPIONS("Scorpions", ENSOULED_SCORPION_HEAD),
	SEA_SNAKES("Sea snakes", SNAKE_CORPSE),
	SHADES("Shades", SHADE_ROBE_TOP, "Loar Shadow", "Loar Shade", "Phrin Shadow", "Phrin Shade", "Riyl Shadow", "Riyl Shade", "Asyn Shadow", "Asyn Shade", "Fiyr Shadow", "Fiyr Shade"),
	SHADOW_WARRIORS("Shadow warriors", BLACK_FULL_HELM),
	SKELETAL_WYVERNS("Skeletal wyverns", SKELETAL_WYVERN),
	SKELETONS("Skeletons", new Variant[]{SKELETON, Variant.VETION}, SKELETON_GUARD),
	SMOKE_DEVILS("Smoke devils", new Variant[]{SMOKE_DEVIL_NORMAL, THERMONUCLEAR}, SMOKE_DEVIL),
	SOURHOGS("Sourhogs", SOURHOG_FOOT),
	SPIDERS("Spiders", HUGE_SPIDER),
	SPIRITUAL_CREATURES("Spiritual creatures", DRAGON_BOOTS, "Spiritual ranger", "Spiritual mage", "Spiritual warrior"),
	STEEL_DRAGONS("Steel dragons", STEEL_DRAGON),
	SULPHUR_LIZARDS("Sulphur Lizards", SULPHUR_LIZARD),
	SUQAHS("Suqahs", SUQAH_TOOTH),
	TEMPLE_SPIDERS("Temple Spiders", RED_SPIDERS_EGGS),
	TERROR_DOGS("Terror dogs", TERROR_DOG),
	THERMONUCLEAR_SMOKE_DEVIL("Thermonuclear Smoke Devil", PET_SMOKE_DEVIL),
	TROLLS("Trolls", new Variant[]{ICE_TROLL, MOUNTAIN_TROLL}, TROLL_GUARD, "Dad", "Arrg"),
	TUROTH("Turoth", new Variant[]{TUROTH_SMALL, TUROTH_LARGE}, ItemID.TUROTH),
	TZHAAR("Tzhaar", new Variant[]{TZHAAR_KET, TZHAAR_XIL, TZHAAR_MEJ, TZHAAR_HUR, Variant.JAD}, ENSOULED_TZHAAR_HEAD),
	UNDEAD_DRUIDS("Undead Druids", MASK_OF_RANUL),
	VAMPYRES("Vampyres", STAKE, "Vyrewatch", "Vampire"),
	VENENATIS("Venenatis", VENENATIS_SPIDERLING),
	VETION("Vet'ion", VETION_JR),
	VORKATH("Vorkath", VORKI),
	WALL_BEASTS("Wall beasts", SWAMP_WALLBEAST),
	WATERFIENDS("Waterfiends", WATER_ORB),
	WEREWOLVES("Werewolves", WOLFBANE, "Werewolf"),
	WOLVES("Wolves", GREY_WOLF_FUR, "Wolf"),
	WYRMS("Wyrms", WYRM),
	ZILYANA("Commander Zilyana", PET_ZILYANA),
	ZOMBIES("Zombies", ZOMBIE_HEAD, "Undead"),
	ZUK("TzKal-Zuk", TZREKZUK),
	ZULRAH("Zulrah", PET_SNAKELING);
	//</editor-fold>

	private static final Map<String, Assignment> assignment;

	private final String name;
	private final int itemSpriteId;
	private final Set<String> targetNames;
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

	Assignment(String name, int itemSpriteId, String... targetNames)
	{
		this(name, new Variant[]{}, itemSpriteId, targetNames);
	}

	Assignment(String name, Variant[] variants, int itemSpriteId, String... targetNames)
	{
		Preconditions.checkArgument(itemSpriteId >= 0);
		this.name = name;
		this.itemSpriteId = itemSpriteId;
		this.targetNames = Stream.concat(
				Arrays.stream(targetNames),
				Stream.of(name.replaceAll("s$", ""))
			)
			.map(String::toLowerCase)
			.collect(Collectors.toSet());
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

	public Optional<Variant> getVariantMatchingNpc(NPC npc)
	{
		final NPCComposition composition = npc.getTransformedComposition();
		if (composition == null)
		{
			return Optional.empty();
		}

		return Arrays.stream(this.getVariants()).filter(variant ->
				Arrays.stream(variant.getCombatLevels()).anyMatch(lvl -> lvl == npc.getCombatLevel())
					|| Arrays.stream(variant.getTargetNames())
					.map(String::toLowerCase)
					.anyMatch(name -> name.equals(npc.getName().toLowerCase())))
			.findAny();
	}
}