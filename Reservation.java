package com.example.demo10.raffles.hotelmgmt.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class Reservation {
    private int reservationID;
    private Integer guestID;
    private Integer roomID; // Can be null if assigned later
    private Integer roomTypeID;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer numberOfAdults;
    private Integer numberOfChildren;
    private String status; // e.g., Pending, Confirmed, CheckedIn, CheckedOut, Cancelled
    private String bookingSource;
    private BigDecimal totalEstimatedCost;
    private LocalDate dateBooked;
    private String specialRequests;
    private String notes;

    // Optional: Transient fields for display purposes (populated by joins in DAO)
    private String guestName;
    private String roomNumber;
    private String roomTypeName;


    public Reservation() {
    }

    // Getters and Setters
    public int getReservationID() {
        return reservationID;
    }

    public void setReservationID(int reservationID) {
        this.reservationID = reservationID;
    }

    public Integer getGuestID() {
        return guestID;
    }

    public void setGuestID(Integer guestID) {
        this.guestID = guestID;
    }

    public Integer getRoomID() {
        return roomID;
    }

    public void setRoomID(Integer roomID) {
        this.roomID = roomID;
    }

    public Integer getRoomTypeID() {
        return roomTypeID;
    }

    public void setRoomTypeID(Integer roomTypeID) {
        this.roomTypeID = roomTypeID;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public Integer getNumberOfAdults() {
        return numberOfAdults;
    }

    public void setNumberOfAdults(Integer numberOfAdults) {
        this.numberOfAdults = numberOfAdults;
    }

    public Integer getNumberOfChildren() {
        return numberOfChildren;
    }

    public void setNumberOfChildren(Integer numberOfChildren) {
        this.numberOfChildren = numberOfChildren;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBookingSource() {
        return bookingSource;
    }

    public void setBookingSource(String bookingSource) {
        this.bookingSource = bookingSource;
    }

    public BigDecimal getTotalEstimatedCost() {
        return totalEstimatedCost;
    }

    public void setTotalEstimatedCost(BigDecimal totalEstimatedCost) {
        this.totalEstimatedCost = totalEstimatedCost;
    }

    public LocalDate getDateBooked() {
        return dateBooked;
    }

    public void setDateBooked(LocalDate dateBooked) {
        this.dateBooked = dateBooked;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Getters and setters for transient display fields
    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getRoomTypeName() { return roomTypeName; }
    public void setRoomTypeName(String roomTypeName) { this.roomTypeName = roomTypeName; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return reservationID == that.reservationID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservationID);
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "reservationID=" + reservationID +
                ", guestID=" + guestID +
                ", roomID=" + roomID +
                ", checkInDate=" + checkInDate +
                ", checkOutDate=" + checkOutDate +
                ", status='" + status + '\'' +
                '}';
    }
}