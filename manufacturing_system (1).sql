-- phpMyAdmin SQL Dump
-- version 5.2.0
-- https://www.phpmyadmin.net/
--
-- Host: localhost:3307
-- Generation Time: Jul 23, 2025 at 09:15 AM
-- Server version: 8.0.30
-- PHP Version: 8.1.10

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `manufacturing_system`
--

-- --------------------------------------------------------

--
-- Table structure for table `audit_log`
--

CREATE TABLE `audit_log` (
  `log_id` int NOT NULL,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `action` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `audit_log`
--

INSERT INTO `audit_log` (`log_id`, `username`, `action`, `status`, `description`, `timestamp`) VALUES
(1, 'admin', 'LOGIN_ERROR', '0', 'Database error during authentication', '2025-07-23 07:41:42'),
(2, 'user', 'LOGIN_ERROR', '0', 'Database error during authentication', '2025-07-23 07:48:14'),
(3, 'user', 'LOGIN_BLOCKED', '0', 'Account locked due to multiple failed attempts', '2025-07-23 07:50:30'),
(4, 'user', 'LOGIN_BLOCKED', '0', 'Account locked due to multiple failed attempts', '2025-07-23 07:53:21'),
(5, 'admin', 'LOGIN_BLOCKED', '0', 'Account locked due to multiple failed attempts', '2025-07-23 07:53:56'),
(6, 'admin', 'LOGIN_ERROR', '0', 'Database error during authentication', '2025-07-23 07:55:46'),
(7, 'admin', 'LOGIN_ERROR', '0', 'Database error during authentication', '2025-07-23 07:56:05'),
(8, 'user', 'LOGIN_ERROR', '0', 'Database error during authentication', '2025-07-23 07:57:08');

-- --------------------------------------------------------

--
-- Table structure for table `production_lines`
--

CREATE TABLE `production_lines` (
  `line_id` int NOT NULL,
  `line_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `line_code` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `line_type` enum('ASSEMBLY','ELECTRONICS','AUTOMOTIVE','FURNITURE','QUALITY_CONTROL','PACKAGING') COLLATE utf8mb4_unicode_ci NOT NULL,
  `capacity_per_hour` int DEFAULT '10',
  `status` enum('ACTIVE','MAINTENANCE','INACTIVE','BREAKDOWN') COLLATE utf8mb4_unicode_ci DEFAULT 'ACTIVE',
  `location` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `supervisor_id` int DEFAULT NULL,
  `shift_pattern` enum('24_HOURS','DAY_SHIFT','NIGHT_SHIFT','TWO_SHIFTS') COLLATE utf8mb4_unicode_ci DEFAULT 'DAY_SHIFT',
  `installation_date` date DEFAULT NULL,
  `last_maintenance` date DEFAULT NULL,
  `next_maintenance` date DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `production_lines`
--

INSERT INTO `production_lines` (`line_id`, `line_name`, `line_code`, `line_type`, `capacity_per_hour`, `status`, `location`, `supervisor_id`, `shift_pattern`, `installation_date`, `last_maintenance`, `next_maintenance`, `created_at`, `updated_at`) VALUES
(1, 'Assembly Line Alpha', 'ASM-A01', 'ASSEMBLY', 15, 'ACTIVE', 'Building A - Floor 1', NULL, 'TWO_SHIFTS', '2023-01-15', NULL, NULL, '2025-06-28 20:57:14', '2025-06-28 20:57:14'),
(2, 'Assembly Line Beta', 'ASM-B01', 'ASSEMBLY', 12, 'ACTIVE', 'Building A - Floor 1', NULL, 'TWO_SHIFTS', '2023-02-01', NULL, NULL, '2025-06-28 20:57:14', '2025-06-28 20:57:14'),
(3, 'Electronics Production Line 1', 'ELC-001', 'ELECTRONICS', 20, 'ACTIVE', 'Building B - Floor 2', NULL, '24_HOURS', '2023-01-20', NULL, NULL, '2025-06-28 20:57:14', '2025-06-28 20:57:14'),
(4, 'Electronics Production Line 2', 'ELC-002', 'ELECTRONICS', 18, 'ACTIVE', 'Building B - Floor 2', NULL, '24_HOURS', '2023-03-10', NULL, NULL, '2025-06-28 20:57:14', '2025-06-28 20:57:14'),
(5, 'Automotive Assembly Line', 'AUT-001', 'AUTOMOTIVE', 8, 'ACTIVE', 'Building C - Floor 1', NULL, 'DAY_SHIFT', '2023-01-25', NULL, NULL, '2025-06-28 20:57:14', '2025-06-28 20:57:14'),
(6, 'Furniture Workshop', 'FUR-001', 'FURNITURE', 5, 'ACTIVE', 'Building D - Floor 1', NULL, 'DAY_SHIFT', '2023-02-15', NULL, NULL, '2025-06-28 20:57:14', '2025-06-28 20:57:14'),
(7, 'Quality Control Station A', 'QC-A01', 'QUALITY_CONTROL', 25, 'ACTIVE', 'Building A - Floor 2', NULL, 'TWO_SHIFTS', '2023-01-10', NULL, NULL, '2025-06-28 20:57:14', '2025-06-28 20:57:14'),
(8, 'Quality Control Station B', 'QC-B01', 'QUALITY_CONTROL', 25, 'ACTIVE', 'Building B - Floor 3', NULL, 'TWO_SHIFTS', '2023-01-12', NULL, NULL, '2025-06-28 20:57:14', '2025-06-28 20:57:14'),
(9, 'Packaging Line 1', 'PKG-001', 'PACKAGING', 30, 'ACTIVE', 'Building E - Floor 1', NULL, 'TWO_SHIFTS', '2023-02-20', NULL, NULL, '2025-06-28 20:57:14', '2025-06-28 20:57:14'),
(10, 'Packaging Line 2', 'PKG-002', 'PACKAGING', 25, 'ACTIVE', 'Building E - Floor 1', NULL, 'DAY_SHIFT', '2023-03-01', NULL, NULL, '2025-06-28 20:57:14', '2025-06-28 20:57:14'),
(11, 'Assembly Line A', '', 'ASSEMBLY', 15, 'ACTIVE', 'Building A - Floor 1', NULL, 'DAY_SHIFT', NULL, NULL, NULL, '2025-07-04 21:20:41', '2025-07-04 21:20:41');

-- --------------------------------------------------------

--
-- Table structure for table `production_orders`
--

CREATE TABLE `production_orders` (
  `order_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `customer_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `product_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `product_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `quantity` int NOT NULL,
  `status` enum('PENDING','IN_PROGRESS','COMPLETED','SHIPPED','CANCELLED','ON_HOLD') COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING',
  `priority` enum('URGENT','HIGH','MEDIUM','LOW') COLLATE utf8mb4_unicode_ci DEFAULT 'MEDIUM',
  `order_date` datetime NOT NULL,
  `due_date` datetime DEFAULT NULL,
  `start_date` datetime DEFAULT NULL,
  `completion_date` datetime DEFAULT NULL,
  `assigned_line` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `assigned_operator` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `assigned_user_id` int DEFAULT NULL,
  `total_cost` decimal(12,2) DEFAULT '0.00',
  `completion_percentage` decimal(5,2) DEFAULT '0.00',
  `notes` text COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `production_reports`
--

CREATE TABLE `production_reports` (
  `report_id` int NOT NULL,
  `order_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `production_date` date NOT NULL,
  `product_category` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `product_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `quantity_produced` int NOT NULL,
  `production_line` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `operator_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `shift` enum('MORNING','AFTERNOON','NIGHT') COLLATE utf8mb4_unicode_ci DEFAULT 'MORNING',
  `quality_passed` int DEFAULT '0',
  `quality_failed` int DEFAULT '0',
  `downtime_minutes` int DEFAULT '0',
  `total_cost` decimal(12,2) DEFAULT '0.00',
  `efficiency_percentage` decimal(5,2) DEFAULT '0.00',
  `notes` text COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `products`
--

CREATE TABLE `products` (
  `product_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `product_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `product_code` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `category` enum('AUTOMOTIVE','ELECTRONICS','FURNITURE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `subcategory` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `unit_cost` decimal(10,2) NOT NULL DEFAULT '0.00',
  `selling_price` decimal(10,2) DEFAULT NULL,
  `specifications` text COLLATE utf8mb4_unicode_ci,
  `materials_required` text COLLATE utf8mb4_unicode_ci,
  `production_time_hours` decimal(5,2) DEFAULT '1.00',
  `quality_standards` text COLLATE utf8mb4_unicode_ci,
  `safety_requirements` text COLLATE utf8mb4_unicode_ci,
  `status` enum('ACTIVE','INACTIVE','DISCONTINUED','DEVELOPMENT') COLLATE utf8mb4_unicode_ci DEFAULT 'ACTIVE',
  `minimum_stock_level` int DEFAULT '0',
  `current_stock_level` int DEFAULT '0',
  `reorder_point` int DEFAULT '10',
  `supplier_info` text COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `products`
--

INSERT INTO `products` (`product_id`, `product_name`, `product_code`, `category`, `subcategory`, `unit_cost`, `selling_price`, `specifications`, `materials_required`, `production_time_hours`, `quality_standards`, `safety_requirements`, `status`, `minimum_stock_level`, `current_stock_level`, `reorder_point`, `supplier_info`, `created_at`, `updated_at`) VALUES
('AUTO-001', 'Sedan Car Frame', '', 'AUTOMOTIVE', NULL, '25000.00', NULL, 'Steel frame for 4-door sedan vehicles', NULL, '1.00', NULL, NULL, 'ACTIVE', 0, 0, 10, NULL, '2025-07-04 21:20:41', '2025-07-04 21:20:41');

-- --------------------------------------------------------

--
-- Table structure for table `roles`
--

CREATE TABLE `roles` (
  `role_id` int NOT NULL,
  `role_name` enum('ADMIN','USER') COLLATE utf8mb4_unicode_ci NOT NULL,
  `role_description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `permissions` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ;

--
-- Dumping data for table `roles`
--

INSERT INTO `roles` (`role_id`, `role_name`, `role_description`, `permissions`, `created_at`, `updated_at`) VALUES
(1, 'ADMIN', 'System Administrator with full access', '{\"user_management\": true, \"production_management\": true, \"reports\": true, \"system_settings\": true, \"all_orders\": true}', '2025-06-28 20:57:13', '2025-06-28 20:57:13'),
(2, 'USER', 'Regular user with limited access', '{\"user_management\": false, \"production_management\": false, \"reports\": true, \"system_settings\": false, \"own_orders\": true}', '2025-06-28 20:57:13', '2025-07-10 19:55:10');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int NOT NULL,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role_id` int NOT NULL,
  `full_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `department` enum('Production','Quality Control','Maintenance','Logistics','Administration','IT') COLLATE utf8mb4_unicode_ci DEFAULT 'Production',
  `employee_id` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `hire_date` date DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT '1',
  `last_login` timestamp NULL DEFAULT NULL,
  `login_attempts` int DEFAULT '0',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `failed_login_attempts` int DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `username`, `password`, `role_id`, `full_name`, `email`, `phone`, `department`, `employee_id`, `hire_date`, `is_active`, `last_login`, `login_attempts`, `created_at`, `updated_at`, `failed_login_attempts`) VALUES
(1, 'admin', 'admin123', 1, 'System Administrator', 'admin@manufacturing.com', NULL, 'IT', 'EMP001', '2025-06-29', 1, '2025-07-23 08:45:28', 1, '2025-06-28 20:57:13', '2025-07-23 08:45:28', 0),
(2, 'user', 'user123', 2, 'Production User', 'user@manufacturing.com', NULL, 'Production', 'EMP002', '2025-06-29', 1, '2025-07-23 09:04:22', 1, '2025-06-28 20:57:13', '2025-07-23 09:04:22', 0);

-- --------------------------------------------------------

--
-- Table structure for table `user_activity_log`
--

CREATE TABLE `user_activity_log` (
  `log_id` int NOT NULL,
  `user_id` int NOT NULL,
  `activity` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `ip_address` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `user_activity_log`
--

INSERT INTO `user_activity_log` (`log_id`, `user_id`, `activity`, `description`, `ip_address`, `timestamp`) VALUES
(1, 1, 'LOGIN', 'User logged in successfully', NULL, '2025-07-04 21:25:27'),
(2, 2, 'LOGIN', 'User logged in successfully', NULL, '2025-07-04 21:29:44'),
(3, 1, 'LOGIN', 'User logged in successfully', NULL, '2025-07-04 21:31:08'),
(4, 2, 'LOGIN', 'User logged in successfully', NULL, '2025-07-04 21:32:37'),
(5, 1, 'LOGIN', 'User logged in successfully', NULL, '2025-07-04 21:33:58'),
(6, 2, 'LOGIN', 'User logged in successfully', NULL, '2025-07-04 21:36:30'),
(7, 1, 'LOGIN', 'User logged in successfully', NULL, '2025-07-05 00:27:44'),
(8, 1, 'LOGIN', 'User logged in successfully', NULL, '2025-07-05 00:51:47'),
(9, 1, 'LOGIN', 'User logged in successfully', NULL, '2025-07-07 04:47:28');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `audit_log`
--
ALTER TABLE `audit_log`
  ADD PRIMARY KEY (`log_id`);

--
-- Indexes for table `production_lines`
--
ALTER TABLE `production_lines`
  ADD PRIMARY KEY (`line_id`),
  ADD UNIQUE KEY `line_name` (`line_name`),
  ADD UNIQUE KEY `line_code` (`line_code`),
  ADD KEY `supervisor_id` (`supervisor_id`),
  ADD KEY `idx_line_type` (`line_type`),
  ADD KEY `idx_status` (`status`),
  ADD KEY `idx_location` (`location`);

--
-- Indexes for table `production_orders`
--
ALTER TABLE `production_orders`
  ADD PRIMARY KEY (`order_id`),
  ADD KEY `assigned_user_id` (`assigned_user_id`),
  ADD KEY `idx_status` (`status`),
  ADD KEY `idx_priority` (`priority`),
  ADD KEY `idx_assigned_operator` (`assigned_operator`);

--
-- Indexes for table `production_reports`
--
ALTER TABLE `production_reports`
  ADD PRIMARY KEY (`report_id`),
  ADD KEY `order_id` (`order_id`),
  ADD KEY `idx_production_date` (`production_date`),
  ADD KEY `idx_product_category` (`product_category`);

--
-- Indexes for table `products`
--
ALTER TABLE `products`
  ADD PRIMARY KEY (`product_id`),
  ADD UNIQUE KEY `product_code` (`product_code`),
  ADD KEY `idx_category` (`category`),
  ADD KEY `idx_status` (`status`),
  ADD KEY `idx_product_code` (`product_code`);
ALTER TABLE `products` ADD FULLTEXT KEY `idx_product_search` (`product_name`,`specifications`);

--
-- Indexes for table `roles`
--
ALTER TABLE `roles`
  ADD PRIMARY KEY (`role_id`),
  ADD UNIQUE KEY `role_name` (`role_name`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD KEY `role_id` (`role_id`),
  ADD KEY `idx_username` (`username`),
  ADD KEY `idx_email` (`email`),
  ADD KEY `idx_department` (`department`),
  ADD KEY `idx_is_active` (`is_active`);

--
-- Indexes for table `user_activity_log`
--
ALTER TABLE `user_activity_log`
  ADD PRIMARY KEY (`log_id`),
  ADD KEY `user_id` (`user_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `audit_log`
--
ALTER TABLE `audit_log`
  MODIFY `log_id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `production_lines`
--
ALTER TABLE `production_lines`
  MODIFY `line_id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=44;

--
-- AUTO_INCREMENT for table `production_reports`
--
ALTER TABLE `production_reports`
  MODIFY `report_id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `roles`
--
ALTER TABLE `roles`
  MODIFY `role_id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=31;

--
-- AUTO_INCREMENT for table `user_activity_log`
--
ALTER TABLE `user_activity_log`
  MODIFY `log_id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `production_lines`
--
ALTER TABLE `production_lines`
  ADD CONSTRAINT `production_lines_ibfk_1` FOREIGN KEY (`supervisor_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `production_orders`
--
ALTER TABLE `production_orders`
  ADD CONSTRAINT `production_orders_ibfk_1` FOREIGN KEY (`assigned_user_id`) REFERENCES `users` (`user_id`);

--
-- Constraints for table `production_reports`
--
ALTER TABLE `production_reports`
  ADD CONSTRAINT `production_reports_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `production_orders` (`order_id`);

--
-- Constraints for table `users`
--
ALTER TABLE `users`
  ADD CONSTRAINT `users_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `roles` (`role_id`) ON UPDATE CASCADE;

--
-- Constraints for table `user_activity_log`
--
ALTER TABLE `user_activity_log`
  ADD CONSTRAINT `user_activity_log_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
