package src.billiardsmanagement.controller.users;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import src.billiardsmanagement.controller.MainController;
import src.billiardsmanagement.dao.PermissionDAO;
import src.billiardsmanagement.dao.UserDAO;
import src.billiardsmanagement.model.Permission;
import src.billiardsmanagement.model.User;
import src.billiardsmanagement.model.UserSession;
import src.billiardsmanagement.view.Main;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;
    @FXML
    private Button loginButton; // Nút đăng nhập

    @FXML
    public void initialize() {
        // Khi nhấn Enter trên usernameField hoặc passwordField, sẽ gọi login
        usernameField.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                handleLogin();
            }
        });

        passwordField.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                handleLogin();
            }
        });

        // Khi click vào nút đăng nhập
        loginButton.setOnAction(event -> handleLogin());
    }


    private UserDAO userDAO = new UserDAO();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            UserDAO userDAO = new UserDAO();
            PermissionDAO permissionDAO = new PermissionDAO();

            User user = userDAO.authenticateUser(username, password);
            if (user != null) {
                List<String> userPermissions = permissionDAO.getUserPermissions(user.getId());
                user.setPermissions(userPermissions);
                UserSession.getInstance().setUser(
                        user.getId(),        // user_id từ database
                        user.getUsername(),  // username
                        user.getRoleName()   // role
                );
                openMainWindow(user);
            } else {
                showAlert("Login Failed", "Invalid username or password!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while logging in.");
        }
    }

    private void openMainWindow(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/main.fxml"));
            BorderPane root = loader.load();

            Scene scene = new Scene(root, 800, 600);
            scene.getStylesheets().add(getClass().getResource("/src/billiardsmanagement/css/style.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/src/billiardsmanagement/css/main.css").toExternalForm());

            // Lấy MainController từ FXMLLoader
            MainController mainController = loader.getController();
            mainController.setLoggedInUser(user); // Cập nhật username

            Stage mainStage = new Stage();
            mainStage.setScene(scene);
            mainStage.setTitle("BILLIARDS MANAGEMENT");
            mainStage.show();

            // Đóng cửa sổ login
            Stage loginStage = (Stage) usernameField.getScene().getWindow();
            loginStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
}
