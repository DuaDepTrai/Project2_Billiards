package src.billiardsmanagement.dao;

import javafx.scene.control.Alert;
import src.billiardsmanagement.model.Customer;
import src.billiardsmanagement.model.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerDAO {
    private final Map<String,Integer> customerNameToIdMap = new HashMap<>();
    public Map<Integer, String> getAllCustomerIds() {
        Map<Integer, String> customerMap = new HashMap<>();
        String query = "SELECT customer_id, name FROM customers";  // Cập nhật truy vấn SQL để lấy cả customer_id và customer_name

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int customerId = resultSet.getInt("customer_id");
                String customerName = resultSet.getString("name");
                customerMap.put(customerId, customerName); // Lưu vào Map
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return customerMap;  // Trả về Map
    }

    public List<Customer> getInfoCustomer(int customerID) {
        List<Customer> customers = new ArrayList<>();
        String query = "SELECT name, phone FROM customers WHERE customer_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Bind the customer_id parameter to the PreparedStatement
            statement.setInt(1, customerID);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String name = resultSet.getString("name");
                    String phone = resultSet.getString("phone");

                    // Assuming Customer only takes name and phone
                    customers.add(new Customer(name, phone));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }
    public void addCustomer(Customer customer) {
        String sql = "INSERT INTO customers (name, phone) VALUES (?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getPhone());
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
    public static List<String> fetchCustomersByPhone(String phonePrefix) {
        List<String> customers = new ArrayList<>();
        String query = "SELECT name, phone FROM customers WHERE phone LIKE ?";
        try (PreparedStatement statement = DatabaseConnection.getConnection().prepareStatement(query)) {
            statement.setString(1, phonePrefix + "%");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String phone = resultSet.getString("phone");
                customers.add(name + " - " + phone); // Định dạng giống như map
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }
    public boolean isPhoneExists(String phone) {
        String query = "SELECT COUNT(*) FROM customers WHERE phone = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, phone);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;  // If count > 0, phone number exists
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false; // Return false if no such phone number
    }
}
