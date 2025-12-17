package com.example.mindmap.tools;

import com.example.mindmap.services.commands.AddNodeCommand;

import java.awt.event.MouseEvent;

public class AddTextNodeTool implements Tool {

    private final EditorContext ctx;

    public AddTextNodeTool(EditorContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void onMouseClicked(MouseEvent e) {
        AddNodeCommand cmd = new AddNodeCommand(
                ctx.getMindMap(),
                ctx.getMindMapService(),
                ctx.getElements(),
                e.getX(),
                e.getY(),
                "New idea"
        );
        ctx.getCommandManager().executeCommand(cmd);
        ctx.repaint();
    }
}
