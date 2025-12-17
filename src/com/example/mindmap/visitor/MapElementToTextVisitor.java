package com.example.mindmap.visitor;

import com.example.mindmap.entities.ImageNode;
import com.example.mindmap.entities.MapElement;
import com.example.mindmap.entities.TextNode;

public class MapElementToTextVisitor implements MapElementVisitor {

    private final StringBuilder sb = new StringBuilder();

    public void append(MapElement el) {
        if (el == null) return;
        el.accept(this);
    }

    public String build() {
        return sb.toString();
    }

    @Override
    public void visitTextNode(TextNode node) {
        sb.append("TEXT")
                .append(" id=").append(node.getId())
                .append(" x=").append(node.getX())
                .append(" y=").append(node.getY())
                .append(" w=").append(node.getWidthPx())
                .append(" h=").append(node.getHeightPx())
                .append(" text=\"").append(escape(node.getTextForDisplay())).append("\"")
                .append(System.lineSeparator());
    }

    @Override
    public void visitImageNode(ImageNode node) {
        sb.append("IMAGE")
                .append(" id=").append(node.getId())
                .append(" x=").append(node.getX())
                .append(" y=").append(node.getY())
                .append(" w=").append(node.getWidthPx())
                .append(" h=").append(node.getHeightPx())
                .append(" path=\"").append(escape(node.getImageUrl())).append("\"")
                .append(System.lineSeparator());
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
