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
public enum Assignment implements Group
{
	//<editor-fold desc="Enums">
	ABERRANT_SPECTRES("Aberrant spectres", ItemID.SLAYERGUIDE_ABERRANTSPECTER, Variant.ABERRANT_SPECTRE, Variant.DEVIANT_SPECTRE),
	ABYSSAL_DEMONS("Abyssal demons", ItemID.SLAYERGUIDE_ABYSSALDEMON, Variant.ABYSSAL_DEMON, Variant.SIRE),
	ABYSSAL_SIRE("The Abyssal Sire", ItemID.ABYSSALSIRE_PET),
	ALCHEMICAL_HYDRA("The Alchemical Hydra", ItemID.HYDRAPET),
	ANKOU("Ankou", ItemID.ANKOU_HEAD, Variant.ANKOU_NORMAL, Variant.ANKOU_CATACOMBS, Variant.ANKOU_WILDY_CAVE),
	ARAXXOR("Araxxor", ItemID.ARAXXORPET),
	ARAXYTES("Araxytes", ItemID.POH_ARAXYTE_HEAD),
	AVIANSIES("Aviansies", ItemID.ARCEUUS_CORPSE_AVIANSIE_INITIAL, Variant.AVIANSIE_NORMAL, Variant.KREEARRA),
	BANDITS("Bandits", ItemID.PICKPOCKET_GUIDE_DESERT_BANDIT),
	BANSHEES("Banshees", ItemID.SLAYERGUIDE_BANSHEE, Variant.BANSHEE_SLAYER_TOWER, Variant.BANSHEE_CATACOMBS),
	BARROWS_BROTHERS("Barrows Brothers", ItemID.BARROWS_KARIL_HEAD),
	BASILISKS("Basilisks", ItemID.SLAYERGUIDE_BASILISK, Variant.BASILISK_NORMAL, Variant.BASILISK_KNIGHT),
	BATS("Bats", ItemID.RAIDS_BAT2_COOKED, Variant.BAT_NORMAL, Variant.BAT_ALBINO, Variant.DEATHWING),
	BEARS("Bears", ItemID.ARCEUUS_CORPSE_BEAR_INITIAL, Variant.BEAR_NORMAL, Variant.CALLISTO),
	BIRDS("Birds", ItemID.FEATHER),
	BLACK_DEMONS("Black demons", ItemID.BLACK_DEMON_MASK, Variant.BLACK_DEMON, Variant.BLACK_DEMON_GORILLA),
	BLACK_DRAGONS("Black dragons", ItemID.DRAGONMASK_BLACK, Variant.BLACK_DRAG_NORMAL, Variant.BLACK_DRAG_BABY, Variant.BLACK_DRAG_BRUTAL),
	BLACK_KNIGHTS("Black Knights", ItemID.BLACK_FULL_HELM),
	BLOODVELD("Bloodveld", ItemID.SLAYERGUIDE_BLOODVELD, Variant.BLOODVELD_WEAK, Variant.BLOODVELD_STRONG, Variant.MUTATED_BLOODVELD),
	BLUE_DRAGONS("Blue dragons", ItemID.DRAGONMASK_BLUE, Variant.BLUE_DRAG_NORMAL, Variant.BLUE_DRAG_BABY, Variant.BLUE_DRAG_BRUTAL, Variant.VORKATH),
	BRINE_RATS("Brine rats", ItemID.OLAF2_BRINE_RAT_INV),
	CALLISTO("Callisto", ItemID.CALLISTO_PET),
	CATABLEPON("Catablepon", ItemID.SOS_HALF_SKULL2),
	CAVE_BUGS("Cave bugs", ItemID.SWAMP_CAVE_BUG),
	CAVE_CRAWLERS("Cave crawlers", ItemID.SLAYERGUIDE_CAVECRAWLER),
	CAVE_HORRORS("Cave horrors", ItemID.SLAYERGUIDE_HARMLESS_CAVE_HORROR),
	CAVE_KRAKEN("Cave kraken", ItemID.CERT_EADGAR_FADE_TO_BLACK_INV, Variant.CAVE_KRAKEN, Variant.KRAKEN),
	CAVE_SLIMES("Cave slimes", ItemID.SWAMP_CAVE_SLIME),
	CERBERUS("Cerberus", ItemID.HELL_PET),
	CHAOS_DRUIDS("Chaos druids", ItemID.ELDERCHAOS_HOOD, Variant.CHAOS_DRUIDS, Variant.CHAOS_DRUIDS_ELDER),
	CHAOS_ELEMENTAL("The Chaos Elemental", ItemID.CHAOSELEPET),
	CHAOS_FANATIC("The Chaos Fanatic", ItemID.STAFF_OF_ZAROS),
	COCKATRICE("Cockatrice", ItemID.SLAYERGUIDE_COCKATRICE),
	COWS("Cows", ItemID.COW_MASK),

