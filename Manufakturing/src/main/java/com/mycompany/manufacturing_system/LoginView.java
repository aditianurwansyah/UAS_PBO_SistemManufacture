package com.mycompany.manufacturing_system;

import java.sql.SQLException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.effect.DropShadow;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

public class LoginView {
    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton; // Store reference to login button
    private final LoginHandler loginHandler;
    private final Stage primaryStage;
    private static User currentUser;

    public LoginView(Stage primaryStage) throws SQLException {
        this.primaryStage = primaryStage;
        this.loginHandler = new LoginHandler();
    }
    
    public VBox getView() {
        VBox mainContainer = new VBox(20);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(40));
        mainContainer.setStyle("-fx-background-color: linear-gradient(135deg, #667eea 0%, #764ba2 100%);");

        // Create login card
        VBox loginCard = createLoginCard();
        
        // Add fade-in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), loginCard);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        mainContainer.getChildren().add(loginCard);
        return mainContainer;
    }
    
    private VBox createLoginCard() {
        VBox loginCard = new VBox(20);
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setPadding(new Insets(40));
        loginCard.setMaxWidth(420);
        loginCard.setStyle("""
            -fx-background-color: rgba(255, 255, 255, 0.95);
            -fx-background-radius: 15;
            -fx-border-radius: 15;
            """);
        
        // Add drop shadow effect
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));
        shadow.setRadius(15);
        shadow.setOffsetY(8);
        loginCard.setEffect(shadow);

        // Header section
        VBox headerSection = createHeaderSection();
        
        // Login form
        VBox loginForm = createLoginForm();
        
        // Default credentials info
        VBox credentialsInfo = createCredentialsInfo();
        
        // Footer section
        VBox footerSection = createFooterSection();

        loginCard.getChildren().addAll(
            headerSection,
            createSeparator(),
            loginForm,
            credentialsInfo,
            createSeparator(),
            footerSection
        );

        return loginCard;
    }
    
    private VBox createHeaderSection() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        
        // App icon/logo placeholder
        Label iconLabel = new Label("🏭");
        iconLabel.setFont(Font.font(48));
        
        // Title
        Label titleLabel = new Label("Manufacturing System");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");

        // Subtitle
        Label subtitleLabel = new Label("Please sign in to continue");
        subtitleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        subtitleLabel.setStyle("-fx-text-fill: #7f8c8d;");

        header.getChildren().addAll(iconLabel, titleLabel, subtitleLabel);
        return header;
    }
    
    private VBox createLoginForm() {
        VBox form = new VBox(15);
        form.setAlignment(Pos.CENTER);
        
        // Username field
        VBox usernameBox = new VBox(5);
        Label usernameLabel = new Label("Username:");
        usernameLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        usernameLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        usernameField = createStyledTextField("Enter your username");
        usernameBox.getChildren().addAll(usernameLabel, usernameField);
        
        // Password field
        VBox passwordBox = new VBox(5);
        Label passwordLabel = new Label("Password:");
        passwordLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        passwordLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        passwordField = createStyledPasswordField();
        passwordBox.getChildren().addAll(passwordLabel, passwordField);

        // Login button
        loginButton = createStyledButton("LOGIN", "#3498db");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setOnAction(e -> handleLogin());
        
        // Handle Enter key press in password field
        passwordField.setOnAction(e -> handleLogin());
        usernameField.setOnAction(e -> passwordField.requestFocus());

        form.getChildren().addAll(usernameBox, passwordBox, loginButton);
        return form;
    }
    
    private VBox createCredentialsInfo() {
        VBox credentialsBox = new VBox(10);
        credentialsBox.setAlignment(Pos.CENTER);
        credentialsBox.setPadding(new Insets(10));
        credentialsBox.setStyle("""
            -fx-background-color: #ecf0f1;
            -fx-background-radius: 8;
            -fx-border-radius: 8;
            """);
        
        Label infoTitle = new Label("📋 Default Login Credentials");
        infoTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        infoTitle.setStyle("-fx-text-fill: #2c3e50;");
        
        Label adminInfo = new Label("👨‍💼 Admin: username='admin' password='admin123'");
        adminInfo.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        adminInfo.setStyle("-fx-text-fill: #e74c3c;");
        
        Label userInfo = new Label("👤 User: username='user' password='user123'");
        userInfo.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        userInfo.setStyle("-fx-text-fill: #27ae60;");
        
        credentialsBox.getChildren().addAll(infoTitle, adminInfo, userInfo);
        return credentialsBox;
    }
    
    private VBox createFooterSection() {
        VBox footer = new VBox(10);
        footer.setAlignment(Pos.CENTER);
        
        Button registerButton = createStyledButton("Create New Account", "#27ae60");
        registerButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setOnAction(e -> showRegistrationDialog());
        
        // System info
        Label systemInfo = new Label("Manufacturing Management System v2.0");
        systemInfo.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10));
        systemInfo.setStyle("-fx-text-fill: #95a5a6;");
        
        footer.getChildren().addAll(registerButton, systemInfo);
        return footer;
    }

    private TextField createStyledTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 8;
            -fx-border-radius: 8;
            -fx-border-color: #bdc3c7;
            -fx-border-width: 1;
            -fx-padding: 12 15;
            -fx-font-size: 14px;
            -fx-pref-height: 40;
            """);
        field.setMaxWidth(Double.MAX_VALUE);
        
        // Add focus effects
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle("""
                    -fx-background-color: white;
                    -fx-background-radius: 8;
                    -fx-border-radius: 8;
                    -fx-border-color: #3498db;
                    -fx-border-width: 2;
                    -fx-padding: 12 15;
                    -fx-font-size: 14px;
                    -fx-pref-height: 40;
                    """);
            } else {
                field.setStyle("""
                    -fx-background-color: white;
                    -fx-background-radius: 8;
                    -fx-border-radius: 8;
                    -fx-border-color: #bdc3c7;
                    -fx-border-width: 1;
                    -fx-padding: 12 15;
                    -fx-font-size: 14px;
                    -fx-pref-height: 40;
                    """);
            }
        });
        
        return field;
    }

    private PasswordField createStyledPasswordField() {
        PasswordField field = new PasswordField();
        field.setPromptText("Enter your password");
        field.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 8;
            -fx-border-radius: 8;
            -fx-border-color: #bdc3c7;
            -fx-border-width: 1;
            -fx-padding: 12 15;
            -fx-font-size: 14px;
            -fx-pref-height: 40;
            """);
        field.setMaxWidth(Double.MAX_VALUE);
        
        // Add focus effects
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle("""
                    -fx-background-color: white;
                    -fx-background-radius: 8;
                    -fx-border-radius: 8;
                    -fx-border-color: #3498db;
                    -fx-border-width: 2;
                    -fx-padding: 12 15;
                    -fx-font-size: 14px;
                    -fx-pref-height: 40;
                    """);
            } else {
                field.setStyle("""
                    -fx-background-color: white;
                    -fx-background-radius: 8;
                    -fx-border-radius: 8;
                    -fx-border-color: #bdc3c7;
                    -fx-border-width: 1;
                    -fx-padding: 12 15;
                    -fx-font-size: 14px;
                    -fx-pref-height: 40;
                    """);
            }
        });
        
        return field;
    }
    
    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        button.setPrefHeight(45);
        button.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-background-radius: 8;
            -fx-border-radius: 8;
            -fx-cursor: hand;
            """, color));
        
        // Hover effects
        button.setOnMouseEntered(e -> {
            button.setStyle(String.format("""
                -fx-background-color: derive(%s, -10%%);
                -fx-text-fill: white;
                -fx-background-radius: 8;
                -fx-border-radius: 8;
                -fx-cursor: hand;
                """, color));
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-text-fill: white;
                -fx-background-radius: 8;
                -fx-border-radius: 8;
                -fx-cursor: hand;
                """, color));
        });
        
        return button;
    }
    
    private Separator createSeparator() {
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #ecf0f1;");
        return separator;
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Login Error", "Please enter both username and password", Alert.AlertType.WARNING);
            return;
        }

        // Show loading state using stored reference
        String originalText = loginButton.getText();
        loginButton.setText("Logging in...");
        loginButton.setDisable(true);

        // Simulate loading delay and authenticate
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Simulate network delay
                
                javafx.application.Platform.runLater(() -> {
                    User user = loginHandler.authenticate(username, password);
                    
                    loginButton.setText(originalText);
                    loginButton.setDisable(false);
                    
                    if (user != null && user.isActive()) {
                        currentUser = user;
                        showSuccessMessage("Welcome " + user.getDisplayName() + "!");
                        navigateToMainMenu(user);
                    } else {
                        showAlert("Login Failed", 
                            "Invalid username or password, or account is inactive.\n\n" +
                            "Please check your credentials and try again.", 
                            Alert.AlertType.ERROR);
                        // Clear password field for security
                        passwordField.clear();
                        usernameField.requestFocus();
                    }
                });
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void navigateToMainMenu(User user) {
        try {
            ModernMainMenuView mainMenuView = new ModernMainMenuView(primaryStage, user);
            Scene mainMenuScene = new Scene(mainMenuView.getView(), 800, 600);
            
            // Add fade transition
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), primaryStage.getScene().getRoot());
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                primaryStage.setScene(mainMenuScene);
                primaryStage.setTitle("Manufacturing Management System - " + user.getRoleDisplayName());
                primaryStage.centerOnScreen();
                
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), mainMenuScene.getRoot());
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            });
            fadeOut.play();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load main menu: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showRegistrationDialog() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Create New Account");
        dialog.setHeaderText("Register a new user account");

        // Create registration form
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        TextField regUsername = new TextField();
        regUsername.setPromptText("Username");
        
        PasswordField regPassword = new PasswordField();
        regPassword.setPromptText("Password");
        
        PasswordField confirmPassword = new PasswordField();
        confirmPassword.setPromptText("Confirm Password");
        
        TextField fullName = new TextField();
        fullName.setPromptText("Full Name");
        
        TextField email = new TextField();
        email.setPromptText("Email (optional)");
        
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("USER", "ADMIN");
        roleCombo.setValue("USER");
        roleCombo.setPromptText("Select Role");
        
        ComboBox<String> departmentCombo = new ComboBox<>();
        departmentCombo.getItems().addAll("Production", "Quality Control", "Maintenance", 
                                         "Logistics", "Administration", "IT");
        departmentCombo.setValue("Production");
        departmentCombo.setPromptText("Select Department");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(regUsername, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(regPassword, 1, 1);
        grid.add(new Label("Confirm Password:"), 0, 2);
        grid.add(confirmPassword, 1, 2);
        grid.add(new Label("Full Name:"), 0, 3);
        grid.add(fullName, 1, 3);
        grid.add(new Label("Email:"), 0, 4);
        grid.add(email, 1, 4);
        grid.add(new Label("Role:"), 0, 5);
        grid.add(roleCombo, 1, 5);
        grid.add(new Label("Department:"), 0, 6);
        grid.add(departmentCombo, 1, 6);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Validation
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                // Validate inputs
                if (regUsername.getText().trim().isEmpty()) {
                    showAlert("Validation Error", "Username is required", Alert.AlertType.ERROR);
                    return null;
                }
                
                if (regPassword.getText().isEmpty()) {
                    showAlert("Validation Error", "Password is required", Alert.AlertType.ERROR);
                    return null;
                }
                
                if (!regPassword.getText().equals(confirmPassword.getText())) {
                    showAlert("Validation Error", "Passwords do not match", Alert.AlertType.ERROR);
                    return null;
                }
                
                if (fullName.getText().trim().isEmpty()) {
                    showAlert("Validation Error", "Full name is required", Alert.AlertType.ERROR);
                    return null;
                }
                
                // Check if username already exists
                if (loginHandler.usernameExists(regUsername.getText().trim())) {
                    showAlert("Registration Error", "Username already exists", Alert.AlertType.ERROR);
                    return null;
                }
                
                return new User(
                    regUsername.getText().trim(),
                    regPassword.getText(),
                    roleCombo.getValue(),
                    fullName.getText().trim(),
                    email.getText().trim(),
                    departmentCombo.getValue()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(user -> {
            if (loginHandler.registerUser(user)) {
                showAlert("Registration Successful", 
                    "Account created successfully!\n\n" +
                    "Username: " + user.getUsername() + "\n" +
                    "Role: " + user.getRoleDisplayName() + "\n\n" +
                    "You can now log in with your credentials.", 
                    Alert.AlertType.INFORMATION);
                
                // Pre-fill username field
                usernameField.setText(user.getUsername());
                passwordField.requestFocus();
            } else {
                showAlert("Registration Failed", 
                    "Failed to create account. Please try again.", 
                    Alert.AlertType.ERROR);
            }
        });
    }
    
    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Login Successful");
        alert.setHeaderText("Welcome!");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        
        // Style the alert dialog
        alert.getDialogPane().setStyle("""
            -fx-background-color: white;
            -fx-font-family: 'Segoe UI';
            """);
        
        alert.showAndWait();
    }

    public static User getCurrentUser() {
        return currentUser;
    }
    
    public static void setCurrentUser(User user) {
        currentUser = user;
    }
    
    public static void clearCurrentUser() {
        currentUser = null;
    }
}