package com.slayertracker;

import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SlayerTrackerPanel extends PluginPanel {
    // TODO all this
    SlayerTrackerPanel(ItemManager itemManager) {

        Assignment assignment = Assignment.WYRMS;

        setBorder(new EmptyBorder(6, 6, 6, 6));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new BorderLayout());

        JPanel recordPanel = new JPanel();
        recordPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        JLabel name = new JLabel(assignment.getName());
        name.setFont(FontManager.getRunescapeSmallFont());
        name.setForeground(Color.WHITE);
        recordPanel.add(name);

        JLabel icon = new JLabel("");
        icon.setIcon(new ImageIcon(itemManager.getImage(assignment.getItemSpriteId())));
        recordPanel.add(icon);

        recordPanel.setLayout(new BoxLayout(recordPanel, BoxLayout.Y_AXIS));
        add(recordPanel);
    }
}
