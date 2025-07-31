package com.mycompany.manufacturing_system;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Quality Inspection model class
 * Represents a quality inspection record in the manufacturing system
 */
public class QualityInspection {
    private final StringProperty inspectionId;
    private final StringProperty orderId;
    private final StringProperty productName;
    private final StringProperty inspectionType;
    private final StringProperty inspector;
    private final StringProperty status; // PASSED, FAILED, PENDING
    private final ObjectProperty<LocalDateTime> inspectionDate;
    private final StringProperty qualityCriteria;
    private final StringProperty defectTypes;
    private final IntegerProperty defectCount;
    private final StringProperty correctiveActions;
    private final StringProperty notes;
    private final DoubleProperty qualityScore;

    public QualityInspection(String inspectionId, String orderId, String inspectionType, 
                           String inspector, String status) {
        this.inspectionId = new SimpleStringProperty(inspectionId);
        this.orderId = new SimpleStringProperty(orderId);
        this.productName = new SimpleStringProperty("");
        this.inspectionType = new SimpleStringProperty(inspectionType);
        this.inspector = new SimpleStringProperty(inspector);
        this.status = new SimpleStringProperty(status);
        this.inspectionDate = new SimpleObjectProperty<>(LocalDateTime.now());
        this.qualityCriteria = new SimpleStringProperty("");
        this.defectTypes = new SimpleStringProperty("");
        this.defectCount = new SimpleIntegerProperty(0);
        this.correctiveActions = new SimpleStringProperty("");
        this.notes = new SimpleStringProperty("");
        this.qualityScore = new SimpleDoubleProperty(0.0);
    }

    // Getters
    public String getInspectionId() { return inspectionId.get(); }
    public String getOrderId() { return orderId.get(); }
    public String getProductName() { return productName.get(); }
    public String getInspectionType() { return inspectionType.get(); }
    public String getInspector() { return inspector.get(); }
    public String getStatus() { return status.get(); }
    public LocalDateTime getInspectionDate() { return inspectionDate.get(); }
    public String getQualityCriteria() { return qualityCriteria.get(); }
    public String getDefectTypes() { return defectTypes.get(); }
    public int getDefectCount() { return defectCount.get(); }
    public String getCorrectiveActions() { return correctiveActions.get(); }
    public String getNotes() { return notes.get(); }
    public double getQualityScore() { return qualityScore.get(); }

    // Property getters for JavaFX TableView
    public StringProperty inspectionIdProperty() { return inspectionId; }
    public StringProperty orderIdProperty() { return orderId; }
    public StringProperty productNameProperty() { return productName; }
    public StringProperty inspectionTypeProperty() { return inspectionType; }
    public StringProperty inspectorProperty() { return inspector; }
    public StringProperty statusProperty() { return status; }
    public ObjectProperty<LocalDateTime> inspectionDateProperty() { return inspectionDate; }
    public StringProperty qualityCriteriaProperty() { return qualityCriteria; }
    public StringProperty defectTypesProperty() { return defectTypes; }
    public IntegerProperty defectCountProperty() { return defectCount; }
    public StringProperty correctiveActionsProperty() { return correctiveActions; }
    public StringProperty notesProperty() { return notes; }
    public DoubleProperty qualityScoreProperty() { return qualityScore; }

    // Setters
    public void setInspectionId(String inspectionId) { this.inspectionId.set(inspectionId); }
    public void setOrderId(String orderId) { this.orderId.set(orderId); }
    public void setProductName(String productName) { this.productName.set(productName); }
    public void setInspectionType(String inspectionType) { this.inspectionType.set(inspectionType); }
    public void setInspector(String inspector) { this.inspector.set(inspector); }
    public void setStatus(String status) { this.status.set(status); }
    public void setInspectionDate(LocalDateTime inspectionDate) { this.inspectionDate.set(inspectionDate); }
    public void setQualityCriteria(String qualityCriteria) { this.qualityCriteria.set(qualityCriteria); }
    public void setDefectTypes(String defectTypes) { this.defectTypes.set(defectTypes); }
    public void setDefectCount(int defectCount) { this.defectCount.set(defectCount); }
    public void setCorrectiveActions(String correctiveActions) { this.correctiveActions.set(correctiveActions); }
    public void setNotes(String notes) { this.notes.set(notes); }
    public void setQualityScore(double qualityScore) { this.qualityScore.set(qualityScore); }

    // Utility methods
    public String getFormattedInspectionDate() {
        return inspectionDate.get().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public boolean isPassed() {
        return "PASSED".equals(status.get());
    }

    public boolean isFailed() {
        return "FAILED".equals(status.get());
    }

    public boolean isPending() {
        return "PENDING".equals(status.get());
    }

    public String getStatusColor() {
        switch (status.get()) {
            case "PASSED": return "#27ae60"; // Green
            case "FAILED": return "#e74c3c"; // Red
            case "PENDING": return "#f39c12"; // Orange
            default: return "#95a5a6"; // Grey
        }
    }

    public String getInspectionTypeDescription() {
        switch (inspectionType.get()) {
            case "INCOMING": return "Incoming Material Inspection";
            case "IN_PROCESS": return "In-Process Quality Check";
            case "FINAL": return "Final Product Inspection";
            case "OUTGOING": return "Outgoing Shipment Check";
            case "QUICK_CHECK": return "Quick Quality Check";
            default: return inspectionType.get();
        }
    }

    @Override
    public String toString() {
        return String.format("Inspection %s: %s [%s] - %s", 
            inspectionId.get(), orderId.get(), status.get(), inspector.get());
    }
}