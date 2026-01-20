package com.slayertracker.groups;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import lombok.Getter;

public enum Master
{
	MAZCHNA("Mazchna",
		Map.of(
			1, 6,
			10, 30,
			50, 90,
			100, 150,
			250, 210,
			1000, 300),
		Task.of(Assignment.BANSHEES, 30, 50, 8),
		Task.of(Assignment.BATS, 30, 50, 7),
		Task.of(Assignment.BEARS, 30, 50, 6),
		Task.of(Assignment.CATABLEPON, 20, 30, 8),
		Task.of(Assignment.CAVE_BUGS, 10, 20, 8),
		Task.of(Assignment.CAVE_CRAWLERS, 30, 50, 8),
		Task.of(Assignment.CAVE_SLIMES, 10, 20, 8),
		Task.of(Assignment.COCKATRICE, 30, 50, 8),
		Task.of(Assignment.CRABS, 30, 50, 8),
		Task.of(Assignment.CRAWLING_HANDS, 30, 50, 8),
		Task.of(Assignment.DOGS, 30, 50, 7),
		Task.of(Assignment.FLESH_CRAWLERS, 15, 25, 7),
		Task.of(Assignment.GHOSTS, 30, 50, 7),
		Task.of(Assignment.GHOULS, 10, 20, 7),
		Task.of(Assignment.HILL_GIANTS, 30, 50, 7),
		Task.of(Assignment.HOBGOBLINS, 30, 50, 7),
		Task.of(Assignment.ICE_WARRIORS, 40, 50, 7),
		Task.of(Assignment.KALPHITE, 30, 50, 6),
		Task.of(Assignment.KILLERWATTS, 30, 50, 6),
		Task.of(Assignment.LIZARDS, 30, 50, 8),
		Task.of(Assignment.MOGRES, 30, 50, 8),
		Task.of(Assignment.PYREFIENDS, 30, 50, 8),
		Task.of(Assignment.ROCKSLUGS, 30, 50, 8),
		Task.of(Assignment.SCORPIONS, 30, 50, 7),
		Task.of(Assignment.SHADES, 30, 70, 8),
		Task.of(Assignment.SKELETONS, 30, 50, 7),
		Task.of(Assignment.VAMPYRES, 10, 20, 200, 250, 6),
		Task.of(Assignment.WALL_BEASTS, 10, 20, 7),
		Task.of(Assignment.WOLVES, 30, 50, 7),
		Task.of(Assignment.ZOMBIES, 30, 50, 7)
	),
	VANNAKA("Vannaka",
		Map.of(1, 8,
			10, 40,
			50, 120,
			100, 200,
			250, 280,
			1000, 400),
		Task.of(Assignment.ABERRANT_SPECTRES, 40, 90, 200, 250, 8),
		Task.of(Assignment.ABYSSAL_DEMONS, 40, 90, 200, 250, 5),
		Task.of(Assignment.ANKOU, 25, 35, 91, 150, 8),
		Task.of(Assignment.BASILISKS, 40, 90, 200, 250, 8),
		Task.of(Assignment.BLOODVELD, 40, 90, 200, 250, 8),
		Task.of(Assignment.BLUE_DRAGONS, 40, 90, 7),
		Task.of(Assignment.BRINE_RATS, 40, 90, 7),
		Task.of(Assignment.COCKATRICE, 40, 90, 8),
		Task.of(Assignment.CRABS, 40, 90, 8),
		Task.of(Assignment.CROCODILES, 40, 90, 6),
		Task.of(Assignment.DAGANNOTH, 40, 90, 7),
		Task.of(Assignment.DUST_DEVILS, 40, 90, 200, 250, 8),
		Task.of(Assignment.ELVES, 30, 70, 7),
		Task.of(Assignment.FEVER_SPIDERS, 30, 90, 7),
		Task.of(Assignment.FIRE_GIANTS, 40, 90, 7),
		Task.of(Assignment.GARGOYLES, 40, 90, 200, 250, 5),
		Task.of(Assignment.GHOULS, 10, 40, 7),
		Task.of(Assignment.GRYPHONS, 30, 80, 7),
		Task.of(Assignment.HARPIE_BUG_SWARMS, 40, 90, 8),
		Task.of(Assignment.HELLHOUNDS, 30, 60, 7),
		Task.of(Assignment.HILL_GIANTS, 40, 90, 7),
		Task.of(Assignment.HOBGOBLINS, 40, 90, 7),
		Task.of(Assignment.ICE_GIANTS, 30, 80, 7),
		Task.of(Assignment.ICE_WARRIORS, 40, 90, 7),
		Task.of(Assignment.INFERNAL_MAGES, 40, 90, 8),
		Task.of(Assignment.JELLIES, 40, 90, 8),
		Task.of(Assignment.JUNGLE_HORROR, 40, 90, 8),
		Task.of(Assignment.KALPHITE, 40, 90, 7),
		Task.of(Assignment.KURASK, 40, 90, 7),
		Task.of(Assignment.LESSER_DEMONS, 40, 90, 7),
		Task.of(Assignment.MOGRES, 40, 90, 7),
		Task.of(Assignment.MOLANISKS, 40, 50, 7),
		Task.of(Assignment.MOSS_GIANTS, 40, 90, 7),
		Task.of(Assignment.NECHRYAEL, 40, 90, 200, 250, 5),
		Task.of(Assignment.OGRES, 40, 90, 7),
		Task.of(Assignment.OTHERWORLDLY_BEING, 40, 90, 8),
		Task.of(Assignment.PYREFIENDS, 40, 90, 8),
		Task.of(Assignment.SEA_SNAKES, 40, 90, 6),
		Task.of(Assignment.SHADES, 40, 90, 8),
		Task.of(Assignment.SHADOW_WARRIORS, 30, 80, 8),
		Task.of(Assignment.SPIRITUAL_CREATURES, 40, 90, 181, 250, 8),
		Task.of(Assignment.TERROR_DOGS, 20, 45, 6),
		Task.of(Assignment.TROLLS, 40, 90, 7),
		Task.of(Assignment.TUROTH, 30, 90, 8),
		Task.of(Assignment.VAMPYRES, 10, 20, 200, 250, 7),
		Task.of(Assignment.WEREWOLVES, 30, 60, 7)
	),
	CHAELDAR("Chaeldar",
		Map.of(1, 10,
			10, 50,
			50, 150,
			100, 250,
			250, 350,
			1000, 500),
		Task.of(Assignment.ABERRANT_SPECTRES, 70, 130, 200, 250, 8),
		Task.of(Assignment.ABYSSAL_DEMONS, 70, 130, 200, 250, 12),
		Task.of(Assignment.AVIANSIES, 70, 130, 130, 250, 9),
		Task.of(Assignment.BASILISKS, 70, 130, 200, 250, 7),
		Task.of(Assignment.BLACK_DEMONS, 70, 130, 200, 250, 10),
		Task.of(Assignment.BLOODVELD, 70, 130, 200, 250, 8),
		Task.of(Assignment.BLUE_DRAGONS, 70, 130, 8),
		Task.of(Assignment.BRINE_RATS, 70, 130, 7),
		Task.of(Assignment.CAVE_HORRORS, 70, 130, 200, 250, 10),
		Task.of(Assignment.CAVE_KRAKEN, 30, 50, 150, 200, 12),
		Task.of(Assignment.CRABS, 70, 130, 8),
		Task.of(Assignment.CUSTODIAN_STALKERS, 70, 130, 200, 250, 11),
		Task.of(Assignment.DAGANNOTH, 70, 130, 11),
		Task.of(Assignment.DUST_DEVILS, 70, 130, 200, 250, 9),
		Task.of(Assignment.ELVES, 70, 130, 8),
		Task.of(Assignment.FEVER_SPIDERS, 70, 130, 7),
		Task.of(Assignment.FIRE_GIANTS, 70, 130, 12),
		Task.of(Assignment.FOSSIL_ISLAND_WYVERNS, 10, 20, 55, 75, 7),
		Task.of(Assignment.GARGOYLES, 70, 130, 200, 250, 11),
		Task.of(Assignment.GREATER_DEMONS, 70, 130, 200, 250, 9),
		Task.of(Assignment.GRYPHONS, 60, 100, 5),
		Task.of(Assignment.HELLHOUNDS, 70, 130, 9),
		Task.of(Assignment.JELLIES, 70, 130, 10),
		Task.of(Assignment.JUNGLE_HORROR, 70, 130, 10),
		Task.of(Assignment.KALPHITE, 70, 130, 11),
		Task.of(Assignment.KURASK, 70, 130, 12),
		Task.of(Assignment.LESSER_DEMONS, 70, 130, 9),
		Task.of(Assignment.LESSER_NAGUA, 50, 100, 4),
		Task.of(Assignment.LIZARDMEN, 50, 90, 8),
		Task.of(Assignment.MUTATED_ZYGOMITES, 8, 15, 7),
		Task.of(Assignment.NECHRYAEL, 70, 130, 200, 250, 12),
		Task.of(Assignment.SHADOW_WARRIORS, 70, 130, 8),
		Task.of(Assignment.SKELETAL_WYVERNS, 10, 20, 50, 70, 7),
		Task.of(Assignment.SPIRITUAL_CREATURES, 70, 130, 181, 250, 12),
		Task.of(Assignment.TROLLS, 70, 130, 11),
		Task.of(Assignment.TUROTH, 70, 130, 10),
		Task.of(Assignment.TZHAAR, 90, 150, 8),
		Task.of(Assignment.VAMPYRES, 80, 100, 200, 250, 6),
		Task.of(Assignment.WARPED_CREATURES, 70, 130, 6),
		Task.of(Assignment.WYRMS, 60, 100, 200, 250, 6)
	),
	NIEVE("Nieve",
		Map.of(1, 12,
			10, 60,
			50, 180,
			100, 300,
			250, 420,
			1000, 600),
		Map.of(1, 15,
			10, 75,
			50, 225,
			100, 375,
			250, 525,
			1000, 750),
		Task.bosses(8),
		Task.of(Assignment.ABERRANT_SPECTRES, 120, 185, 200, 250, 6),
		Task.of(Assignment.ABYSSAL_DEMONS, 120, 185, 200, 250, 9),
		Task.of(Assignment.ANKOU, 50, 90, 91, 150, 5),
		Task.of(Assignment.ARAXYTES, 40, 60, 200, 250, 8),
		Task.of(Assignment.AVIANSIES, 120, 185, 200, 250, 6),
		Task.of(Assignment.AQUANITES, 40, 60, 150, 200, 5),
		Task.of(Assignment.BASILISKS, 120, 185, 200, 250, 6),
		Task.of(Assignment.BLACK_DEMONS, 120, 185, 200, 250, 9),
		Task.of(Assignment.BLACK_DRAGONS, 10, 20, 40, 60, 6),
		Task.of(Assignment.BLOODVELD, 120, 185, 200, 250, 9),
		Task.of(Assignment.BLUE_DRAGONS, 120, 185, 4),
		Task.of(Assignment.BRINE_RATS, 120, 185, 3),
		Task.of(Assignment.CAVE_HORRORS, 120, 180, 200, 250, 5),
		Task.of(Assignment.CAVE_KRAKEN, 100, 120, 150, 200, 6),
		Task.of(Assignment.CUSTODIAN_STALKERS, 110, 170, 200, 250, 8),
		Task.of(Assignment.DAGANNOTH, 120, 185, 8),
		Task.of(Assignment.DARK_BEASTS, 10, 20, 110, 135, 5),
		Task.of(Assignment.DRAKES, 30, 95, 7),
		Task.of(Assignment.DUST_DEVILS, 120, 185, 200, 250, 6),
		Task.of(Assignment.ELVES, 60, 90, 4),
		Task.of(Assignment.FIRE_GIANTS, 120, 185, 9),
		Task.of(Assignment.FOSSIL_ISLAND_WYVERNS, 5, 25, 55, 75, 5),
		Task.of(Assignment.GARGOYLES, 120, 185, 200, 250, 6),
		Task.of(Assignment.GREATER_DEMONS, 120, 185, 200, 250, 7),
		Task.of(Assignment.GRYPHONS, 110, 170, 7),
		Task.of(Assignment.HELLHOUNDS, 120, 185, 8),
		Task.of(Assignment.KALPHITE, 120, 185, 9),
		Task.of(Assignment.KURASK, 120, 185, 3),
		Task.of(Assignment.LIZARDMEN, 90, 120, 8),
		Task.of(Assignment.METAL_DRAGONS, 30, 40, 150, 200, 12),
		Task.of(Assignment.MUTATED_ZYGOMITES, 10, 25, 2),
		Task.of(Assignment.NECHRYAEL, 110, 170, 200, 250, 7),
		Task.of(Assignment.RED_DRAGONS, 30, 80, 5),
		Task.of(Assignment.MINIONS_OF_SCABARAS, 30, 60, 130, 170, 4),
		Task.of(Assignment.SKELETAL_WYVERNS, 5, 15, 50, 70, 5),
		Task.of(Assignment.SMOKE_DEVILS, 120, 185, 7),
		Task.of(Assignment.SPIRITUAL_CREATURES, 120, 185, 181, 250, 6),
		Task.of(Assignment.SUQAHS, 120, 185, 186, 250, 8),
		Task.of(Assignment.TROLLS, 120, 185, 6),
		Task.of(Assignment.TUROTH, 120, 185, 3),
		Task.of(Assignment.TZHAAR, 110, 180, 10),
		Task.of(Assignment.VAMPYRES, 110, 170, 200, 250, 6),
		Task.of(Assignment.WARPED_CREATURES, 120, 185, 6),
		Task.of(Assignment.WYRMS, 80, 145, 200, 250, 7)
	),
	DURADEL("Duradel",
		Map.of(1, 15,
			10, 75,
			50, 225,
			100, 375,
			250, 525,
			1000, 750),
		Task.bosses(12),
		Task.of(Assignment.ABERRANT_SPECTRES, 130, 200, 200, 250, 7),
		Task.of(Assignment.ABYSSAL_DEMONS, 130, 200, 200, 250, 12),
		Task.of(Assignment.ANKOU, 50, 80, 91, 150, 5),
		Task.of(Assignment.AQUANITES, 30, 50, 150, 200, 5),
		Task.of(Assignment.ARAXYTES, 60, 80, 200, 250, 10),
		Task.of(Assignment.AVIANSIES, 120, 200, 200, 250, 8),
		Task.of(Assignment.BASILISKS, 130, 200, 200, 250, 7),
		Task.of(Assignment.BLACK_DEMONS, 130, 200, 200, 250, 8),
		Task.of(Assignment.BLACK_DRAGONS, 10, 20, 40, 60, 9),
		Task.of(Assignment.BLOODVELD, 130, 200, 200, 250, 8),
		Task.of(Assignment.BLUE_DRAGONS, 110, 170, 4),
		Task.of(Assignment.CAVE_HORRORS, 130, 200, 200, 250, 4),
		Task.of(Assignment.CAVE_KRAKEN, 100, 120, 150, 200, 9),
		Task.of(Assignment.DAGANNOTH, 130, 200, 9),
		Task.of(Assignment.DARK_BEASTS, 10, 20, 110, 135, 11),
		Task.of(Assignment.DRAKES, 50, 110, 8),
		Task.of(Assignment.DUST_DEVILS, 130, 200, 200, 250, 5),
		Task.of(Assignment.ELVES, 110, 170, 4),
		Task.of(Assignment.FIRE_GIANTS, 130, 200, 7),
		Task.of(Assignment.FOSSIL_ISLAND_WYVERNS, 20, 50, 55, 75, 7),
		Task.of(Assignment.GARGOYLES, 130, 200, 200, 250, 8),
		Task.of(Assignment.GREATER_DEMONS, 130, 200, 200, 250, 9),
		Task.of(Assignment.GRYPHONS, 100, 210, 7),
		Task.of(Assignment.HELLHOUNDS, 130, 200, 10),
		Task.of(Assignment.KALPHITE, 130, 200, 9),
		Task.of(Assignment.KURASK, 130, 200, 4),
		Task.of(Assignment.LIZARDMEN, 130, 210, 10),
		Task.of(Assignment.METAL_DRAGONS, 35, 45, 150, 200, 14),
		Task.of(Assignment.MUTATED_ZYGOMITES, 20, 30, 2),
		Task.of(Assignment.NECHRYAEL, 130, 200, 200, 250, 9),
		Task.of(Assignment.RED_DRAGONS, 30, 65, 8),
		Task.of(Assignment.SKELETAL_WYVERNS, 20, 40, 50, 70, 7),
		Task.of(Assignment.SMOKE_DEVILS, 130, 200, 9),
		Task.of(Assignment.SPIRITUAL_CREATURES, 130, 200, 181, 250, 7),
		Task.of(Assignment.SUQAHS, 60, 90, 186, 250, 8),
		Task.of(Assignment.TROLLS, 130, 200, 6),
		Task.of(Assignment.TZHAAR, 130, 199, 10),
		Task.of(Assignment.VAMPYRES, 100, 210, 200, 250, 8),
		Task.of(Assignment.WARPED_CREATURES, 130, 200, 8),
		Task.of(Assignment.WATERFIENDS, 130, 200, 2),
		Task.of(Assignment.WYRMS, 100, 160, 200, 250, 8)
	),
	KONAR("Konar",
		Map.of(1, 18,
			10, 90,
			50, 270,
			100, 450,
			250, 630,
			1000, 900),
		Map.of(1, 20,
			10, 100,
			50, 300,
			100, 500,
			250, 700,
			1000, 1000),
		Task.bosses(8),
		Task.of(Assignment.ABERRANT_SPECTRES, 120, 170, 200, 250, 6),
		Task.of(Assignment.ABYSSAL_DEMONS, 120, 170, 200, 250, 9),
		Task.of(Assignment.ANKOU, 50, 50, 91, 150, 5),
		Task.of(Assignment.AVIANSIES, 120, 170, 200, 250, 6),
		Task.of(Assignment.BASILISKS, 110, 170, 200, 250, 5),
		Task.of(Assignment.BLACK_DEMONS, 120, 170, 200, 250, 9),
		Task.of(Assignment.BLACK_DRAGONS, 10, 15, 40, 60, 6),
		Task.of(Assignment.BLOODVELD, 120, 170, 200, 250, 9),
		Task.of(Assignment.BLUE_DRAGONS, 120, 170, 4),
		Task.of(Assignment.BRINE_RATS, 120, 170, 2),
		Task.of(Assignment.CAVE_KRAKEN, 80, 100, 150, 200, 9),
		Task.of(Assignment.DAGANNOTH, 120, 170, 8),
		Task.of(Assignment.DARK_BEASTS, 10, 15, 110, 135, 5),
		Task.of(Assignment.DRAKES, 75, 140, 10),
		Task.of(Assignment.DUST_DEVILS, 120, 170, 200, 250, 6),
		Task.of(Assignment.FIRE_GIANTS, 120, 170, 9),
		Task.of(Assignment.FOSSIL_ISLAND_WYVERNS, 15, 30, 55, 75, 5),
		Task.of(Assignment.GARGOYLES, 120, 170, 200, 250, 6),
		Task.of(Assignment.GREATER_DEMONS, 120, 170, 200, 250, 7),
		Task.of(Assignment.HELLHOUNDS, 120, 170, 8),
		Task.of(Assignment.HYDRAS, 125, 190, 10),
		Task.of(Assignment.JELLIES, 120, 170, 6),
		Task.of(Assignment.KALPHITE, 120, 170, 9),
		Task.of(Assignment.KURASK, 120, 170, 3),
		Task.of(Assignment.LESSER_NAGUA, 55, 120, 2),
		Task.of(Assignment.LIZARDMEN, 90, 110, 8),
		Task.of(Assignment.METAL_DRAGONS, 30, 40, 150, 200, 15),
		Task.of(Assignment.MUTATED_ZYGOMITES, 10, 25, 2),
		Task.of(Assignment.NECHRYAEL, 110, 110, 200, 250, 7),
		Task.of(Assignment.RED_DRAGONS, 30, 50, 5),
		Task.of(Assignment.SKELETAL_WYVERNS, 5, 12, 50, 70, 5),
		Task.of(Assignment.SMOKE_DEVILS, 120, 170, 7),
		Task.of(Assignment.TROLLS, 120, 170, 6),
		Task.of(Assignment.TUROTH, 120, 170, 3),
		Task.of(Assignment.VAMPYRES, 100, 160, 200, 250, 4),
		Task.of(Assignment.WARPED_CREATURES, 110, 170, 4),
		Task.of(Assignment.WATERFIENDS, 120, 170, 2),
		Task.of(Assignment.WYRMS, 125, 190, 200, 250, 10)
	);

