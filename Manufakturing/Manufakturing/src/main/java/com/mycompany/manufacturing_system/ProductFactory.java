package com.mycompany.manufacturing_system;

/**
 * Factory class for creating different types of products
 * Implements Factory design pattern for product creation
 */
public class ProductFactory {
    
    /**
     * Create a product based on category
     * @param category Product category (AUTOMOTIVE, ELECTRONICS, FURNITURE)
     * @param productId Product ID
     * @param productName Product name
     * @param quantity Quantity to produce
     * @param unitCost Unit cost
     * @return Product instance
     */
    public static Product createProduct(String category, String productId, 
                                      String productName, int quantity, double unitCost) {
        switch (category.toUpperCase()) {
            case "AUTOMOTIVE":
                return new AutomotiveProduct(productId, productName, quantity, unitCost);
            case "ELECTRONICS":
                return new ElectronicsProduct(productId, productName, quantity, unitCost);
            case "FURNITURE":
                return new FurnitureProduct(productId, productName, quantity, unitCost);
            default:
                throw new IllegalArgumentException("Unknown product category: " + category);
        }
    }
    
    /**
     * Get available product categories
     * @return Array of available categories
     */
    public static String[] getAvailableCategories() {
        return new String[]{"AUTOMOTIVE", "ELECTRONICS", "FURNITURE"};
    }
    
    /**
     * Validate product category
     * @param category Category to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidCategory(String category) {
        for (String validCategory : getAvailableCategories()) {
            if (validCategory.equals(category.toUpperCase())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get category-specific production requirements
     * @param category Product category
     * @return Production requirements description
     */
    public static String getCategoryRequirements(String category) {
        switch (category.toUpperCase()) {
            case "AUTOMOTIVE":
                return "Requires: Assembly Line, Paint Booth, Quality Testing Station, Safety Compliance";
            case "ELECTRONICS":
                return "Requires: SMT Line, Wave Soldering, ICT Testing, Burn-in Chamber, Certification";
            case "FURNITURE":
                return "Requires: CNC Router, Sanders, Spray Booth, Assembly Station, Wood Processing";
            default:
                return "Standard manufacturing requirements";
        }
    }
}