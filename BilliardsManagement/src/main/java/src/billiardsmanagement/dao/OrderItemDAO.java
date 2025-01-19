package src.billiardsmanagement.dao;

import src.billiardsmanagement.model.OrderItem;
import src.billiardsmanagement.model.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderItemDAO {

    public static boolean addOrderItem(OrderItem orderItem){
        try(Connection con = DatabaseConnection.getConnection()){
            if(con==null) {
                System.err.println("Lỗi kết nối: Không thể kết nối đến cơ sở dữ liệu!");
                return false;
            }
            String addOrderItemQuery = "INSERT INTO orders_items(order_id,product_id,quantity,net_total,subtotal,promotion_id) VALUES (?,?,?,?,?,?)";
            PreparedStatement pre = con.prepareStatement(addOrderItemQuery);

            pre.setInt(1,orderItem.getOrderId());
            pre.setInt(2,orderItem.getProductId());
            pre.setInt(3,orderItem.getQuantity());
            pre.setDouble(4,orderItem.getNetTotal());
            pre.setDouble(5,orderItem.getSubTotal());
            if(orderItem.getPromotionId()>0) pre.setInt(6,orderItem.getPromotionId());
            else pre.setNull(6,Types.INTEGER);

            int affectedRows = pre.executeUpdate();
            if(affectedRows>0) return true;
        } catch (Exception e) {
            System.err.println("Lỗi thêm sản phẩm vào đơn hàng: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static ArrayList<OrderItem> getForEachOrderItem(int orderId) {
        try (Connection con = DatabaseConnection.getConnection()) {
            if (con == null) {
                System.err.println("Lỗi kết nối: Không thể kết nối đến cơ sở dữ liệu!");
                return new ArrayList<>();
            }
            ArrayList<OrderItem> orderItemList = new ArrayList<>();
            String query = "SELECT ori.*,p.name FROM orders_items ori JOIN products p ON ori.product_id = p.product_id WHERE ori.order_id = ?;";
            PreparedStatement prep = con.prepareStatement(query);
            prep.setInt(1, orderId);
            ResultSet rs = prep.executeQuery();

            while (rs.next()) {
                orderItemList.add(new OrderItem(
                        rs.getInt("order_item_id"),
                        rs.getInt("order_id"),
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getDouble("net_total"),
                        rs.getDouble("subtotal"),
                        rs.getInt("promotion_id")
                ));
            }
            return orderItemList;
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn: Không thể lấy danh sách sản phẩm cho đơn hàng " + orderId);
            return null;
        }
    }

    public static boolean updateOrderItem(OrderItem orderItem) {
        try (Connection con = DatabaseConnection.getConnection()) {
            if (con == null) throw new SQLException("Lỗi kết nối: Không thể kết nối đến cơ sở dữ liệu!");
            
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("UPDATE orders_items SET ")
                    .append("product_id = ?, ")
                    .append("quantity = ?, ")
                    .append("net_total = ?, ")
                    .append("subtotal = ?, ")
                    .append("promotion_id = ? ")
                    .append("WHERE order_id = ? AND order_item_id = ?");
            
            String query = queryBuilder.toString();
            
            try (PreparedStatement pst = con.prepareStatement(query)) {
                pst.setInt(1, orderItem.getProductId());
                pst.setInt(2, orderItem.getQuantity());
                pst.setDouble(3, orderItem.getNetTotal());
                pst.setDouble(4, orderItem.getSubTotal());
                
                // Handle promotion ID: set to null if -1
                if (orderItem.getPromotionId() < 0) {
                    pst.setNull(5, java.sql.Types.INTEGER);
                } else {
                    pst.setInt(5, orderItem.getPromotionId());
                }
                
                pst.setInt(6, orderItem.getOrderId());
                pst.setInt(7, orderItem.getOrderItemId());
                
                int rowsAffected = pst.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteOrderItem(int orderItemId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Prepare delete statement
            String deleteQuery = "DELETE FROM orders_items WHERE order_item_id = ?";
            
            try (PreparedStatement pst = conn.prepareStatement(deleteQuery)) {
                pst.setInt(1, orderItemId);
                
                // Execute delete
                int rowsAffected = pst.executeUpdate();
                
                // Return true if at least one row was deleted
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // // Lấy tất cả OrderItems theo order_id
    // public List<OrderItem> getOrderItemsByOrderId(int orderId) {
    //     List<OrderItem> orderItems = new ArrayList<>();
    //     String query = """
    //         SELECT oi.order_item_id, oi.order_id, oi.product_id, p.name AS product_name, 
    //                oi.quantity, oi.net_total, oi.subtotal, oi.promotion_id
    //         FROM orders_items oi
    //         JOIN products p ON oi.product_id = p.product_id
    //         WHERE oi.order_id = ?
    //     """;  // Câu truy vấn SQL để lấy danh sách các order item

    //     // Sử dụng DatabaseConnection để lấy kết nối
    //     try (Connection conn = DatabaseConnection.getConnection();
    //          PreparedStatement stmt = conn.prepareStatement(query)) {
    //         stmt.setInt(1, orderId);
    //         ResultSet rs = stmt.executeQuery();

    //         // Duyệt qua kết quả trả về và thêm vào danh sách orderItems
    //         while (rs.next()) {
    //             OrderItem orderItem = new OrderItem(
    //                     rs.getInt("order_item_id"),
    //                     rs.getInt("order_id"),
    //                     rs.getInt("product_id"),
    //                     rs.getString("product_name"),  // Lấy tên sản phẩm từ bảng products
    //                     rs.getInt("quantity"),
    //                     rs.getDouble("net_total"),
    //                     rs.getDouble("subtotal"),
    //                     rs.getInt("promotion_id")
    //             );
    //             orderItems.add(orderItem);
    //         }
    //     } catch (SQLException e) {
    //         e.printStackTrace();  // In lỗi nếu có sự cố trong quá trình truy vấn
    //     }

    //     return orderItems;  // Trả về danh sách các order item
    // }

    // // Thêm OrderItem mới vào bảng orders_items
    // public static boolean addOrderItem(OrderItem orderItem){
    //     try(Connection con = DatabaseConnection.getConnection()){
    //         if(con==null) {
    //             System.err.println("Lỗi kết nối: Không thể kết nối đến cơ sở dữ liệu!");
    //             return false;
    //         }
    //         String addOrderItemQuery = "INSERT INTO orders_items(order_id,product_id,quantity,net_total,subtotal,promotion_id) VALUES (?,?,?,?,?,?)";
    //         PreparedStatement pre = con.prepareStatement(addOrderItemQuery);

    //         pre.setInt(1,orderItem.getOrderId());
    //         pre.setInt(2,orderItem.getProductId());
    //         pre.setInt(3,orderItem.getQuantity());
    //         pre.setDouble(4,orderItem.getNetTotal());
    //         pre.setDouble(5,orderItem.getSubTotal());
    //         if(orderItem.getPromotionId()>0) pre.setInt(6,orderItem.getPromotionId());
    //         else pre.setNull(6,Types.INTEGER);

    //         int affectedRows = pre.executeUpdate();
    //         if(affectedRows>0) return true;
    //     } catch (Exception e) {
    //         System.err.println("Lỗi thêm sản phẩm vào đơn hàng: " + e.getMessage());
    //         e.printStackTrace();
    //     }
    //     return false;
    // }

    // // Cập nhật thông tin OrderItem
    // public boolean updateOrderItem(OrderItem currentOrderItem) {
    //     String query = "UPDATE orders_items oi SET product_id = ?, quantity = ?, net_total = ?, subtotal = ?, promotion_id = ? WHERE order_item_id = ?";

    //     try (Connection conn = DatabaseConnection.getConnection();
    //          PreparedStatement stmt = conn.prepareStatement(query)) {

    //         // Set các giá trị vào PreparedStatement
    //         stmt.setInt(1, currentOrderItem.getProductId());   // product_id
    //         stmt.setInt(2, currentOrderItem.getQuantity());     // quantity
    //         stmt.setDouble(3, currentOrderItem.getNetTotal());  // net_total
    //         stmt.setDouble(4, currentOrderItem.getSubTotal());  // subtotal
    //         stmt.setInt(5, currentOrderItem.getPromotionId());  // promotion_id
    //         stmt.setInt(6, currentOrderItem.getOrderItemId());  // order_item_id

    //         // Thực thi câu lệnh UPDATE
    //         int rowsAffected = stmt.executeUpdate();
    //         if (rowsAffected > 0) {
    //             System.out.println("OrderItem updated successfully!");
    //             return true;
    //         }
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //     }
    //     return false;
    // }

    // // Xóa OrderItem khỏi bảng orders_items
    // public boolean deleteOrderItem(int orderItemId) {
    //     String sql = "DELETE FROM orders_items WHERE order_item_id = ?";

    //     try (Connection conn = DatabaseConnection.getConnection();
    //          PreparedStatement stmt = conn.prepareStatement(sql)) {
    //         stmt.setInt(1, orderItemId);
    //         int rowsAffected = stmt.executeUpdate();
    //         return rowsAffected > 0;  // Nếu xóa thành công, trả về true
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //         return false;  // Nếu có lỗi xảy ra, trả về false
    //     }
    // }
}