	private final String name;
	private final Map<Integer, Integer> pointRewards;
	private final Map<Integer, Integer> increaedPointRewards;
	private final Task[] tasks;

	Master(String name, Map<Integer, Integer> pointRewards, Map<Integer, Integer> increaedPointRewards, Task[] bosses, Task... tasks)
	{
		this.name = name;
		this.pointRewards = pointRewards;
		this.increaedPointRewards = increaedPointRewards;
		this.tasks = Stream.concat(Arrays.stream(bosses), Arrays.stream(tasks))
			.toArray(Task[]::new);
	}

	Master(String name, Map<Integer, Integer> pointRewards, Task[] bosses, Task... tasks)
	{
		this(name,
			pointRewards,
			Collections.emptyMap(),
			Stream.concat(Arrays.stream(bosses), Arrays.stream(tasks))
				.toArray(Task[]::new)
		);
	}

	Master(String name, Map<Integer, Integer> pointRewards, Task... tasks)
	{
		this(name, pointRewards, new Task[0], tasks);
	}

	public Task getTaskByName(String name)
	{
		return Arrays.stream(tasks).filter(t -> t.getAssignment().getName().equals(name)).findFirst().orElse(null);
	}

	public double getTaskPointRevenueAtTimeScale(int timeScale, boolean westernElite, boolean kourendElite)
	{
		Map<Integer, Integer> rewardsTable;
		if (this == NIEVE && westernElite || this == KONAR && kourendElite)
		{
			rewardsTable = increaedPointRewards;
		}
		else
		{
			rewardsTable = pointRewards;
		}

		double points = 0.0;

		for (int i = timeScale; i > 0; i--)
		{
			int multiple = 1;
			for (int t : new int[]{1000, 250, 100, 50, 10, 1})
			{
				if (i % t == 0)
				{
					multiple = t;
					break;
				}
			}
			points += rewardsTable.get(multiple);
		}

		return points / timeScale;
	}

