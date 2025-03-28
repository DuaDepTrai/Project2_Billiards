package src.billiardsmanagement.controller.report;

import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.text.Text;
import src.billiardsmanagement.model.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CustomerStatsController {
    @FXML
    private Text txtTotalCustomers;
    @FXML
    private PieChart chartCustomersByPlayTime;

    public void initialize() {
        loadCustomerStatistics();
    }

    private void loadCustomerStatistics() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Lấy tổng số khách hàng
            String countQuery = "SELECT COUNT(*) FROM customers";
            int totalCustomers = 0;
            try (PreparedStatement stmt = conn.prepareStatement(countQuery);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    totalCustomers = rs.getInt(1);
                    txtTotalCustomers.setText(String.valueOf(totalCustomers));
                }
            }

            // Lấy số lượng khách hàng theo nhóm thời gian chơi
            String groupQuery = "SELECT " +
                    "SUM(CASE WHEN total_playtime < 10 THEN 1 ELSE 0 END) AS below_10, " +
                    "SUM(CASE WHEN total_playtime BETWEEN 10 AND 30 THEN 1 ELSE 0 END) AS between_10_30, " +
                    "SUM(CASE WHEN total_playtime BETWEEN 30 AND 50 THEN 1 ELSE 0 END) AS between_30_50, " +
                    "SUM(CASE WHEN total_playtime BETWEEN 50 AND 100 THEN 1 ELSE 0 END) AS between_50_100, " +
                    "SUM(CASE WHEN total_playtime > 100 THEN 1 ELSE 0 END) AS above_100 " +
                    "FROM customers";

            try (PreparedStatement stmt = conn.prepareStatement(groupQuery);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    final int finalTotalCustomers = totalCustomers;
                    int below10 = rs.getInt("below_10");
                    int between10_30 = rs.getInt("between_10_30");
                    int between30_50 = rs.getInt("between_30_50");
                    int between50_100 = rs.getInt("between_50_100");
                    int above100 = rs.getInt("above_100");

                    // Cập nhật biểu đồ tròn
                    chartCustomersByPlayTime.getData().clear();
                    if (finalTotalCustomers > 0) {
                        PieChart.Data slice1 = new PieChart.Data(
                                String.format("< 10h (%.1f%%)", (below10 * 100.0 / finalTotalCustomers)), below10);
                        PieChart.Data slice2 = new PieChart.Data(
                                String.format("10 - 30h (%.1f%%)", (between10_30 * 100.0 / finalTotalCustomers)), between10_30);
                        PieChart.Data slice3 = new PieChart.Data(
                                String.format("30 - 50h (%.1f%%)", (between30_50 * 100.0 / finalTotalCustomers)), between30_50);
                        PieChart.Data slice4 = new PieChart.Data(
                                String.format("50 - 100h (%.1f%%)", (between50_100 * 100.0 / finalTotalCustomers)), between50_100);
                        PieChart.Data slice5 = new PieChart.Data(
                                String.format("> 100h (%.1f%%)", (above100 * 100.0 / finalTotalCustomers)), above100);

                        chartCustomersByPlayTime.getData().addAll(slice1, slice2, slice3, slice4, slice5);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
