package com.example.demo10.raffles.hotelmgmt.ui; // Correct package

// No MainApp import needed here if UIConstants has all paths
// import com.example.demo10.raffles.hotelmgmt.MainApp;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.net.URL;

public class AuthUIFactory {

    private static Timeline logoAnimationAuthScreenInternal; // Static to manage its state across auth screens

    public static StackPane createLeftAuthPanel() {
        VBox leftContent = new VBox(20);
        leftContent.setPadding(new Insets(40, 30, 30, 30));
        leftContent.setAlignment(Pos.CENTER);

        // Use the animated logo view method
        ImageView rafflesLogoView = createAnimatedLogoView(330);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Text copyrightLeftText = new Text("© " + java.time.Year.now().getValue() +
                " Raffles Hotels & Resorts\nPowered by Hotel Management Suite");
        copyrightLeftText.setFont(Font.font(UIConstants.FONT_SANS_SERIF_CLEAN, 10));
        copyrightLeftText.setFill(Color.DARKGRAY);
        copyrightLeftText.setTextAlignment(TextAlignment.CENTER);

        if (rafflesLogoView.getImage() != null) {
            VBox.setMargin(rafflesLogoView, new Insets(50, 0, 0, 0));
            leftContent.getChildren().addAll(rafflesLogoView, spacer, copyrightLeftText);
        } else {
            Text logoPlaceholder = new Text("Raffles Logo (Not Found)");
            logoPlaceholder.setFont(Font.font(UIConstants.FONT_SANS_SERIF_CLEAN, FontWeight.BOLD, 20));
            logoPlaceholder.setFill(Color.DARKGRAY);
            VBox.setMargin(logoPlaceholder, new Insets(50, 0, 0, 0));
            leftContent.getChildren().addAll(logoPlaceholder, spacer, copyrightLeftText);
        }

        StackPane leftPanelContainer = new StackPane(leftContent);
        leftPanelContainer.setPrefWidth(450); // Consistent width
        leftPanelContainer.setMinWidth(Region.USE_PREF_SIZE);
        leftPanelContainer.setStyle("-fx-background-color: white; -fx-background-radius: 0 60px 60px 0;"); // Rounded on one side
        leftPanelContainer.setEffect(new DropShadow(15, Color.rgb(0, 0, 0, 0.2)));

        return leftPanelContainer;
    }

    private static ImageView createAnimatedLogoView(double fitWidth) {
        ImageView logoView = new ImageView();
        try {
            // Using UIConstants for the path now
            URL logoUrl = AuthUIFactory.class.getResource(UIConstants.LOGO_RESOURCE_PATH);
            if (logoUrl != null) {
                Image image = new Image(logoUrl.toExternalForm());
                logoView.setImage(image);
                logoView.setFitWidth(fitWidth);
                logoView.setPreserveRatio(true);

                Glow glow = new Glow();
                glow.setLevel(0.0);
                logoView.setEffect(glow);

                // Stop any existing animation before creating a new one
                if (logoAnimationAuthScreenInternal != null) {
                    logoAnimationAuthScreenInternal.stop();
                }
                logoAnimationAuthScreenInternal = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(glow.levelProperty(), 0.0)),
                        new KeyFrame(Duration.seconds(1.5), new KeyValue(glow.levelProperty(), 0.7)), // Brighter glow
                        new KeyFrame(Duration.seconds(3.0), new KeyValue(glow.levelProperty(), 0.0))
                );
                logoAnimationAuthScreenInternal.setCycleCount(Timeline.INDEFINITE);
                // Animation will be started by the controller when the scene is shown
            } else {
                System.err.println("Authentication Screen Logo resource not found: " + UIConstants.LOGO_RESOURCE_PATH);
            }
        } catch (Exception e) {
            System.err.println("Error loading authentication screen logo: " + e.getMessage());
            e.printStackTrace(); // Good to see stack trace for resource loading issues
        }
        return logoView;
    }

    public static void playLogoAnimation() {
        if (logoAnimationAuthScreenInternal != null) {
            logoAnimationAuthScreenInternal.playFromStart();
        }
    }

    public static void pauseLogoAnimation() {
        if (logoAnimationAuthScreenInternal != null &&
                logoAnimationAuthScreenInternal.getStatus() == Timeline.Status.RUNNING) {
            logoAnimationAuthScreenInternal.pause();
        }
    }
    
    /**
     * Creates a styled layout for login and registration forms
     */
    public static StackPane createFormSceneLayout() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, " + 
                    UIConstants.BRAND_COLOR_HEX + ", " + 
                    UIConstants.ACCENT_COLOR_DARK_BROWN_HEX + ");");
        
        // Add a subtle pattern overlay for depth
        Region pattern = new Region();
        pattern.setStyle("-fx-background-color: rgba(0,0,0,0.05); -fx-background-radius: 0;");
        
        root.getChildren().add(pattern);
        return root;
    }
    
    /**
     * Creates a styled title section for forms
     */
    public static VBox createTitleSection(String title, String subtitle) {
        VBox titleBox = new VBox(5);
        titleBox.setAlignment(Pos.CENTER);
        
        Text titleText = new Text(title);
        titleText.setFont(Font.font(UIConstants.FONT_SERIF_ELEGANT, FontWeight.BOLD, 32));
        titleText.setFill(Color.WHITE);
        
        Text subtitleText = new Text(subtitle);
        subtitleText.setFont(Font.font(UIConstants.FONT_SANS_SERIF_CLEAN, 14));
        subtitleText.setFill(Color.web("#E0E0E0"));
        subtitleText.setTextAlignment(TextAlignment.CENTER);
        
        titleBox.getChildren().addAll(titleText, subtitleText);
        return titleBox;
    }
}