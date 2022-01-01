/*
 * Copyright (c) 2021, Jeremy Bahadirli <https://github.com/jeremybahadirli>
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
package com.slayertracker.view;

import com.slayertracker.model.Assignment;
import com.slayertracker.model.AssignmentRecord;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import java.util.HashMap;

public class SlayerTrackerPanel extends PluginPanel {
    private final ItemManager itemManager;

    public SlayerTrackerPanel(ItemManager itemManager) {
        this.itemManager = itemManager;

        setLayout(new DynamicGridLayout(0, 1, 0, 5));
    }

    public void build(HashMap<Assignment, AssignmentRecord> assignmentRecords) {
        removeAll();

        assignmentRecords.forEach((assignmentType, assignmentRecord) -> {
            RecordGroupPanel rgp = new RecordGroupPanel();

            rgp.addRecordPanel(new AssignmentRecordPanel(
                    assignmentType.getName(),
                    String.valueOf(assignmentRecord.getKc()),
                    String.valueOf(assignmentRecord.getXp()),
                    String.valueOf(assignmentRecord.getHa()),
                    String.valueOf(assignmentRecord.getKcRate()),
                    String.valueOf(assignmentRecord.getXpRate()),
                    String.valueOf(assignmentRecord.getHaRate()),
                    new ImageIcon(itemManager.getImage(assignmentType.getItemSpriteId()))
            ));

            assignmentRecord.getVariantRecords().forEach((variantType, variantRecord) ->
                    rgp.addRecordPanel(new VariantRecordPanel(
                            variantType.getName(),
                            String.valueOf(variantRecord.getKc()),
                            String.valueOf(variantRecord.getXp()),
                            String.valueOf(variantRecord.getHa()),
                            String.valueOf(variantRecord.getKcRate()),
                            String.valueOf(variantRecord.getXpRate()),
                            String.valueOf(variantRecord.getHaRate())
                    )));
            add(rgp);
        });

        revalidate();
        repaint();
    }
}