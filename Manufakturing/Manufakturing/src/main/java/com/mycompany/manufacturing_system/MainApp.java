package com.mycompany.manufacturing_system;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private Stage primaryStage;
    private User currentUser; // Akan menyimpan user yang sedang login

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Manufacturing Management System");

        // Tampilkan layar login terlebih dahulu
        showLoginView();
    }

    public void showLoginView() {
        LoginView loginView = new LoginView(primaryStage, this);
        Scene scene = new Scene(loginView.getView(), 500, 400);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public void showMainMenuView(User user) {
        this.currentUser = user; // Simpan user yang berhasil login
        ModernMainMenuView mainMenuView = new ModernMainMenuView(primaryStage, currentUser, this);
        Scene scene = new Scene(mainMenuView.getView(), 1200, 800); // Ukuran disesuaikan
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true); // Maksimalkan jendela untuk tampilan dashboard yang lebih baik
        primaryStage.show();
    }

    // Metode untuk menampilkan Production Dashboard
    public void showProductionDashboardScene() {
        try {
            ProductionDashboardView dashboardView = new ProductionDashboardView(currentUser, this);
            Scene scene = new Scene(dashboardView.getView(), 1400, 800); // Ukuran disesuaikan
            primaryStage.setScene(scene);
            primaryStage.setTitle("Production Dashboard - Manufacturing Management System");
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (Exception e) {
            showError("Error", "Failed to load Production Dashboard: " + e.getMessage());
        }
    }

    // Metode untuk menampilkan Production Management
    public void showProductionManagementScene() {
        try {
            ProductionManagementView productionView = new ProductionManagementView(currentUser, this);
            Scene scene = new Scene(productionView.getView(), 1400, 800);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Production Management - Manufacturing Management System");
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (Exception e) {
            showError("Error", "Failed to load Production Management: " + e.getMessage());
        }
    }

    // Metode untuk menampilkan Quality Control
    public void showQualityControlScene() {
        try {
            QualityControlView qualityView = new QualityControlView(currentUser, this);
            Scene scene = new Scene(qualityView.getView(), 1400, 800);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Quality Control - Manufacturing Management System");
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (Exception e) {
            showError("Error", "Failed to load Quality Control: " + e.getMessage());
        }
    }

    // Metode untuk menampilkan Inventory Management
    public void showInventoryManagementScene() {
        try {
            InventoryManagementView inventoryView = new InventoryManagementView(currentUser, this);
            Scene scene = new Scene(inventoryView.getView(), 1400, 800);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Inventory Management - Manufacturing Management System");
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (Exception e) {
            showError("Error", "Failed to load Inventory Management: " + e.getMessage());
        }
    }

    // Metode untuk menampilkan Reports & Analytics
    public void showReportsAnalyticsScene() {
        try {
            ReportsAnalyticsView reportsView = new ReportsAnalyticsView(currentUser, this);
            Scene scene = new Scene(reportsView.getView(), 1400, 800);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Reports & Analytics - Manufacturing Management System");
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (Exception e) {
            showError("Error", "Failed to load Reports & Analytics: " + e.getMessage());
        }
    }

    // Metode untuk menampilkan User Management
    public void showUserManagementScene() {
        try {
            // Pastikan UserManagementView menerima MainApp
            UserManagementView userMgmtView = new UserManagementView(currentUser, this);
            Scene scene = new Scene(userMgmtView.getView(), 1400, 800);
            primaryStage.setScene(scene);
            primaryStage.setTitle("User Management - Manufacturing Management System");
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (Exception e) {
            showError("Error", "Failed to load User Management: " + e.getMessage());
        }
    }

    // Metode untuk menampilkan My Tasks
    public void showMyTasksScene() {
        try {
            MyTasksView tasksView = new MyTasksView(currentUser, this);
            Scene scene = new Scene(tasksView.getView(), 1400, 800);
            primaryStage.setScene(scene);
            primaryStage.setTitle("My Tasks - Manufacturing Management System");
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (Exception e) {
            showError("Error", "Failed to load My Tasks: " + e.getMessage());
        }
    }

    // Metode untuk kembali ke Main Menu dari sub-tampilan
    public void goBackToMainMenu() {
        showMainMenuView(currentUser);
    }

    // Helper untuk menampilkan error
    private void showError(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
 