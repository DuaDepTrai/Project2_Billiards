package src.billiardsmanagement.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import src.billiardsmanagement.model.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class OrderService {

    public Map<String, Double> getTotalOrdersAndRevenue(String period) throws SQLException {
        Map<String, Double> result = new HashMap<>();
        String dateCondition = getDateCondition(period);

        String query = "SELECT COUNT(*) as total_orders, SUM(total_cost) as total_revenue " +
                "FROM orders WHERE order_status != 'Canceled' AND " + dateCondition;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                result.put("totalOrders", rs.getDouble("total_orders"));
                result.put("totalRevenue", rs.getDouble("total_revenue"));
            }
        }
        return result;
    }

    public ObservableList<XYChart.Series<String, Number>> getRevenueByPeriod(String period) throws SQLException {
        ObservableList<XYChart.Series<String, Number>> seriesList = FXCollections.observableArrayList();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue");

        String groupBy = switch (period) {
            case "day" -> "DATE(order_date)";
            case "week" -> "YEARWEEK(order_date, 1)";
            case "month" -> "DATE_FORMAT(order_date, '%Y-%m')";
            case "year" -> "YEAR(order_date)";
            default -> "DATE(order_date)";
        };

        String query = "SELECT " + groupBy + " as period, SUM(total_cost) as revenue " +
                "FROM orders " +
                "WHERE order_status != 'Canceled' " +
                "GROUP BY " + groupBy + " " +
                "ORDER BY period";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String periodLabel = rs.getString("period");
                double revenue = rs.getDouble("revenue");
                series.getData().add(new XYChart.Data<>(periodLabel, revenue));
            }
        }

        seriesList.add(series);
        return seriesList;
    }

    public ObservableList<PieChart.Data> getRevenueByTableGroup() throws SQLException {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        String query = "SELECT ctp.name AS category_name, SUM(b.net_total) AS revenue " +
                "FROM bookings b " +
                "JOIN pooltables pt ON b.table_id = pt.table_id " +
                "JOIN cate_pooltables ctp ON pt.cate_id = ctp.id " +
                "WHERE b.booking_status = 'Finish' " +
                "GROUP BY ctp.id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String categoryName = rs.getString("category_name");
                double revenue = rs.getDouble("revenue");
                pieChartData.add(new PieChart.Data(categoryName, revenue));
            }
        }

        return pieChartData;
    }

    public ObservableList<PieChart.Data> getRevenueByProductCategory() throws SQLException {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        String query = "SELECT c.category_name, SUM(oi.quantity * oi.subtotal) as revenue " +
                "FROM orders_items oi " +
                "JOIN products p ON oi.product_id = p.product_id " +
                "JOIN category c ON p.category_id = c.category_id " +
                "JOIN orders o ON oi.order_id = o.order_id " +
                "WHERE o.order_status != 'Canceled' " +
                "GROUP BY c.category_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String categoryName = rs.getString("category_name");
                double revenue = rs.getDouble("revenue");
                pieChartData.add(new PieChart.Data(categoryName, revenue));
            }
        }

        return pieChartData;
    }

    private String getDateCondition(String period) {
        LocalDate now = LocalDate.now();
        return switch (period) {
            case "day" -> "order_date = '" + now + "'";
            case "week" -> {
                LocalDate startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1);
                yield "order_date >= '" + startOfWeek + "' AND order_date <= '" + now + "'";
            }
            case "month" -> "YEAR(order_date) = " + now.getYear() + " AND MONTH(order_date) = " + now.getMonthValue();
            case "year" -> "YEAR(order_date) = " + now.getYear();
            default -> "1=1";
        };
    }
}