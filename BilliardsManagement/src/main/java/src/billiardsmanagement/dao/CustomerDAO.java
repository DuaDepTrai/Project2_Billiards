package src.billiardsmanagement.dao;

import src.billiardsmanagement.model.Customer;
import src.billiardsmanagement.model.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerDAO {
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

    public Object getAllCustomers() {
        return null;
    }
}
