package com.example.demo10.raffles.hotelmgmt.dao; // Correct package

import com.example.demo10.raffles.hotelmgmt.DatabaseConnector; // Your connector
import com.example.demo10.raffles.hotelmgmt.model.Role;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class RoleDAO {

    public ObservableList<Role> getAllRoles() {
        ObservableList<Role> roles = FXCollections.observableArrayList();
        String sql = "SELECT RoleID, RoleName, Description, Permissions FROM Role ORDER BY RoleName";
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                roles.add(mapRowToRole(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all roles: " + e.getMessage());
            // Consider throwing a custom DAOException or logging more formally
        }
        return roles;
    }

    public Role getRoleById(int roleId) {
        String sql = "SELECT RoleID, RoleName, Description, Permissions FROM Role WHERE RoleID = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roleId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRowToRole(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching role by ID " + roleId + ": " + e.getMessage());
        }
        return null;
    }

    public int getRoleIdByName(String roleName) {
        String sql = "SELECT RoleID FROM Role WHERE RoleName = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, roleName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("RoleID");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching RoleID by name '" + roleName + "': " + e.getMessage());
        }
        return -1; // Indicates not found or error
    }

    // Add, Update, Delete methods for Roles can be added here if Admins need to manage roles.
    // For now, we'll assume roles are predefined or managed via DB scripts.
    // Example:
    /*
    public boolean addRole(Role role) {
        String sql = "INSERT INTO Role (RoleName, Description, Permissions) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, role.getRoleName());
            pstmt.setString(2, role.getDescription());
            pstmt.setString(3, role.getPermissions());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding role: " + e.getMessage());
            return false;
        }
    }
    */

    private Role mapRowToRole(ResultSet rs) throws SQLException {
        Role role = new Role();
        role.setRoleID(rs.getInt("RoleID"));
        role.setRoleName(rs.getString("RoleName"));
        role.setDescription(rs.getString("Description"));
        role.setPermissions(rs.getString("Permissions"));
        return role;
    }
}