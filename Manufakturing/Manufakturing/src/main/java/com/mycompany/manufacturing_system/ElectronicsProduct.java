package com.mycompany.manufacturing_system;

/**
 * Concrete implementation for Electronics products
 */
public class ElectronicsProduct extends Product {
    private String circuitType;
    private String powerRating;
    private String certificationRequired;
    
    public ElectronicsProduct(String productId, String productName, int quantity, double unitCost) {
        super(productId, productName, "ELECTRONICS", quantity, unitCost);
        this.circuitType = "Digital";
        this.powerRating = "12V DC";
        this.certificationRequired = "CE, FCC";
    }

    @Override
    public void calculateProductionCost() {
        double baseCost = getUnitCost();
        double componentCost = baseCost * 0.5;
        double assemblyLaborCost = baseCost * 0.2;
        double testingCost = baseCost * 0.1;
        double packagingCost = baseCost * 0.05;
        double certificationCost = baseCost * 0.08;
        double overheadCost = baseCost * 0.07;
        
        double totalUnitCost = componentCost + assemblyLaborCost + testingCost + 
                              packagingCost + certificationCost + overheadCost;
        setUnitCost(totalUnitCost);
    }

    @Override
    public void updateQualityStatus() {
        double completion = getCompletionPercentage();
        if (completion >= 100.0) {
            setStatus("QUALITY_CHECK");
        } else if (completion >= 90.0) {
            setStatus("IN_PRODUCTION"); // Electronics need extensive testing
        } else if (completion > 0) {
            setStatus("IN_PRODUCTION");
        }
    }

    @Override
    public String getProductionRequirements() {
        return String.format(
            "Circuit Type: %s | Power Rating: %s | Certification: %s | " +
            "Required Equipment: SMT Line, Wave Soldering, ICT Testing, Burn-in Chamber",
            circuitType, powerRating, certificationRequired
        );
    }

    // Electronics-specific getters and setters
    public String getCircuitType() { return circuitType; }
    public void setCircuitType(String circuitType) { this.circuitType = circuitType; }
    
    public String getPowerRating() { return powerRating; }
    public void setPowerRating(String powerRating) { this.powerRating = powerRating; }
    
    public String getCertificationRequired() { return certificationRequired; }
    public void setCertificationRequired(String certification) { this.certificationRequired = certification; }
}