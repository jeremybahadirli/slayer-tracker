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
package com.slayertracker.records;

import com.google.gson.annotations.Expose;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import net.runelite.api.NPC;

@Getter
public class Record
{
	@Expose
	private int kc;
	@Expose
	private float hours;
	@Expose
	private int xp;
	@Expose
	private int ge;
	@Expose
	private int ha;
	@Expose
	private Instant combatInstant;

	private final Set<NPC> interactors = new HashSet<>();
	private final PropertyChangeSupport support = new PropertyChangeSupport(this);

	public Record(PropertyChangeListener pcl)
	{
		support.addPropertyChangeListener(pcl);

		kc = 0;
		hours = 0f;
		xp = 0;
		ge = 0;
		ha = 0;
		combatInstant = Instant.now();
	}

	public void incrementKc()
	{
		int oldVal = kc;
		kc++;
		support.firePropertyChange("Record kc", oldVal, kc);
	}

	public void addToHours(Duration d)
	{
		float oldVal = hours;
		hours = hours + (d.getSeconds() / 3600f);
		support.firePropertyChange("Record hours", oldVal, hours);
	}

	public void addToXp(int i)
	{
		int oldVal = xp;
		xp += i;
		support.firePropertyChange("Record xp", oldVal, xp);
	}

	public void addToGe(int i)
	{
		int oldVal = ge;
		ge += i;
		support.firePropertyChange("Record ge", oldVal, ge);
	}

	public void addToHa(int i)
	{
		int oldVal = ha;
		ha += i;
		support.firePropertyChange("Record ha", oldVal, ha);
	}

	public void setCombatInstant(Instant instant)
	{
		combatInstant = instant;
	}
}