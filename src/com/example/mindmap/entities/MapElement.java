package com.example.mindmap.entities;

import com.example.mindmap.visitor.MapElementVisitor;

public abstract class MapElement {

    protected int id;
    protected float x;
    protected float y;
    protected MindMap map;

    protected int width;
    protected int height;

    public MapElement(int id, float x, float y, MindMap map) {
        this(id, x, y, map, 140, 45);
    }

    public MapElement(int id, float x, float y, MindMap map, int width, int height) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.map = map;
        this.width = width;
        this.height = height;
    }

    public abstract String getType();
    public abstract String getTextForDisplay();
    public abstract void setTextForDisplay(String text);

    // Visitor entry-point
    public abstract void accept(MapElementVisitor visitor);

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public float getX() { return x; }
    public void setX(float x) { this.x = x; }

    public float getY() { return y; }
    public void setY(float y) { this.y = y; }

    public MindMap getMap() { return map; }
    public void setMap(MindMap map) { this.map = map; }

    public int getWidthPx() { return width; }
    public void setWidthPx(int width) { this.width = Math.max(width, 40); }

    public int getHeightPx() { return height; }
    public void setHeightPx(int height) { this.height = Math.max(height, 30); }
}
