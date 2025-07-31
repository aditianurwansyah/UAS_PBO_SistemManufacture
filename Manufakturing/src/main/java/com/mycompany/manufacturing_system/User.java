package com.mycompany.manufacturing_system;

import javafx.beans.property.*;

/**
 * Enhanced User class for Manufacturing Management System
 * Supports ADMIN and USER roles with additional user information
 */
public class User {
    private IntegerProperty userId;
    private final StringProperty username;
    private final StringProperty password;
    private final StringProperty role; // ADMIN or USER
    private final StringProperty fullName;
    private final StringProperty email;
    private final StringProperty department;
    private final BooleanProperty isActive;

    // Constructor for basic user creation
    public User(String username, String password, String role) {
        this.userId = new SimpleIntegerProperty(0);
        this.username = new SimpleStringProperty(username);
        this.password = new SimpleStringProperty(password);
        this.role = new SimpleStringProperty(role);
        this.fullName = new SimpleStringProperty("");
        this.email = new SimpleStringProperty("");
        this.department = new SimpleStringProperty("");
        this.isActive = new SimpleBooleanProperty(true);
    }

    // Constructor with additional details
    public User(String username, String password, String role, String fullName, String email, String department) {
        this.userId = new SimpleIntegerProperty(0);
        this.username = new SimpleStringProperty(username);
        this.password = new SimpleStringProperty(password);
        this.role = new SimpleStringProperty(role);
        this.fullName = new SimpleStringProperty(fullName != null ? fullName : "");
        this.email = new SimpleStringProperty(email != null ? email : "");
        this.department = new SimpleStringProperty(department != null ? department : "");
        this.isActive = new SimpleBooleanProperty(true);
    }

    // NEW Constructor for retrieving from database with all details including ID and active status
    public User(int userId, String username, String password, String role, String fullName, String email, String department, boolean isActive) {
        this.userId = new SimpleIntegerProperty(userId);
        this.username = new SimpleStringProperty(username);
        this.password = new SimpleStringProperty(password);
        this.role = new SimpleStringProperty(role);
        this.fullName = new SimpleStringProperty(fullName != null ? fullName : "");
        this.email = new SimpleStringProperty(email != null ? email : "");
        this.department = new SimpleStringProperty(department != null ? department : "");
        this.isActive = new SimpleBooleanProperty(isActive);
    }

    // Getters
    public int getUserId() { return userId.get(); }
    public String getUsername() { return username.get(); }
    public String getPassword() { return password.get(); }
    public String getRole() { return role.get(); }
    public String getFullName() { return fullName.get(); }
    public String getEmail() { return email.get(); }
    public String getDepartment() { return department.get(); }
    public boolean isActive() { return isActive.get(); }

    // Property getters for JavaFX binding
    public IntegerProperty userIdProperty() { return userId; }
    public StringProperty usernameProperty() { return username; }
    public StringProperty passwordProperty() { return password; }
    public StringProperty roleProperty() { return role; }
    public StringProperty fullNameProperty() { return fullName; }
    public StringProperty emailProperty() { return email; }
    public StringProperty departmentProperty() { return department; }
    public BooleanProperty isActiveProperty() { return isActive; }

    // Setters
    public void setUserId(int userId) { this.userId.set(userId); }
    public void setPassword(String password) { this.password.set(password != null ? password : ""); }
    public void setRole(String role) { this.role.set(role != null ? role : "USER"); }
    public void setFullName(String fullName) { this.fullName.set(fullName != null ? fullName : ""); }
    public void setEmail(String email) { this.email.set(email != null ? email : ""); }
    public void setDepartment(String department) { this.department.set(department != null ? department : ""); }
    public void setActive(boolean active) { this.isActive.set(active); }

    // Role validation methods
    public boolean isAdmin() {
        return "ADMIN".equals(role.get());
    }

    public boolean isUser() {
        return "USER".equals(role.get());
    }

    public boolean hasRole(String roleToCheck) {
        return roleToCheck != null && roleToCheck.equals(role.get());
    }

    // Access control methods
    public boolean canAccessAdminFeatures() {
        return isAdmin() && isActive();
    }

