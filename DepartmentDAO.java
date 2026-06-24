package com.example.demo10.raffles.hotelmgmt.dao; // Correct package

import com.example.demo10.raffles.hotelmgmt.DatabaseConnector; // Your connector
import com.example.demo10.raffles.hotelmgmt.model.Department;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DepartmentDAO {

    /**
     * Get all departments from the database
     * @return List of all departments
     */
    public ObservableList<Department> getAllDepartments() {
        ObservableList<Department> departments = FXCollections.observableArrayList();
        String sql = "SELECT * FROM Department ORDER BY DepartmentName";
        
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Department dept = new Department();
                dept.setDepartmentID(rs.getInt("DepartmentID"));
                dept.setDepartmentName(rs.getString("DepartmentName"));
                departments.add(dept);
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching all departments: " + e.getMessage());
        }
        
        return departments;
    }

    public Department getDepartmentById(int departmentId) {
        String sql = "SELECT DepartmentID, DepartmentName FROM Department WHERE DepartmentID = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, departmentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRowToDepartment(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching department by ID " + departmentId + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Get department ID by name
     * @param departmentName The name of the department
     * @return The department ID, or -1 if not found
     */
    public int getDepartmentIdByName(String departmentName) {
        String sql = "SELECT DepartmentID FROM Department WHERE DepartmentName = ?";
        
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, departmentName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("DepartmentID");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding department by name: " + e.getMessage());
            e.printStackTrace();
        }
        
        // If department doesn't exist, return -1
        return -1;
    }

    // Add, Update, Delete methods for Departments can be added here if needed.

    private Department mapRowToDepartment(ResultSet rs) throws SQLException {
        Department dept = new Department();
        dept.setDepartmentID(rs.getInt("DepartmentID"));
        dept.setDepartmentName(rs.getString("DepartmentName"));
        return dept;
    }
}