package com.example.mindmap.tools;

public interface ToolFactory {
    Tool createSelectTool(EditorContext ctx);
    Tool createAddTextNodeTool(EditorContext ctx);
    Tool createAddImageNodeTool(EditorContext ctx);

    Tool createPenTool(EditorContext ctx);
    Tool createDashedPenTool(EditorContext ctx);
    Tool createEraserTool(EditorContext ctx);
}
