package src.billiardsmanagement.controller.orders;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;
import javafx.scene.control.TableView;
import src.billiardsmanagement.controller.poolTables.PoolTableController;
import src.billiardsmanagement.dao.PoolTableDAO;
import src.billiardsmanagement.model.*;

public class BookingAndOrderTableListener {
    private final ScheduledService<Void> scheduler;
    private TableView<Order> orderTable;
    private ObservableList<Order> orderList;
    private ForEachOrderController forEachOrderController;
    private PoolTableDAO poolTableDAO = new PoolTableDAO();
    private PoolTableController poolTableController;
    private String cancelMultipleBookingInOrderNotification = "";
    private OrderController orderController;

    public BookingAndOrderTableListener() {
        scheduler = new ScheduledService<>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() {
                        Platform.runLater(BookingAndOrderTableListener.this::reloadData);
                        return null;
                    }
                };
            }
        };
        scheduler.setPeriod(Duration.seconds(60));
    }

    public void startListening() {
        scheduler.start();
    }

    public void stopListening() {
        scheduler.cancel();
    }

    private void reloadData(){
        forEachOrderController.initializeForEachOrderButtonsAndInformation();
        forEachOrderController.loadBookings();

        orderController.initializeOrderController();
    }

//    private void checkForNotifications() {
//        String query = "SELECT * FROM notifications WHERE processed = 0 AND task = ? ORDER BY created_at DESC";
//        String updateQuery = "UPDATE notifications SET processed = 1 WHERE notification_id = ?";
//
//        try (Connection connection = DatabaseConnection.getConnection();
//             PreparedStatement stmt = connection.prepareStatement(query);
//             PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
//
//            stmt.setString(1, String.valueOf(BookingTask.CANCEL_EXPIRED_BOOKING));
//            ResultSet rs = stmt.executeQuery();
//
//            if (rs.next()) {
//                int notificationId = rs.getInt("notification_id");
//                String message = rs.getString("message");
//                String task = rs.getString("task");
//                String taskCreatedAt = rs.getTimestamp("created_at").toLocalDateTime().toString();
//                Notification notification = new Notification(notificationId, BookingTask.valueOf(task), message);
//                System.out.println("*** From notifications table: notification [" + notificationId + "] -- Received notification: " + notification.getMessage() + " at " + taskCreatedAt);
//
//                List<Integer> orderIdList = new ArrayList<>();
//                Matcher matcher = Pattern.compile("order_id:(\\d+)").matcher(message);
//
//                while (matcher.find()) {
//                    orderIdList.add(Integer.parseInt(matcher.group(1)));
//                }
//
//                boolean foundDuplicate = false;
//                for (int i = 0; i < orderIdList.size() - 1; i++) {
//                    if (orderIdList.get(i).equals(orderIdList.get(i + 1))) {
//                        foundDuplicate = true;
//                        cancelMultipleBookingInOrderNotification = "All Ordered bookings have been canceled for Order Number : " + OrderDAO.getOrderBillNo(orderIdList.get(i));
//                        break;
//                    }
//                }
//
//                if (!foundDuplicate) {
//                    cancelMultipleBookingInOrderNotification = "";
//                }
//
//                updateStmt.setInt(1, notificationId);
//                updateStmt.executeUpdate();
//                reloadData(notification);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }

//    private void reloadData(Notification notification) {
//        if (notification.getTask() == BookingTask.CANCEL_EXPIRED_BOOKING) {
//            String message = notification.getMessage();
//
//            try {
//                Matcher matcher = Pattern.compile("booking_id:(\\d+).*?table_id:(\\d+).*?order_id:(\\d+)").matcher(message);
//                if (!matcher.find()) {
//                    throw new IllegalArgumentException("Invalid message format: " + message);
//                }
//
//                System.out.println("From BookingAndOrderTableListener: Using regex ..");
//
//                final int bookingId = Integer.parseInt(matcher.group(1));
//                final int tableId = Integer.parseInt(matcher.group(2));
//                final int orderId = Integer.parseInt(matcher.group(3));
//
//                final Order updatedOrder = OrderDAO.getOrderById(orderId);
//                final PoolTable affectedTable = poolTableDAO.getTableById(tableId);
//
//                if (updatedOrder != null && affectedTable != null && orderController != null) {
//                    System.out.println("From BookingAndOrderTableListener: Handling Order List and Table ..");
//
//                    TableView<Order> orderTable = orderController.getOrderTable();
//                    ObservableList<Order> orderList = orderController.getOrderList();
//
//                    Node virtualFlowNode = orderTable.lookup(".virtual-flow");
//                    double scrollPosition = virtualFlowNode instanceof VirtualFlow<?> ? ((VirtualFlow<?>) virtualFlowNode).getPosition() : 0;
//
//                    for (int i = 0; i < orderList.size(); i++) {
//                        if (orderList.get(i).getOrderId() == orderId) {
//                            orderList.set(i, updatedOrder);
//                            orderTable.getItems().set(i, updatedOrder);
//                            break;
//                        }
//                    }
//
//                    orderController.refreshPage(new ActionEvent());
//                    System.out.println("From BookingAndOrderTableListener: Refresh page ..");
//
//                    if (virtualFlowNode instanceof VirtualFlow<?>) {
//                        ((VirtualFlow<?>) virtualFlowNode).setPosition(scrollPosition);
//                    }
//
//                    if (forEachOrderController != null) {
//                        System.out.println("From BookingAndOrderTableListener: Refresh For Each View ..");
//
//                        forEachOrderController.initializeForEachOrderButtonsAndInformation();
//                        forEachOrderController.loadBookings();
//                    }
//                    else System.out.println("** Listener : forEachOrderController is null");
//
//                    int billNo = OrderDAO.getOrderBillNo(orderId);
//
//                    NotificationService.showNotification(
//                            cancelMultipleBookingInOrderNotification.isEmpty() ? "Booking Canceled" : "Multiple Booking Canceled",
//                            "All Ordered Bookings in Table " + affectedTable.getName() + " have been canceled in Order Number " + billNo,
//                            NotificationStatus.Warning
//                    );
//                    System.out.println("From BookingAndOrderTableListener: Show Notification ..");
//
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                System.out.println("Error - failed to process notification: " + e.getMessage());
//            }
//        }
//    }

    public void setForEachController(ForEachOrderController forEachOrderController) { this.forEachOrderController = forEachOrderController; }
    public void setPoolTableController(PoolTableController poolTableController) { this.poolTableController = poolTableController; }
    public void setOrderController(OrderController orderController) { this.orderController = orderController; }
}
