package com.example.mindmap.repositories;

import com.example.mindmap.db.JdbcConnectionProvider;
import com.example.mindmap.entities.Category;
import com.example.mindmap.entities.MindMap;
import com.example.mindmap.entities.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcMindMapRepository implements Repository<MindMap, Integer> {

    private final JdbcConnectionProvider connectionProvider;

    public JdbcMindMapRepository(JdbcConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public MindMap save(MindMap entity) {
        if (entity.getId() == 0) {
            String sql = "INSERT INTO MindMaps(title, description, created_at, is_favorite, preview_image, user_id, category_id) " +
                    "VALUES(?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = connectionProvider.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, entity.getTitle());
                ps.setString(2, entity.getDescription());
                ps.setTimestamp(3, Timestamp.valueOf(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now()));
                ps.setBoolean(4, entity.isFavorite());
                ps.setString(5, entity.getPreviewImage());
                ps.setInt(6, entity.getOwner().getId());

                Integer catId = (entity.getCategory() == null) ? null : entity.getCategory().getId();
                if (catId == null || catId == 0) ps.setNull(7, Types.INTEGER);
                else ps.setInt(7, catId);

                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) entity.setId(rs.getInt(1));
                }

                return entity;

            } catch (SQLException e) {
                throw new RuntimeException("Error inserting mindmap", e);
            }

        } else {
            String sql = "UPDATE MindMaps SET title = ?, description = ?, is_favorite = ?, preview_image = ?, category_id = ? WHERE id = ?";

            try (Connection conn = connectionProvider.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, entity.getTitle());
                ps.setString(2, entity.getDescription());
                ps.setBoolean(3, entity.isFavorite());
                ps.setString(4, entity.getPreviewImage());

                Integer catId = (entity.getCategory() == null) ? null : entity.getCategory().getId();
                if (catId == null || catId == 0) ps.setNull(5, Types.INTEGER);
                else ps.setInt(5, catId);

                ps.setInt(6, entity.getId());
                ps.executeUpdate();

                return entity;

            } catch (SQLException e) {
                throw new RuntimeException("Error updating mindmap", e);
            }
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM MindMaps WHERE id = ?";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting mindmap", e);
        }
    }

    @Override
    public Optional<MindMap> getById(Integer id) {
        String sql = """
            SELECT m.id, m.title, m.description, m.created_at, m.is_favorite, m.preview_image, m.user_id, m.category_id,
                   c.title AS category_title, c.color AS category_color
            FROM MindMaps m
            LEFT JOIN Categories c ON m.category_id = c.id
            WHERE m.id = ?
        """;

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                MindMap map = new MindMap();
                map.setId(rs.getInt("id"));
                map.setTitle(rs.getString("title"));
                map.setDescription(rs.getString("description"));
                map.setOwner(new User(rs.getInt("user_id"), null, null));

                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) map.setCreatedAt(ts.toLocalDateTime());

                map.setPreviewImage(rs.getString("preview_image"));

                if (rs.getBoolean("is_favorite")) map.markAsFavorite();
                else map.unmarkFavorite();

                int catId = rs.getInt("category_id");
                if (!rs.wasNull() && catId != 0) {
                    Category cat = new Category();
                    cat.setId(catId);
                    cat.setTitle(rs.getString("category_title"));
                    cat.setColor(rs.getString("category_color"));
                    cat.setOwner(map.getOwner());
                    map.setCategory(cat);
                }

                return Optional.of(map);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error loading mindmap by id", e);
        }
    }

    @Override
    public List<MindMap> getAll() {
        throw new UnsupportedOperationException("Not needed");
    }

    public List<MindMap> getAllByUser(User user) {
        String sql = """
            SELECT m.id, m.title, m.description, m.created_at, m.is_favorite, m.preview_image, m.category_id,
                   c.title AS category_title, c.color AS category_color
            FROM MindMaps m
            LEFT JOIN Categories c ON m.category_id = c.id
            WHERE m.user_id = ?
        """;

        List<MindMap> maps = new ArrayList<>();

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, user.getId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MindMap map = new MindMap();
                    map.setId(rs.getInt("id"));
                    map.setTitle(rs.getString("title"));
                    map.setDescription(rs.getString("description"));
                    map.setOwner(user);

                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) map.setCreatedAt(ts.toLocalDateTime());

                    map.setPreviewImage(rs.getString("preview_image"));

                    if (rs.getBoolean("is_favorite")) map.markAsFavorite();
                    else map.unmarkFavorite();

                    int catId = rs.getInt("category_id");
                    if (!rs.wasNull() && catId != 0) {
                        Category cat = new Category();
                        cat.setId(catId);
                        cat.setTitle(rs.getString("category_title"));
                        cat.setColor(rs.getString("category_color"));
                        cat.setOwner(user);
                        map.setCategory(cat);
                    }

                    maps.add(map);
                }
            }

            return maps;

        } catch (SQLException e) {
            throw new RuntimeException("Error loading mindmaps by user", e);
        }
    }
}
