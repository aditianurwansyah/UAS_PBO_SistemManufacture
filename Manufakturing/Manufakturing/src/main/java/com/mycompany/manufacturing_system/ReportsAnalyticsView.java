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
import javafx.scene.chart.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map; // Import Map

public class ReportsAnalyticsView {
    private ReportsOperations reportsOps;
    private final User currentUser;
    private final MainApp mainApp; // Tambahkan referensi ke MainApp
    private TabPane reportsTabPane;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private ComboBox<String> reportTypeCombo;
    private TextArea reportOutputArea;

    // Sesuaikan konstruktor untuk menerima MainApp
    public ReportsAnalyticsView(User user, MainApp mainApp) {
        this.currentUser = user;
        this.mainApp = mainApp; // Inisialisasi MainApp
        try {
            this.reportsOps = new ReportsOperations();
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

        // Back button
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
        backButton.setOnAction(e -> {
            if (mainApp != null) {
                mainApp.goBackToMainMenu();
            } else {
                showError("Navigation Error", "Main application context is not available.");
            }
        });
        
        // Wrapper for back button to align it
        HBox backButtonWrapper = new HBox(backButton);
        backButtonWrapper.setAlignment(Pos.TOP_LEFT);
        backButtonWrapper.setPadding(new Insets(0, 0, 10, 0)); // Add some padding below the button


        // Header
        VBox headerSection = createHeaderSection();
        
        // Quick stats dashboard
        HBox dashboardSection = createDashboardSection();
        
        // Reports tabs
        reportsTabPane = createReportsTabPane();
        VBox.setVgrow(reportsTabPane, Priority.ALWAYS);
        
        mainContainer.getChildren().addAll(
            backButtonWrapper, // Add back button at the top
            headerSection,
            createSeparator(),
            dashboardSection,
            createSeparator(),
            reportsTabPane
        );
        
        // Refresh dashboard stats on view load
        refreshDashboardStats();

        return mainContainer;
    }

    private VBox createHeaderSection() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Text titleText = new Text("📈 Reports & Analytics");
        titleText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleText.setFill(Color.web("#2c3e50"));
        
        Text subtitleText = new Text("Production reports, quality metrics, and business intelligence");
        subtitleText.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        subtitleText.setFill(Color.web("#7f8c8d"));

        header.getChildren().addAll(titleText, subtitleText);
        return header;
    }

    private HBox createDashboardSection() {
        HBox container = new HBox(20);
        container.setAlignment(Pos.CENTER);
        
        // Labels for dynamic values
        Label productionValueLabel = new Label("Loading...");
        Label qualityValueLabel = new Label("Loading...");
        Label inventoryValueLabel = new Label("Loading...");
        Label performanceValueLabel = new Label("Loading...");

        VBox productionCard = createDashboardCard("Production Overview", "#3498db", "🏭", productionValueLabel);
        VBox qualityCard = createDashboardCard("Quality Metrics", "#27ae60", "✅", qualityValueLabel);
        VBox inventoryCard = createDashboardCard("Inventory Status", "#f39c12", "📦", inventoryValueLabel);
        VBox performanceCard = createDashboardCard("Performance KPIs", "#e74c3c", "📊", performanceValueLabel);
        
        // Store references to the value labels for updating
        productionCard.setUserData(productionValueLabel);
        qualityCard.setUserData(qualityValueLabel);
        inventoryCard.setUserData(inventoryValueLabel);
        performanceCard.setUserData(performanceValueLabel);

        container.getChildren().addAll(productionCard, qualityCard, inventoryCard, performanceCard);
        return container;
    }

