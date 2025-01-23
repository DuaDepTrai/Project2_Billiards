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
import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
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

    @FXML
    private Text customerText;

    @FXML
    private Text phoneText;

    @FXML
    private Text orderStatusText;
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
    private TableColumn<Booking, Double> subTotalColumn;

    @FXML
    private TableColumn<Booking, Double> costColumn;

    @FXML
    private TableColumn<Booking,Double> priceColumn;

    // Order Items
    @FXML
    private TableColumn<OrderItem, String> productNameColumn;

    @FXML
    private TableColumn<OrderItem, Integer> quantityColumn;

    @FXML
    private TableColumn<OrderItem, Double> priceOrderItemColumn;

    @FXML
    private TableColumn<OrderItem, Double> netTotalOrderItemColumn;

    @FXML
    private TableColumn<OrderItem, Double> subTotalOrderItemColumn;

    // Rent Cue
    @FXML
    private TableColumn<RentCue, String> productNameCue;

    @FXML
    private TableColumn<RentCue, LocalDateTime> startTimeCue;

    @FXML
    private TableColumn<RentCue, LocalDateTime> endTimeCue;

    @FXML
    private TableColumn<RentCue, String> timeplayCue;

    @FXML
    private TableColumn<RentCue, String> priceCue;

    @FXML
    private TableColumn<RentCue, String> quantityCue;

    @FXML
    private TableColumn<RentCue, String> promotionCue;

    @FXML
    private TableColumn<RentCue, String> statusCue;

    @FXML
    private TableColumn<RentCue, String> subTotalCue;

    @FXML
    private TableColumn<RentCue, String> netTotalCue;

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
    private final OrderDAO orderDAO = new OrderDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final OrderItemDAO orderItemDAO = new OrderItemDAO();
    private final RentCueDAO rentCueDAO = new RentCueDAO();

    private int orderID;
    private int customerID;
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

        // Use CellFactory to round values to one decimal place
        timeplayColumn.setCellFactory(column -> {
            return new TableCell<Booking, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        // Round and display as a string
                        setText(String.format("%.1f", item));  // Round to 1 decimal place
                    }
                }
            };
        });

        subTotalColumn.setCellValueFactory(new PropertyValueFactory<>("subTotal"));
        subTotalColumn.setCellFactory(column -> new TableCell<>() {
            private final DecimalFormat decimalFormat = new DecimalFormat("#,###");

            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : decimalFormat.format(item));
            }
        });

        costColumn.setCellValueFactory(new PropertyValueFactory<>("netTotal"));
        costColumn.setCellFactory(column -> new TableCell<>() {
            private final DecimalFormat decimalFormat = new DecimalFormat("#,###");

            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : decimalFormat.format(item));
            }
        });

        statusColumn.setCellValueFactory(new PropertyValueFactory<>("bookingStatus"));

        // Add priceColumn for table price
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("priceTable"));
        priceColumn.setCellFactory(column -> new TableCell<>() {
            private final DecimalFormat decimalFormat = new DecimalFormat("#,###"); // Display as whole numbers

            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : decimalFormat.format(item) ); // Append "VND" to indicate currency
            }
        });

    }


    private void initializeOrderDetailColumn() {
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));

        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        priceOrderItemColumn.setCellValueFactory(new PropertyValueFactory<>("productPrice"));
        priceOrderItemColumn.setCellFactory(column -> new TableCell<>() {
            private final DecimalFormat decimalFormat = new DecimalFormat("#,###"); // Không có phần thập phân

            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : decimalFormat.format(item));
            }
        });

// Định dạng cho netTotalOrderItemColumn
        netTotalOrderItemColumn.setCellValueFactory(new PropertyValueFactory<>("netTotal"));
        netTotalOrderItemColumn.setCellFactory(column -> new TableCell<>() {
            private final DecimalFormat decimalFormat = new DecimalFormat("#,###"); // Không có phần thập phân

            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : decimalFormat.format(item));
            }
        });

