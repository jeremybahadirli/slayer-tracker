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
import com.slayertracker.groups.Assignment;
import com.slayertracker.records.AssignmentRecord;
import com.slayertracker.records.RecordMap;
import java.awt.Dimension;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import net.runelite.client.game.ItemManager;

public class AssignmentListPanel extends JPanel
{
	private final SlayerTrackerConfig slayerTrackerConfig;
	private final ItemManager itemManager;

	private final RecordMap<Assignment, AssignmentRecord> assignmentRecords;

	private final Set<GroupListPanel> groupListPanels = new HashSet<>();

	AssignmentListPanel(RecordMap<Assignment, AssignmentRecord> assignmentRecords,
						SlayerTrackerConfig slayerTrackerConfig,
						ItemManager itemManager,
						String sortOrder)
	{
		this.assignmentRecords = assignmentRecords;
		this.slayerTrackerConfig = slayerTrackerConfig;
		this.itemManager = itemManager;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		update(sortOrder);
	}

	void update(String sortOrder)
	{
		// Remove/Update/Add Panels

		// Remove panels
		groupListPanels.removeIf(groupListPanel ->
			!assignmentRecords.containsValue(groupListPanel.getAssignmentRecord()));

		// Update panels
		groupListPanels.forEach(groupListPanel -> groupListPanel.update(sortOrder));

		// Add panels
		assignmentRecords.forEach((assignment, assignmentRecord) -> {
			if (groupListPanels.stream().noneMatch(groupListPanel -> groupListPanel.getAssignmentRecord().equals(assignmentRecord)))
			{
				GroupListPanel groupListPanel = new GroupListPanel(assignment, assignmentRecord, assignmentRecords, slayerTrackerConfig, itemManager, sortOrder);
				groupListPanels.add(groupListPanel);
			}
		});

		// Rebuild

		removeAll();

		Function<GroupListPanel, Long> sortFunction;
		switch (sortOrder)
		{
			case "XP Rate":
				sortFunction = groupListPanel ->
					(long) Math.round(-1 * groupListPanel.getAssignmentRecord().getXp() / groupListPanel.getAssignmentRecord().getHours());
				break;
			case "GP Rate":
				if (slayerTrackerConfig.lootUnit().equals(SlayerTrackerConfig.SlayerTrackerLootUnit.GRAND_EXCHANGE))
				{
					sortFunction = groupListPanel ->
						(long) Math.round(-1 * groupListPanel.getAssignmentRecord().getGe() / groupListPanel.getAssignmentRecord().getHours());
				}
				else
				{
					sortFunction = groupListPanel ->
						(long) Math.round(-1 * groupListPanel.getAssignmentRecord().getHa() / groupListPanel.getAssignmentRecord().getHours());
				}
				break;
			default:
				sortFunction = groupListPanel ->
					-1 * groupListPanel.getAssignmentRecord().getCombatInstant().getEpochSecond();
				break;
		}

		groupListPanels.stream()
			.sorted(Comparator.comparing(sortFunction))
			.forEachOrdered((GroupListPanel groupListPanel) -> {
				add(groupListPanel);
				add(Box.createRigidArea(new Dimension(0, 5)));
			});

		revalidate();
		repaint();
	}
}