package ru.itmo.marimiari.repository;

import ru.itmo.marimiari.config.DbConfig;
import ru.itmo.marimiari.domain.Container;
import ru.itmo.marimiari.domain.ContainerType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContainerRepository {
    public List<Container> findALL() {
        List<Container> list = new ArrayList<>();
        String sql = "SELECT c.*, u.login as owner_login FROM containers c JOIN users u ON c.owner_id = u.id";
        try (Connection connection = DbConfig.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Container c = new Container();
                c.setId(rs.getLong("id"));
                c.setName(rs.getString("name"));
                c.setType(ContainerType.valueOf(rs.getString("type")));
                c.setOwnerLogin(String.valueOf(rs.getLong("owner_id")));
                c.setCreatedAt(rs.getTimestamp("created_at").toInstant());
                c.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
                list.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Container insert(String name, ContainerType type, long ownerId) {
        String sql = "INSERT INTO containers (name, type, owner_id, created_at, updated_at) " +
                "VALUES (?, ?, ?, NOW(), NOW()) RETURNING id, created_at, updated_at";
        try (Connection connection = DbConfig.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, type.name());
            stmt.setLong(3, ownerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Container container = new Container();
                container.setId(rs.getLong("id"));
                container.setName(name);
                container.setType(type);
                container.setOwnerId(ownerId);
                container.setOwnerLogin(null);
                container.setCreatedAt(rs.getTimestamp("created_at").toInstant());
                container.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
                return container;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean update(long id, String name, ContainerType type, long ownerId) {
        String sql = "UPDATE containers SET name = ?, type = ?, updated_at = NOW() WHERE id = ? AND owner_id = ?";
        try (Connection connection = DbConfig.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, type.name());
            stmt.setLong(3, id);
            stmt.setLong(4, ownerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void delete(long id, long ownerId) {
        String sql = "DELETE FROM containers WHERE id = ? AND owner_id = ?";
        try (Connection connection = DbConfig.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setLong(2, ownerId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
