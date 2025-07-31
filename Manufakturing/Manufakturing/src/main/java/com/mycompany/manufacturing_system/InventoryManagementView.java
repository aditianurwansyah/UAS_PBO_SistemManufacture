package com.mycompany.manufacturing_system;

import javafx.beans.property.SimpleStringProperty;
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
import java.util.stream.Collectors;

public class InventoryManagementView {
    private final InventoryOperations inventoryOps;
    private final User currentUser;
    private final MainApp mainApp;
    private TableView<InventoryOperations.InventoryItem> rawMaterialsTable;
    private TableView<InventoryOperations.InventoryItem> componentsTable;
    private TableView<InventoryOperations.InventoryItem> finishedGoodsTable;
    private TableView<InventoryOperations.StockMovement> movementsTable;
    private TabPane inventoryTabs;
    private Label totalItemsLabel;
    private Label lowStockItemsLabel;
    private Label totalValueLabel;
    private Label reorderItemsLabel;

    public InventoryManagementView(User user, MainApp mainApp) {
        this.currentUser = user;
        this.mainApp = mainApp;
        try {
            this.inventoryOps = new InventoryOperations();
        } catch (SQLException e) {
            showError("Database Error", "Failed to connect to database: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public VBox getView() {
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: linear-gradient(to bottom right, #f8f9fa, #e9ecef);");

        Button backButton = new Button("← Back to Main Menu");
        backButton.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        backButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;");
        backButton.setOnAction(e -> mainApp.goBackToMainMenu());

        HBox backButtonWrapper = new HBox(backButton);
        backButtonWrapper.setAlignment(Pos.TOP_LEFT);
        backButtonWrapper.setPadding(new Insets(0, 0, 10, 0));

        VBox headerSection = createHeaderSection();
        HBox statsSection = createStatsSection();
        inventoryTabs = createInventoryTabs();
        VBox.setVgrow(inventoryTabs, Priority.ALWAYS);

        mainContainer.getChildren().addAll(backButtonWrapper, headerSection, createSeparator(), statsSection, createSeparator(), inventoryTabs);
        refreshInventoryData();
        return mainContainer;
    }

    private VBox createHeaderSection() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Text titleText = new Text("📦 Inventory Management");
        titleText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleText.setFill(Color.web("#2c3e50"));
        Text subtitleText = new Text("Manage products and stock movements");
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
        card.setStyle(String.format("-fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: %s; -fx-border-width: 2;", color));
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
        Tab rawMaterialsTab = new Tab("📦 Automotive");
        rawMaterialsTab.setContent(createCategoryView("AUTOMOTIVE"));
        Tab componentsTab = new Tab("🔧 Electronics");
        componentsTab.setContent(createCategoryView("ELECTRONICS"));
        Tab finishedGoodsTab = new Tab("✅ Furniture");
        finishedGoodsTab.setContent(createCategoryView("FURNITURE"));
        Tab movementsTab = new Tab("📈 Stock Movements");
        movementsTab.setContent(createStockMovementsView());
        Tab reportsTab = new Tab("📊 Reports");
        reportsTab.setContent(createInventoryReportsView());
        tabPane.getTabs().addAll(rawMaterialsTab, componentsTab, finishedGoodsTab, movementsTab, reportsTab);
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> refreshInventoryData());
        return tabPane;
    }

    private VBox createCategoryView(String category) {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        Text infoText = new Text(category + " inventory");
        infoText.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        infoText.setFill(Color.web("#7f8c8d"));
        HBox filterBox = createFilterControls(category);
        TableView<InventoryOperations.InventoryItem> table = createInventoryTable();
        if (category.equals("AUTOMOTIVE")) rawMaterialsTable = table;
        else if (category.equals("ELECTRONICS")) componentsTable = table;
        else if (category.equals("FURNITURE")) finishedGoodsTable = table;
        VBox.setVgrow(table, Priority.ALWAYS);
        HBox actionButtons = createActionButtons();
        container.getChildren().addAll(infoText, filterBox, table, actionButtons);
        return container;
    }

    private VBox createStockMovementsView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        Text titleText = new Text("Stock Movement History");
        titleText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleText.setFill(Color.web("#2c3e50"));
        movementsTable = createStockMovementsTable();
        VBox.setVgrow(movementsTable, Priority.ALWAYS);
        HBox movementActions = new HBox(10);
        movementActions.setAlignment(Pos.CENTER_LEFT);
        Button receiveBtn = createStyledButton("📥 Receive Stock", "#27ae60", 140, e -> showReceiveStockDialog());
        Button issueBtn = createStyledButton("📤 Issue Stock", "#e74c3c", 140, e -> showIssueStockDialog());
        Button transferBtn = createStyledButton("🔄 Transfer", "#3498db", 140, e -> showTransferStockDialog());
        Button adjustBtn = createStyledButton("📝 Adjustment", "#f39c12", 140, e -> showStockAdjustmentDialog());
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
        Button stockLevelsBtn = createReportButton("📊 Stock Levels Report", "Current stock levels for all items", e -> generateReport("Stock Levels Report"));
        Button lowStockBtn = createReportButton("⚠️ Low Stock Alert", "Items below minimum stock level", e -> generateReport("Low Stock Alert"));
        Button valuationBtn = createReportButton("💰 Inventory Valuation", "Total inventory value by category", e -> generateReport("Inventory Valuation"));
        Button turnoverBtn = createReportButton("🔄 Stock Turnover", "Inventory turnover analysis", e -> generateReport("Stock Turnover"));
        Button movementBtn = createReportButton("📈 Movement Report", "Stock movements over time", e -> generateReport("Movement Report"));
        Button abcBtn = createReportButton("🎯 ABC Analysis", "ABC classification of inventory", e -> generateReport("ABC Analysis"));
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
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters(category, searchField.getText(), null, null));
        ComboBox<String> statusFilter = new ComboBox<>(FXCollections.observableArrayList("All Status", "ACTIVE", "INACTIVE", "DISCONTINUED", "DEVELOPMENT"));
        statusFilter.setValue("All Status");
        statusFilter.setPrefWidth(120);
        statusFilter.setOnAction(e -> applyFilters(category, searchField.getText(), statusFilter.getValue(), null));
        ComboBox<String> stockFilter = new ComboBox<>(FXCollections.observableArrayList("All Stock", "In Stock", "Low Stock", "Out of Stock"));
        stockFilter.setValue("All Stock");
        stockFilter.setPrefWidth(120);
        stockFilter.setOnAction(e -> applyFilters(category, searchField.getText(), statusFilter.getValue(), stockFilter.getValue()));
        Button filterBtn = createStyledButton("🔍 Filter", "#3498db", 80, e -> applyFilters(category, searchField.getText(), statusFilter.getValue(), stockFilter.getValue()));
        Button refreshBtn = createStyledButton("🔄 Refresh", "#27ae60", 80, e -> refreshInventoryData());
        filterBox.getChildren().addAll(new Label("Search:"), searchField, new Label("Status:"), statusFilter, new Label("Stock:"), stockFilter, filterBtn, refreshBtn);
        return filterBox;
    }

    private TableView<InventoryOperations.InventoryItem> createInventoryTable() {
        TableView<InventoryOperations.InventoryItem> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<InventoryOperations.InventoryItem, String> codeCol = new TableColumn<>("Item Code");
        codeCol.setCellValueFactory(data -> data.getValue().itemCodeProperty());
        codeCol.setPrefWidth(100);
        TableColumn<InventoryOperations.InventoryItem, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(data -> data.getValue().descriptionProperty());
        descCol.setPrefWidth(200);
        TableColumn<InventoryOperations.InventoryItem, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(data -> data.getValue().categoryProperty());
        categoryCol.setPrefWidth(120);
        TableColumn<InventoryOperations.InventoryItem, Number> qtyCol = new TableColumn<>("Qty on Hand");
        qtyCol.setCellValueFactory(data -> data.getValue().quantityOnHandProperty());
        qtyCol.setPrefWidth(100);
        qtyCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number quantity, boolean empty) {
                super.updateItem(quantity, empty);
                if (empty || quantity == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%,d", quantity.intValue()));
                    InventoryOperations.InventoryItem item = getTableView().getItems().get(getIndex());
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
        TableColumn<InventoryOperations.InventoryItem, Number> priceCol = new TableColumn<>("Unit Price");
        priceCol.setCellValueFactory(data -> data.getValue().unitPriceProperty());
        priceCol.setPrefWidth(100);
        priceCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : String.format("$%.2f", price.doubleValue()));
                setAlignment(Pos.CENTER_RIGHT);
            }
        });
        TableColumn<InventoryOperations.InventoryItem, String> valueCol = new TableColumn<>("Total Value");
        valueCol.setCellValueFactory(data -> new SimpleStringProperty(String.format("$%,.2f", data.getValue().getQuantityOnHand() * data.getValue().getUnitPrice())));
        valueCol.setPrefWidth(100);
        TableColumn<InventoryOperations.InventoryItem, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(data -> data.getValue().locationProperty());
        locationCol.setPrefWidth(100);
        TableColumn<InventoryOperations.InventoryItem, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> data.getValue().statusProperty());
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    String color = "ACTIVE".equals(status) ? "#27ae60" : "INACTIVE".equals(status) ? "#f39c12" : "#e74c3c";
                    setStyle(String.format("-fx-text-fill: %s; -fx-font-weight: bold; -fx-background-color: %s20; -fx-background-radius: 4;", color, color));
                    setAlignment(Pos.CENTER);
                }
            }
        });
        statusCol.setPrefWidth(80);
        table.getColumns().addAll(codeCol, descCol, categoryCol, qtyCol, priceCol, valueCol, locationCol, statusCol);
        return table;
    }

    private TableView<InventoryOperations.StockMovement> createStockMovementsTable() {
        TableView<InventoryOperations.StockMovement> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<InventoryOperations.StockMovement, String> idCol = new TableColumn<>("Movement ID");
        idCol.setCellValueFactory(data -> data.getValue().movementIdProperty());
        idCol.setPrefWidth(120);
        TableColumn<InventoryOperations.StockMovement, String> dateCol = new TableColumn<>("Date");
        
        dateCol.setPrefWidth(140);
        TableColumn<InventoryOperations.StockMovement, String> itemCol = new TableColumn<>("Item Code");
        itemCol.setCellValueFactory(data -> data.getValue().itemCodeProperty());
        itemCol.setPrefWidth(100);
        TableColumn<InventoryOperations.StockMovement, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> data.getValue().movementTypeProperty());
        typeCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(type);
                    String color = type.contains("RECEIVE") || type.contains("IN") ? "#27ae60" : "#e74c3c";
                    setStyle(String.format("-fx-text-fill: %s; -fx-font-weight: bold;", color));
                    setAlignment(Pos.CENTER);
                }
            }
        });
        typeCol.setPrefWidth(100);
        TableColumn<InventoryOperations.StockMovement, Number> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(data -> data.getValue().quantityProperty());
        qtyCol.setPrefWidth(80);
        TableColumn<InventoryOperations.StockMovement, String> refCol = new TableColumn<>("Reference");
        refCol.setCellValueFactory(data -> data.getValue().referenceProperty());
        refCol.setPrefWidth(120);
        TableColumn<InventoryOperations.StockMovement, String> userCol = new TableColumn<>("User");
        userCol.setCellValueFactory(data -> data.getValue().userProperty());
        userCol.setPrefWidth(100);
        table.getColumns().addAll(idCol, dateCol, itemCol, typeCol, qtyCol, refCol, userCol);
        return table;
    }

    private HBox createActionButtons() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        Button addBtn = createStyledButton("➕ Add Item", "#27ae60", 120, e -> showAddItemDialog());
        Button editBtn = createStyledButton("✏️ Edit Item", "#3498db", 120, e -> showEditItemDialog());
        Button stockBtn = createStyledButton("📊 Stock Check", "#f39c12", 120, e -> showStockCheckDialog());
        Button reorderBtn = createStyledButton("🔄 Reorder", "#9b59b6", 120, e -> showReorderDialog());
        addBtn.setDisable(!hasPermission("user_management")); // Only ADMIN can add items
        editBtn.setDisable(!hasPermission("user_management")); // Only ADMIN can edit items
        buttonBox.getChildren().addAll(addBtn, editBtn, stockBtn, reorderBtn);
        return buttonBox;
    }

    private Button createReportButton(String title, String description, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
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
        button.setStyle("-fx-background-color: #34495e; -fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #2c3e50; -fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #34495e; -fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand;"));
        button.setOnAction(handler);
        return button;
    }

    private Button createStyledButton(String text, String color, double width, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        button.setPrefWidth(width);
        button.setPrefHeight(35);
        String baseStyle = String.format("-fx-text-fill: white; -fx-background-radius: 6; -fx-border-radius: 6; -fx-cursor: hand; -fx-background-color: %s;", color);
        String hoverStyle = String.format("-fx-text-fill: white; -fx-background-radius: 6; -fx-border-radius: 6; -fx-cursor: hand; -fx-background-color: derive(%s, -15%%);", color);
        button.setStyle(baseStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(baseStyle));
        button.setOnAction(handler);
        return button;
    }

    private Separator createSeparator() {
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #ecf0f1;");
        return separator;
    }

    private void showAddItemDialog() {
        if (!hasPermission("user_management")) {
            showError("Permission Denied", "Only administrators can add items.");
            return;
        }
        Dialog<InventoryOperations.InventoryItem> dialog = new Dialog<>();
        dialog.setTitle("Add New Inventory Item");
        dialog.setHeaderText("Enter details for the new inventory item");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        TextField itemCodeField = new TextField();
        itemCodeField.setPromptText("e.g., AUTO-001");
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("e.g., Sedan Car Frame");
        ComboBox<String> categoryCombo = new ComboBox<>(FXCollections.observableArrayList("AUTOMOTIVE", "ELECTRONICS", "FURNITURE"));
        categoryCombo.setValue("AUTOMOTIVE");
        Spinner<Integer> quantitySpinner = new Spinner<>(0, 10000, 0);
        quantitySpinner.setEditable(true);
        TextField unitPriceField = new TextField();
        unitPriceField.setPromptText("e.g., 25000.00");
        TextField locationField = new TextField();
        locationField.setPromptText("e.g., Warehouse A-1");
        ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList("ACTIVE", "INACTIVE", "DISCONTINUED", "DEVELOPMENT"));
        statusCombo.setValue("ACTIVE");
        Spinner<Integer> reorderPointSpinner = new Spinner<>(0, 1000, 10);
        reorderPointSpinner.setEditable(true);
        Spinner<Integer> minStockLevelSpinner = new Spinner<>(0, 1000, 0);
        minStockLevelSpinner.setEditable(true);
        grid.addRow(0, new Label("Item Code:"), itemCodeField);
        grid.addRow(1, new Label("Description:"), descriptionField);
        grid.addRow(2, new Label("Category:"), categoryCombo);
        grid.addRow(3, new Label("Quantity:"), quantitySpinner);
        grid.addRow(4, new Label("Unit Price:"), unitPriceField);
        grid.addRow(5, new Label("Location:"), locationField);
        grid.addRow(6, new Label("Status:"), statusCombo);
        grid.addRow(7, new Label("Reorder Point:"), reorderPointSpinner);
        grid.addRow(8, new Label("Min Stock Level:"), minStockLevelSpinner);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    String itemCode = itemCodeField.getText().trim();
                    String description = descriptionField.getText().trim();
                    String category = categoryCombo.getValue();
                    int quantity = quantitySpinner.getValue();
                    double unitPrice = Double.parseDouble(unitPriceField.getText().trim());
                    String location = locationField.getText().trim();
                    String status = statusCombo.getValue();
                    int reorderPoint = reorderPointSpinner.getValue();
                    int minStockLevel = minStockLevelSpinner.getValue();
                    if (itemCode.isEmpty() || description.isEmpty()) {
                        showError("Validation Error", "Item Code and Description are required.");
                        return null;
                    }
                    return new InventoryOperations.InventoryItem(itemCode, description, category, quantity, unitPrice, location, status, reorderPoint, minStockLevel);
                } catch (NumberFormatException e) {
                    showError("Input Error", "Please enter valid numbers for Quantity, Unit Price, Reorder Point, and Min Stock Level.");
                    return null;
                }
            }
            return null;
        });
        dialog.showAndWait().ifPresent(item -> {
            try {
                if (inventoryOps.addItem(item)) {
                    showInfo("Success", "Inventory item added successfully.");
                    refreshInventoryData();
                } else {
                    showError("Error", "Failed to add inventory item. Item code might already exist.");
                }
            } catch (SQLException e) {
                showError("Database Error", "Failed to add item: " + e.getMessage());
            }
        });
    }

    private void showEditItemDialog() {
        if (!hasPermission("user_management")) {
            showError("Permission Denied", "Only administrators can edit items.");
            return;
        }
        TableView<InventoryOperations.InventoryItem> currentTable = getCurrentTable();
        InventoryOperations.InventoryItem selectedItem = currentTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showError("No Selection", "Please select an item to edit.");
            return;
        }
        Dialog<InventoryOperations.InventoryItem> dialog = new Dialog<>();
        dialog.setTitle("Edit Inventory Item");
        dialog.setHeaderText("Edit details for: " + selectedItem.getItemCode());
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        TextField itemCodeField = new TextField(selectedItem.getItemCode());
        itemCodeField.setEditable(false);
        itemCodeField.setStyle("-fx-control-inner-background: #e0e0e0;");
        TextField descriptionField = new TextField(selectedItem.getDescription());
        descriptionField.setPromptText("Description");
        ComboBox<String> categoryCombo = new ComboBox<>(FXCollections.observableArrayList("AUTOMOTIVE", "ELECTRONICS", "FURNITURE"));
        categoryCombo.setValue(selectedItem.getCategory());
        TextField unitPriceField = new TextField(String.valueOf(selectedItem.getUnitPrice()));
        unitPriceField.setPromptText("Unit Price");
        TextField locationField = new TextField(selectedItem.getLocation());
        locationField.setPromptText("Location");
        ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList("ACTIVE", "INACTIVE", "DISCONTINUED", "DEVELOPMENT"));
        statusCombo.setValue(selectedItem.getStatus());
        Spinner<Integer> reorderPointSpinner = new Spinner<>(0, 1000, selectedItem.getReorderPoint());
        reorderPointSpinner.setEditable(true);
        Spinner<Integer> minStockLevelSpinner = new Spinner<>(0, 1000, selectedItem.getMinStockLevel());
        minStockLevelSpinner.setEditable(true);
        grid.addRow(0, new Label("Item Code:"), itemCodeField);
        grid.addRow(1, new Label("Description:"), descriptionField);
        grid.addRow(2, new Label("Category:"), categoryCombo);
        grid.addRow(3, new Label("Unit Price:"), unitPriceField);
        grid.addRow(4, new Label("Location:"), locationField);
        grid.addRow(5, new Label("Status:"), statusCombo);
        grid.addRow(6, new Label("Reorder Point:"), reorderPointSpinner);
        grid.addRow(7, new Label("Min Stock Level:"), minStockLevelSpinner);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    String description = descriptionField.getText().trim();
                    String category = categoryCombo.getValue();
                    double unitPrice = Double.parseDouble(unitPriceField.getText().trim());
                    String location = locationField.getText().trim();
                    String status = statusCombo.getValue();
                    int reorderPoint = reorderPointSpinner.getValue();
                    int minStockLevel = minStockLevelSpinner.getValue();
                    if (description.isEmpty()) {
                        showError("Validation Error", "Description is required.");
                        return null;
                    }
                    return new InventoryOperations.InventoryItem(selectedItem.getItemCode(), description, category, 0, unitPrice, location, status, reorderPoint, minStockLevel);
                } catch (NumberFormatException e) {
                    showError("Input Error", "Please enter valid numbers for Unit Price, Reorder Point, and Min Stock Level.");
                    return null;
                }
            }
            return null;
        });
        dialog.showAndWait().ifPresent(updatedItem -> {
            try {
                if (inventoryOps.updateItem(
                        selectedItem.getItemCode(),
                        updatedItem.getDescription(),
                        updatedItem.getCategory(),
                        updatedItem.getUnitPrice(),
                        updatedItem.getLocation(),
                        updatedItem.getStatus(),
                        updatedItem.getReorderPoint(),
                        updatedItem.getMinStockLevel())) {
                    showInfo("Success", "Inventory item updated successfully.");
                    refreshInventoryData();
                } else {
                    showError("Error", "Failed to update inventory item.");
                }
            } catch (SQLException e) {
                showError("Database Error", "Failed to update item: " + e.getMessage());
            }
        });
    }

    private void showStockCheckDialog() {
        showInfo("Stock Check", "Stock check functionality will be implemented.");
    }

    private void showReorderDialog() {
        showInfo("Reorder", "Reorder functionality will be implemented.");
    }

    private void showReceiveStockDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Receive Stock");
        dialog.setHeaderText("Record incoming inventory");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        ComboBox<String> itemCodeCombo = new ComboBox<>();
        itemCodeCombo.setPromptText("Select Item Code");
        try {
            inventoryOps.getAllInventoryItems().forEach(item -> itemCodeCombo.getItems().add(item.getItemCode()));
        } catch (SQLException e) {
            showError("Database Error", "Failed to load items: " + e.getMessage());
            return;
        }
        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity");
        TextField referenceField = new TextField();
        referenceField.setPromptText("PO Number / Reference");
        grid.addRow(0, new Label("Item Code:"), itemCodeCombo);
        grid.addRow(1, new Label("Quantity:"), quantityField);
        grid.addRow(2, new Label("Reference:"), referenceField);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    String itemCode = itemCodeCombo.getValue();
                    int quantity = Integer.parseInt(quantityField.getText().trim());
                    String reference = referenceField.getText().trim();
                    if (itemCode == null || itemCode.isEmpty() || quantity <= 0) {
                        showError("Validation Error", "Please select an item and enter a valid quantity.");
                        return null;
                    }
                    if (inventoryOps.recordStockMovement(itemCode, "RECEIVE", quantity, reference, currentUser.getUsername())) {
                        showInfo("Success", "Stock received successfully.");
                        refreshInventoryData();
                    } else {
                        showError("Error", "Failed to receive stock. Item not found or invalid quantity.");
                    }
                } catch (NumberFormatException e) {
                    showError("Input Error", "Please enter a valid number for Quantity.");
                } catch (SQLException e) {
                    showError("Database Error", "Failed to record movement: " + e.getMessage());
                }
            }
            return null;
        });
        dialog.showAndWait();
    }

    private void showIssueStockDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Issue Stock");
        dialog.setHeaderText("Record outgoing inventory");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        ComboBox<String> itemCodeCombo = new ComboBox<>();
        itemCodeCombo.setPromptText("Select Item Code");
        try {
            inventoryOps.getAllInventoryItems().forEach(item -> itemCodeCombo.getItems().add(item.getItemCode()));
        } catch (SQLException e) {
            showError("Database Error", "Failed to load items: " + e.getMessage());
            return;
        }
        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity");
        TextField referenceField = new TextField();
        referenceField.setPromptText("Production Order / Reference");
        grid.addRow(0, new Label("Item Code:"), itemCodeCombo);
        grid.addRow(1, new Label("Quantity:"), quantityField);
        grid.addRow(2, new Label("Reference:"), referenceField);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    String itemCode = itemCodeCombo.getValue();
                    int quantity = Integer.parseInt(quantityField.getText().trim());
                    String reference = referenceField.getText().trim();
                    if (itemCode == null || itemCode.isEmpty() || quantity <= 0) {
                        showError("Validation Error", "Please select an item and enter a valid quantity.");
                        return null;
                    }
                    if (inventoryOps.recordStockMovement(itemCode, "ISSUE", quantity, reference, currentUser.getUsername())) {
                        showInfo("Success", "Stock issued successfully.");
                        refreshInventoryData();
                    } else {
                        showError("Error", "Failed to issue stock. Insufficient quantity or item not found.");
                    }
                } catch (NumberFormatException e) {
                    showError("Input Error", "Please enter a valid number for Quantity.");
                } catch (SQLException e) {
                    showError("Database Error", "Failed to record movement: " + e.getMessage());
                }
            }
            return null;
        });
        dialog.showAndWait();
    }

    private void showTransferStockDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Transfer Stock");
        dialog.setHeaderText("Transfer inventory between locations");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        ComboBox<String> itemCodeCombo = new ComboBox<>();
        itemCodeCombo.setPromptText("Select Item Code");
        try {
            inventoryOps.getAllInventoryItems().forEach(item -> itemCodeCombo.getItems().add(item.getItemCode()));
        } catch (SQLException e) {
            showError("Database Error", "Failed to load items: " + e.getMessage());
            return;
        }
        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity");
        TextField fromLocationField = new TextField();
        fromLocationField.setPromptText("From Location");
        TextField toLocationField = new TextField();
        toLocationField.setPromptText("To Location");
        grid.addRow(0, new Label("Item Code:"), itemCodeCombo);
        grid.addRow(1, new Label("Quantity:"), quantityField);
        grid.addRow(2, new Label("From Location:"), fromLocationField);
        grid.addRow(3, new Label("To Location:"), toLocationField);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    String itemCode = itemCodeCombo.getValue();
                    int quantity = Integer.parseInt(quantityField.getText().trim());
                    String fromLocation = fromLocationField.getText().trim();
                    String toLocation = toLocationField.getText().trim();
                    if (itemCode == null || itemCode.isEmpty() || quantity <= 0 || fromLocation.isEmpty() || toLocation.isEmpty()) {
                        showError("Validation Error", "Please fill all fields with valid data.");
                        return null;
                    }
                    boolean successOut = inventoryOps.recordStockMovement(itemCode, "TRANSFER_OUT", quantity, "Transfer to " + toLocation, currentUser.getUsername());
                    boolean successIn = inventoryOps.recordStockMovement(itemCode, "TRANSFER_IN", quantity, "Transfer from " + fromLocation, currentUser.getUsername());
                    if (successOut && successIn) {
                        showInfo("Success", "Stock transferred successfully.");
                        refreshInventoryData();
                    } else {
                        showError("Error", "Failed to transfer stock. Check item, quantity, and locations.");
                    }
                } catch (NumberFormatException e) {
                    showError("Input Error", "Please enter a valid number for Quantity.");
                } catch (SQLException e) {
                    showError("Database Error", "Failed to record movement: " + e.getMessage());
                }
            }
            return null;
        });
        dialog.showAndWait();
    }

    private void showStockAdjustmentDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Stock Adjustment");
        dialog.setHeaderText("Adjust inventory quantity (e.g., for discrepancies, damage)");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        ComboBox<String> itemCodeCombo = new ComboBox<>();
        itemCodeCombo.setPromptText("Select Item Code");
        try {
            inventoryOps.getAllInventoryItems().forEach(item -> itemCodeCombo.getItems().add(item.getItemCode()));
        } catch (SQLException e) {
            showError("Database Error", "Failed to load items: " + e.getMessage());
            return;
        }
        TextField quantityField = new TextField();
        quantityField.setPromptText("Adjustment Quantity (+/-)");
        TextField reasonField = new TextField();
        reasonField.setPromptText("Reason for adjustment");
        grid.addRow(0, new Label("Item Code:"), itemCodeCombo);
        grid.addRow(1, new Label("Quantity:"), quantityField);
        grid.addRow(2, new Label("Reason:"), reasonField);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    String itemCode = itemCodeCombo.getValue();
                    int quantity = Integer.parseInt(quantityField.getText().trim());
                    String reason = reasonField.getText().trim();
                    if (itemCode == null || itemCode.isEmpty() || reason.isEmpty()) {
                        showError("Validation Error", "Please select an item, enter quantity, and reason.");
                        return null;
                    }
                    if (inventoryOps.recordStockMovement(itemCode, "ADJUSTMENT", quantity, "Reason: " + reason, currentUser.getUsername())) {
                        showInfo("Success", "Stock adjusted successfully.");
                        refreshInventoryData();
                    } else {
                        showError("Error", "Failed to adjust stock. Check item or quantity.");
                    }
                } catch (NumberFormatException e) {
                    showError("Input Error", "Please enter a valid number for Quantity.");
                } catch (SQLException e) {
                    showError("Database Error", "Failed to record movement: " + e.getMessage());
                }
            }
            return null;
        });
        dialog.showAndWait();
    }

    private void generateReport(String reportType) {
        try {
            String content;
            switch (reportType) {
                case "Stock Levels Report":
                    content = inventoryOps.getAllInventoryItems().stream()
                            .map(item -> String.format("%s: %d units", item.getItemCode(), item.getQuantityOnHand()))
                            .collect(Collectors.joining("\n"));
                    break;
                case "Low Stock Alert":
                    content = inventoryOps.getAllInventoryItems().stream()
                            .filter(InventoryOperations.InventoryItem::isLowStock)
                            .map(item -> String.format("%s: %d units (below %d)", item.getItemCode(), item.getQuantityOnHand(), item.getReorderPoint()))
                            .collect(Collectors.joining("\n"));
                    break;
                case "Inventory Valuation":
                    InventoryOperations.InventoryStatistics stats = inventoryOps.getInventoryStatistics();
                    content = String.format("Total Inventory Value: $%,.2f", stats.getTotalValue());
                    break;
                default:
                    content = "Report generation for " + reportType + " will be implemented.";
            }
            showInfo(reportType, content.isEmpty() ? "No data available." : content);
        } catch (SQLException e) {
            showError("Database Error", "Failed to generate report: " + e.getMessage());
        }
    }

    private void applyFilters(String category, String searchTerm, String statusFilter, String stockFilter) {
        try {
            List<InventoryOperations.InventoryItem> filteredItems = inventoryOps.getAllInventoryItems().stream()
                    .filter(item -> {
                        boolean categoryMatch = item.getCategory().equalsIgnoreCase(category);
                        boolean searchMatch = searchTerm.isEmpty() ||
                                item.getItemCode().toLowerCase().contains(searchTerm.toLowerCase()) ||
                                item.getDescription().toLowerCase().contains(searchTerm.toLowerCase());
                        boolean statusMatch = "All Status".equalsIgnoreCase(statusFilter) || item.getStatus().equalsIgnoreCase(statusFilter);
                        boolean stockMatch = true;
                        if ("In Stock".equalsIgnoreCase(stockFilter)) {
                            stockMatch = item.getQuantityOnHand() > item.getReorderPoint();
                        } else if ("Low Stock".equalsIgnoreCase(stockFilter)) {
                            stockMatch = item.isLowStock();
                        } else if ("Out of Stock".equalsIgnoreCase(stockFilter)) {
                            stockMatch = item.isOutOfStock();
                        }
                        return categoryMatch && searchMatch && statusMatch && stockMatch;
                    })
                    .collect(Collectors.toList());
            TableView<InventoryOperations.InventoryItem> currentTable = getCurrentTable();
            currentTable.setItems(FXCollections.observableArrayList(filteredItems));
        } catch (SQLException e) {
            showError("Error", "Failed to apply filters: " + e.getMessage());
        }
    }

    private TableView<InventoryOperations.InventoryItem> getCurrentTable() {
        String selectedTabText = inventoryTabs.getSelectionModel().getSelectedItem().getText();
        if (selectedTabText.contains("Automotive")) return rawMaterialsTable;
        if (selectedTabText.contains("Electronics")) return componentsTable;
        return finishedGoodsTable;
    }

    private void refreshInventoryData() {
        try {
            InventoryOperations.InventoryStatistics stats = inventoryOps.getInventoryStatistics();
            totalItemsLabel.setText(String.valueOf(stats.getTotalItems()));
            lowStockItemsLabel.setText(String.valueOf(stats.getLowStockItems()));
            totalValueLabel.setText(String.format("$%,.2f", stats.getTotalValue()));
            reorderItemsLabel.setText(String.valueOf(stats.getReorderItems()));
            String selectedTabText = inventoryTabs.getSelectionModel().getSelectedItem().getText();
            if (selectedTabText.contains("Automotive")) {
                rawMaterialsTable.setItems(FXCollections.observableArrayList(inventoryOps.getInventoryItemsByCategory("AUTOMOTIVE")));
            } else if (selectedTabText.contains("Electronics")) {
                componentsTable.setItems(FXCollections.observableArrayList(inventoryOps.getInventoryItemsByCategory("ELECTRONICS")));
            } else if (selectedTabText.contains("Furniture")) {
                finishedGoodsTable.setItems(FXCollections.observableArrayList(inventoryOps.getInventoryItemsByCategory("FURNITURE")));
            } else if (selectedTabText.contains("Stock Movements")) {
                movementsTable.setItems(FXCollections.observableArrayList(inventoryOps.getAllStockMovements()));
            }
        } catch (SQLException e) {
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

    private boolean hasPermission(String user_management) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
} 