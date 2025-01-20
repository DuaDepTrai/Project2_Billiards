package src.billiardsmanagement.controller.orders;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import src.billiardsmanagement.dao.CustomerDAO;
import src.billiardsmanagement.dao.OrderDAO;
import src.billiardsmanagement.model.Order;

import java.io.IOException;

public class UpdateOrderController {
    @FXML private TextField orderIdField;
    @FXML private ComboBox<String> orderStatusComboBox;

    @FXML
    private TableView<Order> orderTable;

    private OrderDAO orderDAO = new OrderDAO();
    private CustomerDAO customerDAO = new CustomerDAO();
    private Order currentOrder;

    // Khởi tạo combo box trạng thái đơn hàng
    private void initializeOrderStatusComboBox() {
        ObservableList<String> statuses = FXCollections.observableArrayList("Pending", "Paid", "Canceled");
        orderStatusComboBox.setItems(statuses);
    }

    // Khởi tạo dữ liệu form
    public void initialize(Order order) {
        this.currentOrder = order;
        initializeOrderStatusComboBox();
        loadOrderDetails(order);
    }

    // Hiển thị thông tin đơn hàng
    private void loadOrderDetails(Order order) {
        if (order != null) {
            System.out.println(order.getOrderId()); // In ra orderId
            orderIdField.setText(String.valueOf(order.getOrderId())); // order_id không thể chỉnh sửa
            orderStatusComboBox.setValue(order.getOrderStatus());

            // Chọn đúng khách hàng trong ComboBox (Giả sử bạn có thông tin khách hàng trong Order)
            // customerComboBox.setValue(order.getCustomer()); (Nếu có)
        } else {
            System.out.println("Order is null");
        }
    }


    // Xử lý cập nhật đơn hàng
    @FXML
    public void updateOrder(ActionEvent actionEvent) {
        try {
            // Lấy thông tin từ form
            int orderId = Integer.parseInt(orderIdField.getText());
            String orderStatus = orderStatusComboBox.getValue();

            // Cập nhật thông tin đơn hàng
            currentOrder.setOrderStatus(orderStatus);
            // Cập nhật vào database
           boolean success = orderDAO.updateOrder(currentOrder);

            if (success) {
                showAlert(AlertType.INFORMATION, "Success", "Order updated successfully!");
                loadNavbarScene();
            } else {
                showAlert(AlertType.ERROR, "Error", "Failed to update the order.");
            }
        } catch (NumberFormatException e) {
            showAlert(AlertType.ERROR, "Error", "Invalid input. Please check all fields.");
        }
    }

    // Hiển thị thông báo
    private void showAlert(AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Nhận dữ liệu từ màn hình trước
    public void setData(int orderId) {
        this.currentOrder = orderDAO.getOrderById(orderId);
        if (currentOrder != null) {
            initialize(currentOrder);
        } else {
            showAlert(AlertType.ERROR, "Error", "Order not found.");
        }
    }

    private void loadNavbarScene() {
        try {
            // Tải tệp FXML của navbar
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/main.fxml"));

            // Tạo một scene mới từ FXML
            Parent root = loader.load();
            Scene newScene = new Scene(root);

            // Lấy current stage và set scene mới
            Stage currentStage = (Stage) orderIdField.getScene().getWindow();
            currentStage.setScene(newScene);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Failed to load the navbar.");
        }
    }

}
