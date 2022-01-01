package com.slayertracker.view;

import javax.swing.*;
import java.awt.*;

public class RecordGroupPanel extends JPanel {

    private boolean first;

    RecordGroupPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        first = true;
    }

    void addRecordPanel(RecordPanel p) {
        if (first) {
            add(p);
        } else {
            JPanel hPanel = new JPanel();
            hPanel.setLayout(new BoxLayout(hPanel, BoxLayout.X_AXIS));
            hPanel.add(Box.createRigidArea(new Dimension(36, 0)));
            hPanel.add(p);
            add(hPanel);
        }
        first = false;
    }
}
