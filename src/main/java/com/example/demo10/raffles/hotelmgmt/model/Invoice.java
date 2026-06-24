package com.example.demo10.raffles.hotelmgmt.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class Invoice {
    private int invoiceID;
    private Integer reservationID;
    private Integer guestID;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal balanceDue; // This is a computed column in HSQLDB, so we might not set it directly.
    // Or, we calculate it in Java if not using the DB's computed feature.
    private String status; // e.g., Unpaid, Paid, Partially Paid, Overdue

    // Optional transient fields for display
    private String guestName;
    private String reservationDetails; // e.g., Room number and dates

    public Invoice() {
    }

    // Getters and Setters
    public int getInvoiceID() {
        return invoiceID;
    }

    public void setInvoiceID(int invoiceID) {
        this.invoiceID = invoiceID;
    }

    public Integer getReservationID() {
        return reservationID;
    }

    public void setReservationID(Integer reservationID) {
        this.reservationID = reservationID;
    }

    public Integer getGuestID() {
        return guestID;
    }

    public void setGuestID(Integer guestID) {
        this.guestID = guestID;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
        updateBalanceDue();
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
        updateBalanceDue();
    }

    public BigDecimal getBalanceDue() {
        // If not relying on DB computed column, ensure it's calculated
        if (this.totalAmount != null && this.amountPaid != null) {
            return this.totalAmount.subtract(this.amountPaid);
        }
        return balanceDue; // Could be null if not calculated and not set from DB
    }

    // Setter for balanceDue if read from DB (though HSQLDB might compute it)
    public void setBalanceDue(BigDecimal balanceDue) {
        this.balanceDue = balanceDue;
    }

    private void updateBalanceDue() {
        if (totalAmount != null && amountPaid != null) {
            this.balanceDue = totalAmount.subtract(amountPaid);
        }
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }
    public String getReservationDetails() { return reservationDetails; }
    public void setReservationDetails(String reservationDetails) { this.reservationDetails = reservationDetails; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Invoice invoice = (Invoice) o;
        return invoiceID == invoice.invoiceID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(invoiceID);
    }

    @Override
    public String toString() {
        return "Invoice #" + invoiceID + " for GuestID: " + guestID + ", Amount: $" + totalAmount;
    }
}