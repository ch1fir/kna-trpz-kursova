package com.example.mindmap.tools;

import com.example.mindmap.entities.MapElement;

import javax.swing.*;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;

public class AddImageNodeTool implements Tool {

    private final EditorContext ctx;

    public AddImageNodeTool(EditorContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void onMouseClicked(MouseEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose image");

        int res = chooser.showOpenDialog(null);
        if (res != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        String path = file.getAbsolutePath();

        MapElement el = ctx.getMindMapService().addImageNode(
                ctx.getMindMap(),
                e.getX(),
                e.getY(),
                path,
                120,
                80
        );

        ctx.getElements().add(el);
        ctx.repaint();
    }

    @Override public void onMousePressed(MouseEvent e) {}
    @Override public void onMouseDragged(MouseEvent e) {}
    @Override public void onMouseReleased(MouseEvent e) {}
    @Override public void onKeyPressed(KeyEvent e) {}
    @Override public void paintOverlay(Graphics2D g2) {}
}
