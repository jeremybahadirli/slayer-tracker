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
package com.slayertracker.views;

import com.google.common.collect.ImmutableList;
import com.slayertracker.SlayerTrackerConfig;
import com.slayertracker.groups.Assignment;
import com.slayertracker.records.AssignmentRecord;
import com.slayertracker.records.RecordMap;
import com.slayertracker.state.TrackerState;
import java.awt.Dimension;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import lombok.Getter;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.PluginErrorPanel;

@Getter
public class SlayerTrackerPanel extends PluginPanel
{
	private final int VERTICAL_GAP = 6;

	private final TrackerState trackerState;
	private final RecordMap<Assignment, AssignmentRecord> assignmentRecords;
	private final ItemManager itemManager;
	private final SlayerTrackerConfig config;

	private final PluginErrorPanel welcomeText;
	private final JButton resetAllButton;
	private final JButton resetCustomButton;
	private final RecordingModePanel recordingModePanel;
	private final RecordingModePresenter recordingModePresenter;
	private final JPanel assignmentListPanel;
	private final JComboBox<String> sorterComboBox;
	private final Set<GroupListPresenter> groupListPresenters = new HashSet<>();

	private static final ImmutableList<String> SORT_ORDERS = ImmutableList.of(
		"Recently Killed",
		"XP Rate",
		"GP Rate"
	);
	private Function<? super RecordListPanel, Long> sortFunction;

	public SlayerTrackerPanel(TrackerState trackerState,
							  SlayerTrackerConfig config,
							  ItemManager itemManager)
	{
		this.trackerState = trackerState;
		this.assignmentRecords = trackerState.getAssignmentRecords();
		this.itemManager = itemManager;
		this.config = config;

		setLayout(new DynamicGridLayout(0, 1, 0, VERTICAL_GAP));

		// Sorter
		JPanel sorterPanel = new JPanel();
		sorterPanel.setLayout(new BoxLayout(sorterPanel, BoxLayout.X_AXIS));
		sorterPanel.add(new JLabel("Sort by:"));
		sorterPanel.add(Box.createRigidArea(new Dimension(39, 0)));
		sorterComboBox = new JComboBox<>();
		sorterComboBox.setBackground(ColorScheme.DARK_GRAY_COLOR);
		SORT_ORDERS.forEach(sorterComboBox::addItem);
		sortFunction = sortFunctionFor(String.valueOf(sorterComboBox.getSelectedItem()));
		sorterComboBox.addActionListener(l -> {
			sortFunction = sortFunctionFor(String.valueOf(sorterComboBox.getSelectedItem()));
			update();
		});
		sorterComboBox.setFocusable(false);
		sorterPanel.add(sorterComboBox);
		add(sorterPanel);

		// Recording Mode
		recordingModePanel = new RecordingModePanel(RecordingModePanel.RecordingMode.IN_COMBAT);
		recordingModePresenter = new RecordingModePresenter(recordingModePanel);
		add(recordingModePanel);

		// Assignment list panel
		assignmentListPanel = new JPanel();
		assignmentListPanel.setLayout(new DynamicGridLayout(0, 1, 0, VERTICAL_GAP));
		add(assignmentListPanel);

		// Welcome text
		welcomeText = new PluginErrorPanel();
		welcomeText.setContent(
			"Slayer Tracker",
			"Compare XP and GP rates for each Slayer task.<br><br>Slayer Tracker requires the default Slayer plugin to be enabled.");

		// Reset All button
		resetAllButton = new JButton("Delete All");
		resetAllButton.addActionListener(event -> {
			final int result = JOptionPane.showOptionDialog(this,
				"<html>This will delete: <b>ALL RECORDS</b></html>",
				"Are you sure?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
				null, new String[]{"Yes", "No"}, "No");

			if (result == JOptionPane.YES_OPTION)
			{
				assignmentRecords.clear();
			}
		});
		add(resetAllButton);

		// Reset Custom button
		resetCustomButton = new JButton("Delete Custom Records");
		resetCustomButton.addActionListener(event -> {
			final int result = JOptionPane.showOptionDialog(this,
				"<html>This will delete: <b>ALL CUSTOM RECORDS</b></html>",
				"Are you sure?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
				null, new String[]{"Yes", "No"}, "No");

			if (result == JOptionPane.YES_OPTION)
			{
				assignmentRecords.values().forEach(assignmentRecord ->
					assignmentRecord.getCustomRecords().clear());
			}
		});
		add(resetCustomButton);

		update();
	}

	public void update()
	{
		// Remove/Update/Add Panels

		// Remove panels
		groupListPresenters.removeIf(presenter ->
			!assignmentRecords.containsValue(presenter.getRecord()));

		// Update panels
		groupListPresenters.forEach(presenter -> presenter.update(sortFunction));

		// Add panels
		assignmentRecords.keySet().forEach(assignment -> {
			if (groupListPresenters.stream().noneMatch(groupListPresenter -> groupListPresenter.getAssignment().equals(assignment)))
			{
				GroupListPresenter groupListPresenter = new GroupListPresenter(
					assignment,
					trackerState,
					assignmentRecords,
					config,
					itemManager,
					() -> sortFunction);
				groupListPresenters.add(groupListPresenter);
			}
		});

		// Rebuild

		assignmentListPanel.removeAll();

		if (groupListPresenters.isEmpty())
		{
			// Welcome text
			assignmentListPanel.add(welcomeText);
		}
		else
		{
			// Group List Panels
			groupListPresenters.stream()
				.sorted(Comparator.comparing(presenter -> sortFunction.apply(presenter.getView())))
				.forEachOrdered(presenter -> assignmentListPanel.add(presenter.getView()));
		}

		// Reset All button
		resetAllButton.setVisible(!groupListPresenters.isEmpty());

		// Reset Custom button
		resetCustomButton.setVisible(
			!groupListPresenters.isEmpty()
				&& groupListPresenters.stream()
				.anyMatch(presenter -> !presenter.getRecord().getCustomRecords().isEmpty()));
	}

	public void displayFileError()
	{
		assignmentListPanel.removeAll();
		assignmentListPanel.add(welcomeText);

		PluginErrorPanel fileErrorPanel = new PluginErrorPanel();
		fileErrorPanel.setContent("Error: Could not create save file", "Slayer Tracker needs access to .runelite/slayer-tracker");
		assignmentListPanel.add(fileErrorPanel);
	}

	private Function<? super RecordListPanel, Long> sortFunctionFor(String selectedSort)
	{
		switch (selectedSort)
		{
			case "XP Rate":
				return panel ->
					(long) Math.round(-1 * panel.getRecord().getXp() / panel.getRecord().getHours());
			case "GP Rate":
				if (config.lootUnit().equals(SlayerTrackerConfig.LootUnit.GRAND_EXCHANGE))
				{
					return panel ->
						(long) Math.round(-1 * panel.getRecord().getGe() / panel.getRecord().getHours());
				}
				return panel ->
					(long) Math.round(-1 * panel.getRecord().getHa() / panel.getRecord().getHours());
			default:
				return panel ->
					-1 * panel.getRecord().getCombatInstant().getEpochSecond();
		}
	}
}
