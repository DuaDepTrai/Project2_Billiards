package src.billiardsmanagement.controller.report;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.text.Text;
import src.billiardsmanagement.model.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ProductStatsController {
    @FXML
    private Text txtTotalProducts;
    @FXML
    private Text txtTotalStock;
    @FXML
    private BarChart<String, Number> chartTopProducts;

    public void initialize() {
        loadProductStatistics();
    }

    private void loadProductStatistics() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Lấy tổng số sản phẩm và tổng số hàng tồn kho
            String countQuery = "SELECT COUNT(*) AS total_products, SUM(quantity) AS total_stock FROM products";
            try (PreparedStatement stmt = conn.prepareStatement(countQuery);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    txtTotalProducts.setText(String.valueOf(rs.getInt("total_products")));
                    txtTotalStock.setText(String.valueOf(rs.getInt("total_stock")));
                }
            }

            // Lấy top 10 sản phẩm bán chạy nhất
            String topQuery = "SELECT p.name, SUM(oi.quantity) AS sold, p.quantity FROM orders_items oi " +
                    "JOIN products p ON oi.product_id = p.product_id " +
                    "GROUP BY p.product_id ORDER BY sold DESC LIMIT 10";

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Top 10 best sale");

            try (PreparedStatement stmt = conn.prepareStatement(topQuery);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String productName = rs.getString("name");
                    int sold = rs.getInt("sold");
                    int stock = rs.getInt("quantity");

                    XYChart.Data<String, Number> data = new XYChart.Data<>(productName, sold);
                    data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                        if (newNode != null) {
                            newNode.setStyle("-fx-bar-fill: #4CAF50;");
                            javafx.scene.control.Label label = new javafx.scene.control.Label(String.valueOf(stock));
                            label.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
                            newNode.parentProperty().addListener((observable, oldValue, newValue) -> {
                                if (newValue != null) {
                                    ((javafx.scene.layout.StackPane) newNode).getChildren().add(label);
                                }
                            });
                        }
                    });

                    series.getData().add(data);
                }
            }

            chartTopProducts.getData().clear();
            chartTopProducts.getData().add(series);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