    public boolean canAccessUserFeatures() {
        return (isAdmin() || isUser()) && isActive();
    }

    public boolean canManageUsers() {
        return isAdmin() && isActive();
    }

    public boolean canCreateOrders() {
        return isActive(); // Both ADMIN and USER can create orders
    }

    public boolean canManageProduction() {
        return isAdmin() && isActive(); // Only ADMIN can manage production
    }

    public boolean canViewReports() {
        return isActive(); // Both roles can view reports, but different levels
    }

    public boolean canViewAllOrders() {
        return isAdmin() && isActive(); // Only ADMIN can view all orders
    }

    public boolean canModifySystemSettings() {
        return isAdmin() && isActive();
    }

    // Utility methods
    public String getDisplayName() {
        String full = fullName.get();
        return (full != null && !full.trim().isEmpty()) ? full : username.get();
    }

    public String getRoleDisplayName() {
        switch (role.get()) {
            case "ADMIN":
                return "Administrator";
            case "USER":
                return "Production User";
            default:
                return role.get();
        }
    }

    public String getStatusDisplayName() {
        return isActive() ? "Active" : "Inactive";
    }

    public String getUserInfo() {
        StringBuilder info = new StringBuilder();
        info.append("User: ").append(getDisplayName());
        info.append(" (").append(username.get()).append(")");
        info.append(" | Role: ").append(getRoleDisplayName());
        if (department.get() != null && !department.get().trim().isEmpty()) {
            info.append(" | Department: ").append(department.get());
        }
        info.append(" | Status: ").append(getStatusDisplayName());
        return info.toString();
    }

    // Validation methods
    public boolean isValidForSave() {
        return username.get() != null && !username.get().trim().isEmpty() &&
               password.get() != null && !password.get().trim().isEmpty() &&
               role.get() != null && (isAdmin() || isUser());
    }

    public String getValidationErrors() {
        StringBuilder errors = new StringBuilder();
        
        if (username.get() == null || username.get().trim().isEmpty()) {
            errors.append("Username is required. ");
        }
        
        if (password.get() == null || password.get().trim().isEmpty()) {
            errors.append("Password is required. ");
        }
        
        if (role.get() == null || (!isAdmin() && !isUser())) {
            errors.append("Valid role (ADMIN or USER) is required. ");
        }
        
        if (email.get() != null && !email.get().trim().isEmpty() && !isValidEmail(email.get())) {
            errors.append("Valid email format is required. ");
        }
        
        return errors.toString();
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    }

    // Security methods
    public boolean canAccessResource(String resource) {
        if (!isActive()) return false;
        
        switch (resource.toLowerCase()) {
            case "dashboard":
                return true; // Both roles can access dashboard
            case "production_management":
                return isAdmin();
            case "user_management":
                return isAdmin();
            case "reports":
                return true; // Both can view reports
            case "system_settings":
                return isAdmin();
            case "my_tasks":
                return true; // Both can view their tasks
            default:
                return isAdmin(); // Admin has access to everything by default
        }
    }

    @Override
    public String toString() {
        return String.format("User{id=%d, username='%s', role='%s', fullName='%s', active=%s}", 
            userId.get(), username.get(), role.get(), fullName.get(), isActive());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        User user = (User) obj;
        return userId.get() == user.userId.get() && 
               username.get().equals(user.username.get());
    }

    @Override
    public int hashCode() {
        return username.get().hashCode();
    }

    // Factory methods for common user types
    public static User createAdmin(String username, String password, String fullName) {
        User admin = new User(username, password, "ADMIN", fullName, "", "IT");
        return admin;
    }

    public static User createProductionUser(String username, String password, String fullName, String department) {
        User user = new User(username, password, "USER", fullName, "", department);
        return user;
    }

    // Method to copy user data (useful for updates)
    public void copyFrom(User other) {
        if (other != null) {
            setFullName(other.getFullName());
            setEmail(other.getEmail());
            setDepartment(other.getDepartment());
            setActive(other.isActive());
            // Note: Don't copy username, password, or role for security
        }
    }
}