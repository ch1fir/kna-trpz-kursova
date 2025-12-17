package com.example.mindmap.ui.dashboard;

import java.awt.*;

public class ColorUtil {
    public static String toHex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    public static Color fromHex(String hex, Color fallback) {
        try {
            if (hex == null || hex.isBlank()) return fallback;
            return Color.decode(hex);
        } catch (Exception e) {
            return fallback;
        }
    }
}
