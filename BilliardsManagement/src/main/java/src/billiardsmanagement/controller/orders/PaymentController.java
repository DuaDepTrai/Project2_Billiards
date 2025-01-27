package src.billiardsmanagement.controller.orders;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import src.billiardsmanagement.dao.*;
import src.billiardsmanagement.model.Booking;
import src.billiardsmanagement.model.OrderItem;
import src.billiardsmanagement.model.RentCue;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.ResourceBundle;

public class PaymentController implements Initializable {
    @FXML
    private TableView<Booking> hoursPlayedTable;
    @FXML
    private TableColumn<Booking,Double> colHoursPlayed;
    @FXML
    private TableColumn<Booking,Double> colAmountPlayed;


    private final ObservableList<Booking> bookingList = FXCollections.observableArrayList();
    private final ObservableList<OrderItem> orderItemList = FXCollections.observableArrayList();
    private final ObservableList<RentCue> rentCueList = FXCollections.observableArrayList();

    private final BookingDAO bookingDAO = new BookingDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final OrderItemDAO orderItemDAO = new OrderItemDAO();
    private final RentCueDAO rentCueDAO = new RentCueDAO();
    private int orderId;

    public void setOrderId(int orderId) {
        this.orderId = orderId;
        loadBooking();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeBooking();
    }

    private void initializeBooking() {

        colHoursPlayed.setCellValueFactory(new PropertyValueFactory<>("timeplay"));

        // Use CellFactory to round values to one decimal place
        colHoursPlayed.setCellFactory(column -> {
            return new TableCell<Booking, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        // Round and display as a string
                        setText(String.format("%.1f", item));  // Round to 1 decimal place
                    }
                }
            };
        });

        colAmountPlayed.setCellValueFactory(new PropertyValueFactory<>("netTotal"));
        colAmountPlayed.setCellFactory(column -> new TableCell<>() {
            private final DecimalFormat decimalFormat = new DecimalFormat("#,###");

            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : decimalFormat.format(item));
            }
        });
    }

    public void loadBooking() {

        List<Booking> bookings = bookingDAO.getBookingByOrderId(orderId);
        bookingList.clear();
        bookingList.addAll(bookings);
        hoursPlayedTable.setItems(bookingList);
        System.out.println("Hour Table: " + hoursPlayedTable.getItems());

    }
}