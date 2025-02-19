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
import src.billiardsmanagement.dao.BookingDAO;
import src.billiardsmanagement.dao.CustomerDAO;
import src.billiardsmanagement.dao.OrderDAO;
import src.billiardsmanagement.model.*;

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
    private TableColumn<Order,String> nameTableColumn;

    @FXML
    private TextField autoCompleteTextField;
    @FXML
    private TableView<Order> orderTable;

    private Popup popup;
    private ListView<String> listView;

    private final Connection conn = DatabaseConnection.getConnection();
    private final OrderDAO orderDAO = new OrderDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final Map<String, Integer> customerNameToIdMap = new HashMap<>();

    public TableView<Order> getOrderTable() {
        return orderTable;
    }

    @FXML
    public void addOrder(ActionEvent actionEvent) {
        try {
            loadCustomerNameToIdMap();

            String customerInput = autoCompleteTextField.getText();
            if (customerInput == null || customerInput.trim().isEmpty()) {
                throw new IllegalArgumentException("Please select a valid Customer.");
            }

            Integer customerId = customerNameToIdMap.get(customerInput);
            if (customerId == null) {
                throw new IllegalArgumentException("Customer not found: " + customerInput);
            }

            Order newOrder = new Order(customerId);
            orderDAO.addOrder(newOrder);
            Order orderLastest = orderDAO.getLatestOrderByCustomerId(customerId);

            int orderId = orderLastest.getOrderId();
            System.out.println(orderId);
            loadOrderList();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/orders/forEachOrder.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Order Detail");
            stage.setScene(new Scene(root));
            stage.show();
            ForEachOrderController controller = loader.getController();
            controller.setOrderID(orderId);
            controller.setCustomerID(customerId);
            controller.setOrderTable(orderTable);
            controller.initializeAllTables();
        } catch (IllegalArgumentException e) {
            NotificationService.showNotification("Validation Error", e.getMessage(), NotificationStatus.Error);
        } catch (Exception e) {
            e.printStackTrace();
            NotificationService.showNotification("Error", "An error occurred while saving the order. Please try again.", NotificationStatus.Error);
        }
    }

    private void loadOrderList() {
        List<Order> orders = orderDAO.getAllOrders();
        orderTable.setItems(FXCollections.observableArrayList(orders));
    }

    public void loadCustomerNameToIdMap() {
        String query = "SELECT customer_id, name, phone FROM customers";
        try (PreparedStatement statement = DatabaseConnection.getConnection().prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {

            customerNameToIdMap.clear();
            while (resultSet.next()) {
                int id = resultSet.getInt("customer_id");
                String name = resultSet.getString("name");
                String phone = resultSet.getString("phone");
                String key = name + " - " + phone;
                customerNameToIdMap.put(key, id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadCustomerNameToIdMap();
        loadOrderList();

        totalCostColumn.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        sttColumn.setCellValueFactory(param -> {
            int index = sttColumn.getTableView().getItems().indexOf(param.getValue());
            return new SimpleIntegerProperty(index + 1).asObject();
        });
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        orderStatusColumn.setCellValueFactory(new PropertyValueFactory<>("orderStatus"));
        phoneCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("customerPhone"));
        nameTableColumn.setCellValueFactory(new PropertyValueFactory<>("currentTableName"));
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
                List<String> filteredCustomers = CustomerDAO.fetchCustomersByPhone(newValue);
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
                String selected = listView.getSelectionModel().getSelectedItem();
                autoCompleteTextField.setText(selected);
                popup.hide();
            }
        });

        autoCompleteTextField.setOnKeyPressed(event -> {
            switch (event.getCode()) {
            case ENTER:
                if (!listView.getSelectionModel().isEmpty()) {
                    String selected = listView.getSelectionModel().getSelectedItem();
                    autoCompleteTextField.setText(selected);
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
            if (selectedOrder != null) {
                int orderId = selectedOrder.getOrderId();
                int customerId = selectedOrder.getCustomerId();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/orders/forEachOrder.fxml"));
                Parent root = loader.load();
                ForEachOrderController controller = loader.getController();
                controller.setOrderID(orderId);
                controller.setCustomerID(customerId);
                controller.setOrderTable(orderTable);
                controller.initializeAllTables();

                Stage stage = new Stage();
                stage.setTitle("Order Details");
                stage.setScene(new Scene(root));
                stage.show();
            }
        }
        if(mouseEvent.getClickCount() == 1) {
            Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
            if(selectedOrder != null) {
                autoCompleteTextField.setText(selectedOrder.getCustomerName());
            }
        }
    }

    public void billOrder(ActionEvent event) throws IOException {
        Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();

        if (selectedOrder == null) {
            NotificationService.showNotification("Error", "Please select an order to payment.", NotificationStatus.Error);
            return;
        }

        if (!"Finished".equals(selectedOrder.getOrderStatus()) && !"Paid".equals(selectedOrder.getOrderStatus())) {
            NotificationService.showNotification("Access Denied", "Bills can only be accessed when the order is in Finished or Paid status.", NotificationStatus.Error);
            return;
        }

        int orderId = selectedOrder.getOrderId();
        FXMLLoader paymentLoader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/orders/finalBill.fxml"));
        Parent paymentRoot = paymentLoader.load();
        PaymentController paymentController = paymentLoader.getController();
        paymentController.setOrderID(orderId);
        paymentController.setBill(createBill());

        Stage stage = new Stage();
        stage.setTitle("Payment Details");
        stage.setScene(new Scene(paymentRoot));
        stage.setOnHidden(e -> loadOrderList());
        stage.show();
    }

    private Bill createBill() {
        Order currentOrder = orderTable.getSelectionModel().getSelectedItem();
        if (currentOrder == null) return null;

        Bill bill = new Bill();
        bill.setCustomerName(currentOrder.getCustomerName() == null ? "Guest" : currentOrder.getCustomerName());
        bill.setCustomerPhone(currentOrder.getCustomerPhone() == null ? "GuestPhone" : currentOrder.getCustomerPhone());
        bill.setTotalCost(currentOrder.getTotalCost());
        return bill;
    }

    public void checkOrderStatus(){
        List <Booking> bookings = bookingDAO.
    }
}
