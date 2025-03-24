-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Mar 22, 2025 at 08:10 PM
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
  `total` double DEFAULT NULL,
  `booking_status` enum('Ordered','Playing','Finish','Canceled') NOT NULL DEFAULT 'Playing'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `bookings`
--

INSERT INTO `bookings` (`booking_id`, `order_id`, `table_id`, `start_time`, `end_time`, `timeplay`, `total`, `booking_status`) VALUES
(56, 73, 42, '2025-03-16 01:00:00', NULL, NULL, NULL, 'Canceled'),
(57, 68, 31, '2025-03-15 17:36:27', '2025-03-15 18:04:37', 0.5, 16333.33331, 'Finish'),
(58, 71, 41, '2025-03-15 17:36:36', '2025-03-16 17:22:31', NULL, 1781250, 'Finish'),
(59, 71, 51, '2025-03-16 01:00:00', NULL, NULL, NULL, 'Canceled'),
(60, 72, 33, '2025-03-15 17:37:03', '2025-03-16 17:23:26', NULL, 831833.33331, 'Finish'),
(61, 74, 36, '2025-03-15 17:59:00', NULL, NULL, NULL, 'Canceled'),
(62, 75, 43, '2025-03-17 17:12:00', '2025-03-19 17:04:52', NULL, 3589999.99995, 'Finish'),
(63, 75, 44, '2025-03-17 17:12:03', '2025-03-19 17:04:52', NULL, 3589999.99995, 'Finish'),
(64, 75, 45, '2025-03-17 17:12:06', '2025-03-19 17:04:52', NULL, 3589999.99995, 'Finish'),
(65, 75, 46, '2025-03-17 17:12:10', '2025-03-19 17:04:52', NULL, 3589999.99995, 'Finish'),
(66, 75, 31, '2025-03-17 17:42:20', '2025-03-19 17:04:52', NULL, 1657833.33331, 'Finish'),
(67, 75, 34, '2025-03-17 17:42:24', '2025-03-19 17:04:52', NULL, 1657833.33331, 'Finish'),
(68, 75, 35, '2025-03-17 17:42:28', '2025-03-19 17:04:52', NULL, 1657833.33331, 'Finish'),
(69, 79, 36, '2025-03-19 02:29:06', NULL, NULL, NULL, 'Canceled'),
(70, 80, 32, '2025-03-19 02:39:19', '2025-03-19 17:06:03', NULL, 505166.666655, 'Finish'),
(71, 82, 38, '2025-03-19 16:59:00', NULL, NULL, NULL, 'Canceled'),
(72, 83, 44, '2025-03-19 17:22:21', '2025-03-19 17:22:29', NULL, 0, 'Finish'),
(73, 84, 51, '2025-03-19 17:59:00', NULL, NULL, NULL, 'Canceled'),
(74, 85, 56, '2025-03-19 18:05:00', NULL, NULL, NULL, 'Canceled'),
(75, 86, 47, '2025-03-19 18:15:00', NULL, NULL, NULL, 'Canceled'),
(76, 87, 47, '2025-03-19 18:25:00', NULL, NULL, NULL, 'Canceled'),
(77, 88, 46, '2025-03-19 18:25:00', NULL, NULL, NULL, 'Canceled'),
(78, 89, 46, '2025-03-19 18:50:00', NULL, NULL, NULL, 'Canceled'),
(79, 91, 32, '2025-03-19 19:30:00', NULL, NULL, NULL, 'Canceled'),
(80, 92, 33, '2025-03-19 19:41:26', '2025-03-19 19:54:38', NULL, 7583.33331, 'Finish'),
(81, 93, 44, '2025-03-19 19:41:39', '2025-03-19 19:54:30', NULL, 15000, 'Finish'),
(82, 94, 46, '2025-03-19 19:45:00', NULL, NULL, NULL, 'Canceled'),
(83, 96, 42, '2025-03-20 16:34:25', '2025-03-20 19:34:45', 3, 225000, 'Finish'),
(84, 97, 42, '2025-03-20 16:45:00', NULL, NULL, NULL, 'Canceled'),
(85, 100, 43, '2025-03-21 16:25:00', NULL, NULL, NULL, 'Canceled');

