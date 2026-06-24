package com.example.demo10.raffles.hotelmgmt.controller;

import com.example.demo10.raffles.hotelmgmt.MainApp;
import com.example.demo10.raffles.hotelmgmt.dao.DepartmentDAO;
import com.example.demo10.raffles.hotelmgmt.dao.EmployeeDAO;
import com.example.demo10.raffles.hotelmgmt.dao.RoleDAO;
import com.example.demo10.raffles.hotelmgmt.model.Employee;
import com.example.demo10.raffles.hotelmgmt.ui.AuthUIFactory;
import com.example.demo10.raffles.hotelmgmt.ui.DialogUtil;
import com.example.demo10.raffles.hotelmgmt.ui.UIConstants;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.time.LocalDate;

public class SignUpController {
    private MainApp mainApp;
    private EmployeeDAO employeeDAO;
    private RoleDAO roleDAO;
    private DepartmentDAO departmentDAO;

    public SignUpController(MainApp mainApp) {
        this.mainApp = mainApp;
        this.employeeDAO = new EmployeeDAO();
        this.roleDAO = new RoleDAO();
        this.departmentDAO = new DepartmentDAO();
    }

    public Scene createSignUpScene() {
        HBox mainSplitPane = new HBox();
        StackPane leftPanelContainer = AuthUIFactory.createLeftAuthPanel();

        VBox rightFormPanel = new VBox(18);
        rightFormPanel.setPadding(new Insets(40, 70, 40, 70));
        rightFormPanel.setAlignment(Pos.CENTER);
        HBox.setHgrow(rightFormPanel, Priority.ALWAYS);
        rightFormPanel.setStyle("-fx-background-color: " + UIConstants.BRAND_COLOR_HEX + ";");

        Text signUpFormTitle = new Text("Create New Account");
        signUpFormTitle.setFont(Font.font(UIConstants.FONT_SERIF_ELEGANT, FontWeight.BOLD, 36));
        signUpFormTitle.setFill(Color.WHITE);
        VBox.setMargin(signUpFormTitle, new Insets(0, 0, 25, 0));

        double formElementWidth = 340;

        Label nameLabel = new Label("Full Name");
        nameLabel.setStyle(UIConstants.LABEL_STYLE_AUTH_CSS);
        TextField nameInput = new TextField();
        nameInput.setPromptText("Enter your full name");
        nameInput.setStyle(UIConstants.FIELD_STYLE_AUTH_CSS);
        nameInput.setPrefHeight(45); nameInput.setPrefWidth(formElementWidth); nameInput.setMaxWidth(formElementWidth);

        Label emailLabel = new Label("Email Address");
        emailLabel.setStyle(UIConstants.LABEL_STYLE_AUTH_CSS);
        TextField emailInput = new TextField();
        emailInput.setPromptText("Enter your email");
        emailInput.setStyle(UIConstants.FIELD_STYLE_AUTH_CSS);
        emailInput.setPrefHeight(45); emailInput.setPrefWidth(formElementWidth); emailInput.setMaxWidth(formElementWidth);

        Label usernameLabel = new Label("Username");
        usernameLabel.setStyle(UIConstants.LABEL_STYLE_AUTH_CSS);
        TextField usernameInput = new TextField();
        usernameInput.setPromptText("Choose a username");
        usernameInput.setStyle(UIConstants.FIELD_STYLE_AUTH_CSS);
        usernameInput.setPrefHeight(45); usernameInput.setPrefWidth(formElementWidth); usernameInput.setMaxWidth(formElementWidth);

        Label passLabel = new Label("Password");
        passLabel.setStyle(UIConstants.LABEL_STYLE_AUTH_CSS);
        PasswordField passInput = new PasswordField();
        passInput.setPromptText("Create a password (min 6 chars)");
        passInput.setStyle(UIConstants.FIELD_STYLE_AUTH_CSS);
        passInput.setPrefHeight(45); passInput.setPrefWidth(formElementWidth); passInput.setMaxWidth(formElementWidth);

        Button createAccountButton = new Button("CREATE ACCOUNT");
        createAccountButton.setPrefWidth(formElementWidth);
        createAccountButton.setPrefHeight(50);
        createAccountButton.setFont(Font.font(UIConstants.FONT_SANS_SERIF_CLEAN, FontWeight.BOLD, 16));
        String signUpBtnStyle = "-fx-background-color: #FFFFFF; -fx-text-fill: " + UIConstants.ACCENT_COLOR_DARK_BROWN_HEX + "; -fx-background-radius: 25px; -fx-padding: 10px 20px;" + UIConstants.SHADOW_EFFECT_CSS;
        String signUpBtnHoverStyle = "-fx-background-color: #F0F0F0; -fx-opacity: 0.95;";
        createAccountButton.setStyle(signUpBtnStyle);
        createAccountButton.setOnMouseEntered(e -> createAccountButton.setStyle(signUpBtnStyle + signUpBtnHoverStyle));
        createAccountButton.setOnMouseExited(e -> createAccountButton.setStyle(signUpBtnStyle));

        createAccountButton.setOnAction(e -> {
            String fullName = nameInput.getText().trim();
            String email = emailInput.getText().trim();
            String username = usernameInput.getText().trim();
            String password = passInput.getText();

            if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
                DialogUtil.showAlert(Alert.AlertType.ERROR, mainApp.getPrimaryStage(), "Sign Up Failed", "All fields are required."); return;
            }
            if (password.length() < 6) {
                DialogUtil.showAlert(Alert.AlertType.ERROR, mainApp.getPrimaryStage(), "Sign Up Failed", "Password must be at least 6 characters long."); return;
            }
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                DialogUtil.showAlert(Alert.AlertType.ERROR, mainApp.getPrimaryStage(), "Sign Up Failed", "Please enter a valid email address."); return;
            }

            String[] names = fullName.split(" ", 2);
            String firstName = names[0];
            String lastName = (names.length > 1) ? names[1] : "";

            int staffRoleId = roleDAO.getRoleIdByName("Staff");
            int frontDeskDeptId = departmentDAO.getDepartmentIdByName("Front Office");

            if (staffRoleId == -1) { DialogUtil.showAlert(Alert.AlertType.ERROR, mainApp.getPrimaryStage(), "Sign Up Error", "Default 'Staff' role not found. Admin setup needed."); return;}
            if (frontDeskDeptId == -1) { DialogUtil.showAlert(Alert.AlertType.ERROR, mainApp.getPrimaryStage(), "Sign Up Error", "Default 'Front Office' department not found. Admin setup needed."); return;}

            Employee newEmployee = new Employee();
            newEmployee.setFirstName(firstName); newEmployee.setLastName(lastName); newEmployee.setUsername(username);
            newEmployee.setPassword(password); // Set plain text password
            newEmployee.setEmail(email);
            newEmployee.setRoleID(staffRoleId); newEmployee.setDepartmentID(frontDeskDeptId);
            newEmployee.setHireDate(LocalDate.now()); newEmployee.setActive(true);

            if (employeeDAO.addEmployee(newEmployee)) { // DAO handles plain password
                DialogUtil.showAlert(Alert.AlertType.INFORMATION, mainApp.getPrimaryStage(), "Sign Up Successful", "Account created for " + firstName + ". You can now log in.");
                AuthUIFactory.pauseLogoAnimation();
                mainApp.switchToLogin();
            } else {
                DialogUtil.showAlert(Alert.AlertType.ERROR, mainApp.getPrimaryStage(), "Sign Up Failed", "Could not create account. Username or email might already exist.");
            }
        });

        Text haveAccountPrompt = new Text("Already have an account?");
        haveAccountPrompt.setStyle("-fx-fill: #E0E0E0; -fx-font-family: '" + UIConstants.FONT_SANS_SERIF_CLEAN + "'; -fx-font-size: 13px;");
        Hyperlink loginLink = new Hyperlink("Login Instead");
        loginLink.setStyle("-fx-text-fill: white; -fx-font-family: '" + UIConstants.FONT_SANS_SERIF_CLEAN + "'; -fx-font-weight: bold; -fx-font-size: 13px; -fx-border-color: transparent; -fx-underline: true;");
        loginLink.setOnAction(e -> { AuthUIFactory.pauseLogoAnimation(); mainApp.switchToLogin(); });
        HBox loginLinkBox = new HBox(5, haveAccountPrompt, loginLink);
        loginLinkBox.setAlignment(Pos.CENTER);
        VBox.setMargin(loginLinkBox, new Insets(20, 0, 15, 0));

        Button backToWelcomeBtn = new Button("Back to Welcome");
        String backBtnStyle = "-fx-background-color: transparent; -fx-text-fill: #E0E0E0; -fx-font-family:'" + UIConstants.FONT_SANS_SERIF_CLEAN + "'; -fx-font-size: 13px; -fx-border-color: #E0E0E0; -fx-border-width: 1px; -fx-border-radius: 20px; -fx-padding: 6px 18px;";
        String backBtnHoverStyle = "-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-border-color: white;";
        backToWelcomeBtn.setStyle(backBtnStyle);
        backToWelcomeBtn.setOnMouseEntered(e -> backToWelcomeBtn.setStyle(backBtnStyle + backBtnHoverStyle));
        backToWelcomeBtn.setOnMouseExited(e -> backToWelcomeBtn.setStyle(backBtnStyle));
        backToWelcomeBtn.setOnAction(e -> { AuthUIFactory.pauseLogoAnimation(); mainApp.switchToWelcome(); });

        VBox formElementsGroup = new VBox(10);
        formElementsGroup.setAlignment(Pos.CENTER);
        formElementsGroup.getChildren().addAll( nameLabel, nameInput, emailLabel, emailInput, usernameLabel, usernameInput, passLabel, passInput );
        formElementsGroup.setMaxWidth(formElementWidth);

        rightFormPanel.getChildren().addAll( signUpFormTitle, formElementsGroup, createAccountButton, loginLinkBox, new Region() {{ VBox.setVgrow(this, Priority.ALWAYS); }}, backToWelcomeBtn );
        VBox.setMargin(backToWelcomeBtn, new Insets(25,0,0,0));

        mainSplitPane.getChildren().addAll(leftPanelContainer, rightFormPanel);

        Scene scene = new Scene(mainSplitPane, UIConstants.INITIAL_WIDTH, UIConstants.INITIAL_HEIGHT);
        // scene.setOnShown removed from here, MainApp handles animation trigger
        return scene;
    }
}