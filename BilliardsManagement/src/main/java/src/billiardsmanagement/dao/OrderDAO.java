package src.billiardsmanagement.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import src.billiardsmanagement.controller.orders.OrderController;
import src.billiardsmanagement.model.Order;
import src.billiardsmanagement.model.DatabaseConnection;
import src.billiardsmanagement.model.Pair;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderDAO {
    public static int getOrderBillNo(int orderId) {
        String query = "SELECT COUNT(*) + 1 AS position FROM orders WHERE order_id < ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("position");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if not found
    }

    public static Order getTheLatestOrderCreated() {
        String query = "SELECT o.*, c.name AS customer_name, c.phone AS customer_phone " +
                "FROM orders o " +
                "JOIN customers c ON o.customer_id = c.customer_id " +
                "ORDER BY o.order_id DESC LIMIT 1";
        Order latestOrder = new Order();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet rs = statement.executeQuery()) {
            if (!rs.next()) throw new SQLException("No orders found.");

            latestOrder.setOrderId(rs.getInt("order_id"));
            latestOrder.setCustomerId(rs.getInt("customer_id"));
            latestOrder.setUserId(rs.getInt("user_id"));
            latestOrder.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
            latestOrder.setOrderStatus(rs.getString("order_status"));

            // Assuming you have methods to set customer name and phone in Order
            latestOrder.setCustomerName(rs.getString("customer_name"));
            latestOrder.setCustomerPhone(rs.getString("customer_phone"));

            return latestOrder;
        } catch (SQLException e) {
            e.printStackTrace(); // Handle exceptions appropriately
        }

        return latestOrder; // Consider returning null or an Optional<Order> if not found
    }

    public static Pair<Integer, Integer> getUserIdByOrderId(int orderId) {
        String query = "SELECT user_id, customer_id FROM orders WHERE order_id = ?";
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement pr = con.prepareStatement(query);
            pr.setInt(1, orderId);
            ResultSet rs = pr.executeQuery();
            if (!rs.next()) {
                throw new Exception("No user_id found for order with ID = " + orderId + ". This Order ID might not exist.");
            }

            Pair<Integer, Integer> resultPair = new Pair<>();
            resultPair.setFirstValue(rs.getInt("user_id"));
            resultPair.setSecondValue(rs.getInt("customer_id"));
            return resultPair;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Pair<>();
    }

    // Update status + total price
    public static boolean updateOrderStatus(int orderId, double totalCost) {
        String query = "UPDATE orders SET order_status = 'Finished', total_cost = ? WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDouble(1, totalCost);
            stmt.setInt(2, orderId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public static void updateStatusOrder(int orderId, String orderStatus) {
        String query = "UPDATE orders SET order_status = ? WHERE order_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, orderStatus);
            statement.setInt(2, orderId);
            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Order ID " + orderId + " đã được cập nhật trạng thái thành: " + orderStatus);
            } else {
                System.out.println("Không tìm thấy Order ID: " + orderId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static List<Order> getOrderPaid() {
        List<Order> paidOrders = new ArrayList<>();
        String query = "SELECT * FROM orders WHERE orderStatus = 'Paid'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Order order = new Order(
                        rs.getInt("orderId"),
                        rs.getInt("customerId"),
                        rs.getString("customerName"),
                        rs.getString("customerPhone"),
                        rs.getDouble("totalCost"),
                        rs.getString("orderStatus"));
                paidOrders.add(order);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return paidOrders;
    }


    public static List<Order> getOrdersByPhone(String input) {
        List<Order> orders = new ArrayList<>();

        // Tách chuỗi nếu có dấu "-"
        String[] parts = input.split("-");
        String nameOrPhone = parts[0].trim();
        String possiblePhone = parts.length > 1 ? parts[1].trim() : "";

        // Kiểm tra xem phần sau có phải số điện thoại không
        boolean isPhoneNumber = possiblePhone.matches("\\d{6,}"); // Chứa ít nhất 6 chữ số

        String query = """
                    SELECT o.order_id, o.customer_id, c.name AS customer_name, c.phone AS customer_phone, 
                           o.total_cost, o.order_status
                    FROM orders o
                    JOIN customers c ON o.customer_id = c.customer_id
                    WHERE c.phone LIKE ? OR c.name LIKE ?
                    ORDER BY o.order_id DESC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            if (isPhoneNumber) {
                stmt.setString(1, "%" + possiblePhone + "%"); // Tìm theo số điện thoại
                stmt.setString(2, "%" + nameOrPhone + "%");   // Tìm theo tên
            } else {
                stmt.setString(1, "%" + input + "%"); // Nếu không phải số điện thoại, tìm theo toàn bộ chuỗi
                stmt.setString(2, "%" + input + "%");
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Order order = new Order(
                        rs.getInt("order_id"),
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("customer_phone"),
                        rs.getDouble("total_cost"),
                        rs.getString("order_status")
                );
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public static int getBillNumberCount() {
        String query = "SELECT COUNT(*) as total FROM orders";
        int total = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (!rs.next()) throw new SQLException();
            total = rs.getInt("total");
            return total;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static ObservableList<Order> getOrdersByDate(LocalDate selectedDate) {
        ObservableList<Order> orders = FXCollections.observableArrayList();
        String query = """
        SELECT o.order_id, o.customer_id, c.name AS customer_name, c.phone AS customer_phone, 
               o.total_cost, o.order_date, o.order_status, u.user_id, u.fullname AS user_name, r.role_name,
               GROUP_CONCAT(
                   CONCAT(
                       CASE 
                           WHEN p.name LIKE 'Standard %' THEN 'STD' 
                           WHEN p.name LIKE 'Deluxe %' THEN 'DLX' 
                           WHEN p.name LIKE 'VIP %' THEN 'VIP' 
                           ELSE p.name 
                       END, 
                       SUBSTRING_INDEX(p.name, ' ', -1)
                   ) SEPARATOR ', '
               ) AS currentTableName
        FROM orders o
        JOIN customers c ON o.customer_id = c.customer_id
        JOIN users u ON o.user_id = u.user_id
        JOIN roles r ON u.role_id = r.role_id
        LEFT JOIN bookings b ON o.order_id = b.order_id
        LEFT JOIN pooltables p ON b.table_id = p.table_id
        WHERE DATE(o.order_date) = ?
        GROUP BY o.order_id
        ORDER BY o.order_date DESC
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDate(1, Date.valueOf(selectedDate));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                orders.add(new Order(
                        rs.getInt("order_id"),
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("customer_phone"),
                        rs.getInt("user_id"),
                        rs.getString("user_name"),
                        rs.getString("role_name"),
                        rs.getTimestamp("order_date").toLocalDateTime(),
                        rs.getDouble("total_cost"),
                        rs.getString("order_status"),
                        rs.getString("currentTableName")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public static List<String> getCatePoolTables() {
        List<String> categories = new ArrayList<>();
        String query = "SELECT DISTINCT name FROM cate_pooltables ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categories.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    public static List<String> getOrderStatuses() {
        List<String> statuses = new ArrayList<>();
        String query = "SELECT DISTINCT order_status FROM orders ORDER BY order_status";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                statuses.add(rs.getString("order_status"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statuses;
    }

    public static ObservableList<Order> getOrdersByCatePoolTable(String category) {
        ObservableList<Order> orders = FXCollections.observableArrayList();
        String query = """
            SELECT o.order_id, o.customer_id, c.name AS customer_name, c.phone AS customer_phone,
                   o.user_id, u.fullname AS user_name, r.role_name, o.order_date,
                   o.total_cost, o.order_status,
                   GROUP_CONCAT(
                       CONCAT(
                           CASE 
                               WHEN p.name LIKE 'Standard %' THEN 'STD' 
                               WHEN p.name LIKE 'Deluxe %' THEN 'DLX' 
                               WHEN p.name LIKE 'VIP %' THEN 'VIP' 
                               ELSE p.name 
                           END, 
                           SUBSTRING_INDEX(p.name, ' ', -1)
                       ) SEPARATOR ', '
                   ) AS currentTableName
            FROM orders o
            JOIN customers c ON o.customer_id = c.customer_id
            JOIN users u ON o.user_id = u.user_id
            JOIN roles r ON u.role_id = r.role_id
            LEFT JOIN bookings b ON o.order_id = b.order_id
            LEFT JOIN pooltables p ON b.table_id = p.table_id
            LEFT JOIN cate_pooltables cp ON p.cate_id = cp.id
            WHERE cp.name = ?
            GROUP BY o.order_id
            ORDER BY o.order_id DESC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, category); // Gán loại bàn cần lọc
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Order order = new Order(
                        rs.getInt("order_id"),
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("customer_phone"),
                        rs.getInt("user_id"),
                        rs.getString("user_name"),
                        rs.getString("role_name"),
                        rs.getDate("order_date"),
                        rs.getDouble("total_cost"),
                        rs.getString("order_status"),
                        rs.getString("currentTableName")
                );
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }



    public static ObservableList<Order> getOrdersByStatus(String selectedStatus) {
        ObservableList<Order> orders = FXCollections.observableArrayList();
        String query = """
        SELECT o.order_id, o.customer_id, c.name AS customer_name, c.phone AS customer_phone, 
               o.total_cost, o.order_date, o.order_status, u.user_id, u.fullname AS user_name, r.role_name,
               GROUP_CONCAT(
                   CONCAT(
                       CASE 
                           WHEN p.name LIKE 'Standard %' THEN 'STD' 
                           WHEN p.name LIKE 'Deluxe %' THEN 'DLX' 
                           WHEN p.name LIKE 'VIP %' THEN 'VIP' 
                           ELSE p.name 
                       END, 
                       SUBSTRING_INDEX(p.name, ' ', -1)
                   ) SEPARATOR ', '
               ) AS currentTableName
        FROM orders o
        JOIN customers c ON o.customer_id = c.customer_id
        JOIN users u ON o.user_id = u.user_id
        JOIN roles r ON u.role_id = r.role_id
        LEFT JOIN bookings b ON o.order_id = b.order_id
        LEFT JOIN pooltables p ON b.table_id = p.table_id
        WHERE o.order_status = ?
        GROUP BY o.order_id
        ORDER BY o.order_date DESC
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, selectedStatus);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                orders.add(new Order(
                        rs.getInt("order_id"),
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("customer_phone"),
                        rs.getInt("user_id"),
                        rs.getString("user_name"),
                        rs.getString("role_name"),
                        rs.getTimestamp("order_date").toLocalDateTime(),
                        rs.getDouble("total_cost"),
                        rs.getString("order_status"),
                        rs.getString("currentTableName")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }


    // Không cần khai báo URL, USER, PASSWORD nữa, vì đã có trong DatabaseConnection
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String query = """
                    SELECT o.order_id, o.customer_id, c.name AS customer_name, c.phone AS customer_phone,
                           o.user_id, u.fullname AS user_name, r.role_name, o.order_date,
                           o.total_cost, o.order_status,
                           GROUP_CONCAT(
                               CONCAT(
                                   CASE 
                                       WHEN p.name LIKE 'Standard %' THEN 'STD' 
                                       WHEN p.name LIKE 'Deluxe %' THEN 'DLX' 
                                       WHEN p.name LIKE 'VIP %' THEN 'VIP' 
                                       ELSE p.name 
                                   END, 
                                   SUBSTRING_INDEX(p.name, ' ', -1)
                               ) SEPARATOR ', '
                           ) AS currentTableName
                    FROM orders o
                    JOIN customers c ON o.customer_id = c.customer_id
                    JOIN users u ON o.user_id = u.user_id
                    JOIN roles r ON u.role_id = r.role_id
                    LEFT JOIN bookings b ON o.order_id = b.order_id
                    LEFT JOIN pooltables p ON b.table_id = p.table_id
                    GROUP BY o.order_id
                    ORDER BY o.order_id DESC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Order order = new Order(
                        rs.getInt("order_id"),
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("customer_phone"),
                        rs.getInt("user_id"),
                        rs.getString("user_name"),
                        rs.getString("role_name"),
                        rs.getTimestamp("order_date").toLocalDateTime(),
                        rs.getDouble("total_cost"),
                        rs.getString("order_status"),
                        rs.getString("currentTableName")
                );
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public void addOrder(Order newOrder) {
        String query = "INSERT INTO orders (customer_id, user_id, order_status, order_date) VALUES (?, ?, 'Playing', ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Set các giá trị vào PreparedStatement
            stmt.setInt(1, newOrder.getCustomerId()); // customer_id
            stmt.setInt(2, newOrder.getUserId()); // user_id
            if(newOrder.getOrderDate() != null) {
                stmt.setTimestamp(3,Timestamp.valueOf(newOrder.getOrderDate())); // order_date
            }
            else stmt.setTimestamp(3,Timestamp.valueOf(LocalDateTime.now()));

            // Thực thi câu lệnh INSERT
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Order added successfully!");
                OrderController.setBillNumberCount(OrderController.getBillNumberCount()+1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean updateOrder(int orderID, int customerId) {
        String query = "UPDATE orders SET customer_id = ? WHERE order_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, customerId); // Cập nhật customer_id
            stmt.setInt(2, orderID); // Cập nhật đúng order_id

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Order getOrderById(int orderId) {
        // Truy vấn từ database và kiểm tra dữ liệu trả về
        String query = "SELECT o.order_id, o.customer_id, o.user_id, o.total_cost, o.order_date, o.order_status, c.name AS customer_name, c.phone AS customer_phone "
                +
                "FROM orders o " +
                "LEFT JOIN customers c ON o.customer_id = c.customer_id " +
                "WHERE o.order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Tạo đối tượng Order từ dữ liệu trả về
                Order order = new Order();
                order.setOrderId(rs.getInt("order_id")); // Kiểm tra xem dữ liệu có đúng không
                order.setCustomerId(rs.getInt("customer_id"));
                order.setUserId(rs.getInt("user_id"));
                order.setTotalCost(rs.getDouble("total_cost"));
                order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
                order.setOrderStatus(rs.getString("order_status"));
                order.setCustomerName(rs.getString("customer_name"));
                order.setCustomerPhone(rs.getString("customer_phone"));
                return order;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteOrder(int orderId) {
        String sql = "DELETE FROM orders WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0; // Nếu xóa thành công, trả về true
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Nếu có lỗi xảy ra, trả về false
        }
    }

    // Thêm các phương thức khác nếu cần (thêm, xóa, sửa order)
    public static Order getOrderByIdStatic(int orderID) {
        Order order = new Order();
        // First query: Get basic order and customer details
        String sqlOrderCustomer = "SELECT o.order_id, o.order_status, " +
                "c.name AS customer_name, c.phone AS customer_phone " +
                "FROM orders o " +
                "JOIN customers c ON o.customer_id = c.customer_id " +
                "WHERE o.order_id = ?";

        // Query to get the most recent booking for this order
        String sqlBookingTable = "SELECT pb.name AS current_table_name " +
                "FROM bookings bk " +
                "JOIN pooltables pb ON pb.table_id = bk.table_id " +
                "WHERE bk.order_id = ? " +
                "ORDER BY bk.booking_id DESC " +
                "LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmtOrderCustomer = conn.prepareStatement(sqlOrderCustomer);
             PreparedStatement pstmtBookingTable = conn.prepareStatement(sqlBookingTable)) {

            // Execute first query
            pstmtOrderCustomer.setInt(1, orderID);
            try (ResultSet rsOrderCustomer = pstmtOrderCustomer.executeQuery()) {
                if (rsOrderCustomer.next()) {
                    order.setOrderId(rsOrderCustomer.getInt("order_id"));
                    order.setOrderStatus(rsOrderCustomer.getString("order_status"));
                    System.out.println("Order Status: " + order.getOrderStatus());
                    order.setCustomerName(rsOrderCustomer.getString("customer_name"));
                    order.setCustomerPhone(rsOrderCustomer.getString("customer_phone"));
                }
            }

            // Execute second query if first query was successful
            pstmtBookingTable.setInt(1, orderID);
            try (ResultSet rsBookingTable = pstmtBookingTable.executeQuery()) {
                if (rsBookingTable.next()) {
                    order.setCurrentTableName(rsBookingTable.getString("current_table_name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error retrieving order details: " + e.getMessage());
        }

        return order;
    }

    public static double calculateOrderTotal(int orderId) {
        double total = 0;
        String query = """
                    SELECT COALESCE(SUM(
                        CASE 
                            WHEN b.total IS NOT NULL THEN b.total 
                            ELSE 0 
                        END +
                        CASE 
                            WHEN oi.total IS NOT NULL THEN oi.total
                            ELSE 0 
                        END
                    ), 0) as total_cost
                    FROM orders o
                    LEFT JOIN bookings b ON o.order_id = b.order_id
                    LEFT JOIN orders_items oi ON o.order_id = oi.order_id
                    WHERE o.order_id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                total = rs.getDouble("total_cost");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }

    public Order getLatestOrderByCustomerId(int customerId) {
        final String query =
                "SELECT o.order_id, o.customer_id, o.user_id, o.order_date, o.order_status, " +
                        "c.name AS customer_name, c.phone AS customer_phone " +
                        "FROM orders o " +
                        "JOIN customers c ON o.customer_id = c.customer_id " +
                        "WHERE o.customer_id = ? " +
                        "ORDER BY o.order_id DESC " +
                        "LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, customerId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Order(
                            rs.getInt("order_id"),
                            rs.getInt("customer_id"),
                            rs.getInt("user_id"),
                            rs.getTimestamp("order_date").toLocalDateTime(),
                            rs.getString("order_status"),
                            rs.getString("customer_name"),
                            rs.getString("customer_phone")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Consider proper logging
        }
        return null;
    }


//    public Order getLatestOrderByCustomerId(int customerId) {
//        String query = "SELECT * FROM orders WHERE customer_id = ? ORDER BY order_id DESC LIMIT 1";
//        Order order = null;
//
//        try (Connection conn = DatabaseConnection.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(query)) {
//
//            stmt.setInt(1, customerId);
//            ResultSet rs = stmt.executeQuery();
//
//            if (rs.next()) { // Lấy order mới nhất
//                order = new Order();
//                order.setOrderId(rs.getInt("order_id"));
//                order.setCustomerId(rs.getInt("customer_id"));
//                order.setUserId(rs.getInt("user_id"));
//                order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
//                order.setOrderStatus(rs.getString("order_status"));
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return order;
//    }

    public static boolean updateOrderStaff(int userId, int orderId) {
        String query = "UPDATE orders SET user_id = ? WHERE order_id = ?"; // Assuming you have staff_id and staff_name columns
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, orderId); // You need to provide the orderId to update the specific order

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0; // Return true if the update was successful
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Return false if there was an error
        }
    }

    public static String getStaffNameByOrderId(int orderId) {
        String staffName = null;
        String query = "SELECT u.fullname FROM users u JOIN orders o ON u.user_id = o.user_id WHERE o.order_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                staffName = rs.getString("fullname");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return staffName;
    }

    public Map<String, Double> getTotalOrdersAndRevenue(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT COUNT(*) as total_orders, SUM(total_cost) as total_revenue FROM orders WHERE order_date BETWEEN ? AND ?";
        Map<String, Double> result = new HashMap<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, java.sql.Date.valueOf(startDate));
            pstmt.setDate(2, java.sql.Date.valueOf(endDate));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    result.put("totalOrders", rs.getDouble("total_orders"));
                    result.put("totalRevenue", rs.getDouble("total_revenue"));
                }
            }
        }

        return result;
    }

    public List<Map<String, Object>> getRevenueByPeriod(String period, LocalDate startDate, LocalDate endDate) throws
            SQLException {
        String sql;
        String dateFormat;

        switch (period) {
            case "Ngày":
                sql = "SELECT DATE(order_date) as date, SUM(total_cost) as revenue FROM orders WHERE order_date BETWEEN ? AND ? GROUP BY DATE(order_date)";
                dateFormat = "yyyy-MM-dd";
                break;
            case "Tháng":
                sql = "SELECT DATE_FORMAT(order_date, '%Y-%m') as date, SUM(total_cost) as revenue FROM orders WHERE order_date BETWEEN ? AND ? GROUP BY DATE_FORMAT(order_date, '%Y-%m')";
                dateFormat = "yyyy-MM";
                break;
            case "Năm":
                sql = "SELECT YEAR(order_date) as date, SUM(total_cost) as revenue FROM orders WHERE order_date BETWEEN ? AND ? GROUP BY YEAR(order_date)";
                dateFormat = "yyyy";
                break;
            default:
                throw new IllegalArgumentException("Invalid period: " + period);
        }

        List<Map<String, Object>> result = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, java.sql.Date.valueOf(startDate));
            pstmt.setDate(2, java.sql.Date.valueOf(endDate));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("date", rs.getString("date"));
                    row.put("revenue", rs.getDouble("revenue"));
                    result.add(row);
                }
            }
        }

        return result;
    }

    public List<Map<String, Object>> getRevenueByTableGroup(LocalDate startDate, LocalDate endDate) throws
            SQLException {
        String sql = "SELECT ct.name as group_name, SUM(b.total) as revenue " +
                "FROM bookings b " +
                "JOIN pooltables pt ON b.table_id = pt.table_id " +
                "JOIN cate_pooltables ct ON pt.cate_id = ct.id " +
                "JOIN orders o ON b.order_id = o.order_id " +
                "WHERE o.order_date BETWEEN ? AND ? " +
                "GROUP BY ct.id";

        List<Map<String, Object>> result = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, java.sql.Date.valueOf(startDate));
            pstmt.setDate(2, java.sql.Date.valueOf(endDate));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("groupName", rs.getString("group_name"));
                    row.put("revenue", rs.getDouble("revenue"));
                    result.add(row);
                }
            }
        }

        return result;
    }

    public List<Map<String, Object>> getRevenueByProductCategory(LocalDate startDate, LocalDate endDate) throws
            SQLException {
        String sql = "SELECT c.category_name, SUM(oi.total) as revenue " +
                "FROM orders_items oi " +
                "JOIN products p ON oi.product_id = p.product_id " +
                "JOIN category c ON p.category_id = c.category_id " +
                "JOIN orders o ON oi.order_id = o.order_id " +
                "WHERE o.order_date BETWEEN ? AND ? " +
                "GROUP BY c.category_id";

        List<Map<String, Object>> result = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, java.sql.Date.valueOf(startDate));
            pstmt.setDate(2, java.sql.Date.valueOf(endDate));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("categoryName", rs.getString("category_name"));
                    row.put("revenue", rs.getDouble("revenue"));
                    result.add(row);
                }
            }
        }

        return result;
    }


}
