package src.billiardsmanagement.controller;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import src.billiardsmanagement.controller.products.ProductController;
import src.billiardsmanagement.controller.users.RolesPermissionsController;
import src.billiardsmanagement.controller.users.UserController;
import src.billiardsmanagement.model.TestDBConnection;
import src.billiardsmanagement.model.User;

import java.net.URL;
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
    @FXML
    private ImageView avatarImageView;

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
            usernameLabel.setText("Welcome, " + user.getFullname());
            roleLabel.setText(roleName);

            // Đường dẫn chỉ cần từ thư mục resources trở đi
            String avatarPath = "/src/billiardsmanagement/images/avatars/" + user.getImagePath();

            URL imageUrl = getClass().getResource(avatarPath);

            if (imageUrl != null) {
                avatarImageView.setImage(new Image(imageUrl.toExternalForm()));
            }
            else
            {
                System.out.println("No avatar found, using default.");
                URL defaultImageUrl = getClass().getResource("/src/billiardsmanagement/images/avatars/user.png");
                if (defaultImageUrl != null) {
                    avatarImageView.setImage(new Image(defaultImageUrl.toExternalForm()));
                } else {
                    System.err.println("Default avatar not found!");
                }
            }
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
        BorderPane orderPage = loader.load();  // Tải FXML thành AnchorPane
        contentArea.getChildren().setAll(orderPage);
    }

    @FXML
    private void showProductsPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/products/products.fxml"));
        AnchorPane productPage = loader.load();

        ProductController productController = loader.getController();
        productController.setCurrentUser(loggedInUser);
        System.out.println("🔹 Truyền user vào ProductController: " + (loggedInUser != null ? loggedInUser.getUsername() : "null"));

        if (productController == null) {
            System.out.println("Lỗi: Không lấy được ProductController!");
        } else {
            System.out.println("Debug: ProductController đã load, truyền user...");
            productController.setLoggedInUser(loggedInUser); // ✅ Truyền user đúng cách
        }

        contentArea.getChildren().setAll(productPage);
    }


    @FXML
    private void showCategoryPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/category/category.fxml"));
        AnchorPane categoryPage = loader.load();
        contentArea.getChildren().setAll(categoryPage);
    }

    @FXML
    public void showUsersPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/users/users.fxml"));
        AnchorPane usersPage = loader.load();

        // Hiển thị giao diện Users trong contentArea
        contentArea.getChildren().setAll(usersPage);

        // Lấy controller của Users và truyền MainController vào
        UserController userController = loader.getController();
        userController.setMainController(this);
    }


    @FXML
    public void showRolesPermissionsPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/users/rolesPermissions.fxml"));
        AnchorPane rolesPermissionsPage = loader.load();

        // Lấy controller của RolesPermissions và truyền MainController vào
        RolesPermissionsController rolesPermissionsController = loader.getController();
        rolesPermissionsController.setMainController(this);

        // Hiển thị trang trong contentArea
        contentArea.getChildren().setAll(rolesPermissionsPage);
    }


    public void showHomePage(ActionEvent actionEvent) {
    }

    public void showPoolTablePage(ActionEvent actionEvent) {
    }

    public void showStaffPage(ActionEvent actionEvent) {
    }

    public void showCustomerPage(ActionEvent actionEvent) {
    }

    public void showStatisticPage(ActionEvent actionEvent) {
    }
}
