package src.billiardsmanagement.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import src.billiardsmanagement.model.Category;
import src.billiardsmanagement.model.DatabaseConnection;
import src.billiardsmanagement.model.Pair;
import src.billiardsmanagement.model.TestDBConnection;

public class CategoryDAO {

    // Lấy tất cả danh mục
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT category_id, category_name, image_path FROM category";

        try (Connection connection = TestDBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("category_id");
                String name = resultSet.getString("category_name");
                String imagePath = resultSet.getString("image_path");
                categories.add(new Category(id, name, imagePath));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return categories;
    }

    // Thêm danh mục
    public void addCategory(String name, String imagePath) {
        String sql = "INSERT INTO category (category_name, image_path) VALUES (?, ?)";
        try (Connection connection = TestDBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, imagePath);
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Cập nhật danh mục
    public void updateCategory(int id, String newName, String newImagePath) {
        String sql = "UPDATE category " + "SET " + "category_name = CASE WHEN ? IS NOT NULL AND ? != '' THEN ? ELSE category_name END, " + "image_path = CASE WHEN ? IS NOT NULL AND ? != '' THEN ? ELSE image_path END " + "WHERE category_id = ?";
        try (Connection connection = TestDBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newName); // CASE WHEN condition for name
            statement.setString(2, newName);
            statement.setString(3, newName);

            statement.setString(4, newImagePath); // CASE WHEN condition for image_path
            statement.setString(5, newImagePath);
            statement.setString(6, newImagePath);

            statement.setInt(7, id); // WHERE condition
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Xóa danh mục
    public void removeCategory(int id) {
        String sql = "DELETE FROM category WHERE category_id = ?";
        try (Connection connection = TestDBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Kiểm tra xem danh mục có sản phẩm hay không
    public boolean hasProducts(int categoryId) {
        String sql = "SELECT COUNT(*) FROM products WHERE category_id = ?";
        try (Connection connection = TestDBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, categoryId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Map<String, String> getProductAndCategoryUnitMap() {
        String query = "SELECT p.name AS productName, ct.category_name FROM products p JOIN category ct ON p.category_id = ct.category_id";
        try (Connection con = DatabaseConnection.getConnection()) {
            assert con != null : "Error: Connection is null! ";
            Statement stm = con.createStatement();
            ResultSet resultSet = stm.executeQuery(query);

            Map<String, String> map = new HashMap<>(); // Using a HashMap for better performance and readability
            while (resultSet.next()) {
                String productName = resultSet.getString("productName");
                String categoryName = resultSet.getString("category_name");
                map.put(productName, categoryName);
            }
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    public Category getCategoryById(int id) {
        String sql = "SELECT category_id, category_name, image_path FROM category WHERE category_id = ?";

        try (Connection connection = TestDBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String name = resultSet.getString("category_name");
                String imagePath = resultSet.getString("image_path");
                return new Category(id, name, imagePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Trả về null nếu không tìm thấy danh mục
    }

    // Lấy đơn vị (unit) của sản phẩm từ bảng orders_items và products
    public static String getUnitByProductId(int productId) {
        String sql = "SELECT p.unit " + "FROM orders_items oi " + "JOIN products p ON oi.product_id = p.product_id " + "WHERE p.product_id = ?";

        try (Connection connection = TestDBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("unit");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
