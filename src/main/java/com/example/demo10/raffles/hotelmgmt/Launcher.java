package com.example.demo10.raffles.hotelmgmt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

/**
 * Launcher class to start the application directly from IDE run button
 * This class is a workaround for JavaFX modularity issues when running directly from IDE
 */
public class Launcher {
    
    /**
     * Main entry point for the application
     * @param args command line arguments
     */
    public static void main(String[] args) {
        try {
            // Enable verbose JavaFX logging
            System.setProperty("javafx.verbose", "true");
            
            // Check if JavaFX is already in module path
            try {
                Class.forName("javafx.application.Application");
                System.out.println("JavaFX is available on module path. Launching application...");
                MainApp.main(args);
                return;
            } catch (ClassNotFoundException e) {
                System.out.println("JavaFX not found on module path. Attempting to locate JavaFX jars...");
            }
            
            List<String> modulePath = new ArrayList<>();
            String userHome = System.getProperty("user.home");
            String[] versions = {"17.0.10", "17.0.9", "17.0.8", "17.0.7", "17.0.6", "17.0.2", "17.0.1", "17"};
            String[] modules = {"javafx-base", "javafx-controls", "javafx-fxml", "javafx-graphics", "javafx-media"};
            
            // Try multiple locations for JavaFX jars
            
            // 1. Try Maven repository
            boolean foundInMaven = tryMavenRepository(modulePath, userHome, versions, modules);
            
            // 2. Try JavaFX SDK in common locations
            if (!foundInMaven) {
                boolean foundInCommonLocations = tryCommonLocations(modulePath);
                
                if (!foundInCommonLocations) {
                    System.err.println("ERROR: Could not find JavaFX modules in any location.");
                    System.err.println("Please run with one of these methods:");
                    System.err.println("1. Use the run-raffles.bat file in the project root directory");
                    System.err.println("2. Use Maven: mvnw javafx:run");
                    return;
                }
            }
            
            // Set the module path programmatically
            String modulePathString = String.join(File.pathSeparator, modulePath);
            System.setProperty("javafx.modulepath", modulePathString);
            
            // Add VM arguments
            String vmArgs = "--module-path \"" + modulePathString + "\" --add-modules javafx.controls,javafx.fxml,javafx.media,javafx.graphics";
            System.setProperty("java.module.path", modulePathString);
            System.out.println("Set module path to: " + modulePathString);
            System.out.println("VM args: " + vmArgs);
            
            // Launch the application
            System.out.println("Starting MainApp with custom module path...");
            MainApp.main(args);
            
        } catch (Exception e) {
            System.err.println("Error launching application: " + e.getMessage());
            e.printStackTrace();
            
            // As last resort, try to run with Maven
            try {
                System.out.println("Attempting to launch with Maven as last resort...");
                ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "mvnw.cmd", "javafx:run");
                pb.inheritIO();
                Process process = pb.start();
                process.waitFor();
            } catch (Exception ex) {
                System.err.println("Failed to launch with Maven: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Try to find JavaFX modules in Maven repository
     */
    private static boolean tryMavenRepository(List<String> modulePath, String userHome, String[] versions, String[] modules) {
        String mavenRepo = userHome + File.separator + ".m2" + File.separator + "repository";
        String javafxPath = mavenRepo + File.separator + "org" + File.separator + "openjfx";
        
        boolean foundAny = false;
        
        // Try each version in order
        for (String version : versions) {
            boolean foundAllForVersion = true;
            List<String> tempModulePath = new ArrayList<>();
            
            for (String module : modules) {
                String path = javafxPath + File.separator + module + File.separator + version;
                File dir = new File(path);
                
                if (dir.exists() && dir.isDirectory()) {
                    File[] files = dir.listFiles((d, name) -> name.endsWith(".jar") && 
                                                 !name.contains("sources") && 
                                                 !name.contains("javadoc") &&
                                                 (name.contains("win") || !name.contains("-")));
                    if (files != null && files.length > 0) {
                        tempModulePath.add(files[0].getAbsolutePath());
                        System.out.println("Found " + module + " at: " + files[0].getAbsolutePath());
                    } else {
                        foundAllForVersion = false;
                        break;
                    }
                } else {
                    foundAllForVersion = false;
                    break;
                }
            }
            
            if (foundAllForVersion) {
                modulePath.addAll(tempModulePath);
                foundAny = true;
                System.out.println("Found complete JavaFX " + version + " in Maven repository");
                break; // Use the first complete version found
            }
        }
        
        return foundAny;
    }
    
    /**
     * Try to find JavaFX modules in common installation locations
     */
    private static boolean tryCommonLocations(List<String> modulePath) {
        List<String> commonLocations = Arrays.asList(
            "C:\\Program Files\\Java\\javafx-sdk",
            "C:\\Program Files\\javafx-sdk",
            System.getProperty("user.home") + File.separator + "javafx-sdk",
            "C:\\javafx-sdk"
        );
        
        for (String baseLocation : commonLocations) {
            File dir = new File(baseLocation);
            if (dir.exists() && dir.isDirectory()) {
                // Check sub-directories for version-specific folders
                File[] versionDirs = dir.listFiles(File::isDirectory);
                
                if (versionDirs != null && versionDirs.length > 0) {
                    for (File versionDir : versionDirs) {
                        File libDir = new File(versionDir, "lib");
                        if (libDir.exists() && libDir.isDirectory()) {
                            File[] jars = libDir.listFiles((d, name) -> name.endsWith(".jar"));
                            if (jars != null && jars.length >= 5) { // At least 5 JARs for the core JavaFX modules
                                for (File jar : jars) {
                                    modulePath.add(jar.getAbsolutePath());
                                    System.out.println("Found JavaFX JAR: " + jar.getAbsolutePath());
                                }
                                return true;
                            }
                        }
                    }
                }
                
                // Check if there's a direct lib directory
                File libDir = new File(baseLocation, "lib");
                if (libDir.exists() && libDir.isDirectory()) {
                    File[] jars = libDir.listFiles((d, name) -> name.endsWith(".jar"));
                    if (jars != null && jars.length >= 5) { // At least 5 JARs for the core JavaFX modules
                        for (File jar : jars) {
                            modulePath.add(jar.getAbsolutePath());
                            System.out.println("Found JavaFX JAR: " + jar.getAbsolutePath());
                        }
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
} 