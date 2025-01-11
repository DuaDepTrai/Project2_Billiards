package src.billiardsmanagement.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class BookingsController {
    @FXML
    protected TableView bookingPoolTable;
    @FXML private TableColumn<?, String> tableName;
    @FXML private TableColumn<?, String> startTime;
    @FXML private TableColumn<?, String> endTime;
    @FXML private TableColumn<?, Double> timeplay;
    @FXML private TableColumn<?, Double> price;
    @FXML private TableColumn<?, Double> cost;
}


