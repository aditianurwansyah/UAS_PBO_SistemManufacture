package com.mycompany.manufacturing_system;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.*;

public class InventoryOperations {
    private Connection conn;

    public InventoryOperations() throws SQLException {
        // Replace with your database connection details
        String url = "jdbc:mysql://localhost:3307/manufacturing_system?useSSL=false";
        String user = "root";
        String password = "";
        conn = DriverManager.getConnection(url, user, password);
    }

    public static class InventoryItem {
        private final StringProperty itemCode = new SimpleStringProperty();
        private final StringProperty description = new SimpleStringProperty();
        private final StringProperty category = new SimpleStringProperty();
        private final IntegerProperty quantityOnHand = new SimpleIntegerProperty();
        private final DoubleProperty unitPrice = new SimpleDoubleProperty();
        private final StringProperty location = new SimpleStringProperty();
        private final StringProperty status = new SimpleStringProperty();
        private final IntegerProperty reorderPoint = new SimpleIntegerProperty();
        private final IntegerProperty minStockLevel = new SimpleIntegerProperty();

        public InventoryItem(String itemCode, String description, String category, int quantityOnHand,
                             double unitPrice, String location, String status, int reorderPoint, int minStockLevel) {
            this.itemCode.set(itemCode);
            this.description.set(description);
            this.category.set(category);
            this.quantityOnHand.set(quantityOnHand);
            this.unitPrice.set(unitPrice);
            this.location.set(location);
            this.status.set(status);
            this.reorderPoint.set(reorderPoint);
            this.minStockLevel.set(minStockLevel);
        }

        public StringProperty itemCodeProperty() { return itemCode; }
        public StringProperty descriptionProperty() { return description; }
        public StringProperty categoryProperty() { return category; }
        public IntegerProperty quantityOnHandProperty() { return quantityOnHand; }
        public DoubleProperty unitPriceProperty() { return unitPrice; }
        public StringProperty locationProperty() { return location; }
        public StringProperty statusProperty() { return status; }
        public IntegerProperty reorderPointProperty() { return reorderPoint; }
        public IntegerProperty minStockLevelProperty() { return minStockLevel; }

        public String getItemCode() { return itemCode.get(); }
        public String getDescription() { return description.get(); }
        public String getCategory() { return category.get(); }
        public int getQuantityOnHand() { return quantityOnHand.get(); }
        public double getUnitPrice() { return unitPrice.get(); }
        public String getLocation() { return location.get(); }
        public String getStatus() { return status.get(); }
        public int getReorderPoint() { return reorderPoint.get(); }
        public int getMinStockLevel() { return minStockLevel.get(); }

        public boolean isLowStock() { return getQuantityOnHand() <= getReorderPoint(); }
        public boolean isOutOfStock() { return getQuantityOnHand() == 0; }
    }

    public static class StockMovement {
        private final StringProperty movementId = new SimpleStringProperty();
        private final ObjectProperty<LocalDateTime> movementDate = new SimpleObjectProperty<>();
        private final StringProperty itemCode = new SimpleStringProperty();
        private final StringProperty movementType = new SimpleStringProperty();
        private final IntegerProperty quantity = new SimpleIntegerProperty();
        private final StringProperty reference = new SimpleStringProperty();
        private final StringProperty user = new SimpleStringProperty(); 

        public StockMovement(String movementId, LocalDateTime movementDate, String itemCode, String movementType,
                             int quantity, String reference, String user) {
            this.movementId.set(movementId);
            this.movementDate.set(movementDate);
            this.itemCode.set(itemCode);
            this.movementType.set(movementType);
            this.quantity.set(quantity);
            this.reference.set(reference);
            this.user.set(user);
        }

        public StringProperty movementIdProperty() { return movementId; }
        public ObjectProperty<LocalDateTime> movementDateProperty() { return movementDate; }
        public StringProperty itemCodeProperty() { return itemCode; }
        public StringProperty movementTypeProperty() { return movementType; }
        public IntegerProperty quantityProperty() { return quantity; }
        public StringProperty referenceProperty() { return reference; }
        public StringProperty userProperty() { return user; }
    }

    public static class InventoryStatistics {
        private final int totalItems;
        private final int lowStockItems;
        private final double totalValue;
        private final int reorderItems;

        public InventoryStatistics(int totalItems, int lowStockItems, double totalValue, int reorderItems) {
            this.totalItems = totalItems;
            this.lowStockItems = lowStockItems;
            this.totalValue = totalValue;
            this.reorderItems = reorderItems;
        }

        public int getTotalItems() { return totalItems; }
        public int getLowStockItems() { return lowStockItems; }
        public double getTotalValue() { return totalValue; }
        public int getReorderItems() { return reorderItems; }
    }