	CRABS("Crabs", ItemID.HUNDRED_PIRATE_CRAB_SHELL_GAUNTLET),

	CRAWLING_HANDS("Crawling hands", ItemID.SLAYERGUIDE_CRAWLINGHAND),
	CRAZY_ARCHAEOLOGIST("Crazy Archaeologists", ItemID.FEDORA),
	CROCODILES("Crocodiles", ItemID.GREEN_SALAMANDER),

	CUSTODIAN_STALKERS("Custodian Stalkers", ItemID.SLAYERGUIDE_CUSTODIAN_STALKER_MATURE),

	DAGANNOTH("Dagannoth", ItemID.POH_DAGGANOTH, Variant.DAGANNOTH_NORMAL, Variant.DAGANNOTH_WATERBIRTH, Variant.DAGANNOTH_KINGS),
	DAGANNOTH_KINGS("Dagannoth Kings", ItemID.PRIMEPET),
	DARK_BEASTS("Dark beasts", ItemID.SLAYERGUIDE_DARK_BEAST),
	DARK_WARRIORS("Dark warriors", ItemID.BLACK_MED_HELM, Variant.DARK_WARRIOR_FORTRESS, Variant.DARK_WARRIOR_KOUREND),
	DERANGED_ARCHAEOLOGIST("Deranged Archaeologist", ItemID.FOSSIL_SWAMP_DIARY),
	DOGS("Dogs", ItemID.POH_GUARD_DOG),
	DRAKES("Drakes", ItemID.SLAYERGUIDE_DRAKE),

	DUKE_SUCELLUS("Duke Sucellus", ItemID.DUKESUCELLUSPET),

	DUST_DEVILS("Dust devils", ItemID.SLAYERGUIDE_DUSTDEVIL, Variant.DUST_DEVIL_WEAK, Variant.DUST_DEVIL_STRONG),
	DWARVES("Dwarves", ItemID.GRIM_WEAR_HELMET),
	EARTH_WARRIORS("Earth warriors", ItemID.BRONZE_FULL_HELM_TRIM),

	ELVES("Elves", ItemID.PICKPOCKET_GUIDE_WOODELF),

