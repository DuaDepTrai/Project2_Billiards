package src.billiardsmanagement.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class Order extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/orders/demo.fxml"));
        Parent root = loader.load();
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
