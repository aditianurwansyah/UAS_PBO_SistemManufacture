package com.mycompany.manufacturing_system;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Menangani operasi database untuk entitas Pengguna.
 * Kelas ini sekarang menggunakan database H2 dalam memori mock untuk demonstrasi.
 */
public class UserOperations {
    private Connection connection;

    // Daftar statis untuk menampung pengguna mock untuk operasi dalam memori
    private static List<User> mockUsers = new ArrayList<>();
    private static List<Role> mockRoles = new ArrayList<>();

    // Inisialisasi statis untuk mengisi data mock
    static {
        // Hapus data yang ada untuk mencegah duplikat saat hot reload di beberapa IDE
        mockUsers.clear();
        mockRoles.clear();

        // Isi peran mock tanpa izin JSON
        Role adminRole = new Role(1, "ADMIN");
        Role supervisorRole = new Role(2, "SUPERVISOR");
        Role operatorRole = new Role(3, "OPERATOR");
        Role userRole = new Role(4, "USER");

        mockRoles.add(adminRole);
        mockRoles.add(supervisorRole);
        mockRoles.add(operatorRole);
        mockRoles.add(userRole);

        // Isi pengguna mock
        mockUsers.add(new User(1, "admin", "admin123", adminRole, "Admin User", "admin@example.com", "081234567890", User.Department.ADMINISTRATION.name(), true, "EMP001", LocalDateTime.now().minusYears(2), LocalDateTime.now().minusYears(2), LocalDateTime.now()));
        mockUsers.add(new User(2, "operator", "operator123", operatorRole, "Operator User", "operator@example.example", "081234567891", User.Department.PRODUCTION.name(), true, "EMP002", LocalDateTime.now().minusYears(1), LocalDateTime.now().minusYears(1), LocalDateTime.now()));
        mockUsers.add(new User(3, "supervisor", "supervisor123", supervisorRole, "Supervisor User", "supervisor@example.com", "081234567892", User.Department.PRODUCTION.name(), true, "EMP003", LocalDateTime.now().minusMonths(6), LocalDateTime.now().minusMonths(6), LocalDateTime.now()));
        mockUsers.add(new User(4, "john.doe", "password123", userRole, "John Doe", "john.doe@example.com", "081234567893", User.Department.IT.name(), true, "EMP004", LocalDateTime.now().minusMonths(3), LocalDateTime.now().minusMonths(3), LocalDateTime.now()));
        mockUsers.add(new User(5, "jane.smith", "securepass", userRole, "Jane Smith", "jane.smith@example.com", "081234567894", User.Department.QUALITY_CONTROL.name(), false, "EMP005", LocalDateTime.now().minusMonths(9), LocalDateTime.now().minusMonths(9), LocalDateTime.now()));
    }

    public UserOperations() {
        // Untuk demonstrasi, kita akan menggunakan database H2 dalam memori melalui DatabaseConnection.
        // Dalam aplikasi nyata, ini akan menggunakan kumpulan koneksi yang tepat.
        try {
            this.connection = DatabaseConnection.getConnection();
            System.out.println("UserOperations initialized with mock database connection.");
        } catch (SQLException e) {
            System.err.println("Failed to get database connection for UserOperations: " + e.getMessage());
            // Dalam aplikasi nyata, Anda mungkin melempar runtime exception atau menanganinya dengan lebih baik
        }
    }

