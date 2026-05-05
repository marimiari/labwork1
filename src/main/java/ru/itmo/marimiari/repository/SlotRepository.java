package ru.itmo.marimiari.repository;

import ru.itmo.marimiari.config.DbConfig;
import ru.itmo.marimiari.domain.Slot;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SlotRepository {
    public List<Slot> findAll() {
        List<Slot> list = new ArrayList<>();
        String sql = "SELECT s.*, u.login as owner_login FROM slots s JOIN users u ON s.owner_id = u.id";
        try (Connection connection = DbConfig.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Slot slot = new Slot();
                slot.setId(rs.getLong("id"));
                slot.setContainerId(rs.getLong("container_id"));
                slot.setCode(rs.getString("code"));
                slot.setOccupied(rs.getBoolean("occupied"));
                slot.setCreatedAt(rs.getTimestamp("created_at").toInstant());
                slot.setOwnerId(rs.getLong("owner_id"));
                slot.setOwnerLogin(rs.getString("owner_login"));
                list.add(slot);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Slot> findByContainerId(long containerId) {
        List<Slot> list = new ArrayList<>();
        String sql = "SELECT s.*, u.login as owner_login FROM slots s JOIN users u ON s.owner_id = u.id WHERE s.container_id = ?";
        try (Connection connection = DbConfig.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, containerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Slot slot = new Slot();
                slot.setId(rs.getLong("id"));
                slot.setContainerId(rs.getLong("container_id"));
                slot.setCode(rs.getString("code"));
                slot.setOccupied(rs.getBoolean("occupied"));
                slot.setCreatedAt(rs.getTimestamp("created_at").toInstant());
                slot.setOwnerId(rs.getLong("owner_id"));
                slot.setOwnerLogin(rs.getString("owner_login"));
                list.add(slot);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Slot insert(long containerId, String code, boolean occupied, long ownerId) {
        String sql = "INSERT INTO slots (container_id, code, occupied, created_at, owner_id) VALUES (?, ?, ?, NOW(), ?) RETURNING id, created_at";
        try (Connection connection = DbConfig.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, containerId);
            stmt.setString(2, code);
            stmt.setBoolean(3, occupied);
            stmt.setLong(4, ownerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Slot slot = new Slot();
                slot.setId(rs.getLong("id"));
                slot.setContainerId(containerId);
                slot.setCode(code);
                slot.setOccupied(occupied);
                slot.setCreatedAt(rs.getTimestamp("created_at").toInstant());
                slot.setOwnerId(ownerId);
                return slot;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateOccupied(long slotId, boolean occupied, long ownerId) {
        String sql = "UPDATE slots SET occupied = ? WHERE id = ? AND owner_id = ?";
        try (Connection connection = DbConfig.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBoolean(1, occupied);
            stmt.setLong(2, slotId);
            stmt.setLong(3, ownerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void delete(long id, long ownerId) {
        String sql = "DELETE FROM slots WHERE id = ? AND owner_id = ?";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setLong(2, ownerId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
