package src.billiardsmanagement.controller.orders;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.stage.Stage;
import src.billiardsmanagement.dao.OrderDAO;
import src.billiardsmanagement.dao.OrderItemDAO;
import src.billiardsmanagement.dao.ProductDAO;
import src.billiardsmanagement.model.*;
import src.billiardsmanagement.service.BillService;
import src.billiardsmanagement.service.NotificationService;
import src.billiardsmanagement.model.NotificationStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
            NotificationService.showNotification("Success", "The bill has been successfully printed.", NotificationStatus.Success);
            PrintBillController.cutPdfBill();
            PrintBillController.showPdfBillToScreen();
        } catch (Exception e) {
            logError("An unexpected error occurred while printing the bill: " + e.getMessage());
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

        quantityColumn.setCellValueFactory(cellData -> {
            BillItem billItem = cellData.getValue();
            double quantity = billItem.getQuantity();

            double formattedQuantity = billItem.getItemType().contains("Booking")
                    ? Math.ceil(quantity * 10) / 10.0  // Round up to 1 decimal place
                    : Math.round(quantity);            // Round to nearest integer

            return new SimpleObjectProperty<>(formattedQuantity);
        });

        quantityColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double quantity, boolean empty) {
                super.updateItem(quantity, empty);

                if (empty || quantity == null) {
                    setText(null);
                } else {
                    String formattedText = getTableRow().getItem() != null &&
                            getTableRow().getItem().getItemType().contains("Booking")
                            ? String.format("%.1fh", quantity) // Display with "h" for Booking
                            : String.format("%.0f", quantity); // Display as integer for others

                    setText(formattedText);
                    setAlignment(Pos.CENTER); // Align center
                }
            }
        });

        // Custom number formatter with dots as thousands separator
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat("#,###", symbols);

        unitPriceColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getUnitPrice()));

        unitPriceColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);

                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(decimalFormat.format(price));
                    setAlignment(Pos.CENTER_RIGHT); // Align right
                }
            }
        });

        totalCostColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getTotalPrice()));

        totalCostColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double total, boolean empty) {
                super.updateItem(total, empty);

                if (empty || total == null) {
                    setText(null);
                } else {
                    setText(decimalFormat.format(total));
                    setAlignment(Pos.CENTER_RIGHT); // Align right
                }
            }
        });

        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));

        billTable.getItems().clear();
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


    private String replenishRentCuesNotification = "";
    private boolean replenishRentCues() {
        List<OrderItem> orderItemList = OrderItemDAO.getForEachOrderItem(this.orderID);
        if(orderItemList.isEmpty()) {
            System.out.println("-- From Payment Controller, replenishRentCues() : No Order Item found for this Order Number : "+ OrderDAO.getOrderBillNo(this.orderID));
        }
        boolean success = ProductDAO.replenishMultipleItems(orderItemList);
        if (success) {
            replenishRentCuesNotification = "All Cues Rented in this Order have been returned successfully.";
            return true;
        }
        return false;
    }

    @FXML
    public void payOrder(ActionEvent event) {
        String query = "UPDATE orders SET order_status = 'Paid' WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderID);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                // This method must be called before NotificationService.showNotification()
                // to ensure that the notification String has already been prepared.
                // If this function is called after showNotification(), then the replenish rent cue notification will not show.
                replenishRentCues();
                NotificationService.showNotification("Payment Successful", "Order Number " + OrderDAO.getOrderBillNo(this.orderID) + " has been paid successfully!" + (replenishRentCuesNotification.isEmpty() ? "" : "\n" + replenishRentCuesNotification), NotificationStatus.Success);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.close();
            } else {
                logError("Failed to update order status to Paid.");
            }
        } catch (SQLException e) {
            logError("An error occurred while processing the payment: " + e.getMessage());
        }
    }

    public void setBillNo(int billNo) {
        this.billNo = billNo;
    }

    private void logError(String message) {
        // Log the error (you can implement your logging mechanism here)
        System.err.println("Error: " + message);
        // Show an error notification, but only log the message
    }
}