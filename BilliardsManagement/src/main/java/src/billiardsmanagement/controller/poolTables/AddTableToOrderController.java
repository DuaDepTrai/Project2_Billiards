package src.billiardsmanagement.controller.poolTables;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import src.billiardsmanagement.controller.orders.ForEachOrderController;
import src.billiardsmanagement.controller.orders.OrderController;
import src.billiardsmanagement.dao.BookingDAO;
import src.billiardsmanagement.dao.OrderDAO;
import src.billiardsmanagement.model.NotificationStatus;
import src.billiardsmanagement.model.Order;
import src.billiardsmanagement.service.NotificationService;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;

public class AddTableToOrderController implements Initializable {

    @FXML
    private TableView<Order> orderTable;

    @FXML
    private TableColumn<Order, Integer> sttColumn;
    @FXML
    private TableColumn<Order, String> customerNameColumn;
    @FXML
    private TableColumn<Order, String> orderStatusColumn;
    @FXML
    private TableColumn<Order, String> phoneCustomerColumn;
    @FXML
    private TableColumn<Order, String> nameTableColumn;
    @FXML
    private TableColumn<Order, Void> actionColumn; // Thêm khai báo này
    @FXML
    private TableColumn<Order, Date> dateColumn;

    private OrderDAO orderDAO = new OrderDAO();
    private int currentTableId = -1;

    private PoolTableController poolTableController;
    private StackPane tableContainer;
    private Popup chooseOrderTimePopup;
    private OrderController orderController;

