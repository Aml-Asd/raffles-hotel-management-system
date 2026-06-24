package com.example.demo10.raffles.hotelmgmt.ui;



import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Window;

import java.net.URL;
import java.util.Optional;
public class DialogUtil {

    public static void showAlert(Alert.AlertType alertType, Window owner, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null); // No header for simplicity, or set one
        alert.setContentText(message);
        if (owner != null) {
            alert.initOwner(owner);
        }

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-font-family: '" + UIConstants.FONT_SANS_SERIF_CLEAN + "'; " +
                "-fx-border-color: " + UIConstants.BRAND_COLOR_HEX + "; " +
                "-fx-border-width: 1.5px;");
        // Example: Load a general dialog stylesheet (optional)
        // try {
        //     URL cssUrl = DialogUtil.class.getResource("/styles/dialog.css"); // Assuming dialog.css is in resources/styles
        //     if (cssUrl != null) {
        //         dialogPane.getStylesheets().add(cssUrl.toExternalForm());
        //         // dialogPane.getStyleClass().add("my-dialog-pane"); // If you have a specific class in CSS
        //     } else {
        //         System.err.println("Warning: /styles/dialog.css not found.");
        //     }
        // } catch (Exception e) {
        //     System.err.println("Error loading /styles/dialog.css: " + e.getMessage());
        // }
        alert.showAndWait();
    }

    public static Optional<ButtonType> showConfirmationDialog(Window owner, String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        if (owner != null) {
            alert.initOwner(owner);
        }
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-font-family: '" + UIConstants.FONT_SANS_SERIF_CLEAN + "'; " +
                "-fx-border-color: " + UIConstants.BRAND_COLOR_HEX + "; " +
                "-fx-border-width: 1.5px;");
        return alert.showAndWait();
    }
}