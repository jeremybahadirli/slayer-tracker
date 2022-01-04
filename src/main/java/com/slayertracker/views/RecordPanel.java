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
package com.slayertracker.views;

import com.slayertracker.SlayerTrackerConfig;
import com.slayertracker.records.Record;
import com.slayertracker.views.components.HeaderPanel;
import com.slayertracker.views.components.StatsPanel;
import java.awt.Color;
import java.util.Arrays;
import javax.swing.JPanel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecordPanel extends JPanel
{
	SlayerTrackerConfig slayerTrackerConfig;

	Record record;

	HeaderPanel headerPanel;
	JPanel bodyPanel;
	StatsPanel statsPanel;

	void update()
	{
		bodyPanel.remove(statsPanel);
		statsPanel = new StatsPanel(record, slayerTrackerConfig);
		bodyPanel.add(statsPanel);
	}

	void expand()
	{
		if (isCollapsed())
		{
			bodyPanel.setVisible(true);
			toggleDimmer(true);
		}
	}

	void collapse()
	{
		if (!isCollapsed())
		{
			bodyPanel.setVisible(false);
			toggleDimmer(false);
		}
	}

	void toggleCollapsed()
	{
		if (isCollapsed())
		{
			expand();
		}
		else
		{
			collapse();
		}
	}

	boolean isCollapsed()
	{
		return !bodyPanel.isVisible();
	}

	private void toggleDimmer(boolean brighten)
	{
		Arrays.stream(headerPanel.getComponents()).forEach(component -> {
			Color color = component.getForeground();
			component.setForeground(brighten ? color.brighter() : color.darker());
		});
	}
}