	ENTS("Ents", ItemID.POH_TREE_2),
	FEVER_SPIDERS("Fever spiders", ItemID.SLAYERGUIDE_FEVER_SPIDER),
	FIRE_GIANTS("Fire giants", ItemID.RTBRANDAPET, Variant.FIRE_GIANT_WEAK, Variant.FIRE_GIANT_STRONG),
	FLESH_CRAWLERS("Fleshcrawlers", ItemID.ARCEUUS_CORPSE_SCORPION_INITIAL),
	FOSSIL_ISLAND_WYVERNS("Fossil island wyverns", ItemID.SLAYERGUIDE_FOSSILWYVERN),
	GARGOYLES("Gargoyles", ItemID.SLAYERGUIDE_GARGOYLE, Variant.GARGOYLES, Variant.GROTESQUE_GUARDIANS),
	GENERAL_GRAARDOR("General Graardor", ItemID.BANDOSPET),
	GHOSTS("Ghosts", ItemID.AMULET_OF_GHOSTSPEAK),
	GHOULS("Ghouls", ItemID.TRICK_OR_TREAT_HEAD),
	GIANT_MOLE("The Giant Mole", ItemID.MOLEPET),
	GOBLINS("Goblins", ItemID.ARCEUUS_CORPSE_GOBLIN_INITIAL),
	GREATER_DEMONS("Greater demons", ItemID.GREATER_DEMON_MASK, Variant.GREATER_DEMON_NORMAL, Variant.GREATER_DEMON_CATACOMBS, Variant.GREATER_DEMON_WILDY_CAVE, Variant.KRIL, Variant.SKOTIZO),
	GREEN_DRAGONS("Green dragons", ItemID.DRAGONMASK_GREEN, Variant.GREEN_DRAG_NORMAL, Variant.GREEN_DRAG_BABY, Variant.GREEN_DRAG_BRUTAL),
	GROTESQUE_GUARDIANS("The Grotesque Guardians", ItemID.DUSKPET),
	HARPIE_BUG_SWARMS("Harpie bug swarms", ItemID.SLAYERGUIDE_SWARM),
	HELLHOUNDS("Hellhounds", ItemID.POH_HELLHOUND, Variant.CERBERUS, Variant.HELLHOUND),
	HILL_GIANTS("Hill giants", ItemID.ARCEUUS_CORPSE_GIANT_INITIAL),
	HOBGOBLINS("Hobgoblins", ItemID.POH_HOBGOBLIN),
	HYDRAS("Hydras", ItemID.SLAYERGUIDE_HYDRA, Variant.HYDRA, Variant.ALCHEMICAL),
	ICEFIENDS("Icefiends", ItemID.FD_ICEDIAMOND),
	ICE_GIANTS("Ice giants", ItemID.RTELDRICPET, Variant.ICE_GIANT_NORMAL, Variant.ICE_GIANT_WILDY_CAVE),
	ICE_WARRIORS("Ice warriors", ItemID.MITHRIL_FULL_HELM_TRIM, Variant.ICE_WARRIOR, Variant.ICELORD),
	INFERNAL_MAGES("Infernal mages", ItemID.SLAYERGUIDE_INFERNALMAGE),
	JAD("TzTok-Jad", ItemID.JAD_PET, Variant.JAD, Variant.FIGHT_CAVE_OTHERS),
	JELLIES("Jellies", ItemID.SLAYERGUIDE_JELLY, Variant.JELLY, Variant.WARPED_JELLY),
	JUNGLE_HORROR("Jungle horrors", ItemID.ARCEUUS_CORPSE_HORROR_INITIAL),
	KALPHITE("Kalphites", ItemID.POH_KALPHITE_SOLDIER, Variant.KALPHITE_WORKER, Variant.KALPHITE_SOLDIER, Variant.KALPHITE_GUARDIAN, Variant.KALPHITE_QUEEN),
	KALPHITE_QUEEN("The Kalphite Queen", ItemID.KQPET_WALKING),
	KILLERWATTS("Killerwatts", ItemID.SLAYERGUIDE_KILLERWATT),
	KING_BLACK_DRAGON("The King Black Dragon", ItemID.KBDPET),
	KRAKEN("The Cave Kraken Boss", ItemID.KRAKENPET),
	KREEARRA("Kree'arra", ItemID.ARMADYLPET),
	KRIL_TSUTSAROTH("K'ril Tsutsaroth", ItemID.ZAMORAKPET),
	KURASK("Kurask", ItemID.SLAYERGUIDE_KURASK),
	LAVA_DRAGONS("Lava Dragons", ItemID.LAVA_SCALE),
	LESSER_DEMONS("Lesser demons", ItemID.LESSER_DEMON_MASK, Variant.LESSER_DEMON_NORMAL, Variant.LESSER_DEMON_WILDY_CATACOMBS),

	LESSER_NAGUA("Lesser Nagua", ItemID.SLAYERGUIDE_LESSER_NAGUA),

