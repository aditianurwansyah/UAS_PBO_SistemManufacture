package com.mycompany.manufacturing_system;

/**
 * Concrete implementation for Automotive products
 */
public class AutomotiveProduct extends Product {
    private String vehicleType;
    private String engineSpec;
    private String safetyRating;
    
    public AutomotiveProduct(String productId, String productName, int quantity, double unitCost) {
        super(productId, productName, "AUTOMOTIVE", quantity, unitCost);
        this.vehicleType = "Standard";
        this.engineSpec = "2.0L 4-Cylinder";
        this.safetyRating = "5-Star";
    }

    @Override
    public void calculateProductionCost() {
        // Complex calculation for automotive products
        double baseCost = getUnitCost();
        double materialCost = baseCost * 0.6;
        double laborCost = baseCost * 0.25;
        double overheadCost = baseCost * 0.15;
        
        // Additional costs for automotive-specific requirements
        double qualityTestingCost = baseCost * 0.05;
        double safetyComplianceCost = baseCost * 0.03;
        
        double totalUnitCost = materialCost + laborCost + overheadCost + 
                              qualityTestingCost + safetyComplianceCost;
        setUnitCost(totalUnitCost);
    }

    @Override
    public void updateQualityStatus() {
        double completion = getCompletionPercentage();
        if (completion >= 100.0) {
            setStatus("QUALITY_CHECK");
        } else if (completion >= 75.0) {
            setStatus("IN_PRODUCTION");
        } else if (completion > 0) {
            setStatus("IN_PRODUCTION");
        }
    }

    @Override
    public String getProductionRequirements() {
        return String.format(
            "Vehicle Type: %s | Engine: %s | Safety Rating: %s | " +
            "Required Equipment: Assembly Line, Paint Booth, Quality Testing Station",
            vehicleType, engineSpec, safetyRating
        );
    }

    // Automotive-specific getters and setters
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    
    public String getEngineSpec() { return engineSpec; }
    public void setEngineSpec(String engineSpec) { this.engineSpec = engineSpec; }
    
    public String getSafetyRating() { return safetyRating; }
    public void setSafetyRating(String safetyRating) { this.safetyRating = safetyRating; }
}