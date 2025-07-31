package com.mycompany.manufacturing_system;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reports operations for database interactions and report generation
 */
public class ReportsOperations {
    private Connection connection;
    private static final DateTimeFormatter formatter = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter dateFormatter = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ReportsOperations() throws SQLException {
        connection = DatabaseConnection.getConnection();
    }

    /**
     * Get production summary for a date range
     */
    public ProductionSummary getProductionSummary(LocalDate startDate, LocalDate endDate) {
        ProductionSummary summary = new ProductionSummary();
        
        try (PreparedStatement stmt = connection.prepareStatement("""
            SELECT 
                COUNT(*) as total_orders,
                SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completed_orders,
                SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as in_progress_orders,
                SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) as pending_orders,
                SUM(quantity) as total_quantity,
                SUM(total_cost) as total_value,
                AVG(completion_percentage) as avg_completion
            FROM production_orders 
            WHERE DATE(order_date) BETWEEN ? AND ?
            """)) {
            
            stmt.setString(1, startDate.format(dateFormatter));
            stmt.setString(2, endDate.format(dateFormatter));
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                summary.setTotalOrders(rs.getInt("total_orders"));
                summary.setCompletedOrders(rs.getInt("completed_orders"));
                summary.setInProgressOrders(rs.getInt("in_progress_orders"));
                summary.setPendingOrders(rs.getInt("pending_orders"));
                summary.setTotalQuantity(rs.getInt("total_quantity"));
                summary.setTotalValue(rs.getDouble("total_value"));
                summary.setAverageCompletion(rs.getDouble("avg_completion"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting production summary: " + e.getMessage());
            e.printStackTrace();
        }
        
        return summary;
    }

    /**
     * Get quality summary for a date range
     */
    public QualitySummary getQualitySummary(LocalDate startDate, LocalDate endDate) {
        QualitySummary summary = new QualitySummary();
        
        try (PreparedStatement stmt = connection.prepareStatement("""
            SELECT 
                COUNT(*) as total_inspections,
                SUM(CASE WHEN status = 'PASSED' THEN 1 ELSE 0 END) as passed_inspections,
                SUM(CASE WHEN status = 'FAILED' THEN 1 ELSE 0 END) as failed_inspections,
                SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) as pending_inspections,
                SUM(defect_count) as total_defects,
                AVG(quality_score) as avg_quality_score
            FROM quality_inspections 
            WHERE DATE(inspection_date) BETWEEN ? AND ?
            """)) {
            
            stmt.setString(1, startDate.format(dateFormatter));
            stmt.setString(2, endDate.format(dateFormatter));
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                summary.setTotalInspections(rs.getInt("total_inspections"));
                summary.setPassedInspections(rs.getInt("passed_inspections"));
                summary.setFailedInspections(rs.getInt("failed_inspections"));
                summary.setPendingInspections(rs.getInt("pending_inspections"));
                summary.setTotalDefects(rs.getInt("total_defects"));
                summary.setAverageQualityScore(rs.getDouble("avg_quality_score"));
                summary.calculateRates();
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting quality summary: " + e.getMessage());
            e.printStackTrace();
        }
        
        return summary;
    }

    /**
     * Get inventory summary
     */
    public InventorySummary getInventorySummary() {
        InventorySummary summary = new InventorySummary();
        
        try (Statement stmt = connection.createStatement()) {
            // Total inventory value
            ResultSet rs = stmt.executeQuery("""
                SELECT 
                    COUNT(*) as total_items,
                    SUM(quantity_on_hand * unit_price) as total_value,
                    SUM(CASE WHEN quantity_on_hand <= minimum_stock_level THEN 1 ELSE 0 END) as low_stock_items,
                    SUM(CASE WHEN quantity_on_hand = 0 THEN 1 ELSE 0 END) as out_of_stock_items,
                    SUM(CASE WHEN quantity_on_hand <= reorder_point THEN 1 ELSE 0 END) as reorder_items
                FROM inventory_items 
                WHERE status = 'ACTIVE'
                """);
            
            if (rs.next()) {
                summary.setTotalItems(rs.getInt("total_items"));
                summary.setTotalValue(rs.getDouble("total_value"));
                summary.setLowStockItems(rs.getInt("low_stock_items"));
                summary.setOutOfStockItems(rs.getInt("out_of_stock_items"));
                summary.setReorderItems(rs.getInt("reorder_items"));
            }
            
            // Category breakdown
            rs = stmt.executeQuery("""
                SELECT 
                    category,
                    COUNT(*) as item_count,
                    SUM(quantity_on_hand * unit_price) as category_value
                FROM inventory_items 
                WHERE status = 'ACTIVE'
                GROUP BY category
                """);
            
            Map<String, Integer> categoryCount = new HashMap<>();
            Map<String, Double> categoryValue = new HashMap<>();
            
            while (rs.next()) {
                categoryCount.put(rs.getString("category"), rs.getInt("item_count"));
                categoryValue.put(rs.getString("category"), rs.getDouble("category_value"));
            }
            
            summary.setCategoryCount(categoryCount);
            summary.setCategoryValue(categoryValue);
            
        } catch (SQLException e) {
            System.err.println("Error getting inventory summary: " + e.getMessage());
            e.printStackTrace();
        }
        
        return summary;
    }

    /**
     * Get financial summary for a date range
     */
    public FinancialSummary getFinancialSummary(LocalDate startDate, LocalDate endDate) {
        FinancialSummary summary = new FinancialSummary();
        
        try (PreparedStatement stmt = connection.prepareStatement("""
            SELECT 
                SUM(total_cost) as total_revenue,
                COUNT(*) as total_orders,
                AVG(total_cost) as avg_order_value,
                SUM(CASE WHEN status = 'COMPLETED' THEN total_cost ELSE 0 END) as completed_revenue
            FROM production_orders 
            WHERE DATE(order_date) BETWEEN ? AND ?
            """)) {
            
            stmt.setString(1, startDate.format(dateFormatter));
            stmt.setString(2, endDate.format(dateFormatter));
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                summary.setTotalRevenue(rs.getDouble("total_revenue"));
                summary.setTotalOrders(rs.getInt("total_orders"));
                summary.setAverageOrderValue(rs.getDouble("avg_order_value"));
                summary.setCompletedRevenue(rs.getDouble("completed_revenue"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting financial summary: " + e.getMessage());
            e.printStackTrace();
        }
        
        return summary;
    }

    /**
     * Get production trend data for charts
     */
    public List<TrendData> getProductionTrend(LocalDate startDate, LocalDate endDate, String period) {
        List<TrendData> trendData = new ArrayList<>();
        String groupBy = getGroupByClause(period);
        
        try (PreparedStatement stmt = connection.prepareStatement(String.format("""
            SELECT 
                %s as period,
                COUNT(*) as order_count,
                SUM(quantity) as total_quantity,
                SUM(total_cost) as total_value,
                AVG(completion_percentage) as avg_completion
            FROM production_orders 
            WHERE DATE(order_date) BETWEEN ? AND ?
            GROUP BY %s
            ORDER BY period
            """, groupBy, groupBy))) {
            
            stmt.setString(1, startDate.format(dateFormatter));
            stmt.setString(2, endDate.format(dateFormatter));
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                TrendData data = new TrendData();
                data.setPeriod(rs.getString("period"));
                data.setOrderCount(rs.getInt("order_count"));
                data.setTotalQuantity(rs.getInt("total_quantity"));
                data.setTotalValue(rs.getDouble("total_value"));
                data.setAverageCompletion(rs.getDouble("avg_completion"));
                
                trendData.add(data);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting production trend: " + e.getMessage());
            e.printStackTrace();
        }
        
        return trendData;
    }

    /**
     * Get efficiency data by production line
     */
    public List<EfficiencyData> getProductionEfficiency(LocalDate startDate, LocalDate endDate) {
        List<EfficiencyData> efficiencyData = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement("""
            SELECT 
                assigned_line,
                COUNT(*) as total_orders,
                SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completed_orders,
                AVG(completion_percentage) as avg_completion,
                SUM(quantity) as total_quantity
            FROM production_orders 
            WHERE DATE(order_date) BETWEEN ? AND ? 
            AND assigned_line IS NOT NULL
            GROUP BY assigned_line
            ORDER BY assigned_line
            """)) {
            
            stmt.setString(1, startDate.format(dateFormatter));
            stmt.setString(2, endDate.format(dateFormatter));
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                EfficiencyData data = new EfficiencyData();
                data.setProductionLine(rs.getString("assigned_line"));
                data.setTotalOrders(rs.getInt("total_orders"));
                data.setCompletedOrders(rs.getInt("completed_orders"));
                data.setAverageCompletion(rs.getDouble("avg_completion"));
                data.setTotalQuantity(rs.getInt("total_quantity"));
                data.calculateEfficiency();
                
                efficiencyData.add(data);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting production efficiency: " + e.getMessage());
            e.printStackTrace();
        }
        
        return efficiencyData;
    }

    /**
     * Get defect distribution data for quality charts
     */
    public Map<String, Integer> getDefectDistribution(LocalDate startDate, LocalDate endDate) {
        Map<String, Integer> defectData = new HashMap<>();
        
        try (PreparedStatement stmt = connection.prepareStatement("""
            SELECT 
                defect_types,
                COUNT(*) as defect_count
            FROM quality_inspections 
            WHERE DATE(inspection_date) BETWEEN ? AND ? 
            AND status = 'FAILED'
            AND defect_types IS NOT NULL AND defect_types != ''
            """)) {
            
            stmt.setString(1, startDate.format(dateFormatter));
            stmt.setString(2, endDate.format(dateFormatter));
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String defectTypes = rs.getString("defect_types");
                int count = rs.getInt("defect_count");
                
                // Parse multiple defect types if semicolon separated
                String[] types = defectTypes.split(";");
                for (String type : types) {
                    type = type.trim();
                    if (!type.isEmpty()) {
                        defectData.put(type, defectData.getOrDefault(type, 0) + count);
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting defect distribution: " + e.getMessage());
            e.printStackTrace();
        }
        
        return defectData;
    }

    /**
     * Generate custom report based on SQL query
     */
    public List<Map<String, Object>> executeCustomQuery(String query) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            ResultSetMetaData metadata = rs.getMetaData();
            int columnCount = metadata.getColumnCount();
            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metadata.getColumnName(i);
                    Object value = rs.getObject(i);
                    row.put(columnName, value);
                }
                results.add(row);
            }
            
        } catch (SQLException e) {
            System.err.println("Error executing custom query: " + e.getMessage());
            e.printStackTrace();
        }
        
        return results;
    }

    private String getGroupByClause(String period) {
        switch (period.toUpperCase()) {
            case "DAILY":
                return "DATE(order_date)";
            case "WEEKLY":
                return "YEAR(order_date), WEEK(order_date)";
            case "MONTHLY":
                return "YEAR(order_date), MONTH(order_date)";
            case "QUARTERLY":
                return "YEAR(order_date), QUARTER(order_date)";
            default:
                return "DATE(order_date)";
        }
    }
}

/**
 * Production Summary data class
 */
class ProductionSummary {
    private int totalOrders;
    private int completedOrders;
    private int inProgressOrders;
    private int pendingOrders;
    private int totalQuantity;
    private double totalValue;
    private double averageCompletion;

