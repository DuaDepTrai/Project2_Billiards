package src.billiardsmanagement.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
    @FXML
    private StackPane contentArea;
    @FXML
    private TableColumn<Order, Integer> orderIdColumn;
    @FXML
    private TableColumn<Order, String> customerNameColumn;
    @FXML
    private TableColumn<Order, Double> totalCostColumn;
    @FXML
    private TableColumn<Order, String> orderStatusColumn;

    @FXML
    private TableView<Order> orderTable;



    OrderDAO orderDAO = new OrderDAO();
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        exit.setOnMouseClicked(e -> {
            System.exit(0);
        });
        try {
            Parent fxml = FXMLLoader.load(getClass().getResource("/src/billiardsmanagement/home.fxml"));
            contentArea.getChildren().removeAll();
            contentArea.getChildren().setAll(fxml);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        totalCostColumn.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        orderStatusColumn.setCellValueFactory(new PropertyValueFactory<>("orderStatus"));

        // Load data
        loadOrders();
    }

    public void home(ActionEvent actionEvent) throws IOException {
        Parent fxml = FXMLLoader.load(getClass().getResource("/src/billiardsmanagement/home.fxml"));
        contentArea.getChildren().removeAll();
        contentArea.getChildren().setAll(fxml);
    }

    public void dashboard(ActionEvent actionEvent) throws IOException {
        Parent fxml = FXMLLoader.load(getClass().getResource("/src/billiardsmanagement/dashboard.fxml"));
        contentArea.getChildren().removeAll();
        contentArea.getChildren().setAll(fxml);
    }

    public void order(ActionEvent actionEvent) throws IOException {
        Parent fxml = FXMLLoader.load(getClass().getResource("/src/billiardsmanagement/order.fxml"));
        contentArea.getChildren().removeAll();
        contentArea.getChildren().setAll(fxml);
    }

    public void pool_table(ActionEvent actionEvent) throws IOException {
        Parent fxml = FXMLLoader.load(getClass().getResource("/src/billiardsmanagement/pooltable.fxml"));
        contentArea.getChildren().removeAll();
        contentArea.getChildren().setAll(fxml);
    }

    public void item(ActionEvent actionEvent) throws IOException {
        Parent fxml = FXMLLoader.load(getClass().getResource("/src/billiardsmanagement/item.fxml"));
        contentArea.getChildren().removeAll();
        contentArea.getChildren().setAll(fxml);
    }
    @FXML
    public void addOrder(ActionEvent actionEvent) {
        try {
            // Load FXML cho form thêm Order
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/add_order.fxml"));
            Parent root = loader.load();

            // Tạo một Scene mới
            Stage stage = new Stage();
            stage.setTitle("Add Order");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Sau khi đóng form, refresh bảng Order
            loadOrders();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateOrder(ActionEvent actionEvent) throws IOException {
        // Lấy đối tượng được chọn trong TableView
        Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();

        if (selectedOrder != null) {
            int orderId = selectedOrder.getOrderId();

            // Load giao diện update_order.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/update_order.fxml"));
            Parent root = loader.load();

            // Lấy controller của update_order.fxml
            UpdateOrderController updateOrderController = loader.getController();

            // Truyền order_id sang controller của update_order.fxml
            updateOrderController.setData(orderId);
            // Hiển thị giao diện mới
            Stage stage = new Stage();
            stage.setTitle("Add Order");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } else {
            System.out.println("No order selected.");
        }
    }

    public void deleteButton(ActionEvent actionEvent) {
    }

    public void loadOrders() {
        List<Order> orders = orderDAO.getAllOrders();  // Lấy danh sách orders từ OrderDAO

        // Xóa tất cả các dòng hiện tại trong TableView
        orderTable.getItems().clear();

        // Thêm tất cả các đơn hàng vào TableView
        orderTable.getItems().addAll(orders);
    }
}