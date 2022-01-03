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

import com.slayertracker.SlayerTrackerConfig;
import com.slayertracker.records.Record;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;

import static com.slayertracker.SlayerTrackerConfig.SlayerTrackerLootUnit.HIGH_ALCHEMY;

public class StatsPanel extends JPanel {

    public StatsPanel(Record record, SlayerTrackerConfig slayerTrackerConfig) {

        // Format data

        NumberFormat formatter = NumberFormat.getInstance();

        String kc = formatter.format(record.getKc());
        String kcRate = formatter.format(Math.round(record.getKc() / record.getHours()));
        String xp = formatter.format(record.getXp());
        String xpRate = formatter.format(Math.round(record.getXp() / record.getHours()));

        String gpString;
        String gp;
        String gpRate;
        if (slayerTrackerConfig.lootUnit().equals(HIGH_ALCHEMY)) {
            gpString = "ha";
            gp = formatter.format(record.getHa());
            gpRate = formatter.format(Math.round(record.getHa() / record.getHours()));
        } else {
            gpString = "ge";
            gp = formatter.format(record.getGe());
            gpRate = formatter.format(Math.round(record.getGe() / record.getHours()));
        }

        // Layout panel

        setLayout(new GridLayout());
        setOpaque(false);

        JPanel leftStats = new JPanel();
        leftStats.setLayout(new GridBagLayout());
        leftStats.setOpaque(false);

        JPanel rightStats = new JPanel();
        rightStats.setLayout(new GridBagLayout());
        rightStats.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();

        // All
        c.anchor = GridBagConstraints.LINE_START;

        // Labels
        c.weightx = 0;

        c.gridx = 0;
        c.gridy = 0;
        JLabel kcLabel = new JLabel("kc: ");
        kcLabel.setFont(FontManager.getRunescapeSmallFont());
        leftStats.add(kcLabel, c);

        JLabel kcRateLabel = new JLabel("kc/h: ");
        kcRateLabel.setFont(FontManager.getRunescapeSmallFont());
        rightStats.add(kcRateLabel, c);

        c.gridx = 0;
        c.gridy = 1;
        JLabel xpLabel = new JLabel("xp: ");
        xpLabel.setFont(FontManager.getRunescapeSmallFont());
        leftStats.add(xpLabel, c);

        JLabel xpRateLabel = new JLabel("xp/h: ");
        xpRateLabel.setFont(FontManager.getRunescapeSmallFont());
        rightStats.add(xpRateLabel, c);

        c.gridx = 0;
        c.gridy = 2;
        JLabel gpLabel = new JLabel(gpString + ": ");
        gpLabel.setFont(FontManager.getRunescapeSmallFont());
        leftStats.add(gpLabel, c);

        JLabel gpRateLabel = new JLabel(gpString + "/h: ");
        gpRateLabel.setFont(FontManager.getRunescapeSmallFont());
        rightStats.add(gpRateLabel, c);

        // Values
        c.weightx = 1;

        c.gridx = 1;
        c.gridy = 0;
        JLabel kcValueLabel = new JLabel(kc);
        kcValueLabel.setFont(FontManager.getRunescapeSmallFont());
        leftStats.add(kcValueLabel, c);

        JLabel kcRateValueLabel = new JLabel(kcRate);
        kcRateValueLabel.setFont(FontManager.getRunescapeSmallFont());
        rightStats.add(kcRateValueLabel, c);

        c.gridx = 1;
        c.gridy = 1;
        JLabel xpValueLabel = new JLabel(xp);
        xpValueLabel.setFont(FontManager.getRunescapeSmallFont());
        leftStats.add(xpValueLabel, c);

        JLabel xpRateValueLabel = new JLabel(xpRate);
        xpRateValueLabel.setFont(FontManager.getRunescapeSmallFont());
        rightStats.add(xpRateValueLabel, c);

        c.gridx = 1;
        c.gridy = 2;
        JLabel gpValueLabel = new JLabel(gp);
        gpValueLabel.setFont(FontManager.getRunescapeSmallFont());
        leftStats.add(gpValueLabel, c);

        JLabel gpRateValueLabel = new JLabel(gpRate);
        gpRateValueLabel.setFont(FontManager.getRunescapeSmallFont());
        rightStats.add(gpRateValueLabel, c);

        add(leftStats);
        add(rightStats);
    }
}