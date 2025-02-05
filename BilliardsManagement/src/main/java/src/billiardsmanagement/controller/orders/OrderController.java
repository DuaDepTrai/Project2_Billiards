package src.billiardsmanagement.controller.orders;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;

import javafx.collections.FXCollections;
import javafx.scene.control.cell.PropertyValueFactory;
import src.billiardsmanagement.controller.orders.bookings.BookingController;
import src.billiardsmanagement.dao.OrderDAO;
import src.billiardsmanagement.model.Bill;
import src.billiardsmanagement.model.DatabaseConnection;
import src.billiardsmanagement.model.Order;

public class OrderController implements Initializable {

    @FXML
    private TableColumn<Order, Integer> sttColumn;
    @FXML
    private TableColumn<Order, String> customerNameColumn;
    @FXML
    private TableColumn<Order, Double> totalCostColumn;
    @FXML
    private TableColumn<Order, String> orderStatusColumn;
    @FXML
    private TableColumn<Order, String> phoneCustomerColumn;
    @FXML
    private TextField autoCompleteTextField;
    @FXML
    private TableView<Order> orderTable;
    @FXML
    private Label statusLabel;
    @FXML
    private ComboBox<String> statusComboBox;

    private Popup popup;
    private ListView<String> listView;

    private final Connection conn = DatabaseConnection.getConnection();
    private final OrderDAO orderDAO = new OrderDAO();
    private final Map<String, Integer> customerNameToIdMap = new HashMap<>();

    public TableView<Order> getOrderTable() {
        return orderTable;
    }
    @FXML
    public void addOrder(ActionEvent actionEvent) {
        try {
            String customerName = autoCompleteTextField.getText();
            if (customerName == null || customerName.trim().isEmpty()) {
                throw new IllegalArgumentException("Please select a valid Customer.");
            }

            Integer customerId = customerNameToIdMap.get(customerName);
            if (customerId == null) {
                throw new IllegalArgumentException("Customer not found: " + customerName);
            }

            Order newOrder = new Order(customerId);
            orderDAO.addOrder(newOrder);
            loadOrderList();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Order added successfully!");

        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while saving the order. Please try again.");
        }
    }

