package src.billiardsmanagement.controller.orders;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import src.billiardsmanagement.controller.orders.bookings.AddBookingController;
import src.billiardsmanagement.controller.orders.bookings.UpdateBookingController;
import src.billiardsmanagement.dao.BookingDAO;
import src.billiardsmanagement.dao.OrderItemDAO;
import src.billiardsmanagement.model.Booking;
import src.billiardsmanagement.model.OrderItem;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ForEachOrderController implements Initializable {

    @FXML
    private TableView<Booking> bookingPoolTable;

    @FXML
    private TableView<OrderItem> orderItemsTable;

    @FXML
    private TableColumn<Booking, String> tableNameColumn;

    @FXML
    private TableColumn<Booking, String> startTimeColumn;

    @FXML
    private TableColumn<Booking, String> endTimeColumn;

    @FXML
    private TableColumn<Booking, Double> timeplayColumn;

    @FXML
    private TableColumn<Booking, Double> priceColumn;

    @FXML
    private TableColumn<Booking, Double> costColumn;

    @FXML
    private TableColumn<Booking, String> statusColumn;

    @FXML private TableColumn<OrderItem, String> productNameColumn;
    @FXML private TableColumn<OrderItem, Integer> quantityColumn;
    @FXML private TableColumn<OrderItem, Double>  priceOrderItemColumn;
    @FXML private TableColumn<OrderItem, Double>  costOrderItemColumn;


    private final ObservableList<Booking> bookingList = FXCollections.observableArrayList();
    private final ObservableList<OrderItem> orderItemList = FXCollections.observableArrayList();

    private final BookingDAO bookingDAO = new BookingDAO();
    private final OrderItemDAO orderItemDAO = new OrderItemDAO();

    private int orderID;

    public void setOrderID(int orderID) {
        this.orderID = orderID;
        if (orderID > 0) {
            loadBookings();
            loadOrderDetail();
        } else {
            showAlert(Alert.AlertType.ERROR, "Invalid Order ID", "The provided Order ID is invalid.");
        }
    }

    private void loadBookings() {
        List<Booking> bookings = bookingDAO.getBookingByOrderId(orderID);
        bookingList.clear();
        bookingList.addAll(bookings);
        bookingPoolTable.setItems(bookingList);
    }

    private void loadOrderDetail() {
        List<OrderItem> orderItems = orderItemDAO.getOrderItemsByOrderId(orderID);

        // Kiểm tra dữ liệu
        System.out.println(orderItems);

        // Xóa hết các dữ liệu cũ trong orderItemList
        orderItemList.clear();

        // Thêm các OrderItem mới vào list
        orderItemList.addAll(orderItems);

        // Cập nhật lại dữ liệu cho bảng
        orderItemsTable.setItems(orderItemList);
        System.out.println("Order Item" + orderItemsTable.getItems());
        // Implement this method to load and display order details
    }

    private void initializeBookingColumn() {
        tableNameColumn.setCellValueFactory(new PropertyValueFactory<>("tableName"));

        startTimeColumn.setCellValueFactory(cellData -> {
            LocalDateTime startTime = cellData.getValue().getStartTime().toLocalDateTime();
            return new SimpleStringProperty(startTime != null ?
                    startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
        });

        endTimeColumn.setCellValueFactory(cellData -> {
            LocalDateTime endTime = cellData.getValue().getEndTime();
            return new SimpleStringProperty(endTime != null ?
                    endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
        });

        timeplayColumn.setCellValueFactory(new PropertyValueFactory<>("timeplay"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("subTotal"));
        costColumn.setCellValueFactory(new PropertyValueFactory<>("netTotal"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("bookingStatus"));
    }

    private void initializeOrderDetailColumn() {
        // Implement this method to initialize additional order detail columns, if needed
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceOrderItemColumn.setCellValueFactory(new PropertyValueFactory<>("subTotal"));
        costOrderItemColumn.setCellValueFactory(new PropertyValueFactory<>("netTotal"));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeBookingColumn();
        initializeOrderDetailColumn();
    }

    public void addBooking(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/orders/bookings/addBooking.fxml"));
            Parent root = loader.load();

            AddBookingController addBookingController = loader.getController();
            addBookingController.setOrderId(orderID);

            Stage stage = new Stage();
            stage.setTitle("Add Booking");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadBookings();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load Add Booking form.");
        }
    }

    public void updateBooking(ActionEvent event) {
        Booking selectedBooking = bookingPoolTable.getSelectionModel().getSelectedItem();

        if (selectedBooking == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a booking to update.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/orders/bookings/updateBooking.fxml"));
            Parent root = loader.load();

            UpdateBookingController updateController = loader.getController();
            updateController.setBookingDetails(
                    selectedBooking.getBookingId(),
                    selectedBooking.getOrderId(),
                    selectedBooking.getTableId(),
                    selectedBooking.getTableStatus(),
                    selectedBooking.getBookingStatus()
            );

            Stage stage = new Stage();
            stage.setTitle("Update Booking");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadBookings();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load Update Booking form.");
        }
    }

    public void deleteBooking(ActionEvent event) {
        Booking selectedBooking = bookingPoolTable.getSelectionModel().getSelectedItem();

        if (selectedBooking == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a booking to delete.");
            return;
        }

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Delete Confirmation");
        confirmationAlert.setHeaderText("Are you sure you want to delete this booking?");
        confirmationAlert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = confirmationAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = bookingDAO.deleteBooking(selectedBooking.getBookingId());

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Booking deleted successfully.");
                loadBookings();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete the booking.");
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

    public void addOrderItem(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/orders/items/addBooking.fxml"));
            Parent root = loader.load();

            AddBookingController addBookingController = loader.getController();
            addBookingController.setOrderId(orderID);

            Stage stage = new Stage();
            stage.setTitle("Add Booking");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadBookings();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load Add Booking form.");
        }
    }
}
