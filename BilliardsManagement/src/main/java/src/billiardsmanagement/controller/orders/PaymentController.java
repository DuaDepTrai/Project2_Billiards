package src.billiardsmanagement.controller.orders;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.stage.Stage;
import src.billiardsmanagement.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;

public class PaymentController {
    private int orderID;

    @FXML
    private Button payOrder;

    @FXML
    private Label customerNameLabel;
    @FXML
    private Label customerPhoneLabel;
    @FXML
    private Label totalCostLabel;
    @FXML
    private TableView<BillItem> billTable;
    @FXML
    private TableColumn<BillItem, String> productNameColumn;
    @FXML
    private TableColumn<BillItem, Double> quantityColumn;
    @FXML
    private TableColumn<BillItem, String> unitColumn;
    @FXML
    private TableColumn<BillItem, Double> unitPriceColumn;
    @FXML
    private TableColumn<BillItem, Double> totalCostColumn;
    @FXML
    private Button printButton;
    @FXML
    private Label billNoLabel;

    private int billNo;

    private Bill bill;
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.00");

    @FXML
    public void printBill() {
        try {
                PrintBillController.printBill(BillService.getBillItems(orderID), bill);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("");
                alert.setContentText("The bill has been successfully printed.");
                alert.showAndWait();
                PrintBillController.cutPdfBill();
                PrintBillController.showPdfBillToScreen();
        } catch (Exception e) {
            e.printStackTrace();
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
                new SimpleStringProperty(cellData.getValue().getItemName()));
        quantityColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getQuantity()));
        unitPriceColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getUnitPrice()));
        totalCostColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getTotalPrice()));
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
        billTable.setItems(BillService.getBillItems(this.orderID));

        billNoLabel.setText(String.valueOf(billNo));
    }

    // Getter
    public int getOrderID() {
        return this.orderID;
    }

    // Setter
    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    @FXML
    public void payOrder(ActionEvent event) {
        String query = "UPDATE orders SET order_status = 'Paid' WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderID);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Payment Successful", "The order has been marked as Paid successfully!");
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Payment Error", "Failed to update order status to Paid.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setBillNo(int billNo){
        this.billNo = billNo;
    }
}
