package com.example.mindmap.repositories;

import com.example.mindmap.db.JdbcConnectionProvider;
import com.example.mindmap.entities.Category;
import com.example.mindmap.entities.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcCategoryRepository implements Repository<Category, Integer> {

    private final JdbcConnectionProvider connectionProvider;

    public JdbcCategoryRepository(JdbcConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public Category save(Category entity) {
        if (entity.getId() == 0) {
            String sql = "INSERT INTO Categories(title, color, user_id) VALUES(?, ?, ?)";
            try (Connection conn = connectionProvider.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, entity.getTitle());
                ps.setString(2, entity.getColor());
                ps.setInt(3, entity.getOwner().getId());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) entity.setId(rs.getInt(1));
                }
                return entity;

            } catch (SQLException e) {
                throw new RuntimeException("Error inserting category", e);
            }
        } else {
            String sql = "UPDATE Categories SET title = ?, color = ? WHERE id = ? AND user_id = ?";
            try (Connection conn = connectionProvider.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, entity.getTitle());
                ps.setString(2, entity.getColor());
                ps.setInt(3, entity.getId());
                ps.setInt(4, entity.getOwner().getId());
                ps.executeUpdate();

                return entity;

            } catch (SQLException e) {
                throw new RuntimeException("Error updating category", e);
            }
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM Categories WHERE id = ?";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting category", e);
        }
    }

    @Override
    public Optional<Category> getById(Integer id) {
        String sql = "SELECT id, title, color, user_id FROM Categories WHERE id = ?";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                Category c = new Category();
                c.setId(rs.getInt("id"));
                c.setTitle(rs.getString("title"));
                c.setColor(rs.getString("color"));
                c.setOwner(new User(rs.getInt("user_id"), null, null));
                return Optional.of(c);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error loading category", e);
        }
    }

    @Override
    public List<Category> getAll() {
        throw new UnsupportedOperationException("Not needed");
    }

    public List<Category> getAllByUser(User user) {
        String sql = "SELECT id, title, color, user_id FROM Categories WHERE user_id = ? ORDER BY title ASC";
        List<Category> result = new ArrayList<>();

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, user.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Category c = new Category();
                    c.setId(rs.getInt("id"));
                    c.setTitle(rs.getString("title"));
                    c.setColor(rs.getString("color"));
                    c.setOwner(user);
                    result.add(c);
                }
            }
            return result;

        } catch (SQLException e) {
            throw new RuntimeException("Error loading categories by user", e);
        }
    }
}
