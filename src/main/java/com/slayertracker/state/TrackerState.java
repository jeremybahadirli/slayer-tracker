/*
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

package com.slayertracker.state;

import com.slayertracker.groups.Assignment;
import com.slayertracker.records.AssignmentRecord;
import com.slayertracker.records.RecordMap;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayDeque;
import java.util.Deque;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.NPC;

@Getter
@Singleton
public class TrackerState implements PropertyChangeListener
{
	private final PropertyChangeSupport support = new PropertyChangeSupport(this);

	private final RecordMap<Assignment, AssignmentRecord> assignmentRecords;

	private final Deque<EndedInteraction> endedInteractions = new ArrayDeque<>();
	private final Deque<KillEvent> killEvents = new ArrayDeque<>();
	private final Deque<XpDropEvent> xpDropEvents = new ArrayDeque<>();
	private final Deque<TaskAmountChange> taskAmountChanges = new ArrayDeque<>();

	private Assignment currentAssignment;
	@Setter
	private int remainingAmount;
	@Setter
	private int cachedXp = -1;
	@Setter
	private String profileFileName;

	@Inject
	public TrackerState()
	{
		assignmentRecords = new RecordMap<>(this);
	}

	public void clear()
	{
		assignmentRecords.clear();
		endedInteractions.clear();
		killEvents.clear();
		xpDropEvents.clear();
		taskAmountChanges.clear();
		currentAssignment = null;
		remainingAmount = 0;
		cachedXp = -1;
		profileFileName = null;
	}

	public void setCurrentAssignment(@Nullable Assignment assignment)
	{
		this.currentAssignment = assignment;
		if (currentAssignment != null)
		{
			assignmentRecords.putIfAbsent(currentAssignment, new AssignmentRecord(this));
		}
	}

	public AssignmentRecord getCurrentAssignmentRecord()
	{
		return assignmentRecords.get(currentAssignment);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		support.addPropertyChangeListener(listener);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		support.firePropertyChange(evt);
	}

	@Getter
	public static class EndedInteraction
	{
		private final NPC npc;
		private int lastInteractedTick;
		private boolean dead;

		public EndedInteraction(NPC npc, int lastInteractedTick, boolean dead)
		{
			this.npc = npc;
			this.lastInteractedTick = lastInteractedTick;
			this.dead = dead;
		}

		public void updateTick(int tick)
		{
			lastInteractedTick = tick;
		}

		public void markDead()
		{
			dead = true;
		}

		@Override
		public String toString()
		{
			return "npc=" + npc + ", npc name=" + npc.getName() + ", lastInteractedTick=" + lastInteractedTick + ", dead=" + dead;
		}
	}

	@Getter
	public static class KillEvent
	{
		private final NPC npc;
		private final Assignment assignment;
		private final int tick;

		private boolean kcLogged;
		private boolean xpLogged;
		private boolean lootLogged;

		public KillEvent(NPC npc, Assignment assignment, int tick)
		{
			this.npc = npc;
			this.assignment = assignment;
			this.tick = tick;
		}

		public void markKcLogged()
		{
			kcLogged = true;
		}

		public void markXpLogged()
		{
			xpLogged = true;
		}

		public void markLootLogged()
		{
			lootLogged = true;
		}

		public boolean isCompleted()
		{
			return kcLogged && lootLogged && xpLogged;
		}

		@Override
		public String toString()
		{
			return "npc=" + npc + ", npc name=" + npc.getName() + ", assignment=" + assignment + ", tick=" + tick + ", kcL=" + kcLogged + ", xpL=" + xpLogged + ", lootL=" + lootLogged;
		}
	}

	public record XpDropEvent(int xp, int tick)
	{
	}

	@Getter
	public static class TaskAmountChange
	{
		private final int amount;
		private int unloggedAmount;
		private final int tick;

		public TaskAmountChange(int amount, int tick)
		{
			this.amount = amount;
			this.unloggedAmount = amount;
			this.tick = tick;
		}

		public void consume(int amount)
		{
			unloggedAmount -= amount;
		}

		public boolean isConsumed()
		{
			return unloggedAmount <= 0;
		}

		@Override
		public String toString()
		{
			return "amount=" + amount + ", unlogged amount=" + unloggedAmount + ", tick=" + tick;
		}
	}
}
