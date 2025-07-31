package com.mycompany.manufacturing_system;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// Kelas ini akan mensimulasikan interaksi database.
// Untuk tujuan pengujian, ini akan mengembalikan data mock.
public class ManufacturingOperations {
    private Connection connection;

    // Static list to hold mock production orders
    private static List<ProductionOrder> mockProductionOrders = new ArrayList<>();

    // Static initializer to populate mock data for Production Orders
    static {
        // Clear existing data to prevent duplicates on hot reload in some IDEs
        mockProductionOrders.clear();

        // Add mock production orders with more details for MyTasksView
        mockProductionOrders.add(new ProductionOrder("PO001", "Customer A", "P101", "Product X", 100, "IN_PROGRESS", "HIGH",
                LocalDateTime.now().minusDays(5), LocalDateTime.now().plusDays(2), 60.0, "Line 1", "operator", LocalDateTime.now().minusDays(4), "Initial production run, some material adjustments made."));
        mockProductionOrders.add(new ProductionOrder("PO002", "Customer B", "P102", "Product Y", 50, "PENDING", "URGENT",
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1), 0.0, "Line 2", "operator", null, "Waiting for raw materials."));
        mockProductionOrders.add(new ProductionOrder("PO003", "Customer C", "P103", "Product Z", 200, "COMPLETED", "MEDIUM",
                LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(3), 100.0, "Line 3", "supervisor", LocalDateTime.now().minusDays(9), "Final quality check passed."));
        mockProductionOrders.add(new ProductionOrder("PO004", "Customer D", "P104", "Product A", 75, "ON_HOLD", "LOW",
                LocalDateTime.now().minusDays(7), LocalDateTime.now().plusDays(5), 30.0, "Line 1", "operator", LocalDateTime.now().minusDays(6), "On hold due to equipment maintenance."));
        mockProductionOrders.add(new ProductionOrder("PO005", "Customer E", "P105", "Product B", 120, "IN_PROGRESS", "MEDIUM",
                LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1), 25.0, "Line 2", "operator", LocalDateTime.now().minusDays(2), "Facing minor delays, potential overdue.")); // Overdue task
        mockProductionOrders.add(new ProductionOrder("PO006", "Customer F", "P106", "Product C", 80, "PENDING", "MEDIUM",
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(3), 0.0, "Line 3", "supervisor", null, "Awaiting supervisor approval to start."));
    }


    public ManufacturingOperations() throws SQLException {
        // Untuk demo, kita tidak akan benar-benar terhubung ke database.
        // Jika Anda ingin terhubung, uncomment baris di bawah ini dan sesuaikan kredensial.
        // String url = "jdbc:mysql://localhost:3307/manufacturing_system";
        // String user = "root";
        // String password = "";
        // this.connection = DriverManager.getConnection(url, user, password);
        System.out.println("ManufacturingOperations initialized (mock database connection).");
    }

    // Metode untuk mengautentikasi pengguna (mock)
    public User authenticateUser(String username, String password) throws SQLException {
        // Menggunakan UserOperations untuk autentikasi mock
        UserOperations userOps = new UserOperations();
        List<User> allUsers = userOps.getAllUsers();
        for (User user : allUsers) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return user; // User ditemukan dan password cocok
            }
        }
        return null; // Autentikasi gagal
    }

    // Kelas internal untuk statistik produksi
    public static class ProductionStatistics {
        private int totalOrders;
        private int pendingOrders;
        private int inProgressOrders;
        private int completedOrders;

        public ProductionStatistics(int totalOrders, int pendingOrders, int inProgressOrders, int completedOrders) {
            this.totalOrders = totalOrders;
            this.pendingOrders = pendingOrders;
            this.inProgressOrders = inProgressOrders;
            this.completedOrders = completedOrders;
        }

        public int getTotalOrders() { return totalOrders; }
        public int getPendingOrders() { return pendingOrders; }
        public int getInProgressOrders() { return inProgressOrders; }
        public int getCompletedOrders() { return completedOrders; }
    }

    // Metode untuk mendapatkan statistik produksi (mock)
    public ProductionStatistics getProductionStatistics() throws SQLException {
        // Menghitung statistik dari mockProductionOrders
        long total = mockProductionOrders.size();
        long pending = mockProductionOrders.stream().filter(o -> "PENDING".equals(o.getStatus())).count();
        long inProgress = mockProductionOrders.stream().filter(o -> "IN_PROGRESS".equals(o.getStatus())).count();
        long completed = mockProductionOrders.stream().filter(ProductionOrder::isCompleted).count();

        return new ProductionStatistics((int)total, (int)pending, (int)inProgress, (int)completed);
    }

    // Metode untuk mendapatkan semua pesanan produksi (mock)
    public List<ProductionOrder> getAllProductionOrders() throws SQLException {
        return new ArrayList<>(mockProductionOrders); // Return a copy to prevent external modification
    }

    // Metode untuk mendapatkan pesanan produksi berdasarkan user_id (mock)
    public List<ProductionOrder> getProductionOrdersByUserId(int userId) throws SQLException {
        // Ini adalah contoh filter, Anda mungkin perlu menyesuaikannya
        // berdasarkan bagaimana user_id dihubungkan dengan ProductionOrder
        // Untuk mock, kita asumsikan user ID 2 adalah 'operator'
        UserOperations userOps = new UserOperations();
        Optional<User> userOpt = userOps.getAllUsers().stream().filter(u -> u.getUserId() == userId).findFirst();

        if (userOpt.isPresent()) {
            String username = userOpt.get().getUsername();
            return mockProductionOrders.stream()
                    .filter(order -> username.equalsIgnoreCase(order.getAssignedOperator()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    // Metode placeholder untuk operasi lain
    public void createProductionOrder(ProductionOrder order) throws SQLException {
        System.out.println("Creating production order: " + order.getOrderId());
        // In a real app, add to DB and then refresh mock list or add to it
        mockProductionOrders.add(order);
    }

    // Metode untuk memperbarui status dan progress produksi (mock)
    public boolean updateProductionProgress(String orderId, double progress, String notes) throws SQLException {
        Optional<ProductionOrder> orderOpt = mockProductionOrders.stream()
                .filter(o -> o.getOrderId().equalsIgnoreCase(orderId))
                .findFirst();

        if (orderOpt.isPresent()) {
            ProductionOrder order = orderOpt.get();
            order.setCompletionPercentage(progress);
            order.setNotes(notes);
            if (progress >= 100.0) {
                order.setStatus("COMPLETED");
            } else if (progress > 0 && !"IN_PROGRESS".equals(order.getStatus())) {
                order.setStatus("IN_PROGRESS");
            }
            System.out.println("Updated order " + orderId + ": Progress=" + progress + "%, Status=" + order.getStatus());
            return true;
        }
        System.out.println("Order not found for progress update: " + orderId);
        return false;
    }

    // Metode untuk memperbarui status produksi (jika diperlukan terpisah dari progress)
    public void updateProductionOrderStatus(String orderId, String newStatus) throws SQLException {
        Optional<ProductionOrder> orderOpt = mockProductionOrders.stream()
                .filter(o -> o.getOrderId().equalsIgnoreCase(orderId))
                .findFirst();

        if (orderOpt.isPresent()) {
            ProductionOrder order = orderOpt.get();
            order.setStatus(newStatus);
            System.out.println("Updated order " + orderId + ": Status=" + newStatus);
        }
    }
}
 