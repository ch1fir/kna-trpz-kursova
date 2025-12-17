package com.example.mindmap.tools;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public interface Tool {
    default void onMousePressed(MouseEvent e) {}
    default void onMouseDragged(MouseEvent e) {}
    default void onMouseReleased(MouseEvent e) {}
    default void onMouseClicked(MouseEvent e) {}
    default void onKeyPressed(KeyEvent e) {}

    default void paintOverlay(Graphics2D g2) {}
}
