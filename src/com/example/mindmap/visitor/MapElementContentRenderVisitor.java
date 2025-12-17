package com.example.mindmap.visitor;

import com.example.mindmap.entities.ImageNode;
import com.example.mindmap.entities.TextNode;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class MapElementContentRenderVisitor implements MapElementVisitor {

    @FunctionalInterface
    public interface ImageProvider {
        BufferedImage get(String path);
    }

    private final Graphics2D g2;
    private final Rectangle bounds;
    private final int padding;
    private final ImageProvider imageProvider;

    public MapElementContentRenderVisitor(
            Graphics2D g2,
            Rectangle bounds,
            int padding,
            ImageProvider imageProvider
    ) {
        this.g2 = g2;
        this.bounds = bounds;
        this.padding = padding;
        this.imageProvider = imageProvider;
    }

    @Override
    public void visitImageNode(ImageNode imgNode) {
        String path = imgNode.getImageUrl();
        BufferedImage img = (path == null) ? null : imageProvider.get(path);

        if (img != null) {
            g2.drawImage(img, bounds.x, bounds.y, bounds.width, bounds.height, null);
            return;
        }

        // fallback “Image not found”
        g2.setColor(new Color(200, 60, 60));
        g2.drawLine(bounds.x + 6, bounds.y + 6, bounds.x + bounds.width - 6, bounds.y + bounds.height - 6);
        g2.drawLine(bounds.x + bounds.width - 6, bounds.y + 6, bounds.x + 6, bounds.y + bounds.height - 6);
        g2.setColor(new Color(80, 80, 80));
        g2.drawString("Image not found", bounds.x + 10, bounds.y + 20);
    }

    @Override
    public void visitTextNode(TextNode textNode) {
        String text = textNode.getTextForDisplay();
        if (text == null) text = "";

        g2.setColor(new Color(40, 40, 60));
        FontMetrics fm = g2.getFontMetrics();

        int maxTextWidth = Math.max(20, bounds.width - padding * 2);
        List<String> lines = wrapText(text, fm, maxTextWidth);

        // AUTO-HEIGHT (як у вас зараз у CanvasPanel) :contentReference[oaicite:1]{index=1}
        int neededH = Math.max(45, lines.size() * fm.getHeight() + padding * 2);
        if (textNode.getHeightPx() < neededH) {
            textNode.setHeightPx(neededH);
        }

        int y = bounds.y + padding + fm.getAscent();
        for (String line : lines) {
            g2.drawString(line, bounds.x + padding, y);
            y += fm.getHeight();
            if (y > bounds.y + bounds.height - padding) break;
        }
    }

    private List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null) return lines;

        String trimmed = text.trim();
        if (trimmed.isEmpty()) return lines;

        String[] words = trimmed.split("\\s+");
        StringBuilder line = new StringBuilder();

        for (String w : words) {
            if (line.length() == 0) {
                line.append(w);
                continue;
            }
            String test = line + " " + w;
            if (fm.stringWidth(test) <= maxWidth) {
                line.append(" ").append(w);
            } else {
                lines.add(line.toString());
                line = new StringBuilder(w);
            }
        }
        if (line.length() > 0) lines.add(line.toString());
        if (lines.isEmpty() && !trimmed.isEmpty()) lines.add(trimmed);
        return lines;
    }
}
