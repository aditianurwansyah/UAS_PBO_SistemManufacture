package com.mycompany.manufacturing_system;

import javafx.beans.property.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Inventory Operations for database interactions
 */
public class InventoryOperations {
    private Connection connection;
    private static final DateTimeFormatter formatter = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public InventoryOperations() throws SQLException {
        connection = DatabaseConnection.getConnection();
        initializeInventoryTables();
    }

    private void initializeInventoryTables() {
        try (Statement stmt = connection.createStatement()) {
            
            // Create inventory_items table
            String createInventoryTable = """
                CREATE TABLE IF NOT EXISTS inventory_items (
                    item_id INT AUTO_INCREMENT PRIMARY KEY,
                    item_code VARCHAR(50) NOT NULL UNIQUE,
                    description VARCHAR(200) NOT NULL,
                    category ENUM('RAW_MATERIAL', 'COMPONENT', 'FINISHED_GOOD', 'CONSUMABLE') NOT NULL,
                    unit_of_measure VARCHAR(20) DEFAULT 'PIECES',
                    unit_price DECIMAL(10,2) DEFAULT 0.00,
                    quantity_on_hand INT DEFAULT 0,
                    minimum_stock_level INT DEFAULT 0,
                    maximum_stock_level INT DEFAULT 0,
                    reorder_point INT DEFAULT 0,
                    reorder_quantity INT DEFAULT 0,
                    location VARCHAR(50),
                    supplier VARCHAR(100),
                    status ENUM('ACTIVE', 'INACTIVE', 'DISCONTINUED') DEFAULT 'ACTIVE',
                    last_movement_date DATETIME,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    INDEX idx_item_code (item_code),
                    INDEX idx_category (category),
                    INDEX idx_status (status)
                )
                """;
            stmt.execute(createInventoryTable);
            
            // Create stock_movements table
            String createMovementsTable = """
                CREATE TABLE IF NOT EXISTS stock_movements (
                    movement_id VARCHAR(50) PRIMARY KEY,
                    item_code VARCHAR(50) NOT NULL,
                    movement_type ENUM('RECEIPT', 'ISSUE', 'TRANSFER_IN', 'TRANSFER_OUT', 
                                      'ADJUSTMENT_IN', 'ADJUSTMENT_OUT', 'RETURN') NOT NULL,
                    quantity INT NOT NULL,
                    reference VARCHAR(100),
                    notes TEXT,
                    movement_date DATETIME NOT NULL,
                    user_id VARCHAR(50) NOT NULL,
                    location_from VARCHAR(50),
                    location_to VARCHAR(50),
                    unit_cost DECIMAL(10,2) DEFAULT 0.00,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (item_code) REFERENCES inventory_items(item_code),
                    INDEX idx_item_code (item_code),
                    INDEX idx_movement_date (movement_date),
                    INDEX idx_movement_type (movement_type)
                )
                """;
            stmt.execute(createMovementsTable);
            
            // Create purchase_orders table
            String createPurchaseOrdersTable = """
                CREATE TABLE IF NOT EXISTS purchase_orders (
                    po_id VARCHAR(50) PRIMARY KEY,
                    supplier VARCHAR(100) NOT NULL,
                    order_date DATETIME NOT NULL,
                    expected_date DATETIME,
                    status ENUM('PENDING', 'APPROVED', 'ORDERED', 'RECEIVED', 'CANCELLED') DEFAULT 'PENDING',
                    total_amount DECIMAL(12,2) DEFAULT 0.00,
                    notes TEXT,
                    created_by VARCHAR(50),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    INDEX idx_supplier (supplier),
                    INDEX idx_status (status),
                    INDEX idx_order_date (order_date)
                )
                """;
            stmt.execute(createPurchaseOrdersTable);
            
            insertSampleInventoryData(stmt);
            System.out.println("Inventory tables initialized successfully.");
            
        } catch (SQLException e) {
            System.err.println("Failed to initialize inventory tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void insertSampleInventoryData(Statement stmt) throws SQLException {
        String insertSampleItems = """
            INSERT IGNORE INTO inventory_items 
            (item_code, description, category, unit_of_measure, unit_price, quantity_on_hand, 
             minimum_stock_level, reorder_point, location, supplier, status) VALUES
            ('RM-STEEL-001', 'Steel Sheet 2mm', 'RAW_MATERIAL', 'SHEETS', 45.50, 250, 50, 75, 'A-01-01', 'Steel Corp', 'ACTIVE'),
            ('RM-PLASTIC-001', 'ABS Plastic Pellets', 'RAW_MATERIAL', 'KG', 12.30, 500, 100, 150, 'A-01-02', 'Plastic Inc', 'ACTIVE'),
            ('COMP-BOLT-001', 'M6x20 Hex Bolt', 'COMPONENT', 'PIECES', 0.25, 1000, 200, 300, 'B-02-01', 'Fasteners Ltd', 'ACTIVE'),
            ('COMP-CIRCUIT-001', 'PCB Main Board', 'COMPONENT', 'PIECES', 35.00, 75, 20, 30, 'B-02-02', 'Electronics Co', 'ACTIVE'),
            ('FG-CHAIR-001', 'Ergonomic Office Chair', 'FINISHED_GOOD', 'PIECES', 250.00, 25, 5, 10, 'C-03-01', '', 'ACTIVE'),
            ('FG-TABLE-001', 'Conference Table', 'FINISHED_GOOD', 'PIECES', 800.00, 12, 3, 5, 'C-03-02', '', 'ACTIVE'),
            ('CONS-OIL-001', 'Hydraulic Oil', 'CONSUMABLE', 'LITERS', 8.50, 100, 25, 40, 'D-04-01', 'Oil Supply', 'ACTIVE')
            """;
        stmt.execute(insertSampleItems);
    }

    public boolean addInventoryItem(InventoryItem item) {
        String query = """
            INSERT INTO inventory_items 
            (item_code, description, category, unit_of_measure, unit_price, quantity_on_hand,
             minimum_stock_level, maximum_stock_level, reorder_point, reorder_quantity,
             location, supplier, status) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
            
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, item.getItemCode());
            stmt.setString(2, item.getDescription());
            stmt.setString(3, item.getCategory());
            stmt.setString(4, item.getUnitOfMeasure());
            stmt.setDouble(5, item.getUnitPrice());
            stmt.setInt(6, item.getQuantityOnHand());
            stmt.setInt(7, item.getMinimumStockLevel());
            stmt.setInt(8, item.getMaximumStockLevel());
            stmt.setInt(9, item.getReorderPoint());
            stmt.setInt(10, item.getReorderQuantity());
            stmt.setString(11, item.getLocation());
            stmt.setString(12, item.getSupplier());
            stmt.setString(13, item.getStatus());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error adding inventory item: " + e.getMessage());
            return false;
        }
    }

    public List<InventoryItem> getAllInventoryItems() {
        List<InventoryItem> items = new ArrayList<>();
        String query = "SELECT * FROM inventory_items ORDER BY item_code";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                InventoryItem item = new InventoryItem();
                item.setItemId(rs.getInt("item_id"));
                item.setItemCode(rs.getString("item_code"));
                item.setDescription(rs.getString("description"));
                item.setCategory(rs.getString("category"));
                item.setUnitOfMeasure(rs.getString("unit_of_measure"));
                item.setUnitPrice(rs.getDouble("unit_price"));
                item.setQuantityOnHand(rs.getInt("quantity_on_hand"));
                item.setMinimumStockLevel(rs.getInt("minimum_stock_level"));
                item.setMaximumStockLevel(rs.getInt("maximum_stock_level"));
                item.setReorderPoint(rs.getInt("reorder_point"));
                item.setReorderQuantity(rs.getInt("reorder_quantity"));
                item.setLocation(rs.getString("location"));
                item.setSupplier(rs.getString("supplier"));
                item.setStatus(rs.getString("status"));
                
                items.add(item);
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving inventory items: " + e.getMessage());
        }
        
        return items;
    }

    public InventoryStatistics getInventoryStatistics() {
        InventoryStatistics stats = new InventoryStatistics();
        
        try (Statement stmt = connection.createStatement()) {
            // Total items
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM inventory_items WHERE status = 'ACTIVE'");
            if (rs.next()) {
                stats.setTotalItems(rs.getInt("total"));
            }
            
            // Low stock items
            rs = stmt.executeQuery("""
                SELECT COUNT(*) as low_stock 
                FROM inventory_items 
                WHERE quantity_on_hand <= minimum_stock_level AND status = 'ACTIVE'
                """);
            if (rs.next()) {
                stats.setLowStockItems(rs.getInt("low_stock"));
            }
            
            // Total value
            rs = stmt.executeQuery("""
                SELECT SUM(quantity_on_hand * unit_price) as total_value 
                FROM inventory_items 
                WHERE status = 'ACTIVE'
                """);
            if (rs.next()) {
                stats.setTotalValue(rs.getDouble("total_value"));
            }
            
            // Reorder items
            rs = stmt.executeQuery("""
                SELECT COUNT(*) as reorder_items 
                FROM inventory_items 
                WHERE quantity_on_hand <= reorder_point AND status = 'ACTIVE'
                """);
            if (rs.next()) {
                stats.setReorderItems(rs.getInt("reorder_items"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting inventory statistics: " + e.getMessage());
        }
        
        return stats;
    }

    public boolean recordStockMovement(StockMovement movement) {
        String query = """
            INSERT INTO stock_movements 
            (movement_id, item_code, movement_type, quantity, reference, notes,
             movement_date, user_id, location_from, location_to, unit_cost) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
            
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, movement.getMovementId());
            stmt.setString(2, movement.getItemCode());
            stmt.setString(3, movement.getMovementType());
            stmt.setInt(4, movement.getQuantity());
            stmt.setString(5, movement.getReference());
            stmt.setString(6, movement.getNotes());
            stmt.setString(7, movement.getMovementDate().format(formatter));
            stmt.setString(8, movement.getUser());
            stmt.setString(9, movement.getLocationFrom());
            stmt.setString(10, movement.getLocationTo());
            stmt.setDouble(11, movement.getUnitCost());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error recording stock movement: " + e.getMessage());
            return false;
        }
    }
}

