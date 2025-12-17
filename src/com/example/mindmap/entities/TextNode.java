package com.example.mindmap.entities;

import com.example.mindmap.visitor.MapElementVisitor;

public class TextNode extends MapElement {

    private String textContent;
    private int fontSize;
    private String shapeType;

    public TextNode() {
        super(0, 0, 0, null, 140, 45);
    }

    public TextNode(int id, float x, float y, MindMap map,
                    String textContent, int fontSize, String shapeType) {
        super(id, x, y, map, 140, 45);
        this.textContent = textContent;
        this.fontSize = fontSize;
        this.shapeType = shapeType;
    }

    public TextNode(int id, float x, float y, MindMap map,
                    String textContent, int fontSize, String shapeType,
                    int width, int height) {
        super(id, x, y, map, width, height);
        this.textContent = textContent;
        this.fontSize = fontSize;
        this.shapeType = shapeType;
    }

    public TextNode(float x, float y, String textContent) {
        super(0, x, y, null, 140, 45);
        this.textContent = textContent;
        this.fontSize = 14;
        this.shapeType = "RECT";
    }

    @Override
    public String getType() { return "TEXT"; }

    @Override
    public String getTextForDisplay() { return textContent; }

    @Override
    public void setTextForDisplay(String text) { this.textContent = text; }

    @Override
    public void accept(MapElementVisitor visitor) {
        visitor.visitTextNode(this);
    }

    public String getTextContent() { return textContent; }
    public void setTextContent(String textContent) { this.textContent = textContent; }

    public int getFontSize() { return fontSize; }
    public void setFontSize(int fontSize) { this.fontSize = fontSize; }

    public String getShapeType() { return shapeType; }
    public void setShapeType(String shapeType) { this.shapeType = shapeType; }
}
