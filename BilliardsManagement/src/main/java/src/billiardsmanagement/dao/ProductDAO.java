package src.billiardsmanagement.dao;

import src.billiardsmanagement.model.Product;
import src.billiardsmanagement.model.TestDBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    // Phương thức để lấy tất cả sản phẩm
    public List<Product> getAllProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.product_id, p.name, c.category_name, p.price, p.unit, p.quantity " +
                "FROM products p " +
                "JOIN category c ON p.category_id = c.category_id";

        try (Connection connection = TestDBConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                int id = resultSet.getInt("product_id");
                String name = resultSet.getString("name");
                String category = resultSet.getString("category_name");
                double price = resultSet.getDouble("price");
                String unit = resultSet.getString("unit");
                int quantity = resultSet.getInt("quantity");

                products.add(new Product(id, name, category, price, unit, quantity));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return products;
    }

    // Phương thức để thêm sản phẩm mới
    public void addProduct(String name, int categoryId, double price, String unit, int quantity) throws SQLException {
        String sql = "INSERT INTO products (name, category_id, price, unit, quantity) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = TestDBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.setInt(2, categoryId);
            statement.setDouble(3, price);
            statement.setString(4, unit);
            statement.setInt(5, quantity);
            statement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Phương thức để cập nhật thông tin sản phẩm
    public void updateProduct(int productId, String name, int categoryId, double price, String unit, int quantity) throws SQLException {
        String sql = "UPDATE products SET name = ?, category_id = ?, price = ?, unit = ?, quantity = ? WHERE product_id = ?";
        try (Connection connection = TestDBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.setInt(2, categoryId);
            statement.setDouble(3, price);
            statement.setString(4, unit);
            statement.setInt(5, quantity);
            statement.setInt(6, productId);
            statement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Phương thức để stockUp
    public void stockUp(int productId, int quantity) throws SQLException {
        String sql = "UPDATE products SET quantity = quantity + ? WHERE product_id = ?";
        try (Connection connection = TestDBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, quantity);
            statement.setInt(2, productId);
            statement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Phương thức để xóa sản phẩm
    public void removeProduct(int productId) throws SQLException {
        String sql = "DELETE FROM products WHERE product_id = ?";
        try (Connection connection = TestDBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, productId);
            statement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Phương thức để lấy sản phẩm theo danh mục
    public List<Product> getProductsByCategory(int categoryId) throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.product_id, p.name, c.category_name, p.price, p.unit, p.quantity " +
                "FROM products p " +
                "JOIN category c ON p.category_id = c.category_id " +
                "WHERE c.category_id = ?";

        try (Connection connection = TestDBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, categoryId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("product_id");
                String name = resultSet.getString("name");
                String category = resultSet.getString("category_name");
                double price = resultSet.getDouble("price");
                String unit = resultSet.getString("unit");
                int quantity = resultSet.getInt("quantity");

                products.add(new Product(id, name, category, price, unit, quantity));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return products;
    }
}