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
import com.slayertracker.RecordingModeController;
import java.awt.Dimension;
import java.awt.Graphics;
import java.time.Instant;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

@Getter
public class RecordingModePanel extends JPanel implements RecordingModeController
{

	Instant continuousRecordingStartInstant;
	private boolean isContinuousRecordingMode = false;
	private final JComboBox<String> recordingModeComboBox;
	private final JButton recordingBreakButton;
	private final JPanel recordButtonBorderPanel;
	private final ImageIcon recordActiveIcon;
	private static final ImmutableList<String> RECORDING_MODES = ImmutableList.of(
		"In Combat",
		"Continuous"
	);
	private final String inCombatToolTipText = "Only log combat time. Recommended for most tasks, to normalize for varying focus level.";
	private final String continuousToolTipText = "Log time between kills, until paused. Recommended for tasks where banking is significant, such as bosses.";

	RecordingModePanel()
	{
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(new JLabel("Recording:"));
		add(Box.createRigidArea(new Dimension(24, 0)));
		// Recording mode combo box
		recordingModeComboBox = new JComboBox<>();
		recordingModeComboBox.setRenderer(new DefaultListCellRenderer()
		{
			@Override
			public void paint(Graphics g)
			{
				setBackground(ColorScheme.DARK_GRAY_COLOR);
				super.paint(g);
			}
		});
		RECORDING_MODES.forEach(recordingModeComboBox::addItem);
		recordingModeComboBox.addActionListener(l ->
			setContinuousRecordingMode("Continuous".equals(String.valueOf(recordingModeComboBox.getSelectedItem()))));
		recordingModeComboBox.setFocusable(false);
		recordingModeComboBox.setToolTipText(inCombatToolTipText);
		add(recordingModeComboBox);
		// Record button
		recordActiveIcon = new ImageIcon(ImageUtil.loadImageResource(getClass(), "/record_active_icon.png"));
		recordingBreakButton = new JButton(recordActiveIcon);
		recordingBreakButton.setEnabled(false);
		recordingBreakButton.addActionListener(l -> setContinuousRecording(false));
		recordingBreakButton.setPreferredSize(new Dimension(16, 16));
		recordingBreakButton.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		recordingBreakButton.setToolTipText("Pause Continuous Recording");
		recordButtonBorderPanel = new JPanel();
		recordButtonBorderPanel.setLayout(new BoxLayout(recordButtonBorderPanel, BoxLayout.X_AXIS));
		recordButtonBorderPanel.setBorder(new EmptyBorder(3, 4, 3, 4));
		recordButtonBorderPanel.setVisible(false);
		recordButtonBorderPanel.add(recordingBreakButton);
		add(recordButtonBorderPanel);
	}

	public void setContinuousRecording(boolean b)
	{
		recordingBreakButton.setEnabled(b);
		if (!b)
		{
			continuousRecordingStartInstant = Instant.now();
		}
	}

	private void setContinuousRecordingMode(boolean b)
	{
		isContinuousRecordingMode = b;
		recordingBreakButton.setEnabled(false);
		continuousRecordingStartInstant = Instant.now();
		recordButtonBorderPanel.setVisible(b);
		if (!b)
		{
			setContinuousRecording(false);
			recordingModeComboBox.setToolTipText(inCombatToolTipText);
		}
		else
		{
			recordingModeComboBox.setToolTipText(continuousToolTipText);
		}
	}
}
