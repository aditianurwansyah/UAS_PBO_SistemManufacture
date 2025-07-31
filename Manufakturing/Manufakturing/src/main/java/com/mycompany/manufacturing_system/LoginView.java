package com.mycompany.manufacturing_system;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import java.sql.SQLException;

public class LoginView {
    private Stage primaryStage;
    private MainApp App;
    private ManufacturingOperations manufacturingOps; // Ini akan digunakan untuk autentikasi
    private static User currentUserInstance; // Untuk menyimpan user yang login

    public LoginView(Stage primaryStage, MainApp mainApp) { 
        this.primaryStage = primaryStage;
        this.App = mainApp;
        try {
            this.manufacturingOps = new ManufacturingOperations(); // Inisialisasi ManufacturingOperations
        } catch (SQLException e) {
            showError("Database Error", "Failed to initialize Manufacturing Operations: " + e.getMessage());
        }
    }

    LoginView(Stage stage) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public VBox getView() {
        VBox loginBox = new VBox(20);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setPadding(new Insets(30));
        loginBox.setStyle("""
            -fx-background-color: linear-gradient(to top left, #ece9e6, #ffffff);
            """);

        VBox loginCard = new VBox(20);
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setPadding(new Insets(30));
        loginCard.setMaxWidth(350);
        loginCard.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 15;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 5);
            """);

        Text title = new Text("Login");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setFill(Color.web("#34495e"));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefHeight(40);
        usernameField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #bdc3c7;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefHeight(40);
        passwordField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #bdc3c7;");

        Button loginButton = new Button("Sign In");
        loginButton.setPrefWidth(150);
        loginButton.setPrefHeight(45);
        loginButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        loginButton.setStyle("""
            -fx-background-color: #3498db;
            -fx-text-fill: white;
            -fx-background-radius: 10;
            -fx-cursor: hand;
            """);
        loginButton.setOnMouseEntered(e -> loginButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand;"));
        loginButton.setOnMouseExited(e -> loginButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand;"));

        loginButton.setOnAction(e -> handleLogin(usernameField.getText(), passwordField.getText()));

        loginCard.getChildren().addAll(title, usernameField, passwordField, loginButton);
        loginBox.getChildren().add(loginCard);

        // Animasi fade-in untuk login card
        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), loginCard);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        return loginBox;
    }

    private void handleLogin(String username, String password) {
        if (manufacturingOps == null) {
            showError("Database Error", "Database connection not established.");
            return;
        }

        try {
            // Menggunakan ManufacturingOperations untuk autentikasi
            User user = manufacturingOps.authenticateUser(username, password);
            if (user != null) {
                currentUserInstance = user; // Set user yang berhasil login
                showInfo("Login Successful", "Welcome, " + user.getDisplayName() + "!");
                App.showMainMenuView(user); // Pindah ke main menu
            } else {
                showError("Login Failed", "Invalid username or password.");
            }
        } catch (SQLException e) {
            showError("Database Error", "Error during login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static User getCurrentUser() {
        return currentUserInstance;
    }

    public static void clearCurrentUser() {
        currentUserInstance = null;
    }
}
 