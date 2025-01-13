package src.billiardsmanagement.dao;

import src.billiardsmanagement.model.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {
    public List<Integer> getAllCustomerIds() {
        List<Integer> customerIds = new ArrayList<>();
        String query = "SELECT customer_id FROM customers";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                customerIds.add(resultSet.getInt("customer_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customerIds;
    }

    public Object getAllCustomers() {
        return null;
    }
}
