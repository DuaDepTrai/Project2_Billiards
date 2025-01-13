package src.billiardsmanagement.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class RentCueController {
    @FXML
    protected TableView rentCueTable;
    @FXML private TableColumn<?, String> productName;
    @FXML private TableColumn<?, String> startTime;
    @FXML private TableColumn<?, String> endTime;
    @FXML private TableColumn<?, Double> timeplay;
    @FXML private TableColumn<?, Double> price;
    @FXML private TableColumn<?, Double> cost;
}
