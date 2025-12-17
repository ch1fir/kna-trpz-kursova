package com.example.mindmap.repositories;

import com.example.mindmap.db.JdbcConnectionProvider;
import com.example.mindmap.entities.ImageNode;
import com.example.mindmap.entities.MapElement;
import com.example.mindmap.entities.TextNode;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcMapElementRepository implements MapElementRepository {

    private final JdbcConnectionProvider connectionProvider;

    public JdbcMapElementRepository(JdbcConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public void save(MapElement element, int mapId) {
        String sql = """
            INSERT INTO MapElements
            (map_id, x_coord, y_coord, element_type, text_content, font_size, shape_type, image_url, width, height)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, mapId);
            ps.setFloat(2, element.getX());
            ps.setFloat(3, element.getY());
            ps.setString(4, element.getType()); // "TEXT" або "IMAGE"

            if (element instanceof TextNode text) {
                ps.setString(5, text.getTextContent());
                ps.setInt(6, text.getFontSize());
                ps.setString(7, text.getShapeType());

                ps.setNull(8, Types.VARCHAR); // image_url
            } else if (element instanceof ImageNode img) {
                ps.setNull(5, Types.LONGVARCHAR); // text_content
                ps.setNull(6, Types.INTEGER);     // font_size
                ps.setNull(7, Types.VARCHAR);     // shape_type

                ps.setString(8, img.getImageUrl());
            } else {
                ps.setNull(5, Types.LONGVARCHAR);
                ps.setNull(6, Types.INTEGER);
                ps.setNull(7, Types.VARCHAR);
                ps.setNull(8, Types.VARCHAR);
            }

            // width/height для будь-якого елемента
            ps.setInt(9, element.getWidthPx());
            ps.setInt(10, element.getHeightPx());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                element.setId(rs.getInt(1));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error saving map element", e);
        }
    }

    @Override
    public void update(MapElement element) {
        String sql = """
            UPDATE MapElements
            SET x_coord = ?, y_coord = ?,
                text_content = ?, font_size = ?, shape_type = ?,
                image_url = ?, width = ?, height = ?
            WHERE id = ?
        """;

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setFloat(1, element.getX());
            ps.setFloat(2, element.getY());

            if (element instanceof TextNode text) {
                ps.setString(3, text.getTextContent());
                ps.setInt(4, text.getFontSize());
                ps.setString(5, text.getShapeType());

                ps.setNull(6, Types.VARCHAR); // image_url
            } else if (element instanceof ImageNode img) {
                ps.setNull(3, Types.LONGVARCHAR);
                ps.setNull(4, Types.INTEGER);
                ps.setNull(5, Types.VARCHAR);

                ps.setString(6, img.getImageUrl());
            } else {
                ps.setNull(3, Types.LONGVARCHAR);
                ps.setNull(4, Types.INTEGER);
                ps.setNull(5, Types.VARCHAR);
                ps.setNull(6, Types.VARCHAR);
            }

            ps.setInt(7, element.getWidthPx());
            ps.setInt(8, element.getHeightPx());

            ps.setInt(9, element.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating map element", e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM MapElements WHERE id = ?";

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting map element", e);
        }
    }

    @Override
    public List<MapElement> findByMapId(int mapId) {
        String sql = """
            SELECT id, x_coord, y_coord, element_type,
                   text_content, font_size, shape_type,
                   image_url, width, height
            FROM MapElements
            WHERE map_id = ?
        """;

        List<MapElement> elements = new ArrayList<>();

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, mapId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String type = rs.getString("element_type");
                float x = rs.getFloat("x_coord");
                float y = rs.getFloat("y_coord");

                int width = rs.getInt("width");
                int height = rs.getInt("height");

                if ("TEXT".equalsIgnoreCase(type)) {
                    String text = rs.getString("text_content");
                    int fontSize = rs.getInt("font_size");
                    String shapeType = rs.getString("shape_type");

                    // Якщо в БД null -> rs.getInt дає 0, тому підстрахуємось:
                    if (fontSize <= 0) fontSize = 14;
                    if (shapeType == null || shapeType.isBlank()) shapeType = "RECT";
                    if (width <= 0) width = 140;
                    if (height <= 0) height = 45;

                    TextNode node = new TextNode(
                            id,
                            x,
                            y,
                            null,
                            text,
                            fontSize,
                            shapeType,
                            width,
                            height
                    );
                    elements.add(node);

                } else if ("IMAGE".equalsIgnoreCase(type)) {
                    String imageUrl = rs.getString("image_url");

                    if (width <= 0) width = 120;
                    if (height <= 0) height = 80;

                    ImageNode node = new ImageNode(
                            id,
                            x,
                            y,
                            null,
                            imageUrl,
                            width,
                            height
                    );
                    elements.add(node);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error loading map elements", e);
        }

        return elements;
    }
}
