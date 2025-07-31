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
 * Comprehensive Inventory Management View
 * Handles raw materials, finished goods, and inventory tracking
 */
public class InventoryManagementView {
    private InventoryOperations inventoryOps;
    private final User currentUser;
    private TableView<InventoryItem> inventoryTable;
    private TabPane inventoryTabs;
    private Label totalItemsLabel;
    private Label lowStockItemsLabel;
    private Label totalValueLabel;
    private Label reorderItemsLabel;

    public InventoryManagementView(User user) {
        this.currentUser = user;
        try {
            this.inventoryOps = new InventoryOperations();
        } catch (SQLException e) {
            showError("Database Error", "Failed to connect to database: " + e.getMessage());
        }
    }

    public VBox getView() {
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("""
            -fx-background-color: linear-gradient(to bottom right, #f8f9fa, #e9ecef);
            """);

        // Header
        VBox headerSection = createHeaderSection();
        
        // Statistics cards
        HBox statsSection = createStatsSection();
        
        // Main content with tabs
        inventoryTabs = createInventoryTabs();
        VBox.setVgrow(inventoryTabs, Priority.ALWAYS);
        
        mainContainer.getChildren().addAll(
            headerSection,
            createSeparator(),
            statsSection,
            createSeparator(),
            inventoryTabs
        );
        
        refreshInventoryData();
        return mainContainer;
    }

    private VBox createHeaderSection() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Text titleText = new Text("📦 Inventory Management");
        titleText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleText.setFill(Color.web("#2c3e50"));
        
        Text subtitleText = new Text("Raw materials, components, and finished goods tracking");
        subtitleText.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        subtitleText.setFill(Color.web("#7f8c8d"));

