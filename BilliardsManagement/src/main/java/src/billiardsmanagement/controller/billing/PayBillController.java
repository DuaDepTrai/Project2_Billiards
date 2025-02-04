package src.billiardsmanagement.controller.billing;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.util.Pair;
import src.billiardsmanagement.dao.*;
import src.billiardsmanagement.model.*;

import java.util.List;
import java.util.ResourceBundle;
import java.net.URL;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class PayBillController{
    @FXML
    protected TableView<Bill> orderAllDetailsTable;
    
    @FXML private TableColumn<Bill, String> productNameColumn;
    @FXML private TableColumn<Bill, Integer> quantityColumn;
    @FXML private TableColumn<Bill, String> unitColumn;
    @FXML private TableColumn<Bill, Double> unitPriceColumn;
    @FXML private TableColumn<Bill, Double> totalPriceColumn;

    @FXML private TableView<Bill> promotionsTable;
    @FXML private TableColumn<Bill, String> promotionNameColumn;
    @FXML private TableColumn<Bill, Double> promotionDiscountColumn;

    // Bill Summary Labels
    @FXML private Label totalAmountDetail;
    @FXML private Label discountAmountDetail;
    @FXML private Label finalAmountDetail;

    // Data List
    List<Booking> bookingList = new ArrayList<>();
    List<OrderItem> orderItemList = new ArrayList<>();
    List<RentCue> rentCueList = new ArrayList<>();

    private int orderId;

    public void setOrderId(int orderId) {
        this.orderId = orderId;
        // calculateBillSummary();
    }

    public void initializeAllData() {
        List<Bill> allOrderDetails = loadDataList();
        
        // Initialize Order Details Table
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
        unitPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        // Initialize Promotions Table
        promotionNameColumn.setCellValueFactory(new PropertyValueFactory<>("promotionName"));
        promotionDiscountColumn.setCellValueFactory(new PropertyValueFactory<>("promotionDiscount"));

        loadAllOrderDetail(allOrderDetails);
    }

    private List<Bill> loadDataList(){
        List<Bill> allOrderDetails = new ArrayList<>();

        // Map Booking to Bill
        allOrderDetails.addAll(mapBookingToBill(bookingList));

        // Map OrderItem to Bill
        allOrderDetails.addAll(mapOrderItemToBill(orderItemList));

        // Map RentCue to Bill (assuming RentCue is a subclass of OrderItem)
        allOrderDetails.addAll(mapRentCueToBill(rentCueList));  
        return allOrderDetails;
    }

    private void loadAllOrderDetail(List<Bill> allOrderDetails) {
        orderAllDetailsTable.getItems().setAll(allOrderDetails);

        // Load Promotions
        loadPromotions(allOrderDetails);
    }

    private void loadPromotions(List<Bill> allOrderDetails) {
        List<Bill> discountList = allOrderDetails.stream()
            .map(bill -> new Bill(bill.getPromotionName(),bill.getPromotionDiscount()))
            .toList();
            System.out.println("Discount List: " + discountList);
        promotionsTable.getItems().setAll(discountList);
    }

    public List<Bill> mapBookingToBill(List<Booking> bookings) {
        List<Bill> bills = new ArrayList<>();
        for(Booking booking : bookings) {
            double totalPrice = booking.getPriceTable() * booking.getTimeplay();
            bills.add(new Bill(booking.getTableName(), 1, "Table", booking.getPriceTable(), totalPrice));
        }
        return bills;
    }

    public List<Bill> mapOrderItemToBill(List<OrderItem> orderItems) {
        List<Bill> bills = new ArrayList<>();
        for(OrderItem orderItem : orderItems) {
            String unit = CategoryDAO.getUnitByProductId(orderItem.getProductId());
            System.out.println("Unit: " + unit);
            bills.add(new Bill(orderItem.getProductName(), orderItem.getQuantity(), unit, orderItem.getProductPrice(), orderItem.getSubTotal()));
        }
        return bills;
    }

    public List<Bill> mapRentCueToBill(List<RentCue> rentCues) {
        List<Bill> bills = new ArrayList<>();
        for(RentCue rentCue : rentCues) {
            String unit = CategoryDAO.getUnitByProductId(rentCue.getProductId());
            System.out.println("Unit: " + unit);
            bills.add(new Bill(rentCue.getProductName(), rentCue.getQuantity(), unit, rentCue.getProductPrice(), rentCue.getSubTotal()));
        }
        return bills;
    }

    // private void calculateBillSummary() {
    //     double totalAmount = calculateTotalAmount();
    //     double discountAmount = calculateDiscountAmount();
    //     double finalAmount = totalAmount - discountAmount;

    //     totalAmountDetail.setText(String.format("Total Amount: %.2f", totalAmount));
    //     discountAmountDetail.setText(String.format("Discount Amount: %.2f", discountAmount));
    //     finalAmountDetail.setText(String.format("Final Amount to Pay: %.2f", finalAmount));
    // }

    private double calculateTotalAmount() {
        return orderAllDetailsTable.getItems().stream()
                .mapToDouble(Bill::getTotalPrice)
                .sum();
    }

    // private double calculateDiscountAmount() {
    //     return promotionsTable.getItems().stream()
    //             .mapToDouble(promotion -> {
    //                 double totalAmount = calculateTotalAmount();
    //                 return totalAmount * (promotion.getDiscount() / 100.0);
    //             })
    //             .sum();
    // }

    @FXML
    public void handlePayNow(ActionEvent event) {
        // Implement payment logic
        // This might involve updating order status, creating payment record, etc.
        showAlert(Alert.AlertType.INFORMATION, "Payment Successful", "Thank you for your payment!");
        closeWindow();
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) totalAmountDetail.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Getter and Setter for bookingList
    public List<Booking> getBookingList() {
        return bookingList;
    }

    public void setBookingList(List<Booking> bookingList) {
        this.bookingList = bookingList;
    }

    // Getter and Setter for orderItemList
    public List<OrderItem> getOrderItemList() {
        return orderItemList;
    }

    public void setOrderItemList(List<OrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }

    // Getter and Setter for rentCueList
    public List<RentCue> getRentCueList() {
        return rentCueList;
    }

    public void setRentCueList(List<RentCue> rentCueList) {
        this.rentCueList = rentCueList;
    }
}
