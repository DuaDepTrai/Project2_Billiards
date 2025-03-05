-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Mar 03, 2025 at 06:02 PM
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
  `net_total` double DEFAULT NULL,
  `booking_status` enum('Order','Playing','Finish','Canceled') NOT NULL DEFAULT 'Playing'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `bookings`
--

INSERT INTO `bookings` (`booking_id`, `order_id`, `table_id`, `start_time`, `end_time`, `timeplay`, `net_total`, `booking_status`) VALUES
(1, 1, 1, '2024-12-31 20:00:00', '2024-12-31 22:00:00', 2, 70000, 'Finish'),
(2, 2, 2, '2025-01-02 00:00:00', '2025-01-02 02:30:00', 2.5, 187500, 'Finish'),
(3, 3, 3, '2025-01-03 04:00:00', '2025-01-03 05:30:00', 1.5, 150000, 'Finish'),
(4, 4, 1, '2025-01-03 19:00:00', '2025-01-03 20:00:00', 1, 35000, 'Finish'),
(5, 5, 2, '2025-01-04 21:00:00', '2025-01-04 22:45:00', 1.75, 131250, 'Finish'),
(6, 6, 3, '2025-01-05 23:00:00', '2025-01-06 01:00:00', 2, 200000, 'Finish'),
(7, 7, 1, '2025-01-07 02:00:00', '2025-01-07 03:30:00', 1.5, 52500, 'Finish'),
(8, 8, 2, '2025-01-08 03:00:00', '2025-01-08 04:00:00', 1, 75000, 'Finish'),
(9, 9, 3, '2025-01-09 04:00:00', NULL, NULL, NULL, 'Finish'),
(25, 26, 6, '2025-02-14 16:49:00', '2025-02-14 17:00:15', 0.183333333, 18333.3333, 'Finish'),
(26, 29, 4, '2025-02-21 16:38:00', '2025-02-21 16:57:07', 0.316666666, NULL, 'Finish'),
(27, 29, 4, '2025-02-21 16:39:00', '2025-02-21 16:57:07', 0.3, 7000, 'Finish'),
(29, 29, 9, '2025-02-21 16:42:00', '2025-02-21 16:57:07', 0.25, NULL, 'Finish'),
(30, 31, 4, '2025-02-21 17:03:00', '2025-02-21 17:25:08', 0.4, 14000, 'Finish'),
(31, 32, 6, '2025-02-21 17:26:00', '2025-02-21 17:26:40', 0, 0, 'Finish'),
(32, 30, 7, '2025-02-21 17:27:00', '2025-02-21 17:27:25', 0, 0, 'Finish'),
(33, 28, 4, '2025-02-21 17:31:00', '2025-02-21 17:34:38', 0.05, 1750, 'Finish'),
(34, 28, 9, '2025-02-21 17:31:00', '2025-02-21 17:34:38', 0.05, 5000, 'Finish'),
(35, 27, 7, '2025-02-21 17:39:00', '2025-02-21 17:39:29', 0, 0, 'Finish'),
(36, 27, 9, '2025-02-21 17:39:00', '2025-02-21 17:39:29', 0, 0, 'Finish'),
(37, 33, 4, '2025-02-22 16:18:00', '2025-02-22 16:25:39', 0.116666666, NULL, 'Finish'),
(38, 33, 7, '2025-02-22 16:18:00', '2025-02-22 16:25:39', 0.116666666, NULL, 'Finish'),
(39, 33, 4, '2025-02-22 16:22:00', '2025-02-22 16:25:39', 0.05, NULL, 'Finish'),
(40, 33, 7, '2025-02-22 16:22:00', '2025-02-22 16:25:39', 0.05, NULL, 'Finish'),
(41, 34, 6, '2025-02-22 16:25:00', '2025-02-22 16:26:15', 0.016666666, 1666.6666, 'Finish'),
(42, 36, 9, '2025-02-22 16:27:00', '2025-02-22 16:27:45', 0, 0, 'Finish');