        header.getChildren().addAll(titleText, subtitleText);
        return header;
    }

    private HBox createStatsSection() {
        HBox container = new HBox(20);
        container.setAlignment(Pos.CENTER);
        
        totalItemsLabel = new Label("0");
        lowStockItemsLabel = new Label("0");
        totalValueLabel = new Label("$0");
        reorderItemsLabel = new Label("0");
        
        VBox totalCard = createStatCard("Total Items", totalItemsLabel, "#3498db", "📊");
        VBox lowStockCard = createStatCard("Low Stock", lowStockItemsLabel, "#e74c3c", "⚠️");
        VBox valueCard = createStatCard("Total Value", totalValueLabel, "#27ae60", "💰");
        VBox reorderCard = createStatCard("Reorder Needed", reorderItemsLabel, "#f39c12", "🔄");
        
        container.getChildren().addAll(totalCard, lowStockCard, valueCard, reorderCard);
        return container;
    }

    private VBox createStatCard(String title, Label valueLabel, String color, String icon) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setPrefWidth(150);
        card.setPrefHeight(100);
        card.setStyle(String.format("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-border-radius: 10;
            -fx-border-color: %s;
            -fx-border-width: 2;
            """, color));
        
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.1));
        shadow.setRadius(6);
        shadow.setOffsetY(2);
        card.setEffect(shadow);
        
        Text iconText = new Text(icon);
        iconText.setFont(Font.font(20));
        
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        valueLabel.setTextFill(Color.web(color));
        
        Text titleText = new Text(title);
        titleText.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        titleText.setFill(Color.web("#7f8c8d"));
        
        card.getChildren().addAll(iconText, valueLabel, titleText);
        return card;
    }

    private TabPane createInventoryTabs() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Raw Materials tab
        Tab rawMaterialsTab = new Tab("📦 Raw Materials");
        rawMaterialsTab.setContent(createRawMaterialsView());
        
        // Components tab
        Tab componentsTab = new Tab("🔧 Components");
        componentsTab.setContent(createComponentsView());
        
        // Finished Goods tab
        Tab finishedGoodsTab = new Tab("✅ Finished Goods");
        finishedGoodsTab.setContent(createFinishedGoodsView());
        
        // Stock Movements tab
        Tab movementsTab = new Tab("📈 Stock Movements");
        movementsTab.setContent(createStockMovementsView());
        
        // Reports tab
        Tab reportsTab = new Tab("📊 Reports");
        reportsTab.setContent(createInventoryReportsView());
        
        tabPane.getTabs().addAll(rawMaterialsTab, componentsTab, finishedGoodsTab, movementsTab, reportsTab);
        return tabPane;
    }

    private VBox createRawMaterialsView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        
        // Filter controls
        HBox filterBox = createFilterControls("RAW_MATERIAL");
        
        // Inventory table
        inventoryTable = createInventoryTable();
        VBox.setVgrow(inventoryTable, Priority.ALWAYS);
        
        // Action buttons
        HBox actionButtons = createActionButtons();
        
        container.getChildren().addAll(filterBox, inventoryTable, actionButtons);
        return container;
    }

    private VBox createComponentsView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        
        Text infoText = new Text("Components and sub-assemblies inventory");
        infoText.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        infoText.setFill(Color.web("#7f8c8d"));
        
        // Components table (similar to inventory table but filtered for components)
        TableView<InventoryItem> componentsTable = createInventoryTable();
        VBox.setVgrow(componentsTable, Priority.ALWAYS);
        
        HBox actionButtons = createActionButtons();
        
        container.getChildren().addAll(infoText, componentsTable, actionButtons);
        return container;
    }

    private VBox createFinishedGoodsView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        
        Text infoText = new Text("Finished products ready for shipment");
        infoText.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        infoText.setFill(Color.web("#7f8c8d"));
        
        TableView<InventoryItem> finishedGoodsTable = createInventoryTable();
        VBox.setVgrow(finishedGoodsTable, Priority.ALWAYS);
        
        HBox actionButtons = createActionButtons();
        
        container.getChildren().addAll(infoText, finishedGoodsTable, actionButtons);
        return container;
    }

    private VBox createStockMovementsView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        
        Text titleText = new Text("Stock Movement History");
        titleText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleText.setFill(Color.web("#2c3e50"));
        
        TableView<StockMovement> movementsTable = createStockMovementsTable();
        VBox.setVgrow(movementsTable, Priority.ALWAYS);
        
        HBox movementActions = new HBox(10);
        movementActions.setAlignment(Pos.CENTER_LEFT);
        
        Button receiveBtn = createStyledButton("📥 Receive Stock", "#27ae60", 140);
        receiveBtn.setOnAction(e -> showReceiveStockDialog());
        
        Button issueBtn = createStyledButton("📤 Issue Stock", "#e74c3c", 140);
        issueBtn.setOnAction(e -> showIssueStockDialog());
        
        Button transferBtn = createStyledButton("🔄 Transfer", "#3498db", 140);
        transferBtn.setOnAction(e -> showTransferStockDialog());
        
        Button adjustBtn = createStyledButton("📝 Adjustment", "#f39c12", 140);
        adjustBtn.setOnAction(e -> showStockAdjustmentDialog());
        
        movementActions.getChildren().addAll(receiveBtn, issueBtn, transferBtn, adjustBtn);
        
        container.getChildren().addAll(titleText, movementsTable, movementActions);
        return container;
    }

    private VBox createInventoryReportsView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        
        Text titleText = new Text("Inventory Reports & Analytics");
        titleText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleText.setFill(Color.web("#2c3e50"));
        
        GridPane reportsGrid = new GridPane();
        reportsGrid.setHgap(15);
        reportsGrid.setVgap(15);
        reportsGrid.setAlignment(Pos.CENTER);
        
        // Report buttons
        Button stockLevelsBtn = createReportButton("📊 Stock Levels Report", "Current stock levels for all items");
        Button lowStockBtn = createReportButton("⚠️ Low Stock Alert", "Items below minimum stock level");
        Button valuationBtn = createReportButton("💰 Inventory Valuation", "Total inventory value by category");
        Button turnoverBtn = createReportButton("🔄 Stock Turnover", "Inventory turnover analysis");
        Button movementBtn = createReportButton("📈 Movement Report", "Stock movements over time");
        Button abcBtn = createReportButton("🎯 ABC Analysis", "ABC classification of inventory");
        
        reportsGrid.add(stockLevelsBtn, 0, 0);
        reportsGrid.add(lowStockBtn, 1, 0);
        reportsGrid.add(valuationBtn, 0, 1);
        reportsGrid.add(turnoverBtn, 1, 1);
        reportsGrid.add(movementBtn, 0, 2);
        reportsGrid.add(abcBtn, 1, 2);
        
        container.getChildren().addAll(titleText, reportsGrid);
        return container;
    }

    private HBox createFilterControls(String category) {
        HBox filterBox = new HBox(15);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        filterBox.setPadding(new Insets(0, 0, 10, 0));
        
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search items...");
        searchField.setPrefWidth(200);
        
        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All Status", "ACTIVE", "INACTIVE", "DISCONTINUED");
        statusFilter.setValue("All Status");
        statusFilter.setPrefWidth(120);
        
        ComboBox<String> stockFilter = new ComboBox<>();
        stockFilter.getItems().addAll("All Stock", "In Stock", "Low Stock", "Out of Stock");
        stockFilter.setValue("All Stock");
        stockFilter.setPrefWidth(120);
        
        Button filterBtn = createStyledButton("🔍 Filter", "#3498db", 80);
        Button refreshBtn = createStyledButton("🔄 Refresh", "#27ae60", 80);
        refreshBtn.setOnAction(e -> refreshInventoryData());
        
        filterBox.getChildren().addAll(
            new Label("Search:"), searchField,
            new Label("Status:"), statusFilter,
            new Label("Stock:"), stockFilter,
            filterBtn, refreshBtn
        );
        
        return filterBox;
    }

    private TableView<InventoryItem> createInventoryTable() {
        TableView<InventoryItem> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Item Code column
        TableColumn<InventoryItem, String> codeCol = new TableColumn<>("Item Code");
        codeCol.setCellValueFactory(data -> data.getValue().itemCodeProperty());
        codeCol.setPrefWidth(100);
        
        // Description column
        TableColumn<InventoryItem, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(data -> data.getValue().descriptionProperty());
        descCol.setPrefWidth(200);
        
        // Category column
        TableColumn<InventoryItem, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(data -> data.getValue().categoryProperty());
        categoryCol.setPrefWidth(120);
        
        // Quantity column
        TableColumn<InventoryItem, Number> qtyCol = new TableColumn<>("Qty on Hand");
        qtyCol.setCellValueFactory(data -> data.getValue().quantityOnHandProperty());
        qtyCol.setPrefWidth(100);
        qtyCol.setCellFactory(col -> new TableCell<InventoryItem, Number>() {
            @Override
            protected void updateItem(Number quantity, boolean empty) {
                super.updateItem(quantity, empty);
                if (empty || quantity == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%,d", quantity.intValue()));
                    
                    InventoryItem item = getTableView().getItems().get(getIndex());
                    if (item.isLowStock()) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else if (item.isOutOfStock()) {
                        setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold; -fx-background-color: #ffebee;");
                    } else {
                        setStyle("");
                    }
                    setAlignment(Pos.CENTER_RIGHT);
                }
            }
        });
        
        // Unit Price column
        TableColumn<InventoryItem, Number> priceCol = new TableColumn<>("Unit Price");
        priceCol.setCellValueFactory(data -> data.getValue().unitPriceProperty());
        priceCol.setPrefWidth(100);
        priceCol.setCellFactory(col -> new TableCell<InventoryItem, Number>() {
            @Override
            protected void updateItem(Number price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", price.doubleValue()));
                    setAlignment(Pos.CENTER_RIGHT);
                }
            }
        });
        
        // Total Value column
        TableColumn<InventoryItem, String> valueCol = new TableColumn<>("Total Value");
        valueCol.setCellValueFactory(data -> {
            InventoryItem item = data.getValue();
            double totalValue = item.getQuantityOnHand() * item.getUnitPrice();
            return new javafx.beans.property.SimpleStringProperty(String.format("$%,.2f", totalValue));
        });
        valueCol.setPrefWidth(100);
        
        // Location column
        TableColumn<InventoryItem, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(data -> data.getValue().locationProperty());
        locationCol.setPrefWidth(100);
        
        // Status column
        TableColumn<InventoryItem, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> data.getValue().statusProperty());
        statusCol.setCellFactory(col -> new TableCell<InventoryItem, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    String color = "ACTIVE".equals(status) ? "#27ae60" : 
                                  "INACTIVE".equals(status) ? "#f39c12" : "#e74c3c";
                    setStyle(String.format("""
                        -fx-text-fill: %s; 
                        -fx-font-weight: bold;
                        -fx-background-color: %s20;
                        -fx-background-radius: 4;
                        """, color, color));
                    setAlignment(Pos.CENTER);
                }
            }
        });
        statusCol.setPrefWidth(80);
        
        table.getColumns().addAll(codeCol, descCol, categoryCol, qtyCol, priceCol, valueCol, locationCol, statusCol);
        return table;
    }

    private TableView<StockMovement> createStockMovementsTable() {
        TableView<StockMovement> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Movement ID column
        TableColumn<StockMovement, String> idCol = new TableColumn<>("Movement ID");
        idCol.setCellValueFactory(data -> data.getValue().movementIdProperty());
        idCol.setPrefWidth(120);
        
        // Date column
        TableColumn<StockMovement, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> {
            LocalDateTime date = data.getValue().getMovementDate();
            return new javafx.beans.property.SimpleStringProperty(
                date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
            );
        });
        dateCol.setPrefWidth(140);
        
        // Item Code column
        TableColumn<StockMovement, String> itemCol = new TableColumn<>("Item Code");
        itemCol.setCellValueFactory(data -> data.getValue().itemCodeProperty());
        itemCol.setPrefWidth(100);
        
        // Movement Type column
        TableColumn<StockMovement, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> data.getValue().movementTypeProperty());
        typeCol.setCellFactory(col -> new TableCell<StockMovement, String>() {
            @Override
            protected void updateItem(String type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(type);
                    String color = type.contains("IN") ? "#27ae60" : "#e74c3c";
                    setStyle(String.format("-fx-text-fill: %s; -fx-font-weight: bold;", color));
                    setAlignment(Pos.CENTER);
                }
            }
        });
        typeCol.setPrefWidth(100);
        
        // Quantity column
        TableColumn<StockMovement, Number> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(data -> data.getValue().quantityProperty());
        qtyCol.setPrefWidth(80);
        
        // Reference column
        TableColumn<StockMovement, String> refCol = new TableColumn<>("Reference");
        refCol.setCellValueFactory(data -> data.getValue().referenceProperty());
        refCol.setPrefWidth(120);
        
        // User column
        TableColumn<StockMovement, String> userCol = new TableColumn<>("User");
        userCol.setCellValueFactory(data -> data.getValue().userProperty());
        userCol.setPrefWidth(100);
        
        table.getColumns().addAll(idCol, dateCol, itemCol, typeCol, qtyCol, refCol, userCol);
        return table;
    }

    private HBox createActionButtons() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button addBtn = createStyledButton("➕ Add Item", "#27ae60", 120);
        addBtn.setOnAction(e -> showAddItemDialog());
        
        Button editBtn = createStyledButton("✏️ Edit Item", "#3498db", 120);
        editBtn.setOnAction(e -> showEditItemDialog());
        
        Button stockBtn = createStyledButton("📊 Stock Check", "#f39c12", 120);
        stockBtn.setOnAction(e -> showStockCheckDialog());
        
        Button reorderBtn = createStyledButton("🔄 Reorder", "#9b59b6", 120);
        reorderBtn.setOnAction(e -> showReorderDialog());
        
        buttonBox.getChildren().addAll(addBtn, editBtn, stockBtn, reorderBtn);
        return buttonBox;
    }

    private Button createReportButton(String title, String description) {
        VBox buttonContent = new VBox(5);
        buttonContent.setAlignment(Pos.CENTER);
        buttonContent.setPrefWidth(200);
        buttonContent.setPrefHeight(80);
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        titleLabel.setTextFill(Color.WHITE);
        
        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10));
        descLabel.setTextFill(Color.web("#ecf0f1"));
        descLabel.setWrapText(true);
        
        buttonContent.getChildren().addAll(titleLabel, descLabel);
        
        Button button = new Button();
        button.setGraphic(buttonContent);
        button.setStyle("""
            -fx-background-color: #34495e;
            -fx-background-radius: 8;
            -fx-border-radius: 8;
            -fx-cursor: hand;
            """);
        
        button.setOnMouseEntered(e -> {
            button.setStyle("""
                -fx-background-color: #2c3e50;
                -fx-background-radius: 8;
                -fx-border-radius: 8;
                -fx-cursor: hand;
                """);
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle("""
                -fx-background-color: #34495e;
                -fx-background-radius: 8;
                -fx-border-radius: 8;
                -fx-cursor: hand;
                """);
        });
        
        button.setOnAction(e -> generateReport(title));
        
        return button;
    }

    private Button createStyledButton(String text, String color, double width) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        button.setPrefWidth(width);
        button.setPrefHeight(35);
        button.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-background-radius: 6;
            -fx-border-radius: 6;
            -fx-cursor: hand;
            """, color));
        
        button.setOnMouseEntered(e -> {
            button.setStyle(String.format("""
                -fx-background-color: derive(%s, -15%%);
                -fx-text-fill: white;
                -fx-background-radius: 6;
                -fx-border-radius: 6;
                -fx-cursor: hand;
                """, color));
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-text-fill: white;
                -fx-background-radius: 6;
                -fx-border-radius: 6;
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

    // Action methods
    private void showAddItemDialog() {
        showInfo("Add Item", "Add item dialog will be implemented.");
    }

    private void showEditItemDialog() {
        showInfo("Edit Item", "Edit item dialog will be implemented.");
    }

    private void showStockCheckDialog() {
        showInfo("Stock Check", "Stock check dialog will be implemented.");
    }

    private void showReorderDialog() {
        showInfo("Reorder", "Reorder dialog will be implemented.");
    }

    private void showReceiveStockDialog() {
        showInfo("Receive Stock", "Receive stock dialog will be implemented.");
    }

    private void showIssueStockDialog() {
        showInfo("Issue Stock", "Issue stock dialog will be implemented.");
    }

    private void showTransferStockDialog() {
        showInfo("Transfer Stock", "Transfer stock dialog will be implemented.");
    }

    private void showStockAdjustmentDialog() {
        showInfo("Stock Adjustment", "Stock adjustment dialog will be implemented.");
    }

    private void generateReport(String reportType) {
        showInfo("Generate Report", "Report generation for " + reportType + " will be implemented.");
    }

    private void refreshInventoryData() {
        try {
            // Refresh statistics
            InventoryStatistics stats = inventoryOps.getInventoryStatistics();
            totalItemsLabel.setText(String.valueOf(stats.getTotalItems()));
            lowStockItemsLabel.setText(String.valueOf(stats.getLowStockItems()));
            totalValueLabel.setText(String.format("$%,.2f", stats.getTotalValue()));
            reorderItemsLabel.setText(String.valueOf(stats.getReorderItems()));
            
            // Refresh inventory table
            List<InventoryItem> items = inventoryOps.getAllInventoryItems();
            inventoryTable.setItems(FXCollections.observableArrayList(items));
            
        } catch (Exception e) {
            showError("Error", "Failed to refresh inventory data: " + e.getMessage());
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
}