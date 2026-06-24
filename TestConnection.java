package com.example.demo10.raffles.hotelmgmt;// Add a package declaration

import java.sql.Connection;
import java.sql.SQLException;

public class TestConnection { // Class name should be PascalCase
    public static void main(String[] args) {
        try (Connection conn = DatabaseConnector.getConnection()) { // Fix class name
            System.out.println("Connected to SQL Server using Windows Authentication!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}