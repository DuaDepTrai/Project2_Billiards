package src.billiardsmanagement.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import src.billiardsmanagement.model.NotificationStatus;

public class NotificationController {
    @FXML
    private Label titleLabel;

    @FXML
    private Label messageLabel;
    private NotificationStatus notificationStatus;
    private String notificationColour;

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void setMessage(String message) {
        messageLabel.setText(message);
    }

    public void setNotificationColour(String colour){
        this.notificationColour = colour;
        setTitleColour(colour);
        setMessageColour(colour);
    }

    private void setTitleColour(String colour){
        titleLabel.setStyle("-fx-text-fill: "+colour);
    }

    private void setMessageColour(String colour){
        messageLabel.setStyle("-fx-text-fill: "+colour);
    }
}
