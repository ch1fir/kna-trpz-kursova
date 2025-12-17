package com.example.mindmap.visitor;

import com.example.mindmap.entities.ImageNode;
import com.example.mindmap.entities.TextNode;

public interface MapElementVisitor {
    void visitTextNode(TextNode node);
    void visitImageNode(ImageNode node);
}