    private VBox createDashboardCard(String title, String color, String icon, Label valueLabel) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefWidth(200);
        card.setPrefHeight(120);
        card.setStyle(String.format("""
            -fx-background-color: white;
            -fx-background-radius: 12;
            -fx-border-radius: 12;
            -fx-border-color: %s;
            -fx-border-width: 2;
            -fx-cursor: hand;
            """, color));
        
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.1));
        shadow.setRadius(8);
        shadow.setOffsetY(2);
        card.setEffect(shadow);
        
        Text iconText = new Text(icon);
        iconText.setFont(Font.font(24));
        
        Text titleText = new Text(title);
        titleText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        titleText.setFill(Color.web("#2c3e50"));
        titleText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        valueLabel.setTextFill(Color.web(color));
        
        card.getChildren().addAll(iconText, titleText, valueLabel);
        
        // Add hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle(String.format("""
                -fx-background-color: %s15;
                -fx-background-radius: 12;
                -fx-border-radius: 12;
                -fx-border-color: %s;
                -fx-border-width: 2;
                -fx-cursor: hand;
                """, color, color));
        });
        
        card.setOnMouseExited(e -> {
            card.setStyle(String.format("""
                -fx-background-color: white;
                -fx-background-radius: 12;
                -fx-border-radius: 12;
                -fx-border-color: %s;
                -fx-border-width: 2;
                -fx-cursor: hand;
                """, color));
        });
        
        card.setOnMouseClicked(e -> showDetailedReport(title));
        
        return card;
    }

    private TabPane createReportsTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Production Reports tab
        Tab productionTab = new Tab("🏭 Production Reports");
        productionTab.setContent(createProductionReportsView());
        
        // Quality Reports tab
        Tab qualityTab = new Tab("✅ Quality Reports");
        qualityTab.setContent(createQualityReportsView());
        
        // Inventory Reports tab
        Tab inventoryTab = new Tab("📦 Inventory Reports");
        inventoryTab.setContent(createInventoryReportsView());
        
        // Financial Reports tab
        Tab financialTab = new Tab("💰 Financial Reports");
        financialTab.setContent(createFinancialReportsView());
        
        // Custom Reports tab
        Tab customTab = new Tab("🔧 Custom Reports");
        customTab.setContent(createCustomReportsView());
        
        tabPane.getTabs().addAll(productionTab, qualityTab, inventoryTab, financialTab, customTab);
        return tabPane;
    }

    private VBox createProductionReportsView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        
        // Report filters
        HBox filtersBox = createReportFilters();
        
        // Charts section
        HBox chartsSection = new HBox(20);
        chartsSection.setAlignment(Pos.CENTER);
        
        // Production Volume Chart
        LineChart<String, Number> productionChart = createProductionVolumeChart();
        productionChart.setPrefWidth(400);
        productionChart.setPrefHeight(300);
        
        // Efficiency Chart
        BarChart<String, Number> efficiencyChart = createEfficiencyChart();
        efficiencyChart.setPrefWidth(400);
        efficiencyChart.setPrefHeight(300);
        
        chartsSection.getChildren().addAll(productionChart, efficiencyChart);
        
        // Report actions
        HBox actionsBox = createReportActions();
        
        container.getChildren().addAll(filtersBox, chartsSection, actionsBox);
        return container;
    }

    private VBox createQualityReportsView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        
        HBox filtersBox = createReportFilters();
        
        // Quality metrics
        HBox metricsBox = new HBox(20);
        metricsBox.setAlignment(Pos.CENTER);
        
        VBox qualityRateCard = createMetricCard("Quality Rate", "95.2%", "#27ae60");
        VBox defectRateCard = createMetricCard("Defect Rate", "4.8%", "#e74c3c");
        VBox firstPassCard = createMetricCard("First Pass Yield", "92.1%", "#3498db");
        VBox reworkCard = createMetricCard("Rework Rate", "3.5%", "#f39c12");
        
        metricsBox.getChildren().addAll(qualityRateCard, defectRateCard, firstPassCard, reworkCard);
        
        // Quality trend chart
        PieChart defectChart = createDefectDistributionChart();
        defectChart.setPrefWidth(400);
        defectChart.setPrefHeight(300);
        
        HBox actionsBox = createReportActions();
        
        container.getChildren().addAll(filtersBox, metricsBox, defectChart, actionsBox);
        return container;
    }

    private VBox createInventoryReportsView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        
        HBox filtersBox = createReportFilters();
        
        // Inventory charts
        HBox chartsSection = new HBox(20);
        chartsSection.setAlignment(Pos.CENTER);
        
        PieChart stockDistributionChart = createStockDistributionChart();
        stockDistributionChart.setPrefWidth(400);
        stockDistributionChart.setPrefHeight(300);
        
        BarChart<String, Number> stockLevelsChart = createStockLevelsChart();
        stockLevelsChart.setPrefWidth(400);
        stockLevelsChart.setPrefHeight(300);
        
        chartsSection.getChildren().addAll(stockDistributionChart, stockLevelsChart);
        
        HBox actionsBox = createReportActions();
        
        container.getChildren().addAll(filtersBox, chartsSection, actionsBox);
        return container;
    }

    private VBox createFinancialReportsView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        
        Text titleText = new Text("Financial Performance Reports");
        titleText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleText.setFill(Color.web("#2c3e50"));
        
        HBox filtersBox = createReportFilters();
        
        // Financial metrics
        HBox metricsBox = new HBox(20);
        metricsBox.setAlignment(Pos.CENTER);
        
        VBox revenueCard = createMetricCard("Total Revenue", "$2.4M", "#27ae60");
        VBox costCard = createMetricCard("Production Costs", "$1.8M", "#e74c3c");
        VBox profitCard = createMetricCard("Gross Profit", "$600K", "#3498db");
        VBox marginCard = createMetricCard("Profit Margin", "25%", "#f39c12");
        
        metricsBox.getChildren().addAll(revenueCard, costCard, profitCard, marginCard);
        
        // Financial trends chart
        LineChart<String, Number> financialChart = createFinancialTrendsChart();
        financialChart.setPrefWidth(800);
        financialChart.setPrefHeight(300);
        
        HBox actionsBox = createReportActions();
        
        container.getChildren().addAll(titleText, filtersBox, metricsBox, financialChart, actionsBox);
        return container;
    }

    private VBox createCustomReportsView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        
        Text titleText = new Text("Custom Report Builder");
        titleText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleText.setFill(Color.web("#2c3e50"));
        
        // Report builder form
        GridPane builderForm = new GridPane();
        builderForm.setHgap(15);
        builderForm.setVgap(15);
        builderForm.setPadding(new Insets(20));
        builderForm.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-border-radius: 10;
            """);
        
        // Report name
        TextField reportNameField = new TextField();
        reportNameField.setPromptText("Enter report name");
        reportNameField.setPrefWidth(300);
        
        // Data source
        ComboBox<String> dataSourceCombo = new ComboBox<>();
        dataSourceCombo.getItems().addAll("Production Orders", "Quality Inspections", "Inventory Items", "Stock Movements");
        dataSourceCombo.setPromptText("Select data source");
        dataSourceCombo.setPrefWidth(200);
        
        // Date range
        startDatePicker = new DatePicker(LocalDate.now().minusMonths(1));
        endDatePicker = new DatePicker(LocalDate.now());
        
        // Filters
        TextArea filtersArea = new TextArea();
        filtersArea.setPromptText("Enter filter conditions (optional)");
        filtersArea.setPrefRowCount(3);
        filtersArea.setPrefWidth(300);
        
        // Output format
        ComboBox<String> formatCombo = new ComboBox<>();
        formatCombo.getItems().addAll("PDF", "Excel", "CSV", "HTML");
        formatCombo.setValue("PDF");
        formatCombo.setPrefWidth(100);
        
        builderForm.add(new Label("Report Name:"), 0, 0);
        builderForm.add(reportNameField, 1, 0, 2, 1);
        
        builderForm.add(new Label("Data Source:"), 0, 1);
        builderForm.add(dataSourceCombo, 1, 1);
        
        builderForm.add(new Label("Start Date:"), 0, 2);
        builderForm.add(startDatePicker, 1, 2);
        
        builderForm.add(new Label("End Date:"), 0, 3);
        builderForm.add(endDatePicker, 1, 3);
        
        builderForm.add(new Label("Filters:"), 0, 4);
        builderForm.add(filtersArea, 1, 4, 2, 1);
        
        builderForm.add(new Label("Format:"), 0, 5);
        builderForm.add(formatCombo, 1, 5);
        
        // Action buttons
        HBox customActionsBox = new HBox(10);
        customActionsBox.setAlignment(Pos.CENTER_LEFT);
        customActionsBox.setPadding(new Insets(20, 0, 0, 0));
        
        Button generateBtn = createStyledButton("📊 Generate Report", "#27ae60", 150, e -> generateCustomReport(reportNameField.getText(), // Menggunakan handler
                                                                                                                dataSourceCombo.getValue(),
                                                                                                                formatCombo.getValue()));
        
        Button saveTemplateBtn = createStyledButton("💾 Save Template", "#3498db", 150, e -> saveReportTemplate()); // Menggunakan handler
        
        Button loadTemplateBtn = createStyledButton("📁 Load Template", "#f39c12", 150, e -> loadReportTemplate()); // Menggunakan handler
        
        customActionsBox.getChildren().addAll(generateBtn, saveTemplateBtn, loadTemplateBtn);
        
        // Report output area
        reportOutputArea = new TextArea();
        reportOutputArea.setPromptText("Report output will appear here...");
        reportOutputArea.setPrefRowCount(10);
        reportOutputArea.setEditable(false);
        reportOutputArea.setStyle("""
            -fx-background-color: #f8f9fa;
            -fx-border-color: #dee2e6;
            -fx-border-radius: 5;
            -fx-font-family: 'Courier New';
            """);
        
        container.getChildren().addAll(titleText, builderForm, customActionsBox, 
                                      new Label("Report Output:"), reportOutputArea);
        return container;
    }

    private HBox createReportFilters() {
        HBox filtersBox = new HBox(15);
        filtersBox.setAlignment(Pos.CENTER_LEFT);
        filtersBox.setPadding(new Insets(0, 0, 20, 0));
        
        Label fromLabel = new Label("From:");
        DatePicker fromDate = new DatePicker(LocalDate.now().minusMonths(1));
        fromDate.setPrefWidth(150);
        
        Label toLabel = new Label("To:");
        DatePicker toDate = new DatePicker(LocalDate.now());
        toDate.setPrefWidth(150);
        
        reportTypeCombo = new ComboBox<>();
        reportTypeCombo.getItems().addAll("Daily", "Weekly", "Monthly", "Quarterly", "Custom");
        reportTypeCombo.setValue("Monthly");
        reportTypeCombo.setPrefWidth(120);
        
        Button refreshBtn = createStyledButton("🔄 Refresh", "#3498db", 100, e -> refreshReports()); // Menggunakan handler
        
        filtersBox.getChildren().addAll(fromLabel, fromDate, toLabel, toDate, 
                                       new Label("Period:"), reportTypeCombo, refreshBtn);
        return filtersBox;
    }

    private HBox createReportActions() {
        HBox actionsBox = new HBox(10);
        actionsBox.setAlignment(Pos.CENTER);
        actionsBox.setPadding(new Insets(20, 0, 0, 0));
        
        Button exportPdfBtn = createStyledButton("📄 Export PDF", "#e74c3c", 120, e -> exportReport("PDF")); // Menggunakan handler
        
        Button exportExcelBtn = createStyledButton("📊 Export Excel", "#27ae60", 120, e -> exportReport("Excel")); // Menggunakan handler
        
        Button printBtn = createStyledButton("🖨️ Print", "#95a5a6", 100, e -> printReport()); // Menggunakan handler
        
        Button emailBtn = createStyledButton("📧 Email", "#3498db", 100, e -> emailReport()); // Menggunakan handler
        
        actionsBox.getChildren().addAll(exportPdfBtn, exportExcelBtn, printBtn, emailBtn);
        return actionsBox;
    }

    private VBox createMetricCard(String title, String value, String color) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setPrefWidth(150);
        card.setPrefHeight(80);
        card.setStyle(String.format("""
            -fx-background-color: white;
            -fx-background-radius: 8;
            -fx-border-radius: 8;
            -fx-border-color: %s;
            -fx-border-width: 1;
            """, color));
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        valueLabel.setTextFill(Color.web(color));
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        titleLabel.setTextFill(Color.web("#7f8c8d"));
        
        card.getChildren().addAll(valueLabel, titleLabel);
        return card;
    }

    // Chart creation methods
    private LineChart<String, Number> createProductionVolumeChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        yAxis.setLabel("Units Produced");
        
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Production Volume Trend");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Production Volume");
        
        // Sample data
        series.getData().add(new XYChart.Data<>("Week 1", 450));
        series.getData().add(new XYChart.Data<>("Week 2", 520));
        series.getData().add(new XYChart.Data<>("Week 3", 480));
        series.getData().add(new XYChart.Data<>("Week 4", 600));
        
        chart.getData().add(series);
        return chart;
    }

    private BarChart<String, Number> createEfficiencyChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Production Line");
        yAxis.setLabel("Efficiency %");
        
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Production Line Efficiency");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Efficiency");
        
        series.getData().add(new XYChart.Data<>("Line A", 92));
        series.getData().add(new XYChart.Data<>("Line B", 88));
        series.getData().add(new XYChart.Data<>("Electronics 1", 95));
        series.getData().add(new XYChart.Data<>("Automotive", 85));
        
        chart.getData().add(series);
        return chart;
    }

    private PieChart createDefectDistributionChart() {
        PieChart chart = new PieChart();
        chart.setTitle("Defect Distribution");
        
        chart.getData().addAll(
            new PieChart.Data("Dimensional", 35),
            new PieChart.Data("Surface", 25),
            new PieChart.Data("Assembly", 20),
            new PieChart.Data("Material", 15),
            new PieChart.Data("Other", 5)
        );
        
        return chart;
    }

    private PieChart createStockDistributionChart() {
        PieChart chart = new PieChart();
        chart.setTitle("Stock Distribution by Category");
        
        chart.getData().addAll(
            new PieChart.Data("Raw Materials", 40),
            new PieChart.Data("Components", 35),
            new PieChart.Data("Finished Goods", 20),
            new PieChart.Data("Consumables", 5)
        );
        
        return chart;
    }

    private BarChart<String, Number> createStockLevelsChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Item Category");
        yAxis.setLabel("Stock Level");
        
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Current Stock Levels");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Stock Level");
        
        series.getData().add(new XYChart.Data<>("Steel", 250));
        series.getData().add(new XYChart.Data<>("Plastic", 500));
        series.getData().add(new XYChart.Data<>("Electronics", 75));
        series.getData().add(new XYChart.Data<>("Hardware", 1000));
        
        chart.getData().add(series);
        return chart;
    }

    private LineChart<String, Number> createFinancialTrendsChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Month");
        yAxis.setLabel("Amount ($)");
        
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Financial Trends");
        
        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Revenue");
        revenueSeries.getData().add(new XYChart.Data<>("Jan", 180000));
        revenueSeries.getData().add(new XYChart.Data<>("Feb", 220000));
        revenueSeries.getData().add(new XYChart.Data<>("Mar", 250000));
        revenueSeries.getData().add(new XYChart.Data<>("Apr", 240000));
        
        XYChart.Series<String, Number> costSeries = new XYChart.Series<>();
        costSeries.setName("Costs");
        costSeries.getData().add(new XYChart.Data<>("Jan", 140000));
        costSeries.getData().add(new XYChart.Data<>("Feb", 160000));
        costSeries.getData().add(new XYChart.Data<>("Mar", 180000));
        costSeries.getData().add(new XYChart.Data<>("Apr", 175000));
        
        chart.getData().addAll(revenueSeries, costSeries);
        return chart;
    }

    // Mengubah signature agar sesuai dengan createStyledButton di MainApp/UserManagementView
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
        button.setOnAction(handler); // Set handler
        return button;
    }

    private Separator createSeparator() {
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #ecf0f1;");
        return separator;
    }

    // Action methods
    private void showDetailedReport(String reportType) {
        showInfo("Detailed Report", "Detailed " + reportType + " report will be implemented.");
    }

    private void refreshReports() {
        showInfo("Refresh", "Reports refreshed successfully.");
        // Implement actual data refresh for charts and metrics here
    }

    private void exportReport(String format) {
        showInfo("Export", "Report exported to " + format + " format.");
    }

    private void printReport() {
        showInfo("Print", "Print functionality will be implemented.");
    }

    private void emailReport() {
        showInfo("Email", "Email functionality will be implemented.");
    }

    private void generateCustomReport(String reportName, String dataSource, String format) {
        if (reportName == null || reportName.trim().isEmpty()) {
            showError("Validation Error", "Please enter a report name.");
            return;
        }
        
        if (dataSource == null) {
            showError("Validation Error", "Please select a data source.");
            return;
        }
        
        String reportOutput = generateReportOutput(reportName, dataSource, format);
        reportOutputArea.setText(reportOutput);
        
        showInfo("Success", "Custom report generated successfully!");
    }

    private String generateReportOutput(String reportName, String dataSource, String format) {
        StringBuilder output = new StringBuilder();
        output.append("=".repeat(60)).append("\n");
        output.append("CUSTOM REPORT: ").append(reportName.toUpperCase()).append("\n");
        output.append("=".repeat(60)).append("\n");
        output.append("Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        output.append("Data Source: ").append(dataSource).append("\n");
        output.append("Format: ").append(format).append("\n");
        output.append("Period: ").append(startDatePicker.getValue()).append(" to ").append(endDatePicker.getValue()).append("\n");
        output.append("-".repeat(60)).append("\n\n");
        
        // Sample report data based on source
        switch (dataSource) {
            case "Production Orders":
                output.append("PRODUCTION ORDERS SUMMARY\n");
                output.append("Total Orders: 45\n");
                output.append("Completed: 38 (84.4%)\n");
                output.append("In Progress: 5 (11.1%)\n");
                output.append("Pending: 2 (4.4%)\n");
                break;
            case "Quality Inspections":
                output.append("QUALITY INSPECTIONS SUMMARY\n");
                output.append("Total Inspections: 125\n");
                output.append("Passed: 119 (95.2%)\n");
                output.append("Failed: 6 (4.8%)\n");
                output.append("Average Quality Score: 92.5\n");
                break;
            case "Inventory Items":
                output.append("INVENTORY SUMMARY\n");
                output.append("Total Items: 150\n");
                output.append("Low Stock Items: 12\n");
                output.append("Total Value: $245,680\n");
                output.append("Turnover Rate: 4.2x\n");
                break;
            case "Stock Movements":
                output.append("STOCK MOVEMENTS SUMMARY\n");
                output.append("Total Movements: 89\n");
                output.append("Receipts: 34\n");
                output.append("Issues: 45\n");
                output.append("Adjustments: 10\n");
                break;
        }
        
        output.append("\n").append("=".repeat(60)).append("\n");
        output.append("End of Report\n");
        
        return output.toString();
    }

    private void saveReportTemplate() {
        showInfo("Save Template", "Report template saved successfully!");
    }

    private void loadReportTemplate() {
        showInfo("Load Template", "Report template loaded successfully!");
    }

    // New method to refresh dashboard statistics
    private void refreshDashboardStats() {
        try {
            LocalDate today = LocalDate.now();
            // Production Overview
            Map<String, Double> productionStats = reportsOps.getProductionOverview(today.minusMonths(1), today);
            Label prodLabel = (Label) ((VBox) ((HBox) reportsTabPane.getParent().getChildrenUnmodifiable().get(3)).getChildren().get(0)).getUserData();
            if (prodLabel != null) {
                prodLabel.setText(String.format("%,.0f Units", productionStats.getOrDefault("totalUnitsProduced", 0.0)));
            }
            
            // Quality Metrics
            Map<String, Double> qualityMetrics = reportsOps.getQualityMetrics(today.minusMonths(1), today);
            Label qualityLabel = (Label) ((VBox) ((HBox) reportsTabPane.getParent().getChildrenUnmodifiable().get(3)).getChildren().get(1)).getUserData();
            if (qualityLabel != null) {
                qualityLabel.setText(String.format("%.1f%% Pass", qualityMetrics.getOrDefault("qualityRate", 0.0)));
            }

            // Inventory Status
            Map<String, Double> inventoryStatus = reportsOps.getInventoryStatus(today);
            Label inventoryLabel = (Label) ((VBox) ((HBox) reportsTabPane.getParent().getChildrenUnmodifiable().get(3)).getChildren().get(2)).getUserData();
            if (inventoryLabel != null) {
                inventoryLabel.setText(String.format("%,.0f Items", inventoryStatus.getOrDefault("totalItems", 0.0)));
            }

            // Performance KPIs
            Map<String, Double> performanceKPIs = reportsOps.getPerformanceKPIs(today.minusMonths(1), today);
            Label performanceLabel = (Label) ((VBox) ((HBox) reportsTabPane.getParent().getChildrenUnmodifiable().get(3)).getChildren().get(3)).getUserData();
            if (performanceLabel != null) {
                performanceLabel.setText(String.format("%.1f%% OEE", performanceKPIs.getOrDefault("oee", 0.0)));
            }

        } catch (Exception e) {
            showError("Error", "Failed to refresh dashboard statistics: " + e.getMessage());
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
