package com.mycompany.manufacturing_system;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Penangan login dimodifikasi untuk menggunakan kata sandi teks biasa
 */
public class LoginHandler {
    private Connection connection;
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
    );

    public LoginHandler() throws SQLException {
        // Mendapatkan koneksi dari kelas DatabaseConnection
        connection = DatabaseConnection.getConnection();
    }

    /**
     * Mengotentikasi pengguna dengan kata sandi teks biasa
     * @param username Nama pengguna untuk diotentikasi
     * @param password Kata sandi teks biasa
     * @return Objek Pengguna jika otentikasi berhasil, null jika tidak
     */
    public User authenticate(String username, String password) {
        // Validasi masukan
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            logSecurityEvent(username, "LOGIN_ATTEMPT", false, "Empty credentials");
            return null;
        }
        
        // Sanitasi masukan
        username = sanitizeInput(username);
        
        // Periksa apakah akun terkunci
        if (isAccountLocked(username)) {
            logSecurityEvent(username, "LOGIN_BLOCKED", false, "Account locked due to multiple failed attempts");
            return null;
        }
        
        // Kueri SQL untuk mengambil detail pengguna termasuk nama peran dan role_id
        // Tidak lagi mengambil kolom 'permissions'
        String query = "SELECT u.user_id, u.username, u.password, r.role_name, u.full_name, u.email, u.phone, u.department, u.is_active, u.employee_id, u.hire_date, u.created_at, u.updated_at, r.role_id " +
                       "FROM users u JOIN roles r ON u.role_id = r.role_id " +
                       "WHERE u.username = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                
                // Verifikasi kata sandi (perbandingan teks biasa)
                if (password.equals(storedPassword)) {
                    // Login berhasil
                    User user = createUserFromResultSet(rs);
                    
                    // Reset upaya gagal dan perbarui login terakhir
                    resetFailedAttempts(username);
                    updateLastLogin(username);
                    
                    // Catat login berhasil
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
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            logSecurityEvent(username, "LOGIN_ERROR", false, "Database error during authentication: " + e.getMessage());
        }
        return null;
    }

    /**
     * Mendaftarkan pengguna baru dengan kata sandi teks biasa
     * @param user Objek Pengguna yang berisi detail pendaftaran
     * @return true jika pendaftaran berhasil, false jika tidak. Catatan: Objek Pengguna harus memiliki objek Role yang valid.
     */
    public boolean registerUser(User user) {
        if (!isValidRegistrationData(user)) {
            logSecurityEvent(user.getUsername(), "REGISTER_FAILED", false, "Invalid registration data provided.");
            return false;
        }
        
        // Periksa apakah nama pengguna sudah ada untuk mencegah entri duplikat
        if (usernameExists(user.getUsername())) {
            logSecurityEvent(user.getUsername(), "REGISTER_FAILED", false, "Username already exists.");
            System.err.println("Registration error: Username '" + user.getUsername() + "' already exists.");
            return false;
        }

        String query = """
            INSERT INTO users (username, password, role_id, full_name, email, phone, department, employee_id, hire_date, is_active, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword()); // Simpan kata sandi sebagai teks biasa
            stmt.setInt(3, user.getRole().getRoleId()); // Gunakan ID Peran dari objek Role
            stmt.setString(4, user.getFullName());
            stmt.setString(5, user.getEmail().isEmpty() ? null : user.getEmail());
            stmt.setString(6, user.getPhone().isEmpty() ? null : user.getPhone()); // Menambahkan phone
            stmt.setString(7, user.getDepartment());
            stmt.setString(8, user.getEmployeeId() != null && !user.getEmployeeId().isEmpty() ? user.getEmployeeId() : generateEmployeeId()); // Gunakan yang sudah ada atau hasilkan
            stmt.setTimestamp(9, user.getHireDate() != null ? Timestamp.valueOf(user.getHireDate()) : null); // Gunakan hire_date dari objek Pengguna
            stmt.setBoolean(10, user.isActive()); // Atur is_active
            stmt.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now())); // created_at
            stmt.setTimestamp(12, Timestamp.valueOf(LocalDateTime.now())); // updated_at

            int rowsAffected = stmt.executeUpdate();
            
            logSecurityEvent(user.getUsername(), "REGISTER_SUCCESS", true, "New user registered: " + user.getUsername());
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Registration error: " + e.getMessage());
            logSecurityEvent(user.getUsername(), "REGISTER_FAILED", false, "Registration error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Periksa apakah nama pengguna sudah ada di database
     * @param username Nama pengguna untuk diperiksa
     * @return true jika nama pengguna ada, false jika tidak
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
            System.err.println("Error checking username: " + e.getMessage());
        }
        return false;
    }

    /**
     * Buat objek Pengguna dari ResultSet
     * @param rs ResultSet yang berisi data pengguna
     * @return Objek Pengguna yang diisi dengan data dari ResultSet
     * @throws SQLException jika terjadi kesalahan akses database
     */
    private User createUserFromResultSet(ResultSet rs) throws SQLException {
        int roleId = rs.getInt("role_id");
        String roleName = rs.getString("role_name");
        // Tidak lagi mengambil permissions dari ResultSet
        Role userRole = new Role(roleId, roleName); // Tidak lagi meneruskan permissions ke konstruktor Role

        return new User(
            rs.getInt("user_id"),
            rs.getString("username"),
            rs.getString("password"), // Kata sandi dibaca di sini untuk konsistensi internal mock, tetapi tidak digunakan secara eksternal
            userRole,
            rs.getString("full_name"),
            rs.getString("email"),
            rs.getString("phone"), // Mengambil phone dari ResultSet
            rs.getString("department"),
            rs.getBoolean("is_active"),
            rs.getString("employee_id"),
            rs.getTimestamp("hire_date") != null ? rs.getTimestamp("hire_date").toLocalDateTime() : null,
            rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null,
            rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null
        );
    }

    /**
     * Periksa apakah akun saat ini terkunci karena terlalu banyak upaya login yang gagal.
     * @param username Nama pengguna untuk diperiksa
     * @return true jika akun terkunci, false jika tidak
     */
    private boolean isAccountLocked(String username) {
        String query = "SELECT account_locked_until, failed_login_attempts FROM users WHERE username = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Timestamp lockedUntil = rs.getTimestamp("account_locked_until");
                int failedAttempts = rs.getInt("failed_login_attempts");
                
                if (failedAttempts >= MAX_LOGIN_ATTEMPTS && lockedUntil != null) {
                    LocalDateTime lockTime = lockedUntil.toLocalDateTime();
                    LocalDateTime now = LocalDateTime.now();
                    
                    if (lockTime.isAfter(now)) {
                        return true; // Akun masih terkunci
                    } else {
                        // Periode penguncian berakhir, reset upaya
                        resetFailedAttempts(username);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking account lock status: " + e.getMessage());
        }
        return false;
    }

    /**
     * Reset upaya login yang gagal dan buka kunci akun.
     * @param username Nama pengguna untuk mereset upaya
     */
    private void resetFailedAttempts(String username) {
        String query = "UPDATE users SET failed_login_attempts = 0, account_locked_until = NULL WHERE username = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error resetting failed attempts: " + e.getMessage());
        }
    }

    /**
     * Perbarui stempel waktu login terakhir untuk pengguna.
     * @param username Nama pengguna untuk memperbarui login terakhir
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
     * Tingkatkan upaya login yang gagal untuk pengguna. Jika upaya melebihi MAX_LOGIN_ATTEMPTS,
     * akun akan dikunci selama LOCKOUT_DURATION_MINUTES.
     * @param username Nama pengguna untuk meningkatkan upaya gagal
     */
    private void incrementFailedAttempts(String username) {
        String query = """
            UPDATE users 
            SET failed_login_attempts = failed_login_attempts + 1,
                account_locked_until = CASE 
                    WHEN failed_login_attempts + 1 >= ? THEN ? 
                    ELSE account_locked_until 
                END
            WHERE username = ?
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            LocalDateTime lockoutTime = LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES);
            stmt.setInt(1, MAX_LOGIN_ATTEMPTS);
            stmt.setTimestamp(2, Timestamp.valueOf(lockoutTime));
            stmt.setString(3, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error incrementing failed attempts: " + e.getMessage());
        }
    }

    /**
     * Mencatat peristiwa terkait keamanan ke tabel audit_log.
     * @param username Nama pengguna yang terkait dengan peristiwa tersebut.
     * @param event Jenis peristiwa keamanan (misalnya, "LOGIN_SUCCESS", "LOGIN_FAILED").
     * @param success True jika peristiwa berhasil, false jika tidak.
     * @param details Detail tambahan tentang peristiwa tersebut.
     */
    private void logSecurityEvent(String username, String event, boolean success, String details) {
        String insertLog = """
            INSERT INTO audit_log (username, action, status, description, timestamp) 
            VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(insertLog)) {
            stmt.setString(1, username != null ? username : "unknown");
            stmt.setString(2, event);
            stmt.setString(3, success ? "SUCCESS" : "FAILED");
            stmt.setString(4, details);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Security logging error: " + e.getMessage());
        }
    }

    /**
     * Mencatat aktivitas pengguna umum ke tabel user_activity_log.
     * @param userId ID pengguna yang melakukan aktivitas tersebut.
     * @param activity Jenis aktivitas (misalnya, "LOGIN", "VIEW_REPORT").
     * @param description Deskripsi rinci tentang aktivitas tersebut.
     */
    private void logUserActivity(int userId, String activity, String description) {
        String insertLog = """
            INSERT INTO user_activity_log (user_id, activity, description, timestamp) 
            VALUES (?, ?, ?, CURRENT_TIMESTAMP)
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(insertLog)) {
            stmt.setInt(1, userId);
            stmt.setString(2, activity);
            stmt.setString(3, description);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Activity logging error: " + e.getMessage());
        }
    }

    /**
     * Dapatkan IP klien (placeholder untuk implementasi aktual)
     * Dalam aplikasi web nyata, ini akan mengambil IP klien dari permintaan.
     * Untuk aplikasi desktop, ini mungkin mengembalikan localhost atau IP mesin.
     * @return String yang merepresentasikan alamat IP klien.
     */
    private String getClientIp() {
        return "127.0.0.1"; // Placeholder
    }

    /**
     * Dapatkan agen pengguna (placeholder untuk implementasi aktual)
     * Dalam aplikasi web nyata, ini akan mengambil string agen pengguna dari permintaan.
     * Untuk aplikasi desktop, ini mungkin mengembalikan pengenal generik.
     * @return String yang merepresentasikan agen pengguna.
     */
    private String getUserAgent() {
        return "JavaFX_App/1.0"; // Placeholder
    }

    /**
     * Hasilkan ID sesi unik (placeholder untuk implementasi aktual)
     * Dalam aplikasi nyata, ini akan menjadi bagian dari sistem manajemen sesi.
     * @return String ID sesi unik.
     */
    private String generateSessionId() {
        return java.util.UUID.randomUUID().toString();
    }

    /**
     * Hasilkan ID karyawan sederhana.
     * Dalam sistem nyata, ini akan lebih kuat.
     * @return String ID karyawan yang dihasilkan.
     */
    private String generateEmployeeId() {
        return "EMP" + System.currentTimeMillis();
    }

    /**
     * Sanitasi string masukan untuk menghapus karakter yang berpotensi berbahaya.
     * Ini adalah sanitasi dasar dan harus dilengkapi dengan pernyataan yang sudah disiapkan.
     * @param input String yang akan disanitasi.
     * @return String yang telah disanitasi.
     */
    private String sanitizeInput(String input) {
        if (input == null) return null;
        // Mengizinkan alfanumerik, @, ., _, -
        return input.replaceAll("[^a-zA-Z0-9@._-]", ""); 
    }

    /**
     * Memvalidasi data yang disediakan untuk pendaftaran pengguna.
     * Memeriksa bidang yang tidak null/kosong, format email yang valid, dan peran yang valid.
     * @param user Objek Pengguna yang akan divalidasi.
     * @return true jika data valid, false jika tidak.
     */
    public boolean isValidRegistrationData(User user) {
        if (user == null || user.getUsername() == null || user.getUsername().trim().isEmpty() ||
            user.getPassword() == null || user.getPassword().isEmpty() ||
            user.getRole() == null || user.getRole().getRoleName().trim().isEmpty() || // Periksa objek peran dan namanya
            user.getFullName() == null || user.getFullName().trim().isEmpty() ||
            user.getDepartment() == null || user.getDepartment().trim().isEmpty()) {
            System.err.println("Validation failed: Missing required fields (username, password, role, full name, department).");
            return false;
        }
        
        // Validasi email format if provided
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty() && !EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            System.err.println("Validation failed: Invalid email format for '" + user.getEmail() + "'.");
            return false;
        }

        // Validasi phone format (basic check for digits and optional +)
        if (user.getPhone() != null && !user.getPhone().trim().isEmpty() && !user.getPhone().matches("^[+]?[0-9]+$")) {
            System.err.println("Validation failed: Invalid phone number format for '" + user.getPhone() + "'. Only digits and optional leading '+' are allowed.");
            return false;
        }
        
        // Validasi peran terhadap nilai yang diharapkan (menggunakan nama objek peran)
        String roleName = user.getRole().getRoleName();
        // Peran yang diizinkan sekarang harus sesuai dengan yang ada di mockRoles/DB
        if (!roleName.equals("ADMIN") && !roleName.equals("USER") && 
            !roleName.equals("SUPERVISOR") && !roleName.equals("OPERATOR")) {
            System.err.println("Validation failed: Invalid role '" + roleName + "'. Allowed roles: ADMIN, USER, SUPERVISOR, OPERATOR.");
            return false;
        }
        
        // Validasi panjang nama pengguna dan kata sandi
        if (user.getUsername().length() < 4) {
            System.err.println("Validation failed: Username must be at least 4 characters long.");
            return false;
        }
        if (user.getPassword().length() < 8) {
            System.err.println("Validation failed: Password must be at least 8 characters long.");
            return false;
        }
        
        return true;
    }

    /**
     * Mengambil objek Role berdasarkan namanya dari database.
     * Metode ini ditambahkan ke LoginHandler untuk menyelesaikan nama peran ke objek Role.
     * @param roleName Nama peran yang akan diambil.
     * @return Optional yang berisi objek Role jika ditemukan, kosong jika tidak.
     */
    public Optional<Role> getRoleByName(String roleName) {
        String query = "SELECT role_id, role_name FROM roles WHERE role_name = ?"; // Tidak lagi mengambil permissions
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, roleName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(new Role(rs.getInt("role_id"), rs.getString("role_name"))); // Tidak lagi meneruskan permissions
            }
        } catch (SQLException e) {
            System.err.println("Error fetching role by name: " + e.getMessage());
        }
        return Optional.empty();
    }
}
 