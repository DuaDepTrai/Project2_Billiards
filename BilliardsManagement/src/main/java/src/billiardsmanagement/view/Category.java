package src.billiardsmanagement.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Category extends Application {
    double x,y;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/src/billiardsmanagement/category/category.fxml"));
        primaryStage.setTitle("Category List");
        primaryStage.setScene(new Scene(root, 1000, 750));
        primaryStage.show();
    }

}
