package com.example.demo10.raffles.hotelmgmt.controller;

import com.example.demo10.raffles.hotelmgmt.MainApp;
import com.example.demo10.raffles.hotelmgmt.dao.GuestDAO;
import com.example.demo10.raffles.hotelmgmt.dao.ReservationDAO;
import com.example.demo10.raffles.hotelmgmt.dao.RoomDAO;
import com.example.demo10.raffles.hotelmgmt.dao.RoomTypeDAO;
import com.example.demo10.raffles.hotelmgmt.model.Guest;
import com.example.demo10.raffles.hotelmgmt.model.Reservation;
import com.example.demo10.raffles.hotelmgmt.model.Room;
import com.example.demo10.raffles.hotelmgmt.model.RoomType;
import com.example.demo10.raffles.hotelmgmt.ui.DialogUtil;
import com.example.demo10.raffles.hotelmgmt.ui.UIConstants;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class GuestPortalController {
    private final MainApp mainApp;
    private final Guest currentGuest;
    private final ReservationDAO reservationDAO;
    private final RoomDAO roomDAO;
    private final RoomTypeDAO roomTypeDAO;
    private final GuestDAO guestDAO;
    
    // Content panes
    private BorderPane root;
    private Node reservationsContent;
    private Node profileContent;
    private Node bookingContent;
    
    // Current active button for styling
    private Button activeButton;
    
    public GuestPortalController(MainApp mainApp, Guest guest) {
        this.mainApp = mainApp;
        this.currentGuest = guest;
        this.reservationDAO = new ReservationDAO();
        this.roomDAO = new RoomDAO();
        this.roomTypeDAO = new RoomTypeDAO();
        this.guestDAO = new GuestDAO();
    }
    
    public Scene createGuestPortalScene() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: " + UIConstants.ACCENT_COLOR_LIGHT_CREAM_HEX + ";");
        
        // Create header
        HBox header = createHeader();
        root.setTop(header);
        
        // Create sidebar
        VBox sidebar = createSidebar();
        root.setLeft(sidebar);
        
        // Initialize content panes
        this.reservationsContent = createReservationsContent();
        this.profileContent = createProfileContent();
        this.bookingContent = createBookingContent();
        
        // Default content is reservation list
        root.setCenter(reservationsContent);
        
        return new Scene(root, UIConstants.INITIAL_WIDTH, UIConstants.INITIAL_HEIGHT);
    }
    
    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setPadding(new Insets(15, 30, 15, 30));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: " + UIConstants.ACCENT_COLOR_DARK_BROWN_HEX + ";");
        
        Text welcomeText = new Text("Welcome, " + currentGuest.getFirstName() + "!");
        welcomeText.setFont(Font.font(UIConstants.FONT_SERIF_ELEGANT, FontWeight.BOLD, 24));
        welcomeText.setFill(Color.WHITE);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle(UIConstants.LUXURY_BUTTON_STYLE);
        logoutBtn.setOnAction(e -> mainApp.logout());
        
        header.getChildren().addAll(welcomeText, spacer, logoutBtn);
        return header;
    }
    
    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("-fx-background-color: " + UIConstants.BRAND_COLOR_HEX + ";");
        sidebar.setPrefWidth(220);
        
        Button reservationsBtn = new Button("My Reservations");
        Button profileBtn = new Button("My Profile");
        Button bookingBtn = new Button("Book a Room");
        
        String normalButtonStyle = "-fx-background-color: transparent; -fx-text-fill: white; " +
                "-fx-font-family: '" + UIConstants.FONT_SANS_SERIF_CLEAN + "'; " +
                "-fx-font-size: 16px; -fx-padding: 15 10; -fx-alignment: CENTER_LEFT;";
        
        String activeButtonStyle = "-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; " +
                "-fx-font-family: '" + UIConstants.FONT_SANS_SERIF_CLEAN + "'; " +
                "-fx-font-size: 16px; -fx-padding: 15 10; -fx-alignment: CENTER_LEFT; " +
                "-fx-font-weight: bold;";
        
        reservationsBtn.setStyle(activeButtonStyle); // Default active button
        profileBtn.setStyle(normalButtonStyle);
        bookingBtn.setStyle(normalButtonStyle);
        
        reservationsBtn.setPrefWidth(200);
        profileBtn.setPrefWidth(200);
        bookingBtn.setPrefWidth(200);
        
        reservationsBtn.setOnAction(e -> {
            setActiveButton(reservationsBtn, normalButtonStyle, activeButtonStyle);
            root.setCenter(reservationsContent);
        });
        
        profileBtn.setOnAction(e -> {
            setActiveButton(profileBtn, normalButtonStyle, activeButtonStyle);
            root.setCenter(profileContent);
        });
        
        bookingBtn.setOnAction(e -> {
            setActiveButton(bookingBtn, normalButtonStyle, activeButtonStyle);
            root.setCenter(bookingContent);
        });
        
        // Set initial active button
        this.activeButton = reservationsBtn;
        
        sidebar.getChildren().addAll(
            new Label(""), // Spacer
            reservationsBtn,
            profileBtn,
            bookingBtn
        );
        
        return sidebar;
    }
    
    private void setActiveButton(Button newActiveButton, String normalStyle, String activeStyle) {
        // Reset previous active button
        if (activeButton != null) {
            activeButton.setStyle(normalStyle);
        }
        
        // Set new active button
        newActiveButton.setStyle(activeStyle);
        this.activeButton = newActiveButton;
    }
    
    // CRUD for Reservations
    private Node createReservationsContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        
        // Title
        Label titleLabel = new Label("My Reservations");
        titleLabel.setFont(Font.font(UIConstants.FONT_SERIF_ELEGANT, FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.web(UIConstants.ACCENT_COLOR_DARK_BROWN_HEX));
        
        // Actions bar
        HBox actionsBar = new HBox(15);
        actionsBar.setAlignment(Pos.CENTER_LEFT);
        
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle(UIConstants.STANDARD_BUTTON_STYLE);
        refreshBtn.setOnAction(e -> refreshReservations(content));
        
        actionsBar.getChildren().add(refreshBtn);
        
        // Table of reservations
        TableView<Reservation> reservationTable = new TableView<>();
        
        TableColumn<Reservation, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("reservationID"));
        idCol.setPrefWidth(60);
        
        TableColumn<Reservation, LocalDate> checkInCol = new TableColumn<>("Check-In");
        checkInCol.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
        checkInCol.setPrefWidth(120);
        checkInCol.setCellFactory(col -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : formatter.format(item));
            }
        });
        
        TableColumn<Reservation, LocalDate> checkOutCol = new TableColumn<>("Check-Out");
        checkOutCol.setCellValueFactory(new PropertyValueFactory<>("checkOutDate"));
        checkOutCol.setPrefWidth(120);
        checkOutCol.setCellFactory(col -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : formatter.format(item));
            }
        });
        
        TableColumn<Reservation, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        roomCol.setPrefWidth(100);
        
        TableColumn<Reservation, String> roomTypeCol = new TableColumn<>("Room Type");
        roomTypeCol.setCellValueFactory(new PropertyValueFactory<>("roomTypeName"));
        roomTypeCol.setPrefWidth(120);
        
        TableColumn<Reservation, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);
        
        // Actions column
        TableColumn<Reservation, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(200);
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button cancelBtn = new Button("Cancel");
            private final HBox buttonsPane = new HBox(10);
            
            {
                viewBtn.setStyle(UIConstants.SMALL_BUTTON_STYLE);
                cancelBtn.setStyle(UIConstants.SMALL_ERROR_BUTTON_STYLE);
                
                buttonsPane.setAlignment(Pos.CENTER);
                buttonsPane.getChildren().addAll(viewBtn, cancelBtn);
                
                viewBtn.setOnAction(e -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    showReservationDetails(reservation);
                });
                
                cancelBtn.setOnAction(e -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    cancelReservation(reservation, content);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    // Only show cancel button if reservation can be canceled
                    cancelBtn.setVisible(!reservation.getStatus().equalsIgnoreCase("Cancelled") && 
                                        !reservation.getStatus().equalsIgnoreCase("Completed"));
                    setGraphic(buttonsPane);
                }
            }
        });
        
        reservationTable.getColumns().addAll(idCol, checkInCol, checkOutCol, roomCol, roomTypeCol, statusCol, actionsCol);
        VBox.setVgrow(reservationTable, Priority.ALWAYS);
        
        // Get reservations for this guest
        ObservableList<Reservation> reservations = FXCollections.observableArrayList();
        if (currentGuest != null && currentGuest.getGuestID() > 0) {
            reservations = reservationDAO.getReservationsByGuestID(currentGuest.getGuestID());
        }
        
        reservationTable.setItems(reservations);
        
        if (reservations.isEmpty()) {
            reservationTable.setPlaceholder(new Label("You don't have any reservations yet."));
        }
        
        content.getChildren().addAll(titleLabel, actionsBar, reservationTable);
        return content;
    }
    
    private void refreshReservations(VBox contentPane) {
        // Clear existing reservation table
        TableView<Reservation> reservationTable = null;
        
        // Find the TableView in the content pane
        for (Node node : contentPane.getChildren()) {
            if (node instanceof TableView) {
                reservationTable = (TableView<Reservation>) node;
                break;
            }
        }
        
        if (reservationTable != null) {
            // Get fresh reservations data
            ObservableList<Reservation> reservations = reservationDAO.getReservationsByGuestID(currentGuest.getGuestID());
            
            // Update table
            reservationTable.setItems(reservations);
            
            if (reservations.isEmpty()) {
                reservationTable.setPlaceholder(new Label("You don't have any reservations yet."));
            }
        }
    }
    
    private void showReservationDetails(Reservation reservation) {
        // Get full reservation details
        Reservation fullReservation = reservationDAO.getReservationByIdWithDetails(reservation.getReservationID());
        
        if (fullReservation == null) {
            DialogUtil.showAlert(
                Alert.AlertType.ERROR, 
                mainApp.getPrimaryStage(),
                "Error", 
                "Could not load reservation details"
            );
            return;
        }
        
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Reservation Details");
        dialog.setHeaderText("Reservation #" + fullReservation.getReservationID());
        
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(closeButton);
        
        // Create content grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Add reservation details
        int row = 0;
        
        grid.add(new Label("Room:"), 0, row);
        grid.add(new Label(fullReservation.getRoomNumber() + " (" + fullReservation.getRoomTypeName() + ")"), 1, row++);
        
        grid.add(new Label("Check-in Date:"), 0, row);
        grid.add(new Label(fullReservation.getCheckInDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))), 1, row++);
        
        grid.add(new Label("Check-out Date:"), 0, row);
        grid.add(new Label(fullReservation.getCheckOutDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))), 1, row++);
        
        grid.add(new Label("Status:"), 0, row);
        grid.add(new Label(fullReservation.getStatus()), 1, row++);
        
        grid.add(new Label("Guests:"), 0, row);
        grid.add(new Label(
            (fullReservation.getNumberOfAdults() != null ? fullReservation.getNumberOfAdults() : 1) + " Adults, " +
            (fullReservation.getNumberOfChildren() != null ? fullReservation.getNumberOfChildren() : 0) + " Children"
        ), 1, row++);
        
        if (fullReservation.getTotalEstimatedCost() != null) {
            grid.add(new Label("Estimated Cost:"), 0, row);
            grid.add(new Label("$" + fullReservation.getTotalEstimatedCost()), 1, row++);
        }
        
        if (fullReservation.getSpecialRequests() != null && !fullReservation.getSpecialRequests().isEmpty()) {
            grid.add(new Label("Special Requests:"), 0, row);
            grid.add(new Label(fullReservation.getSpecialRequests()), 1, row++);
        }
        
        dialog.getDialogPane().setContent(grid);
        dialog.showAndWait();
    }
    
    private void cancelReservation(Reservation reservation, VBox contentPane) {
        // Show confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Cancel Reservation");
        confirmDialog.setHeaderText("Cancel Reservation #" + reservation.getReservationID());
        confirmDialog.setContentText("Are you sure you want to cancel this reservation?");
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Update reservation status to Cancelled
            boolean success = reservationDAO.updateReservationStatus(reservation.getReservationID(), "Cancelled");
            
            if (success) {
                // Show success message
                DialogUtil.showAlert(
                    Alert.AlertType.INFORMATION, 
                    mainApp.getPrimaryStage(),
                    "Success", 
                    "Reservation cancelled successfully"
                );
                
                // Refresh reservations list
                refreshReservations(contentPane);
            } else {
                DialogUtil.showAlert(
                    Alert.AlertType.ERROR, 
                    mainApp.getPrimaryStage(),
                    "Error", 
                    "Failed to cancel reservation. Please try again."
                );
            }
        }
    }
    
    // CRUD for Profile Management
    private Node createProfileContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        
        // Title
        Label titleLabel = new Label("My Profile");
        titleLabel.setFont(Font.font(UIConstants.FONT_SERIF_ELEGANT, FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.web(UIConstants.ACCENT_COLOR_DARK_BROWN_HEX));
        
        // Profile form
        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(15);
        formGrid.setPadding(new Insets(20));
        formGrid.setStyle("-fx-background-color: white; -fx-background-radius: 5px; " +
                UIConstants.SHADOW_EFFECT_CSS);
        
        int row = 0;
        
        // First Name
        Label firstNameLabel = new Label("First Name:");
        TextField firstNameField = new TextField(currentGuest.getFirstName());
        formGrid.add(firstNameLabel, 0, row);
        formGrid.add(firstNameField, 1, row++);
        
        // Last Name
        Label lastNameLabel = new Label("Last Name:");
        TextField lastNameField = new TextField(currentGuest.getLastName());
        formGrid.add(lastNameLabel, 0, row);
        formGrid.add(lastNameField, 1, row++);
        
        // Email
        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField(currentGuest.getEmail());
        formGrid.add(emailLabel, 0, row);
        formGrid.add(emailField, 1, row++);
        
        // Phone Number
        Label phoneLabel = new Label("Phone Number:");
        TextField phoneField = new TextField(currentGuest.getPhoneNumber());
        formGrid.add(phoneLabel, 0, row);
        formGrid.add(phoneField, 1, row++);
        
        // Address
        Label addressLabel = new Label("Address:");
        TextArea addressArea = new TextArea(currentGuest.getAddress());
        addressArea.setPrefRowCount(3);
        formGrid.add(addressLabel, 0, row);
        formGrid.add(addressArea, 1, row++);
        
        // Nationality
        Label nationalityLabel = new Label("Nationality:");
        TextField nationalityField = new TextField(currentGuest.getNationality());
        formGrid.add(nationalityLabel, 0, row);
        formGrid.add(nationalityField, 1, row++);
        
        // Date of Birth
        Label dobLabel = new Label("Date of Birth:");
        DatePicker dobPicker = new DatePicker(currentGuest.getDateOfBirth());
        formGrid.add(dobLabel, 0, row);
        formGrid.add(dobPicker, 1, row++);
        
        // ID Type
        Label idTypeLabel = new Label("ID Type:");
        TextField idTypeField = new TextField(currentGuest.getIdentificationType());
        formGrid.add(idTypeLabel, 0, row);
        formGrid.add(idTypeField, 1, row++);
        
        // ID Number
        Label idNumLabel = new Label("ID Number:");
        TextField idNumField = new TextField(currentGuest.getIdentificationNumber());
        formGrid.add(idNumLabel, 0, row);
        formGrid.add(idNumField, 1, row++);
        
        // Preferences
        Label preferencesLabel = new Label("Preferences:");
        TextArea preferencesArea = new TextArea(currentGuest.getPreferences());
        preferencesArea.setPrefRowCount(3);
        formGrid.add(preferencesLabel, 0, row);
        formGrid.add(preferencesArea, 1, row++);
        
        // Save button
        Button saveButton = new Button("Save Changes");
        saveButton.setStyle(UIConstants.LUXURY_BUTTON_STYLE);
        
        HBox buttonBox = new HBox(saveButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));
        
        saveButton.setOnAction(e -> {
            // Update guest object with form values
            currentGuest.setFirstName(firstNameField.getText());
            currentGuest.setLastName(lastNameField.getText());
            currentGuest.setEmail(emailField.getText());
            currentGuest.setPhoneNumber(phoneField.getText());
            currentGuest.setAddress(addressArea.getText());
            currentGuest.setNationality(nationalityField.getText());
            currentGuest.setDateOfBirth(dobPicker.getValue());
            currentGuest.setIdentificationType(idTypeField.getText());
            currentGuest.setIdentificationNumber(idNumField.getText());
            currentGuest.setPreferences(preferencesArea.getText());
            
            // Save to database
            boolean success = guestDAO.updateGuest(currentGuest);
            
            if (success) {
                DialogUtil.showAlert(
                    Alert.AlertType.INFORMATION, 
                    mainApp.getPrimaryStage(),
                    "Success", 
                    "Profile updated successfully"
                );
            } else {
                DialogUtil.showAlert(
                    Alert.AlertType.ERROR, 
                    mainApp.getPrimaryStage(),
                    "Error", 
                    "Failed to update profile. Please try again."
                );
            }
        });
        
        content.getChildren().addAll(titleLabel, formGrid, buttonBox);
        return content;
    }
    
    // CRUD for Room Booking
    private Node createBookingContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        
        // Title
        Label titleLabel = new Label("Book a Room");
        titleLabel.setFont(Font.font(UIConstants.FONT_SERIF_ELEGANT, FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.web(UIConstants.ACCENT_COLOR_DARK_BROWN_HEX));
        
        // Booking form
        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(15);
        formGrid.setPadding(new Insets(20));
        formGrid.setStyle("-fx-background-color: white; -fx-background-radius: 5px; " +
                UIConstants.SHADOW_EFFECT_CSS);
        
        int row = 0;
        
        // Room Type
        Label roomTypeLabel = new Label("Room Type:");
        ComboBox<RoomType> roomTypeCombo = new ComboBox<>();
        roomTypeCombo.setItems(roomTypeDAO.getAllRoomTypes());
        roomTypeCombo.setConverter(new StringConverter<RoomType>() {
            @Override
            public String toString(RoomType roomType) {
                if (roomType == null) return null;
                return roomType.getTypeName() + (roomType.getBaseRate() != null ? " - $" + roomType.getBaseRate() + "/night" : "");
            }
            
            @Override
            public RoomType fromString(String string) {
                return null; // Not needed for ComboBox
            }
        });
        roomTypeCombo.setPrefWidth(300);
        formGrid.add(roomTypeLabel, 0, row);
        formGrid.add(roomTypeCombo, 1, row++);
        
        // Check-in Date
        Label checkInLabel = new Label("Check-in Date:");
        DatePicker checkInPicker = new DatePicker(LocalDate.now().plusDays(1));
        checkInPicker.setPrefWidth(300);
        formGrid.add(checkInLabel, 0, row);
        formGrid.add(checkInPicker, 1, row++);
        
        // Check-out Date
        Label checkOutLabel = new Label("Check-out Date:");
        DatePicker checkOutPicker = new DatePicker(LocalDate.now().plusDays(3));
        checkOutPicker.setPrefWidth(300);
        formGrid.add(checkOutLabel, 0, row);
        formGrid.add(checkOutPicker, 1, row++);
        
        // Guests
        Label guestsLabel = new Label("Number of Guests:");
        HBox guestsBox = new HBox(10);
        
        Label adultsLabel = new Label("Adults:");
        Spinner<Integer> adultsSpinner = new Spinner<>(1, 10, 2);
        
        Label childrenLabel = new Label("Children:");
        Spinner<Integer> childrenSpinner = new Spinner<>(0, 10, 0);
        
        guestsBox.getChildren().addAll(adultsLabel, adultsSpinner, childrenLabel, childrenSpinner);
        formGrid.add(guestsLabel, 0, row);
        formGrid.add(guestsBox, 1, row++);
        
        // Special Requests
        Label specialReqLabel = new Label("Special Requests:");
        TextArea specialReqArea = new TextArea();
        specialReqArea.setPrefRowCount(3);
        formGrid.add(specialReqLabel, 0, row);
        formGrid.add(specialReqArea, 1, row++);
        
        // Search button
        Button searchButton = new Button("Search Available Rooms");
        searchButton.setStyle(UIConstants.LUXURY_BUTTON_STYLE);
        HBox searchBox = new HBox(searchButton);
        searchBox.setAlignment(Pos.CENTER_RIGHT);
        searchBox.setPadding(new Insets(20, 0, 0, 0));
        
        // Results area
        VBox resultsBox = new VBox(15);
        resultsBox.setPadding(new Insets(20, 0, 0, 0));
        resultsBox.setVisible(false);
        
        Label resultsLabel = new Label("Available Rooms");
        resultsLabel.setFont(Font.font(UIConstants.FONT_SERIF_ELEGANT, FontWeight.BOLD, 24));
        
        TableView<Room> roomsTable = new TableView<>();
        
        TableColumn<Room, String> roomNumCol = new TableColumn<>("Room");
        roomNumCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        roomNumCol.setPrefWidth(100);
        
        TableColumn<Room, String> roomTypeNameCol = new TableColumn<>("Type");
        roomTypeNameCol.setCellValueFactory(new PropertyValueFactory<>("roomTypeName"));
        roomTypeNameCol.setPrefWidth(150);
        
        TableColumn<Room, Integer> floorCol = new TableColumn<>("Floor");
        floorCol.setCellValueFactory(new PropertyValueFactory<>("floorNumber"));
        floorCol.setPrefWidth(80);
        
        TableColumn<Room, Integer> maxOccupancyCol = new TableColumn<>("Max Occupancy");
        maxOccupancyCol.setCellValueFactory(new PropertyValueFactory<>("maxOccupancy"));
        maxOccupancyCol.setPrefWidth(120);
        
        TableColumn<Room, String> featuresCol = new TableColumn<>("Features");
        featuresCol.setCellValueFactory(new PropertyValueFactory<>("features"));
        featuresCol.setPrefWidth(200);
        
        TableColumn<Room, Void> actionsCol = new TableColumn<>("Book");
        actionsCol.setPrefWidth(100);
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button bookBtn = new Button("Book Now");
            
            {
                bookBtn.setStyle(UIConstants.SMALL_BUTTON_STYLE);
                bookBtn.setOnAction(e -> {
                    Room room = getTableView().getItems().get(getIndex());
                    bookRoom(room, roomTypeCombo.getValue(), checkInPicker.getValue(), 
                            checkOutPicker.getValue(), adultsSpinner.getValue(), 
                            childrenSpinner.getValue(), specialReqArea.getText());
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : bookBtn);
            }
        });
        
        roomsTable.getColumns().addAll(roomNumCol, roomTypeNameCol, floorCol, maxOccupancyCol, featuresCol, actionsCol);
        roomsTable.setPlaceholder(new Label("No available rooms found for your search criteria."));
        
        resultsBox.getChildren().addAll(resultsLabel, roomsTable);
        VBox.setVgrow(roomsTable, Priority.ALWAYS);
        
        // Set up search action
        searchButton.setOnAction(e -> {
            // Validate inputs
            if (checkInPicker.getValue() == null || checkOutPicker.getValue() == null) {
                DialogUtil.showAlert(
                    Alert.AlertType.ERROR,
                    mainApp.getPrimaryStage(),
                    "Error",
                    "Please select both check-in and check-out dates."
                );
                return;
            }
            
            if (checkInPicker.getValue().isBefore(LocalDate.now())) {
                DialogUtil.showAlert(
                    Alert.AlertType.ERROR,
                    mainApp.getPrimaryStage(),
                    "Error",
                    "Check-in date must be today or a future date."
                );
                return;
            }
            
            if (checkOutPicker.getValue().isBefore(checkInPicker.getValue().plusDays(1))) {
                DialogUtil.showAlert(
                    Alert.AlertType.ERROR,
                    mainApp.getPrimaryStage(),
                    "Error",
                    "Check-out date must be at least one day after check-in date."
                );
                return;
            }
            
            // Get available rooms
            ObservableList<Room> availableRooms = FXCollections.observableArrayList();
            
            // If room type is selected, filter by that type
            Integer roomTypeId = null;
            if (roomTypeCombo.getValue() != null) {
                roomTypeId = roomTypeCombo.getValue().getRoomTypeID();
            }
            
            // Get all rooms or rooms by type
            ObservableList<Room> allRooms;
            if (roomTypeId != null) {
                allRooms = roomDAO.getRoomsByType(roomTypeId);
            } else {
                allRooms = roomDAO.getAllRooms();
            }
            
            // Filter to only available rooms
            for (Room room : allRooms) {
                if (room.getStatus().equalsIgnoreCase("Available") && 
                    roomDAO.isRoomAvailable(room.getRoomID(), checkInPicker.getValue(), checkOutPicker.getValue()) &&
                    (room.getMaxOccupancy() == null || room.getMaxOccupancy() >= adultsSpinner.getValue())) {
                    availableRooms.add(room);
                }
            }
            
            // Update table
            roomsTable.setItems(availableRooms);
            resultsBox.setVisible(true);
        });
        
        content.getChildren().addAll(titleLabel, formGrid, searchBox, resultsBox);
        return content;
    }
    
    private void bookRoom(Room room, RoomType roomType, LocalDate checkInDate, LocalDate checkOutDate, 
                         int adults, int children, String specialRequests) {
        // Create new reservation
        Reservation reservation = new Reservation();
        reservation.setGuestID(currentGuest.getGuestID());
        reservation.setRoomID(room.getRoomID());
        reservation.setRoomTypeID(room.getRoomTypeID());
        reservation.setCheckInDate(checkInDate);
        reservation.setCheckOutDate(checkOutDate);
        reservation.setNumberOfAdults(adults);
        reservation.setNumberOfChildren(children);
        reservation.setStatus("Confirmed");
        reservation.setBookingSource("Guest Portal");
        reservation.setSpecialRequests(specialRequests);
        reservation.setDateBooked(LocalDate.now());
        
        // Calculate estimated cost
        if (roomType != null && roomType.getBaseRate() != null) {
            long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
            BigDecimal totalCost = roomType.getBaseRate().multiply(new BigDecimal(nights));
            reservation.setTotalEstimatedCost(totalCost);
        }
        
        // Save reservation
        boolean success = reservationDAO.addReservation(reservation);
        
        if (success) {
            DialogUtil.showAlert(
                Alert.AlertType.INFORMATION,
                mainApp.getPrimaryStage(),
                "Success",
                "Room booked successfully! You can view your reservation in 'My Reservations'."
            );
            
            // Switch to reservations view and refresh
            root.setCenter(reservationsContent);
            refreshReservations((VBox)reservationsContent);
            
            // Reset active button
            for (Node node : ((VBox)root.getLeft()).getChildren()) {
                if (node instanceof Button && ((Button)node).getText().equals("My Reservations")) {
                    String normalButtonStyle = "-fx-background-color: transparent; -fx-text-fill: white; " +
                        "-fx-font-family: '" + UIConstants.FONT_SANS_SERIF_CLEAN + "'; " +
                        "-fx-font-size: 16px; -fx-padding: 15 10; -fx-alignment: CENTER_LEFT;";
                    
                    String activeButtonStyle = "-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; " +
                        "-fx-font-family: '" + UIConstants.FONT_SANS_SERIF_CLEAN + "'; " +
                        "-fx-font-size: 16px; -fx-padding: 15 10; -fx-alignment: CENTER_LEFT; " +
                        "-fx-font-weight: bold;";
                    
                    setActiveButton((Button)node, normalButtonStyle, activeButtonStyle);
                    break;
                }
            }
        } else {
            DialogUtil.showAlert(
                Alert.AlertType.ERROR,
                mainApp.getPrimaryStage(),
                "Error",
                "Failed to book room. Please try again."
            );
        }
    }
} 