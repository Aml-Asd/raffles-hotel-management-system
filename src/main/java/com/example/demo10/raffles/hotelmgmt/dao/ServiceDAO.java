package com.example.demo10.raffles.hotelmgmt.dao;

import com.example.demo10.raffles.hotelmgmt.DatabaseConnector;
import com.example.demo10.raffles.hotelmgmt.model.Service;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
// Add PreparedStatement, BigDecimal, Types for full CRUD

public class ServiceDAO {

    /**
     * Get all services from the database
     * @return List of all services
     */
    public ObservableList<Service> getAllServices() {
        ObservableList<Service> services = FXCollections.observableArrayList();
        String sql = "SELECT s.*, d.DepartmentName FROM Service s LEFT JOIN Department d ON s.DepartmentID = d.DepartmentID";
        
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Service service = new Service();
                service.setServiceID(rs.getInt("ServiceID"));
                service.setServiceName(rs.getString("ServiceName"));
                service.setDescription(rs.getString("Description"));
                service.setDefaultPrice(rs.getBigDecimal("DefaultPrice"));
                
                int deptId = rs.getInt("DepartmentID");
                if (!rs.wasNull()) {
                    service.setDepartmentID(deptId);
                    service.setDepartmentName(rs.getString("DepartmentName"));
                }
                
                services.add(service);
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching services: " + e.getMessage());
            e.printStackTrace();
        }
        
        return services;
    }
    
    /**
     * Add a new service to the database
     * @param service The service to add
     * @return true if successful, false otherwise
     */
    public boolean addService(Service service) {
        String sql = "INSERT INTO Service (ServiceName, Description, DefaultPrice, DepartmentID) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, service.getServiceName());
            pstmt.setString(2, service.getDescription());
            pstmt.setBigDecimal(3, service.getDefaultPrice());
            
            if (service.getDepartmentID() != null) {
                pstmt.setInt(4, service.getDepartmentID());
            } else {
                pstmt.setNull(4, java.sql.Types.INTEGER);
            }
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        service.setServiceID(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
            
            return false;
            
        } catch (SQLException e) {
            System.err.println("Error adding service: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Ensure that default services exist in the database
     * This is used to populate the database with initial services
     */
    public void ensureDefaultServicesExist() {
        ObservableList<Service> existingServices = getAllServices();
        
        if (existingServices.isEmpty()) {
            System.out.println("Creating default services...");
            
            // Get department IDs
            DepartmentDAO deptDAO = new DepartmentDAO();
            int foodBeverageDeptId = deptDAO.getDepartmentIdByName("Food & Beverage");
            int housekeepingDeptId = deptDAO.getDepartmentIdByName("Housekeeping");
            int frontOfficeDeptId = deptDAO.getDepartmentIdByName("Front Office");
            
            // Create room service
            Service roomService = new Service();
            roomService.setServiceName("Room Service");
            roomService.setDescription("In-room dining service");
            roomService.setDefaultPrice(new BigDecimal("25.00"));
            roomService.setDepartmentID(foodBeverageDeptId);
            addService(roomService);
            
            // Create laundry service
            Service laundryService = new Service();
            laundryService.setServiceName("Laundry");
            laundryService.setDescription("Clothes washing and ironing service");
            laundryService.setDefaultPrice(new BigDecimal("15.00"));
            laundryService.setDepartmentID(housekeepingDeptId);
            addService(laundryService);
            
            // Create minibar service
            Service minibarService = new Service();
            minibarService.setServiceName("Minibar");
            minibarService.setDescription("In-room refreshments and snacks");
            minibarService.setDefaultPrice(new BigDecimal("10.00"));
            minibarService.setDepartmentID(foodBeverageDeptId);
            addService(minibarService);
            
            // Create airport transfer
            Service airportService = new Service();
            airportService.setServiceName("Airport Transfer");
            airportService.setDescription("Transportation to/from airport");
            airportService.setDefaultPrice(new BigDecimal("50.00"));
            airportService.setDepartmentID(frontOfficeDeptId);
            addService(airportService);
            
            System.out.println("Default services created successfully");
        }
    }
}