    // Initialize the table columns
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        sttColumn.setCellValueFactory(param -> {
            TableView<?> tableView = sttColumn.getTableView();
            int totalRows = tableView.getItems().size();
            int index = tableView.getItems().indexOf(param.getValue());
            return new SimpleIntegerProperty(totalRows - index).asObject();
        });
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        orderStatusColumn.setCellValueFactory(new PropertyValueFactory<>("orderStatus"));
        phoneCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("customerPhone"));
        nameTableColumn.setCellValueFactory(new PropertyValueFactory<>("currentTableName"));
        nameTableColumn.setCellFactory(column -> new TableCell<Order, String>() {
            private final Text text = new Text();

            {
                text.wrappingWidthProperty().bind(nameTableColumn.widthProperty());
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    text.setText(item);
                    setGraphic(text);
                }
            }
        });
        // Bill No column
        sttColumn.setCellFactory(column -> {
            return new TableCell<Order, Integer>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                    } else {
                        setText(String.valueOf(item));
                        getStyleClass().add("bill-number");
                    }
                }
            };
        });

        // Table Name column
        nameTableColumn.setCellFactory(column -> {
            return new TableCell<Order, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                    } else {
                        setText(item);
                        getStyleClass().add("table-name-cell");
                    }
                }
            };
        });

        dateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        actionColumn.setCellFactory(column -> new TableCell<Order, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                Order order = getTableView().getItems().get(getIndex());

                // Create buttons for each row
                Button playButton = new Button("Play");
                Button orderButton = new Button("Order");

                // Set button size
                playButton.setPrefSize(90, 20);
                orderButton.setPrefSize(90, 20);

                // Play button action
                playButton.setOnAction(e -> {
                    addToOrder(order, "Playing", null);
                });

                // Order button action (opens chooseOrderTimePopup)
                orderButton.setOnAction(e -> {
                    Bounds bounds = orderButton.localToScreen(orderButton.getBoundsInLocal());
                    double xPos = bounds.getMinX();
                    double yPos = bounds.getMaxY(); // Position below the button

                    showChooseOrderTimePopup(xPos, yPos, (selectedDate, selectedTime) -> {
                        // Combine date and time correctly
                        LocalDateTime startTime = LocalDateTime.of(selectedDate, selectedTime);

                        // Format the LocalDateTime correctly
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                        String formattedDateTime = startTime.format(formatter);

                        System.out.println("Selected Order Time: " + formattedDateTime);

                        // Store the selected time in a variable or use it
                        addToOrder(order, "Order", startTime);
                    });
                });


                // HBox to hold buttons
                HBox actionBox = new HBox(15, playButton, orderButton);
                actionBox.setAlignment(Pos.CENTER);
                actionBox.setMaxWidth(Double.MAX_VALUE); // Full width of the column
                HBox.setHgrow(actionBox, Priority.ALWAYS); // Ensure it expands

                setGraphic(actionBox);
            }
        });


        loadOrderList();
    }

    private void showChooseOrderTimePopup(double xPos, double yPos, BiConsumer<LocalDate, LocalTime> onTimeSelected) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/pooltables/chooseOrderTime.fxml"));
            Parent root = loader.load();

            ChooseOrderTimeController controller = loader.getController();
            controller.setOnTimeSelected(onTimeSelected);

            Popup popup = new Popup();
            popup.getContent().add(root);

            // Hide popup when clicking outside
            popup.setAutoHide(true);
            popup.setX(xPos);
            popup.setY(yPos);

            // Apply fade-in effect
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.12), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

            // Close popup from controller when Confirm is clicked
            controller.setOnClosePopup(popup::hide);

            popup.show(orderTable.getScene().getWindow());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addToOrder(Order order, String bookingStatus, LocalDateTime orderStartTime) {
        if (currentTableId != -1) {
            boolean success = false;
            if (orderStartTime == null) {
                success = BookingDAO.addBookingToExistedOrder(order.getOrderId(), currentTableId, bookingStatus);
            } else {
                success = BookingDAO.addOrderedBookingToExistedOrder(order.getOrderId(), currentTableId, bookingStatus, orderStartTime);
            }

            if (success) {
                NotificationService.showNotification(
                        "Success",
                        "Table added to order successfully!",
                        NotificationStatus.Success
                );
                showForEachOrderView(order);
            } else {
                NotificationService.showNotification(
                        "Error",
                        "Failed to add table to order. Please try again.",
                        NotificationStatus.Error
                );
            }
        } else {
            NotificationService.showNotification(
                    "Error",
                    "No table selected. Please select a table first.",
                    NotificationStatus.Error
            );
        }
    }

    private void showForEachOrderView(Order order) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/orders/forEachOrder.fxml"));
            Parent root = loader.load();

            ForEachOrderController forEachOrderController = loader.getController();
            forEachOrderController.setOrderID(order.getOrderId());
            forEachOrderController.setCustomerID(order.getCustomerId());
            forEachOrderController.setForEachUserID(order.getUserId());
            forEachOrderController.setOrderDate(order.getOrderDate());
            forEachOrderController.setOrderController(this.orderController);

            int billNo = OrderDAO.getOrderBillNo(order.getOrderId());
            if (billNo != -1) forEachOrderController.setBillNo(billNo);
            forEachOrderController.setPoolTableController(this.poolTableController);
            if (order.getCustomerPhone() != null) {
                forEachOrderController.setInitialPhoneText(order.getCustomerPhone());
            }
            forEachOrderController.initializeAllTables();
            poolTableController.handleViewAllTables();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Order Information");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            NotificationService.showNotification("Error",
                    "Failed to open order information: " + e.getMessage(),
                    NotificationStatus.Error);
        }
    }

    private void loadOrderList() {
        List<Order> orders = orderDAO.getAllOrders();
        if (!orders.isEmpty()) {
            orders = orders.stream().filter(order -> order.getOrderStatus().equalsIgnoreCase("Playing") || order.getOrderStatus().equalsIgnoreCase("Order")).toList();
            orderTable.setItems(FXCollections.observableArrayList(orders));
        }
    }

    public void setCurrentTableId(int tableId) {
        this.currentTableId = tableId;
    }

    public void setTableContainer(StackPane tableContainer) {
        this.tableContainer = tableContainer;
    }

    public StackPane getTableContainer() {
        return this.tableContainer;
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
}