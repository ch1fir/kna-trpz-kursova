package com.example.mindmap.soa.common;

import com.example.mindmap.entities.*;
import com.example.mindmap.tools.drawing.DrawingStroke;

import java.awt.Color;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class Codecs {
    private Codecs() {}

    private static String b64(String s) {
        if (s == null) s = "";
        return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    private static String unb64(String b64) {
        if (b64 == null || b64.isBlank()) return "";
        return new String(Base64.getDecoder().decode(b64), StandardCharsets.UTF_8);
    }

    // MAP|id|titleB64|descB64|favorite|catId|catTitleB64|catColor
    public static String encodeMap(MindMap m) {
        Category c = m.getCategory();
        int catId = (c == null) ? 0 : c.getId();
        String catTitle = (c == null) ? "" : c.getTitle();
        String catColor = (c == null) ? "" : c.getColor();

        return "MAP|" + m.getId()
                + "|" + b64(m.getTitle())
                + "|" + b64(m.getDescription())
                + "|" + (m.isFavorite() ? "1" : "0")
                + "|" + catId
                + "|" + b64(catTitle)
                + "|" + (catColor == null ? "" : catColor);
    }

    public static MindMap decodeMap(String line, User owner) {
        // tolerant parsing
        String[] p = line.split("\\|", -1);
        if (p.length < 5 || !"MAP".equals(p[0])) throw new IllegalArgumentException("Bad map line: " + line);

        MindMap m = new MindMap();
        m.setId(Integer.parseInt(p[1]));
        m.setTitle(unb64(p[2]));
        m.setDescription(unb64(p[3]));
        m.setFavorite("1".equals(p[4]));
        m.setOwner(owner);

        if (p.length >= 8) {
            int catId = Integer.parseInt(p[5].isBlank() ? "0" : p[5]);
            if (catId != 0) {
                Category c = new Category();
                c.setId(catId);
                c.setTitle(unb64(p[6]));
                c.setColor(p[7]);
                c.setOwner(owner);
                m.setCategory(c);
            }
        }
        return m;
    }

    // CAT|id|titleB64|color
    public static String encodeCategory(Category c) {
        return "CAT|" + c.getId() + "|" + b64(c.getTitle()) + "|" + (c.getColor() == null ? "" : c.getColor());
    }

    public static Category decodeCategory(String line, User owner) {
        String[] p = line.split("\\|", -1);
        if (p.length < 4 || !"CAT".equals(p[0])) throw new IllegalArgumentException("Bad category line: " + line);

        Category c = new Category();
        c.setId(Integer.parseInt(p[1]));
        c.setTitle(unb64(p[2]));
        c.setColor(p[3]);
        c.setOwner(owner);
        return c;
    }

    // TEXT|id|x|y|w|h|font|shapeB64|textB64
    // IMAGE|id|x|y|w|h|pathB64
    public static String encodeElement(MapElement el) {
        if (el instanceof TextNode t) {
            return "TEXT|" + t.getId()
                    + "|" + t.getX()
                    + "|" + t.getY()
                    + "|" + t.getWidthPx()
                    + "|" + t.getHeightPx()
                    + "|" + t.getFontSize()
                    + "|" + b64(t.getShapeType())
                    + "|" + b64(t.getTextForDisplay());
        }
        if (el instanceof ImageNode im) {
            return "IMAGE|" + im.getId()
                    + "|" + im.getX()
                    + "|" + im.getY()
                    + "|" + im.getWidthPx()
                    + "|" + im.getHeightPx()
                    + "|" + b64(im.getImageUrl());
        }
        throw new IllegalArgumentException("Unknown element type: " + el);
    }

    public static MapElement decodeElement(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length < 1) throw new IllegalArgumentException("Bad element line: " + line);

        switch (p[0]) {
            case "TEXT" -> {
                int id = Integer.parseInt(p[1]);
                float x = Float.parseFloat(p[2]);
                float y = Float.parseFloat(p[3]);
                int w = Integer.parseInt(p[4]);
                int h = Integer.parseInt(p[5]);
                int font = Integer.parseInt(p[6]);
                String shape = unb64(p[7]);
                String text = unb64(p[8]);

                // конструктор, який вже є в твоєму коді (використовується і в JDBC репозиторії)
                return new TextNode(id, x, y, null, text, font, shape, w, h);
            }
            case "IMAGE" -> {
                int id = Integer.parseInt(p[1]);
                float x = Float.parseFloat(p[2]);
                float y = Float.parseFloat(p[3]);
                int w = Integer.parseInt(p[4]);
                int h = Integer.parseInt(p[5]);
                String path = unb64(p[6]);

                return new ImageNode(id, x, y, null, path, w, h);
            }
            default -> throw new IllegalArgumentException("Bad element type: " + p[0]);
        }
    }

    // STROKE|idB64|argb|width|dashed|pointsB64
    public static String encodeStroke(DrawingStroke s) {
        String id = s.getId() == null ? "" : s.getId();
        String points = s.serializePoints();
        return "STROKE|" + b64(id)
                + "|" + s.getColor().getRGB()
                + "|" + s.getWidthPx()
                + "|" + (s.isDashed() ? "1" : "0")
                + "|" + b64(points);
    }

    public static DrawingStroke decodeStroke(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length < 6 || !"STROKE".equals(p[0])) throw new IllegalArgumentException("Bad stroke line: " + line);

        String id = unb64(p[1]);
        int argb = Integer.parseInt(p[2]);
        int width = Integer.parseInt(p[3]);
        boolean dashed = "1".equals(p[4]);
        String points = unb64(p[5]);

        DrawingStroke s = new DrawingStroke(id, new Color(argb, true), width, dashed);
        DrawingStroke.deserializePointsInto(s, points);
        return s;
    }
}
