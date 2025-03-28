package src.billiardsmanagement.controller.orders;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import src.billiardsmanagement.controller.MainController;
import src.billiardsmanagement.controller.orders.bookings.AddBookingController;
import src.billiardsmanagement.controller.orders.items.AddOrderItemController;
import src.billiardsmanagement.controller.orders.items.UpdateOrderItemController;
import src.billiardsmanagement.controller.poolTables.PoolTableController;
import src.billiardsmanagement.dao.*;
import src.billiardsmanagement.model.*;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import src.billiardsmanagement.model.DatabaseConnection;
import src.billiardsmanagement.service.NotificationService;
import src.billiardsmanagement.model.NotificationStatus;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class ForEachOrderController {
    @FXML
    private AnchorPane forEachAnchorPane;
    @FXML
    private Text orderTotalCost;

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
    @FXML
    private Button btnBack;

    // Actions
    @FXML
    private TableColumn<Booking, Void> bookingActionColumn;

    @FXML
    private TableColumn<OrderItem, Void> orderItemActionColumn;

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
    private Text orderStatusText;

    @FXML
    private Text billNoText;

    @FXML
    private Text dateText;

    @FXML
    private Text staffNameText;

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
    private TableColumn<Booking, Double> totalColumn;

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
    private TableColumn<OrderItem, Double> totalOrderItemColumn;

    // @FXML
    // private TableColumn<OrderItem, Double> subTotalOrderItemColumn;

    // @FXML
    // private TableColumn<OrderItem, String> promotionOrderItem;

    // @FXML
    // private TableColumn<OrderItem, Double> promotionDiscountOrderItem;

    private MainController mainController; // Biến để lưu MainController
    private ChosenPage mainControllerChosenPage;
    private String orderPageChosen = "OrderPage";
    private String poolTablePageChosen = "PoolTablePage";

    private Popup forEachPopup = new Popup();
    private StackPane contentArea;

    public void setMainController(MainController mainController, ChosenPage mainControllerChosenPage) {
        this.mainControllerChosenPage = mainControllerChosenPage;
        this.mainController = mainController;
    }

    private final ObservableList<Booking> bookingList = FXCollections.observableArrayList();
    private final ObservableList<OrderItem> orderItemList = FXCollections.observableArrayList();

    private final BookingDAO bookingDAO = new BookingDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final OrderItemDAO orderItemDAO = new OrderItemDAO();
    private Connection conn = DatabaseConnection.getConnection();

    private int orderID = -1; // Default value
    private int userID;
    private int customerID;
    private int billNo;

    private Booking currentBookingSelected;
    private OrderItem currentOrderItemSelected;
    private AutoCompletionBinding<String> phoneAutoCompletion;
    private AutoCompletionBinding<String> customerNameAutoCompletion;

    private String initialPhoneText;
    private LocalDateTime orderDate;
    private Order currentOrder;

    //    @FXML private Button confirmUpdateDataCustomer;
    @FXML
    private Button confirmSaveCustomer;

    private PoolTable selectedTable;
    private PoolTableController poolTableController;
    private OrderController orderController;

    private List<Button> createdOrderItemButtons = new ArrayList<>();


    public void loadBookings() {
        // tam thoi comment lai de tranh vong lap goi ham
//        checkBookingStatus();
        List<Booking> bookings = BookingDAO.getBookingByOrderId(orderID);
        // Filter bookings to only show the selected table's bookings
//        if (selectedTable != null) {
//            bookings = bookings.stream()
//                    .filter(booking -> booking.getTableId() == selectedTable.getTableId())
//                    .collect(Collectors.toList());
//        }
        bookings.sort((b1, b2) -> b2.getBookingId() - b1.getBookingId());
        bookingList.clear();
        bookingList.addAll(bookings);
        bookingPoolTable.setItems(bookingList);
        System.out.println("From ForEachOrderController, loadBookings() has been called : " + bookings.size());
    }

    private void loadOrderDetail() {
        orderItemList.clear();
        List<OrderItem> items = OrderItemDAO.getForEachOrderItem(orderID);
        items.sort((i1, i2) -> i2.getOrderItemId() - i1.getOrderItemId());
        System.out.println(items.isEmpty()
                ? "Order Item list in ForEachOrderController, loadOrderDetail() don't have any element !"
                : "");
        orderItemList.addAll(items);

        orderItemsTable.getItems().clear();
        orderItemsTable.getItems().addAll(items);

        // Promotion-related code commented out
        /*
         * promotionOrderItem.setCellValueFactory(cellData -> {
         * OrderItem orderItem = cellData.getValue();
         * String promotionName = orderItem.getPromotionName();
         * return new SimpleStringProperty(promotionName != null ? promotionName : "");
         * });
         * promotionDiscountOrderItem.setCellValueFactory(cellData -> {
         * OrderItem orderItem = cellData.getValue();
         * Double discount = orderItem.getPromotionDiscount();
         * return new SimpleObjectProperty<>(discount != null ? discount : 0.0);
         * });
         */
    }

    private void initializeBookingColumn() {
        // Add New button in header
        // Button addBookingButton = new Button("Add New");
        // addBookingButton.getStyleClass().add("header-button");
        // addBookingButton.setPrefWidth(bookingActionColumn.getPrefWidth());
        // addBookingButton.setOnAction(event -> addBooking(event));
        // bookingActionColumn.setGraphic(addBookingButton);

        // Check Order Status
        String orderStatus = orderStatusText.getText();
        if (orderStatus.equals("Finished") || orderStatus.equals("Paid") || orderStatus.equals("Canceled")) {
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
                HBox actionBox = new HBox(10); // Optional: add spacing between icons
                actionBox.setAlignment(Pos.CENTER); // This centers the children horizontally
                actionBox.getStyleClass().add("action-hbox");

                switch (booking.getBookingStatus()) {
                    case "Ordered":
                        // Play Icon
                        FontAwesomeIconView playIcon = new FontAwesomeIconView(FontAwesomeIcon.PLAY);
                        playIcon.setStyle("-fx-font-family: 'FontAwesome';");
                        playIcon.prefWidth(14); // Set icon size
                        playIcon.prefHeight(14); // Set icon size
                        Button playButton = new Button();
                        playButton.setGraphic(playIcon);
                        playButton.setMinSize(17, 17); // Set button size
                        playButton.setAlignment(Pos.CENTER); // Center the icon in the button
                        playButton.setOnMouseClicked(e -> {
                            setCurrentBookingSelected(booking);
                            updateBooking(new ActionEvent());
                        });

                        // Cancel Icon
                        FontAwesomeIconView cancelIcon = new FontAwesomeIconView(FontAwesomeIcon.TIMES);
                        cancelIcon.setStyle("-fx-font-family: 'FontAwesome';");
                        cancelIcon.prefWidth(14);
                        cancelIcon.prefHeight(14);
                        Button cancelButton = new Button();
                        cancelButton.setGraphic(cancelIcon);
                        cancelButton.setMinSize(17, 17);
                        cancelButton.setAlignment(Pos.CENTER); // Center the icon in the button
                        cancelButton.setOnMouseClicked(e -> {
                            setCurrentBookingSelected(booking);
                            cancelBooking(new ActionEvent());
                        });

                        actionBox.getChildren().addAll(playButton, cancelButton);
                        break;

                    case "Playing":
                        // Stop Icon
                        FontAwesomeIconView stopIcon = new FontAwesomeIconView(FontAwesomeIcon.STOP);
                        stopIcon.setStyle("-fx-font-family: 'FontAwesome';");
                        stopIcon.prefWidth(14);
                        stopIcon.prefHeight(14);
                        Button stopButton = new Button();
                        stopButton.setGraphic(stopIcon);
                        stopButton.setMinSize(17, 17);
                        stopButton.setAlignment(Pos.CENTER); // Center the icon in the button
                        stopButton.setOnMouseClicked(e -> {
                            setCurrentBookingSelected(booking);
                            stopBooking(new ActionEvent());

                        });
                        actionBox.getChildren().add(stopButton);
                        break;

                    case "Canceled":
                        // Delete Icon
                        FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
                        deleteIcon.setStyle("-fx-font-family: 'FontAwesome';");
                        deleteIcon.prefWidth(14);
                        deleteIcon.prefHeight(14);
                        Button deleteButton = new Button();
                        deleteButton.setGraphic(deleteIcon);
                        deleteButton.setMinSize(17, 17);
                        deleteButton.setAlignment(Pos.CENTER); // Center the icon in the button
                        deleteButton.setOnMouseClicked(e -> {
                            setCurrentBookingSelected(booking);
                            deleteBooking(new ActionEvent());
                        });
                        actionBox.getChildren().add(deleteButton);
                        break;

                    case "Finish":
                        Text finishedText = new Text("Finished");
                        finishedText.getStyleClass().add("finished-text");
                        HBox.setHgrow(finishedText, Priority.ALWAYS); // This helps with centering
                        actionBox.getChildren().add(finishedText);
                        break;
                }

                setAlignment(Pos.CENTER); // This centers the entire cell content
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

        // subTotalColumn.setCellFactory(column -> new TableCell<>() {
        // private final DecimalFormat decimalFormat = new DecimalFormat("#,###");
        //
        // @Override
        // protected void updateItem(Double item, boolean empty) {
        // super.updateItem(item, empty);
        // setText((empty || item == null) ? null : decimalFormat.format(item));
        // }
        // });

        costColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
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
        // Button addOrderItemButton = new Button("Add New");
        // addOrderItemButton.getStyleClass().add("header-button");
        // addOrderItemButton.setPrefWidth(orderItemActionColumn.getPrefWidth());
        // addOrderItemButton.setOnAction(event -> addOrderItem(event));
        // orderItemActionColumn.setGraphic(addOrderItemButton);

        String orderStatus = orderStatusText.getText();
        if (orderStatus.equals("Finished") || orderStatus.equals("Paid") || orderStatus.equals("Canceled")) {
            addOrderItemButton.setDisable(true);
        }

        // Set cell factory for action column

        orderItemActionColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(null); // Remove any existing graphics
            }
        });

        Order order = OrderDAO.getOrderByIdStatic(this.orderID);
        if(order.getOrderStatus().equals(String.valueOf(OrderStatus.Finished)) || order.getOrderStatus().equals(String.valueOf(OrderStatus.Canceled)) || order.getOrderStatus().equals(String.valueOf(OrderStatus.Paid))){
            orderItemActionColumn.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                        return;
                    }

                    HBox actionBox = new HBox(5); // Added spacing between buttons
                    actionBox.setAlignment(Pos.CENTER);
                    actionBox.getStyleClass().add("action-hbox");

                    // Edit Icon
                    FontAwesomeIconView editIcon = new FontAwesomeIconView(FontAwesomeIcon.PENCIL);
                    editIcon.setStyle("-fx-font-family: 'FontAwesome';-fx-font-size: 14px;");
                    Button editButton = new Button();
                    editButton.setGraphic(editIcon);
                    editButton.setMinSize(17, 17);
                    editButton.setAlignment(Pos.CENTER);
                    editButton.setDisable(true); // Disabled to prevent interaction

                    // Reset / Return Icon
                    FontAwesomeIconView resetIcon = new FontAwesomeIconView(FontAwesomeIcon.REFRESH);
                    resetIcon.setStyle("-fx-font-family: 'FontAwesome';-fx-font-size: 14px;");
                    Button resetButton = new Button();
                    resetButton.setGraphic(resetIcon);
                    resetButton.setMinSize(17, 17);
                    resetButton.setAlignment(Pos.CENTER);
                    resetButton.setDisable(true); // Disabled to prevent interaction

                    actionBox.getChildren().addAll(editButton, resetButton);

                    setAlignment(Pos.CENTER);
                    setGraphic(actionBox);
                }
            });
        }
        else{
            orderItemActionColumn.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                        return;
                    }

                    OrderItem orderItem = getTableView().getItems().get(getIndex());
                    HBox actionBox = new HBox(5); // Added spacing between buttons
                    actionBox.setAlignment(Pos.CENTER);
                    actionBox.getStyleClass().add("action-hbox");

                    // Edit Icon
                    FontAwesomeIconView editIcon = new FontAwesomeIconView(FontAwesomeIcon.PENCIL);
                    // This is an example of how to fix if icons not showing.
                    // Just add : -fx-font-family: 'FontAwesome'; and everything gonna works fine.
                    editIcon.setStyle("-fx-font-family: 'FontAwesome';-fx-font-size: 14px;"); // Set icon size
                    Button editButton = new Button();
                    editButton.setGraphic(editIcon);
                    editButton.setMinSize(17, 17);
                    editButton.setAlignment(Pos.CENTER);
                    editButton.setOnMouseClicked(e -> {
                        setCurrentOrderItemSelected(orderItem);
                        updateOrderItem(new ActionEvent());
                    });

                    // Reset / Return Icon
                    FontAwesomeIconView resetIcon = new FontAwesomeIconView(FontAwesomeIcon.REFRESH);
                    resetIcon.setStyle("-fx-font-family: 'FontAwesome';-fx-font-size: 14px;");
                    Button resetButton = new Button();
                    resetButton.setGraphic(resetIcon);
                    resetButton.setMinSize(17, 17);
                    resetButton.setAlignment(Pos.CENTER);

                    resetButton.setOnMouseClicked(e -> {
                        setCurrentOrderItemSelected(orderItem);

                        VBox confirmationBox = new VBox(10);
                        confirmationBox.setAlignment(Pos.CENTER);
                        confirmationBox.setPadding(new Insets(15));

                        Label messageLabel = new Label("Are you sure you want to return this item?");
                        messageLabel.setWrapText(true);
                        messageLabel.setStyle("-fx-font-size: 14px;");

                        Button confirmButton = new Button("Confirm");
                        confirmButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-pref-width: 150.0;");
                        confirmButton.setOnAction(event -> {
                            returnOrderItem(orderItem);
                            hideForEachPopup();
                        });

                        confirmationBox.getChildren().addAll(messageLabel, confirmButton);

                        showForEachPopup(confirmationBox);
                    });

                    actionBox.getChildren().addAll(editButton, resetButton);

                    setAlignment(Pos.CENTER);
                    setGraphic(actionBox);
                }
            });
        }


        //
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

        totalOrderItemColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        totalOrderItemColumn.setSortType(TableColumn.SortType.ASCENDING);
        totalOrderItemColumn.setCellFactory(column -> new TableCell<OrderItem, Double>() {
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

        // subTotalOrderItemColumn.setCellFactory(column -> new TableCell<OrderItem,
        // Double>() {
        // @Override
        // protected void updateItem(Double item, boolean empty) {
        // super.updateItem(item, empty);
        // if (empty || item == null) {
        // setText("");
        // } else {
        // setText(String.format("%,d", Math.round(item)));
        // }
        // }
        // });

        // Remove or comment out these lines
        // promotionOrderItem.setCellValueFactory(new
        // PropertyValueFactory<>("promotionName"));
        // promotionDiscountOrderItem.setCellValueFactory(new
        // PropertyValueFactory<>("promotionDiscount"));
    }

    // Remove implement Initializable to take control over code flow
    public void initializeAllTables() {
        bookingPoolTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        orderItemsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        // Finish Order Button disable / enable

        initializeForEachOrderButtonsAndInformation();

        // Set "Tab" key Traversal logic
        customerText.addEventFilter(KeyEvent.KEY_PRESSED, this::handleTabTraversal);
        phoneText.addEventFilter(KeyEvent.KEY_PRESSED, this::handleTabTraversal);
        confirmSaveCustomer.addEventFilter(KeyEvent.KEY_PRESSED, this::handleTabTraversal);

        // Staff Name
        String staffName = OrderDAO.getStaffNameByOrderId(orderID);
        if (staffName == null) {
            System.out.println("Error : Currently, there is no user logged in !");
            staffNameText.setText("[ No staff logged in ! ]");
        } else if (staffName.isEmpty()) {
            System.out.println("Error : Currently, there is no user logged in !");
            staffNameText.setText("[ No staff logged in ! ]");
        } else {
            staffNameText.setText(staffName);
        }
        initializeBookingColumn();
        initializeOrderDetailColumn();
        loadInfo();
        loadBookings();
        loadOrderDetail();
        setupPhoneAutoCompletion();

        Order order = OrderDAO.getOrderByIdStatic(this.orderID);
        if(!order.getOrderStatus().equals("Finished") && !order.getOrderStatus().equals("Paid")){
            checkOrderStatus();
        }

        // Set current timestamp in dateText
        if (orderDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH'h'mm | dd-MM-yyyy");
            String formattedDateTime = orderDate.format(formatter);
            dateText.setText(formattedDateTime);
        }

        // If we have a selected table, create a booking for it
        if (selectedTable != null) {
            Booking newBooking = new Booking();
            newBooking.setOrderId(orderID);
            newBooking.setTableId(selectedTable.getTableId());
            newBooking.setTableName(selectedTable.getName());
            newBooking.setStartTime(LocalDateTime.now());
            newBooking.setBookingStatus("Playing");
            newBooking.setPriceTable(selectedTable.getPrice());

            // Add the booking
            bookingDAO.addBooking(newBooking);
            loadBookings(); // Refresh the bookings list
        }
    }

    private void handleTabTraversal(KeyEvent event) {
        if (event.getCode() == KeyCode.TAB) {
            if (event.isShiftDown()) { // Shift + Tab (reverse order)
                if (customerText.isFocused()) {
                    if (!confirmSaveCustomer.isDisabled()) {
                        confirmSaveCustomer.requestFocus();
                    } else {
                        phoneText.requestFocus();
                    }
                } else if (phoneText.isFocused()) {
                    customerText.requestFocus();
                } else if (confirmSaveCustomer.isFocused()) {
                    phoneText.requestFocus();
                }
            } else { // Normal Tab (forward order)
                if (customerText.isFocused()) {
                    phoneText.requestFocus();
                } else if (phoneText.isFocused()) {
                    if (!confirmSaveCustomer.isDisabled()) {
                        confirmSaveCustomer.requestFocus();
                    } else {
                        customerText.requestFocus();
                    }
                } else if (confirmSaveCustomer.isFocused()) {
                    customerText.requestFocus();
                }
            }
        }
    }

    public void initializeForEachOrderButtonsAndInformation() {
        List<Customer> customerList = customerDAO.getInfoCustomer(customerID);
        Order order = OrderDAO.getOrderById(orderID);
        currentOrder = order;

        String status = currentOrder != null ? currentOrder.getOrderStatus() : orderStatusText.getText();
        if (status.equalsIgnoreCase("Finished") || status.equalsIgnoreCase("Canceled")
                || status.equalsIgnoreCase("Paid")) {
            disableAllActionButtons();
            finishOrderButton.setDisable(true);
            addOrderItemButton.setDisable(true);
            addBookingButton.setDisable(true);
            customerText.setDisable(true);
            phoneText.setDisable(true);
            confirmSaveCustomer.setDisable(true);
        } else {
            finishOrderButton.setDisable(false);
            addOrderItemButton.setDisable(false);
            addBookingButton.setDisable(false);
            customerText.setDisable(false);
            phoneText.setDisable(false);
            confirmSaveCustomer.setDisable(false);
        }

        if (customerList != null && !customerList.isEmpty() && order != null) {
            double totalCost = order.getTotalCost();
            System.out.println("\033[1;34m" + "💰 Total Cost: " + totalCost + "\033[0m" + " 😄🎉");
            String orderStatus = order.getOrderStatus();

            Customer customer = customerList.get(0);
            customerText.setText(customer.getName());
            phoneText.setText(customer.getPhone());
            orderStatusText.setText(orderStatus);
            System.out.println("From ForEachController, initializeForEachOrderButtonsAndInformation() : Order : " + order);

            // setup total cost text
            switch (orderStatus) {
                case "Playing" -> orderTotalCost.setText("Calculating ...");
                case "Ordered" -> orderTotalCost.setText("Pending ...");
                case "Canceled" -> orderTotalCost.setText("No Charge");
                case "Finished", "Paid" ->
                        orderTotalCost.setText(totalCost > 0.0 ? formatTotal(totalCost) : "Zero Cost");
            }

//            if (order.getTotalCost() > 0.0) {
//                orderTotalCost.setText(formatTotal(order.getTotalCost()));
//                System.out.println("-- Formatted total = " + formatTotal(order.getTotalCost()));
//            }
            confirmSaveCustomer.setDisable(phoneText.getText().equalsIgnoreCase(initialPhoneText));

        }

        loadBookings();
        if (currentOrder.getOrderStatus().equalsIgnoreCase("Finished")) {
            disableAllActionButtons();
        }
        System.out.println("From ForEachOrderController, initializeForEachOrderButtonsAndInformation() has been called !");
    }


    private final AtomicBoolean isFirstFocus = new AtomicBoolean(true);

    private void setupPhoneAutoCompletion() {
        if (phoneAutoCompletion != null) {
            phoneAutoCompletion.dispose();
            phoneText.setOnKeyPressed(null); // Remove previous event listener
        }

        List<String> phoneNumberList = CustomerDAO.getAllPhoneNumbers();

        // Get customer list and prepare suggestions
        List<Customer> customers = customerDAO.getAllCustomers();
        List<String> suggestions = customers.stream()
                .map(c -> c.getPhone() + " - " + c.getName())
                .collect(Collectors.toList());

        // Bind auto-completion
        phoneAutoCompletion = TextFields.bindAutoCompletion(phoneText, suggestions);

        // Ensure only one focus listener exists
        phoneText.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && isFirstFocus.get()) {  // First focus event
                if (phoneText.getText() != null && !phoneText.getText().equalsIgnoreCase(initialPhoneText)) {
                    isFirstFocus.set(false);
                    return;
                }

                phoneText.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.BACK_SPACE) {
                        phoneText.clear();
                        phoneText.setOnKeyPressed(null); // Remove listener after first backspace
                        event.consume();
                    }
                });

                isFirstFocus.set(false);
            }
            if (!newValue) {
                isFirstFocus.set(true);
            }
        });

        phoneAutoCompletion.setHideOnEscape(true);
        phoneAutoCompletion.setOnAutoCompleted(autoCompletionEvent -> {
            confirmSaveCustomer.setDisable(true);
            if (phoneText.getText() != null && !phoneText.getText().isBlank() && !phoneText.getText().equalsIgnoreCase(initialPhoneText) && customerText.getText() != null && !customerText.getText().isBlank()) {
                updateCustomerInformation(new ActionEvent());
                autoCompletionEvent.consume();
            } else {
                NotificationService.showNotification("No Changes", "No changes were made in the customer information field.",
                        NotificationStatus.Warning);
            }
        });

        // Ensure only one text listener exists
        phoneText.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isBlank()) {
                confirmSaveCustomer.setDisable(true);
                return;
            }
            if (newValue.trim().equalsIgnoreCase(initialPhoneText) || phoneNumberList.contains(newValue.trim())) {
                confirmSaveCustomer.setDisable(true);
                return;
            } else {
                confirmSaveCustomer.setDisable(false);
            }

            String phone = newValue.split(" - ")[0];
            Customer customer = customerDAO.getCustomerByPhone(phone);
            if (customer != null) {
                customerText.setText(customer.getName());
                phoneText.setText(customer.getPhone());
            }
        });
    }

    //    public void addBooking(ActionEvent event) {
