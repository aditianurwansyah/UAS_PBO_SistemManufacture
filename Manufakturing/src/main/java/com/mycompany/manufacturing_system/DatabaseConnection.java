package com.mycompany.manufacturing_system;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Enhanced Database connection class with connection pooling
 */
public class DatabaseConnection {
    // Menggunakan MySQL Database
    private static final String URL = "jdbc:mysql://localhost:3307/manufacturing_system?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root"; 
    private static final String PASSWORD = ""; // Sesuaikan dengan password MySQL Anda

    // Connection pool
    private static final ConcurrentLinkedQueue<Connection> connectionPool = new ConcurrentLinkedQueue<>();
    private static final AtomicInteger connectionCount = new AtomicInteger(0);
    private static final int MAX_CONNECTIONS = 10;
    private static final int MIN_CONNECTIONS = 3;

    static {
        try {
            // Menggunakan driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            initializeConnectionPool();
            initializeDatabase();
            System.out.println("Manufacturing database connection pool initialized successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found. Please ensure MySQL JDBC driver is in your classpath.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Failed to initialize database connection pool.");
            e.printStackTrace();
        }
    }

    private static void initializeConnectionPool() throws SQLException {
        for (int i = 0; i < MIN_CONNECTIONS; i++) {
            connectionPool.offer(createNewConnection());
        }
    }

    private static Connection createNewConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
        connectionCount.incrementAndGet();
        return conn;
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = connectionPool.poll();

        if (conn == null || conn.isClosed()) {
            if (connectionCount.get() < MAX_CONNECTIONS) {
                conn = createNewConnection();
            } else {
                // Wait for available connection
                for (int i = 0; i < 10; i++) {
                    try {
                        Thread.sleep(100);
                        conn = connectionPool.poll();
                        if (conn != null && !conn.isClosed()) {
                            break;
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new SQLException("Interrupted while waiting for connection");
                    }
                }
                if (conn == null || conn.isClosed()) {
                    throw new SQLException("No available connections in pool");
                }
            }
        }

        return conn;
    }

