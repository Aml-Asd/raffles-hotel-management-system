package com.example.demo10.raffles.hotelmgmt.dao;

import com.example.demo10.raffles.hotelmgmt.DatabaseConnector;
import com.example.demo10.raffles.hotelmgmt.model.Payment;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date; // For converting LocalDate to java.sql.Date
import java.sql.Types; // For setting NULL values explicitly

public class PaymentDAO {

    public ObservableList<Payment> getPaymentsByInvoiceId(int invoiceId) {
        ObservableList<Payment> payments = FXCollections.observableArrayList();
        String sql = "SELECT p.PaymentID, p.InvoiceID, p.PaymentDate, p.AmountPaid, p.PaymentMethod, " +
                "p.TransactionReference, p.ProcessedByEmployeeID, " +
                "e.FirstName || ' ' || e.LastName AS EmployeeName " + // Concatenate for HSQLDB
                "FROM Payment p " +
                "LEFT JOIN Employee e ON p.ProcessedByEmployeeID = e.EmployeeID " +
                "WHERE p.InvoiceID = ? ORDER BY p.PaymentDate DESC, p.PaymentID DESC"; // Show recent first
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, invoiceId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Payment payment = new Payment();
                payment.setPaymentID(rs.getInt("PaymentID"));
                payment.setInvoiceID(rs.getInt("InvoiceID"));
                Date paymentDateSql = rs.getDate("PaymentDate");
                if (paymentDateSql != null) {
                    payment.setPaymentDate(paymentDateSql.toLocalDate());
                }
                payment.setAmountPaid(rs.getBigDecimal("AmountPaid"));
                payment.setPaymentMethod(rs.getString("PaymentMethod"));
                payment.setTransactionReference(rs.getString("TransactionReference"));

                int empId = rs.getInt("ProcessedByEmployeeID");
                if (!rs.wasNull()) {
                    payment.setProcessedByEmployeeID(empId);
                }
                payment.setEmployeeName(rs.getString("EmployeeName")); // From join
                payments.add(payment);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching payments for invoice ID " + invoiceId + ": " + e.getMessage());
        }
        return payments;
    }

    public boolean addPayment(Payment payment) {
        String sql = "INSERT INTO Payment (InvoiceID, PaymentDate, AmountPaid, PaymentMethod, TransactionReference, ProcessedByEmployeeID) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, payment.getInvoiceID()); // Must be set on the payment object

            if (payment.getPaymentDate() != null) {
                pstmt.setDate(2, Date.valueOf(payment.getPaymentDate()));
            } else {
                pstmt.setDate(2, Date.valueOf(java.time.LocalDate.now())); // Default to current date if not provided
            }
            pstmt.setBigDecimal(3, payment.getAmountPaid());
            pstmt.setString(4, payment.getPaymentMethod());
            pstmt.setString(5, payment.getTransactionReference());

            if (payment.getProcessedByEmployeeID() != null) {
                pstmt.setInt(6, payment.getProcessedByEmployeeID());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        payment.setPaymentID(generatedKeys.getInt(1)); // Set generated ID back to object
                    }
                }
                // After adding a payment, you might want to update the associated Invoice's AmountPaid and Status
                // This logic could be in a service layer or triggered here.
                // For simplicity, we'll assume Invoice status is updated separately or via triggers if DB supports.
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error adding payment: " + e.getMessage());
            return false;
        }
    }

    // TODO: Add methods for updatePayment, deletePayment (usually not allowed or restricted), getPaymentById if needed.
    // Deleting payments is often a business rule restriction. Voiding/Refunding might be a separate process.
}