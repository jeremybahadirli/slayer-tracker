package com.slayertracker.view;

import javax.swing.*;
import java.awt.*;

public class VariantRecordPanel extends RecordPanel {

    VariantRecordPanel(
            String name,
            String kc,
            String xp,
            String gp,
            String kcRate,
            String xpRate,
            String gpRate) {

        super(name, kc, xp, gp, kcRate, xpRate, gpRate);

        GridBagConstraints c = new GridBagConstraints();

        // Space in place of icon
        c.gridx = 0;
        c.gridy = 1;
        c.gridheight = 3;
        c.weightx = 0.2;
        add(Box.createHorizontalGlue(), c);
    }
}