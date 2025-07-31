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
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Tampilan Manajemen Pengguna Modern untuk Sistem Manajemen Manufaktur
 * Mendukung peran ADMIN dan SUPERVISOR dengan UI yang ditingkatkan
 */
public class UserManagementView {
    private final UserOperations userOps;
    private TableView<User> userTable;
    private User selectedUser;
    private final User currentUser;
    private final MainApp App; 
    private TextField searchField;
    private ComboBox<String> roleFilter; // Diubah menjadi String untuk kesederhanaan di UI filter
    private ComboBox<String> statusFilter;
    private Label totalUsersLabel;
    private Label activeUsersLabel;
    private Label adminCountLabel;
    private Label userCountLabel; // Ini sekarang akan menyertakan peran 'USER' biasa

    public UserManagementView(User user, MainApp App) {
        this.userOps = new UserOperations();
        this.currentUser = user;
        this.App = App; 
    }

    public VBox getView() {
        // Pastikan currentUser dan perannya tidak null sebelum memeriksa izin
        // Menggunakan perbandingan nama peran langsung
        if (currentUser == null || currentUser.getRole() == null || !currentUser.canManageUsers()) {
            return createAccessDeniedView();
        }

        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: linear-gradient(to bottom right, #f8f9fa, #e9ecef);");

        // Tombol kembali
        Button backButton = new Button("← Kembali ke Menu Utama");
        backButton.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        backButton.setStyle("""
            -fx-background-color: #3498db;
            -fx-text-fill: white;
            -fx-background-radius: 8;
            -fx-border-radius: 8;
            -fx-padding: 10 20;
            -fx-cursor: hand;
            """);
        backButton.setOnAction(e -> {
            if (App != null) {
                App.goBackToMainMenu();
            } else {
                showAlert("Kesalahan Navigasi", "Konteks aplikasi utama tidak tersedia.", Alert.AlertType.ERROR);
            }
        });
        
        // Pembungkus untuk tombol kembali untuk menyelaraskannya
        HBox backButtonWrapper = new HBox(backButton);
        backButtonWrapper.setAlignment(Pos.TOP_LEFT);
        backButtonWrapper.setPadding(new Insets(0, 0, 10, 0)); // Tambahkan padding di bawah tombol


        VBox headerSection = createHeaderSection();
        HBox statsSection = createStatsSection();
        HBox filterSection = createFilterSection();
        VBox tableSection = createTableSection();
        HBox actionSection = createActionSection();

        mainContainer.getChildren().addAll(
            backButtonWrapper, // Tambahkan tombol kembali di bagian atas
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
        
        Label deniedLabel = new Label("Akses Ditolak");
        deniedLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        deniedLabel.setStyle("-fx-text-fill: #e74c3c;");
        
        Label detailLabel = new Label("Halaman ini membutuhkan hak akses administrator atau supervisor."); // Pesan diperbarui
        detailLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        detailLabel.setStyle("-fx-text-fill: #7f8c8d;");

        Button backButton = new Button("← Kembali ke Menu Utama");
        backButton.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        backButton.setStyle("""
            -fx-background-color: #3498db;
            -fx-text-fill: white;
            -fx-background-radius: 8;
            -fx-border-radius: 8;
            -fx-padding: 10 20;
            -fx-cursor: hand;
            """);
        backButton.setOnAction(e -> {
            if (App != null) {
                App.goBackToMainMenu(); 
            } else {
                showAlert("Kesalahan Navigasi", "Konteks aplikasi utama tidak tersedia.", Alert.AlertType.ERROR);
            }
        });
        
        deniedContainer.getChildren().addAll(iconText, deniedLabel, detailLabel, backButton);
        
        VBox wrapper = new VBox(deniedContainer);
        wrapper.setAlignment(Pos.CENTER);
        VBox.setVgrow(wrapper, Priority.ALWAYS);
        
        return wrapper;
    }

    private VBox createHeaderSection() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Text titleText = new Text("👥 Manajemen Pengguna");
        titleText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleText.setFill(Color.web("#2c3e50"));
        
        Text subtitleText = new Text("Kelola pengguna sistem dan peran mereka");
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
        userCountLabel = new Label("0"); // Ini sekarang akan menyertakan peran 'USER' biasa
        
        container.getChildren().addAll(
            createStatCard("Total Pengguna", totalUsersLabel, "#3498db", "👤"),
            createStatCard("Pengguna Aktif", activeUsersLabel, "#27ae60", "✅"),
            createStatCard("Administrator", adminCountLabel, "#e74c3c", "👨‍💼"),
            createStatCard("Operator/Pengguna", userCountLabel, "#f39c12", "👷") // Label diperbarui
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
        searchField.setPromptText("🔍 Cari pengguna...");
        searchField.setPrefWidth(200);
        searchField.setStyle("-fx-background-color: white; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #bdc3c7; -fx-padding: 8 12;");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        
        // Dapatkan semua nama peran yang tersedia dari UserOperations
        List<String> allRoleNames = userOps.getAllUsers().stream()
                                            .map(user -> user.getRole().getRoleName())
                                            .distinct()
                                            .sorted() // Urutkan peran secara alfabetis
                                            .collect(Collectors.toList());
        allRoleNames.add(0, "Semua Peran"); // Tambahkan "Semua Peran" di awal
        roleFilter = new ComboBox<>(FXCollections.observableArrayList(allRoleNames));
        roleFilter.setValue("Semua Peran");
        roleFilter.setPrefWidth(120);
        roleFilter.setOnAction(e -> applyFilters());
        
        statusFilter = new ComboBox<>(FXCollections.observableArrayList("Semua Status", "Aktif", "Tidak Aktif"));
        statusFilter.setValue("Semua Status");
        statusFilter.setPrefWidth(120);
        statusFilter.setOnAction(e -> applyFilters());
        
        Button refreshBtn = createStyledButton("🔄 Segarkan", "#3498db", 100, e -> {
            refreshUserTable();
            refreshStatistics();
        });
        filterBox.getChildren().addAll(new Label("Cari:"), searchField, new Label("Peran:"), roleFilter, new Label("Status:"), statusFilter, refreshBtn);
        return filterBox;
    }

    private VBox createTableSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(15));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-radius: 12;");
        
        DropShadow shadow = new DropShadow(8, Color.rgb(0, 0, 0, 0.1));
        shadow.setOffsetY(2);
        section.setEffect(shadow);
        
        Text sectionTitle = new Text("Pengguna Sistem");
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

        TableColumn<User, String> usernameCol = new TableColumn<>("Nama Pengguna");
        usernameCol.setCellValueFactory(data -> data.getValue().usernameProperty());
        usernameCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String username, boolean empty) {
                super.updateItem(username, empty);
                setText(empty ? null : username);
                setStyle(empty ? "" : "-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            }
        });

