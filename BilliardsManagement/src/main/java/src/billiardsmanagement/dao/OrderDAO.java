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
            SELECT o.order_id, o.customer_id, c.name AS customer_name, o.total_cost, o.order_status
            FROM orders o
            JOIN customers c ON o.customer_id = c.customer_id ORDER BY o.order_id
        """;  // Câu truy vấn SQL để lấy danh sách đơn hàng

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
        String query = "INSERT INTO orders (customer_id, total_cost, order_status) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Set các giá trị vào PreparedStatement
            stmt.setInt(1, newOrder.getCustomerId());        // customer_id
            stmt.setDouble(2, newOrder.getTotalCost());      // total_cost
            stmt.setString(3, newOrder.getOrderStatus());    // order_status

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
        // Cập nhật câu lệnh SQL với logic tính toán total_cost
        String query = "UPDATE orders o SET total_cost = ("
                + "  COALESCE((SELECT oi.net_total FROM orders_items oi WHERE oi.order_id = o.order_id LIMIT 1), 0)"
                + "  + COALESCE((SELECT rc.net_total FROM rent_cues rc WHERE rc.order_id = o.order_id LIMIT 1), 0)"
                + "  + COALESCE((SELECT b.net_total FROM bookings b WHERE b.order_id = o.order_id LIMIT 1), 0)"
                + "), order_status = ? WHERE order_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Cập nhật order_status và order_id
            stmt.setString(1, currentOrder.getOrderStatus());   // order_status
            stmt.setInt(2, currentOrder.getOrderId());          // order_id

            // Thực thi câu lệnh UPDATE
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Order updated successfully!");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public Order getOrderById(int orderId) {
        // Truy vấn từ database và kiểm tra dữ liệu trả về
        String query = "SELECT * FROM orders WHERE order_id = ?";
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
        Order order = null;

        // First query: Get basic order and customer details
        String sqlOrderCustomer = "SELECT o.order_id, o.order_status, " +
                                  "c.name AS customer_name, c.phone AS customer_phone " +
                                  "FROM orders o " +
                                  "JOIN customers c ON o.customer_id = c.customer_id " +
                                  "WHERE o.order_id = ?";

        // Second query: Get table name from bookings
        String sqlBookingTable = "SELECT pt.name AS current_table_name " +
                                 "FROM bookings b " +
                                 "JOIN pooltables pt ON b.table_id = pt.table_id " +
                                 "WHERE b.order_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmtOrderCustomer = conn.prepareStatement(sqlOrderCustomer);
             PreparedStatement pstmtBookingTable = conn.prepareStatement(sqlBookingTable)) {

            // Execute first query
            pstmtOrderCustomer.setInt(1, orderID);
            try (ResultSet rsOrderCustomer = pstmtOrderCustomer.executeQuery()) {
                if (rsOrderCustomer.next()) {
                    order.setOrderId(rsOrderCustomer.getInt("order_id"));
                    order.setOrderStatus(rsOrderCustomer.getString("order_status"));
                    order.setCustomerName(rsOrderCustomer.getString("customer_name"));
                    order.setCustomerPhone(rsOrderCustomer.getString("customer_phone"));
                }
            }

            System.out.println("Order : "+order);

            // Execute second query if first query was successful
            if (order != null) {
                pstmtBookingTable.setInt(1, orderID);
                try (ResultSet rsBookingTable = pstmtBookingTable.executeQuery()) {
                    if (rsBookingTable.next()) {
                        order.setCurrentTableName(rsBookingTable.getString("current_table_name"));
                    }
                }
            }

            

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error retrieving order details: " + e.getMessage());
        }

        return order;
    }
}
