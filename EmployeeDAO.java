package com.example.demo10.raffles.hotelmgmt.dao; // Correct package

import com.example.demo10.raffles.hotelmgmt.DatabaseConnector; // Your connector
// import com.example.demo10.raffles.hotelmgmt.PasswordUtil; // Not used if plain text
import com.example.demo10.raffles.hotelmgmt.model.Employee;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;

public class EmployeeDAO {

    public boolean addEmployee(Employee employee) { // Plain password directly from employee object
        String sql = "INSERT INTO Employee (FirstName, LastName, RoleID, DepartmentID, Username, Password, ContactNumber, Email, HireDate, IsActive) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"; // Storing plain Password
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, employee.getFirstName());
            pstmt.setString(2, employee.getLastName());
            if (employee.getRoleID() != null) pstmt.setInt(3, employee.getRoleID()); else pstmt.setNull(3, Types.INTEGER);
            if (employee.getDepartmentID() != null) pstmt.setInt(4, employee.getDepartmentID()); else pstmt.setNull(4, Types.INTEGER);
            pstmt.setString(5, employee.getUsername());
            pstmt.setString(6, employee.getPassword()); // Store plain password
            pstmt.setString(7, employee.getContactNumber());
            pstmt.setString(8, employee.getEmail());
            pstmt.setDate(9, employee.getHireDate() != null ? Date.valueOf(employee.getHireDate()) : null);
            pstmt.setBoolean(10, employee.isActive());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding employee: " + e.getMessage());
            // Check for unique constraint violation (e.g., username or email)
            if (e.getSQLState().equals("23505")) { // HSQLDB unique constraint violation code
                System.err.println("Username or Email already exists for employee.");
            }
            return false;
        }
    }

    public boolean updateEmployee(Employee employee) { // Password update handled by setting plain password on employee object
        String sql = "UPDATE Employee SET FirstName = ?, LastName = ?, RoleID = ?, DepartmentID = ?, Username = ?, " +
                "Password = ?, ContactNumber = ?, Email = ?, HireDate = ?, IsActive = ? WHERE EmployeeID = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, employee.getFirstName());
            pstmt.setString(2, employee.getLastName());
            if (employee.getRoleID() != null) pstmt.setInt(3, employee.getRoleID()); else pstmt.setNull(3, Types.INTEGER);
            if (employee.getDepartmentID() != null) pstmt.setInt(4, employee.getDepartmentID()); else pstmt.setNull(4, Types.INTEGER);
            pstmt.setString(5, employee.getUsername());
            pstmt.setString(6, employee.getPassword()); // Store updated plain password
            pstmt.setString(7, employee.getContactNumber());
            pstmt.setString(8, employee.getEmail());
            pstmt.setDate(9, employee.getHireDate() != null ? Date.valueOf(employee.getHireDate()) : null);
            pstmt.setBoolean(10, employee.isActive());
            pstmt.setInt(11, employee.getEmployeeID());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating employee: " + e.getMessage());
            if (e.getSQLState().equals("23505")) {
                System.err.println("Update failed: Username or Email already exists for another employee.");
            }
            return false;
        }
    }


    public Employee getEmployeeByUsername(String username) {
        String sql = "SELECT e.*, r.RoleName, d.DepartmentName " +
                "FROM Employee e " +
                "LEFT JOIN Role r ON e.RoleID = r.RoleID " +
                "LEFT JOIN Department d ON e.DepartmentID = d.DepartmentID " +
                "WHERE e.Username = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRowToEmployee(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching employee by username '" + username + "': " + e.getMessage());
        }
        return null;
    }

    public ObservableList<Employee> getAllEmployees() {
        ObservableList<Employee> employees = FXCollections.observableArrayList();
        String sql = "SELECT e.*, r.RoleName, d.DepartmentName " +
                "FROM Employee e " +
                "LEFT JOIN Role r ON e.RoleID = r.RoleID " +
                "LEFT JOIN Department d ON e.DepartmentID = d.DepartmentID " +
                "ORDER BY e.LastName, e.FirstName"; // Ensure consistent ordering
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                employees.add(mapRowToEmployee(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all employees: " + e.getMessage());
        }
        return employees;
    }

    public boolean deleteEmployee(int employeeId) {
        String sql = "DELETE FROM Employee WHERE EmployeeID = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting employee with ID " + employeeId + ": " + e.getMessage());
            if (e.getSQLState().startsWith("23")) { // HSQLDB foreign key violation
                System.err.println("Cannot delete employee. They might be referenced in other records.");
            }
            return false;
        }
    }


    private Employee mapRowToEmployee(ResultSet rs) throws SQLException {
        Employee emp = new Employee();
        emp.setEmployeeID(rs.getInt("EmployeeID"));
        emp.setFirstName(rs.getString("FirstName"));
        emp.setLastName(rs.getString("LastName"));
        emp.setRoleID(rs.getObject("RoleID") != null ? rs.getInt("RoleID") : null);
        emp.setRoleName(rs.getString("RoleName")); // From JOIN
        emp.setDepartmentID(rs.getObject("DepartmentID") != null ? rs.getInt("DepartmentID") : null);
        emp.setDepartmentName(rs.getString("DepartmentName")); // From JOIN
        emp.setUsername(rs.getString("Username"));
        emp.setPassword(rs.getString("Password")); // Get plain password
        emp.setContactNumber(rs.getString("ContactNumber"));
        emp.setEmail(rs.getString("Email"));
        Date hireDateSql = rs.getDate("HireDate");
        if (hireDateSql != null) {
            emp.setHireDate(hireDateSql.toLocalDate());
        }
        emp.setActive(rs.getBoolean("IsActive"));
        return emp;
    }

    public long getEmployeeCount() {
        String sql = "SELECT COUNT(*) FROM Employee WHERE IsActive = TRUE";
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting active employee count: " + e.getMessage());
        }
        return 0;
    }
}