    /**
     * Mengambil semua pengguna dari database mock.
     * @return Daftar semua objek Pengguna.
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        // Menggunakan JOIN untuk mendapatkan nama peran dari tabel roles
        String query = "SELECT u.user_id, u.username, u.password, u.full_name, u.email, u.phone, u.department, u.is_active, u.employee_id, u.hire_date, u.created_at, u.updated_at, r.role_id, r.role_name " +
                       "FROM users u JOIN roles r ON u.role_id = r.role_id";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(createUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all users: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }

    /**
     * Membuat pengguna baru di database mock.
     * @param user Objek Pengguna yang akan dibuat.
     * @return true jika pengguna berhasil dibuat, false jika tidak.
     */
    public boolean createUser(User user) {
        if (userExists(user.getUsername())) {
            System.err.println("User with username " + user.getUsername() + " already exists.");
            return false;
        }

        String query = """
            INSERT INTO users (username, password, role_id, full_name, email, phone, department, is_active, employee_id, hire_date, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword()); // Dalam aplikasi nyata, hash kata sandi ini
            stmt.setInt(3, user.getRole().getRoleId()); // Gunakan ID Peran
            stmt.setString(4, user.getFullName());
            stmt.setString(5, user.getEmail());
            stmt.setString(6, user.getPhone()); // Menambahkan phone
            stmt.setString(7, user.getDepartment());
            stmt.setBoolean(8, user.isActive());
            // Gunakan employeeId yang ada jika disediakan, jika tidak, hasilkan
            String employeeIdToUse = (user.getEmployeeId() != null && !user.getEmployeeId().isEmpty()) ? user.getEmployeeId() : generateEmployeeId();
            stmt.setString(9, employeeIdToUse);
            
            // Gunakan hireDate yang ada jika disediakan, jika tidak, null (atau CURRENT_DATE() dari DB)
            Timestamp hireDateToUse = user.getHireDate() != null ? Timestamp.valueOf(user.getHireDate()) : null;
            stmt.setTimestamp(10, hireDateToUse);

            Timestamp currentTimestamp = Timestamp.valueOf(LocalDateTime.now());
            stmt.setTimestamp(11, currentTimestamp); // created_at
            stmt.setTimestamp(12, currentTimestamp); // updated_at

            System.out.println("Attempting to create user with details:");
            System.out.println("  Username: " + user.getUsername());
            System.out.println("  Password (plain): " + user.getPassword()); // Untuk debugging, hapus di produksi
            System.out.println("  Role ID: " + user.getRole().getRoleId() + " (Role Name: " + user.getRole().getRoleName() + ")");
            System.out.println("  Full Name: " + user.getFullName());
            System.out.println("  Email: " + user.getEmail());
            System.out.println("  Phone: " + user.getPhone()); // Log phone
            System.out.println("  Department: " + user.getDepartment());
            System.out.println("  Is Active: " + user.isActive());
            System.out.println("  Employee ID: " + employeeIdToUse);
            System.out.println("  Hire Date: " + hireDateToUse);
            System.out.println("  Created At: " + currentTimestamp);


            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setUserId(generatedKeys.getInt(1)); // Atur ID pengguna yang dihasilkan
                        System.out.println("  Generated User ID: " + user.getUserId());
                    }
                }
                // Juga tambahkan ke daftar mock untuk konsistensi
                mockUsers.add(user);
                System.out.println("User '" + user.getUsername() + "' created successfully in DB and mock list.");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
            e.printStackTrace(); // Cetak stack trace lengkap untuk debugging
        }
        return false;
    }

    /**
     * Mengambil pengguna berdasarkan nama pengguna dari database.
     * @param username Nama pengguna yang akan diambil.
     * @return Objek Pengguna jika ditemukan, null jika tidak.
     */
    public User getUserByUsername(String username) {
        // Menggunakan JOIN untuk mendapatkan nama peran dari tabel roles
        String query = "SELECT u.user_id, u.username, u.password, u.full_name, u.email, u.phone, u.department, u.is_active, u.employee_id, u.hire_date, u.created_at, u.updated_at, r.role_id, r.role_name " +
                       "FROM users u JOIN roles r ON u.role_id = r.role_id " +
                       "WHERE u.username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return createUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by username: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Memperbarui informasi pengguna yang ada.
     * @param username Nama pengguna yang akan diperbarui.
     * @param fullName Nama lengkap baru.
     * @param email Email baru.
     * @param phone Nomor telepon baru.
     * @param department Departemen baru.
     * @return true jika pengguna berhasil diperbarui, false jika tidak.
     */
    public boolean updateUser(String username, String fullName, String email, String phone, String department) {
        String query = "UPDATE users SET full_name = ?, email = ?, phone = ?, department = ?, updated_at = CURRENT_TIMESTAMP WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, fullName);
            stmt.setString(2, email);
            stmt.setString(3, phone); // Menambahkan phone
            stmt.setString(4, department);
            stmt.setString(5, username);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Perbarui daftar mock
                mockUsers.stream()
                        .filter(u -> u.getUsername().equals(username))
                        .findFirst()
                        .ifPresent(u -> {
                            u.setFullName(fullName);
                            u.setEmail(email);
                            u.setPhone(phone); // Perbarui phone di mock list
                            u.setDepartment(department);
                            u.setUpdatedAt(LocalDateTime.now());
                        });
                System.out.println("User '" + username + "' updated successfully.");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Mengubah peran pengguna.
     * @param username Nama pengguna.
     * @param newRoleName Nama peran baru (misalnya, "ADMIN", "USER").
     * @return true jika peran berhasil diubah, false jika tidak.
     */
    public boolean changeUserRole(String username, String newRoleName) {
        Optional<Role> newRoleOpt = mockRoles.stream()
                                            .filter(r -> r.getRoleName().equalsIgnoreCase(newRoleName))
                                            .findFirst();
        if (newRoleOpt.isEmpty()) {
            System.err.println("Role '" + newRoleName + "' not found in mock roles. Trying DB.");
            // Coba ambil dari DB jika tidak ada di mockRoles (jika mockRoles mungkin tidak lengkap)
            newRoleOpt = getRoleByName(newRoleName); // Panggil getRoleByName dari UserOperations
            if (newRoleOpt.isEmpty()) {
                System.err.println("Role '" + newRoleName + "' not found in DB either.");
                return false;
            }
        }
        // Pastikan newRoleId dan finalNewRole dideklarasikan sebagai final atau effectively final
        final int newRoleId = newRoleOpt.get().getRoleId(); 
        final Role finalNewRole = newRoleOpt.get(); 

        String query = "UPDATE users SET role_id = ?, updated_at = CURRENT_TIMESTAMP WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, newRoleId);
            stmt.setString(2, username);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Perbarui daftar mock
                mockUsers.stream()
                        .filter(u -> u.getUsername().equals(username))
                        .findFirst()
                        .ifPresent(u -> {
                            u.setRole(finalNewRole); // Menggunakan objek Role yang efektif final
                            u.setUpdatedAt(LocalDateTime.now());
                        });
                System.out.println("Role for user '" + username + "' changed to '" + newRoleName + "' successfully.");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error changing user role: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Mengubah status aktif pengguna.
     * @param username Nama pengguna.
     * @param isActive Status aktif baru.
     * @return true jika status berhasil diubah, false jika tidak.
     */
    public boolean toggleUserStatus(String username, boolean isActive) {
        String query = "UPDATE users SET is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setBoolean(1, isActive);
            stmt.setString(2, username);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Perbarui daftar mock
                mockUsers.stream()
                        .filter(u -> u.getUsername().equals(username))
                        .findFirst()
                        .ifPresent(u -> {
                            u.setActive(isActive);
                            u.setUpdatedAt(LocalDateTime.now());
                        });
                System.out.println("Status for user '" + username + "' toggled to " + isActive + " successfully.");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error toggling user status: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Mereset kata sandi pengguna.
     * @param username Nama pengguna.
     * @param newPassword Kata sandi teks biasa yang baru.
     * @return true jika kata sandi berhasil direset, false jika tidak.
     */
    public boolean resetUserPassword(String username, String newPassword) {
        String query = "UPDATE users SET password = ?, updated_at = CURRENT_TIMESTAMP WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, newPassword); // Dalam aplikasi nyata, hash kata sandi ini
            stmt.setString(2, username);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Perbarui daftar mock
                mockUsers.stream()
                        .filter(u -> u.getUsername().equals(username))
                        .findFirst()
                        .ifPresent(u -> {
                            u.setPassword(newPassword);
                            u.setUpdatedAt(LocalDateTime.now());
                        });
                System.out.println("Password for user '" + username + "' reset successfully.");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error resetting user password: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Memeriksa apakah pengguna dengan nama pengguna yang diberikan ada.
     * @param username Nama pengguna untuk diperiksa.
     * @return true jika pengguna ada, false jika tidak.
     */
    public boolean userExists(String username) {
        // Periksa di daftar mock terlebih dahulu untuk respons cepat
        if (mockUsers.stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username))) {
            return true;
        }
        // Secara opsional, periksa juga database untuk ketahanan, meskipun mockUsers harus tetap sinkron
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking user existence in DB: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Mengambil objek Role berdasarkan namanya.
     * @param roleName Nama peran.
     * @return Optional yang berisi objek Role jika ditemukan, kosong jika tidak.
     */
    public Optional<Role> getRoleByName(String roleName) {
        // Coba dapatkan dari mockRoles terlebih dahulu
        Optional<Role> roleOpt = mockRoles.stream()
                                        .filter(r -> r.getRoleName().equalsIgnoreCase(roleName))
                                        .findFirst();
        if (roleOpt.isPresent()) {
            return roleOpt;
        }

        // Jika tidak ada di mockRoles, coba ambil dari DB (jika mockRoles mungkin tidak lengkap)
        String query = "SELECT role_id, role_name FROM roles WHERE role_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, roleName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Role role = new Role(rs.getInt("role_id"), rs.getString("role_name"));
                mockRoles.add(role); // Tambahkan ke mockRoles untuk akses cepat di masa mendatang
                return Optional.of(role);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching role by name from DB: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Mengambil objek Role berdasarkan ID-nya.
     * @param roleId ID peran.
     * @return Optional yang berisi objek Role jika ditemukan, kosong jika tidak.
     */
    public Optional<Role> getRoleById(int roleId) {
        // Coba dapatkan dari mockRoles terlebih dahulu
        Optional<Role> roleOpt = mockRoles.stream()
                                        .filter(r -> r.getRoleId() == roleId)
                                        .findFirst();
        if (roleOpt.isPresent()) {
            return roleOpt;
        }

        // Jika tidak ada di mockRoles, coba ambil dari DB
        String query = "SELECT role_id, role_name FROM roles WHERE role_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, roleId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Role role = new Role(rs.getInt("role_id"), rs.getString("role_name"));
                mockRoles.add(role); // Tambahkan ke mockRoles untuk akses cepat di masa mendatang
                return Optional.of(role);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching role by ID from DB: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Pembantu untuk membuat objek Pengguna dari ResultSet.
     * @param rs ResultSet yang berisi data pengguna dan peran.
     * @return Objek Pengguna.
     * @throws SQLException jika terjadi kesalahan saat mengakses ResultSet.
     */
    private User createUserFromResultSet(ResultSet rs) throws SQLException {
        int roleId = rs.getInt("role_id");
        String roleName = rs.getString("role_name");
        Role userRole = new Role(roleId, roleName);

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

    // Kelas internal untuk Statistik Pengguna
    public static class UserStatistics {
        private int totalUsers;
        private int activeUsers;
        private int adminCount;
        private int supervisorCount;
        private int operatorCount;
        private int userCount; // Ini sekarang merepresentasikan peran 'USER' secara spesifik

        public UserStatistics(int totalUsers, int activeUsers, int adminCount, int supervisorCount, int operatorCount, int userCount) {
            this.totalUsers = totalUsers;
            this.activeUsers = activeUsers;
            this.adminCount = adminCount;
            this.supervisorCount = supervisorCount;
            this.operatorCount = operatorCount;
            this.userCount = userCount;
        }

        public int getTotalUsers() { return totalUsers; }
        public int getActiveUsers() { return activeUsers; }
        public int getAdminCount() { return adminCount; }
        public int getSupervisorCount() { return supervisorCount; }
        public int getOperatorCount() { return operatorCount; }
        public int getUserCount() { return userCount; }
    }

    /**
     * Menghitung dan mengembalikan statistik pengguna.
     * @return Objek UserStatistics.
     */
    public static UserStatistics getUserStatistics() {
        // Gunakan daftar mockUsers statis untuk statistik
        int total = mockUsers.size();
        int active = (int) mockUsers.stream().filter(User::isActive).count();
        int admin = (int) mockUsers.stream().filter(u -> u.getRole().getRoleName().equals("ADMIN")).count();
        int supervisor = (int) mockUsers.stream().filter(u -> u.getRole().getRoleName().equals("SUPERVISOR")).count();
        int operator = (int) mockUsers.stream().filter(u -> u.getRole().getRoleName().equals("OPERATOR")).count();
        int regularUser = (int) mockUsers.stream().filter(u -> u.getRole().getRoleName().equals("USER")).count();

        return new UserStatistics(total, active, admin, supervisor, operator, regularUser);
    }

    /**
     * Menghasilkan ID karyawan sederhana.
     * Dalam sistem nyata, ini akan lebih kuat.
     * @return String ID karyawan yang dihasilkan.
     */
    private String generateEmployeeId() {
        return "EMP" + System.currentTimeMillis();
    }
}
 