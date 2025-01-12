package src.billiardsmanagement.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import src.billiardsmanagement.dao.CustomerDAO;
import src.billiardsmanagement.dao.OrderDAO;
import src.billiardsmanagement.model.Order;

public class UpdateOrderController {
    @FXML private TextField orderIdField;
    @FXML private TextField totalCostField;
    @FXML private ComboBox<String> orderStatusComboBox;


    private OrderDAO orderDAO = new OrderDAO();
    private CustomerDAO customerDAO = new CustomerDAO();
    private Order currentOrder;
    private int orderId;
    // Khởi tạo combo box trạng thái đơn hàng

    private void initializeOrderStatusComboBox() {
        ObservableList<String> statuses = FXCollections.observableArrayList("đã_book", "đang chơi", "kết thúc");
        orderStatusComboBox.setItems(statuses);
    }

    // Khởi tạo combo box khách hàng

    // Khởi tạo dữ liệu form
    public void initialize(Order order) {
        this.currentOrder = order;
        initializeOrderStatusComboBox();

        loadOrderDetails(order);
    }

    // Hiển thị thông tin đơn hàng
    private void loadOrderDetails(Order order) {
        totalCostField.setText(String.valueOf(order.getTotalCost()));
        orderStatusComboBox.setValue(order.getOrderStatus());

        // Chọn đúng khách hàng trong ComboBox

    }

    // Xử lý cập nhật đơn hàng
    @FXML
    public void updateOrder(ActionEvent actionEvent) {
        try {
            int orderId = Integer.parseInt(orderIdField.getText());
            double totalCost = Double.parseDouble(totalCostField.getText());
            String orderStatus = orderStatusComboBox.getValue();



            // Cập nhật thông tin đơn hàng
            Order updatedOrder = new Order(  totalCost, orderStatus);
            boolean success = orderDAO.updateOrder(updatedOrder);

            if (success) {
                showAlert(AlertType.INFORMATION, "Success", "Order updated successfully!");
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
        this.orderId = orderId;
        currentOrder = orderDAO.getOrderById(orderId);
        if (currentOrder != null) {
            initialize(currentOrder);
        } else {
            showAlert(AlertType.ERROR, "Error", "Order not found.");
        }
    }
}
