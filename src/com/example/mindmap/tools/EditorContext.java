package com.example.mindmap.tools;

import com.example.mindmap.entities.MapElement;
import com.example.mindmap.entities.MindMap;
import com.example.mindmap.services.MindMapService;
import com.example.mindmap.services.commands.CommandManager;
import com.example.mindmap.tools.drawing.DrawingLayer;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class EditorContext {

    private final JComponent canvas;
    private final MindMap mindMap;
    private final MindMapService mindMapService;
    private final CommandManager commandManager;
    private final List<MapElement> elements;

    // шар мазків
    private final DrawingLayer drawingLayer;

    // налаштування пензля/ластика
    private Color brushColor = Color.BLACK;
    private float brushWidthPx = 3f;
    private int eraserRadiusPx = 10;

    private MapElement selected;

    // Старий конструктор (для сумісності)
    public EditorContext(JComponent canvas,
                         MindMap mindMap,
                         MindMapService mindMapService,
                         CommandManager commandManager,
                         List<MapElement> elements) {
        this(canvas, mindMap, mindMapService, commandManager, elements, new DrawingLayer());
    }

    // конструктор з drawingLayer
    public EditorContext(JComponent canvas,
                         MindMap mindMap,
                         MindMapService mindMapService,
                         CommandManager commandManager,
                         List<MapElement> elements,
                         DrawingLayer drawingLayer) {
        this.canvas = canvas;
        this.mindMap = mindMap;
        this.mindMapService = mindMapService;
        this.commandManager = commandManager;
        this.elements = elements;
        this.drawingLayer = drawingLayer;
    }

    public MindMap getMindMap() { return mindMap; }
    public MindMapService getMindMapService() { return mindMapService; }
    public CommandManager getCommandManager() { return commandManager; }
    public List<MapElement> getElements() { return elements; }
    public DrawingLayer getDrawingLayer() { return drawingLayer; }

    public MapElement getSelected() { return selected; }
    public void setSelected(MapElement el) { this.selected = el; repaint(); }

    public void repaint() { canvas.repaint(); }
    public void requestFocus() { canvas.requestFocusInWindow(); }

    // brush settings
    public Color getBrushColor() { return brushColor; }
    public void setBrushColor(Color brushColor) { this.brushColor = brushColor; }

    public float getBrushWidthPx() { return brushWidthPx; }
    public void setBrushWidthPx(float brushWidthPx) { this.brushWidthPx = Math.max(1f, brushWidthPx); }

    public int getEraserRadiusPx() { return eraserRadiusPx; }
    public void setEraserRadiusPx(int eraserRadiusPx) { this.eraserRadiusPx = Math.max(3, eraserRadiusPx); }

    public MapElement findElementAt(int x, int y) {
        for (int i = elements.size() - 1; i >= 0; i--) {
            MapElement el = elements.get(i);
            Rectangle r = new Rectangle((int) el.getX(), (int) el.getY(), el.getWidthPx(), el.getHeightPx());
            if (r.contains(x, y)) return el;
        }
        return null;
    }
}
