package src.billiardsmanagement.controller.poolTables;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import src.billiardsmanagement.controller.MainController;
import src.billiardsmanagement.controller.orders.ForEachOrderController;
import src.billiardsmanagement.controller.orders.OrderController;
import src.billiardsmanagement.controller.poolTables.ChooseOrderTimeController;
import src.billiardsmanagement.dao.BookingDAO;
import src.billiardsmanagement.dao.OrderDAO;
import src.billiardsmanagement.dao.PoolTableDAO;
import src.billiardsmanagement.model.*;
import src.billiardsmanagement.service.NotificationService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.BiConsumer;

public class PoolTableOrderInformationController {
    @FXML
    private Label orderInformationTitle;
    @FXML
    private Label statusText;
    @FXML
    private Text noOrderText;
    @FXML
    private Button showOrderButton;
    @FXML
    private Button createOrderBtn;
    @FXML
    private Button addToOrderBtn;
//    @FXML private Button finishBtn;
//    @FXML private Button playBtn;
//    @FXML private Button cancelBtn;

    private PoolTable currentTable;
    private Order currentOrder;
    private OrderDAO orderDAO = new OrderDAO();
    private PoolTableDAO poolTableDAO = new PoolTableDAO();

    private String greenColor = "#04FF00";
    private String redColor = "#C21E00";
    private String orangeColor = "#FFA500";

    private PoolTableController poolTableController;
    private StackPane tablesContainer;
    private Popup chooseOrderTimePopup;
    private Popup choosePlayOrderPopup;
    private OrderController orderController;

    private MainController mainController;
    private ForEachOrderController forEachOrderController;
    private Parent forEachRoot;

    public void initializeView() {
        orderInformationTitle.setText("Table " + currentTable.getName() + "'s Order Information");

        if (currentTable == null) return;

        statusText.setText(currentTable.getStatus());
        try {
            if (currentTable.getStatus().equalsIgnoreCase("Available")) {
                createOrderBtn.setDisable(false);
                addToOrderBtn.setDisable(false);
//                finishBtn.setDisable(true);
//                playBtn.setDisable(true);
//                cancelBtn.setDisable(true);

                statusText.setStyle("fx-font-weight: bold; -fx-text-fill: " + greenColor);
                noOrderText.setVisible(true);
                showOrderButton.setVisible(false);

//                createOrderBtn.setOnAction(e -> handleCreateOrder());
//                addToOrderBtn.setOnAction(e -> handleAddToOrder());
            } else if (currentTable.getStatus().equalsIgnoreCase("Ordered")) {
                createOrderBtn.setDisable(true);
                addToOrderBtn.setDisable(true);
//                finishBtn.setDisable(true);
//                playBtn.setDisable(false);
//                cancelBtn.setDisable(false);

                statusText.setStyle("fx-font-weight: bold; -fx-text-fill: " + orangeColor);
                noOrderText.setVisible(false);
                showOrderButton.setVisible(true);

                int orderId = BookingDAO.getTheLatestOrderByTableId(currentTable.getTableId());
                currentOrder = OrderDAO.getOrderById(orderId);

//                showOrderButton.setOnAction(e -> showForEachOrderView(currentOrder));
//                playBtn.setOnAction(e -> handlePlay(orderId, currentTable.getTableId()));
//                cancelBtn.setOnAction(e -> handleCancel(orderId, currentTable.getTableId()));
            } else if (currentTable.getStatus().equalsIgnoreCase("Playing")) {
                createOrderBtn.setDisable(true);
                addToOrderBtn.setDisable(true);
//                finishBtn.setDisable(false);
//                playBtn.setDisable(true);
//                cancelBtn.setDisable(true);

                statusText.setStyle("fx-font-weight: bold; -fx-text-fill: " + redColor);
                noOrderText.setVisible(false);
                showOrderButton.setVisible(true);

                int orderId = BookingDAO.getTheLatestOrderByTableId(currentTable.getTableId());
                currentOrder = OrderDAO.getOrderById(orderId);

//                showOrderButton.setOnAction(e -> showForEachOrderView(currentOrder));
                System.out.println("Order ID: " + orderId + " Table ID: " + currentTable.getTableId());
//                finishBtn.setOnAction(e -> handleFinish(orderId, currentTable.getTableId()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            NotificationService.showNotification("Error",
                    "Failed to check order status: " + e.getMessage(),
                    NotificationStatus.Error);
        }

    }

