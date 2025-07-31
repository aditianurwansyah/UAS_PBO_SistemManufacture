package com.mycompany.manufacturing_system;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Modern Manufacturing Dashboard with real-time production monitoring
 */
public class ProductionDashboardView {
    private ManufacturingOperations manufacturingOps;
    private final User currentUser;
    private final MainApp mainApp; // Tambahkan referensi ke MainApp
    private VBox mainContainer;
    private HBox statsContainer;
    private TableView<ProductionOrder> ordersTable;
    private Label totalOrdersLabel;
    private Label pendingOrdersLabel;
    private Label inProgressLabel;
    private Label completedLabel;

    // Perbaiki konstruktor untuk menerima MainApp
    public ProductionDashboardView(User user, MainApp mainApp) {
        this.currentUser = user;
        this.mainApp = mainApp;
        try {
            this.manufacturingOps = new ManufacturingOperations();
        } catch (SQLException e) {
            showError("Database Error", "Failed to connect to database: " + e.getMessage());
        }
    }

    public VBox getView() {
        mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("""
            -fx-background-color: linear-gradient(to bottom right, #f8f9fa, #e9ecef);
            """);

        // Header with modern styling
        VBox headerSection = createHeaderSection();
        
        // Statistics cards
        statsContainer = createStatsContainer();
        
        // Quick actions section
        HBox quickActionsSection = createQuickActionsSection();
        
        // Recent orders table
        VBox ordersSection = createOrdersSection();

        mainContainer.getChildren().addAll(
            headerSection,
            createModernSeparator(),
            statsContainer,
            createModernSeparator(),
            quickActionsSection,
            createModernSeparator(),
            ordersSection
        );

        refreshDashboard();
        return mainContainer;
    }

    private VBox createHeaderSection() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        // Main title
        Text titleText = new Text("Manufacturing Dashboard");
        titleText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        titleText.setFill(Color.web("#2c3e50"));
        
        // Subtitle with current time
        Text subtitleText = new Text("Welcome back, " + currentUser.getDisplayName() + 
            " | " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy HH:mm")));
        subtitleText.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        subtitleText.setFill(Color.web("#7f8c8d"));

        header.getChildren().addAll(titleText, subtitleText);
        return header;
    }

    private HBox createStatsContainer() {
        HBox container = new HBox(20);
        container.setAlignment(Pos.CENTER);
        
        totalOrdersLabel = new Label("0");
        pendingOrdersLabel = new Label("0");
        inProgressLabel = new Label("0");
        completedLabel = new Label("0");
        
        VBox totalCard = createStatCard("Total Orders", totalOrdersLabel, "#3498db", "📊");
        VBox pendingCard = createStatCard("Pending", pendingOrdersLabel, "#f39c12", "⏳");
        VBox progressCard = createStatCard("In Progress", inProgressLabel, "#e74c3c", "🔄");
        VBox completedCard = createStatCard("Completed", completedLabel, "#27ae60", "✅");
        
        container.getChildren().addAll(totalCard, pendingCard, progressCard, completedCard);
        return container;
    }

    private VBox createStatCard(String title, Label valueLabel, String color, String icon) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefWidth(180);
        card.setPrefHeight(120);
        card.setStyle(String.format("""
            -fx-background-color: white;
            -fx-background-radius: 12;
            -fx-border-radius: 12;
            -fx-border-color: %s;
            -fx-border-width: 2;
            """, color));
        
