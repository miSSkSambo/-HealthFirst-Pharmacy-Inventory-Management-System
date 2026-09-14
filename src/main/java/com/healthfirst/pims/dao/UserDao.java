package com.healthfirst.pims.dao;

import com.healthfirst.pims.config.Database;
import com.healthfirst.pims.model.User;
import com.healthfirst.pims.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Encapsulates every SQL operation for application users. */
public final class UserDao {
    public Optional<User> authenticate(String username, String plainPassword) throws SQLException {
        String sql = "SELECT user_id, username, role, full_name FROM users WHERE username = ? AND password = ?";
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            ps.setString(2, PasswordUtil.sha256(plainPassword));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(
                "SELECT user_id, username, role, full_name FROM users ORDER BY username"); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) users.add(map(rs));
        }
        return users;
    }

    public void create(String username, String fullName, String role, String plainPassword) throws SQLException {
        String sql = "INSERT INTO users(username, password, role, full_name) VALUES (?, ?, ?, ?)";
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            ps.setString(2, PasswordUtil.sha256(plainPassword));
            ps.setString(3, role);
            ps.setString(4, fullName.trim());
            ps.executeUpdate();
        }
    }

    /** Blank password retains the user's existing password during an edit. */
    public void update(int id, String username, String fullName, String role, String plainPassword) throws SQLException {
        boolean changePassword = plainPassword != null && !plainPassword.isBlank();
        String sql = changePassword
                ? "UPDATE users SET username=?, full_name=?, role=?, password=? WHERE user_id=?"
                : "UPDATE users SET username=?, full_name=?, role=? WHERE user_id=?";
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username.trim()); ps.setString(2, fullName.trim()); ps.setString(3, role);
            if (changePassword) { ps.setString(4, PasswordUtil.sha256(plainPassword)); ps.setInt(5, id); }
            else ps.setInt(4, id);
            if (ps.executeUpdate() != 1) throw new SQLException("User record was not found.");
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM users WHERE user_id=?")) {
            ps.setInt(1, id);
            if (ps.executeUpdate() != 1) throw new SQLException("User record was not found.");
        }
    }

    private User map(ResultSet rs) throws SQLException {
        return new User(rs.getInt("user_id"), rs.getString("username"), rs.getString("role"), rs.getString("full_name"));
    }
}
