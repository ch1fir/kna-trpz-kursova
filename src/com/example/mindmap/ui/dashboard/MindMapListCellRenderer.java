package com.example.mindmap.ui.dashboard;

import com.example.mindmap.entities.Category;
import com.example.mindmap.entities.MindMap;

import javax.swing.*;
import java.awt.*;

public class MindMapListCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof MindMap map) {
            String star = map.isFavorite() ? "★ " : "☆ ";
            Category cat = map.getCategory();

            lbl.setText(star + map.getTitle() + (cat != null ? "  [" + cat.getTitle() + "]" : ""));
            lbl.setFont(lbl.getFont().deriveFont(map.isFavorite() ? Font.BOLD : Font.PLAIN));

            if (!isSelected) {
                lbl.setBackground(map.isFavorite() ? new Color(255, 250, 220) : Color.WHITE);
            }

            if (cat != null) {
                Color c = ColorUtil.fromHex(cat.getColor(), new Color(140, 140, 140));
                lbl.setIcon(new DotIcon(c, 10));
            } else {
                lbl.setIcon(null);
            }
        }

        lbl.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        return lbl;
    }
}
