package src.billiardsmanagement.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        // Tải layout chính (Main layout)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/main/main.fxml"));
        BorderPane root = loader.load();

        Scene scene = new Scene(root, 2000, 1000);
//        scene.getStylesheets().add(getClass().getResource("/src/billiardsmanagement/css/style.css").toExternalForm());
//        scene.getStylesheets().add(getClass().getResource("/src/billiardsmanagement/css/main.css").toExternalForm());
        primaryStage.setTitle("BILLIARDS MANAGEMENT");
        primaryStage.setScene(scene);

        primaryStage.setMaximized(true);
        primaryStage.show();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
