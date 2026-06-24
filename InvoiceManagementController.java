package com.example.demo10.raffles.hotelmgmt.controller;

import com.example.demo10.raffles.hotelmgmt.MainApp;
import com.example.demo10.raffles.hotelmgmt.dao.ChargeDAO;
import com.example.demo10.raffles.hotelmgmt.dao.GuestDAO;
import com.example.demo10.raffles.hotelmgmt.dao.InvoiceDAO;
import com.example.demo10.raffles.hotelmgmt.dao.ReservationDAO;
import com.example.demo10.raffles.hotelmgmt.dao.ServiceDAO;
import com.example.demo10.raffles.hotelmgmt.model.Charge;
import com.example.demo10.raffles.hotelmgmt.model.Guest;
import com.example.demo10.raffles.hotelmgmt.model.Invoice;
import com.example.demo10.raffles.hotelmgmt.model.Reservation;
import com.example.demo10.raffles.hotelmgmt.model.Service;
import com.example.demo10.raffles.hotelmgmt.ui.DialogUtil;
import com.example.demo10.raffles.hotelmgmt.ui.UIConstants;
import com.example.demo10.raffles.hotelmgmt.util.DatabaseResetUtil;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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
import javafx.util.Callback;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class InvoiceManagementController {

    private final MainApp mainApp;
    private final InvoiceDAO invoiceDAO;
    private final ChargeDAO chargeDAO;
    private final ServiceDAO serviceDAO;
    private final ObservableList<Invoice> invoiceObservableList;
    private TableView<Invoice> invoiceTable;

    public InvoiceManagementController(MainApp mainApp) {
        this.mainApp = mainApp;
        this.invoiceDAO = new InvoiceDAO();
        this.chargeDAO = new ChargeDAO();
        this.serviceDAO = new ServiceDAO();
        this.invoiceObservableList = FXCollections.observableArrayList();
        loadInvoices();
    }

    public Node createInvoiceManagementPane() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30, 40, 30, 40));

        // --- Title and Search Bar ---
        Text title = new Text("Invoice Management");
        title.setFont(Font.font(UIConstants.FONT_SERIF_ELEGANT, FontWeight.BOLD, 28));
        title.setFill(Color.web(UIConstants.ACCENT_COLOR_DARK_BROWN_HEX));

        TextField searchField = new TextField();
        searchField.setPromptText("Search by Guest, ID or Status");
        searchField.setPrefWidth(250);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterInvoices(newVal));

        Button refreshButton = new Button("Refresh");
        refreshButton.setStyle("-fx-background-color: " + UIConstants.BRAND_COLOR_HEX + "; -fx-text-fill: white;");
        refreshButton.setOnAction(e -> loadInvoices());
        
        HBox searchBar = new HBox(10, new Label("Filter:"), searchField, refreshButton);
        searchBar.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button newInvoiceButton = new Button("New Invoice");
        newInvoiceButton.setStyle("-fx-background-color: " + UIConstants.ACCENT_COLOR_DARK_BROWN_HEX + "; -fx-text-fill: white;");
        newInvoiceButton.setOnAction(e -> showCreateInvoiceDialog());
        
        Button resetDBButton = new Button("Reset DB");
        resetDBButton.setStyle("-fx-background-color: #AA0000; -fx-text-fill: white;");
        resetDBButton.setOnAction(e -> resetDatabase());

        Button createSampleButton = new Button("Create Sample");
        createSampleButton.setStyle("-fx-background-color: #006600; -fx-text-fill: white;");
        createSampleButton.setOnAction(e -> createSampleInvoice());

        HBox titleBar = new HBox(20, title, spacer, searchBar, newInvoiceButton, resetDBButton, createSampleButton);
        titleBar.setAlignment(Pos.CENTER_LEFT);

        // --- Invoice Table ---
        invoiceTable = new TableView<>(invoiceObservableList);
        invoiceTable.setPlaceholder(new Label("No invoices found."));
        try {
            URL cssUrl = getClass().getResource("/styles/tableview.css");
            if (cssUrl != null) {
                invoiceTable.getStylesheets().add(cssUrl.toExternalForm());
            }
        } catch (Exception e) {
            System.err.println("Error loading tableview.css for Invoices: " + e.getMessage());
        }

        TableColumn<Invoice, Integer> idCol = new TableColumn<>("Inv. ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("invoiceID"));
        idCol.setPrefWidth(80);
        idCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Invoice, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("invoiceDate"));
        dateCol.setCellFactory(getDateCellFactory());
        dateCol.setPrefWidth(120);

        TableColumn<Invoice, String> guestNameCol = new TableColumn<>("Guest Name");
        guestNameCol.setCellValueFactory(new PropertyValueFactory<>("guestName"));
        guestNameCol.setPrefWidth(200);
        
        TableColumn<Invoice, Integer> reservationIdCol = new TableColumn<>("Res. ID");
        reservationIdCol.setCellValueFactory(new PropertyValueFactory<>("reservationID"));
        reservationIdCol.setPrefWidth(80);
        reservationIdCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Invoice, BigDecimal> totalCol = new TableColumn<>("Total Amt.");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        totalCol.setCellFactory(getCurrencyCellFactory());
        totalCol.setPrefWidth(120);
        totalCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<Invoice, BigDecimal> paidCol = new TableColumn<>("Amt. Paid");
        paidCol.setCellValueFactory(new PropertyValueFactory<>("amountPaid"));
        paidCol.setCellFactory(getCurrencyCellFactory());
        paidCol.setPrefWidth(120);
        paidCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        
        TableColumn<Invoice, BigDecimal> dueCol = new TableColumn<>("Balance Due");
        dueCol.setCellValueFactory(new PropertyValueFactory<>("balanceDue"));
        dueCol.setCellFactory(getCurrencyCellFactory());
        dueCol.setPrefWidth(120);
        dueCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<Invoice, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);
        statusCol.setCellFactory(column -> new TableCell<Invoice, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Paid".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: " + UIConstants.SUCCESS_COLOR_HEX + "; -fx-font-weight: bold;");
                    } else if ("Unpaid".equalsIgnoreCase(item) || "Overdue".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: " + UIConstants.ERROR_COLOR_HEX + ";");
                    } else if ("Partially Paid".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: orange;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        // Add actions column with buttons
        TableColumn<Invoice, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(240);  // Make wider to fit delete button
        actionsCol.setCellFactory(column -> {
            return new TableCell<Invoice, Void>() {
                private final Button viewBtn = new Button("View");
                private final Button payBtn = new Button("Pay");
                private final Button printBtn = new Button("Print");
                private final Button deleteBtn = new Button("Delete");
                
                {
                    viewBtn.setStyle("-fx-background-color: " + UIConstants.BRAND_COLOR_HEX + "; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 3px 8px;");
                    payBtn.setStyle("-fx-background-color: " + UIConstants.SUCCESS_COLOR_HEX + "; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 3px 8px;");
                    printBtn.setStyle("-fx-background-color: " + UIConstants.ACCENT_COLOR_DARK_BROWN_HEX + "; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 3px 8px;");
                    deleteBtn.setStyle("-fx-background-color: " + UIConstants.ERROR_COLOR_HEX + "; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 3px 8px;");
                    
                    viewBtn.setOnAction(event -> {
                        Invoice invoice = getTableView().getItems().get(getIndex());
                        showInvoiceDetailsDialog(invoice);
                    });
                    
                    payBtn.setOnAction(event -> {
                        Invoice invoice = getTableView().getItems().get(getIndex());
                        showPaymentDialog(invoice);
                    });
                    
                    printBtn.setOnAction(event -> {
                        Invoice invoice = getTableView().getItems().get(getIndex());
                        printInvoice(invoice);
                    });
                    
                    deleteBtn.setOnAction(event -> {
                        Invoice invoice = getTableView().getItems().get(getIndex());
                        deleteInvoice(invoice);
                    });
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        HBox buttons = new HBox(5, viewBtn, payBtn, printBtn, deleteBtn);
                        buttons.setAlignment(Pos.CENTER);
                        setGraphic(buttons);
                    }
                }
            };
        });

        invoiceTable.getColumns().addAll(idCol, dateCol, guestNameCol, reservationIdCol, totalCol, paidCol, dueCol, statusCol, actionsCol);
        VBox.setVgrow(invoiceTable, Priority.ALWAYS);

        layout.getChildren().addAll(titleBar, invoiceTable);
        return layout;
    }

    private void loadInvoices() {
        invoiceObservableList.setAll(invoiceDAO.getAllInvoicesWithDetails());
    }

    private void filterInvoices(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            invoiceTable.setItems(invoiceObservableList);
            return;
        }

        String lowerCaseFilter = searchText.toLowerCase();
        ObservableList<Invoice> filteredData = FXCollections.observableArrayList();

        for (Invoice invoice : invoiceObservableList) {
            boolean match = false;
            if (String.valueOf(invoice.getInvoiceID()).contains(lowerCaseFilter)) {
                match = true;
            } else if (invoice.getGuestName() != null && invoice.getGuestName().toLowerCase().contains(lowerCaseFilter)) {
                match = true;
            } else if (invoice.getReservationID() != null && String.valueOf(invoice.getReservationID()).contains(lowerCaseFilter)) {
                match = true;
            } else if (invoice.getStatus() != null && invoice.getStatus().toLowerCase().contains(lowerCaseFilter)) {
                match = true;
            }
            
            if (match) {
                filteredData.add(invoice);
            }
        }
        invoiceTable.setItems(filteredData);
    }

    private <T> Callback<TableColumn<T, LocalDate>, TableCell<T, LocalDate>> getDateCellFactory() {
        return column -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
            
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatter.format(item));
            }
        };
    }

    private <T> Callback<TableColumn<T, BigDecimal>, TableCell<T, BigDecimal>> getCurrencyCellFactory() {
        return column -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item.doubleValue()));
                }
            }
        };
    }

    private void showInvoiceDetailsDialog(Invoice invoice) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Invoice #" + invoice.getInvoiceID() + " Details");
        dialogStage.initOwner(mainApp.getPrimaryStage());
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");
        
        // Header with invoice information
        Text headerText = new Text("Invoice #" + invoice.getInvoiceID());
        headerText.setFont(Font.font(UIConstants.FONT_SERIF_ELEGANT, FontWeight.BOLD, 24));
        
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(15);
        infoGrid.setVgap(8);
        
        // Add invoice details
        int row = 0;
        infoGrid.add(new Label("Guest:"), 0, row);
        infoGrid.add(new Label(invoice.getGuestName() != null ? invoice.getGuestName() : "N/A"), 1, row++);
        
        infoGrid.add(new Label("Reservation:"), 0, row);
        infoGrid.add(new Label(invoice.getReservationDetails() != null ? invoice.getReservationDetails() : 
                              (invoice.getReservationID() != null ? "#" + invoice.getReservationID() : "N/A")), 1, row++);
        
        infoGrid.add(new Label("Invoice Date:"), 0, row);
        infoGrid.add(new Label(invoice.getInvoiceDate() != null ? 
                           invoice.getInvoiceDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) : "N/A"), 1, row++);
        
        infoGrid.add(new Label("Due Date:"), 0, row);
        infoGrid.add(new Label(invoice.getDueDate() != null ? 
                           invoice.getDueDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) : "N/A"), 1, row++);
        
        infoGrid.add(new Label("Status:"), 0, row);
        Label statusLabel = new Label(invoice.getStatus() != null ? invoice.getStatus() : "Unknown");
        if ("Paid".equalsIgnoreCase(invoice.getStatus())) {
            statusLabel.setTextFill(Color.web(UIConstants.SUCCESS_COLOR_HEX));
        } else if ("Unpaid".equalsIgnoreCase(invoice.getStatus()) || "Overdue".equalsIgnoreCase(invoice.getStatus())) {
            statusLabel.setTextFill(Color.web(UIConstants.ERROR_COLOR_HEX));
        }
        infoGrid.add(statusLabel, 1, row++);
        
        // Get charges for this invoice
        ObservableList<Charge> charges = chargeDAO.getChargesByInvoiceId(invoice.getInvoiceID());
        
        // Charges table
        TableView<Charge> chargesTable = new TableView<>(charges);
        chargesTable.setPlaceholder(new Label("No charges found for this invoice."));
        
        TableColumn<Charge, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(250);
        
        TableColumn<Charge, LocalDate> chargeDateCol = new TableColumn<>("Date");
        chargeDateCol.setCellValueFactory(new PropertyValueFactory<>("chargeDate"));
        chargeDateCol.setCellFactory(getDateCellFactory());
        chargeDateCol.setPrefWidth(120);
        
        TableColumn<Charge, BigDecimal> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setCellFactory(getCurrencyCellFactory());
        amountCol.setPrefWidth(100);
        amountCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        
        TableColumn<Charge, String> serviceCol = new TableColumn<>("Service");
        serviceCol.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        serviceCol.setPrefWidth(150);
        
        chargesTable.getColumns().addAll(chargeDateCol, descCol, serviceCol, amountCol);
        VBox.setVgrow(chargesTable, Priority.ALWAYS);
        
        // Totals section
        GridPane totalsGrid = new GridPane();
        totalsGrid.setHgap(15);
        totalsGrid.setVgap(5);
        totalsGrid.setPadding(new Insets(10, 0, 0, 0));
        totalsGrid.setStyle("-fx-background-color: #f7f7f7; -fx-padding: 10px;");
        
        row = 0;
        totalsGrid.add(new Label("Total Amount:"), 0, row);
        Label totalLabel = new Label(String.format("$%.2f", invoice.getTotalAmount().doubleValue()));
        totalLabel.setStyle("-fx-font-weight: bold;");
        totalsGrid.add(totalLabel, 1, row++);
        
        totalsGrid.add(new Label("Amount Paid:"), 0, row);
        Label paidLabel = new Label(String.format("$%.2f", invoice.getAmountPaid().doubleValue()));
        paidLabel.setStyle("-fx-font-weight: bold;");
        totalsGrid.add(paidLabel, 1, row++);
        
        totalsGrid.add(new Label("Balance Due:"), 0, row);
        Label balanceLabel = new Label(String.format("$%.2f", invoice.getBalanceDue().doubleValue()));
        balanceLabel.setStyle("-fx-font-weight: bold;");
        if (invoice.getBalanceDue().compareTo(BigDecimal.ZERO) > 0) {
            balanceLabel.setTextFill(Color.web(UIConstants.ERROR_COLOR_HEX));
        }
        totalsGrid.add(balanceLabel, 1, row++);
        
        // Buttons
        Button addChargeBtn = new Button("Add Charge");
        Button closeBtn = new Button("Close");
        
        addChargeBtn.setOnAction(e -> showAddChargeDialog(invoice, chargesTable));
        closeBtn.setOnAction(e -> dialogStage.close());
        
        HBox buttonBar = new HBox(10, addChargeBtn, closeBtn);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        
        content.getChildren().addAll(headerText, infoGrid, new Separator(), 
                                    new Label("Charges:"), chargesTable, 
                                    totalsGrid, buttonBar);
        
        Scene dialogScene = new Scene(content, 700, 600);
        dialogStage.setScene(dialogScene);
        dialogStage.showAndWait();
    }
    
    private void showPaymentDialog(Invoice invoice) {
        Dialog<BigDecimal> dialog = new Dialog<>();
        dialog.setTitle("Record Payment");
        dialog.setHeaderText("Record payment for Invoice #" + invoice.getInvoiceID());
        
        // Set button types
        ButtonType payButtonType = new ButtonType("Process Payment", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(payButtonType, ButtonType.CANCEL);
        
        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField amountField = new TextField();
        ComboBox<String> paymentMethodCombo = new ComboBox<>(
            FXCollections.observableArrayList("Cash", "Credit Card", "Debit Card", "Bank Transfer", "Other")
        );
        paymentMethodCombo.getSelectionModel().selectFirst();
        
        grid.add(new Label("Current Balance:"), 0, 0);
        grid.add(new Label(String.format("$%.2f", invoice.getBalanceDue().doubleValue())), 1, 0);
        grid.add(new Label("Payment Amount:"), 0, 1);
        grid.add(amountField, 1, 1);
        grid.add(new Label("Payment Method:"), 0, 2);
        grid.add(paymentMethodCombo, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on amount field
        Platform.runLater(amountField::requestFocus);
        
        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == payButtonType) {
                try {
                    BigDecimal amount = new BigDecimal(amountField.getText());
                    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                        DialogUtil.showAlert(Alert.AlertType.ERROR, mainApp.getPrimaryStage(),
                            "Invalid Amount", "Payment amount must be greater than zero.");
                        return null;
                    }
                    return amount;
                } catch (NumberFormatException e) {
                    DialogUtil.showAlert(Alert.AlertType.ERROR, mainApp.getPrimaryStage(),
                        "Invalid Amount", "Please enter a valid payment amount.");
                    return null;
                }
            }
            return null;
        });
        
        Optional<BigDecimal> result = dialog.showAndWait();
        
        result.ifPresent(amount -> {
            // Update invoice with payment
            BigDecimal newPaid = invoice.getAmountPaid().add(amount);
            String newStatus = "Unpaid";
            
            if (newPaid.compareTo(invoice.getTotalAmount()) >= 0) {
                newStatus = "Paid";
            } else if (newPaid.compareTo(BigDecimal.ZERO) > 0) {
                newStatus = "Partially Paid";
            }
            
            boolean success = invoiceDAO.updateInvoiceStatus(invoice.getInvoiceID(), newStatus, newPaid);
            
            if (success) {
                DialogUtil.showAlert(Alert.AlertType.INFORMATION, mainApp.getPrimaryStage(),
                    "Payment Recorded", "Payment of $" + amount + " has been recorded.");
                loadInvoices();
            } else {
                DialogUtil.showAlert(Alert.AlertType.ERROR, mainApp.getPrimaryStage(),
                    "Error", "Failed to record payment. Please try again.");
            }
        });
    }
    
    private void showAddChargeDialog(Invoice invoice, TableView<Charge> chargesTable) {
        Dialog<Charge> dialog = new Dialog<>();
        dialog.setTitle("Add Charge");
        dialog.setHeaderText("Add a new charge to Invoice #" + invoice.getInvoiceID());
        
        // Set button types
        ButtonType addButtonType = new ButtonType("Add Charge", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        
        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        ComboBox<Service> serviceCombo = new ComboBox<>(serviceDAO.getAllServices());
        serviceCombo.setPromptText("Select a service or enter custom charge");
        
        TextField descriptionField = new TextField();
        TextField amountField = new TextField();
        DatePicker datePicker = new DatePicker(LocalDate.now());
        
        // When service is selected, pre-fill description and amount
        serviceCombo.setOnAction(e -> {
            Service selected = serviceCombo.getValue();
            if (selected != null) {
                descriptionField.setText(selected.getServiceName());
                if (selected.getDefaultPrice() != null) {
                    amountField.setText(selected.getDefaultPrice().toString());
                }
            }
        });
        
        grid.add(new Label("Service:"), 0, 0);
        grid.add(serviceCombo, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("Amount:"), 0, 2);
        grid.add(amountField, 1, 2);
        grid.add(new Label("Date:"), 0, 3);
        grid.add(datePicker, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on service combo
        Platform.runLater(serviceCombo::requestFocus);
        
        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    if (descriptionField.getText().isEmpty()) {
                        DialogUtil.showAlert(Alert.AlertType.ERROR, mainApp.getPrimaryStage(),
                            "Missing Description", "Please enter a charge description.");
                        return null;
                    }
                    
                    BigDecimal amount;
                    try {
                        amount = new BigDecimal(amountField.getText());
                    } catch (NumberFormatException ex) {
                        DialogUtil.showAlert(Alert.AlertType.ERROR, mainApp.getPrimaryStage(),
                            "Invalid Amount", "Please enter a valid amount.");
                        return null;
                    }
                    
                    Charge charge = new Charge();
                    charge.setInvoiceID(invoice.getInvoiceID());
                    
                    if (serviceCombo.getValue() != null) {
                        charge.setServiceID(serviceCombo.getValue().getServiceID());
                    }
                    
                    charge.setDescription(descriptionField.getText());
                    charge.setAmount(amount);
                    charge.setChargeDate(datePicker.getValue());
                    charge.setChargedByEmployeeID(mainApp.getCurrentLoggedInUser().getEmployeeID());
                    
                    return charge;
                } catch (Exception e) {
                    DialogUtil.showAlert(Alert.AlertType.ERROR, mainApp.getPrimaryStage(),
                        "Error", "Please check your input and try again.");
                    return null;
                }
            }
            return null;
        });
        
        Optional<Charge> result = dialog.showAndWait();
        
        result.ifPresent(charge -> {
            boolean success = chargeDAO.addCharge(charge);
            
            if (success) {
                DialogUtil.showAlert(Alert.AlertType.INFORMATION, mainApp.getPrimaryStage(),
                    "Charge Added", "Charge has been added to the invoice.");
                    
                // Refresh the charges table
                chargesTable.setItems(chargeDAO.getChargesByInvoiceId(invoice.getInvoiceID()));
                
                // Refresh the invoice list to show updated totals
                loadInvoices();
            } else {
                DialogUtil.showAlert(Alert.AlertType.ERROR, mainApp.getPrimaryStage(),
                    "Error", "Failed to add charge. Please try again.");
            }
        });
    }
    
    private void showCreateInvoiceDialog() {
        Dialog<Invoice> dialog = new Dialog<>();
        dialog.setTitle("Create New Invoice");
        dialog.setHeaderText("Create a new invoice");
        
        // Set the button types
        ButtonType createButtonType = new ButtonType("Create Invoice", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);
        
        // Create the form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Guest dropdown
        ComboBox<Guest> guestComboBox = new ComboBox<>();
        guestComboBox.setPromptText("Select a guest");
        guestComboBox.setMaxWidth(Double.MAX_VALUE);
        
        // Load guests
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
        
        // Reservation dropdown (optional)
        ComboBox<Reservation> reservationComboBox = new ComboBox<>();
        reservationComboBox.setPromptText("Select a reservation (optional)");
        reservationComboBox.setMaxWidth(Double.MAX_VALUE);
        
        // When guest is selected, load their reservations
        guestComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                ReservationDAO reservationDAO = new ReservationDAO();
                ObservableList<Reservation> guestReservations = reservationDAO.getReservationsByGuestID(newVal.getGuestID());
                
                // Use only active reservations
                ObservableList<Reservation> activeReservations = guestReservations.filtered(
                    reservation -> "Confirmed".equals(reservation.getStatus()) || 
                                  "Checked In".equals(reservation.getStatus()));
                
                reservationComboBox.setItems(activeReservations);
                reservationComboBox.setCellFactory(param -> new ListCell<Reservation>() {
                    @Override
                    protected void updateItem(Reservation item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            String details = "Res #" + item.getReservationID();
                            if (item.getCheckInDate() != null && item.getCheckOutDate() != null) {
                                details += " (" + item.getCheckInDate().format(DateTimeFormatter.ofPattern("MM/dd")) + 
                                          " to " + item.getCheckOutDate().format(DateTimeFormatter.ofPattern("MM/dd")) + ")";
                            }
                            setText(details);
                        }
                    }
                });
            } else {
                reservationComboBox.getItems().clear();
            }
        });
        
        // Date pickers for invoice date and due date
        DatePicker invoiceDatePicker = new DatePicker(LocalDate.now());
        DatePicker dueDatePicker = new DatePicker(LocalDate.now().plusDays(30));
        
        // Add fields to grid
        grid.add(new Label("Guest:"), 0, 0);
        grid.add(guestComboBox, 1, 0);
        grid.add(new Label("Reservation:"), 0, 1);
        grid.add(reservationComboBox, 1, 1);
        grid.add(new Label("Invoice Date:"), 0, 2);
        grid.add(invoiceDatePicker, 1, 2);
        grid.add(new Label("Due Date:"), 0, 3);
        grid.add(dueDatePicker, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the guest field
        Platform.runLater(guestComboBox::requestFocus);
        
        // Convert result to an invoice when button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                // Validate input
                if (guestComboBox.getValue() == null) {
                    DialogUtil.showAlert(Alert.AlertType.ERROR, mainApp.getPrimaryStage(), 
                        "Validation Error", "Please select a guest.");
                    return null;
                }
                
                // Create the invoice
                Invoice newInvoice = new Invoice();
                newInvoice.setGuestID(guestComboBox.getValue().getGuestID());
                
                if (reservationComboBox.getValue() != null) {
                    newInvoice.setReservationID(reservationComboBox.getValue().getReservationID());
                }
                
                newInvoice.setInvoiceDate(invoiceDatePicker.getValue());
                newInvoice.setDueDate(dueDatePicker.getValue());
                newInvoice.setTotalAmount(BigDecimal.ZERO);
                newInvoice.setAmountPaid(BigDecimal.ZERO);
                newInvoice.setStatus("Unpaid");
                
                return newInvoice;
            }
            return null;
        });
        
        Optional<Invoice> result = dialog.showAndWait();
        
        result.ifPresent(invoice -> {
            // Save the invoice to database
            boolean success = invoiceDAO.createInvoice(invoice);
            
            if (success) {
                DialogUtil.showAlert(Alert.AlertType.INFORMATION, mainApp.getPrimaryStage(),
                    "Invoice Created", "New invoice #" + invoice.getInvoiceID() + " has been created successfully.");
                
                // Refresh the invoice list
                loadInvoices();
                
                // Show the invoice details dialog to add charges right away
                showInvoiceDetailsDialog(invoiceDAO.getInvoiceById(invoice.getInvoiceID()));
            } else {
                DialogUtil.showAlert(Alert.AlertType.ERROR, mainApp.getPrimaryStage(),
                    "Error", "Failed to create invoice. Please try again.");
            }
        });
    }
    
    private void printInvoice(Invoice invoice) {
        // Placeholder for now - you would implement invoice printing/PDF generation
        DialogUtil.showAlert(Alert.AlertType.INFORMATION, mainApp.getPrimaryStage(),
            "Print Invoice", "Printing feature is coming soon!");
    }

    private void deleteInvoice(Invoice invoice) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Invoice #" + invoice.getInvoiceID() + "?");
        confirmDialog.setContentText("Are you sure you want to delete this invoice? This action cannot be undone and will remove all associated charges.");
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = invoiceDAO.deleteInvoice(invoice.getInvoiceID());
            
            if (success) {
                DialogUtil.showAlert(Alert.AlertType.INFORMATION, mainApp.getPrimaryStage(), 
                    "Invoice Deleted", "Invoice #" + invoice.getInvoiceID() + " has been deleted successfully.");
                
                // Refresh the invoice list
                loadInvoices();
            } else {
                DialogUtil.showAlert(Alert.AlertType.ERROR, mainApp.getPrimaryStage(),
                    "Error", "Failed to delete invoice. Please try again.");
            }
        }
    }

    /**
     * Creates an invoice directly from a specified reservation
     * @param reservationId The reservation ID to create an invoice for
     */
    private void createInvoiceFromReservation(int reservationId) {
        ReservationDAO reservationDAO = new ReservationDAO();
        Reservation reservation = reservationDAO.getReservationByIdWithDetails(reservationId);
        
        if (reservation == null) {
            DialogUtil.showAlert(Alert.AlertType.ERROR, mainApp.getPrimaryStage(),
                "Error", "Reservation not found.");
            return;
        }
        
        // Confirm with the user
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Create Invoice from Reservation");
        confirm.setHeaderText("Create invoice for Reservation #" + reservationId + "?");
        confirm.setContentText("This will create a new invoice for " + reservation.getGuestName() + 
                              " with the estimated total of $" + 
                              (reservation.getTotalEstimatedCost() != null ? 
                               String.format("%.2f", reservation.getTotalEstimatedCost()) : "0.00"));
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Use the DAO's method to create an invoice from a reservation
            Invoice newInvoice = invoiceDAO.createInvoiceFromReservation(reservationId);
            
            if (newInvoice != null) {
                DialogUtil.showAlert(Alert.AlertType.INFORMATION, mainApp.getPrimaryStage(),
                    "Invoice Created", "Invoice #" + newInvoice.getInvoiceID() + " has been created successfully.");
                    
                // Refresh the invoice list
                loadInvoices();
                
                // Show the invoice details dialog
                showInvoiceDetailsDialog(newInvoice);
            } else {
                DialogUtil.showAlert(Alert.AlertType.ERROR, mainApp.getPrimaryStage(),
                    "Error", "Failed to create invoice from reservation.");
            }
        }
    }

    /**
     * Reset the invoice database tables
     */
    private void resetDatabase() {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Reset Database");
        confirmDialog.setHeaderText("Reset Invoice Database Tables?");
        confirmDialog.setContentText("This will delete all invoices and charges. This action cannot be undone.");
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = DatabaseResetUtil.resetInvoiceTables();
            
            if (success) {
                DialogUtil.showAlert(Alert.AlertType.INFORMATION, mainApp.getPrimaryStage(), 
                    "Database Reset", "Invoice database tables have been reset successfully.");
                
                // Refresh the invoice list
                loadInvoices();
            } else {
                DialogUtil.showAlert(Alert.AlertType.ERROR, mainApp.getPrimaryStage(),
                    "Error", "Failed to reset database tables. Please try again.");
            }
        }
    }

    /**
     * Create a sample invoice for testing
     */
    private void createSampleInvoice() {
        try {
            // First find an existing guest
            GuestDAO guestDAO = new GuestDAO();
            ObservableList<Guest> guests = guestDAO.getAllGuests();
            
            if (guests.isEmpty()) {
                // Create a guest if none exists
                Guest guest = new Guest();
                guest.setFirstName("John");
                guest.setLastName("Doe");
                guest.setEmail("john.doe@example.com");
                guest.setPhoneNumber("555-123-4567");
                guestDAO.addGuest(guest);
                guests = guestDAO.getAllGuests();
            }
            
            if (!guests.isEmpty()) {
                Guest guest = guests.get(0);
                
                // Create a new invoice
                Invoice invoice = new Invoice();
                invoice.setGuestID(guest.getGuestID());
                invoice.setInvoiceDate(LocalDate.now());
                invoice.setDueDate(LocalDate.now().plusDays(14));
                invoice.setTotalAmount(new BigDecimal("0.00"));
                invoice.setAmountPaid(new BigDecimal("0.00"));
                invoice.setStatus("Unpaid");
                
                // Save the invoice
                InvoiceDAO invoiceDAO = new InvoiceDAO();
                boolean success = invoiceDAO.createInvoice(invoice);
                
                if (success) {
                    // Add some charges
                    ChargeDAO chargeDAO = new ChargeDAO();
                    
                    // Room charge
                    Charge roomCharge = new Charge();
                    roomCharge.setInvoiceID(invoice.getInvoiceID());
                    roomCharge.setDescription("Deluxe Room - 2 nights");
                    roomCharge.setAmount(new BigDecimal("450.00"));
                    roomCharge.setChargeDate(LocalDate.now());
                    chargeDAO.addCharge(roomCharge);
                    
                    // Room service charge
                    Charge foodCharge = new Charge();
                    foodCharge.setInvoiceID(invoice.getInvoiceID());
                    foodCharge.setDescription("Room Service - Dinner");
                    foodCharge.setAmount(new BigDecimal("75.50"));
                    foodCharge.setChargeDate(LocalDate.now());
                    chargeDAO.addCharge(foodCharge);
                    
                    // Mini bar charge
                    Charge minibarCharge = new Charge();
                    minibarCharge.setInvoiceID(invoice.getInvoiceID());
                    minibarCharge.setDescription("Minibar Items");
                    minibarCharge.setAmount(new BigDecimal("35.25"));
                    minibarCharge.setChargeDate(LocalDate.now());
                    chargeDAO.addCharge(minibarCharge);
                    
                    // Update invoice total
                    BigDecimal total = new BigDecimal("560.75"); // Sum of all charges
                    invoice.setTotalAmount(total);
                    invoice.setBalanceDue(total);
                    invoiceDAO.updateInvoice(invoice);
                    
                    // Show success message
                    DialogUtil.showAlert(Alert.AlertType.INFORMATION, mainApp.getPrimaryStage(),
                        "Sample Created", "Sample invoice created successfully with ID: " + invoice.getInvoiceID());
                    
                    // Refresh the invoice list
                    loadInvoices();
                } else {
                    DialogUtil.showAlert(Alert.AlertType.ERROR, mainApp.getPrimaryStage(),
                        "Error", "Failed to create sample invoice.");
                }
            } else {
                DialogUtil.showAlert(Alert.AlertType.ERROR, mainApp.getPrimaryStage(),
                    "Error", "No guests found in the database.");
            }
        } catch (Exception e) {
            System.err.println("Error creating sample invoice: " + e.getMessage());
            e.printStackTrace();
            DialogUtil.showAlert(Alert.AlertType.ERROR, mainApp.getPrimaryStage(),
                "Error", "An error occurred while creating the sample invoice: " + e.getMessage());
        }
    }
}