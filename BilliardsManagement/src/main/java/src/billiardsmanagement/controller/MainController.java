package src.billiardsmanagement.controller;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import src.billiardsmanagement.model.TestDBConnection;
import src.billiardsmanagement.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;


public class MainController {

    @FXML
    private Label usernameLabel;
    @FXML
    private Label roleLabel;

    private User loggedInUser;

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;

        String roleId = user.getRole(); // Lấy role_id từ user
        if (roleId == null || roleId.trim().isEmpty()) {
            System.err.println("Error: roleId is null or empty.");
            return;
        }

        // Truy vấn role_name từ bảng roles dựa vào role_id
        String roleName = getRoleName(Integer.parseInt(user.getRole()));

        Platform.runLater(() -> {
            usernameLabel.setText("Welcome, " + user.getUsername());
            roleLabel.setText(roleName);
        });
    }


    private String getRoleName(int roleId) {
        String roleName = "Unknown"; // Giá trị mặc định nếu không tìm thấy

        String query = "SELECT role_name FROM roles WHERE role_id = ?";
        try (Connection connection = TestDBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, roleId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                roleName = resultSet.getString("role_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return roleName;
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to logout?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            // Đóng cửa sổ hiện tại
            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            stage.close();

            // Mở lại cửa sổ login
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/users/login.fxml"));
                Scene scene = new Scene(loader.load());

                Stage loginStage = new Stage();
                loginStage.setScene(scene);
                loginStage.setTitle("Login");
                loginStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private StackPane contentArea;

    @FXML
    private void showOrdersPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/orders/order.fxml"));
        AnchorPane orderPage = loader.load();  // Tải FXML thành AnchorPane
        contentArea.getChildren().setAll(orderPage);
    }

    @FXML
    private void showProductsPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/products/products.fxml"));
        AnchorPane productPage = loader.load();
        contentArea.getChildren().setAll(productPage);
    }

    @FXML
    private void showCategoryPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/category/category.fxml"));
        AnchorPane categoryPage = loader.load();
        contentArea.getChildren().setAll(categoryPage);
    }

    @FXML
    private void showUsersPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/users/users.fxml"));
        AnchorPane categoryPage = loader.load();
        contentArea.getChildren().setAll(categoryPage);
    }
}
