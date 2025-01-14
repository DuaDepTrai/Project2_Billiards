package src.billiardsmanagement.controller.orders.bookings;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class BookingController {
    @FXML
    protected TableView bookingPoolTable;
    @FXML private TableColumn<?, String> tableNameColumn;
    @FXML private TableColumn<?, String> startTimeColumn;
    @FXML private TableColumn<?, String> endTimeColumn;
    @FXML private TableColumn<?, Double> timeplayColumn;
    @FXML private TableColumn<?, Double> priceColumn;
    @FXML private TableColumn<?, Double> costColumn;


}


