package src.billiardsmanagement.controller.orders.items;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class OrderItemController {
    @FXML
    protected TableView orderItemsTable;
    @FXML private TableColumn<?, String> productName;
    @FXML private TableColumn<?, Integer> quantity;
    @FXML private TableColumn<?, Double> price;
    @FXML private TableColumn<?, Double> cost;
}
