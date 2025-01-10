package src.billiardsmanagement.controller;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;
public class NavbarController implements Initializable {

    @FXML
    private Label exit;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        exit.setOnMouseClicked(e->{
            System.exit(0);
        });
    }
    public void home(ActionEvent actionEvent) {
    }

    public void dashboard(ActionEvent actionEvent) {
    }

    public void order(ActionEvent actionEvent) {
    }

    public void pool_table(ActionEvent actionEvent) {
    }

    public void item(ActionEvent actionEvent) {
    }

}
