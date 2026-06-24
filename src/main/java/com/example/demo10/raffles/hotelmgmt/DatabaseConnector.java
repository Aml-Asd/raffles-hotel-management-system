package com.example.demo10.raffles.hotelmgmt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;

public class DatabaseConnector {

    // Use absolute file path to ensure persistence between runs
    private static final String DB_FOLDER = System.getProperty("user.home") + File.separator + "raffles_db";
    private static final String DB_NAME = "raffleshotel";
    private static final String DB_URL = "jdbc:hsqldb:file:" + DB_FOLDER + File.separator + DB_NAME;
    private static final String USER = "SA";
    private static final String PASSWORD = "";
    private static Connection connection = null;
    
    static {
        // Create database directory if it doesn't exist
        File dbDir = new File(DB_FOLDER);
        if (!dbDir.exists()) {
            boolean created = dbDir.mkdirs();
            System.out.println("Created database directory: " + dbDir.getAbsolutePath() + " - Success: " + created);
        }
        System.out.println("Database URL: " + DB_URL);
        
        // Add shutdown hook to ensure DB is properly closed on application exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown hook activated: Ensuring database connection is properly closed");
            closeConnection();
        }));
    }

    // Get a connection to the database
    public static synchronized Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                // Add database connection properties to ensure proper file persistence
                connection = DriverManager.getConnection(DB_URL + ";hsqldb.write_delay=false;shutdown=true", USER, PASSWORD);
                
                // Execute some initial setup commands
                try (Statement stmt = connection.createStatement()) {
                    // Disable write delay to ensure immediate persistence
                    stmt.execute("SET FILES WRITE DELAY 0");
                    // Set log size to ensure proper transaction handling
                    stmt.execute("SET FILES LOG SIZE 10");
                    // Check connection is valid
                    try (ResultSet rs = stmt.executeQuery("SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS")) {
                        if (rs.next()) {
                            System.out.println("Database connection validated successfully");
                        }
                    }
                }
                System.out.println("Database connection established successfully");
            }
            return connection;
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            throw e;
        }
    }

    // Close the database connection
    public static synchronized void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    // Check if there are uncommitted transactions and commit them
                    if (!connection.getAutoCommit()) {
                        System.out.println("Committing pending transactions before shutdown");
                        connection.commit();
                    }
                    
                    // Execute SHUTDOWN command to ensure data is properly persisted
                    try (Statement stmt = connection.createStatement()) {
                        stmt.execute("CHECKPOINT");
                        System.out.println("Database checkpoint completed successfully");
                        stmt.execute("SHUTDOWN");
                        System.out.println("Database shutdown command executed successfully");
                    }
                    
                    // Now close the connection
                    connection.close();
                    connection = null;
                    System.out.println("Database connection properly closed with SHUTDOWN command.");
                }
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Reset the database by dropping all tables and recreating them.
     * USE WITH CAUTION: This will delete all data!
     */
    public static boolean resetDatabase() {
        closeConnection(); // Close any existing connection first
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            System.out.println("WARNING: Resetting database - all data will be lost!");
            
            // Drop the tables in reverse order to avoid foreign key constraints
            stmt.executeUpdate("DROP TABLE IF EXISTS MaintenanceRequest");
            stmt.executeUpdate("DROP TABLE IF EXISTS HousekeepingTask");
            stmt.executeUpdate("DROP TABLE IF EXISTS GuestAccount");
            stmt.executeUpdate("DROP TABLE IF EXISTS Payment");
            stmt.executeUpdate("DROP TABLE IF EXISTS Charge");
            stmt.executeUpdate("DROP TABLE IF EXISTS Service");
            stmt.executeUpdate("DROP TABLE IF EXISTS Invoice");
            stmt.executeUpdate("DROP TABLE IF EXISTS Reservation");
            stmt.executeUpdate("DROP TABLE IF EXISTS Room");
            stmt.executeUpdate("DROP TABLE IF EXISTS RoomType");
            stmt.executeUpdate("DROP TABLE IF EXISTS Guest");
            stmt.executeUpdate("DROP TABLE IF EXISTS Employee");
            stmt.executeUpdate("DROP TABLE IF EXISTS Department");
            stmt.executeUpdate("DROP TABLE IF EXISTS Role");
            
            System.out.println("All tables dropped successfully.");
            
            // Close connection to ensure clean state
            closeConnection();
            
            return true;
        } catch (SQLException e) {
            System.err.println("Error resetting database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Verify database tables exist and have data
     * @return true if verification passes, false otherwise
     */
    public static boolean verifyDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Verifying database tables and data...");
            
            // Check Reservation table
            boolean hasReservations = false;
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Reservation")) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    hasReservations = true; // Even if count is 0, the table exists
                    System.out.println("Reservations Count: " + count);
                }
            } catch (SQLException e) {
                System.out.println("Reservation table does not exist: " + e.getMessage());
            }
            
            // Check RoomType table
            boolean hasRoomTypes = false;
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM RoomType")) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    hasRoomTypes = count > 0;
                    System.out.println("Room Types Count: " + count);
                }
            } catch (SQLException e) {
                System.out.println("RoomType table does not exist: " + e.getMessage());
            }
            
            // Check Room table
            boolean hasRooms = false;
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Room")) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    hasRooms = count > 0;
                    System.out.println("Rooms Count: " + count);
                }
            } catch (SQLException e) {
                System.out.println("Room table does not exist: " + e.getMessage());
            }
            
            if (!hasRoomTypes || !hasRooms || !hasReservations) {
                System.out.println("Database verification failed: Missing tables or data");
                return false;
            }
            
            return true;
        } catch (SQLException e) {
            System.err.println("Database verification error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Run a checkpoint to ensure data is persisted to disk
     */
    public static void checkpoint() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CHECKPOINT");
            System.out.println("Database checkpoint executed");
        } catch (SQLException e) {
            System.err.println("Error executing checkpoint: " + e.getMessage());
        }
    }
}