package com.example.demo10.raffles.hotelmgmt.dao;

import com.example.demo10.raffles.hotelmgmt.DatabaseConnector;
import com.example.demo10.raffles.hotelmgmt.model.RoomType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.math.BigDecimal; // For BaseRate

public class RoomTypeDAO {

    public ObservableList<RoomType> getAllRoomTypes() {
        ObservableList<RoomType> roomTypes = FXCollections.observableArrayList();
        String sql = "SELECT RoomTypeID, TypeName, Description, BaseRate, DefaultMaxOccupancy, IncludedAmenities " +
                "FROM RoomType ORDER BY TypeName";
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                roomTypes.add(mapRowToRoomType(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all room types: " + e.getMessage());
        }
        return roomTypes;
    }

    public RoomType getRoomTypeById(int roomTypeId) {
        String sql = "SELECT RoomTypeID, TypeName, Description, BaseRate, DefaultMaxOccupancy, IncludedAmenities " +
                "FROM RoomType WHERE RoomTypeID = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roomTypeId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRowToRoomType(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching room type by ID " + roomTypeId + ": " + e.getMessage());
        }
        return null;
    }

    // Implement CRUD operations for room types

    public boolean addRoomType(RoomType roomType) {
        String sql = "INSERT INTO RoomType (TypeName, Description, BaseRate, DefaultMaxOccupancy, IncludedAmenities) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, roomType.getTypeName());
            pstmt.setString(2, roomType.getDescription());
            pstmt.setBigDecimal(3, roomType.getBaseRate());
            pstmt.setInt(4, roomType.getDefaultMaxOccupancy() != null ? roomType.getDefaultMaxOccupancy() : 2);
            pstmt.setString(5, roomType.getIncludedAmenities());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        roomType.setRoomTypeID(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error adding room type: " + e.getMessage());
            return false;
        }
    }

    public boolean updateRoomType(RoomType roomType) {
        String sql = "UPDATE RoomType SET TypeName = ?, Description = ?, BaseRate = ?, " +
                "DefaultMaxOccupancy = ?, IncludedAmenities = ? WHERE RoomTypeID = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, roomType.getTypeName());
            pstmt.setString(2, roomType.getDescription());
            pstmt.setBigDecimal(3, roomType.getBaseRate());
            pstmt.setInt(4, roomType.getDefaultMaxOccupancy() != null ? roomType.getDefaultMaxOccupancy() : 2);
            pstmt.setString(5, roomType.getIncludedAmenities());
            pstmt.setInt(6, roomType.getRoomTypeID());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating room type: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteRoomType(int roomTypeId) {
        // First check if this room type is in use
        String checkSql = "SELECT COUNT(*) FROM Room WHERE RoomTypeID = ?";
        String deleteSql = "DELETE FROM RoomType WHERE RoomTypeID = ?";
        
        try (Connection conn = DatabaseConnector.getConnection()) {
            // Check if any rooms are using this type
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, roomTypeId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    // Cannot delete if rooms are using this type
                    System.err.println("Cannot delete room type: it is in use by " + rs.getInt(1) + " rooms");
                    return false;
                }
            }
            
            // No rooms using this type, safe to delete
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, roomTypeId);
                return deleteStmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting room type: " + e.getMessage());
            return false;
        }
    }

    private RoomType mapRowToRoomType(ResultSet rs) throws SQLException {
        RoomType rt = new RoomType();
        rt.setRoomTypeID(rs.getInt("RoomTypeID"));
        rt.setTypeName(rs.getString("TypeName"));
        rt.setDescription(rs.getString("Description")); // getString for CLOB is fine for HSQLDB for reasonable sizes
        rt.setBaseRate(rs.getBigDecimal("BaseRate"));
        rt.setDefaultMaxOccupancy(rs.getInt("DefaultMaxOccupancy")); // Assuming it's NOT NULL or handle getObject
        rt.setIncludedAmenities(rs.getString("IncludedAmenities"));
        return rt;
    }
}