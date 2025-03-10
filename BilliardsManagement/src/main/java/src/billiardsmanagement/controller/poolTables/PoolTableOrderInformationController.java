package src.billiardsmanagement.controller.poolTables;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import src.billiardsmanagement.controller.orders.ForEachOrderController;
import src.billiardsmanagement.controller.orders.OrderController;
import src.billiardsmanagement.dao.BookingDAO;
import src.billiardsmanagement.dao.OrderDAO;
import src.billiardsmanagement.dao.PoolTableDAO;
import src.billiardsmanagement.model.*;
import src.billiardsmanagement.service.NotificationService;

import java.time.LocalDateTime;
import java.util.Optional;

public class PoolTableOrderInformationController {
    @FXML private Label orderInformationTitle;
    @FXML private Label statusText;
    @FXML private Text noOrderText;
    @FXML private Button showOrderButton;
    @FXML private Button createOrderBtn;
    @FXML private Button addToOrderBtn;
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

    public void initializeView() {
        orderInformationTitle.setText("Table "+currentTable.getName()+"'s Order Information");

        if (currentTable == null) return;

        statusText.setText(currentTable.getStatus());
        try {
            if(currentTable.getStatus().equalsIgnoreCase("Available")){
                createOrderBtn.setDisable(false);
                addToOrderBtn.setDisable(false);
//                finishBtn.setDisable(true);
//                playBtn.setDisable(true);
//                cancelBtn.setDisable(true);

                statusText.setStyle("fx-font-weight: bold; -fx-text-fill: "+greenColor);
                noOrderText.setVisible(true);
                showOrderButton.setVisible(false);

                createOrderBtn.setOnAction(e -> handleCreateOrder());
                addToOrderBtn.setOnAction(e -> handleAddToOrder());
            }
            else if(currentTable.getStatus().equalsIgnoreCase("Ordered")){
                createOrderBtn.setDisable(true);
                addToOrderBtn.setDisable(true);
//                finishBtn.setDisable(true);
//                playBtn.setDisable(false);
//                cancelBtn.setDisable(false);

                statusText.setStyle("fx-font-weight: bold; -fx-text-fill: "+orangeColor);
                noOrderText.setVisible(false);
                showOrderButton.setVisible(true);

                int orderId = BookingDAO.getTheLatestOrderByTableId(currentTable.getTableId());
                currentOrder = OrderDAO.getOrderById(orderId);

                showOrderButton.setOnAction(e -> showForEachOrderView(currentOrder));
//                playBtn.setOnAction(e -> handlePlay(orderId, currentTable.getTableId()));
//                cancelBtn.setOnAction(e -> handleCancel(orderId, currentTable.getTableId()));
            }
            else if(currentTable.getStatus().equalsIgnoreCase("Playing")){
                createOrderBtn.setDisable(true);
                addToOrderBtn.setDisable(true);
//                finishBtn.setDisable(false);
//                playBtn.setDisable(true);
//                cancelBtn.setDisable(true);

                statusText.setStyle("fx-font-weight: bold; -fx-text-fill: "+redColor);
                noOrderText.setVisible(false);
                showOrderButton.setVisible(true);

                int orderId = BookingDAO.getTheLatestOrderByTableId(currentTable.getTableId());
                currentOrder = OrderDAO.getOrderById(orderId);

                showOrderButton.setOnAction(e -> showForEachOrderView(currentOrder));
                System.out.println("Order ID: "+orderId + " Table ID: "+currentTable.getTableId());
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/orders/forEachOrder.fxml"));
            Parent root = loader.load();

            ForEachOrderController forEachOrderController = loader.getController();
            forEachOrderController.setOrderID(order.getOrderId());
            forEachOrderController.setCustomerID(order.getCustomerId());
            forEachOrderController.setForEachUserID(order.getUserId());
            if(order.getCustomerPhone()!=null){
                forEachOrderController.setInitialPhoneText(order.getCustomerPhone());
            }
            forEachOrderController.setBillNo(OrderController.getBillNumberCount());
            forEachOrderController.initializeAllTables();

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
        if(noOrderText.isVisible()){
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
            orderDAO.addOrder(order);

            Order newOrder = OrderDAO.getTheLatestOrderCreated();
            if (newOrder.getOrderId() > 0) {
                // Create a dialog to ask user's choice
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setHeaderText("Order created successfully!");
                alert.setContentText("Do you want to play on the table or just order?");

                ButtonType playButton = new ButtonType("Play on Table");
                ButtonType orderButton = new ButtonType("Order on Table");
                ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

                alert.getButtonTypes().setAll(playButton, orderButton, cancelButton);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == playButton) {
                    // User chose to play
                    Booking newBooking = new Booking();
                    newBooking.setOrderId(newOrder.getOrderId());
                    newBooking.setTableId(currentTable.getTableId());
                    newBooking.setStartTime(LocalDateTime.now());
                    newBooking.setBookingStatus("Playing");

                    BookingDAO.handleAddBooking(newBooking);

                    showForEachOrderView(newOrder);

                    NotificationService.showNotification("Success",
                            "You are now playing on the table!",
                            NotificationStatus.Success);
                } else if (result.isPresent() && result.get() == orderButton) {
                    // User chose to order
                    Booking newBooking = new Booking();
                    newBooking.setOrderId(newOrder.getOrderId());
                    newBooking.setTableId(currentTable.getTableId());
                    newBooking.setStartTime(LocalDateTime.now());
                    newBooking.setBookingStatus("Order"); // Changed to "Order"

                    BookingDAO.handleAddBooking(newBooking);

                    System.out.println("This Order ID = "+order.getOrderId());
                    showForEachOrderView(newOrder);
                    NotificationService.showNotification("Success",
                            "Your order is placed on the table!",
                            NotificationStatus.Success);
                } else {
                    // User canceled the action
                    NotificationService.showNotification("Info",
                            "Action canceled.",
                            NotificationStatus.Information);
                }

                closeWindow();
            } else {
                NotificationService.showNotification("Error",
                        "Failed to create order",
                        NotificationStatus.Error);
            }
        } catch (Exception e) {
            e.printStackTrace();
            NotificationService.showNotification("Error",
                    "Failed to create order: " + e.getMessage(),
                    NotificationStatus.Error);
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

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Order Information");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            closeWindow();
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
                closeWindow();
            } else {
                NotificationService.showNotification("Error Finish Order",
                        "An unexpected error happens when finishing this order. Please try again later !",
                        NotificationStatus.Error);
            }
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

            boolean updateSuccess = BookingDAO.updateBooking(bookingId,orderId,tableId,"Playing");

            // Kiểm tra kết quả cập nhật và hiển thị thông báo
            if (updateSuccess) {
                NotificationService.showNotification("Start Playing Successful",
                        "Start playing on this table successfully.", NotificationStatus.Success);
                closeWindow();
            } else {
                NotificationService.showNotification("Update Failed",
                        "Failed to update the booking status. Please try again.", NotificationStatus.Error);
            }
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
                closeWindow();
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

    private void closeWindow() {
        Stage stage = (Stage) statusText.getScene().getWindow();
        stage.close();
    }
}
