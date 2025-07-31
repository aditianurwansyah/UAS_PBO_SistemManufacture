package com.mycompany.manufacturing_system;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;


/**
 * Modern User Management View for Manufacturing Management System
 * Supports ADMIN and USER roles with enhanced UI
 */
public class UserManagementView {
    private final UserOperations userOps;
    private TableView<User> userTable;
    private User selectedUser;
    private final User currentUser;
    private TextField searchField;
    private ComboBox<String> roleFilter;
    private ComboBox<String> statusFilter;
    private Label totalUsersLabel;
    private Label activeUsersLabel;
    private Label adminCountLabel;
    private Label userCountLabel;

    public UserManagementView() {
        this.userOps = new UserOperations();
        // In a real app, the current user would be properly authenticated and passed here.
        // For demonstration, we create a default admin user.
        this.currentUser = new User("tempAdmin", "pass", "ADMIN");
    }

    public UserManagementView(User user) {
        this.userOps = new UserOperations();
        this.currentUser = user;
    }

    public VBox getView() {
        if (currentUser == null || !currentUser.canManageUsers()) {
            return createAccessDeniedView();
        }

        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: linear-gradient(to bottom right, #f8f9fa, #e9ecef);");

        VBox headerSection = createHeaderSection();
        HBox statsSection = createStatsSection();
        HBox filterSection = createFilterSection();
        VBox tableSection = createTableSection();
        HBox actionSection = createActionSection();

        mainContainer.getChildren().addAll(
            headerSection,
            createSeparator(),
            statsSection,
            createSeparator(),
            filterSection,
            tableSection,
            actionSection
        );

        refreshUserTable();
        refreshStatistics();

        return mainContainer;
    }

    private VBox createAccessDeniedView() {
        VBox deniedContainer = new VBox(20);
        deniedContainer.setAlignment(Pos.CENTER);
        deniedContainer.setPadding(new Insets(50));
        deniedContainer.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        
        Text iconText = new Text("🚫");
        iconText.setFont(Font.font(48));
        
        Label deniedLabel = new Label("Access Denied");
        deniedLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        deniedLabel.setStyle("-fx-text-fill: #e74c3c;");
        
        Label detailLabel = new Label("This page requires administrator privileges.");
        detailLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        detailLabel.setStyle("-fx-text-fill: #7f8c8d;");
        
        deniedContainer.getChildren().addAll(iconText, deniedLabel, detailLabel);
        
        VBox wrapper = new VBox(deniedContainer);
        wrapper.setAlignment(Pos.CENTER);
        VBox.setVgrow(wrapper, Priority.ALWAYS);
        
        return wrapper;
    }

    private VBox createHeaderSection() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Text titleText = new Text("👥 User Management");
        titleText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleText.setFill(Color.web("#2c3e50"));
        
        Text subtitleText = new Text("Manage system users and their roles");
        subtitleText.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        subtitleText.setFill(Color.web("#7f8c8d"));

        header.getChildren().addAll(titleText, subtitleText);
        return header;
    }

    private HBox createStatsSection() {
        HBox container = new HBox(20);
        container.setAlignment(Pos.CENTER);
        
        totalUsersLabel = new Label("0");
        activeUsersLabel = new Label("0");
        adminCountLabel = new Label("0");
        userCountLabel = new Label("0");
        
        container.getChildren().addAll(
            createStatCard("Total Users", totalUsersLabel, "#3498db", "👤"),
            createStatCard("Active Users", activeUsersLabel, "#27ae60", "✅"),
            createStatCard("Administrators", adminCountLabel, "#e74c3c", "👨‍💼"),
            createStatCard("Regular Users", userCountLabel, "#f39c12", "👷")
        );
        return container;
    }

    private VBox createStatCard(String title, Label valueLabel, String color, String icon) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setPrefWidth(150);
        card.setPrefHeight(100);
        card.setStyle(String.format("-fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: %s; -fx-border-width: 2;", color));
        
        DropShadow shadow = new DropShadow(6, Color.rgb(0, 0, 0, 0.1));
        shadow.setOffsetY(2);
        card.setEffect(shadow);
        
        Text iconText = new Text(icon);
        iconText.setFont(Font.font(20));
        
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        valueLabel.setTextFill(Color.web(color));
        
        Text titleText = new Text(title);
        titleText.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        titleText.setFill(Color.web("#7f8c8d"));
        