// Định dạng cho subTotalOrderItemColumn
        subTotalOrderItemColumn.setCellValueFactory(new PropertyValueFactory<>("subTotal"));
        subTotalOrderItemColumn.setCellFactory(column -> new TableCell<>() {
            private final DecimalFormat decimalFormat = new DecimalFormat("#,###"); // Không có phần thập phân

            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : decimalFormat.format(item));
            }
        });
    }

    private void initializeRentCueColumn() {
        productNameCue.setCellValueFactory(new PropertyValueFactory<>("productName"));
        productNameCue.setSortType(TableColumn.SortType.ASCENDING);

        startTimeCue.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        startTimeCue.setSortType(TableColumn.SortType.ASCENDING);
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
        timeplayCue.setSortType(TableColumn.SortType.ASCENDING);

        priceCue.setCellValueFactory(new PropertyValueFactory<>("subTotal"));
        priceCue.setSortType(TableColumn.SortType.ASCENDING);

        endTimeCue.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        endTimeCue.setSortType(TableColumn.SortType.ASCENDING);
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
        quantityCue.setSortType(TableColumn.SortType.ASCENDING);

        promotionCue.setCellValueFactory(new PropertyValueFactory<>("promotionName"));
        promotionCue.setSortType(TableColumn.SortType.ASCENDING);

        statusCue.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCue.setSortType(TableColumn.SortType.ASCENDING);

        subTotalCue.setCellValueFactory(new PropertyValueFactory<>("subTotal"));
        subTotalCue.setSortType(TableColumn.SortType.ASCENDING);

        netTotalCue.setCellValueFactory(new PropertyValueFactory<>("netTotal"));
        netTotalCue.setSortType(TableColumn.SortType.ASCENDING);
    }

    private void initializeOrderCustomerDetail() {
        Order order = OrderDAO.getOrderByIdStatic(orderID);
        if (order != null) {
            customerNameData.setText(order.getCustomerName()!=null ? order.getCustomerName() : "");
            phoneData.setText(order.getCustomerPhone()!=null ? order.getCustomerPhone() : "");
            currentTableData.setText(order.getCurrentTableName()!=null ? order.getCurrentTableName() : "");
            

        }
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeBookingColumn();
        initializeOrderDetailColumn();
        initializeRentCueColumn();

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

        if (selectedBooking.getBookingStatus().equals("finish")) {
            showAlert(Alert.AlertType.WARNING, "Can't Update Status", "Cannot update the status of a booking that is already finished.");
            return;
        }

        LocalDate startDate = selectedBooking.getStartTime().toLocalDateTime().toLocalDate();
        if (!startDate.equals(LocalDate.now())) {
            showAlert(Alert.AlertType.WARNING, "Invalid Date", "You can only update bookings with today's start time.");
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
        if (selectedBooking.getBookingStatus().equals("finish")) {
            showAlert(Alert.AlertType.WARNING, "Can't Delete", "Bookings marked as 'finish' cannot be deleted.");
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
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/orders/rent/addRentCue.fxml"));
//            Parent root = loader.load();
//
//            AddRentCueController addRentCueController = loader.getController();
//            addRentCueController.setOrderId(orderID);
//
//            Stage stage = new Stage();
//            stage.setTitle("Add new Rent Cue");
//            stage.setScene(new Scene(root));
//            stage.showAndWait();
//
//            loadRentCue();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
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

    public void stopBooking(ActionEvent event) {
        Booking selectedBooking = bookingPoolTable.getSelectionModel().getSelectedItem();

        if (selectedBooking == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a booking to update.");
            return;
        }

        if (selectedBooking.getBookingStatus().equals("finish") || selectedBooking.getBookingStatus().equals("order")) {
            showAlert(Alert.AlertType.WARNING, "Can't Stop", "Please select a booking different to stop.");
            return;
        }

        if (selectedBooking.getEndTime() != null) {
            showAlert(Alert.AlertType.WARNING, "Can't Stop", "Please select a booking different to stop.");
            return;
        }

        int bookingId = selectedBooking.getBookingId();
        Timestamp startTime = selectedBooking.getStartTime();
        int poolTableId = selectedBooking.getTableId();

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Start a transaction
            conn.setAutoCommit(false);

            // Get the current time (end time)
            String currentTimeQuery = "SELECT NOW()";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(currentTimeQuery)) {
                rs.next();
                Timestamp currentTime = rs.getTimestamp(1);

                // Validate: Ensure endTime is not earlier than startTime
                if (currentTime.before(startTime)) {
                    showAlert(Alert.AlertType.WARNING, "Invalid End Time", "End time cannot be earlier than start time. Please try again.");
                    return;
                }

                // Calculate time played (timeplay) in minutes
                String timeplayQuery = "SELECT TIMESTAMPDIFF(MINUTE, ?, ?) AS timeplay";
                try (PreparedStatement timeplayStmt = conn.prepareStatement(timeplayQuery)) {
                    timeplayStmt.setTimestamp(1, startTime);
                    timeplayStmt.setTimestamp(2, currentTime);
                    try (ResultSet timeplayRs = timeplayStmt.executeQuery()) {
                        timeplayRs.next();
                        int timeplayInMinutes = timeplayRs.getInt("timeplay");

                        // Convert time played from minutes to hours and round to 1 decimal place
                        double timeplayInHours = Math.round((timeplayInMinutes / 60.0) * 10.0) / 10.0;

                        // Get the price of the pool table
                        String priceQuery = "SELECT price FROM pooltables WHERE table_id = ?";
                        try (PreparedStatement priceStmt = conn.prepareStatement(priceQuery)) {
                            priceStmt.setInt(1, poolTableId);
                            try (ResultSet priceRs = priceStmt.executeQuery()) {
                                priceRs.next();
                                double price = priceRs.getDouble("price");

                                // Calculate subtotal
                                double subtotal = timeplayInHours * price;

                                // Update the booking record
                                String updateQuery = "UPDATE bookings SET end_time = ?, timeplay = ?, subtotal = ?, booking_status = 'finish' WHERE booking_id = ?";
                                try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                                    updateStmt.setTimestamp(1, currentTime);
                                    updateStmt.setDouble(2, timeplayInHours);
                                    updateStmt.setDouble(3, subtotal);
                                    updateStmt.setInt(4, bookingId);
                                    updateStmt.executeUpdate();

                                    // Commit the transaction
                                    conn.commit();

                                    // Success notification
                                    showAlert(Alert.AlertType.INFORMATION, "Booking Stopped", "Booking has been stopped and updated.");
                                    loadBookings();
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();

            // Rollback transaction on error
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }

            showAlert(Alert.AlertType.ERROR, "Error", "Failed to stop booking: " + e.getMessage());
        }
    }


    public void setCustomerID(int customerId) {
        this.customerID = customerId;
        if(customerId > 0 ){
            loadInfo();
        }
    }

    private void loadInfo() {
        List<Customer> customerList = customerDAO.getInfoCustomer(customerID);
        Order  orderList = orderDAO.getOrderById(orderID);

        if(customerList != null && !customerList.isEmpty() && orderList != null ){
            Customer customer = customerList.get(0);

            customerText.setText(customer.getName());
            phoneText.setText(customer.getPhone());
            orderStatusText.setText(orderList.getOrderStatus());
        }
    }
}