/**
 * Inventory Item model class
 */
class InventoryItem {
    private IntegerProperty itemId;
    private StringProperty itemCode;
    private StringProperty description;
    private StringProperty category;
    private StringProperty unitOfMeasure;
    private DoubleProperty unitPrice;
    private IntegerProperty quantityOnHand;
    private IntegerProperty minimumStockLevel;
    private IntegerProperty maximumStockLevel;
    private IntegerProperty reorderPoint;
    private IntegerProperty reorderQuantity;
    private StringProperty location;
    private StringProperty supplier;
    private StringProperty status;

    public InventoryItem() {
        this.itemId = new SimpleIntegerProperty();
        this.itemCode = new SimpleStringProperty("");
        this.description = new SimpleStringProperty("");
        this.category = new SimpleStringProperty("");
        this.unitOfMeasure = new SimpleStringProperty("");
        this.unitPrice = new SimpleDoubleProperty();
        this.quantityOnHand = new SimpleIntegerProperty();
        this.minimumStockLevel = new SimpleIntegerProperty();
        this.maximumStockLevel = new SimpleIntegerProperty();
        this.reorderPoint = new SimpleIntegerProperty();
        this.reorderQuantity = new SimpleIntegerProperty();
        this.location = new SimpleStringProperty("");
        this.supplier = new SimpleStringProperty("");
        this.status = new SimpleStringProperty("");
    }