--
-- Triggers `bookings`
--
DELIMITER $$
CREATE TRIGGER `after_bookings_insert` AFTER INSERT ON `bookings` FOR EACH ROW BEGIN
    IF NEW.booking_status = 'ordered' THEN
        UPDATE pooltables
        SET status = 'ordered'
        WHERE table_id = NEW.table_id;
    ELSEIF NEW.booking_status = 'playing' THEN
        UPDATE pooltables
        SET status = 'playing'
        WHERE table_id = NEW.table_id;
    ELSEIF NEW.booking_status = 'finish' THEN
        UPDATE pooltables
        SET status = 'available'
        WHERE table_id = NEW.table_id;
    END IF;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `after_delete_booking` AFTER DELETE ON `bookings` FOR EACH ROW BEGIN
    UPDATE pooltables
    SET status = 'available'
    WHERE table_id = OLD.table_id;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `update_total_playtime` AFTER UPDATE ON `bookings` FOR EACH ROW BEGIN
    -- Kiểm tra nếu giá trị timeplay thay đổi (tránh NULL gây lỗi)
    IF COALESCE(OLD.timeplay, 0) <> COALESCE(NEW.timeplay, 0) THEN
        UPDATE customers c
        SET c.total_playtime = (
        SELECT COALESCE(SUM(b.timeplay), 0)
            FROM bookings b
            JOIN orders o ON b.order_id = o.order_id
            WHERE o.customer_id = c.customer_id
        )
        WHERE c.customer_id = (
            SELECT o.customer_id
            FROM orders o
            WHERE o.order_id = NEW.order_id
            LIMIT 1
        );
    END IF;
END
$$
DELIMITER ;

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
(2, 'Cues For Rent'),
(3, 'Drinks'),
(4, 'Food'),
(5, 'Board Game');

-- --------------------------------------------------------

--
-- Table structure for table `cate_pooltables`
--

CREATE TABLE `cate_pooltables` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `shortName` varchar(10) NOT NULL,
  `price` double NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `cate_pooltables`
--

INSERT INTO `cate_pooltables` (`id`, `name`, `shortName`, `price`) VALUES
(1, 'Standard Pool', 'STD', 35000),
(2, 'Deluxe Pool', 'DLX', 75000),
(3, 'VIP Pool', 'VIP', 100000);

-- --------------------------------------------------------

--
-- Table structure for table `customers`
--

