package com.example.demo10.raffles.hotelmgmt.model;

import java.util.Objects;

public class Room {
    private int roomID;
    private String roomNumber;
    private Integer roomTypeID;
    private String roomTypeName; // For display purposes, from JOIN
    private Integer floorNumber;
    private String status;       // Examples: 'Available', 'Occupied', 'Cleaning', 'Maintenance'
    private String features;
    private Integer maxOccupancy;
    private boolean smoking;      // Matches BIT in DB
    private String notes;

    public Room() {
        // Default constructor with sensible defaults
        this.status = "Available";
        this.maxOccupancy = 2;
        this.smoking = false;
    }

    // Constructor with essential fields
    public Room(String roomNumber, Integer roomTypeID, String roomTypeName, Integer floorNumber) {
        this.roomNumber = roomNumber;
        this.roomTypeID = roomTypeID;
        this.roomTypeName = roomTypeName;
        this.floorNumber = floorNumber;
        this.status = "Available";
        this.maxOccupancy = 2; 
        this.smoking = false;
    }

    // Getters and Setters
    public int getRoomID() {
        return roomID;
    }

    public void setRoomID(int roomID) {
        this.roomID = roomID;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public Integer getRoomTypeID() {
        return roomTypeID;
    }

    public void setRoomTypeID(Integer roomTypeID) {
        this.roomTypeID = roomTypeID;
    }

    public String getRoomTypeName() {
        return roomTypeName;
    }

    public void setRoomTypeName(String roomTypeName) {
        this.roomTypeName = roomTypeName;
    }

    public Integer getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(Integer floorNumber) {
        this.floorNumber = floorNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public Integer getMaxOccupancy() {
        return maxOccupancy;
    }

    public void setMaxOccupancy(Integer maxOccupancy) {
        this.maxOccupancy = maxOccupancy;
    }

    public boolean isSmoking() {
        return smoking;
    }

    public void setSmoking(boolean smoking) {
        this.smoking = smoking;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "Room " + roomNumber + " (" + roomTypeName + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return roomID == room.roomID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomID);
    }
}