package src.billiardsmanagement.dao;

import src.billiardsmanagement.model.Order;
import src.billiardsmanagement.model.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    // Không cần khai báo URL, USER, PASSWORD nữa, vì đã có trong DatabaseConnection
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String query = """
        SELECT o.order_id, o.customer_id, c.name AS customer_name, c.phone AS customer_phone, 
               o.total_cost, o.order_status
        FROM orders o
        JOIN customers c ON o.customer_id = c.customer_id
        ORDER BY o.order_id
    """;  // Câu truy vấn SQL để lấy danh sách đơn hàng (bao gồm số điện thoại khách hàng)

        // Sử dụng DatabaseConnection để lấy kết nối
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            // Duyệt qua kết quả trả về và thêm vào danh sách orders
            while (rs.next()) {
                Order order = new Order(
                        rs.getInt("order_id"),
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("customer_phone"),  // Thêm phone vào đối tượng Order
                        rs.getDouble("total_cost"),
                        rs.getString("order_status")
                );
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();  // In lỗi nếu có sự cố trong quá trình truy vấn
        }

        return orders;  // Trả về danh sách các đơn hàng
    }


    public void addOrder(Order newOrder) {
        String query = "INSERT INTO orders (customer_id,order_status) VALUES ( ?, 'Pending')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Set các giá trị vào PreparedStatement
            stmt.setInt(1, newOrder.getCustomerId());        // customer_id
            // Thực thi câu lệnh INSERT
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Order added successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean updateOrder(Order currentOrder) {
        String query = "UPDATE orders SET order_status = ? WHERE order_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, currentOrder.getOrderStatus()); // order_status
            stmt.setInt(2, currentOrder.getOrderId());        // order_id

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Order getOrderById(int orderId) {
        // Truy vấn từ database và kiểm tra dữ liệu trả về
        String query = "SELECT o.order_id, o.total_cost, o.order_status, c.name AS customer_name, c.phone AS customer_phone " +
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
                order.setTotalCost(rs.getDouble("total_cost"));
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
            return rowsAffected > 0;  // Nếu xóa thành công, trả về true
        } catch (SQLException e) {
            e.printStackTrace();
            return false;  // Nếu có lỗi xảy ra, trả về false
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
}
