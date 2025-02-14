package src.billiardsmanagement.controller.users;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import src.billiardsmanagement.dao.UserDAO;
import src.billiardsmanagement.model.TestDBConnection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
    @FXML
    private Label lblImagePath;

    private ObservableList<String> roleList = FXCollections.observableArrayList();
    private String uploadedImagePath = null; // Đường dẫn file đã upload
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

        try (Connection connection = TestDBConnection.getConnection()) {
            // Kiểm tra username đã tồn tại hay chưa
            String checkUsernameSql = "SELECT COUNT(*) FROM users WHERE username = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkUsernameSql);
            checkStmt.setString(1, username);
            ResultSet checkResult = checkStmt.executeQuery();

            if (checkResult.next() && checkResult.getInt(1) > 0) {
                showAlert("Duplicate Username", "Username already exists. Please choose a different username.");
                return; // Dừng lại nếu username đã tồn tại
            }

            // Get role_id from role name
            String getRoleSql = "SELECT role_id FROM roles WHERE role_name = ?";
            PreparedStatement roleStmt = connection.prepareStatement(getRoleSql);
            roleStmt.setString(1, role);
            ResultSet resultSet = roleStmt.executeQuery();
            int role_id = resultSet.next() ? resultSet.getInt("role_id") : 0;

            String hashedPassword = hashPassword(password);

            if (hashedPassword == null || hashedPassword.isEmpty()) {
                showAlert("Error", "Failed to hash password.");
                return;
            }

            String finalImageName = "user.png"; // Sử dụng ảnh mặc định nếu không có ảnh tải lên

            if (uploadedImagePath != null && !uploadedImagePath.isEmpty()) {
                try {
                    File destinationDir = new File("BilliardsManagement/src/main/resources/src/billiardsmanagement/images/avatars");
                    if (!destinationDir.exists()) {
                        destinationDir.mkdirs();
                    }

                    Path sourcePath = Paths.get(uploadedImagePath); // File ảnh được chọn
                    finalImageName = sourcePath.getFileName().toString(); // Lấy tên file ảnh
                    Path destinationPath = Paths.get(destinationDir.getAbsolutePath(), finalImageName);

                    // Sao chép ảnh
                    Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Error copying file: " + e.getMessage());
                    return;
                }
            }

            // Thêm user mới
            userDAO.addUser(username, hashedPassword, role_id, finalImageName);

            System.out.println("User added successfully!");

            // Đóng cửa sổ Add User
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Image File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        File file = fileChooser.showOpenDialog(txtUsername.getScene().getWindow());
        if (file != null) {
            uploadedImagePath = file.getAbsolutePath(); // Lưu đường dẫn file nguồn
            lblImagePath.setText(file.getName()); // Hiển thị tên file trong giao diện
            System.out.println("Selected file: " + uploadedImagePath);
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