package com.example.mindmap.tools;

import com.example.mindmap.services.commands.EraseStrokesCommand;
import com.example.mindmap.tools.drawing.DrawingLayer;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EraserTool implements Tool {

    private final EditorContext ctx;

    private final List<DrawingLayer.RemovedStroke> removedDuringDrag = new ArrayList<>();
    private final Set<String> removedIds = new HashSet<>();

    private int lastX = -1;
    private int lastY = -1;

    public EraserTool(EditorContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void onMousePressed(MouseEvent e) {
        removedDuringDrag.clear();
        removedIds.clear();
        eraseAt(e.getX(), e.getY());
        ctx.repaint();
    }

    @Override
    public void onMouseDragged(MouseEvent e) {
        if (Math.abs(e.getX() - lastX) + Math.abs(e.getY() - lastY) < 2) return;
        eraseAt(e.getX(), e.getY());
        ctx.repaint();
    }

    @Override
    public void onMouseReleased(MouseEvent e) {
        if (removedDuringDrag.isEmpty()) return;

        // щоб redo працював: повертаємо назад, а команда знову видалить
        ctx.getDrawingLayer().restoreAll(removedDuringDrag);

        EraseStrokesCommand cmd = new EraseStrokesCommand(
                ctx.getDrawingLayer(),
                new ArrayList<>(removedDuringDrag),
                ctx.getMindMapService(),
                ctx.getMindMap()
        );
        ctx.getCommandManager().executeCommand(cmd);

        removedDuringDrag.clear();
        removedIds.clear();
        lastX = lastY = -1;
        ctx.repaint();
    }

    private void eraseAt(int x, int y) {
        lastX = x;
        lastY = y;

        int radius = ctx.getEraserRadiusPx();
        List<DrawingLayer.RemovedStroke> removedNow = ctx.getDrawingLayer().eraseAt(x, y, radius);

        for (DrawingLayer.RemovedStroke r : removedNow) {
            if (removedIds.add(r.stroke().getId())) {
                removedDuringDrag.add(r);
            }
        }
    }

    @Override public void onMouseClicked(MouseEvent e) {}
    @Override public void onKeyPressed(KeyEvent e) {}
}
