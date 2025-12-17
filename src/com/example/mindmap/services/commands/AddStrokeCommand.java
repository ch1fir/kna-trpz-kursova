package com.example.mindmap.services.commands;

import com.example.mindmap.entities.MindMap;
import com.example.mindmap.services.MindMapService;
import com.example.mindmap.tools.drawing.DrawingLayer;
import com.example.mindmap.tools.drawing.DrawingStroke;

public class AddStrokeCommand implements Command {

    private final DrawingLayer layer;
    private final DrawingStroke stroke;
    private final MindMapService service;
    private final MindMap map;

    public AddStrokeCommand(DrawingLayer layer,
                            DrawingStroke stroke,
                            MindMapService service,
                            MindMap map) {
        this.layer = layer;
        this.stroke = stroke;
        this.service = service;
        this.map = map;
    }

    @Override
    public void execute() {
        layer.addStroke(stroke);
        service.saveStroke(map, stroke);
    }

    @Override
    public void undo() {
        layer.removeStroke(stroke);
        service.deleteStroke(stroke);
    }
}
