package com.example.demo10.raffles.hotelmgmt.dao;

import com.example.demo10.raffles.hotelmgmt.DatabaseConnector;
import com.example.demo10.raffles.hotelmgmt.model.Charge;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.Date;
import java.sql.Types;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ChargeDAO {

    public ObservableList<Charge> getChargesByInvoiceId(int invoiceId) {
        ObservableList<Charge> charges = FXCollections.observableArrayList();
        // Join with Service and Employee for display names
        String sql = "SELECT ch.*, s.ServiceName, e.FirstName || ' ' || e.LastName AS EmployeeName " +
                "FROM Charge ch " +
                "LEFT JOIN Service s ON ch.ServiceID = s.ServiceID " +
                "LEFT JOIN Employee e ON ch.ChargedByEmployeeID = e.EmployeeID " +
                "WHERE ch.InvoiceID = ? ORDER BY ch.ChargeDate, ch.ChargeID";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
                 
            pstmt.setInt(1, invoiceId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                charges.add(mapResultSetToCharge(rs));
            }
            System.out.println("Retrieved " + charges.size() + " charges for invoice #" + invoiceId);
        } catch (SQLException e) {
            System.err.println("Error fetching charges for invoice ID " + invoiceId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return charges;
    }

    /**
     * Add a new charge to an invoice
     * @param charge The charge to add
     * @return true if successful, false otherwise
     */
    public boolean addCharge(Charge charge) {
        String sql = "INSERT INTO Charge (InvoiceID, ServiceID, Description, Amount, ChargeDate, ChargedByEmployeeID) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
                     
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Invoice ID is required
            if (charge.getInvoiceID() == null) {
                System.err.println("Error: Cannot add charge without an invoice ID");
                return false;
            }
            pstmt.setInt(1, charge.getInvoiceID());
            
            // Service ID is optional
            if (charge.getServiceID() != null) {
                pstmt.setInt(2, charge.getServiceID());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            
            // Description is required
            if (charge.getDescription() == null || charge.getDescription().trim().isEmpty()) {
                System.err.println("Error: Charge description cannot be empty");
                return false;
            }
            pstmt.setString(3, charge.getDescription());
            
            // Amount is required
            if (charge.getAmount() == null) {
                System.err.println("Error: Charge amount cannot be null");
                return false;
            }
            pstmt.setBigDecimal(4, charge.getAmount());
            
            // Charge date defaults to today if not provided
            if (charge.getChargeDate() != null) {
                pstmt.setDate(5, Date.valueOf(charge.getChargeDate()));
            } else {
                pstmt.setDate(5, Date.valueOf(LocalDate.now()));
            }
            
            // Employee ID is optional
            if (charge.getChargedByEmployeeID() != null) {
                pstmt.setInt(6, charge.getChargedByEmployeeID());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        charge.setChargeID(generatedKeys.getInt(1));
                        
                        // Update the invoice total to include this charge
                        updateInvoiceTotal(charge.getInvoiceID());
                        
                        System.out.println("Added charge #" + charge.getChargeID() + " to invoice #" + charge.getInvoiceID());
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error adding charge: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update an existing charge
     * @param charge The charge with updated information
     * @return true if successful, false otherwise
     */
    public boolean updateCharge(Charge charge) {
        String sql = "UPDATE Charge SET ServiceID = ?, Description = ?, Amount = ?, " +
                     "ChargeDate = ?, ChargedByEmployeeID = ? " +
                     "WHERE ChargeID = ?";
                     
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Service ID is optional
            if (charge.getServiceID() != null) {
                pstmt.setInt(1, charge.getServiceID());
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }
            
            // Description is required
            if (charge.getDescription() == null || charge.getDescription().trim().isEmpty()) {
                System.err.println("Error: Charge description cannot be empty");
                return false;
            }
            pstmt.setString(2, charge.getDescription());
            
            // Amount is required
            if (charge.getAmount() == null) {
                System.err.println("Error: Charge amount cannot be null");
                return false;
            }
            pstmt.setBigDecimal(3, charge.getAmount());
            
            // Charge date is required
            if (charge.getChargeDate() != null) {
                pstmt.setDate(4, Date.valueOf(charge.getChargeDate()));
            } else {
                pstmt.setDate(4, Date.valueOf(LocalDate.now()));
            }
            
            // Employee ID is optional
            if (charge.getChargedByEmployeeID() != null) {
                pstmt.setInt(5, charge.getChargedByEmployeeID());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }
            
            pstmt.setInt(6, charge.getChargeID());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                // Update the invoice total since charge amounts may have changed
                updateInvoiceTotal(charge.getInvoiceID());
                System.out.println("Updated charge #" + charge.getChargeID());
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error updating charge: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete a charge
     * @param chargeId The ID of the charge to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteCharge(int chargeId) {
        // First we need to get the invoice ID to update its total after deletion
        Integer invoiceId = getInvoiceIdForCharge(chargeId);
        if (invoiceId == null) {
            return false;
        }
        
        String sql = "DELETE FROM Charge WHERE ChargeID = ?";
        
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, chargeId);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                // Update the invoice total after deletion
                updateInvoiceTotal(invoiceId);
                System.out.println("Deleted charge #" + chargeId + " from invoice #" + invoiceId);
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error deleting charge: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get a single charge by ID
     * @param chargeId The ID of the charge to retrieve
     * @return The charge, or null if not found
     */
    public Charge getChargeById(int chargeId) {
        String sql = "SELECT ch.*, s.ServiceName, e.FirstName || ' ' || e.LastName AS EmployeeName " +
                     "FROM Charge ch " +
                     "LEFT JOIN Service s ON ch.ServiceID = s.ServiceID " +
                     "LEFT JOIN Employee e ON ch.ChargedByEmployeeID = e.EmployeeID " +
                     "WHERE ch.ChargeID = ?";
                     
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, chargeId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCharge(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching charge by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Get the invoice ID for a specific charge
     * @param chargeId The charge ID
     * @return The invoice ID, or null if not found
     */
    private Integer getInvoiceIdForCharge(int chargeId) {
        String sql = "SELECT InvoiceID FROM Charge WHERE ChargeID = ?";
        
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, chargeId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("InvoiceID");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting invoice ID for charge: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Update the total amount of an invoice based on its charges
     * @param invoiceId The invoice ID
     */
    private void updateInvoiceTotal(int invoiceId) {
        String sumSql = "SELECT SUM(Amount) AS Total FROM Charge WHERE InvoiceID = ?";
        String updateSql = "UPDATE Invoice SET TotalAmount = ?, BalanceDue = ? - AmountPaid WHERE InvoiceID = ?";
        
        try (Connection conn = DatabaseConnector.getConnection()) {
            BigDecimal totalAmount = BigDecimal.ZERO;
            
            // Calculate the total from charges
            try (PreparedStatement sumStmt = conn.prepareStatement(sumSql)) {
                sumStmt.setInt(1, invoiceId);
                
                try (ResultSet rs = sumStmt.executeQuery()) {
                    if (rs.next()) {
                        BigDecimal sum = rs.getBigDecimal("Total");
                        if (sum != null) {
                            totalAmount = sum;
                        }
                    }
                }
            }
            
            // Update the invoice total and balance due
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setBigDecimal(1, totalAmount);
                updateStmt.setBigDecimal(2, totalAmount);
                updateStmt.setInt(3, invoiceId);
                
                updateStmt.executeUpdate();
                System.out.println("Updated invoice #" + invoiceId + " total to " + totalAmount);
            }
        } catch (SQLException e) {
            System.err.println("Error updating invoice total: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Add multiple charges to an invoice in a single transaction
     * @param charges The list of charges to add
     * @param invoiceId The invoice ID for all charges
     * @return true if all charges were added successfully, false otherwise
     */
    public boolean addBulkCharges(ObservableList<Charge> charges, int invoiceId) {
        if (charges == null || charges.isEmpty()) {
            return true; // Nothing to do
        }
        
        String sql = "INSERT INTO Charge (InvoiceID, ServiceID, Description, Amount, ChargeDate, ChargedByEmployeeID) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
                     
        try (Connection conn = DatabaseConnector.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                for (Charge charge : charges) {
                    // Set invoice ID
                    pstmt.setInt(1, invoiceId);
                    
                    // Set other parameters with null handling
                    if (charge.getServiceID() != null) {
                        pstmt.setInt(2, charge.getServiceID());
                    } else {
                        pstmt.setNull(2, Types.INTEGER);
                    }
                    
                    pstmt.setString(3, charge.getDescription() != null ? charge.getDescription() : "");
                    pstmt.setBigDecimal(4, charge.getAmount() != null ? charge.getAmount() : BigDecimal.ZERO);
                    
                    if (charge.getChargeDate() != null) {
                        pstmt.setDate(5, Date.valueOf(charge.getChargeDate()));
                    } else {
                        pstmt.setDate(5, Date.valueOf(LocalDate.now()));
                    }
                    
                    if (charge.getChargedByEmployeeID() != null) {
                        pstmt.setInt(6, charge.getChargedByEmployeeID());
                    } else {
                        pstmt.setNull(6, Types.INTEGER);
                    }
                    
                    pstmt.addBatch();
                }
                
                int[] results = pstmt.executeBatch();
                
                // Check if all inserts were successful
                boolean allSuccessful = true;
                for (int result : results) {
                    if (result <= 0) {
                        allSuccessful = false;
                        break;
                    }
                }
                
                if (allSuccessful) {
                    // Update the invoice total
                    updateInvoiceTotal(invoiceId);
                    
                    conn.commit();
                    System.out.println("Added " + charges.size() + " charges to invoice #" + invoiceId);
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            System.err.println("Error adding bulk charges: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Map a ResultSet row to a Charge object
     */
    private Charge mapResultSetToCharge(ResultSet rs) throws SQLException {
        Charge charge = new Charge();
        charge.setChargeID(rs.getInt("ChargeID"));
        charge.setInvoiceID(rs.getInt("InvoiceID"));
        
        int serviceId = rs.getInt("ServiceID");
        if (!rs.wasNull()) charge.setServiceID(serviceId);
        
        charge.setDescription(rs.getString("Description"));
        charge.setAmount(rs.getBigDecimal("Amount"));
        charge.setChargeDate(rs.getDate("ChargeDate") != null ? rs.getDate("ChargeDate").toLocalDate() : null);
        
        int empId = rs.getInt("ChargedByEmployeeID");
        if (!rs.wasNull()) charge.setChargedByEmployeeID(empId);

        charge.setServiceName(rs.getString("ServiceName"));
        charge.setEmployeeName(rs.getString("EmployeeName"));
        
        return charge;
    }

    /**
     * Add a new charge with an existing transaction connection
     * @param charge The charge to add
     * @param conn An existing database connection for transaction support
     * @return true if successful, false otherwise
     */
    public boolean addCharge(Charge charge, Connection conn) {
        if (charge == null) return false;
        
        String sql = "INSERT INTO Charge (InvoiceID, ServiceID, Description, Amount, ChargeDate, ChargedByEmployeeID) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            // Set parameters
            pstmt.setInt(1, charge.getInvoiceID());
            
            if (charge.getServiceID() != null) {
                pstmt.setInt(2, charge.getServiceID());
            } else {
                pstmt.setNull(2, java.sql.Types.INTEGER);
            }
            
            pstmt.setString(3, charge.getDescription());
            pstmt.setBigDecimal(4, charge.getAmount());
            
            if (charge.getChargeDate() != null) {
                pstmt.setDate(5, java.sql.Date.valueOf(charge.getChargeDate()));
            } else {
                pstmt.setDate(5, java.sql.Date.valueOf(java.time.LocalDate.now()));
            }
            
            if (charge.getChargedByEmployeeID() != null) {
                pstmt.setInt(6, charge.getChargedByEmployeeID());
            } else {
                pstmt.setNull(6, java.sql.Types.INTEGER);
            }
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        charge.setChargeID(generatedKeys.getInt(1));
                        
                        // Update the invoice total
                        updateInvoiceTotal(charge.getInvoiceID(), conn);
                        
                        return true;
                    }
                }
            }
            
            return false;
            
        } catch (SQLException e) {
            System.err.println("Error adding charge with transaction: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update the invoice total based on its charges
     * @param invoiceId The invoice ID
     * @param conn The database connection to use
     * @throws SQLException If a database error occurs
     */
    private void updateInvoiceTotal(int invoiceId, Connection conn) throws SQLException {
        String sql = "UPDATE Invoice SET TotalAmount = (SELECT COALESCE(SUM(Amount), 0) FROM Charge WHERE InvoiceID = ?), " +
                     "BalanceDue = (SELECT COALESCE(SUM(Amount), 0) FROM Charge WHERE InvoiceID = ?) - AmountPaid " +
                     "WHERE InvoiceID = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, invoiceId);
            pstmt.setInt(2, invoiceId);
            pstmt.setInt(3, invoiceId);
            pstmt.executeUpdate();
        }
    }
}