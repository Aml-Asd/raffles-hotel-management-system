package com.example.demo10.raffles.hotelmgmt.dao;

import com.example.demo10.raffles.hotelmgmt.DatabaseConnector;
import com.example.demo10.raffles.hotelmgmt.model.Room;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

public class RoomDAO {

    public ObservableList<Room> getAllRooms() {
        ObservableList<Room> rooms = FXCollections.observableArrayList();
        // Join with RoomType to get TypeName for display
        String sql = "SELECT r.RoomID, r.RoomNumber, r.RoomTypeID, rt.TypeName AS RoomTypeName, r.FloorNumber, " +
                "r.Status, r.Features, r.MaxOccupancy, r.IsSmoking, r.Notes " +
                "FROM Room r " +
                "LEFT JOIN RoomType rt ON r.RoomTypeID = rt.RoomTypeID " +
                "ORDER BY r.RoomNumber";
        
        System.out.println("Getting all rooms with SQL: " + sql);
        
        try (Connection conn = DatabaseConnector.getConnection()) {
            System.out.println("Database connection established: " + (conn != null));
            
            if (conn != null) {
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    
                    int count = 0;
                    while (rs.next()) {
                        Room room = mapRowToRoom(rs);
                        rooms.add(room);
                        count++;
                        System.out.println("Loaded room: " + room.getRoomNumber() + ", Type: " + room.getRoomTypeName() + ", Status: " + room.getStatus());
                    }
                    
                    System.out.println("Total rooms loaded: " + count);
                }
            } else {
                System.err.println("Failed to get database connection");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all rooms: " + e.getMessage());
            e.printStackTrace();
        }
        
        return rooms;
    }

