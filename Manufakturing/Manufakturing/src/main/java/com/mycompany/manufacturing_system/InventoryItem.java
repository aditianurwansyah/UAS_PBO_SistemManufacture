package com.mycompany.manufacturing_system;

import javafx.beans.property.*;

public class InventoryItem {
    private final StringProperty itemCode;
    private final StringProperty description;
    private final StringProperty category;
    private final IntegerProperty quantityOnHand;
    private final DoubleProperty unitPrice;
    private final StringProperty location;
    private final StringProperty status; // e.g., ACTIVE, INACTIVE, DISCONTINUED
    private final IntegerProperty reorderPoint;
    private final IntegerProperty minStockLevel;

    public InventoryItem(String itemCode, String description, String category, int quantityOnHand,
                         double unitPrice, String location, String status, int reorderPoint, int minStockLevel) {
        this.itemCode = new SimpleStringProperty(itemCode);
        this.description = new SimpleStringProperty(description);
        this.category = new SimpleStringProperty(category);
        this.quantityOnHand = new SimpleIntegerProperty(quantityOnHand);
        this.unitPrice = new SimpleDoubleProperty(unitPrice);
        this.location = new SimpleStringProperty(location);
        this.status = new SimpleStringProperty(status);
        this.reorderPoint = new SimpleIntegerProperty(reorderPoint);
        this.minStockLevel = new SimpleIntegerProperty(minStockLevel);
    }

    // Property getters
    public StringProperty itemCodeProperty() { return itemCode; }
    public StringProperty descriptionProperty() { return description; }
    public StringProperty categoryProperty() { return category; }
    public IntegerProperty quantityOnHandProperty() { return quantityOnHand; }
    public DoubleProperty unitPriceProperty() { return unitPrice; }
    public StringProperty locationProperty() { return location; }
    public StringProperty statusProperty() { return status; }
    public IntegerProperty reorderPointProperty() { return reorderPoint; }
    public IntegerProperty minStockLevelProperty() { return minStockLevel; }

    // Regular getters
    public String getItemCode() { return itemCode.get(); }
    public String getDescription() { return description.get(); }
    public String getCategory() { return category.get(); }
    public int getQuantityOnHand() { return quantityOnHand.get(); }
    public double getUnitPrice() { return unitPrice.get(); }
    public String getLocation() { return location.get(); }
    public String getStatus() { return status.get(); }
    public int getReorderPoint() { return reorderPoint.get(); }
    public int getMinStockLevel() { return minStockLevel.get(); }

    // Setters (if needed for updates)
    public void setQuantityOnHand(int quantityOnHand) { this.quantityOnHand.set(quantityOnHand); }
    public void setStatus(String status) { this.status.set(status); }

    // Helper methods for stock status
    public boolean isLowStock() {
        return getQuantityOnHand() <= getReorderPoint() && getQuantityOnHand() > 0;
    }

    public boolean isOutOfStock() {
        return getQuantityOnHand() <= 0;
    }

    public boolean needsReorder() {
        return getQuantityOnHand() <= getReorderPoint();
    }
}
 