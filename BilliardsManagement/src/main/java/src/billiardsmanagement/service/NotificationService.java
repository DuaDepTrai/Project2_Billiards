package src.billiardsmanagement.service;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.controlsfx.control.NotificationPane;
import src.billiardsmanagement.controller.NotificationController;
import src.billiardsmanagement.model.NotificationStatus;

public class NotificationService {

    public static void showNotification(String title, String message, NotificationStatus status) {
        try {
            // Tải FXML của thông báo
            FXMLLoader loader = new FXMLLoader(NotificationService.class.getResource("/src/billiardsmanagement/notification/notification.fxml"));
            Parent root = loader.load();

            // Thiết lập tiêu đề và nội dung cho thông báo
            NotificationController controller = loader.getController();
            controller.setTitle(title + " ");
            controller.setMessage(message);

            String colour;
            switch (status) {
                case Error:
                    colour = "#DB2B3D"; // Màu đỏ
                    break;
                case Success:
                    colour = "#28BE8E"; // Màu xanh lục
                    break;
                case Warning:
                    colour = "#FF9D23"; // Màu cam
                    break;
                case Information:
                    colour = "#333333"; // Màu xám đậm
                    break;
                default:
                    colour = "#000000"; // Màu đen mặc định
                    break;
            }
            controller.setNotificationColour(colour);

            // Tạo một Stage mới cho thông báo
            Stage notificationStage = new Stage();
            notificationStage.initStyle(StageStyle.TRANSPARENT); // Không viền và trong suốt
            notificationStage.initModality(Modality.NONE); // Không chặn các cửa sổ khác

            // Tạo Scene với nền trong suốt
            Scene scene = new Scene(root);
            scene.setFill(null); // Nền trong suốt
            notificationStage.setScene(scene);

            // Tính toán vị trí xuất hiện của thông báo (giữa phía trên màn hình)
//            double screenWidth = Screen.getPrimary().getBounds().getWidth();
//            double notificationWidth = 700; // Chiều rộng của thông báo
//            System.out.println("Screen W = " + screenWidth);
//            System.out.println("Noti w = " + notificationWidth);
//            double xPos = (screenWidth - notificationWidth) / 2;
//            double yPos = -root.prefHeight(-1); // Bắt đầu ngoài màn hình
            // Lấy kích thước màn hình
            double screenWidth = Screen.getPrimary().getBounds().getWidth();
            double screenHeight = Screen.getPrimary().getBounds().getHeight();


            root.layoutBoundsProperty().addListener((obs, oldValue, newValue) -> {
                double notificationWidth;
                double notificationHeight;
                if (newValue == null) {
                    notificationWidth = root.prefWidth(-1);
                    notificationHeight = root.prefHeight(-1);
                } else {
                    notificationWidth = newValue.getWidth();  // Get new width
                    notificationHeight = newValue.getHeight(); // Get new height
                }

                System.out.println("Updated Size: " + notificationWidth + " x " + notificationHeight);
                double xPos;
                double yPos;

                if (status == NotificationStatus.Error) {
                    xPos = (screenWidth - notificationWidth) / 2; // Center horizontally
                    yPos = 10; // Top position
                } else {
                    xPos = screenWidth - notificationWidth - 10; // Right position
                    yPos = screenHeight - notificationHeight - 55 - 10; // Bottom position
                }

                notificationStage.setX(xPos);
                notificationStage.setY(yPos);
            });

            // setAlwaysOnTop
            notificationStage.setAlwaysOnTop(true);

            if (status == NotificationStatus.Error || status == NotificationStatus.Warning) {
                TranslateTransition slideDown = new TranslateTransition(Duration.millis(350), root);
                slideDown.setFromY(-150); // Start off-screen
                slideDown.setToY(0); // Stop at top-center
                slideDown.setInterpolator(Interpolator.EASE_OUT);
                slideDown.play();
            } else {
                // Slide in from right for other statuses
                double startX = screenWidth;
                double endX = 0;
                System.out.println("startX = " + startX);
                System.out.println("endX = " + endX);
                TranslateTransition slideIn = new TranslateTransition(Duration.millis(350), root);
                slideIn.setFromX(startX);
                slideIn.setToX(endX);
                slideIn.setInterpolator(Interpolator.EASE_OUT);
                slideIn.play();
            }

            // Hiệu ứng mờ dần sau 3 giây
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), root);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setDelay(Duration.seconds(3));
            fadeOut.setOnFinished(event -> notificationStage.close());

            // Chạy các hiệu ứng
//            slideDown.play();
            fadeOut.play();
            notificationStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


//
//import javafx.animation.FadeTransition;
//import javafx.animation.Interpolator;
//import javafx.animation.TranslateTransition;
//import javafx.application.Platform;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.scene.layout.Pane;
//import javafx.stage.*;
//import javafx.util.Duration;
//import src.billiardsmanagement.controller.NotificationController;
//import src.billiardsmanagement.view.Main;
//
//public class NotificationService {
//
//    public static void showNotification(Window owner,String title, String message) {
//        try {
//            FXMLLoader loader = new FXMLLoader(NotificationService.class.getResource("/src/billiardsmanagement/notification/notification.fxml"));
//            Parent root = loader.load();
//
//            NotificationController controller = loader.getController();
//            controller.setTitle(title);
//            controller.setMessage(message);
//
//            Popup popup = new Popup();
//            popup.getContent().add(root);
//            popup.setAutoHide(true);
//            popup.setOpacity(1);
//            popup.
//
//            // Calculate the center-top position
//            // Calculate the center-top position
//            double screenWidth = Screen.getPrimary().getBounds().getWidth();
//            double popupWidth = 300; // Set your popup width
//            double xPos = (screenWidth - popupWidth) / 2;
//            double yPos = -popup.getHeight(); // Start off-screen
//
//            popup.show(owner, xPos, yPos);
//
//            // Animate the popup sliding down into view
//            TranslateTransition slideDown = new TranslateTransition(Duration.millis(350), popup.getContent().get(0));
//            slideDown.setFromY(yPos*2);
//            slideDown.setToY(6);
//            slideDown.setInterpolator(Interpolator.EASE_OUT);
//            slideDown.play();
//
//            // Fade out after 3 seconds
//            FadeTransition fadeTransition = new FadeTransition(Duration.millis(300), root);
//            fadeTransition.setFromValue(1.0);
//            fadeTransition.setToValue(0.0);
//            fadeTransition.setDelay(Duration.millis(2500));
//            fadeTransition.setOnFinished(event -> popup.hide());
//
//            slideDown.play();
//            fadeTransition.play();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static Stage getPrimaryStage() {
//        return Main.getPrimaryStage();
//    }
//}