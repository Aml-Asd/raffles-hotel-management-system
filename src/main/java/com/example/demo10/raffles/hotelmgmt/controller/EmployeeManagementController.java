package com.example.demo10.raffles.hotelmgmt.controller;

import com.example.demo10.raffles.hotelmgmt.MainApp;
// No PasswordUtil
import com.example.demo10.raffles.hotelmgmt.dao.DepartmentDAO;
import com.example.demo10.raffles.hotelmgmt.dao.EmployeeDAO;
import com.example.demo10.raffles.hotelmgmt.dao.RoleDAO;
import com.example.demo10.raffles.hotelmgmt.model.Department;
import com.example.demo10.raffles.hotelmgmt.model.Employee;
import com.example.demo10.raffles.hotelmgmt.model.Role;
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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;

public class EmployeeManagementController {
    private MainApp mainApp;
    private EmployeeDAO employeeDAO;
    private RoleDAO roleDAO;
    private DepartmentDAO departmentDAO;
    private ObservableList<Employee> employeeObservableList;
    private TableView<Employee> employeeTable;

    public EmployeeManagementController(MainApp mainApp, EmployeeDAO employeeDAO, RoleDAO roleDAO, DepartmentDAO departmentDAO) {
        this.mainApp = mainApp;
        this.employeeDAO = employeeDAO;
        this.roleDAO = roleDAO;
        this.departmentDAO = departmentDAO;
        this.employeeObservableList = FXCollections.observableArrayList();
    }

