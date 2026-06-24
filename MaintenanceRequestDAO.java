package com.example.demo10.raffles.hotelmgmt.dao;

import com.example.demo10.raffles.hotelmgmt.DatabaseConnector;
import com.example.demo10.raffles.hotelmgmt.model.MaintenanceRequest;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.sql.Types;

public class MaintenanceRequestDAO {

    public ObservableList<MaintenanceRequest> getAllMaintenanceRequests() {
        ObservableList<MaintenanceRequest> requests = FXCollections.observableArrayList();
        // Join with Room, Guest, Employee for display names
        String sql = "SELECT mr.*, r.RoomNumber, " +
                "g.FirstName || ' ' || g.LastName AS GuestName, " +
                "e.FirstName || ' ' || e.LastName AS EmployeeReporterName " +
                "FROM MaintenanceRequest mr " +
                "LEFT JOIN Room r ON mr.RoomID = r.RoomID " +
                "LEFT JOIN Guest g ON mr.ReportedByGuestID = g.GuestID " +
                "LEFT JOIN Employee e ON mr.ReportedByEmployeeID = e.EmployeeID " +
                "ORDER BY mr.ReportedDate DESC, mr.Priority, mr.RequestID DESC";
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                requests.add(mapRowToMaintenanceRequest(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all maintenance requests: " + e.getMessage());
        }
        return requests;
    }

    public boolean addMaintenanceRequest(MaintenanceRequest request) {
        String sql = "INSERT INTO MaintenanceRequest (RoomID, ReportedByGuestID, ReportedByEmployeeID, ReportedDate, " +
                "IssueDescription, Priority, Status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (request.getRoomID() != null) pstmt.setInt(1, request.getRoomID()); else pstmt.setNull(1, Types.INTEGER);
            if (request.getReportedByGuestID() != null) pstmt.setInt(2, request.getReportedByGuestID()); else pstmt.setNull(2, Types.INTEGER);
            if (request.getReportedByEmployeeID() != null) pstmt.setInt(3, request.getReportedByEmployeeID()); else pstmt.setNull(3, Types.INTEGER);

            if (request.getReportedDate() != null) pstmt.setDate(4, Date.valueOf(request.getReportedDate())); else pstmt.setDate(4, Date.valueOf(java.time.LocalDate.now()));

            pstmt.setString(5, request.getIssueDescription());
            pstmt.setString(6, request.getPriority() != null ? request.getPriority() : "Medium");
            pstmt.setString(7, request.getStatus() != null ? request.getStatus() : "Open");

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        request.setRequestID(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error adding maintenance request: " + e.getMessage());
            return false;
        }
    }

    public boolean updateMaintenanceRequestStatus(int requestId, String newStatus, String newPriority) {
        String sql = "UPDATE MaintenanceRequest SET Status = ?, Priority = ? WHERE RequestID = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setString(2, newPriority);
            pstmt.setInt(3, requestId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating maintenance request ID " + requestId + ": " + e.getMessage());
            return false;
        }
    }

    // TODO: Add more specific query methods (e.g., getOpenRequests, getRequestsByRoom)

    private MaintenanceRequest mapRowToMaintenanceRequest(ResultSet rs) throws SQLException {
        MaintenanceRequest req = new MaintenanceRequest();
        req.setRequestID(rs.getInt("RequestID"));
        int roomId = rs.getInt("RoomID"); if(!rs.wasNull()) req.setRoomID(roomId);
        int guestId = rs.getInt("ReportedByGuestID"); if(!rs.wasNull()) req.setReportedByGuestID(guestId);
        int empId = rs.getInt("ReportedByEmployeeID"); if(!rs.wasNull()) req.setReportedByEmployeeID(empId);

        Date reportedDateSql = rs.getDate("ReportedDate");
        if (reportedDateSql != null) {
            req.setReportedDate(reportedDateSql.toLocalDate());
        }
        req.setIssueDescription(rs.getString("IssueDescription"));
        req.setPriority(rs.getString("Priority"));
        req.setStatus(rs.getString("Status"));

        // Populating transient fields
        req.setRoomNumber(rs.getString("RoomNumber"));
        req.setGuestName(rs.getString("GuestName"));
        req.setEmployeeName(rs.getString("EmployeeReporterName")); // This alias is for reporter

        return req;
    }
}