    // Getters and Setters
    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

    public int getCompletedOrders() { return completedOrders; }
    public void setCompletedOrders(int completedOrders) { this.completedOrders = completedOrders; }

    public int getInProgressOrders() { return inProgressOrders; }
    public void setInProgressOrders(int inProgressOrders) { this.inProgressOrders = inProgressOrders; }

    public int getPendingOrders() { return pendingOrders; }
    public void setPendingOrders(int pendingOrders) { this.pendingOrders = pendingOrders; }

    public int getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; }

    public double getTotalValue() { return totalValue; }
    public void setTotalValue(double totalValue) { this.totalValue = totalValue; }

    public double getAverageCompletion() { return averageCompletion; }
    public void setAverageCompletion(double averageCompletion) { this.averageCompletion = averageCompletion; }

    public double getCompletionRate() {
        return totalOrders > 0 ? (double) completedOrders / totalOrders * 100 : 0;
    }
}

/**
 * Quality Summary data class
 */
class QualitySummary {
    private int totalInspections;
    private int passedInspections;
    private int failedInspections;
    private int pendingInspections;
    private int totalDefects;
    private double averageQualityScore;
    private double passRate;
    private double defectRate;

    public void calculateRates() {
        if (totalInspections > 0) {
            passRate = (double) passedInspections / totalInspections * 100;
            defectRate = (double) failedInspections / totalInspections * 100;
        }
    }

