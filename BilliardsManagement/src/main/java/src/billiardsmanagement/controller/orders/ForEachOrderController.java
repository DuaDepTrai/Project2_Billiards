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
import javafx.scene.text.Text;
import javafx.stage.Stage;
import src.billiardsmanagement.controller.orders.bookings.AddBookingController;
import src.billiardsmanagement.controller.orders.bookings.UpdateBookingController;
import src.billiardsmanagement.controller.orders.items.AddOrderItemController;
import src.billiardsmanagement.controller.orders.items.UpdateOrderItemController;
import src.billiardsmanagement.controller.orders.rent.AddRentCueController;
import src.billiardsmanagement.dao.*;
import src.billiardsmanagement.model.*;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ForEachOrderController implements Initializable {

    // Tables 
    @FXML
    private TableView<Booking> bookingPoolTable;

    @FXML
    private TableView<OrderItem> orderItemsTable;

    @FXML
    private TableView<RentCue> rentCueTable;

    // Bookings
    @FXML
    private TableColumn<Booking, String> tableNameColumn;

    @FXML
    private TableColumn<Booking, String> startTimeColumn;

    @FXML
    private TableColumn<Booking, String> endTimeColumn;

    @FXML
    private TableColumn<Booking, String> statusColumn;

    @FXML
    private TableColumn<Booking, Double> timeplayColumn;

    @FXML
    private TableColumn<Booking, Double> priceColumn;

    @FXML
    private TableColumn<Booking, Double> costColumn;

    // Order Items
    @FXML
    private TableColumn<OrderItem, String> productNameColumn;

    @FXML
    private TableColumn<OrderItem, Integer> quantityColumn;

    @FXML
    private TableColumn<OrderItem, Double> priceOrderItemColumn;

    @FXML
    private TableColumn<OrderItem, Double> costOrderItemColumn;

    // Rent Cue
    @FXML
    private TableColumn<RentCue, String> productNameCue;

    @FXML
    private TableColumn<RentCue, LocalDateTime> startTimeCue;

    @FXML
    private TableColumn<RentCue, String> timeplayCue;

    @FXML
    private TableColumn<RentCue, String> priceCue;

    @FXML
    private TableColumn<RentCue, String> costCue;

    @FXML
    private TableColumn<RentCue, LocalDateTime> endTimeCue;

    @FXML
    private TableColumn<RentCue, String> quantityCue;

    @FXML
    private TableColumn<RentCue, String> promotionCue;

    @FXML
    private TableColumn<RentCue, String> statusCue;

    // Order + Customer Overview Details
    @FXML
    private Text customerNameData;
    @FXML
    private Text phoneData;
    @FXML
    private Text currentTableData;
    @FXML
    private Text tableStatusData;

    private final ObservableList<Booking> bookingList = FXCollections.observableArrayList();
    private final ObservableList<OrderItem> orderItemList = FXCollections.observableArrayList();
    private final ObservableList<RentCue> rentCueList = FXCollections.observableArrayList();
    
    private final BookingDAO bookingDAO = new BookingDAO();
    private final OrderItemDAO orderItemDAO = new OrderItemDAO();
    private final RentCueDAO rentCueDAO = new RentCueDAO();

    private int orderID;

    public void setOrderID(int orderID) {
        this.orderID = orderID;
        if (orderID > 0) {
            loadBookings();
            loadOrderDetail();
            loadRentCue();
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
        List<OrderItem> orderItems = OrderItemDAO.getForEachOrderItem(orderID);

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

    private void loadRentCue() {
        // Retrieve rent cue items for the current order
        List<RentCue> rentCues = new ArrayList<>();

        // Kiểm tra dữ liệu
        System.out.println(rentCues);

        for(RentCue rc : RentCueDAO.getAllRentCuesByOrderId(orderID)) {
            if(!rc.getProductName().endsWith("Sale")) {
                rentCues.add(rc);
            }
        }

        // Clear existing items in the table
        rentCueTable.getItems().clear();

        // Check if rentCues is null or empty
        if (rentCues == null || rentCues.isEmpty()) {
            System.out.println("No rent cue items found for order ID: " + orderID);
            return;
        }

        // Add retrieved rent cue items to the table
        rentCueTable.getItems().addAll(rentCues);
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

    private void initializeRentCueColumn() {
        productNameCue.setCellValueFactory(new PropertyValueFactory<>("productName"));
        
        startTimeCue.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        startTimeCue.setCellFactory(column -> new TableCell<RentCue, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("dd-MM '[' HH:mm ']'")));
                }
            }
        });
        
        timeplayCue.setCellValueFactory(new PropertyValueFactory<>("timeplay"));
        priceCue.setCellValueFactory(new PropertyValueFactory<>("subTotal"));
        costCue.setCellValueFactory(new PropertyValueFactory<>("netTotal"));
        
        endTimeCue.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        endTimeCue.setCellFactory(column -> new TableCell<RentCue, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("dd-MM '[' HH:mm ']'")));
                }
            }
        });
        
        quantityCue.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        promotionCue.setCellValueFactory(new PropertyValueFactory<>("promotionName"));
        statusCue.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void initializeOrderCustomerDetail() {
        Order order = OrderDAO.getOrderByIdStatic(orderID);
        if (order != null) {
            customerNameData.setText(order.getCustomerName()!=null ? order.getCustomerName() : "");
            phoneData.setText(order.getCustomerPhone()!=null ? order.getCustomerPhone() : "");
            currentTableData.setText(order.getCurrentTableName()!=null ? order.getCurrentTableName() : "");
            
            OrderStatus status = OrderStatus.valueOf(order.getOrderStatus());
            tableStatusData.setText(convertOrderStatusToDisplayString(status));
            tableStatusData.setStyle(getStyleForStatus(status));
        }
    }
    
    private String convertOrderStatusToDisplayString(OrderStatus status) {
        switch (status) {
            case Booked:
                return "Đã được đặt trước";
            case Playing:
                return "Đang chơi";
            case Finished:
                return "Đã kết thúc";
            default:
                return "Không Xác Định";
        }
    }
    
    private String getStyleForStatus(OrderStatus status) {
        switch (status) {
            case Booked:
                return "-fx-text-fill: orange;";
            case Playing:
                return "-fx-text-fill: green;";
            case Finished:
                return "-fx-text-fill: gray;";
            default:
                return "-fx-text-fill: black;";
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeBookingColumn();
        initializeOrderDetailColumn();
        initializeRentCueColumn();
        initializeOrderCustomerDetail();
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/orders/items/addOrderItem.fxml"));
            Parent root = loader.load();

            AddOrderItemController addOrderItemController = loader.getController();
            addOrderItemController.setOrderId(orderID);

            Stage stage = new Stage();
            stage.setTitle("Add Booking");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadOrderDetail();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load Add Booking form.");
        }
    }

    public void updateOrderItem(ActionEvent event) {
        OrderItem selectedItem = orderItemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Bạn chưa chọn sản phẩm nào để chỉnh sửa !");
            alert.showAndWait();
            return;
        }

        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/orders/items/updateOrderItem.fxml"));
            Parent root = loader.load();

            UpdateOrderItemController updateOrderItemController = loader.getController();
            updateOrderItemController.setOrderId(orderID);
            updateOrderItemController.setOrderItemDetails(selectedItem);
            
            Stage stage = new Stage();
            stage.setTitle("Update Order Item");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Refresh the table after update
            loadOrderDetail();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteOrderItem() {
        try {
            // Get the selected order item from the table
            OrderItem selectedItem = orderItemsTable.getSelectionModel().getSelectedItem();
            
            // Check if an item is selected
            if (selectedItem == null) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Bạn chưa chọn mục để xóa!");
                return;
            }

            // Confirm deletion
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Xác nhận xóa");
            confirmAlert.setHeaderText("Bạn có chắc chắn muốn xóa mục này?");
            confirmAlert.setContentText("Mục đã chọn sẽ bị xóa vĩnh viễn khỏi đơn hàng.");
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Perform deletion in the database
                boolean success = OrderItemDAO.deleteOrderItem(selectedItem.getOrderItemId());
                
                if (success) {
                    // Remove from table
                    orderItemsTable.getItems().remove(selectedItem);
                    
                    // Refresh the table
                    loadOrderDetail();
                    
                    // Show success message
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa mục khỏi đơn hàng!");
                } else {
                    // Show error if deletion fails
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa mục. Vui lòng thử lại!");
                }
            }
        } catch (Exception e) {
            // Handle any unexpected errors
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Đã xảy ra lỗi: " + e.getMessage());
        }
    }


    // Rent Cue Functions
    public void addRentCue(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/orders/rent/addRentCue.fxml"));
            Parent root = loader.load();

            AddRentCueController addRentCueController = loader.getController();
            addRentCueController.setOrderId(orderID);
            
            Stage stage = new Stage();
            stage.setTitle("Add new Rent Cue");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadRentCue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void updateRentCue(ActionEvent event) {
        try {
            // Get the selected rent cue from the table
            Object selectedItem = rentCueTable.getSelectionModel().getSelectedItem();
            
            if (selectedItem == null) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Bạn chưa chọn mục để sửa!");
                return;
            }

            // Open a dialog or window to edit the selected rent cue
            // TODO: Implement logic to open edit rent cue window
            showAlert(Alert.AlertType.INFORMATION, "Sửa thuê cơ", "Chức năng sửa thuê cơ đang được phát triển.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở cửa sổ sửa thuê cơ: " + e.getMessage());
        }
    }

    @FXML
    public void deleteRentCue(ActionEvent event) {
        try {
            // Get the selected rent cue from the table
            RentCue selectedItem = (RentCue) rentCueTable.getSelectionModel().getSelectedItem();
            
            if (selectedItem == null) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Bạn chưa chọn mục để xóa!");
                return;
            }

            // Confirm deletion
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Xác nhận xóa");
            confirmAlert.setHeaderText("Bạn có chắc chắn muốn xóa mục này?");
            confirmAlert.setContentText("Mục đã chọn sẽ bị xóa vĩnh viễn.");
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean deleteSuccess = RentCueDAO.deleteRentCue(selectedItem);
                
                if (deleteSuccess) {
                    // Remove from table
                    rentCueTable.getItems().remove(selectedItem);
                    
                    // Show success message
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa mục thuê cơ!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa mục thuê cơ. Vui lòng thử lại.");
                }
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa mục: " + e.getMessage());
        }
    }

    // Sửa thêm, sao cho không thể end một hàng rentcue 2 lần
    @FXML
    public void endCueRental(ActionEvent event) {
        try {
            // Get the selected rent cue from the table
            RentCue selectedItem = rentCueTable.getSelectionModel().getSelectedItem();
            
            if (selectedItem == null) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Bạn chưa chọn mục thuê cơ để kết thúc!");
                return;
            }

            // Confirm ending the rental
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Xác nhận kết thúc thuê cơ");
            confirmAlert.setHeaderText("Bạn có chắc chắn muốn kết thúc thuê cơ này?");
            confirmAlert.setContentText("Thao tác này sẽ đánh dấu mục thuê cơ là đã kết thúc.");
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Calculate end time and timeplay
                LocalDateTime endTime = LocalDateTime.now();
                selectedItem.setEndTime(endTime);
                
                // Calculate total minutes
                long totalMinutes = java.time.Duration.between(selectedItem.getStartTime(), endTime).toMinutes();
                double timeplay = Math.round(totalMinutes / 60.0 * 10.0) / 10.0; // Convert to hours and round to 1 decimal place
                selectedItem.setTimeplay(timeplay);
                
                // Calculate subtotal (price per hour * hours played)
                double subTotal = Math.ceil(selectedItem.getProductPrice() * timeplay);
                selectedItem.setSubTotal(subTotal);
                
                // Calculate net total based on promotion
                double netTotal;
                if (selectedItem.getPromotionId() <=0) {
                    // No promotion applied
                    netTotal = subTotal;
                } else {
                    // Apply promotion discount
                    netTotal = Math.ceil(subTotal - (subTotal * (selectedItem.getPromotionDiscount() / 100.0)));
                }
                selectedItem.setNetTotal(netTotal);
                
                // Update the status to completed or ended
                selectedItem.setStatus(RentCueStatus.Returned);

                // Update in the database
                boolean updateSuccess = RentCueDAO.endCueRental(selectedItem);
                
                if (updateSuccess) {
                    // Refresh the table
                    loadRentCue();
                    
                    // Show success message
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã kết thúc thuê cơ thành công!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể kết thúc thuê cơ. Vui lòng thử lại.");
                }
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể kết thúc thuê cơ: " + e.getMessage());
        }
    }
}
