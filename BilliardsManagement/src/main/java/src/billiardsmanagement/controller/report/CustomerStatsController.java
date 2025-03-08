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
            int totalCustomers = 0; // Biến này sẽ không được sử dụng trong lambda
            try (PreparedStatement stmt = conn.prepareStatement(countQuery);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    totalCustomers = rs.getInt(1);
                    txtTotalCustomers.setText(String.valueOf(totalCustomers));
                }
            }

            // Lấy số lượng khách hàng theo nhóm thời gian chơi
            String groupQuery = "SELECT " +
                    "SUM(CASE WHEN total_playtime < 100 THEN 1 ELSE 0 END) AS below_100, " +
                    "SUM(CASE WHEN total_playtime BETWEEN 100 AND 300 THEN 1 ELSE 0 END) AS between_100_300, " +
                    "SUM(CASE WHEN total_playtime > 300 THEN 1 ELSE 0 END) AS above_300 " +
                    "FROM customers";

            try (PreparedStatement stmt = conn.prepareStatement(groupQuery);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    final int finalTotalCustomers = totalCustomers; // Biến final
                    int below100 = rs.getInt("below_100");
                    int between100_300 = rs.getInt("between_100_300");
                    int above300 = rs.getInt("above_300");

                    // Cập nhật biểu đồ tròn
                    // Cập nhật biểu đồ tròn
                    chartCustomersByPlayTime.getData().clear();
                    if (finalTotalCustomers > 0) { // Đảm bảo tổng số khách hàng không bằng 0
                        PieChart.Data slice1 = new PieChart.Data(
                                String.format("< 100h (%.1f%%)", (below100 * 100.0 / finalTotalCustomers)), below100);
                        PieChart.Data slice2 = new PieChart.Data(
                                String.format("100 - 300h (%.1f%%)", (between100_300 * 100.0 / finalTotalCustomers)), between100_300);
                        PieChart.Data slice3 = new PieChart.Data(
                                String.format("> 300h (%.1f%%)", (above300 * 100.0 / finalTotalCustomers)), above300);

                        chartCustomersByPlayTime.getData().addAll(slice1, slice2, slice3);
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
