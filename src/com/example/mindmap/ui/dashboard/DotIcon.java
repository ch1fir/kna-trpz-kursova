package com.example.mindmap.ui.dashboard;

import javax.swing.*;
import java.awt.*;

public class DotIcon implements Icon {
    private final int size;
    private final Color color;

    public DotIcon(Color color, int size) {
        this.color = color;
        this.size = size;
    }

    @Override public int getIconWidth() { return size; }
    @Override public int getIconHeight() { return size; }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.fillOval(x, y, size, size);
        g2.setColor(new Color(0, 0, 0, 60));
        g2.drawOval(x, y, size, size);
        g2.dispose();
    }
}