    private void showForEachOrderView(Order order) {
        try {
            if(forEachRoot == null || forEachOrderController == null){
                if(mainController!=null){
                    mainController.initializeAllControllers();
                }
            }

            forEachOrderController.setOrderID(order.getOrderId());
            forEachOrderController.setCustomerID(order.getCustomerId());
            forEachOrderController.setForEachUserID(order.getUserId());
            forEachOrderController.setOrderDate(order.getOrderDate());
            forEachOrderController.setOrderController(this.orderController);
            forEachOrderController.setMainController(this.mainController, ChosenPage.POOLTABLES);
            int billNo = OrderDAO.getOrderBillNo(order.getOrderId());
            if (billNo != -1) forEachOrderController.setBillNo(billNo);
            forEachOrderController.setPoolTableController(this.poolTableController);
            if (order.getCustomerPhone() != null) {
                forEachOrderController.setInitialPhoneText(order.getCustomerPhone());
            }
            forEachOrderController.initializeAllTables();

            if(mainController!=null){
                StackPane contentArea = mainController.getContentArea();
                contentArea.getChildren().clear();
                contentArea.getChildren().add(forEachRoot);
            }

            poolTableController.handleViewAllTables();
        } catch (Exception e) {
            e.printStackTrace();
            NotificationService.showNotification("Error",
                    "Failed to open order information: " + e.getMessage(),
                    NotificationStatus.Error);
        }
    }

    public void setPoolTable(PoolTable table) {
        this.currentTable = table;
    }

//    private void updateUI() {
//        if (currentTable == null) return;
//
//        // Update status text
//        statusText.setText(currentTable.getStatus());
//
//        // Check for existing order
//        try {
//            // You'll need to implement this method in OrderDAO
//            int orderId = BookingDAO.getTheLatestOrderByTableId(currentTable.getTableId());
//            Order order = OrderDAO.getOrderById(orderId);
//
//            if (order != null) {
//                currentOrder = order;
//                noOrderText.setVisible(false);
//                showOrderButton.setVisible(true);
//            } else {
//                noOrderText.setVisible(true);
//                showOrderButton.setVisible(false);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            NotificationService.showNotification("Error",
//                    "Failed to check order status: " + e.getMessage(),
//                    NotificationStatus.Error);
//        }
//
//        // Update button states based on table status
//        updateButtonStates();
//    }

    private void updateButtonStates() {
        String status = currentTable.getStatus();
        if (noOrderText.isVisible()) {
            createOrderBtn.setDisable(false);
            addToOrderBtn.setDisable(false);
//            finishBtn.setDisable(true);
//            playBtn.setDisable(true);
//            cancelBtn.setDisable(true);
            return;
        }
        switch (status) {
            case "Available":
                createOrderBtn.setDisable(false);
                addToOrderBtn.setDisable(false);
//                finishBtn.setDisable(true);
//                playBtn.setDisable(true);
//                cancelBtn.setDisable(true);
                break;
            case "Ordered":
                createOrderBtn.setDisable(true);
                addToOrderBtn.setDisable(true);
//                finishBtn.setDisable(true);
//                playBtn.setDisable(false);
//                cancelBtn.setDisable(false);
                break;
            case "Playing":
                createOrderBtn.setDisable(true);
                addToOrderBtn.setDisable(true);
//                finishBtn.setDisable(false);
//                playBtn.setDisable(true);
//                cancelBtn.setDisable(true);
                break;
        }
    }

