package ru.itmo.marimiari.repository;

import ru.itmo.marimiari.config.DbConfig;
import ru.itmo.marimiari.user.User;
import java.sql.*;
import java.util.Optional;

public class UserRepository {
    public Optional<User> findByLogin(String login) {
        String sql = "SELECT id, login, password_hash FROM users WHERE login = ?";
        try (Connection connection = DbConfig.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setLogin(rs.getString("login"));
                user.setPasswordHash(rs.getString("password_hash"));
                return Optional.of(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public boolean register(String login, String passwordHash) {
        String sql = "INSERT INTO users (login, password_hash) VALUES (?, ?)";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, login);
            stmt.setString(2, passwordHash);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) {
                return false;
            }
            e.printStackTrace();
            return false;
        }
    }
}
