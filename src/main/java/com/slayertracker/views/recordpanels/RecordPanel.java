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
package com.slayertracker.views.recordpanels;

import com.slayertracker.records.Record;
import com.slayertracker.views.GroupListPanel;
import com.slayertracker.views.RecordInteractionHandler;
import com.slayertracker.views.RecordListPanel;
import com.slayertracker.views.recordpanels.components.StatsPanel;
import java.awt.Color;
import java.util.Arrays;
import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import net.runelite.client.ui.ColorScheme;

@Getter
public class RecordPanel extends JPanel implements RecordListPanel
{
	private final GroupListPanel groupListPanel;

	private final Record record;
	private final RecordInteractionHandler recordInteractionHandler;

	final JPanel headerPanel;
	final JMenuItem resetMenuItem;
	final JPanel bodyPanel;
	JPopupMenu popupMenu;

	StatsPanel statsPanel;

	RecordPanel(Record record, GroupListPanel groupListPanel, RecordInteractionHandler recordInteractionHandler)
	{
		this.record = record;
		this.groupListPanel = groupListPanel;
		this.recordInteractionHandler = recordInteractionHandler;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// Header Panel

		headerPanel = new JPanel();
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));
		headerPanel.setBorder(new EmptyBorder(4, 4, 4, 4));
		headerPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());

		// Right-click menu

		// Delete button
		resetMenuItem = new JMenuItem("Delete");
		// Copy to custom record button
		final JMenuItem copyToCustomRecordMenuItem = new JMenuItem("Copy to Custom Record");
		copyToCustomRecordMenuItem.addActionListener(e ->
			recordInteractionHandler.copyRecordToCustom(record));
		popupMenu = getComponentPopupMenu();
		if (popupMenu == null)
		{
			popupMenu = new JPopupMenu();
			popupMenu.setBorder(new EmptyBorder(5, 5, 5, 5));
			headerPanel.setComponentPopupMenu(popupMenu);
		}
		popupMenu.add(copyToCustomRecordMenuItem);
		popupMenu.add(resetMenuItem);

		// Body Panel
		bodyPanel = new JPanel();
		bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.X_AXIS));
		bodyPanel.setBackground((ColorScheme.DARKER_GRAY_COLOR));
		bodyPanel.setBorder(new EmptyBorder(4, 4, 4, 4));

		statsPanel = new StatsPanel(record, groupListPanel.getConfig().lootUnit());
	}

	public void update()
	{
		bodyPanel.remove(statsPanel);
		statsPanel = new StatsPanel(record, groupListPanel.getConfig().lootUnit());
		bodyPanel.add(statsPanel);
	}

	public void expand()
	{
		bodyPanel.setVisible(true);
		toggleDimmer(true);
	}

	public void collapse()
	{
		bodyPanel.setVisible(false);
		toggleDimmer(false);
	}

	public void toggleCollapsed()
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

	public boolean isCollapsed()
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
