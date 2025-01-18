package src.billiardsmanagement.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import src.billiardsmanagement.dao.OrderDAO;
import src.billiardsmanagement.model.Order;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NavbarController implements Initializable {

    @FXML
    private Label exit;
//
//    @FXML
//    private TableColumn<Order, Integer> orderIdColumn;
//    @FXML
//    private TableColumn<Order, String> customerNameColumn;
//    @FXML
//    private TableColumn<Order, Double> totalCostColumn;
//    @FXML
//    private TableColumn<Order, String> orderStatusColumn;
//
//    @FXML
//    private TableView<Order> orderTable;


    @FXML
    private BorderPane mainLayout; // Tham chiếu tới BorderPane chính (được định nghĩa trong Main Layout)



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
//
    }
    private void loadPage(String page) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(page));
            mainLayout.setCenter(root); // Đặt nội dung FXML vào khu vực trung tâm
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showHome() {
        loadPage("/src/billiardsmanagement/home.fxml");
    }

    @FXML
    private void showDashboard() {
        loadPage("/src/billiardsmanagement/dashboard.fxml");
    }

    @FXML
    private void showOrder() {
        loadPage("/src/billiardsmanagement/orders/order.fxml");
    }

    @FXML
    private void showPoolTable() {
        loadPage("/src/billiardsmanagement/pooltable.fxml");
    }

    @FXML
    private void showItem() {
        loadPage("/src/billiardsmanagement/item.fxml");
    }

    public void getClass(BorderPane mainLayout) {
        this.mainLayout = mainLayout;
    }
}