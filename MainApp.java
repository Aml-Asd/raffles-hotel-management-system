package com.example.demo10.raffles.hotelmgmt;

import com.example.demo10.raffles.hotelmgmt.controller.DashboardController;
import com.example.demo10.raffles.hotelmgmt.controller.GuestPortalController;
import com.example.demo10.raffles.hotelmgmt.controller.LoginController;
import com.example.demo10.raffles.hotelmgmt.controller.SignUpController;
import com.example.demo10.raffles.hotelmgmt.model.Employee;
import com.example.demo10.raffles.hotelmgmt.model.Guest;
import com.example.demo10.raffles.hotelmgmt.ui.AuthUIFactory;
import com.example.demo10.raffles.hotelmgmt.ui.DialogUtil;
import com.example.demo10.raffles.hotelmgmt.ui.UIConstants;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;

public class MainApp extends Application {

    private Stage primaryStage;
    private Scene welcomeScene, loginScene, signUpScene, dashboardScene;
    private MediaPlayer mediaPlayer;

    private Employee currentLoggedInUser;

    private LoginController loginController;
    private SignUpController signUpController;
    private DashboardController dashboardController;


    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        System.out.println("Starting Raffles Hotels & Resorts Management Suite...");

        // Verify database or reset if needed
        if (!DatabaseConnector.verifyDatabase()) {
            System.out.println("Database verification failed. Resetting database...");
            if (DatabaseConnector.resetDatabase()) {
                System.out.println("Database reset successfully. Initializing fresh database...");
            } else {
                System.err.println("Database reset failed. Application may not function correctly.");
            }
        } else {
            System.out.println("Database verification successful.");
            
            // Reset Invoice and Charge tables to fix any issues
            try {
                System.out.println("Resetting Invoice and Charge tables to ensure compatibility...");
                com.example.demo10.raffles.hotelmgmt.util.DatabaseResetUtil.resetInvoiceTables();
            } catch (Exception e) {
                System.err.println("Error resetting invoice tables: " + e.getMessage());
            }
        }

        // Initialize database tables and sample data
        DatabaseInitializer.initialize();

        try {
            URL dancingScriptUrl = getClass().getResource("/fonts/DancingScript-Bold.ttf");
            if (dancingScriptUrl != null) Font.loadFont(dancingScriptUrl.toExternalForm(), 10);
            else System.out.println("Warning: DancingScript-Bold.ttf not found in resources/fonts/");

            URL georgiaUrl = getClass().getResource("/fonts/Georgia.ttf");
            if (georgiaUrl != null) Font.loadFont(georgiaUrl.toExternalForm(), 10);
            else System.out.println("Warning: Georgia.ttf not found in resources/fonts/");
        } catch (Exception e) {
            System.out.println("Notice: Custom fonts in /fonts/ directory could not be loaded (this is optional). Error: " + e.getMessage());
        }

        loginController = new LoginController(this);
        signUpController = new SignUpController(this);

        this.welcomeScene = createWelcomeScene();
        this.loginScene = loginController.createLoginScene(); // Scene is created here
        this.signUpScene = signUpController.createSignUpScene(); // Scene is created here

        primaryStage.setTitle("Raffles Hotels & Resorts - Management Suite");
        primaryStage.setScene(this.welcomeScene);
        primaryStage.setWidth(UIConstants.INITIAL_WIDTH);
        primaryStage.setHeight(UIConstants.INITIAL_HEIGHT);
        primaryStage.setMinWidth(UIConstants.MIN_WIDTH);
        primaryStage.setMinHeight(UIConstants.MIN_HEIGHT);
        try {
            URL iconUrl = getClass().getResource(UIConstants.LOGO_RESOURCE_PATH);
            if (iconUrl != null) {
                primaryStage.getIcons().add(new Image(iconUrl.toExternalForm()));
            } else {
                System.err.println("Application icon resource not found: " + UIConstants.LOGO_RESOURCE_PATH);
            }
        } catch (Exception e) {
            System.err.println("Error loading application icon: " + e.getMessage());
        }
        primaryStage.show();

