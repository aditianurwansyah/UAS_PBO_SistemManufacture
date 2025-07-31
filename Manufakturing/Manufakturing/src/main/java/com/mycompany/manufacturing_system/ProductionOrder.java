package com.mycompany.manufacturing_system;

import javafx.beans.property.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

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
    private final DoubleProperty completionPercentage;
    private final StringProperty assignedLine; // New field
    private final StringProperty assignedOperator; // New field
    private final ObjectProperty<LocalDateTime> startDate; // New field
    private final StringProperty notes; // New field

    public ProductionOrder(String orderId, String customerName, String productId, String productName,
                           int quantity, String status, String priority, LocalDateTime orderDate,
                           LocalDateTime dueDate, double completionPercentage, String assignedLine,
                           String assignedOperator, LocalDateTime startDate, String notes) {
        this.orderId = new SimpleStringProperty(orderId);
        this.customerName = new SimpleStringProperty(customerName);
        this.productId = new SimpleStringProperty(productId);
        this.productName = new SimpleStringProperty(productName);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.status = new SimpleStringProperty(status);
        this.priority = new SimpleStringProperty(priority);
        this.orderDate = new SimpleObjectProperty<>(orderDate);
        this.dueDate = new SimpleObjectProperty<>(dueDate);
        this.completionPercentage = new SimpleDoubleProperty(completionPercentage);
        this.assignedLine = new SimpleStringProperty(assignedLine);
        this.assignedOperator = new SimpleStringProperty(assignedOperator);
        this.startDate = new SimpleObjectProperty<>(startDate);
        this.notes = new SimpleStringProperty(notes);
    }

    // Property getters
    public StringProperty orderIdProperty() { return orderId; }
    public StringProperty customerNameProperty() { return customerName; }
    public StringProperty productIdProperty() { return productId; }
    public StringProperty productNameProperty() { return productName; }
    public IntegerProperty quantityProperty() { return quantity; }
    public StringProperty statusProperty() { return status; }
    public StringProperty priorityProperty() { return priority; }
    public ObjectProperty<LocalDateTime> orderDateProperty() { return orderDate; }
    public ObjectProperty<LocalDateTime> dueDateProperty() { return dueDate; }
    public DoubleProperty completionPercentageProperty() { return completionPercentage; }
    public StringProperty assignedLineProperty() { return assignedLine; }
    public StringProperty assignedOperatorProperty() { return assignedOperator; }
    public ObjectProperty<LocalDateTime> startDateProperty() { return startDate; }
    public StringProperty notesProperty() { return notes; }


    // Regular getters
    public String getOrderId() { return orderId.get(); }
    public String getCustomerName() { return customerName.get(); }
    public String getProductId() { return productId.get(); }
    public String getProductName() { return productName.get(); }
    public int getQuantity() { return quantity.get(); }
    public String getStatus() { return status.get(); }
    public String getPriority() { return priority.get(); }
    public LocalDateTime getOrderDate() { return orderDate.get(); }
    public LocalDateTime getDueDate() { return dueDate.get(); }
    public double getCompletionPercentage() { return completionPercentage.get(); }
    public String getAssignedLine() { return assignedLine.get(); }
    public String getAssignedOperator() { return assignedOperator.get(); }
    public LocalDateTime getStartDate() { return startDate.get(); }
    public String getNotes() { return notes.get(); }

    // Setters (if needed, though properties are often bound directly)
    public void setStatus(String status) { this.status.set(status); }
    public void setCompletionPercentage(double completionPercentage) { this.completionPercentage.set(completionPercentage); }
    public void setNotes(String notes) { this.notes.set(notes); }


    // Helper methods for status and priority colors
    public String getStatusColor() {
        return switch (status.get().toUpperCase()) {
            case "PENDING" -> "#f39c12"; // Orange
            case "IN_PROGRESS" -> "#e74c3c"; // Red
            case "COMPLETED" -> "#27ae60"; // Green
            case "SHIPPED" -> "#3498db"; // Blue
            case "CANCELLED" -> "#7f8c8d"; // Gray
            case "ON_HOLD" -> "#95a5a6"; // Light Gray
            default -> "#000000"; // Black
        };
    }

    public String getPriorityColor() {
        return switch (priority.get().toUpperCase()) {
            case "URGENT" -> "#e74c3c"; // Red
            case "HIGH" -> "#f39c12"; // Orange
            case "MEDIUM" -> "#3498db"; // Blue
            case "LOW" -> "#27ae60"; // Green
            default -> "#000000"; // Black
        };
    }

    // New helper methods for MyTasksView
    public boolean isOverdue() {
        return "IN_PROGRESS".equals(getStatus()) && dueDate.get() != null && LocalDateTime.now().isAfter(dueDate.get());
    }

    public boolean isCompleted() {
        return "COMPLETED".equals(getStatus());
    }

    public String getFormattedOrderDate() {
        return orderDate.get() != null ? orderDate.get().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) : "N/A";
    }

    public String getFormattedDueDate() {
        return dueDate.get() != null ? dueDate.get().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) : "N/A";
    }

    public String getFormattedStartDate() {
        return startDate.get() != null ? startDate.get().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) : "N/A";
    }
}
 