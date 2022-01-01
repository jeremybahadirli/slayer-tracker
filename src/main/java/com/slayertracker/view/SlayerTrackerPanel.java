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

        assignmentRecords.values().forEach(assignmentRecord -> {
            RecordGroupPanel rgp = new RecordGroupPanel();

            rgp.addRecordPanel(new AssignmentRecordPanel(
                    assignmentRecord.getType().getName(),
                    String.valueOf(assignmentRecord.getKc()),
                    String.valueOf(assignmentRecord.getXp()),
                    String.valueOf(assignmentRecord.getHa()),
                    String.valueOf(assignmentRecord.getKcRate()),
                    String.valueOf(assignmentRecord.getXpRate()),
                    String.valueOf(assignmentRecord.getHaRate()),
                    new ImageIcon(itemManager.getImage(assignmentRecord.getType().getItemSpriteId()))
            ));

            assignmentRecord.getVariantRecords().values().forEach(variantRecord ->
                    rgp.addRecordPanel(new VariantRecordPanel(
                            variantRecord.getType().getName(),
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