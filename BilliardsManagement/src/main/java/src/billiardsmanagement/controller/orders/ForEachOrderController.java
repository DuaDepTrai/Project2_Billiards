package src.billiardsmanagement.controller.orders;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import src.billiardsmanagement.controller.orders.bookings.AddBookingController;
import src.billiardsmanagement.controller.orders.items.AddOrderItemController;
import src.billiardsmanagement.controller.orders.items.UpdateOrderItemController;
import src.billiardsmanagement.dao.*;
import src.billiardsmanagement.model.*;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.layout.HBox;
import src.billiardsmanagement.model.DatabaseConnection;
import src.billiardsmanagement.model.NotificationService;
import src.billiardsmanagement.model.NotificationStatus;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    protected Button addOrderItemButton;
    @FXML
    protected Button editOrderItemButton;
    @FXML
    protected Button deleteOrderItemButton;
    @FXML
    protected Button addBookingButton;
    @FXML
    protected Button cancelBookingButton;

    // Actions
    @FXML
    private TableColumn<Booking,Void> bookingActionColumn;

    @FXML
    private TableColumn<OrderItem,Void> orderItemActionColumn;

    // Tables
    @FXML
    private TableView<Booking> bookingPoolTable;

    @FXML
    private TableView<OrderItem> orderItemsTable;

    @FXML
    private TableView<Order> orderTable;

    @FXML
    private TextField customerText;

    @FXML
    private TextField phoneText;

    @FXML
    private TextField orderStatusText;

    @FXML
    private TextField billNoText;

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

    // @FXML
    // private TableColumn<OrderItem, String> promotionOrderItem;

    // @FXML
    // private TableColumn<OrderItem, Double> promotionDiscountOrderItem;

    private final ObservableList<Booking> bookingList = FXCollections.observableArrayList();
    private final ObservableList<OrderItem> orderItemList = FXCollections.observableArrayList();

    private final BookingDAO bookingDAO = new BookingDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final OrderItemDAO orderItemDAO = new OrderItemDAO();
    private Connection conn = DatabaseConnection.getConnection();

    private int orderID;
    private int customerID;
    private int billNo;

    private Booking currentBookingSelected;
    private OrderItem currentOrderItemSelected;

    public void setOrderID(int orderID) {
        this.orderID = orderID;
        if (orderID > 0) {
            loadBookings();
            loadOrderDetail();
        } else {
            NotificationService.showNotification("Invalid Order ID", "The provided Order ID is invalid.", NotificationStatus.Error);
        }
    }

    private void loadBookings() {
        List<Booking> bookings = BookingDAO.getBookingByOrderId(orderID);
        bookings.sort((b1,b2) -> b2.getBookingId() - b1.getBookingId());
        bookingList.clear();
        bookingList.addAll(bookings);
        bookingPoolTable.setItems(bookingList);
    }

    private void loadOrderDetail() {
        orderItemList.clear();
        List<OrderItem> items = OrderItemDAO.getForEachOrderItem(orderID);
        items.sort((i1,i2) -> i2.getOrderItemId() - i1.getOrderItemId());
        System.out.println(items.isEmpty() ? "Order Item list in ForEachOrderController, loadOrderDetail() don't have any element !" : "");
        orderItemList.addAll(items); 
        orderItemsTable.setItems(orderItemList);
        // Promotion-related code commented out
        /*
        promotionOrderItem.setCellValueFactory(cellData -> {
            OrderItem orderItem = cellData.getValue();
            String promotionName = orderItem.getPromotionName();
            return new SimpleStringProperty(promotionName != null ? promotionName : "");
        });
        promotionDiscountOrderItem.setCellValueFactory(cellData -> {
            OrderItem orderItem = cellData.getValue();
            Double discount = orderItem.getPromotionDiscount();
            return new SimpleObjectProperty<>(discount != null ? discount : 0.0);
        });
        */
    }

    private void initializeBookingColumn() {
        // Add New button in header
        Button addBookingButton = new Button("Add New");
        addBookingButton.getStyleClass().add("header-button");
        addBookingButton.setPrefWidth(bookingActionColumn.getPrefWidth());
        addBookingButton.setOnAction(event -> addBooking(event));
        bookingActionColumn.setGraphic(addBookingButton);

        // Check Order Status
        String orderStatus = orderStatusText.getText();
        if(orderStatus.equals("Finished") || orderStatus.equals("Paid") || orderStatus.equals("Canceled")){
            addBookingButton.setDisable(true);
        }

        // Set cell factory for action column
        bookingActionColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                Booking booking = getTableView().getItems().get(getIndex());
                HBox actionBox = new HBox();
                actionBox.getStyleClass().add("action-hbox");

                switch (booking.getBookingStatus()) {
                    case "Order":
                        FontAwesomeIconView playIcon = new FontAwesomeIconView(FontAwesomeIcon.PLAY);
                        playIcon.getStyleClass().add("action-icon");
                        playIcon.setOnMouseClicked(e -> {
                            setCurrentBookingSelected(booking);
                            updateBooking(new ActionEvent());
                        });

                        FontAwesomeIconView cancelIcon = new FontAwesomeIconView(FontAwesomeIcon.TIMES);
                        cancelIcon.getStyleClass().add("action-icon");
                        cancelIcon.setOnMouseClicked(e -> {
                            setCurrentBookingSelected(booking);
                            cancelBooking(new ActionEvent());
                        });

                        actionBox.getChildren().addAll(playIcon, cancelIcon);
                        break;

                    case "Playing":
                        FontAwesomeIconView stopIcon = new FontAwesomeIconView(FontAwesomeIcon.PAUSE);
                        stopIcon.getStyleClass().add("action-icon");
                        stopIcon.setOnMouseClicked(e -> {
                            setCurrentBookingSelected(booking);
                            stopBooking(new ActionEvent());
                        });
                        actionBox.getChildren().add(stopIcon);
                        break;

                    case "Canceled":
                        FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
                        deleteIcon.getStyleClass().add("action-icon");
                        deleteIcon.setOnMouseClicked(e -> {
                            setCurrentBookingSelected(booking);
                            deleteBooking(new ActionEvent());
                        });
                        actionBox.getChildren().add(deleteIcon);
                        break;

                    case "Finish":
                        Text finishedText = new Text("Finished");
                        finishedText.getStyleClass().add("finished-text");
                        actionBox.getChildren().add(finishedText);
                        break;
                }

                setGraphic(actionBox);
            }
        });

        sttColumn.setCellValueFactory(param -> {
            int index = sttColumn.getTableView().getItems().indexOf(param.getValue());
            return new SimpleIntegerProperty(index + 1).asObject();
        });
        tableNameColumn.setCellValueFactory(new PropertyValueFactory<>("tableName"));

        startTimeColumn.setCellValueFactory(cellData -> {
            LocalDateTime startTime = cellData.getValue().getStartTime().toLocalDateTime();
            return new SimpleStringProperty(
                    startTime != null ? startTime.format(DateTimeFormatter.ofPattern("HH'h'mm")) : "");
        });

        endTimeColumn.setCellValueFactory(cellData -> {
            LocalDateTime endTime = cellData.getValue().getEndTime();
            return new SimpleStringProperty(
                    endTime != null ? endTime.format(DateTimeFormatter.ofPattern("HH'h'mm")) : "");
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

        productNameColumn.getStyleClass().add("left-aligned");
        priceColumn.getStyleClass().add("right-aligned");
    }

    private void initializeOrderDetailColumn() {
        // Add New button in header
        Button addOrderItemButton = new Button("Add New");
        addOrderItemButton.getStyleClass().add("header-button");
        addOrderItemButton.setPrefWidth(orderItemActionColumn.getPrefWidth());
        addOrderItemButton.setOnAction(event -> addOrderItem(event));
        orderItemActionColumn.setGraphic(addOrderItemButton);

        String orderStatus = orderStatusText.getText();
        if(orderStatus.equals("Finished") || orderStatus.equals("Paid") || orderStatus.equals("Canceled")){
            addOrderItemButton.setDisable(true);
        }

        // Set cell factory for action column
        orderItemActionColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                OrderItem orderItem = getTableView().getItems().get(getIndex());
                HBox actionBox = new HBox();
                actionBox.getStyleClass().add("action-hbox");

                FontAwesomeIconView editIcon = new FontAwesomeIconView(FontAwesomeIcon.PENCIL);
                editIcon.getStyleClass().add("action-icon");
                editIcon.setOnMouseClicked(e -> {
                    setCurrentOrderItemSelected(orderItem);
                    updateOrderItem(new ActionEvent());
                });

                actionBox.getChildren().add(editIcon);
                setGraphic(actionBox);
            }
        });

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

        // Remove or comment out these lines
        // promotionOrderItem.setCellValueFactory(new PropertyValueFactory<>("promotionName"));
        // promotionDiscountOrderItem.setCellValueFactory(new PropertyValueFactory<>("promotionDiscount"));
    }

    // Remove implement Initializable to take control over code flow
    public void initializeAllTables() {
        bookingPoolTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        orderItemsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        initializeBookingColumn();
        initializeOrderDetailColumn();
        loadInfo();
        loadBookings();
        loadOrderDetail();
    }

    public void addBooking(ActionEvent event) {
        if (orderStatusText.getText().equals("Paid")) {
            NotificationService.showNotification("Error!", "Cannot add booking with status Paid", NotificationStatus.Error);
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
            NotificationService.showNotification("Error!", "Cannot add Load Booking form !", NotificationStatus.Error);
        }
    }

    public void updateBooking(ActionEvent event) {
        if (orderStatusText.getText().equals("Paid")) {
            NotificationService.showNotification("Error!", "Cannot add booking with status Paid", NotificationStatus.Error);
            return;
        }
        // Lấy booking được chọn
        Booking selectedBooking = bookingPoolTable.getSelectionModel().getSelectedItem();
        // Kiểm tra xem có booking nào được chọn không
        if (selectedBooking == null) {
            NotificationService.showNotification("You haven't choose a Booking.", "Please select a Booking !", NotificationStatus.Warning);
            return;
        }

        // Kiểm tra trạng thái booking
        if ("Finish".equals(selectedBooking.getBookingStatus())) {
            System.out.println("Status finish");
            NotificationService.showNotification("Can't Update Status",
                    "Cannot update the status of a booking that is already finished.", NotificationStatus.Error);
            return;
        }

        if ("Playing".equals(selectedBooking.getBookingStatus())) {
            System.out.println("Status playing");
            NotificationService.showNotification("Can't Update Status",
                    "Cannot update the status of a booking that is already finished.", NotificationStatus.Error);
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
                        "Start playing on this table successfully.", NotificationStatus.Success);
                loadBookings(); // Tải lại danh sách booking sau khi cập nhật
                checkOrderStatus();

            } else {
                NotificationService.showNotification("Update Failed",
                        "Failed to update the booking status. Please try again.", NotificationStatus.Error);
            }
        }
    }

    public void deleteBooking(ActionEvent event) {
        if (orderStatusText.getText().equals("Paid")) {
            NotificationService.showNotification("Error", "Cannot add booking with status 'Paid'.", NotificationStatus.Error);
            return;
        }
        Booking selectedBooking = bookingPoolTable.getSelectionModel().getSelectedItem();

        if (selectedBooking == null) {
            NotificationService.showNotification("No Selection", "Please select a booking to delete.", NotificationStatus.Error);
            return;
        }
        if (selectedBooking.getBookingStatus().equals("Finish")) {
            NotificationService.showNotification("Can't Delete", "Bookings marked as 'finish' cannot be deleted", NotificationStatus.Error);
            return;
        }

        if (selectedBooking.getBookingStatus().equals("Playing")) {
            NotificationService.showNotification("Can't Delete", "Bookings marked as 'playing' cannot be deleted.", NotificationStatus.Error);
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
                NotificationService.showNotification("Success", "Booking deleted successfully.", NotificationStatus.Success);
                loadBookings();
            } else {
                NotificationService.showNotification("Error", "Failed to delete the booking.", NotificationStatus.Error);
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
            updateOrderItemController.setOrderItemList(orderItemsTable.getItems().stream().map(OrderItem::getProductName).toList());
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
                    BookingDAO.updateTableStatusAfterBooking(bookingId);
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
                    BookingDAO.cancelBooking(booking.getBookingId());
                    System.out.println("Đã hủy bàn " + booking.getBookingId() + "thành công");
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
        try {
            // Get confirmation from user
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Finish Order");
            confirmAlert.setHeaderText("Are you sure you want to finish this order?");
            confirmAlert.setContentText("This will finish all bookings and update the order status.");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Finish all bookings
                boolean finishAllSuccess = BookingDAO.finishOrder(this.orderID);

                if (finishAllSuccess) {
                    // Calculate total cost
                    double totalCost = OrderDAO.calculateOrderTotal(orderID);
                    // Update order status and total cost
                    boolean updateOrderSuccess = OrderDAO.updateOrderStatus(this.orderID, totalCost);

                    if (updateOrderSuccess) {
                        // Show success message
                        NotificationService.showNotification("Success", 
                            "Order has been finished successfully! Total cost: " + formatTotal(totalCost), 
                            NotificationStatus.Success);
                        
                        // Refresh the tables
                        loadBookings();
                        loadOrderDetail();
                        loadInfo();
                    } else {
                        NotificationService.showNotification("Error", "Failed to update order status.", NotificationStatus.Error);
                    }
                } else {
                    NotificationService.showNotification("Error", "Failed to finish all bookings.", NotificationStatus.Error);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            NotificationService.showNotification("Error", "Failed to finish order: " + e.getMessage(), NotificationStatus.Error);
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

    public void checkOrderStatus() {
        List<Booking> bookings = BookingDAO.getBookingByOrderId(orderID);
        for (Booking booking : bookings) {
            if (booking.getBookingStatus().equals("Playing")) {
                OrderDAO.updateStatusOrder(orderID, "Playing");
                System.out.println("Da chuyen order thanh playing");
            } else if (!booking.getBookingStatus().equals("Finish") && (booking.getBookingStatus().equals("Order"))) {
                OrderDAO.updateStatusOrder(orderID, "Order");
                System.out.println("Da chuyen order thanh order");

            }else if(booking.getBookingStatus().equals("Finish")){

                System.out.println("Da chuyen booking thanh Finish");
            }else{
                OrderDAO.updateStatusOrder(orderID, "Canceled");
                System.out.println("Da chuyen order thanh canceled");
            }
        }
    }

    @FXML
    public void cancelBooking(ActionEvent actionEvent) {
        Booking selectedBooking = bookingPoolTable.getSelectionModel().getSelectedItem();
        if (selectedBooking == null) {
            NotificationService.showNotification("Error Cancel Booking", "You haven't choose a booking to cancel !", NotificationStatus.Error);
            return;
        }

        boolean success = BookingDAO.cancelBooking(selectedBooking.getBookingId());
        if (success){
            BookingDAO.updateTableStatusAfterBooking(selectedBooking.getBookingId());
            NotificationService.showNotification("Cancel Booking Success", "Booking in Table : " + selectedBooking.getTableName() + " has been cancelled successfully!", NotificationStatus.Success);
            loadBookings();
        }
        else
            NotificationService.showNotification("Error Cancel Booking", "An unexpected error happens when cancelling this booking. Please try again later !", NotificationStatus.Error);
    }
    public void setBillNo(int billNo) {
        this.billNo = billNo;
        System.out.println("Bill No: " + billNo);
        billNoText.setText(String.valueOf(billNo));
    }

    // Getters and Setters for selected items
    public Booking getCurrentBookingSelected() {
        return currentBookingSelected;
    }

    public void setCurrentBookingSelected(Booking booking) {
        this.currentBookingSelected = booking;
    }

    public OrderItem getCurrentOrderItemSelected() {
        return currentOrderItemSelected;
    }

    public void setCurrentOrderItemSelected(OrderItem orderItem) {
        this.currentOrderItemSelected = orderItem;
    }
}
