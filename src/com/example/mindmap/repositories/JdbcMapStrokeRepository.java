package com.example.mindmap.repositories;

import com.example.mindmap.db.JdbcConnectionProvider;
import com.example.mindmap.tools.drawing.DrawingStroke;

import java.awt.Color;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcMapStrokeRepository {

    private final JdbcConnectionProvider connectionProvider;

    public JdbcMapStrokeRepository(JdbcConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public List<DrawingStroke> findByMapId(int mapId) {
        String sql = """
            SELECT stroke_id, color_argb, width, dashed, points
            FROM MapStrokes
            WHERE map_id = ?
            ORDER BY created_at ASC
        """;

        List<DrawingStroke> result = new ArrayList<>();

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, mapId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String id = rs.getString("stroke_id");
                int argb = rs.getInt("color_argb");
                float width = rs.getFloat("width");
                boolean dashed = rs.getBoolean("dashed");
                String points = rs.getString("points");

                DrawingStroke stroke = new DrawingStroke(id, new Color(argb, true), width, dashed);
                DrawingStroke.deserializePointsInto(stroke, points);

                // пропускаємо “биті” мазки
                if (!stroke.isEmpty()) {
                    result.add(stroke);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error loading strokes", e);
        }

        return result;
    }

    public void save(int mapId, DrawingStroke stroke) {
        String sql = """
            INSERT INTO MapStrokes (stroke_id, map_id, color_argb, width, dashed, points)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, stroke.getId());
            ps.setInt(2, mapId);
            ps.setInt(3, stroke.getColor().getRGB());
            ps.setFloat(4, stroke.getWidthPx());
            ps.setBoolean(5, stroke.isDashed());
            ps.setString(6, stroke.serializePoints());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error saving stroke", e);
        }
    }

    public void deleteById(String strokeId) {
        String sql = "DELETE FROM MapStrokes WHERE stroke_id = ?";

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, strokeId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting stroke", e);
        }
    }
}
