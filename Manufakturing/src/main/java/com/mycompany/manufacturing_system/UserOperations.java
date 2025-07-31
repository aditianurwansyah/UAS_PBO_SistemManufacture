package com.mycompany.manufacturing_system;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserOperations {

    // Tidak ada password hashing, penyimpanan langsung.
    public boolean createUser(User user) {
        String sql = "INSERT INTO users(username, password, role, full_name, email, department, is_active) VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword()); // Simpan password langsung
            pstmt.setString(3, user.getRole());
            pstmt.setString(4, user.getFullName());
            pstmt.setString(5, user.getEmail());
            pstmt.setString(6, user.getDepartment());
            pstmt.setBoolean(7, user.isActive());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
            return false;
        }
    }

    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"), // Ambil password langsung
                        rs.getString("role"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("department"),
                        rs.getBoolean("is_active")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by username: " + e.getMessage());
        }
        return null;
    }

    public boolean updateUser(String username, String fullName, String email, String department) {
        String sql = "UPDATE users SET full_name = ?, email = ?, department = ? WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, fullName);
            pstmt.setString(2, email);
            pstmt.setString(3, department);
            pstmt.setString(4, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            return false;
        }
    }

    public boolean changeUserRole(String username, String newRole) {
        String sql = "UPDATE users SET role = ? WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newRole);
            pstmt.setString(2, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error changing user role: " + e.getMessage());
            return false;
        }
    }

    public boolean toggleUserStatus(String username, boolean newStatus) {
        String sql = "UPDATE users SET is_active = ? WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, newStatus);
            pstmt.setString(2, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error toggling user status: " + e.getMessage());
            return false;
        }
    }

    public boolean resetUserPassword(String username, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newPassword); // Simpan password baru langsung
            pstmt.setString(2, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error resetting user password: " + e.getMessage());
            return false;
        }
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"), // Ambil password langsung
                        rs.getString("role"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("department"),
                        rs.getBoolean("is_active")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all users: " + e.getMessage());
        }
        return users;
    }

    public static UserStatistics getUserStatistics() {
        int totalUsers = 0;
        int activeUsers = 0;
        int adminCount = 0;
        int userCount = 0;

        String sqlTotal = "SELECT COUNT(*) FROM users";
        String sqlActive = "SELECT COUNT(*) FROM users WHERE is_active = TRUE";
        String sqlAdmin = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'";
        String sqlUser = "SELECT COUNT(*) FROM users WHERE role = 'USER'";

        try (Connection conn = DatabaseConnection.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(sqlTotal);
                if (rs.next()) {
                    totalUsers = rs.getInt(1);
                }
            }
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(sqlActive);
                if (rs.next()) {
                    activeUsers = rs.getInt(1);
                }
            }
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(sqlAdmin);
                if (rs.next()) {
                    adminCount = rs.getInt(1);
                }
            }
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(sqlUser);
                if (rs.next()) {
                    userCount = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting user statistics: " + e.getMessage());
        }
        return new UserStatistics(totalUsers, activeUsers, adminCount, userCount);
    }
}