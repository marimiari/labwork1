package ru.itmo.marimiari.repository;

import ru.itmo.marimiari.config.DbConfig;
import ru.itmo.marimiari.domain.Sample;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SampleRepository {
    public List<Sample> findAll() {
        List<Sample> list = new ArrayList<>();
        String sql = "SELECT id, owner_id FROM samples";
        try (Connection connection = DbConfig.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Sample sample = new Sample();
                sample.setId(rs.getLong("id"));
                sample.setOwnerId(rs.getLong("owner_id"));
                list.add(sample);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Sample insert(long ownerId) {
        String sql = "INSERT INTO samples (owner_id) VALUES (?) RETURNING id";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, ownerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Sample sample = new Sample();
                sample.setId(rs.getLong(1));
                sample.setOwnerId(rs.getLong("owner_id"));
                return sample;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void delete(long id) {
        String sql = "DELETE FROM samples WHERE id = ?";
        try (Connection connection = DbConfig.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
