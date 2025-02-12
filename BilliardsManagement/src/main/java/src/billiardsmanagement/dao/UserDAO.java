package src.billiardsmanagement.dao;

import src.billiardsmanagement.model.DatabaseConnection;
import src.billiardsmanagement.model.Pair;
import src.billiardsmanagement.model.User;
import src.billiardsmanagement.model.TestDBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class UserDAO {
    // Phương thức để lấy tất cả users
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u.user_id, u.username, u.password, r.role_name, u.image_path " +
                "FROM users u " +
                "JOIN roles r ON u.role_id = r.role_id";

        try (Connection connection = TestDBConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                int user_id = resultSet.getInt("user_id");
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");
                String role = resultSet.getString("role_name");
                String imagePath = resultSet.getString("image_path");

                users.add(new User(user_id, username, password, role, imagePath));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return users;
    }

    // Phương thức để thêm user mới
    public void addUser(String username, String password, int role_id, String imagePath) throws SQLException {
        String sql = "INSERT INTO users (username, password, role_id, image_path) VALUES (?, ?, ?, ?)";
        try (Connection connection = TestDBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);
            statement.setInt(3, role_id);
            statement.setString(4, imagePath);
            statement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Phương thức để cập nhật thông tin sản phẩm
    public void updateUser(int user_id, String username, String password, int role_id, String imagePath) throws SQLException {
        String sql = "UPDATE users SET username = ?, password = ?, role_id = ?, image_path = ? WHERE user_id = ?";
        try (Connection connection = TestDBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);
            statement.setInt(3, role_id);
            statement.setString(4, imagePath);
            statement.setInt(5, user_id);
            statement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Phương thức để xóa user
    public void removeUser(int user_id) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection connection = TestDBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, user_id);
            statement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public User getUserByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection connection = TestDBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return new User(
                        resultSet.getInt("user_id"),
                        resultSet.getString("username"),
                        resultSet.getString("password"),
                        resultSet.getString("role_id"),
                        resultSet.getString("image_path")
                );
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public String getHashedPassword(int userId) {
        String hashedPassword = null;
        String sql = "SELECT password FROM users WHERE user_id = ?";

        try (Connection connection = TestDBConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                hashedPassword = rs.getString("password");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return hashedPassword;
    }

}
