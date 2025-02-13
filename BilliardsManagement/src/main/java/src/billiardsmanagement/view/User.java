package src.billiardsmanagement.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class User extends Application {
    double x,y;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/src/billiardsmanagement/users/users.fxml"));
    //        primaryStage.initStyle(StageStyle.UNDECORATED);

        root.setOnMousePressed((event->{
            x = event.getSceneX();
            y = event.getSceneY();
        }));

        root.setOnMouseDragged(event->{
            primaryStage.setX(event.getScreenX()-x);
            primaryStage.setY(event.getScreenY()-y);
        });

        primaryStage.setTitle("Users");
        primaryStage.setResizable(true);
        primaryStage.setScene(new Scene(root,1200,900));
        primaryStage.show();
    }
}
