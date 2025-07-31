package com.mycompany.manufacturing_system;

/**
 * Data Transfer Object untuk menampung data statistik pengguna.
 */
public class UserStatistics {
    private final int totalUsers;
    private final int activeUsers;
    private final int adminCount;
    private final int userCount;

    public UserStatistics(int totalUsers, int activeUsers, int adminCount, int userCount) {
        this.totalUsers = totalUsers;
        this.activeUsers = activeUsers;
        this.adminCount = adminCount;
        this.userCount = userCount;
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public int getActiveUsers() {
        return activeUsers;
    }

    public int getAdminCount() {
        return adminCount;
    }

    public int getUserCount() {
        return userCount;
    }
}