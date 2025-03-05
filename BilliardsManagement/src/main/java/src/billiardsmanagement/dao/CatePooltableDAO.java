package src.billiardsmanagement.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import src.billiardsmanagement.model.CatePooltable;
import src.billiardsmanagement.model.DatabaseConnection;
import src.billiardsmanagement.model.PoolTable;

public class CatePooltableDAO {
    // CRUD operations for cate_pooltables
    public static List<CatePooltable> getAllCategories() {
        List<CatePooltable> categories = new ArrayList<>();
        String query = "SELECT * FROM cate_pooltables";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                categories.add(new CatePooltable(id, name, price));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    public static int addCategory(CatePooltable category) {
        String query = "INSERT INTO cate_pooltables (name, price) VALUES (?, ?)";
        int generatedId = -1;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, category.getName());
            pstmt.setDouble(2, category.getPrice());
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

    public static void updateCategory(CatePooltable category) {
        String query = "UPDATE cate_pooltables SET name = ?, price = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, category.getName());
            pstmt.setDouble(2, category.getPrice());
            pstmt.setInt(3, category.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
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