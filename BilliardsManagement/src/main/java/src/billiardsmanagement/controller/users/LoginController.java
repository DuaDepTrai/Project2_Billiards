package src.billiardsmanagement.controller.users;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import src.billiardsmanagement.controller.MainController;
import src.billiardsmanagement.dao.UserDAO;
import src.billiardsmanagement.model.User;
import src.billiardsmanagement.view.Main;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
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

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Invalid Username or Password.");
            return;
        }

        try {
            User user = userDAO.getUserByUsername(username);
            if (user == null) {
                errorLabel.setText("Invalid username or password.");
                return;
            }

        String hashedInputPassword = hashPassword(password);

        if (!user.getPassword().equals(hashedInputPassword)) {
            errorLabel.setText("Invalid username or password.");
            return;
        }

            // Đăng nhập thành công, mở main window
            openMainWindow(user);
        } catch (SQLException e) {
            errorLabel.setText("Database error.");
        }

    }

    private void openMainWindow(User user) {
        try {
            Main mainApp = new Main();
            Stage mainStage = new Stage();
            mainApp.start(mainStage); // Khởi động lại Main
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
}