//        if (orderStatusText.getText().equals("Paid")) {
//            NotificationService.showNotification("Error!", "Cannot add booking with status Paid",
//                    NotificationStatus.Error);
//            return;
//        }
//        // Don't allow adding other tables if we have a selected table
//        if (selectedTable != null) {
//            NotificationService.showNotification("Error!",
//                    "Cannot add other tables to this order. Please create a new order for other tables.",
//                    NotificationStatus.Error);
//            return;
//        }
//        try {
//            FXMLLoader loader = new FXMLLoader(
//                    getClass().getResource("/src/billiardsmanagement/orders/bookings/addBooking.fxml"));
//            Parent root = loader.load();
//
//            AddBookingController addBookingController = loader.getController();
//            addBookingController.setOrderId(orderID);
//            addBookingController.setOrderTable(orderTable);
//            Stage stage = new Stage();
//            stage.setTitle("Add Booking");
//            stage.setScene(new Scene(root));
//            stage.showAndWait();
//
//            loadBookings();
//        } catch (IOException e) {
//            NotificationService.showNotification("Error!", "Cannot add Load Booking form !", NotificationStatus.Error);
//        }
//    }

    public boolean returnOrderItem(OrderItem orderItem) {
        boolean success = ProductDAO.replenishItem(orderItem.getProductName(), orderItem.getQuantity()) && OrderItemDAO.removeOrderItem(this.orderID, orderItem);;
        if (success) {
            NotificationService.showNotification("Success",
                    orderItem.getProductName() + " has been successfully returned.",
                    NotificationStatus.Success);
            loadOrderDetail();
        } else {
            System.err.println("❌ Error: Failed to return order item - " + orderItem.getProductName());
        }
        return success;
    }


    public void showForEachPopup(Pane content) {
        // Ensure the content is not null
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null");
        }

        // Initialize forEachPopup if it's null
        if (this.forEachPopup == null) {
            this.forEachPopup = new Popup();
        } else if (this.forEachPopup.isShowing()) {
            this.forEachPopup.setOnHidden(null);
            this.forEachPopup.hide();
        }

        Platform.runLater(() -> {
            StackPane contentPane = new StackPane();
            contentPane.setStyle("-fx-background-color: white; -fx-padding: 10;");
            contentPane.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, null, null)));
            contentPane.getChildren().add(content);

            // Clear existing content before adding new content
            forEachPopup.getContent().clear();
            forEachPopup.getContent().add(contentPane);

            Scene scene = orderItemsTable.getScene();
            double sceneWidth = scene.getWidth();
            double sceneHeight = scene.getHeight();

            double popupWidth = content.getPrefWidth();
            double popupHeight = content.getPrefHeight();

            double xPos = (sceneWidth - popupWidth) / 2;
            double yPos = (sceneHeight - popupHeight) / 2;

            forEachPopup.setX(xPos);
            forEachPopup.setY(yPos);

            FadeTransition fadeIn = new FadeTransition(javafx.util.Duration.seconds(0.16), contentPane);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            forEachPopup.setAutoHide(true);
            forEachPopup.setAutoFix(true);
            forEachPopup.show(scene.getWindow());

            fadeIn.play();
        });
    }

    public void addBooking(ActionEvent event) {
        if (orderStatusText.getText().equals("Paid")) {
            NotificationService.showNotification("Error!", "Cannot add booking with status Paid",
                    NotificationStatus.Error);
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/src/billiardsmanagement/orders/bookings/addBooking.fxml"));
            AnchorPane pane = loader.load();

            // Retrieve the controller before showing the popup
            AddBookingController addBookingController = loader.getController();
            addBookingController.setForEachPopup(this.forEachPopup);
            addBookingController.setOrderId(this.orderID);
            addBookingController.initializeAddBooking();

            // Show the Popup using the provided method
            showForEachPopup(pane);

            // Load bookings only when forEach Popup hidden to avoid multiple loading
            this.forEachPopup.setOnHidden(e -> {
                loadBookings();
                checkOrderStatus();
                this.forEachPopup.setOnHidden(null); // this line is very important
            });

        } catch (IOException e) {
            NotificationService.showNotification("Error!", "Cannot load Booking form!", NotificationStatus.Error);
        } catch (Exception e) {
            // Catch any other exceptions and log them
            e.printStackTrace();
            NotificationService.showNotification("Error!", "An unexpected error occurred!", NotificationStatus.Error);
        }
    }

    public void updateBooking(ActionEvent event) {
        if (orderStatusText.getText().equals("Paid")) {
            NotificationService.showNotification("Error!", "Cannot add booking with status Paid",
                    NotificationStatus.Error);
            return;
        }

        Booking selectedBooking = currentBookingSelected;
        if (selectedBooking == null) {
            NotificationService.showNotification("You haven't chosen a Booking.", "Please select a Booking !",
                    NotificationStatus.Warning);
            return;
        }

        if ("Finish".equals(selectedBooking.getBookingStatus())) {
            NotificationService.showNotification("Can't Update Status",
                    "Cannot update the status of a booking that is already finished.", NotificationStatus.Error);
            return;
        }

        if ("Playing".equals(selectedBooking.getBookingStatus())) {
            NotificationService.showNotification("Can't Update Status",
                    "Cannot update the status of a booking that is already started.", NotificationStatus.Error);
            return;
        }

        String newTableStatus = "Playing";
        String newBookingStatus = "playing";

        // Create a VBox for the popup content
        VBox confirmationPane = new VBox();
        confirmationPane.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-border-radius: 8;");
        confirmationPane.setSpacing(10);

        Label confirmationLabel = new Label("Confirm start playing on this table?");
        confirmationLabel.setStyle("-fx-font-size: 14px;");

        Button confirmButton = new Button("Confirm");
        confirmButton.setOnAction(e -> {
            boolean updateSuccess = BookingDAO.updateBooking(selectedBooking.getBookingId(),
                    selectedBooking.getOrderId(), selectedBooking.getTableId(), newTableStatus);

            if (updateSuccess) {
                NotificationService.showNotification("Start Playing Successful",
                        "Start playing on this table successfully.", NotificationStatus.Success);
            } else {
                System.err.println("😱💔 Update Failed: Failed to update the booking status. Please try again. 🤷‍♂️");
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> hideForEachPopup());

        HBox buttonContainer = new HBox(10, confirmButton, cancelButton);
        buttonContainer.setAlignment(Pos.CENTER);

        confirmationPane.getChildren().addAll(confirmationLabel, buttonContainer);
        confirmationPane.setAlignment(Pos.CENTER);

        forEachPopup.setOnHidden(ev -> {
            loadBookings();
            checkOrderStatus();
            initializeForEachOrderButtonsAndInformation();
            if(this.poolTableController != null) poolTableController.handleViewAllTables();
            forEachPopup.setOnHidden(null);
        });

        showForEachPopup(confirmationPane);
    }


    public void deleteBooking(ActionEvent event) {
        if (orderStatusText.getText().equals("Paid")) {
            NotificationService.showNotification("Error", "Cannot delete booking with status 'Paid'.", NotificationStatus.Error);
            return;
        }

        Booking selectedBooking = currentBookingSelected;

        if (selectedBooking == null) {
            NotificationService.showNotification("No Selection", "Please select a booking to delete.", NotificationStatus.Error);
            return;
        }

        if ("Finish".equals(selectedBooking.getBookingStatus())) {
            NotificationService.showNotification("Can't Delete", "Bookings marked as 'Finish' cannot be deleted.", NotificationStatus.Error);
            return;
        }

        if ("Playing".equals(selectedBooking.getBookingStatus())) {
            NotificationService.showNotification("Can't Delete", "Bookings marked as 'Playing' cannot be deleted.", NotificationStatus.Error);
            return;
        }

        // Create a VBox for the popup content
        VBox confirmationPane = new VBox();
        confirmationPane.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-border-radius: 8;");
        confirmationPane.setSpacing(10);

        Label confirmationLabel = new Label("Are you sure you want to delete this booking?");
        confirmationLabel.setStyle("-fx-font-size: 14px;");

        Button confirmButton = new Button("Confirm");
        confirmButton.setOnAction(e -> {
            boolean success = bookingDAO.deleteBooking(selectedBooking.getBookingId());

            if (success) {
                NotificationService.showNotification("Success", "Booking deleted successfully.", NotificationStatus.Success);
            } else {
                NotificationService.showNotification("Error", "Failed to delete the booking.", NotificationStatus.Error);
            }

            hideForEachPopup();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> hideForEachPopup());

        HBox buttonContainer = new HBox(10, confirmButton, cancelButton);
        buttonContainer.setAlignment(Pos.CENTER);

        confirmationPane.getChildren().addAll(confirmationLabel, buttonContainer);
        confirmationPane.setAlignment(Pos.CENTER);

        // Ensure data reloads after the popup is closed
        forEachPopup.setOnHidden(ev -> {
            loadBookings();
            if (poolTableController != null) poolTableController.handleViewAllTables();
            forEachPopup.setOnHidden(null);
        });

        showForEachPopup(confirmationPane);
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
            addOrderItemController.setForEachPopup(this.forEachPopup);

            // Show as a popup instead of a Stage
            showForEachPopup((Pane) root);

            forEachPopup.setOnHidden(e -> {
                loadOrderDetail();
                forEachPopup.setOnHidden(null);
            });
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

        OrderItem selectedItem = currentOrderItemSelected;
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
            updateOrderItemController
                    .setOrderItemList(orderItemsTable.getItems().stream().map(OrderItem::getProductName).toList());
            updateOrderItemController.setForEachPopup(this.forEachPopup);
            updateOrderItemController.initializeOrderItem();

            showForEachPopup((Pane) root);

            // Refresh the table after update
            forEachPopup.setOnHidden(e -> {
                loadOrderDetail();
                forEachPopup.setOnHidden(null);
            });
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

        Booking selectedBooking = currentBookingSelected;
        if (selectedBooking == null) {
            NotificationService.showNotification("Warning",
                    "Please select a booking to update.",
                    NotificationStatus.Warning);
            return;
        }

        String bookingStatus = selectedBooking.getBookingStatus();
        if (bookingStatus.equalsIgnoreCase("Ordered")) {
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

        // Create a popup
        VBox popupContent = new VBox();
        popupContent.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-border-radius: 8;");
        popupContent.setSpacing(10);
        popupContent.setAlignment(Pos.CENTER);

        Label confirmationLabel = new Label("Confirm stopping this booking?");
        confirmationLabel.setStyle("-fx-font-size: 14px;");

        Button confirmButton = new Button("Confirm");
        confirmButton.setOnAction(e -> {
            boolean success = BookingDAO.stopBooking(bookingId, startTime, poolTableId);
            if (success) {
                NotificationService.showNotification("Success",
                        "Booking has been stopped and updated.",
                        NotificationStatus.Success);
                BookingDAO.updateTableStatusAfterBooking(bookingId);
            } else {
                NotificationService.showNotification("Error",
                        "Failed to stop booking due to invalid data.",
                        NotificationStatus.Error);
            }
            hideForEachPopup();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> hideForEachPopup());

        HBox buttonContainer = new HBox(10, confirmButton, cancelButton);
        buttonContainer.setAlignment(Pos.CENTER);

        popupContent.getChildren().addAll(confirmationLabel, buttonContainer);

        // Clear existing listeners when popup closes
        forEachPopup.setOnHidden(ev -> {
            loadBookings();
            checkOrderStatus();
            initializeForEachOrderButtonsAndInformation();
            if (this.poolTableController != null) poolTableController.handleViewAllTables();
            forEachPopup.setOnHidden(null); // Clear event handlers
        });

        showForEachPopup(popupContent);
    }


    public void checkBookingStatus() {
//        int minutesLimit = 30;
//        List<Booking> bookings = BookingDAO.getBookingByOrderId(orderID); // Lấy danh sách booking
//
//        LocalDateTime now = LocalDateTime.now(); // Thời gian hiện tại
//
//        for (Booking booking : bookings) {
//            LocalDateTime bookingTime = booking.getStartTimeBooking(); // Lấy thời gian bắt đầu booking
//            if (bookingTime != null) { // Kiểm tra nếu booking có thời gian bắt đầu
//                long minutesPassed = Duration.between(bookingTime, now).toMinutes();
//                System.out.println("Thời gian chênh lệch: " + minutesPassed);
//                if (minutesPassed > minutesLimit && "Ordered".equals(booking.getBookingStatus())) {
//                    BookingDAO.cancelBooking(booking.getBookingId());
//                    System.out.println("Đã hủy bàn " + booking.getBookingId() + "thành công");
//                }
//            }
//        }
    }

    public void setCustomerID(int customerId) {
        this.customerID = customerId;
        if (customerId > 0) {
            loadInfo();
        }
    }

    private void loadInfo() {
        List<Customer> customerList = customerDAO.getInfoCustomer(customerID);
        Order order = OrderDAO.getOrderById(orderID);

        if (customerList != null && !customerList.isEmpty() && order != null) {
            Customer customer = customerList.get(0);
            customerText.setText(customer.getName());
            phoneText.setText(customer.getPhone());
            orderStatusText.setText(order.getOrderStatus());
            if (order.getTotalCost() != 0.0) {
                orderTotalCost.setText(formatTotal(order.getTotalCost()));
            }
        }
    }

    private String formatTotal(double total) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        return decimalFormat.format(total);
    }

//    public void disableAllActionButtons() {
//        Platform.runLater(() -> {
//            for (int rowIndex = 0; rowIndex < orderItemsTable.getItems().size(); rowIndex++) {
//                TableRow<OrderItem> row = getTableRowAt(rowIndex);
//                if (row != null) {
//                    List<Node> cells = row.lookupAll(".table-cell").stream().toList();
//                    if (cells.size() > 5) { // Ensure the column index exists (6th column → index 5)
//                        TableCell<OrderItem, Void> cell = (TableCell<OrderItem, Void>) cells.get(5);
//                        if (cell != null) {
//                            System.out.println("Disabling button in row " + rowIndex);
//                            HBox actionBox = (HBox) cell.getGraphic();
//                            if (actionBox != null) {
//                                for (Node buttonNode : actionBox.getChildren()) {
//                                    if (buttonNode instanceof Button) {
//                                        ((Button) buttonNode).setDisable(true);
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        });
//    }

    public void disableAllActionButtons() {
        // something here
    }


    // ✅ Method to Get the TableRow Correctly (Without Lookup Issues)
    private TableRow<OrderItem> getTableRowAt(int rowIndex) {
        for (Node node : orderItemsTable.lookupAll(".table-row-cell")) {
            if (node instanceof TableRow<?> row) {
                if (row.getIndex() == rowIndex) {
                    return (TableRow<OrderItem>) row;
                }
            }
        }
        return null;
    }


    public void finishOrder(ActionEvent event) {
        // Create a new VBox for the confirmation popup
        VBox confirmationPane = new VBox();
        confirmationPane.setStyle("-fx-background-color: white; -fx-padding: 20;");
        confirmationPane.setSpacing(10); // Space between the label and button

        // Create a label to ask for confirmation
        Label confirmationLabel = new Label("This action will finalize your order. \nDo you want to proceed ?");
        confirmationLabel.setStyle("-fx-font-size: 14px;");

        // Create a button for confirmation
        Button confirmButton = new Button("Confirm Finish Order");
        confirmButton.setPrefWidth(130.0);
        confirmButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");

// Create a cancel button
        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefWidth(60.0);
        cancelButton.setStyle("-fx-background-color: gray; -fx-text-fill: white;");

        HBox buttonBox = new HBox(confirmButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(10.0);

        cancelButton.setOnAction(e -> {
            hideForEachPopup();
        });

        confirmButton.setOnAction(e -> {
            try {
                // Logic to finish the order
                if (bookingPoolTable.getItems().isEmpty()) {
                    double totalCost = OrderDAO.calculateOrderTotal(orderID);
                    boolean updateOrderSuccess = OrderDAO.updateOrderStatus(this.orderID, totalCost);
                    if (updateOrderSuccess) {
                        // Log success to console
                        NotificationService.showNotification("Success", "Finish order successfully. There's no booking in this table.", NotificationStatus.Success);
                        checkOrderStatus();
                        initializeForEachOrderButtonsAndInformation();
                    } else {
                        // Log error to console with funny icon
                        System.err.println("😱 Error: Failed to finish order.");
                    }
                    hideForEachPopup();
                    initializeOrderDetailColumn();
                    return;
                }

                // Finish all bookings
                boolean finishAllSuccess = BookingDAO.finishAllBookings(this.orderID);
                if (finishAllSuccess) {
                    double totalCost = OrderDAO.calculateOrderTotal(orderID);
                    boolean updateOrderSuccess = OrderDAO.updateOrderStatus(this.orderID, totalCost);
                    if (updateOrderSuccess) {
                        // Log success to console
                        NotificationService.showNotification("Success", "Order has been finished successfully! Total cost: " + formatTotal(totalCost), NotificationStatus.Success);
                        checkOrderStatus();
                        initializeForEachOrderButtonsAndInformation();
                        loadBookings();
                        initializeOrderDetailColumn();
                        System.out.println("Order Status = "+orderStatusText.getText());
                        if (poolTableController != null) poolTableController.handleViewAllTables();
                    } else {
                        // Log error to console with funny icon
                        System.err.println("😱 Error: Failed to update order status.");
                    }
                } else {
                    // Log error to console with funny icon
                    System.err.println("😱 Error: Failed to finish all bookings.");
                }
                loadOrderList();
                hideForEachPopup();
                finishOrderButton.setDisable(true);
                addBookingButton.setDisable(true);
                addOrderItemButton.setDisable(true);
            } catch (Exception ex) {
                // Log the exception to console with a funny icon
                System.err.println("🤦‍♂️ Oops! Something went wrong: " + ex.getMessage());
                ex.printStackTrace(); // Still print the stack trace for debugging
            }
        });

        // Add the label and button to the VBox
        confirmationPane.getChildren().addAll(confirmationLabel, buttonBox);
        confirmationPane.setAlignment(Pos.CENTER);

        // Show the confirmation popup
        showForEachPopup(confirmationPane);
    }

    // Giả sử bạn có phương thức refreshOrderDetails để cập nhật giao diện

    public void setOrderTable(TableView<Order> orderTable) {
        this.orderTable = orderTable;
    }

    public void loadOrderList() {
        if (orderTable != null) {
            List<Order> orders = orderDAO.getAllOrders();
            orderTable.setItems(FXCollections.observableArrayList(orders));
        }
    }

    private ObservableValue<Integer> orderItemCall(TableColumn.CellDataFeatures<OrderItem, Integer> cellData) {
        // Lấy vị trí (index) của dòng hiện tại trong danh sách
        int index = orderItemsTable.getItems().indexOf(cellData.getValue()) + 1;
        return new SimpleIntegerProperty(index).asObject();
    }

    public void checkOrderStatus() {
        List<Booking> bookings = BookingDAO.getBookingByOrderId(orderID);
        Order order = OrderDAO.getOrderByIdStatic(orderID);

        if(order.getOrderStatus().equals("Paid") || order.getOrderStatus().equals("Canceled")){
            return;
        }

        if (bookings.size() == 1) {
            String bookingStatus = bookings.get(0).getBookingStatus();

            if (bookingStatus.equals(String.valueOf(BookingStatus.Canceled))) {
                OrderDAO.updateStatusOrder(orderID, String.valueOf(OrderStatus.Canceled));
                initializeForEachOrderButtonsAndInformation();
                initializeOrderDetailColumn();
                System.out.println("✅ From ForEachOrder: Order updated to Canceled; only 1 booking found.");
            } else if (bookingStatus.equals(String.valueOf(BookingStatus.Playing))) {
                OrderDAO.updateStatusOrder(orderID, String.valueOf(OrderStatus.Playing));
                initializeForEachOrderButtonsAndInformation();
                System.out.println("✅ From ForEachOrder: Order updated to Playing; only 1 booking found.");
            } else if (bookingStatus.equals(String.valueOf(BookingStatus.Ordered))) {
                OrderDAO.updateStatusOrder(orderID, String.valueOf(OrderStatus.Ordered));
                initializeForEachOrderButtonsAndInformation();
                System.out.println("✅ From ForEachOrder: Order updated to Ordered; only 1 booking found.");
            } else if (bookingStatus.equals(String.valueOf(BookingStatus.Finish))) {
                OrderDAO.updateStatusOrder(orderID, String.valueOf(OrderStatus.Finished));
                initializeForEachOrderButtonsAndInformation();
                initializeOrderDetailColumn();
            }
        }

        // Case: All bookings are canceled
        if (bookings.size() > 1) {
            if (bookings.stream()
                    .allMatch(booking -> String.valueOf(BookingStatus.Canceled).equals(booking.getBookingStatus()))) {
                OrderDAO.updateStatusOrder(orderID, String.valueOf(OrderStatus.Canceled));
                initializeForEachOrderButtonsAndInformation();
                initializeOrderDetailColumn();
                System.out.println("✅ From ForEachOrder: Order updated to Canceled; all bookings are canceled.");
            } else if (bookings.stream().anyMatch(booking -> booking.getBookingStatus().equals(String.valueOf(BookingStatus.Playing)))) {
                OrderDAO.updateStatusOrder(orderID, String.valueOf(OrderStatus.Playing));
                initializeForEachOrderButtonsAndInformation();
                System.out.println("✅ From ForEachOrder: Order updated to Playing ; there's booking remain playing.");
            } else if (bookings.stream().anyMatch(booking -> booking.getBookingStatus().equals(String.valueOf(BookingStatus.Ordered)))) {
                OrderDAO.updateStatusOrder(orderID, String.valueOf(OrderStatus.Ordered));
                initializeForEachOrderButtonsAndInformation();
                System.out.println("✅ From ForEachOrder: Order updated to Playing ; there's booking remain playing.");
            } else if (bookings.stream().anyMatch(booking -> booking.getBookingStatus().equals(String.valueOf(BookingStatus.Finish)))) {
                OrderDAO.updateStatusOrder(orderID, String.valueOf(OrderStatus.Finished));
                initializeForEachOrderButtonsAndInformation();
                initializeOrderDetailColumn();
                System.out.println("✅ From ForEachOrder: Order updated to Playing ; there's booking remain playing.");
            }
        }

        if(order.getOrderStatus().equals("Finished")){
            boolean finishAllSuccess =  BookingDAO.finishAllBookings(this.orderID);
            double totalCost = OrderDAO.calculateOrderTotal(orderID);
            boolean updateOrderSuccess = OrderDAO.updateOrderStatus(this.orderID, totalCost);

            if(finishAllSuccess && updateOrderSuccess && totalCost > 0.0){
                initializeForEachOrderButtonsAndInformation();
                loadBookings();
                loadOrderList();
                initializeOrderDetailColumn();
            }
        }
    }

    @FXML
    public void cancelBooking(ActionEvent actionEvent) {
        Booking selectedBooking = currentBookingSelected;
        if (selectedBooking == null) {
            NotificationService.showNotification("Error Cancel Booking", "You haven't chosen a booking to cancel!", NotificationStatus.Error);
            return;
        }

        // Create a VBox for the popup content
        VBox confirmationPane = new VBox();
        confirmationPane.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-border-radius: 8;");
        confirmationPane.setSpacing(10);

        Label confirmationLabel = new Label("Are you sure you want to cancel this booking?");
        confirmationLabel.setStyle("-fx-font-size: 14px;");

        Button confirmButton = new Button("Confirm");
        confirmButton.setOnAction(e -> {
            System.out.println("From ForEachOrderController : Cancel Booking Called");
            boolean success = BookingDAO.cancelBooking(selectedBooking.getBookingId());

            if (success) {
                NotificationService.showNotification("Cancel Booking Success",
                        "Booking in Table: " + selectedBooking.getTableName() + " has been cancelled successfully!",
                        NotificationStatus.Success);

                loadBookings();
                if (poolTableController != null) poolTableController.handleViewAllTables();

                if (orderController == null) {
                    System.out.println("❌ From ForEachOrderController: orderController is null. Cannot call loadOrderList()");
                } else {
                    orderController.loadOrderList();
                    System.out.println("✅ From ForEachOrderController, cancelBooking(): loadOrderList() called from orderController non-null");
                    System.out.println("✅ From ForEachOrderController, cancelBooking(): checkOrderStatus() called");
                }
            } else {
                NotificationService.showNotification("Error Cancel Booking",
                        "An unexpected error happened while cancelling this booking. Please try again later!",
                        NotificationStatus.Error);
            }

            hideForEachPopup();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> hideForEachPopup());

        HBox buttonContainer = new HBox(10, confirmButton, cancelButton);
        buttonContainer.setAlignment(Pos.CENTER);

        confirmationPane.getChildren().addAll(confirmationLabel, buttonContainer);
        confirmationPane.setAlignment(Pos.CENTER);

        // Ensure actions are run after the popup is closed
        forEachPopup.setOnHidden(ev -> {
            checkOrderStatus();
            loadBookings();
            if (poolTableController != null) poolTableController.handleViewAllTables();
            forEachPopup.setOnHidden(null);
        });

        showForEachPopup(confirmationPane);
    }


    public void setBillNo(int billNo) {
        this.billNo = billNo;
        System.out.println("Bill No: " + billNo);
        billNoText.setText(String.valueOf(billNo));
    }

    @FXML
    public void saveCustomer(ActionEvent actionEvent) {
        try {
            CustomerDAO customerDAO = new CustomerDAO();
            // Get input from fields
            String customerName = customerText.getText();
            String customerPhone = phoneText.getText();

            // Validate input data
            if (customerName == null || customerName.trim().isEmpty()) {
                throw new IllegalArgumentException("Please enter the customer's name.");
            }
            if (customerPhone == null || customerPhone.trim().isEmpty()) {
                throw new IllegalArgumentException("Please enter the customer's phone number.");
            }

            // Validate phone number format (10 digits)
            if (!customerPhone.matches("^0(3[2-9]|5[2689]|7[0-9]|8[1-9]|9[0-9])\\d{7}")) {
                throw new IllegalArgumentException("Phone number must be 10 digits.");
            }

            // Check if the phone number already exists in the database
            if (customerDAO.isPhoneExists(customerPhone)) {
                throw new IllegalArgumentException("This phone number already exists.");
            }

            // Create a Customer object
            Customer customer = new Customer();
            customer.setName(customerName);
            customer.setPhone(customerPhone);

            // Add customer to the database
            customerDAO.addCustomer(customer);

            // Get the customer ID
            Integer customerId = customer.getCustomerId();
            if (customerId == null) {
                throw new IllegalStateException("Failed to retrieve the customer ID after adding.");
            }

            // Show success message
            NotificationService.showNotification("Success", "Customer added successfully!", NotificationStatus.Success);
            updateCustomerInformation(new ActionEvent());
        } catch (IllegalArgumentException e) {
            // Show validation error
            NotificationService.showNotification("Error", e.getMessage(), NotificationStatus.Error);
        } catch (Exception e) {
            e.printStackTrace();
            // Show unexpected error message
            NotificationService.showNotification("Error", "An error occurred while saving the customer. Please try again.", NotificationStatus.Error);
        }
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

    // Confirm Customer Phone + Name change
    public boolean updateCustomerInformation(ActionEvent actionEvent) {
        try {
            String phoneNumber = phoneText.getText();
            if (initialPhoneText != null && initialPhoneText.equalsIgnoreCase(phoneNumber)) {
                NotificationService.showNotification("No Changes", "No changes were made in the customer information field.",
                        NotificationStatus.Warning);
            }
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                NotificationService.showNotification("Error", "Phone number is required", NotificationStatus.Error);
                return false;
            }

            Integer customerID = CustomerDAO.getCustomerIdByPhone(phoneNumber);
            if (customerID == null) {
                NotificationService.showNotification("Error", "Customer not found for the provided phone number", NotificationStatus.Error);
                return false;
            }

            boolean success = orderDAO.updateOrder(orderID, customerID);
            if (success) {
                NotificationService.showNotification("Success", "Order updated successfully", NotificationStatus.Success);
                initialPhoneText = phoneNumber;
                confirmSaveCustomer.setDisable(true);
                loadOrderList();
                return true;
            } else {
                NotificationService.showNotification("Error", "Failed to update order", NotificationStatus.Error);
            }
        } catch (Exception e) {
            e.printStackTrace();
            NotificationService.showNotification("Error", "Failed to update order", NotificationStatus.Error);
        }
        return false;
    }
//    public void ActionEvent actionEvent) {
//        Booking bookingselected = bookingPoolTable.getSelectionModel().getSelectedItem();
//        OrderItem orderItemselected = orderItemsTable.getSelectionModel().getSelectedItem();
//
//        if (bookingselected == null && orderItemselected == null) {
//            try {
//                String phoneNumber = phoneText.getText();
//                if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
//                    showAlert(Alert.AlertType.ERROR, "Error", "Phone number is required");
//                    return;
//                }
//                int customerID = CustomerDAO.getCustomerIdByPhone(phoneNumber);
//                boolean success = orderDAO.orderID, customerID);
//                if (success) {
//                    showAlert(Alert.AlertType.INFORMATION, "Success", "Order updated successfully");
//                    loadOrderList();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update order");
//            }
//        }
//    }

    public void setSelectedTable(PoolTable table) {
        this.selectedTable = table;
    }

    public int getForEachUserID() {
        return this.userID;
    }

    public void setForEachUserID(int userID) {
        this.userID = userID;
    }

    public void setInitialPhoneText(String initialPhoneText) {
        this.initialPhoneText = initialPhoneText;
    }

    public void setCurrentOrder(Order order) {
        this.currentOrder = order;
    }

    public Order getCurrentOrder() {
        return this.currentOrder;
    }

    @FXML
    public void goBack(ActionEvent actionEvent) {
        try {
            if (mainController != null) {
                if (mainControllerChosenPage == ChosenPage.ORDERS)
                    mainController.showOrdersPage();
                if (mainControllerChosenPage == ChosenPage.POOLTABLES)
                    mainController.showPoolTablePage();
            } else
                System.out.println("From ForEachOrderController : mainController is not set !");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("From ForEachOrderController : Failed to navigate back !");
        }
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDateTime getOrderDate() {
        return this.orderDate;
    }

    public void setPoolTableController(PoolTableController poolTableController) {
        this.poolTableController = poolTableController;
    }

    public PoolTableController getPoolTableController() {
        return this.poolTableController;
    }

    public void setOrderController(OrderController orderController) {
        this.orderController = orderController;
    }

    public OrderController getOrderController() {
        return this.orderController;
    }

    public void hideForEachPopup() {
        this.forEachPopup.hide();
    }

    public void setMainControllerContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    public TableView<Booking> getBookingTable() {
        return this.bookingPoolTable;
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
        if (orderID > 0) {
            loadBookings();
            loadOrderDetail();
        } else {
            NotificationService.showNotification("Invalid Order ID", "The provided Order ID is invalid.",
                    NotificationStatus.Error);
        }
    }

    public int getOrderID() {
        return this.orderID;
    }
}
