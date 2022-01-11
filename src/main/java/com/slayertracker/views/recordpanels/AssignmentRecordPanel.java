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
package com.slayertracker.views.recordpanels;

import com.slayertracker.SlayerTrackerConfig;
import com.slayertracker.groups.Assignment;
import com.slayertracker.records.AssignmentRecord;
import com.slayertracker.records.RecordMap;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

@Getter
public class AssignmentRecordPanel extends RecordPanel
{
	JButton addCustomRecordButton;

	public AssignmentRecordPanel(Assignment assignment,
								 RecordMap<Assignment, AssignmentRecord> assignmentRecords,
								 SlayerTrackerConfig config,
								 ItemManager itemManager)
	{
		super(assignment, assignmentRecords, config);

		BufferedImage addIcon = ImageUtil.loadImageResource(getClass(), "/add_icon.png");

		addCustomRecordButton = new JButton(new ImageIcon(addIcon));
		addCustomRecordButton.setBorder(new EmptyBorder(0, 0, 0, 0));
		addCustomRecordButton.setPreferredSize(new Dimension(16, 16));
		addCustomRecordButton.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		addCustomRecordButton.setFocusPainted(false);
		Component hg = Box.createHorizontalGlue();
		hg.setBackground(Color.red);
		headerPanel.add(hg);
		headerPanel.add(addCustomRecordButton);

		bodyPanel.add(new JLabel(new ImageIcon(itemManager.getImage(assignment.getItemSpriteId()))));
		bodyPanel.add(statsPanel);
	}
}