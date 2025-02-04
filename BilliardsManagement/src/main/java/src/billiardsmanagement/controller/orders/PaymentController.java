package src.billiardsmanagement.controller.orders;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import src.billiardsmanagement.model.Bill;
import src.billiardsmanagement.model.Booking;
import src.billiardsmanagement.model.OrderItem;
import src.billiardsmanagement.model.RentCue;

import java.text.DecimalFormat;

public class PaymentController {

    @FXML
    private Label customerNameLabel;
    @FXML
    private Label customerPhoneLabel;
    @FXML
    private Label totalCostLabel;
    @FXML
    private TableView<OrderItem> orderItemTable;
    @FXML
    private TableView<Booking> bookingsTable;
    @FXML
    //order Item table
    private TableColumn<OrderItem, String> productNameColumn;
    @FXML
    private TableColumn<OrderItem, Integer> quantityColumn;
    @FXML
    private TableColumn<OrderItem, Double> priceColumn;
    @FXML
    private TableColumn<OrderItem, Double> totalColumn;
    //bookings table

    @FXML
    private TableColumn<Booking, String> tableNameColumn;
    @FXML
    private TableColumn<Booking,Double> hoursColumn;
    @FXML
    private TableColumn<Booking,Double>tablePriceColumn;
    @FXML
    private TableColumn<Booking,Double>bookingTotalColumn;
//    Rent Cue Table
    @FXML
    private TableView<RentCue> rentCuesTable;
    @FXML
    private TableColumn<RentCue, String> cueNameColumn;
    @FXML
    private TableColumn<RentCue, Integer> cueQuantityColumn;
    @FXML
    private TableColumn<RentCue, Double> cueHoursColumn;
    @FXML
    private TableColumn<RentCue, Double> cuePriceColumn;
    @FXML
    private TableColumn<RentCue, Double> cueTotalColumn;


    private Bill bill;
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0");

    @FXML
    public void printBill() {
        // TODO: Thêm logic in hóa đơn
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
        initializeOrderItemTable();
        initializeBookingTable();
        initializeRentCueTable();
    }

    private void initializeOrderItemTable() {
        ObservableList<OrderItem> orderItems = FXCollections.observableArrayList(bill.getOrderItemsFromDB());
        orderItemTable.setItems(orderItems);

        productNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProductName()));
        quantityColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getQuantity()));
        priceColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getProductPrice()));
        totalColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getSubTotal()));
    }

    private void initializeBookingTable(){
        ObservableList<Booking> bookings = FXCollections.observableArrayList(bill.getBookingsFromDB());
        bookingsTable.setItems(bookings);
        tableNameColumn.setCellValueFactory(cellData->
                new SimpleObjectProperty<>(cellData.getValue().getTableName()));
        hoursColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getTimeplay()));
        tablePriceColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getPriceTable()));
        bookingTotalColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getSubTotal()));
    }

    private void initializeRentCueTable() {
        // Lấy dữ liệu từ Bill (giả sử có phương thức getRentCuesFromDB trong Bill)
        ObservableList<RentCue> rentCues = FXCollections.observableArrayList(bill.getRentCuesFromDB());
        rentCuesTable.setItems(rentCues);

        // Cài đặt giá trị cho các cột
        cueNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProductName())); // Tên gậy
        cueQuantityColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getQuantity())); // Số lượng
        cueHoursColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getTimeplay())); // Số giờ thuê
        cuePriceColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getProductPrice())); // Đơn giá
        cueTotalColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getSubTotal())); // Thành tiền (Tính từ productPrice * quantity * timeplay)
    }
}