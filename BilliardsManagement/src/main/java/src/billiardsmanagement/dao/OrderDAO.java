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
            JOIN customers c ON o.customer_id = c.customer_id
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
        String query = "UPDATE orders SET  total_cost = ?, order_status = ? WHERE order_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Cập nhật các trường
            stmt.setDouble(1, currentOrder.getTotalCost());     // total_cost
            stmt.setString(2, currentOrder.getOrderStatus());   // order_status
            stmt.setInt(3, currentOrder.getOrderId());          // order_id

            // Thực thi câu lệnh UPDATE
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Order updated successfully!");
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Order getOrderById(int orderId) {
        // Truy vấn SQL để lấy thông tin đơn hàng
        String query = "SELECT * FROM orders WHERE order_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Gán giá trị cho tham số truy vấn
            statement.setInt(1, orderId);

            // Thực thi truy vấn
            ResultSet resultSet = statement.executeQuery();

            // Kiểm tra kết quả
            if (resultSet.next()) {
                // Lấy các giá trị từ bảng orders
                int id = resultSet.getInt("order_id");
                int customerId = resultSet.getInt("customer_id");
                double totalCost = resultSet.getDouble("total_cost");
                String orderStatus = resultSet.getString("order_status");

                // Tạo đối tượng Customer (nếu cần liên kết)
             // Giả sử Customer có constructor nhận customer_id

                // Tạo và trả về đối tượng Order
                return new Order( customerId, totalCost, orderStatus);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Trả về null nếu không tìm thấy hoặc có lỗi
        return null;
    }

    // Thêm các phương thức khác nếu cần (thêm, xóa, sửa order)
}
