package src.billiardsmanagement.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Login extends Application {

    private static Stage primaryStage; // Store primary stage reference

    @Override
    public void start(Stage stage) {
        try {
            primaryStage = stage; // Assign primaryStage

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/users/login.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/src/billiardsmanagement/css/main.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/src/billiardsmanagement/css/style.css").toExternalForm());

            primaryStage.setTitle("Login");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage; // Getter method to access primary stage
    }

    public static void main(String[] args) {
        launch(args);
    }
}