        if (mediaPlayer != null && this.welcomeScene.equals(stage.getScene()) && primaryStage.isShowing()) {
            mediaPlayer.play();
        }
    }

    private Scene createWelcomeScene() {
        StackPane root = new StackPane(); root.setStyle("-fx-background-color: #222;");
        MediaView mediaView = createWelcomeMediaView(root); Pane overlay = new Pane();
        overlay.setStyle("-fx-background-color: " + UIConstants.VIDEO_OVERLAY_COLOR_RGBA + ";");
        overlay.prefWidthProperty().bind(root.widthProperty()); overlay.prefHeightProperty().bind(root.heightProperty());
        overlay.setMouseTransparent(true); VBox uiContentPane = createWelcomeUIContent();
        root.getChildren().addAll(mediaView, overlay, uiContentPane); StackPane.setAlignment(uiContentPane, Pos.CENTER);
        return new Scene(root);
    }

    private VBox createWelcomeUIContent() {
        VBox contentPane = new VBox(20); contentPane.setAlignment(Pos.CENTER); contentPane.setPadding(new Insets(30));
        contentPane.setStyle("-fx-background-color: transparent;"); contentPane.setMaxWidth(600);
        Text titleText = new Text("Raffles");
        titleText.setFont(Font.font(UIConstants.FONT_FAMILY_TITLE_WELCOME, FontWeight.BOLD, 72));
        titleText.setFill(UIConstants.COLOR_TEXT_OVER_VIDEO_FX);
        titleText.setEffect(new DropShadow(5, 2, 2, Color.rgb(0,0,0,0.5)));
        Text subtitleText = new Text("Welcome to Raffles Hotels & Resorts\nExperience Unrivaled Luxury");
        subtitleText.setFont(Font.font(UIConstants.FONT_FAMILY_BODY_WELCOME, FontWeight.NORMAL, 22));
        subtitleText.setFill(UIConstants.COLOR_TEXT_OVER_VIDEO_FX);
        subtitleText.setTextAlignment(TextAlignment.CENTER);
        subtitleText.setEffect(new DropShadow(3, 1, 1, Color.rgb(0,0,0,0.4)));
        Region spacer1 = new Region(); spacer1.setPrefHeight(15);
        Region spacer2 = new Region(); spacer2.setPrefHeight(40);
        Button btnLogin = new Button("MANAGEMENT LOGIN");
        Button btnSignUp = new Button("STAFF REGISTRATION");
        String buttonStyle = "-fx-background-color: rgba(255, 255, 255, 0.15); -fx-text-fill: " + UIConstants.COLOR_TEXT_OVER_VIDEO_HEX + "; -fx-font-family: '" + UIConstants.FONT_SANS_SERIF_CLEAN + "'; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 12px 25px; -fx-border-color: rgba(255, 255, 255, 0.7); -fx-border-width: 1.5px; -fx-background-radius: 8px; -fx-border-radius: 8px;" + UIConstants.SHADOW_EFFECT_CSS;
        String buttonHoverStyle = "-fx-background-color: rgba(255, 255, 255, 0.35); -fx-border-color: white;";
        btnLogin.setStyle(buttonStyle); btnSignUp.setStyle(buttonStyle);
        btnLogin.setOnMouseEntered(e -> btnLogin.setStyle(buttonStyle + buttonHoverStyle));
        btnLogin.setOnMouseExited(e -> btnLogin.setStyle(buttonStyle));
        btnSignUp.setOnMouseEntered(e -> btnSignUp.setStyle(buttonStyle + buttonHoverStyle));
        btnSignUp.setOnMouseExited(e -> btnSignUp.setStyle(buttonStyle));
        btnLogin.setOnAction(e -> switchToLogin());
        btnSignUp.setOnAction(e -> switchToSignUp());
        HBox buttonPane = new HBox(25, btnLogin, btnSignUp);
        buttonPane.setAlignment(Pos.CENTER);
        contentPane.getChildren().addAll(titleText, spacer1, subtitleText, spacer2, buttonPane);
        return contentPane;
    }

    private MediaView createWelcomeMediaView(StackPane rootPane) {
        MediaView mediaView = new MediaView();
        File videoFile = new File(UIConstants.VIDEO_ABSOLUTE_PATH);
        final String[] videoPathForErrorDisplay = {UIConstants.VIDEO_ABSOLUTE_PATH};
        
        try {
            URL finalVideoUrl;
            System.out.println("Attempting to load video from: " + UIConstants.VIDEO_ABSOLUTE_PATH);
            
            if (videoFile.exists() && videoFile.isFile()) {
                finalVideoUrl = videoFile.toURI().toURL();
                System.out.println("Video file found at absolute path: " + UIConstants.VIDEO_ABSOLUTE_PATH);
            } else {
                System.out.println("Video file not found at absolute path: " + UIConstants.VIDEO_ABSOLUTE_PATH + ". Attempting resource fallback: " + UIConstants.VIDEO_RESOURCE_PATH_FALLBACK);
                videoPathForErrorDisplay[0] = UIConstants.VIDEO_RESOURCE_PATH_FALLBACK;
                URL resourceUrl = getClass().getResource(UIConstants.VIDEO_RESOURCE_PATH_FALLBACK);
                if (resourceUrl == null) {
                    // Try one more path as last resort
                    String altPath = "/src/raffles.mp4";
                    System.out.println("Video resource not found at " + UIConstants.VIDEO_RESOURCE_PATH_FALLBACK + ". Trying alternative path: " + altPath);
                    File altFile = new File(altPath);
                    if (altFile.exists()) {
                        finalVideoUrl = altFile.toURI().toURL();
                        System.out.println("Video found at alternative path: " + altPath);
                    } else {
                        throw new Exception("Video not found at any location. Please ensure it exists at either " + 
                                          UIConstants.VIDEO_ABSOLUTE_PATH + " or " + 
                                          UIConstants.VIDEO_RESOURCE_PATH_FALLBACK);
                    }
                } else {
                    finalVideoUrl = resourceUrl;
                    System.out.println("Using video from resource path: " + UIConstants.VIDEO_RESOURCE_PATH_FALLBACK);
                }
            }
            
            // Stop and dispose any existing media player
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }
            
            // Create the media and player
            Media media = new Media(finalVideoUrl.toExternalForm());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setAutoPlay(true);
            mediaView.setMediaPlayer(mediaPlayer);
            
            // Configure the view
            mediaView.fitWidthProperty().bind(rootPane.widthProperty());
            mediaView.fitHeightProperty().bind(rootPane.heightProperty());
            mediaView.setPreserveRatio(false);
            
            // Configure the player
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setMute(true); // Mute the welcome video
            
            // Error handling
            mediaPlayer.setOnError(() -> { 
                String eMsg = "Unknown Media Player Error"; 
                if (mediaPlayer.getError() != null) {
                    eMsg = mediaPlayer.getError().toString();
                }
                System.err.println("MediaPlayer Error for " + videoPathForErrorDisplay[0] + ": " + eMsg);
                displayVideoError(rootPane, videoPathForErrorDisplay[0], eMsg);
            });
            
            // Ready handler
            mediaPlayer.setOnReady(() -> {
                System.out.println("Media player ready, setting up video");
                if (primaryStage.getScene() == welcomeScene && primaryStage.isShowing()) {
                    System.out.println("Welcome scene is active, playing video");
                    mediaPlayer.play();
                }
            });
            
        } catch (Exception e) {
            System.err.println("Critical error setting up welcome media view for video '" + videoPathForErrorDisplay[0] + "': " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> displayVideoError(rootPane, videoPathForErrorDisplay[0], e.getMessage()));
            return new MediaView();
        }
        
        return mediaView;
    }

    private void displayVideoError(StackPane rootPane, String videoPathInfo, String specificError) {
        Text errorText = new Text("Video Playback Error\nSource: " + videoPathInfo + "\nDetails: " + specificError);
        errorText.setFill(UIConstants.COLOR_TEXT_OVER_VIDEO_FX);
        errorText.setFont(Font.font(UIConstants.FONT_FAMILY_BODY_WELCOME, 14));
        errorText.setTextAlignment(TextAlignment.CENTER);
        errorText.wrappingWidthProperty().bind(rootPane.widthProperty().subtract(40));
        StackPane errorPane = new StackPane(errorText);
        errorPane.setStyle("-fx-background-color: #550000DD; -fx-border-color: " + UIConstants.COLOR_TEXT_OVER_VIDEO_HEX + "; -fx-border-width: 1px; -fx-background-radius: 8px; -fx-border-radius: 8px;");
        errorPane.setAlignment(Pos.CENTER); errorPane.setPadding(new Insets(20));
        errorPane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        rootPane.getChildren().removeIf(node -> node instanceof MediaView);
        if (rootPane.getChildren().stream().noneMatch(node -> node.equals(errorPane))) {
            rootPane.getChildren().add(errorPane);
        }
        errorPane.toFront();
    }

    public ImageView createGeneralLogoView(double fitWidth) {
        ImageView logoView = new ImageView();
        try {
            URL logoUrl = getClass().getResource(UIConstants.LOGO_RESOURCE_PATH);
            if (logoUrl != null) {
                Image image = new Image(logoUrl.toExternalForm());
                logoView.setImage(image);
                logoView.setFitWidth(fitWidth);
                logoView.setPreserveRatio(true);
            } else {
                System.err.println("General Logo resource not found: " + UIConstants.LOGO_RESOURCE_PATH);
            }
        } catch (Exception e) {
            System.err.println("Error loading general logo: " + e.getMessage());
        }
        return logoView;
    }

    public Stage getPrimaryStage() { return this.primaryStage; }
    public void setCurrentLoggedInUser(Employee employee){ this.currentLoggedInUser = employee; }
    public Employee getCurrentLoggedInUser(){ return this.currentLoggedInUser; }

    public void switchToWelcome() {
        switchSceneWithFade(this.welcomeScene, false, true);
    }

    public void switchToLogin() {
        // loginScene is created in start() by its controller
        switchSceneWithFade(this.loginScene, true, false);
    }

    public void switchToSignUp() {
        // signUpScene is created in start() by its controller
        switchSceneWithFade(this.signUpScene, true, false);
    }

    public void switchToDashboard() {
        System.out.println("switchToDashboard called - Starting dashboard transition");
        if (currentLoggedInUser == null) {
            System.out.println("ERROR: No user logged in");
            DialogUtil.showAlert(Alert.AlertType.ERROR, primaryStage, "Access Denied", "No user logged in. Redirecting to login.");
            switchToLogin();
            return;
        }
        
        System.out.println("User logged in: " + currentLoggedInUser.getUsername() + " (" + currentLoggedInUser.getFirstName() + " " + currentLoggedInUser.getLastName() + ")");
        
        if (dashboardController == null) { // Create if it's the first time or if user changed
            System.out.println("Creating new Dashboard controller");
            dashboardController = new DashboardController(this, currentLoggedInUser);
        } else if (dashboardController.getCurrentUser() != currentLoggedInUser) { // If user changed, re-create
            System.out.println("User changed, recreating Dashboard controller");
            dashboardController = new DashboardController(this, currentLoggedInUser);
        }
        
        this.dashboardScene = dashboardController.createDashboardScene(); // Get/Recreate the scene
        System.out.println("Dashboard scene created, width=" + dashboardScene.getWidth() + ", height=" + dashboardScene.getHeight());
        
        // Use direct scene setting for dashboard to avoid transition issues
        if (currentLoggedInUser != null) {
            System.out.println("Setting dashboard scene directly");
            primaryStage.setScene(dashboardScene);
            System.out.println("Dashboard scene set directly");
        } else {
            switchSceneWithFade(this.dashboardScene, false, false);
        }
        System.out.println("Dashboard transition complete");
    }

    public void logout() {
        currentLoggedInUser = null;
        AuthUIFactory.pauseLogoAnimation();
        if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.stop();
        }
        switchToLogin();
    }

    /**
     * Opens the guest portal for self-service booking and reservation management
     * @param guest The guest account to use for the portal
     */
    public void switchToGuestPortal(Guest guest) {
        if (guest == null) {
            DialogUtil.showAlert(Alert.AlertType.ERROR, primaryStage, 
                "Access Denied", "Guest information not available. Please log in again.");
            switchToLogin();
            return;
        }
        
        System.out.println("Opening guest portal for: " + guest.getFirstName() + " " + guest.getLastName());
        
        try {
            GuestPortalController guestPortalController = new GuestPortalController(this, guest);
            Scene guestPortalScene = guestPortalController.createGuestPortalScene();
            
            // Use fade transition for a smooth switch
            switchSceneWithFade(guestPortalScene, false, false);
            
            // Update window title to reflect guest portal
            primaryStage.setTitle("Raffles Hotels & Resorts - Guest Portal");
            
        } catch (Exception e) {
            System.err.println("Error switching to guest portal: " + e.getMessage());
            e.printStackTrace();
            DialogUtil.showAlert(Alert.AlertType.ERROR, primaryStage, 
                "System Error", "Could not open guest portal. Please try again later.");
        }
    }

    private void switchSceneWithFade(Scene newScene, boolean isTargetAuthScreen, boolean isTargetWelcomeScreen) {
        System.out.println("switchSceneWithFade called with: isTargetAuthScreen=" + isTargetAuthScreen + ", isTargetWelcomeScreen=" + isTargetWelcomeScreen);
        
        if (!Platform.isFxApplicationThread()) {
            System.out.println("Not on FX thread, using Platform.runLater");
            Platform.runLater(() -> switchSceneWithFade(newScene, isTargetAuthScreen, isTargetWelcomeScreen));
            return;
        }
        
        Scene currentScene = primaryStage.getScene();
        Node currentRoot = (currentScene != null) ? currentScene.getRoot() : null;
        System.out.println("Current scene: " + (currentScene == null ? "null" : "not null") + 
                           ", current root: " + (currentRoot == null ? "null" : "not null"));

        Runnable setupNewScene = () -> {
            System.out.println("Setting up new scene");
            primaryStage.setScene(newScene);
            Node newRoot = newScene.getRoot();
            if (newRoot != null) {
                System.out.println("New root found, fading in");
                newRoot.setOpacity(0.0);
                fadeIn(newRoot);
            } else {
                System.out.println("WARNING: New scene has null root!");
            }

            if (isTargetWelcomeScreen && mediaPlayer != null) {
                if (mediaPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
                    mediaPlayer.seek(Duration.ZERO);
                    if (primaryStage.isShowing()) mediaPlayer.play();
                }
            }
            if (isTargetAuthScreen) {
                // The AuthUIFactory's playLogoAnimation should be called by the Auth Scene
                // itself when it becomes visible (e.g., via its setOnShown handler if it has one,
                // or directly if the scene is being created anew each time).
                // For simplicity here, if the newScene is loginScene or signUpScene, call it.
                if(newScene == loginScene || newScene == signUpScene){
                    AuthUIFactory.playLogoAnimation();
                }
            }
            System.out.println("New scene setup complete");
        };

        if (currentRoot != null) {
            if (currentScene == welcomeScene && mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
            }
            if ((currentScene == loginScene || currentScene == signUpScene)) {
                AuthUIFactory.pauseLogoAnimation();
            }

            System.out.println("Fading out current scene");
            FadeTransition fadeOut = new FadeTransition(UIConstants.FADE_DURATION, currentRoot);
            fadeOut.setFromValue(currentRoot.getOpacity());
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(event -> {
                System.out.println("Fade out complete, setting up new scene");
                setupNewScene.run();
            });
            fadeOut.play();
        } else {
            System.out.println("No current scene to fade out, directly setting up new scene");
            setupNewScene.run(); // No current scene to fade out
        }
    }

    private void fadeIn(Node node) {
        FadeTransition fadeIn = new FadeTransition(UIConstants.FADE_DURATION, node);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    @Override
    public void stop() {
        System.out.println("Application stopping - shutting down database properly...");
        DatabaseConnector.closeConnection();
    }

    public static void main(String[] args) {
        try {
            // Check if JavaFX runtime is available
            Class.forName("javafx.application.Application");
            launch(args);
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: JavaFX runtime not found. Please run the application using one of these methods:");
            System.err.println("1. Use the run-raffles.bat file in the project root directory");
            System.err.println("2. Use Maven: mvnw javafx:run");
            System.err.println("3. Set up module path manually: java --module-path \"path/to/javafx-sdk/lib\" " +
                               "--add-modules javafx.controls,javafx.fxml,javafx.media,javafx.graphics " +
                               "-jar raffles-hotel.jar");
            
            // Try to run via Launcher to see if that helps
            try {
                System.err.println("Attempting to launch using Launcher class...");
                Launcher.main(args);
            } catch (Exception ex) {
                System.err.println("Launcher attempt also failed. Please use one of the methods above.");
            }
        }
    }
}