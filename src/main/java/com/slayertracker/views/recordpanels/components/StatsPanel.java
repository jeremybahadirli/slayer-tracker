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
package com.slayertracker.views.recordpanels.components;

import com.slayertracker.SlayerTrackerConfig;
import com.slayertracker.records.Record;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.text.NumberFormat;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.runelite.client.ui.FontManager;

public class StatsPanel extends JPanel
{

	public StatsPanel(Record record, SlayerTrackerConfig.LootUnit lootUnit)
	{
		NumberFormat formatter = NumberFormat.getInstance();
		GpValues gpValues = new GpValues(record, lootUnit, formatter);

		setLayout(new GridLayout());
		setOpaque(false);

		add(buildStatColumn(new String[][]{
			{"kc: ", formatter.format(record.getKc())},
			{"xp: ", formatter.format(record.getXp())},
			{gpValues.label, gpValues.value}
		}));

		add(buildStatColumn(new String[][]{
			{"kc/h: ", formatter.format(Math.round(record.getKc() / record.getHours()))},
			{"xp/h: ", formatter.format(Math.round(record.getXp() / record.getHours()))},
			{gpValues.rateLabel, gpValues.rateValue}
		}));
	}

	private JPanel buildStatColumn(String[][] rows)
	{
		JPanel stats = new JPanel();
		stats.setLayout(new GridBagLayout());
		stats.setOpaque(false);

		GridBagConstraints baseConstraints = new GridBagConstraints();
		baseConstraints.anchor = GridBagConstraints.LINE_START;

		for (int rowIndex = 0; rowIndex < rows.length; rowIndex++)
		{
			addRow(stats, baseConstraints, rowIndex, rows[rowIndex][0], rows[rowIndex][1]);
		}

		return stats;
	}

	private void addRow(JPanel panel, GridBagConstraints template, int row, String labelText, String valueText)
	{
		GridBagConstraints labelConstraints = (GridBagConstraints) template.clone();
		labelConstraints.weightx = 0;
		labelConstraints.gridx = 0;
		labelConstraints.gridy = row;
		panel.add(createStatLabel(labelText), labelConstraints);

		GridBagConstraints valueConstraints = (GridBagConstraints) template.clone();
		valueConstraints.weightx = 1;
		valueConstraints.gridx = 1;
		valueConstraints.gridy = row;
		panel.add(createStatLabel(valueText), valueConstraints);
	}

	private JLabel createStatLabel(String text)
	{
		JLabel label = new JLabel(text);
		label.setFont(FontManager.getRunescapeSmallFont());
		return label;
	}

	private static class GpValues
	{
		private final String label;
		private final String value;
		private final String rateLabel;
		private final String rateValue;

		private GpValues(Record record, SlayerTrackerConfig.LootUnit lootUnit, NumberFormat formatter)
		{
			if (lootUnit.equals(SlayerTrackerConfig.LootUnit.HIGH_ALCHEMY))
			{
				label = "ha: ";
				value = formatter.format(record.getHa());
				rateLabel = "ha/h: ";
				rateValue = formatter.format(Math.round(record.getHa() / record.getHours()));
			}
			else
			{
				label = "ge: ";
				value = formatter.format(record.getGe());
				rateLabel = "ge/h: ";
				rateValue = formatter.format(Math.round(record.getGe() / record.getHours()));
			}
		}
	}
}