	public static class Task
	{
		public static final int NUM_BOSSES = (int) Arrays.stream(Assignment.values()).filter(Assignment::isBoss).count();
		public static final int BOSS_MIN_AMOUNT = 3;
		public static final int BOSS_MAX_AMOUNT = 35;

		@Getter
		Assignment assignment;
		int minAmount;
		int maxAmount;
		int minExtendedAmount;
		int maxExtendedAmount;
		@Getter
		double weight;

		Task(Assignment assignment, int minAmount, int maxAmount, int minExtendedAmount, int maxExtendedAmount, double weight)
		{
			this.assignment = assignment;
			this.minAmount = minAmount;
			this.maxAmount = maxAmount;
			this.minExtendedAmount = minExtendedAmount;
			this.maxExtendedAmount = maxExtendedAmount;
			this.weight = weight;
		}

		static Task[] bosses(double weight)
		{
			return Arrays.stream(Assignment.values())
				.filter(Assignment::isBoss)
				.map(a -> Task.of(a, BOSS_MIN_AMOUNT, BOSS_MAX_AMOUNT, weight / NUM_BOSSES))
				.toArray(Task[]::new);
		}

		static Task of(Assignment assignment, int minAmount, int maxAmount, int minExtendedAmount, int maxExtendedAmount, double weight)
		{
			return new Task(assignment, minAmount, maxAmount, minExtendedAmount, maxExtendedAmount, weight);
		}

		static Task of(Assignment assignment, int minAmount, int maxAmount, double weight)
		{
			return of(assignment, minAmount, maxAmount, -1, -1, weight);
		}

		public double getAverageAmount(Set<Assignment> extendedAssignments)
		{
			boolean extended = extendedAssignments.contains(assignment);
			return extended ? (minExtendedAmount + maxExtendedAmount) / 2.0 : (minAmount + maxAmount) / 2.0;
		}
	}
}
