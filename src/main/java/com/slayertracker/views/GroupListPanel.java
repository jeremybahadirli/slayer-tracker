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
import com.slayertracker.records.CustomRecord;
import com.slayertracker.records.RecordMap;
import com.slayertracker.views.recordpanels.AssignmentRecordPanel;
import com.slayertracker.views.recordpanels.CustomRecordPanel;
import com.slayertracker.views.recordpanels.RecordPanel;
import com.slayertracker.views.recordpanels.VariantRecordPanel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.beans.PropertyChangeListener;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import net.runelite.client.game.ItemManager;

@Getter
public class GroupListPanel extends JPanel implements RecordListPanel
{
	private final SlayerTrackerConfig config;
	private final ItemManager itemManager;

	private final Assignment assignment;
	private final AssignmentRecord record;

	private final AssignmentRecordPanel assignmentRecordPanel;
	private final Set<VariantRecordPanel> variantRecordPanels = new HashSet<>();
	private final Set<CustomRecordPanel> customRecordPanels = new HashSet<>();
	private final PropertyChangeListener changeListener;

	GroupListPanel(Assignment assignment,
				   PropertyChangeListener changeListener,
				   RecordMap<Assignment, AssignmentRecord> assignmentRecords,
				   SlayerTrackerConfig config,
				   ItemManager itemManager,
				   Function<? super RecordListPanel, Long> sortFunction)
	{
		this.assignment = assignment;
		this.config = config;
		this.itemManager = itemManager;
		this.record = assignmentRecords.get(assignment);
		this.changeListener = changeListener;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// Assignment Record Panel
		assignmentRecordPanel = new AssignmentRecordPanel(assignment, assignmentRecords, this);
		assignmentRecordPanel.getHeaderPanel().addMouseListener(new MouseAdapter()
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
		assignmentRecordPanel.getAddCustomRecordButton().addActionListener(l ->
			record.getCustomRecords().add(new CustomRecord(changeListener)));

		update(sortFunction);
	}

	void update(Function<? super RecordListPanel, Long> sortFunction)
	{
		// Remove/Update/Add Panels

		// Remove panels
		variantRecordPanels.removeIf(recordPanel ->
			!record.getVariantRecords().containsValue(recordPanel.getRecord()));

		customRecordPanels.removeIf(recordPanel ->
			!record.getCustomRecords().contains(recordPanel.getRecord()));

		// Update panels
		assignmentRecordPanel.update();
		variantRecordPanels.forEach(RecordPanel::update);
		customRecordPanels.forEach(CustomRecordPanel::update);

		// Add panels
		record.getVariantRecords().keySet().forEach(variant -> {
			// For each Variant Record in model, add panel if NO Variant Panel's Record matches it
			if (variantRecordPanels.stream().noneMatch(variantRecordPanel ->
				variantRecordPanel.getVariant().equals(variant)))
			{
				VariantRecordPanel variantRecordPanel = new VariantRecordPanel(variant, record.getVariantRecords(), this);
				variantRecordPanel.getHeaderPanel().addMouseListener(new MouseAdapter()
				{
					@Override
					public void mouseClicked(MouseEvent e)
					{
						if (e.getButton() == MouseEvent.BUTTON1)
						{
							variantRecordPanel.toggleCollapsed();
						}
					}
				});
				variantRecordPanels.add(variantRecordPanel);
			}
		});

		record.getCustomRecords().forEach(customRecord -> {
			if (customRecordPanels.stream().noneMatch(customRecordPanel ->
				customRecordPanel.getRecord().equals(customRecord)))
			{
				CustomRecordPanel customRecordPanel = new CustomRecordPanel(customRecord.getName(), customRecord, record.getCustomRecords(), this);
				customRecordPanel.getHeaderPanel().addMouseListener(new MouseAdapter()
				{
					@Override
					public void mouseClicked(MouseEvent e)
					{
						if (e.getButton() == MouseEvent.BUTTON1)
						{
							customRecordPanel.toggleCollapsed();
						}
					}
				});
				customRecordPanels.add(customRecordPanel);
			}
		});

		// Rebuild

		removeAll();

		add(assignmentRecordPanel);

		variantRecordPanels.stream()
			.sorted(Comparator.comparing(sortFunction))
			.forEachOrdered(variantRecordPanel -> {
				variantRecordPanel.setBorder(new EmptyBorder(0, 36, 0, 0));
				add(variantRecordPanel);
			});

		customRecordPanels.stream()
			.sorted(Comparator.comparing(sortFunction))
			.forEachOrdered(customRecordPanel -> {
				customRecordPanel.setBorder(new EmptyBorder(0, 36, 0, 0));
				add(customRecordPanel);
			});

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
		variantRecordPanels.forEach(variantRecordPanel ->
			variantRecordPanel.setVisible(true));
		customRecordPanels.forEach(customRecordPanel ->
			customRecordPanel.setVisible(true));
	}

	private void collapseAll()
	{
		// Collapse Assignment Record panel and all of its Variant Record panels
		assignmentRecordPanel.collapse();
		variantRecordPanels.forEach(variantRecordPanel ->
			variantRecordPanel.setVisible(false));
		customRecordPanels.forEach(customRecordPanel ->
			customRecordPanel.setVisible(false));
	}
}
