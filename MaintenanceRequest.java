package com.example.demo10.raffles.hotelmgmt.model;

import java.time.LocalDate;
import java.util.Objects;

public class MaintenanceRequest {
    private int requestID;
    private Integer roomID;
    private Integer reportedByGuestID;    // Nullable
    private Integer reportedByEmployeeID; // Nullable
    private LocalDate reportedDate;
    private String issueDescription;
    private String priority; // e.g., Low, Medium, High, Urgent
    private String status;   // e.g., Open, In Progress, Resolved, Closed

    // Optional transient fields
    private String roomNumber;
    private String guestName;
    private String employeeName; // Employee who reported or is assigned

    public MaintenanceRequest() {
    }

    // Getters and Setters
    public int getRequestID() {
        return requestID;
    }

    public void setRequestID(int requestID) {
        this.requestID = requestID;
    }

    public Integer getRoomID() {
        return roomID;
    }

    public void setRoomID(Integer roomID) {
        this.roomID = roomID;
    }

    public Integer getReportedByGuestID() {
        return reportedByGuestID;
    }

    public void setReportedByGuestID(Integer reportedByGuestID) {
        this.reportedByGuestID = reportedByGuestID;
    }

    public Integer getReportedByEmployeeID() {
        return reportedByEmployeeID;
    }

    public void setReportedByEmployeeID(Integer reportedByEmployeeID) {
        this.reportedByEmployeeID = reportedByEmployeeID;
    }

    public LocalDate getReportedDate() {
        return reportedDate;
    }

    public void setReportedDate(LocalDate reportedDate) {
        this.reportedDate = reportedDate;
    }

    public String getIssueDescription() {
        return issueDescription;
    }

    public void setIssueDescription(String issueDescription) {
        this.issueDescription = issueDescription;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MaintenanceRequest that = (MaintenanceRequest) o;
        return requestID == that.requestID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestID);
    }
}