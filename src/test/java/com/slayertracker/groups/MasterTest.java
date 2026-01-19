package com.slayertracker.groups;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class MasterTest
{
	@Test
	public void duradelRevenueAtMaxIntervalUsesAllThresholds()
	{
		double revenue;

		revenue = Master.DURADEL.getTaskPointRevenueAtTimeScale(1);
		assertEquals(15.0, revenue, 1e-9);

		revenue = Master.DURADEL.getTaskPointRevenueAtTimeScale(10);
		assertEquals(21.0, revenue, 1e-9);

		revenue = Master.DURADEL.getTaskPointRevenueAtTimeScale(50);
		assertEquals(24.0, revenue, 1e-9);

		revenue = Master.DURADEL.getTaskPointRevenueAtTimeScale(100);
		assertEquals(25.5, revenue, 1e-9);

		revenue = Master.DURADEL.getTaskPointRevenueAtTimeScale(250);
		assertEquals(26.4, revenue, 1e-9);

		revenue = Master.DURADEL.getTaskPointRevenueAtTimeScale(1000);
		assertEquals(26.625, revenue, 1e-9);
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsNonPositiveInterval()
	{
		Master.DURADEL.getTaskPointRevenueAtTimeScale(0);
	}
}
