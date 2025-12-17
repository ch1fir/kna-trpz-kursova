package com.example.mindmap.entities;

import com.example.mindmap.visitor.MapElementVisitor;

public class ImageNode extends MapElement {

    private String imageUrl;

    public ImageNode() {
        super(0, 0, 0, null, 120, 80);
    }

    public ImageNode(int id, float x, float y, MindMap map,
                     String imageUrl, int width, int height) {
        super(id, x, y, map, width, height);
        this.imageUrl = imageUrl;
    }

    public ImageNode(float x, float y, String imageUrl) {
        super(0, x, y, null, 120, 80);
        this.imageUrl = imageUrl;
    }

    @Override
    public String getType() { return "IMAGE"; }

    @Override
    public String getTextForDisplay() { return null; }

    @Override
    public void setTextForDisplay(String text) {
        // нічого не робимо
    }

    @Override
    public void accept(MapElementVisitor visitor) {
        visitor.visitImageNode(this);
    }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
