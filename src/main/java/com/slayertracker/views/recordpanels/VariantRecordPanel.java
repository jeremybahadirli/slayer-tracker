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

import com.slayertracker.groups.Variant;
import com.slayertracker.records.Record;
import com.slayertracker.records.RecordMap;
import com.slayertracker.views.GroupListPanel;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import lombok.Getter;

@Getter
public class VariantRecordPanel extends RecordPanel
{

	Variant variant;

	public VariantRecordPanel(Variant variant,
							  RecordMap<Variant, Record> variantRecords,
							  GroupListPanel groupListPanel)
	{
		super(variantRecords.get(variant), groupListPanel);
		this.variant = variant;

		// Header panel

		JLabel titleLabel = new JLabel(variant.getName());
		titleLabel.setMinimumSize(new Dimension(1, titleLabel.getPreferredSize().height));
		headerPanel.add(titleLabel);
		headerPanel.add(Box.createHorizontalGlue());

		// Right-click menu

		// Delete button
		resetMenuItem.addActionListener(e ->
		{
			final int selection = JOptionPane.showOptionDialog(this,
				"<html>This will delete the record: <b>" + variant.getName().toUpperCase() + "</b></html>",
				"Are you sure?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
				null, new String[]{"Yes", "No"}, "No");
			if (selection == JOptionPane.YES_OPTION)
			{
				variantRecords.remove(variant);
			}
		});

		// Stats panel

		bodyPanel.add(statsPanel);

		add(headerPanel);
		add(bodyPanel);
	}
}