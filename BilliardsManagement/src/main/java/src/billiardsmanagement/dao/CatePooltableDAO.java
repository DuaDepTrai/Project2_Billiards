package src.billiardsmanagement.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import src.billiardsmanagement.model.CatePooltable;
import src.billiardsmanagement.model.DatabaseConnection;
import src.billiardsmanagement.model.PoolTable;
import src.billiardsmanagement.service.NotificationService;
import src.billiardsmanagement.model.NotificationStatus;

public class CatePooltableDAO {
    // CRUD operations for cate_pooltables
    public static List<CatePooltable> getAllCategories() {
        List<CatePooltable> categories = new ArrayList<>();
        String query = "SELECT * FROM cate_pooltables";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                categories.add(new CatePooltable(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("shortName"),
                    rs.getDouble("price")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            NotificationService.showNotification("Database Error", 
                "Failed to fetch categories: " + e.getMessage(), 
                NotificationStatus.Error);
        }
        return categories;
    }

    public static int addCategory(CatePooltable category) {
        String query = "INSERT INTO cate_pooltables (name, shortName, price) VALUES (?, ?, ?)";
        int generatedId = -1;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getShortName());
            pstmt.setDouble(3, category.getPrice());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating category failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    generatedId = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating category failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            NotificationService.showNotification("Database Error", 
                "Failed to add category: " + e.getMessage(), 
                NotificationStatus.Error);
        }
        return generatedId;
    }

    public static void updateCategory(CatePooltable category) {
        String query = "UPDATE cate_pooltables SET name = ?, shortName = ?, price = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getShortName());
            pstmt.setDouble(3, category.getPrice());
            pstmt.setInt(4, category.getId());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            NotificationService.showNotification("Database Error", 
                "Failed to update category: " + e.getMessage(), 
                NotificationStatus.Error);
        }
    }

    public static void deleteCategory(int categoryId) {
        // First delete all pool tables in this category
        String deleteTablesQuery = "DELETE FROM pooltables WHERE cate_id = ?";
        String deleteCategoryQuery = "DELETE FROM cate_pooltables WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Delete associated pool tables first
            try (PreparedStatement pstmt = conn.prepareStatement(deleteTablesQuery)) {
                pstmt.setInt(1, categoryId);
                pstmt.executeUpdate();
            }

            // Then delete the category
            try (PreparedStatement pstmt = conn.prepareStatement(deleteCategoryQuery)) {
                pstmt.setInt(1, categoryId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static CatePooltable getCategoryById(int categoryId) {
        CatePooltable category = null;
        String query = "SELECT * FROM cate_pooltables WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, categoryId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                category = new CatePooltable(categoryId, name, price);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return category;
    }

    // Methods for managing pool tables within categories
    public static List<PoolTable> getTablesByCategory(int categoryId) {
        List<PoolTable> tables = new ArrayList<>();
        String query = "SELECT p.table_id, p.name, p.status, cp.name AS category_name, cp.price " +
                "FROM pooltables p " +
                "JOIN cate_pooltables cp ON cp.id = p.cate_id " +
                "WHERE p.cate_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, categoryId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int tableId = rs.getInt("table_id");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                String status = rs.getString("status");
                String categoryName = rs.getString("category_name");
                tables.add(new PoolTable(tableId, name, price, status, categoryName));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tables;
    }

    public static int addTableToCategory(int categoryId, String tableName, String status) {
        String query = "INSERT INTO pooltables (cate_id, name, status) VALUES (?, ?, ?)";
        int generatedId = -1;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, categoryId);
            pstmt.setString(2, tableName);
            pstmt.setString(3, status);
            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                generatedId = generatedKeys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return generatedId;
    }
}