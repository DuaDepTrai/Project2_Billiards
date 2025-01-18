package src.billiardsmanagement.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    // Lấy tất cả danh mục
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT category_id, category_name, image_path FROM category";

        try (Connection connection = TestDBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

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
        try (Connection connection = TestDBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, imagePath);
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Cập nhật danh mục
    public void updateCategory(int id, String newName, String newImagePath) {
        String sql = "UPDATE category " +
                "SET " +
                "category_name = CASE WHEN ? IS NOT NULL AND ? != '' THEN ? ELSE category_name END, " +
                "image_path = CASE WHEN ? IS NOT NULL AND ? != '' THEN ? ELSE image_path END " +
                "WHERE category_id = ?";
        try (Connection connection = TestDBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
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
        try (Connection connection = TestDBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Kiểm tra xem danh mục có sản phẩm hay không
    public boolean hasProducts(int categoryId) {
        String sql = "SELECT COUNT(*) FROM products WHERE category_id = ?";
        try (Connection connection = TestDBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
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
}