    // Getters
    public int getItemId() { return itemId.get(); }
    public String getItemCode() { return itemCode.get(); }
    public String getDescription() { return description.get(); }
    public String getCategory() { return category.get(); }
    public String getUnitOfMeasure() { return unitOfMeasure.get(); }
    public double getUnitPrice() { return unitPrice.get(); }
    public int getQuantityOnHand() { return quantityOnHand.get(); }
    public int getMinimumStockLevel() { return minimumStockLevel.get(); }
    public int getMaximumStockLevel() { return maximumStockLevel.get(); }
    public int getReorderPoint() { return reorderPoint.get(); }
    public int getReorderQuantity() { return reorderQuantity.get(); }
    public String getLocation() { return location.get(); }
    public String getSupplier() { return supplier.get(); }
    public String getStatus() { return status.get(); }

    // Property getters
    public IntegerProperty itemIdProperty() { return itemId; }
    public StringProperty itemCodeProperty() { return itemCode; }
    public StringProperty descriptionProperty() { return description; }
    public StringProperty categoryProperty() { return category; }
    public StringProperty unitOfMeasureProperty() { return unitOfMeasure; }
    public DoubleProperty unitPriceProperty() { return unitPrice; }
    public IntegerProperty quantityOnHandProperty() { return quantityOnHand; }
    public IntegerProperty minimumStockLevelProperty() { return minimumStockLevel; }
    public IntegerProperty maximumStockLevelProperty() { return maximumStockLevel; }
    public IntegerProperty reorderPointProperty() { return reorderPoint; }
    public IntegerProperty reorderQuantityProperty() { return reorderQuantity; }
    public StringProperty locationProperty() { return location; }
    public StringProperty supplierProperty() { return supplier; }
    public StringProperty statusProperty() { return status; }

