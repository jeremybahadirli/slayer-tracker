package com.slayertracker.view;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RecordPanel extends JPanel {

    RecordPanel(String name,
                String kc,
                String xp,
                String gp,
                String kcRate,
                String xpRate,
                String gpRate) {

        setBorder(new EmptyBorder(0, 0, 4, 0));
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        // Name
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(FontManager.getRunescapeSmallFont());
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());
        nameLabel.setOpaque(true);
        nameLabel.setBorder(new EmptyBorder(4, 4, 4, 4));
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 5;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        add(nameLabel, c);
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = 1;

        // Metrics only
        c.ipady = 4;
        c.anchor = GridBagConstraints.LINE_START;

        // Labels
        c.weightx = 0.1;

        JLabel kcLabel = new JLabel("kc:");
        kcLabel.setFont(FontManager.getRunescapeSmallFont());
        c.gridx = 1;
        c.gridy = 1;
        add(kcLabel, c);

        JLabel kcRateLabel = new JLabel("kc/h:");
        kcRateLabel.setFont(FontManager.getRunescapeSmallFont());
        c.gridx = 3;
        c.gridy = 1;
        add(kcRateLabel, c);

        JLabel xpLabel = new JLabel("xp:");
        xpLabel.setFont(FontManager.getRunescapeSmallFont());
        c.gridx = 1;
        c.gridy = 2;
        add(xpLabel, c);

        JLabel xpRateLabel = new JLabel("xp/h:");
        xpRateLabel.setFont(FontManager.getRunescapeSmallFont());
        c.gridx = 3;
        c.gridy = 2;
        add(xpRateLabel, c);

        JLabel gpLabel = new JLabel("gp:");
        gpLabel.setFont(FontManager.getRunescapeSmallFont());
        c.gridx = 1;
        c.gridy = 3;
        add(gpLabel, c);

        JLabel gpRateLabel = new JLabel("gp/h:");
        gpRateLabel.setFont(FontManager.getRunescapeSmallFont());
        c.gridx = 3;
        c.gridy = 3;
        add(gpRateLabel, c);

        // Values
        c.weightx = 1;

        JLabel kcValueLabel = new JLabel(kc);
        kcValueLabel.setFont(FontManager.getRunescapeSmallFont());
        c.gridx = 2;
        c.gridy = 1;
        add(kcValueLabel, c);

        JLabel kcRateValueLabel = new JLabel(kcRate);
        kcRateValueLabel.setFont(FontManager.getRunescapeSmallFont());
        c.gridx = 4;
        c.gridy = 1;
        add(kcRateValueLabel, c);

        JLabel xpValueLabel = new JLabel(xp);
        xpValueLabel.setFont(FontManager.getRunescapeSmallFont());
        c.gridx = 2;
        c.gridy = 2;
        add(xpValueLabel, c);

        JLabel xpRateValueLabel = new JLabel(xpRate);
        xpRateValueLabel.setFont(FontManager.getRunescapeSmallFont());
        c.gridx = 4;
        c.gridy = 2;
        add(xpRateValueLabel, c);

        JLabel gpValueLabel = new JLabel(gp);
        gpValueLabel.setFont(FontManager.getRunescapeSmallFont());
        c.gridx = 2;
        c.gridy = 3;
        add(gpValueLabel, c);

        JLabel gpRateValueLabel = new JLabel(gpRate);
        gpRateValueLabel.setFont(FontManager.getRunescapeSmallFont());
        c.gridx = 4;
        c.gridy = 3;
        add(gpRateValueLabel, c);
    }
}
