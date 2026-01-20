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
import javax.annotation.Nullable;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.QuantityFormatter;

public class StatsPanel extends JPanel
{

	public StatsPanel(Record record, SlayerTrackerConfig.LootUnit lootUnit)
	{
		GpValues gpValues = new GpValues(record, lootUnit);

		setLayout(new GridLayout());
		setOpaque(false);

		add(buildStatColumn(new String[][]{
			{
				"kc: ",
				QuantityFormatter.quantityToRSDecimalStack(record.getKc(), true),
				QuantityFormatter.formatNumber(record.getKc()) + " kc"
			},
			{
				"xp: ",
				QuantityFormatter.quantityToRSDecimalStack(record.getXp(), true),
				QuantityFormatter.formatNumber(record.getXp()) + " xp"
			},
			{
				gpValues.label,
				gpValues.value,
				gpValues.valueTooltip
			}
		}));

		add(buildStatColumn(new String[][]{
			{
				"kc/h: ",
				QuantityFormatter.quantityToRSDecimalStack(Math.round(record.getKc() / record.getHours()), true),
				QuantityFormatter.formatNumber(Math.round(record.getKc() / record.getHours())) + " kc/h"
			},
			{
				"xp/h: ",
				QuantityFormatter.quantityToRSDecimalStack(Math.round(record.getXp() / record.getHours()), true),
				QuantityFormatter.formatNumber(Math.round(record.getXp() / record.getHours())) + " xp/h"
			},
			{
				gpValues.rateLabel,
				gpValues.rateValue,
				gpValues.rateValueTooltip
			}
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
			addRow(stats, baseConstraints, rowIndex, rows[rowIndex][0], rows[rowIndex][1], rows[rowIndex][2]);
		}

		return stats;
	}

	private void addRow(JPanel panel, GridBagConstraints template, int row, String labelText, String valueText, String tooltipText)
	{
		GridBagConstraints labelConstraints = (GridBagConstraints) template.clone();
		labelConstraints.weightx = 0;
		labelConstraints.gridx = 0;
		labelConstraints.gridy = row;
		panel.add(createStatLabel(labelText, null), labelConstraints);

		GridBagConstraints valueConstraints = (GridBagConstraints) template.clone();
		valueConstraints.weightx = 1;
		valueConstraints.gridx = 1;
		valueConstraints.gridy = row;
		panel.add(createStatLabel(valueText, tooltipText), valueConstraints);
	}

	private JLabel createStatLabel(String text, @Nullable String tooltipText)
	{
		JLabel label = new JLabel(text);
		label.setFont(FontManager.getRunescapeSmallFont());
		if (tooltipText != null)
		{
			label.setToolTipText(tooltipText);
		}
		return label;
	}

	private static class GpValues
	{
		private final String label;
		private final String value;
		private final String rateLabel;
		private final String rateValue;
		private final String valueTooltip;
		private final String rateValueTooltip;

		private GpValues(Record record, SlayerTrackerConfig.LootUnit lootUnit)
		{
			if (lootUnit.equals(SlayerTrackerConfig.LootUnit.HIGH_ALCHEMY))
			{
				label = "ha: ";
				value = QuantityFormatter.quantityToRSDecimalStack(record.getHa(), true);
				valueTooltip = QuantityFormatter.formatNumber(record.getHa()) + " gp";

				rateLabel = "ha/h: ";
				rateValue = QuantityFormatter.quantityToRSDecimalStack(Math.round(record.getHa() / record.getHours()), true);
				rateValueTooltip = QuantityFormatter.formatNumber(Math.round(record.getHa() / record.getHours())) + " gp/h";
			}
			else
			{
				label = "ge: ";
				value = QuantityFormatter.quantityToRSDecimalStack(record.getGe(), true);
				valueTooltip = QuantityFormatter.formatNumber(record.getGe()) + " gp";

				rateLabel = "ge/h: ";
				rateValue = QuantityFormatter.quantityToRSDecimalStack(Math.round(record.getGe() / record.getHours()), true);
				rateValueTooltip = QuantityFormatter.formatNumber(Math.round(record.getGe() / record.getHours())) + " gp/h";
			}
		}
	}
}
