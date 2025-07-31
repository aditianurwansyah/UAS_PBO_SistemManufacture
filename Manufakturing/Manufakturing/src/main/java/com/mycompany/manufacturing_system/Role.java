package com.mycompany.manufacturing_system;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Objects;

/**
 * Merepresentasikan peran pengguna dalam sistem manufaktur.
 * Sesuai dengan tabel 'roles' di database, tanpa properti izin JSON.
 */
public class Role {
    private final IntegerProperty roleId;
    private final StringProperty roleName;

    public Role(int roleId, String roleName) {
        this.roleId = new SimpleIntegerProperty(roleId);
        this.roleName = new SimpleStringProperty(roleName);
    }

    // Getters for properties
    public IntegerProperty roleIdProperty() {
        return roleId;
    }

    public StringProperty roleNameProperty() {
        return roleName;
    }

    // Getters for direct access
    public int getRoleId() {
        return roleId.get();
    }

    public String getRoleName() {
        return roleName.get();
    }

    // Setters (if needed, though roles are often static)
    public void setRoleName(String roleName) {
        this.roleName.set(roleName);
    }

    @Override
    public String toString() {
        return getRoleName(); // Useful for ComboBoxes
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role1 = (Role) o;
        return getRoleId() == role1.getRoleId() &&
               Objects.equals(getRoleName(), role1.getRoleName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRoleId(), getRoleName());
    }
}
 