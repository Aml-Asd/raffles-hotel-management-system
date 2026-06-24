package com.example.demo10.raffles.hotelmgmt.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Service {
    private int serviceID;
    private String serviceName;
    private String description;
    private BigDecimal defaultPrice;
    private Integer departmentID;

    // Optional transient field for display
    private String departmentName;

    public Service() {
    }

    // Getters and Setters
    public int getServiceID() {
        return serviceID;
    }

    public void setServiceID(int serviceID) {
        this.serviceID = serviceID;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getDefaultPrice() {
        return defaultPrice;
    }

    public void setDefaultPrice(BigDecimal defaultPrice) {
        this.defaultPrice = defaultPrice;
    }

    public Integer getDepartmentID() {
        return departmentID;
    }

    public void setDepartmentID(Integer departmentID) {
        this.departmentID = departmentID;
    }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    @Override
    public String toString() {
        return serviceName + (defaultPrice != null ? " ($" + defaultPrice + ")" : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Service service = (Service) o;
        return serviceID == service.serviceID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceID);
    }
}