        card.getChildren().addAll(iconText, valueLabel, titleText);
        return card;
    }

    private HBox createFilterSection() {
        HBox filterBox = new HBox(15);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        filterBox.setPadding(new Insets(10));
        
        searchField = new TextField();
        searchField.setPromptText("🔍 Search users...");
        searchField.setPrefWidth(200);
        searchField.setStyle("-fx-background-color: white; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #bdc3c7; -fx-padding: 8 12;");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        
        roleFilter = new ComboBox<>(FXCollections.observableArrayList("All Roles", "ADMIN", "USER"));
        roleFilter.setValue("All Roles");
        roleFilter.setPrefWidth(120);
        roleFilter.setOnAction(e -> applyFilters());
        
        statusFilter = new ComboBox<>(FXCollections.observableArrayList("All Status", "Active", "Inactive"));
        statusFilter.setValue("All Status");
        statusFilter.setPrefWidth(120);
        statusFilter.setOnAction(e -> applyFilters());
        
        Button refreshBtn = createStyledButton("🔄 Refresh", "#3498db", 100, e -> {
        refreshUserTable();
         refreshStatistics();
        });
        filterBox.getChildren().addAll(new Label("Search:"), searchField, new Label("Role:"), roleFilter, new Label("Status:"), statusFilter, refreshBtn);
        return filterBox;
    }

    private VBox createTableSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(15));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-radius: 12;");
        
        DropShadow shadow = new DropShadow(8, Color.rgb(0, 0, 0, 0.1));
        shadow.setOffsetY(2);
        section.setEffect(shadow);
        
        Text sectionTitle = new Text("System Users");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        sectionTitle.setFill(Color.web("#2c3e50"));
        
        userTable = createUserTable();
        VBox.setVgrow(userTable, Priority.ALWAYS);
        
        section.getChildren().addAll(sectionTitle, userTable);
        return section;
    }

    private TableView<User> createUserTable() {
        TableView<User> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-background-color: transparent; -fx-background-radius: 8;");

        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(data -> data.getValue().usernameProperty());
        usernameCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String username, boolean empty) {
                super.updateItem(username, empty);
                setText(empty ? null : username);
                setStyle(empty ? "" : "-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            }
        });

        TableColumn<User, String> fullNameCol = new TableColumn<>("Full Name");
        fullNameCol.setCellValueFactory(data -> data.getValue().fullNameProperty());

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(data -> data.getValue().emailProperty());

        TableColumn<User, String> departmentCol = new TableColumn<>("Department");
        departmentCol.setCellValueFactory(data -> data.getValue().departmentProperty());

        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(data -> data.getValue().roleProperty());
        roleCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(role);
                    String color = "ADMIN".equals(role) ? "#e74c3c" : "#27ae60";
                    setStyle(String.format("-fx-text-fill: %s; -fx-font-weight: bold; -fx-background-color: %s20; -fx-background-radius: 4;", color, color));
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<User, Boolean> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> data.getValue().isActiveProperty());
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status ? "Active" : "Inactive");
                    String color = status ? "#27ae60" : "#e74c3c";
                    setStyle(String.format("-fx-text-fill: %s; -fx-font-weight: bold; -fx-background-color: %s20; -fx-background-radius: 4;", color, color));
                    setAlignment(Pos.CENTER);
                }
            }
        });

        table.getColumns().setAll(usernameCol, fullNameCol, emailCol, departmentCol, roleCol, statusCol);
        table.setMinHeight(300);
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> selectedUser = newVal);

        return table;
    }

    private HBox createActionSection() {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));

        buttonBox.getChildren().addAll(
            createStyledButton("➕ Add User", "#27ae60", 120, e -> showAddUserDialog()),
            createStyledButton("✏️ Edit User", "#3498db", 120, e -> showEditUserDialog()),
            createStyledButton("🔄 Change Role", "#f39c12", 120, e -> showChangeRoleDialog()),
            createStyledButton("🔲 Toggle Status", "#9b59b6", 120, e -> toggleUserStatus()),
            createStyledButton("🔑 Reset Password", "#e67e22", 140, e -> showResetPasswordDialog())
        );
        return buttonBox;
    }
    
    private Button createStyledButton(String text, String color, double width, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        button.setPrefWidth(width);
        button.setPrefHeight(35);
        String baseStyle = String.format("-fx-text-fill: white; -fx-background-radius: 6; -fx-border-radius: 6; -fx-cursor: hand; -fx-background-color: %s;", color);
        String hoverStyle = String.format("-fx-text-fill: white; -fx-background-radius: 6; -fx-border-radius: 6; -fx-cursor: hand; -fx-background-color: derive(%s, -15%%);", color);
        button.setStyle(baseStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(baseStyle));
        button.setOnAction(handler);
        return button;
    }

    private Separator createSeparator() {
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #ecf0f1;");
        return separator;
    }

    // --- Dialog Methods ---

    private void showAddUserDialog() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Add New User");
        dialog.setHeaderText("Create a new user account");

        GridPane grid = createUserFormGrid();
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        PasswordField confirmPasswordField = new PasswordField();
        TextField fullNameField = new TextField();
        TextField emailField = new TextField();
        ComboBox<String> roleCombo = new ComboBox<>(FXCollections.observableArrayList("USER", "ADMIN"));
        roleCombo.setValue("USER");
        ComboBox<String> departmentCombo = new ComboBox<>(FXCollections.observableArrayList("Production", "Quality Control", "Maintenance", "Logistics", "Administration", "IT"));
        departmentCombo.setValue("Production");

        grid.add(new Label("Username:"), 0, 0); grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1); grid.add(passwordField, 1, 1);
        grid.add(new Label("Confirm Password:"), 0, 2); grid.add(confirmPasswordField, 1, 2);
        grid.add(new Label("Full Name:"), 0, 3); grid.add(fullNameField, 1, 3);
        grid.add(new Label("Email:"), 0, 4); grid.add(emailField, 1, 4);
        grid.add(new Label("Role:"), 0, 5); grid.add(roleCombo, 1, 5);
        grid.add(new Label("Department:"), 0, 6); grid.add(departmentCombo, 1, 6);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                if (usernameField.getText().trim().isEmpty() || passwordField.getText().isEmpty() || fullNameField.getText().trim().isEmpty()) {
                    showAlert("Validation Error", "Username, Password, and Full Name are required.", Alert.AlertType.ERROR);
                    return null;
                }
                if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                    showAlert("Validation Error", "Passwords do not match.", Alert.AlertType.ERROR);
                    return null;
                }
                return new User(usernameField.getText().trim(), passwordField.getText(), roleCombo.getValue(), fullNameField.getText().trim(), emailField.getText().trim(), departmentCombo.getValue());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(user -> {
            if (userOps.createUser(user)) {
                showAlert("Success", "User added successfully.", Alert.AlertType.INFORMATION);
                refreshUserTable();
                refreshStatistics();
            } else {
                showAlert("Error", "Failed to add user. Username might already exist.", Alert.AlertType.ERROR);
            }
        });
    }

    private void showEditUserDialog() {
        if (selectedUser == null) {
            showAlert("Error", "Please select a user to edit.", Alert.AlertType.ERROR);
            return;
        }

        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Edit User");
        dialog.setHeaderText("Edit user information for: " + selectedUser.getUsername());

        GridPane grid = createUserFormGrid();
        TextField fullNameField = new TextField(selectedUser.getFullName());
        TextField emailField = new TextField(selectedUser.getEmail());
        ComboBox<String> departmentCombo = new ComboBox<>(FXCollections.observableArrayList("Production", "Quality Control", "Maintenance", "Logistics", "Administration", "IT"));
        departmentCombo.setValue(selectedUser.getDepartment());

        grid.add(new Label("Full Name:"), 0, 0); grid.add(fullNameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1); grid.add(emailField, 1, 1);
        grid.add(new Label("Department:"), 0, 2); grid.add(departmentCombo, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                // Return a dummy user with updated info
                return new User(null, null, null, fullNameField.getText().trim(), emailField.getText().trim(), departmentCombo.getValue());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedInfo -> {
            if (userOps.updateUser(selectedUser.getUsername(), updatedInfo.getFullName(), updatedInfo.getEmail(), updatedInfo.getDepartment())) {
                showAlert("Success", "User information updated successfully.", Alert.AlertType.INFORMATION);
                refreshUserTable();
            } else {
                showAlert("Error", "Failed to update user information.", Alert.AlertType.ERROR);
            }
        });
    }

    private void showChangeRoleDialog() {
        if (selectedUser == null) {
            showAlert("Error", "Please select a user.", Alert.AlertType.ERROR);
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Change User Role");
        dialog.setHeaderText("Change role for user: " + selectedUser.getDisplayName());

        GridPane grid = createUserFormGrid();
        ComboBox<String> roleCombo = new ComboBox<>(FXCollections.observableArrayList("USER", "ADMIN"));
        roleCombo.setValue(selectedUser.getRole());
        grid.add(new Label("New Role:"), 0, 0);
        grid.add(roleCombo, 1, 0);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> bt == ButtonType.OK ? roleCombo.getValue() : null);

        dialog.showAndWait().ifPresent(role -> {
            if (userOps.changeUserRole(selectedUser.getUsername(), role)) {
                showAlert("Success", "User role updated successfully.", Alert.AlertType.INFORMATION);
                refreshUserTable();
                refreshStatistics();
            } else {
                showAlert("Error", "Failed to update user role.", Alert.AlertType.ERROR);
            }
        });
    }

    private void toggleUserStatus() {
        if (selectedUser == null) {
            showAlert("Error", "Please select a user.", Alert.AlertType.ERROR);
            return;
        }

        String action = selectedUser.isActive() ? "deactivate" : "activate";
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, String.format("Are you sure you want to %s user '%s'?", action, selectedUser.getDisplayName()), ButtonType.OK, ButtonType.CANCEL);
        confirmAlert.setTitle("Confirm Action");
        confirmAlert.setHeaderText("Toggle User Status");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean newStatus = !selectedUser.isActive();
                if (userOps.toggleUserStatus(selectedUser.getUsername(), newStatus)) {
                    showAlert("Success", "User status updated successfully.", Alert.AlertType.INFORMATION);
                    refreshUserTable();
                    refreshStatistics();
                } else {
                    showAlert("Error", "Failed to update user status.", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void showResetPasswordDialog() {
        if (selectedUser == null) {
            showAlert("Error", "Please select a user.", Alert.AlertType.ERROR);
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Reset password for user: " + selectedUser.getDisplayName());

        GridPane grid = createUserFormGrid();
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("New Password");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm New Password");
        
        grid.add(new Label("New Password:"), 0, 0); grid.add(passwordField, 1, 0);
        grid.add(new Label("Confirm Password:"), 0, 1); grid.add(confirmPasswordField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                if (passwordField.getText().isEmpty()) {
                    showAlert("Error", "Password cannot be empty.", Alert.AlertType.ERROR); return null;
                }
                if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                    showAlert("Error", "Passwords do not match.", Alert.AlertType.ERROR); return null;
                }
                return passwordField.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(password -> {
            if (userOps.resetUserPassword(selectedUser.getUsername(), password)) {
                showAlert("Success", "Password reset successfully.", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Error", "Failed to reset password.", Alert.AlertType.ERROR);
            }
        });
    }

    private GridPane createUserFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        return grid;
    }

    private void applyFilters() {
        List<User> allUsers = userOps.getAllUsers();
        List<User> filteredUsers = allUsers.stream().filter(user -> {
            String searchTerm = searchField.getText().toLowerCase();
            String roleFilterValue = roleFilter.getValue();
            String statusFilterValue = statusFilter.getValue();
            
            boolean searchMatch = searchTerm.isEmpty() || 
                                  user.getUsername().toLowerCase().contains(searchTerm) ||
                                  user.getFullName().toLowerCase().contains(searchTerm);
            
            boolean roleMatch = "All Roles".equals(roleFilterValue) || user.getRole().equals(roleFilterValue);
            
            boolean statusMatch = "All Status".equals(statusFilterValue) ||
                                  ("Active".equals(statusFilterValue) && user.isActive()) ||
                                  ("Inactive".equals(statusFilterValue) && !user.isActive());
            
            return searchMatch && roleMatch && statusMatch;
        }).collect(Collectors.toList());
        
        userTable.setItems(FXCollections.observableArrayList(filteredUsers));
    }

    private void refreshUserTable() {
        applyFilters(); 
    }

     private void refreshStatistics() {
        UserStatistics stats = UserOperations.getUserStatistics();
        totalUsersLabel.setText(String.valueOf(stats.getTotalUsers()));
        activeUsersLabel.setText(String.valueOf(stats.getActiveUsers()));
        adminCountLabel.setText(String.valueOf(stats.getAdminCount()));
        userCountLabel.setText(String.valueOf(stats.getUserCount()));
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}