package src.billiardsmanagement.controller.database;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TableView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.skin.VirtualFlow;
import src.billiardsmanagement.controller.orders.ForEachOrderController;
import src.billiardsmanagement.controller.poolTables.PoolTableController;
import src.billiardsmanagement.dao.BookingDAO;
import src.billiardsmanagement.dao.OrderDAO;
import src.billiardsmanagement.dao.PoolTableDAO;
import src.billiardsmanagement.model.*;
import src.billiardsmanagement.service.NotificationService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BookingAndOrderTableListener {
    private Timer timer;

    private TableView<Order> orderTable;
    private ObservableList<Order> orderList;
    private ForEachOrderController forEachOrderController;
    private PoolTableDAO poolTableDAO = new PoolTableDAO();
    private PoolTableController poolTableController;

    private String cancelMultipleBookingInOrderNotification = "";

    public void startListening() {
        timer = new Timer(true); // Daemon thread
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkForNotifications();
            }
        }, 0, 15000); // Check every 15 seconds
    }

    private void checkForNotifications() {
        String query = "SELECT * FROM notifications WHERE processed = 0 AND task = ? ORDER BY created_at DESC";
        String updateQuery = "UPDATE notifications SET processed = 1 WHERE notification_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query);
             PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {

            stmt.setString(1, String.valueOf(BookingTask.CANCEL_EXPIRED_BOOKING));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int notificationId = rs.getInt("notification_id");
                String message = rs.getString("message");
                String task = rs.getString("task");
                Notification notification = new Notification(notificationId, BookingTask.valueOf(task), message);
                System.out.println("*** From notifications table: notification [" + notificationId + "] -- Received notification: " + notification.getMessage());

                // Extract order_id values from message using regex
                List<Integer> orderIdList = new ArrayList<>();
                Pattern pattern = Pattern.compile("order_id:(\\d+)");
                Matcher matcher = pattern.matcher(message);

                while (matcher.find()) {
                    orderIdList.add(Integer.parseInt(matcher.group(1)));
                }

                // Check for adjacent duplicate order_id occurrences
                boolean foundDuplicate = false;
                for (int i = 0; i < orderIdList.size() - 1; i++) {
                    if (orderIdList.get(i).equals(orderIdList.get(i + 1))) {
                        foundDuplicate = true;
                        cancelMultipleBookingInOrderNotification = "All Ordered bookings have been canceled for Order Number : " + OrderDAO.getOrderBillNo(orderIdList.get(i));
                        break;
                    }
                }

                // Reset if no duplicates found
                if (!foundDuplicate) {
                    cancelMultipleBookingInOrderNotification = "";
                }

                updateStmt.setInt(1, notificationId);
                updateStmt.executeUpdate();
                reloadData(notification);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void reloadData(Notification notification) {
        if (notification.getTask() == BookingTask.CANCEL_EXPIRED_BOOKING) {
            String message = notification.getMessage();

            try {
                // Extract IDs using regex
                Pattern pattern = Pattern.compile("booking_id:(\\d+).*?table_id:(\\d+).*?order_id:(\\d+)");
                Matcher matcher = pattern.matcher(message);

                if (!matcher.find()) {
                    throw new IllegalArgumentException("Invalid message format: " + message);
                }

                final int bookingId = Integer.parseInt(matcher.group(1));
                final int tableId = Integer.parseInt(matcher.group(2));
                final int orderId = Integer.parseInt(matcher.group(3));

                // Fetch required objects
                final Order updatedOrder = OrderDAO.getOrderById(orderId);
                final PoolTable affectedTable = poolTableDAO.getTableById(tableId);

                if (updatedOrder != null && affectedTable != null) {
                    javafx.application.Platform.runLater(() -> {
                        if (orderTable != null && orderList != null) {
                            // Save scroll position using VirtualFlow
                            Node virtualFlowNode = orderTable.lookup(".virtual-flow");
                            double scrollPosition = 0;
                            if (virtualFlowNode instanceof VirtualFlow<?>) {
                                scrollPosition = ((VirtualFlow<?>) virtualFlowNode).getPosition();
                            }

                            // Update order in the list
                            for (int i = 0; i < orderList.size(); i++) {
                                if (orderList.get(i).getOrderId() == orderId) {
                                    orderList.set(i, updatedOrder);
                                    break;
                                }
                            }

                            // Refresh table and restore scroll position
                            orderTable.refresh();
                            if (virtualFlowNode instanceof VirtualFlow<?>) {
                                ((VirtualFlow<?>) virtualFlowNode).setPosition(scrollPosition);
                            }

                            // Show notification
                            int billNo = OrderDAO.getOrderBillNo(orderId);
                            NotificationService.showNotification(
                                    "Booking Canceled",
                                    "Ordered Booking in Table " + affectedTable.getName() +
                                            " has been canceled in Order Number " + billNo,
                                    NotificationStatus.Warning
                            );

                            // Ensure forEachOrderController is not null before calling methods
                            if (forEachOrderController != null) {
                                forEachOrderController.initializeForEachOrderButtonsAndInformation();
                                forEachOrderController.loadBookings();
                            }
                        }
                    });
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                NotificationService.showNotification(
                        "Error",
                        "Invalid number format in notification message.",
                        NotificationStatus.Error
                );
            } catch (Exception e) {
                e.printStackTrace();
                NotificationService.showNotification(
                        "Error",
                        "Failed to process notification: " + e.getMessage(),
                        NotificationStatus.Error
                );
            }
        }
    }


    public void stopListening() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public void setOrderTable(TableView<Order> orderTable) {
        this.orderTable = orderTable;
    }

    public void setOrderList(ObservableList<Order> orderList) {
        this.orderList = orderList;
    }

    public void setForEachController(ForEachOrderController forEachOrderController) {
        this.forEachOrderController = forEachOrderController;
    }

    public void setPoolTableController(PoolTableController poolTableController) {
        this.poolTableController = poolTableController;
    }
}
