package com.reservation.dao;

import com.reservation.db.DatabaseManager;
import com.reservation.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/** Data access for the {@code users} table. */
public class UserDAO {

    /**
     * Checks a username / password-hash pair.
     *
     * @param username     the entered username
     * @param passwordHash SHA-256 hex digest of the entered password
     * @return the user if the credentials match, otherwise empty
     * @throws SQLException on database failure
     */
    public Optional<User> authenticate(String username, String passwordHash) throws SQLException {
        String sql = "SELECT user_id, username FROM users WHERE username = ? AND password_hash = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new User(rs.getInt("user_id"), rs.getString("username")));
                }
                return Optional.empty();
            }
        }
    }
}