    public static void releaseConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    connectionPool.offer(conn);
                } else {
                    connectionCount.decrementAndGet();
                }
            } catch (SQLException e) {
                connectionCount.decrementAndGet();
                System.err.println("Error releasing connection: " + e.getMessage());
            }
        }
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public static void closeAllConnections() {
        while (!connectionPool.isEmpty()) {
            Connection conn = connectionPool.poll();
            if (conn != null) {
                try {
                    conn.close();
                    connectionCount.decrementAndGet();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
        System.out.println("All database connections closed.");
    }

    private static void initializeDatabase() throws SQLException {
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();

            // Create database if not exists
            // MySQL does not allow CREATE DATABASE and USE in the same statement block like H2
            // You might need to create the database manually or use a separate connection for CREATE DATABASE
            // For simplicity, we assume 'manufacturing_system' database already exists.
            stmt.execute("CREATE DATABASE IF NOT EXISTS manufacturing_system");
            stmt.execute("USE manufacturing_system");

            // Create roles table
            String createRolesTable = """
                CREATE TABLE IF NOT EXISTS roles (
                    role_id INT AUTO_INCREMENT PRIMARY KEY,
                    role_name VARCHAR(50) NOT NULL UNIQUE,
                    role_description VARCHAR(255),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;
            stmt.execute(createRolesTable);

            // Insert default roles
            String insertRoles = """
                INSERT INTO roles (role_name, role_description) VALUES
                ('ADMIN', 'System Administrator with full access'),
                ('USER', 'Regular user with limited access')
                ON DUPLICATE KEY UPDATE role_description = VALUES(role_description);
                """;
            stmt.execute(insertRoles);

            // Create users table - Added password_hash, failed_login_attempts, account_locked_until, password_changed_at, phone
            String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    user_id INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(50) NOT NULL UNIQUE,
                    password_hash VARCHAR(255) NOT NULL,
                    role_id INT NOT NULL,
                    full_name VARCHAR(100),
                    email VARCHAR(100),
                    department VARCHAR(50),
                    phone VARCHAR(20),
                    is_active BOOLEAN DEFAULT TRUE,
                    last_login TIMESTAMP NULL,
                    failed_login_attempts INT DEFAULT 0,
                    account_locked_until TIMESTAMP NULL,
                    password_changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    FOREIGN KEY (role_id) REFERENCES roles(role_id),
                    INDEX idx_username (username),
                    INDEX idx_active (is_active)
                )
                """;
            stmt.execute(createUsersTable);

            // Create audit_log table (for security events)
            String createAuditLogTable = """
                CREATE TABLE IF NOT EXISTS audit_log (
                    log_id INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(50) NOT NULL,
                    action VARCHAR(100) NOT NULL,
                    status VARCHAR(50) NOT NULL,
                    description TEXT,
                    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;
            stmt.execute(createAuditLogTable);

            // Create user_activity_log table
            String createUserActivityLogTable = """
                CREATE TABLE IF NOT EXISTS user_activity_log (
                    log_id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT NOT NULL,
                    activity VARCHAR(50) NOT NULL,
                    description TEXT,
                    ip_address VARCHAR(45),
                    user_agent TEXT,
                    session_id VARCHAR(100),
                    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(user_id),
                    INDEX idx_user_id (user_id),
                    INDEX idx_timestamp (timestamp)
                )
                """;
            stmt.execute(createUserActivityLogTable);

            insertDefaultUsers(stmt);

            createProductionTables(stmt);
            createReportingTables(stmt);
            createProductionLines(stmt);
            insertSampleData(stmt);

            releaseConnection(conn);
            System.out.println("Manufacturing database initialized successfully.");

        } catch (SQLException e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            throw e;
        }
    }

    private static void insertDefaultUsers(Statement stmt) throws SQLException {
        // These passwords are not hashed here. They should be hashed by the application before insertion.
        // For quick setup, they are inserted as plain text. LoginHandler expects hashed passwords.
        String insertDefaultAdmin = """
            INSERT INTO users (username, password, role_id, full_name, department, is_active, failed_login_attempts)
            SELECT 'admin', 'admin123', r.role_id, 'System Administrator', 'IT', TRUE, 0
            FROM roles r WHERE r.role_name = 'ADMIN'
            ON DUPLICATE KEY UPDATE password = VALUES(password), full_name = VALUES(full_name), department = VALUES(department), is_active = VALUES(is_active), failed_login_attempts = VALUES(failed_login_attempts);
            """;
        stmt.execute(insertDefaultAdmin);

         String insertUsers = """
            INSERT IGNORE INTO users (user_id, username, password, role_id, full_name, email, department, is_active, last_login, failed_login_attempts, account_locked) VALUES
            (1, 'admin', 'b5bb9c1f24e65082a7a51352e23543d780775a2a', (SELECT role_id FROM roles WHERE role_name = 'ADMIN'), 'Super Admin', 'admin@example.com', 'IT', TRUE, NOW(), 0, FALSE),
            (2, 'user', 'ee11cbb19052e40b07aac0ca060c23ee', (SELECT role_id FROM roles WHERE role_name = 'USER'), 'Regular User', 'user@example.com', 'Production', TRUE, NOW(), 0, FALSE),
            (3, 'supervisor', '3780775a2a', (SELECT role_id FROM roles WHERE role_name = 'SUPERVISOR'), 'Production Supervisor', 'supervisor@example.com', 'Production', TRUE, NOW(), 0, FALSE),
            (4, 'operator', 'a7a51352e23543d780775a2a', (SELECT role_id FROM roles WHERE role_name = 'OPERATOR'), 'Machine Operator', 'operator@example.com', 'Production', TRUE, NOW(), 0, FALSE)
            ON DUPLICATE KEY UPDATE username = VALUES(username), password = VALUES(password);
            """;
        stmt.execute(insertUsers);
    }

    // Pastikan metode createProductionTables, createReportingTables, createProductionLines, insertSampleData ada di sini
    private static void createProductionTables(Statement stmt) throws SQLException {
        // Products table
        String createProductsTable = """
            CREATE TABLE IF NOT EXISTS products (
                product_id VARCHAR(50) PRIMARY KEY,
                product_name VARCHAR(100) NOT NULL,
                category VARCHAR(50) NOT NULL,
                unit_cost DECIMAL(10,2) NOT NULL,
                specifications TEXT,
                status VARCHAR(50) DEFAULT 'ACTIVE',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
            """;
        stmt.execute(createProductsTable);

        // Production orders table
        String createOrdersTable = """
            CREATE TABLE IF NOT EXISTS production_orders (
                order_id VARCHAR(50) PRIMARY KEY,
                customer_name VARCHAR(100) NOT NULL,
                product_id VARCHAR(50) NOT NULL,
                product_name VARCHAR(100) NOT NULL,
                quantity INT NOT NULL,
                status VARCHAR(50) DEFAULT 'PENDING',
                priority VARCHAR(50) DEFAULT 'MEDIUM',
                order_date TIMESTAMP NOT NULL,
                due_date TIMESTAMP,
                start_date TIMESTAMP,
                completion_date TIMESTAMP,
                assigned_line VARCHAR(50),
                assigned_operator VARCHAR(50),
                assigned_user_id INT,
                total_cost DECIMAL(12,2) DEFAULT 0.00,
                completion_percentage DECIMAL(5,2) DEFAULT 0.00,
                notes TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                FOREIGN KEY (assigned_user_id) REFERENCES users(user_id)
            )
            """;
        stmt.execute(createOrdersTable);
    }

    private static void createReportingTables(Statement stmt) throws SQLException {
        // Production reports table
        String createReportsTable = """
            CREATE TABLE IF NOT EXISTS production_reports (
                report_id INT AUTO_INCREMENT PRIMARY KEY,
                order_id VARCHAR(50) NOT NULL,
                production_date DATE NOT NULL,
                product_category VARCHAR(50) NOT NULL,
                product_name VARCHAR(100) NOT NULL,
                quantity_produced INT NOT NULL,
                production_line VARCHAR(50),
                operator_name VARCHAR(50),
                shift VARCHAR(50) DEFAULT 'MORNING',
                quality_passed INT DEFAULT 0,
                quality_failed INT DEFAULT 0,
                downtime_minutes INT DEFAULT 0,
                total_cost DECIMAL(12,2) DEFAULT 0.00,
                efficiency_percentage DECIMAL(5,2) DEFAULT 0.00,
                notes TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (order_id) REFERENCES production_orders(order_id)
            )
            """;
        stmt.execute(createReportsTable);
    }

    private static void createProductionLines(Statement stmt) throws SQLException {
        String createProductionLinesTable = """
            CREATE TABLE IF NOT EXISTS production_lines (
                line_id INT AUTO_INCREMENT PRIMARY KEY,
                line_name VARCHAR(100) NOT NULL UNIQUE,
                line_type VARCHAR(50) NOT NULL,
                capacity_per_hour INT DEFAULT 10,
                status VARCHAR(50) DEFAULT 'ACTIVE',
                location VARCHAR(100),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;
        stmt.execute(createProductionLinesTable);

        // Insert default production lines
        String insertProductionLines = """
            INSERT INTO production_lines (line_name, line_type, capacity_per_hour, location) VALUES
            ('Assembly Line A', 'ASSEMBLY', 15, 'Building A - Floor 1'),
            ('Assembly Line B', 'ASSEMBLY', 12, 'Building A - Floor 1'),
            ('Electronics Line 1', 'ELECTRONICS', 20, 'Building B - Floor 2'),
            ('Electronics Line 2', 'ELECTRONICS', 18, 'Building B - Floor 2'),
            ('Automotive Line 1', 'AUTOMOTIVE', 8, 'Building C - Floor 1'),
            ('Furniture Workshop', 'FURNITURE', 5, 'Building D - Floor 1'),
            ('Quality Control Station', 'QUALITY_CONTROL', 25, 'Building A - Floor 2')
            ON DUPLICATE KEY UPDATE line_type = VALUES(line_type), capacity_per_hour = VALUES(capacity_per_hour);
            """;
        stmt.execute(insertProductionLines);
    }

    private static void insertSampleData(Statement stmt) throws SQLException {
        // Insert sample products
        String insertSampleProducts = """
            INSERT INTO products (product_id, product_name, category, unit_cost, specifications) VALUES
            ('AUTO-001', 'Sedan Car Frame', 'AUTOMOTIVE', 25000.00, 'Steel frame for 4-door sedan vehicles'),
            ('AUTO-002', 'Engine Assembly V6', 'AUTOMOTIVE', 15000.00, '3.6L V6 engine with turbocharger'),
            ('ELEC-001', 'Smart Phone PCB', 'ELECTRONICS', 150.00, 'Main circuit board for smartphones'),
            ('ELEC-002', 'LED Display Panel', 'ELECTRONICS', 300.00, '15.6 inch Full HD LED display'),
            ('FURN-001', 'Office Chair', 'FURNITURE', 200.00, 'Ergonomic office chair with lumbar support'),
            ('FURN-002', 'Conference Table', 'FURNITURE', 800.00, '8-person conference table, solid wood')
            ON DUPLICATE KEY UPDATE product_name = VALUES(product_name), category = VALUES(category), unit_cost = VALUES(unit_cost);
            """;
        stmt.execute(insertSampleProducts);
    }

    static void closeConnection() {
        // Implement proper connection closing if needed, or rely on pool shutdown
        System.out.println("Individual connection close not supported directly. Use closeAllConnections() for pool shutdown.");
    }
}