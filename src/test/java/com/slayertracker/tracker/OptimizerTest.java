package com.slayertracker.tracker;

import java.util.List;
import org.junit.Test;

public class OptimizerTest
{
	@Test
	public void optimizerTest()
	{
		List<Optimizer.Task> tasks = List.of(
			Optimizer.Task.normal("Dagannoth", 14577.1133, 1.11798, 9.0),
			Optimizer.Task.normal("Spiritual creatures", 13445.8584, 0.97353, 7.0),
			Optimizer.Task.normal("Lizardmen", 3351.4792, 7.98056, 10.0),
			Optimizer.Task.normal("Suqahs", 13620.6553, 0.59245, 8.0),
			Optimizer.Task.normal("Ankou", 16457.1445, 0.21065, 5.0),
			Optimizer.Task.boss("Sarachnis", 203625.0, 0.10, 0.50667, 0.36363636363636365),
			Optimizer.Task.normal("Hellhounds", 17410.4395, 1.09934, 10.0),
			Optimizer.Task.normal("Gargoyles", 7954.1494, 11.59583, 8.0)
		);

		Optimizer.Result r = Optimizer.optimize(tasks, 20, 1);
		System.out.println(Optimizer.formatResult(r));
	}
}
