package src.billiardsmanagement.dao;

import src.billiardsmanagement.model.RentCue;
import src.billiardsmanagement.model.RentCueStatus;
import src.billiardsmanagement.model.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RentCueDAO {
    public static List<RentCue> getAllRentCuesByOrderId(int orderId) {
        List<RentCue> rentCues = new ArrayList<>();
        String query = "SELECT rc.*, p.name as product_name, p.price as product_price, " +
                "promo.name as promotion_name, promo.discount as promotion_discount " +
                "FROM rent_cues rc " +
                "JOIN products p ON rc.product_id = p.product_id " +
                "LEFT JOIN promotions promo ON rc.promotion_id = promo.promotion_id " +
                "WHERE rc.order_id = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            
            preparedStatement.setInt(1, orderId);
            
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    RentCue rentCue = new RentCue();
                    
                    rentCue.setRentCueId(rs.getInt("rent_cue_id"));
                    rentCue.setOrderId(rs.getInt("order_id"));
                    rentCue.setProductId(rs.getInt("product_id"));
                    rentCue.setProductName(rs.getString("product_name"));
                    rentCue.setProductPrice(rs.getDouble("product_price"));
                    
                    // Convert timestamp to LocalDateTime
                    rentCue.setStartTime(rs.getObject("start_time", LocalDateTime.class));
                    rentCue.setEndTime(rs.getObject("end_time", LocalDateTime.class));
                    
                    rentCue.setTimeplay(rs.getDouble("timeplay"));
                    rentCue.setNetTotal(rs.getDouble("net_total"));
                    rentCue.setSubTotal(rs.getDouble("subtotal"));
                    rentCue.setPromotionId(rs.getInt("promotion_id"));
                    rentCue.setPromotionName(rs.getString("promotion_name"));
                    rentCue.setPromotionDiscount(rs.getDouble("promotion_discount"));
                    rentCue.setQuantity(rs.getInt("quantity"));
                    
                    // Set status based on endTime
                    rentCue.setStatus(rentCue.getEndTime() == null ? 
                        RentCueStatus.Rented : RentCueStatus.Returned);
                    
                    rentCues.add(rentCue);
                    System.out.println(rentCue);
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.err.println("Lỗi truy vấn: Không thể lấy danh sách thuê cơ cho đơn hàng " + orderId);
            return null;
        }
        
        return rentCues;
    }

    public static boolean addRentCue(RentCue rentCue) {
        String insertRentCueQuery = "INSERT INTO rent_cues " +
                "(rent_cue_id, order_id, product_id, start_time, " +
                "promotion_id, quantity) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        
        String updateProductQuantityQuery = "UPDATE products SET quantity = quantity - ? WHERE product_id = ?";
        
        Connection connection = null;
        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false);  // Start transaction
            
            // Insert Rent Cue
            try (PreparedStatement rentCueStmt = connection.prepareStatement(insertRentCueQuery)) {
                rentCueStmt.setInt(1, rentCue.getRentCueId());
                rentCueStmt.setInt(2, rentCue.getOrderId());
                rentCueStmt.setInt(3, rentCue.getProductId());
                rentCueStmt.setObject(4, rentCue.getStartTime());
                
                if (rentCue.getPromotionId() > 0) {
                    rentCueStmt.setInt(5, rentCue.getPromotionId());
                } else {
                    rentCueStmt.setNull(5, java.sql.Types.INTEGER);
                }
                
                rentCueStmt.setInt(6, rentCue.getQuantity());
                
                rentCueStmt.executeUpdate();
            }
            
            // Update Product Quantity
            try (PreparedStatement productStmt = connection.prepareStatement(updateProductQuantityQuery)) {
                productStmt.setInt(1, rentCue.getQuantity());
                productStmt.setInt(2, rentCue.getProductId());
                productStmt.executeUpdate();
            }
            
            connection.commit();  // Commit transaction
            return true;
            
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();  // Rollback transaction on error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);  // Reset to default
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean endCueRental(RentCue rentCue) {
        String updateRentCueQuery = "UPDATE rent_cues SET end_time = ?, net_total = ?, subtotal = ? WHERE rent_cue_id = ?";
        String updateProductQuantityQuery = "UPDATE products SET quantity = quantity + ? WHERE product_id = ?";
        
        Connection connection = null;
        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false);  // Start transaction
            
            // Update Rent Cue with end time and totals
            try (PreparedStatement rentCueStmt = connection.prepareStatement(updateRentCueQuery)) {
                rentCueStmt.setObject(1, rentCue.getEndTime());
                rentCueStmt.setDouble(2, rentCue.getNetTotal());
                rentCueStmt.setDouble(3, rentCue.getSubTotal());
                rentCueStmt.setInt(4, rentCue.getRentCueId());
                
                rentCueStmt.executeUpdate();
            }
            
            // Update Product Quantity (return the rented quantity)
            try (PreparedStatement productStmt = connection.prepareStatement(updateProductQuantityQuery)) {
                productStmt.setInt(1, rentCue.getQuantity());
                productStmt.setInt(2, rentCue.getProductId());
                productStmt.executeUpdate();
            }
            
            connection.commit();  // Commit transaction
            return true;
            
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();  // Rollback transaction on error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);  // Reset to default
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean deleteRentCue(RentCue rentCue) {
        String deleteRentCueQuery = "DELETE FROM rent_cues WHERE rent_cue_id = ? AND order_id = ? AND product_id = ?";
        String updateProductQuantityQuery = "UPDATE products SET quantity = quantity + ? WHERE product_id = ?";
        
        Connection connection = null;
        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false);  // Start transaction
            
            // Delete Rent Cue
            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteRentCueQuery)) {
                deleteStmt.setInt(1, rentCue.getRentCueId());
                deleteStmt.setInt(2, rentCue.getOrderId());
                deleteStmt.setInt(3, rentCue.getProductId());
                
                int rowsAffected = deleteStmt.executeUpdate();
                
                // If no rows were deleted, rollback and return false
                if (rowsAffected == 0) {
                    connection.rollback();
                    return false;
                }
            }
            
            // Update Product Quantity (return the rented quantity)
            try (PreparedStatement productStmt = connection.prepareStatement(updateProductQuantityQuery)) {
                productStmt.setInt(1, rentCue.getQuantity());
                productStmt.setInt(2, rentCue.getProductId());
                productStmt.executeUpdate();
            }
            
            connection.commit();  // Commit transaction
            return true;
            
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();  // Rollback transaction on error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);  // Reset to default
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Additional method to get next rent cue ID
    public int getNextRentCueId() {
        String query = "SELECT COALESCE(MAX(rent_cue_id), 0) + 1 AS next_id FROM rent_cues";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet rs = preparedStatement.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt("next_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 1;  // Default if no existing records
    }
}
