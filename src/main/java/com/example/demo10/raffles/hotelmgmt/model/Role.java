package com.example.demo10.raffles.hotelmgmt.model;

import java.util.Objects;

public class Role {
    private int roleID;
    private String roleName;
    private String description;
    private String permissions; // For future use, can store comma-separated or JSON

    public Role() {
    }

    public Role(int roleID, String roleName, String description, String permissions) {
        this.roleID = roleID;
        this.roleName = roleName;
        this.description = description;
        this.permissions = permissions;
    }

    // Getters and Setters
    public int getRoleID() {
        return roleID;
    }

    public void setRoleID(int roleID) {
        this.roleID = roleID;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    @Override
    public String toString() {
        // This is important for displaying Role objects in ComboBoxes
        return roleName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return roleID == role.roleID && Objects.equals(roleName, role.roleName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleID, roleName);
    }
}
