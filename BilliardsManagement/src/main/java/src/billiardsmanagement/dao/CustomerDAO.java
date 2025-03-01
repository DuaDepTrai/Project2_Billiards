package src.billiardsmanagement.dao;

import src.billiardsmanagement.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/biamanagement"; // Thay đổi tên DB
    private static final String USER = "root"; // Thay đổi tên người dùng
    private static final String PASS = "Qkien@111123"; // Thay đổi mật khẩu

    public void addCustomer(Customer customer) {
        String sql = "INSERT INTO customers (name, phone, total_playtime) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getPhone());
            pstmt.setDouble(3, customer.getTotalPlaytime());
            pstmt.executeUpdate();

            // Lấy ID được tạo tự động
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                customer.setCustomerId(generatedKeys.getInt(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateCustomer(Customer customer) {
        String sql = "UPDATE customers SET name = ?, phone = ?, total_playtime = ? WHERE customer_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getPhone());
            pstmt.setDouble(3, customer.getTotalPlaytime());
            pstmt.setInt(4, customer.getCustomerId());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeCustomer(int customerId) {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerId);
            pstmt.executeUpdate(); q

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Customer customer = new Customer();
                customer.setCustomerId(rs.getInt("customer_id"));
                customer.setName(rs.getString("name"));
                customer.setPhone(rs.getString("phone"));
                customer.setTotalPlaytime(rs.getDouble("total_playtime")); // Đảm bảo lấy đúng trường
                customers.add(customer);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

    // Phương thức để lấy tất cả các ID khách hàng
    public List<Integer> getAllCustomerIds() {
        List<Integer> customerIds = new ArrayList<>();
        for (Customer customer : getAllCustomers()) {
            customerIds.add(customer.getCustomerId());
        }
        return customerIds;
    }
}