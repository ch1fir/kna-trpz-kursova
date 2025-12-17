package com.example.mindmap.export;

import com.example.mindmap.entities.MapElement;
import com.example.mindmap.entities.MindMap;
import com.example.mindmap.visitor.MapElementToTextVisitor;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TxtExportStrategy implements ExportStrategy {

    private final List<MapElement> elements;

    public TxtExportStrategy(List<MapElement> elements) {
        this.elements = elements;
    }

    @Override
    public void export(MindMap map, JComponent canvas, File targetFile) throws IOException {
        MapElementToTextVisitor visitor = new MapElementToTextVisitor();
        for (MapElement el : elements) visitor.append(el);

        try (Writer w = new OutputStreamWriter(new FileOutputStream(targetFile), StandardCharsets.UTF_8)) {
            w.write("Mind Map Export (TXT)\n");
            w.write("Title: " + map.getTitle() + "\n");
            w.write("Canvas size: " + canvas.getWidth() + "x" + canvas.getHeight() + "\n");
            w.write("Elements:\n");
            w.write(visitor.build());
        }
    }
}
