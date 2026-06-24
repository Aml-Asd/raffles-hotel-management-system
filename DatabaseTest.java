package com.example.demo10.raffles.hotelmgmt;

import com.example.demo10.raffles.hotelmgmt.dao.RoomDAO;
import com.example.demo10.raffles.hotelmgmt.dao.RoomTypeDAO;
import com.example.demo10.raffles.hotelmgmt.model.Room;
import com.example.demo10.raffles.hotelmgmt.model.RoomType;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Simple database test class to verify connectivity and data.
 * Run this class directly to test database functionality.
 */
public class DatabaseTest {

    public static void main(String[] args) {
        System.out.println("**** RAFFLES HOTEL DATABASE TEST ****");
        
        try {
            // Test database connection
            System.out.println("\nTesting database connection...");
            Connection conn = DatabaseConnector.getConnection();
            System.out.println("Connection successful: " + (conn != null && !conn.isClosed()));
            
            // Print database URL
            System.out.println("Database path: " + System.getProperty("user.home") + "/raffles_hotel_db");
            
            // Test room types
            System.out.println("\nTesting room types...");
            RoomTypeDAO roomTypeDAO = new RoomTypeDAO();
            ObservableList<RoomType> roomTypes = roomTypeDAO.getAllRoomTypes();
            
            System.out.println("Room Types Count: " + roomTypes.size());
            for (RoomType type : roomTypes) {
                System.out.println("Type: " + type.getTypeName() + 
                                  " (ID: " + type.getRoomTypeID() + 
                                  ", Occupancy: " + type.getDefaultMaxOccupancy() + ")");
            }
            
            // If no room types, create some
            if (roomTypes.isEmpty()) {
                System.out.println("\nNo room types found. Creating room types...");
                createSampleRoomTypes(roomTypeDAO);
                
                // Verify again
                roomTypes = roomTypeDAO.getAllRoomTypes();
                System.out.println("Room Types Count after creation: " + roomTypes.size());
            }
            
            // Test rooms
            System.out.println("\nTesting rooms...");
            RoomDAO roomDAO = new RoomDAO();
            ObservableList<Room> rooms = roomDAO.getAllRooms();
            
            System.out.println("Rooms Count: " + rooms.size());
            for (Room room : rooms) {
                System.out.println("Room: " + room.getRoomNumber() + 
                                  " (Type: " + room.getRoomTypeName() + 
                                  ", Floor: " + room.getFloorNumber() + 
                                  ", Status: " + room.getStatus() + ")");
            }
            
            // If no rooms, create some
            if (rooms.isEmpty() && !roomTypes.isEmpty()) {
                System.out.println("\nNo rooms found. Creating sample rooms...");
                createSampleRooms(roomDAO, roomTypes);
                
                // Verify again
                rooms = roomDAO.getAllRooms();
                System.out.println("Rooms Count after creation: " + rooms.size());
            }
            
            // Check direct SQL query to verify table structure
            System.out.println("\nVerifying database tables structure...");
            Statement stmt = conn.createStatement();
            
            try {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM RoomType");
                if (rs.next()) {
                    System.out.println("RoomType table verified - count: " + rs.getInt(1));
                }
            } catch (Exception e) {
                System.err.println("Error querying RoomType table: " + e.getMessage());
            }
            
            try {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Room");
                if (rs.next()) {
                    System.out.println("Room table verified - count: " + rs.getInt(1));
                }
            } catch (Exception e) {
                System.err.println("Error querying Room table: " + e.getMessage());
            }
            
            System.out.println("\n**** DATABASE TEST COMPLETED ****");
            
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeConnection();
        }
    }
    
    private static void createSampleRoomTypes(RoomTypeDAO roomTypeDAO) {
        try {
            // Create Standard Room
            RoomType standard = new RoomType();
            standard.setTypeName("Standard");
            standard.setDescription("A comfortable standard room with essential amenities.");
            standard.setDefaultMaxOccupancy(2);
            standard.setBaseRate(new java.math.BigDecimal("150.00"));
            roomTypeDAO.addRoomType(standard);
            
            // Create Deluxe Room
            RoomType deluxe = new RoomType();
            deluxe.setTypeName("Deluxe");
            deluxe.setDescription("Spacious deluxe room with premium amenities.");
            deluxe.setDefaultMaxOccupancy(3);
            deluxe.setBaseRate(new java.math.BigDecimal("250.00"));
            roomTypeDAO.addRoomType(deluxe);
            
            // Create Suite
            RoomType suite = new RoomType();
            suite.setTypeName("Suite");
            suite.setDescription("Luxury suite with separate living area.");
            suite.setDefaultMaxOccupancy(4);
            suite.setBaseRate(new java.math.BigDecimal("400.00"));
            roomTypeDAO.addRoomType(suite);
            
            System.out.println("Sample room types created successfully.");
        } catch (Exception e) {
            System.err.println("Error creating sample room types: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void createSampleRooms(RoomDAO roomDAO, ObservableList<RoomType> roomTypes) {
        try {
            int count = 0;
            
            // Create rooms for each floor
            for (int floor = 1; floor <= 2; floor++) {
                // Create a few rooms of each type on each floor
                for (RoomType type : roomTypes) {
                    for (int i = 0; i < 2; i++) {
                        Room room = new Room();
                        room.setRoomNumber(floor + "0" + (++count));
                        room.setRoomTypeID(type.getRoomTypeID());
                        room.setRoomTypeName(type.getTypeName());
                        room.setFloorNumber(floor);
                        
                        // Mix up statuses
                        String status = "Available";
                        if (count % 5 == 0) status = "Occupied";
                        if (count % 7 == 0) status = "Maintenance";
                        if (count % 11 == 0) status = "Reserved";
                        room.setStatus(status);
                        
                        room.setMaxOccupancy(type.getDefaultMaxOccupancy());
                        room.setFeatures(type.getTypeName() + " room with view");
                        
                        roomDAO.addRoom(room);
                        System.out.println("Created room " + room.getRoomNumber());
                    }
                }
            }
            
            System.out.println("Created " + count + " sample rooms");
        } catch (Exception e) {
            System.err.println("Error creating sample rooms: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 