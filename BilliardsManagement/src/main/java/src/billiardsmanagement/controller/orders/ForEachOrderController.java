package src.billiardsmanagement.controller.orders;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import src.billiardsmanagement.controller.orders.bookings.AddBookingController;
import src.billiardsmanagement.controller.orders.items.AddOrderItemController;
import src.billiardsmanagement.controller.orders.items.UpdateOrderItemController;
import src.billiardsmanagement.controller.orders.rent.AddRentCueController;
import src.billiardsmanagement.controller.orders.rent.UpdateRentCueController;
import src.billiardsmanagement.dao.*;
import src.billiardsmanagement.model.*;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ForEachOrderController {
    // Buttons
    @FXML
    protected Button finishOrderButton;
    @FXML
    protected Button updateBookingButton;
    @FXML
    protected Button deleteBookingButton;
    @FXML
    protected Button stopBookingButton;
    @FXML
    protected Button addRentCueButton;
    @FXML
    protected Button editRentCueButton;
    @FXML
    protected Button deleteRentCueButton;
    @FXML
    protected Button endCueRentalButton;
    @FXML
    protected Button addOrderItemButton;
    @FXML
    protected Button editOrderItemButton;
    @FXML
    protected Button deleteOrderItemButton;
    @FXML
    protected Button addBookingButton;

    // Tables
    @FXML
    private TableView<Booking> bookingPoolTable;

    @FXML
    private TableView<OrderItem> orderItemsTable;

    @FXML
    private TableView<Order> orderTable;

    @FXML
    private TableView<RentCue> rentCueTable;

    @FXML
    private Text customerText;

    @FXML
    private Text phoneText;

    @FXML
    private Text orderStatusText;

    @FXML
    private TableColumn<Booking, Integer> sttColumn;
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
    private TableColumn<Booking, Double> priceColumn;

    // Order Items
    @FXML
    private TableColumn<OrderItem, String> productNameColumn;

    @FXML
    private TableColumn<OrderItem, Integer> sttOrderItemColumn;

    @FXML
    private TableColumn<OrderItem, Integer> quantityColumn;

    @FXML
    private TableColumn<OrderItem, Double> priceOrderItemColumn;

    @FXML
    private TableColumn<OrderItem, Double> netTotalOrderItemColumn;

    @FXML
    private TableColumn<OrderItem, Double> subTotalOrderItemColumn;

    @FXML
    private TableColumn<OrderItem, String> promotionOrderItem;

    @FXML
    private TableColumn<OrderItem, Double> promotionDiscountOrderItem;

    // Rent Cue
    @FXML
    private TableColumn<RentCue, String> productNameCue;

    @FXML
    private TableColumn<RentCue, Integer> sttRentCueColumn;

    @FXML
    private TableColumn<RentCue, LocalDateTime> startTimeCue;

    @FXML
    private TableColumn<RentCue, LocalDateTime> endTimeCue;

    @FXML
    private TableColumn<RentCue, String> timeplayCue;

    @FXML
    private TableColumn<RentCue, Double> priceCue;

    @FXML
    private TableColumn<RentCue, String> promotionCue;

    @FXML
    private TableColumn<RentCue, Double> promotionDiscountCue;

    @FXML
    private TableColumn<RentCue, String> statusCue;

    @FXML
    private TableColumn<RentCue, Double> subTotalCue;

    @FXML
    private TableColumn<RentCue, Double> netTotalCue;

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
    private Connection conn = DatabaseConnection.getConnection();

    private int orderID;
    private int customerID;

    public void setOrderID(int orderID) {
        this.orderID = orderID;
        if (orderID > 0) {
            loadBookings();
            loadOrderDetail();
            loadRentCue();
        } else {
            NotificationService.showNotification("Invalid Order ID", "The provided Order ID is invalid.",NotificationStatus.Error);
        }
    }

    private void loadBookings() {
        List<Booking> bookings = BookingDAO.getBookingByOrderId(orderID);
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

        for (RentCue rc : RentCueDAO.getAllRentCuesByOrderId(orderID)) {
            if (!rc.getProductName().endsWith("Sale")) {
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
        rentCueList.clear();
        rentCueList.addAll(rentCues);
    }

    private void initializeBookingColumn() {
        bookingPoolTable.setRowFactory(tv -> {
            TableRow<Booking> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1) { // Handle single click
                    int index = row.getTableView().getSelectionModel().getSelectedIndex();
                    if (index >= 0) {
                        Booking currentBooking = row.getTableView().getItems().get(index);
                        if ("Playing".equals(currentBooking.getBookingStatus())) {
                            updateBookingButton.setDisable(true);
                            deleteBookingButton.setDisable(false);
                            stopBookingButton.setDisable(false);
                            return;
                        } else {
                            updateBookingButton.setDisable(false);
                            deleteBookingButton.setDisable(false);
                            stopBookingButton.setDisable(false);
                        }

                        if("Finish".equals(currentBooking.getBookingStatus())){
                            updateBookingButton.setDisable(true);
                            deleteBookingButton.setDisable(true);
                            stopBookingButton.setDisable(true);
                        }
                        else {
                            updateBookingButton.setDisable(false);
                            deleteBookingButton.setDisable(false);
                            stopBookingButton.setDisable(false);
                        }
                    }
                }
            });
            row.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && row.getTableView().getSelectionModel().getSelectedItem() != null) {
                    Booking currentBooking = row.getTableView().getSelectionModel().getSelectedItem();
                    if ("Playing".equals(currentBooking.getBookingStatus())) {
                        updateBookingButton.setDisable(true);
                        deleteBookingButton.setDisable(false);
                        stopBookingButton.setDisable(false);
                        return;
                    } else {
                        updateBookingButton.setDisable(false);
                        deleteBookingButton.setDisable(false);
                        stopBookingButton.setDisable(false);
                    }

                    if("Finish".equals(currentBooking.getBookingStatus())){
                        updateBookingButton.setDisable(true);
                        deleteBookingButton.setDisable(true);
                        stopBookingButton.setDisable(true);
                    }
                    else {
                        updateBookingButton.setDisable(false);
                        deleteBookingButton.setDisable(false);
                        stopBookingButton.setDisable(false);
                    }
                }
            });
            return row;
        });

        sttColumn.setCellValueFactory(param -> {
            int index = sttColumn.getTableView().getItems().indexOf(param.getValue());
            return new SimpleIntegerProperty(index + 1).asObject();
        });
        tableNameColumn.setCellValueFactory(new PropertyValueFactory<>("tableName"));

        startTimeColumn.setCellValueFactory(cellData -> {
            LocalDateTime startTime = cellData.getValue().getStartTime().toLocalDateTime();
            return new SimpleStringProperty(
                    startTime != null ? startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
        });

        endTimeColumn.setCellValueFactory(cellData -> {
            LocalDateTime endTime = cellData.getValue().getEndTime();
            return new SimpleStringProperty(
                    endTime != null ? endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
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
                        setText(String.format("%.1f", item)); // Round to 1 decimal place
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
                setText((empty || item == null) ? null : decimalFormat.format(item)); // Append "VND" to indicate
                // currency
            }
        });

    }

    private void initializeOrderDetailColumn() {
        sttOrderItemColumn.setCellValueFactory(this::orderItemCall);
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        productNameColumn.setSortType(TableColumn.SortType.ASCENDING);

        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityColumn.setSortType(TableColumn.SortType.ASCENDING);

        priceOrderItemColumn.setCellValueFactory(new PropertyValueFactory<>("productPrice"));
        priceOrderItemColumn.setSortType(TableColumn.SortType.ASCENDING);

        priceOrderItemColumn.setCellFactory(column -> new TableCell<OrderItem, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(String.format("%,d", Math.round(item)));
                }
            }
        });

        netTotalOrderItemColumn.setCellValueFactory(new PropertyValueFactory<>("netTotal"));
        netTotalOrderItemColumn.setSortType(TableColumn.SortType.ASCENDING);
        netTotalOrderItemColumn.setCellFactory(column -> new TableCell<OrderItem, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(String.format("%,d", Math.round(item)));
                }
            }
        });

        subTotalOrderItemColumn.setCellValueFactory(new PropertyValueFactory<>("subTotal"));
        subTotalOrderItemColumn.setSortType(TableColumn.SortType.ASCENDING);
        // Initialize OrderItem Promotion Columns
        subTotalOrderItemColumn.setCellFactory(column -> new TableCell<OrderItem, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(String.format("%,d", Math.round(item)));
                }
            }
        });

        promotionOrderItem.setCellValueFactory(new PropertyValueFactory<>("promotionName"));
        promotionDiscountOrderItem.setCellValueFactory(new PropertyValueFactory<>("promotionDiscount"));
    }

    private void initializeRentCueColumn() {
        rentCueTable.setRowFactory(tv -> {
            TableRow<RentCue> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1) {
                    int index = row.getTableView().getSelectionModel().getSelectedIndex();
                    if (index >= 0) {
                        RentCue currentRentCue = row.getTableView().getItems().get(index);
                        if (RentCueStatus.Available.equals(currentRentCue.getStatus())) { // Fixing the error naming
                            // here
                            editRentCueButton.setDisable(true);
                            deleteRentCueButton.setDisable(true);
                            endCueRentalButton.setDisable(true);
                            return;
                        } else {
                            editRentCueButton.setDisable(false);
                            deleteRentCueButton.setDisable(false);
                            endCueRentalButton.setDisable(false);
                        }
                    }
                }
            });
            row.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && row.getTableView().getSelectionModel().getSelectedItem() != null) {
                    RentCue currentRentCue = row.getTableView().getSelectionModel().getSelectedItem();
                    // Assuming there's a method to check the status of order items
                    if (RentCueStatus.Available.equals(currentRentCue.getStatus())) { // Fixing the error naming here
                        editRentCueButton.setDisable(true);
                        deleteRentCueButton.setDisable(true);
                        endCueRentalButton.setDisable(true);
                        return;
                    } else {
                        editRentCueButton.setDisable(false);
                        deleteRentCueButton.setDisable(false);
                        endCueRentalButton.setDisable(false);
                    }
                }
            });
            return row;
        });

        sttRentCueColumn.setCellValueFactory(this::rentCueCall);
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
                    setText(item.format(DateTimeFormatter.ofPattern("dd-MM '['HH:mm']'")));
                }
            }
        });

        timeplayCue.setCellValueFactory(new PropertyValueFactory<>("timeplay"));
        timeplayCue.setSortType(TableColumn.SortType.ASCENDING);

        priceCue.setCellValueFactory(new PropertyValueFactory<>("subTotal"));
        priceCue.setSortType(TableColumn.SortType.ASCENDING);
        priceCue.setCellFactory(column -> new TableCell<RentCue, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(String.format("%,d", Math.round(item)));
                }
            }
        });

        endTimeCue.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        endTimeCue.setSortType(TableColumn.SortType.ASCENDING);
        endTimeCue.setCellFactory(column -> new TableCell<RentCue, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("dd-MM '['HH:mm']'")));
                }
            }
        });

        promotionCue.setCellValueFactory(new PropertyValueFactory<>("promotionName"));
        promotionCue.setSortType(TableColumn.SortType.ASCENDING);

        statusCue.setCellValueFactory(param -> {
            final RentCue rentCue = param.getValue();
            String rentCueStatus = String.valueOf(rentCue.getStatus());
            String newStatus = rentCueStatus.equals("Available") ? "Returned" : "Rented";
            return new SimpleStringProperty(newStatus);
        });
        statusCue.setSortType(TableColumn.SortType.ASCENDING);

        subTotalCue.setCellValueFactory(new PropertyValueFactory<>("subTotal"));
        subTotalCue.setSortType(TableColumn.SortType.ASCENDING);
        subTotalCue.setCellFactory(column -> new TableCell<RentCue, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(String.format("%,d", Math.round(item)));
                }
            }
        });

        netTotalCue.setCellValueFactory(new PropertyValueFactory<>("netTotal"));
        netTotalCue.setSortType(TableColumn.SortType.ASCENDING);
        netTotalCue.setCellFactory(column -> new TableCell<RentCue, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(String.format("%,d", Math.round(item)));
                }
            }
        });

        // Initialize RentCue Promotion Columns
        promotionCue.setCellValueFactory(new PropertyValueFactory<>("promotionName"));
        promotionDiscountCue.setCellValueFactory(new PropertyValueFactory<>("promotionDiscount"));
    }

    private void initializeOrderCustomerDetail() {
        Order order = OrderDAO.getOrderByIdStatic(orderID);
        if (order != null) {
            customerNameData.setText(order.getCustomerName() != null ? order.getCustomerName() : "");
            phoneData.setText(order.getCustomerPhone() != null ? order.getCustomerPhone() : "");
            currentTableData.setText(order.getCurrentTableName() != null ? order.getCurrentTableName() : "");

        }
    }

    // Remove implement Initializable to take control over code flow
    public void initializeAllTables() {
        initializeBookingColumn();
        initializeOrderDetailColumn();
        initializeRentCueColumn();
        checkBookingStatus();

        // if Finished / Paid, disable all Buttons
        if (orderStatusText.getText().equals("Finished") ||
                orderStatusText.getText().equals("Paid") ||
                orderStatusText.getText().equals("Canceled")) {

            finishOrderButton.setDisable(true);
            updateBookingButton.setDisable(true);
            deleteBookingButton.setDisable(true);
            stopBookingButton.setDisable(true);
            addRentCueButton.setDisable(true);
            editRentCueButton.setDisable(true);
            deleteRentCueButton.setDisable(true);
            endCueRentalButton.setDisable(true);
            addOrderItemButton.setDisable(true);
            editOrderItemButton.setDisable(true);
            deleteOrderItemButton.setDisable(true);
            addBookingButton.setDisable(true);
        }
    }

    public void addBooking(ActionEvent event) {
        if (orderStatusText.getText().equals("Paid")) {
            NotificationService.showNotification("Error!","Cannot add booking with status Paid",NotificationStatus.Error);
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/src/billiardsmanagement/orders/bookings/addBooking.fxml"));
            Parent root = loader.load();

            AddBookingController addBookingController = loader.getController();
            addBookingController.setOrderId(orderID);
            addBookingController.setOrderTable(orderTable);
            Stage stage = new Stage();
            stage.setTitle("Add Booking");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadBookings();
        } catch (IOException e) {
            NotificationService.showNotification("Error!","Cannot add Load Booking form !",NotificationStatus.Error);
        }
    }

    public void updateBooking(ActionEvent event) {
        if (orderStatusText.getText().equals("Paid")) {
            NotificationService.showNotification("Error!","Cannot add booking with status Paid",NotificationStatus.Error);
            return;
        }
        // Lấy booking được chọn
        Booking selectedBooking = bookingPoolTable.getSelectionModel().getSelectedItem();
        // Kiểm tra xem có booking nào được chọn không
        if (selectedBooking == null) {
            NotificationService.showNotification("You haven't choose a Booking.","Please select a Booking !",NotificationStatus.Warning);
            return;
        }

        // Kiểm tra trạng thái booking
        if ("Finish".equals(selectedBooking.getBookingStatus())) {
            System.out.println("Status finish");
            NotificationService.showNotification("Can't Update Status",
                    "Cannot update the status of a booking that is already finished.",NotificationStatus.Error);
            return;
        }

        if ("Playing".equals(selectedBooking.getBookingStatus())) {
            System.out.println("Status playing");
            NotificationService.showNotification("Can't Update Status",
                    "Cannot update the status of a booking that is already finished.",NotificationStatus.Error);
            return;
        }

        // Xác định trạng thái mới
        String newTableStatus = "Playing";
        String newBookingStatus = "playing";

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirm Start Playing");
        confirmationAlert.setHeaderText("Confirm start playing on this table ?");

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean updateSuccess = BookingDAO.updateBooking(selectedBooking.getBookingId(),
                    selectedBooking.getOrderId(), selectedBooking.getTableId(), newTableStatus);

            // Kiểm tra kết quả cập nhật và hiển thị thông báo
            if (updateSuccess) {
                NotificationService.showNotification("Start Playing Successful",
                        "Start playing on this table successfully.",NotificationStatus.Success);
                loadBookings(); // Tải lại danh sách booking sau khi cập nhật
            } else {
                NotificationService.showNotification("Update Failed",
                        "Failed to update the booking status. Please try again.",NotificationStatus.Error);
            }
        }
    }

    public void deleteBooking(ActionEvent event) {
        if (orderStatusText.getText().equals("Paid")) {
            NotificationService.showNotification("Error", "Cannot add booking with status 'Paid'.",NotificationStatus.Error);
            return;
        }
        Booking selectedBooking = bookingPoolTable.getSelectionModel().getSelectedItem();

        if (selectedBooking == null) {
            NotificationService.showNotification("No Selection", "Please select a booking to delete.",NotificationStatus.Error);
            return;
        }
        if (selectedBooking.getBookingStatus().equals("Finish")) {
            NotificationService.showNotification("Can't Delete", "Bookings marked as 'finish' cannot be deleted",NotificationStatus.Error);
            return;
        }

        if (selectedBooking.getBookingStatus().equals("Playing")) {
            NotificationService.showNotification("Can't Delete", "Bookings marked as 'playing' cannot be deleted.",NotificationStatus.Error);
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
                NotificationService.showNotification("Success", "Booking deleted successfully.",NotificationStatus.Success);
                loadBookings();
            } else {
                NotificationService.showNotification("Error", "Failed to delete the booking.",NotificationStatus.Error);
            }
        }
    }

    public void addOrderItem(ActionEvent event) {
        if (orderStatusText.getText().equals("Paid")) {
            NotificationService.showNotification("Error",
                    "You cannot make any changes in this order as it has already been paid.",
                    NotificationStatus.Error);
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/src/billiardsmanagement/orders/items/addOrderItem.fxml"));
            Parent root = loader.load();

            AddOrderItemController addOrderItemController = loader.getController();
            addOrderItemController.setOrderId(orderID);
            Stage stage = new Stage();
            stage.setTitle("Add a new Order Item");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadOrderDetail();
        } catch (IOException e) {
            e.printStackTrace();
            NotificationService.showNotification("Error", "Failed to load Add Booking form.", NotificationStatus.Error);
        }
    }

    public void updateOrderItem(ActionEvent event) {
        if (orderStatusText.getText().equals("Paid")) {
            NotificationService.showNotification("Error",
                    "You cannot make any changes in this order as it has already been paid.",
                    NotificationStatus.Error);
            return;
        }

        OrderItem selectedItem = orderItemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            NotificationService.showNotification("Error",
                    "You haven't selected any item to edit!",
                    NotificationStatus.Error);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/src/billiardsmanagement/orders/items/updateOrderItem.fxml"));
            Parent root = loader.load();

            UpdateOrderItemController updateOrderItemController = loader.getController();
            updateOrderItemController.setOrderId(orderID);
            updateOrderItemController.setOrderItemDetails(selectedItem);
            updateOrderItemController.initializeOrderItem();

            Stage stage = new Stage();
            stage.setTitle("Update Order Item");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Refresh the table after update
            loadOrderDetail();
        } catch (Exception e) {
            e.printStackTrace();
            NotificationService.showNotification("Error",
                    "Failed to load Update Order Item form.",
                    NotificationStatus.Error);
        }
    }


    public void deleteOrderItem() {
        if (orderStatusText.getText().equals("Paid")) {
            NotificationService.showNotification("Error",
                    "You cannot make any changes in this order as it has already been paid.",
                    NotificationStatus.Error);
            return;
        }
        try {
            // Get the selected order item from the table
            OrderItem selectedItem = orderItemsTable.getSelectionModel().getSelectedItem();

            // Check if an item is selected
            if (selectedItem == null) {
                NotificationService.showNotification("Warning",
                        "You haven't selected an item to delete!",
                        NotificationStatus.Warning);
                return;
            }

            // Confirm deletion
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Deletion");
            confirmAlert.setHeaderText("Are you sure you want to delete this item?");
            confirmAlert.setContentText("The selected item will be permanently removed from the order.");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Perform deletion in the database
                boolean success = OrderItemDAO.deleteOrderItem(selectedItem.getOrderItemId())
                        && ProductDAO.replenishItem(selectedItem.getProductName(), selectedItem.getQuantity());

                if (success) {
                    // Remove from table
                    orderItemsTable.getItems().remove(selectedItem);

                    // Refresh the table
                    loadOrderDetail();

                    // Show success message
                    NotificationService.showNotification("Success",
                            "Item has been removed from the order!",
                            NotificationStatus.Success);
                } else {
                    // Show error if deletion fails
                    NotificationService.showNotification("Error",
                            "Failed to delete item. Please try again!",
                            NotificationStatus.Error);
                }
            }
        } catch (Exception e) {
            // Handle any unexpected errors
            NotificationService.showNotification("Error",
                    "An error occurred: " + e.getMessage(),
                    NotificationStatus.Error);
        }
    }


    // Rent Cue Functions
    public void addRentCue(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/src/billiardsmanagement/orders/rent/addRentCue.fxml"));
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
        if (orderStatusText.getText().equals("Paid")) {
            NotificationService.showNotification("Error",
                    "You cannot make any changes in this order as it has already been paid.",
                    NotificationStatus.Error);
            return;
        }
        try {
            // Get the selected rent cue from the table
            RentCue selectedItem = rentCueTable.getSelectionModel().getSelectedItem();

            if (selectedItem == null) {
                NotificationService.showNotification("Warning",
                        "You haven't selected an item to edit!",
                        NotificationStatus.Warning);
                return;
            }

            if (selectedItem.getStatus().equals(RentCueStatus.Available)) {
                NotificationService.showNotification("Warning",
                        "This Cue Rental has already been returned. You cannot edit this!",
                        NotificationStatus.Warning);
                return;
            }

            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/src/billiardsmanagement/orders/rent/updateRentCue.fxml"));
                Parent root = loader.load();

                UpdateRentCueController updateRentCueController = loader.getController();
                updateRentCueController.setOrderID(this.orderID);
                updateRentCueController.setRentCueId(selectedItem.getRentCueId());
                updateRentCueController.setPromotionName(selectedItem.getPromotionName());
                updateRentCueController.initializeRentCue();

                Stage stage = new Stage();
                stage.setTitle("Edit Rent Cue");
                stage.setScene(new Scene(root));
                stage.showAndWait();

                loadRentCue();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            NotificationService.showNotification("Error",
                    "Failed to open edit rent cue window: " + e.getMessage(),
                    NotificationStatus.Error);
        }
    }

    @FXML
    public void deleteRentCue(ActionEvent event) {
        if (orderStatusText.getText().equals("Paid")) {
            NotificationService.showNotification("Error",
                    "You cannot make any changes in this order as it has already been paid.",
                    NotificationStatus.Error);
            return;
        }

        try {
            // Get the selected rent cue from the table
            RentCue selectedItem = rentCueTable.getSelectionModel().getSelectedItem();

            if (selectedItem == null) {
                NotificationService.showNotification("Warning",
                        "You haven't selected an item to delete!",
                        NotificationStatus.Warning);
                return;
            }

            if (selectedItem.getStatus().equals(RentCueStatus.Available)) {
                NotificationService.showNotification("Warning",
                        "You cannot delete this Cue Rental; it has already been returned.",
                        NotificationStatus.Warning);
                return;
            }

            // Confirm deletion
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Deletion");
            confirmAlert.setHeaderText("Are you sure you want to delete this item?");
            confirmAlert.setContentText("The selected item will be permanently deleted.");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean deleteSuccess = RentCueDAO.deleteRentCue(selectedItem)
                        && ProductDAO.replenishItem(selectedItem.getProductName(), 1);

                if (deleteSuccess) {
                    // Remove from table
                    rentCueTable.getItems().remove(selectedItem);

                    // Show success message
                    NotificationService.showNotification("Success",
                            "Rent cue has been deleted!",
                            NotificationStatus.Success);
                } else {
                    NotificationService.showNotification("Error",
                            "Failed to delete rent cue. Please try again.",
                            NotificationStatus.Error);
                }
            }
        } catch (Exception e) {
            NotificationService.showNotification("Error",
                    "Failed to delete item: " + e.getMessage(),
                    NotificationStatus.Error);
        }
    }

    @FXML
    public void endCueRental(ActionEvent event) {
        if (orderStatusText.getText().equals("Paid")) {
            NotificationService.showNotification("Error",
                    "Cannot add booking with status 'Paid'.",
                    NotificationStatus.Error);
            return;
        }

        try {
            // Get the selected rent cue from the table
            RentCue selectedItem = rentCueTable.getSelectionModel().getSelectedItem();

            if (selectedItem == null) {
                NotificationService.showNotification("Warning",
                        "You haven't selected a rent cue to end!",
                        NotificationStatus.Warning);
                return;
            }

            if (selectedItem.getStatus().equals(RentCueStatus.Available)) {
                NotificationService.showNotification("Warning",
                        "This Cue Rental has already been returned. You cannot return it again!",
                        NotificationStatus.Warning);
                return;
            }

            // Confirm ending the rental
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm End Rental");
            confirmAlert.setHeaderText("Are you sure you want to end this rent cue?");
            confirmAlert.setContentText("This action will mark the rent cue as ended.");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Calculate end time and time played
                boolean updateSuccess = finishEachCueRental(selectedItem);

                if (updateSuccess) {
                    // Refresh the table
                    loadRentCue();

                    // Show success message
                    NotificationService.showNotification("Success",
                            "Cue rental has been successfully ended!",
                            NotificationStatus.Success);
                } else {
                    NotificationService.showNotification("Error",
                            "Failed to end rent cue rental. Please try again.",
                            NotificationStatus.Error);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            NotificationService.showNotification("Error",
                    "Failed to end rent cue rental: " + e.getMessage(),
                    NotificationStatus.Error);
        }
    }


    // finishEachCueRental
    public boolean finishEachCueRental(RentCue selectedItem) {
        LocalDateTime endTime = LocalDateTime.now();
        selectedItem.setEndTime(endTime);

        // Calculate total minutes
        long totalMinutes = java.time.Duration.between(selectedItem.getStartTime(), endTime).toMinutes();
        double timeplay = Math.round(totalMinutes / 60.0 * 10.0) / 10.0; // Convert to hours and round to 1

        selectedItem.setTimeplay(timeplay);

        // Calculate subtotal (price per hour * hours played)
        double subTotal = Math.ceil(selectedItem.getProductPrice() * timeplay);
        selectedItem.setSubTotal(subTotal);

        // Calculate net t otal based on promotion
        double netTotal;
        if (selectedItem.getPromotionId() <= 0) {
            // No promotion applied
            netTotal = subTotal;
        } else {
            // Apply promotion discount
            netTotal = Math.ceil(subTotal - (subTotal * (selectedItem.getPromotionDiscount() / 100.0)));
        }
        selectedItem.setNetTotal(netTotal);

        // Update the status to completed or ended
        selectedItem.setStatus(RentCueStatus.Available);

        // Update in the database
        return RentCueDAO.endCueRental(selectedItem) && ProductDAO.replenishItem(selectedItem.getProductName(), 1);
    }

    protected void updateProductQuantity(String productName) {
        // Assuming you have a method to get the database connection
        Connection conn = DatabaseConnection.getConnection();

        try (PreparedStatement pstmt = conn
                .prepareStatement("UPDATE products SET quantity = quantity + 1 WHERE name = ?")) {
            pstmt.setString(1, productName);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                System.out.println("No product found with the name: " + productName);
            } else {
                System.out.println("Product quantity updated successfully.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            NotificationService.showNotification("Error", "Failed to update product quantity: " + e.getMessage(),NotificationStatus.Error);
        }
    }

    public void stopBooking(ActionEvent event) {
        if (orderStatusText.getText().equals("Paid")) {
            NotificationService.showNotification("Error",
                    "Cannot add booking with status 'Paid'.",
                    NotificationStatus.Error);
            return;
        }

        Booking selectedBooking = bookingPoolTable.getSelectionModel().getSelectedItem();
        if (selectedBooking == null) {
            NotificationService.showNotification("Warning",
                    "Please select a booking to update.",
                    NotificationStatus.Warning);
            return;
        }

        String bookingStatus = selectedBooking.getBookingStatus();
        if (bookingStatus.equalsIgnoreCase("Order")) {
            NotificationService.showNotification("Warning",
                    "This booking is in 'Order' status. You cannot end this booking!",
                    NotificationStatus.Warning);
            return;
        }
        if (bookingStatus.equalsIgnoreCase("Finish")) {
            NotificationService.showNotification("Warning",
                    "This booking has already finished. You cannot end this booking!",
                    NotificationStatus.Warning);
            return;
        }

        int bookingId = selectedBooking.getBookingId();
        Timestamp startTime = selectedBooking.getStartTime();
        int poolTableId = selectedBooking.getTableId();

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirm Stop Booking");
        confirmationAlert.setHeaderText("This booking will be stopped. Are you sure?");

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = BookingDAO.stopBooking(bookingId, startTime, poolTableId);
                if (success) {
                    NotificationService.showNotification("Success",
                            "Booking has been stopped and updated.",
                            NotificationStatus.Success);
                    loadBookings();
                } else {
                    NotificationService.showNotification("Error",
                            "Failed to stop booking due to invalid data.",
                            NotificationStatus.Error);
                }
            } catch (SQLException e) {
                NotificationService.showNotification("Error",
                        "Failed to stop booking: " + e.getMessage(),
                        NotificationStatus.Error);
            }
        }
    }


    public void checkBookingStatus() {
        List<Booking> bookings = BookingDAO.getBookingByOrderId(orderID); // Lấy danh sách booking

        LocalDateTime now = LocalDateTime.now(); // Thời gian hiện tại

        for (Booking booking : bookings) {
            LocalDateTime bookingTime = booking.getStartTimeBooking(); // Lấy thời gian bắt đầu booking
            if (bookingTime != null) { // Kiểm tra nếu booking có thời gian bắt đầu
                long minutesPassed = Duration.between(bookingTime, now).toMinutes();
                System.out.println("Thời gian chênh lệch: " + minutesPassed);
                if (minutesPassed > 30 && "Order".equals(booking.getBookingStatus())) {
                    BookingDAO.updateBookingStatus(booking.getBookingId());
                    System.out.println("Đã hủy bàn "+ booking.getBookingId() + "thành công");
                }
            }
        }

    }

    public void setCustomerID(int customerId) {
        this.customerID = customerId;
        if (customerId > 0) {
            loadInfo();
        }
    }

    private void loadInfo() {
        List<Customer> customerList = customerDAO.getInfoCustomer(customerID);
        Order orderList = OrderDAO.getOrderById(orderID);

        if (customerList != null && !customerList.isEmpty() && orderList != null) {
            Customer customer = customerList.get(0);

            customerText.setText(customer.getName());
            phoneText.setText(customer.getPhone());
            orderStatusText.setText(orderList.getOrderStatus());
        }
    }

    private String formatTotal(double total) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        return decimalFormat.format(total);
    }

    public void finishOrder(ActionEvent event) {
        if (orderStatusText.getText().equals("Finished")) {
            NotificationService.showNotification("Error",
                    "This order has already been finished. You cannot finish it again!",
                    NotificationStatus.Error);
            return;
        }

        // Kiểm tra nếu có booking nào đang ở trạng thái "Order"
        boolean hasPendingBookings = bookingPoolTable.getItems().stream()
                .anyMatch(booking -> "Order".equals(booking.getBookingStatus()));

        if (hasPendingBookings) {
            NotificationService.showNotification("Error",
                    "There are pending bookings in 'Order' status. Please complete them before finishing the order.",
                    NotificationStatus.Error);
            return;
        }

        // Hiển thị xác nhận trước khi hoàn tất đơn hàng
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Finish Order");
        confirmationAlert.setHeaderText("Are you sure you want to finish this order?");
        confirmationAlert.setContentText("This will mark the order as finished and finalize all bookings and rentals.");

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            boolean finishAllSuccess = RentCueDAO.endAllCueRentals(this.orderID, rentCueList)
                    && BookingDAO.finishOrder(this.orderID)
                    && ProductDAO.replenishMultipleItems(rentCueList);

            if (!finishAllSuccess) {
                NotificationService.showNotification("Error",
                        "An error occurred while finalizing the order. Please try again later.",
                        NotificationStatus.Error);
                return;
            }

            // Cập nhật trạng thái đơn hàng và tổng tiền
            double totalCost = OrderDAO.calculateOrderTotal(orderID);
            boolean success = OrderDAO.updateOrderStatus(orderID, totalCost);

            if (success) {
                System.out.println("Order total cost updated successfully: $" + totalCost);

                // Thông báo thanh toán thành công
                NotificationService.showNotification("Success",
                        "The order has been successfully finished! Total cost: $" + totalCost,
                        NotificationStatus.Success);

                // Đóng cửa sổ hiện tại
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.close();

                // Cập nhật danh sách đơn hàng
                loadOrderList();
            } else {
                NotificationService.showNotification("Error",
                        "Failed to finalize the order. Please check the data and try again.",
                        NotificationStatus.Error);
            }
        } catch (Exception e) {
            e.printStackTrace();
            NotificationService.showNotification("Error",
                    "Unexpected error: " + e.getMessage(),
                    NotificationStatus.Error);
        }
    }


    // Giả sử bạn có phương thức refreshOrderDetails để cập nhật giao diện

    public void setOrderTable(TableView<Order> orderTable) {
        this.orderTable = orderTable;
    }

    private void loadOrderList() {
        List<Order> orders = orderDAO.getAllOrders();
        orderTable.setItems(FXCollections.observableArrayList(orders));
    }

    private ObservableValue<Integer> orderItemCall(TableColumn.CellDataFeatures<OrderItem, Integer> cellData) {
        // Lấy vị trí (index) của dòng hiện tại trong danh sách
        int index = orderItemsTable.getItems().indexOf(cellData.getValue()) + 1;
        return new SimpleIntegerProperty(index).asObject();
    }

    private ObservableValue<Integer> rentCueCall(TableColumn.CellDataFeatures<RentCue, Integer> cellData) {
        // Lấy vị trí (index) của dòng hiện tại trong danh sách
        int index = rentCueTable.getItems().indexOf(cellData.getValue()) + 1;
        return new SimpleIntegerProperty(index).asObject();
    }
}
