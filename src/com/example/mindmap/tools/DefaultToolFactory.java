package com.example.mindmap.tools;

public class DefaultToolFactory implements ToolFactory {

    @Override
    public Tool createSelectTool(EditorContext ctx) {
        return new SelectTool(ctx);
    }

    @Override
    public Tool createAddTextNodeTool(EditorContext ctx) {
        return new AddTextNodeTool(ctx);
    }

    @Override
    public Tool createAddImageNodeTool(EditorContext ctx) {
        return new AddImageNodeTool(ctx);
    }

    @Override
    public Tool createPenTool(EditorContext ctx) {
        return new PenTool(ctx, false);
    }

    @Override
    public Tool createDashedPenTool(EditorContext ctx) {
        return new PenTool(ctx, true);
    }

    @Override
    public Tool createEraserTool(EditorContext ctx) {
        return new EraserTool(ctx);
    }
}
