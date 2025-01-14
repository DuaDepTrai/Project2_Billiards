package src.billiardsmanagement.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.stage.Stage;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
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
import javafx.util.Callback;
import src.billiardsmanagement.dao.OrderDAO;
import src.billiardsmanagement.model.Order;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class OrderController implements Initializable {

    @FXML
    private Label exit;
    @FXML
    private BorderPane mainLayout;
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
//        // Lấy đối tượng được chọn trong TableView
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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        totalCostColumn.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
       orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
       customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
       orderStatusColumn.setCellValueFactory(new PropertyValueFactory<>("orderStatus"));
        // Load data
        // Định dạng cột totalCost
        totalCostColumn.setCellFactory(new Callback<TableColumn<Order, Double>, TableCell<Order, Double>>() {
            @Override
            public TableCell<Order, Double> call(TableColumn<Order, Double> param) {
                return new TableCell<Order, Double>() {
                    @Override
                    protected void updateItem(Double item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                        } else {
                            // Định dạng số theo kiểu 1.000.000
                            DecimalFormat df = new DecimalFormat("#,###");
                            setText(df.format(item));
                        }
                    }
                };
            }
        });
        loadOrders();
    }

    public void getClass(BorderPane mainLayout) {
        this.mainLayout = mainLayout;
    }

    public void showItem(MouseEvent mouseEvent) throws IOException {
        if(mouseEvent.getClickCount() == 2){
            Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();

            if(selectedOrder != null){
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/forEachOrder.fxml"));
                Parent root = loader.load();

                ForEachOrderController detailController = loader.getController();
                detailController.setData(selectedOrder);

                Stage stage = new Stage();
                stage.setTitle("Order Details");
                stage.setScene(new Scene(root));
                stage.show();
            }
        }
    }
}
