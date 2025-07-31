package com.mycompany.manufacturing_system;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;

public class ProductionManagementView {
    private User currentUser;
    private MainApp mainApp;

    public ProductionManagementView(User user, MainApp mainApp) {
        this.currentUser = user;
        this.mainApp = mainApp;
    }

    public VBox getView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f0f2f5;");

        Label title = new Label("Production Management");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 30));
        title.setTextFill(Color.web("#2c3e50"));

        Label description = new Label("This section will allow you to create, view, and manage production orders and lines.");
        description.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        description.setTextFill(Color.web("#7f8c8d"));
        description.setWrapText(true);
        description.setMaxWidth(600);
        description.setAlignment(Pos.CENTER);

        // Tombol kembali ke Main Menu
        Button backButton = new Button("← Back to Main Menu");
        backButton.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        backButton.setStyle("""
            -fx-background-color: #3498db;
            -fx-text-fill: white;
            -fx-background-radius: 8;
            -fx-border-radius: 8;
            -fx-padding: 10 20;
            -fx-cursor: hand;
            """);
        backButton.setOnAction(e -> mainApp.goBackToMainMenu());

        root.getChildren().addAll(title, description, backButton);
        return root;
    }
}
 