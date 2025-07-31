package com.mycompany.manufacturing_system;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Abstract base class for all manufacturing products
 * Replaces the Console class with manufacturing-focused attributes
 */
public abstract class Product {
    protected StringProperty productId;
    protected StringProperty productName;
    protected StringProperty category;
    protected IntegerProperty quantity;
    protected DoubleProperty unitCost;
    protected StringProperty status; // PLANNING, IN_PRODUCTION, QUALITY_CHECK, COMPLETED, SHIPPED
    protected StringProperty assignedOperator;
    protected StringProperty productionLine;
    protected ObjectProperty<LocalDateTime> startTime;
    protected ObjectProperty<LocalDateTime> estimatedCompletion;
    protected IntegerProperty priority; // 1-5 (1 highest)
    protected StringProperty specifications;
    protected DoubleProperty completionPercentage;

    public Product(String productId, String productName, String category, int quantity, double unitCost) {
        this.productId = new SimpleStringProperty(productId);
        this.productName = new SimpleStringProperty(productName);
        this.category = new SimpleStringProperty(category);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.unitCost = new SimpleDoubleProperty(unitCost);
        this.status = new SimpleStringProperty("PLANNING");
        this.assignedOperator = new SimpleStringProperty("");
        this.productionLine = new SimpleStringProperty("");
        this.startTime = new SimpleObjectProperty<>();
        this.estimatedCompletion = new SimpleObjectProperty<>();
        this.priority = new SimpleIntegerProperty(3); // Default medium priority
        this.specifications = new SimpleStringProperty("");
        this.completionPercentage = new SimpleDoubleProperty(0.0);
    }

    // Abstract methods for subclasses
    public abstract void calculateProductionCost();
    public abstract void updateQualityStatus();
    public abstract String getProductionRequirements();

    // Getters and Property methods
    public String getProductId() { return productId.get(); }
    public StringProperty productIdProperty() { return productId; }
    
    public String getProductName() { return productName.get(); }
    public StringProperty productNameProperty() { return productName; }
    
    public String getCategory() { return category.get(); }
    public StringProperty categoryProperty() { return category; }
    
    public int getQuantity() { return quantity.get(); }
    public IntegerProperty quantityProperty() { return quantity; }
    
    public double getUnitCost() { return unitCost.get(); }
    public DoubleProperty unitCostProperty() { return unitCost; }
    
    public String getStatus() { return status.get(); }
    public StringProperty statusProperty() { return status; }
    
    public String getAssignedOperator() { return assignedOperator.get(); }
    public StringProperty assignedOperatorProperty() { return assignedOperator; }
    
    public String getProductionLine() { return productionLine.get(); }
    public StringProperty productionLineProperty() { return productionLine; }
    
    public LocalDateTime getStartTime() { return startTime.get(); }
    public ObjectProperty<LocalDateTime> startTimeProperty() { return startTime; }
    
    public LocalDateTime getEstimatedCompletion() { return estimatedCompletion.get(); }
    public ObjectProperty<LocalDateTime> estimatedCompletionProperty() { return estimatedCompletion; }
    
    public int getPriority() { return priority.get(); }
    public IntegerProperty priorityProperty() { return priority; }
    
    public String getSpecifications() { return specifications.get(); }
    public StringProperty specificationsProperty() { return specifications; }
    
    public double getCompletionPercentage() { return completionPercentage.get(); }
    public DoubleProperty completionPercentageProperty() { return completionPercentage; }

    // Setters
    public void setProductId(String productId) { this.productId.set(productId); }
    public void setProductName(String productName) { this.productName.set(productName); }
    public void setCategory(String category) { this.category.set(category); }
    public void setQuantity(int quantity) { this.quantity.set(quantity); }
    public void setUnitCost(double unitCost) { this.unitCost.set(unitCost); }
    public void setStatus(String status) { this.status.set(status); }
    public void setAssignedOperator(String operator) { this.assignedOperator.set(operator); }
    public void setProductionLine(String line) { this.productionLine.set(line); }
    public void setStartTime(LocalDateTime startTime) { this.startTime.set(startTime); }
    public void setEstimatedCompletion(LocalDateTime completion) { this.estimatedCompletion.set(completion); }
    public void setPriority(int priority) { this.priority.set(priority); }
    public void setSpecifications(String specs) { this.specifications.set(specs); }
    public void setCompletionPercentage(double percentage) { this.completionPercentage.set(percentage); }

    // Utility methods
    public double getTotalCost() {
        return quantity.get() * unitCost.get();
    }

    public String getFormattedStartTime() {
        LocalDateTime time = startTime.get();
        return time != null ? time.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
    }

    public String getFormattedEstimatedCompletion() {
        LocalDateTime time = estimatedCompletion.get();
        return time != null ? time.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
    }

    public String getPriorityLabel() {
        switch (priority.get()) {
            case 1: return "URGENT";
            case 2: return "HIGH";
            case 3: return "MEDIUM";
            case 4: return "LOW";
            case 5: return "DEFERRED";
            default: return "MEDIUM";
        }
    }

    public boolean isInProduction() {
        return "IN_PRODUCTION".equals(status.get());
    }

    public boolean isCompleted() {
        return "COMPLETED".equals(status.get()) || "SHIPPED".equals(status.get());
    }

    @Override
    public String toString() {
        return String.format("%s - %s [%s] (Qty: %d)", 
            productId.get(), productName.get(), status.get(), quantity.get());
    }
}