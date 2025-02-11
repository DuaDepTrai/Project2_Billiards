package src.billiardsmanagement.controller;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import src.billiardsmanagement.model.User;
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

    private User loggedInUser;

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        usernameLabel.setText("Welcome, " + user.getUsername());
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
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/login.fxml"));
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
    private void showOrderPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/orders/order.fxml"));
        AnchorPane orderPage = loader.load();  // Tải FXML thành AnchorPane
        contentArea.getChildren().setAll(orderPage);
    }

    @FXML
    private void showProductPage() throws IOException {
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
}
