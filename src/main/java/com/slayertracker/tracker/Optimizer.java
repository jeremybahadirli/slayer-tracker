package com.slayertracker.tracker;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Slayer policy optimizer (DO / SKIP / BLOCK) with bosses.
 * <p>
 * Primary objective: maximize achieved value/hour (XP or GP).
 * Tie-break objective: among policies with equal value/hour (within epsilon), maximize surplus points
 * (reported as points/assignment and points/hour).
 * <p>
 * Assumptions (your "initial assumptions"):
 * - SKIP is instant: 0 time, 0 value.
 * - DO yields value at constant valuePerHour for that task, over the chosen duration.
 * - Points:
 * - DO earns taskPointRevenue (TPR) points per completion.
 * - SKIP costs skipPrice (SP) points per skip.
 * - Sustainability constraint: expected points change per assignment >= 0.
 * - BLOCK removes a task from the assignment pool; limited by blockSlots.
 * - Bosses are NOT blockable.
 * - Boss kill-count choice affects duration only; under constant value/h, optimal is extremal:
 * - If boss value/h > equilibrium value/h, DO_BOSS_MAX; else DO_BOSS_MIN (when DO).
 * <p>
 * Notes:
 * - BLOCK selection uses a greedy "try each candidate block and re-optimize" step.
 * - Tie-break is applied in the greedy selection too.
 */
public class Optimizer
{
	private static final int SKIP_PRICE = 30;

	// -------------------- Data types --------------------

	public static final class Task
	{
		public final String name;
		public final double valuePerHour;  // r_i
		public final double weight;     // w_i (prob proportional)
		public final boolean boss;

		// For normal tasks: minHours == maxHours == fixed duration.
		public final double minHours;   // boss: time at min kills
		public final double maxHours;   // boss: time at max kills

		private Task(String name, double valuePerHour, double weight, boolean boss, double minHours, double maxHours)
		{
			if (name == null || name.isBlank())
			{
				throw new IllegalArgumentException("name");
			}
			if (!Double.isFinite(valuePerHour) || valuePerHour < 0.0)
			{
				throw new IllegalArgumentException("valuePerHour");
			}
			if (!Double.isFinite(weight) || weight <= 0.0)
			{
				throw new IllegalArgumentException("weight");
			}
			if (!Double.isFinite(minHours) || minHours <= 0.0)
			{
				throw new IllegalArgumentException("minHours");
			}
			if (!Double.isFinite(maxHours) || maxHours <= 0.0)
			{
				throw new IllegalArgumentException("maxHours");
			}
			if (maxHours < minHours)
			{
				throw new IllegalArgumentException("maxHours must be >= minHours");
			}

			this.name = name;
			this.valuePerHour = valuePerHour;
			this.weight = weight;
			this.boss = boss;
			this.minHours = minHours;
			this.maxHours = maxHours;
		}

		public static Task normal(String name, double valuePerHour, double hours, double weight)
		{
			return new Task(name, valuePerHour, weight, false, hours, hours);
		}

		public static Task boss(String name, double valuePerHour, double minHours, double maxHours, double weight)
		{
			return new Task(name, valuePerHour, weight, true, minHours, maxHours);
		}
	}

	public enum Action
	{
		BLOCK,
		SKIP,
		DO,
		DO_BOSS_MIN,
		DO_BOSS_MAX
	}

	public static final class Result
	{
		public final Map<String, Action> actionByTask;

		public final List<String> block;
		public final List<String> skip;
		public final List<String> doNormal;
		public final List<String> doBossMin;
		public final List<String> doBossMax;

		public final double achievedValuePerHour;
		public final double pointsPerHour;

		private Result(
			Map<String, Action> actionByTask,
			List<String> block,
			List<String> skip,
			List<String> doNormal,
			List<String> doBossMin,
			List<String> doBossMax,
			double achievedValuePerHour,
			double pointsPerHour)
		{
			this.actionByTask = actionByTask;
			this.block = block;
			this.skip = skip;
			this.doNormal = doNormal;
			this.doBossMin = doBossMin;
			this.doBossMax = doBossMax;
			this.achievedValuePerHour = achievedValuePerHour;
			this.pointsPerHour = pointsPerHour;
		}
	}

	// -------------------- Public API --------------------