	LIZARDMEN("Lizardmen", ItemID.LIZARDMAN_FANG, Variant.LIZARDMAN, Variant.LIZARDMAN_BRUTE, Variant.LIZARDMAN_SHAMAN),
	LIZARDS("Lizards", ItemID.SLAYERGUIDE_LIZARD, Variant.LIZARD_DESERT, Variant.LIZARD_SULFUR),
	MAGIC_AXES("Magic axes", ItemID.IRON_BATTLEAXE),
	MAMMOTHS("Mammoths", ItemID.BARBASSAULT_ATT_HORN_01),

	METAL_DRAGONS("Metal dragons", ItemID.POH_STEEL_DRAGON),

	MINIONS_OF_SCABARAS("Minions of scabaras", ItemID.NTK_SCARAB_GOLD),
	MINOTAURS("Minotaurs", ItemID.ARCEUUS_CORPSE_MINOTAUR_INITIAL),
	MOGRES("Mogres", ItemID.SLAYERGUIDE_MOGRE),
	MOLANISKS("Molanisks", ItemID.SLAYERGUIDE_MOLANISK),
	MONKEYS("Monkeys", ItemID.ARCEUUS_CORPSE_MONKEY_INITIAL, Variant.MONKEY_NORMAL, Variant.MONKEY_APE_ATOLL),
	MOSS_GIANTS("Moss giants", ItemID.MOSSY_KEY),
	MUTATED_ZYGOMITES("Mutated zygomites", ItemID.SLAYER_ZYGOMITE_OBJECT, Variant.ZYGOMITE_MUTATED, Variant.ZYGOMITE_ANCIENT),
	NECHRYAEL("Nechryael", ItemID.SLAYERGUIDE_NECHRYAEL),
	OGRES("Ogres", ItemID.ARCEUUS_CORPSE_OGRE_INITIAL),
	OTHERWORLDLY_BEING("Otherworldly beings", ItemID.SECRET_GHOST_HAT),

	PHANTOM_MUSPAH("The Phantom Muspah", ItemID.MUSPAHPET),

	PIRATES("Pirates", ItemID.BREW_RED_PIRATE_HAT),
	PYREFIENDS("Pyrefiends", ItemID.SLAYERGUIDE_PYRFIEND, Variant.PYREFIEND, Variant.PYRELORD),
	RATS("Rats", ItemID.RATS_TAIL),
	RED_DRAGONS("Red dragons", ItemID.POH_DRAGON, Variant.RED_DRAGON_NORMAL, Variant.RED_DRAGON_BABY, Variant.RED_DRAGON_BRUTAL),
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
	SKELETONS("Skeletons", ItemID.POH_SKELETON_GUARD, Variant.SKELETON, Variant.VETION),
	SMOKE_DEVILS("Smoke devils", ItemID.CERT_GUIDE_ICON_DUMMY, Variant.SMOKE_DEVIL_NORMAL, Variant.THERMONUCLEAR),
	SOURHOGS("Sourhogs", ItemID.PORCINE_SOURHOG_TROPHY),
	SPIDERS("Spiders", ItemID.POH_SPIDER),
	SPIRITUAL_CREATURES("Spiritual creatures", ItemID.DRAGON_BOOTS),
	SUQAHS("Suqahs", ItemID.SUQKA_TOOTH),
	TERROR_DOGS("Terror dogs", ItemID.SLAYERGUIDE_TERRORDOG),

	THE_LEVIATHAN("The Leviathan", ItemID.LEVIATHANPET),
	THE_WHISPERER("The Whisperer", ItemID.WHISPERERPET),

	THERMONUCLEAR_SMOKE_DEVIL("The Thermonuclear Smoke Devil", ItemID.SMOKEPET),
	TROLLS("Trolls", ItemID.POH_TROLL, Variant.ICE_TROLL, Variant.MOUNTAIN_TROLL),
	TUROTH("Turoth", ItemID.SLAYERGUIDE_TUROTH, Variant.TUROTH_SMALL, Variant.TUROTH_LARGE),
	TZHAAR("Tzhaar", ItemID.ARCEUUS_CORPSE_TZHAAR_INITIAL, Variant.TZHAAR_KET, Variant.TZHAAR_XIL, Variant.TZHAAR_MEJ, Variant.TZHAAR_HUR, Variant.JAD),
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