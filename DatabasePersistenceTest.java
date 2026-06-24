package com.example.demo10.raffles.hotelmgmt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

/**
 * Test class to verify database persistence between connections.
 */
public class DatabasePersistenceTest {

    public static void main(String[] args) {
        System.out.println("==== RAFFLES HOTEL DATABASE PERSISTENCE TEST ====");
        
        // Create a unique test ID for this run
        String testId = UUID.randomUUID().toString().substring(0, 8);
        String testTable = "persistence_test";
        String testData = "Test data " + testId;
        
        System.out.println("Test ID: " + testId);
        System.out.println("Test data: " + testData);

        try {
            // First connection: Create the test table if it doesn't exist
            Connection conn1 = DatabaseConnector.getConnection();
            System.out.println("\n1. First connection established");
            
            try (Statement stmt = conn1.createStatement()) {
                // Create test table if it doesn't exist
                stmt.execute("CREATE TABLE IF NOT EXISTS " + testTable + " (" +
                             "id VARCHAR(50) PRIMARY KEY, " +
                             "data VARCHAR(200), " +
                             "created TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
                System.out.println("Test table created or already exists");
                
                // Insert test data
                try (PreparedStatement pstmt = conn1.prepareStatement(
                        "INSERT INTO " + testTable + " (id, data) VALUES (?, ?)")) {
                    pstmt.setString(1, testId);
                    pstmt.setString(2, testData);
                    pstmt.executeUpdate();
                    System.out.println("Test data inserted: " + testData);
                }
                
                // Verify we can read it back in the same connection
                try (ResultSet rs = stmt.executeQuery("SELECT id, data FROM " + testTable + 
                                                    " WHERE id = '" + testId + "'")) {
                    if (rs.next()) {
                        String retrievedData = rs.getString("data");
                        System.out.println("Data verified in first connection: " + retrievedData);
                        if (!testData.equals(retrievedData)) {
                            System.err.println("ERROR: Retrieved data doesn't match what was inserted!");
                        }
                    } else {
                        System.err.println("ERROR: Could not find the inserted test data!");
                    }
                }
                
                // Show all test data in the table
                try (ResultSet rs = stmt.executeQuery("SELECT id, data, created FROM " + testTable)) {
                    System.out.println("\nAll test entries:");
                    while (rs.next()) {
                        System.out.println("  " + rs.getString("id") + " : " + 
                                         rs.getString("data") + " (" + 
                                         rs.getTimestamp("created") + ")");
                    }
                }
            }
            
            // Properly close the first connection
            System.out.println("\nClosing first connection with proper shutdown...");
            DatabaseConnector.closeConnection();
            
            // Now create a second connection to verify persistence
            System.out.println("\n2. Creating second connection to verify persistence...");
            Connection conn2 = DatabaseConnector.getConnection();
            
            try (Statement stmt = conn2.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id, data FROM " + testTable + 
                                               " WHERE id = '" + testId + "'")) {
                
                if (rs.next()) {
                    String retrievedData = rs.getString("data");
                    System.out.println("Data verified in second connection: " + retrievedData);
                    if (testData.equals(retrievedData)) {
                        System.out.println("\n✅ SUCCESS: Database persistence verified!");
                    } else {
                        System.err.println("\n❌ ERROR: Retrieved data doesn't match what was inserted!");
                    }
                } else {
                    System.err.println("\n❌ ERROR: Could not find the previously inserted test data!");
                    System.err.println("This indicates the database is not persisting data between connections.");
                }
            }
            
            // Cleanup
            System.out.println("\nCleaning up test table entries older than 1 hour...");
            try (Statement stmt = conn2.createStatement()) {
                int deleted = stmt.executeUpdate("DELETE FROM " + testTable + 
                                              " WHERE created < DATEADD('HOUR', -1, CURRENT_TIMESTAMP)");
                System.out.println("Deleted " + deleted + " old test entries");
            }
            
            // Close the second connection properly
            DatabaseConnector.closeConnection();
            
        } catch (Exception e) {
            System.err.println("TEST ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n==== PERSISTENCE TEST COMPLETED ====");
    }
} 