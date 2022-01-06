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

import com.google.common.collect.ImmutableList;
import com.slayertracker.SlayerTrackerConfig;
import com.slayertracker.groups.Assignment;
import com.slayertracker.records.AssignmentRecord;
import com.slayertracker.records.RecordMap;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import lombok.Getter;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.PluginPanel;

@Getter
public class SlayerTrackerPanel extends PluginPanel
{
	private final RecordMap<Assignment, AssignmentRecord> assignmentRecords;

	private final JButton resetAllButton;
	private final AssignmentListPanel assignmentListPanel;
	private final JComboBox<String> sorterComboBox;

	private static final ImmutableList<String> SORT_ORDERS = ImmutableList.of(
		"Recently Killed",
		"XP Rate",
		"GP Rate"
	);

	public SlayerTrackerPanel(RecordMap<Assignment, AssignmentRecord> assignmentRecords,
							  SlayerTrackerConfig slayerTrackerConfig,
							  ItemManager itemManager)
	{
		this.assignmentRecords = assignmentRecords;

		// Sorter
		JPanel sorterPanel = new JPanel();
		sorterPanel.setLayout(new BoxLayout(sorterPanel, BoxLayout.X_AXIS));
		sorterPanel.add(new JLabel("Sort by:"));
		sorterPanel.add(Box.createRigidArea(new Dimension(48, 0)));
		sorterComboBox = new JComboBox<>();
		SORT_ORDERS.forEach(sorterComboBox::addItem);
		sorterComboBox.addActionListener(l ->
			update());
		sorterPanel.add(sorterComboBox);
		add(sorterPanel);

		// Assignment list
		assignmentListPanel = new AssignmentListPanel(assignmentRecords, slayerTrackerConfig, itemManager, sorterComboBox.getSelectedItem().toString());
		add(assignmentListPanel);

		// Reset All button
		resetAllButton = new JButton("Reset All");
		resetAllButton.addActionListener(event -> {
			final int result = JOptionPane.showOptionDialog(this,
				"This will permanently delete all records.",
				"Are you sure?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
				null, new String[]{"Yes", "No"}, "No");

			if (result == JOptionPane.YES_OPTION)
			{
				assignmentRecords.clear();
			}
		});
		add(resetAllButton);

		update();
	}

	public void update()
	{
		assignmentListPanel.update(sorterComboBox.getSelectedItem().toString());
		resetAllButton.setVisible(!assignmentRecords.isEmpty());
	}
}