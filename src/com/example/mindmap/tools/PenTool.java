package com.example.mindmap.tools;

import com.example.mindmap.services.commands.AddStrokeCommand;
import com.example.mindmap.tools.drawing.DrawingStroke;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class PenTool implements Tool {

    private final EditorContext ctx;
    private final boolean dashed;

    private DrawingStroke current;

    public PenTool(EditorContext ctx, boolean dashed) {
        this.ctx = ctx;
        this.dashed = dashed;
    }

    @Override
    public void onMousePressed(MouseEvent e) {
        current = new DrawingStroke(ctx.getBrushColor(), ctx.getBrushWidthPx(), dashed);
        current.addPoint(e.getX(), e.getY());
        ctx.repaint();
    }

    @Override
    public void onMouseDragged(MouseEvent e) {
        if (current == null) return;
        current.addPoint(e.getX(), e.getY());
        ctx.repaint();
    }

    @Override
    public void onMouseReleased(MouseEvent e) {
        if (current == null) return;

        if (!current.isEmpty()) {
            AddStrokeCommand cmd = new AddStrokeCommand(
                    ctx.getDrawingLayer(),
                    current,
                    ctx.getMindMapService(),
                    ctx.getMindMap()
            );
            ctx.getCommandManager().executeCommand(cmd);
        }

        current = null;
        ctx.repaint();
    }

    @Override
    public void paintOverlay(Graphics2D g2) {
        if (current == null || current.isEmpty()) return;

        g2.setColor(current.getColor());
        g2.setStroke(current.buildAwtStroke());
        g2.draw(current.buildPath());
    }

    @Override public void onMouseClicked(MouseEvent e) {}
    @Override public void onKeyPressed(KeyEvent e) {}
}
