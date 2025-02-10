package src.billiardsmanagement.controller.users;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import src.billiardsmanagement.dao.UserDAO;
import src.billiardsmanagement.model.TestDBConnection;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;

public class AddUserController {
    @FXML
    private TextField txtUsername;
    @FXML
    private TextField txtPassword;
    @FXML
    private TextField txtRePassword;
    @FXML
    private ComboBox<String> comboRole;

    private ObservableList<String> roleList = FXCollections.observableArrayList();
    private UserDAO userDAO = new UserDAO();

    public void initialize() {
        loadRoles();
    }

    private void loadRoles() {
        try (Connection connection = TestDBConnection.getConnection()) {
            String sql = "SELECT role_name FROM roles";
            ResultSet resultSet = connection.createStatement().executeQuery(sql);

            while (resultSet.next()) {
                roleList.add(resultSet.getString("role_name"));
            }
            comboRole.setItems(roleList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAdd() {
        String username = txtUsername.getText();
        String password = txtPassword.getText();
        String rePassword = txtRePassword.getText();
        String role = comboRole.getValue();

        if (!isValidUsername(username)) {
            showAlert("Invalid Username", "Username must be at least 6 characters long and contain only letters, numbers, '.', or '_'.");
            return;
        }

        if (!isValidPassword(password)) {
            showAlert("Invalid Password", "Password must be at least 6 characters long and contain letters, numbers, or special characters.");
            return;
        }

        if (!password.equals(rePassword)) {
            showAlert("Error Password", "Re-entered password does not match the original password.");
            return;
        }

        if (role == null) {
            showAlert("Empty Role", "Please select a role.");
            return;
        }

        String hashedPassword = hashPassword(password);

        if (hashedPassword == null || hashedPassword.isEmpty()) {
            showAlert("Error", "Failed to hash password.");
            return;
        }

        try (Connection connection = TestDBConnection.getConnection()) {
            // Get role_id from role name
            String getRoleSql = "SELECT role_id FROM roles WHERE role_name = ?";
            PreparedStatement roleStmt = connection.prepareStatement(getRoleSql);
            roleStmt.setString(1, role);
            ResultSet resultSet = roleStmt.executeQuery();
            int role_id = resultSet.next() ? resultSet.getInt("role_id") : 0;

            // Thêm user mới
            userDAO.addUser(username, hashedPassword, role_id);

            System.out.println("User added successfully!");

            // Đóng cửa sổ Add User
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
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean isValidUsername(String username) {
        return username.matches("^[a-zA-Z0-9_.]{6,}$");
    }

    private boolean isValidPassword(String password) {
        return password.matches("^.{6,}$");
    }

}