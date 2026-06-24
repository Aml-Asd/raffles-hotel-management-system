package com.example.demo10.raffles.hotelmgmt.model;

import java.math.BigDecimal;
import java.util.Objects;

public class RoomType {
    private int roomTypeID;
    private String typeName; // Example: 'Standard', 'Deluxe', 'Suite'
    private String description;
    private BigDecimal baseRate;
    private Integer defaultMaxOccupancy; // Use Integer to allow null if not set
    private String includedAmenities;

    public RoomType() {
    }

    // Getters and Setters
    public int getRoomTypeID() {
        return roomTypeID;
    }

    public void setRoomTypeID(int roomTypeID) {
        this.roomTypeID = roomTypeID;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getBaseRate() {
        return baseRate;
    }

    public void setBaseRate(BigDecimal baseRate) {
        this.baseRate = baseRate;
    }

    public Integer getDefaultMaxOccupancy() {
        return defaultMaxOccupancy;
    }

    public void setDefaultMaxOccupancy(Integer defaultMaxOccupancy) {
        this.defaultMaxOccupancy = defaultMaxOccupancy;
    }

    public String getIncludedAmenities() {
        return includedAmenities;
    }

    public void setIncludedAmenities(String includedAmenities) {
        this.includedAmenities = includedAmenities;
    }

    @Override
    public String toString() {
        // For displaying in ComboBoxes
        return typeName + (baseRate != null ? " - $" + baseRate : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoomType roomType = (RoomType) o;
        return roomTypeID == roomType.roomTypeID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomTypeID);
    }
}