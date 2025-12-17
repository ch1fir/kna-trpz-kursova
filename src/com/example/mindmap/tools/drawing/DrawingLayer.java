package com.example.mindmap.tools.drawing;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DrawingLayer {

    // Глобальні мазки по всій мапі
    private final List<DrawingStroke> strokes = new ArrayList<>();

    public List<DrawingStroke> getStrokes() {
        return strokes;
    }

    public void addStroke(DrawingStroke stroke) {
        strokes.add(stroke);
    }

    public void removeStroke(DrawingStroke stroke) {
        strokes.remove(stroke);
    }

    public void insertAt(int index, DrawingStroke stroke) {
        int i = Math.max(0, Math.min(index, strokes.size()));
        strokes.add(i, stroke);
    }

    public void removeAll(List<RemovedStroke> removed) {
        for (RemovedStroke r : removed) {
            strokes.remove(r.stroke());
        }
    }

    public void restoreAll(List<RemovedStroke> removed) {
        // вставляємо у зростаючому порядку індексів
        List<RemovedStroke> copy = new ArrayList<>(removed);
        copy.sort(Comparator.comparingInt(RemovedStroke::index));
        for (RemovedStroke r : copy) {
            insertAt(r.index(), r.stroke());
        }
    }

    /**
     * Стирає мазки, які перетинаються з кругом-ластиком у (x,y) з радіусом radiusPx.
     * ВАЖЛИВО: метод одразу видаляє зі списку і повертає, що саме видалили (для undo).
     */
    public List<RemovedStroke> eraseAt(int x, int y, int radiusPx) {
        if (strokes.isEmpty()) return Collections.emptyList();

        Ellipse2D eraser = new Ellipse2D.Float(
                x - radiusPx, y - radiusPx,
                radiusPx * 2f, radiusPx * 2f
        );

        List<RemovedStroke> removed = new ArrayList<>();

        for (int i = strokes.size() - 1; i >= 0; i--) {
            DrawingStroke s = strokes.get(i);

            if (!s.getBounds().intersects(eraser.getBounds2D())) continue;

            Path2D path = s.buildPath();
            Shape thick = s.buildAwtStroke().createStrokedShape(path);

            if (thick.intersects(eraser.getBounds2D())) {
                removed.add(new RemovedStroke(s, i));
                strokes.remove(i);
            }
        }

        return removed;
    }

    public record RemovedStroke(DrawingStroke stroke, int index) {}
}
