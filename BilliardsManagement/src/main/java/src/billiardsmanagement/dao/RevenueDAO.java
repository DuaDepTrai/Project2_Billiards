package src.billiardsmanagement.dao;

import src.billiardsmanagement.model.DatabaseConnection;;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RevenueDAO {

    // 1. Lấy tổng số đơn hàng và tổng doanh thu trong khoảng thời gian
    public static RevenueSummary getTotalRevenue(String fromDate, String toDate) {
        String query = "SELECT COUNT(order_id) AS totalOrders, SUM(total_cost) AS totalRevenue " +
                "FROM orders WHERE order_status = 'Paid' " +
                "AND order_date BETWEEN ? AND ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, fromDate);
            stmt.setString(2, toDate);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new RevenueSummary(rs.getInt("totalOrders"), rs.getDouble("totalRevenue"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new RevenueSummary(0, 0);
    }

    // 2. Lấy doanh thu theo ngày/tuần/tháng/năm
    public static List<RevenueByDate> getRevenueByDate(String groupBy, String fromDate, String toDate) {
        String dateFormat = switch (groupBy) {
            case "day" -> "DATE(order_date)";
            case "week" -> "YEARWEEK(order_date)";
            case "month" -> "DATE_FORMAT(order_date, '%Y-%m')";
            case "year" -> "YEAR(order_date)";
            default -> "DATE(order_date)";
        };

        String query = "SELECT " + dateFormat + " AS period, SUM(total_cost) AS revenue " +
                "FROM orders WHERE order_status = 'Paid' " +
                "AND order_date BETWEEN ? AND ? GROUP BY period ORDER BY period ASC";

        List<RevenueByDate> revenues = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, fromDate);
            stmt.setString(2, toDate);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                revenues.add(new RevenueByDate(rs.getString("period"), rs.getDouble("revenue")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return revenues;
    }

    // 3. Lấy doanh thu theo nhóm bàn
    public static List<RevenueByCategory> getRevenueByTableGroup(String fromDate, String toDate) {
        String query = "SELECT p.name AS category, SUM(b.total) AS revenue " +
                "FROM bookings b JOIN pooltables p ON b.table_id = p.table_id " +
                "WHERE b.booking_status = 'Finished' AND b.start_time BETWEEN ? AND ? " +
                "GROUP BY p.name";

        List<RevenueByCategory> revenues = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, fromDate);
            stmt.setString(2, toDate);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                revenues.add(new RevenueByCategory(rs.getString("category"), rs.getDouble("revenue")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return revenues;
    }

    // 4. Lấy doanh thu theo nhóm sản phẩm
    public static List<RevenueByCategory> getRevenueByProductGroup(String fromDate, String toDate) {
        String query = "SELECT c.name AS category, SUM(oi.total) AS revenue " +
                "FROM order_items oi JOIN products p ON oi.product_id = p.product_id " +
                "JOIN categories c ON p.category_id = c.category_id " +
                "WHERE oi.order_id IN (SELECT order_id FROM orders WHERE order_status = 'Paid' " +
                "AND order_date BETWEEN ? AND ?) GROUP BY c.name";

        List<RevenueByCategory> revenues = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, fromDate);
            stmt.setString(2, toDate);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                revenues.add(new RevenueByCategory(rs.getString("category"), rs.getDouble("revenue")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return revenues;
    }
}
