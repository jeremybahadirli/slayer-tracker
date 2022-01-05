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
package com.slayertracker.views.components;

import com.slayertracker.groups.Group;
import com.slayertracker.records.Record;
import com.slayertracker.records.RecordMap;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;

public class HeaderPanel extends JPanel
{
	public HeaderPanel(Group group, RecordMap<? extends Group, ? extends Record> recordMap)
	{
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());
		setBorder(new EmptyBorder(4, 4, 4, 4));
		add(new JLabel(group.getName()));

		// Popup Menu
		final JMenuItem reset = new JMenuItem("Reset");
		reset.addActionListener(e ->
		{
			final int result = JOptionPane.showOptionDialog(this,
				"This will delete the record: " + group.getName(),
				"Are you sure?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
				null, new String[]{"Yes", "No"}, "No");

			if (result == JOptionPane.YES_OPTION)
			{
				recordMap.remove(group);
			}
		});
		JPopupMenu popupMenu = getComponentPopupMenu();
		if (popupMenu == null)
		{
			popupMenu = new JPopupMenu();
			popupMenu.setBorder(new EmptyBorder(5, 5, 5, 5));
			setComponentPopupMenu(popupMenu);
		}
		popupMenu.add(reset);
	}
}
