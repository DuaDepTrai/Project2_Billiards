package src.billiardsmanagement.controller.orders;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import src.billiardsmanagement.dao.OrderDAO;
import src.billiardsmanagement.model.Bill;
import src.billiardsmanagement.model.BillItem;
import src.billiardsmanagement.service.BillService;
import src.billiardsmanagement.model.Order;

import java.text.DecimalFormat;

public class BookingController {
    @FXML
    private Label customerNameLabel, customerPhoneLabel, totalCostLabel;
    @FXML
    private TableView<BillItem> billTableView;
    @FXML
    private TableColumn<BillItem, String> colItemName;
    @FXML
    private TableColumn<BillItem, Integer> colQuantity;
    @FXML
    private TableColumn<BillItem, Double> colUnitPrice;
    @FXML
    private TableColumn<BillItem, Double> colTotalPrice;

    private int orderId; // Giá trị orderId lấy từ giao diện hoặc truyền vào
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0");
    private Bill bill;
    public void initialize() {
        colItemName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colUnitPrice.setCellFactory(column -> new TableCell<>() {
            private final DecimalFormat decimalFormat = new DecimalFormat("#,###"); // Không có phần thập phân

            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : decimalFormat.format(item));
            }
        });

        colTotalPrice.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        colTotalPrice.setCellFactory(column -> new TableCell<>() {
            private final DecimalFormat decimalFormat = new DecimalFormat("#,###"); // Không có phần thập phân

            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : decimalFormat.format(item));
            }
        });

        loadBillData();
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
        Order order = OrderDAO.getOrderById(orderId);

        customerNameLabel.setText(order.getCustomerName());
        customerPhoneLabel.setText(order.getCustomerPhone());
        totalCostLabel.setText(DECIMAL_FORMAT.format(order.getTotalCost()));
        loadBillData();

    }

    private void loadBillData() {
        billTableView.setItems(BillService.getBillItems(orderId));
    }

    public void setBill(Bill bill) {
        if (bill == null) {
            System.err.println("Bill is null. Cannot display payment details.");
            return;
        }

        this.bill = bill;
    }

    public void printBill(ActionEvent event) {
        try {
            PrintBillController.printBill(BillService.getBillItems(orderId), bill);

            // Hiển thị thông báo in hóa đơn thành công
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Bill printed successfully!");
            alert.setContentText("The bill has been printed successfully.");
            alert.showAndWait();
        } catch (Exception e) {
            // Hiển thị thông báo lỗi nếu có lỗi xảy ra
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("An unexpected error occurred while printing the bill.");
            alert.setContentText("Please try again later.");
            alert.showAndWait();
        }
    }
}
