package src.billiardsmanagement.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.collections.FXCollections;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import src.billiardsmanagement.controller.orders.UpdateOrderController;
import src.billiardsmanagement.dao.OrderDAO;
import src.billiardsmanagement.model.Order;

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
        Parent fxml = FXMLLoader.load(getClass().getResource("/src/billiardsmanagement/orders/order.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/orders/add_order.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/orders/update_order.fxml"));
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

    public void deleteOrder(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Are you sure you want to delete this order?");
        alert.setContentText("This action cannot be undone.");

        // Hiển thị cảnh báo và chờ người dùng trả lời
        Optional<ButtonType> result = alert.showAndWait();

        // Nếu người dùng chọn OK, thực hiện xóa bản ghi
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Lấy ID đơn hàng hoặc thông tin cần thiết từ dòng được chọn trong TableView
            Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
            if (selectedOrder != null) {
                boolean success = orderDAO.deleteOrder(selectedOrder.getOrderId());

                if (success) {
                    // Xóa thành công, làm mới bảng
                    loadOrderList();  // Tải lại danh sách đơn hàng
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Order deleted successfully!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete the order.");
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "Warning", "No order selected.");
            }
        }
    }
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void loadOrderList() {
        List<Order> orders = orderDAO.getAllOrders(); // Lấy tất cả các đơn hàng từ DB
        orderTable.setItems(FXCollections.observableArrayList(orders)); // Cập nhật lại TableView
    }

    public void loadOrders() {
        List<Order> orders = orderDAO.getAllOrders();  // Lấy danh sách orders từ OrderDAO

        // Xóa tất cả các dòng hiện tại trong TableView
        orderTable.getItems().clear();

        // Thêm tất cả các đơn hàng vào TableView
        orderTable.getItems().addAll(orders);
    }
}