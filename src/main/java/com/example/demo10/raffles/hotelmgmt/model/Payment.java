package com.example.demo10.raffles.hotelmgmt.model;

import java.math.BigDecimal;
import java.time.LocalDate; // Use LocalDate for PaymentDate
import java.util.Objects;

public class Payment {
    private int paymentID;
    private Integer invoiceID;
    private LocalDate paymentDate;
    private BigDecimal amountPaid;
    private String paymentMethod; // e.g., Cash, Credit Card, Bank Transfer
    private String transactionReference;
    private Integer processedByEmployeeID;

    // Optional transient fields
    private String employeeName;

    public Payment() {
    }

    // Getters and Setters
    public int getPaymentID() {
        return paymentID;
    }

    public void setPaymentID(int paymentID) {
        this.paymentID = paymentID;
    }

    public Integer getInvoiceID() {
        return invoiceID;
    }

    public void setInvoiceID(Integer invoiceID) {
        this.invoiceID = invoiceID;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public Integer getProcessedByEmployeeID() {
        return processedByEmployeeID;
    }

    public void setProcessedByEmployeeID(Integer processedByEmployeeID) {
        this.processedByEmployeeID = processedByEmployeeID;
    }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return paymentID == payment.paymentID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentID);
    }
}