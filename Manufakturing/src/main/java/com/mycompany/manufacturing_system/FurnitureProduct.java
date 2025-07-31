package com.mycompany.manufacturing_system;

/**
 * Concrete implementation for Furniture products
 */
public class FurnitureProduct extends Product {
    private String materialType;
    private String finishType;
    private String dimensions;
    
    public FurnitureProduct(String productId, String productName, int quantity, double unitCost) {
        super(productId, productName, "FURNITURE", quantity, unitCost);
        this.materialType = "Solid Wood";
        this.finishType = "Natural Lacquer";
        this.dimensions = "Standard";
    }

    @Override
    public void calculateProductionCost() {
        double baseCost = getUnitCost();
        double materialCost = baseCost * 0.4;
        double laborCost = baseCost * 0.35; // Furniture is labor-intensive
        double finishingCost = baseCost * 0.15;
        double hardwareCost = baseCost * 0.05;
        double overheadCost = baseCost * 0.05;
        
        double totalUnitCost = materialCost + laborCost + finishingCost + hardwareCost + overheadCost;
        setUnitCost(totalUnitCost);
    }

    @Override
    public void updateQualityStatus() {
        double completion = getCompletionPercentage();
        if (completion >= 100.0) {
            setStatus("QUALITY_CHECK");
        } else if (completion >= 80.0) {
            setStatus("IN_PRODUCTION"); // Finishing stage
        } else if (completion > 0) {
            setStatus("IN_PRODUCTION");
        }
    }

    @Override
    public String getProductionRequirements() {
        return String.format(
            "Material: %s | Finish: %s | Dimensions: %s | " +
            "Required Equipment: CNC Router, Sanders, Spray Booth, Assembly Station",
            materialType, finishType, dimensions
        );
    }

    // Furniture-specific getters and setters
    public String getMaterialType() { return materialType; }
    public void setMaterialType(String materialType) { this.materialType = materialType; }
    
    public String getFinishType() { return finishType; }
    public void setFinishType(String finishType) { this.finishType = finishType; }
    
    public String getDimensions() { return dimensions; }
    public void setDimensions(String dimensions) { this.dimensions = dimensions; }
}