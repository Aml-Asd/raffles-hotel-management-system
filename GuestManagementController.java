package com.example.demo10.raffles.hotelmgmt.controller;

import com.example.demo10.raffles.hotelmgmt.MainApp;
import com.example.demo10.raffles.hotelmgmt.dao.GuestDAO;
import com.example.demo10.raffles.hotelmgmt.model.Guest;
import com.example.demo10.raffles.hotelmgmt.ui.DialogUtil;
import com.example.demo10.raffles.hotelmgmt.ui.UIConstants;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;

public class GuestManagementController {
    private MainApp mainApp;
    private GuestDAO guestDAO;
    private ObservableList<Guest> guestObservableList;
    private TableView<Guest> guestTable;

    public GuestManagementController(MainApp mainApp, GuestDAO guestDAO) {
        this.mainApp = mainApp;
        this.guestDAO = guestDAO;
        this.guestObservableList = FXCollections.observableArrayList();
    }

    public Node createGuestManagementPane() {
        // Load guests immediately
        loadAllGuests();

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30, 40, 30, 40));

        HBox titleBar = new HBox(15);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        Text title = new Text("Guest Management");
        title.setFont(Font.font(UIConstants.FONT_SERIF_ELEGANT, FontWeight.BOLD, 34));
        title.setFill(UIConstants.ACCENT_COLOR_DARK_BROWN_FX);
        title.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.3), 3, 0, 1, 1));
        
        Region titleSpacer = new Region(); 
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);
        
        Button refreshButton = new Button("Refresh");
        String refreshBtnStyle = UIConstants.LUXURY_BUTTON_STYLE + "-fx-font-size: 13px; -fx-background-radius: 20; -fx-padding: 8px 15px;";
        String refreshBtnHoverStyle = UIConstants.LUXURY_BUTTON_HOVER_STYLE;
        refreshButton.setStyle(refreshBtnStyle);
        refreshButton.setOnMouseEntered(e -> refreshButton.setStyle(refreshBtnStyle + refreshBtnHoverStyle));
        refreshButton.setOnMouseExited(e -> refreshButton.setStyle(refreshBtnStyle));
        refreshButton.setOnAction(e -> {
            loadAllGuests();
            animateRefresh();
        });
        
        TextField searchField = new TextField();
        searchField.setPromptText("Search guests...");
        searchField.setPrefWidth(250);
        searchField.setStyle("-fx-background-color: white; -fx-border-color: " + UIConstants.BRAND_COLOR_HEX + 
                           "; -fx-border-radius: 20px; -fx-background-radius: 20px; -fx-padding: 8px 15px;" +
                           UIConstants.SHADOW_EFFECT_CSS);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterGuestList(newVal);
        });
        
        titleBar.getChildren().addAll(title, titleSpacer, searchField, refreshButton);

        guestTable = new TableView<>(guestObservableList);
        guestTable.setPlaceholder(new Label("No guests found or added yet. Click 'Add Guest' to begin."));
        guestTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Table columns with better styling
        TableColumn<Guest, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("guestID"));
        idCol.setMaxWidth(60);
        
        TableColumn<Guest, String> firstNameCol = new TableColumn<>("First Name");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        firstNameCol.setPrefWidth(120);
        
        TableColumn<Guest, String> lastNameCol = new TableColumn<>("Last Name");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        lastNameCol.setPrefWidth(120);
        
        TableColumn<Guest, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200);
        
        TableColumn<Guest, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        phoneCol.setPrefWidth(120);
        
        TableColumn<Guest, LocalDate> dobCol = new TableColumn<>("Date of Birth");
        dobCol.setCellValueFactory(new PropertyValueFactory<>("dateOfBirth"));
        dobCol.setPrefWidth(120);
        
        TableColumn<Guest, String> loyaltyCol = new TableColumn<>("Loyalty ID");
        loyaltyCol.setCellValueFactory(new PropertyValueFactory<>("loyaltyProgramID")); 
        loyaltyCol.setPrefWidth(100);
        
        // Add action column with view/edit/delete buttons
        TableColumn<Guest, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(col -> new TableCell<Guest, Void>() {
            private final Button viewBtn = new Button("View");
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox actionBox = new HBox(5, viewBtn, editBtn, deleteBtn);
            
            {
                // Style buttons
                String btnStyle = "-fx-background-color: " + UIConstants.BRAND_COLOR_HEX + 
                                "; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 3 8;";
                String editStyle = "-fx-background-color: " + UIConstants.DARK_GOLD_COLOR_HEX + ";";
                String deleteStyle = "-fx-background-color: " + UIConstants.ERROR_COLOR_HEX + ";";
                
                viewBtn.setStyle(btnStyle);
                editBtn.setStyle(btnStyle + editStyle);
                deleteBtn.setStyle(btnStyle + deleteStyle);
                
                viewBtn.setOnAction(e -> viewGuestDetails(getTableView().getItems().get(getIndex())));
                editBtn.setOnAction(e -> showGuestFormDialog(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> deleteGuest(getTableView().getItems().get(getIndex())));
                
                actionBox.setAlignment(Pos.CENTER);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionBox);
            }
        });
        actionsCol.setPrefWidth(180);

        guestTable.getColumns().setAll(idCol, firstNameCol, lastNameCol, emailCol, phoneCol, dobCol, loyaltyCol, actionsCol);
        
        // Enhance table styling
        guestTable.setStyle(UIConstants.LUXURY_CARD_STYLE_CSS);
        try {
            URL cssUrl = getClass().getResource("/styles/table-styles.css");
            if (cssUrl != null) {
                guestTable.getStylesheets().add(cssUrl.toExternalForm());
                System.out.println("Successfully loaded table-styles.css");
            } else {
                System.err.println("Warning: Could not find /styles/table-styles.css resource");
            }
        } catch (Exception e) {
            System.err.println("Error loading CSS stylesheet: " + e.getMessage());
        }
        
        VBox.setVgrow(guestTable, Priority.ALWAYS);

        // Create styled action buttons
        Button addButton = new Button("Add Guest");
        addButton.setGraphic(createButtonIcon("plus-circle"));

        String addBtnStyle = UIConstants.LUXURY_BUTTON_STYLE;
        String addBtnHoverStyle = UIConstants.LUXURY_BUTTON_HOVER_STYLE;
        
        addButton.setStyle(addBtnStyle);
        addButton.setOnMouseEntered(e -> addButton.setStyle(addBtnStyle + addBtnHoverStyle));
        addButton.setOnMouseExited(e -> addButton.setStyle(addBtnStyle));
        addButton.setOnAction(e -> showGuestFormDialog(null));

        HBox buttonBar = new HBox(15, addButton);
        buttonBar.setAlignment(Pos.CENTER_LEFT);
        buttonBar.setPadding(new Insets(15, 0, 0, 0));

        layout.getChildren().addAll(titleBar, guestTable, buttonBar);
        return layout;
    }

    private Node createButtonIcon(String iconName) {
        // Placeholder for icon - in a real app, you'd use an actual icon library
        Region icon = new Region();
        icon.setStyle("-fx-background-color: white;");
        icon.setPrefSize(14, 14);
        return icon;
    }
    
    private void loadAllGuests() {
        // Get fresh data from database
        ObservableList<Guest> freshData = guestDAO.getAllGuests();
        guestObservableList.setAll(freshData);
        
        // Debug output
        System.out.println("Loaded " + freshData.size() + " guests from database");
    }
    
    private void filterGuestList(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            guestObservableList.setAll(guestDAO.getAllGuests());
            return;
        }
        
        searchText = searchText.toLowerCase();
        ObservableList<Guest> filteredList = FXCollections.observableArrayList();
        
        for (Guest guest : guestDAO.getAllGuests()) {
            if (guest.getFirstName().toLowerCase().contains(searchText) ||
                guest.getLastName().toLowerCase().contains(searchText) ||
                guest.getEmail().toLowerCase().contains(searchText) ||
                (guest.getPhoneNumber() != null && guest.getPhoneNumber().toLowerCase().contains(searchText))) {
                filteredList.add(guest);
            }
        }
        
        guestObservableList.setAll(filteredList);
    }
    
    private void animateRefresh() {
        // Add a subtle fade animation when refreshing data
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), guestTable);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.7);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), guestTable);
        fadeIn.setFromValue(0.7);
        fadeIn.setToValue(1.0);
        
        fadeOut.setOnFinished(e -> fadeIn.play());
        fadeOut.play();
    }
    
    private void viewGuestDetails(Guest guest) {
        if (guest == null) return;
        
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Guest Details");
        dialog.setHeaderText("Guest Information");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: " + UIConstants.ACCENT_COLOR_LIGHT_CREAM_HEX + ";");
        
        Text nameText = new Text(guest.getFirstName() + " " + guest.getLastName());
        nameText.setFont(Font.font(UIConstants.FONT_SERIF_ELEGANT, FontWeight.BOLD, 24));
        nameText.setFill(UIConstants.ACCENT_COLOR_DARK_BROWN_FX);
        
        content.getChildren().add(nameText);
        
        addDetailField(content, "Email:", guest.getEmail());
        addDetailField(content, "Phone:", guest.getPhoneNumber());
        addDetailField(content, "Address:", guest.getAddress());
        addDetailField(content, "Date of Birth:", guest.getDateOfBirth() != null ? guest.getDateOfBirth().toString() : "Not provided");
        addDetailField(content, "Nationality:", guest.getNationality());
        addDetailField(content, "ID Type:", guest.getIdentificationType());
        addDetailField(content, "ID Number:", guest.getIdentificationNumber());
        addDetailField(content, "Loyalty Program ID:", guest.getLoyaltyProgramID());
        
        // Add preferences with special styling
        if (guest.getPreferences() != null && !guest.getPreferences().isEmpty()) {
            Label prefLabel = new Label("Preferences:");
            prefLabel.setFont(Font.font(UIConstants.FONT_SERIF_ELEGANT, FontWeight.BOLD, 14));
            
            TextArea prefText = new TextArea(guest.getPreferences());
            prefText.setWrapText(true);
            prefText.setEditable(false);
            prefText.setPrefRowCount(3);
            prefText.setStyle("-fx-control-inner-background: white; -fx-border-color: " + 
                            UIConstants.GOLD_COLOR_HEX + "; -fx-border-width: 1; -fx-border-radius: 5;");
            
            content.getChildren().addAll(prefLabel, prefText);
        }
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500);
        
        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().setPrefWidth(600);
        
        // Style the dialog
        try {
            URL cssUrl = getClass().getResource("/styles/dialog-styles.css");
            if (cssUrl != null) {
                dialog.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
                System.out.println("Successfully loaded dialog-styles.css");
            } else {
                System.err.println("Warning: Could not find /styles/dialog-styles.css resource");
            }
        } catch (Exception e) {
            System.err.println("Error loading dialog CSS stylesheet: " + e.getMessage());
        }
        
        dialog.showAndWait();
    }
    
    private void addDetailField(VBox container, String label, String value) {
        if (value == null || value.isEmpty()) return;
        
        Label labelNode = new Label(label);
        labelNode.setFont(Font.font(UIConstants.FONT_SERIF_ELEGANT, FontWeight.BOLD, 14));
        
        Text valueNode = new Text(value);
        valueNode.setFont(Font.font(UIConstants.FONT_SANS_SERIF_CLEAN, 14));
        
        VBox fieldBox = new VBox(3, labelNode, valueNode);
        fieldBox.setPadding(new Insets(0, 0, 10, 0));
        container.getChildren().add(fieldBox);
    }
    
    private void deleteGuest(Guest guest) {
        if (guest == null) return;
        
        Optional<ButtonType> result = DialogUtil.showConfirmationDialog(mainApp.getPrimaryStage(),
            "Confirm Deletion", 
            "Delete Guest Record: " + guest.getFirstName() + " " + guest.getLastName() + "?",
            "This action cannot be undone and might affect existing reservations.");
            
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (guestDAO.deleteGuest(guest.getGuestID())) {
                guestObservableList.remove(guest);
                DialogUtil.showAlert(Alert.AlertType.INFORMATION, mainApp.getPrimaryStage(), 
                    "Success", "Guest record deleted successfully.");
            } else {
                DialogUtil.showAlert(Alert.AlertType.ERROR, mainApp.getPrimaryStage(), 
                    "Deletion Error", "Failed to delete guest. They may have associated records (e.g., reservations, invoices) that prevent deletion.");
            }
        }
    }

    private void showGuestFormDialog(Guest guestToEdit) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle(guestToEdit == null ? "Add New Guest" : "Edit Guest");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(mainApp.getPrimaryStage());
        
        VBox dialogRoot = new VBox(20);
        dialogRoot.setPadding(new Insets(30));
        dialogRoot.setStyle("-fx-background-color: linear-gradient(to bottom right, " + 
                          UIConstants.BRAND_COLOR_HEX + "99, " + UIConstants.ACCENT_COLOR_DARK_BROWN_HEX + "99); " + 
                          "-fx-background-radius: 10px;");
        
        // Add fancy header
        Text headerText = new Text(guestToEdit == null ? "Register New Guest" : "Update Guest Information");
        headerText.setFont(Font.font(UIConstants.FONT_SERIF_ELEGANT, FontWeight.BOLD, 26));
        headerText.setFill(Color.WHITE);
        headerText.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.5), 5, 0.2, 0, 1));
        
        // Create a luxurious form container
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white; -fx-background-radius: 8px; " + UIConstants.LUXURY_SHADOW_EFFECT_CSS);
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(25));
        grid.setStyle("-fx-background-color: white; -fx-background-radius: 8px;");
        scrollPane.setContent(grid);
        
        // Create stylish form fields
        TextField fnF = new TextField();
        fnF.setPromptText("First Name"); 
        fnF.setStyle(UIConstants.FIELD_STYLE_DIALOG_CSS);
        
        TextField lnF = new TextField(); 
        lnF.setPromptText("Last Name"); 
        lnF.setStyle(UIConstants.FIELD_STYLE_DIALOG_CSS);
        
        TextField emF = new TextField(); 
        emF.setPromptText("Email"); 
        emF.setStyle(UIConstants.FIELD_STYLE_DIALOG_CSS);
        
        TextField phF = new TextField(); 
        phF.setPromptText("Phone Number"); 
        phF.setStyle(UIConstants.FIELD_STYLE_DIALOG_CSS);
        
        TextArea adA = new TextArea(); 
        adA.setPromptText("Address"); 
        adA.setStyle(UIConstants.FIELD_STYLE_DIALOG_CSS);
        adA.setPrefRowCount(3);
        adA.setWrapText(true);
        
        TextField naF = new TextField(); 
        naF.setPromptText("Nationality"); 
        naF.setStyle(UIConstants.FIELD_STYLE_DIALOG_CSS);
        
        DatePicker dobP = new DatePicker(); 
        dobP.setPromptText("Date of Birth");
        dobP.setStyle(UIConstants.FIELD_STYLE_DIALOG_CSS);
        
        TextField idTF = new TextField(); 
        idTF.setPromptText("ID Type (Passport, etc.)"); 
        idTF.setStyle(UIConstants.FIELD_STYLE_DIALOG_CSS);
        
        TextField idNF = new TextField(); 
        idNF.setPromptText("ID Number"); 
        idNF.setStyle(UIConstants.FIELD_STYLE_DIALOG_CSS);
        
        TextField loyaltyF = new TextField(); 
        loyaltyF.setPromptText("Loyalty Program ID"); 
        loyaltyF.setStyle(UIConstants.FIELD_STYLE_DIALOG_CSS);
        
        TextArea prefA = new TextArea(); 
        prefA.setPromptText("Preferences"); 
        prefA.setStyle(UIConstants.FIELD_STYLE_DIALOG_CSS); 
        prefA.setPrefRowCount(3); 
        prefA.setWrapText(true);
        
        TextArea notesA = new TextArea(); 
        notesA.setPromptText("Internal Notes"); 
        notesA.setStyle(UIConstants.FIELD_STYLE_DIALOG_CSS); 
        notesA.setPrefRowCount(3); 
        notesA.setWrapText(true);

        if (guestToEdit != null) {
            fnF.setText(guestToEdit.getFirstName()); 
            lnF.setText(guestToEdit.getLastName()); 
            emF.setText(guestToEdit.getEmail());
            phF.setText(guestToEdit.getPhoneNumber()); 
            adA.setText(guestToEdit.getAddress()); 
            naF.setText(guestToEdit.getNationality());
            dobP.setValue(guestToEdit.getDateOfBirth()); 
            idTF.setText(guestToEdit.getIdentificationType());
            idNF.setText(guestToEdit.getIdentificationNumber()); 
            loyaltyF.setText(guestToEdit.getLoyaltyProgramID());
            prefA.setText(guestToEdit.getPreferences()); 
            notesA.setText(guestToEdit.getNotes());
        }

        // Create luxurious buttons
        Button saveButton = new Button(guestToEdit == null ? "Register Guest" : "Save Changes");
        saveButton.setStyle(UIConstants.LUXURY_BUTTON_STYLE);
        saveButton.setOnMouseEntered(e -> saveButton.setStyle(UIConstants.LUXURY_BUTTON_STYLE + UIConstants.LUXURY_BUTTON_HOVER_STYLE));
        saveButton.setOnMouseExited(e -> saveButton.setStyle(UIConstants.LUXURY_BUTTON_STYLE));

        saveButton.setOnAction(e -> {
            if (fnF.getText().trim().isEmpty() || lnF.getText().trim().isEmpty() || emF.getText().trim().isEmpty()){
                DialogUtil.showAlert(Alert.AlertType.ERROR, dialogStage, 
                    "Validation Error", "First Name, Last Name, and Email are mandatory.");
                return;
            }
            
            if (!emF.getText().trim().isEmpty() && (!emF.getText().contains("@") || !emF.getText().contains("."))) {
                DialogUtil.showAlert(Alert.AlertType.ERROR, dialogStage, 
                    "Validation Error", "Please enter a valid email address.");
                return;
            }

            Guest guestData = (guestToEdit == null) ? new Guest() : guestToEdit;
            guestData.setFirstName(fnF.getText().trim()); 
            guestData.setLastName(lnF.getText().trim()); 
            guestData.setEmail(emF.getText().trim());
            guestData.setPhoneNumber(phF.getText().trim()); 
            guestData.setAddress(adA.getText().trim()); 
            guestData.setNationality(naF.getText().trim());
            guestData.setDateOfBirth(dobP.getValue()); 
            guestData.setIdentificationType(idTF.getText().trim());
            guestData.setIdentificationNumber(idNF.getText().trim()); 
            guestData.setLoyaltyProgramID(loyaltyF.getText().trim());
            guestData.setPreferences(prefA.getText().trim()); 
            guestData.setNotes(notesA.getText().trim());

            boolean success;
            if (guestToEdit == null) {
                success = guestDAO.addGuest(guestData);
                if (success) {
                    // Load fresh data to get the generated ID
                    loadAllGuests();
                    // Select the newly added guest
                    guestTable.getSelectionModel().select(guestData);
                    guestTable.scrollTo(guestData);
                    
                    // Animation effect for new guest
                    animateNewGuestAdded();
                }
            } else {
                success = guestDAO.updateGuest(guestData);
                if (success) {
                    // Full refresh to ensure data consistency
                    loadAllGuests();
                }
            }

            if (success) {
                dialogStage.close();
                DialogUtil.showAlert(Alert.AlertType.INFORMATION, mainApp.getPrimaryStage(), 
                    "Success", "Guest information saved successfully.");
            } else {
                DialogUtil.showAlert(Alert.AlertType.ERROR, dialogStage, 
                    "Database Error", "Failed to save guest information. Email might already exist or another database issue occurred.");
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 1.5; -fx-border-radius: 30; -fx-padding: 10 20;");
        cancelButton.setOnMouseEntered(e -> cancelButton.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 1.5; -fx-border-radius: 30; -fx-padding: 10 20;"));
        cancelButton.setOnMouseExited(e -> cancelButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 1.5; -fx-border-radius: 30; -fx-padding: 10 20;"));
        cancelButton.setOnAction(e -> dialogStage.close());

        // Enhance field labels with elegant styling
        String labelStyle = "-fx-text-fill: " + UIConstants.ACCENT_COLOR_DARK_BROWN_HEX + "; -fx-font-family: '" + 
                          UIConstants.FONT_SERIF_ELEGANT + "'; -fx-font-weight: bold; -fx-font-size: 14px;";
        
        grid.add(new Label("First Name*"){{setStyle(labelStyle);}}, 0, 0);
        grid.add(fnF, 1, 0, 2, 1);
        grid.add(new Label("Last Name*"){{setStyle(labelStyle);}}, 0, 1);
        grid.add(lnF, 1, 1, 2, 1);
        grid.add(new Label("Email*"){{setStyle(labelStyle);}}, 0, 2);
        grid.add(emF, 1, 2, 2, 1);
        grid.add(new Label("Phone"){{setStyle(labelStyle);}}, 0, 3);
        grid.add(phF, 1, 3, 2, 1);
        grid.add(new Label("Address"){{setStyle(labelStyle);}}, 0, 4);
        grid.add(adA, 1, 4, 2, 1);
        grid.add(new Label("Nationality"){{setStyle(labelStyle);}}, 0, 5);
        grid.add(naF, 1, 5);
        grid.add(new Label("Loyalty ID"){{setStyle(labelStyle);}}, 2, 5);
        grid.add(loyaltyF, 3, 5);
        grid.add(new Label("Date of Birth"){{setStyle(labelStyle);}}, 0, 6);
        grid.add(dobP, 1, 6);
        grid.add(new Label("ID Type"){{setStyle(labelStyle);}}, 0, 7);
        grid.add(idTF, 1, 7);
        grid.add(new Label("ID Number"){{setStyle(labelStyle);}}, 2, 7);
        grid.add(idNF, 3, 7);
        grid.add(new Label("Preferences"){{setStyle(labelStyle);}}, 0, 8);
        grid.add(prefA, 1, 8, 3, 1);
        grid.add(new Label("Notes"){{setStyle(labelStyle);}}, 0, 9);
        grid.add(notesA, 1, 9, 3, 1);

        HBox buttonBar = new HBox(15, saveButton, cancelButton);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        
        dialogRoot.getChildren().addAll(headerText, scrollPane, buttonBar);
        Scene dialogScene = new Scene(dialogRoot, 750, 650);
        
        // Add elegant animations
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), dialogRoot);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
        
        dialogStage.setScene(dialogScene);
        dialogStage.showAndWait();
    }
    
    private void animateNewGuestAdded() {
        // Create an elegant pulse effect on the table
        ScaleTransition scale1 = new ScaleTransition(Duration.millis(150), guestTable);
        scale1.setFromX(1.0);
        scale1.setFromY(1.0);
        scale1.setToX(1.02);
        scale1.setToY(1.02);
        
        ScaleTransition scale2 = new ScaleTransition(Duration.millis(150), guestTable);
        scale2.setFromX(1.02);
        scale2.setFromY(1.02);
        scale2.setToX(1.0);
        scale2.setToY(1.0);
        
        scale1.setOnFinished(e -> scale2.play());
        scale1.play();
    }

    /**
     * Refresh the guest list to show the latest changes
     */
    public void refreshData() {
        loadAllGuests();
        animateRefresh();
        System.out.println("Guest data refreshed");
    }
    
    /**
     * Filter the guest list based on provided criteria
     * @param filter The search text to filter guests
     */
    public void filterData(String filter) {
        filterGuestList(filter);
    }
}