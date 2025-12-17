package com.example.mindmap.services.commands;

import com.example.mindmap.entities.MapElement;
import com.example.mindmap.services.MindMapService;

public class ResizeNodeCommand implements Command {

    private final MapElement element;
    private final int oldW, oldH;
    private final int newW, newH;
    private final MindMapService service;

    public ResizeNodeCommand(MapElement element, int oldW, int oldH, int newW, int newH, MindMapService service) {
        this.element = element;
        this.oldW = oldW;
        this.oldH = oldH;
        this.newW = newW;
        this.newH = newH;
        this.service = service;
    }

    @Override
    public void execute() {
        element.setWidthPx(newW);
        element.setHeightPx(newH);
        service.updateElement(element);
    }

    @Override
    public void undo() {
        element.setWidthPx(oldW);
        element.setHeightPx(oldH);
        service.updateElement(element);
    }
}