    public List<InventoryItem> getAllInventoryItems() throws SQLException {
        List<InventoryItem> items = new ArrayList<>();
        String query = "SELECT product_id, product_name, category, current_stock_level, unit_cost, location, " +
                       "status, minimum_stock_level, reorder_point FROM products";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                items.add(new InventoryItem(
                    rs.getString("product_id"),
                    rs.getString("product_name"),
                    rs.getString("category"),
                    rs.getInt("current_stock_level"),
                    rs.getDouble("unit_cost"),
                    rs.getString("location"),
                    rs.getString("status"),
                    rs.getInt("reorder_point"),
                    rs.getInt("minimum_stock_level")
                ));
            }
        }
        return items;
    }

    public List<InventoryItem> getInventoryItemsByCategory(String category) throws SQLException {
        List<InventoryItem> items = new ArrayList<>();
        String query = "SELECT product_id, product_name, category, current_stock_level, unit_cost, location, " +
                       "status, minimum_stock_level, reorder_point FROM products WHERE category = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, category);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(new InventoryItem(
                        rs.getString("product_id"),
                        rs.getString("product_name"),
                        rs.getString("category"),
                        rs.getInt("current_stock_level"),
                        rs.getDouble("unit_cost"),
                        rs.getString("location"),
                        rs.getString("status"),
                        rs.getInt("reorder_point"),
                        rs.getInt("minimum_stock_level")
                    ));
                }
            }
        }
        return items;
    }

    public boolean addItem(InventoryItem item) throws SQLException {
        String query = "INSERT INTO products (product_id, product_name, product_code, category, unit_cost, location, " +
                       "status, current_stock_level, minimum_stock_level, reorder_point) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, item.getItemCode());
            stmt.setString(2, item.getDescription());
            stmt.setString(3, item.getItemCode()); // Assuming product_code is same as product_id
            stmt.setString(4, item.getCategory());
            stmt.setDouble(5, item.getUnitPrice());
            stmt.setString(6, item.getLocation());
            stmt.setString(7, item.getStatus());
            stmt.setInt(8, item.getQuantityOnHand());
            stmt.setInt(9, item.getMinStockLevel());
            stmt.setInt(10, item.getReorderPoint());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateItem(String itemCode, String description, String category, double unitPrice,
                             String location, String status, int reorderPoint, int minStockLevel) throws SQLException {
        String query = "UPDATE products SET product_name = ?, category = ?, unit_cost = ?, location = ?, " +
                       "status = ?, minimum_stock_level = ?, reorder_point = ? WHERE product_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, description);
            stmt.setString(2, category);
            stmt.setDouble(3, unitPrice);
            stmt.setString(4, location);
            stmt.setString(5, status);
            stmt.setInt(6, minStockLevel);
            stmt.setInt(7, reorderPoint);
            stmt.setString(8, itemCode);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean recordStockMovement(String itemCode, String movementType, int quantity, String reference, String user) throws SQLException {
        // Update stock level
        String updateQuery = "UPDATE products SET current_stock_level = current_stock_level + ? WHERE product_id = ?";
        try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
            int delta = movementType.contains("ISSUE") || movementType.contains("TRANSFER_OUT") ? -quantity : quantity;
            updateStmt.setInt(1, delta);
            updateStmt.setString(2, itemCode);
            if (updateStmt.executeUpdate() == 0) return false;
        }

        // Record movement
        String insertQuery = "INSERT INTO stock_movements (item_code, movement_type, quantity, reference, user) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
            insertStmt.setString(1, itemCode);
            insertStmt.setString(2, movementType);
            insertStmt.setInt(3, quantity);
            insertStmt.setString(4, reference);
            insertStmt.setString(5, user);
            return insertStmt.executeUpdate() > 0;
        }
    }

    public List<StockMovement> getAllStockMovements() throws SQLException {
        List<StockMovement> movements = new ArrayList<>();
        String query = "SELECT movement_id, movement_date, item_code, movement_type, quantity, reference, user " +
                       "FROM stock_movements ORDER BY movement_date DESC";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                movements.add(new StockMovement(
                    rs.getString("movement_id"),
                    rs.getTimestamp("movement_date").toLocalDateTime(),
                    rs.getString("item_code"),
                    rs.getString("movement_type"),
                    rs.getInt("quantity"),
                    rs.getString("reference"),
                    rs.getString("user")
                ));
            }
        }
        return movements;
    }

    public InventoryStatistics getInventoryStatistics() throws SQLException {
        int totalItems = 0;
        int lowStockItems = 0;
        double totalValue = 0.0;
        int reorderItems = 0;

        String query = "SELECT current_stock_level, unit_cost, minimum_stock_level, reorder_point FROM products";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                totalItems++;
                int stock = rs.getInt("current_stock_level");
                double cost = rs.getDouble("unit_cost");
                int minStock = rs.getInt("minimum_stock_level");
                int reorder = rs.getInt("reorder_point");
                totalValue += stock * cost;
                if (stock <= reorder) lowStockItems++;
                if (stock < minStock) reorderItems++;
            }
        }
        return new InventoryStatistics(totalItems, lowStockItems, totalValue, reorderItems);
    }
} 