-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jan 06, 2025 at 01:31 PM
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
  `cost` double DEFAULT NULL
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
(1, 'Gậy Thuê'),
(2, 'Gậy Bán'),
(3, 'Đồ Ăn'),
(4, 'Đồ Uống');

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
(1, 'Nguyễn Minh An', '0912345678', 10.5),
(2, 'Trần Thị Lan', '0987654321', 15),
(3, 'Lê Văn Hải', '0901122334', 8.75),
(4, 'Phạm Mai Linh', '0934455667', 12),
(5, 'Hoàng Minh Tú', '0922233445', 18.5);

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
(1, 1, 800000, 'đã book'),
(2, 2, 1500000, 'đã book'),
(3, 3, 500000, 'đã book'),
(4, 4, 1200000, 'đã book'),
(5, 5, 2000000, 'đã book');

-- --------------------------------------------------------

--
-- Table structure for table `orders_items`
--

CREATE TABLE `orders_items` (
  `orders_items_id` int(11) NOT NULL,
  `orders_id` int(11) DEFAULT NULL,
  `products_id` int(11) DEFAULT NULL,
  `quantity` int(11) DEFAULT NULL,
  `cost` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `orders_items`
--

INSERT INTO `orders_items` (`orders_items_id`, `orders_id`, `products_id`, `quantity`, `cost`) VALUES
(1, 1, 1, 2, 100000),
(2, 1, 3, 1, 70000),
(3, 1, 5, 1, 150000),
(4, 2, 2, 1, 500000),
(5, 2, 4, 1, 800000),
(6, 2, 6, 1, 1200000),
(7, 3, 7, 3, 25000),
(8, 3, 8, 2, 30000),
(9, 3, 9, 1, 20000),
(10, 3, 10, 1, 40000),
(11, 4, 12, 2, 15000),
(12, 4, 13, 1, 25000);

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
(1, 'Bàn Billiard Thường', 350000, 'Available'),
(2, 'Bàn Billiard VIP', 1000000, 'Ordered'),
(3, 'Bàn Billiard Cao Cấp', 750000, 'Playing');

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
(1, 'Gậy Thuê - Basic', 1, 50000, 'Giờ', 10),
(2, 'Gậy Thuê - Cao Cấp', 1, 70000, 'Giờ', 8),
(3, 'Gậy Thuê - Chuyên Nghiệp', 1, 100000, 'Giờ', 8),
(4, 'Gậy Thuê - Mini', 1, 30000, 'Giờ', 15),
(5, 'Gậy Thuê - Siêu Cứng', 1, 120000, 'Giờ', 6),
(6, 'Gậy Bán - Thường', 2, 500000, 'Cái', 20),
(7, 'Gậy Bán - Cao Cấp', 2, 800000, 'Cái', 12),
(8, 'Gậy Bán - Professional', 2, 1200000, 'Cái', 8),
(9, 'Gậy Bán - Đặc Biệt', 2, 1500000, 'Cái', 5),
(10, 'Gậy Bán - Siêu Nhẹ', 2, 650000, 'Cái', 10),
(11, 'Snack - Khoai Tây Chiên', 3, 25000, 'Bịch', 40),
(12, 'Snack - Hạt Dẻ', 3, 30000, 'Bịch', 30),
(13, 'Snack - Bắp Rang Bơ', 3, 20000, 'Bịch', 50),
(14, 'Snack - Kẹo Dẻo', 3, 35000, 'Hộp', 20),
(15, 'Snack - Bánh Quy', 3, 25000, 'Hộp', 45),
(16, 'Nước Ngọt - Cola', 4, 20000, 'Lon', 100),
(17, 'Nước Ngọt - Fanta', 4, 22000, 'Lon', 90),
(18, 'Nước Ép - Cam', 4, 25000, 'Chai', 60),
(19, 'Cà Phê - Espresso', 4, 40000, 'Cốc', 50),
(20, 'Trà - Trà Sữa', 4, 35000, 'Cốc', 70);

-- --------------------------------------------------------

--
-- Table structure for table `promotions`
--

CREATE TABLE `promotions` (
  `id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `promotion_type` enum('LoyaltyCustomer','SinglePlaytime','Combo','NoPromotion') DEFAULT NULL,
  `discount` double DEFAULT NULL,
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `rent_sticks`
--

CREATE TABLE `rent_sticks` (
  `rent_sticks_id` int(11) NOT NULL,
  `orders_id` int(11) DEFAULT NULL,
  `products_id` int(11) DEFAULT NULL,
  `start_time` timestamp NOT NULL DEFAULT current_timestamp(),
  `end_time` timestamp NULL DEFAULT NULL,
  `timeplay` double DEFAULT NULL,
  `cost` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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

--
-- Indexes for dumped tables
--

--
-- Indexes for table `bookings`
--
ALTER TABLE `bookings`
  ADD PRIMARY KEY (`bookings_id`),
  ADD KEY `orders_id` (`orders_id`),
  ADD KEY `tables_id` (`tables_id`);

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
  ADD KEY `products_id` (`products_id`);

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
-- Indexes for table `rent_sticks`
--
ALTER TABLE `rent_sticks`
  ADD PRIMARY KEY (`rent_sticks_id`),
  ADD KEY `orders_id` (`orders_id`),
  ADD KEY `products_id` (`products_id`);

--
-- Indexes for table `revenue`
--
ALTER TABLE `revenue`
  ADD PRIMARY KEY (`revenue_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `bookings`
--
ALTER TABLE `bookings`
  MODIFY `bookings_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `category`
--
ALTER TABLE `category`
  MODIFY `category_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `customers`
--
ALTER TABLE `customers`
  MODIFY `customers_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `orders`
--
ALTER TABLE `orders`
  MODIFY `orders_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `orders_items`
--
ALTER TABLE `orders_items`
  MODIFY `orders_items_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT for table `pooltables`
--
ALTER TABLE `pooltables`
  MODIFY `tables_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `products`
--
ALTER TABLE `products`
  MODIFY `products_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;

--
-- AUTO_INCREMENT for table `promotions`
--
ALTER TABLE `promotions`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `rent_sticks`
--
ALTER TABLE `rent_sticks`
  MODIFY `rent_sticks_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `revenue`
--
ALTER TABLE `revenue`
  MODIFY `revenue_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `bookings`
--
ALTER TABLE `bookings`
  ADD CONSTRAINT `bookings_ibfk_1` FOREIGN KEY (`orders_id`) REFERENCES `orders` (`orders_id`),
  ADD CONSTRAINT `bookings_ibfk_2` FOREIGN KEY (`tables_id`) REFERENCES `pooltables` (`tables_id`);

--
-- Constraints for table `orders`
--
ALTER TABLE `orders`
  ADD CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`customers_id`) REFERENCES `customers` (`customers_id`);

--
-- Constraints for table `orders_items`
--
ALTER TABLE `orders_items`
  ADD CONSTRAINT `orders_items_ibfk_1` FOREIGN KEY (`orders_id`) REFERENCES `orders` (`orders_id`),
  ADD CONSTRAINT `orders_items_ibfk_2` FOREIGN KEY (`products_id`) REFERENCES `products` (`products_id`);

--
-- Constraints for table `products`
--
ALTER TABLE `products`
  ADD CONSTRAINT `products_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`);

--
-- Constraints for table `rent_sticks`
--
ALTER TABLE `rent_sticks`
  ADD CONSTRAINT `rent_sticks_ibfk_1` FOREIGN KEY (`orders_id`) REFERENCES `orders` (`orders_id`),
  ADD CONSTRAINT `rent_sticks_ibfk_2` FOREIGN KEY (`products_id`) REFERENCES `products` (`products_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
