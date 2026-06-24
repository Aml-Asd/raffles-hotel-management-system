package com.example.demo10.raffles.hotelmgmt.dao;

import com.example.demo10.raffles.hotelmgmt.DatabaseConnector;
import com.example.demo10.raffles.hotelmgmt.model.Reservation;
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

public class ReservationDAO {

    public ObservableList<Reservation> getAllReservationsWithDetails() {
        ObservableList<Reservation> reservations = FXCollections.observableArrayList();
        // Join with Guest, Room, RoomType to get names/numbers for display
        String sql = "SELECT res.*, " +
                "g.FirstName || ' ' || g.LastName AS GuestName, " +
                "r.RoomNumber, rt.TypeName AS RoomTypeName " +
                "FROM Reservation res " +
                "LEFT JOIN Guest g ON res.GuestID = g.GuestID " +
                "LEFT JOIN Room r ON res.RoomID = r.RoomID " +
                "LEFT JOIN RoomType rt ON res.RoomTypeID = rt.RoomTypeID " +
                "ORDER BY res.CheckInDate DESC, res.ReservationID DESC";
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                reservations.add(mapRowToReservationWithDetails(rs));
            }
            System.out.println("Retrieved " + reservations.size() + " reservations from database");
        } catch (SQLException e) {
            System.err.println("Error fetching all reservations with details: " + e.getMessage());
            e.printStackTrace();
        }
        return reservations;
    }

    /**
     * Get reservations filtered by date range and/or status
     * @param fromDate Start date for reservations (check-in date)
     * @param toDate End date for reservations (check-in date)
     * @param status Reservation status filter (can be null for all statuses)
     * @return List of reservations matching the criteria
     */
    public ObservableList<Reservation> searchReservations(LocalDate fromDate, LocalDate toDate, String status) {
        ObservableList<Reservation> reservations = FXCollections.observableArrayList();
        
        StringBuilder sqlBuilder = new StringBuilder(
            "SELECT res.*, " +
            "g.FirstName || ' ' || g.LastName AS GuestName, " +
            "r.RoomNumber, rt.TypeName AS RoomTypeName " +
            "FROM Reservation res " +
            "LEFT JOIN Guest g ON res.GuestID = g.GuestID " +
            "LEFT JOIN Room r ON res.RoomID = r.RoomID " +
            "LEFT JOIN RoomType rt ON res.RoomTypeID = rt.RoomTypeID " +
            "WHERE 1=1 "
        );
        
        // Add date range conditions if provided
        if (fromDate != null) {
            sqlBuilder.append("AND res.CheckInDate >= ? ");
        }
        
        if (toDate != null) {
            sqlBuilder.append("AND res.CheckInDate <= ? ");
        }
        
        // Add status condition if provided and not "All Statuses"
        if (status != null && !status.isEmpty() && !status.equals("All Statuses")) {
            sqlBuilder.append("AND res.Status = ? ");
        }
        
        sqlBuilder.append("ORDER BY res.CheckInDate ASC, res.ReservationID DESC");
        
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlBuilder.toString())) {
            
            int paramIndex = 1;
            
            if (fromDate != null) {
                pstmt.setDate(paramIndex++, Date.valueOf(fromDate));
            }
            
            if (toDate != null) {
                pstmt.setDate(paramIndex++, Date.valueOf(toDate));
            }
            
            if (status != null && !status.isEmpty() && !status.equals("All Statuses")) {
                pstmt.setString(paramIndex, status);
            }
            
            System.out.println("Executing search query: " + sqlBuilder.toString());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                reservations.add(mapRowToReservationWithDetails(rs));
            }
            System.out.println("Search found " + reservations.size() + " reservations matching criteria");
            
        } catch (SQLException e) {
            System.err.println("Error searching reservations: " + e.getMessage());
            e.printStackTrace();
        }
        
        return reservations;
    }

    /**
     * Add a new reservation to the database
     * @param reservation The reservation to add
     * @return true if successful, false otherwise
     */
    public boolean addReservation(Reservation reservation) {
        String sql = "INSERT INTO Reservation (GuestID, RoomID, RoomTypeID, CheckInDate, CheckOutDate, " +
                "NumberOfAdults, NumberOfChildren, Status, BookingSource, TotalEstimatedCost, " +
                "DateBooked, SpecialRequests, Notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Set parameters, handling nulls appropriately
            if (reservation.getGuestID() != null) {
                pstmt.setInt(1, reservation.getGuestID());
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }

            if (reservation.getRoomID() != null) {
                pstmt.setInt(2, reservation.getRoomID());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }

            if (reservation.getRoomTypeID() != null) {
                pstmt.setInt(3, reservation.getRoomTypeID());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }

            // Required dates
            if (reservation.getCheckInDate() != null) {
                pstmt.setDate(4, Date.valueOf(reservation.getCheckInDate()));
            } else {
                throw new SQLException("Check-in date must not be null");
            }

            if (reservation.getCheckOutDate() != null) {
                pstmt.setDate(5, Date.valueOf(reservation.getCheckOutDate()));
            } else {
                throw new SQLException("Check-out date must not be null");
            }

            // Adults and children can be null
            if (reservation.getNumberOfAdults() != null) {
                pstmt.setInt(6, reservation.getNumberOfAdults());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }

            if (reservation.getNumberOfChildren() != null) {
                pstmt.setInt(7, reservation.getNumberOfChildren());
            } else {
                pstmt.setNull(7, Types.INTEGER);
            }

            // Status defaults to "Confirmed" if not provided
            pstmt.setString(8, reservation.getStatus() != null ? reservation.getStatus() : "Confirmed");

            pstmt.setString(9, reservation.getBookingSource());
            pstmt.setBigDecimal(10, reservation.getTotalEstimatedCost());

            // Default dateBooked to current date if not provided
            LocalDate dateBooked = reservation.getDateBooked();
            if (dateBooked == null) {
                dateBooked = LocalDate.now();
            }
            pstmt.setDate(11, Date.valueOf(dateBooked));

            pstmt.setString(12, reservation.getSpecialRequests());
            pstmt.setString(13, reservation.getNotes());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        reservation.setReservationID(generatedKeys.getInt(1));
                        System.out.println("Reservation created with ID: " + reservation.getReservationID());
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error adding reservation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update the status of a reservation
     * @param reservationId The ID of the reservation to update
     * @param newStatus The new status to set
     * @return True if successful, false otherwise
     */
    public boolean updateReservationStatus(int reservationId, String newStatus) {
        String sql = "UPDATE Reservation SET Status = ? WHERE ReservationID = ?";
        
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, reservationId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating reservation status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Check if a room is available for the given date range
     * @param roomId The room ID to check
     * @param checkInDate The check-in date
     * @param checkOutDate The check-out date
     * @return true if available, false if not
     */
    public boolean isRoomAvailable(int roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        String sql = "SELECT COUNT(*) FROM Reservation " +
                "WHERE RoomID = ? " +
                "AND Status NOT IN ('Cancelled', 'Completed') " +
                "AND ((CheckInDate <= ? AND CheckOutDate > ?) OR " +
                "(CheckInDate < ? AND CheckOutDate >= ?) OR " +
                "(CheckInDate >= ? AND CheckOutDate <= ?))";
        
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, roomId);
            pstmt.setDate(2, Date.valueOf(checkOutDate));
            pstmt.setDate(3, Date.valueOf(checkInDate));
            pstmt.setDate(4, Date.valueOf(checkOutDate));
            pstmt.setDate(5, Date.valueOf(checkInDate));
            pstmt.setDate(6, Date.valueOf(checkInDate));
            pstmt.setDate(7, Date.valueOf(checkOutDate));
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                return count == 0; // If count is 0, the room is available
            }
        } catch (SQLException e) {
            System.err.println("Error checking room availability: " + e.getMessage());
        }
        return false; // Default to not available if there's an error
    }

    /**
     * Get all reservations for a specific guest
     * @param guestId The ID of the guest
     * @return List of reservations for this guest
     */
    public ObservableList<Reservation> getReservationsByGuestID(int guestId) {
        ObservableList<Reservation> reservations = FXCollections.observableArrayList();
        
        String sql = "SELECT res.*, " +
                "g.FirstName || ' ' || g.LastName AS GuestName, " +
                "r.RoomNumber, rt.TypeName AS RoomTypeName " +
                "FROM Reservation res " +
                "LEFT JOIN Guest g ON res.GuestID = g.GuestID " +
                "LEFT JOIN Room r ON res.RoomID = r.RoomID " +
                "LEFT JOIN RoomType rt ON res.RoomTypeID = rt.RoomTypeID " +
                "WHERE res.GuestID = ? " +
                "ORDER BY res.CheckInDate DESC";
                
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, guestId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapRowToReservationWithDetails(rs));
                }
                System.out.println("Retrieved " + reservations.size() + " reservations for guest ID " + guestId);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching reservations for guest ID " + guestId + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return reservations;
    }

    /**
     * Get a single reservation by ID with full details
     * @param reservationId The reservation ID
     * @return The reservation with all joined details, or null if not found
     */
    public Reservation getReservationByIdWithDetails(int reservationId) {
        String sql = "SELECT res.*, " +
                "g.FirstName || ' ' || g.LastName AS GuestName, " +
                "r.RoomNumber, rt.TypeName AS RoomTypeName " +
                "FROM Reservation res " +
                "LEFT JOIN Guest g ON res.GuestID = g.GuestID " +
                "LEFT JOIN Room r ON res.RoomID = r.RoomID " +
                "LEFT JOIN RoomType rt ON res.RoomTypeID = rt.RoomTypeID " +
                "WHERE res.ReservationID = ?";
                
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, reservationId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToReservationWithDetails(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching reservation by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    private Reservation mapRowToReservationWithDetails(ResultSet rs) throws SQLException {
        Reservation res = new Reservation();
        res.setReservationID(rs.getInt("ReservationID"));

        int guestId = rs.getInt("GuestID");
        if (!rs.wasNull()) res.setGuestID(guestId);
        int roomId = rs.getInt("RoomID");
        if (!rs.wasNull()) res.setRoomID(roomId);
        int roomTypeId = rs.getInt("RoomTypeID");
        if (!rs.wasNull()) res.setRoomTypeID(roomTypeId);

        res.setCheckInDate(rs.getDate("CheckInDate").toLocalDate());
        res.setCheckOutDate(rs.getDate("CheckOutDate").toLocalDate());

        res.setNumberOfAdults(rs.getObject("NumberOfAdults") != null ? rs.getInt("NumberOfAdults") : null);
        res.setNumberOfChildren(rs.getObject("NumberOfChildren") != null ? rs.getInt("NumberOfChildren") : null);

        res.setStatus(rs.getString("Status"));
        res.setBookingSource(rs.getString("BookingSource"));
        res.setTotalEstimatedCost(rs.getBigDecimal("TotalEstimatedCost"));

        Date dateBookedSql = rs.getDate("DateBooked");
        if (dateBookedSql != null) res.setDateBooked(dateBookedSql.toLocalDate());

        res.setSpecialRequests(rs.getString("SpecialRequests"));
        res.setNotes(rs.getString("Notes"));

        // Populating transient fields from JOINs
        res.setGuestName(rs.getString("GuestName"));
        res.setRoomNumber(rs.getString("RoomNumber"));
        res.setRoomTypeName(rs.getString("RoomTypeName"));

        return res;
    }
}