package src.billiardsmanagement.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


import java.io.IOException;
import java.util.Objects;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        // Tải layout chính (Main layout)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/main.fxml"));
        BorderPane root = loader.load();

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/src/billiardsmanagement/css/style.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/src/billiardsmanagement/css/main.css").toExternalForm());
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
