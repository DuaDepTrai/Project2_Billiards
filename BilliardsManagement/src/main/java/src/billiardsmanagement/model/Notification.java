package src.billiardsmanagement.model;

public class Notification {
    private int notificationId;
    private BookingTask task;
    private String message;

    // No-args constructor
    public Notification() {
    }

    // Full constructor
    public Notification(int notificationId, BookingTask task, String message) {
        this.notificationId = notificationId;
        this.task = task;
        this.message = message;
    }

    // Getter for notificationId
    public int getNotificationId() {
        return notificationId;
    }

    // Setter for notificationId
    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    // Getter for task
    public BookingTask getTask() {
        return task;
    }

    // Setter for task
    public void setTask(BookingTask task) {
        this.task = task;
    }

    // Getter for message
    public String getMessage() {
        return message;
    }

    // Setter for message
    public void setMessage(String message) {
        this.message = message;
    }
}