package com.example.demo10.raffles.hotelmgmt.controller;

import com.example.demo10.raffles.hotelmgmt.MainApp;
import com.example.demo10.raffles.hotelmgmt.dao.EmployeeDAO;
import com.example.demo10.raffles.hotelmgmt.model.Employee;
import com.example.demo10.raffles.hotelmgmt.model.Guest;
import com.example.demo10.raffles.hotelmgmt.ui.AuthUIFactory;
import com.example.demo10.raffles.hotelmgmt.ui.DialogUtil;
import com.example.demo10.raffles.hotelmgmt.ui.UIConstants;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Optional;

public class LoginController {

    private MainApp mainApp;
    private EmployeeDAO employeeDAO;

    public LoginController(MainApp mainApp) {
        this.mainApp = mainApp;
        this.employeeDAO = new EmployeeDAO();
    }

    public Scene createLoginScene() {
        StackPane root = AuthUIFactory.createFormSceneLayout();

        // Create title section
        Node titleNode = AuthUIFactory.createTitleSection("Management Login", "Sign in to access Raffles Hotel Management System");

        VBox formBox = new VBox(20);
        formBox.setPadding(new Insets(30, 40, 35, 40));
        formBox.setMaxWidth(450);
        formBox.setAlignment(Pos.CENTER);

        // Create error message area (initially hidden)
        HBox errorBox = new HBox();
        errorBox.setAlignment(Pos.CENTER_LEFT);
        errorBox.setVisible(false);
        errorBox.setPadding(new Insets(10));
        errorBox.setStyle("-fx-background-color: #FFEBEE; -fx-background-radius: 5px; -fx-border-color: #FFCDD2; -fx-border-radius: 5px;");
        Text errorText = new Text();
        errorText.setFill(Color.web("#D32F2F"));
        errorText.setFont(Font.font(UIConstants.FONT_SANS_SERIF_CLEAN, 14));
        errorBox.getChildren().add(errorText);

        // Create form fields
        Label usernameLabel = new Label("Username");
        usernameLabel.setStyle(UIConstants.LABEL_STYLE_AUTH_CSS);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setStyle(UIConstants.FIELD_STYLE_AUTH_CSS);

        Label passwordLabel = new Label("Password");
        passwordLabel.setStyle(UIConstants.LABEL_STYLE_AUTH_CSS);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setStyle(UIConstants.FIELD_STYLE_AUTH_CSS);

        // Create login button
        Button loginButton = new Button("LOGIN");
        loginButton.setPrefWidth(200);
        loginButton.setPrefHeight(40);
        loginButton.setStyle("-fx-background-color: rgba(255,255,255,0.25); -fx-text-fill: white; -fx-font-family: '" + UIConstants.FONT_SANS_SERIF_CLEAN + "'; -fx-font-size: 16px; -fx-font-weight: bold;");
        loginButton.setOnAction(e -> {
            if (validateForm(usernameField.getText(), passwordField.getText())) {
                // Attempt to login
                tryLogin(usernameField.getText(), passwordField.getText(), errorBox, errorText);
            }
        });

        // Create registration link
        HBox signupBox = new HBox();
        signupBox.setAlignment(Pos.CENTER);
        Text signupText1 = new Text("New employee? ");
        signupText1.setFill(Color.web("#CCCCCC"));
        signupText1.setFont(Font.font(UIConstants.FONT_SANS_SERIF_CLEAN, 14));

        Text signupText2 = new Text("Register here");
        signupText2.setFill(Color.web("#FFFFFF"));
        signupText2.setFont(Font.font(UIConstants.FONT_SANS_SERIF_CLEAN, FontWeight.BOLD, 14));
        signupText2.setUnderline(true);
        signupText2.setOnMouseClicked(e -> mainApp.switchToSignUp());
        signupText2.setStyle("-fx-cursor: hand;");

        signupBox.getChildren().addAll(signupText1, signupText2);
        
        // Add guest access button
        HBox guestAccessBox = new HBox();
        guestAccessBox.setAlignment(Pos.CENTER);
        guestAccessBox.setPadding(new Insets(20, 0, 0, 0));
        
        Button guestLoginBtn = new Button("I'M A GUEST");
        guestLoginBtn.setStyle("-fx-background-color: rgba(150,120,80,0.5); -fx-text-fill: white; -fx-font-family: '" + 
                             UIConstants.FONT_SERIF_ELEGANT + "'; -fx-font-size: 14px; -fx-padding: 10 20; -fx-border-color: white; -fx-border-width: 1; -fx-border-radius: 5;");
        guestLoginBtn.setOnAction(e -> showGuestLoginDialog());
        
        guestAccessBox.getChildren().add(guestLoginBtn);

        // Add welcome back animation if needed
        VBox welcomeBackBox = new VBox(15);
        welcomeBackBox.setAlignment(Pos.CENTER);
        HBox iconBox = new HBox();
        iconBox.setAlignment(Pos.CENTER);
        Text welcomeText = new Text("Welcome to Raffles");
        welcomeText.setFill(Color.WHITE);
        welcomeText.setFont(Font.font(UIConstants.FONT_SERIF_ELEGANT, 20));
        welcomeBackBox.getChildren().addAll(iconBox, welcomeText);
        welcomeBackBox.setOpacity(0);

        // Add everything to form
        formBox.getChildren().addAll(
                errorBox,
                usernameLabel,
                usernameField,
                passwordLabel,
                passwordField,
                loginButton,
                signupBox,
                guestAccessBox);

        VBox contentBox = new VBox(40);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.getChildren().addAll(titleNode, formBox);

        // Add to root
        root.getChildren().add(contentBox);
        StackPane.setAlignment(contentBox, Pos.CENTER);

        // Setup key event handling for form fields
        Scene scene = new Scene(root);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                if (validateForm(usernameField.getText(), passwordField.getText())) {
                    tryLogin(usernameField.getText(), passwordField.getText(), errorBox, errorText);
                }
            }
        });

        return scene;
    }
    
    /**
     * Shows a guest login dialog allowing guests to access the self-service portal
     */
    private void showGuestLoginDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Guest Access");
        dialog.setHeaderText("Guest Portal Access");
        
        // Set the button types
        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);
        
        // Create the email and password labels and fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        
        grid.add(new Label("Email:"), 0, 0);
        grid.add(emailField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        
        // Add registration link
        Hyperlink registerLink = new Hyperlink("New Guest? Register Here");
        registerLink.setOnAction(e -> {
            dialog.close();
            showGuestRegistrationDialog();
        });
        grid.add(registerLink, 1, 2);
        
        // Enable/Disable login button depending on whether a email was entered
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);
        
        // Validation listener
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the email field by default
        Platform.runLater(emailField::requestFocus);
        
        // Convert the result to the guest login data when the login button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                try {
                    // Create GuestAccountDAO
                    com.example.demo10.raffles.hotelmgmt.dao.GuestAccountDAO guestAccountDAO = 
                        new com.example.demo10.raffles.hotelmgmt.dao.GuestAccountDAO();
                    
                    // Create GuestDAO
                    com.example.demo10.raffles.hotelmgmt.dao.GuestDAO guestDAO = 
                        new com.example.demo10.raffles.hotelmgmt.dao.GuestDAO();
                    
                    // Validate login credentials against database
                    java.util.Optional<com.example.demo10.raffles.hotelmgmt.model.GuestAccount> accountOpt = 
                        guestAccountDAO.validateLogin(emailField.getText(), passwordField.getText());
                    
                    if (accountOpt.isPresent()) {
                        com.example.demo10.raffles.hotelmgmt.model.GuestAccount account = accountOpt.get();
                        
                        // Get the guest details
                        java.util.Optional<Guest> guestOpt = guestDAO.getGuestById(account.getGuestId());
                        
                        if (guestOpt.isPresent()) {
                            Guest guestAccount = guestOpt.get();
                            
                            // Record successful login
                            guestAccountDAO.recordLogin(account.getAccountId());
                            
                            // Switch to the guest portal
                            mainApp.switchToGuestPortal(guestAccount);
                        } else {
                            DialogUtil.showAlert(Alert.AlertType.ERROR, mainApp.getPrimaryStage(),
                                "Account Error", "Guest profile not found. Please contact support.");
                        }
                    } else {
                        DialogUtil.showAlert(Alert.AlertType.ERROR, mainApp.getPrimaryStage(),
                            "Login Failed", "Invalid email or password. Please try again.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    DialogUtil.showAlert(Alert.AlertType.ERROR, mainApp.getPrimaryStage(),
                        "Login Error", "An error occurred during login: " + e.getMessage());
                }
            }
            return null;
        });
        
        dialog.showAndWait();
    }
    
    /**
     * Shows a dialog for new guests to register for the portal using simple dialogs
     * that should bypass any event handling or focus issues
     */
    private void showGuestRegistrationDialog() {
        // Use a series of simple dialogs to collect information
        
        // STEP 1: Create the Guest object and start collecting information
        Guest newGuest = new Guest();
        
        // STEP 2: First Name
        TextInputDialog firstNameDialog = new TextInputDialog();
        firstNameDialog.setTitle("Guest Registration");
        firstNameDialog.setHeaderText("Enter Your Information");
        firstNameDialog.setContentText("First Name:");
        
        Optional<String> firstNameResult = firstNameDialog.showAndWait();
        if (firstNameResult.isEmpty() || firstNameResult.get().trim().isEmpty()) {
            return; // User cancelled or left empty
        }
        String firstName = firstNameResult.get().trim();
        newGuest.setFirstName(firstName);
        
        // STEP 3: Last Name
        TextInputDialog lastNameDialog = new TextInputDialog();
        lastNameDialog.setTitle("Guest Registration");
        lastNameDialog.setHeaderText("Enter Your Information");
        lastNameDialog.setContentText("Last Name:");
        
        Optional<String> lastNameResult = lastNameDialog.showAndWait();
        if (lastNameResult.isEmpty() || lastNameResult.get().trim().isEmpty()) {
            return; // User cancelled or left empty
        }
        String lastName = lastNameResult.get().trim();
        newGuest.setLastName(lastName);
        
        // STEP 4: Email
        TextInputDialog emailDialog = new TextInputDialog();
        emailDialog.setTitle("Guest Registration");
        emailDialog.setHeaderText("Enter Your Information");
        emailDialog.setContentText("Email Address:");
        
        Optional<String> emailResult = emailDialog.showAndWait();
        if (emailResult.isEmpty() || emailResult.get().trim().isEmpty()) {
            return; // User cancelled or left empty
        }
        String email = emailResult.get().trim();
        
        if (!email.contains("@") || !email.contains(".")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Email");
            alert.setHeaderText(null);
            alert.setContentText("Please enter a valid email address.");
            alert.showAndWait();
            return;
        }
        newGuest.setEmail(email);
        
        // STEP 5: Phone Number
        TextInputDialog phoneDialog = new TextInputDialog();
        phoneDialog.setTitle("Guest Registration");
        phoneDialog.setHeaderText("Enter Your Information");
        phoneDialog.setContentText("Phone Number:");
        
        Optional<String> phoneResult = phoneDialog.showAndWait();
        if (phoneResult.isPresent()) {
            String phone = phoneResult.get().trim();
            newGuest.setPhoneNumber(phone);
        }
        
        // STEP 6: Password (using a custom dialog since TextInputDialog doesn't support password fields)
        Dialog<String> passwordDialog = new Dialog<>();
        passwordDialog.setTitle("Guest Registration");
        passwordDialog.setHeaderText("Create Password");
        
        ButtonType confirmPasswordButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        passwordDialog.getDialogPane().getButtonTypes().addAll(confirmPasswordButton, ButtonType.CANCEL);
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password (at least 6 characters)");
        
        HBox passwordContent = new HBox();
        passwordContent.setAlignment(Pos.CENTER_LEFT);
        passwordContent.setSpacing(10);
        passwordContent.getChildren().addAll(new Label("Password:"), passwordField);
        passwordContent.setPadding(new Insets(20));
        
        passwordDialog.getDialogPane().setContent(passwordContent);
        
        passwordDialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmPasswordButton) {
                return passwordField.getText();
            }
            return null;
        });
        
        Optional<String> passwordResult = passwordDialog.showAndWait();
        if (passwordResult.isEmpty() || passwordResult.get().length() < 6) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Password");
            alert.setHeaderText(null);
            alert.setContentText("Password must be at least 6 characters long.");
            alert.showAndWait();
            return;
        }
        String password = passwordResult.get();
        
        // STEP 7: Confirm Password
        Dialog<String> confirmDialog = new Dialog<>();
        confirmDialog.setTitle("Guest Registration");
        confirmDialog.setHeaderText("Confirm Password");
        
        ButtonType confirmButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        confirmDialog.getDialogPane().getButtonTypes().addAll(confirmButton, ButtonType.CANCEL);
        
        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm your password");
        
        HBox confirmContent = new HBox();
        confirmContent.setAlignment(Pos.CENTER_LEFT);
        confirmContent.setSpacing(10);
        confirmContent.getChildren().addAll(new Label("Confirm Password:"), confirmField);
        confirmContent.setPadding(new Insets(20));
        
        confirmDialog.getDialogPane().setContent(confirmContent);
        
        confirmDialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButton) {
                return confirmField.getText();
            }
            return null;
        });
        
        Optional<String> confirmResult = confirmDialog.showAndWait();
        if (confirmResult.isEmpty() || !confirmResult.get().equals(password)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Password Mismatch");
            alert.setHeaderText(null);
            alert.setContentText("Passwords do not match.");
            alert.showAndWait();
            return;
        }
        
        // STEP 8: Terms and Conditions
        Alert termsAlert = new Alert(Alert.AlertType.CONFIRMATION);
        termsAlert.setTitle("Guest Registration");
        termsAlert.setHeaderText("Terms and Conditions");
        termsAlert.setContentText("Do you agree to the terms and conditions?");
        
        Optional<ButtonType> termsResult = termsAlert.showAndWait();
        if (termsResult.isEmpty() || termsResult.get() != ButtonType.OK) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Registration Cancelled");
            alert.setHeaderText(null);
            alert.setContentText("You must agree to the terms and conditions to register.");
            alert.showAndWait();
            return;
        }
        
        // STEP 9: Process Registration
        try {
            // Create DAOs
            com.example.demo10.raffles.hotelmgmt.dao.GuestDAO guestDAO = 
                new com.example.demo10.raffles.hotelmgmt.dao.GuestDAO();
            com.example.demo10.raffles.hotelmgmt.dao.GuestAccountDAO guestAccountDAO = 
                new com.example.demo10.raffles.hotelmgmt.dao.GuestAccountDAO();
            
            // Ensure the guest account table exists
            guestAccountDAO.initTable();
            
            // Save guest to database
            boolean guestCreated = guestDAO.addGuest(newGuest);
            
            if (guestCreated && newGuest.getGuestID() > 0) {
                // Create guest account for portal access
                com.example.demo10.raffles.hotelmgmt.model.GuestAccount guestAccount = 
                    new com.example.demo10.raffles.hotelmgmt.model.GuestAccount(
                        email,
                        password,
                        newGuest.getGuestID()
                    );
                
                boolean accountCreated = guestAccountDAO.addGuestAccount(guestAccount);
                
                if (accountCreated) {
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Registration Successful");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Welcome to Raffles Hotels & Resorts, " + 
                        firstName + "! Your account has been created.");
                    successAlert.showAndWait();
                    
                    // Switch to guest portal
                    mainApp.switchToGuestPortal(newGuest);
                } else {
                    Alert failAlert = new Alert(Alert.AlertType.ERROR);
                    failAlert.setTitle("Registration Failed");
                    failAlert.setHeaderText(null);
                    failAlert.setContentText("Failed to create portal access. Please try again.");
                    failAlert.showAndWait();
                }
            } else {
                Alert failAlert = new Alert(Alert.AlertType.ERROR);
                failAlert.setTitle("Registration Failed");
                failAlert.setHeaderText(null);
                failAlert.setContentText("Failed to register. This email may already be registered.");
                failAlert.showAndWait();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Registration Error");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Error: " + ex.getMessage());
            errorAlert.showAndWait();
        }
    }

    private boolean validateForm(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            DialogUtil.showAlert(Alert.AlertType.ERROR, mainApp.getPrimaryStage(), "Login Failed", "Username and password cannot be empty.");
            return false;
        }
        return true;
    }

    private void tryLogin(String username, String password, HBox errorBox, Text errorText) {
        Employee employee = employeeDAO.getEmployeeByUsername(username);

        if (employee != null && employee.getPassword().equals(password)) {
            if (!employee.isActive()){
                DialogUtil.showAlert(Alert.AlertType.ERROR, mainApp.getPrimaryStage(), "Login Failed", "Your account is inactive. Please contact an administrator.");
                return;
            }
            mainApp.setCurrentLoggedInUser(employee);
            System.out.println("Login successful for: " + employee.getUsername() + " with Role: " + employee.getRoleName());
            AuthUIFactory.pauseLogoAnimation();
            mainApp.switchToDashboard();
        } else {
            errorBox.setVisible(true);
            errorText.setText("Invalid username or password.");
        }
    }
}