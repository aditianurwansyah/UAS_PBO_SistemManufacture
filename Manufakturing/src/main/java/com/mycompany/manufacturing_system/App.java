package com.mycompany.manufacturing_system;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.util.logging.Level;

/**
 * Enhanced Main Application class for Manufacturing Management System
 * Improved error handling, logging, and application lifecycle management
 */
public class App extends Application {
    
    private static final Logger logger = Logger.getLogger(App.class.getName());
    private static final String APP_NAME = "Manufacturing Management System";
    private static final String APP_VERSION = "2.0";
    private Stage primaryStage;
    private boolean shutdownInProgress = false;
    
    static {
        initializeLogging();
    }
    
    /**
     * Initialize application logging
     */
    private static void initializeLogging() {
        try {
            // Create log file handler
            FileHandler fileHandler = new FileHandler("manufacturing_system.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            
            // Configure logger
            logger.addHandler(fileHandler);
            logger.setLevel(Level.INFO);
            logger.setUseParentHandlers(true); // Also log to console
            
            logger.info("Application logging initialized");
            
        } catch (Exception e) {
            System.err.println("Failed to initialize logging: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void init() throws Exception {
        super.init();
        logger.info("Initializing " + APP_NAME + " v" + APP_VERSION + "...");
        
        try {
            // Pre-initialize database connection with retry logic
            if (!initializeDatabaseWithRetry()) {
                throw new Exception("Failed to initialize database connection after multiple attempts");
            }
            
            logger.info("Database connection initialized successfully");
            
        } catch (Exception e) {
            logger.severe("Failed to initialize database connection: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Initialize database connection with retry logic
     */
    private boolean initializeDatabaseWithRetry() {
        int maxRetries = 3;
        int retryDelay = 2000; // 2 seconds
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                logger.info("Database connection attempt " + attempt + " of " + maxRetries);
                
                // Test database connection
                if (DatabaseConnection.testConnection()) {
                    logger.info("Database connection successful on attempt " + attempt);
                    return true;
                }
                
            } catch (Exception e) {
                logger.warning("Database connection attempt " + attempt + " failed: " + e.getMessage());
                
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        logger.severe("Database initialization interrupted");
                        return false;
                    }
                }
            }
        }
        
        logger.severe("Failed to establish database connection after " + maxRetries + " attempts");
        return false;
    }
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        try {
            logger.info("Starting " + APP_NAME + " application...");
            
            // Set application properties
            configureStage(primaryStage);
            
            // Verify database connection before proceeding
            if (!DatabaseConnection.testConnection()) {
                showDatabaseError(primaryStage);
                return;
            }
            
            // Create and show login view
            showLoginView(primaryStage);
            
            // Configure application shutdown handler
            configureShutdownHandler(primaryStage);
            
            // Show the stage
            primaryStage.show();
            
            // Center the stage on screen
            centerStageOnScreen(primaryStage);
            
            logger.info(APP_NAME + " started successfully");
            logSystemInfo();
            
        } catch (Exception e) {
            logger.severe("Failed to start application: " + e.getMessage());
            e.printStackTrace();
            showStartupError(primaryStage, e.getMessage());
        }
    }
    
    /**
     * Configure the primary stage
     */
    private void configureStage(Stage stage) {
        stage.setTitle(APP_NAME + " - Login");
        stage.setResizable(true);
        stage.setMinWidth(500);
        stage.setMinHeight(400);
        stage.setWidth(800);
        stage.setHeight(600);
        
        // Set application icon
        loadApplicationIcon(stage);
        
        // Prevent stage from being too small
        stage.minWidthProperty().bind(stage.widthProperty().multiply(0.5));
        stage.minHeightProperty().bind(stage.heightProperty().multiply(0.5));
    }
    
    /**
     * Load application icon safely
     */
    private void loadApplicationIcon(Stage stage) {
        try {
            InputStream iconStream = getClass().getResourceAsStream("/icon.png");
            if (iconStream != null) {
                Image icon = new Image(iconStream);
                stage.getIcons().add(icon);
                logger.info("Application icon loaded successfully");
            } else {
                // Try alternative icon paths
                String[] iconPaths = {"/images/icon.png", "/assets/icon.png", "/favicon.ico"};
                boolean iconLoaded = false;
                
                for (String path : iconPaths) {
                    try {
                        InputStream altIconStream = getClass().getResourceAsStream(path);
                        if (altIconStream != null) {
                            Image icon = new Image(altIconStream);
                            stage.getIcons().add(icon);
                            logger.info("Application icon loaded from: " + path);
                            iconLoaded = true;
                            break;
                        }
                    } catch (Exception e) {
                        // Continue trying other paths
                    }
                }
                
                if (!iconLoaded) {
                    logger.warning("Application icon not found, using default system icon");
                }
            }
        } catch (Exception e) {
            logger.warning("Failed to load application icon: " + e.getMessage());
        }
    }
    
    /**
     * Create and show login view
     */
    private void showLoginView(Stage stage) {
        try {
            LoginView loginView = new LoginView(stage);
            Scene loginScene = new Scene(loginView.getView(), 500, 400);
            
            // Apply CSS styling
            loadStylesheet(loginScene);
            
            stage.setScene(loginScene);
            
        } catch (Exception e) {
            logger.severe("Failed to create login view: " + e.getMessage());
            throw new RuntimeException("Failed to initialize login interface", e);
        }
    }
    
    /**
     * Load CSS stylesheet safely
     */
    private void loadStylesheet(Scene scene) {
        try {
            String[] stylesheetPaths = {
                "/styles.css",
                "/css/styles.css", 
                "/assets/styles.css",
                "/resources/styles.css"
            };
            
            boolean stylesheetLoaded = false;
            
            for (String path : stylesheetPaths) {
                try {
                    InputStream styleStream = getClass().getResourceAsStream(path);
                    if (styleStream != null) {
                        String styleUrl = getClass().getResource(path).toExternalForm();
                        scene.getStylesheets().add(styleUrl);
                        logger.info("Stylesheet loaded from: " + path);
                        stylesheetLoaded = true;
                        break;
                    }
                } catch (Exception e) {
                    // Continue trying other paths
                }
            }
            
            if (!stylesheetLoaded) {
                logger.info("No custom stylesheet found, using default styling");
            }
            
        } catch (Exception e) {
            logger.warning("Failed to load stylesheet: " + e.getMessage());
        }
    }
    
    /**
     * Configure application shutdown handler
     */
    private void configureShutdownHandler(Stage stage) {
        stage.setOnCloseRequest(event -> {
            if (!shutdownInProgress) {
                event.consume(); // Prevent default close
                handleApplicationShutdown();
            }
        });
        
        // Handle platform exit
        Platform.setImplicitExit(false);
        
        // Add JVM shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!shutdownInProgress) {
                logger.info("JVM shutdown hook triggered");
                performShutdownCleanup();
            }
        }));
    }
    
    /**
     * Handle application shutdown with user confirmation
     */
    private void handleApplicationShutdown() {
        try {
            logger.info("Application shutdown requested");
            
            // Show confirmation dialog
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Exit");
            confirmAlert.setHeaderText("Exit " + APP_NAME);
            confirmAlert.setContentText("Are you sure you want to exit the application?\n\nAny unsaved work will be lost.");
            
            // Add custom buttons
            ButtonType exitButton = new ButtonType("Exit");
            ButtonType cancelButton = new ButtonType("Cancel");
            confirmAlert.getButtonTypes().setAll(exitButton, cancelButton);
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            
            if (result.isPresent() && result.get() == exitButton) {
                logger.info("User confirmed application exit");
                performApplicationShutdown();
            } else {
                logger.info("Application exit cancelled by user");
            }
            
        } catch (Exception e) {
            logger.severe("Error during shutdown confirmation: " + e.getMessage());
            // Force shutdown if dialog fails
            performApplicationShutdown();
        }
    }
    
    /**
     * Perform actual application shutdown
     */
    private void performApplicationShutdown() {
        if (shutdownInProgress) {
            return;
        }
        
        shutdownInProgress = true;
        logger.info("Performing application shutdown...");
        
        try {
            // Perform cleanup operations
            performShutdownCleanup();
            
            // Close primary stage
            if (primaryStage != null) {
                primaryStage.close();
            }
            
            // Exit platform
            Platform.exit();
            
            logger.info("Application shutdown completed successfully");
            
        } catch (Exception e) {
            logger.severe("Error during application shutdown: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Force exit if normal shutdown fails
            System.exit(0);
        }
    }
    
    /**
     * Perform cleanup operations
     */
    private void performShutdownCleanup() {
        try {
            logger.info("Performing shutdown cleanup...");
            
            // Close database connections
            DatabaseConnection.closeConnection();
            logger.info("Database connections closed");
            
            // Clear current user session
            LoginView.clearCurrentUser();
            logger.info("User session cleared");
            
            // Additional cleanup can be added here
            // - Save application state
            // - Close file handles
            // - Clean temporary files
            
            logger.info("Shutdown cleanup completed");
            
        } catch (Exception e) {
            logger.warning("Error during shutdown cleanup: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Center stage on screen
     */
    private void centerStageOnScreen(Stage stage) {
        try {
            // Get screen bounds
            javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
            javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();
            
            // Center the stage
            stage.setX((bounds.getWidth() - stage.getWidth()) / 2);
            stage.setY((bounds.getHeight() - stage.getHeight()) / 2);
            
        } catch (Exception e) {
            logger.warning("Failed to center stage on screen: " + e.getMessage());
        }
    }
    
    /**
     * Log system information
     */
    private void logSystemInfo() {
        try {
            logger.info("=== System Information ===");
            logger.info("Application: " + APP_NAME + " v" + APP_VERSION);
            logger.info("Java Version: " + System.getProperty("java.version"));
            logger.info("JavaFX Version: " + System.getProperty("javafx.version", "Unknown"));
            logger.info("Operating System: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
            logger.info("Architecture: " + System.getProperty("os.arch"));
            logger.info("User: " + System.getProperty("user.name"));
            logger.info("Working Directory: " + System.getProperty("user.dir"));
            logger.info("Available Processors: " + Runtime.getRuntime().availableProcessors());
            logger.info("Max Memory: " + (Runtime.getRuntime().maxMemory() / 1024 / 1024) + " MB");
            logger.info("Free Memory: " + (Runtime.getRuntime().freeMemory() / 1024 / 1024) + " MB");
            logger.info("Database Connection: " + (DatabaseConnection.testConnection() ? "OK" : "Failed"));
            logger.info("=== End System Information ===");
            
        } catch (Exception e) {
            logger.warning("Failed to log system information: " + e.getMessage());
        }
    }
    
    /**
     * Show database connection error dialog
     */
    private void showDatabaseError(Stage primaryStage) {
        logger.severe("Database connection failed, showing error dialog");
        
        try {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Connection Error");
            alert.setHeaderText("Failed to connect to manufacturing database");
            alert.setContentText(
                "Cannot establish connection to the manufacturing database.\n\n" +
                "Please verify the following:\n" +
                "• MySQL server is running and accessible\n" +
                "• Database 'manufacturing_system' exists\n" +
                "• Connection credentials are correct\n" +
                "• Network connectivity is available\n" +
                "• Firewall settings allow database access\n\n" +
                "Check the application log file for detailed error information.\n" +
                "Contact your system administrator if the problem persists."
            );
            
            // Add retry and exit buttons
            ButtonType retryButton = new ButtonType("Retry Connection");
            ButtonType exitButton = new ButtonType("Exit Application");
            alert.getButtonTypes().setAll(retryButton, exitButton);
            
            Optional<ButtonType> result = alert.showAndWait();
            
            if (result.isPresent() && result.get() == retryButton) {
                logger.info("User requested database connection retry");
                
                // Retry database connection
                if (initializeDatabaseWithRetry()) {
                    logger.info("Database connection retry successful");
                    showLoginView(primaryStage);
                } else {
                    logger.severe("Database connection retry failed");
                    showDatabaseError(primaryStage); // Show error again
                }
            } else {
                logger.info("User chose to exit due to database error");
                performApplicationShutdown();
            }
            
        } catch (Exception e) {
            logger.severe("Failed to show database error dialog: " + e.getMessage());
            performApplicationShutdown();
        }
    }
    
    /**
     * Show general startup error dialog
     */
    private void showStartupError(Stage primaryStage, String errorMessage) {
        logger.severe("Startup error occurred: " + errorMessage);
        
        try {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Application Startup Error");
            alert.setHeaderText("Failed to start " + APP_NAME);
            alert.setContentText(
                "An error occurred while starting the application:\n\n" +
                errorMessage + "\n\n" +
                "Please check the application log file for detailed information.\n" +
                "If the problem persists, contact technical support."
            );
            
            alert.showAndWait();
            
        } catch (Exception e) {
            logger.severe("Failed to show startup error dialog: " + e.getMessage());
            System.err.println("Critical startup error: " + errorMessage);
        } finally {
            performApplicationShutdown();
        }
    }
    
    /**
     * Application entry point with enhanced error handling
     */
    public static void main(String[] args) {
        try {
            logger.info("Starting " + APP_NAME + " v" + APP_VERSION + "...");
            
            // Validate Java version
            validateJavaVersion();
            
            // Set system properties for better JavaFX performance
            setSystemProperties();
            
            // Check for required libraries
            checkRequiredLibraries();
            
            // Launch JavaFX application
            logger.info("Launching JavaFX application...");
            launch(args);
            
        } catch (Exception e) {
            logger.severe("Fatal error starting application: " + e.getMessage());
            e.printStackTrace();
            
            // Show error message if possible
            System.err.println("\n=== FATAL APPLICATION ERROR ===");
            System.err.println("Application: " + APP_NAME + " v" + APP_VERSION);
            System.err.println("Error: " + e.getMessage());
            System.err.println("Check the application log file for detailed information.");
            System.err.println("==================================\n");
            
            System.exit(1);
        }
    }
    
    /**
     * Validate Java version requirements
     */
    private static void validateJavaVersion() {
        try {
            String javaVersion = System.getProperty("java.version");
            logger.info("Java version: " + javaVersion);
            
            // Extract major version number
            int majorVersion;
            if (javaVersion.startsWith("1.")) {
                majorVersion = Integer.parseInt(javaVersion.substring(2, 3));
            } else {
                int dotIndex = javaVersion.indexOf('.');
                if (dotIndex > 0) {
                    majorVersion = Integer.parseInt(javaVersion.substring(0, dotIndex));
                } else {
                    majorVersion = Integer.parseInt(javaVersion);
                }
            }
            
            // Check minimum Java version (Java 11 or higher recommended for JavaFX)
            if (majorVersion < 11) {
                logger.warning("Java version " + javaVersion + " detected. Java 11 or higher is recommended.");
            }
            
        } catch (Exception e) {
            logger.warning("Failed to validate Java version: " + e.getMessage());
        }
    }
    
    /**
     * Set system properties for optimal performance
     */
    private static void setSystemProperties() {
        try {
            // JavaFX performance optimizations
            System.setProperty("javafx.animation.fullspeed", "true");
            System.setProperty("javafx.animation.pulse", "60");
            System.setProperty("javafx.animation.framerate", "60");
            
            // Enable hardware acceleration if available
            System.setProperty("prism.vsync", "false");
            System.setProperty("prism.lcdtext", "false");
            
            // Font rendering improvements
            System.setProperty("prism.subpixeltext", "true");
            System.setProperty("prism.text", "t2k");
            
            // Memory management
            System.setProperty("javafx.animation.pulse", "60");
            
            // Logging properties
            System.setProperty("java.util.logging.SimpleFormatter.format", 
                "[%1$tF %1$tT] [%4$-7s] %5$s %n");
            
            logger.info("System properties configured for optimal performance");
            
        } catch (Exception e) {
            logger.warning("Failed to set system properties: " + e.getMessage());
        }
    }
    
    /**
     * Check for required libraries
     */
    private static void checkRequiredLibraries() {
        try {
            // Check for JavaFX
            Class.forName("javafx.application.Application");
            logger.info("JavaFX runtime detected");
            
            // Check for MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            logger.info("MySQL JDBC driver detected");
            
        } catch (ClassNotFoundException e) {
            String missingLibrary = e.getMessage();
            logger.severe("Required library not found: " + missingLibrary);
            
            System.err.println("\n=== MISSING REQUIRED LIBRARY ===");
            System.err.println("Missing: " + missingLibrary);
            
            if (missingLibrary.contains("javafx")) {
                System.err.println("JavaFX runtime is required to run this application.");
                System.err.println("Please install JavaFX or use a JDK that includes JavaFX.");
            } else if (missingLibrary.contains("mysql")) {
                System.err.println("MySQL JDBC driver is required.");
                System.err.println("Please add mysql-connector-java to the classpath.");
            }
            
            System.err.println("=================================\n");
            throw new RuntimeException("Missing required library: " + missingLibrary, e);
        }
    }
    
    @Override
    public void stop() throws Exception {
        if (!shutdownInProgress) {
            logger.info("Application stop() method called");
            performShutdownCleanup();
        }
        super.stop();
    }
    
    /**
     * Get application information
     */
    public static String getApplicationInfo() {
        return String.format("%s v%s", APP_NAME, APP_VERSION);
    }
    
    /**
     * Get application version
     */
    public static String getVersion() {
        return APP_VERSION;
    }
    
    /**
     * Check if application is in development mode
     */
    public static boolean isDevelopmentMode() {
        return "true".equalsIgnoreCase(System.getProperty("app.development", "false"));
    }
}