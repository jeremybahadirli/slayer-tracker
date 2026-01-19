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
package com.slayertracker.views.recordpanels;

import com.slayertracker.groups.Assignment;
import com.slayertracker.views.GroupListPanel;
import com.slayertracker.views.RecordInteractionHandler;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Locale;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.LinkBrowser;
import okhttp3.HttpUrl;

@Getter
public class AssignmentRecordPanel extends RecordPanel
{
	JButton addCustomRecordButton;

	public AssignmentRecordPanel(Assignment assignment,
								 GroupListPanel groupListPanel,
								 RecordInteractionHandler recordInteractionHandler)
	{
		super(groupListPanel.getRecord(), groupListPanel, recordInteractionHandler);

		// Header panel

		// Title
		JLabel titleLabel = new JLabel(assignment.getName());
		titleLabel.setMinimumSize(new Dimension(1, titleLabel.getPreferredSize().height));
		headerPanel.add(titleLabel);
		// Add button
		BufferedImage addIcon = ImageUtil.loadImageResource(getClass(), "/add_icon.png");
		addCustomRecordButton = new JButton(new ImageIcon(addIcon));
		addCustomRecordButton.setBorder(new EmptyBorder(0, 0, 0, 0));
		addCustomRecordButton.setPreferredSize(new Dimension(16, 16));
		addCustomRecordButton.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		addCustomRecordButton.setFocusPainted(false);
		addCustomRecordButton.setToolTipText("New Custom Record");
		Component hg = Box.createHorizontalGlue();
		headerPanel.add(hg);
		headerPanel.add(addCustomRecordButton);

		// Right-click menu

		// Delete button
		resetMenuItem.addActionListener(e ->
		{
			final int selection = JOptionPane.showOptionDialog(this,
				"<html>This will delete the record: <b>" + assignment.getName().toUpperCase(Locale.ROOT) + "</b></html>",
				"Are you sure?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
				null, new String[]{"Yes", "No"}, "No");
			if (selection == JOptionPane.YES_OPTION)
			{
				recordInteractionHandler.deleteAssignment(assignment);
			}
		});

		// Stats panel

		JLabel iconLabel = new JLabel(new ImageIcon(groupListPanel.getItemManager().getImage(assignment.getItemSpriteId())));
		JPopupMenu iconPopupMenu = getComponentPopupMenu();
		if (iconPopupMenu == null)
		{
			iconPopupMenu = new JPopupMenu();
			iconPopupMenu.setBorder(new EmptyBorder(5, 5, 5, 5));
			iconLabel.setComponentPopupMenu(iconPopupMenu);
		}
		JMenuItem wikiMenuItem = new JMenuItem("Wiki");
		wikiMenuItem.addActionListener(e -> {
			final HttpUrl WIKI_BASE = HttpUrl.get("https://oldschool.runescape.wiki");
			LinkBrowser.browse(WIKI_BASE.newBuilder()
				.addQueryParameter("search", assignment.getName())
				.build()
				.toString());
		});
		iconPopupMenu.add(wikiMenuItem);
		iconLabel.add(iconPopupMenu);
		bodyPanel.add(iconLabel);
		bodyPanel.add(statsPanel);

		add(headerPanel);
		add(bodyPanel);
	}

	public void expand()
	{
		super.expand();
		Arrays.stream(headerPanel.getComponents())
			.filter(component -> component instanceof JButton)
			.forEach(button -> button.setEnabled(true));
	}

	public void collapse()
	{
		super.collapse();
		Arrays.stream(headerPanel.getComponents())
			.filter(component -> component instanceof JButton)
			.forEach(button -> button.setEnabled(false));
	}
}
