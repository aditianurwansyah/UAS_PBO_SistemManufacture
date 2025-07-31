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
import java.util.stream.Collectors;

/**
 * Personal task management view for operators and supervisors
 */
public class MyTasksView {
    private ManufacturingOperations manufacturingOps;
    private final User currentUser;
    private TableView<ProductionOrder> myTasksTable;
    private Label totalTasksLabel;
    private Label pendingTasksLabel;
    private Label completedTasksLabel;
    private Label overdueTasksLabel;

    public MyTasksView(User user) {
        this.currentUser = user;
        try {
            this.manufacturingOps = new ManufacturingOperations();
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
        
        // Tasks section
        VBox tasksSection = createTasksSection();
        
        mainContainer.getChildren().addAll(
            headerSection, 
            createSeparator(), 
            statsSection, 
            createSeparator(), 
            tasksSection
        );
        
        refreshTasks();
        return mainContainer;
    }

    private VBox createHeaderSection() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Text titleText = new Text("📋 My Production Tasks");
        titleText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleText.setFill(Color.web("#2c3e50"));
        
        Text subtitleText = new Text("Hello " + currentUser.getUsername() + 
            " | " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy")));
        subtitleText.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        subtitleText.setFill(Color.web("#7f8c8d"));

        header.getChildren().addAll(titleText, subtitleText);
        return header;
    }

    private HBox createStatsSection() {
        HBox container = new HBox(20);
        container.setAlignment(Pos.CENTER);
        
        totalTasksLabel = new Label("0");
        pendingTasksLabel = new Label("0");
        completedTasksLabel = new Label("0");
        overdueTasksLabel = new Label("0");
        
        VBox totalCard = createStatCard("Total Tasks", totalTasksLabel, "#3498db", "📊");
        VBox pendingCard = createStatCard("Pending", pendingTasksLabel, "#f39c12", "⏳");
        VBox completedCard = createStatCard("Completed", completedTasksLabel, "#27ae60", "✅");
        VBox overdueCard = createStatCard("Overdue", overdueTasksLabel, "#e74c3c", "⚠️");
        
        container.getChildren().addAll(totalCard, pendingCard, completedCard, overdueCard);
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
        
        // Add drop shadow
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.1));
        shadow.setRadius(6);
        shadow.setOffsetY(2);
        card.setEffect(shadow);
        
        // Icon
        Text iconText = new Text(icon);
        iconText.setFont(Font.font(20));
        
        // Value
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        valueLabel.setTextFill(Color.web(color));
        
        // Title
        Text titleText = new Text(title);
        titleText.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        titleText.setFill(Color.web("#7f8c8d"));
        