	public static Result optimize(
		List<Task> tasks,
		double taskPointRevenue,
		int blockSlots)
	{
		if (tasks == null || tasks.isEmpty())
		{
			return emptyResult();
		}
		if (taskPointRevenue <= 0)
		{
			throw new IllegalArgumentException("task point revenue must be > 0.");
		}
		if (blockSlots < 0)
		{
			throw new IllegalArgumentException("blockSlots must be >= 0.");
		}

		final List<Task> all = new ArrayList<>(tasks);

		// Greedy block selection. Bosses cannot be blocked.
		final Set<Integer> blocked = new LinkedHashSet<>();

		Policy bestSoFar = optimizeDoSkip(all, blocked, taskPointRevenue);

		for (int slotsLeft = blockSlots; slotsLeft > 0; slotsLeft--)
		{
			int bestIdx = -1;
			Policy bestTrial = bestSoFar;

			for (int i = 0; i < all.size(); i++)
			{
				if (blocked.contains(i) || all.get(i).boss)
				{
					continue;
				}

				Set<Integer> trialBlocked = new LinkedHashSet<>(blocked);
				trialBlocked.add(i);

				Policy trial = optimizeDoSkip(all, trialBlocked, taskPointRevenue);

				// Choose better policy: value/hour first, then surplus points.
				if (better(trial, bestTrial))
				{
					bestTrial = trial;
					bestIdx = i;
				}
			}

			// Stop if no candidate improves (or ties value/hour but improves points).
			if (bestIdx < 0 || !better(bestTrial, bestSoFar))
			{
				break;
			}

			blocked.add(bestIdx);
			bestSoFar = bestTrial;
		}

		Policy finalPolicy = optimizeDoSkip(all, blocked, taskPointRevenue);
		double equilibriumRate = finalPolicy.valuePerHour;

		// Classify tasks into DO/SKIP/BLOCK and boss min/max buckets.
		Map<String, Action> actionByTask = new LinkedHashMap<>();
		List<String> block = new ArrayList<>();
		List<String> skip = new ArrayList<>();
		List<String> doNormal = new ArrayList<>();
		List<String> doBossMin = new ArrayList<>();
		List<String> doBossMax = new ArrayList<>();

		for (int i = 0; i < all.size(); i++)
		{
			Task t = all.get(i);

			if (blocked.contains(i))
			{
				actionByTask.put(t.name, Action.BLOCK);
				block.add(t.name);
				continue;
			}

			if (!finalPolicy.doMask[i])
			{
				actionByTask.put(t.name, Action.SKIP);
				skip.add(t.name);
				continue;
			}

			if (!t.boss)
			{
				actionByTask.put(t.name, Action.DO);
				doNormal.add(t.name);
			}
			else if (t.valuePerHour > equilibriumRate + EPS_VALUE)
			{
				actionByTask.put(t.name, Action.DO_BOSS_MAX);
				doBossMax.add(t.name);
			}
			else
			{
				actionByTask.put(t.name, Action.DO_BOSS_MIN);
				doBossMin.add(t.name);
			}
		}

		return new Result(
			actionByTask,
			block,
			skip,
			doNormal,
			doBossMin,
			doBossMax,
			finalPolicy.valuePerHour,
			finalPolicy.pointsPerHour
		);
	}

	public static String formatResult(Result r)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Optimized value rate: ").append(String.format(Locale.US, "%.2f", r.achievedValuePerHour)).append('\n');
		sb.append("Surplus points/h: ").append(String.format(Locale.US, "%.4f", r.pointsPerHour)).append('\n');

		sb.append("\nBLOCK (").append(r.block.size()).append(")\n");
		for (String s : r.block)
		{
			sb.append("  - ").append(s).append('\n');
		}

		sb.append("\nSKIP (").append(r.skip.size()).append(")\n");
		for (String s : r.skip)
		{
			sb.append("  - ").append(s).append('\n');
		}

		sb.append("\nDO (normal) (").append(r.doNormal.size()).append(")\n");
		for (String s : r.doNormal)
		{
			sb.append("  - ").append(s).append('\n');
		}

		sb.append("\nDO (boss min) (").append(r.doBossMin.size()).append(")\n");
		for (String s : r.doBossMin)
		{
			sb.append("  - ").append(s).append('\n');
		}

		sb.append("\nDO (boss max) (").append(r.doBossMax.size()).append(")\n");
		for (String s : r.doBossMax)
		{
			sb.append("  - ").append(s).append('\n');
		}

