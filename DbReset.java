package com.example.demo10.raffles.hotelmgmt.util;

import java.io.File;

/**
 * Standalone utility to reset the database
 */
public class DbReset {
    public static void main(String[] args) {
        System.out.println("Starting complete database reset utility...");
        
        try {
            // First, find and delete all database files
            String dbFolder = System.getProperty("user.home") + File.separator + "raffles_db";
            File folder = new File(dbFolder);
            
            if (folder.exists() && folder.isDirectory()) {
                System.out.println("Found database folder at: " + folder.getAbsolutePath());
                File[] files = folder.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile() && file.getName().startsWith("raffleshotel")) {
                            boolean deleted = file.delete();
                            System.out.println("Deleted file " + file.getName() + ": " + deleted);
                        }
                    }
                }
                System.out.println("All database files have been deleted.");
            } else {
                System.out.println("Database folder not found at: " + dbFolder);
            }
            
            System.out.println("Database has been reset. Next time you run the application, a fresh database will be created.");
            
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR during database reset: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("Database reset utility completed.");
    }
} 