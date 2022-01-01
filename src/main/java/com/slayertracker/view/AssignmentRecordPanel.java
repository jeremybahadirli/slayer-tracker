package com.slayertracker.view;

import javax.swing.*;
import java.awt.*;

public class AssignmentRecordPanel extends RecordPanel {

    AssignmentRecordPanel(
            String name,
            String kc,
            String xp,
            String gp,
            String kcRate,
            String xpRate,
            String gpRate,
            ImageIcon icon) {

        super(name, kc, xp, gp, kcRate, xpRate, gpRate);

        GridBagConstraints c = new GridBagConstraints();

        // Icon
        JLabel iconLabel = new JLabel();
        iconLabel.setIcon(icon);
        c.gridheight = 3;
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.2;
        add(iconLabel, c);
    }
}
