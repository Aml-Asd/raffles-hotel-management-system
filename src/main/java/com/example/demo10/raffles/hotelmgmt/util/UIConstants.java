package com.example.demo10.raffles.hotelmgmt.ui;

import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.scene.effect.BlurType;

public class UIConstants {
    // Fonts
    public static final String FONT_FAMILY_TITLE_WELCOME = "Dancing Script";
    public static final String FONT_FAMILY_BODY_WELCOME = "Dancing Script";
    public static final String FONT_SERIF_ELEGANT = "Georgia";
    public static final String FONT_SANS_SERIF_CLEAN = "Segoe UI";
    public static final String FONT_LUXURY = "Didot";
    
    // Colors
    public static final String COLOR_TEXT_OVER_VIDEO_HEX = "#FFFFFF";
    public static final Color COLOR_TEXT_OVER_VIDEO_FX = Color.web(COLOR_TEXT_OVER_VIDEO_HEX);
    public static final String VIDEO_OVERLAY_COLOR_RGBA = "rgba(0, 0, 0, 0.45)";
    public static final String BRAND_COLOR_HEX = "#97866A"; // Gold/beige
    public static final Color BRAND_COLOR_FX = Color.web(BRAND_COLOR_HEX);
    public static final String ACCENT_COLOR_DARK_BROWN_HEX = "#5C503A";
    public static final Color ACCENT_COLOR_DARK_BROWN_FX = Color.web(ACCENT_COLOR_DARK_BROWN_HEX);
    public static final String ACCENT_COLOR_LIGHT_CREAM_HEX = "#FDFBF5";
    public static final String ACCENT_COLOR_MID_BEIGE_HEX = "#EDE7DA";
    public static final String GOLD_COLOR_HEX = "#D4AF37"; // Luxury gold
    public static final Color GOLD_COLOR_FX = Color.web(GOLD_COLOR_HEX);
    public static final String DARK_GOLD_COLOR_HEX = "#85754E";
    public static final Color DARK_GOLD_COLOR_FX = Color.web(DARK_GOLD_COLOR_HEX);
    public static final String ERROR_COLOR_HEX = "#D32F2F";
    public static final Color ERROR_COLOR_FX = Color.web(ERROR_COLOR_HEX);
    public static final String SUCCESS_COLOR_HEX = "#388E3C";
    public static final Color SUCCESS_COLOR_FX = Color.web(SUCCESS_COLOR_HEX);
    
    // Effects
    public static final String SHADOW_EFFECT_CSS = "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0.2, 0, 3);";
    public static final String LUXURY_SHADOW_EFFECT_CSS = "-fx-effect: dropshadow(three-pass-box, rgba(212,175,55,0.25), 15, 0.2, 0, 4);";
    public static final String GOLD_BORDER_EFFECT = "-fx-border-color: linear-gradient(to bottom right, #D4AF37, #85754E, #D4AF37); -fx-border-width: 1.5px; -fx-border-radius: 6px;";
    
    // Component styles
    public static final String CARD_STYLE_CSS = "-fx-background-color: white; -fx-background-radius: 8px; " + SHADOW_EFFECT_CSS;
    public static final String LUXURY_CARD_STYLE_CSS = "-fx-background-color: white; -fx-background-radius: 8px; " + 
                                                     LUXURY_SHADOW_EFFECT_CSS + GOLD_BORDER_EFFECT;
    public static final String FIELD_STYLE_AUTH_CSS = "-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-prompt-text-fill: #E0E0E0; -fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: rgba(255,255,255,0.4); -fx-font-family: '" + FONT_SANS_SERIF_CLEAN + "'; -fx-font-size: 15px; -fx-padding: 12px;";
    public static final String LABEL_STYLE_AUTH_CSS = "-fx-text-fill: white; -fx-font-family: '" + FONT_SANS_SERIF_CLEAN + "'; -fx-font-size: 14px; -fx-padding: 0 0 6px 0;";
    public static final String FIELD_STYLE_DIALOG_CSS = "-fx-background-color: white; -fx-border-color: #D0C0A0; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-font-family: '" + FONT_SANS_SERIF_CLEAN + "'; -fx-font-size: 14px; -fx-padding: 10px;" + SHADOW_EFFECT_CSS.replace("0, 3", "0, 1");
    public static final String LABEL_STYLE_DIALOG_CSS = "-fx-font-family: '" + FONT_SANS_SERIF_CLEAN + "'; -fx-font-size: 14px; -fx-text-fill: " + ACCENT_COLOR_DARK_BROWN_HEX + ";";
    
