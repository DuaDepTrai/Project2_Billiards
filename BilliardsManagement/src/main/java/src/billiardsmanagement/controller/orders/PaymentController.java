package src.billiardsmanagement.controller.orders;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
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

    @FXML
    public void printBill() {
        // TODO: Thêm logic in hóa đơn
    }




}