--
-- Triggers `bookings`
--
DELIMITER $$
CREATE TRIGGER `after_bookings_insert` AFTER INSERT ON `bookings` FOR EACH ROW BEGIN
    IF NEW.booking_status = 'order' THEN
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
CREATE TRIGGER `update_total_playtime_and_table_status` AFTER UPDATE ON `bookings` FOR EACH ROW BEGIN
    -- Kiểm tra nếu giá trị timeplay thay đổi
    IF OLD.timeplay <> NEW.timeplay THEN
        UPDATE customers c
        JOIN orders o ON c.customer_id = o.customer_id
        JOIN bookings b ON o.order_id = b.order_id
        SET c.total_playtime = (
            SELECT COALESCE(SUM(b.timeplay), 0)
            FROM bookings b
            JOIN orders o2 ON b.order_id = o2.order_id
            WHERE o2.customer_id = c.customer_id
        )
        WHERE o.customer_id = c.customer_id;
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
  `category_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `image_path` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `category`
--

INSERT INTO `category` (`category_id`, `category_name`, `image_path`) VALUES
(2, 'Cues For Rent', 'cues-rent.png'),
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
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `phone` varchar(15) NOT NULL,
  `total_playtime` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `customers`
--

INSERT INTO `customers` (`customer_id`, `name`, `phone`, `total_playtime`) VALUES
(1, 'Guest', '0999999999', 0),
(2, 'Le Thi Mai', '0912345678', 0),
(3, 'Tran Minh Tu', '0903456789', 0),
(4, 'Phan Quoc Toan', '0856789012', 0),
(5, 'Hoang Minh Thao', '0321234567', 0),
(6, 'Nguyen Van A\nnh', '0708765432', 0),
(7, 'Tran Thi Binh', '0562345678', 0),
(8, 'Le Hoang Nam', '0593987654', 0),
(9, 'Pham Thanh Tam', '0345678910', 0),
(10, 'Dang Minh Duc', '0777123456', 0),
(11, 'Bui Van Khoa', '0965123456', 0),
(12, 'Hoang Thi Lan', '0946789012', 0),
(13, 'Vo Quoc Bao', '0932345678', 0),
(14, 'Doan Ngoc Hai', '0887654321', 0),
(15, 'Truong Kim Ngan', '0813456789', 0),
(16, 'Ngo Van Son', '0398765432', 0),
(17, 'Luong Bao Chau', '0789123456', 0),
(18, 'Phan Thanh Phong', '0582345678', 0),
(19, 'Dinh Hai Dang', '0356789012', 0),
(20, 'Cao Anh Tuan', '0765432189', 0),
(21, 'Tran Van B', '9876543211', 0),
(22, 'Nguyen Anh Tuan', '0987654321', 0);

-- --------------------------------------------------------

--
-- Table structure for table `orders`
--

CREATE TABLE `orders` (
  `order_id` int(11) NOT NULL,
  `customer_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `total_cost` double DEFAULT NULL,
  `order_date` date NOT NULL,
  `order_status` enum('Order','Playing','Finished','Paid','Canceled') NOT NULL DEFAULT 'Playing'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `orders`
--

INSERT INTO `orders` (`order_id`, `customer_id`, `user_id`, `total_cost`, `order_date`, `order_status`) VALUES
(1, 1, 2, 1345000, '2025-01-01', 'Finished'),
(2, 2, 2, 1647500, '2025-01-02', 'Paid'),
(3, 3, 2, 3280000, '2025-01-03', 'Finished'),
(4, 4, 2, 695000, '2025-01-04', 'Finished'),
(5, 5, 2, 1513750, '2025-01-05', 'Finished'),
(6, 1, 2, 300000, '2025-01-06', 'Finished'),
(7, 2, 2, 202500, '2025-01-07', 'Finished'),
(8, 3, 2, 225000, '2025-01-08', 'Paid'),
(9, 4, 2, 0, '2025-01-09', 'Canceled'),
(26, 4, 2, NULL, '2025-02-14', 'Finished'),
(27, 2, 2, 320000, '2025-02-22', 'Finished'),
(28, 1, 2, 611750, '2025-02-22', 'Finished'),
(29, 21, 2, 467000, '2025-02-21', 'Canceled'),
(30, 1, 2, NULL, '2025-02-22', 'Canceled'),
(31, 3, 2, NULL, '2025-02-22', 'Canceled'),
(32, 5, 2, NULL, '2025-02-22', 'Canceled'),
(33, 2, 2, 215000, '2025-02-22', 'Finished'),
(34, 5, 2, 1666.6666, '2025-02-22', 'Canceled'),
(35, 5, 2, 0, '2025-02-22', 'Finished'),
(36, 9, 2, NULL, '2025-02-22', 'Canceled'),
(37, 11, 2, NULL, '2025-02-22', 'Playing'),
(38, 1, 1, NULL, '2025-03-01', 'Playing'),
(39, 1, 1, NULL, '2025-03-01', 'Playing');

-- --------------------------------------------------------

--
-- Table structure for table `orders_items`
--

