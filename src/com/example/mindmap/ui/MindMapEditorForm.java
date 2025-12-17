package com.example.mindmap.ui;

import com.example.mindmap.entities.MindMap;
import com.example.mindmap.entities.MapElement;
import com.example.mindmap.entities.TextNode;
import com.example.mindmap.export.ExportService;
import com.example.mindmap.export.JpgExportStrategy;
import com.example.mindmap.export.PdfExportStrategy;
import com.example.mindmap.export.PngExportStrategy;
import com.example.mindmap.export.TxtExportStrategy;
import com.example.mindmap.services.MindMapService;
import com.example.mindmap.services.commands.CommandManager;
import com.example.mindmap.services.commands.DeleteNodeCommand;
import com.example.mindmap.tools.DefaultToolFactory;
import com.example.mindmap.tools.EditorContext;
import com.example.mindmap.tools.SelectTool;
import com.example.mindmap.tools.Tool;
import com.example.mindmap.tools.ToolFactory;
import com.example.mindmap.tools.drawing.DrawingStroke;
import com.example.mindmap.visitor.MapElementContentRenderVisitor;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MindMapEditorForm extends JFrame {

    private final MindMap mindMap;
    private final MindMapService mindMapService;
    private final CanvasPanel canvasPanel;
    private final ExportService exportService;
    private final CommandManager commandManager;

    public MindMapEditorForm(MindMap mindMap, MindMapService mindMapService) {
        super("Editing: " + mindMap.getTitle());
        this.mindMap = mindMap;
        this.mindMapService = mindMapService;
        this.exportService = new ExportService();
        this.commandManager = new CommandManager();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(980, 680);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        add(mainPanel);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        topBar.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(mindMap.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        topBar.add(titleLabel, BorderLayout.WEST);

        // ===== Right buttons =====
        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightButtons.setOpaque(false);

        JButton undoBtn = new JButton("Undo");
        JButton redoBtn = new JButton("Redo");
        JButton exportBtn = new JButton("Export"); // залишаємо "Export"

        styleButton(undoBtn, new Color(224, 224, 224));
        styleButton(redoBtn, new Color(224, 224, 224));
        styleButton(exportBtn, new Color(88, 101, 242));

        rightButtons.add(undoBtn);
        rightButtons.add(redoBtn);
        rightButtons.add(exportBtn);

        // ===== Tools =====
        JPanel toolsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        toolsPanel.setOpaque(false);

        JToggleButton selectToolBtn = new JToggleButton("Select");
        JToggleButton textToolBtn = new JToggleButton("Text");
        JToggleButton imageToolBtn = new JToggleButton("Image");
        JToggleButton penToolBtn = new JToggleButton("Pen");
        JToggleButton dashPenToolBtn = new JToggleButton("Dash pen");
        JToggleButton eraserToolBtn = new JToggleButton("Eraser");

        styleToggle(selectToolBtn);
        styleToggle(textToolBtn);
        styleToggle(imageToolBtn);
        styleToggle(penToolBtn);
        styleToggle(dashPenToolBtn);
        styleToggle(eraserToolBtn);

        ButtonGroup toolGroup = new ButtonGroup();
        toolGroup.add(selectToolBtn);
        toolGroup.add(textToolBtn);
        toolGroup.add(imageToolBtn);
        toolGroup.add(penToolBtn);
        toolGroup.add(dashPenToolBtn);
        toolGroup.add(eraserToolBtn);

        toolsPanel.add(selectToolBtn);
        toolsPanel.add(textToolBtn);
        toolsPanel.add(imageToolBtn);
        toolsPanel.add(penToolBtn);
        toolsPanel.add(dashPenToolBtn);
        toolsPanel.add(eraserToolBtn);

        // ===== Brush controls =====
        JPanel brushPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        brushPanel.setOpaque(false);

        JButton colorBtn = new JButton("Color");
        styleButton(colorBtn, new Color(245, 245, 245));

        Integer[] sizes = {2, 3, 4, 6, 8, 10};
        JComboBox<Integer> penSizeBox = new JComboBox<>(sizes);
        penSizeBox.setSelectedItem(3);

        brushPanel.add(colorBtn);
        brushPanel.add(new JLabel("Pen:"));
        brushPanel.add(penSizeBox);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(toolsPanel, BorderLayout.CENTER);
        center.add(brushPanel, BorderLayout.WEST);

        topBar.add(center, BorderLayout.CENTER);
        topBar.add(rightButtons, BorderLayout.EAST);

        mainPanel.add(topBar, BorderLayout.NORTH);

        // ===== Canvas =====
        canvasPanel = new CanvasPanel(mindMap, mindMapService, commandManager);
        mainPanel.add(canvasPanel, BorderLayout.CENTER);

        // ===== Handlers =====
        selectToolBtn.addActionListener(e -> canvasPanel.setTool(CanvasPanel.ToolType.SELECT));
        textToolBtn.addActionListener(e -> canvasPanel.setTool(CanvasPanel.ToolType.ADD_TEXT));
        imageToolBtn.addActionListener(e -> canvasPanel.setTool(CanvasPanel.ToolType.ADD_IMAGE));
        penToolBtn.addActionListener(e -> canvasPanel.setTool(CanvasPanel.ToolType.PEN));
        dashPenToolBtn.addActionListener(e -> canvasPanel.setTool(CanvasPanel.ToolType.DASH_PEN));
        eraserToolBtn.addActionListener(e -> canvasPanel.setTool(CanvasPanel.ToolType.ERASER));

        selectToolBtn.setSelected(true);
        canvasPanel.setTool(CanvasPanel.ToolType.SELECT);

        undoBtn.addActionListener(e -> { commandManager.undo(); canvasPanel.repaint(); });
        redoBtn.addActionListener(e -> { commandManager.redo(); canvasPanel.repaint(); });
        exportBtn.addActionListener(e -> onExport());

        colorBtn.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(this, "Choose pen color", canvasPanel.getBrushColor());
            if (chosen != null) canvasPanel.setBrushColor(chosen);
        });

        penSizeBox.addActionListener(e -> {
            Integer v = (Integer) penSizeBox.getSelectedItem();
            if (v != null) canvasPanel.setBrushWidth(v);
        });
    }

    private static void styleButton(AbstractButton b, Color bg) {
        b.setBackground(bg);
        b.setForeground(Color.BLACK);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private static void styleToggle(AbstractButton b) {
        styleButton(b, new Color(245, 245, 245));
    }

    private void onExport() {
        // тільки тут перейменували пункт TXT
        String[] options = {"PNG", "JPG", "PDF", "Statistics/Report (TXT)", "Cancel"};

        int choice = JOptionPane.showOptionDialog(
                this,
                "Choose export format:",
                "Export map",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );
        if (choice == -1 || "Cancel".equals(options[choice])) return;

        String selectedFormat = options[choice];

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save exported file");

        //  нормальне розширення для TXT-репорту
        String extension = selectedFormat.contains("TXT") ? "txt" : selectedFormat.toLowerCase();

        String base = mindMap.getTitle().replaceAll("\\s+", "_");
        if (selectedFormat.contains("TXT")) base += "_report";

        chooser.setSelectedFile(new File(base + "." + extension));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File targetFile = chooser.getSelectedFile();

        switch (selectedFormat) {
            case "PNG" -> exportService.setStrategy(new PngExportStrategy());
            case "JPG" -> exportService.setStrategy(new JpgExportStrategy());
            case "PDF" -> exportService.setStrategy(new PdfExportStrategy());
            case "Statistics/Report (TXT)" -> exportService.setStrategy(new TxtExportStrategy(canvasPanel.getElements()));
            default -> {
                JOptionPane.showMessageDialog(this, "Unsupported format");
                return;
            }
        }

        try {
            exportService.export(mindMap, canvasPanel, targetFile);
            JOptionPane.showMessageDialog(this, "Export successful:\n" + targetFile.getAbsolutePath());
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Unexpected error: " + ex.getMessage());
        }
    }

    private static class CanvasPanel extends JPanel {

        enum ToolType { SELECT, ADD_TEXT, ADD_IMAGE, PEN, DASH_PEN, ERASER }

        private final MindMap mindMap;
        private final MindMapService mindMapService;
        private final CommandManager commandManager;

        private final List<MapElement> elements = new ArrayList<>();
        private MapElement selectedElement = null;

        private final ToolFactory toolFactory;
        private final EditorContext ctx;
        private Tool currentTool;

        private final Map<String, BufferedImage> imageCache = new HashMap<>();
        private static final int PADDING = 10;

        public CanvasPanel(MindMap mindMap, MindMapService mindMapService, CommandManager commandManager) {
            this.mindMap = mindMap;
            this.mindMapService = mindMapService;
            this.commandManager = commandManager;

            setBackground(new Color(250, 250, 253));
            setFocusable(true);

            elements.addAll(mindMapService.getElementsForMap(mindMap));

            this.ctx = new EditorContext(this, mindMap, mindMapService, commandManager, elements) {
                @Override public void setSelected(MapElement el) { super.setSelected(el); selectedElement = el; }
                @Override public MapElement getSelected() { return selectedElement; }

                @Override
                public MapElement findElementAt(int x, int y) {
                    for (int i = elements.size() - 1; i >= 0; i--) {
                        MapElement el = elements.get(i);
                        Rectangle b = getElementBounds(el);
                        if (b.contains(x, y)) return el;
                    }
                    return null;
                }
            };

            // load strokes for map
            ctx.getDrawingLayer().getStrokes().addAll(mindMapService.getStrokesForMap(mindMap));

            this.toolFactory = new DefaultToolFactory();
            this.currentTool = toolFactory.createSelectTool(ctx);

            MouseAdapter mouseHandler = new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) { currentTool.onMousePressed(e); }
                @Override public void mouseDragged(MouseEvent e) { currentTool.onMouseDragged(e); }
                @Override public void mouseReleased(MouseEvent e) { currentTool.onMouseReleased(e); }

                @Override
                public void mouseClicked(MouseEvent e) {

                    if (currentTool instanceof SelectTool
                            && SwingUtilities.isLeftMouseButton(e)
                            && e.getClickCount() == 2) {

                        MapElement hit = ctx.findElementAt(e.getX(), e.getY());
                        if (hit instanceof TextNode textNode) {

                            String oldText = textNode.getTextForDisplay();
                            if (oldText == null) oldText = "";

                            String newText = JOptionPane.showInputDialog(CanvasPanel.this, "Edit text:", oldText);
                            if (newText == null) return;

                            textNode.setTextForDisplay(newText.trim());
                            mindMapService.updateElement(textNode);
                            repaint();
                        }
                        return;
                    }

                    currentTool.onMouseClicked(e);
                }
            };

            addMouseListener(mouseHandler);
            addMouseMotionListener(mouseHandler);

            setupKeyBindings();
        }

        // for Statistics/Report (TXT)
        public List<MapElement> getElements() {
            return elements;
        }

        public Color getBrushColor() { return ctx.getBrushColor(); }
        public void setBrushColor(Color c) { ctx.setBrushColor(c); }
        public void setBrushWidth(int px) { ctx.setBrushWidthPx(px); }

        public void setEraserRadius(int px) { ctx.setEraserRadiusPx(px); }

        public void setTool(ToolType type) {
            switch (type) {
                case SELECT -> currentTool = toolFactory.createSelectTool(ctx);
                case ADD_TEXT -> currentTool = toolFactory.createAddTextNodeTool(ctx);
                case ADD_IMAGE -> currentTool = toolFactory.createAddImageNodeTool(ctx);
                case PEN -> currentTool = toolFactory.createPenTool(ctx);
                case DASH_PEN -> currentTool = toolFactory.createDashedPenTool(ctx);
                case ERASER -> currentTool = toolFactory.createEraserTool(ctx);
            }
            requestFocusInWindow();
        }

        private void setupKeyBindings() {
            InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            ActionMap am = getActionMap();

            im.put(KeyStroke.getKeyStroke("DELETE"), "delete-selected");
            im.put(KeyStroke.getKeyStroke("BACK_SPACE"), "delete-selected");

            am.put("delete-selected", new AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    deleteSelectedElement();
                }
            });
        }

        private void deleteSelectedElement() {
            if (selectedElement == null) return;
            DeleteNodeCommand cmd = new DeleteNodeCommand(mindMap, mindMapService, elements, selectedElement);
            commandManager.executeCommand(cmd);
            selectedElement = null;
            repaint();
        }

        private Rectangle getElementBounds(MapElement el) {
            return new Rectangle((int) el.getX(), (int) el.getY(), el.getWidthPx(), el.getHeightPx());
        }

        private BufferedImage getCachedImage(String path) {
            if (path == null || path.isBlank()) return null;
            if (imageCache.containsKey(path)) return imageCache.get(path);
            try {
                BufferedImage img = ImageIO.read(new File(path));
                imageCache.put(path, img);
                return img;
            } catch (IOException ex) {
                imageCache.put(path, null);
                return null;
            }
        }

        private void paintAllStrokes(Graphics2D g2) {
            List<DrawingStroke> strokes = ctx.getDrawingLayer().getStrokes();
            if (strokes.isEmpty()) return;

            for (DrawingStroke s : strokes) {
                g2.setColor(s.getColor());
                g2.setStroke(s.buildAwtStroke());
                g2.draw(s.buildPath());
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for (MapElement el : elements) {
                Rectangle r = getElementBounds(el);

                // background
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);

                // content via Visitor
                MapElementContentRenderVisitor v =
                        new MapElementContentRenderVisitor(g2, r, PADDING, this::getCachedImage);
                el.accept(v);

                // border
                if (el == selectedElement) {
                    g2.setStroke(new BasicStroke(2f));
                    g2.setColor(new Color(255, 140, 0));
                } else {
                    g2.setStroke(new BasicStroke(1f));
                    g2.setColor(new Color(180, 180, 200));
                }
                g2.drawRoundRect(r.x, r.y, r.width, r.height, 12, 12);
            }

            // strokes over nodes
            paintAllStrokes(g2);

            // tool overlay
            currentTool.paintOverlay(g2);

            g2.dispose();
        }
    }
}