    // Getters and Setters
    public int getTotalInspections() { return totalInspections; }
    public void setTotalInspections(int totalInspections) { this.totalInspections = totalInspections; }

    public int getPassedInspections() { return passedInspections; }
    public void setPassedInspections(int passedInspections) { this.passedInspections = passedInspections; }

    public int getFailedInspections() { return failedInspections; }
    public void setFailedInspections(int failedInspections) { this.failedInspections = failedInspections; }

    public int getPendingInspections() { return pendingInspections; }
    public void setPendingInspections(int pendingInspections) { this.pendingInspections = pendingInspections; }

    public int getTotalDefects() { return totalDefects; }
    public void setTotalDefects(int totalDefects) { this.totalDefects = totalDefects; }

    public double getAverageQualityScore() { return averageQualityScore; }
    public void setAverageQualityScore(double averageQualityScore) { this.averageQualityScore = averageQualityScore; }

    public double getPassRate() { return passRate; }
    public double getDefectRate() { return defectRate; }
}

/**
 * Inventory Summary data class
 */
class InventorySummary {
    private int totalItems;
    private double totalValue;
    private int lowStockItems;
    private int outOfStockItems;
    private int reorderItems;
    private Map<String, Integer> categoryCount;
    private Map<String, Double> categoryValue;