    // Setters
    public void setItemId(int itemId) { this.itemId.set(itemId); }
    public void setItemCode(String itemCode) { this.itemCode.set(itemCode); }
    public void setDescription(String description) { this.description.set(description); }
    public void setCategory(String category) { this.category.set(category); }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure.set(unitOfMeasure); }
    public void setUnitPrice(double unitPrice) { this.unitPrice.set(unitPrice); }
    public void setQuantityOnHand(int quantityOnHand) { this.quantityOnHand.set(quantityOnHand); }
    public void setMinimumStockLevel(int minimumStockLevel) { this.minimumStockLevel.set(minimumStockLevel); }
    public void setMaximumStockLevel(int maximumStockLevel) { this.maximumStockLevel.set(maximumStockLevel); }
    public void setReorderPoint(int reorderPoint) { this.reorderPoint.set(reorderPoint); }
    public void setReorderQuantity(int reorderQuantity) { this.reorderQuantity.set(reorderQuantity); }
    public void setLocation(String location) { this.location.set(location); }
    public void setSupplier(String supplier) { this.supplier.set(supplier); }
    public void setStatus(String status) { this.status.set(status); }

    // Utility methods
    public boolean isLowStock() {
        return quantityOnHand.get() <= minimumStockLevel.get() && quantityOnHand.get() > 0;
    }

    public boolean isOutOfStock() {
        return quantityOnHand.get() == 0;
    }

    public boolean needsReorder() {
        return quantityOnHand.get() <= reorderPoint.get();
    }

    public double getTotalValue() {
        return quantityOnHand.get() * unitPrice.get();
    }
}

/**
 * Stock Movement model class
 */
class StockMovement {
    private StringProperty movementId;
    private StringProperty itemCode;
    private StringProperty movementType;
    private IntegerProperty quantity;
    private StringProperty reference;
    private StringProperty notes;
    private ObjectProperty<LocalDateTime> movementDate;
    private StringProperty user;
    private StringProperty locationFrom;
    private StringProperty locationTo;
    private DoubleProperty unitCost;