        TableColumn<User, String> fullNameCol = new TableColumn<>("Nama Lengkap");
        fullNameCol.setCellValueFactory(data -> data.getValue().fullNameProperty());

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(data -> data.getValue().emailProperty());

        TableColumn<User, String> phoneCol = new TableColumn<>("Telepon"); // Diperbarui label
        phoneCol.setCellValueFactory(data -> data.getValue().phoneProperty());

        TableColumn<User, String> departmentCol = new TableColumn<>("Departemen");
        departmentCol.setCellValueFactory(data -> data.getValue().departmentProperty());

        TableColumn<User, String> roleCol = new TableColumn<>("Peran");
        // Menggunakan getRole().getRoleName() dari objek Role di dalam User
        roleCol.setCellValueFactory(data -> data.getValue().getRole().roleNameProperty());
        roleCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(role);
                    String color = switch (role) {
                        case "ADMIN" -> "#e74c3c"; // Merah
                        case "SUPERVISOR" -> "#f39c12"; // Oranye
                        case "OPERATOR" -> "#3498db"; // Biru
                        case "USER" -> "#27ae60"; // Hijau
                        default -> "#7f8c8d"; // Abu-abu
                    };
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
                    setText(status ? "Aktif" : "Tidak Aktif");
                    String color = status ? "#27ae60" : "#e74c3c";
                    setStyle(String.format("-fx-text-fill: %s; -fx-font-weight: bold; -fx-background-color: %s20; -fx-background-radius: 4;", color, color));
                    setAlignment(Pos.CENTER);
                }
            }
        });

        table.getColumns().setAll(usernameCol, fullNameCol, emailCol, phoneCol, departmentCol, roleCol, statusCol);
        table.setMinHeight(300);
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> selectedUser = newVal);

        return table;
    }

    private HBox createActionSection() {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));

        buttonBox.getChildren().addAll(
            createStyledButton("➕ Tambah Pengguna", "#27ae60", 120, e -> showAddUserDialog()),
            createStyledButton("✏️ Edit Pengguna", "#3498db", 120, e -> showEditUserDialog()),
            createStyledButton("🔄 Ubah Peran", "#f39c12", 120, e -> showChangeRoleDialog()),
            createStyledButton("🔲 Ganti Status", "#9b59b6", 120, e -> toggleUserStatus()),
            createStyledButton("🔑 Reset Kata Sandi", "#e67e22", 140, e -> showResetPasswordDialog())
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

    // --- Metode Dialog ---

    private void showAddUserDialog() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Tambah Pengguna Baru");
        dialog.setHeaderText("Buat akun pengguna baru");

        GridPane grid = createUserFormGrid();
        TextField usernameField = new TextField();
        usernameField.setPromptText("Masukkan nama pengguna");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Masukkan kata sandi");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Konfirmasi kata sandi");
        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Masukkan nama lengkap");
        TextField emailField = new TextField();
        emailField.setPromptText("Masukkan email (opsional)");
        TextField phoneField = new TextField(); // Menambahkan field phone
        phoneField.setPromptText("Masukkan nomor telepon (opsional)");
        
        // Isi kotak kombo peran dengan peran yang tersedia dari UserOperations
        List<String> allRoleNames = userOps.getAllUsers().stream()
                                            .map(user -> user.getRole().getRoleName())
                                            .distinct()
                                            .sorted() // Urutkan peran secara alfabetis
                                            .collect(Collectors.toList());
        ComboBox<String> roleCombo = new ComboBox<>(FXCollections.observableArrayList(allRoleNames));
        roleCombo.setValue("USER"); // Peran default
        
        ComboBox<String> departmentCombo = new ComboBox<>(FXCollections.observableArrayList(
            User.Department.PRODUCTION.getDisplayName(), User.Department.QUALITY_CONTROL.getDisplayName(), 
            User.Department.MAINTENANCE.getDisplayName(), User.Department.LOGISTICS.getDisplayName(), 
            User.Department.ADMINISTRATION.getDisplayName(), User.Department.IT.getDisplayName(),
            User.Department.UNKNOWN.getDisplayName()
        ));
        departmentCombo.setValue(User.Department.PRODUCTION.getDisplayName());

        grid.add(new Label("Nama Pengguna:"), 0, 0); grid.add(usernameField, 1, 0);
        grid.add(new Label("Kata Sandi:"), 0, 1); grid.add(passwordField, 1, 1);
        grid.add(new Label("Konfirmasi Kata Sandi:"), 0, 2); grid.add(confirmPasswordField, 1, 2);
        grid.add(new Label("Nama Lengkap:"), 0, 3); grid.add(fullNameField, 1, 3);
        grid.add(new Label("Email:"), 0, 4); grid.add(emailField, 1, 4);
        grid.add(new Label("Telepon:"), 0, 5); grid.add(phoneField, 1, 5); // Menambahkan field phone
        grid.add(new Label("Peran:"), 0, 6); grid.add(roleCombo, 1, 6);
        grid.add(new Label("Departemen:"), 0, 7); grid.add(departmentCombo, 1, 7);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String username = usernameField.getText().trim();
                String password = passwordField.getText();
                String confirmPassword = confirmPasswordField.getText();
                String fullName = fullNameField.getText().trim();
                String email = emailField.getText().trim();
                String phone = phoneField.getText().trim(); // Mengambil nilai phone
                String roleName = roleCombo.getValue();
                String department = departmentCombo.getValue();

                if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || roleName == null || department == null) {
                    showAlert("Kesalahan Validasi", "Nama Pengguna, Kata Sandi, Nama Lengkap, Peran, dan Departemen wajib diisi.", Alert.AlertType.ERROR);
                    return null;
                }
                if (!password.equals(confirmPasswordField.getText())) {
                    showAlert("Kesalahan Validasi", "Kata sandi tidak cocok.", Alert.AlertType.ERROR);
                    return null;
                }
                
                // Dapatkan objek Role dari nama peran menggunakan UserOperations
                Optional<Role> roleOpt = userOps.getRoleByName(roleName);
                if (roleOpt.isEmpty()) {
                    showAlert("Kesalahan", "Peran yang dipilih tidak valid atau tidak ditemukan.", Alert.AlertType.ERROR);
                    return null;
                }
                
                return new User(username, password, roleOpt.get(), fullName, email, phone, department); // Meneruskan phone
            }
            return null;
        });

        dialog.showAndWait().ifPresent(user -> {
            if (user != null) { // Periksa apakah objek pengguna tidak null (yaitu, OK ditekan dan validasi berhasil)
                if (userOps.createUser(user)) {
                    showAlert("Berhasil", "Pengguna berhasil ditambahkan.", Alert.AlertType.INFORMATION);
                    refreshUserTable();
                    refreshStatistics();
                } else {
                    showAlert("Kesalahan", "Gagal menambahkan pengguna. Nama pengguna mungkin sudah ada atau terjadi kesalahan database. Periksa konsol untuk detail.", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void showEditUserDialog() {
        if (selectedUser == null) {
            showAlert("Kesalahan", "Pilih pengguna untuk diedit.", Alert.AlertType.ERROR);
            return;
        }

        Dialog<Boolean> dialog = new Dialog<>(); // Mengubah tipe hasil ke Boolean
        dialog.setTitle("Edit Pengguna");
        dialog.setHeaderText("Edit informasi pengguna untuk: " + selectedUser.getUsername());

        GridPane grid = createUserFormGrid();
        TextField fullNameField = new TextField(selectedUser.getFullName());
        TextField emailField = new TextField(selectedUser.getEmail());
        TextField phoneField = new TextField(selectedUser.getPhone()); // Menambahkan field phone
        ComboBox<String> departmentCombo = new ComboBox<>(FXCollections.observableArrayList(
            User.Department.PRODUCTION.getDisplayName(), User.Department.QUALITY_CONTROL.getDisplayName(), 
            User.Department.MAINTENANCE.getDisplayName(), User.Department.LOGISTICS.getDisplayName(), 
            User.Department.ADMINISTRATION.getDisplayName(), User.Department.IT.getDisplayName(),
            User.Department.UNKNOWN.getDisplayName()
        ));
        departmentCombo.setValue(selectedUser.getDepartment());

        grid.add(new Label("Nama Lengkap:"), 0, 0); grid.add(fullNameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1); grid.add(emailField, 1, 1);
        grid.add(new Label("Telepon:"), 0, 2); grid.add(phoneField, 1, 2); // Menambahkan field phone
        grid.add(new Label("Departemen:"), 0, 3); grid.add(departmentCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String fullName = fullNameField.getText().trim();
                String email = emailField.getText().trim();
                String phone = phoneField.getText().trim(); // Mengambil nilai phone
                String department = departmentCombo.getValue();

                if (fullName.isEmpty() || department == null) {
                    showAlert("Kesalahan Validasi", "Nama Lengkap dan Departemen wajib diisi.", Alert.AlertType.ERROR);
                    return false; // Kembalikan false untuk menunjukkan kegagalan validasi
                }
                // Langsung perbarui selectedUser dan kembalikan true jika berhasil
                return userOps.updateUser(selectedUser.getUsername(), 
                                          fullName, 
                                          email, 
                                          phone, // Meneruskan phone
                                          department);
            }
            return false;
        });

        dialog.showAndWait().ifPresent(success -> {
            if (success) {
                showAlert("Berhasil", "Informasi pengguna berhasil diperbarui.", Alert.AlertType.INFORMATION);
                refreshUserTable();
                refreshStatistics();
            } else {
                showAlert("Kesalahan", "Gagal memperbarui informasi pengguna. Periksa konsol untuk detail.", Alert.AlertType.ERROR);
            }
        });
    }

    private void showChangeRoleDialog() {
        if (selectedUser == null) {
            showAlert("Kesalahan", "Pilih pengguna.", Alert.AlertType.ERROR);
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Ubah Peran Pengguna");
        dialog.setHeaderText("Ubah peran untuk pengguna: " + selectedUser.getDisplayName());

        GridPane grid = createUserFormGrid();
        // Isi kotak kombo peran dengan semua peran yang tersedia
        List<String> allRoleNames = userOps.getAllUsers().stream()
                                            .map(user -> user.getRole().getRoleName())
                                            .distinct()
                                            .sorted()
                                            .collect(Collectors.toList());
        ComboBox<String> roleCombo = new ComboBox<>(FXCollections.observableArrayList(allRoleNames));
        roleCombo.setValue(selectedUser.getRole().getRoleName()); // Atur peran saat ini
        grid.add(new Label("Peran Baru:"), 0, 0);
        grid.add(roleCombo, 1, 0);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> bt == ButtonType.OK ? roleCombo.getValue() : null);

        dialog.showAndWait().ifPresent(roleName -> {
            if (roleName != null) { // Pastikan roleName tidak null (jika pengguna menekan OK)
                if (userOps.changeUserRole(selectedUser.getUsername(), roleName)) {
                    showAlert("Berhasil", "Peran pengguna berhasil diperbarui.", Alert.AlertType.INFORMATION);
                    refreshUserTable();
                    refreshStatistics();
                } else {
                    showAlert("Kesalahan", "Gagal memperbarui peran pengguna. Periksa konsol untuk detail.", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void toggleUserStatus() {
        if (selectedUser == null) {
            showAlert("Kesalahan", "Pilih pengguna.", Alert.AlertType.ERROR);
            return;
        }

        String action = selectedUser.isActive() ? "menonaktifkan" : "mengaktifkan";
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, String.format("Apakah Anda yakin ingin %s pengguna '%s'?", action, selectedUser.getDisplayName()), ButtonType.OK, ButtonType.CANCEL);
        confirmAlert.setTitle("Konfirmasi Tindakan");
        confirmAlert.setHeaderText("Ganti Status Pengguna");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean newStatus = !selectedUser.isActive();
                if (userOps.toggleUserStatus(selectedUser.getUsername(), newStatus)) {
                    showAlert("Berhasil", "Status pengguna berhasil diperbarui.", Alert.AlertType.INFORMATION);
                    refreshUserTable();
                    refreshStatistics();
                } else {
                    showAlert("Kesalahan", "Gagal memperbarui status pengguna. Periksa konsol untuk detail.", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void showResetPasswordDialog() {
        if (selectedUser == null) {
            showAlert("Kesalahan", "Pilih pengguna.", Alert.AlertType.ERROR);
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Reset Kata Sandi");
        dialog.setHeaderText("Reset kata sandi untuk pengguna: " + selectedUser.getDisplayName());

        GridPane grid = createUserFormGrid();
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Kata Sandi Baru");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Konfirmasi Kata Sandi Baru");
        
        grid.add(new Label("Kata Sandi Baru:"), 0, 0); grid.add(passwordField, 1, 0);
        grid.add(new Label("Konfirmasi Kata Sandi:"), 0, 1); grid.add(confirmPasswordField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                if (passwordField.getText().isEmpty()) {
                    showAlert("Kesalahan", "Kata sandi tidak boleh kosong.", Alert.AlertType.ERROR); return null;
                }
                if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                    showAlert("Kesalahan", "Kata sandi tidak cocok.", Alert.AlertType.ERROR); return null;
                }
                return passwordField.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(password -> {
            if (password != null) { // Pastikan kata sandi tidak null (jika pengguna menekan OK)
                if (userOps.resetUserPassword(selectedUser.getUsername(), password)) {
                    showAlert("Berhasil", "Kata sandi berhasil direset.", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Kesalahan", "Gagal mereset kata sandi. Periksa konsol untuk detail.", Alert.AlertType.ERROR);
                }
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
                                  user.getFullName().toLowerCase().contains(searchTerm) ||
                                  (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchTerm)) ||
                                  (user.getPhone() != null && user.getPhone().toLowerCase().contains(searchTerm)) || // Menambahkan filter phone
                                  (user.getDepartment() != null && user.getDepartment().toLowerCase().contains(searchTerm));
            
            boolean roleMatch = "Semua Peran".equals(roleFilterValue) || user.getRole().getRoleName().equals(roleFilterValue);
            
            boolean statusMatch = "Semua Status".equals(statusFilterValue) ||
                                  ("Aktif".equals(statusFilterValue) && user.isActive()) ||
                                  ("Tidak Aktif".equals(statusFilterValue) && !user.isActive());
            
            return searchMatch && roleMatch && statusMatch;
        }).collect(Collectors.toList());
        
        userTable.setItems(FXCollections.observableArrayList(filteredUsers));
    }

    private void refreshUserTable() {
        applyFilters(); 
    }

    private void refreshStatistics() {
        UserOperations.UserStatistics stats = UserOperations.getUserStatistics();
        totalUsersLabel.setText(String.valueOf(stats.getTotalUsers()));
        activeUsersLabel.setText(String.valueOf(stats.getActiveUsers()));
        adminCountLabel.setText(String.valueOf(stats.getAdminCount()));
        // Jumlah supervisor, operator, dan pengguna biasa untuk label 'Operator/Pengguna'
        userCountLabel.setText(String.valueOf(stats.getSupervisorCount() + stats.getOperatorCount() + stats.getUserCount()));
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
 