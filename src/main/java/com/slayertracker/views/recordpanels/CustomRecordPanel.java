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
import com.slayertracker.records.CustomRecord;
import com.slayertracker.records.CustomRecordSet;
import com.slayertracker.views.RecordListPanel;
import com.slayertracker.views.components.StatsPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Arrays;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

@Getter
public class CustomRecordPanel extends JPanel implements RecordListPanel
{
	private final CustomRecord record;
	private final SlayerTrackerConfig config;
	private final JPanel headerPanel;
	private final JPanel bodyPanel;
	private final JTextField titleField;
	private StatsPanel statsPanel;

	public CustomRecordPanel(CustomRecord record, CustomRecordSet<CustomRecord> customRecordSet, SlayerTrackerConfig config)
	{
		this.record = record;
		this.config = config;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// Header Panel

		headerPanel = new JPanel();
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));
		headerPanel.setBorder(new EmptyBorder(4, 4, 4, 4));
		headerPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());
		// Title text field
		titleField = new JTextField("New Custom Record");
		titleField.setBorder(new EmptyBorder(0, 0, 0, 0));
		titleField.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());
		titleField.setMinimumSize(new Dimension(1, titleField.getPreferredSize().height));
		titleField.setPreferredSize(new Dimension(titleField.getPreferredSize().width, titleField.getPreferredSize().height - 1));
		titleField.addActionListener(l ->
			KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent());
		titleField.addFocusListener(new FocusListener()
		{
			@Override
			public void focusGained(FocusEvent e)
			{
			}

			@Override
			public void focusLost(FocusEvent e)
			{
				record.setName(titleField.getText());
			}
		});
		headerPanel.add(titleField);
		// Record button
		ImageIcon recordIcon = new ImageIcon(ImageUtil.loadImageResource(getClass(), "/record_button.png"));
		ImageIcon recordActiveIcon = new ImageIcon(ImageUtil.loadImageResource(getClass(), "/record_button_active.png"));
		JToggleButton addCustomRecordButton = new JToggleButton(recordIcon);
		addCustomRecordButton.addActionListener(l -> {
			if (addCustomRecordButton.isSelected())
			{
				record.setRecording(true);
				addCustomRecordButton.setIcon(recordActiveIcon);
			}
			else
			{
				record.setRecording(false);
				record.getInteractors().clear();
				addCustomRecordButton.setIcon(recordIcon);
			}
		});
		addCustomRecordButton.setBorder(new EmptyBorder(0, 0, 0, 0));
		addCustomRecordButton.setPreferredSize(new Dimension(16, 16));
		addCustomRecordButton.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		addCustomRecordButton.setFocusPainted(false);
		headerPanel.add(Box.createHorizontalGlue());
		headerPanel.add(addCustomRecordButton);
		// Right-click menu
		final JMenuItem resetMenuItem = new JMenuItem("Delete");
		resetMenuItem.addActionListener(e ->
		{
			final int selection = JOptionPane.showOptionDialog(this,
				"This will delete the record: " + titleField.getText(),
				"Are you sure?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
				null, new String[]{"Yes", "No"}, "No");
			if (selection == JOptionPane.YES_OPTION)
			{
				customRecordSet.remove(record);
			}
		});
		JPopupMenu popupMenu = getComponentPopupMenu();
		if (popupMenu == null)
		{
			popupMenu = new JPopupMenu();
			popupMenu.setBorder(new EmptyBorder(5, 5, 5, 5));
			headerPanel.setComponentPopupMenu(popupMenu);
		}
		popupMenu.add(resetMenuItem);
		add(headerPanel);

		// Body Panel

		bodyPanel = new JPanel();
		bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.X_AXIS));
		bodyPanel.setBackground((ColorScheme.DARKER_GRAY_COLOR));
		bodyPanel.setBorder(new EmptyBorder(4, 4, 4, 4));
		add(bodyPanel);
		// Stats Panel
		statsPanel = new StatsPanel(record, config.lootUnit());
		bodyPanel.add(statsPanel);
	}

	public CustomRecordPanel(String title, CustomRecord record, CustomRecordSet<CustomRecord> customRecordSet, SlayerTrackerConfig config)
	{
		this(record, customRecordSet, config);
		titleField.setText(title);
	}

	public void update()
	{
		bodyPanel.remove(statsPanel);
		statsPanel = new StatsPanel(record, config.lootUnit());
		bodyPanel.add(statsPanel);
	}

	void expand()
	{
		if (isCollapsed())
		{
			bodyPanel.setVisible(true);
			toggleDimmer(true);
		}
	}

	void collapse()
	{
		if (!isCollapsed())
		{
			bodyPanel.setVisible(false);
			toggleDimmer(false);
		}
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

	boolean isCollapsed()
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
