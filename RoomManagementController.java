package com.example.demo10.raffles.hotelmgmt.controller;

import com.example.demo10.raffles.hotelmgmt.DatabaseConnector;
import com.example.demo10.raffles.hotelmgmt.MainApp;
import com.example.demo10.raffles.hotelmgmt.dao.RoomDAO;
import com.example.demo10.raffles.hotelmgmt.dao.RoomTypeDAO;
import com.example.demo10.raffles.hotelmgmt.model.Room;
import com.example.demo10.raffles.hotelmgmt.model.RoomType;
import com.example.demo10.raffles.hotelmgmt.ui.DialogUtil;
import com.example.demo10.raffles.hotelmgmt.ui.UIConstants;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.math.BigDecimal;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class RoomManagementController {

    private MainApp mainApp;
    private RoomDAO roomDAO;
    private RoomTypeDAO roomTypeDAO;
    private ObservableList<Room> roomsList;
    private FilteredList<Room> filteredRoomsList;
    private TableView<Room> roomsTable;
    private String currentFloorFilter = "All Floors";
    private String currentTypeFilter = "All Types";
    
    // Status counts for the summary display
    private Label totalRoomsLabel;
    private Label availableLabel;
    private Label occupiedLabel;
    private Label maintenanceLabel;
    private Label reservedLabel;
    
    private HBox actionBar;
    
    public RoomManagementController(MainApp mainApp) {
        this.mainApp = mainApp;
        this.roomDAO = new RoomDAO();
        this.roomTypeDAO = new RoomTypeDAO();
        this.roomsList = FXCollections.observableArrayList();
        
        // Initialize the filtered list to show all items at the beginning
        this.filteredRoomsList = new FilteredList<>(roomsList);
        
        // Check if room types and rooms exist in the database
        System.out.println("RoomManagementController: Checking if room data exists...");
        
        ObservableList<RoomType> roomTypes = roomTypeDAO.getAllRoomTypes();
        System.out.println("Room types found: " + roomTypes.size());
        
        ObservableList<Room> rooms = roomDAO.getAllRooms();
        System.out.println("Rooms found: " + rooms.size());
        
        // If no rooms exist, try to create some sample rooms
        if (rooms.isEmpty()) {
            System.out.println("No rooms found. Creating sample rooms...");
            
            // Check if we have room types
            if (roomTypes.isEmpty()) {
                System.out.println("No room types found either. Cannot create sample rooms.");
            } else {
                createSampleRooms(roomTypes);
            }
        }
    }
    
    private void createSampleRooms(ObservableList<RoomType> roomTypes) {
        try {
            // Create one room of each type
            int roomsCreated = 0;
            
            for (RoomType type : roomTypes) {
                Room room = new Room();
                room.setRoomNumber("10" + (roomsCreated + 1));
                room.setRoomTypeID(type.getRoomTypeID());
                room.setRoomTypeName(type.getTypeName());
                room.setFloorNumber(1);
                room.setStatus("Available");
                room.setMaxOccupancy(type.getDefaultMaxOccupancy());
                room.setFeatures("Sample " + type.getTypeName() + " room");
                room.setSmoking(false);
                
                boolean success = roomDAO.addRoom(room);
                if (success) {
                    System.out.println("Created sample room: " + room.getRoomNumber() + " (" + type.getTypeName() + ")");
                    roomsCreated++;
                } else {
                    System.err.println("Failed to create sample room for type: " + type.getTypeName());
                }
            }
            
            System.out.println("Created " + roomsCreated + " sample rooms");
        } catch (Exception e) {
            System.err.println("Error creating sample rooms: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public Node createRoomManagementPane() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        
        // Title with luxurious styling
        Text titleText = new Text("Room Management");
        titleText.setFont(Font.font(UIConstants.FONT_SERIF_ELEGANT, FontWeight.BOLD, 36));
        titleText.setFill(UIConstants.ACCENT_COLOR_DARK_BROWN_FX);
        titleText.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.3), 4, 0, 1, 1));
        
        HBox titleBar = new HBox(15);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.getChildren().add(titleText);
        
        // Action bar with luxurious styling
        actionBar = new HBox(15);
        actionBar.setPadding(new Insets(10, 0, 20, 0));
        actionBar.setAlignment(Pos.CENTER_LEFT);
        
        // Add Room Button with gold gradient
        Button addButton = new Button("Add Room");
        addButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, " + UIConstants.BRAND_COLOR_HEX + ", " + UIConstants.GOLD_COLOR_HEX + "); " +
            "-fx-text-fill: white; -fx-font-family: '" + UIConstants.FONT_SANS_SERIF_CLEAN + "'; " +
            "-fx-font-size: 14px; -fx-padding: 8px 15px; -fx-background-radius: 5px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 3, 0, 0, 1);"
        );
        addButton.setOnAction(e -> showRoomDialog(null));
        
        // Refresh Button with elegant styling
        Button refreshButton = new Button("Refresh");
        refreshButton.setStyle(
            "-fx-background-color: " + UIConstants.ACCENT_COLOR_DARK_BROWN_HEX + "; " +
            "-fx-text-fill: white; -fx-font-family: '" + UIConstants.FONT_SANS_SERIF_CLEAN + "'; " +
            "-fx-font-size: 14px; -fx-padding: 8px 15px; -fx-background-radius: 5px;"
        );
        refreshButton.setOnAction(e -> refreshRooms());
        
        // Floor Filter ComboBox
        ComboBox<String> floorFilter = new ComboBox<>();
        floorFilter.getItems().addAll("All Floors", "1st Floor", "2nd Floor", "3rd Floor", "4th Floor", "5th Floor");
        floorFilter.setValue("All Floors");
        floorFilter.setPrefWidth(150);
        floorFilter.setStyle(
            "-fx-font-family: '" + UIConstants.FONT_SANS_SERIF_CLEAN + "'; " +
            "-fx-font-size: 14px;"
        );
        floorFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            currentFloorFilter = newVal;
            updateFilters();
        });
        
        // Room Type Filter ComboBox
        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.getItems().add("All Types");
        // Add room types from database
        for (RoomType type : roomTypeDAO.getAllRoomTypes()) {
            typeFilter.getItems().add(type.getTypeName());
        }
        typeFilter.setValue("All Types");
        typeFilter.setPrefWidth(150);
        typeFilter.setStyle(
            "-fx-font-family: '" + UIConstants.FONT_SANS_SERIF_CLEAN + "'; " +
            "-fx-font-size: 14px;"
        );
        typeFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            currentTypeFilter = newVal;
            updateFilters();
        });
        
        // Status Filter ComboBox
        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll(
            "All Statuses", "Available", "Occupied", "Reserved", "Maintenance", "Cleaning", "Out of Order"
        );
        statusFilter.setValue("All Statuses");
        statusFilter.setPrefWidth(150);
        statusFilter.setStyle(
            "-fx-font-family: '" + UIConstants.FONT_SANS_SERIF_CLEAN + "'; " +
            "-fx-font-size: 14px;"
        );
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateFilters();
        });
        
        // Layout elements with elegant labels
        Label floorLabel = new Label("Floor:");
        floorLabel.setStyle("-fx-font-weight: bold;");
        
        Label typeLabel = new Label("Type:");
        typeLabel.setStyle("-fx-font-weight: bold;");
        
        Label statusLabel = new Label("Status:");
        statusLabel.setStyle("-fx-font-weight: bold;");
        
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Add everything to the action bar
        actionBar.getChildren().addAll(
            addButton, refreshButton, 
            floorLabel, floorFilter, 
            typeLabel, typeFilter,
            statusLabel, statusFilter,
            spacer
        );
        
        // Create room table with elegant styling
        roomsTable = new TableView<>(filteredRoomsList);
        roomsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        roomsTable.setStyle("-fx-background-color: white; -fx-background-radius: 5px; " + UIConstants.SHADOW_EFFECT_CSS);
        
        // Define columns with proper styling
        TableColumn<Room, String> roomNumberCol = new TableColumn<>("Room Number");
        roomNumberCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        roomNumberCol.setPrefWidth(120);
        
        TableColumn<Room, String> roomTypeCol = new TableColumn<>("Room Type");
        roomTypeCol.setCellValueFactory(new PropertyValueFactory<>("roomTypeName"));
        roomTypeCol.setPrefWidth(150);
        
        TableColumn<Room, Integer> floorCol = new TableColumn<>("Floor");
        floorCol.setCellValueFactory(new PropertyValueFactory<>("floorNumber"));
        floorCol.setPrefWidth(80);
        
        TableColumn<Room, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);
        
        // Customize status cell to show colored status indicators
        statusCol.setCellFactory(column -> new TableCell<Room, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(status);
                    
                    // Create colored status indicator
                    Circle indicator = new Circle(6);
                    
                    switch (status) {
                        case "Available":
                            indicator.setFill(Color.GREEN);
                            break;
                        case "Occupied":
                            indicator.setFill(Color.BLUE);
                            break;
                        case "Reserved":
                            indicator.setFill(Color.ORANGE);
                            break;
                        case "Maintenance":
                            indicator.setFill(Color.RED);
                            break;
                        case "Cleaning":
                            indicator.setFill(Color.YELLOW);
                            break;
                        case "Out of Order":
                            indicator.setFill(Color.DARKGRAY);
                            break;
                        default:
                            indicator.setFill(Color.LIGHTGRAY);
                    }
                    
                    HBox box = new HBox(10);
                    box.setAlignment(Pos.CENTER_LEFT);
                    box.getChildren().addAll(indicator, new Label(status));
                    setGraphic(box);
                }
            }
        });
        
        TableColumn<Room, Integer> maxOccupancyCol = new TableColumn<>("Occupancy");
        maxOccupancyCol.setCellValueFactory(new PropertyValueFactory<>("maxOccupancy"));
        maxOccupancyCol.setPrefWidth(100);
        
        TableColumn<Room, Boolean> isSmoking = new TableColumn<>("Smoking");
        isSmoking.setCellValueFactory(new PropertyValueFactory<>("smoking"));
        isSmoking.setPrefWidth(80);
        isSmoking.setCellFactory(column -> new TableCell<Room, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Yes" : "No");
                }
            }
        });
        
        TableColumn<Room, String> featuresCol = new TableColumn<>("Features");
        featuresCol.setCellValueFactory(new PropertyValueFactory<>("features"));
        featuresCol.setPrefWidth(200);
        
        // Add actions column with buttons
        TableColumn<Room, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(column -> new TableCell<Room, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final Button statusBtn = new Button("Status");
            private final HBox pane = new HBox(5);
            
            {
                // Style buttons
                editBtn.setStyle("-fx-background-color: " + UIConstants.BRAND_COLOR_HEX + "; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 3 8;");
                deleteBtn.setStyle("-fx-background-color: " + UIConstants.ERROR_COLOR_HEX + "; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 3 8;");
                statusBtn.setStyle("-fx-background-color: " + UIConstants.ACCENT_COLOR_DARK_BROWN_HEX + "; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 3 8;");
                
                pane.setAlignment(Pos.CENTER);
                pane.getChildren().addAll(editBtn, statusBtn, deleteBtn);
                
                // Set button actions
                editBtn.setOnAction(event -> {
                    Room room = getTableView().getItems().get(getIndex());
                    showRoomDialog(room);
                });
                
                statusBtn.setOnAction(event -> {
                    Room room = getTableView().getItems().get(getIndex());
                    showStatusChangeDialog(room);
                });
                
                deleteBtn.setOnAction(event -> {
                    Room room = getTableView().getItems().get(getIndex());
                    deleteRoom(room);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        actionsCol.setPrefWidth(180);
        
        // Add all columns to the table
        roomsTable.getColumns().addAll(
            roomNumberCol, roomTypeCol, floorCol, statusCol, maxOccupancyCol, isSmoking, featuresCol, actionsCol
        );
        
        VBox.setVgrow(roomsTable, Priority.ALWAYS);
        
        // Create status summary panel with elegant styling
        GridPane statusSummary = new GridPane();
        statusSummary.setHgap(30);
        statusSummary.setVgap(10);
        statusSummary.setPadding(new Insets(20));
        statusSummary.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 5px; " + 
            UIConstants.SHADOW_EFFECT_CSS
        );
        
        // Summary Title
        Label summaryLabel = new Label("Room Status Summary");
        summaryLabel.setFont(Font.font(UIConstants.FONT_SERIF_ELEGANT, FontWeight.BOLD, 18));
        summaryLabel.setTextFill(UIConstants.ACCENT_COLOR_DARK_BROWN_FX);
        
        // Status count labels
        totalRoomsLabel = new Label("Total Rooms: 0");
        totalRoomsLabel.setFont(Font.font(UIConstants.FONT_SANS_SERIF_CLEAN, FontWeight.BOLD, 14));
        
        availableLabel = new Label("Available: 0");
        availableLabel.setFont(Font.font(UIConstants.FONT_SANS_SERIF_CLEAN, 14));
        availableLabel.setTextFill(Color.GREEN);
        
        occupiedLabel = new Label("Occupied: 0");
        occupiedLabel.setFont(Font.font(UIConstants.FONT_SANS_SERIF_CLEAN, 14));
        occupiedLabel.setTextFill(Color.BLUE);
        
        maintenanceLabel = new Label("Maintenance: 0");
        maintenanceLabel.setFont(Font.font(UIConstants.FONT_SANS_SERIF_CLEAN, 14));
        maintenanceLabel.setTextFill(Color.RED);
        
        reservedLabel = new Label("Reserved: 0");
        reservedLabel.setFont(Font.font(UIConstants.FONT_SANS_SERIF_CLEAN, 14));
        reservedLabel.setTextFill(Color.ORANGE);
        
        // Add everything to the status summary
        statusSummary.add(summaryLabel, 0, 0, 5, 1);
        statusSummary.add(totalRoomsLabel, 0, 1);
        statusSummary.add(availableLabel, 1, 1);
        statusSummary.add(occupiedLabel, 2, 1);
        statusSummary.add(maintenanceLabel, 3, 1);
        statusSummary.add(reservedLabel, 4, 1);
        
        // Put everything together in the main content
        content.getChildren().addAll(titleBar, actionBar, roomsTable, statusSummary);
        
        // Force a refresh to ensure data is loaded
        System.out.println("Room Management pane created, forcing data refresh...");
        Platform.runLater(() -> {
            try {
                // Get fresh data
                roomsList.clear();
                ObservableList<Room> freshRooms = roomDAO.getAllRooms();
                roomsList.addAll(freshRooms);
                System.out.println("Loaded " + roomsList.size() + " rooms into table");
                
                // Reset filters
                filteredRoomsList.setPredicate(null);
                
                // Update counts
                updateStatusCounts();
                
                // Force table refresh
                roomsTable.refresh();
                roomsTable.requestLayout();
                
                System.out.println("Room table refreshed with " + filteredRoomsList.size() + " rooms");
            } catch (Exception e) {
                System.err.println("Error refreshing room data: " + e.getMessage());
                e.printStackTrace();
            }
        });
        
        return content;
    }
    
    private void refreshRooms() {
        System.out.println("Refreshing rooms list...");
        try {
            // Force a full refresh of database connection
            DatabaseConnector.checkpoint();
            
            // Clear existing rooms and load fresh data
            ObservableList<Room> freshRooms = roomDAO.getAllRooms();
            System.out.println("Loaded " + freshRooms.size() + " rooms from database");
            
            // Reset filters to "All" to ensure all rooms are visible after refresh
            currentFloorFilter = "All Floors";
            currentTypeFilter = "All Types";
            
            // Reset filter combos if they exist
            Platform.runLater(() -> {
                for (Node node : actionBar.getChildren()) {
                    if (node instanceof ComboBox) {
                        @SuppressWarnings("unchecked")
                        ComboBox<String> comboBox = (ComboBox<String>) node;
                        if (comboBox.getItems().contains("All Floors")) {
                            comboBox.setValue("All Floors");
                        } else if (comboBox.getItems().contains("All Types")) {
                            comboBox.setValue("All Types");
                        } else if (comboBox.getItems().contains("All Statuses")) {
                            comboBox.setValue("All Statuses");
                        }
                    }
                }
            });
            
            // Update the observable list with new data
            Platform.runLater(() -> {
                roomsList.clear(); // Clear first to ensure proper refresh  
                roomsList.addAll(freshRooms);
                
                // Reset filters
                filteredRoomsList.setPredicate(null); // Show all rooms
                
                // Update status counts
                updateStatusCounts();
                
                // Force table refresh
                roomsTable.refresh();
                System.out.println("Table refreshed, showing " + filteredRoomsList.size() + " rooms");
            });
        } catch (Exception e) {
            System.err.println("Error refreshing rooms: " + e.getMessage());
            e.printStackTrace();
            
            // Show error dialog
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Database Error");
                alert.setHeaderText("Failed to load rooms");
                alert.setContentText("There was a problem loading the rooms from the database: " + e.getMessage());
                alert.showAndWait();
            });
        }
    }
    
    private void updateFilters() {
        System.out.println("Updating filters: Floor=" + currentFloorFilter + ", Type=" + currentTypeFilter);
        
        filteredRoomsList.setPredicate(room -> {
            boolean matchesFloor = true;
            boolean matchesType = true;
            
            // Floor filter
            if (!currentFloorFilter.equals("All Floors")) {
                int floor = extractFloorNumber(currentFloorFilter);
                matchesFloor = room.getFloorNumber() == floor;
            }
            
            // Type filter
            if (!currentTypeFilter.equals("All Types")) {
                matchesType = currentTypeFilter.equals(room.getRoomTypeName());
            }
            
            boolean finalResult = matchesFloor && matchesType;
            
            if (!finalResult) {
                System.out.println("Room " + room.getRoomNumber() + " filtered out: [Floor match: " + matchesFloor + 
                                  ", Type match: " + matchesType + "]");
            }
            
            return finalResult;
        });
        
        // Refresh the table to show the filtered results
        Platform.runLater(() -> {
            roomsTable.refresh();
            System.out.println("Filter applied, filtered list now has " + filteredRoomsList.size() + " rooms");
        });
        
        // Update status counts based on filtered data
        updateFilteredStatusCounts();
    }
    
    private int extractFloorNumber(String floorText) {
        // Convert "1st Floor", "2nd Floor", etc. to 1, 2, etc.
        return Integer.parseInt(floorText.substring(0, floorText.indexOf("st") > 0 ? 1 : 
                                                  (floorText.indexOf("nd") > 0 ? 1 : 
                                                  (floorText.indexOf("rd") > 0 ? 1 : 
                                                  (floorText.indexOf("th") > 0 ? 1 : 1)))));
    }
    
    private void updateStatusCounts() {
        int total = roomDAO.getTotalRoomCount();
        int available = roomDAO.countRoomsByStatus("Available");
        int occupied = roomDAO.countRoomsByStatus("Occupied");
        int maintenance = roomDAO.countRoomsByStatus("Maintenance");
        int reserved = roomDAO.countRoomsByStatus("Reserved");
        
        totalRoomsLabel.setText("Total Rooms: " + total);
        availableLabel.setText("Available: " + available);
        occupiedLabel.setText("Occupied: " + occupied);
        maintenanceLabel.setText("Maintenance: " + maintenance);
        reservedLabel.setText("Reserved: " + reserved);
    }
    
    private void updateFilteredStatusCounts() {
        if (currentFloorFilter.equals("All Floors") && currentTypeFilter.equals("All Types")) {
            // Use the database counts if no filtering
            updateStatusCounts();
            return;
        }
        
        // Count filtered items by status
        Map<String, Integer> statusCounts = new HashMap<>();
        statusCounts.put("Available", 0);
        statusCounts.put("Occupied", 0);
        statusCounts.put("Maintenance", 0);
        statusCounts.put("Reserved", 0);
        
        for (Room room : filteredRoomsList) {
            String status = room.getStatus();
            statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);
        }
        
        totalRoomsLabel.setText("Total Rooms: " + filteredRoomsList.size());
        availableLabel.setText("Available: " + statusCounts.getOrDefault("Available", 0));
        occupiedLabel.setText("Occupied: " + statusCounts.getOrDefault("Occupied", 0));
        maintenanceLabel.setText("Maintenance: " + statusCounts.getOrDefault("Maintenance", 0));
        reservedLabel.setText("Reserved: " + statusCounts.getOrDefault("Reserved", 0));
    }
    
    private void showRoomDialog(Room room) {
        boolean isNewRoom = (room == null);
        Room editRoom = isNewRoom ? new Room() : room;
        
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isNewRoom ? "Add New Room" : "Edit Room");
        dialog.setHeaderText(isNewRoom ? "Create a new room" : "Edit room details");
        
        // Apply luxurious styling to dialog
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().add("luxury-dialog");
        dialogPane.setStyle("-fx-background-color: linear-gradient(to bottom right, " + 
                UIConstants.ACCENT_COLOR_LIGHT_CREAM_HEX + ", #FFFFFF); " +
                "-fx-background-radius: 15px; -fx-padding: 20;");
        
        // Load CSS for dialog styling
        try {
            URL cssUrl = getClass().getResource("/styles/dialog-styles.css");
            if (cssUrl != null) {
                dialogPane.getStylesheets().add(cssUrl.toExternalForm());
                System.out.println("Successfully loaded dialog-styles.css for room dialog");
            } else {
                System.err.println("Warning: Could not find /styles/dialog-styles.css resource");
            }
        } catch (Exception e) {
            System.err.println("Error loading CSS stylesheet: " + e.getMessage());
        }
        
        // Set button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create a grid for the form fields
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 20, 10, 20));
        
        // Room number field
        TextField roomNumberField = new TextField();
        roomNumberField.setPromptText("Room Number");
        roomNumberField.setPrefWidth(200);
        roomNumberField.setStyle("-fx-font-size: 14px;");
        
        // Room type dropdown
        ComboBox<RoomType> roomTypeCombo = new ComboBox<>();
        roomTypeCombo.setPromptText("Select Room Type");
        roomTypeCombo.setPrefWidth(200);
        roomTypeCombo.setStyle("-fx-font-size: 14px;");
        
        // Get all room types and ensure the list is not empty
        ObservableList<RoomType> roomTypes = roomTypeDAO.getAllRoomTypes();
        if (roomTypes.isEmpty()) {
            // Add default room types if none exist
            RoomType standard = new RoomType();
            standard.setRoomTypeID(1);
            standard.setTypeName("Standard");
            standard.setDefaultMaxOccupancy(2);
            roomTypes.add(standard);
        }
        roomTypeCombo.setItems(roomTypes);
        
        // Floor dropdown
        ComboBox<Integer> floorCombo = new ComboBox<>();
        floorCombo.setPromptText("Select Floor");
        floorCombo.getItems().addAll(1, 2, 3, 4, 5);
        floorCombo.setPrefWidth(200);
        floorCombo.setStyle("-fx-font-size: 14px;");
        
        // Status dropdown
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.setPromptText("Select Status");
        statusCombo.getItems().addAll(
            "Available", "Occupied", "Reserved", "Maintenance", "Cleaning", "Out of Order"
        );
        statusCombo.setPrefWidth(200);
        statusCombo.setStyle("-fx-font-size: 14px;");
        
        // Max occupancy field
        Spinner<Integer> occupancySpinner = new Spinner<>(1, 10, 2);
        occupancySpinner.setEditable(true);
        occupancySpinner.setPrefWidth(200);
        occupancySpinner.setStyle("-fx-font-size: 14px;");
        
        // Smoking checkbox
        CheckBox smokingCheck = new CheckBox("Smoking Allowed");
        smokingCheck.setStyle("-fx-font-size: 14px;");
        
        // Features textarea
        TextArea featuresArea = new TextArea();
        featuresArea.setPromptText("Room Features (e.g., King Bed, Ocean View)");
        featuresArea.setPrefRowCount(3);
        featuresArea.setPrefWidth(300);
        featuresArea.setStyle("-fx-font-size: 14px;");
        
        // Notes textarea
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Additional Notes");
        notesArea.setPrefRowCount(3);
        notesArea.setPrefWidth(300);
        notesArea.setStyle("-fx-font-size: 14px;");
        
        // Populate fields if editing existing room
        if (!isNewRoom) {
            roomNumberField.setText(editRoom.getRoomNumber());
            
            if (editRoom.getRoomTypeID() != null) {
                for (RoomType rt : roomTypeCombo.getItems()) {
                    if (rt.getRoomTypeID() == editRoom.getRoomTypeID()) {
                        roomTypeCombo.setValue(rt);
                        break;
                    }
                }
            }
            
            if (editRoom.getFloorNumber() != null) {
                floorCombo.setValue(editRoom.getFloorNumber());
            }
            
            statusCombo.setValue(editRoom.getStatus() != null ? editRoom.getStatus() : "Available");
            
            if (editRoom.getMaxOccupancy() != null) {
                occupancySpinner.getValueFactory().setValue(editRoom.getMaxOccupancy());
            }
            
            smokingCheck.setSelected(editRoom.isSmoking());
            
            if (editRoom.getFeatures() != null) {
                featuresArea.setText(editRoom.getFeatures());
            }
            
            if (editRoom.getNotes() != null) {
                notesArea.setText(editRoom.getNotes());
            }
        } else {
            statusCombo.setValue("Available"); // Default status for new rooms
            floorCombo.setValue(1); // Default floor
        }
        
        // Add labels with elegant styling
        Label roomNumberLabel = new Label("Room Number:");
        roomNumberLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label roomTypeLabel = new Label("Room Type:");
        roomTypeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label floorLabel = new Label("Floor:");
        floorLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label statusLabel = new Label("Status:");
        statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label occupancyLabel = new Label("Max Occupancy:");
        occupancyLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label featuresLabel = new Label("Features:");
        featuresLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label notesLabel = new Label("Notes:");
        notesLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        // Add fields to the grid
        grid.add(roomNumberLabel, 0, 0);
        grid.add(roomNumberField, 1, 0);
        
        grid.add(roomTypeLabel, 0, 1);
        grid.add(roomTypeCombo, 1, 1);
        
        grid.add(floorLabel, 0, 2);
        grid.add(floorCombo, 1, 2);
        
        grid.add(statusLabel, 0, 3);
        grid.add(statusCombo, 1, 3);
        
        grid.add(occupancyLabel, 0, 4);
        grid.add(occupancySpinner, 1, 4);
        
        grid.add(smokingCheck, 1, 5);
        
        grid.add(featuresLabel, 0, 6);
        grid.add(featuresArea, 1, 6);
        
        grid.add(notesLabel, 0, 7);
        grid.add(notesArea, 1, 7);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the room number field by default
        Platform.runLater(() -> roomNumberField.requestFocus());
        
        // Convert the result to room data when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                editRoom.setRoomNumber(roomNumberField.getText());
                
                if (roomTypeCombo.getValue() != null) {
                    editRoom.setRoomTypeID(roomTypeCombo.getValue().getRoomTypeID());
                    editRoom.setRoomTypeName(roomTypeCombo.getValue().getTypeName());
                } else {
                    // Set default room type if none selected
                    if (!roomTypeCombo.getItems().isEmpty()) {
                        RoomType defaultType = roomTypeCombo.getItems().get(0);
                        editRoom.setRoomTypeID(defaultType.getRoomTypeID());
                        editRoom.setRoomTypeName(defaultType.getTypeName());
                    }
                }
                
                editRoom.setFloorNumber(floorCombo.getValue());
                editRoom.setStatus(statusCombo.getValue());
                editRoom.setMaxOccupancy(occupancySpinner.getValue());
                editRoom.setSmoking(smokingCheck.isSelected());
                editRoom.setFeatures(featuresArea.getText());
                editRoom.setNotes(notesArea.getText());
                
                return ButtonType.OK;
            }
            return null;
        });
        
        // Show the dialog and process the result
        Optional<ButtonType> result = dialog.showAndWait();
        
        if (result.isPresent() && result.get() == saveButtonType) {
            boolean success;
            
            if (isNewRoom) {
                System.out.println("Adding new room: " + editRoom.getRoomNumber());
                success = roomDAO.addRoom(editRoom);
                if (success) {
                    System.out.println("Room added successfully with ID: " + editRoom.getRoomID() + 
                                       ", Number: " + editRoom.getRoomNumber() + 
                                       ", Type: " + editRoom.getRoomTypeName());
                    // Success message
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Room " + editRoom.getRoomNumber() + " added successfully!");
                    successAlert.showAndWait();
                }
            } else {
                System.out.println("Updating room: " + editRoom.getRoomNumber());
                success = roomDAO.updateRoom(editRoom);
                if (success) {
                    System.out.println("Room updated successfully with ID: " + editRoom.getRoomID());
                }
            }
            
            if (success) {
                // Reset all filters to ensure all rooms are visible
                currentFloorFilter = "All Floors";
                currentTypeFilter = "All Types";
                
                // Do a full refresh to show the new/updated room
                refreshRooms();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Database Error");
                alert.setHeaderText(null);
                alert.setContentText(isNewRoom ? "Failed to add room." : "Failed to update room.");
                alert.showAndWait();
            }
        }
    }
    
    private void showStatusChangeDialog(Room room) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Change Room Status");
        dialog.setHeaderText("Change status for Room " + room.getRoomNumber());
        
        // Set button types
        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);
        
        // Create a grid for the form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Status dropdown
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(
            "Available", "Occupied", "Reserved", "Maintenance", "Cleaning", "Out of Order"
        );
        statusCombo.setValue(room.getStatus());
        
        // Add field to the grid
        grid.add(new Label("New Status:"), 0, 0);
        grid.add(statusCombo, 1, 0);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the status dropdown
        Platform.runLater(() -> statusCombo.requestFocus());
        
        // Convert the result when the update button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                return statusCombo.getValue();
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            String newStatus = result.get();
            boolean success = roomDAO.updateRoomStatus(room.getRoomID(), newStatus);
            
            if (success) {
                // Update the room's status in the display model
                room.setStatus(newStatus);
                roomsTable.refresh();
                
                // Refresh status counts
                updateStatusCounts();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Database Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to update room status.");
                alert.showAndWait();
            }
        }
    }
    
    private void deleteRoom(Room room) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Deletion");
        confirmDialog.setHeaderText("Delete Room " + room.getRoomNumber());
        confirmDialog.setContentText("Are you sure you want to delete this room? This cannot be undone.");
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = roomDAO.deleteRoom(room.getRoomID());
            
            if (success) {
                roomsList.remove(room);
                
                // Refresh status counts
                updateStatusCounts();
                
                // Show success message
                Alert successDialog = new Alert(Alert.AlertType.INFORMATION);
                successDialog.setTitle("Success");
                successDialog.setHeaderText(null);
                successDialog.setContentText("Room deleted successfully.");
                successDialog.showAndWait();
            } else {
                Alert errorDialog = new Alert(Alert.AlertType.ERROR);
                errorDialog.setTitle("Database Error");
                errorDialog.setHeaderText(null);
                errorDialog.setContentText("Failed to delete room. It may be referenced by reservations or other records.");
                errorDialog.showAndWait();
            }
        }
    }
} 