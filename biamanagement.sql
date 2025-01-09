-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jan 09, 2025 at 04:44 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `biamanagement`
--

-- --------------------------------------------------------

--
-- Table structure for table `bookings`
--

CREATE TABLE `bookings` (
  `bookings_id` int(11) NOT NULL,
  `orders_id` int(11) DEFAULT NULL,
  `tables_id` int(11) DEFAULT NULL,
  `start_time` timestamp NOT NULL DEFAULT current_timestamp(),
  `end_time` timestamp NULL DEFAULT NULL,
  `timeplay` double DEFAULT NULL,
  `net_total` double DEFAULT NULL,
  `subtotal` double NOT NULL,
  `promotion_id` int(10) UNSIGNED NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `category`
--

CREATE TABLE `category` (
  `category_id` int(11) NOT NULL,
  `category_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `category`
--

INSERT INTO `category` (`category_id`, `category_name`) VALUES
(1, ' Sale Cues'),
(2, ' Rent Cues'),
(3, 'Drinks'),
(4, 'Food');

-- --------------------------------------------------------

--
-- Table structure for table `customers`
--

CREATE TABLE `customers` (
  `customers_id` int(11) NOT NULL,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `total_playtime` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `customers`
--

INSERT INTO `customers` (`customers_id`, `name`, `phone`, `total_playtime`) VALUES
(6, 'Nguyen Anh Tuan', '1234567890', 0),
(7, 'Le Thi Mai', '2345678901', 0),
(8, 'Tran Minh Tu', '3456789012', 0),
(9, 'Phan Quoc Toan', '4567890123', 0),
(10, 'Hoang Minh Thao', '5678901234', 0);

-- --------------------------------------------------------

--
-- Table structure for table `employees`
--

CREATE TABLE `employees` (
  `employee_id` int(11) NOT NULL,
  `username` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `password` varchar(255) NOT NULL,
  `name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `phone` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `role_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `employees`
--

INSERT INTO `employees` (`employee_id`, `username`, `password`, `name`, `phone`, `role_id`) VALUES
(6, 'nguyenvana', 'matkhau123', 'Nguyễn Văn A', '0987654321', 1),
(7, 'tranthib', 'baomat456', 'Trần Thị B', '0976543210', 2),
(8, 'phamvanc', 'phanmem789', 'Phạm Văn C', '0965432109', 3),
(9, 'ledangd', 'dangky321', 'Lê Đăng D', '0954321098', 1),
(10, 'dothie', 'matkhau987', 'Đỗ Thị E', '0943210987', 2);

-- --------------------------------------------------------

--
-- Table structure for table `orders`
--

CREATE TABLE `orders` (
  `orders_id` int(11) NOT NULL,
  `customers_id` int(11) DEFAULT NULL,
  `total_cost` double DEFAULT NULL,
  `order_status` enum('đã book','đang chơi','kết thúc') NOT NULL DEFAULT 'đã book'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `orders`
--

INSERT INTO `orders` (`orders_id`, `customers_id`, `total_cost`, `order_status`) VALUES
(14, 6, 850000, ''),
(15, 7, 1250000, ''),
(16, 8, 950000, ''),
(17, 9, 1100000, ''),
(18, 10, 1350000, ''),
(19, 6, 800000, ''),
(20, 7, 950000, ''),
(21, 8, 1150000, ''),
(22, 9, 1200000, ''),
(23, 10, 1400000, '');

-- --------------------------------------------------------

--
-- Table structure for table `orders_items`
--

CREATE TABLE `orders_items` (
  `orders_items_id` int(11) NOT NULL,
  `orders_id` int(11) DEFAULT NULL,
  `products_id` int(11) DEFAULT NULL,
  `quantity` int(11) DEFAULT NULL,
  `net_total` double DEFAULT NULL,
  `subtotal` double NOT NULL,
  `promotion_id` int(10) UNSIGNED NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `orders_items`
--

INSERT INTO `orders_items` (`orders_items_id`, `orders_id`, `products_id`, `quantity`, `net_total`, `subtotal`, `promotion_id`) VALUES
(31, 14, 1, 2, 95000, 100000, 1),
(32, 14, 12, 3, 45000, 50000, 2),
(33, 14, 7, 1, 19000, 20000, 3),
(34, 15, 2, 1, 66500, 70000, 4),
(35, 15, 13, 2, 47500, 50000, 5),
(36, 15, 8, 1, 28500, 30000, 6),
(37, 16, 3, 2, 190000, 200000, 7),
(38, 16, 14, 1, 9500, 10000, 8),
(39, 16, 10, 1, 47500, 50000, 9),
(40, 17, 1, 1, 47500, 50000, 10),
(41, 17, 15, 1, 28500, 30000, 1),
(42, 17, 11, 1, 47500, 50000, 2),
(43, 18, 2, 1, 66500, 70000, 3),
(44, 18, 16, 2, 38000, 40000, 4),
(45, 18, 9, 2, 47500, 50000, 5);

-- --------------------------------------------------------

--
-- Table structure for table `permissions`
--

CREATE TABLE `permissions` (
  `permission_id` int(11) NOT NULL,
  `permission_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `pooltables`
--

CREATE TABLE `pooltables` (
  `tables_id` int(11) NOT NULL,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `price` double DEFAULT NULL,
  `status` enum('Available','Ordered','Playing') DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `pooltables`
--

INSERT INTO `pooltables` (`tables_id`, `name`, `price`, `status`) VALUES
(1, 'Standard Pool Table', 350000, 'Available'),
(2, 'Deluxe Pool Table', 750000, 'Ordered'),
(3, 'VIP Pool Table', 1000000, 'Playing');

-- --------------------------------------------------------

--
-- Table structure for table `products`
--

CREATE TABLE `products` (
  `products_id` int(11) NOT NULL,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `category_id` int(11) DEFAULT NULL,
  `price` double DEFAULT NULL,
  `unit` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `quantity` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `products`
--

INSERT INTO `products` (`products_id`, `name`, `category_id`, `price`, `unit`, `quantity`) VALUES
(1, 'Standard Cue - Sale', 1, 500000, 'Piece', 20),
(2, 'Deluxe Cue - Sale', 1, 1000000, 'Piece', 15),
(3, 'Professional Cue - Sale', 1, 1500000, 'Piece', 10),
(4, 'Standard Cue - Rent', 2, 50000, 'Hour', 10),
(5, 'Deluxe Cue - Rent', 2, 70000, 'Hour', 8),
(6, 'Professional Cue - Rent', 2, 100000, 'Hour', 5),
(7, 'Soda', 3, 15000, 'Can', 100),
(8, 'Juice', 3, 25000, 'Bottle', 80),
(9, 'Water', 3, 10000, 'Bottle', 120),
(10, 'Coffee', 3, 30000, 'Cup', 50),
(11, 'Tea', 3, 20000, 'Cup', 60),
(12, 'Chips', 4, 20000, 'Bag', 50),
(13, 'Nuts', 4, 30000, 'Bag', 40),
(14, 'Popcorn', 4, 25000, 'Bag', 60),
(15, 'Chocolate', 4, 40000, 'Bar', 30),
(16, 'Cookies', 4, 50000, 'Box', 25);

-- --------------------------------------------------------

--
-- Table structure for table `promotions`
--

CREATE TABLE `promotions` (
  `id` int(11) UNSIGNED NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `promotion_type` enum('LoyaltyCustomer','SinglePlaytime','Combo','NoPromotion') DEFAULT NULL,
  `discount` double DEFAULT NULL,
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `promotions`
--

INSERT INTO `promotions` (`id`, `name`, `promotion_type`, `discount`, `description`) VALUES
(1, 'Loyalty Reward', 'LoyaltyCustomer', 10, 'Giảm giá 10% cho khách hàng thân thiết.'),
(2, 'Weekend Special', 'SinglePlaytime', 15, 'Giảm giá 15% cho các lượt chơi vào cuối tuần.'),
(3, 'Combo Offer', 'Combo', 20, 'Mua 1 gậy thuê + 1 đồ uống + 1 đồ ăn, giảm giá 20%.'),
(4, 'Happy Hour', 'SinglePlaytime', 25, 'Giảm giá 25% cho các lượt chơi từ 15:00 đến 17:00.'),
(5, 'New Customer Discount', 'LoyaltyCustomer', 5, 'Giảm giá 5% cho khách hàng mới lần đầu sử dụng dịch vụ.'),
(6, 'Holiday Deal', 'Combo', 30, 'Ưu đãi 30% cho combo 2 gậy thuê + 2 đồ uống + 2 đồ ăn vào các ngày lễ.'),
(7, 'Birthday Special', 'NoPromotion', 50, 'Giảm giá 50% cho khách hàng vào ngày sinh nhật.'),
(8, 'Group Discount', 'Combo', 15, 'Giảm giá 15% cho các nhóm từ 4 người trở lên.'),
(9, 'Student Offer', 'SinglePlaytime', 10, 'Giảm giá 10% cho sinh viên có thẻ sinh viên hợp lệ.'),
(10, 'Early Bird Discount', 'SinglePlaytime', 20, 'Giảm giá 20% cho các lượt chơi trước 12:00 trưa.');

-- --------------------------------------------------------

--
-- Table structure for table `rent_cues`
--

CREATE TABLE `rent_cues` (
  `rent_cues_id` int(11) NOT NULL,
  `orders_id` int(11) DEFAULT NULL,
  `products_id` int(11) DEFAULT NULL,
  `start_time` timestamp NOT NULL DEFAULT current_timestamp(),
  `end_time` timestamp NULL DEFAULT NULL,
  `timeplay` double DEFAULT NULL,
  `net_total` double DEFAULT NULL,
  `subtotal` double NOT NULL,
  `promotion_id` int(10) UNSIGNED DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `rent_cues`
--

INSERT INTO `rent_cues` (`rent_cues_id`, `orders_id`, `products_id`, `start_time`, `end_time`, `timeplay`, `net_total`, `subtotal`, `promotion_id`) VALUES
(11, 14, 1, '2025-01-01 03:00:00', '2025-01-01 05:00:00', 2, 100000, 95000, 1),
(12, 15, 2, '2025-01-02 08:00:00', '2025-01-02 09:30:00', 1.5, 105000, 100000, 2),
(13, 16, 3, '2025-01-03 11:00:00', '2025-01-03 13:30:00', 2.5, 250000, 240000, 3),
(14, 17, 1, '2025-01-04 02:00:00', '2025-01-04 04:00:00', 2, 100000, 95000, 4),
(15, 18, 2, '2025-01-05 07:00:00', '2025-01-05 08:30:00', 1.5, 105000, 100000, 5),
(16, 19, 3, '2025-01-06 12:00:00', '2025-01-06 15:00:00', 3, 300000, 270000, 6),
(17, 20, 1, '2025-01-07 03:00:00', '2025-01-07 04:00:00', 1, 50000, 47500, 7),
(18, 21, 2, '2025-01-08 06:00:00', '2025-01-08 08:00:00', 2, 140000, 133000, 8),
(19, 22, 3, '2025-01-09 09:00:00', '2025-01-09 11:30:00', 2.5, 250000, 237500, 9),
(20, 23, 1, '2025-01-10 02:00:00', '2025-01-10 04:00:00', 2, 100000, 95000, 10);

-- --------------------------------------------------------

--
-- Table structure for table `revenue`
--

CREATE TABLE `revenue` (
  `revenue_id` int(11) NOT NULL,
  `date` date DEFAULT NULL,
  `total_revenue` int(11) DEFAULT NULL,
  `total_customers` int(11) DEFAULT NULL,
  `total_orders` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `roles`
--

CREATE TABLE `roles` (
  `role_id` int(11) NOT NULL,
  `role_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `roles`
--

INSERT INTO `roles` (`role_id`, `role_name`, `description`) VALUES
(1, 'Administrator', 'Manages the entire system'),
(2, 'Sales Staff', 'Handles orders and customer support'),
(3, 'Warehouse Staff', 'Manages goods in the warehouse');

-- --------------------------------------------------------

--
-- Table structure for table `role_permissions`
--

CREATE TABLE `role_permissions` (
  `role_permission_id` int(11) NOT NULL,
  `role_id` int(11) NOT NULL,
  `permission_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `bookings`
--
ALTER TABLE `bookings`
  ADD PRIMARY KEY (`bookings_id`),
  ADD KEY `orders_id` (`orders_id`),
  ADD KEY `tables_id` (`tables_id`),
  ADD KEY `promotion_id` (`promotion_id`);

--
-- Indexes for table `category`
--
ALTER TABLE `category`
  ADD PRIMARY KEY (`category_id`);

--
-- Indexes for table `customers`
--
ALTER TABLE `customers`
  ADD PRIMARY KEY (`customers_id`);

--
-- Indexes for table `employees`
--
ALTER TABLE `employees`
  ADD PRIMARY KEY (`employee_id`),
  ADD KEY `role_id` (`role_id`);

--
-- Indexes for table `orders`
--
ALTER TABLE `orders`
  ADD PRIMARY KEY (`orders_id`),
  ADD KEY `customers_id` (`customers_id`);

--
-- Indexes for table `orders_items`
--
ALTER TABLE `orders_items`
  ADD PRIMARY KEY (`orders_items_id`),
  ADD KEY `orders_id` (`orders_id`),
  ADD KEY `products_id` (`products_id`),
  ADD KEY `promotion_id` (`promotion_id`);

--
-- Indexes for table `permissions`
--
ALTER TABLE `permissions`
  ADD PRIMARY KEY (`permission_id`);

--
-- Indexes for table `pooltables`
--
ALTER TABLE `pooltables`
  ADD PRIMARY KEY (`tables_id`);

--
-- Indexes for table `products`
--
ALTER TABLE `products`
  ADD PRIMARY KEY (`products_id`),
  ADD KEY `category_id` (`category_id`);

--
-- Indexes for table `promotions`
--
ALTER TABLE `promotions`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `rent_cues`
--
ALTER TABLE `rent_cues`
  ADD PRIMARY KEY (`rent_cues_id`),
  ADD KEY `orders_id` (`orders_id`),
  ADD KEY `products_id` (`products_id`),
  ADD KEY `promotion_id` (`promotion_id`);

--
-- Indexes for table `revenue`
--
ALTER TABLE `revenue`
  ADD PRIMARY KEY (`revenue_id`);

--
-- Indexes for table `roles`
--
ALTER TABLE `roles`
  ADD PRIMARY KEY (`role_id`);

--
-- Indexes for table `role_permissions`
--
ALTER TABLE `role_permissions`
  ADD PRIMARY KEY (`role_permission_id`),
  ADD KEY `role_id` (`role_id`),
  ADD KEY `permission_id` (`permission_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `bookings`
--
ALTER TABLE `bookings`
  MODIFY `bookings_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- AUTO_INCREMENT for table `category`
--
ALTER TABLE `category`
  MODIFY `category_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `customers`
--
ALTER TABLE `customers`
  MODIFY `customers_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `employees`
--
ALTER TABLE `employees`
  MODIFY `employee_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `orders`
--
ALTER TABLE `orders`
  MODIFY `orders_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=24;

--
-- AUTO_INCREMENT for table `orders_items`
--
ALTER TABLE `orders_items`
  MODIFY `orders_items_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=46;

--
-- AUTO_INCREMENT for table `permissions`
--
ALTER TABLE `permissions`
  MODIFY `permission_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `pooltables`
--
ALTER TABLE `pooltables`
  MODIFY `tables_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `products`
--
ALTER TABLE `products`
  MODIFY `products_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- AUTO_INCREMENT for table `promotions`
--
ALTER TABLE `promotions`
  MODIFY `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `rent_cues`
--
ALTER TABLE `rent_cues`
  MODIFY `rent_cues_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;

--
-- AUTO_INCREMENT for table `revenue`
--
ALTER TABLE `revenue`
  MODIFY `revenue_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `roles`
--
ALTER TABLE `roles`
  MODIFY `role_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `role_permissions`
--
ALTER TABLE `role_permissions`
  MODIFY `role_permission_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `bookings`
--
ALTER TABLE `bookings`
  ADD CONSTRAINT `bookings_ibfk_1` FOREIGN KEY (`orders_id`) REFERENCES `orders` (`orders_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `bookings_ibfk_2` FOREIGN KEY (`tables_id`) REFERENCES `pooltables` (`tables_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `bookings_ibfk_3` FOREIGN KEY (`promotion_id`) REFERENCES `promotions` (`id`);

--
-- Constraints for table `employees`
--
ALTER TABLE `employees`
  ADD CONSTRAINT `employees_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `roles` (`role_id`);

--
-- Constraints for table `orders`
--
ALTER TABLE `orders`
  ADD CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`customers_id`) REFERENCES `customers` (`customers_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `orders_items`
--
ALTER TABLE `orders_items`
  ADD CONSTRAINT `orders_items_ibfk_1` FOREIGN KEY (`orders_id`) REFERENCES `orders` (`orders_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `orders_items_ibfk_2` FOREIGN KEY (`products_id`) REFERENCES `products` (`products_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `orders_items_ibfk_3` FOREIGN KEY (`promotion_id`) REFERENCES `promotions` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `products`
--
ALTER TABLE `products`
  ADD CONSTRAINT `products_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `rent_cues`
--
ALTER TABLE `rent_cues`
  ADD CONSTRAINT `rent_cues_ibfk_1` FOREIGN KEY (`orders_id`) REFERENCES `orders` (`orders_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `rent_cues_ibfk_2` FOREIGN KEY (`products_id`) REFERENCES `products` (`products_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `rent_cues_ibfk_3` FOREIGN KEY (`promotion_id`) REFERENCES `promotions` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `role_permissions`
--
ALTER TABLE `role_permissions`
  ADD CONSTRAINT `role_permissions_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `roles` (`role_id`),
  ADD CONSTRAINT `role_permissions_ibfk_2` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`permission_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
