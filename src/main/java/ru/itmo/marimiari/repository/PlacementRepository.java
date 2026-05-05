package ru.itmo.marimiari.repository;

import ru.itmo.marimiari.config.DbConfig;
import ru.itmo.marimiari.domain.Placement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlacementRepository {
    public List<Placement> findAll() {
        List<Placement> list = new ArrayList<>();
        String sql = "SELECT p.*, u.login as owner_login FROM placements p JOIN users u ON p.owner_id = u.id";
        try (Connection connection = DbConfig.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Placement placement = new Placement();
                placement.setId(rs.getLong("id"));
                placement.setSampleId(rs.getLong("sample_id"));
                placement.setContainerId(rs.getLong("container_id"));
                placement.setSlotId(rs.getLong("slot_id"));
                placement.setPlacedAt(rs.getTimestamp("placed_at").toInstant());
                placement.setOwnerId(rs.getLong("owner_id"));
                placement.setOwnerLogin(rs.getString("owner_login"));
                list.add(placement);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Placement insert(long sampleId, long containerId, long slotId, long ownerId) {
        String sql = "INSERT INTO placements (sample_id, container_id, slot_id, placed_at, owner_id) " +
                "VALUES (?, ?, ?, NOW(), ?) RETURNING id, placed_at";
        try (Connection connection = DbConfig.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, sampleId);
            stmt.setLong(2, containerId);
            stmt.setLong(3, slotId);
            stmt.setLong(4, ownerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Placement placement = new Placement();
                placement.setId(rs.getLong("id"));
                placement.setSampleId(sampleId);
                placement.setContainerId(containerId);
                placement.setSlotId(slotId);
                placement.setPlacedAt(rs.getTimestamp("placed_at").toInstant());
                placement.setOwnerId(ownerId);
                return placement;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteBySampleId(long sampleId, long ownerId) {
        String sql = "DELETE FROM placements WHERE sample_id = ? AND owner_id = ?";
        try (Connection connection = DbConfig.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, sampleId);
            stmt.setLong(2, ownerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Placement findBySampleId(long sampleId) {
        String sql = "SELECT p.*, u.login as owner_login FROM placements p JOIN users u ON p.owner_id = u.id WHERE p.sample_id = ?";
        try (Connection connection = DbConfig.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, sampleId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Placement placement = new Placement();
                placement.setId(rs.getLong("id"));
                placement.setSampleId(rs.getLong("sample_id"));
                placement.setContainerId(rs.getLong("container_id"));
                placement.setSlotId(rs.getLong("slot_id"));
                placement.setPlacedAt(rs.getTimestamp("placed_at").toInstant());
                placement.setOwnerId(rs.getLong("owner_id"));
                placement.setOwnerLogin(rs.getString("owner_login"));
                return placement;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
