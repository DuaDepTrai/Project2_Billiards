package src.billiardsmanagement.dao;

import src.billiardsmanagement.model.OrderItem;
import src.billiardsmanagement.model.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderItemDAO {
    // ANSI color codes for console output
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_RESET = "\u001B[0m";

    // Cannot set a new PromotionId when add duplicate order item
    public static boolean addOrderItemDuplicate(OrderItem orderItem) {
        try (Connection con = DatabaseConnection.getConnection()) {
            String updateQuery = """
                        UPDATE orders_items
                        SET quantity = ?,
                            total = ?
                        WHERE order_id = ? AND order_item_id = ?
                    """;

            try (PreparedStatement pst = con.prepareStatement(updateQuery)) {
                pst.setInt(1, orderItem.getQuantity());
                pst.setDouble(2, orderItem.getTotal());
                pst.setInt(3, orderItem.getOrderId());
                pst.setInt(4, orderItem.getOrderItemId());

                int rowsAffected = pst.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            System.err.println(ANSI_RED + "✖ Error adding product to order: " + e.getMessage() + ANSI_RESET);
            return false;
        }
    }

    public static boolean addOrderItem(OrderItem orderItem) {
        try (Connection con = DatabaseConnection.getConnection()) {
            if (con == null) {
                System.err.println(ANSI_RED + "✖ Connection Error: Cannot connect to database!" + ANSI_RESET);
                return false;
            }

            // First, verify if the order exists
            String checkOrderQuery = "SELECT 1 FROM orders WHERE order_id = ?";
            try (PreparedStatement checkStmt = con.prepareStatement(checkOrderQuery)) {
                checkStmt.setInt(1, orderItem.getOrderId());
                System.out.println("Add Order Item prepared = "+checkStmt);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        System.err.println(ANSI_RED + "✖ Error: Order does not exist with ID " + orderItem.getOrderId()
                                + ANSI_RESET);
                        return false;
                    }
                }
            }

            // If order exists, proceed with adding order item
            String addOrderItemQuery = "INSERT INTO orders_items(order_id,product_id,quantity,total) VALUES (?,?,?,?)";
            try (PreparedStatement pre = con.prepareStatement(addOrderItemQuery)) {
                pre.setInt(1, orderItem.getOrderId());
                pre.setInt(2, orderItem.getProductId());
                pre.setInt(3, orderItem.getQuantity());
                pre.setDouble(4, orderItem.getTotal());
                System.out.println("Add Order Item Query = " + pre);
                // Promotion
                // if(orderItem.getPromotionId() > 0) {
                // pre.setInt(6, orderItem.getPromotionId());
                // } else {
                // pre.setNull(6, Types.INTEGER);
                // }

                int affectedRows = pre.executeUpdate();
                return affectedRows > 0;
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            System.err.println(ANSI_RED + "✖ Constraint Violation Error: " + e.getMessage() + ANSI_RESET);
            System.err.println(ANSI_RED + "✖ Possible reasons:" + ANSI_RESET);
            System.err.println(ANSI_RED + "  1. The referenced order does not exist" + ANSI_RESET);
            System.err.println(ANSI_RED + "  2. Foreign key constraint violation" + ANSI_RESET);
            return false;
        } catch (Exception e) {
            System.err.println(ANSI_RED + "✖ Error adding product to order: " + e.getMessage() + ANSI_RESET);
            e.printStackTrace();
            return false;
        }
    }

    // Rest of the methods remain the same, just update error messages to English
    // with red color
    public static ArrayList<OrderItem> getForEachOrderItem(int orderId) {
        try (Connection con = DatabaseConnection.getConnection()) {
            if (con == null) {
                System.err.println(ANSI_RED + "✖ Connection Error: Cannot connect to database!" + ANSI_RESET);
                return new ArrayList<>();
            }
            ArrayList<OrderItem> orderItemList = new ArrayList<>();
            String query = "SELECT ori.*, p.name AS product_name, p.price AS product_price " +
                    "FROM orders_items ori " +
                    "JOIN products p ON ori.product_id = p.product_id " +
                    "WHERE ori.order_id = ?;";

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
                orderItem.setTotal(rs.getDouble("total"));

                orderItemList.add(orderItem);
            }
            return orderItemList;
        } catch (SQLException e) {
            System.err.println(
                    ANSI_RED + "✖ Query Error: Cannot retrieve product list for order " + orderId + ANSI_RESET);
            e.printStackTrace();
        }
        // cannot use check Null in this function !
        return new ArrayList<>();
    }

    // Similar changes to other methods for error handling and message translation
    public static boolean updateOrderItem(OrderItem orderItem) {
        try (Connection con = DatabaseConnection.getConnection()) {
            if (con == null)
                throw new SQLException("Lỗi kết nối: Không thể kết nối đến cơ sở dữ liệu!");

            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("UPDATE orders_items SET ")
                    .append("product_id = ?, ")
                    .append("quantity = ?, ")
                    .append("total = ? ")
                    .append("WHERE order_id = ? AND order_item_id = ?");

            String query = queryBuilder.toString();

            try (PreparedStatement pst = con.prepareStatement(query)) {
                pst.setInt(1, orderItem.getProductId());
                pst.setInt(2, orderItem.getQuantity());
                pst.setDouble(3, orderItem.getTotal());

                pst.setInt(4, orderItem.getOrderId());
                pst.setInt(5, orderItem.getOrderItemId());

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
