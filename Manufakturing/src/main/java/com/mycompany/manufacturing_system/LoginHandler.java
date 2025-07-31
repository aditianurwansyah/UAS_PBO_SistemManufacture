package com.mycompany.manufacturing_system;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Enhanced login handler with improved security features
 * Includes password hashing, account lockout, and audit logging
 */
public class LoginHandler {
    private Connection connection;
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
    );

    public LoginHandler() throws SQLException {
        connection = DatabaseConnection.getConnection();
    }

    /**
     * Enhanced authentication with security features
     */
    public User authenticate(String username, String password) {
        // Input validation
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            logSecurityEvent(username, "LOGIN_ATTEMPT", false, "Empty credentials");
            return null;
        }
        
        // Sanitize input
        username = sanitizeInput(username);
        
        // Check if account is locked
        if (isAccountLocked(username)) {
            logSecurityEvent(username, "LOGIN_BLOCKED", false, "Account locked due to multiple failed attempts");
            return null;
        }
        
        String query = "SELECT u.user_id, u.username, u.password, r.role_name, u.full_name, u.email, u.department, u.is_active " +
                       "FROM users u JOIN roles r ON u.role_id = r.role_id " +
                       "WHERE u.username = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedPasswordHash = rs.getString("password_hash");
                
                // Verify password
                if (verifyPassword(password, storedPasswordHash)) {
                    // Successful login
                    User user = createUserFromResultSet(rs);
                    
                    // Reset failed attempts and update last login
                    resetFailedAttempts(username);
                    updateLastLogin(username);
                    
                    // Log successful login
                    logSecurityEvent(username, "LOGIN_SUCCESS", true, "User logged in successfully");
                    logUserActivity(user.getUserId(), "LOGIN", "User logged in successfully");
                    
                    return user;
                } else {
                    // Failed login - increment failed attempts
                    incrementFailedAttempts(username);
                    logSecurityEvent(username, "LOGIN_FAILED", false, "Invalid password");
                }
            } else {
                logSecurityEvent(username, "LOGIN_FAILED", false, "Username not found");
            }
        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
            e.printStackTrace();
            logSecurityEvent(username, "LOGIN_ERROR", false, "Database error during authentication");
        }
        return null;
    }

    /**
     * Create User object from ResultSet
     */
    private User createUserFromResultSet(ResultSet rs) throws SQLException {
        String role = rs.getString("role_name");
        User user = new User(rs.getString("username"), "", role);
        user.setUserId(rs.getInt("user_id"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setDepartment(rs.getString("department"));
        user.setActive(rs.getBoolean("is_active"));
        return user;
    }

    /**
     * Hash password using SHA-256 with salt
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update("ManufacturingSalt2024".getBytes());
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (Exception e) {
            System.err.println("Error hashing password: " + e.getMessage());
            return password; // Fallback - not recommended for production
        }
    }

    /**
     * Verify password against hash
     */
    private boolean verifyPassword(String password, String hashedPassword) {
        if (hashedPassword == null || hashedPassword.isEmpty()) {
            // Handle legacy plain text passwords (for migration)
            return password.equals(hashedPassword);
        }
        return hashPassword(password).equals(hashedPassword);
    }

    /**
     * Check if account is locked due to failed attempts
     */
    private boolean isAccountLocked(String username) {
        String query = """
            SELECT account_locked_until, failed_login_attempts 
            FROM users 
            WHERE username = ?
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Timestamp lockedUntil = rs.getTimestamp("account_locked_until");
                int failedAttempts = rs.getInt("failed_login_attempts");
                
                // Check if account is currently locked
                if (lockedUntil != null && lockedUntil.after(new Timestamp(System.currentTimeMillis()))) {
                    return true;
                }
                
                // Auto-unlock if lockout period has expired
                if (lockedUntil != null && lockedUntil.before(new Timestamp(System.currentTimeMillis()))) {
                    unlockAccount(username);
                }
                
                return failedAttempts >= MAX_LOGIN_ATTEMPTS;
            }
        } catch (SQLException e) {
            System.err.println("Error checking account lock status: " + e.getMessage());
        }
        return false;
    }

    /**
     * Increment failed login attempts and lock account if necessary
     */
    private void incrementFailedAttempts(String username) {
        String updateQuery = """
            UPDATE users 
            SET failed_login_attempts = failed_login_attempts + 1,
                account_locked_until = CASE 
                    WHEN failed_login_attempts + 1 >= ? THEN DATE_ADD(NOW(), INTERVAL ? MINUTE)
                    ELSE account_locked_until 
                END,
                updated_at = CURRENT_TIMESTAMP
            WHERE username = ?
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
            stmt.setInt(1, MAX_LOGIN_ATTEMPTS);
            stmt.setInt(2, LOCKOUT_DURATION_MINUTES);
            stmt.setString(3, username);
            stmt.executeUpdate();
            
            // Check if account was just locked
            if (getFailedAttempts(username) >= MAX_LOGIN_ATTEMPTS) {
                logSecurityEvent(username, "ACCOUNT_LOCKED", false, 
                    "Account locked due to " + MAX_LOGIN_ATTEMPTS + " failed login attempts");
            }
        } catch (SQLException e) {
            System.err.println("Error incrementing failed attempts: " + e.getMessage());
        }
    }

    /**
     * Reset failed login attempts
     */
    private void resetFailedAttempts(String username) {
        String query = """
            UPDATE users 
            SET failed_login_attempts = 0, 
                account_locked_until = NULL,
                updated_at = CURRENT_TIMESTAMP
            WHERE username = ?
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error resetting failed attempts: " + e.getMessage());
        }
    }

    /**
     * Get current failed attempts count
     */
    private int getFailedAttempts(String username) {
        String query = "SELECT failed_login_attempts FROM users WHERE username = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("failed_login_attempts");
            }
        } catch (SQLException e) {
            System.err.println("Error getting failed attempts: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Unlock account manually
     */
    private void unlockAccount(String username) {
        resetFailedAttempts(username);
        logSecurityEvent(username, "ACCOUNT_UNLOCKED", true, "Account automatically unlocked after lockout period");
    }

    /**
     * Update last login timestamp
     */
    private void updateLastLogin(String username) {
        String query = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE username = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating last login: " + e.getMessage());
        }
    }

    /**
     * Enhanced user registration with validation
     */
    public boolean registerUser(User user) {
        // Enhanced validation
        if (!isValidRegistrationData(user)) {
            return false;
        }
        
        // Check if username already exists
        if (usernameExists(user.getUsername())) {
            logSecurityEvent(user.getUsername(), "REGISTRATION_FAILED", false, "Username already exists");
            return false;
        }
        
        String query = "INSERT INTO users (username, password, role_id, full_name, email, department, is_active) " +
                       "VALUES (?, ?, (SELECT role_id FROM roles WHERE role_name = ?), ?, ?, ?, ?)";
            
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, hashPassword(user.getPassword()));
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getDepartment());
            stmt.setString(6, ""); // phone - can be added to User class
            stmt.setBoolean(7, user.isActive());
            stmt.setString(8, user.getRole());

            int result = stmt.executeUpdate();

            if (result > 0) {
                System.out.println("User registered successfully: " + user.getUsername());
                logSecurityEvent(user.getUsername(), "USER_REGISTERED", true, "New user account created");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            logSecurityEvent(user.getUsername(), "REGISTRATION_ERROR", false, "Database error during registration");
        }
        return false;
    }

    /**
     * Validate registration data
     */
    private boolean isValidRegistrationData(User user) {
        if (user == null) return false;
        
        // Username validation
        if (user.getUsername() == null || user.getUsername().trim().length() < 3 || 
            user.getUsername().length() > 50 || !user.getUsername().matches("^[a-zA-Z0-9_]+$")) {
            return false;
        }
        
        // Password validation
        if (user.getPassword() == null || user.getPassword().length() < 8) {
            return false;
        }
        
        // Email validation (if provided)
        if (user.getEmail() != null && !user.getEmail().isEmpty() && 
            !EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            return false;
        }
        
        // Role validation
        if (user.getRole() == null || (!user.getRole().equals("ADMIN") && !user.getRole().equals("USER") && 
            !user.getRole().equals("SUPERVISOR") && !user.getRole().equals("OPERATOR"))) {
            return false;
        }
        
        return true;
    }

    /**
     * Enhanced password update with security checks
     */
    public boolean updatePassword(String username, String newPassword) {
        // Validate new password
        if (newPassword == null || newPassword.length() < 8) {
            logSecurityEvent(username, "PASSWORD_CHANGE_FAILED", false, "Password too weak");
            return false;
        }
        
        String query = """
            UPDATE users 
            SET password_hash = ?, 
                password_changed_at = CURRENT_TIMESTAMP,
                updated_at = CURRENT_TIMESTAMP 
            WHERE username = ?
            """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, hashPassword(newPassword));
            stmt.setString(2, username);

            int result = stmt.executeUpdate();

            if (result > 0) {
                int userId = getUserIdByUsername(username);
                if (userId > 0) {
                    logUserActivity(userId, "PASSWORD_CHANGE", "Password updated successfully");
                    logSecurityEvent(username, "PASSWORD_CHANGED", true, "Password updated by user");
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Password update error: " + e.getMessage());
            e.printStackTrace();
            logSecurityEvent(username, "PASSWORD_CHANGE_ERROR", false, "Database error during password update");
        }
        return false;
    }

    /**
     * Check if username already exists
     */
    public boolean usernameExists(String username) {
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Username check error: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get user details by username with enhanced fields
     */
    public User getUserByUsername(String username) {
        String query = "SELECT u.user_id, u.username, u.password, r.role_name, u.full_name, u.email, u.department, u.is_active " +
                       "FROM users u JOIN roles r ON u.role_id = r.role_id " +
                       "WHERE u.username = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return createUserFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Get user error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Enhanced role validation with permission checking
     */
    public boolean validateRoleAccess(String username, String requiredRole) {
        String query = """
            SELECT r.role_name, r.permissions
            FROM users u 
            JOIN roles r ON u.role_id = r.role_id 
            WHERE u.username = ? AND u.is_active = TRUE
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String userRole = rs.getString("role_name");
                // Admin has access to everything
                if ("ADMIN".equals(userRole)) {
                    return true;
                }
                return requiredRole.equals(userRole);
            }
            
        } catch (SQLException e) {
            System.err.println("Role validation error: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get user ID by username
     */
    private int getUserIdByUsername(String username) {
        String query = "SELECT user_id FROM users WHERE username = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("user_id");
            }
            
        } catch (SQLException e) {
            System.err.println("Get user ID error: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Enhanced user activity logging
     */
    private void logUserActivity(int userId, String activity, String description) {
        // Create user_activity_log table if not exists
        String createLogTable = """
            CREATE TABLE IF NOT EXISTS user_activity_log (
                log_id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                activity VARCHAR(50) NOT NULL,
                description TEXT,
                ip_address VARCHAR(45),
                user_agent TEXT,
                session_id VARCHAR(100),
                timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(user_id),
                INDEX idx_user_id (user_id),
                INDEX idx_timestamp (timestamp)
            )
            """;
        
        String insertLog = """
            INSERT INTO user_activity_log (user_id, activity, description) 
            VALUES (?, ?, ?)
            """;
        
        try (PreparedStatement createStmt = connection.prepareStatement(createLogTable);
             PreparedStatement insertStmt = connection.prepareStatement(insertLog)) {
            
            createStmt.execute();
            
            insertStmt.setInt(1, userId);
            insertStmt.setString(2, activity);
            insertStmt.setString(3, description);
            insertStmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Activity logging error: " + e.getMessage());
        }
    }

    /**
     * Security event logging for audit trail
     */
    private void logSecurityEvent(String username, String event, boolean success, String details) {
        String insertLog = """
            INSERT INTO audit_log (username, action, table_name, new_values, timestamp) 
            VALUES (?, ?, 'users', JSON_OBJECT('success', ?, 'details', ?), CURRENT_TIMESTAMP)
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(insertLog)) {
            stmt.setString(1, username != null ? username : "unknown");
            stmt.setString(2, event);
            stmt.setBoolean(3, success);
            stmt.setString(4, details);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Security logging error: " + e.getMessage());
        }
    }

    /**
     * Sanitize input to prevent SQL injection
     */
    private String sanitizeInput(String input) {
        if (input == null) return null;
        return input.replaceAll("[';\"\\\\]", "").trim();
    }

    /**
     * Get enhanced system statistics
     */
    public int[] getSystemStatistics() {
        String query = """
            SELECT 
                COUNT(*) as total_users,
                SUM(CASE WHEN is_active = TRUE THEN 1 ELSE 0 END) as active_users,
                SUM(CASE WHEN r.role_name = 'ADMIN' THEN 1 ELSE 0 END) as admin_count,
                SUM(CASE WHEN r.role_name = 'USER' THEN 1 ELSE 0 END) as user_count,
                SUM(CASE WHEN r.role_name = 'SUPERVISOR' THEN 1 ELSE 0 END) as supervisor_count,
                SUM(CASE WHEN r.role_name = 'OPERATOR' THEN 1 ELSE 0 END) as operator_count,
                SUM(CASE WHEN account_locked_until > NOW() THEN 1 ELSE 0 END) as locked_accounts
            FROM users u 
            JOIN roles r ON u.role_id = r.role_id
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new int[] {
                    rs.getInt("total_users"),
                    rs.getInt("active_users"),
                    rs.getInt("admin_count"),
                    rs.getInt("user_count"),
                    rs.getInt("supervisor_count"),
                    rs.getInt("operator_count"),
                    rs.getInt("locked_accounts")
                };
            }
            
        } catch (SQLException e) {
            System.err.println("Statistics error: " + e.getMessage());
        }
        return new int[]{0, 0, 0, 0, 0, 0, 0};
    }

    /**
     * Force unlock account (admin function)
     */
    public boolean forceUnlockAccount(String username) {
        resetFailedAttempts(username);
        logSecurityEvent(username, "ACCOUNT_FORCE_UNLOCKED", true, "Account manually unlocked by administrator");
        return true;
    }

    /**
     * Check password expiry (for future implementation)
     */
    public boolean isPasswordExpired(String username) {
        // Placeholder for password expiry check
        // Could check if password_changed_at is older than X days
        return false;
    }
}