package src.billiardsmanagement.controller.revenue;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import src.billiardsmanagement.dao.BookingDAO;
import src.billiardsmanagement.dao.OrderDAO;
import src.billiardsmanagement.dao.RevenueDAO;
import src.billiardsmanagement.model.Booking;
import src.billiardsmanagement.model.Order;
import src.billiardsmanagement.model.Revenue;
import src.billiardsmanagement.model.RevenueService;

import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class RevenueController implements Initializable {

    @FXML
    private RadioButton dayRadio, monthRadio, yearRadio;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TableView<Revenue> revenueTable;
    @FXML
    private TableColumn<Revenue, Integer> sttColumn;
    @FXML
    private TableColumn<Revenue, Double> totalRevenueColumn;
    @FXML
    private TableColumn<Revenue, Integer> totalCustomerColumn;
    @FXML
    private TableColumn<Revenue, Integer> totalOrderColumn;
    @FXML
    private ComboBox<String> optionCombobox;

    private final RevenueDAO revenueDAO = new RevenueDAO();
    private final ObservableList<Revenue> revenueList = FXCollections.observableArrayList();
    private Revenue revenue = null;
    private String description = "";
    @FXML
    public void searchRevenue(ActionEvent event) {
        LocalDate date = datePicker.getValue();

        if (date == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a date before searching!");
            return;
        }

        revenueList.clear();
        List<Order> orders = OrderDAO.getOrderPaid();
        List<Booking> bookings = orders.stream()
                .flatMap(order -> BookingDAO.getBookingByOrderId(order.getOrderId()).stream())
                .collect(Collectors.toList());
        if(optionCombobox.getValue().equals("Day")){
            revenue = RevenueService.calculateRevenueByDate(orders, bookings, date);
            description = "Revenue for " + date;
        }else if(optionCombobox.getValue().equals("Month")){
            YearMonth month = YearMonth.from(date);
            revenue = RevenueService.calculateRevenueByMonth(orders,bookings,month);
            description = "Revenue for " + month;
        }else if(optionCombobox.getValue().equals("Year")){
            int year = date.getYear();
            revenue = RevenueService.calculateRevenueByYear(orders,bookings,year);
        }else{
            showAlert(Alert.AlertType.ERROR,"Error","Please chose a option");
        }

        if (revenueDAO.existsByDate(date)) {
            showAlert(Alert.AlertType.INFORMATION, "Info", "Revenue for this date already exists!");
        }

        if (revenue == null || revenue.getTotal_revenue() == 0) {
            revenue = new Revenue(date, 0, 0, 0, description);
            showAlert(Alert.AlertType.WARNING, "Warning", "No revenue today!");
        }

        revenue.setDescription(description);
        revenueList.add(revenue);

        if (!revenueDAO.existsByDate(date)) {
            revenueDAO.insertRevenue(revenue);
        }

        revenueTable.refresh();
        revenueTable.setItems(revenueList);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        sttColumn.setCellValueFactory(param -> {
            int index = sttColumn.getTableView().getItems().indexOf(param.getValue());
            return new SimpleIntegerProperty(index + 1).asObject();
        });

        totalRevenueColumn.setCellValueFactory(new PropertyValueFactory<>("total_revenue"));
        totalRevenueColumn.setCellFactory(param -> new TableCell<Revenue, Double>() {
            private final DecimalFormat df = new DecimalFormat("#,###");

            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : df.format(item));
            }
        });

        totalCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("total_customers"));
        totalOrderColumn.setCellValueFactory(new PropertyValueFactory<>("total_orders"));

        loadRevenueList();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();  
    }

    public void loadRevenueList() {
        List<Order> orders = OrderDAO.getOrderPaid();
        List<Booking> bookings = orders.stream()
                .flatMap(order -> BookingDAO.getBookingByOrderId(order.getOrderId()).stream())
                .collect(Collectors.toList());
        Revenue revenue = RevenueService.calculateRevenueByDate(orders, bookings, LocalDate.now());
        revenueList.add(revenue);
        revenueTable.setItems(revenueList);
    }
}