    @FXML
    private void handleCreateOrder() {
        try {
            // Implement create order logic
            Order order = new Order();
            order.setUserId(UserSession.getInstance().getUserId());
            order.setCustomerId(1);
            order.setOrderDate(LocalDateTime.now());

            if (true) {
                // Create a StackPane to hold the custom dialog
                StackPane stackPane = new StackPane();
                stackPane.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-color: gray; -fx-border-width: 1;");

                Label header = new Label("Choose Play or Order on this table.");
                Label content = new Label("Do you want to play on the table or just order?");

                Button playButtonNode = new Button("Play on Table");
                Button orderButtonNode = new Button("Order on Table");
                Button cancelButtonNode = new Button("Cancel");

                VBox dialogContent = new VBox(10, header, content, playButtonNode, orderButtonNode, cancelButtonNode);
                dialogContent.setAlignment(Pos.CENTER);
                stackPane.getChildren().add(dialogContent);

                poolTableController.showPoolPopup(tablesContainer, stackPane);

                // Handle button clicks manually
                playButtonNode.setOnAction(event -> {
                    orderDAO.addOrder(order);
                    Order newOrder = OrderDAO.getTheLatestOrderCreated();

                    Booking newBooking = new Booking();
                    newBooking.setOrderId(newOrder.getOrderId());
                    newBooking.setTableId(currentTable.getTableId());
                    newBooking.setStartTime(LocalDateTime.now());
                    newBooking.setBookingStatus("Playing");

                    BookingDAO.handleAddBooking(newBooking);
                    NotificationService.showNotification("Success", "You are now playing on the table!", NotificationStatus.Success);

                    showForEachOrderView(newOrder);

                    if (choosePlayOrderPopup != null && chooseOrderTimePopup.isShowing())
                        chooseOrderTimePopup.hide(); // Close` popup
                });

                orderButtonNode.setOnAction(event -> {
                    Bounds bounds = orderButtonNode.localToScreen(orderButtonNode.getBoundsInLocal());
                    double xPos = bounds.getMinX();
                    double yPos = bounds.getMaxY(); // Position below the button

                    showChooseOrderTimePopup(xPos, yPos, (selectedDate, selectedTime) -> {
                        // Combine selected date and time into LocalDateTime
                        LocalDateTime startTime = LocalDateTime.of(selectedDate, selectedTime);

                        // Print for debugging
                        System.out.println("Selected Order Time: " + startTime);

                        // Process the order
                        orderDAO.addOrder(order);
                        Order newOrder = OrderDAO.getTheLatestOrderCreated();

                        // Create a new booking
                        Booking newBooking = new Booking();
                        newBooking.setOrderId(newOrder.getOrderId());
                        newBooking.setTableId(currentTable.getTableId());
                        newBooking.setStartTime(startTime);
                        newBooking.setBookingStatus("Ordered");

                        // Insert booking into the database
                        BookingDAO.handleAddBooking(newBooking);

                        // Show success notification
                        System.out.println("This Order ID = " + order.getOrderId());
                        NotificationService.showNotification("Success", "Your order is placed on the table!", NotificationStatus.Success);

                        // Update UI
                        showForEachOrderView(newOrder);

                        // Close pool table popup if open
                        if (poolTableController != null && poolTableController.getCurrentPoolPopup().isShowing()) {
                            poolTableController.hidePoolPopup();
                        }
                    });

                });

                cancelButtonNode.setOnAction(event -> {
                    NotificationService.showNotification("Info", "Action canceled.", NotificationStatus.Information);
                    if (poolTableController != null && poolTableController.getCurrentPoolPopup().isShowing())
                        poolTableController.hidePoolPopup();
                });
            } else {
                NotificationService.showNotification("Error", "Failed to create order", NotificationStatus.Error);
            }
        } catch (Exception e) {
            e.printStackTrace();
            NotificationService.showNotification("Error", "Failed to create order: " + e.getMessage(), NotificationStatus.Error);
        }
    }


//    public void showChoosePlayOrderPopup(Scene scene, Parent content, double xPos, double yPos) {
//        this.choosePlayOrderPopup = new Popup();
//        StackPane contentPane = new StackPane();
//        contentPane.setStyle("-fx-background-color: white; -fx-padding: 10;");
//
//        contentPane.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, null, null)));
//        contentPane.getChildren().add(content);
//
//        choosePlayOrderPopup.getContent().add(contentPane);
//
//        choosePlayOrderPopup.setX(xPos);
//        choosePlayOrderPopup.setY(yPos);
//
//        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.12), contentPane);
//        fadeIn.setFromValue(0);
//        fadeIn.setToValue(1);
//
//        choosePlayOrderPopup.setAutoHide(true);
//        choosePlayOrderPopup.setAutoFix(true);
//        choosePlayOrderPopup.show(scene.getWindow());
//
//        fadeIn.play();
//    }

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

