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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import lombok.Getter;
import net.runelite.client.game.ItemManager;

@Getter
public class GroupListPanel extends JPanel
{
	private final SlayerTrackerConfig slayerTrackerConfig;

	private final AssignmentRecord assignmentRecord;
	private final RecordMap<Assignment, AssignmentRecord> recordMap;

	private final AssignmentRecordPanel assignmentRecordPanel;
	private final HashMap<RecordPanel, JPanel> variantRecordPanelToHorizontalPanel = new HashMap<>();

	GroupListPanel(Assignment assignment,
				   AssignmentRecord assignmentRecord,
				   RecordMap<Assignment, AssignmentRecord> assignmentRecords,
				   SlayerTrackerConfig slayerTrackerConfig,
				   ItemManager itemManager)
	{
		this.slayerTrackerConfig = slayerTrackerConfig;
		this.assignmentRecord = assignmentRecord;
		this.recordMap = assignmentRecords;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// Assignment Record Panel
		assignmentRecordPanel = new AssignmentRecordPanel(assignment, assignmentRecord, assignmentRecords, slayerTrackerConfig, itemManager);
		assignmentRecordPanel.headerPanel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (e.getButton() == MouseEvent.BUTTON1)
				{
					toggleCollapsedAll();
				}
			}
		});
		add(assignmentRecordPanel);

		update();
	}

	void update()
	{
		// Remove panels
		variantRecordPanelToHorizontalPanel.keySet().removeIf(recordPanel ->
			!assignmentRecord.getVariantRecords().containsValue(recordPanel.getRecord()));

		// Update panels
		assignmentRecordPanel.update();
		variantRecordPanelToHorizontalPanel.keySet().forEach(RecordPanel::update);

		// Add panels
		assignmentRecord.getVariantRecords().forEach((variant, variantRecord) -> {
			// For each Variant Record in model, add panel if NO Variant Panel's Record matches it
			if (variantRecordPanelToHorizontalPanel.keySet().stream().noneMatch(variantRecordPanel -> variantRecordPanel.getRecord().equals(variantRecord)))
			{
				JPanel horizontalPanel = new JPanel();
				horizontalPanel.setLayout(new BoxLayout(horizontalPanel, BoxLayout.X_AXIS));
				horizontalPanel.add(Box.createRigidArea(new Dimension(36, 0)));
				VariantRecordPanel variantRecordPanel = new VariantRecordPanel(variant, variantRecord, assignmentRecord.getVariantRecords(), slayerTrackerConfig);
				horizontalPanel.add(variantRecordPanel);
				variantRecordPanelToHorizontalPanel.put(variantRecordPanel, horizontalPanel);
			}
		});

		build();
	}

	void build()
	{
		removeAll();
		add(assignmentRecordPanel);
		variantRecordPanelToHorizontalPanel.values().forEach(this::add);

		revalidate();
		repaint();
	}

	void toggleCollapsedAll()
	{
		if (assignmentRecordPanel.isCollapsed())
		{
			expandAll();
		}
		else
		{
			collapseAll();
		}
	}

	private void expandAll()
	{
		// Expand Assignment Record panel and all of its Variant Record panels
		assignmentRecordPanel.expand();
		variantRecordPanelToHorizontalPanel.values().forEach(variantRecordPanel ->
			variantRecordPanel.setVisible(true));
	}

	private void collapseAll()
	{
		// Collapse Assignment Record panel and all of its Variant Record panels
		assignmentRecordPanel.collapse();
		variantRecordPanelToHorizontalPanel.values().forEach(variantRecordPanel ->
			variantRecordPanel.setVisible(false));
	}
}