        // Add drop shadow
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.1));
        shadow.setRadius(8);
        shadow.setOffsetY(2);
        card.setEffect(shadow);
        
        Text iconText = new Text(icon);
        iconText.setFont(Font.font(24));
        
        // Value
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        valueLabel.setTextFill(Color.web(color));
        
        // Title
        Text titleText = new Text(title);
        titleText.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        titleText.setFill(Color.web("#7f8c8d"));
        
        card.getChildren().addAll(iconText, valueLabel, titleText);
        
        // Add hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-background-radius: 12;
                -fx-border-radius: 12;
                -fx-border-color: %s;
                -fx-border-width: 2;
                -fx-cursor: hand;
                """, color + "15", color));
        });
        
        card.setOnMouseExited(e -> {
            card.setStyle(String.format("""
                -fx-background-color: white;
                -fx-background-radius: 12;
                -fx-border-radius: 12;
                -fx-border-color: %s;
                -fx-border-width: 2;
                """, color));
        });
        
        return card;
    }

    private HBox createQuickActionsSection() {
        HBox container = new HBox(15);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(10));
        
        Text sectionTitle = new Text("Quick Actions");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        sectionTitle.setFill(Color.web("#2c3e50"));
        
        Button newOrderBtn = createModernButton("New Order", "#3498db", "➕");
        newOrderBtn.setOnAction(e -> handleNewOrder());
        
        Button startProductionBtn = createModernButton("Start Production", "#27ae60", "▶️");
        startProductionBtn.setOnAction(e -> handleStartProduction());
        
        Button qualityCheckBtn = createModernButton("Quality Check", "#f39c12", "🔍");
        qualityCheckBtn.setOnAction(e -> handleQualityCheck());
        
        Button reportsBtn = createModernButton("View Reports", "#9b59b6", "📈");
        reportsBtn.setOnAction(e -> handleViewReports());
        
        VBox titleContainer = new VBox(5);
        titleContainer.setAlignment(Pos.CENTER_LEFT);
        titleContainer.getChildren().add(sectionTitle);
        
        HBox buttonsContainer = new HBox(15);
        buttonsContainer.setAlignment(Pos.CENTER);
        buttonsContainer.getChildren().addAll(newOrderBtn, startProductionBtn, qualityCheckBtn, reportsBtn);
        
        VBox fullContainer = new VBox(15);
        fullContainer.getChildren().addAll(titleContainer, buttonsContainer);
        
        HBox wrapper = new HBox();
        wrapper.setAlignment(Pos.CENTER);
        wrapper.getChildren().add(fullContainer);
        
        return wrapper;
    }

    private Button createModernButton(String text, String color, String icon) {
        Button button = new Button(icon + " " + text);
        button.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        button.setPrefWidth(150);
        button.setPrefHeight(45);
        button.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-background-radius: 8;
            -fx-border-radius: 8;
            -fx-cursor: hand;
            """, color));
        
        // Hover effect
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

    private VBox createOrdersSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(10));
        
        Text sectionTitle = new Text("Recent Production Orders");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        sectionTitle.setFill(Color.web("#2c3e50"));
        
        ordersTable = createModernOrdersTable();
        VBox.setVgrow(ordersTable, Priority.ALWAYS);
        
        section.getChildren().addAll(sectionTitle, ordersTable);
        return section;
    }

    private TableView<ProductionOrder> createModernOrdersTable() {
        TableView<ProductionOrder> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 8;
            -fx-border-radius: 8;
            """);
        
        // Order ID column
        TableColumn<ProductionOrder, String> orderIdCol = new TableColumn<>("Order ID");
        orderIdCol.setCellValueFactory(data -> data.getValue().orderIdProperty());
        orderIdCol.setPrefWidth(100);
        
        // Customer column
        TableColumn<ProductionOrder, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(data -> data.getValue().customerNameProperty());
        customerCol.setPrefWidth(120);
        
        // Product column
        TableColumn<ProductionOrder, String> productCol = new TableColumn<>("Product");
        productCol.setCellValueFactory(data -> data.getValue().productNameProperty());
        productCol.setPrefWidth(150);
        
        // Quantity column
        TableColumn<ProductionOrder, Number> quantityCol = new TableColumn<>("Qty");
        quantityCol.setCellValueFactory(data -> data.getValue().quantityProperty());
        quantityCol.setPrefWidth(60);
        
        // Status column with custom cell factory
        TableColumn<ProductionOrder, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> data.getValue().statusProperty());
        statusCol.setCellFactory(col -> new TableCell<ProductionOrder, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    ProductionOrder order = getTableView().getItems().get(getIndex());
                    String color = order.getStatusColor();
                    setStyle(String.format("-fx-text-fill: %s; -fx-font-weight: bold;", color));
                }
            }
        });
        statusCol.setPrefWidth(100);
        
        // Priority column
        TableColumn<ProductionOrder, String> priorityCol = new TableColumn<>("Priority");
        priorityCol.setCellValueFactory(data -> data.getValue().priorityProperty());
        priorityCol.setCellFactory(col -> new TableCell<ProductionOrder, String>() {
            @Override
            protected void updateItem(String priority, boolean empty) {
                super.updateItem(priority, empty);
                if (empty || priority == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(priority);
                    ProductionOrder order = getTableView().getItems().get(getIndex());
                    String color = order.getPriorityColor();
                    setStyle(String.format("-fx-text-fill: %s; -fx-font-weight: bold;", color));
                }
            }
        });
        priorityCol.setPrefWidth(80);
        
        // Progress column
        TableColumn<ProductionOrder, Number> progressCol = new TableColumn<>("Progress");
        progressCol.setCellValueFactory(data -> data.getValue().completionPercentageProperty());
        progressCol.setCellFactory(col -> new TableCell<ProductionOrder, Number>() {
            @Override
            protected void updateItem(Number progress, boolean empty) {
                super.updateItem(progress, empty);
                if (empty || progress == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    ProgressBar progressBar = new ProgressBar(progress.doubleValue() / 100.0);
                    progressBar.setPrefWidth(80);
                    progressBar.setPrefHeight(15);
                    setGraphic(progressBar);
                    setText(String.format("%.1f%%", progress.doubleValue()));
                }
            }
        });
        progressCol.setPrefWidth(100);
        
        table.getColumns().addAll(orderIdCol, customerCol, productCol, quantityCol, 
                                 statusCol, priorityCol, progressCol);
        table.setMaxHeight(300);
        
        return table;
    }

    private Separator createModernSeparator() {
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #bdc3c7;");
        return separator;
    }

    private void refreshDashboard() {
        try {
            // Update statistics
            ManufacturingOperations.ProductionStatistics stats = manufacturingOps.getProductionStatistics();
            totalOrdersLabel.setText(String.valueOf(stats.getTotalOrders()));
            pendingOrdersLabel.setText(String.valueOf(stats.getPendingOrders()));
            inProgressLabel.setText(String.valueOf(stats.getInProgressOrders()));
            completedLabel.setText(String.valueOf(stats.getCompletedOrders()));
            
            // Update orders table
            List<ProductionOrder> orders;
            // Jika pengguna adalah USER, hanya tampilkan pesanan terkait dengannya (asumsi 'own_orders' permission)
            // Jika pengguna adalah ADMIN, tampilkan semua pesanan ('all_orders' permission)
            if (currentUser.getRole() != null && "USER".equals(currentUser.getRole().getRoleName())) {
                orders = manufacturingOps.getProductionOrdersByUserId(currentUser.getUserId()); 
            } else { // Termasuk ADMIN atau jika role tidak terdefinisi
                orders = manufacturingOps.getAllProductionOrders();
            }
            ordersTable.setItems(FXCollections.observableArrayList(orders));
            
        } catch (Exception e) {
            showError("Error", "Failed to refresh dashboard: " + e.getMessage());
        }
    }

    // --- Implementasi metode penanganan tombol yang diperbaiki ---

    private void handleNewOrder() {
        if (currentUser.getRole() != null && "ADMIN".equals(currentUser.getRole().getRoleName())) {
            mainApp.showProductionManagementScene(); // Arahkan ke Production Management untuk membuat pesanan baru
        } else {
            showError("Access Denied", "Anda tidak memiliki izin untuk membuat pesanan baru.");
        }
    }

    private void handleStartProduction() {
        // Asumsi departemen PRODUCTION atau ADMIN bisa memulai produksi
        if (currentUser.getRole() != null && ("ADMIN".equals(currentUser.getRole().getRoleName()) || User.Department.PRODUCTION.name().equals(currentUser.getDepartment()))) {
            mainApp.showProductionManagementScene(); // Arahkan ke Production Management untuk memulai produksi
        } else {
            showError("Access Denied", "Anda tidak memiliki izin untuk memulai produksi.");
        }
    }

    private void handleQualityCheck() {
        // Asumsi departemen QUALITY_CONTROL atau ADMIN bisa melakukan quality check
        if (currentUser.getRole() != null && ("ADMIN".equals(currentUser.getRole().getRoleName()) || User.Department.QUALITY_CONTROL.name().equals(currentUser.getDepartment()))) {
            mainApp.showQualityControlScene(); // Arahkan ke Quality Control
        } else {
            showError("Access Denied", "Anda tidak memiliki izin untuk melakukan pemeriksaan kualitas.");
        }
    }

    private void handleViewReports() {
        mainApp.showReportsAnalyticsScene(); // Arahkan ke Reports & Analytics
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
  