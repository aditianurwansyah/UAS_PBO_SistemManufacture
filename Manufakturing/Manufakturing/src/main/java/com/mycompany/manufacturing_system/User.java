package com.mycompany.manufacturing_system;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class User {
    private final IntegerProperty userId;
    private final StringProperty username;
    private final StringProperty password; // Catatan: Untuk tampilan/logika internal, bukan untuk penyimpanan/pengambilan langsung dari DB
    private final ObjectProperty<Role> role; // Diubah dari StringProperty menjadi ObjectProperty<Role>
    private final StringProperty fullName;
    private final StringProperty email;
    private final StringProperty phone; // Menambahkan properti phone
    private final StringProperty department;
    private final BooleanProperty isActive;
    private final StringProperty employeeId;
    private final ObjectProperty<LocalDateTime> hireDate;
    private final ObjectProperty<LocalDateTime> createdAt;
    private final ObjectProperty<LocalDateTime> updatedAt;

    // Enum untuk Departemen
    public enum Department {
        PRODUCTION("Production"),
        QUALITY_CONTROL("Quality Control"),
        MAINTENANCE("Maintenance"),
        LOGISTICS("Logistics"),
        ADMINISTRATION("Administration"),
        IT("IT"),
        UNKNOWN("Unknown");

        private final String displayName;

        Department(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }

        public static Department fromString(String text) {
            for (Department b : Department.values()) {
                if (b.displayName.equalsIgnoreCase(text)) {
                    return b;
                }
            }
            return UNKNOWN; // Default ke UNKNOWN jika tidak ditemukan
        }
    }


    // Konstruktor untuk pendaftaran pengguna baru (tanpa userId, employeeId, dates)
    public User(String username, String password, Role role, String fullName, String email, String phone, String department) {
        this(0, username, password, role, fullName, email, phone, department, true, null, null, null, null);
    }

    // Konstruktor lengkap
    public User(int userId, String username, String password, Role role, String fullName, String email,
                String phone, String department, boolean isActive, String employeeId, LocalDateTime hireDate,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.userId = new SimpleIntegerProperty(userId);
        this.username = new SimpleStringProperty(username);
        this.password = new SimpleStringProperty(password); // Simpan teks biasa untuk penggunaan internal sebelum hashing/DB
        this.role = new SimpleObjectProperty<>(role); // Gunakan objek Role
        this.fullName = new SimpleStringProperty(fullName);
        this.email = new SimpleStringProperty(email);
        this.phone = new SimpleStringProperty(phone); // Inisialisasi properti phone
        this.department = new SimpleStringProperty(department);
        this.isActive = new SimpleBooleanProperty(isActive);
        this.employeeId = new SimpleStringProperty(employeeId);
        this.hireDate = new SimpleObjectProperty<>(hireDate);
        this.createdAt = new SimpleObjectProperty<>(createdAt);
        this.updatedAt = new SimpleObjectProperty<>(updatedAt);
    }

    // Getter properti
    public IntegerProperty userIdProperty() { return userId; }
    public StringProperty usernameProperty() { return username; }
    public StringProperty passwordProperty() { return password; }
    public ObjectProperty<Role> roleProperty() { return role; } // Mengembalikan ObjectProperty<Role>
    public StringProperty fullNameProperty() { return fullName; }
    public StringProperty emailProperty() { return email; }
    public StringProperty phoneProperty() { return phone; } // Getter properti phone
    public StringProperty departmentProperty() { return department; }
    public BooleanProperty isActiveProperty() { return isActive; }
    public StringProperty employeeIdProperty() { return employeeId; }
    public ObjectProperty<LocalDateTime> hireDateProperty() { return hireDate; }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }
    public ObjectProperty<LocalDateTime> updatedAtProperty() { return updatedAt; }


    // Getter biasa
    public int getUserId() { return userId.get(); }
    public String getUsername() { return username.get(); }
    public String getPassword() { return password.get(); }
    public Role getRole() { return role.get(); } // Mengembalikan objek Role
    public String getFullName() { return fullName.get(); }
    public String getEmail() { return email.get(); }
    public String getPhone() { return phone.get(); } // Getter biasa phone
    public String getDepartment() { return department.get(); }
    public boolean isActive() { return isActive.get(); }
    public String getEmployeeId() { return employeeId.get(); }
    public LocalDateTime getHireDate() { return hireDate.get(); }
    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public LocalDateTime getUpdatedAt() { return updatedAt.get(); }


    // Setter
    public void setUserId(int userId) { this.userId.set(userId); }
    public void setUsername(String username) { this.username.set(username); }
    public void setPassword(String password) { this.password.set(password); }
    public void setRole(Role role) { this.role.set(role); } // Menerima objek Role
    public void setFullName(String fullName) { this.fullName.set(fullName); }
    public void setEmail(String email) { this.email.set(email); }
    public void setPhone(String phone) { this.phone.set(phone); } // Setter phone
    public void setDepartment(String department) { this.department.set(department); }
    public void setActive(boolean isActive) { this.isActive.set(isActive); }
    public void setEmployeeId(String employeeId) { this.employeeId.set(employeeId); }
    public void setHireDate(LocalDateTime hireDate) { this.hireDate.set(hireDate); }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt.set(createdAt); }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt.set(updatedAt); }

    // Nama Tampilan (bisa nama lengkap atau nama pengguna)
    public String getDisplayName() {
        return (fullName != null && !fullName.get().isEmpty()) ? fullName.get() : username.get();
    }

    /**
     * Memeriksa apakah pengguna saat ini memiliki hak akses administratif.
     * @return true jika peran pengguna adalah ADMIN atau SUPERVISOR, false jika tidak.
     */
    public boolean canManageUsers() {
        return getRole() != null &&
               (getRole().getRoleName().equals("ADMIN") || getRole().getRoleName().equals("SUPERVISOR"));
    }
}
 