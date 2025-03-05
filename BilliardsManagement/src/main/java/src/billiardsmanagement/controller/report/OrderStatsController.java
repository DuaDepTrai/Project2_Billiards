package src.billiardsmanagement.controller.report;
// OrderStatsController.java

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import src.billiardsmanagement.service.OrderService;

import java.net.URL;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Map;
import java.util.ResourceBundle;

public class OrderStatsController implements Initializable {

    @FXML
    private ComboBox<String> periodComboBox;

    @FXML
    private Label totalOrdersLabel;

    @FXML
    private Label totalRevenueLabel;

    @FXML
    private BarChart<String, Number> revenueByPeriodChart;

    @FXML
    private PieChart revenueByTableChart;

    @FXML
    private PieChart revenueByProductCategoryChart;

    private OrderService orderService;
    private String currentPeriod = "day";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        orderService = new OrderService();

        // Initialize period combo box
        periodComboBox.setItems(FXCollections.observableArrayList("day", "week", "month", "year"));
        periodComboBox.setValue("day");

        refreshData();
    }

    @FXML
    private void applyPeriodFilter() {
        currentPeriod = periodComboBox.getValue();
        refreshData();
    }

    private void refreshData() {
        try {
            // Update total orders and revenue
            Map<String, Double> totals = orderService.getTotalOrdersAndRevenue(currentPeriod);
            totalOrdersLabel.setText(String.format("%.0f", totals.get("totalOrders")));

            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
            totalRevenueLabel.setText(currencyFormat.format(totals.get("totalRevenue")));

            // Update revenue by period chart
            revenueByPeriodChart.getData().clear();
            revenueByPeriodChart.getData().addAll(orderService.getRevenueByPeriod(currentPeriod));

            // Update revenue by table group chart
            revenueByTableChart.setData(orderService.getRevenueByTableGroup());

            // Update revenue by product category chart
            revenueByProductCategoryChart.setData(orderService.getRevenueByProductCategory());

        } catch (SQLException e) {
            e.printStackTrace();
            // Show error dialog
        }
    }
}