    // Getters and Setters
    public int getTotalItems() { return totalItems; }
    public void setTotalItems(int totalItems) { this.totalItems = totalItems; }

    public double getTotalValue() { return totalValue; }
    public void setTotalValue(double totalValue) { this.totalValue = totalValue; }

    public int getLowStockItems() { return lowStockItems; }
    public void setLowStockItems(int lowStockItems) { this.lowStockItems = lowStockItems; }

    public int getOutOfStockItems() { return outOfStockItems; }
    public void setOutOfStockItems(int outOfStockItems) { this.outOfStockItems = outOfStockItems; }

    public int getReorderItems() { return reorderItems; }
    public void setReorderItems(int reorderItems) { this.reorderItems = reorderItems; }

    public Map<String, Integer> getCategoryCount() { return categoryCount; }
    public void setCategoryCount(Map<String, Integer> categoryCount) { this.categoryCount = categoryCount; }

    public Map<String, Double> getCategoryValue() { return categoryValue; }
    public void setCategoryValue(Map<String, Double> categoryValue) { this.categoryValue = categoryValue; }
}

/**
 * Financial Summary data class
 */
class FinancialSummary {
    private double totalRevenue;
    private double completedRevenue;
    private int totalOrders;
    private double averageOrderValue;

    // Getters and Setters
    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

    public double getCompletedRevenue() { return completedRevenue; }
    public void setCompletedRevenue(double completedRevenue) { this.completedRevenue = completedRevenue; }

    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

    public double getAverageOrderValue() { return averageOrderValue; }
    public void setAverageOrderValue(double averageOrderValue) { this.averageOrderValue = averageOrderValue; }
}

/**
 * Trend Data class for charts
 */
class TrendData {
    private String period;
    private int orderCount;
    private int totalQuantity;
    private double totalValue;
    private double averageCompletion;

    // Getters and Setters
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public int getOrderCount() { return orderCount; }
    public void setOrderCount(int orderCount) { this.orderCount = orderCount; }

    public int getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; }

    public double getTotalValue() { return totalValue; }
    public void setTotalValue(double totalValue) { this.totalValue = totalValue; }

    public double getAverageCompletion() { return averageCompletion; }
    public void setAverageCompletion(double averageCompletion) { this.averageCompletion = averageCompletion; }
}

/**
 * Efficiency Data class
 */
class EfficiencyData {
    private String productionLine;
    private int totalOrders;
    private int completedOrders;
    private double averageCompletion;
    private int totalQuantity;
    private double efficiency;

    public void calculateEfficiency() {
        if (totalOrders > 0) {
            efficiency = (double) completedOrders / totalOrders * 100;
        }
    }

    // Getters and Setters
    public String getProductionLine() { return productionLine; }
    public void setProductionLine(String productionLine) { this.productionLine = productionLine; }

    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

    public int getCompletedOrders() { return completedOrders; }
    public void setCompletedOrders(int completedOrders) { this.completedOrders = completedOrders; }

    public double getAverageCompletion() { return averageCompletion; }
    public void setAverageCompletion(double averageCompletion) { this.averageCompletion = averageCompletion; }

    public int getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; }

    public double getEfficiency() { return efficiency; }
}