		return sb.toString();
	}

	// -------------------- Internals: DO/SKIP optimizer --------------------

	private static final double EPS_VALUE = 1e-10;
	private static final double EPS_POINTS = 1e-10;

	private static final class Policy
	{
		final boolean[] doMask;    // indexed over all tasks; blocked are false
		final double[] doHours;    // effective hours when DO; 0 otherwise

		final double valuePerHour;         // objective #1
		final double pointsPerAssignment; // tie-break metric option #1
		final double pointsPerHour;       // tie-break metric option #2

		final double pMin; // sustainability constraint lower bound on DO probability mass

		Policy(boolean[] doMask, double[] doHours, double valuePerHour, double pPerA, double pPerH, double pMin)
		{
			this.doMask = doMask;
			this.doHours = doHours;
			this.valuePerHour = valuePerHour;
			this.pointsPerAssignment = pPerA;
			this.pointsPerHour = pPerH;
			this.pMin = pMin;
		}
	}

	private static Policy optimizeDoSkip(List<Task> all, Set<Integer> blocked, double tpr)
	{
		final int n = all.size();

		final double totalWeight = totalWeight(all, blocked);
		if (totalWeight <= 0.0)
		{
			return new Policy(new boolean[n], new double[n], 0.0, 0.0, 0.0, 0.0);
		}

		final double pMin = (double) Optimizer.SKIP_PRICE / (tpr + (double) Optimizer.SKIP_PRICE);

		double R = initialRateGuess(all, blocked, totalWeight);
		final int maxIters = 100;
		final double eps = 1e-10;

		Choice last = null;

		for (int iter = 0; iter < maxIters; iter++)
		{
			Choice choice = chooseDoSetForRate(all, blocked, totalWeight, pMin, R);

			double num = 0.0;
			double den = 0.0;

			for (int i = 0; i < n; i++)
			{
				if (blocked.contains(i) || !choice.doMask[i])
				{
					continue;
				}
				Task t = all.get(i);
				double q = t.weight / totalWeight;
				double h = choice.doHours[i];

				num += q * t.valuePerHour * h;
				den += q * h;
			}

			last = choice;

			if (den <= 0.0)
			{
				R = 0.0;
				break;
			}

			double newR = num / den;
			if (Math.abs(newR - R) <= eps * Math.max(1.0, Math.abs(R)))
			{
				R = newR;
				break;
			}
			R = newR;
		}

		// Enforce blocked tasks not DO (cosmetic).
		for (int i = 0; i < n; i++)
		{
			if (blocked.contains(i))
			{
				last.doMask[i] = false;
				last.doHours[i] = 0.0;
			}
		}

		// Compute surplus points metrics for tie-breaking / reporting.
		Metrics m = computeMetrics(all, blocked, last.doMask, last.doHours, totalWeight, tpr, R);
		return new Policy(last.doMask, last.doHours, m.valuePerHour, m.pointsPerAssignment, m.pointsPerHour, pMin);
	}

	private static final class Metrics
	{
		final double valuePerHour;
		final double pointsPerAssignment;
		final double pointsPerHour;

		Metrics(double valuePerHour, double pointsPerAssignment, double pointsPerHour)
		{
			this.valuePerHour = valuePerHour;
			this.pointsPerAssignment = pointsPerAssignment;
			this.pointsPerHour = pointsPerHour;
		}
	}

	private static Metrics computeMetrics(
		List<Task> all,
		Set<Integer> blocked,
		boolean[] doMask,
		double[] doHours,
		double totalWeight,
		double tpr,
		double valuePerHour)
	{
		// DO probability mass
		double pDo = 0.0;

		// Expected time per assignment: E[T] = sum q_i * hours_i (over DO tasks)
		double expectedTimePerAssignment = 0.0;

		for (int i = 0; i < all.size(); i++)
		{
			if (blocked.contains(i) || !doMask[i])
			{
				continue;
			}
			Task t = all.get(i);
			double q = t.weight / totalWeight;
			pDo += q;
			expectedTimePerAssignment += q * doHours[i];
		}

		// Expected net points per assignment:
		//   +TPR with probability pDo, -SP otherwise
		double pointsPerAssignment = (tpr + Optimizer.SKIP_PRICE) * pDo - Optimizer.SKIP_PRICE;

		// Points per hour: divide by expected time per assignment.
		// If expectedTimePerAssignment is 0 (shouldn't when pMin>0), define p/h as 0.
		double pointsPerHour = expectedTimePerAssignment > 0.0
			? (pointsPerAssignment / expectedTimePerAssignment)
			: 0.0;

		// In theory, pointsPerAssignment should be >= 0 (sustainability), but floating point can go tiny negative.
		// Not clamping; reporting raw helps debug.
		return new Metrics(valuePerHour, pointsPerAssignment, pointsPerHour);
	}

	private static double totalWeight(List<Task> all, Set<Integer> blocked)
	{
		double sum = 0.0;
		for (int i = 0; i < all.size(); i++)
		{
			if (!blocked.contains(i))
			{
				sum += all.get(i).weight;
			}
		}
		return sum;
	}

	private static double initialRateGuess(List<Task> all, Set<Integer> blocked, double totalWeight)
	{
		// Conservative guess: use minHours for bosses.
		double num = 0.0;
		double den = 0.0;

		for (int i = 0; i < all.size(); i++)
		{
			if (blocked.contains(i))
			{
				continue;
			}
			Task t = all.get(i);
			double q = t.weight / totalWeight;
			double h = t.minHours;

			num += q * t.valuePerHour * h;
			den += q * h;
		}

		return den > 0.0 ? (num / den) : 0.0;
	}

	private static final class Choice
	{
		final boolean[] doMask;
		final double[] doHours;

		Choice(boolean[] doMask, double[] doHours)
		{
			this.doMask = doMask;
			this.doHours = doHours;
		}
	}

	/**
	 * For candidate rate R, choose DO/SKIP to maximize:
	 * sum q_i * h_i * (value_i - R) * x_i
	 * subject to:
	 * sum q_i * x_i >= pMin
	 * <p>
	 * Boss hours selection:
	 * - If value_i > R: choose maxHours (bigger positive contribution).
	 * - If value_i <= R and forced to DO: choose minHours (smaller negative penalty).
	 */
	private static Choice chooseDoSetForRate(
		List<Task> all,
		Set<Integer> blocked,
		double totalWeight,
		double pMin,
		double R)
	{
		final int n = all.size();
		boolean[] doMask = new boolean[n];
		double[] doHours = new double[n];

		double doMass = 0.0;

		// Step 1: DO all tasks with value/h > R
		for (int i = 0; i < n; i++)
		{
			if (blocked.contains(i))
			{
				continue;
			}
			Task t = all.get(i);
			if (t.valuePerHour > R)
			{
				doMask[i] = true;
				doMass += t.weight / totalWeight;

				doHours[i] = t.boss ? t.maxHours : t.minHours;
			}
		}

		if (doMass + 1e-15 >= pMin)
		{
			return new Choice(doMask, doHours);
		}

		// Step 2: add least-harmful remaining tasks until we hit pMin.
		List<Integer> candidates = new ArrayList<>();
		for (int i = 0; i < n; i++)
		{
			if (blocked.contains(i) || doMask[i])
			{
				continue;
			}
			candidates.add(i);
		}

		candidates.sort((a, b) ->
		{
			Task ta = all.get(a);
			Task tb = all.get(b);

			// Forced-DO region: use minHours (boss min; normal fixed anyway).
			double sa = ta.minHours * (ta.valuePerHour - R);
			double sb = tb.minHours * (tb.valuePerHour - R);

			return Double.compare(sb, sa); // descending (least negative first)
		});

		for (int idx : candidates)
		{
			if (doMass + 1e-15 >= pMin)
			{
				break;
			}
			Task t = all.get(idx);

			doMask[idx] = true;
			doMass += t.weight / totalWeight;

			doHours[idx] = t.minHours; // forced DO => min hours for bosses
		}

		return new Choice(doMask, doHours);
	}

	// -------------------- Policy comparison (lexicographic) --------------------

	private static boolean better(Policy a, Policy b)
	{
		if (b == null)
		{
			return true;
		}

		// Primary: maximize value/hour
		if (a.valuePerHour > b.valuePerHour + EPS_VALUE)
		{
			return true;
		}
		if (Math.abs(a.valuePerHour - b.valuePerHour) > EPS_VALUE)
		{
			return false;
		}

		// Secondary: maximize surplus points/hour (more useful than points/assignment in practice)
		if (a.pointsPerHour > b.pointsPerHour + EPS_POINTS)
		{
			return true;
		}
		if (Math.abs(a.pointsPerHour - b.pointsPerHour) > EPS_POINTS)
		{
			return false;
		}

		// Tertiary: maximize surplus points/assignment
		return a.pointsPerAssignment > b.pointsPerAssignment + EPS_POINTS;
	}

	// -------------------- Misc --------------------

	private static Result emptyResult()
	{
		return new Result(
			new LinkedHashMap<>(),
			List.of(), List.of(), List.of(), List.of(), List.of(),
			0.0,
			0.0
		);
	}
}
