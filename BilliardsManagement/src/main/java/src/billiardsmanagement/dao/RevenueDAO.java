package src.billiardsmanagement.dao;

import src.billiardsmanagement.model.DatabaseConnection;
import src.billiardsmanagement.model.Order;
import src.billiardsmanagement.model.Revenue;
import src.billiardsmanagement.model.RevenueService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class RevenueDAO {
    private Connection connection;

    public RevenueDAO() {
        this.connection = DatabaseConnection.getConnection(); // Lấy kết nối từ DatabaseConnection
    }

    // Chèn dữ liệu Revenue vào bảng revenue
    public boolean insertRevenue(Revenue revenue) {
        String sql = "INSERT INTO revenue (date, total_revenue, total_orders, total_customers) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(revenue.getDate()));
            stmt.setDouble(2, revenue.getTotal_revenue());
            stmt.setInt(3, revenue.getTotal_orders());
            stmt.setInt(4, revenue.getTotal_customers());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Tạo Revenue từ danh sách Order và chèn vào bảng revenue

}