CREATE TABLE `customers` (
  `customer_id` int(11) NOT NULL,
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `phone` varchar(15) NOT NULL,
  `total_playtime` double DEFAULT NULL,
  `birthday` date DEFAULT '1990-01-01',
  `address` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `customers`
--

INSERT INTO `customers` (`customer_id`, `name`, `phone`, `total_playtime`, `birthday`, `address`) VALUES
(1, 'Guest', '0999999999', 0.5, '1990-01-01', 'Ba Dinh, Hanoi'),
(2, 'Le Thi Mai', '0912345678', 0, '1990-01-01', 'Hoan Kiem, Hanoi'),
(3, 'Tran Minh Tu', '0903456789', 0, '1990-01-01', 'Thanh Xuan, Hanoi'),
(4, 'Phan Quoc Toan', '0856789012', 0, '1990-01-01', 'Dong Da, Hanoi'),
(5, 'Hoang Minh Thao', '0321234567', 0, '1990-01-01', 'Cau Giay, Hanoi'),
(6, 'Nguyen Van Anh', '0708765432', 0, '1990-01-01', 'Hoang Mai, Hanoi'),
(7, 'Tran Thi Binh', '0562345678', 0, '1990-01-01', 'Ha Dong, Hanoi'),
(8, 'Le Hoang Nam', '0593987654', 0.2, '1990-01-01', 'Tay Ho, Hanoi'),
(9, 'Pham Thanh Tam', '0345678910', 6, '1990-01-01', 'Nam Tu Liem, Hanoi'),
(10, 'Dang Minh Duc', '0777123456', 0, '1990-01-01', 'Bac Tu Liem, Hanoi'),
(11, 'Bui Van Khoa', '0965123456', 0, '1990-01-01', 'Long Bien, Hanoi'),
(12, 'Hoang Thi Lan', '0946789012', 0, '1990-01-01', 'Ba Dinh, Hanoi'),
(13, 'Vo Quoc Bao', '0932345678', 0, '1990-01-01', 'Hoan Kiem, Hanoi'),
(14, 'Doan Ngoc Hai', '0887654321', 0, '1990-01-01', 'Thanh Xuan, Hanoi'),
(15, 'Truong Kim Ngan', '0813456789', 0, '1990-01-01', 'Dong Da, Hanoi'),
(16, 'Ngo Van Son', '0398765432', 0, '1990-01-01', 'Cau Giay, Hanoi'),
(17, 'Luong Bao Chau', '0789123456', 0, '1990-01-01', 'Hoang Mai, Hanoi'),
(18, 'Phan Thanh Phong', '0582345678', 0, '1990-01-01', 'Ha Dong, Hanoi'),
(19, 'Dinh Hai Dang', '0356789012', 0.216666666, '1990-01-01', 'Tay Ho, Hanoi'),
(20, 'Cao Anh Tuan', '0765432189', 0, '1990-01-01', 'Nam Tu Liem, Hanoi'),
(21, 'Tran Van B', '9876543211', 0, '1990-01-01', 'Bac Tu Liem, Hanoi'),
(22, 'Nguyen Anh Tuan', '0987654321', 0, '1991-01-01', 'Long Bien, Hanoi');

-- --------------------------------------------------------

--
-- Table structure for table `orders`
--

CREATE TABLE `orders` (
  `order_id` int(11) NOT NULL,
  `customer_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `total_cost` double DEFAULT NULL,
  `order_date` datetime NOT NULL,
  `order_status` enum('Ordered','Playing','Finished','Paid','Canceled') NOT NULL DEFAULT 'Playing'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `orders`
--

INSERT INTO `orders` (`order_id`, `customer_id`, `user_id`, `total_cost`, `order_date`, `order_status`) VALUES
(68, 1, 1, 16333.33331, '2025-03-13 23:43:18', 'Finished'),
(71, 11, 1, 1781250, '2025-03-16 00:23:04', 'Paid'),
(72, 12, 1, 831833.33331, '2025-03-16 00:29:04', 'Paid'),
(73, 22, 1, NULL, '2025-03-16 00:33:52', 'Canceled'),
(74, 18, 1, NULL, '2025-03-16 00:58:05', 'Canceled'),
(75, 8, 1, 19333499.999730002, '2025-03-17 23:35:13', 'Paid'),
(76, 1, 1, NULL, '2025-03-17 23:35:29', 'Playing'),
(77, 1, 1, NULL, '2025-03-17 23:36:02', 'Playing'),
(78, 1, 1, NULL, '2025-03-17 23:47:36', 'Playing'),
(79, 19, 1, NULL, '2025-03-19 09:29:04', 'Canceled'),
(80, 1, 1, 505166.666655, '2025-03-19 09:39:15', 'Finished'),
(81, 1, 1, NULL, '2025-03-19 09:48:02', 'Playing'),
(82, 18, 1, NULL, '2025-03-19 23:55:19', 'Canceled'),
(83, 7, 1, 20000, '2025-03-20 00:22:10', 'Finished'),
(84, 19, 1, NULL, '2025-03-20 00:50:40', 'Canceled'),
(85, 20, 1, NULL, '2025-03-20 01:03:51', 'Canceled'),
(86, 20, 1, NULL, '2025-03-20 01:13:00', 'Canceled'),
(87, 4, 1, NULL, '2025-03-20 01:20:41', 'Canceled'),
(88, 6, 1, NULL, '2025-03-20 01:21:10', 'Canceled'),
(89, 16, 1, NULL, '2025-03-20 01:45:18', 'Canceled'),
(90, 9, 1, NULL, '2025-03-20 02:24:46', 'Playing'),
(91, 16, 1, NULL, '2025-03-20 02:29:14', 'Canceled'),
(92, 19, 1, 7583.33331, '2025-03-20 02:41:25', 'Paid'),
(93, 8, 1, 15000, '2025-03-20 02:41:38', 'Paid'),
(94, 14, 1, NULL, '2025-03-20 02:42:14', 'Canceled'),
(95, 10, 1, 40000, '2025-03-20 23:31:15', 'Finished'),
(96, 9, 1, 225000, '2025-03-20 23:32:01', 'Finished'),
(97, 5, 1, NULL, '2025-03-20 23:40:52', 'Canceled'),
(98, 1, 1, NULL, '2025-03-21 23:21:20', 'Playing'),
(99, 20, 1, NULL, '2025-03-21 23:21:31', 'Playing'),
(100, 16, 1, NULL, '2025-03-21 23:23:44', 'Canceled'),
(101, 18, 1, 230000, '2025-03-22 02:25:15', 'Finished');

-- --------------------------------------------------------

--
-- Table structure for table `orders_items`
--

CREATE TABLE `orders_items` (
  `order_item_id` int(11) NOT NULL,
  `order_id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL,
  `quantity` int(11) NOT NULL,
  `total` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `orders_items`
--

INSERT INTO `orders_items` (`order_item_id`, `order_id`, `product_id`, `quantity`, `total`) VALUES
(59, 83, 17, 1, 20000),
(60, 95, 15, 1, 40000),
(61, 101, 12, 1, 20000),
(62, 101, 15, 1, 40000),
(63, 101, 17, 1, 20000),
(64, 101, 16, 1, 50000),
(65, 101, 5, 1, 100000);

-- --------------------------------------------------------

--
-- Table structure for table `permissions`
--

CREATE TABLE `permissions` (
  `permission_id` int(11) NOT NULL,
  `permission_name` varchar(100) NOT NULL,
  `description` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `permissions`
--

INSERT INTO `permissions` (`permission_id`, `permission_name`, `description`) VALUES
(1, 'view_product', 'View products'),
(2, 'add_product', 'Add product'),
(3, 'update_product', 'Update product'),
(4, 'remove_product', 'Remove product'),
(5, 'stock_up_product', 'Stock up product'),
(6, 'add_product_category', 'Add product category'),
(7, 'update_product_category', 'Update product category'),
(8, 'remove_product_category', 'Remove product category'),
(9, 'view_user', 'View users'),
(10, 'add_user', 'Add user'),
(11, 'edit_user', 'Update user'),
(12, 'delete_user', 'Remove user'),
(13, 'view_order', 'View orders'),
(14, 'view_customer', 'View customers'),
(15, 'view_pool', 'View pools'),
(16, 'add_pool', 'Add pool table'),
(17, 'update_pool', 'Update pool table'),
(18, 'remove_pool', 'Remove pool table'),
(19, 'add_pool_category', 'Add pool category'),
(20, 'update_pool_category', 'Update pool category'),
(21, 'remove_pool_category', 'Remove pool category'),
(22, 'view_report', 'View report'),
(23, 'view_role_permission', 'View roles & permissions');

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
(1, 31, 'Standard 01', 'Available'),
(1, 32, 'Standard 02', 'Available'),
(1, 33, 'Standard 03', 'Available'),
(1, 34, 'Standard 04', 'Available'),
(1, 35, 'Standard 05', 'Available'),
(1, 36, 'Standard 06', 'Available'),
(1, 37, 'Standard 07', 'Available'),
(1, 38, 'Standard 08', 'Available'),
(1, 39, 'Standard 09', 'Available'),
(1, 40, 'Standard 10', 'Available'),
(2, 41, 'Deluxe 01', 'Available'),
(2, 42, 'Deluxe 02', 'Available'),
(2, 43, 'Deluxe 03', 'Available'),
(2, 44, 'Deluxe 04', 'Available'),
(2, 45, 'Deluxe 05', 'Available'),
(2, 46, 'Deluxe 06', 'Available'),
(2, 47, 'Deluxe 07', 'Available'),
(2, 48, 'Deluxe 08', 'Available'),
(2, 49, 'Deluxe 09', 'Available'),
(2, 50, 'Deluxe 10', 'Available'),
(3, 51, 'VIP 01', 'Available'),
(3, 52, 'VIP 02', 'Available'),
(3, 53, 'VIP 03', 'Available'),
(3, 54, 'VIP 04', 'Available'),
(3, 55, 'VIP 05', 'Available'),
(3, 56, 'VIP 06', 'Available'),
(3, 57, 'VIP 07', 'Available'),
(3, 58, 'VIP 08', 'Available'),
(3, 59, 'VIP 09', 'Available'),
(3, 60, 'VIP 10', 'Available');

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
(4, 'Standard Cue - For Rent', 2, 50000, 'Piece', 20),
(5, 'Deluxe Cue - For Rent', 2, 100000, 'Piece', 19),
(6, 'Professional Cue - For Rent', 2, 150000, 'Piece', 20),
(7, 'Soda', 3, 15000, 'Can', 50),
(8, 'Juice', 3, 25000, 'Can', 50),
(9, 'Water', 3, 10000, 'Bottle', 50),
(10, 'Coffee', 3, 30000, 'Cup', 50),
(11, 'Tea', 3, 20000, 'Cup', 50),
(12, 'Chips', 4, 20000, 'Bag', 49),
(13, 'Nuts', 4, 30000, 'Bag', 50),
(14, 'Popcorn', 4, 25000, 'Bag', 50),
(15, 'Chocolate', 4, 40000, 'Bar', 48),
(16, 'Cookies Box', 4, 50000, 'Box', 49),
(17, 'Coca Cola', 3, 20000, 'Can', 48),
(20, 'Uno', 5, 60000, 'Set', 30),
(21, 'Poker Deck (Plastic Cards)', 5, 80000, 'Set', 30),
(22, 'Poker Deck (Plastic Coated)', 5, 50000, 'Set', 30),
(23, 'Sting', 3, 15000, 'Bottle', 50);

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
(2, 'Manager'),
(3, 'Receptionist'),
(4, 'Warehouse');

-- --------------------------------------------------------

--
-- Table structure for table `role_permission`
--

CREATE TABLE `role_permission` (
  `role_id` int(11) NOT NULL,
  `permission_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `role_permission`
--

INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES
(1, 1),
(1, 2),
(1, 3),
(1, 4),
(1, 5),
(1, 6),
(1, 7),
(1, 8),
(1, 9),
(1, 10),
(1, 11),
(1, 12),
(1, 13),
(1, 14),
(1, 15),
(1, 16),
(1, 17),
(1, 18),
(1, 19),
(1, 20),
(1, 21),
(1, 22),
(1, 23),
(2, 1),
(2, 2),
(2, 3),
(2, 4),
(2, 5),
(2, 6),
(2, 7),
(2, 8),
(2, 13),
(2, 14),
(2, 15),
(2, 16),
(2, 17),
(2, 18),
(2, 19),
(2, 20),
(2, 21),
(2, 22),
(3, 1),
(3, 13),
(3, 14),
(3, 15),
(4, 1),
(4, 5);

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `username` varchar(100) NOT NULL,
  `password` varchar(100) NOT NULL,
  `fullname` varchar(50) NOT NULL,
  `phone` varchar(15) NOT NULL,
  `address` varchar(100) DEFAULT NULL,
  `hire_date` date NOT NULL,
  `birthday` date DEFAULT NULL,
  `role_id` int(11) NOT NULL,
  `image_path` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `username`, `password`, `fullname`, `phone`, `address`, `hire_date`, `birthday`, `role_id`, `image_path`) VALUES
(1, 'admin', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Administrator', '0961886965', 'Hanoi', '2025-01-01', '1992-01-01', 1, 'manager.png'),
(2, 'hieu', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Nguyen Van Hieu', '0961886966', 'Hanoi', '2025-01-01', '1992-01-01', 2, 'manager.png'),
(3, 'quan', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Luu Minh Quan', '0987654322', 'Hanoi', '2025-01-01', '1992-01-01', 2, 'manager.png'),
(4, 'manh', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Nguyen Duc Manh', '0987654323', 'Hanoi', '2025-01-01', '1992-01-01', 2, 'trancung.jpg'),
(6, 'long', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Nguyen Viet Long', '0987654324', 'Hanoi', '2025-01-01', '1992-01-01', 2, 'manager.png'),
(7, 'letan3', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Ho Hien', '0987654325', 'Hanoi', '2025-01-01', '1992-01-01', 3, 'telesales.png'),
(8, 'nvkho2', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Pham Kien', '0987654326', 'Hanoi', '2025-01-01', '1992-01-01', 4, 'user.png'),
(9, 'letan2', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Vu Thi Tra', '0987654327', 'Hanoi', '2025-01-01', '1992-01-01', 3, 'user.png'),
(11, 'letan1', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Phuong Thao', '0965438482', 'Hanoi', '2025-03-02', '1997-12-09', 3, '366339749_258391096996728_4824303083280686790_n.jpg'),
(14, 'nvkho1', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Van Dua', '0979473639', 'Hanoi', '2025-03-02', '2021-12-01', 4, 'user.png'),
(16, 'nvkho3', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Le Khanh', '0986463456', 'Hanoi', '2025-03-19', '1990-03-20', 4, 'user.png');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `bookings`
--
ALTER TABLE `bookings`
  ADD PRIMARY KEY (`booking_id`),
  ADD KEY `orders_id` (`order_id`),
  ADD KEY `tables_id` (`table_id`);

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
  ADD KEY `customers_id` (`customer_id`),
  ADD KEY `orders_users_FK` (`user_id`);

--
-- Indexes for table `orders_items`
--
ALTER TABLE `orders_items`
  ADD PRIMARY KEY (`order_item_id`),
  ADD KEY `orders_id` (`order_id`),
  ADD KEY `products_id` (`product_id`);

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
  ADD UNIQUE KEY `users_unique_1` (`phone`),
  ADD KEY `users_roles_FK` (`role_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `bookings`
--
ALTER TABLE `bookings`
  MODIFY `booking_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=86;

--
-- AUTO_INCREMENT for table `category`
--
ALTER TABLE `category`
  MODIFY `category_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=23;

--
-- AUTO_INCREMENT for table `cate_pooltables`
--
ALTER TABLE `cate_pooltables`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `customers`
--
ALTER TABLE `customers`
  MODIFY `customer_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=25;

--
-- AUTO_INCREMENT for table `orders`
--
ALTER TABLE `orders`
  MODIFY `order_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=102;

--
-- AUTO_INCREMENT for table `orders_items`
--
ALTER TABLE `orders_items`
  MODIFY `order_item_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=66;

--
-- AUTO_INCREMENT for table `permissions`
--
ALTER TABLE `permissions`
  MODIFY `permission_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=24;

--
-- AUTO_INCREMENT for table `pooltables`
--
ALTER TABLE `pooltables`
  MODIFY `table_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=61;

--
-- AUTO_INCREMENT for table `products`
--
ALTER TABLE `products`
  MODIFY `product_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=28;

--
-- AUTO_INCREMENT for table `roles`
--
ALTER TABLE `roles`
  MODIFY `role_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=20;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `bookings`
--
ALTER TABLE `bookings`
  ADD CONSTRAINT `bookings_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `bookings_ibfk_2` FOREIGN KEY (`table_id`) REFERENCES `pooltables` (`table_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `orders`
--
ALTER TABLE `orders`
  ADD CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `orders_users_FK` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `orders_items`
--
ALTER TABLE `orders_items`
  ADD CONSTRAINT `orders_items_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `orders_items_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`) ON DELETE CASCADE ON UPDATE CASCADE;

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

DELIMITER $$
--
-- Events
--
CREATE DEFINER=`root`@`localhost` EVENT `update_table_status` ON SCHEDULE EVERY 1 MINUTE STARTS '2025-02-22 23:25:13' ON COMPLETION NOT PRESERVE ENABLE DO BEGIN
    UPDATE pooltables 
    SET status = 'available'
    WHERE table_id IN (
        SELECT table_id FROM bookings WHERE booking_status IN ('Finish', 'Canceled')
    );
END$$

DELIMITER ;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
