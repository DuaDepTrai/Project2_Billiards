package src.billiardsmanagement.controller.orders;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import src.billiardsmanagement.model.Bill;
import src.billiardsmanagement.model.BillService;
import src.billiardsmanagement.model.OrderItem;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

public class PaymentController {
    private int orderID;

    @FXML
    private Label customerNameLabel;
    @FXML
    private Label customerPhoneLabel;
    @FXML
    private Label totalCostLabel;
    @FXML
    private TableView<OrderItem> billTable;
    @FXML
    private TableColumn<OrderItem, String> productNameColumn;
    @FXML
    private TableColumn<OrderItem, Integer> quantityColumn;
    @FXML
    private TableColumn<OrderItem, Double> priceColumn;
    @FXML
    private TableColumn<OrderItem, Double> totalColumn;
    @FXML
    private Button printButton;

    private Bill bill;
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.00");

    @FXML
    public void printBill() {
        try {
            PrintBillController.printBill(BillService.getBillItems(orderID),bill);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("An unexpected error occurred while printing the bill.");
            alert.setContentText("Please try again later.");
            alert.showAndWait();
        }
    }

    public void setBill(Bill bill) {
        if (bill == null) {
            System.err.println("Bill is null. Cannot display payment details.");
            return;
        }

        this.bill = bill;
        customerNameLabel.setText(bill.getCustomerName());
        customerPhoneLabel.setText(bill.getCustomerPhone());
        totalCostLabel.setText(DECIMAL_FORMAT.format(bill.getTotalCost()));

        initializeBillTable();
    }

    public void initializeBillTable() {
        productNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProductName()));
        quantityColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getQuantity()));
        priceColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getProductPrice()));
        totalColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getSubTotal()));

        ObservableList<OrderItem> orderItems = FXCollections.observableArrayList(bill.getOrderItemsFromDB());
        System.out.println("Order Item List: " + orderItems);
        billTable.setItems(orderItems);
    }

    // Getter
    public int getOrderID() {
        return this.orderID;
    }

    // Setter
    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }
}
