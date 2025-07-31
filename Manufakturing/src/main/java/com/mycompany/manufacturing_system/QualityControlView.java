package com.mycompany.manufacturing_system;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Quality Control View for Manufacturing System
 * Provides quality inspection and defect tracking functionality
 */
public class QualityControlView {
    private User currentUser;
    private ObservableList<QualityRecord> qualityRecords;
    private TableView<QualityRecord> qualityTable;

    public QualityControlView(User currentUser) {
        this.currentUser = currentUser;
        this.qualityRecords = FXCollections.observableArrayList();
        initializeData();
    }

    public VBox getView() {
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("""
            -fx-background-color: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
            """);

        // Create main content card
        VBox contentCard = createContentCard();
        
        // Add fade-in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(600), contentCard);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        mainContainer.getChildren().add(contentCard);
        return mainContainer;
    }

    private VBox createContentCard() {
        VBox card = new VBox(25);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(30));
        card.setStyle("""
            -fx-background-color: rgba(255, 255, 255, 0.95);
            -fx-background-radius: 20;
            -fx-border-radius: 20;
            """);

        // Add drop shadow
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.2));
        shadow.setRadius(15);
        shadow.setOffsetY(8);
        card.setEffect(shadow);

        // Header section
        VBox headerSection = createHeaderSection();
        
        // Statistics cards
        HBox statsSection = createStatsSection();
        
        // Control buttons
        HBox controlSection = createControlSection();
        
        // Quality records table
        VBox tableSection = createTableSection();

        card.getChildren().addAll(headerSection, createStyledSeparator(), 
                                 statsSection, controlSection, tableSection);
        return card;
    }

    private VBox createHeaderSection() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        
        // Title
        Text titleText = new Text("🔍 Quality Control Center");
        titleText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleText.setFill(Color.web("#2c3e50"));
        
        // Subtitle
        Text subtitleText = new Text("Quality Inspections & Defect Tracking System");
        subtitleText.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        subtitleText.setFill(Color.web("#7f8c8d"));
        
        // User info
        Text userText = new Text("Inspector: " + currentUser.getDisplayName());
        userText.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        userText.setFill(Color.web("#34495e"));

        header.getChildren().addAll(titleText, subtitleText, userText);
        return header;
    }

    private HBox createStatsSection() {
        HBox statsContainer = new HBox(20);
        statsContainer.setAlignment(Pos.CENTER);
        statsContainer.setPadding(new Insets(10));

        // Statistics cards
        VBox totalInspections = createStatCard("📊", "Total Inspections", "245", "#3498db");
        VBox passedItems = createStatCard("✅", "Passed", "198", "#27ae60");
        VBox failedItems = createStatCard("❌", "Failed", "47", "#e74c3c");
        VBox passRate = createStatCard("📈", "Pass Rate", "80.8%", "#f39c12");

        statsContainer.getChildren().addAll(totalInspections, passedItems, failedItems, passRate);
        return statsContainer;
    }

    private VBox createStatCard(String icon, String title, String value, String color) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefWidth(160);
        card.setStyle(String.format("""
            -fx-background-color: white;
            -fx-background-radius: 12;
            -fx-border-radius: 12;
            -fx-border-color: %s;
            -fx-border-width: 2;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);
            """, color));

        Text iconText = new Text(icon);
        iconText.setFont(Font.font(24));

        Text titleText = new Text(title);
        titleText.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        titleText.setFill(Color.web("#7f8c8d"));

        Text valueText = new Text(value);
        valueText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        valueText.setFill(Color.web(color));

        card.getChildren().addAll(iconText, titleText, valueText);
        return card;
    }

    private HBox createControlSection() {
        HBox controlContainer = new HBox(15);
        controlContainer.setAlignment(Pos.CENTER);
        controlContainer.setPadding(new Insets(10));

        // Action buttons
        Button newInspectionBtn = createActionButton("🔍 New Inspection", "#3498db", this::showNewInspection);
        Button defectReportBtn = createActionButton("📋 Defect Report", "#e74c3c", this::showDefectReport);
        Button qualityReportBtn = createActionButton("📊 Quality Report", "#27ae60", this::showQualityReport);
        Button settingsBtn = createActionButton("⚙️ Settings", "#95a5a6", this::showQualitySettings);

        controlContainer.getChildren().addAll(newInspectionBtn, defectReportBtn, qualityReportBtn, settingsBtn);
        return controlContainer;
    }

    private Button createActionButton(String text, String color, Runnable action) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        button.setPrefWidth(140);
        button.setPrefHeight(40);
        button.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-background-radius: 8;
            -fx-border-radius: 8;
            -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0, 0, 1);
            """, color));

        // Hover effects
        button.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(100), button);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();
            
            button.setStyle(String.format("""
                -fx-background-color: derive(%s, -15%%);
                -fx-text-fill: white;
                -fx-background-radius: 8;
                -fx-border-radius: 8;
                -fx-cursor: hand;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);
                """, color));
        });

        button.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(100), button);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
            
            button.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-text-fill: white;
                -fx-background-radius: 8;
                -fx-border-radius: 8;
                -fx-cursor: hand;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0, 0, 1);
                """, color));
        });

        button.setOnAction(e -> action.run());
        return button;
    }

    private VBox createTableSection() {
        VBox tableContainer = new VBox(15);
        tableContainer.setPadding(new Insets(10));

        // Table title
        Text tableTitle = new Text("Recent Quality Inspections");
        tableTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        tableTitle.setFill(Color.web("#2c3e50"));

        // Create table
        qualityTable = new TableView<>();
        qualityTable.setItems(qualityRecords);
        qualityTable.setPrefHeight(300);
        qualityTable.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 8;
            -fx-border-radius: 8;
            """);

        // Table columns
        TableColumn<QualityRecord, String> idColumn = new TableColumn<>("Inspection ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("inspectionId"));
        idColumn.setPrefWidth(120);

        TableColumn<QualityRecord, String> productColumn = new TableColumn<>("Product");
        productColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        productColumn.setPrefWidth(200);

        TableColumn<QualityRecord, String> inspectorColumn = new TableColumn<>("Inspector");
        inspectorColumn.setCellValueFactory(new PropertyValueFactory<>("inspector"));
        inspectorColumn.setPrefWidth(150);

        TableColumn<QualityRecord, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setPrefWidth(100);

        TableColumn<QualityRecord, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("inspectionDate"));
        dateColumn.setPrefWidth(150);

        TableColumn<QualityRecord, String> notesColumn = new TableColumn<>("Notes");
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));
        notesColumn.setPrefWidth(250);

        qualityTable.getColumns().addAll(idColumn, productColumn, inspectorColumn, 
                                        statusColumn, dateColumn, notesColumn);

        // Style status column
        statusColumn.setCellFactory(column -> new TableCell<QualityRecord, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("PASSED".equals(item)) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else if ("FAILED".equals(item)) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                    }
                }
            }
        });

        tableContainer.getChildren().addAll(tableTitle, qualityTable);
        return tableContainer;
    }

    private Separator createStyledSeparator() {
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #ecf0f1; -fx-pref-height: 2;");
        separator.setPadding(new Insets(5, 0, 5, 0));
        return separator;
    }

    private void initializeData() {
        // Sample quality records
        qualityRecords.addAll(
            new QualityRecord("QC001", "Widget A", "John Smith", "PASSED", "2025-07-10 08:30", "No defects found"),
            new QualityRecord("QC002", "Widget B", "Jane Doe", "FAILED", "2025-07-10 09:15", "Surface scratches detected"),
            new QualityRecord("QC003", "Widget C", "Bob Johnson", "PASSED", "2025-07-10 10:00", "All parameters within spec"),
            new QualityRecord("QC004", "Widget A", "Alice Brown", "PENDING", "2025-07-10 10:45", "Under inspection"),
            new QualityRecord("QC005", "Widget D", "Charlie Wilson", "FAILED", "2025-07-10 11:30", "Dimensional deviation")
        );
    }

    // Action methods
    private void showNewInspection() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("New Inspection");
        alert.setHeaderText("Quality Inspection Form");
        alert.setContentText("""
            New Quality Inspection features:
            
            ✓ Product selection
            ✓ Inspection checklist
            ✓ Defect categorization
            ✓ Photo documentation
            ✓ Inspector assignment
            ✓ Quality standards verification
            
            Full inspection form interface will be available in the next update.
            """);
        alert.showAndWait();
    }

    private void showDefectReport() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Defect Report");
        alert.setHeaderText("Quality Defect Analysis");
        alert.setContentText("""
            Defect Tracking System:
            
            📊 Total Defects: 47
            🔍 Most Common: Surface defects (65%)
            📈 Trend: Decreasing (-12% this month)
            🎯 Target: <5% defect rate
            
            Defect Categories:
            • Surface defects: 31 items
            • Dimensional issues: 12 items
            • Material defects: 4 items
            
            Detailed defect analysis dashboard coming soon.
            """);
        alert.showAndWait();
    }

    private void showQualityReport() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Quality Report");
        alert.setHeaderText("Quality Performance Analytics");
        alert.setContentText("""
            Quality Metrics Summary:
            
            📊 Overall Quality Score: 85.2/100
            ✅ Pass Rate: 80.8% (Target: 85%)
            🎯 Customer Satisfaction: 92%
            📈 Improvement Trend: +3.2% this quarter
            
            Key Performance Indicators:
            • First Pass Yield: 78.5%
            • Inspection Efficiency: 95.2%
            • Defect Cost Impact: $2,340
            • Process Capability: 1.33
            
            Comprehensive quality analytics dashboard available.
            """);
        alert.showAndWait();
    }

    private void showQualitySettings() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Quality Settings");
        alert.setHeaderText("Quality Control Configuration");
        alert.setContentText("""
            Quality System Settings:
            
            🔧 Inspection Standards: ISO 9001:2015
            📋 Quality Procedures: 24 active SOPs
            👥 Inspector Access: Level %s
            📊 Report Frequency: Daily
            
            Configuration Options:
            • Inspection templates
            • Defect classification
            • Approval workflows
            • Notification settings
            • Performance thresholds
            
            Settings panel requires administrator privileges.
            """.formatted(currentUser.getRole()));
        alert.showAndWait();
    }

    // Inner class for Quality Records
    public static class QualityRecord {
        private String inspectionId;
        private String productName;
        private String inspector;
        private String status;
        private String inspectionDate;
        private String notes;

        public QualityRecord(String inspectionId, String productName, String inspector, 
                           String status, String inspectionDate, String notes) {
            this.inspectionId = inspectionId;
            this.productName = productName;
            this.inspector = inspector;
            this.status = status;
            this.inspectionDate = inspectionDate;
            this.notes = notes;
        }

        // Getters
        public String getInspectionId() { return inspectionId; }
        public String getProductName() { return productName; }
        public String getInspector() { return inspector; }
        public String getStatus() { return status; }
        public String getInspectionDate() { return inspectionDate; }
        public String getNotes() { return notes; }

        // Setters
        public void setInspectionId(String inspectionId) { this.inspectionId = inspectionId; }
        public void setProductName(String productName) { this.productName = productName; }
        public void setInspector(String inspector) { this.inspector = inspector; }
        public void setStatus(String status) { this.status = status; }
        public void setInspectionDate(String inspectionDate) { this.inspectionDate = inspectionDate; }
        public void setNotes(String notes) { this.notes = notes; }
    }
}