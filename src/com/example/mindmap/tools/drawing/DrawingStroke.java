package com.example.mindmap.tools.drawing;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

public class DrawingStroke {
    private final String id;

    private final List<PointF> points = new ArrayList<>();
    private final Color color;
    private final float widthPx;
    private final boolean dashed;

    public DrawingStroke(Color color, float widthPx, boolean dashed) {
        this(UUID.randomUUID().toString(), color, widthPx, dashed);
    }

    public DrawingStroke(String id, Color color, float widthPx, boolean dashed) {
        this.id = id;
        this.color = color;
        this.widthPx = widthPx;
        this.dashed = dashed;
    }

    public String getId() { return id; }
    public Color getColor() { return color; }
    public float getWidthPx() { return widthPx; }
    public boolean isDashed() { return dashed; }

    public void addPoint(float x, float y) {
        points.add(new PointF(x, y));
    }

    public List<PointF> getPoints() {
        return Collections.unmodifiableList(points);
    }

    public boolean isEmpty() {
        return points.size() < 2;
    }

    public Path2D buildPath() {
        Path2D path = new Path2D.Float();
        if (points.isEmpty()) return path;

        PointF p0 = points.get(0);
        path.moveTo(p0.x, p0.y);

        for (int i = 1; i < points.size(); i++) {
            PointF p = points.get(i);
            path.lineTo(p.x, p.y);
        }
        return path;
    }

    public Stroke buildAwtStroke() {
        if (!dashed) {
            return new BasicStroke(widthPx, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        }
        float dashLen = Math.max(6f, widthPx * 3f);
        return new BasicStroke(
                widthPx,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND,
                10f,
                new float[]{dashLen, dashLen},
                0f
        );
    }

    public Rectangle2D getBounds() {
        return buildPath().getBounds2D();
    }

    /** Серіалізація точок у рядок: "x,y;x,y;..." (Locale.US щоб крапка) */
    public String serializePoints() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < points.size(); i++) {
            PointF p = points.get(i);
            if (i > 0) sb.append(';');
            sb.append(String.format(Locale.US, "%f,%f", p.x, p.y));
        }
        return sb.toString();
    }

    public static void deserializePointsInto(DrawingStroke stroke, String data) {
        if (data == null || data.isBlank()) return;
        String[] pairs = data.split(";");
        for (String pair : pairs) {
            String[] xy = pair.split(",");
            if (xy.length != 2) continue;
            try {
                float x = Float.parseFloat(xy[0]);
                float y = Float.parseFloat(xy[1]);
                stroke.addPoint(x, y);
            } catch (NumberFormatException ignored) {}
        }
    }

    public static class PointF {
        public final float x;
        public final float y;

        public PointF(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}