    public Room getRoomById(int roomId) {
        String sql = "SELECT r.RoomID, r.RoomNumber, r.RoomTypeID, rt.TypeName AS RoomTypeName, r.FloorNumber, " +
                "r.Status, r.Features, r.MaxOccupancy, r.IsSmoking, r.Notes " +
                "FROM Room r " +
                "LEFT JOIN RoomType rt ON r.RoomTypeID = rt.RoomTypeID " +
                "WHERE r.RoomID = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roomId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRowToRoom(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching room by ID " + roomId + ": " + e.getMessage());
        }
        return null;
    }

    // Implementation of room management operations
    public boolean addRoom(Room room) {
        String sql = "INSERT INTO Room (RoomNumber, RoomTypeID, FloorNumber, Status, Features, MaxOccupancy, IsSmoking, Notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        System.out.println("Adding room with number: " + room.getRoomNumber() + ", Type ID: " + room.getRoomTypeID());
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            
            // Start transaction for consistency
            conn.setAutoCommit(false);
            
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            // Validate room data
            if (room.getRoomNumber() == null || room.getRoomNumber().trim().isEmpty()) {
                System.err.println("Error adding room: Room number cannot be empty");
                return false;
            }
            
            pstmt.setString(1, room.getRoomNumber().trim());
            
            if (room.getRoomTypeID() != null) {
                pstmt.setInt(2, room.getRoomTypeID());
            } else {
                pstmt.setNull(2, Types.INTEGER);
                System.err.println("Warning: Adding room with null room type ID");
            }
            
            if (room.getFloorNumber() != null) {
                pstmt.setInt(3, room.getFloorNumber());
            } else {
                // Default to floor 1 if not specified
                pstmt.setInt(3, 1);
                System.err.println("Warning: Using default floor (1) for room " + room.getRoomNumber());
            }
            
            // Use "Available" as default status if null or empty
            String status = (room.getStatus() != null && !room.getStatus().trim().isEmpty()) ? 
                            room.getStatus() : "Available";
            pstmt.setString(4, status);
            
            // Features can be null
            pstmt.setString(5, room.getFeatures());
            
            // Default max occupancy to 2 if not specified
            if (room.getMaxOccupancy() != null) {
                pstmt.setInt(6, room.getMaxOccupancy());
            } else {
                pstmt.setInt(6, 2);
            }
            
            pstmt.setBoolean(7, room.isSmoking());
            pstmt.setString(8, room.getNotes());
            
            int affectedRows = pstmt.executeUpdate();
            boolean success = false;
            
            if (affectedRows > 0) {
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int newId = generatedKeys.getInt(1);
                    room.setRoomID(newId);
                    
                    // Get room type name if not provided
                    if (room.getRoomTypeName() == null || room.getRoomTypeName().isEmpty()) {
                        // Get the room type name from the database
                        String typeSql = "SELECT TypeName FROM RoomType WHERE RoomTypeID = ?";
                        try (PreparedStatement typeStmt = conn.prepareStatement(typeSql)) {
                            typeStmt.setInt(1, room.getRoomTypeID());
                            ResultSet typeRs = typeStmt.executeQuery();
                            if (typeRs.next()) {
                                room.setRoomTypeName(typeRs.getString("TypeName"));
                            }
                        }
                    }
                    
                    System.out.println("Room added successfully with ID: " + newId + 
                                      ", Number: " + room.getRoomNumber() + 
                                      ", Type: " + room.getRoomTypeName());
                    success = true;
                }
            }
            
            // Commit the transaction on success
            conn.commit();
            return success;
            
        } catch (SQLException e) {
            // Rollback transaction on error
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Error during rollback: " + rollbackEx.getMessage());
            }
            
            System.err.println("Error adding room: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // Make sure everything is closed properly
            try {
                if (generatedKeys != null) generatedKeys.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    // Don't close the connection here - DatabaseConnector handles that
                }
            } catch (SQLException closeEx) {
                System.err.println("Error closing resources: " + closeEx.getMessage());
            }
        }
    }
    
    public boolean updateRoom(Room room) {
        String sql = "UPDATE Room SET RoomNumber = ?, RoomTypeID = ?, FloorNumber = ?, " +
                     "Status = ?, Features = ?, MaxOccupancy = ?, IsSmoking = ?, Notes = ? " +
                     "WHERE RoomID = ?";
        
        System.out.println("Updating room ID: " + room.getRoomID() + ", Number: " + room.getRoomNumber());
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            
            // Start transaction
            conn.setAutoCommit(false);
            
            pstmt = conn.prepareStatement(sql);
            
            // Validate room data
            if (room.getRoomNumber() == null || room.getRoomNumber().trim().isEmpty()) {
                System.err.println("Error updating room: Room number cannot be empty");
                return false;
            }
            
            pstmt.setString(1, room.getRoomNumber().trim());
            
            if (room.getRoomTypeID() != null) {
                pstmt.setInt(2, room.getRoomTypeID());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            
            if (room.getFloorNumber() != null) {
                pstmt.setInt(3, room.getFloorNumber());
            } else {
                pstmt.setInt(3, 1);
            }
            
            // Use "Available" as default status if null or empty
            String status = (room.getStatus() != null && !room.getStatus().trim().isEmpty()) ? 
                            room.getStatus() : "Available";
            pstmt.setString(4, status);
            
            pstmt.setString(5, room.getFeatures());
            
            if (room.getMaxOccupancy() != null) {
                pstmt.setInt(6, room.getMaxOccupancy());
            } else {
                pstmt.setInt(6, 2);
            }
            
            pstmt.setBoolean(7, room.isSmoking());
            pstmt.setString(8, room.getNotes());
            pstmt.setInt(9, room.getRoomID());
            
            int affectedRows = pstmt.executeUpdate();
            
            // Get room type name if needed
            if (room.getRoomTypeID() != null && (room.getRoomTypeName() == null || room.getRoomTypeName().isEmpty())) {
                String typeSql = "SELECT TypeName FROM RoomType WHERE RoomTypeID = ?";
                try (PreparedStatement typeStmt = conn.prepareStatement(typeSql)) {
                    typeStmt.setInt(1, room.getRoomTypeID());
                    ResultSet typeRs = typeStmt.executeQuery();
                    if (typeRs.next()) {
                        room.setRoomTypeName(typeRs.getString("TypeName"));
                    }
                }
            }
            
            // Commit transaction
            conn.commit();
            
            if (affectedRows > 0) {
                System.out.println("Room updated successfully: ID=" + room.getRoomID());
                return true;
            } else {
                System.err.println("Room update failed: No matching room found with ID " + room.getRoomID());
                return false;
            }
            
        } catch (SQLException e) {
            // Rollback on error
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Error during rollback: " + rollbackEx.getMessage());
            }
            
            System.err.println("Error updating room: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException closeEx) {
                System.err.println("Error closing resources: " + closeEx.getMessage());
            }
        }
    }
    
    public boolean updateRoomStatus(int roomID, String newStatus) {
        String sql = "UPDATE Room SET Status = ? WHERE RoomID = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            conn.setAutoCommit(false);
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, roomID);
            
            int affectedRows = pstmt.executeUpdate();
            
            conn.commit();
            
            if (affectedRows > 0) {
                System.out.println("Room " + roomID + " status updated to: " + newStatus);
                return true;
            } else {
                System.err.println("Failed to update room status: No room with ID " + roomID);
                return false;
            }
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Error during rollback: " + rollbackEx.getMessage());
            }
            
            System.err.println("Error updating room status: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException closeEx) {
                System.err.println("Error closing resources: " + closeEx.getMessage());
            }
        }
    }
    
    public boolean deleteRoom(int roomId) {
        String sql = "DELETE FROM Room WHERE RoomID = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roomId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting room: " + e.getMessage());
            return false;
        }
    }
    
    public ObservableList<Room> getRoomsByFloor(int floorNumber) {
        ObservableList<Room> rooms = FXCollections.observableArrayList();
        String sql = "SELECT r.RoomID, r.RoomNumber, r.RoomTypeID, rt.TypeName AS RoomTypeName, r.FloorNumber, " +
                "r.Status, r.Features, r.MaxOccupancy, r.IsSmoking, r.Notes " +
                "FROM Room r " +
                "LEFT JOIN RoomType rt ON r.RoomTypeID = rt.RoomTypeID " +
                "WHERE r.FloorNumber = ? " +
                "ORDER BY r.RoomNumber";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, floorNumber);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                rooms.add(mapRowToRoom(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching rooms by floor: " + e.getMessage());
        }
        return rooms;
    }
    
    public ObservableList<Room> getRoomsByType(int roomTypeId) {
        ObservableList<Room> rooms = FXCollections.observableArrayList();
        String sql = "SELECT r.RoomID, r.RoomNumber, r.RoomTypeID, rt.TypeName AS RoomTypeName, r.FloorNumber, " +
                "r.Status, r.Features, r.MaxOccupancy, r.IsSmoking, r.Notes " +
                "FROM Room r " +
                "LEFT JOIN RoomType rt ON r.RoomTypeID = rt.RoomTypeID " +
                "WHERE r.RoomTypeID = ? " +
                "ORDER BY r.RoomNumber";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roomTypeId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                rooms.add(mapRowToRoom(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching rooms by type: " + e.getMessage());
        }
        return rooms;
    }
    
    public ObservableList<Room> getRoomsByStatus(String status) {
        ObservableList<Room> rooms = FXCollections.observableArrayList();
        String sql = "SELECT r.RoomID, r.RoomNumber, r.RoomTypeID, rt.TypeName AS RoomTypeName, r.FloorNumber, " +
                "r.Status, r.Features, r.MaxOccupancy, r.IsSmoking, r.Notes " +
                "FROM Room r " +
                "LEFT JOIN RoomType rt ON r.RoomTypeID = rt.RoomTypeID " +
                "WHERE r.Status = ? " +
                "ORDER BY r.RoomNumber";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                rooms.add(mapRowToRoom(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching rooms by status: " + e.getMessage());
        }
        return rooms;
    }
    
    public int countRoomsByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM Room WHERE Status = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting rooms by status: " + e.getMessage());
        }
        return 0;
    }
    
    public int getTotalRoomCount() {
        String sql = "SELECT COUNT(*) FROM Room";
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting total rooms: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Checks if a room is available for booking during a specific date range
     * @param roomId the room ID to check
     * @param checkInDate the requested check-in date
     * @param checkOutDate the requested check-out date
     * @return true if the room is available, false otherwise
     */
    public boolean isRoomAvailable(int roomId, java.time.LocalDate checkInDate, java.time.LocalDate checkOutDate) {
        String sql = "SELECT COUNT(*) FROM Reservation r " +
                     "WHERE r.RoomID = ? " +
                     "AND r.Status NOT IN ('Cancelled', 'No-Show') " +
                     "AND NOT ((r.CheckOutDate <= ?) OR (r.CheckInDate >= ?))";
        
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, roomId);
            pstmt.setDate(2, java.sql.Date.valueOf(checkInDate));
            pstmt.setDate(3, java.sql.Date.valueOf(checkOutDate));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int conflictCount = rs.getInt(1);
                    return conflictCount == 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking room availability: " + e.getMessage());
            e.printStackTrace();
        }
        
        // If there was an error, assume room is not available
        return false;
    }

    private Room mapRowToRoom(ResultSet rs) throws SQLException {
        Room room = new Room();
        room.setRoomID(rs.getInt("RoomID"));
        room.setRoomNumber(rs.getString("RoomNumber"));
        // Handle nullable RoomTypeID
        int roomTypeId = rs.getInt("RoomTypeID");
        if (!rs.wasNull()) {
            room.setRoomTypeID(roomTypeId);
        } else {
            room.setRoomTypeID(null);
        }
        room.setRoomTypeName(rs.getString("RoomTypeName")); // From join
        room.setFloorNumber(rs.getInt("FloorNumber")); // Assuming NOT NULL or handle getObject
        room.setStatus(rs.getString("Status"));
        room.setFeatures(rs.getString("Features"));
        room.setMaxOccupancy(rs.getInt("MaxOccupancy")); // Assuming NOT NULL or handle getObject
        room.setSmoking(rs.getBoolean("IsSmoking"));
        room.setNotes(rs.getString("Notes"));
        return room;
    }
}