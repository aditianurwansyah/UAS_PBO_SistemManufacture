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
import java.util.UUID;

/**
 * Modern Production Management interface for creating and managing production orders
 */
public class ProductionManagementView {
    private ManufacturingOperations manufacturingOps;
    private final User currentUser;
    private TableView<ProductionOrder> ordersTable;
    private TextField orderIdField;
    private TextField customerNameField;
    private ComboBox<String> productTypeCombo;
    private TextField productNameField;
    private TextField quantityField;
    private ComboBox<String> priorityCombo;
    private DatePicker dueDatePicker;
    private TextArea notesArea;
    private ProductionOrder selectedOrder;

    public ProductionManagementView(User user) {
        this.currentUser = user;
        try {
            this.manufacturingOps = new ManufacturingOperations();
        } catch (SQLException e) {
            showError("Database Error", "Failed to connect to database: " + e.getMessage());
        }
    }

    public VBox getView() {
        VBox mainContainer = new VBox(10);
        mainContainer.setPadding(new Insets(10));
        mainContainer.setStyle("""
            -fx-background-color: linear-gradient(to bottom right, #f8f9fa, #e9ecef);
            """);

        // Header
        VBox headerSection = createHeaderSection();
        
        // Main content with split view
        HBox contentSection = createContentSection();
        
        mainContainer.getChildren().addAll(headerSection, createSeparator(), contentSection);
        
        refreshOrdersTable();
        return mainContainer;
    }

    private VBox createHeaderSection() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Text titleText = new Text("🏭 Production Order Management");
        titleText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleText.setFill(Color.web("#2c3e50"));
        
        Text subtitleText = new Text("Create, track, and manage production orders");
        subtitleText.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        subtitleText.setFill(Color.web("#7f8c8d"));

