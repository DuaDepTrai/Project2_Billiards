package src.billiardsmanagement.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class MainController {

    @FXML
    private Pane centerPane;
    @FXML
    private BorderPane mainLayout;
    // Tham chiếu tới BorderPane chính (được
    public Pane getCenterPane() {
        return centerPane;
    }

    @FXML
    public void initialize() throws IOException, IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/navbar.fxml"));
        Parent navbar = loader.load();

        NavbarController navbarController = loader.getController();
        navbarController.getClass(mainLayout);

        OrderController orderController = new OrderController();
        orderController.getClass(mainLayout);
        mainLayout.setLeft(navbar);
    }
}