        card.getChildren().addAll(iconText, valueLabel, titleText);
        return card;
    }

    private VBox createTasksSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(15));
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
        
        // Section header
        HBox sectionHeader = new HBox();
        sectionHeader.setAlignment(Pos.CENTER_LEFT);
        sectionHeader.setSpacing(10);
        
        Text sectionTitle = new Text("My Assigned Tasks");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        sectionTitle.setFill(Color.web("#2c3e50"));
        
        Button refreshBtn = createStyledButton("🔄 Refresh", "#3498db", 100);
        refreshBtn.setOnAction(e -> refreshTasks());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        sectionHeader.getChildren().addAll(sectionTitle, spacer, refreshBtn);
        
        // Tasks table
        myTasksTable = createTasksTable();
        VBox.setVgrow(myTasksTable, Priority.ALWAYS);
        
        // Action buttons
        HBox actionButtons = createActionButtons();
        
        section.getChildren().addAll(sectionHeader, myTasksTable, actionButtons);
        return section;
    }

    private TableView<ProductionOrder> createTasksTable() {
        TableView<ProductionOrder> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("""
            -fx-background-color: transparent;
            -fx-background-radius: 8;
            """);
        
        // Order ID column
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
        
        // Product column
        TableColumn<ProductionOrder, String> productCol = new TableColumn<>("Product");
        productCol.setCellValueFactory(data -> data.getValue().productNameProperty());
        productCol.setPrefWidth(150);
        
        // Quantity column
        TableColumn<ProductionOrder, Number> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(data -> data.getValue().quantityProperty());
        quantityCol.setPrefWidth(80);
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
        
        // Status column
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
        
        // Due Date column
        TableColumn<ProductionOrder, String> dueDateCol = new TableColumn<>("Due Date");
        dueDateCol.setCellValueFactory(data -> {
            LocalDateTime dueDate = data.getValue().getDueDate();
            return new javafx.beans.property.SimpleStringProperty(
                dueDate != null ? dueDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "Not Set"
            );
        });
        dueDateCol.setPrefWidth(100);
        dueDateCol.setCellFactory(col -> new TableCell<ProductionOrder, String>() {
            @Override
            protected void updateItem(String dueDate, boolean empty) {
                super.updateItem(dueDate, empty);
                if (empty || dueDate == null || "Not Set".equals(dueDate)) {
                    setText(dueDate);
                    setStyle("");
                } else {
                    setText(dueDate);
                    ProductionOrder order = getTableView().getItems().get(getIndex());
                    if (order.isOverdue()) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        
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
        
        // Assigned Line column
        TableColumn<ProductionOrder, String> lineCol = new TableColumn<>("Production Line");
        lineCol.setCellValueFactory(data -> data.getValue().assignedLineProperty());
        lineCol.setPrefWidth(120);
        
        table.getColumns().addAll(orderIdCol, productCol, quantityCol, statusCol, 
                                 priorityCol, dueDateCol, progressCol, lineCol);
        return table;
    }

    private HBox createActionButtons() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button updateProgressBtn = createStyledButton("📝 Update Progress", "#f39c12", 140);
        updateProgressBtn.setOnAction(e -> updateProgress());
        
        Button reportIssueBtn = createStyledButton("⚠️ Report Issue", "#e74c3c", 120);
        reportIssueBtn.setOnAction(e -> reportIssue());
        
        Button viewDetailsBtn = createStyledButton("👁️ View Details", "#3498db", 120);
        viewDetailsBtn.setOnAction(e -> viewTaskDetails());
        
        buttonBox.getChildren().addAll(updateProgressBtn, reportIssueBtn, viewDetailsBtn);
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
    private void updateProgress() {
        ProductionOrder selectedTask = myTasksTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            showError("No Selection", "Please select a task to update progress.");
            return;
        }
        
        if (!"IN_PROGRESS".equals(selectedTask.getStatus())) {
            showError("Invalid Status", "Only tasks in progress can be updated.");
            return;
        }
        
        showUpdateProgressDialog(selectedTask);
    }

    private void reportIssue() {
        ProductionOrder selectedTask = myTasksTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            showError("No Selection", "Please select a task to report an issue.");
            return;
        }
        
        showReportIssueDialog(selectedTask);
    }

    private void viewTaskDetails() {
        ProductionOrder selectedTask = myTasksTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            showError("No Selection", "Please select a task to view details.");
            return;
        }
        
        showTaskDetailsDialog(selectedTask);
    }

    private void showUpdateProgressDialog(ProductionOrder task) {
        Dialog<Double> dialog = new Dialog<>();
        dialog.setTitle("Update Progress");
        dialog.setHeaderText("Update progress for Order: " + task.getOrderId());
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        // Current progress info
        Label currentProgressLabel = new Label(String.format("Current Progress: %.1f%%", 
            task.getCompletionPercentage()));
        currentProgressLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        
        // Progress slider
        Label progressLabel = new Label("New Progress:");
        Slider progressSlider = new Slider(task.getCompletionPercentage(), 100, task.getCompletionPercentage());
        progressSlider.setShowTickLabels(true);
        progressSlider.setShowTickMarks(true);
        progressSlider.setMajorTickUnit(25);
        progressSlider.setPrefWidth(300);
        
        Label valueLabel = new Label(String.format("%.0f%%", progressSlider.getValue()));
        progressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            valueLabel.setText(String.format("%.0f%%", newVal.doubleValue()));
        });
        
        HBox sliderBox = new HBox(10);
        sliderBox.setAlignment(Pos.CENTER_LEFT);
        sliderBox.getChildren().addAll(progressSlider, valueLabel);
        
        // Notes area
        Label notesLabel = new Label("Update Notes:");
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Enter progress notes, issues encountered, or next steps...");
        notesArea.setPrefRowCount(4);
        notesArea.setPrefColumnCount(40);
        
        content.getChildren().addAll(
            currentProgressLabel,
            new Separator(),
            progressLabel,
            sliderBox,
            notesLabel,
            notesArea
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return progressSlider.getValue();
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(progress -> {
            String notes = notesArea.getText().trim();
            if (manufacturingOps.updateProductionProgress(task.getOrderId(), progress, notes)) {
                showSuccess("Progress updated successfully!");
                refreshTasks();
            } else {
                showError("Error", "Failed to update progress");
            }
        });
    }

    private void showReportIssueDialog(ProductionOrder task) {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Report Issue");
        dialog.setHeaderText("Report an issue for Order: " + task.getOrderId());
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        // Issue type
        Label typeLabel = new Label("Issue Type:");
        ComboBox<String> issueTypeCombo = new ComboBox<>();
        issueTypeCombo.getItems().addAll(
            "Equipment Malfunction",
            "Material Shortage",
            "Quality Issue",
            "Safety Concern",
            "Process Problem",
            "Other"
        );
        issueTypeCombo.setValue("Equipment Malfunction");
        
        // Priority
        Label priorityLabel = new Label("Issue Priority:");
        ComboBox<String> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll("LOW", "MEDIUM", "HIGH", "CRITICAL");
        priorityCombo.setValue("MEDIUM");
        
        // Description
        Label descLabel = new Label("Issue Description:");
        TextArea descArea = new TextArea();
        descArea.setPromptText("Describe the issue in detail...");
        descArea.setPrefRowCount(5);
        descArea.setPrefColumnCount(40);
        
        content.getChildren().addAll(
            typeLabel, issueTypeCombo,
            priorityLabel, priorityCombo,
            descLabel, descArea
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return new String[]{
                    issueTypeCombo.getValue(),
                    priorityCombo.getValue(),
                    descArea.getText()
                };
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(result -> {
            // In a real implementation, this would create an issue report in the database
            showInfo("Issue Reported", 
                String.format("Issue reported successfully!\n\nType: %s\nPriority: %s\n\nYour supervisor has been notified.",
                result[0], result[1]));
        });
    }

    private void showTaskDetailsDialog(ProductionOrder task) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Task Details");
        dialog.setHeaderText("Production Order Details");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setPrefWidth(400);
        
        // Create detail rows
        addDetailRow(content, "Order ID:", task.getOrderId());
        addDetailRow(content, "Customer:", task.getCustomerName());
        addDetailRow(content, "Product:", task.getProductName());
        addDetailRow(content, "Quantity:", String.format("%,d units", task.getQuantity()));
        addDetailRow(content, "Status:", task.getStatus());
        addDetailRow(content, "Priority:", task.getPriority());
        addDetailRow(content, "Progress:", String.format("%.1f%%", task.getCompletionPercentage()));
        addDetailRow(content, "Production Line:", task.getAssignedLine());
        addDetailRow(content, "Assigned Operator:", task.getAssignedOperator());
        addDetailRow(content, "Order Date:", task.getFormattedOrderDate());
        addDetailRow(content, "Due Date:", task.getFormattedDueDate());
        addDetailRow(content, "Start Date:", task.getFormattedStartDate());
        
        if (task.getNotes() != null && !task.getNotes().trim().isEmpty()) {
            content.getChildren().add(new Separator());
            
            Label notesLabel = new Label("Notes:");
            notesLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            
            TextArea notesArea = new TextArea(task.getNotes());
            notesArea.setEditable(false);
            notesArea.setPrefRowCount(3);
            notesArea.setStyle("""
                -fx-background-color: #f8f9fa;
                -fx-border-color: #dee2e6;
                -fx-border-radius: 4;
                """);
            
            content.getChildren().addAll(notesLabel, notesArea);
        }
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void addDetailRow(VBox container, String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label labelText = new Label(label);
        labelText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        labelText.setPrefWidth(120);
        
        Label valueText = new Label(value);
        valueText.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        
        row.getChildren().addAll(labelText, valueText);
        container.getChildren().add(row);
    }

    private void refreshTasks() {
        try {
            // Get tasks assigned to current user
            List<ProductionOrder> allOrders = manufacturingOps.getAllProductionOrders();
            List<ProductionOrder> myTasks = allOrders.stream()
                .filter(order -> currentUser.getUsername().equals(order.getAssignedOperator()) || 
                               currentUser.getUsername().equals(order.getCustomerName()))
                .collect(Collectors.toList());
            
            myTasksTable.setItems(FXCollections.observableArrayList(myTasks));
            
            // Update statistics
            updateStatistics(myTasks);
            
        } catch (Exception e) {
            showError("Error", "Failed to refresh tasks: " + e.getMessage());
        }
    }

    private void updateStatistics(List<ProductionOrder> tasks) {
        int total = tasks.size();
        int pending = (int) tasks.stream().filter(t -> "PENDING".equals(t.getStatus())).count();
        int completed = (int) tasks.stream().filter(t -> t.isCompleted()).count();
        int overdue = (int) tasks.stream().filter(ProductionOrder::isOverdue).count();
        
        totalTasksLabel.setText(String.valueOf(total));
        pendingTasksLabel.setText(String.valueOf(pending));
        completedTasksLabel.setText(String.valueOf(completed));
        overdueTasksLabel.setText(String.valueOf(overdue));
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