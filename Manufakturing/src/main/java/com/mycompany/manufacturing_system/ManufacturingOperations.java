package com.mycompany.manufacturing_system;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Core manufacturing operations class for database interactions
 */
public class ManufacturingOperations {
    private Connection connection;
    private static final DateTimeFormatter formatter = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ManufacturingOperations() throws SQLException {
        connection = DatabaseConnection.getConnection();
        initializeTables();
    }

    /**
     * Initialize database tables if they don't exist
     */
    private void initializeTables() {
        try {
             String createProductsTable = """
                CREATE TABLE IF NOT EXISTS products (
                    product_id VARCHAR(50) PRIMARY KEY,
                    product_name VARCHAR(100) NOT NULL,
                    category ENUM('AUTOMOTIVE', 'ELECTRONICS', 'FURNITURE') NOT NULL,
                    quantity INT NOT NULL DEFAULT 0,
                    unit_cost DECIMAL(10,2) NOT NULL,
                    status ENUM('PLANNING', 'IN_PRODUCTION', 'QUALITY_CHECK', 'COMPLETED', 'SHIPPED') DEFAULT 'PLANNING',
                    assigned_operator VARCHAR(50),
                    production_line VARCHAR(50),
                    start_time DATETIME,
                    estimated_completion DATETIME,
                    priority INT DEFAULT 3,
                    specifications TEXT,
                    completion_percentage DECIMAL(5,2) DEFAULT 0.00,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
                """;

            // Create production_orders table
            String createOrdersTable = """
                CREATE TABLE IF NOT EXISTS production_orders (
                    order_id VARCHAR(50) PRIMARY KEY,
                    customer_name VARCHAR(100) NOT NULL,
                    product_id VARCHAR(50) NOT NULL,
                    product_name VARCHAR(100) NOT NULL,
                    quantity INT NOT NULL,
                    status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'SHIPPED', 'CANCELLED', 'ON_HOLD') DEFAULT 'PENDING',
                    priority ENUM('URGENT', 'HIGH', 'MEDIUM', 'LOW') DEFAULT 'MEDIUM',
                    order_date DATETIME NOT NULL,
                    due_date DATETIME,
                    start_date DATETIME,
                    completion_date DATETIME,
                    assigned_line VARCHAR(50),
                    assigned_operator VARCHAR(50),
                    total_cost DECIMAL(12,2) DEFAULT 0.00,
                    completion_percentage DECIMAL(5,2) DEFAULT 0.00,
                    notes TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
                """;

            // Create production_reports table
            String createReportsTable = """
                CREATE TABLE IF NOT EXISTS production_reports (
                    report_id INT AUTO_INCREMENT PRIMARY KEY,
                    production_date DATE NOT NULL,
                    product_category VARCHAR(50) NOT NULL,
                    product_name VARCHAR(100) NOT NULL,
                    quantity_produced INT NOT NULL,
                    production_line VARCHAR(50),
                    operator_name VARCHAR(50),
                    shift ENUM('MORNING', 'AFTERNOON', 'NIGHT') DEFAULT 'MORNING',
                    quality_passed INT DEFAULT 0,
                    quality_failed INT DEFAULT 0,
                    downtime_minutes INT DEFAULT 0,
                    total_cost DECIMAL(12,2) DEFAULT 0.00,
                    efficiency_percentage DECIMAL(5,2) DEFAULT 0.00,
                    notes TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;

            try (Statement stmt = connection.createStatement()) {
                stmt.execute(createProductsTable);
                stmt.execute(createOrdersTable);
                stmt.execute(createReportsTable);
                System.out.println("Manufacturing database tables initialized successfully.");
            }

        } catch (SQLException e) {
            System.err.println("Failed to initialize database tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create a new production order
     */
    public boolean createProductionOrder(ProductionOrder order) {
        String query = """
            INSERT INTO production_orders 
            (order_id, customer_name, product_id, product_name, quantity, priority, 
             order_date, due_date, total_cost, notes) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
            
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, order.getOrderId());
            stmt.setString(2, order.getCustomerName());
            stmt.setString(3, order.getProductId());
            stmt.setString(4, order.getProductName());
            stmt.setInt(5, order.getQuantity());
            stmt.setString(6, order.getPriority());
            stmt.setString(7, order.getOrderDate().format(formatter));
            
            if (order.getDueDate() != null) {
                stmt.setString(8, order.getDueDate().format(formatter));
            } else {
                stmt.setNull(8, Types.TIMESTAMP);
            }
            
            stmt.setDouble(9, order.getTotalCost());
            stmt.setString(10, order.getNotes());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Start production for an order
     */
    public boolean startProduction(String orderId, String productionLine, String operator) {
        String query = """
            UPDATE production_orders SET 
            status = 'IN_PROGRESS', 
            start_date = ?, 
            assigned_line = ?, 
            assigned_operator = ? 
            WHERE order_id = ?
            """;
            
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, LocalDateTime.now().format(formatter));
            stmt.setString(2, productionLine);
            stmt.setString(3, operator);
            stmt.setString(4, orderId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update production progress
     */
    public boolean updateProductionProgress(String orderId, double completionPercentage, String notes) {
        String query = """
            UPDATE production_orders SET 
            completion_percentage = ?, 
            notes = ?,
            status = CASE 
                WHEN ? >= 100.0 THEN 'COMPLETED'
                WHEN ? > 0 THEN 'IN_PROGRESS'
                ELSE status
            END,
            completion_date = CASE WHEN ? >= 100.0 THEN ? ELSE completion_date END
            WHERE order_id = ?
            """;
            
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDouble(1, completionPercentage);
            stmt.setString(2, notes);
            stmt.setDouble(3, completionPercentage);
            stmt.setDouble(4, completionPercentage);
            stmt.setDouble(5, completionPercentage);
            stmt.setString(6, LocalDateTime.now().format(formatter));
            stmt.setString(7, orderId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all production orders
     */
    public List<ProductionOrder> getAllProductionOrders() {
        List<ProductionOrder> orders = new ArrayList<>();
        String query = "SELECT * FROM production_orders ORDER BY order_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                ProductionOrder order = new ProductionOrder(
                    rs.getString("order_id"),
                    rs.getString("customer_name"),
                    rs.getString("product_id"),
                    rs.getString("product_name"),
                    rs.getInt("quantity"),
                    rs.getString("priority")
                );
                
                order.setStatus(rs.getString("status"));
                order.setOrderDate(LocalDateTime.parse(rs.getString("order_date"), formatter));
                
                String dueDateStr = rs.getString("due_date");
                if (dueDateStr != null) {
                    order.setDueDate(LocalDateTime.parse(dueDateStr, formatter));
                }
                
                String startDateStr = rs.getString("start_date");
                if (startDateStr != null) {
                    order.setStartDate(LocalDateTime.parse(startDateStr, formatter));
                }
                
                String completionDateStr = rs.getString("completion_date");
                if (completionDateStr != null) {
                    order.setCompletionDate(LocalDateTime.parse(completionDateStr, formatter));
                }
                
                order.setAssignedLine(rs.getString("assigned_line"));
                order.setAssignedOperator(rs.getString("assigned_operator"));
                order.setTotalCost(rs.getDouble("total_cost"));
                order.setCompletionPercentage(rs.getDouble("completion_percentage"));
                order.setNotes(rs.getString("notes"));
                
                orders.add(order);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return orders;
    }

    /**
     * Get orders for a specific user
     */
    public List<ProductionOrder> getOrdersForUser(String username) {
        List<ProductionOrder> orders = new ArrayList<>();
        String query = """
            SELECT * FROM production_orders 
            WHERE customer_name = ? OR assigned_operator = ? 
            ORDER BY order_date DESC
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, username);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ProductionOrder order = new ProductionOrder(
                    rs.getString("order_id"),
                    rs.getString("customer_name"),
                    rs.getString("product_id"),
                    rs.getString("product_name"),
                    rs.getInt("quantity"),
                    rs.getString("priority")
                );
                
                // Set other properties...
                order.setStatus(rs.getString("status"));
                order.setTotalCost(rs.getDouble("total_cost"));
                order.setCompletionPercentage(rs.getDouble("completion_percentage"));
                
                orders.add(order);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return orders;
    }

    /**
     * Get production statistics
     */
    public ProductionStatistics getProductionStatistics() {
        ProductionStatistics stats = new ProductionStatistics();
        
        try (Statement stmt = connection.createStatement()) {
            // Total orders
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM production_orders");
            if (rs.next()) {
                stats.setTotalOrders(rs.getInt("total"));
            }
            
            // Orders by status
            rs = stmt.executeQuery("""
                SELECT status, COUNT(*) as count 
                FROM production_orders 
                GROUP BY status
                """);
            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("count");
                switch (status) {
                    case "PENDING": stats.setPendingOrders(count); break;
                    case "IN_PROGRESS": stats.setInProgressOrders(count); break;
                    case "COMPLETED": stats.setCompletedOrders(count); break;
                    case "SHIPPED": stats.setShippedOrders(count); break;
                }
            }
            
            // Today's production
            rs = stmt.executeQuery("""
                SELECT COUNT(*) as today_count 
                FROM production_orders 
                WHERE DATE(start_date) = CURDATE()
                """);
            if (rs.next()) {
                stats.setTodayProduction(rs.getInt("today_count"));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return stats;
    }

    /**
     * Get available production lines
     */
    public List<String> getAvailableProductionLines() {
        List<String> lines = new ArrayList<>();
        lines.add("Assembly Line A");
        lines.add("Assembly Line B");
        lines.add("Electronics Line 1");
        lines.add("Electronics Line 2");
        lines.add("Automotive Line 1");
        lines.add("Furniture Workshop");
        lines.add("Quality Control Station");
        return lines;
    }

    /**
     * Inner class for production statistics
     */
    public static class ProductionStatistics {
        private int totalOrders;
        private int pendingOrders;
        private int inProgressOrders;
        private int completedOrders;
        private int shippedOrders;
        private int todayProduction;
        
        // Getters and setters
        public int getTotalOrders() { return totalOrders; }
        public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }
        
        public int getPendingOrders() { return pendingOrders; }
        public void setPendingOrders(int pendingOrders) { this.pendingOrders = pendingOrders; }
        
        public int getInProgressOrders() { return inProgressOrders; }
        public void setInProgressOrders(int inProgressOrders) { this.inProgressOrders = inProgressOrders; }
        
        public int getCompletedOrders() { return completedOrders; }
        public void setCompletedOrders(int completedOrders) { this.completedOrders = completedOrders; }
        
        public int getShippedOrders() { return shippedOrders; }
        public void setShippedOrders(int shippedOrders) { this.shippedOrders = shippedOrders; }
        
        public int getTodayProduction() { return todayProduction; }
        public void setTodayProduction(int todayProduction) { this.todayProduction = todayProduction; }
    }

    private static class DatabaseConnection {

        private static Connection getConnection() {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        public DatabaseConnection() {
        }
    }
}