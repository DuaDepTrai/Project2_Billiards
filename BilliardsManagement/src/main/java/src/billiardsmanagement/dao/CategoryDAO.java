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
        String sql = "SELECT category_id, category_name FROM category";


        try (Connection connection = TestDBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("category_id");
                String name = resultSet.getString("category_name");
                categories.add(new Category(id, name));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return categories;
    }

    // Thêm danh mục
    public void addCategory(String name) {
        String sql = "INSERT INTO category (category_name) VALUES (?, ?)";
        try (Connection connection = TestDBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Cập nhật danh mục
    public void updateCategory(int id, String newName) {
        String sql = "UPDATE category " + "SET " + "category_name = CASE WHEN ? IS NOT NULL AND ? != '' THEN ? ELSE category_name END, " + "WHERE category_id = ?";
        try (Connection connection = TestDBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newName); // CASE WHEN condition for name
            statement.setString(2, newName);
            statement.setString(3, newName);

            statement.setInt(4, id); // WHERE condition
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
        String query = "SELECT * FROM categories WHERE id = ?";
        System.out.println("DEBUG: Query lấy danh mục: " + query + " với ID: " + id);

        String sql = "SELECT category_id, category_name FROM category WHERE category_id = ?";

        try (Connection connection = TestDBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int categoryId = resultSet.getInt("category_id");
                String name = resultSet.getString("category_name");
                System.out.println("Danh mục từ DB -> ID: " + categoryId + ", Name: " + name);

                return new Category(id, name);
            }else {
                System.out.println("Không tìm thấy danh mục với ID: " + id);
                return null;
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