            popup.show(orderInformationTitle.getScene().getWindow());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddToOrder() {
        try {
            // Implement add to order logic
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/pooltables/addTableToOrder.fxml"));
            Parent root = loader.load();

            AddTableToOrderController controller = loader.getController();
            controller.setCurrentTableId(currentTable.getTableId());
            controller.setPoolTableController(this.poolTableController);
            controller.setTableContainer(this.tablesContainer);
            controller.setOrderController(this.orderController);
            controller.setMainController(this.mainController);

            // set ForEach
            controller.setForEachOrderController(this.forEachOrderController);
            controller.setForEachRoot(this.forEachRoot);
            controller.initializeAddTableToOrderController();

            if (poolTableController != null) poolTableController.showPoolPopup(tablesContainer, root);

        } catch (Exception e) {
            e.printStackTrace();
            NotificationService.showNotification("Error",
                    "Failed to add to order: " + e.getMessage(),
                    NotificationStatus.Error);
        }
    }

    private void handleFinish(int orderId, int tableId) {
        try {
            // Implement finish logic
            int bookingId = BookingDAO.getBookingIdByOrderIdAndTableId(orderId, tableId);
            boolean success = BookingDAO.handleFinishBooking(bookingId, tableId);

            if (success) {
                NotificationService.showNotification("Finish Order Success",
                        "Order in table " + currentTable.getName() + " has been finished successfully.",
                        NotificationStatus.Success);

            } else {
                NotificationService.showNotification("Error Finish Order",
                        "An unexpected error happens when finishing this order. Please try again later !",
                        NotificationStatus.Error);
            }
            poolTableController.hidePoolPopup();
        } catch (Exception e) {
            e.printStackTrace();
            NotificationService.showNotification("Error",
                    "Failed to finish table: " + e.getMessage(),
                    NotificationStatus.Error);
        }
    }

    private void handlePlay(int orderId, int tableId) {
        try {
            // Implement play logic
            int bookingId = BookingDAO.getBookingIdByOrderIdAndTableId(orderId, tableId);

            boolean updateSuccess = BookingDAO.updateBooking(bookingId, orderId, tableId, "Playing");

            // Kiểm tra kết quả cập nhật và hiển thị thông báo
            if (updateSuccess) {
                NotificationService.showNotification("Start Playing Successful",
                        "Start playing on this table successfully.", NotificationStatus.Success);

            } else {
                NotificationService.showNotification("Update Failed",
                        "Failed to update the booking status. Please try again.", NotificationStatus.Error);
            }
            poolTableController.hidePoolPopup();
        } catch (Exception e) {
            e.printStackTrace();
            NotificationService.showNotification("Error",
                    "Failed to update table status: " + e.getMessage(),
                    NotificationStatus.Error);
        }
    }

    private void handleCancel(int orderId, int tableId) {
        try {
            // Implement cancel logic
            // Calculate total, based on price and time

            int bookingId = BookingDAO.getBookingIdByOrderIdAndTableId(orderId, tableId);
            boolean success = BookingDAO.handleCancelBooking(bookingId, tableId);
            if (success) {
                BookingDAO.updateTableStatusAfterBooking(bookingId);
                NotificationService.showNotification("Cancel Booking Success",
                        "Booking in table " + currentTable.getName() + " has been cancelled successfully.",
                        NotificationStatus.Success);
                poolTableController.hidePoolPopup();
            } else {
                NotificationService.showNotification("Error Cancel Booking",
                        "An unexpected error happens when cancelling this booking. Please try again later !",
                        NotificationStatus.Error);
            }
        } catch (Exception e) {
            e.printStackTrace();
            NotificationService.showNotification("Error",
                    "Failed to cancel order: " + e.getMessage(),
                    NotificationStatus.Error);
        }
    }


    public void setPoolTableController(PoolTableController poolTableController) {
        this.poolTableController = poolTableController;
    }

    public PoolTableController getPoolTableController() {
        return poolTableController;
    }

    public StackPane getTableContainer() {
        return this.tablesContainer;
    }

    public void setTablesContainer(StackPane tablesContainer) {
        this.tablesContainer = tablesContainer;
    }

    public void setOrderController(OrderController orderController) {
        this.orderController = orderController;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setForEachOrderController(ForEachOrderController forEachOrderController) {
        this.forEachOrderController = forEachOrderController;
    }

    public void setForEachRoot(Parent forEachRoot) {
        this.forEachRoot = forEachRoot;
    }
}
