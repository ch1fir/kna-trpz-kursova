package com.example.mindmap.services.commands;

import com.example.mindmap.entities.MindMap;
import com.example.mindmap.services.MindMapService;
import com.example.mindmap.tools.drawing.DrawingLayer;

import java.util.List;

public class EraseStrokesCommand implements Command {

    private final DrawingLayer layer;
    private final List<DrawingLayer.RemovedStroke> removed;
    private final MindMapService service;
    private final MindMap map;

    public EraseStrokesCommand(DrawingLayer layer,
                               List<DrawingLayer.RemovedStroke> removed,
                               MindMapService service,
                               MindMap map) {
        this.layer = layer;
        this.removed = removed;
        this.service = service;
        this.map = map;
    }

    @Override
    public void execute() {
        layer.removeAll(removed);
        for (DrawingLayer.RemovedStroke r : removed) {
            service.deleteStroke(r.stroke());
        }
    }

    @Override
    public void undo() {
        layer.restoreAll(removed);
        for (DrawingLayer.RemovedStroke r : removed) {
            service.saveStroke(map, r.stroke());
        }
    }
}
