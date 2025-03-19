package src.billiardsmanagement.controller.orders.bookings;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import src.billiardsmanagement.dao.OrderDAO;
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
    public void initialize() {
        colItemName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colTotalPrice.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

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

}
