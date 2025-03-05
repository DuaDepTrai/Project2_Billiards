package src.billiardsmanagement.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Report extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/reports/orders_stats.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Scene scene = new Scene(root, 800, 600);
        Scene scene = new Scene(root, 1000, 750);


        scene.getStylesheets().add(getClass().getResource("/src/billiardsmanagement/css/style.css").toExternalForm());

        primaryStage.setTitle("BILLIARDS MANAGEMENT");
        primaryStage.setScene(scene);

        primaryStage.setMaximized(true);
        primaryStage.show();
        primaryStage.show();
    }
}
