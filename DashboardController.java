package com.example.demo10.raffles.hotelmgmt.controller;

import com.example.demo10.raffles.hotelmgmt.DatabaseConnector;
import com.example.demo10.raffles.hotelmgmt.MainApp;
import com.example.demo10.raffles.hotelmgmt.dao.GuestDAO;
import com.example.demo10.raffles.hotelmgmt.dao.ReservationDAO;
import com.example.demo10.raffles.hotelmgmt.dao.RoomDAO;
import com.example.demo10.raffles.hotelmgmt.dao.RoomTypeDAO;
import com.example.demo10.raffles.hotelmgmt.dao.EmployeeDAO;
import com.example.demo10.raffles.hotelmgmt.dao.RoleDAO;
import com.example.demo10.raffles.hotelmgmt.dao.DepartmentDAO;
import com.example.demo10.raffles.hotelmgmt.model.Employee;
import com.example.demo10.raffles.hotelmgmt.model.Guest;
import com.example.demo10.raffles.hotelmgmt.model.Reservation;
import com.example.demo10.raffles.hotelmgmt.model.Room;
import com.example.demo10.raffles.hotelmgmt.model.RoomType;
import com.example.demo10.raffles.hotelmgmt.ui.DialogUtil;
import com.example.demo10.raffles.hotelmgmt.ui.UIConstants;
import com.example.demo10.raffles.hotelmgmt.controller.RoomManagementController;
import com.example.demo10.raffles.hotelmgmt.controller.InvoiceManagementController;
import com.example.demo10.raffles.hotelmgmt.controller.EmployeeManagementController;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.math.BigDecimal;
import javafx.beans.value.ChangeListener;
import javafx.beans.property.SimpleStringProperty;

public class DashboardController {
    private final MainApp mainApp;
    private final Employee currentUser;
    private final GuestDAO guestDAO;
    private ObservableList<Guest> guestList;
    private String currentSection = "dashboard";

    public DashboardController(MainApp mainApp, Employee currentUser) {
        this.mainApp = mainApp;
        this.currentUser = currentUser;
        this.guestDAO = new GuestDAO();
        System.out.println("DashboardController created for user: " + currentUser.getUsername());
    }

    public Scene createDashboardScene() {
        System.out.println("Creating dashboard scene for: " + currentUser.getFirstName() + " " + currentUser.getLastName());
        
        BorderPane dashboardLayout = new BorderPane();
        dashboardLayout.setStyle("-fx-background-color: white;");
        
        // Create sidebar with navigation
        VBox sidebar = createSidebar();
        dashboardLayout.setLeft(sidebar);
        
        // Create header with welcome message and logout button
        HBox header = createHeader();
        dashboardLayout.setTop(header);
        
        // Main content area - initially dashboard
        dashboardLayout.setCenter(createDashboardContent());
        
        // Add Debug Reset Button (remove for production)
        addDebugResetOption(dashboardLayout);
        
        // Create scene with explicit dimensions
        Scene scene = new Scene(dashboardLayout, UIConstants.INITIAL_WIDTH, UIConstants.INITIAL_HEIGHT);
        System.out.println("Dashboard scene created with dimensions: " + UIConstants.INITIAL_WIDTH + "x" + UIConstants.INITIAL_HEIGHT);
        
        return scene;
    }
    
    private VBox createSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPrefWidth(240);
        sidebar.setStyle("-fx-background-color: " + UIConstants.ACCENT_COLOR_DARK_BROWN_HEX + ";");
        sidebar.setPadding(new Insets(20));
        
        // Logo at the top
        Text logoText = new Text("RAFFLES");
        logoText.setFont(Font.font(UIConstants.FONT_FAMILY_TITLE_WELCOME, FontWeight.BOLD, 32));
        logoText.setFill(Color.WHITE);
        
        Text sublogoText = new Text("HOTEL MANAGEMENT");
        sublogoText.setFont(Font.font(UIConstants.FONT_SANS_SERIF_CLEAN, FontWeight.NORMAL, 12));
        sublogoText.setFill(Color.LIGHTGRAY);
        
