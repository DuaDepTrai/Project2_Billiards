package src.billiardsmanagement.controller.revenue;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import src.billiardsmanagement.dao.OrderDAO;
import src.billiardsmanagement.dao.RevenueDAO;
import src.billiardsmanagement.model.Order;
import src.billiardsmanagement.model.Revenue;
import src.billiardsmanagement.model.RevenueService;

import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.ResourceBundle;

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
    @FXML
    public void searchRevenue(ActionEvent event) {
        LocalDate date = datePicker.getValue();
        int year = date.getYear();
        int month = date.getMonthValue();
        revenueList.clear();
        List<Order> orders = OrderDAO.getOrderPaid();
        Revenue revenue = null;


        if (date == null) {
            System.out.println("Lỗi: Chưa chọn ngày!");
            Alert alert = new Alert(Alert.AlertType.WARNING, "Vui lòng chọn ngày trước khi tìm kiếm!", ButtonType.OK);
            alert.showAndWait();
            return; // Thoát sớm để tránh lỗi
        }
        String description = "";
/*
       if (optionCombobox.getValue().equals("Day")) {
           System.out.println("Date selected: " + date);
            revenue = RevenueService.calculateRevenueByDate(orders, date);
            description = "Doanh thu theo ngày " + date;
        } else if (optionCombobox.getValue().equals("Month")) {
            revenue = RevenueService.calculateRevenueByMonth(orders,year, month);
            description = "Doanh thu theo tháng " + month + "/" + year;
        } else if (optionCombobox.getValue().equals("Year")) {
            revenue = RevenueService.calculateRevenueByYear(orders, date.getYear());
            description = "Doanh thu theo năm " + date.getYear();
        } else {
           System.out.println("Lỗi: Giá trị không hợp lệ trong ComboBox!");
           return;
        }

        if (revenue != null) {
            revenue.setDescription(description); // Thêm mô tả vào revenue
            revenueList.add(revenue);
            revenueDAO.insertRevenue(revenue);
        } else {
            System.out.println("Lỗi: Revenue tính toán bị null!");
        }

        revenueTable.setItems(null);
        revenueTable.setItems(revenueList);

 */

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

    public void  loadRevenueList(){
        /*
        List<Order> orders = OrderDAO.getOrderPaid();
        Revenue revenues = RevenueService.calculateRevenueByDate(orders,LocalDate.now());
        revenueList.add(revenues);
        revenueTable.setItems(revenueList);

         */
    }
}
