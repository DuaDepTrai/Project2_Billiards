package src.billiardsmanagement.dao;

import src.billiardsmanagement.model.OrderItem;
import src.billiardsmanagement.model.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderItemDAO {
    // Cannot set a new PromotionId when add duplicate order item
    public static boolean addOrderItemDuplicate(OrderItem orderItem) {
        try (Connection con = DatabaseConnection.getConnection()) {
            String updateQuery = """
                UPDATE orders_items 
                SET quantity = ?, 
                    net_total = ?, 
                    subtotal = ?
                WHERE order_id = ? AND order_item_id = ?
            """;
            
            try (PreparedStatement pst = con.prepareStatement(updateQuery)) {
                pst.setInt(1, orderItem.getQuantity());
                pst.setDouble(2, orderItem.getNetTotal());
                pst.setDouble(3, orderItem.getSubTotal());
                pst.setInt(4, orderItem.getOrderId());
                pst.setInt(5, orderItem.getOrderItemId());
                
                int rowsAffected = pst.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi thêm sản phẩm vào đơn hàng: " + e.getMessage());
            return false;
        }
    }
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
            String query = "SELECT ori.*, p.name AS product_name, p.price AS product_price, promo.name AS promotion_name, promo.discount AS promotion_discount FROM orders_items ori JOIN products p ON ori.product_id = p.product_id LEFT JOIN promotions promo ON ori.promotion_id = promo.promotion_id WHERE ori.order_id = ?;";
            PreparedStatement prep = con.prepareStatement(query);
            prep.setInt(1, orderId);
            ResultSet rs = prep.executeQuery();

            while (rs.next()) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderItemId(rs.getInt("order_item_id"));
                orderItem.setOrderId(rs.getInt("order_id"));
                orderItem.setProductId(rs.getInt("product_id"));
                orderItem.setProductName(rs.getString("product_name"));
                orderItem.setProductPrice(rs.getDouble("product_price"));
                orderItem.setQuantity(rs.getInt("quantity"));
                orderItem.setNetTotal(rs.getDouble("net_total"));
                orderItem.setSubTotal(rs.getDouble("subtotal"));
                orderItem.setPromotionId(rs.getInt("promotion_id"));
                orderItem.setPromotionName(rs.getString("promotion_name"));
                orderItem.setPromotionDiscount(rs.getDouble("promotion_discount"));

                orderItemList.add(orderItem);
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
}