CREATE TABLE `orders_items` (
  `order_item_id` int(11) NOT NULL,
  `order_id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL,
  `quantity` int(11) NOT NULL,
  `net_total` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `orders_items`
--

INSERT INTO `orders_items` (`order_item_id`, `order_id`, `product_id`, `quantity`, `net_total`) VALUES
(2, 1, 12, 3, 60000),
(3, 1, 7, 1, 15000),
(5, 2, 13, 2, 60000),
(6, 2, 8, 1, 25000),
(8, 3, 14, 1, 25000),
(9, 3, 10, 1, 30000),
(11, 4, 15, 1, 40000),
(12, 4, 11, 1, 20000),
(14, 5, 16, 2, 100000),
(15, 5, 9, 2, 20000),
(50, 26, 17, 24, 480000),
(51, 26, 16, 30, 1500000),
(52, 27, 15, 10, 320000),
(53, 29, 15, 10, 400000),
(54, 28, 10, 1, 30000),
(55, 28, 16, 1, 50000),
(56, 28, 8, 1, 25000),
(58, 33, 17, 10, 200000);

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
(6, 'view_user', 'View users'),
(7, 'add_user', 'Add user'),
(8, 'edit_user', 'Edit user'),
(9, 'delete_user', 'Remove user');

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
(1, 1, 'Standard 1', 'Playing'),
(2, 2, 'Deluxe 1', 'Ordered'),
(3, 3, 'VIP 1', 'Playing'),
(1, 4, 'Standard 2', 'Playing'),
(2, 5, 'Deluxe 2', 'Available'),
(3, 6, 'VIP 2', 'Playing'),
(1, 7, 'Standard 3', 'Playing'),
(2, 8, 'Deluxe 3', 'Available'),
(3, 9, 'VIP 3', 'Playing');

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
(4, 'Standard Cue - For Rent', 2, 50000, 'Piece', 10),
(5, 'Deluxe Cue - For Rent', 2, 100000, 'Piece', 9),
(6, 'Professional Cue - For Rent', 2, 150000, 'Piece', 10),
(7, 'Soda', 3, 15000, 'Can', 100),
(8, 'Juice', 3, 25000, 'Bottle', 79),
(9, 'Water', 3, 10000, 'Bottle', 120),
(10, 'Coffee', 3, 30000, 'Cup', 49),
(11, 'Tea', 3, 20000, 'Cup', 60),
(12, 'Chips', 4, 20000, 'Bag', 50),
(13, 'Nuts', 4, 30000, 'Bag', 40),
(14, 'Popcorn', 4, 25000, 'Bag', 60),
(15, 'Chocolate', 4, 40000, 'Bar', 25),
(16, 'Cookies Box', 4, 50000, 'Box', 24),
(17, 'Coca Cola', 3, 20000, 'Can', 26),
(20, 'Uno', 5, 60000, 'Set', 20),
(21, 'Poker Deck (Plastic Cards)', 5, 80000, 'Set', 10),
(22, 'Poker Deck (Plastic Coated)', 5, 50000, 'Set', 30);

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
(2, 1),
(2, 2),
(2, 3),
(2, 4),
(2, 5),
(3, 1),
(3, 2),
(3, 3),
(4, 1),
(4, 2),
(4, 3),
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
(1, 'admin', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Admin', '0961886965', 'Hà Nội', '2025-01-01', '1992-01-01', 1, 'manager.png'),
(2, 'hieu', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Nguyễn Văn Hiếu', '0961886965', 'Hà Nội', '2025-01-01', '1992-01-01', 2, 'manager.png'),
(3, 'quan', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Lưu Minh Quân', '0987654321', 'Hà Nội', '2025-01-01', '1992-01-01', 2, 'manager.png'),
(4, 'manh', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Nguyễn Đức Mạnh', '0987654321', 'Hà Nội', '2025-01-01', '1992-01-01', 2, 'trancung.jpg'),
(6, 'long', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Nguyễn Việt Long', '0987654321', 'Hà Nội', '2025-01-01', '1992-01-01', 2, 'manager.png'),
(7, 'letan3', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Hồ Hiền', '0987654321', 'Hà Nội', '2025-01-01', '1992-01-01', 3, 'user.png'),
(8, 'nvkho2', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Phạm Kiên', '0987654321', 'Hà Nội', '2025-01-01', '1992-01-01', 4, 'user.png'),
(9, 'letan2', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Vũ Trà', '0987654321', 'Hà Nội', '2025-01-01', '1992-01-01', 3, 'user.png'),
(10, 'nvkho3', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Lê Khánh', '0987654321', 'Hà Nội', '2025-01-01', '1990-06-15', 4, 'user.png'),
(11, 'letan1', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Phương Thảo', '0965438482', 'Hà Nội', '2025-03-02', '1997-12-09', 3, '366339749_258391096996728_4824303083280686790_n.jpg'),
(14, 'nvkho1', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Văn Dứa', '0979473639', 'Hà Nội', '2025-03-02', '2025-03-06', 4, 'user.png');

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
  ADD KEY `users_roles_FK` (`role_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `bookings`
--
ALTER TABLE `bookings`
  MODIFY `booking_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=43;

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
  MODIFY `customer_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=23;

--
-- AUTO_INCREMENT for table `orders`
--
ALTER TABLE `orders`
  MODIFY `order_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=40;

--
-- AUTO_INCREMENT for table `orders_items`
--
ALTER TABLE `orders_items`
  MODIFY `order_item_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=59;

--
-- AUTO_INCREMENT for table `permissions`
--
ALTER TABLE `permissions`
  MODIFY `permission_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `pooltables`
--
ALTER TABLE `pooltables`
  MODIFY `table_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `products`
--
ALTER TABLE `products`
  MODIFY `product_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=23;

--
-- AUTO_INCREMENT for table `roles`
--
ALTER TABLE `roles`
  MODIFY `role_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

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