    public StockMovement() {
        this.movementId = new SimpleStringProperty("");
        this.itemCode = new SimpleStringProperty("");
        this.movementType = new SimpleStringProperty("");
        this.quantity = new SimpleIntegerProperty();
        this.reference = new SimpleStringProperty("");
        this.notes = new SimpleStringProperty("");
        this.movementDate = new SimpleObjectProperty<>(LocalDateTime.now());
        this.user = new SimpleStringProperty("");
        this.locationFrom = new SimpleStringProperty("");
        this.locationTo = new SimpleStringProperty("");
        this.unitCost = new SimpleDoubleProperty();
    }

    // Getters
    public String getMovementId() { return movementId.get(); }
    public String getItemCode() { return itemCode.get(); }
    public String getMovementType() { return movementType.get(); }
    public int getQuantity() { return quantity.get(); }
    public String getReference() { return reference.get(); }
    public String getNotes() { return notes.get(); }
    public LocalDateTime getMovementDate() { return movementDate.get(); }
    public String getUser() { return user.get(); }
    public String getLocationFrom() { return locationFrom.get(); }
    public String getLocationTo() { return locationTo.get(); }
    public double getUnitCost() { return unitCost.get(); }

    // Property getters
    public StringProperty movementIdProperty() { return movementId; }
    public StringProperty itemCodeProperty() { return itemCode; }
    public StringProperty movementTypeProperty() { return movementType; }
    public IntegerProperty quantityProperty() { return quantity; }
    public StringProperty referenceProperty() { return reference; }
    public StringProperty notesProperty() { return notes; }
    public ObjectProperty<LocalDateTime> movementDateProperty() { return movementDate; }
    public StringProperty userProperty() { return user; }
    public StringProperty locationFromProperty() { return locationFrom; }
    public StringProperty locationToProperty() { return locationTo; }
    public DoubleProperty unitCostProperty() { return unitCost; }

    // Setters
    public void setMovementId(String movementId) { this.movementId.set(movementId); }
    public void setItemCode(String itemCode) { this.itemCode.set(itemCode); }
    public void setMovementType(String movementType) { this.movementType.set(movementType); }
    public void setQuantity(int quantity) { this.quantity.set(quantity); }
    public void setReference(String reference) { this.reference.set(reference); }
    public void setNotes(String notes) { this.notes.set(notes); }
    public void setMovementDate(LocalDateTime movementDate) { this.movementDate.set(movementDate); }
    public void setUser(String user) { this.user.set(user); }
    public void setLocationFrom(String locationFrom) { this.locationFrom.set(locationFrom); }
    public void setLocationTo(String locationTo) { this.locationTo.set(locationTo); }
    public void setUnitCost(double unitCost) { this.unitCost.set(unitCost); }
}

/**
 * Inventory Statistics helper class
 */
class InventoryStatistics {
    private int totalItems;
    private int lowStockItems;
    private int reorderItems;
    private double totalValue;
    private int activeItems;
    private int inactiveItems;

    public InventoryStatistics() {
        this.totalItems = 0;
        this.lowStockItems = 0;
        this.reorderItems = 0;
        this.totalValue = 0.0;
        this.activeItems = 0;
        this.inactiveItems = 0;
    }

    // Getters and Setters
    public int getTotalItems() { return totalItems; }
    public void setTotalItems(int totalItems) { this.totalItems = totalItems; }

    public int getLowStockItems() { return lowStockItems; }
    public void setLowStockItems(int lowStockItems) { this.lowStockItems = lowStockItems; }

    public int getReorderItems() { return reorderItems; }
    public void setReorderItems(int reorderItems) { this.reorderItems = reorderItems; }

    public double getTotalValue() { return totalValue; }
    public void setTotalValue(double totalValue) { this.totalValue = totalValue; }

    public int getActiveItems() { return activeItems; }
    public void setActiveItems(int activeItems) { this.activeItems = activeItems; }

    public int getInactiveItems() { return inactiveItems; }
    public void setInactiveItems(int inactiveItems) { this.inactiveItems = inactiveItems; }
}