    private void loadCustomerNameToIdMap() {
        String query = "SELECT customer_id, name FROM customers";
        try (PreparedStatement statement = conn.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            customerNameToIdMap.clear();
            while (resultSet.next()) {
                int id = resultSet.getInt("customer_id");
                String name = resultSet.getString("name");
                customerNameToIdMap.put(name, id);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load customer data.");
        }
    }

    private void loadOrderList() {
        List<Order> orders = orderDAO.getAllOrders();
        orderTable.setItems(FXCollections.observableArrayList(orders));
    }

    private List<String> fetchCustomersByPhone(String phonePrefix) {
        List<String> customers = new ArrayList<>();
        String query = "SELECT phone, name FROM customers WHERE phone LIKE ?";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, phonePrefix + "%");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                customers.add(name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadCustomerNameToIdMap();
        loadOrderList();
        statusLabel.setVisible(false);
        statusComboBox.setVisible(false);
        totalCostColumn.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        sttColumn.setCellValueFactory(param -> {
            int index = sttColumn.getTableView().getItems().indexOf(param.getValue());
            return new SimpleIntegerProperty(index + 1).asObject();
        });
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        orderStatusColumn.setCellValueFactory(new PropertyValueFactory<>("orderStatus"));
        phoneCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("customerPhone"));

        totalCostColumn.setCellFactory(param -> new TableCell<Order, Double>() {
            private final DecimalFormat df = new DecimalFormat("#,###");

            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : df.format(item));
            }
        });

        popup = new Popup();
        listView = new ListView<>();
        popup.getContent().add(listView);

        autoCompleteTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                List<String> filteredCustomers = fetchCustomersByPhone(newValue);
                listView.getItems().setAll(filteredCustomers);

                if (!filteredCustomers.isEmpty() && !popup.isShowing()) {
                    popup.show(autoCompleteTextField,
                            autoCompleteTextField.localToScreen(autoCompleteTextField.getBoundsInLocal()).getMinX(),
                            autoCompleteTextField.localToScreen(autoCompleteTextField.getBoundsInLocal()).getMaxY());
                }
            } else {
                popup.hide();
            }
        });

        listView.setOnMouseClicked(event -> {
            if (!listView.getSelectionModel().isEmpty()) {
                autoCompleteTextField.setText(listView.getSelectionModel().getSelectedItem());
                popup.hide();
            }
        });

        autoCompleteTextField.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER:
                    if (!listView.getSelectionModel().isEmpty()) {
                        autoCompleteTextField.setText(listView.getSelectionModel().getSelectedItem());
                        popup.hide();
                    }
                    break;
                case ESCAPE:
                    popup.hide();
                    break;
                default:
                    break;
            }
        });
        orderTable.setOnMouseClicked(event -> {
            try {
                showItem(event);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void updateOrder(ActionEvent event) {
        Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();

        if(selectedOrder.getOrderStatus().equals("Paid")){
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot update an order that has already been paid.");
            return;
        }
        if (selectedOrder != null) {
            String orderStatus = statusComboBox.getValue();
            selectedOrder.setOrderStatus(orderStatus);
            boolean success = orderDAO.updateOrder(selectedOrder);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Order updated successfully!");
                loadOrderList();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update the order.");
            }
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Error", "Please select an order to update.");
        }
    }

    public void deleteOrder(ActionEvent event) {
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

    public void addCustomer(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/orders/addCustomer.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle("Add Customer");
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void showItem(MouseEvent mouseEvent) throws IOException {
        if (mouseEvent.getClickCount() == 2) {
            Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();


            if (selectedOrder != null ) {
                // Lấy orderId từ selectedOrder
                int orderId = selectedOrder.getOrderId();
                int customerId = selectedOrder.getCustomerId();
                // Tạo loader và tải forEachOrder.fxml
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/orders/forEachOrder.fxml"));
                Parent root = loader.load();

                // Lấy controller của ForEachOrderController
                ForEachOrderController controller = loader.getController();
                controller.setOrderID(orderId); // Truyền orderId
                controller.setCustomerID(customerId);
                controller.setOrderTable(orderTable);


                // Kiểm tra orderID (debug)
                System.out.println("Order ID in BookingController: " + orderId);

                // Hiển thị cửa sổ mới
                Stage stage = new Stage();
                stage.setTitle("Order Details");
                stage.setScene(new Scene(root));
                stage.show();
            }
        }
        if(mouseEvent.getClickCount() == 1){
            statusLabel.setVisible(true);
            statusComboBox.setVisible(true);
            Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
            if(selectedOrder != null){
                autoCompleteTextField.setText(selectedOrder.getCustomerName());
                statusComboBox.setValue(selectedOrder.getOrderStatus());
            }
        }
    }

    public void paymentOrder(ActionEvent event) throws IOException {
        Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();


        if(selectedOrder != null){
            int orderId = selectedOrder.getOrderId();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/orders/bill.fxml"));
            Parent root = loader.load();
            PaymentController paymentController = loader.getController();
            Bill bill = createBill(); // Phương thức tạo hóa đơn từ dữ liệu hiện có
            paymentController.setBill(bill);
            paymentController.setOrderID(orderId);

//            FXMLLoader loaderBooking = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/orders/bill_booking.fxml"));
//            Parent rootBooking = loaderBooking.load();
//            BookingController bookingController = loaderBooking.getController();
//            bookingController.setOrderId(orderId);
//
            Stage stage = new Stage();
            stage.setTitle("Payment Details");
            stage.setScene(new Scene(root));
            stage.show();

        }else{
            showAlert(Alert.AlertType.INFORMATION, "Error", "Please select an order to payment.");
        }
    }

    private Bill createBill() {
        // Lấy thông tin đơn hàng hiện tại
        Order currentOrder = orderTable.getSelectionModel().getSelectedItem();
        if (currentOrder == null) return null;

        return new Bill(
                currentOrder.getOrderId(),
                currentOrder.getCustomerId(),  // Lấy customerId từ Order
                currentOrder.getCustomerName(), // Tránh gọi getCustomer()
                currentOrder.getCustomerPhone(), // Tránh lỗi null
                currentOrder.getTotalCost(),
                currentOrder.getOrderStatus(),
                new ArrayList<>(), // Giả sử chưa có danh sách OrderItems, Booking, RentCues
                new ArrayList<>(),
                new ArrayList<>(),
                LocalDateTime.now()
        );
    }
}
