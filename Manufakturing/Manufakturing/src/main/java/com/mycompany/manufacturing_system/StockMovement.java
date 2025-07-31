package com.mycompany.manufacturing_system;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StockMovement { 
    private final StringProperty movementId;
    private final ObjectProperty<LocalDateTime> movementDate;
    private final StringProperty itemCode;
    private final StringProperty movementType; // e.g., RECEIVE, ISSUE, ADJUSTMENT, TRANSFER
    private final IntegerProperty quantity;
    private final StringProperty reference; // e.g., PO number, production order ID
    private final StringProperty user; // User who performed the movement

    public StockMovement(String movementId, LocalDateTime movementDate, String itemCode,
                         String movementType, int quantity, String reference, String user) {
        this.movementId = new SimpleStringProperty(movementId);
        this.movementDate = new SimpleObjectProperty<>(movementDate);
        this.itemCode = new SimpleStringProperty(itemCode);
        this.movementType = new SimpleStringProperty(movementType);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.reference = new SimpleStringProperty(reference);
        this.user = new SimpleStringProperty(user);
    }

    // Property getters
    public StringProperty movementIdProperty() { return movementId; }
    public ObjectProperty<LocalDateTime> movementDateProperty() { return movementDate; }
    public StringProperty itemCodeProperty() { return itemCode; }
    public StringProperty movementTypeProperty() { return movementType; }
    public IntegerProperty quantityProperty() { return quantity; }
    public StringProperty referenceProperty() { return reference; }
    public StringProperty userProperty() { return user; }

    // Regular getters
    public String getMovementId() { return movementId.get(); }
    public LocalDateTime getMovementDate() { return movementDate.get(); }
    public String getItemCode() { return itemCode.get(); }
    public String getMovementType() { return movementType.get(); }
    public int getQuantity() { return quantity.get(); }
    public String getReference() { return reference.get(); }
    public String getUser() { return user.get(); }

    // Formatted date getter
    public String getFormattedMovementDate() {
        return movementDate.get().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
    }
}
 