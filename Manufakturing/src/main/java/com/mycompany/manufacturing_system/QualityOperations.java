package com.mycompany.manufacturing_system;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class QualityOperations {
    private Connection connection;
    private static final DateTimeFormatter formatter = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public QualityOperations() throws SQLException {
        connection = DatabaseConnection.getConnection();
        initializeQualityTables();
    }

    /**
     * Initialize quality control database tables
     */
    private void initializeQualityTables() {
        try (Statement stmt = connection.createStatement()) {
            
            // Create quality_inspections table
            String createInspectionsTable = """
                CREATE TABLE IF NOT EXISTS quality_inspections (
                    inspection_id VARCHAR(50) PRIMARY KEY,
                    order_id VARCHAR(50) NOT NULL,
                    product_name VARCHAR(100),
                    inspection_type ENUM('INCOMING', 'IN_PROCESS', 'FINAL', 'OUTGOING', 'QUICK_CHECK') NOT NULL,
                    inspector VARCHAR(50) NOT NULL,
                    status ENUM('PASSED', 'FAILED', 'PENDING') DEFAULT 'PENDING',
                    inspection_date DATETIME NOT NULL,
                    quality_criteria TEXT,
                    defect_types TEXT,
                    defect_count INT DEFAULT 0,
                    corrective_actions TEXT,
                    notes TEXT,
                    quality_score DECIMAL(5,2) DEFAULT 0.00,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    INDEX idx_order_id (order_id),
                    INDEX idx_inspector (inspector),
                    INDEX idx_status (status),
                    INDEX idx_inspection_date (inspection_date)
                )
                """;
            stmt.execute(createInspectionsTable);
            
            // Create defect_types table
            String createDefectTypesTable = """
                CREATE TABLE IF NOT EXISTS defect_types (
                    defect_id INT AUTO_INCREMENT PRIMARY KEY,
                    defect_name VARCHAR(100) NOT NULL UNIQUE,
                    defect_category VARCHAR(50),
                    severity ENUM('CRITICAL', 'MAJOR', 'MINOR') DEFAULT 'MINOR',
                    description TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;
            stmt.execute(createDefectTypesTable);
            
            // Create quality_metrics table
            String createMetricsTable = """
                CREATE TABLE IF NOT EXISTS quality_metrics (
                    metric_id INT AUTO_INCREMENT PRIMARY KEY,
                    metric_date DATE NOT NULL,
                    product_category VARCHAR(50),
                    total_inspections INT DEFAULT 0,
                    passed_inspections INT DEFAULT 0,
                    failed_inspections INT DEFAULT 0,
                    quality_rate DECIMAL(5,2) DEFAULT 0.00,
                    defect_rate DECIMAL(5,2) DEFAULT 0.00,
                    first_pass_yield DECIMAL(5,2) DEFAULT 0.00,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_metric_date (metric_date),
                    INDEX idx_product_category (product_category)
                )
                """;
            stmt.execute(createMetricsTable);
            
            // Insert default defect types
            insertDefaultDefectTypes(stmt);
            
            System.out.println("Quality control tables initialized successfully.");
            
        } catch (SQLException e) {
            System.err.println("Failed to initialize quality tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void insertDefaultDefectTypes(Statement stmt) throws SQLException {
        String insertDefectTypes = """
            INSERT IGNORE INTO defect_types (defect_name, defect_category, severity, description) VALUES
            ('Dimensional Variance', 'DIMENSIONAL', 'MAJOR', 'Product dimensions outside tolerance'),
            ('Surface Defect', 'COSMETIC', 'MINOR', 'Scratches, dents, or surface imperfections'),
            ('Material Defect', 'MATERIAL', 'CRITICAL', 'Material quality or composition issues'),
            ('Assembly Error', 'ASSEMBLY', 'MAJOR', 'Incorrect assembly or missing components'),
            ('Functional Failure', 'FUNCTIONAL', 'CRITICAL', 'Product does not function as designed'),
            ('Color Mismatch', 'COSMETIC', 'MINOR', 'Color does not match specification'),
            ('Contamination', 'CLEANLINESS', 'MAJOR', 'Foreign material or contamination present'),
            ('Incomplete Finish', 'FINISHING', 'MINOR', 'Incomplete or poor surface finish')
            """;
        stmt.execute(insertDefectTypes);
    }

    /**
     * Create a new quality inspection
     */
    public boolean createInspection(QualityInspection inspection) {
        String query = """
            INSERT INTO quality_inspections 
            (inspection_id, order_id, product_name, inspection_type, inspector, 
             status, inspection_date, quality_criteria, defect_types, defect_count, 
             corrective_actions, notes, quality_score) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
            
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, inspection.getInspectionId());
            stmt.setString(2, inspection.getOrderId());
            stmt.setString(3, inspection.getProductName());
            stmt.setString(4, inspection.getInspectionType());
            stmt.setString(5, inspection.getInspector());
            stmt.setString(6, inspection.getStatus());
            stmt.setString(7, inspection.getInspectionDate().format(formatter));
            stmt.setString(8, inspection.getQualityCriteria());
            stmt.setString(9, inspection.getDefectTypes());
            stmt.setInt(10, inspection.getDefectCount());
            stmt.setString(11, inspection.getCorrectiveActions());
            stmt.setString(12, inspection.getNotes());
            stmt.setDouble(13, inspection.getQualityScore());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creating inspection: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update inspection status
     */
    public boolean updateInspectionStatus(String inspectionId, String status, String notes) {
        String query = """
            UPDATE quality_inspections SET 
            status = ?, 
            notes = ?,
            updated_at = CURRENT_TIMESTAMP
            WHERE inspection_id = ?
            """;
            
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, status);
            stmt.setString(2, notes);
            stmt.setString(3, inspectionId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating inspection: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all quality inspections
     */
    public List<QualityInspection> getAllInspections() {
        List<QualityInspection> inspections = new ArrayList<>();
        String query = """
            SELECT qi.*, po.product_name
            FROM quality_inspections qi
            LEFT JOIN production_orders po ON qi.order_id = po.order_id
            ORDER BY qi.inspection_date DESC
            """;
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                QualityInspection inspection = new QualityInspection(
                    rs.getString("inspection_id"),
                    rs.getString("order_id"),
                    rs.getString("inspection_type"),
                    rs.getString("inspector"),
                    rs.getString("status")
                );
                
                inspection.setProductName(rs.getString("product_name"));
                inspection.setInspectionDate(LocalDateTime.parse(rs.getString("inspection_date"), formatter));
                inspection.setQualityCriteria(rs.getString("quality_criteria"));
                inspection.setDefectTypes(rs.getString("defect_types"));
                inspection.setDefectCount(rs.getInt("defect_count"));
                inspection.setCorrectiveActions(rs.getString("corrective_actions"));
                inspection.setNotes(rs.getString("notes"));
                inspection.setQualityScore(rs.getDouble("quality_score"));
                
                inspections.add(inspection);
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving inspections: " + e.getMessage());
            e.printStackTrace();
        }
        
        return inspections;
    }

    /**
     * Get inspections by inspector
     */
    public List<QualityInspection> getInspectionsByInspector(String inspector) {
        List<QualityInspection> inspections = new ArrayList<>();
        String query = """
            SELECT qi.*, po.product_name
            FROM quality_inspections qi
            LEFT JOIN production_orders po ON qi.order_id = po.order_id
            WHERE qi.inspector = ?
            ORDER BY qi.inspection_date DESC
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, inspector);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                QualityInspection inspection = new QualityInspection(
                    rs.getString("inspection_id"),
                    rs.getString("order_id"),
                    rs.getString("inspection_type"),
                    rs.getString("inspector"),
                    rs.getString("status")
                );
                
                inspection.setProductName(rs.getString("product_name"));
                inspection.setInspectionDate(LocalDateTime.parse(rs.getString("inspection_date"), formatter));
                inspection.setQualityCriteria(rs.getString("quality_criteria"));
                inspection.setDefectTypes(rs.getString("defect_types"));
                inspection.setDefectCount(rs.getInt("defect_count"));
                inspection.setCorrectiveActions(rs.getString("corrective_actions"));
                inspection.setNotes(rs.getString("notes"));
                inspection.setQualityScore(rs.getDouble("quality_score"));
                
                inspections.add(inspection);
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving inspections by inspector: " + e.getMessage());
            e.printStackTrace();
        }
        
        return inspections;
    }

    /**
     * Get quality metrics for a date range
     */
    public List<QualityMetric> getQualityMetrics(LocalDateTime startDate, LocalDateTime endDate) {
        List<QualityMetric> metrics = new ArrayList<>();
        String query = """
            SELECT 
                DATE(inspection_date) as metric_date,
                COUNT(*) as total_inspections,
                SUM(CASE WHEN status = 'PASSED' THEN 1 ELSE 0 END) as passed_inspections,
                SUM(CASE WHEN status = 'FAILED' THEN 1 ELSE 0 END) as failed_inspections,
                ROUND(AVG(CASE WHEN status = 'PASSED' THEN 100 ELSE 0 END), 2) as quality_rate
            FROM quality_inspections
            WHERE inspection_date BETWEEN ? AND ?
            GROUP BY DATE(inspection_date)
            ORDER BY metric_date DESC
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, startDate.format(formatter));
            stmt.setString(2, endDate.format(formatter));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                QualityMetric metric = new QualityMetric();
                metric.setMetricDate(rs.getDate("metric_date").toLocalDate());
                metric.setTotalInspections(rs.getInt("total_inspections"));
                metric.setPassedInspections(rs.getInt("passed_inspections"));
                metric.setFailedInspections(rs.getInt("failed_inspections"));
                metric.setQualityRate(rs.getDouble("quality_rate"));
                
                metrics.add(metric);
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving quality metrics: " + e.getMessage());
            e.printStackTrace();
        }
        
        return metrics;
    }

    /**
     * Record defect for an inspection
     */
    public boolean recordDefect(String inspectionId, String defectType, int count, String description) {
        String query = """
            UPDATE quality_inspections SET 
            defect_types = CONCAT(IFNULL(defect_types, ''), ?, '; '),
            defect_count = defect_count + ?,
            notes = CONCAT(IFNULL(notes, ''), 'Defect: ', ?, ' (Count: ', ?, ') - ', ?, '\n'),
            status = 'FAILED',
            updated_at = CURRENT_TIMESTAMP
            WHERE inspection_id = ?
            """;
            
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, defectType);
            stmt.setInt(2, count);
            stmt.setString(3, defectType);
            stmt.setInt(4, count);
            stmt.setString(5, description);
            stmt.setString(6, inspectionId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error recording defect: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get available defect types
     */
    public List<DefectType> getDefectTypes() {
        List<DefectType> defectTypes = new ArrayList<>();
        String query = "SELECT * FROM defect_types ORDER BY defect_category, defect_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                DefectType defectType = new DefectType();
                defectType.setDefectId(rs.getInt("defect_id"));
                defectType.setDefectName(rs.getString("defect_name"));
                defectType.setDefectCategory(rs.getString("defect_category"));
                defectType.setSeverity(rs.getString("severity"));
                defectType.setDescription(rs.getString("description"));
                
                defectTypes.add(defectType);
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving defect types: " + e.getMessage());
            e.printStackTrace();
        }
        
        return defectTypes;
    }

    /**
     * Calculate first pass yield for a period
     */
    public double calculateFirstPassYield(LocalDateTime startDate, LocalDateTime endDate) {
        String query = """
            SELECT 
                COUNT(*) as total_orders,
                SUM(CASE WHEN first_inspection.status = 'PASSED' THEN 1 ELSE 0 END) as first_pass_count
            FROM (
                SELECT order_id, MIN(inspection_date) as first_inspection_date
                FROM quality_inspections
                WHERE inspection_date BETWEEN ? AND ?
                GROUP BY order_id
            ) first_orders
            JOIN quality_inspections first_inspection ON 
                first_orders.order_id = first_inspection.order_id AND 
                first_orders.first_inspection_date = first_inspection.inspection_date
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, startDate.format(formatter));
            stmt.setString(2, endDate.format(formatter));
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int total = rs.getInt("total_orders");
                int firstPass = rs.getInt("first_pass_count");
                
                if (total > 0) {
                    return ((double) firstPass / total) * 100.0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error calculating first pass yield: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0.0;
    }

    /**
     * Get inspection by ID
     */
    public QualityInspection getInspectionById(String inspectionId) {
        String query = """
            SELECT qi.*, po.product_name
            FROM quality_inspections qi
            LEFT JOIN production_orders po ON qi.order_id = po.order_id
            WHERE qi.inspection_id = ?
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, inspectionId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                QualityInspection inspection = new QualityInspection(
                    rs.getString("inspection_id"),
                    rs.getString("order_id"),
                    rs.getString("inspection_type"),
                    rs.getString("inspector"),
                    rs.getString("status")
                );
                
                inspection.setProductName(rs.getString("product_name"));
                inspection.setInspectionDate(LocalDateTime.parse(rs.getString("inspection_date"), formatter));
                inspection.setQualityCriteria(rs.getString("quality_criteria"));
                inspection.setDefectTypes(rs.getString("defect_types"));
                inspection.setDefectCount(rs.getInt("defect_count"));
                inspection.setCorrectiveActions(rs.getString("corrective_actions"));
                inspection.setNotes(rs.getString("notes"));
                inspection.setQualityScore(rs.getDouble("quality_score"));
                
                return inspection;
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving inspection by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Delete inspection
     */
    public boolean deleteInspection(String inspectionId) {
        String query = "DELETE FROM quality_inspections WHERE inspection_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, inspectionId);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting inspection: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get pending inspections for an order
     */
    public boolean hasPendingInspections(String orderId) {
        String query = "SELECT COUNT(*) FROM quality_inspections WHERE order_id = ? AND status = 'PENDING'";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, orderId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking pending inspections: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Get inspections by status
     */
    public List<QualityInspection> getInspectionsByStatus(String status) {
        List<QualityInspection> inspections = new ArrayList<>();
        String query = """
            SELECT qi.*, po.product_name
            FROM quality_inspections qi
            LEFT JOIN production_orders po ON qi.order_id = po.order_id
            WHERE qi.status = ?
            ORDER BY qi.inspection_date DESC
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                QualityInspection inspection = new QualityInspection(
                    rs.getString("inspection_id"),
                    rs.getString("order_id"),
                    rs.getString("inspection_type"),
                    rs.getString("inspector"),
                    rs.getString("status")
                );
                
                inspection.setProductName(rs.getString("product_name"));
                inspection.setInspectionDate(LocalDateTime.parse(rs.getString("inspection_date"), formatter));
                inspection.setQualityCriteria(rs.getString("quality_criteria"));
                inspection.setDefectTypes(rs.getString("defect_types"));
                inspection.setDefectCount(rs.getInt("defect_count"));
                inspection.setCorrectiveActions(rs.getString("corrective_actions"));
                inspection.setNotes(rs.getString("notes"));
                inspection.setQualityScore(rs.getDouble("quality_score"));
                
                inspections.add(inspection);
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving inspections by status: " + e.getMessage());
            e.printStackTrace();
        }
        
        return inspections;
    }

    /**
     * Get quality statistics
     */
    public QualityStatistics getQualityStatistics() {
        QualityStatistics stats = new QualityStatistics();
        
        try (Statement stmt = connection.createStatement()) {
            // Total inspections
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM quality_inspections");
            if (rs.next()) {
                stats.setTotalInspections(rs.getInt("total"));
            }
            
            // Inspections by status
            rs = stmt.executeQuery("""
                SELECT status, COUNT(*) as count 
                FROM quality_inspections 
                GROUP BY status
                """);
            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("count");
                switch (status) {
                    case "PASSED": stats.setPassedInspections(count); break;
                    case "FAILED": stats.setFailedInspections(count); break;
                    case "PENDING": stats.setPendingInspections(count); break;
                }
            }
            
            // Calculate quality rate
            stats.calculateQualityRate();
            
        } catch (SQLException e) {
            System.err.println("Error getting quality statistics: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stats;
    }

    /**
     * Get inspections for a specific order
     */
    public List<QualityInspection> getInspectionsByOrder(String orderId) {
        List<QualityInspection> inspections = new ArrayList<>();
        String query = """
            SELECT qi.*, po.product_name
            FROM quality_inspections qi
            LEFT JOIN production_orders po ON qi.order_id = po.order_id
            WHERE qi.order_id = ?
            ORDER BY qi.inspection_date DESC
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, orderId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                QualityInspection inspection = new QualityInspection(
                    rs.getString("inspection_id"),
                    rs.getString("order_id"),
                    rs.getString("inspection_type"),
                    rs.getString("inspector"),
                    rs.getString("status")
                );
                
                inspection.setProductName(rs.getString("product_name"));
                inspection.setInspectionDate(LocalDateTime.parse(rs.getString("inspection_date"), formatter));
                inspection.setQualityCriteria(rs.getString("quality_criteria"));
                inspection.setDefectTypes(rs.getString("defect_types"));
                inspection.setDefectCount(rs.getInt("defect_count"));
                inspection.setCorrectiveActions(rs.getString("corrective_actions"));
                inspection.setNotes(rs.getString("notes"));
                inspection.setQualityScore(rs.getDouble("quality_score"));
                
                inspections.add(inspection);
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving inspections by order: " + e.getMessage());
            e.printStackTrace();
        }
        
        return inspections;
    }
}

/**
 * Quality Metric helper class
 */
class QualityMetric {
    private java.time.LocalDate metricDate;
    private int totalInspections;
    private int passedInspections;
    private int failedInspections;
    private double qualityRate;
    private double defectRate;
    private double firstPassYield;

    // Getters and Setters
    public java.time.LocalDate getMetricDate() { return metricDate; }
    public void setMetricDate(java.time.LocalDate metricDate) { this.metricDate = metricDate; }

    public int getTotalInspections() { return totalInspections; }
    public void setTotalInspections(int totalInspections) { this.totalInspections = totalInspections; }

    public int getPassedInspections() { return passedInspections; }
    public void setPassedInspections(int passedInspections) { this.passedInspections = passedInspections; }

    public int getFailedInspections() { return failedInspections; }
    public void setFailedInspections(int failedInspections) { this.failedInspections = failedInspections; }

    public double getQualityRate() { return qualityRate; }
    public void setQualityRate(double qualityRate) { this.qualityRate = qualityRate; }

    public double getDefectRate() { return defectRate; }
    public void setDefectRate(double defectRate) { this.defectRate = defectRate; }

    public double getFirstPassYield() { return firstPassYield; }
    public void setFirstPassYield(double firstPassYield) { this.firstPassYield = firstPassYield; }
}

/**
 * Defect Type helper class
 */
class DefectType {
    private int defectId;
    private String defectName;
    private String defectCategory;
    private String severity;
    private String description;

    // Getters and Setters
    public int getDefectId() { return defectId; }
    public void setDefectId(int defectId) { this.defectId = defectId; }

    public String getDefectName() { return defectName; }
    public void setDefectName(String defectName) { this.defectName = defectName; }

    public String getDefectCategory() { return defectCategory; }
    public void setDefectCategory(String defectCategory) { this.defectCategory = defectCategory; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return defectName + " (" + severity + ")";
    }
}

/**
 * Quality Statistics helper class
 */
class QualityStatistics {
    private int totalInspections;
    private int passedInspections;
    private int failedInspections;
    private int pendingInspections;
    private double qualityRate;

    // Getters and Setters
    public int getTotalInspections() { return totalInspections; }
    public void setTotalInspections(int totalInspections) { this.totalInspections = totalInspections; }

    public int getPassedInspections() { return passedInspections; }
    public void setPassedInspections(int passedInspections) { this.passedInspections = passedInspections; }

    public int getFailedInspections() { return failedInspections; }
    public void setFailedInspections(int failedInspections) { this.failedInspections = failedInspections; }

    public int getPendingInspections() { return pendingInspections; }
    public void setPendingInspections(int pendingInspections) { this.pendingInspections = pendingInspections; }

    public double getQualityRate() { return qualityRate; }
    public void setQualityRate(double qualityRate) { this.qualityRate = qualityRate; }

    public void calculateQualityRate() {
        if (totalInspections > 0) {
            this.qualityRate = ((double) passedInspections / totalInspections) * 100.0;
        } else {
            this.qualityRate = 0.0;
        }
    }
}