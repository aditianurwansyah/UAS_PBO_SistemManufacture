package com.mycompany.manufacturing_system;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a production order in the manufacturing system
 */
public class ProductionOrder {
    private final StringProperty orderId;
    private final StringProperty customerName;
    private final StringProperty productId;
    private final StringProperty productName;
    private final IntegerProperty quantity;
    private final StringProperty status;
    private final StringProperty priority;
    private final ObjectProperty<LocalDateTime> orderDate;
    private final ObjectProperty<LocalDateTime> dueDate;
    private final ObjectProperty<LocalDateTime> startDate;
    private final ObjectProperty<LocalDateTime> completionDate;
    private final StringProperty assignedLine;
    private final StringProperty assignedOperator;
    private final DoubleProperty totalCost;
    private final DoubleProperty completionPercentage;
    private final StringProperty notes;

    public ProductionOrder(String orderId, String customerName, String productId, 
                          String productName, int quantity, String priority) {
        this.orderId = new SimpleStringProperty(orderId);
        this.customerName = new SimpleStringProperty(customerName);
        this.productId = new SimpleStringProperty(productId);
        this.productName = new SimpleStringProperty(productName);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.status = new SimpleStringProperty("PENDING");
        this.priority = new SimpleStringProperty(priority);
        this.orderDate = new SimpleObjectProperty<>(LocalDateTime.now());
        this.dueDate = new SimpleObjectProperty<>();
        this.startDate = new SimpleObjectProperty<>();
        this.completionDate = new SimpleObjectProperty<>();
        this.assignedLine = new SimpleStringProperty("");
        this.assignedOperator = new SimpleStringProperty("");
        this.totalCost = new SimpleDoubleProperty(0.0);
        this.completionPercentage = new SimpleDoubleProperty(0.0);
        this.notes = new SimpleStringProperty("");
    }

    // Getters
    public String getOrderId() { return orderId.get(); }
    public String getCustomerName() { return customerName.get(); }
    public String getProductId() { return productId.get(); }
    public String getProductName() { return productName.get(); }
    public int getQuantity() { return quantity.get(); }
    public String getStatus() { return status.get(); }
    public String getPriority() { return priority.get(); }
    public LocalDateTime getOrderDate() { return orderDate.get(); }
    public LocalDateTime getDueDate() { return dueDate.get(); }
    public LocalDateTime getStartDate() { return startDate.get(); }
    public LocalDateTime getCompletionDate() { return completionDate.get(); }
    public String getAssignedLine() { return assignedLine.get(); }
    public String getAssignedOperator() { return assignedOperator.get(); }
    public double getTotalCost() { return totalCost.get(); }
    public double getCompletionPercentage() { return completionPercentage.get(); }
    public String getNotes() { return notes.get(); }

    // Property getters for JavaFX TableView
    public StringProperty orderIdProperty() { return orderId; }
    public StringProperty customerNameProperty() { return customerName; }
    public StringProperty productIdProperty() { return productId; }
    public StringProperty productNameProperty() { return productName; }
    public IntegerProperty quantityProperty() { return quantity; }
    public StringProperty statusProperty() { return status; }
    public StringProperty priorityProperty() { return priority; }
    public ObjectProperty<LocalDateTime> orderDateProperty() { return orderDate; }
    public ObjectProperty<LocalDateTime> dueDateProperty() { return dueDate; }
    public ObjectProperty<LocalDateTime> startDateProperty() { return startDate; }
    public ObjectProperty<LocalDateTime> completionDateProperty() { return completionDate; }
    public StringProperty assignedLineProperty() { return assignedLine; }
    public StringProperty assignedOperatorProperty() { return assignedOperator; }
    public DoubleProperty totalCostProperty() { return totalCost; }
    public DoubleProperty completionPercentageProperty() { return completionPercentage; }
    public StringProperty notesProperty() { return notes; }

    // Setters
    public void setOrderId(String orderId) { this.orderId.set(orderId); }
    public void setCustomerName(String customerName) { this.customerName.set(customerName); }
    public void setProductId(String productId) { this.productId.set(productId); }
    public void setProductName(String productName) { this.productName.set(productName); }
    public void setQuantity(int quantity) { this.quantity.set(quantity); }
    public void setStatus(String status) { this.status.set(status); }
    public void setPriority(String priority) { this.priority.set(priority); }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate.set(orderDate); }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate.set(dueDate); }
    public void setStartDate(LocalDateTime startDate) { this.startDate.set(startDate); }
    public void setCompletionDate(LocalDateTime completionDate) { this.completionDate.set(completionDate); }
    public void setAssignedLine(String assignedLine) { this.assignedLine.set(assignedLine); }
    public void setAssignedOperator(String assignedOperator) { this.assignedOperator.set(assignedOperator); }
    public void setTotalCost(double totalCost) { this.totalCost.set(totalCost); }
    public void setCompletionPercentage(double completionPercentage) { this.completionPercentage.set(completionPercentage); }
    public void setNotes(String notes) { this.notes.set(notes); }

    // Utility methods
    public String getFormattedOrderDate() {
        return orderDate.get().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getFormattedDueDate() {
        LocalDateTime due = dueDate.get();
        return due != null ? due.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "Not Set";
    }

    public String getFormattedStartDate() {
        LocalDateTime start = startDate.get();
        return start != null ? start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "Not Started";
    }

    public String getFormattedCompletionDate() {
        LocalDateTime completion = completionDate.get();
        return completion != null ? completion.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "Not Completed";
    }

    public boolean isOverdue() {
        if (dueDate.get() == null || isCompleted()) {
            return false;
        }
        return LocalDateTime.now().isAfter(dueDate.get());
    }

    public boolean isCompleted() {
        return "COMPLETED".equals(status.get()) || "SHIPPED".equals(status.get());
    }

    public boolean isInProgress() {
        return "IN_PROGRESS".equals(status.get());
    }

    public String getStatusColor() {
        switch (status.get()) {
            case "PENDING": return "#ff9800"; // Orange
            case "IN_PROGRESS": return "#2196f3"; // Blue
            case "COMPLETED": return "#4caf50"; // Green
            case "SHIPPED": return "#9c27b0"; // Purple
            case "CANCELLED": return "#f44336"; // Red
            case "ON_HOLD": return "#607d8b"; // Blue Grey
            default: return "#757575"; // Grey
        }
    }

    public String getPriorityColor() {
        switch (priority.get()) {
            case "URGENT": return "#f44336"; // Red
            case "HIGH": return "#ff9800"; // Orange
            case "MEDIUM": return "#2196f3"; // Blue
            case "LOW": return "#4caf50"; // Green
            default: return "#757575"; // Grey
        }
    }

    @Override
    public String toString() {
        return String.format("Order %s: %s x%d [%s] - %s", 
            orderId.get(), productName.get(), quantity.get(), status.get(), priority.get());
    }
}