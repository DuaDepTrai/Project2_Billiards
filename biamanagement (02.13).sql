-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Feb 12, 2025 at 06:09 PM
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
  `booking_id` int(11) NOT NULL,
  `order_id` int(11) NOT NULL,
  `table_id` int(11) NOT NULL,
  `start_time` timestamp NOT NULL DEFAULT current_timestamp(),
  `end_time` timestamp NULL DEFAULT NULL,
  `timeplay` double DEFAULT NULL,
  `subtotal` double DEFAULT NULL,
  `net_total` double DEFAULT NULL,
  `booking_status` enum('Order','Playing','Finish','Canceled') NOT NULL DEFAULT 'Playing',
  `promotion_id` int(10) UNSIGNED DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `bookings`
--

INSERT INTO `bookings` (`booking_id`, `order_id`, `table_id`, `start_time`, `end_time`, `timeplay`, `subtotal`, `net_total`, `booking_status`, `promotion_id`) VALUES
(1, 1, 1, '2024-12-31 20:00:00', '2024-12-31 22:00:00', 2, 70000, 70000, 'Finish', NULL),
(2, 2, 2, '2025-01-02 00:00:00', '2025-01-02 02:30:00', 2.5, 187500, 187500, 'Finish', NULL),
(3, 3, 3, '2025-01-03 04:00:00', '2025-01-03 05:30:00', 1.5, 150000, 150000, 'Finish', NULL),
(4, 4, 1, '2025-01-03 19:00:00', '2025-01-03 20:00:00', 1, 35000, 35000, 'Finish', NULL),
(5, 5, 2, '2025-01-04 21:00:00', '2025-01-04 22:45:00', 1.75, 131250, 131250, 'Finish', NULL),
(6, 6, 3, '2025-01-05 23:00:00', '2025-01-06 01:00:00', 2, 200000, 200000, 'Finish', NULL),
(7, 7, 1, '2025-01-07 02:00:00', '2025-01-07 03:30:00', 1.5, 52500, 52500, 'Finish', NULL),
(8, 8, 2, '2025-01-08 03:00:00', '2025-01-08 04:00:00', 1, 75000, 75000, 'Finish', NULL),
(9, 9, 3, '2025-01-09 04:00:00', NULL, NULL, NULL, NULL, 'Finish', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `category`
--

CREATE TABLE `category` (
  `category_id` int(11) NOT NULL,
  `category_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `image_path` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `category`
--

INSERT INTO `category` (`category_id`, `category_name`, `image_path`) VALUES
(1, 'Cues-sale', 'cues-sale.png'),
(2, 'Cues-rent', 'cues-rent.png'),
(3, 'Drinks', 'drinks.png'),
(4, 'Food', 'food.png'),
(5, 'Board Game', 'board_game.png');

-- --------------------------------------------------------

--
-- Table structure for table `cate_pooltables`
--

CREATE TABLE `cate_pooltables` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `price` double NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `cate_pooltables`
--

INSERT INTO `cate_pooltables` (`id`, `name`, `price`) VALUES
(1, 'Standard Pool', 35000),
(2, 'Deluxe Pool', 75000),
(3, 'VIP Pool', 100000);

-- --------------------------------------------------------

--
-- Table structure for table `customers`
--

CREATE TABLE `customers` (
  `customer_id` int(11) NOT NULL,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `phone` varchar(255) NOT NULL,
  `total_playtime` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `customers`
--

INSERT INTO `customers` (`customer_id`, `name`, `phone`, `total_playtime`) VALUES
(1, 'Nguyen Anh Tuan', '1234567890', 0),
(2, 'Le Thi Mai', '2345678901', 0),
(3, 'Tran Minh Tu', '3456789012', 0),
(4, 'Phan Quoc Toan', '4567890123', 0),
(5, 'Hoang Minh Thao', '5678901234', 0);

-- --------------------------------------------------------

--
-- Table structure for table `orders`
--

CREATE TABLE `orders` (
  `order_id` int(11) NOT NULL,
  `customer_id` int(11) NOT NULL,
  `total_cost` double DEFAULT NULL,
  `order_status` enum('Order','Playing','Finished','Paid','Canceled') NOT NULL DEFAULT 'Playing'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `orders`
--

INSERT INTO `orders` (`order_id`, `customer_id`, `total_cost`, `order_status`) VALUES
(1, 1, 1345000, 'Finished'),
(2, 2, 1647500, 'Paid'),
(3, 3, 3280000, 'Finished'),
(4, 4, 695000, 'Finished'),
(5, 5, 1513750, 'Finished'),
(6, 1, 300000, 'Finished'),
(7, 2, 202500, 'Finished'),
(8, 3, 225000, 'Paid'),
(9, 4, 0, 'Canceled');

-- --------------------------------------------------------

--
-- Table structure for table `orders_items`
--

CREATE TABLE `orders_items` (
  `order_item_id` int(11) NOT NULL,
  `order_id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL,
  `quantity` int(11) NOT NULL,
  `subtotal` double DEFAULT NULL,
  `net_total` double DEFAULT NULL,
  `promotion_id` int(10) UNSIGNED DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `orders_items`
--

INSERT INTO `orders_items` (`order_item_id`, `order_id`, `product_id`, `quantity`, `subtotal`, `net_total`, `promotion_id`) VALUES
(1, 1, 1, 2, 1000000, 1000000, NULL),
(2, 1, 12, 3, 60000, 60000, NULL),
(3, 1, 7, 1, 15000, 15000, NULL),
(4, 2, 2, 1, 1000000, 1000000, NULL),
(5, 2, 13, 2, 60000, 60000, NULL),
(6, 2, 8, 1, 25000, 25000, NULL),
(7, 3, 3, 2, 3000000, 3000000, NULL),
(8, 3, 14, 1, 25000, 25000, NULL),
(9, 3, 10, 1, 30000, 30000, NULL),
(10, 4, 1, 1, 500000, 500000, NULL),
(11, 4, 15, 1, 40000, 40000, NULL),
(12, 4, 11, 1, 20000, 20000, NULL),
(13, 5, 2, 1, 1000000, 1000000, NULL),
(14, 5, 16, 2, 100000, 100000, NULL),
(15, 5, 9, 2, 20000, 20000, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `permissions`
--

CREATE TABLE `permissions` (
  `permission_id` int(11) NOT NULL,
  `permission_name` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `pooltables`
--

CREATE TABLE `pooltables` (
  `cate_id` int(11) NOT NULL,
  `table_id` int(11) NOT NULL,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `status` enum('Available','Ordered','Playing') NOT NULL DEFAULT 'Available'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `pooltables`
--

INSERT INTO `pooltables` (`cate_id`, `table_id`, `name`, `status`) VALUES
(1, 1, 'Standard Pool 1', 'Playing'),
(2, 2, 'Deluxe Pool 1', 'Ordered'),
(3, 3, 'VIP Pool 1', 'Playing'),
(1, 4, 'Standard Pool 2', 'Available'),
(2, 5, 'Deluxe Pool 2', 'Available'),
(3, 6, 'VIP Pool 2', 'Available'),
(1, 7, 'Standard Pool 3', 'Available'),
(2, 8, 'Deluxe Pool 3', 'Available'),
(3, 9, 'VIP Pool 3', 'Available');

-- --------------------------------------------------------

--
-- Table structure for table `products`
--

CREATE TABLE `products` (
  `product_id` int(11) NOT NULL,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `category_id` int(11) NOT NULL,
  `price` double DEFAULT NULL,
  `unit` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `quantity` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `products`
--

INSERT INTO `products` (`product_id`, `name`, `category_id`, `price`, `unit`, `quantity`) VALUES
(1, 'Standard Cue - Sale', 1, 500000, 'Piece', 20),
(2, 'Deluxe Cue - Sale', 1, 1000000, 'Piece', 15),
(3, 'Professional Cue - Sale', 1, 1500000, 'Piece', 10),
(4, 'Standard Cue - Rent', 2, 50000, 'hour', 10),
(5, 'Deluxe Cue - Rent', 2, 100000, 'hour', 10),
(6, 'Professional Cue - Rent', 2, 150000, 'hour', 10),
(7, 'Soda', 3, 15000, 'Can', 100),
(8, 'Juice', 3, 25000, 'Bottle', 80),
(9, 'Water', 3, 10000, 'Bottle', 120),
(10, 'Coffee', 3, 30000, 'Cup', 50),
(11, 'Tea', 3, 20000, 'Cup', 60),
(12, 'Chips', 4, 20000, 'Bag', 50),
(13, 'Nuts', 4, 30000, 'Bag', 40),
(14, 'Popcorn', 4, 25000, 'Bag', 60),
(15, 'Chocolate', 4, 40000, 'Bar', 30),
(16, 'Cookies', 4, 50000, 'Box', 25),
(17, 'Coca Cola', 3, 20000, 'Can', 20);

-- --------------------------------------------------------

--
-- Table structure for table `promotions`
--

CREATE TABLE `promotions` (
  `promotion_id` int(11) UNSIGNED NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `promotion_type` enum('LoyaltyCustomer','SinglePlaytime','Combo','NoPromotion') DEFAULT NULL,
  `discount` double DEFAULT NULL,
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `promotions`
--

INSERT INTO `promotions` (`promotion_id`, `name`, `promotion_type`, `discount`, `description`) VALUES
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
  `rent_cue_id` int(11) NOT NULL,
  `order_id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL,
  `start_time` timestamp NOT NULL DEFAULT current_timestamp(),
  `end_time` timestamp NULL DEFAULT NULL,
  `status` enum('Rented','Available') NOT NULL DEFAULT 'Available',
  `timeplay` double DEFAULT NULL,
  `subtotal` double DEFAULT NULL,
  `net_total` double DEFAULT NULL,
  `promotion_id` int(10) UNSIGNED DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `rent_cues`
--

INSERT INTO `rent_cues` (`rent_cue_id`, `order_id`, `product_id`, `start_time`, `end_time`, `status`, `timeplay`, `subtotal`, `net_total`, `promotion_id`) VALUES
(1, 1, 4, '2024-12-31 20:00:00', '2024-12-31 22:00:00', 'Available', 2, 100000, 100000, NULL),
(2, 1, 5, '2024-12-31 20:00:00', '2024-12-31 21:00:00', 'Available', 1, 100000, 100000, NULL),
(3, 2, 6, '2025-01-02 00:00:00', '2025-01-02 02:30:00', 'Available', 2.5, 375000, 375000, NULL),
(4, 3, 4, '2025-01-03 04:00:00', '2025-01-03 05:30:00', 'Available', 1.5, 75000, 75000, NULL),
(5, 4, 5, '2025-01-03 19:00:00', '2025-01-03 20:00:00', 'Available', 1, 100000, 100000, NULL),
(6, 5, 6, '2025-01-04 21:00:00', '2025-01-04 22:45:00', 'Available', 1.75, 262500, 262500, NULL),
(7, 6, 4, '2025-01-05 23:00:00', '2025-01-06 01:00:00', 'Available', 2, 100000, 100000, NULL),
(8, 7, 5, '2025-01-07 02:00:00', '2025-01-07 03:30:00', 'Available', 1.5, 150000, 150000, NULL),
(9, 8, 6, '2025-01-08 03:00:00', '2025-01-08 04:00:00', 'Available', 1, 150000, 150000, NULL);

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
  `role_name` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `roles`
--

INSERT INTO `roles` (`role_id`, `role_name`) VALUES
(1, 'Admin'),
(2, 'Receptionist'),
(3, 'Warehouse');

-- --------------------------------------------------------

--
-- Table structure for table `role_permission`
--

CREATE TABLE `role_permission` (
  `role_id` int(11) NOT NULL,
  `permission_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `username` varchar(100) NOT NULL,
  `password` varchar(100) NOT NULL,
  `role_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `username`, `password`, `role_id`) VALUES
(1, 'admin', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 1),
(2, 'hieu', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 1),
(3, 'quan', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 1),
(4, 'manh', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 1),
(6, 'long', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 1),
(7, 'receptionist1', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 2),
(8, 'whstaff1', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 3);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `bookings`
--
ALTER TABLE `bookings`
  ADD PRIMARY KEY (`booking_id`),
  ADD KEY `orders_id` (`order_id`),
  ADD KEY `tables_id` (`table_id`),
  ADD KEY `promotion_id` (`promotion_id`);

--
-- Indexes for table `category`
--
ALTER TABLE `category`
  ADD PRIMARY KEY (`category_id`);

--
-- Indexes for table `cate_pooltables`
--
ALTER TABLE `cate_pooltables`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `customers`
--
ALTER TABLE `customers`
  ADD PRIMARY KEY (`customer_id`);

--
-- Indexes for table `orders`
--
ALTER TABLE `orders`
  ADD PRIMARY KEY (`order_id`),
  ADD KEY `customers_id` (`customer_id`);

--
-- Indexes for table `orders_items`
--
ALTER TABLE `orders_items`
  ADD PRIMARY KEY (`order_item_id`),
  ADD KEY `orders_id` (`order_id`),
  ADD KEY `products_id` (`product_id`),
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
  ADD PRIMARY KEY (`table_id`),
  ADD KEY `pooltables_cate_pooltables_fk` (`cate_id`);

--
-- Indexes for table `products`
--
ALTER TABLE `products`
  ADD PRIMARY KEY (`product_id`),
  ADD KEY `category_id` (`category_id`);

--
-- Indexes for table `promotions`
--
ALTER TABLE `promotions`
  ADD PRIMARY KEY (`promotion_id`);

--
-- Indexes for table `rent_cues`
--
ALTER TABLE `rent_cues`
  ADD PRIMARY KEY (`rent_cue_id`),
  ADD KEY `orders_id` (`order_id`),
  ADD KEY `products_id` (`product_id`),
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
-- Indexes for table `role_permission`
--
ALTER TABLE `role_permission`
  ADD PRIMARY KEY (`role_id`,`permission_id`),
  ADD KEY `permission_id` (`permission_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `users_unique` (`username`),
  ADD KEY `users_roles_FK` (`role_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `bookings`
--
ALTER TABLE `bookings`
  MODIFY `booking_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=24;

--
-- AUTO_INCREMENT for table `category`
--
ALTER TABLE `category`
  MODIFY `category_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=20;

--
-- AUTO_INCREMENT for table `cate_pooltables`
--
ALTER TABLE `cate_pooltables`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `customers`
--
ALTER TABLE `customers`
  MODIFY `customer_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `orders`
--
ALTER TABLE `orders`
  MODIFY `order_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=25;

--
-- AUTO_INCREMENT for table `orders_items`
--
ALTER TABLE `orders_items`
  MODIFY `order_item_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=47;

--
-- AUTO_INCREMENT for table `permissions`
--
ALTER TABLE `permissions`
  MODIFY `permission_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `pooltables`
--
ALTER TABLE `pooltables`
  MODIFY `table_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `products`
--
ALTER TABLE `products`
  MODIFY `product_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT for table `promotions`
--
ALTER TABLE `promotions`
  MODIFY `promotion_id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `rent_cues`
--
ALTER TABLE `rent_cues`
  MODIFY `rent_cue_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;

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
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `bookings`
--
ALTER TABLE `bookings`
  ADD CONSTRAINT `bookings_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `bookings_ibfk_2` FOREIGN KEY (`table_id`) REFERENCES `pooltables` (`table_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `bookings_ibfk_3` FOREIGN KEY (`promotion_id`) REFERENCES `promotions` (`promotion_id`);

--
-- Constraints for table `orders`
--
ALTER TABLE `orders`
  ADD CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `orders_items`
--
ALTER TABLE `orders_items`
  ADD CONSTRAINT `orders_items_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `orders_items_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `orders_items_ibfk_3` FOREIGN KEY (`promotion_id`) REFERENCES `promotions` (`promotion_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `pooltables`
--
ALTER TABLE `pooltables`
  ADD CONSTRAINT `pooltables_cate_pooltables_fk` FOREIGN KEY (`cate_id`) REFERENCES `cate_pooltables` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `products`
--
ALTER TABLE `products`
  ADD CONSTRAINT `products_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `rent_cues`
--
ALTER TABLE `rent_cues`
  ADD CONSTRAINT `rent_cues_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `rent_cues_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `rent_cues_ibfk_3` FOREIGN KEY (`promotion_id`) REFERENCES `promotions` (`promotion_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `role_permission`
--
ALTER TABLE `role_permission`
  ADD CONSTRAINT `role_permission_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `roles` (`role_id`),
  ADD CONSTRAINT `role_permission_ibfk_2` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`permission_id`);

--
-- Constraints for table `users`
--
ALTER TABLE `users`
  ADD CONSTRAINT `users_roles_FK` FOREIGN KEY (`role_id`) REFERENCES `roles` (`role_id`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