        VBox logoBox = new VBox(5, logoText, sublogoText);
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(0, 0, 25, 0));
        
        // Navigation buttons
        Button dashboardBtn = createNavButton("Dashboard", "dashboard");
        
        // Guest Management
        Button guestsBtn = createNavButton("Guests", "guests");
        
        // Room Management
        Button roomsBtn = createNavButton("Rooms", "rooms");
        
        // Reservation Management
        Button reservationsBtn = createNavButton("Reservations", "reservations");
        
        // Invoice Management
        Button invoicesBtn = createNavButton("Invoices", "invoices");
        
        // Add all database table access buttons for admin
        Button employeesBtn = createNavButton("Employees", "employees");
        Button maintenanceBtn = createNavButton("Maintenance", "maintenance");
        Button housekeepingBtn = createNavButton("Housekeeping", "housekeeping");
        Button departmentsBtn = createNavButton("Departments", "departments");
        Button rolesBtn = createNavButton("Roles", "roles");
        Button settingsBtn = createNavButton("Settings", "settings");
        
        // Add section title for admin options
        Text adminSectionText = new Text("ADMIN CONTROLS");
        adminSectionText.setFont(Font.font(UIConstants.FONT_SANS_SERIF_CLEAN, FontWeight.BOLD, 12));
        adminSectionText.setFill(Color.LIGHTGRAY);
        adminSectionText.setOpacity(0.7);
        
        VBox adminSection = new VBox(2, adminSectionText);
        adminSection.setPadding(new Insets(15, 0, 5, 0));
        
        // Add logos and main menu buttons to sidebar
        sidebar.getChildren().addAll(
                logoBox, 
                dashboardBtn,
                guestsBtn,
                roomsBtn,
                reservationsBtn,
                invoicesBtn,
                employeesBtn  // Add Employees button for all users
        );
        
        // If the user is admin, add the admin section
        if (currentUser.getRoleName() != null && (
                currentUser.getRoleName().equalsIgnoreCase("Admin") ||
                currentUser.getRoleName().equalsIgnoreCase("Administrator") ||
                currentUser.getRoleName().equalsIgnoreCase("Manager"))) {
            
            sidebar.getChildren().addAll(
                    adminSection,
                    departmentsBtn,
                    rolesBtn,
                    maintenanceBtn,
                    housekeepingBtn,
                    settingsBtn
            );
        }
        
        return sidebar;
    }
    
    private Button createNavButton(String text, String sectionId) {
        Button btn = new Button(text);
        btn.setPrefWidth(200);
        btn.setPrefHeight(45);
        
        // More luxurious button styling with gold accents and better transitions
        String activeStyle = "-fx-background-color: linear-gradient(to right, " + UIConstants.BRAND_COLOR_HEX + ", " + 
                           UIConstants.GOLD_COLOR_HEX + "); " +
                           "-fx-text-fill: white; " +
                           "-fx-font-family: '" + UIConstants.FONT_SANS_SERIF_CLEAN + "'; " +
                           "-fx-font-size: 14px; " +
                           "-fx-background-radius: 8px; " +
                           "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 4, 0, 1, 2);";
                           
        String inactiveStyle = "-fx-background-color: transparent; " +
                             "-fx-text-fill: white; " +
                             "-fx-font-family: '" + UIConstants.FONT_SANS_SERIF_CLEAN + "'; " +
                             "-fx-font-size: 14px; " +
                             "-fx-background-radius: 8px;";
        
        String hoverStyle = "-fx-background-color: rgba(255,255,255,0.2); " +
                          "-fx-text-fill: white; " +
                          "-fx-cursor: hand;";
        
        // Set initial style
        btn.setStyle(sectionId.equals(currentSection) ? activeStyle : inactiveStyle);
        
        // Add hover effects for inactive buttons
        btn.setOnMouseEntered(e -> {
            if (!sectionId.equals(currentSection)) {
                btn.setStyle(inactiveStyle + hoverStyle);
            }
        });
        
        btn.setOnMouseExited(e -> {
            if (!sectionId.equals(currentSection)) {
                btn.setStyle(inactiveStyle);
            }
        });
        
        // Set action
        btn.setOnAction(e -> {
            // Update all button styles in the sidebar
            VBox sidebar = (VBox) btn.getParent();
            for (Node node : sidebar.getChildren()) {
                if (node instanceof Button) {
                    Button b = (Button) node;
                    b.setStyle(b == btn ? activeStyle : inactiveStyle);
                }
            }
            
            // Use our updateDashboardCenter method to handle content switching
            updateDashboardCenter(sectionId);
        });
        
        return btn;
    }
    
    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setPadding(new Insets(15, 30, 15, 30));
        header.setStyle("-fx-background-color: white; -fx-border-width: 0 0 1 0; -fx-border-color: #ddd;");
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label welcomeLabel = new Label("Welcome, " + currentUser.getFirstName() + "!");
        welcomeLabel.setFont(Font.font(UIConstants.FONT_SERIF_ELEGANT, FontWeight.BOLD, 22));
        welcomeLabel.setTextFill(Color.web(UIConstants.ACCENT_COLOR_DARK_BROWN_HEX));
        
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label roleLabel = new Label(currentUser.getRoleName() != null ? currentUser.getRoleName() : "Staff");
        roleLabel.setFont(Font.font(UIConstants.FONT_SANS_SERIF_CLEAN, 14));
        roleLabel.setTextFill(Color.web(UIConstants.BRAND_COLOR_HEX));
        roleLabel.setPadding(new Insets(0, 20, 0, 0));
        
        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: " + UIConstants.ACCENT_COLOR_DARK_BROWN_HEX + "; -fx-text-fill: white;");
        logoutBtn.setOnAction(e -> {
            System.out.println("Logout button clicked - shutting down connections");
            // Ensure database connections are closed properly
            DatabaseConnector.closeConnection();
            mainApp.logout();
        });
        
        header.getChildren().addAll(welcomeLabel, spacer, roleLabel, logoutBtn);
        return header;
    }
    
    private VBox createDashboardContent() {
        VBox content = new VBox(30);
        content.setPadding(new Insets(30));
        content.setAlignment(Pos.TOP_LEFT);
        
        // Title
        Label titleLabel = new Label("Hotel Dashboard");
        titleLabel.setFont(Font.font(UIConstants.FONT_SERIF_ELEGANT, FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.web(UIConstants.ACCENT_COLOR_DARK_BROWN_HEX));
        titleLabel.setPadding(new Insets(0, 0, 10, 0));
        
        // Stats cards - first row
        HBox statsCards = new HBox(20);
        statsCards.setAlignment(Pos.CENTER);
        
        // Get real counts from database
        long guestCount = guestDAO.getGuestCount();
        
        // Create cards similar to the screenshot
        VBox occupancyCard = createOccupancyCard("0%");
        VBox inHouseGuestsCard = createStatsCard("Guests In-House", "0");
        VBox arrivalsCard = createStatsCard("Today's Arrivals", "0");
        VBox departuresCard = createStatsCard("Today's Departures", "0");
        
        statsCards.getChildren().addAll(occupancyCard, inHouseGuestsCard, arrivalsCard, departuresCard);
        
        // Second row with two panels
        HBox secondRow = new HBox(20);
        secondRow.setPadding(new Insets(10, 0, 0, 0));
        
        // Occupancy by room type chart panel
        VBox occupancyByTypePanel = new VBox(10);
        occupancyByTypePanel.setStyle("-fx-background-color: white; -fx-background-radius: 10px; " + UIConstants.SHADOW_EFFECT_CSS);
        occupancyByTypePanel.setPadding(new Insets(20));
        
        Label occupancyByTypeTitle = new Label("Occupancy by Room Type");
        occupancyByTypeTitle.setFont(Font.font(UIConstants.FONT_SERIF_ELEGANT, FontWeight.BOLD, 18));
        
        // Placeholder for chart
        Pane chartPlaceholder = new Pane();
        chartPlaceholder.setPrefHeight(200);
        chartPlaceholder.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #eeeeee; -fx-border-radius: 5px;");
        
        Label chartLabel = new Label("Occupancy Rate Chart");
        chartLabel.setLayoutX(20);
        chartLabel.setLayoutY(20);
        
        chartPlaceholder.getChildren().add(chartLabel);
        occupancyByTypePanel.getChildren().addAll(occupancyByTypeTitle, chartPlaceholder);
        VBox.setVgrow(chartPlaceholder, Priority.ALWAYS);
        
        // Room status overview panel
        VBox roomStatusPanel = new VBox(10);
        roomStatusPanel.setStyle("-fx-background-color: white; -fx-background-radius: 10px; " + UIConstants.SHADOW_EFFECT_CSS);
        roomStatusPanel.setPadding(new Insets(20));
        
        Label roomStatusTitle = new Label("Room Status Overview");
        roomStatusTitle.setFont(Font.font(UIConstants.FONT_SERIF_ELEGANT, FontWeight.BOLD, 18));
        
        GridPane roomStatusGrid = new GridPane();
        roomStatusGrid.setHgap(20);
        roomStatusGrid.setVgap(15);
        
        // Room status categories
        createRoomStatusCategory(roomStatusGrid, 0, "Available", "38", "#4CAF50");
        createRoomStatusCategory(roomStatusGrid, 1, "Occupied", "0", "#2196F3");
        createRoomStatusCategory(roomStatusGrid, 2, "Reserved", "4", "#FF9800");
        createRoomStatusCategory(roomStatusGrid, 3, "Maintenance", "0", "#F44336");
        
        roomStatusPanel.getChildren().addAll(roomStatusTitle, roomStatusGrid);
        VBox.setVgrow(roomStatusGrid, Priority.ALWAYS);
        
        // Add panels to second row
        HBox.setHgrow(occupancyByTypePanel, Priority.ALWAYS);
        HBox.setHgrow(roomStatusPanel, Priority.ALWAYS);
        secondRow.getChildren().addAll(occupancyByTypePanel, roomStatusPanel);
        
        // Third row - Recent Activity and Revenue Sources
        HBox thirdRow = new HBox(20);
        thirdRow.setPadding(new Insets(10, 0, 0, 0));
        
        // Recent Activity Panel
        VBox recentActivityPanel = new VBox(10);
        recentActivityPanel.setStyle("-fx-background-color: white; -fx-background-radius: 10px; " + UIConstants.SHADOW_EFFECT_CSS);
        recentActivityPanel.setPadding(new Insets(20));
        
        Label recentActivityTitle = new Label("Recent Activity");
        recentActivityTitle.setFont(Font.font(UIConstants.FONT_SERIF_ELEGANT, FontWeight.BOLD, 18));
        
        VBox activityList = new VBox(10);
        activityList.getChildren().addAll(
            createActivityItem("21:23:09", "Application data loaded."),
            createActivityItem("21:23:09", "User " + currentUser.getUsername() + " logged in."),
            createActivityItem("21:22:52", "System initialized. Welcome!")
        );
        
        recentActivityPanel.getChildren().addAll(recentActivityTitle, activityList);
        
        // Revenue Sources Panel
        VBox revenuePanel = new VBox(10);
        revenuePanel.setStyle("-fx-background-color: white; -fx-background-radius: 10px; " + UIConstants.SHADOW_EFFECT_CSS);
        revenuePanel.setPadding(new Insets(20));
        
        Label revenueTitle = new Label("Revenue Sources");
        revenueTitle.setFont(Font.font(UIConstants.FONT_SERIF_ELEGANT, FontWeight.BOLD, 18));
        
        // Placeholder for revenue chart
        Pane revenuePlaceholder = new Pane();
        revenuePlaceholder.setPrefHeight(150);
        revenuePlaceholder.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #eeeeee; -fx-border-radius: 5px;");
        
        Label revenueChartLabel = new Label("Revenue Distribution");
        revenueChartLabel.setLayoutX(20);
        revenueChartLabel.setLayoutY(20);
        
        revenuePlaceholder.getChildren().add(revenueChartLabel);
        revenuePanel.getChildren().addAll(revenueTitle, revenuePlaceholder);
        
        // Add panels to third row
        HBox.setHgrow(recentActivityPanel, Priority.ALWAYS);
        HBox.setHgrow(revenuePanel, Priority.ALWAYS);
        thirdRow.getChildren().addAll(recentActivityPanel, revenuePanel);
        
        // Add all components to the dashboard
        content.getChildren().addAll(titleLabel, statsCards, secondRow, thirdRow);
        return content;
    }
    
    private VBox createOccupancyCard(String percentage) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setPrefWidth(200);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10px; " + UIConstants.SHADOW_EFFECT_CSS);
        
        Label iconLabel = new Label("🏠"); // Unicode icon for occupancy
        iconLabel.setFont(Font.font(UIConstants.FONT_SANS_SERIF_CLEAN, 14));
        iconLabel.setTextFill(Color.web("#666666"));
        
        Label titleLabel = new Label("Occupancy");
        titleLabel.setFont(Font.font(UIConstants.FONT_SANS_SERIF_CLEAN, 14));
        titleLabel.setTextFill(Color.web("#666666"));
        
        Label valueLabel = new Label(percentage);
        valueLabel.setFont(Font.font(UIConstants.FONT_SANS_SERIF_CLEAN, FontWeight.BOLD, 32));
        valueLabel.setTextFill(Color.web(UIConstants.ACCENT_COLOR_DARK_BROWN_HEX));
        
        card.getChildren().addAll(iconLabel, titleLabel, valueLabel);
        return card;
    }
    
    private VBox createStatsCard(String title, String value) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setPrefWidth(200);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10px; " + UIConstants.SHADOW_EFFECT_CSS);
        
        // Add icon based on card type
        Label iconLabel = new Label();
        if (title.contains("In-House")) {
            iconLabel.setText("👥"); // People icon
        } else if (title.contains("Arrivals")) {
            iconLabel.setText("📥"); // Arrivals icon
        } else if (title.contains("Departures")) {
            iconLabel.setText("📤"); // Departures icon
        } else {
            iconLabel.setText("📊"); // Default stats icon
        }
        iconLabel.setFont(Font.font(UIConstants.FONT_SANS_SERIF_CLEAN, 14));
        iconLabel.setTextFill(Color.web("#666666"));
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font(UIConstants.FONT_SANS_SERIF_CLEAN, 14));
        titleLabel.setTextFill(Color.web("#666666"));
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font(UIConstants.FONT_SANS_SERIF_CLEAN, FontWeight.BOLD, 32));
        valueLabel.setTextFill(Color.web(UIConstants.ACCENT_COLOR_DARK_BROWN_HEX));
        
        card.getChildren().addAll(iconLabel, titleLabel, valueLabel);
        return card;
    }
    
    private void createRoomStatusCategory(GridPane grid, int row, String status, String count, String colorHex) {
        Rectangle colorIndicator = new Rectangle(15, 15);
        colorIndicator.setFill(Color.web(colorHex));
        colorIndicator.setArcWidth(5);
        colorIndicator.setArcHeight(5);
        
        Label statusLabel = new Label(status);
        statusLabel.setFont(Font.font(UIConstants.FONT_SANS_SERIF_CLEAN, 14));
        
        Label countLabel = new Label(count);
        countLabel.setFont(Font.font(UIConstants.FONT_SANS_SERIF_CLEAN, FontWeight.BOLD, 14));
        
        grid.add(colorIndicator, 0, row);
        grid.add(statusLabel, 1, row);
        grid.add(countLabel, 2, row);
    }
    
    private HBox createActivityItem(String time, String description) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        
        Label timeLabel = new Label(time);
        timeLabel.setFont(Font.font(UIConstants.FONT_SANS_SERIF_CLEAN, FontWeight.BOLD, 12));
        timeLabel.setTextFill(Color.web("#666666"));
        timeLabel.setPrefWidth(80);
        
        Label dashLabel = new Label("-");
        
        Label descLabel = new Label(description);
        descLabel.setFont(Font.font(UIConstants.FONT_SANS_SERIF_CLEAN, 12));
        
        item.getChildren().addAll(timeLabel, dashLabel, descLabel);
        return item;
    }
    
    private VBox createGuestsContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        
        // Title
        Label titleLabel = new Label("Guest Management");
        titleLabel.setFont(Font.font(UIConstants.FONT_SERIF_ELEGANT, FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.web(UIConstants.ACCENT_COLOR_DARK_BROWN_HEX));
        
        // Action bar
        HBox actionBar = new HBox(10);
        actionBar.setPadding(new Insets(10, 0, 20, 0));
        
        Button addButton = new Button("Add Guest");
        addButton.setStyle("-fx-background-color: " + UIConstants.BRAND_COLOR_HEX + "; -fx-text-fill: white;");
        addButton.setOnAction(e -> showGuestDialog(null));
        
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> refreshGuestList());
        
        TextField searchField = new TextField();
        searchField.setPromptText("Search guests...");
        searchField.setPrefWidth(250);
        
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        actionBar.getChildren().addAll(addButton, refreshButton, spacer, searchField);
        
        // Guest table
        TableView<Guest> guestTable = new TableView<>();
        guestTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<Guest, String> firstNameCol = new TableColumn<>("First Name");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        
        TableColumn<Guest, String> lastNameCol = new TableColumn<>("Last Name");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        
        TableColumn<Guest, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        TableColumn<Guest, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        
        TableColumn<Guest, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(5, editBtn, deleteBtn);
            
            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                
                editBtn.setOnAction(event -> {
                    Guest guest = getTableView().getItems().get(getIndex());
                    showGuestDialog(guest);
                });
                
                deleteBtn.setOnAction(event -> {
                    Guest guest = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Delete Guest");
                    alert.setHeaderText("Delete " + guest.getFirstName() + " " + guest.getLastName() + "?");
                    alert.setContentText("Are you sure you want to delete this guest? This cannot be undone.");
                    
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        if (guestDAO.deleteGuest(guest.getGuestID())) {
                            refreshGuestList();
                        }
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        
        guestTable.getColumns().addAll(firstNameCol, lastNameCol, emailCol, phoneCol, actionsCol);
        
        // Initialize guest list as observable
        guestList = FXCollections.observableArrayList();
        refreshGuestList();
        guestTable.setItems(guestList);
        
        VBox.setVgrow(guestTable, Priority.ALWAYS);
        content.getChildren().addAll(titleLabel, actionBar, guestTable);
        
        return content;
    }
    
    private VBox createPlaceholderContent(String title) {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font(UIConstants.FONT_SERIF_ELEGANT, FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.web(UIConstants.ACCENT_COLOR_DARK_BROWN_HEX));
        
        Label placeholderLabel = new Label("This section is under development.");
        placeholderLabel.setFont(Font.font(UIConstants.FONT_SANS_SERIF_CLEAN, 16));
        
        content.getChildren().addAll(titleLabel, placeholderLabel);
        return content;
    }
    
    private Node createReservationsContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        
        // Title
        Label titleLabel = new Label("Reservations Management");
        titleLabel.setFont(Font.font(UIConstants.FONT_SERIF_ELEGANT, FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.web(UIConstants.ACCENT_COLOR_DARK_BROWN_HEX));
        
        // Search and filter section
        HBox filterBar = new HBox(15);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(0, 0, 15, 0));
        
        DatePicker fromDatePicker = new DatePicker(LocalDate.now());
        fromDatePicker.setPromptText("From Date");
        fromDatePicker.setPrefWidth(150);
        
        DatePicker toDatePicker = new DatePicker(LocalDate.now().plusDays(30));
        toDatePicker.setPromptText("To Date");
        toDatePicker.setPrefWidth(150);
        
        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All Statuses", "Confirmed", "Pending", "Checked In", "Completed", "Cancelled");
        statusFilter.setValue("All Statuses");
        statusFilter.setPrefWidth(150);
        
        Button searchButton = new Button("Search");
        searchButton.setStyle("-fx-background-color: " + UIConstants.BRAND_COLOR_HEX + "; -fx-text-fill: white;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button addReservationBtn = new Button("New Reservation");
        addReservationBtn.setStyle("-fx-background-color: " + UIConstants.ACCENT_COLOR_DARK_BROWN_HEX + "; -fx-text-fill: white;");
        addReservationBtn.setOnAction(e -> showNewReservationDialog());
        
        filterBar.getChildren().addAll(
                new Label("From:"), fromDatePicker, 
                new Label("To:"), toDatePicker,
                new Label("Status:"), statusFilter,
                searchButton, spacer, addReservationBtn);
        
        // Reservations table
        TableView<Reservation> reservationTable = new TableView<>();
        reservationTable.setPlaceholder(new Label("No reservations found"));
        
        TableColumn<Reservation, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("reservationID"));
        idCol.setPrefWidth(60);
        
        TableColumn<Reservation, String> guestCol = new TableColumn<>("Guest");
        guestCol.setCellValueFactory(new PropertyValueFactory<>("guestName"));
        guestCol.setPrefWidth(180);
        
        TableColumn<Reservation, LocalDate> checkInCol = new TableColumn<>("Check-In");
        checkInCol.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
        checkInCol.setPrefWidth(120);
        checkInCol.setCellFactory(col -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
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
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });
        
        TableColumn<Reservation, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(cellData -> {
            String roomNumber = cellData.getValue().getRoomNumber();
            String roomType = cellData.getValue().getRoomTypeName();
            String roomInfo = "";
            
            if (roomNumber != null && !roomNumber.isEmpty()) {
                roomInfo += roomNumber;
                if (roomType != null && !roomType.isEmpty()) {
                    roomInfo += " - " + roomType;
                }
            } else if (roomType != null && !roomType.isEmpty()) {
                roomInfo = roomType;
            } else {
                roomInfo = "Not Assigned";
            }
            
            return new SimpleStringProperty(roomInfo);
        });
        roomCol.setPrefWidth(150);
        
        TableColumn<Reservation, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status) {
                        case "Confirmed":
                            setStyle("-fx-text-fill: " + UIConstants.SUCCESS_COLOR_HEX + ";");
                            break;
                        case "Pending":
                            setStyle("-fx-text-fill: #FFA000;");
                            break;
                        case "Cancelled":
                            setStyle("-fx-text-fill: " + UIConstants.ERROR_COLOR_HEX + ";");
                            break;
                        case "Checked In":
                            setStyle("-fx-text-fill: #2196F3;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });
        
        TableColumn<Reservation, BigDecimal> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalEstimatedCost"));
        totalCol.setPrefWidth(100);
        totalCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", price));
                }
            }
        });
        
        TableColumn<Reservation, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(200);
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button checkInBtn = new Button("Check In");
            private final Button cancelBtn = new Button("Cancel");
            
            {
                viewBtn.setStyle("-fx-background-color: " + UIConstants.BRAND_COLOR_HEX + "; -fx-text-fill: white;");
                checkInBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                cancelBtn.setStyle("-fx-background-color: " + UIConstants.ERROR_COLOR_HEX + "; -fx-text-fill: white;");
                
                viewBtn.setOnAction(event -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    showReservationDetailsNew(reservation);
                });
                
                checkInBtn.setOnAction(event -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    // Update the reservation status in the database
                    ReservationDAO reservationDAO = new ReservationDAO();
                    boolean success = reservationDAO.updateReservationStatus(reservation.getReservationID(), "Checked In");
                    
                    if (success) {
                        DialogUtil.showAlert(Alert.AlertType.INFORMATION, mainApp.getPrimaryStage(), 
                            "Check-In", "Guest " + reservation.getGuestName() + " checked in successfully.");
                        reservation.setStatus("Checked In");
                        getTableView().refresh();
                    } else {
                        DialogUtil.showAlert(Alert.AlertType.ERROR, mainApp.getPrimaryStage(), 
                            "Check-In Failed", "Failed to check in guest. Please try again.");
                    }
                });
                
                cancelBtn.setOnAction(event -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    Optional<ButtonType> result = DialogUtil.showConfirmationDialog(mainApp.getPrimaryStage(),
                        "Cancel Reservation", "Are you sure you want to cancel this reservation?",
                        "This action cannot be undone.");
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        // Update status in the database
                        ReservationDAO reservationDAO = new ReservationDAO();
                        boolean success = reservationDAO.updateReservationStatus(reservation.getReservationID(), "Cancelled");
                        
                        if (success) {
                            reservation.setStatus("Cancelled");
                            getTableView().refresh();
                        } else {
                            DialogUtil.showAlert(Alert.AlertType.ERROR, mainApp.getPrimaryStage(), 
                                "Cancellation Failed", "Failed to cancel reservation. Please try again.");
                        }
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    HBox buttons = new HBox(5);
                    buttons.getChildren().add(viewBtn);
                    
                    if ("Confirmed".equals(reservation.getStatus())) {
                        buttons.getChildren().add(checkInBtn);
                    }
                    
                    if (!reservation.getStatus().equals("Cancelled") && !reservation.getStatus().equals("Completed")) {
                        buttons.getChildren().add(cancelBtn);
                    }
                    
                    setGraphic(buttons);
                }
            }
        });
        
        reservationTable.getColumns().addAll(idCol, guestCol, checkInCol, checkOutCol, roomCol, statusCol, totalCol, actionsCol);
        VBox.setVgrow(reservationTable, Priority.ALWAYS);
        
        // Get actual data from database
        ReservationDAO reservationDAO = new ReservationDAO();
        ObservableList<Reservation> reservations = reservationDAO.getAllReservationsWithDetails();
        reservationTable.setItems(reservations);
        
        // Set up search button action
        searchButton.setOnAction(e -> {
            LocalDate fromDate = fromDatePicker.getValue();
            LocalDate toDate = toDatePicker.getValue();
            String status = statusFilter.getValue();
            
            // Validate dates
            if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
                DialogUtil.showAlert(Alert.AlertType.ERROR, mainApp.getPrimaryStage(),
                    "Invalid Date Range", "From date cannot be after To date.");
                return;
            }
            
            // Perform search
            ObservableList<Reservation> searchResults = reservationDAO.searchReservations(fromDate, toDate, status);
            reservationTable.setItems(searchResults);
        });
        
        // Add everything to the content pane
        content.getChildren().addAll(titleLabel, filterBar, reservationTable);
        return content;
    }
    
    private void showReservationDetailsNew(Reservation reservation) {
        Stage detailStage = new Stage();
        detailStage.initOwner(mainApp.getPrimaryStage());
        detailStage.setTitle("Reservation Details");
        
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setStyle("-fx-background-color: " + UIConstants.ACCENT_COLOR_LIGHT_CREAM_HEX + ";");
        
        // First column - Reservation details
        Label headerLabel = new Label("Reservation #" + reservation.getReservationID());
        headerLabel.setFont(Font.font(UIConstants.FONT_SERIF_ELEGANT, FontWeight.BOLD, 24));
        headerLabel.setTextFill(Color.web(UIConstants.ACCENT_COLOR_DARK_BROWN_HEX));
        GridPane.setColumnSpan(headerLabel, 4);
        grid.add(headerLabel, 0, 0);
        
        grid.add(createDetailLabel("Guest:"), 0, 1);
        grid.add(createDetailValue(reservation.getGuestName() != null ? reservation.getGuestName() : "Not assigned"), 1, 1);
        
        grid.add(createDetailLabel("Status:"), 0, 2);
        Label statusValue = createDetailValue(reservation.getStatus());
        switch (reservation.getStatus()) {
            case "Confirmed":
                statusValue.setTextFill(Color.web(UIConstants.SUCCESS_COLOR_HEX));
                break;
            case "Pending":
                statusValue.setTextFill(Color.web("#FFA000"));
                break;
            case "Cancelled":
                statusValue.setTextFill(Color.web(UIConstants.ERROR_COLOR_HEX));
                break;
            case "Checked In":
                statusValue.setTextFill(Color.web("#2196F3"));
                break;
        }
        grid.add(statusValue, 1, 2);
        
        grid.add(createDetailLabel("Check-in:"), 0, 3);
        grid.add(createDetailValue(reservation.getCheckInDate().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))), 1, 3);
        
        grid.add(createDetailLabel("Check-out:"), 0, 4);
        grid.add(createDetailValue(reservation.getCheckOutDate().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))), 1, 4);
        
        int nights = java.time.Period.between(reservation.getCheckInDate(), reservation.getCheckOutDate()).getDays();
        grid.add(createDetailLabel("Duration:"), 0, 5);
        grid.add(createDetailValue(nights + (nights == 1 ? " night" : " nights")), 1, 5);
        
        grid.add(createDetailLabel("Room:"), 0, 6);
        String roomInfo = reservation.getRoomNumber() != null ? 
            reservation.getRoomNumber() + (reservation.getRoomTypeName() != null ? " - " + reservation.getRoomTypeName() : "") : 
            (reservation.getRoomTypeName() != null ? reservation.getRoomTypeName() : "Not Assigned");
        grid.add(createDetailValue(roomInfo), 1, 6);
        
        BigDecimal totalCost = reservation.getTotalEstimatedCost();
        BigDecimal nightly = nights > 0 && totalCost != null ? 
            totalCost.divide(BigDecimal.valueOf(nights), 2, BigDecimal.ROUND_HALF_UP) : null;
            
        if (nightly != null) {
            grid.add(createDetailLabel("Rate:"), 0, 7);
            grid.add(createDetailValue(String.format("$%.2f per night", nightly)), 1, 7);
        }
        
        grid.add(createDetailLabel("Total:"), 0, 8);
        Label totalLabel = createDetailValue(totalCost != null ? String.format("$%.2f", totalCost) : "N/A");
        totalLabel.setFont(Font.font(UIConstants.FONT_SERIF_ELEGANT, FontWeight.BOLD, 16));
        grid.add(totalLabel, 1, 8);
        
        if (reservation.getSpecialRequests() != null && !reservation.getSpecialRequests().isEmpty()) {
            grid.add(createDetailLabel("Special Requests:"), 0, 9);
            Label requestsLabel = createDetailValue(reservation.getSpecialRequests());
            requestsLabel.setWrapText(true);
            grid.add(requestsLabel, 1, 9);
        }
        
        // Button row
        HBox buttonBar = new HBox(10);
        buttonBar.setPadding(new Insets(20, 0, 0, 0));
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        
        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: #444444;");
        closeButton.setOnAction(e -> detailStage.close());
        
        Button printButton = new Button("Print Details");
        printButton.setStyle("-fx-background-color: " + UIConstants.BRAND_COLOR_HEX + "; -fx-text-fill: white;");
        printButton.setOnAction(e -> {
            DialogUtil.showAlert(Alert.AlertType.INFORMATION, detailStage, "Print", "Sending reservation details to printer...");
        });
        
        buttonBar.getChildren().addAll(printButton, closeButton);
        GridPane.setColumnSpan(buttonBar, 4);
        grid.add(buttonBar, 0, 10);
        
        // Add column constraints
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPrefWidth(120);
        
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPrefWidth(250);
        
        grid.getColumnConstraints().addAll(col1, col2);
        
        Scene scene = new Scene(grid, 400, 500);
        detailStage.setScene(scene);
        detailStage.showAndWait();
    }
    
    private Label createDetailLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font(UIConstants.FONT_SANS_SERIF_CLEAN, FontWeight.BOLD, 14));
        label.setTextFill(Color.web(UIConstants.ACCENT_COLOR_DARK_BROWN_HEX));
        return label;
    }
    
    private Label createDetailValue(String text) {
        Label label = new Label(text);
        label.setFont(Font.font(UIConstants.FONT_SANS_SERIF_CLEAN, 14));
        return label;
    }

    private void refreshGuestList() {
        if (guestList == null) {
            guestList = FXCollections.observableArrayList();
        }
        guestList.setAll(guestDAO.getAllGuests());
    }
    
    private void showGuestDialog(Guest guest) {
        boolean isNewGuest = (guest == null);
        Guest editGuest = isNewGuest ? new Guest() : guest;
        
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isNewGuest ? "Add New Guest" : "Edit Guest");
        
        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        if (!isNewGuest) firstNameField.setText(editGuest.getFirstName());
        
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        if (!isNewGuest) lastNameField.setText(editGuest.getLastName());
        
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        if (!isNewGuest) emailField.setText(editGuest.getEmail());
        
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone");
        if (!isNewGuest) phoneField.setText(editGuest.getPhoneNumber());
        
        DatePicker dobPicker = new DatePicker();
        if (!isNewGuest && editGuest.getDateOfBirth() != null) {
            dobPicker.setValue(editGuest.getDateOfBirth());
        }
        
        TextField nationalityField = new TextField();
        nationalityField.setPromptText("Nationality");
        if (!isNewGuest) nationalityField.setText(editGuest.getNationality());
        
        grid.add(new Label("First Name:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Phone:"), 0, 3);
        grid.add(phoneField, 1, 3);
        grid.add(new Label("Date of Birth:"), 0, 4);
        grid.add(dobPicker, 1, 4);
        grid.add(new Label("Nationality:"), 0, 5);
        grid.add(nationalityField, 1, 5);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                editGuest.setFirstName(firstNameField.getText());
                editGuest.setLastName(lastNameField.getText());
                editGuest.setEmail(emailField.getText());
                editGuest.setPhoneNumber(phoneField.getText());
                editGuest.setDateOfBirth(dobPicker.getValue());
                editGuest.setNationality(nationalityField.getText());
                return ButtonType.OK;
            }
            return null;
        });
        
        Optional<ButtonType> result = dialog.showAndWait();
        
        if (result.isPresent() && result.get() == saveButton) {
            boolean success;
            if (isNewGuest) {
                success = guestDAO.addGuest(editGuest);
            } else {
                success = guestDAO.updateGuest(editGuest);
            }
            
            if (success) {
                refreshGuestList();
            }
        }
    }

    public Employee getCurrentUser() {
        return this.currentUser;
    }
    
    // Inner class to hold reservation data
    public class ReservationData {
        private final int id;
        private final String guestName;
        private final LocalDate checkIn;
        private final LocalDate checkOut;
        private final String roomInfo;
        private String status;
        private final double total;
        
        public ReservationData(int id, String guestName, LocalDate checkIn, LocalDate checkOut, 
                              String roomInfo, String status, double total) {
            this.id = id;
            this.guestName = guestName;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
            this.roomInfo = roomInfo;
            this.status = status;
            this.total = total;
        }
        
        // Getters
        public int getId() { return id; }
        public String getGuestName() { return guestName; }
        public LocalDate getCheckIn() { return checkIn; }
        public LocalDate getCheckOut() { return checkOut; }
        public String getRoomInfo() { return roomInfo; }
        public String getStatus() { return status; }
        public double getTotal() { return total; }
        
        // Setters
        public void setStatus(String status) { this.status = status; }
    }

    private void updateDashboardCenter(String section) {
        Node content;
        currentSection = section;
        switch (section) {
            case "dashboard":
                content = createDashboardContent();
                break;
            case "guests":
                // Use our enhanced GuestManagementController instead of inline implementation
                GuestManagementController guestManagementController = new GuestManagementController(mainApp, guestDAO);
                content = guestManagementController.createGuestManagementPane();
                break;
            case "rooms":
                content = createRoomsContent();
                break;
            case "reservations":
                content = createReservationsContent();
                break;
            case "invoices":
                content = createInvoicesContent();
                break;
            case "employees":
                content = createEmployeesContent();
                break;
            case "maintenance":
                content = createMaintenanceContent();
                break;
            case "housekeeping":
                content = createHousekeepingContent();
                break;
            case "departments":
                content = createDepartmentsContent();
                break;
            case "roles":
                content = createRolesContent();
                break;
            case "settings":
                content = createSettingsContent();
                break;
            default:
                content = createDashboardContent();
                break;
        }
        BorderPane rootPane = (BorderPane) mainApp.getPrimaryStage().getScene().getRoot();
        rootPane.setCenter(content);
    }

    private Node createRoomsContent() {
        // Use the dedicated RoomManagementController for complete room management functionality
        RoomManagementController roomController = new RoomManagementController(mainApp);
        return roomController.createRoomManagementPane();
    }
    
    private Node createEmployeesContent() {
        // Use the dedicated EmployeeManagementController for complete employee management functionality
        EmployeeManagementController employeeController = new EmployeeManagementController(
                mainApp,
                new EmployeeDAO(),
                new RoleDAO(),
                new DepartmentDAO());
        return employeeController.createEmployeeManagementPane();
    }
    
    private Node createMaintenanceContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        
        // Title
        Label titleLabel = new Label("Maintenance Requests");
        titleLabel.setFont(Font.font(UIConstants.FONT_SERIF_ELEGANT, FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.web(UIConstants.ACCENT_COLOR_DARK_BROWN_HEX));
        
        // Action bar
        HBox actionBar = new HBox(10);
        actionBar.setPadding(new Insets(10, 0, 20, 0));
        
        Button addButton = new Button("New Request");
        addButton.setStyle("-fx-background-color: " + UIConstants.BRAND_COLOR_HEX + "; -fx-text-fill: white;");
        
        Button refreshButton = new Button("Refresh");
        refreshButton.setStyle("-fx-background-color: " + UIConstants.ACCENT_COLOR_DARK_BROWN_HEX + "; -fx-text-fill: white;");
        
        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All Status", "Open", "In Progress", "Completed", "Cancelled");
        statusFilter.setValue("All Status");
        statusFilter.setPrefWidth(150);
        
        ComboBox<String> priorityFilter = new ComboBox<>();
        priorityFilter.getItems().addAll("All Priorities", "Low", "Medium", "High", "Urgent");
        priorityFilter.setValue("All Priorities");
        priorityFilter.setPrefWidth(150);
        
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        actionBar.getChildren().addAll(addButton, refreshButton, new Label("Status:"), statusFilter, 
            new Label("Priority:"), priorityFilter, spacer);
        
        // Create table for maintenance requests
        TableView<Object> maintenanceTable = new TableView<>();
        maintenanceTable.setPlaceholder(new Label("Maintenance requests database will be integrated soon"));
        VBox.setVgrow(maintenanceTable, Priority.ALWAYS);
        
        content.getChildren().addAll(titleLabel, actionBar, maintenanceTable);
        return content;
    }
    
    private Node createHousekeepingContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        
        // Title
        Label titleLabel = new Label("Housekeeping Management");
        titleLabel.setFont(Font.font(UIConstants.FONT_SERIF_ELEGANT, FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.web(UIConstants.ACCENT_COLOR_DARK_BROWN_HEX));
        
        // Create tabs for different housekeeping views
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Room status tab
        Tab roomStatusTab = new Tab("Room Status");
        VBox roomStatusContent = new VBox(15);
        roomStatusContent.setPadding(new Insets(20));
        
        HBox filterBar = new HBox(10);
        filterBar.setPadding(new Insets(0, 0, 10, 0));
        
        ComboBox<String> floorFilter = new ComboBox<>();
        floorFilter.getItems().addAll("All Floors", "1st Floor", "2nd Floor", "3rd Floor", "4th Floor", "5th Floor");
        floorFilter.setValue("All Floors");
        
        Button refreshButton = new Button("Refresh");
        refreshButton.setStyle("-fx-background-color: " + UIConstants.ACCENT_COLOR_DARK_BROWN_HEX + "; -fx-text-fill: white;");
        
        filterBar.getChildren().addAll(new Label("Floor:"), floorFilter, refreshButton);
        
        // Room status grid
        GridPane roomGrid = new GridPane();
        roomGrid.setHgap(10);
        roomGrid.setVgap(10);
        roomGrid.setPadding(new Insets(10));
        roomGrid.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 5px; " + 
                         UIConstants.SHADOW_EFFECT_CSS);
        
        // Add some placeholder room items
        for (int i = 0; i < 10; i++) {
            VBox roomBox = createRoomStatusBox("10" + (i+1), i % 3 == 0 ? "Clean" : 
                                              i % 3 == 1 ? "Dirty" : "Inspecting");
            roomGrid.add(roomBox, i % 5, i / 5);
        }
        
        ScrollPane scrollPane = new ScrollPane(roomGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        roomStatusContent.getChildren().addAll(filterBar, scrollPane);
        roomStatusTab.setContent(roomStatusContent);
        
        // Staff assignments tab
        Tab staffAssignmentsTab = new Tab("Staff Assignments");
        VBox staffContent = new VBox(15);
        staffContent.setPadding(new Insets(20));
        staffContent.getChildren().add(new Label("Staff assignment interface will be implemented soon."));
        staffAssignmentsTab.setContent(staffContent);
        
        // Supplies inventory tab
        Tab suppliesTab = new Tab("Supplies Inventory");
        VBox suppliesContent = new VBox(15);
        suppliesContent.setPadding(new Insets(20));
        suppliesContent.getChildren().add(new Label("Supplies inventory interface will be implemented soon."));
        suppliesTab.setContent(suppliesContent);
        
        tabPane.getTabs().addAll(roomStatusTab, staffAssignmentsTab, suppliesTab);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        
        content.getChildren().addAll(titleLabel, tabPane);
        return content;
    }
    
    private Node createDepartmentsContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        
        // Title
        Label titleLabel = new Label("Department Management");
        titleLabel.setFont(Font.font(UIConstants.FONT_SERIF_ELEGANT, FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.web(UIConstants.ACCENT_COLOR_DARK_BROWN_HEX));
        
        // Action bar
        HBox actionBar = new HBox(10);
        actionBar.setPadding(new Insets(10, 0, 20, 0));
        
        Button addButton = new Button("Add Department");
        addButton.setStyle("-fx-background-color: " + UIConstants.BRAND_COLOR_HEX + "; -fx-text-fill: white;");
        
        Button refreshButton = new Button("Refresh");
        refreshButton.setStyle("-fx-background-color: " + UIConstants.ACCENT_COLOR_DARK_BROWN_HEX + "; -fx-text-fill: white;");
        
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        actionBar.getChildren().addAll(addButton, refreshButton, spacer);
        
        // Create department cards
        FlowPane departmentsPane = new FlowPane(15, 15);
        departmentsPane.setPadding(new Insets(10));
        
        // Add sample departments
        String[] departments = {"Management", "Front Office", "Housekeeping", "Maintenance", "Food & Beverage", "Security"};
        int[] staffCounts = {5, 12, 8, 4, 15, 6};
        
        for (int i = 0; i < departments.length; i++) {
            VBox deptCard = createDepartmentCard(departments[i], staffCounts[i]);
            departmentsPane.getChildren().add(deptCard);
        }
        
        ScrollPane scrollPane = new ScrollPane(departmentsPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        content.getChildren().addAll(titleLabel, actionBar, scrollPane);
        return content;
    }
    
    private Node createRolesContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        
        // Title
        Label titleLabel = new Label("User Roles & Permissions");
        titleLabel.setFont(Font.font(UIConstants.FONT_SERIF_ELEGANT, FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.web(UIConstants.ACCENT_COLOR_DARK_BROWN_HEX));
        
        // Action bar
        HBox actionBar = new HBox(10);
        actionBar.setPadding(new Insets(10, 0, 20, 0));
        
        Button addButton = new Button("Add Role");
        addButton.setStyle("-fx-background-color: " + UIConstants.BRAND_COLOR_HEX + "; -fx-text-fill: white;");
        
        Button refreshButton = new Button("Refresh");
        refreshButton.setStyle("-fx-background-color: " + UIConstants.ACCENT_COLOR_DARK_BROWN_HEX + "; -fx-text-fill: white;");
        
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        actionBar.getChildren().addAll(addButton, refreshButton, spacer);
        
        // Create table for roles
        TableView<Object> rolesTable = new TableView<>();
        rolesTable.setPlaceholder(new Label("Roles management database will be integrated soon"));
        VBox.setVgrow(rolesTable, Priority.ALWAYS);
        
        // Permission matrix section
        Label permissionMatrixTitle = new Label("Permission Matrix");
        permissionMatrixTitle.setFont(Font.font(UIConstants.FONT_SERIF_ELEGANT, FontWeight.BOLD, 20));
        permissionMatrixTitle.setPadding(new Insets(20, 0, 10, 0));
        
        GridPane permissionGrid = new GridPane();
        permissionGrid.setHgap(20);
        permissionGrid.setVgap(10);
        permissionGrid.setPadding(new Insets(20));
        permissionGrid.setStyle("-fx-background-color: white; -fx-background-radius: 5px; " + 
                              UIConstants.SHADOW_EFFECT_CSS);
        
        // Add permission matrix headers
        permissionGrid.add(new Label("Permission"), 0, 0);
        permissionGrid.add(new Label("Admin"), 1, 0);
        permissionGrid.add(new Label("Manager"), 2, 0);
        permissionGrid.add(new Label("Staff"), 3, 0);
        permissionGrid.add(new Label("Housekeeper"), 4, 0);
        
        // Add some sample permissions
        String[] permissions = {"View Dashboard", "Manage Guests", "Manage Rooms", "Manage Reservations", 
                              "Manage Employees", "Manage Departments", "Manage Roles", "System Settings"};
        
        for (int i = 0; i < permissions.length; i++) {
            permissionGrid.add(new Label(permissions[i]), 0, i + 1);
            
            for (int j = 1; j <= 4; j++) {
                CheckBox checkBox = new CheckBox();
                checkBox.setDisable(true); // Since this is just a demonstration
                
                // Set some checkboxes as checked based on role logic
                if (j == 1) { // Admin has all permissions
                    checkBox.setSelected(true);
                } else if (j == 2) { // Manager has most permissions
                    checkBox.setSelected(i < 6);
                } else if (j == 3) { // Staff has limited permissions
                    checkBox.setSelected(i < 4);
                } else if (j == 4) { // Housekeeper has very limited permissions
                    checkBox.setSelected(i == 0 || i == 2);
                }
                
                permissionGrid.add(checkBox, j, i + 1);
            }
        }
        
        content.getChildren().addAll(titleLabel, actionBar, rolesTable, permissionMatrixTitle, permissionGrid);
        return content;
    }
    
    private Node createSettingsContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        
        // Title
        Label titleLabel = new Label("System Settings");
        titleLabel.setFont(Font.font(UIConstants.FONT_SERIF_ELEGANT, FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.web(UIConstants.ACCENT_COLOR_DARK_BROWN_HEX));
        
        // Create tabs for different settings sections
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // General settings tab
        Tab generalTab = new Tab("General");
        VBox generalContent = new VBox(15);
        generalContent.setPadding(new Insets(20));
        
        GridPane generalGrid = new GridPane();
        generalGrid.setHgap(20);
        generalGrid.setVgap(15);
        generalGrid.setPadding(new Insets(20));
        
        Label hotelNameLabel = new Label("Hotel Name:");
        TextField hotelNameField = new TextField("Raffles Hotels & Resorts");
        
        Label hotelAddressLabel = new Label("Hotel Address:");
        TextArea hotelAddressField = new TextArea("1 Beach Road, Singapore 189673");
        hotelAddressField.setPrefRowCount(2);
        
        Label contactLabel = new Label("Contact Number:");
        TextField contactField = new TextField("+65 1234 5678");
        
        Label emailLabel = new Label("Email Address:");
        TextField emailField = new TextField("info@raffleshotels.com");
        
        Label currencyLabel = new Label("Currency:");
        ComboBox<String> currencyComboBox = new ComboBox<>();
        currencyComboBox.getItems().addAll("USD ($)", "EUR (€)", "GBP (£)", "JPY (¥)", "SGD (S$)");
        currencyComboBox.setValue("USD ($)");
        
        Label timeZoneLabel = new Label("Time Zone:");
        ComboBox<String> timeZoneComboBox = new ComboBox<>();
        timeZoneComboBox.getItems().addAll("UTC-08:00 (PST)", "UTC-05:00 (EST)", "UTC+00:00 (GMT)", "UTC+08:00 (SGT)", "UTC+09:00 (JST)");
        timeZoneComboBox.setValue("UTC+08:00 (SGT)");
        
        generalGrid.add(hotelNameLabel, 0, 0);
        generalGrid.add(hotelNameField, 1, 0);
        generalGrid.add(hotelAddressLabel, 0, 1);
        generalGrid.add(hotelAddressField, 1, 1);
        generalGrid.add(contactLabel, 0, 2);
        generalGrid.add(contactField, 1, 2);
        generalGrid.add(emailLabel, 0, 3);
        generalGrid.add(emailField, 1, 3);
        generalGrid.add(currencyLabel, 0, 4);
        generalGrid.add(currencyComboBox, 1, 4);
        generalGrid.add(timeZoneLabel, 0, 5);
        generalGrid.add(timeZoneComboBox, 1, 5);
        
        Button saveGeneralButton = new Button("Save Changes");
        saveGeneralButton.setStyle("-fx-background-color: " + UIConstants.BRAND_COLOR_HEX + "; -fx-text-fill: white;");
        
        generalContent.getChildren().addAll(generalGrid, saveGeneralButton);
        generalTab.setContent(generalContent);
        
        // Database settings tab
        Tab databaseTab = new Tab("Database");
        VBox databaseContent = new VBox(15);
        databaseContent.setPadding(new Insets(20));
        
        GridPane databaseGrid = new GridPane();
        databaseGrid.setHgap(20);
        databaseGrid.setVgap(15);
        databaseGrid.setPadding(new Insets(20));
        
        Label dbTypeLabel = new Label("Database Type:");
        ComboBox<String> dbTypeComboBox = new ComboBox<>();
        dbTypeComboBox.getItems().addAll("HSQLDB", "MySQL", "PostgreSQL", "SQL Server");
        dbTypeComboBox.setValue("HSQLDB");
        
        Label dbLocationLabel = new Label("Database Location:");
        TextField dbLocationField = new TextField("C:/Users/loq/Downloads/raffles_hotel_db");
        
        Label dbUserLabel = new Label("Username:");
        TextField dbUserField = new TextField("sa");
        
        Label dbPasswordLabel = new Label("Password:");
        PasswordField dbPasswordField = new PasswordField();
        
        Label backupLocationLabel = new Label("Backup Location:");
        HBox backupLocationBox = new HBox(10);
        TextField backupLocationField = new TextField("C:/Users/loq/Downloads/raffles_backups");
        Button browseButton = new Button("Browse...");
        backupLocationBox.getChildren().addAll(backupLocationField, browseButton);
        
        databaseGrid.add(dbTypeLabel, 0, 0);
        databaseGrid.add(dbTypeComboBox, 1, 0);
        databaseGrid.add(dbLocationLabel, 0, 1);
        databaseGrid.add(dbLocationField, 1, 1);
        databaseGrid.add(dbUserLabel, 0, 2);
        databaseGrid.add(dbUserField, 1, 2);
        databaseGrid.add(dbPasswordLabel, 0, 3);
        databaseGrid.add(dbPasswordField, 1, 3);
        databaseGrid.add(backupLocationLabel, 0, 4);
        databaseGrid.add(backupLocationBox, 1, 4);
        
        HBox dbButtonBar = new HBox(10);
        Button saveDatabaseButton = new Button("Save Configuration");
        saveDatabaseButton.setStyle("-fx-background-color: " + UIConstants.BRAND_COLOR_HEX + "; -fx-text-fill: white;");
        Button backupButton = new Button("Backup Database Now");
        backupButton.setStyle("-fx-background-color: " + UIConstants.ACCENT_COLOR_DARK_BROWN_HEX + "; -fx-text-fill: white;");
        dbButtonBar.getChildren().addAll(saveDatabaseButton, backupButton);
        
        databaseContent.getChildren().addAll(databaseGrid, dbButtonBar);
        databaseTab.setContent(databaseContent);
        
        // User interface settings tab
        Tab uiTab = new Tab("User Interface");
        VBox uiContent = new VBox(15);
        uiContent.setPadding(new Insets(20));
        
        // UI settings content here
        uiContent.getChildren().add(new Label("User interface settings will be implemented soon."));
        uiTab.setContent(uiContent);
        
        tabPane.getTabs().addAll(generalTab, databaseTab, uiTab);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        
        content.getChildren().addAll(titleLabel, tabPane);
        return content;
    }
    
    // Helper method for department cards
    private VBox createDepartmentCard(String name, int staffCount) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setMinWidth(200);
        card.setMaxWidth(200);
        card.setMinHeight(120);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10px; " + UIConstants.SHADOW_EFFECT_CSS);
        
        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font(UIConstants.FONT_SERIF_ELEGANT, FontWeight.BOLD, 16));
        nameLabel.setWrapText(true);
        
        HBox staffInfo = new HBox(5);
        Label staffIcon = new Label("👥");
        Label staffLabel = new Label(staffCount + " Staff");
        staffInfo.getChildren().addAll(staffIcon, staffLabel);
        
        HBox buttonBar = new HBox(5);
        Button editButton = new Button("Edit");
        editButton.setStyle("-fx-background-color: " + UIConstants.BRAND_COLOR_HEX + "; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 3 8;");
        Button viewButton = new Button("View");
        viewButton.setStyle("-fx-background-color: " + UIConstants.ACCENT_COLOR_DARK_BROWN_HEX + "; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 3 8;");
        buttonBar.getChildren().addAll(editButton, viewButton);
        
        card.getChildren().addAll(nameLabel, staffInfo, new Region(), buttonBar);
        VBox.setVgrow(card.getChildren().get(2), Priority.ALWAYS);
        
        return card;
    }
    
    // Helper method for room status boxes in housekeeping
    private VBox createRoomStatusBox(String roomNumber, String status) {
        VBox box = new VBox(5);
        box.setPadding(new Insets(10));
        box.setPrefWidth(120);
        box.setPrefHeight(80);
        
        String backgroundColor;
        switch (status) {
            case "Clean":
                backgroundColor = "#4CAF50"; // Green
                break;
            case "Dirty":
                backgroundColor = "#FF9800"; // Orange
                break;
            case "Inspecting":
                backgroundColor = "#2196F3"; // Blue
                break;
            default:
                backgroundColor = "#9E9E9E"; // Gray
        }
        
        box.setStyle("-fx-background-color: " + backgroundColor + "; " +
                   "-fx-background-radius: 5px; -fx-alignment: center;");
        
        Label roomLabel = new Label("Room " + roomNumber);
        roomLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        
        Label statusLabel = new Label(status);
        statusLabel.setStyle("-fx-text-fill: white;");
        
        box.getChildren().addAll(roomLabel, statusLabel);
        
        return box;
    }

    /**
     * Debug utility to reset the database completely.
     * This is for development/testing only - remove in production.
     */
    private void addDebugResetOption(BorderPane dashboardLayout) {
        // Add a small button at the bottom right corner
        Button resetDbBtn = new Button("Reset DB");
        resetDbBtn.setStyle("-fx-font-size: 10px; -fx-background-color: #550000; -fx-text-fill: white;");
        resetDbBtn.setOnAction(e -> {
            // Ask for confirmation
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("DANGER: Database Reset");
            alert.setHeaderText("Reset Database?");
            alert.setContentText("WARNING: This will delete ALL data! The application will restart. Continue?");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                DatabaseConnector.resetDatabase();
                // Force application restart
                mainApp.getPrimaryStage().close();
                Platform.runLater(() -> {
                    try {
                        new MainApp().start(new Stage());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }
        });
        
        // Place in the bottom right corner
        BorderPane.setAlignment(resetDbBtn, Pos.BOTTOM_RIGHT);
        BorderPane.setMargin(resetDbBtn, new Insets(5));
        dashboardLayout.setBottom(resetDbBtn);
    }

    private void showNewReservationDialog() {
        // Create a dialog for new reservation
        Dialog<Reservation> dialog = new Dialog<>();
        dialog.setTitle("New Reservation");
        dialog.setHeaderText("Create a new reservation");
        
        // Set the button types
        ButtonType saveButtonType = new ButtonType("Create Reservation", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create the reservation form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Load guests for the guest dropdown
        ComboBox<Guest> guestComboBox = new ComboBox<>();
        guestComboBox.setPromptText("Select a guest");
        guestComboBox.setMaxWidth(Double.MAX_VALUE);
        ObservableList<Guest> guests = new GuestDAO().getAllGuests();
        guestComboBox.setItems(guests);
        guestComboBox.setCellFactory(param -> new ListCell<Guest>() {
            @Override
            protected void updateItem(Guest item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getFirstName() + " " + item.getLastName());
            }
        });
        guestComboBox.setButtonCell(new ListCell<Guest>() {
            @Override
            protected void updateItem(Guest item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getFirstName() + " " + item.getLastName());
            }
        });
        
        // Room type dropdown
        ComboBox<RoomType> roomTypeComboBox = new ComboBox<>();
        roomTypeComboBox.setPromptText("Select a room type");
        roomTypeComboBox.setMaxWidth(Double.MAX_VALUE);
        ObservableList<RoomType> roomTypes = new RoomTypeDAO().getAllRoomTypes();
        roomTypeComboBox.setItems(roomTypes);
        
        // Available rooms for the selected room type
        ComboBox<Room> roomComboBox = new ComboBox<>();
        roomComboBox.setPromptText("Select a room");
        roomComboBox.setMaxWidth(Double.MAX_VALUE);
        roomComboBox.setDisable(true); // Enable when room type is selected
        
        // Date pickers for check-in and check-out
        DatePicker checkInDatePicker = new DatePicker(LocalDate.now());
        DatePicker checkOutDatePicker = new DatePicker(LocalDate.now().plusDays(1));
        
        // Number of adults and children
        Spinner<Integer> adultsSpinner = new Spinner<>(1, 10, 1);
        adultsSpinner.setEditable(true);
        adultsSpinner.setPrefWidth(100);
        
        Spinner<Integer> childrenSpinner = new Spinner<>(0, 10, 0);
        childrenSpinner.setEditable(true);
        childrenSpinner.setPrefWidth(100);
        
        // Special requests
        TextArea specialRequestsArea = new TextArea();
        specialRequestsArea.setPrefRowCount(3);
        specialRequestsArea.setPrefWidth(300);
        
        // Total cost field
        Label totalCostLabel = new Label("$0.00");
        totalCostLabel.setFont(Font.font(UIConstants.FONT_SANS_SERIF_CLEAN, FontWeight.BOLD, 16));
        totalCostLabel.setTextFill(Color.web(UIConstants.BRAND_COLOR_HEX));
        
        // Update total cost when dates or room type changes
        ChangeListener<Object> updateCostListener = (observable, oldValue, newValue) -> {
            if (roomTypeComboBox.getValue() != null && 
                checkInDatePicker.getValue() != null && 
                checkOutDatePicker.getValue() != null) {
                
                LocalDate checkIn = checkInDatePicker.getValue();
                LocalDate checkOut = checkOutDatePicker.getValue();
                RoomType selectedRoomType = roomTypeComboBox.getValue();
                
                if (checkIn != null && checkOut != null && selectedRoomType != null) {
                    long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
                    if (nights > 0 && selectedRoomType.getBaseRate() != null) {
                        BigDecimal totalCost = selectedRoomType.getBaseRate().multiply(BigDecimal.valueOf(nights));
                        totalCostLabel.setText(String.format("$%.2f", totalCost));
                    }
                }
            }
        };
        
        roomTypeComboBox.valueProperty().addListener(updateCostListener);
        checkInDatePicker.valueProperty().addListener(updateCostListener);
        checkOutDatePicker.valueProperty().addListener(updateCostListener);
        
        // Update available rooms when dates or room type changes
        ChangeListener<Object> updateRoomsListener = (observable, oldValue, newValue) -> {
            if (roomTypeComboBox.getValue() != null && 
                checkInDatePicker.getValue() != null && 
                checkOutDatePicker.getValue() != null) {
                
                LocalDate checkIn = checkInDatePicker.getValue();
                LocalDate checkOut = checkOutDatePicker.getValue();
                RoomType selectedRoomType = roomTypeComboBox.getValue();
                
                if (checkIn != null && checkOut != null && selectedRoomType != null) {
                    // Get rooms of the selected type
                    RoomDAO roomDAO = new RoomDAO();
                    ObservableList<Room> availableRooms = roomDAO.getRoomsByType(selectedRoomType.getRoomTypeID());
                    
                    // Filter rooms that are available for the selected dates
                    ReservationDAO reservationDAO = new ReservationDAO();
                    ObservableList<Room> filteredRooms = availableRooms.filtered(room -> 
                        room.getStatus().equals("Available") && 
                        reservationDAO.isRoomAvailable(room.getRoomID(), checkIn, checkOut)
                    );
                    
                    roomComboBox.setItems(filteredRooms);
                    roomComboBox.setDisable(filteredRooms.isEmpty());
                    
                    if (filteredRooms.isEmpty()) {
                        roomComboBox.setPromptText("No rooms available");
                    } else {
                        roomComboBox.setPromptText("Select a room");
                        roomComboBox.getSelectionModel().selectFirst();
                    }
                }
            }
        };
        
        roomTypeComboBox.valueProperty().addListener(updateRoomsListener);
        checkInDatePicker.valueProperty().addListener(updateRoomsListener);
        checkOutDatePicker.valueProperty().addListener(updateRoomsListener);
        
        // Add components to grid
        grid.add(new Label("Guest:"), 0, 0);
        grid.add(guestComboBox, 1, 0);
        grid.add(new Label("Room Type:"), 0, 1);
        grid.add(roomTypeComboBox, 1, 1);
        grid.add(new Label("Room:"), 0, 2);
        grid.add(roomComboBox, 1, 2);
        grid.add(new Label("Check-in Date:"), 0, 3);
        grid.add(checkInDatePicker, 1, 3);
        grid.add(new Label("Check-out Date:"), 0, 4);
        grid.add(checkOutDatePicker, 1, 4);
        grid.add(new Label("Adults:"), 0, 5);
        grid.add(adultsSpinner, 1, 5);
        grid.add(new Label("Children:"), 0, 6);
        grid.add(childrenSpinner, 1, 6);
        grid.add(new Label("Special Requests:"), 0, 7);
        grid.add(specialRequestsArea, 1, 7);
        grid.add(new Label("Total Cost:"), 0, 8);
        grid.add(totalCostLabel, 1, 8);
        
        dialog.getDialogPane().setContent(grid);
        
        // Convert the result to reservation object when button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Validate inputs
                if (guestComboBox.getValue() == null) {
                    DialogUtil.showAlert(Alert.AlertType.ERROR, mainApp.getPrimaryStage(), 
                        "Validation Error", "Please select a guest.");
                    return null;
                }
                
                if (roomTypeComboBox.getValue() == null) {
                    DialogUtil.showAlert(Alert.AlertType.ERROR, mainApp.getPrimaryStage(), 
                        "Validation Error", "Please select a room type.");
                    return null;
                }
                
                LocalDate checkIn = checkInDatePicker.getValue();
                LocalDate checkOut = checkOutDatePicker.getValue();
                
                if (checkIn == null || checkOut == null || checkIn.isAfter(checkOut)) {
                    DialogUtil.showAlert(Alert.AlertType.ERROR, mainApp.getPrimaryStage(), 
                        "Validation Error", "Please select valid check-in and check-out dates.");
                    return null;
                }
                
                // Create reservation object
                Reservation newReservation = new Reservation();
                newReservation.setGuestID(guestComboBox.getValue().getGuestID());
                newReservation.setRoomTypeID(roomTypeComboBox.getValue().getRoomTypeID());
                if (roomComboBox.getValue() != null) {
                    newReservation.setRoomID(roomComboBox.getValue().getRoomID());
                }
                newReservation.setCheckInDate(checkIn);
                newReservation.setCheckOutDate(checkOut);
                newReservation.setNumberOfAdults(adultsSpinner.getValue());
                newReservation.setNumberOfChildren(childrenSpinner.getValue());
                newReservation.setStatus("Confirmed");
                newReservation.setBookingSource("Front Desk");
                newReservation.setDateBooked(LocalDate.now());
                newReservation.setSpecialRequests(specialRequestsArea.getText());
                
                // Calculate total cost
                long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
                if (nights > 0 && roomTypeComboBox.getValue().getBaseRate() != null) {
                    BigDecimal totalCost = roomTypeComboBox.getValue().getBaseRate().multiply(BigDecimal.valueOf(nights));
                    newReservation.setTotalEstimatedCost(totalCost);
                }
                
                return newReservation;
            }
            return null;
        });
        
        // Show dialog and process result
        Optional<Reservation> result = dialog.showAndWait();
        result.ifPresent(reservation -> {
            ReservationDAO reservationDAO = new ReservationDAO();
            boolean success = reservationDAO.addReservation(reservation);
            
            if (success) {
                DialogUtil.showAlert(Alert.AlertType.INFORMATION, mainApp.getPrimaryStage(),
                    "Reservation Created", "Reservation has been created successfully.");
                
                // Refresh reservations if we're in the reservations tab
                if ("reservations".equals(currentSection)) {
                    System.out.println("Refreshing reservations view");
                    
                    // Force a complete refresh of the reservations content
                    Platform.runLater(() -> {
                        updateDashboardCenter("reservations");
                    });
                }
            } else {
                DialogUtil.showAlert(Alert.AlertType.ERROR, mainApp.getPrimaryStage(),
                    "Error", "Failed to create reservation. Please try again.");
            }
        });
    }

    private Node createInvoicesContent() {
        // Use the InvoiceManagementController to create the invoice management UI
        InvoiceManagementController invoiceManagementController = new InvoiceManagementController(mainApp);
        return invoiceManagementController.createInvoiceManagementPane();
    }
}
