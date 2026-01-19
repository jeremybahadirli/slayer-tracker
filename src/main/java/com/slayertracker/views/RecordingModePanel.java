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

import com.slayertracker.RecordingModeController;
import com.slayertracker.views.components.DarkComboBoxRenderer;
import java.awt.Dimension;
import java.util.Arrays;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import lombok.Setter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

@Getter
public class RecordingModePanel extends JPanel implements RecordingModeController
{

	private boolean isRecording = false;
	private RecordingMode recordingMode = RecordingMode.IN_COMBAT;
	private final JComboBox<String> recordingModeComboBox;
	private final JButton recordingBreakButton;
	private final JPanel recordButtonBorderPanel;
	private final ImageIcon recordActiveIcon;
	@Setter
	private Runnable pauseRequestHandler;

	RecordingModePanel()
	{
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(new JLabel("Recording:"));
		add(Box.createRigidArea(new Dimension(24, 0)));
		// Recording mode combo box
		recordingModeComboBox = new JComboBox<>();
		recordingModeComboBox.setRenderer(new DarkComboBoxRenderer());
		Arrays.stream(RecordingMode.values()).map(RecordingMode::getLabel).forEach(recordingModeComboBox::addItem);
		recordingModeComboBox.addActionListener(l ->
			setRecordingMode(RecordingMode.getRecordingModeByLabel(String.valueOf(recordingModeComboBox.getSelectedItem()))));
		recordingModeComboBox.setToolTipText(recordingMode.tooltip);
		recordingModeComboBox.setFocusable(false);
		add(recordingModeComboBox);
		// Record button
		recordActiveIcon = new ImageIcon(ImageUtil.loadImageResource(getClass(), "/record_active_icon.png"));
		recordingBreakButton = new JButton(recordActiveIcon);
		recordingBreakButton.setEnabled(false);
		recordingBreakButton.addActionListener(l -> {
			if (pauseRequestHandler != null)
			{
				pauseRequestHandler.run();
			}
		});
		recordingBreakButton.setPreferredSize(new Dimension(16, 16));
		recordingBreakButton.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		recordingBreakButton.setToolTipText("Pause Recording");
		recordButtonBorderPanel = new JPanel();
		recordButtonBorderPanel.setLayout(new BoxLayout(recordButtonBorderPanel, BoxLayout.X_AXIS));
		recordButtonBorderPanel.setBorder(new EmptyBorder(3, 4, 3, 4));
		recordButtonBorderPanel.add(recordingBreakButton);
		add(recordButtonBorderPanel);
	}

	public void setRecording(boolean b)
	{
		recordingBreakButton.setEnabled(b);
		isRecording = b;
	}

	private void setRecordingMode(RecordingMode recordingMode)
	{
		this.recordingMode = recordingMode;
		recordingModeComboBox.setToolTipText(recordingMode.tooltip);
	}

	@Getter
	public enum RecordingMode
	{
		IN_COMBAT("In Combat",
			"Only log combat time. Recommended for most tasks, to normalize for varying focus level."),
		CONTINUOUS("Continuous",
			"Log time between kills, until paused. Recommended for tasks where banking is significant, such as bosses.");

		final String label;
		final String tooltip;

		RecordingMode(String label, String tooltip)
		{
			this.label = label;
			this.tooltip = tooltip;
		}

		static RecordingMode getRecordingModeByLabel(String label)
		{
			return Arrays.stream(RecordingMode.values()).filter(rm -> rm.getLabel().equals(label)).findFirst().orElse(null);
		}
	}
}
