package src.billiardsmanagement.controller.users;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import src.billiardsmanagement.dao.UserDAO;
import src.billiardsmanagement.model.User;
import src.billiardsmanagement.model.TestDBConnection;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.Optional;

public class UpdateUserController {
    @FXML private TextField txtUsername;
    @FXML private TextField txtPassword;
    @FXML private TextField txtRePassword;
    @FXML private ComboBox<String> comboRole;

    private int userId;  // Biến để lưu trữ user_id
    private UserDAO userDAO = new UserDAO();

    public void initialize() {
        loadRoles();
    }

    private void loadRoles() {
        try (Connection connection = TestDBConnection.getConnection()) {
            String sql = "SELECT role_name FROM roles";
            ResultSet resultSet = connection.createStatement().executeQuery(sql);
            while (resultSet.next()) {
                comboRole.getItems().add(resultSet.getString("role_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUserData(User user) {
        this.userId = user.getId();
        txtUsername.setText(user.getUsername());
        txtPassword.setText(user.getPlainPassword()); // Lưu ý: cần truyền password chưa hash từ DB
        txtRePassword.setText(user.getPlainPassword());
        comboRole.setValue(user.getRole());
    }

    @FXML
    private void handleUpdate() {
        String username = txtUsername.getText();
        String password = txtPassword.getText() != null ? txtPassword.getText().trim() : "";
        String rePassword = txtRePassword.getText() != null ? txtRePassword.getText().trim() : "";
        String role = comboRole.getValue();

        if (!isValidUsername(username)) {
            showAlert("Invalid Username", "Username must be at least 6 characters long and contain only letters, numbers, '.', or '_'.");
            return;
        }

        if (password != null && !password.isEmpty() && !isValidPassword(password)) {
            showAlert("Invalid Password", "Password must be at least 6 characters long and contain letters, numbers, or special characters.");
            return;
        }

        if (!password.equals(rePassword)) {
            showAlert("Error", "Passwords do not match");
            return;
        }

        String hashedPassword;

        // Nếu password trống, giữ nguyên mật khẩu hash cũ từ DB
        if (password == null || password.isEmpty()) {
            hashedPassword = userDAO.getHashedPassword(userId);
        } else {
            hashedPassword = hashPassword(password);
        }

        try (Connection connection = TestDBConnection.getConnection()) {
            String getRoleSql = "SELECT role_id FROM roles WHERE role_name = ?";
            PreparedStatement roleStmt = connection.prepareStatement(getRoleSql);
            roleStmt.setString(1, role);
            ResultSet resultSet = roleStmt.executeQuery();
            int role_id = resultSet.next() ? resultSet.getInt("role_id") : 0;

            userDAO.updateUser(userId, username, hashedPassword, role_id);

            System.out.println("User updated successfully!");
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @FXML
    private void handleCancel() {
        Stage stage = (Stage) txtUsername.getScene().getWindow();
        stage.close();
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean isValidUsername(String username) {
        return username.matches("^[a-zA-Z0-9_.]{6,}$");
    }

    private boolean isValidPassword(String password) {
        return password != null && password.matches("^.{6,}$");
    }
}