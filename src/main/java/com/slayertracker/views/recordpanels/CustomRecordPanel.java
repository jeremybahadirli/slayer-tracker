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

import com.slayertracker.records.CustomRecord;
import com.slayertracker.records.CustomRecordSet;
import com.slayertracker.views.GroupListPanel;
import com.slayertracker.views.RecordListPanel;
import com.slayertracker.views.recordpanels.components.StatsPanel;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

@Getter
public class CustomRecordPanel extends RecordPanel implements RecordListPanel
{
	private final GroupListPanel groupListPanel;

	private final CustomRecord record;

	private final JTextField titleField;

	public CustomRecordPanel(CustomRecord record,
							 CustomRecordSet<CustomRecord> customRecordSet,
							 GroupListPanel groupListPanel)
	{
		super(record, groupListPanel);
		this.record = record;
		this.groupListPanel = groupListPanel;

		// Header Panel

		// Title text field
		titleField = new JTextField("New Custom Record");
		titleField.setBorder(new EmptyBorder(0, 0, 0, 0));
		titleField.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());
		titleField.setMinimumSize(new Dimension(1, titleField.getPreferredSize().height));
		titleField.setPreferredSize(new Dimension(titleField.getPreferredSize().width, titleField.getPreferredSize().height - 1));
		titleField.addActionListener(l -> KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent());
		titleField.addFocusListener(new FocusListener()
		{
			@Override
			public void focusGained(FocusEvent e)
			{}

			@Override
			public void focusLost(FocusEvent e)
			{
				record.setName(titleField.getText());
			}
		});
		headerPanel.add(titleField);
		// Record button
		ImageIcon recordIcon = new ImageIcon(ImageUtil.loadImageResource(getClass(), "/record_icon.png"));
		ImageIcon recordActiveIcon = new ImageIcon(ImageUtil.loadImageResource(getClass(), "/record_active_icon.png"));
		JToggleButton recordButton = new JToggleButton(recordIcon);
		recordButton.addActionListener(l -> {
			if (recordButton.isSelected())
			{
				record.setRecording(true);
				recordButton.setIcon(recordActiveIcon);
			}
			else
			{
				record.setRecording(false);
				record.getInteractors().clear();
				recordButton.setIcon(recordIcon);
			}
		});
		recordButton.setBorder(new EmptyBorder(0, 0, 0, 0));
		recordButton.setPreferredSize(new Dimension(16, 16));
		recordButton.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		recordButton.setFocusPainted(false);
		recordButton.setToolTipText("Start/Stop logging to this Custom Record.");
		headerPanel.add(Box.createHorizontalGlue());
		headerPanel.add(recordButton);

		// Right-click menu

		// Delete button
		resetMenuItem.addActionListener(e ->
		{
			final int selection = JOptionPane.showOptionDialog(this,
				"<html>This will delete the record: <b>" + titleField.getText().toUpperCase() + "</b></html>",
				"Are you sure?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
				null, new String[]{"Yes", "No"}, "No");
			if (selection == JOptionPane.YES_OPTION)
			{
				customRecordSet.remove(record);
			}
		});
		popupMenu.add(resetMenuItem);

		// Stats Panel

		statsPanel = new StatsPanel(record, groupListPanel.getConfig().lootUnit());
		bodyPanel.add(statsPanel);

		add(headerPanel);
		add(bodyPanel);
	}

	public CustomRecordPanel(String title,
							 CustomRecord record,
							 CustomRecordSet<CustomRecord> customRecordSet,
							 GroupListPanel groupListPanel)
	{
		this(record, customRecordSet, groupListPanel);
		titleField.setText(title);
	}
}