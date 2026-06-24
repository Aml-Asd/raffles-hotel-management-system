package com.example.demo10.raffles.hotelmgmt.dao; // Correct package

import com.example.demo10.raffles.hotelmgmt.DatabaseConnector; // Your connector
import com.example.demo10.raffles.hotelmgmt.model.Guest;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.Optional;

public class GuestDAO {

    public boolean addGuest(Guest guest) {
        String sql = "INSERT INTO Guest (FirstName, LastName, Email, PhoneNumber, Address, Nationality, DateOfBirth, " +
                "IdentificationType, IdentificationNumber, LoyaltyProgramID, Preferences, Notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            mapGuestToStatement(pstmt, guest);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        guest.setGuestID(generatedKeys.getInt(1)); // Set the auto-generated ID back to the object
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error adding guest: " + e.getMessage());
            if (e.getSQLState().equals("23505")) { // HSQLDB unique constraint violation (likely Email)
                System.err.println("A guest with this email may already exist.");
            }
            return false;
        }
    }

    public boolean updateGuest(Guest guest) {
        String sql = "UPDATE Guest SET FirstName = ?, LastName = ?, Email = ?, PhoneNumber = ?, Address = ?, " +
                "Nationality = ?, DateOfBirth = ?, IdentificationType = ?, IdentificationNumber = ?, " +
                "LoyaltyProgramID = ?, Preferences = ?, Notes = ? WHERE GuestID = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            mapGuestToStatement(pstmt, guest);
            pstmt.setInt(13, guest.getGuestID()); // Set the GuestID for the WHERE clause

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating guest with ID " + guest.getGuestID() + ": " + e.getMessage());
            if (e.getSQLState().equals("23505")) {
                System.err.println("Update failed: A guest with this email may already exist (other than the current one).");
            }
            return false;
        }
    }

    public ObservableList<Guest> getAllGuests() {
        ObservableList<Guest> guests = FXCollections.observableArrayList();
        // Select all columns as defined in the Guest table
        String sql = "SELECT GuestID, FirstName, LastName, Email, PhoneNumber, Address, Nationality, DateOfBirth, " +
                "IdentificationType, IdentificationNumber, LoyaltyProgramID, Preferences, Notes " +
                "FROM Guest ORDER BY LastName, FirstName";
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                guests.add(mapRowToGuest(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all guests: " + e.getMessage());
        }
        return guests;
    }

    public boolean deleteGuest(int guestId) {
        String sql = "DELETE FROM Guest WHERE GuestID = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, guestId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting guest with ID " + guestId + ": " + e.getMessage());
            // HSQLDB ON DELETE CASCADE in Reservation table should handle linked reservations.
            // If other tables have FKs to Guest without ON DELETE CASCADE/SET NULL, this might fail.
            if (e.getSQLState().startsWith("23")) {
                System.err.println("Cannot delete guest. They might be referenced in other tables that do not cascade deletes (e.g., Invoices if not set to cascade/set null).");
            }
            return false;
        }
    }

    public long getGuestCount() {
        String sql = "SELECT COUNT(*) FROM Guest";
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting guest count: " + e.getMessage());
        }
        return 0;
    }

    private void mapGuestToStatement(PreparedStatement pstmt, Guest guest) throws SQLException {
        pstmt.setString(1, guest.getFirstName());
        pstmt.setString(2, guest.getLastName());
        pstmt.setString(3, guest.getEmail());
        pstmt.setString(4, guest.getPhoneNumber());
        pstmt.setString(5, guest.getAddress());
        pstmt.setString(6, guest.getNationality());
        if (guest.getDateOfBirth() != null) {
            pstmt.setDate(7, Date.valueOf(guest.getDateOfBirth()));
        } else {
            pstmt.setNull(7, Types.DATE);
        }
        pstmt.setString(8, guest.getIdentificationType());
        pstmt.setString(9, guest.getIdentificationNumber());
        pstmt.setString(10, guest.getLoyaltyProgramID());

        // Handling CLOB fields: setString should work for HSQLDB with reasonable string lengths.
        // For very large strings, or other databases, you might need setClob or setCharacterStream.
        pstmt.setString(11, guest.getPreferences());
        pstmt.setString(12, guest.getNotes());
    }

    private Guest mapRowToGuest(ResultSet rs) throws SQLException {
        Guest guest = new Guest();
        guest.setGuestID(rs.getInt("GuestID"));
        guest.setFirstName(rs.getString("FirstName"));
        guest.setLastName(rs.getString("LastName"));
        guest.setEmail(rs.getString("Email"));
        guest.setPhoneNumber(rs.getString("PhoneNumber"));
        guest.setAddress(rs.getString("Address"));
        guest.setNationality(rs.getString("Nationality"));
        Date dobSql = rs.getDate("DateOfBirth");
        if (dobSql != null) {
            guest.setDateOfBirth(dobSql.toLocalDate());
        }
        guest.setIdentificationType(rs.getString("IdentificationType"));
        guest.setIdentificationNumber(rs.getString("IdentificationNumber"));
        guest.setLoyaltyProgramID(rs.getString("LoyaltyProgramID"));

        // Reading CLOB fields
        guest.setPreferences(rs.getString("Preferences"));
        guest.setNotes(rs.getString("Notes"));

        return guest;
    }

    /**
     * Get a guest by ID
     * @param guestId The guest ID to look up
     * @return Optional containing the guest if found, empty otherwise
     */
    public Optional<Guest> getGuestById(int guestId) {
        String sql = "SELECT GuestID, FirstName, LastName, Email, PhoneNumber, Address, Nationality, DateOfBirth, " +
                "IdentificationType, IdentificationNumber, LoyaltyProgramID, Preferences, Notes " +
                "FROM Guest WHERE GuestID = ?";
        
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, guestId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToGuest(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching guest by ID " + guestId + ": " + e.getMessage());
        }
        
        return Optional.empty();
    }
}