package com.example.demo10.raffles.hotelmgmt.dao;

import com.example.demo10.raffles.hotelmgmt.DatabaseConnector;
import com.example.demo10.raffles.hotelmgmt.model.Invoice;
import com.example.demo10.raffles.hotelmgmt.model.Reservation;
import com.example.demo10.raffles.hotelmgmt.model.Charge;
import com.example.demo10.raffles.hotelmgmt.util.DatabaseResetUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.Date;
import java.sql.Types;
import java.time.LocalDate;
import java.math.BigDecimal;

public class InvoiceDAO {

    public ObservableList<Invoice> getAllInvoicesWithDetails() {
        ObservableList<Invoice> invoices = FXCollections.observableArrayList();
        // Join with Guest for guest name and Reservation for reservation details
        String sql = "SELECT i.*, " +
                "g.FirstName || ' ' || g.LastName AS GuestName, " +
                "r.RoomID, r.CheckInDate, r.CheckOutDate, room.RoomNumber " +
                "FROM Invoice i " +
                "LEFT JOIN Guest g ON i.GuestID = g.GuestID " +
                "LEFT JOIN Reservation r ON i.ReservationID = r.ReservationID " +
                "LEFT JOIN Room room ON r.RoomID = room.RoomID " +
                "ORDER BY i.InvoiceDate DESC, i.InvoiceID DESC";
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                invoices.add(mapResultSetToInvoice(rs));
            }
            System.out.println("Retrieved " + invoices.size() + " invoices from database");
        } catch (SQLException e) {
            System.err.println("Error fetching all invoices: " + e.getMessage());
            e.printStackTrace();
        }
        return invoices;
    }
    
    /**
     * Create a new invoice in the database
     * @param invoice The invoice to create
     * @return true if successful, false otherwise
     */
    public boolean createInvoice(Invoice invoice) {
        System.out.println("Creating invoice: " + invoice);
        
        // We're no longer enforcing GuestID as required at the database level
        // but for proper business logic we should verify it exists
        if (invoice.getGuestID() == null) {
            System.err.println("Warning: Creating invoice without guest ID is not recommended");
        }
        
        String sql = "INSERT INTO Invoice (ReservationID, GuestID, InvoiceDate, DueDate, " +
                     "TotalAmount, AmountPaid, BalanceDue, Status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();
            
            // Debug: Check if connection is valid
            if (conn == null || conn.isClosed()) {
                System.err.println("ERROR: Database connection is null or closed");
                return false;
            }
            
            // Debug: Check if Invoice table exists
            try {
                conn.createStatement().executeQuery("SELECT COUNT(*) FROM Invoice");
                System.out.println("Invoice table exists and is accessible");
            } catch (SQLException e) {
                System.err.println("ERROR: Invoice table does not exist or is not accessible: " + e.getMessage());
                System.out.println("Attempting to recreate Invoice table...");
                DatabaseResetUtil.resetInvoiceTables();
            }
            
            // Check if the guest exists
            String checkGuestSql = "SELECT COUNT(*) FROM Guest WHERE GuestID = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkGuestSql)) {
                checkStmt.setInt(1, invoice.getGuestID());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        System.err.println("Error: Guest with ID " + invoice.getGuestID() + " does not exist");
                        return false;
                    } else {
                        System.out.println("Guest with ID " + invoice.getGuestID() + " exists, proceeding with invoice creation");
                    }
                }
            }
            
            // Check if reservation exists if specified
            if (invoice.getReservationID() != null) {
                String checkResSql = "SELECT COUNT(*) FROM Reservation WHERE ReservationID = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkResSql)) {
                    checkStmt.setInt(1, invoice.getReservationID());
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) == 0) {
                            System.err.println("Error: Reservation with ID " + invoice.getReservationID() + " does not exist");
                            return false;
                        } else {
                            System.out.println("Reservation with ID " + invoice.getReservationID() + " exists");
                        }
                    }
                }
            }
            
            // Now create the invoice
            System.out.println("Executing SQL: " + sql);
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                // Set parameters with null handling
                if (invoice.getReservationID() != null) {
                    pstmt.setInt(1, invoice.getReservationID());
                    System.out.println("Parameter 1 (ReservationID): " + invoice.getReservationID());
                } else {
                    pstmt.setNull(1, Types.INTEGER);
                    System.out.println("Parameter 1 (ReservationID): NULL");
                }
                
                pstmt.setInt(2, invoice.getGuestID());
                System.out.println("Parameter 2 (GuestID): " + invoice.getGuestID());
                
                // Handle dates
                if (invoice.getInvoiceDate() != null) {
                    pstmt.setDate(3, Date.valueOf(invoice.getInvoiceDate()));
                    System.out.println("Parameter 3 (InvoiceDate): " + invoice.getInvoiceDate());
                } else {
                    LocalDate today = LocalDate.now();
                    pstmt.setDate(3, Date.valueOf(today));
                    System.out.println("Parameter 3 (InvoiceDate): " + today + " (default)");
                }
                
                if (invoice.getDueDate() != null) {
                    pstmt.setDate(4, Date.valueOf(invoice.getDueDate()));
                    System.out.println("Parameter 4 (DueDate): " + invoice.getDueDate());
                } else {
                    // Default due date to 30 days from now
                    LocalDate dueDate = LocalDate.now().plusDays(30);
                    pstmt.setDate(4, Date.valueOf(dueDate));
                    System.out.println("Parameter 4 (DueDate): " + dueDate + " (default)");
                }
                
                // Handle amounts
                BigDecimal totalAmount = invoice.getTotalAmount() != null ? 
                    invoice.getTotalAmount() : BigDecimal.ZERO;
                pstmt.setBigDecimal(5, totalAmount);
                System.out.println("Parameter 5 (TotalAmount): " + totalAmount);
                
                BigDecimal amountPaid = invoice.getAmountPaid() != null ? 
                    invoice.getAmountPaid() : BigDecimal.ZERO;
                pstmt.setBigDecimal(6, amountPaid);
                System.out.println("Parameter 6 (AmountPaid): " + amountPaid);
                    
                // Calculate balance due
                BigDecimal balanceDue = totalAmount.subtract(amountPaid);
                pstmt.setBigDecimal(7, balanceDue);
                System.out.println("Parameter 7 (BalanceDue): " + balanceDue);
                
                // Set status, defaulting to "Unpaid" if not provided
                String status = invoice.getStatus() != null ? invoice.getStatus() : "Unpaid";
                pstmt.setString(8, status);
                System.out.println("Parameter 8 (Status): " + status);
                
                System.out.println("Executing insert statement...");
                int affectedRows = pstmt.executeUpdate();
                System.out.println("Insert statement executed. Affected rows: " + affectedRows);
                
                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            invoice.setInvoiceID(generatedKeys.getInt(1));
                            System.out.println("Created invoice #" + invoice.getInvoiceID() + " successfully");
                            return true;
                        } else {
                            System.err.println("Failed to get generated invoice ID");
                        }
                    }
                }
                System.err.println("Failed to create invoice: No rows affected");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error creating invoice: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update an existing invoice
     * @param invoice The invoice with updated data
     * @return true if successful, false otherwise
     */
    public boolean updateInvoice(Invoice invoice) {
        String sql = "UPDATE Invoice SET ReservationID = ?, GuestID = ?, InvoiceDate = ?, " +
                     "DueDate = ?, TotalAmount = ?, AmountPaid = ?, BalanceDue = ?, Status = ? " +
                     "WHERE InvoiceID = ?";
                     
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Set parameters with null handling
            if (invoice.getReservationID() != null) {
                pstmt.setInt(1, invoice.getReservationID());
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }
            
            if (invoice.getGuestID() != null) {
                pstmt.setInt(2, invoice.getGuestID());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            
            // Handle dates
            if (invoice.getInvoiceDate() != null) {
                pstmt.setDate(3, Date.valueOf(invoice.getInvoiceDate()));
            } else {
                pstmt.setNull(3, Types.DATE);
            }
            
            if (invoice.getDueDate() != null) {
                pstmt.setDate(4, Date.valueOf(invoice.getDueDate()));
            } else {
                pstmt.setNull(4, Types.DATE);
            }
            
            // Handle amounts
            pstmt.setBigDecimal(5, invoice.getTotalAmount() != null ? 
                invoice.getTotalAmount() : BigDecimal.ZERO);
            pstmt.setBigDecimal(6, invoice.getAmountPaid() != null ? 
                invoice.getAmountPaid() : BigDecimal.ZERO);
                
            // Calculate balance due
            BigDecimal balanceDue = invoice.getBalanceDue();
            if (balanceDue == null) {
                if (invoice.getTotalAmount() != null && invoice.getAmountPaid() != null) {
                    balanceDue = invoice.getTotalAmount().subtract(invoice.getAmountPaid());
                } else if (invoice.getTotalAmount() != null) {
                    balanceDue = invoice.getTotalAmount();
                } else {
                    balanceDue = BigDecimal.ZERO;
                }
            }
            pstmt.setBigDecimal(7, balanceDue);
            
            // Set status
            pstmt.setString(8, invoice.getStatus() != null ? invoice.getStatus() : "Unpaid");
            
            // Set ID for WHERE clause
            pstmt.setInt(9, invoice.getInvoiceID());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Updated invoice #" + invoice.getInvoiceID());
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error updating invoice: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update just the payment status of an invoice
     * @param invoiceId Invoice ID to update
     * @param status New status value
     * @param amountPaid New amount paid (if null, only status will be updated)
     * @return true if successful, false otherwise
     */
    public boolean updateInvoiceStatus(int invoiceId, String status, BigDecimal amountPaid) {
        StringBuilder sqlBuilder = new StringBuilder("UPDATE Invoice SET Status = ?");
        
        if (amountPaid != null) {
            sqlBuilder.append(", AmountPaid = ?, BalanceDue = TotalAmount - ?");
        }
        
        sqlBuilder.append(" WHERE InvoiceID = ?");
        
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlBuilder.toString())) {
            
            pstmt.setString(1, status);
            
            if (amountPaid != null) {
                pstmt.setBigDecimal(2, amountPaid);
                pstmt.setBigDecimal(3, amountPaid);
                pstmt.setInt(4, invoiceId);
            } else {
                pstmt.setInt(2, invoiceId);
            }
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating invoice status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete an invoice from the database
     * @param invoiceId The ID of the invoice to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteInvoice(int invoiceId) {
        // First delete related charges to maintain referential integrity
        String deleteChargesSql = "DELETE FROM Charge WHERE InvoiceID = ?";
        String deleteInvoiceSql = "DELETE FROM Invoice WHERE InvoiceID = ?";
        
        try (Connection conn = DatabaseConnector.getConnection()) {
            // Start transaction
            conn.setAutoCommit(false);
            
            try (PreparedStatement deleteChargesStmt = conn.prepareStatement(deleteChargesSql);
                 PreparedStatement deleteInvoiceStmt = conn.prepareStatement(deleteInvoiceSql)) {
                
                // Delete associated charges
                deleteChargesStmt.setInt(1, invoiceId);
                deleteChargesStmt.executeUpdate();
                
                // Delete the invoice
                deleteInvoiceStmt.setInt(1, invoiceId);
                int affectedRows = deleteInvoiceStmt.executeUpdate();
                
                // Commit transaction
                conn.commit();
                
                if (affectedRows > 0) {
                    System.out.println("Deleted invoice #" + invoiceId + " and its charges");
                    return true;
                }
                return false;
                
            } catch (SQLException e) {
                // Rollback transaction on error
                conn.rollback();
                System.err.println("Transaction rolled back: " + e.getMessage());
                throw e;
            } finally {
                // Restore auto-commit mode
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            System.err.println("Error deleting invoice: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get a single invoice by ID with all details
     * @param invoiceId The invoice ID to retrieve
     * @return The invoice, or null if not found
     */
    public Invoice getInvoiceById(int invoiceId) {
        String sql = "SELECT i.*, " +
                "g.FirstName || ' ' || g.LastName AS GuestName, " +
                "r.RoomID, r.CheckInDate, r.CheckOutDate, room.RoomNumber " +
                "FROM Invoice i " +
                "LEFT JOIN Guest g ON i.GuestID = g.GuestID " +
                "LEFT JOIN Reservation r ON i.ReservationID = r.ReservationID " +
                "LEFT JOIN Room room ON r.RoomID = room.RoomID " +
                "WHERE i.InvoiceID = ?";
                
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, invoiceId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToInvoice(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching invoice by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get all invoices for a specific guest
     * @param guestId The guest ID to search for
     * @return List of invoices for the guest
     */
    public ObservableList<Invoice> getInvoicesByGuest(int guestId) {
        ObservableList<Invoice> invoices = FXCollections.observableArrayList();
        
        String sql = "SELECT i.*, " +
                "g.FirstName || ' ' || g.LastName AS GuestName, " +
                "r.RoomID, r.CheckInDate, r.CheckOutDate, room.RoomNumber " +
                "FROM Invoice i " +
                "LEFT JOIN Guest g ON i.GuestID = g.GuestID " +
                "LEFT JOIN Reservation r ON i.ReservationID = r.ReservationID " +
                "LEFT JOIN Room room ON r.RoomID = room.RoomID " +
                "WHERE i.GuestID = ? " +
                "ORDER BY i.InvoiceDate DESC";
                
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, guestId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    invoices.add(mapResultSetToInvoice(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching invoices by guest: " + e.getMessage());
            e.printStackTrace();
        }
        
        return invoices;
    }
    
    /**
     * Get all invoices for a specific reservation
     * @param reservationId The reservation ID
     * @return List of invoices for the reservation
     */
    public ObservableList<Invoice> getInvoicesByReservation(int reservationId) {
        ObservableList<Invoice> invoices = FXCollections.observableArrayList();
        
        String sql = "SELECT i.*, " +
                "g.FirstName || ' ' || g.LastName AS GuestName, " +
                "r.RoomID, r.CheckInDate, r.CheckOutDate, room.RoomNumber " +
                "FROM Invoice i " +
                "LEFT JOIN Guest g ON i.GuestID = g.GuestID " +
                "LEFT JOIN Reservation r ON i.ReservationID = r.ReservationID " +
                "LEFT JOIN Room room ON r.RoomID = room.RoomID " +
                "WHERE i.ReservationID = ? " +
                "ORDER BY i.InvoiceDate DESC";
                
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, reservationId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    invoices.add(mapResultSetToInvoice(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching invoices by reservation: " + e.getMessage());
            e.printStackTrace();
        }
        
        return invoices;
    }
    
    /**
     * Creates a new invoice directly from a reservation
     * @param reservationId The ID of the reservation to create an invoice for
     * @return The created invoice, or null if creation failed
     */
    public Invoice createInvoiceFromReservation(int reservationId) {
        // First, get the reservation details
        ReservationDAO reservationDAO = new ReservationDAO();
        Reservation reservation = reservationDAO.getReservationByIdWithDetails(reservationId);
        
        if (reservation == null) {
            System.err.println("Cannot create invoice: Reservation #" + reservationId + " not found");
            return null;
        }
        
        // Begin transaction
        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();
            conn.setAutoCommit(false);
            
            // Create a new invoice
            Invoice invoice = new Invoice();
            invoice.setReservationID(reservationId);
            invoice.setGuestID(reservation.getGuestID());
            invoice.setInvoiceDate(LocalDate.now());
            invoice.setDueDate(LocalDate.now().plusDays(30)); // Due in 30 days
            
            // Set initial amounts
            BigDecimal totalAmount = reservation.getTotalEstimatedCost() != null ? 
                reservation.getTotalEstimatedCost() : BigDecimal.ZERO;
            invoice.setTotalAmount(totalAmount);
            invoice.setAmountPaid(BigDecimal.ZERO);
            invoice.setBalanceDue(totalAmount);
            invoice.setStatus("Unpaid");
            
            // Create the invoice
            String sql = "INSERT INTO Invoice (ReservationID, GuestID, InvoiceDate, DueDate, " +
                    "TotalAmount, AmountPaid, BalanceDue, Status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, invoice.getReservationID());
                pstmt.setInt(2, invoice.getGuestID());
                pstmt.setDate(3, Date.valueOf(invoice.getInvoiceDate()));
                pstmt.setDate(4, Date.valueOf(invoice.getDueDate()));
                pstmt.setBigDecimal(5, invoice.getTotalAmount());
                pstmt.setBigDecimal(6, invoice.getAmountPaid());
                pstmt.setBigDecimal(7, invoice.getBalanceDue());
                pstmt.setString(8, invoice.getStatus());
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating invoice failed, no rows affected.");
                }
                
                // Get the generated ID
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        invoice.setInvoiceID(generatedKeys.getInt(1));
                    } else {
                        throw new SQLException("Creating invoice failed, no ID obtained.");
                    }
                }
            }
            
            // Add a charge for the room rate
            if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                String description = "Room charge: ";
                if (reservation.getRoomTypeName() != null) {
                    description += reservation.getRoomTypeName();
                } else {
                    description += "Room";
                }
                
                if (reservation.getCheckInDate() != null && reservation.getCheckOutDate() != null) {
                    long nights = java.time.temporal.ChronoUnit.DAYS.between(
                        reservation.getCheckInDate(), reservation.getCheckOutDate());
                    description += " for " + nights + (nights == 1 ? " night" : " nights");
                }
                
                ChargeDAO chargeDAO = new ChargeDAO();
                Charge charge = new Charge();
                charge.setInvoiceID(invoice.getInvoiceID());
                charge.setDescription(description);
                charge.setAmount(totalAmount);
                charge.setChargeDate(LocalDate.now());
                
                boolean chargeAdded = chargeDAO.addCharge(charge, conn);
                if (!chargeAdded) {
                    throw new SQLException("Failed to add room charge to invoice");
                }
            }
            
            // Commit the transaction
            conn.commit();
            
            // Return the complete invoice with details
            return getInvoiceById(invoice.getInvoiceID());
            
        } catch (SQLException e) {
            System.err.println("Error creating invoice from reservation: " + e.getMessage());
            e.printStackTrace();
            
            // Rollback if there's an error
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error rolling back transaction: " + ex.getMessage());
                }
            }
            
            return null;
        } finally {
            // Reset auto-commit
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Error resetting auto-commit: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Map a ResultSet row to an Invoice object
     */
    private Invoice mapResultSetToInvoice(ResultSet rs) throws SQLException {
        Invoice inv = new Invoice();
        inv.setInvoiceID(rs.getInt("InvoiceID"));
        
        int resId = rs.getInt("ReservationID"); 
        if (!rs.wasNull()) inv.setReservationID(resId);
        
        int guestId = rs.getInt("GuestID"); 
        if (!rs.wasNull()) inv.setGuestID(guestId);
        
        inv.setInvoiceDate(rs.getDate("InvoiceDate") != null ? 
            rs.getDate("InvoiceDate").toLocalDate() : null);
        inv.setDueDate(rs.getDate("DueDate") != null ? 
            rs.getDate("DueDate").toLocalDate() : null);
            
        inv.setTotalAmount(rs.getBigDecimal("TotalAmount"));
        inv.setAmountPaid(rs.getBigDecimal("AmountPaid"));
        inv.setBalanceDue(rs.getBigDecimal("BalanceDue"));
        inv.setStatus(rs.getString("Status"));
        inv.setGuestName(rs.getString("GuestName"));
        
        // Create reservation details string if we have room info
        try {
            String roomNumber = rs.getString("RoomNumber");
            if (roomNumber != null) {
                Date checkInDate = rs.getDate("CheckInDate");
                Date checkOutDate = rs.getDate("CheckOutDate");
                StringBuilder details = new StringBuilder("Room " + roomNumber);
                
                if (checkInDate != null && checkOutDate != null) {
                    details.append(" (")
                           .append(checkInDate.toLocalDate().format(java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy")))
                           .append(" - ")
                           .append(checkOutDate.toLocalDate().format(java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy")))
                           .append(")");
                }
                
                inv.setReservationDetails(details.toString());
            }
        } catch (SQLException e) {
            // Handle case where columns don't exist in result set
            System.out.println("Note: Reservation details not available in query result");
        }
        
        return inv;
    }
}