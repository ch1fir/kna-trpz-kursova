package com.example.mindmap.tools;

import com.example.mindmap.entities.MapElement;
import com.example.mindmap.services.commands.MoveNodeCommand;
import com.example.mindmap.services.commands.ResizeNodeCommand;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class SelectTool implements Tool {

    private enum DragMode { NONE, MOVE, RESIZE_RIGHT, RESIZE_BOTTOM, RESIZE_CORNER }

    private final EditorContext ctx;

    private MapElement active;
    private DragMode dragMode = DragMode.NONE;

    // move
    private int offsetX, offsetY;
    private float startX, startY;

    // resize
    private int startW, startH;

    private static final int RESIZE_MARGIN = 7;

    public SelectTool(EditorContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void onMousePressed(MouseEvent e) {
        MapElement hit = ctx.findElementAt(e.getX(), e.getY());
        if (hit == null) {
            ctx.setSelected(null);
            active = null;
            dragMode = DragMode.NONE;
            return;
        }

        ctx.setSelected(hit);
        ctx.requestFocus();

        active = hit;
        startX = hit.getX();
        startY = hit.getY();
        startW = hit.getWidthPx();
        startH = hit.getHeightPx();

        Rectangle r = new Rectangle((int) hit.getX(), (int) hit.getY(), hit.getWidthPx(), hit.getHeightPx());
        dragMode = detectDragMode(r, e.getX(), e.getY());

        offsetX = (int) (e.getX() - hit.getX());
        offsetY = (int) (e.getY() - hit.getY());
    }

    @Override
    public void onMouseDragged(MouseEvent e) {
        if (active == null) return;

        switch (dragMode) {
            case MOVE -> {
                active.setX(e.getX() - offsetX);
                active.setY(e.getY() - offsetY);
            }
            case RESIZE_RIGHT -> active.setWidthPx(e.getX() - (int) active.getX());
            case RESIZE_BOTTOM -> active.setHeightPx(e.getY() - (int) active.getY());
            case RESIZE_CORNER -> {
                active.setWidthPx(e.getX() - (int) active.getX());
                active.setHeightPx(e.getY() - (int) active.getY());
            }
            default -> {}
        }

        ctx.repaint();
    }

    @Override
    public void onMouseReleased(MouseEvent e) {
        if (active == null) return;

        // commit move
        float endX = active.getX();
        float endY = active.getY();
        if (dragMode == DragMode.MOVE && (endX != startX || endY != startY)) {
            MoveNodeCommand cmd = new MoveNodeCommand(active, startX, startY, endX, endY, ctx.getMindMapService());
            ctx.getCommandManager().executeCommand(cmd);
        }

        // commit resize
        int endW = active.getWidthPx();
        int endH = active.getHeightPx();
        if ((dragMode == DragMode.RESIZE_RIGHT || dragMode == DragMode.RESIZE_BOTTOM || dragMode == DragMode.RESIZE_CORNER)
                && (endW != startW || endH != startH)) {
            ResizeNodeCommand cmd = new ResizeNodeCommand(active, startW, startH, endW, endH, ctx.getMindMapService());
            ctx.getCommandManager().executeCommand(cmd);
        }

        active = null;
        dragMode = DragMode.NONE;
    }

    private DragMode detectDragMode(Rectangle r, int x, int y) {
        boolean nearRight = Math.abs(x - (r.x + r.width)) <= RESIZE_MARGIN;
        boolean nearBottom = Math.abs(y - (r.y + r.height)) <= RESIZE_MARGIN;

        if (nearRight && nearBottom) return DragMode.RESIZE_CORNER;
        if (nearRight) return DragMode.RESIZE_RIGHT;
        if (nearBottom) return DragMode.RESIZE_BOTTOM;
        return DragMode.MOVE;
    }
    @Override public void onKeyPressed(KeyEvent e) {}
    @Override public void paintOverlay(Graphics2D g2) {}

    @Override
    public void onMouseClicked(MouseEvent e) {
        if (e.getClickCount() != 2) return;

        MapElement hit = ctx.findElementAt(e.getX(), e.getY());
        if (!(hit instanceof com.example.mindmap.entities.TextNode)) return;

        String oldText = hit.getTextForDisplay();
        if (oldText == null) oldText = "";

        String newText = javax.swing.JOptionPane.showInputDialog(
                null, "Edit text:", oldText
        );
        if (newText == null) return;

        hit.setTextForDisplay(newText.trim());
        ctx.getMindMapService().updateElement(hit);
        ctx.repaint();
    }

}
