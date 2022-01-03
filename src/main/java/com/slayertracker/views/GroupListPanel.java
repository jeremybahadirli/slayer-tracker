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
import net.runelite.client.game.ItemManager;

import javax.swing.*;
import java.awt.*;

public class GroupListPanel extends JPanel {

    private final Assignment assignment;
    private final AssignmentRecord assignmentRecord;
    private final SlayerTrackerConfig slayerTrackerConfig;
    private final ItemManager itemManager;

    GroupListPanel(
            Assignment assignment,
            AssignmentRecord assignmentRecord,
            SlayerTrackerConfig slayerTrackerConfig,
            ItemManager itemManager) {

        this.assignment = assignment;
        this.assignmentRecord = assignmentRecord;
        this.slayerTrackerConfig = slayerTrackerConfig;
        this.itemManager = itemManager;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        build();
    }

    private void build() {
        add(new AssignmentRecordPanel(assignment, assignmentRecord, slayerTrackerConfig, itemManager));

        assignmentRecord.getVariantRecords().forEach((variant, variantRecord) -> {
            JPanel horizontalPanel = new JPanel();
            horizontalPanel.setLayout(new BoxLayout(horizontalPanel, BoxLayout.X_AXIS));
            horizontalPanel.add(Box.createRigidArea(new Dimension(36, 0)));
            horizontalPanel.add(new VariantRecordPanel(variant, variantRecord, slayerTrackerConfig));
            add(horizontalPanel);
        });

        add(Box.createRigidArea(new Dimension(0, 5)));
    }
}