        header.getChildren().addAll(titleText, subtitleText);
        return header;
    }

    private HBox createContentSection() {
        HBox contentSection = new HBox(20);
        contentSection.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(contentSection, Priority.ALWAYS);
        
        // Left side - Orders table
        VBox tableSection = createTableSection();
        HBox.setHgrow(tableSection, Priority.ALWAYS);
        
        // Right side - Order form
        VBox formSection = createFormSection();
        formSection.setPrefWidth(400);
        
        contentSection.getChildren().addAll(tableSection, formSection);
        return contentSection;
    }

    private VBox createTableSection() {
        VBox section = new VBox(5);
        section.setPadding(new Insets(10));
        section.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 12;
            -fx-border-radius: 12;
            """);
        
        // Add drop shadow
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.1));
        shadow.setRadius(8);
        shadow.setOffsetY(2);
        section.setEffect(shadow);
        
        Text sectionTitle = new Text("Production Orders");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        sectionTitle.setFill(Color.web("#2c3e50"));
        
        // Filter controls
        HBox filterBox = createFilterControls();
        
        // Orders table
        ordersTable = createOrdersTable();
        VBox.setVgrow(ordersTable, Priority.ALWAYS);
        
        // Action buttons
        HBox actionButtons = createTableActionButtons();
        
        section.getChildren().addAll(sectionTitle, filterBox, ordersTable, actionButtons);
        return section;
    }
    

    private HBox createFilterControls() {
        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        filterBox.setPadding(new Insets(0, 0, 10, 0));
        
        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All Status", "PENDING", "IN_PROGRESS", "COMPLETED", "SHIPPED");
        statusFilter.setValue("All Status");
        statusFilter.setStyle("-fx-pref-width: 120;");
        
        ComboBox<String> priorityFilter = new ComboBox<>();
        priorityFilter.getItems().addAll("All Priority", "URGENT", "HIGH", "MEDIUM", "LOW");
        priorityFilter.setValue("All Priority");
        priorityFilter.setStyle("-fx-pref-width: 120;");
        
        TextField searchField = new TextField();
        searchField.setPromptText("Search orders...");
        searchField.setStyle("-fx-pref-width: 200;");
        
        Button searchBtn = createStyledButton("🔍 Search", "#3498db", 80);
        Button refreshBtn = createStyledButton("🔄 Refresh", "#27ae60", 80);
        refreshBtn.setOnAction(e -> refreshOrdersTable());
        
        filterBox.getChildren().addAll(
            new Label("Status:"), statusFilter,
            new Label("Priority:"), priorityFilter,
            searchField, searchBtn, refreshBtn
        );
        
        return filterBox;
    }

    private TableView<ProductionOrder> createOrdersTable() {
        TableView<ProductionOrder> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("""
            -fx-background-color: transparent;
            -fx-background-radius: 8;
            """);
        
        // Order ID column with custom cell factory
        TableColumn<ProductionOrder, String> orderIdCol = new TableColumn<>("Order ID");
        orderIdCol.setCellValueFactory(data -> data.getValue().orderIdProperty());
        orderIdCol.setPrefWidth(100);
        orderIdCol.setCellFactory(col -> new TableCell<ProductionOrder, String>() {
            @Override
            protected void updateItem(String orderId, boolean empty) {
                super.updateItem(orderId, empty);
                if (empty || orderId == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(orderId);
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #3498db;");
                }
            }
        });
        
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
        quantityCol.setCellFactory(col -> new TableCell<ProductionOrder, Number>() {
            @Override
            protected void updateItem(Number quantity, boolean empty) {
                super.updateItem(quantity, empty);
                if (empty || quantity == null) {
                    setText(null);
                } else {
                    setText(String.format("%,d", quantity.intValue()));
                    setAlignment(Pos.CENTER_RIGHT);
                }
            }
        });
        
        // Status column with color coding
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
                    setAlignment(Pos.CENTER);
                }
            }
        });
        priorityCol.setPrefWidth(80);
        
        // Progress column with progress bar
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
                    VBox container = new VBox(2);
                    container.setAlignment(Pos.CENTER);
                    
                    ProgressBar progressBar = new ProgressBar(progress.doubleValue() / 100.0);
                    progressBar.setPrefWidth(80);
                    progressBar.setPrefHeight(12);
                    
                    Label percentLabel = new Label(String.format("%.0f%%", progress.doubleValue()));
                    percentLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10));
                    
                    container.getChildren().addAll(progressBar, percentLabel);
                    setGraphic(container);
                    setText(null);
                }
            }
        });
        progressCol.setPrefWidth(100);
        
        table.getColumns().addAll(orderIdCol, customerCol, productCol, quantityCol, 
                                 statusCol, priorityCol, progressCol);
        
        // Row selection handler
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedOrder = newVal;
            if (newVal != null) {
                populateFormWithOrder(newVal);
            }
        });
        
        return table;
    }

    private HBox createTableActionButtons() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button startBtn = createStyledButton("▶️ Start Production", "#27ae60", 140);
        startBtn.setOnAction(e -> startSelectedOrder());
        
        Button updateBtn = createStyledButton("📝 Update Progress", "#f39c12", 140);
        updateBtn.setOnAction(e -> updateOrderProgress());
        
        Button cancelBtn = createStyledButton("❌ Cancel Order", "#e74c3c", 120);
        cancelBtn.setOnAction(e -> cancelSelectedOrder());
        
        buttonBox.getChildren().addAll(startBtn, updateBtn, cancelBtn);
        return buttonBox;
    }

    private VBox createFormSection() {
        VBox section = new VBox(20);
        section.setPadding(new Insets(20));
        section.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 12;
            -fx-border-radius: 12;
            """);
        
        // Add drop shadow
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.1));
        shadow.setRadius(8);
        shadow.setOffsetY(2);
        section.setEffect(shadow);
        
        Text sectionTitle = new Text("Create New Order");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        sectionTitle.setFill(Color.web("#2c3e50"));
        
        // Form fields
        VBox formFields = createFormFields();
        
        // Form buttons
        HBox formButtons = createFormButtons();
        
        section.getChildren().addAll(sectionTitle, formFields, formButtons);
        return section;
    }

    private VBox createFormFields() {
        VBox fields = new VBox(15);
        
        // Order ID (auto-generated)
        orderIdField = createStyledTextField("Auto-generated");
        orderIdField.setEditable(false);
        orderIdField.setText(generateOrderId());
        addFormField(fields, "Order ID:", orderIdField);
        
        // Customer Name
        customerNameField = createStyledTextField("Enter customer name");
        addFormField(fields, "Customer Name:", customerNameField);
        
        // Product Type
        productTypeCombo = new ComboBox<>();
        productTypeCombo.getItems().addAll("AUTOMOTIVE", "ELECTRONICS", "FURNITURE");
        productTypeCombo.setValue("AUTOMOTIVE");
        productTypeCombo.setStyle("-fx-pref-width: 200;");
        addFormField(fields, "Product Category:", productTypeCombo);
        
        // Product Name
        productNameField = createStyledTextField("Enter product name");
        addFormField(fields, "Product Name:", productNameField);
        
        // Quantity
        quantityField = createStyledTextField("Enter quantity");
        addFormField(fields, "Quantity:", quantityField);
        
        // Priority
        priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll("URGENT", "HIGH", "MEDIUM", "LOW");
        priorityCombo.setValue("MEDIUM");
        priorityCombo.setStyle("-fx-pref-width: 200;");
        addFormField(fields, "Priority:", priorityCombo);
        
        // Due Date
        dueDatePicker = new DatePicker();
        dueDatePicker.setStyle("-fx-pref-width: 200;");
        addFormField(fields, "Due Date:", dueDatePicker);
        
        // Notes
        notesArea = new TextArea();
        notesArea.setPromptText("Enter order notes or special requirements...");
        notesArea.setPrefRowCount(3);
        notesArea.setStyle("-fx-pref-width: 200;");
        addFormField(fields, "Notes:", notesArea);
        
        return fields;
    }

    private void addFormField(VBox container, String labelText, Control control) {
        VBox fieldContainer = new VBox(5);
        
        Label label = new Label(labelText);
        label.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        label.setTextFill(Color.web("#2c3e50"));
        
        fieldContainer.getChildren().addAll(label, control);
        container.getChildren().add(fieldContainer);
    }

    private TextField createStyledTextField(String promptText) {
        TextField field = new TextField();
        field.setPromptText(promptText);
        field.setStyle("""
            -fx-background-color: #f8f9fa;
            -fx-background-radius: 6;
            -fx-border-radius: 6;
            -fx-border-color: #dee2e6;
            -fx-padding: 8 12;
            -fx-pref-width: 200;
            """);
        
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle("""
                    -fx-background-color: white;
                    -fx-background-radius: 6;
                    -fx-border-radius: 6;
                    -fx-border-color: #3498db;
                    -fx-border-width: 2;
                    -fx-padding: 8 12;
                    -fx-pref-width: 200;
                    """);
            } else {
                field.setStyle("""
                    -fx-background-color: #f8f9fa;
                    -fx-background-radius: 6;
                    -fx-border-radius: 6;
                    -fx-border-color: #dee2e6;
                    -fx-padding: 8 12;
                    -fx-pref-width: 200;
                    """);
            }
        });
        
        return field;
    }

    private HBox createFormButtons() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button createBtn = createStyledButton("✅ Create Order", "#27ae60", 120);
        createBtn.setOnAction(e -> createNewOrder());
        
        Button clearBtn = createStyledButton("🗑️ Clear Form", "#95a5a6", 120);
        clearBtn.setOnAction(e -> clearForm());
        
        buttonBox.getChildren().addAll(createBtn, clearBtn);
        return buttonBox;
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
    private void createNewOrder() {
        if (!validateForm()) {
            return;
        }
        
        try {
            ProductionOrder order = new ProductionOrder(
                orderIdField.getText(),
                customerNameField.getText(),
                generateProductId(),
                productNameField.getText(),
                Integer.parseInt(quantityField.getText()),
                priorityCombo.getValue()
            );
            
            if (dueDatePicker.getValue() != null) {
                order.setDueDate(dueDatePicker.getValue().atStartOfDay());
            }
            
            order.setNotes(notesArea.getText());
            
            if (manufacturingOps.createProductionOrder(order)) {
                showSuccess("Order created successfully!");
                clearForm();
                refreshOrdersTable();
            } else {
                showError("Error", "Failed to create production order");
            }
            
        } catch (Exception e) {
            showError("Error", "Failed to create order: " + e.getMessage());
        }
    }

    private void startSelectedOrder() {
        if (selectedOrder == null) {
            showError("No Selection", "Please select an order to start production.");
            return;
        }
        
        if (!"PENDING".equals(selectedOrder.getStatus())) {
            showError("Invalid Status", "Only pending orders can be started.");
            return;
        }
        
        // Show production line selection dialog
        showStartProductionDialog();
    }

    private void updateOrderProgress() {
        if (selectedOrder == null) {
            showError("No Selection", "Please select an order to update.");
            return;
        }
        
        showUpdateProgressDialog();
    }

    private void cancelSelectedOrder() {
        if (selectedOrder == null) {
            showError("No Selection", "Please select an order to cancel.");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Cancellation");
        confirmAlert.setHeaderText("Cancel Production Order");
        confirmAlert.setContentText("Are you sure you want to cancel order " + selectedOrder.getOrderId() + "?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Implementation for canceling order
                showInfo("Order Cancelled", "Order " + selectedOrder.getOrderId() + " has been cancelled.");
                refreshOrdersTable();
            }
        });
    }

    private void showStartProductionDialog() {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Start Production");
        dialog.setHeaderText("Start production for Order: " + selectedOrder.getOrderId());
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        ComboBox<String> lineCombo = new ComboBox<>();
        lineCombo.getItems().addAll(manufacturingOps.getAvailableProductionLines());
        lineCombo.setValue(lineCombo.getItems().get(0));
        
        TextField operatorField = new TextField();
        operatorField.setPromptText("Enter operator name");
        operatorField.setText(currentUser.getUsername());
        
        grid.add(new Label("Production Line:"), 0, 0);
        grid.add(lineCombo, 1, 0);
        grid.add(new Label("Operator:"), 0, 1);
        grid.add(operatorField, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return new String[]{lineCombo.getValue(), operatorField.getText()};
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(result -> {
            if (manufacturingOps.startProduction(selectedOrder.getOrderId(), result[0], result[1])) {
                showSuccess("Production started successfully!");
                refreshOrdersTable();
            } else {
                showError("Error", "Failed to start production");
            }
        });
    }

    private void showUpdateProgressDialog() {
        Dialog<Double[]> dialog = new Dialog<>();
        dialog.setTitle("Update Progress");
        dialog.setHeaderText("Update progress for Order: " + selectedOrder.getOrderId());
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        Slider progressSlider = new Slider(0, 100, selectedOrder.getCompletionPercentage());
        progressSlider.setShowTickLabels(true);
        progressSlider.setShowTickMarks(true);
        progressSlider.setMajorTickUnit(25);
        
        Label progressLabel = new Label(String.format("%.0f%%", progressSlider.getValue()));
        progressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            progressLabel.setText(String.format("%.0f%%", newVal.doubleValue()));
        });
        
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Update notes...");
        notesArea.setPrefRowCount(3);
        
        grid.add(new Label("Progress:"), 0, 0);
        grid.add(progressSlider, 1, 0);
        grid.add(progressLabel, 2, 0);
        grid.add(new Label("Notes:"), 0, 1);
        grid.add(notesArea, 1, 1, 2, 1);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return new Double[]{progressSlider.getValue()};
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(result -> {
            if (manufacturingOps.updateProductionProgress(selectedOrder.getOrderId(), 
                    result[0], notesArea.getText())) {
                showSuccess("Progress updated successfully!");
                refreshOrdersTable();
            } else {
                showError("Error", "Failed to update progress");
            }
        });
    }

    private boolean validateForm() {
        if (customerNameField.getText().trim().isEmpty()) {
            showError("Validation Error", "Customer name is required");
            return false;
        }
        
        if (productNameField.getText().trim().isEmpty()) {
            showError("Validation Error", "Product name is required");
            return false;
        }
        
        try {
            int quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            showError("Validation Error", "Please enter a valid quantity");
            return false;
        }
        
        return true;
    }

    private void populateFormWithOrder(ProductionOrder order) {
        orderIdField.setText(order.getOrderId());
        customerNameField.setText(order.getCustomerName());
        productNameField.setText(order.getProductName());
        quantityField.setText(String.valueOf(order.getQuantity()));
        priorityCombo.setValue(order.getPriority());
        notesArea.setText(order.getNotes());
        
        if (order.getDueDate() != null) {
            dueDatePicker.setValue(order.getDueDate().toLocalDate());
        }
    }

    private void clearForm() {
        orderIdField.setText(generateOrderId());
        customerNameField.clear();
        productNameField.clear();
        quantityField.clear();
        notesArea.clear();
        dueDatePicker.setValue(null);
        priorityCombo.setValue("MEDIUM");
        productTypeCombo.setValue("AUTOMOTIVE");
    }

    private void refreshOrdersTable() {
        try {
            List<ProductionOrder> orders = manufacturingOps.getAllProductionOrders();
            ordersTable.setItems(FXCollections.observableArrayList(orders));
        } catch (Exception e) {
            showError("Error", "Failed to refresh orders: " + e.getMessage());
        }
    }

    private String generateOrderId() {
        return "PO-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + 
               "-" + String.format("%04d", (int)(Math.random() * 10000));
    }

    private String generateProductId() {
        return productTypeCombo.getValue().substring(0, 3) + "-" + 
               UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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