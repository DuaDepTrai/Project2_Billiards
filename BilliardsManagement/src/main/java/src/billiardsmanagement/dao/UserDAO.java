package src.billiardsmanagement.dao;

import src.billiardsmanagement.model.DatabaseConnection;
import src.billiardsmanagement.model.Pair;
import src.billiardsmanagement.model.User;
import src.billiardsmanagement.model.TestDBConnection;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class UserDAO {
    // Phương thức để lấy tất cả users
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u.user_id, u.username, u.password, r.role_name, u.fullname, u.phone, u.birthday, u.address, u.hire_date, u.image_path " +
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
                String fullname = resultSet.getString("fullname");
                String phone = resultSet.getString("phone");
                Date birthday = resultSet.getDate("birthday");
                String address = resultSet.getString("address");
                Date hire_date = resultSet.getDate("hire_date");
                String image_path = resultSet.getString("image_path");

                users.add(new User(user_id, username, password, role, fullname, phone, birthday, address, hire_date, image_path));

                System.out.println("📌 Debug: User Data");
                System.out.println("Username: " + username);
                System.out.println("Full Name: " + fullname);
                System.out.println("Role: " + role);
                System.out.println("Phone: " + phone);
                System.out.println("Birthday: " + birthday);
                System.out.println("Address: " + address);
                System.out.println("Hire Date: " + hire_date);
                System.out.println("Image Path: " + image_path);

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return users;
    }

    // Phương thức để thêm user mới
    public void addUser(String username, String password, int role_id, String fullname, String phone, Date birthday, String address, Date hire_date, String imagePath) throws SQLException {
        String sql = "INSERT INTO users (username, password, role_id, fullname, phone, birthday, address, hire_date, image_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = TestDBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);
            statement.setInt(3, role_id);
            statement.setString(4, fullname);
            statement.setString(5, phone);
            statement.setDate(6, birthday);
            statement.setString(7, address);
            statement.setDate(8, hire_date);
            statement.setString(9, imagePath);
            statement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Phương thức để cập nhật thông tin user
    public void updateUser(int user_id, String username, String password, int role_id, String fullname, String phone, Date birthday, String address, String imagePath) throws SQLException {
        String sql = "UPDATE users SET username = ?, password = ?, role_id = ?, fullname = ?, phone = ?, birthday = ?, address = ?, image_path = ? WHERE user_id = ?";
        try (Connection connection = TestDBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);
            statement.setInt(3, role_id);
            statement.setString(4, fullname);
            statement.setString(5, phone);
            statement.setDate(6, birthday);
            statement.setString(7, address);
            statement.setString(8, imagePath);
            statement.setInt(9, user_id);
            statement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Phương thức Change User Information
    public void changeUserInfo(int user_id, String password, String fullname, String phone, Date birthday, String address, String imagePath) throws SQLException {
        String sql = "UPDATE users SET password = ?, fullname = ?, phone = ?, birthday = ?, address = ?, image_path = ? WHERE user_id = ?";
        try (Connection connection = TestDBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, password);
            statement.setString(2, fullname);
            statement.setString(3, phone);
            statement.setDate(4, birthday);
            statement.setString(5, address);
            statement.setString(6, imagePath);
            statement.setInt(7, user_id);
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
        String sql = "SELECT * FROM users WHERE BINARY username = ?";
        try (Connection connection = TestDBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String fullname = resultSet.getString("fullname");  // Kiểm tra fullname
                if (fullname == null) {
                    System.err.println("❌ Lỗi: fullname từ database bị NULL!");
                }

                User user = new User(
                        resultSet.getInt("user_id"),
                        resultSet.getString("username"),
                        resultSet.getString("password"),
                        resultSet.getString("role_id"),
                        fullname,  // Lấy fullname đúng cách
                        resultSet.getString("phone"),
                        resultSet.getDate("birthday"),
                        resultSet.getString("address"),
                        resultSet.getDate("hire_date"),
                        resultSet.getString("image_path")
                );

                return user;

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

    public User authenticateUser(String username, String password) {
        String sql = "SELECT u.*, r.role_name FROM users u " +
                "JOIN roles r ON u.role_id = r.role_id " +
                "WHERE BINARY u.username = ?";

        try (Connection connection = TestDBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String hashedPasswordFromDB = resultSet.getString("password");

                // Băm mật khẩu nhập vào và so sánh với mật khẩu trong DB
                if (hashPassword(password).equals(hashedPasswordFromDB)) {
                    return new User(
                            resultSet.getInt("user_id"),
                            resultSet.getString("username"),
                            hashedPasswordFromDB,
                            resultSet.getString("role_id"),
                            resultSet.getString("role_name"), // Add role_name
                            resultSet.getString("fullname"),
                            resultSet.getString("image_path")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Trả về null nếu xác thực thất bại
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getUserPermissions(int userId) {
        List<String> permissions = new ArrayList<>();
        String sql = "SELECT p.name FROM permissions p " +
                "JOIN role_permissions rp ON p.id = rp.permission_id " +
                "JOIN roles r ON rp.role_id = r.id " +
                "JOIN users u ON r.id = u.role_id " +
                "WHERE u.id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                permissions.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return permissions;
    }

    private Connection connection;

    public UserDAO() {
        try {
            this.connection = TestDBConnection.getConnection(); // Lấy connection từ class DatabaseConnection
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
