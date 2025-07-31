package com.mycompany.manufacturing_system;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

/**
 * Enhanced Modern Main Menu View with all implemented features
 */
public class ModernMainMenuView {
    private Stage primaryStage;
    private Scene previousScene;
    private User currentUser;

    public ModernMainMenuView(Stage primaryStage, User user) {
        this.primaryStage = primaryStage;
        this.currentUser = user;
    }

    public VBox getView() {
        VBox menuBox = new VBox(30);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setPadding(new Insets(40));
        menuBox.setStyle("""
            -fx-background-color: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            """);

        // Create main menu card
        VBox menuCard = createMenuCard();
        
        // Add fade-in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), menuCard);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        menuBox.getChildren().add(menuCard);
        return menuBox;
    }

    private VBox createMenuCard() {
        VBox menuCard = new VBox(25);
        menuCard.setAlignment(Pos.CENTER);
        menuCard.setPadding(new Insets(40));
        menuCard.setMaxWidth(900);
        menuCard.setStyle("""
            -fx-background-color: rgba(255, 255, 255, 0.95);
            -fx-background-radius: 20;
            -fx-border-radius: 20;
            """);

        // Add drop shadow
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));
        shadow.setRadius(20);
        shadow.setOffsetY(10);
        menuCard.setEffect(shadow);

        // Header section
        VBox headerSection = createHeaderSection();
        
        // Navigation grid
        GridPane navigationGrid = createNavigationGrid();
        
        // Footer section
        HBox footerSection = createFooterSection();

        menuCard.getChildren().addAll(headerSection, createStyledSeparator(), navigationGrid, footerSection);
        return menuCard;
    }

    private VBox createHeaderSection() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        
        // Main title with modern styling
        Text titleText = new Text("🏭 Manufacturing Control Center");
        titleText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleText.setFill(Color.web("#2c3e50"));
        
        // Welcome message
        Text welcomeText = new Text("Welcome back, " + currentUser.getDisplayName());
        welcomeText.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        welcomeText.setFill(Color.web("#7f8c8d"));
        
        // Role badge
        Label roleBadge = createRoleBadge(currentUser.getRole());

        header.getChildren().addAll(titleText, welcomeText, roleBadge);
        return header;
    }

    private Label createRoleBadge(String role) {
        Label badge = new Label(role);
        badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        badge.setPadding(new Insets(5, 15, 5, 15));
        
        String color = switch (role.toUpperCase()) {
            case "ADMIN" -> "#e74c3c";
            case "MANAGER" -> "#f39c12";
            case "SUPERVISOR" -> "#3498db";
            case "OPERATOR" -> "#27ae60";
            default -> "#95a5a6";
        };
        
        badge.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-background-radius: 15;
            """, color));
        
        return badge;
    }

    private GridPane createNavigationGrid() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(25);
        grid.setVgap(25);
        grid.setPadding(new Insets(20));

        // Create navigation buttons based on user role
        if ("ADMIN".equals(currentUser.getRole()) || "MANAGER".equals(currentUser.getRole())) {
            // Dashboard - Available for Admin and Manager
            VBox dashboardBtn = createNavigationButton("📊", "Production Dashboard", 
                "Monitor real-time production metrics", "#3498db", () -> showProductionDashboard());
            grid.add(dashboardBtn, 0, 0);

            // Production Management
            VBox productionBtn = createNavigationButton("🏭", "Production Orders", 
                "Create and manage production orders", "#e74c3c", () -> showProductionManagement());
            grid.add(productionBtn, 1, 0);

            // Quality Control
            VBox qualityBtn = createNavigationButton("🔍", "Quality Control", 
                "Quality inspections and defect tracking", "#f39c12", () -> showQualityControl());
            grid.add(qualityBtn, 2, 0);

            // Inventory Management
            VBox inventoryBtn = createNavigationButton("📦", "Inventory", 
                "Raw materials and finished goods", "#9b59b6", () -> showInventoryManagement());
            grid.add(inventoryBtn, 0, 1);

            // Reports & Analytics
            VBox reportsBtn = createNavigationButton("📈", "Reports & Analytics", 
                "Production reports and business intelligence", "#27ae60", () -> showReports());
            grid.add(reportsBtn, 1, 1);

            // User Management (Admin only)
            if ("ADMIN".equals(currentUser.getRole())) {
                VBox userMgmtBtn = createNavigationButton("👥", "User Management", 
                    "Manage system users and permissions", "#34495e", () -> showUserManagement());
                grid.add(userMgmtBtn, 2, 1);
            }
        } else {
            // For Operators and Supervisors - Limited access
            VBox myTasksBtn = createNavigationButton("📋", "My Tasks", 
                "View assigned production tasks", "#3498db", () -> showMyTasks());
            grid.add(myTasksBtn, 0, 0);

            VBox productionBtn = createNavigationButton("🏭", "Production Line", 
                "Production line operations", "#e74c3c", () -> showProductionLine());
            grid.add(productionBtn, 1, 0);

            VBox qualityBtn = createNavigationButton("✅", "Quality Check", 
                "Quality control tasks", "#f39c12", () -> showQualityTasks());
            grid.add(qualityBtn, 2, 0);

            VBox inventoryBtn = createNavigationButton("📦", "Inventory Status", 
                "View inventory levels", "#9b59b6", () -> showInventoryStatus());
            grid.add(inventoryBtn, 0, 1);

            VBox reportsBtn = createNavigationButton("📊", "My Reports", 
                "Personal reports and metrics", "#27ae60", () -> showMyReports());
            grid.add(reportsBtn, 1, 1);
        }

        return grid;
    }

    private VBox createNavigationButton(String icon, String title, String description, 
                                       String color, Runnable action) {
        VBox button = new VBox(12);
        button.setAlignment(Pos.CENTER);
        button.setPadding(new Insets(25));
        button.setPrefWidth(200);
        button.setPrefHeight(160);
        button.setStyle(String.format("""
            -fx-background-color: white;
            -fx-background-radius: 15;
            -fx-border-radius: 15;
            -fx-border-color: %s;
            -fx-border-width: 2;
            -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);
            """, color));

        // Icon
        Text iconText = new Text(icon);
        iconText.setFont(Font.font(36));

        // Title
        Text titleText = new Text(title);
        titleText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        titleText.setFill(Color.web("#2c3e50"));
        titleText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // Description
        Text descText = new Text(description);
        descText.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        descText.setFill(Color.web("#7f8c8d"));
        descText.setWrappingWidth(170);
        descText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        button.getChildren().addAll(iconText, titleText, descText);

        // Add hover animations
        button.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), button);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();
            
            button.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-background-radius: 15;
                -fx-border-radius: 15;
                -fx-border-color: %s;
                -fx-border-width: 2;
                -fx-cursor: hand;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 5);
                """, color + "20", color));
            
            titleText.setFill(Color.web(color));
        });

        button.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), button);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
            
            button.setStyle(String.format("""
                -fx-background-color: white;
                -fx-background-radius: 15;
                -fx-border-radius: 15;
                -fx-border-color: %s;
                -fx-border-width: 2;
                -fx-cursor: hand;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);
                """, color));
            
            titleText.setFill(Color.web("#2c3e50"));
        });

        button.setOnMouseClicked(e -> action.run());

        return button;
    }

    private HBox createFooterSection() {
        HBox footer = new HBox(20);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(15));

        // Settings button
        Button settingsBtn = createFooterButton("⚙️ Settings", "#95a5a6", () -> showSettings());
        
        // Help button  
        Button helpBtn = createFooterButton("❓ Help", "#3498db", () -> showHelp());
        
        // Logout button
        Button logoutBtn = createFooterButton("🚪 Logout", "#e74c3c", () -> handleLogout());

        footer.getChildren().addAll(settingsBtn, helpBtn, logoutBtn);
        return footer;
    }

    private Button createFooterButton(String text, String color, Runnable action) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        button.setPrefWidth(130);
        button.setPrefHeight(40);
        button.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-background-radius: 8;
            -fx-border-radius: 8;
            -fx-cursor: hand;
            """, color));

        button.setOnMouseEntered(e -> {
            button.setStyle(String.format("""
                -fx-background-color: derive(%s, -15%%);
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

        button.setOnAction(e -> action.run());
        return button;
    }

    private Separator createStyledSeparator() {
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #ecf0f1; -fx-pref-height: 2;");
        separator.setPadding(new Insets(10, 0, 10, 0));
        return separator;
    }

    // Navigation methods - Enhanced with new features
    private void showProductionDashboard() {
        try {
            ProductionDashboardView dashboardView = new ProductionDashboardView(currentUser);
            VBox dashboardContent = dashboardView.getView();
            switchToScene(dashboardContent, "Production Dashboard");
        } catch (Exception e) {
            showError("Error", "Failed to load Production Dashboard: " + e.getMessage());
        }
    }

    private void showProductionManagement() {
        try {
            ProductionManagementView productionView = new ProductionManagementView(currentUser);
            VBox productionContent = productionView.getView();
            switchToScene(productionContent, "Production Management");
        } catch (Exception e) {
            showError("Error", "Failed to load Production Management: " + e.getMessage());
        }
    }

    private void showQualityControl() {
        try {
              QualityControlView qualityView = new QualityControlView(currentUser);
            VBox qualityContent = qualityView.getView();
            switchToScene(qualityContent, "Quality Control");
        } catch (Exception e) {
            showError("Error", "Failed to load Quality Control: " + e.getMessage());
        }
    }

    private void showInventoryManagement() {
        try {
            InventoryManagementView inventoryView = new InventoryManagementView(currentUser);
            VBox inventoryContent = inventoryView.getView();
            switchToScene(inventoryContent, "Inventory Management");
        } catch (Exception e) {
            showError("Error", "Failed to load Inventory Management: " + e.getMessage());
        }
    }

    private void showReports() {
        try {
             ReportsAnalyticsView reportsView = new ReportsAnalyticsView(currentUser);
            VBox reportsContent = reportsView.getView();
            switchToScene(reportsContent, "Reports & Analytics");
        } catch (Exception e) {
            showError("Error", "Failed to load Reports & Analytics: " + e.getMessage());
        }
    }

    private void showUserManagement() {
        try {
            UserManagementView userMgmtView = new UserManagementView(currentUser);
            VBox userMgmtContent = userMgmtView.getView();
            switchToScene(userMgmtContent, "User Management");
        } catch (Exception e) {
            showError("Error", "Failed to load User Management: " + e.getMessage());
        }
    }

    private void showMyTasks() {
        try {
            MyTasksView tasksView = new MyTasksView(currentUser);
            VBox tasksContent = tasksView.getView();
            switchToScene(tasksContent, "My Tasks");
        } catch (Exception e) {
            showError("Error", "Failed to load My Tasks: " + e.getMessage());
        }
    }

    private void showProductionLine() {
        showInfo("Production Line", "Production Line interface is available through Production Management.");
    }

    private void showQualityTasks() {
        try {
            QualityControlView qualityView = new QualityControlView(currentUser);
            VBox qualityContent = qualityView.getView();
            switchToScene(qualityContent, "Quality Tasks");
        } catch (Exception e) {
            showError("Error", "Failed to load Quality Tasks: " + e.getMessage());
        }
    }

    private void showInventoryStatus() {
        try {
            InventoryManagementView inventoryView = new InventoryManagementView(currentUser);
            VBox inventoryContent = inventoryView.getView();
            switchToScene(inventoryContent, "Inventory Status");
        } catch (Exception e) {
            showError("Error", "Failed to load Inventory Status: " + e.getMessage());
        }
    }

    private void showMyReports() {
        try {
            ReportsAnalyticsView reportsView = new ReportsAnalyticsView(currentUser);
            VBox reportsContent = reportsView.getView();
            switchToScene(reportsContent, "My Reports");
        } catch (Exception e) {
            showError("Error", "Failed to load My Reports: " + e.getMessage());
        }
    }

    private void showSettings() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("System Settings");
        alert.setHeaderText("Application Settings");
        alert.setContentText("""
            System Configuration:
            • Database: Connected to manufacturing_system
            • User Role: %s
            • System Version: 2.0
            • Features: Production, Quality, Inventory, Reports
            
            Settings panel will be available in future updates.
            """.formatted(currentUser.getRoleDisplayName()));
        alert.showAndWait();
    }

    private void showHelp() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Help & Support");
        alert.setHeaderText("Manufacturing System Help");
        alert.setContentText("""
            Welcome to the Manufacturing Management System!
            
            📊 Dashboard: Real-time production monitoring
            🏭 Production: Create and manage orders
            🔍 Quality: Inspections and defect tracking
            📦 Inventory: Stock management and tracking
            📈 Reports: Analytics and business intelligence
            👥 User Management: System user administration (Admin only)
            
            Features by Role:
            • Admin: Full system access
            • Manager: Production and reporting access
            • User: Limited operational access
            
            For technical support, contact your system administrator.
            Version: 2.0 - Enhanced Edition
            """);
        alert.showAndWait();
    }

    private void handleLogout() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Logout");
        confirmAlert.setHeaderText("Are you sure you want to logout?");
        confirmAlert.setContentText("You will be returned to the login screen.");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Clear current user session
                    LoginView.clearCurrentUser();
                    
                    LoginView loginView = new LoginView(primaryStage);
                    Scene loginScene = new Scene(loginView.getView(), 500, 400);
                    
                    // Add fade transition
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(300), primaryStage.getScene().getRoot());
                    fadeOut.setFromValue(1);
                    fadeOut.setToValue(0);
                    fadeOut.setOnFinished(e -> {
                        primaryStage.setScene(loginScene);
                        primaryStage.setTitle("Login - Manufacturing Management System");
                        primaryStage.centerOnScreen();
                        
                        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), loginScene.getRoot());
                        fadeIn.setFromValue(0);
                        fadeIn.setToValue(1);
                        fadeIn.play();
                    });
                    fadeOut.play();
                    
                } catch (Exception e) {
                    showError("Error", "Failed to logout: " + e.getMessage());
                }
            }
        });
    }

    private void switchToScene(VBox content, String title) {
        previousScene = primaryStage.getScene();
        
        VBox container = new VBox(15);
        container.setPadding(new Insets(15));
        container.setStyle("""
            -fx-background-color: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
            """);
        
        // Enhanced back button
        Button backButton = new Button("← Back to Main Menu");
        backButton.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        backButton.setStyle("""
            -fx-background-color: #3498db;
            -fx-text-fill: white;
            -fx-background-radius: 8;
            -fx-border-radius: 8;
            -fx-padding: 10 20;
            -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0, 0, 1);
            """);
        
        backButton.setOnMouseEntered(e -> {
            backButton.setStyle("""
                -fx-background-color: #2980b9;
                -fx-text-fill: white;
                -fx-background-radius: 8;
                -fx-border-radius: 8;
                -fx-padding: 10 20;
                -fx-cursor: hand;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);
                """);
        });
        
        backButton.setOnMouseExited(e -> {
            backButton.setStyle("""
                -fx-background-color: #3498db;
                -fx-text-fill: white;
                -fx-background-radius: 8;
                -fx-border-radius: 8;
                -fx-padding: 10 20;
                -fx-cursor: hand;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0, 0, 1);
                """);
        });
        
        backButton.setOnAction(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), primaryStage.getScene().getRoot());
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(event -> {
                primaryStage.setScene(previousScene);
                primaryStage.setTitle("Manufacturing Management System - " + currentUser.getRoleDisplayName());
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), previousScene.getRoot());
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            });
            fadeOut.play();
        });
        
        container.getChildren().addAll(backButton, content);
        VBox.setVgrow(content, Priority.ALWAYS);
        
        Scene newScene = new Scene(container, 1400, 800);
        
        // Add fade transition
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), primaryStage.getScene().getRoot());
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            primaryStage.setScene(newScene);
            primaryStage.setTitle(title + " - Manufacturing Management System");
            primaryStage.setMaximized(true);
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), newScene.getRoot());
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        });
        fadeOut.play();
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
}