    public Node createEmployeeManagementPane() {
        employeeObservableList.setAll(employeeDAO.getAllEmployees());

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30, 40, 30, 40));

        HBox titleBar = new HBox(15);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        Text title = new Text("Employee Registry");
        title.setFont(Font.font(UIConstants.FONT_SERIF_ELEGANT, FontWeight.BOLD, 28));
        title.setFill(UIConstants.ACCENT_COLOR_DARK_BROWN_FX);
        Region titleSpacer = new Region(); HBox.setHgrow(titleSpacer, Priority.ALWAYS);
        Button refreshButton = new Button("Refresh List");
        String refreshBtnStyle = "-fx-background-color: transparent; -fx-text-fill: " + UIConstants.BRAND_COLOR_HEX + "; -fx-font-family: '" + UIConstants.FONT_SANS_SERIF_CLEAN + "'; -fx-font-size: 14px; -fx-padding: 8px 15px; -fx-border-color: " + UIConstants.BRAND_COLOR_HEX + "; -fx-border-width: 1px; -fx-border-radius: 5px;";
        refreshButton.setStyle(refreshBtnStyle);
        refreshButton.setOnAction(e -> employeeObservableList.setAll(employeeDAO.getAllEmployees()));
        titleBar.getChildren().addAll(title, titleSpacer, refreshButton);

        employeeTable = new TableView<>(employeeObservableList);
        employeeTable.setPlaceholder(new Label("No employees found. Use 'Add New Employee' to create one."));
        try {
            URL cssUrl = getClass().getResource("/styles/tableview.css");
            if (cssUrl != null) employeeTable.getStylesheets().add(cssUrl.toExternalForm());
            else System.err.println("Warning: /styles/tableview.css not found for Employee Table.");
        } catch (Exception e) { System.err.println("Error loading /styles/tableview.css for Employee Table: " + e.getMessage()); }

        TableColumn<Employee, Integer> idCol = new TableColumn<>("ID"); idCol.setCellValueFactory(new PropertyValueFactory<>("employeeID")); idCol.setPrefWidth(50); idCol.setStyle("-fx-alignment: CENTER;");
        TableColumn<Employee, String> nameCol = new TableColumn<>("Name"); nameCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFirstName() + " " + cellData.getValue().getLastName())); nameCol.setPrefWidth(180);
        TableColumn<Employee, String> usernameCol = new TableColumn<>("Username"); usernameCol.setCellValueFactory(new PropertyValueFactory<>("username")); usernameCol.setPrefWidth(120);
        TableColumn<Employee, String> emailCol = new TableColumn<>("Email"); emailCol.setCellValueFactory(new PropertyValueFactory<>("email")); emailCol.setPrefWidth(200);
        TableColumn<Employee, String> roleCol = new TableColumn<>("Role"); roleCol.setCellValueFactory(new PropertyValueFactory<>("roleName")); roleCol.setPrefWidth(100);
        TableColumn<Employee, String> deptCol = new TableColumn<>("Department"); deptCol.setCellValueFactory(new PropertyValueFactory<>("departmentName")); deptCol.setPrefWidth(120);
        TableColumn<Employee, Boolean> activeCol = new TableColumn<>("Active"); activeCol.setCellValueFactory(new PropertyValueFactory<>("active")); activeCol.setPrefWidth(70);
        activeCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle("");}
                else { setText(item ? "Yes" : "No"); setStyle(item ? "-fx-text-fill: " + UIConstants.SUCCESS_COLOR_HEX + ";" : "-fx-text-fill: " + UIConstants.ERROR_COLOR_HEX + ";");}
            }
        });
        employeeTable.getColumns().setAll(idCol, nameCol, usernameCol, emailCol, roleCol, deptCol, activeCol);
        VBox.setVgrow(employeeTable, Priority.ALWAYS);
        employeeTable.setStyle(UIConstants.SHADOW_EFFECT_CSS + "-fx-background-color: white; -fx-background-radius: 8px;");

        Button addButton = new Button("Add New Employee");
        Button editButton = new Button("Edit Selected");
        Button deleteButton = new Button("Delete Selected");

        String crudBtnStyle = "-fx-background-color: " + UIConstants.BRAND_COLOR_HEX + "; -fx-text-fill: white; -fx-font-family: '"+UIConstants.FONT_SANS_SERIF_CLEAN+"'; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-radius: 5px;" + UIConstants.SHADOW_EFFECT_CSS;
        String crudBtnHover = "-fx-background-color: " + UIConstants.ACCENT_COLOR_DARK_BROWN_HEX + ";";
        String deleteBtnStyle = "-fx-background-color: " + UIConstants.ERROR_COLOR_HEX + "; -fx-text-fill: white; -fx-font-family: '"+UIConstants.FONT_SANS_SERIF_CLEAN+"'; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-radius: 5px;" + UIConstants.SHADOW_EFFECT_CSS;
        String deleteBtnHover = "-fx-background-color: #A83939;";

        addButton.setStyle(crudBtnStyle); editButton.setStyle(crudBtnStyle); deleteButton.setStyle(deleteBtnStyle);
        addButton.setOnMouseEntered(e->addButton.setStyle(crudBtnStyle+crudBtnHover)); addButton.setOnMouseExited(e->addButton.setStyle(crudBtnStyle));
        editButton.setOnMouseEntered(e->editButton.setStyle(crudBtnStyle+crudBtnHover)); editButton.setOnMouseExited(e->editButton.setStyle(crudBtnStyle));
        deleteButton.setOnMouseEntered(e->deleteButton.setStyle(deleteBtnStyle+deleteBtnHover)); deleteButton.setOnMouseExited(e->deleteButton.setStyle(deleteBtnStyle));

        addButton.setOnAction(e -> showEmployeeFormDialog(null));
        editButton.setOnAction(e -> { Employee selected = employeeTable.getSelectionModel().getSelectedItem(); if (selected != null) showEmployeeFormDialog(selected); else DialogUtil.showAlert(Alert.AlertType.WARNING, mainApp.getPrimaryStage(), "No Selection", "Please select an employee to edit.");});
        deleteButton.setOnAction(e -> {
            Employee selected = employeeTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if ("admin".equalsIgnoreCase(selected.getUsername())) {
                    DialogUtil.showAlert(Alert.AlertType.WARNING, mainApp.getPrimaryStage(), "Action Denied", "The default 'admin' user cannot be deleted.");
                    return;
                }
                Optional<ButtonType> result = DialogUtil.showConfirmationDialog(mainApp.getPrimaryStage(),
                        "Confirm Deletion", "Delete Employee: " + selected.getFirstName() + " " + selected.getLastName() + "?",
                        "This action cannot be undone. Associated records might be affected.");
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    if (employeeDAO.deleteEmployee(selected.getEmployeeID())) {
                        employeeObservableList.remove(selected);
                        DialogUtil.showAlert(Alert.AlertType.INFORMATION, mainApp.getPrimaryStage(), "Success", "Employee record deleted successfully.");
                    } else {
                        DialogUtil.showAlert(Alert.AlertType.ERROR, mainApp.getPrimaryStage(), "Deletion Error", "Failed to delete employee. They might be referenced in other records.");
                    }
                }
            } else {
                DialogUtil.showAlert(Alert.AlertType.WARNING, mainApp.getPrimaryStage(), "No Selection", "Please select an employee to delete.");
            }
        });

        HBox buttonBar = new HBox(15, addButton, editButton, deleteButton);
        buttonBar.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(buttonBar, new Insets(10,0,0,0));

        layout.getChildren().addAll(titleBar, employeeTable, buttonBar);
        return layout;
    }

    private void showEmployeeFormDialog(Employee empToEdit) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(mainApp.getPrimaryStage());
        dialogStage.setTitle(empToEdit == null ? "Add New Employee" : "Edit Employee: " + empToEdit.getFirstName() + " " + empToEdit.getLastName());

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(25)); grid.setHgap(15); grid.setVgap(15);
        grid.setStyle("-fx-background-color: " + UIConstants.ACCENT_COLOR_LIGHT_CREAM_HEX + ";");

        TextField fnF = new TextField(); fnF.setPromptText("First Name*"); fnF.setStyle(UIConstants.FIELD_STYLE_DIALOG_CSS);
        TextField lnF = new TextField(); lnF.setPromptText("Last Name*"); lnF.setStyle(UIConstants.FIELD_STYLE_DIALOG_CSS);
        TextField unF = new TextField(); unF.setPromptText("Username*"); unF.setStyle(UIConstants.FIELD_STYLE_DIALOG_CSS);
        PasswordField pwF = new PasswordField(); pwF.setPromptText(empToEdit == null ? "Password* (min 6)" : "New Password (leave blank to keep old)"); pwF.setStyle(UIConstants.FIELD_STYLE_DIALOG_CSS);
        TextField emF = new TextField(); emF.setPromptText("Email Address*"); emF.setStyle(UIConstants.FIELD_STYLE_DIALOG_CSS);
        TextField phF = new TextField(); phF.setPromptText("Contact Number"); phF.setStyle(UIConstants.FIELD_STYLE_DIALOG_CSS);
        ComboBox<Role> roleCB = new ComboBox<>(roleDAO.getAllRoles()); roleCB.setPromptText("Select Role*"); roleCB.setStyle(UIConstants.FIELD_STYLE_DIALOG_CSS + "-fx-pref-width: 220px;");
        ComboBox<Department> deptCB = new ComboBox<>(departmentDAO.getAllDepartments()); deptCB.setPromptText("Select Department*"); deptCB.setStyle(UIConstants.FIELD_STYLE_DIALOG_CSS + "-fx-pref-width: 220px;");
        DatePicker hireDP = new DatePicker(LocalDate.now()); hireDP.setPromptText("Hire Date"); hireDP.setStyle(UIConstants.FIELD_STYLE_DIALOG_CSS + "-fx-pref-width: 220px;");
        CheckBox activeCB = new CheckBox("Is Active"); activeCB.setSelected(true); activeCB.setStyle(UIConstants.LABEL_STYLE_DIALOG_CSS);

        if (empToEdit != null) {
            fnF.setText(empToEdit.getFirstName()); lnF.setText(empToEdit.getLastName()); unF.setText(empToEdit.getUsername());
            emF.setText(empToEdit.getEmail()); phF.setText(empToEdit.getContactNumber());
            // Select current role in ComboBox
            roleDAO.getAllRoles().stream().filter(r -> empToEdit.getRoleID() != null && r.getRoleID() == empToEdit.getRoleID()).findFirst().ifPresent(roleCB::setValue);
            // Select current department
            departmentDAO.getAllDepartments().stream().filter(d -> empToEdit.getDepartmentID() != null && d.getDepartmentID() == empToEdit.getDepartmentID()).findFirst().ifPresent(deptCB::setValue);
            hireDP.setValue(empToEdit.getHireDate());
            activeCB.setSelected(empToEdit.isActive());
            if("admin".equalsIgnoreCase(empToEdit.getUsername())) {
                unF.setDisable(true); // Cannot change username of default admin
                roleCB.setDisable(true); // Prevent changing role of default admin easily
            }
        }

        Button saveButton = new Button(empToEdit == null ? "Add Employee" : "Save Changes");
        String saveBtnStl = "-fx-background-color: " + UIConstants.BRAND_COLOR_HEX + "; -fx-text-fill: white; -fx-font-family:'"+UIConstants.FONT_SANS_SERIF_CLEAN+"'; -fx-font-weight:bold; -fx-padding: 10px 22px; -fx-background-radius: 5px; -fx-font-size: 15px;" + UIConstants.SHADOW_EFFECT_CSS;
        String saveBtnHov = "-fx-background-color: " + UIConstants.ACCENT_COLOR_DARK_BROWN_HEX + ";";
        saveButton.setStyle(saveBtnStl);
        saveButton.setOnMouseEntered(e->saveButton.setStyle(saveBtnStl+saveBtnHov));
        saveButton.setOnMouseExited(e->saveButton.setStyle(saveBtnStl));

        saveButton.setOnAction(e -> {
            if (fnF.getText().trim().isEmpty() || lnF.getText().trim().isEmpty() || unF.getText().trim().isEmpty() ||
                    emF.getText().trim().isEmpty() || roleCB.getValue() == null || deptCB.getValue() == null ||
                    (empToEdit == null && pwF.getText().isEmpty())) {
                DialogUtil.showAlert(Alert.AlertType.ERROR, dialogStage, "Validation Error", "All fields marked * are mandatory. Password required for new employee."); return;
            }
            if ((empToEdit == null || !pwF.getText().isEmpty()) && pwF.getText().length() < 6) { // Check password length only if it's new or being changed
                DialogUtil.showAlert(Alert.AlertType.ERROR, dialogStage, "Validation Error", "Password must be at least 6 characters."); return;
            }
            if (!emF.getText().trim().isEmpty() && (!emF.getText().trim().matches("^[A-Za-z0-9+_.-]+@(.+)$"))) {
                DialogUtil.showAlert(Alert.AlertType.ERROR, dialogStage, "Validation Error", "Please enter a valid email address."); return;
            }

            Employee empData = (empToEdit == null) ? new Employee() : empToEdit;
            empData.setFirstName(fnF.getText().trim());
            empData.setLastName(lnF.getText().trim());
            if (!unF.isDisabled()) empData.setUsername(unF.getText().trim()); // Only update if not disabled
            empData.setEmail(emF.getText().trim());
            empData.setContactNumber(phF.getText().trim());
            empData.setRoleID(roleCB.getValue().getRoleID());
            empData.setDepartmentID(deptCB.getValue().getDepartmentID());
            empData.setHireDate(hireDP.getValue() == null ? LocalDate.now() : hireDP.getValue());
            empData.setActive(activeCB.isSelected());

            boolean success;
            String plainPasswordFromField = pwF.getText();

            if (empToEdit == null) { // Adding new employee
                empData.setPassword(plainPasswordFromField); // Set plain password
                success = employeeDAO.addEmployee(empData);
            } else { // Updating existing employee
                if (!plainPasswordFromField.isEmpty()) {
                    empData.setPassword(plainPasswordFromField); // Update with new plain password
                } else {
                    empData.setPassword(empToEdit.getPassword()); // Keep old plain password if field is empty
                }
                success = employeeDAO.updateEmployee(empData);
            }

            if (success) {
                employeeObservableList.setAll(employeeDAO.getAllEmployees()); // Refresh table
                dialogStage.close();
                DialogUtil.showAlert(Alert.AlertType.INFORMATION, mainApp.getPrimaryStage(), "Success", "Employee information saved successfully.");
            } else {
                DialogUtil.showAlert(Alert.AlertType.ERROR, dialogStage, "Database Error", "Failed to save employee. Username or Email might already exist, or another database issue occurred.");
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> dialogStage.close());
        cancelButton.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: #444444; -fx-font-family:'"+UIConstants.FONT_SANS_SERIF_CLEAN+"'; -fx-padding: 10px 22px; -fx-background-radius: 5px; -fx-font-size: 15px;");

        grid.add(new Label("First Name*:"){{setStyle(UIConstants.LABEL_STYLE_DIALOG_CSS);}},0,0); grid.add(fnF,1,0);
        grid.add(new Label("Last Name*:"){{setStyle(UIConstants.LABEL_STYLE_DIALOG_CSS);}},2,0); grid.add(lnF,3,0);
        grid.add(new Label("Username*:"){{setStyle(UIConstants.LABEL_STYLE_DIALOG_CSS);}},0,1); grid.add(unF,1,1);
        grid.add(new Label(empToEdit == null ? "Password*:" : "New Password:"){{setStyle(UIConstants.LABEL_STYLE_DIALOG_CSS);}},2,1); grid.add(pwF,3,1);
        grid.add(new Label("Email*:"){{setStyle(UIConstants.LABEL_STYLE_DIALOG_CSS);}},0,2); grid.add(emF,1,2,3,1);
        grid.add(new Label("Phone:"){{setStyle(UIConstants.LABEL_STYLE_DIALOG_CSS);}},0,3); grid.add(phF,1,3);
        grid.add(new Label("Hire Date:"){{setStyle(UIConstants.LABEL_STYLE_DIALOG_CSS);}},2,3); grid.add(hireDP,3,3);
        grid.add(new Label("Role*:"){{setStyle(UIConstants.LABEL_STYLE_DIALOG_CSS);}},0,4); grid.add(roleCB,1,4);
        grid.add(new Label("Department*:"){{setStyle(UIConstants.LABEL_STYLE_DIALOG_CSS);}},2,4); grid.add(deptCB,3,4);
        grid.add(activeCB, 0, 5, 2,1);

        HBox buttons = new HBox(15, saveButton, cancelButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        grid.add(buttons, 0, 6, 4, 1);

        ColumnConstraints colConstraint = new ColumnConstraints(); colConstraint.setPercentWidth(25);
        grid.getColumnConstraints().addAll(colConstraint,colConstraint,colConstraint,colConstraint);

        Scene dialogScene = new Scene(grid);
        dialogStage.setScene(dialogScene);
        dialogStage.sizeToScene();
        dialogStage.showAndWait();
    }
}