    // Button styles
    public static final String LUXURY_BUTTON_STYLE = "-fx-background-color: linear-gradient(to bottom right, " + GOLD_COLOR_HEX + ", " + DARK_GOLD_COLOR_HEX + "); " +
                                                 "-fx-text-fill: white; " +
                                                 "-fx-font-family: '" + FONT_SERIF_ELEGANT + "'; " +
                                                 "-fx-font-size: 14px; " +
                                                 "-fx-padding: 12 25; " +
                                                 "-fx-background-radius: 30; " +
                                                 "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0.2, 0, 3);";
    
    public static final String LUXURY_BUTTON_HOVER_STYLE = "-fx-background-color: linear-gradient(to bottom right, " + DARK_GOLD_COLOR_HEX + ", " + GOLD_COLOR_HEX + "); " +
                                                      "-fx-effect: dropshadow(gaussian, rgba(212,175,55,0.4), 15, 0.3, 0, 5);";
    
    // Common button styles for consistent UI
    public static final String BUTTON_STYLE_PRIMARY_ACTION = "-fx-background-color: " + BRAND_COLOR_HEX + "; -fx-text-fill: white; -fx-font-family: '" 
            + FONT_SANS_SERIF_CLEAN + "'; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-radius: 5px; " 
            + SHADOW_EFFECT_CSS;
            
    public static final String BUTTON_STYLE_PRIMARY_ACTION_HOVER = "-fx-background-color: " + ACCENT_COLOR_DARK_BROWN_HEX + ";";
    
    public static final String BUTTON_STYLE_DANGER_ACTION = "-fx-background-color: " + ERROR_COLOR_HEX + "; -fx-text-fill: white; -fx-font-family: '"
            + FONT_SANS_SERIF_CLEAN + "'; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-radius: 5px; " 
            + SHADOW_EFFECT_CSS;
            
    public static final String BUTTON_STYLE_DANGER_ACTION_HOVER = "-fx-background-color: #A83939;";
    
    // Guest portal specific button styles
    public static final String STANDARD_BUTTON_STYLE = "-fx-background-color: " + BRAND_COLOR_HEX + "; -fx-text-fill: white; -fx-font-family: '" 
            + FONT_SANS_SERIF_CLEAN + "'; -fx-font-size: 14px; -fx-padding: 8px 15px; -fx-background-radius: 4px; "
            + SHADOW_EFFECT_CSS;
            
    public static final String SMALL_BUTTON_STYLE = "-fx-background-color: " + BRAND_COLOR_HEX + "; -fx-text-fill: white; -fx-font-family: '" 
            + FONT_SANS_SERIF_CLEAN + "'; -fx-font-size: 12px; -fx-padding: 5px 10px; -fx-background-radius: 3px; "
            + SHADOW_EFFECT_CSS;
            
    public static final String SMALL_ERROR_BUTTON_STYLE = "-fx-background-color: " + ERROR_COLOR_HEX + "; -fx-text-fill: white; -fx-font-family: '"
            + FONT_SANS_SERIF_CLEAN + "'; -fx-font-size: 12px; -fx-padding: 5px 10px; -fx-background-radius: 3px; " 
            + SHADOW_EFFECT_CSS;
    
    // Animations
    public static final Duration FADE_DURATION = Duration.millis(400);
    public static final Duration LUXURY_TRANSITION_DURATION = Duration.millis(600);
    public static final Duration BUTTON_HOVER_DURATION = Duration.millis(200);
    
    // Layout
    public static final double INITIAL_WIDTH = 1200;
    public static final double INITIAL_HEIGHT = 800;
    public static final double MIN_WIDTH = 950;
    public static final double MIN_HEIGHT = 700;
    
    // Resources
    public static final String LOGO_RESOURCE_PATH = "/images/b67a0d3016d6e9fa6428df96ab13d33f.jpg";
    public static final String VIDEO_RESOURCE_PATH_FALLBACK = "/videos/raffles.mp4";
    public static final String VIDEO_ABSOLUTE_PATH = "src/raffles.mp4";
}