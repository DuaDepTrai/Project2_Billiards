package src.billiardsmanagement.controller.database;

import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import src.billiardsmanagement.model.Booking;
import src.billiardsmanagement.model.DatabaseConnection;
import src.billiardsmanagement.model.Notification;
import src.billiardsmanagement.model.Order;

import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;

public class BookingAndOrderTableListener {
    private Timer timer;
    private final String cancelExpiredBookingTask = "Cancel Expired Booking";

    private TableView<Order> orderTable;
    private ObservableList<Order> orderList;

    public void startListening() {
        timer = new Timer(true); // Daemon thread
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkForNotifications();
            }
        }, 0, 30000); // Check every 30 seconds
    }

    private void checkForNotifications() {
        String query = "SELECT * FROM notifications WHERE processed = 0 AND task = ? ORDER BY created_at DESC LIMIT 1";
        String updateQuery = "UPDATE notifications SET processed = 1 WHERE notification_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query);
             PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {

            stmt.setString(1, cancelExpiredBookingTask);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int notificationId = rs.getInt("notification_id");
                String message = rs.getString("message");
                String task = rs.getString("task");
                Notification notification = new Notification(notificationId, task, message);
                System.out.println("From notifications table: Received notification: " + notification.getMessage());

                updateStmt.setInt(1, notificationId);
                updateStmt.executeUpdate();
                reloadData(notification);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void reloadData(Notification notification) {

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
}
