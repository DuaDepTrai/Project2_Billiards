package src.billiardsmanagement.dao;

import src.billiardsmanagement.model.Customer;
import src.billiardsmanagement.model.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/biamanagement"; // Thay đổi tên DB
    private static final String USER = "root"; // Thay đổi tên người dùng
    private static final String PASS = ""; // Thay đổi mật khẩu

    public void addCustomer(Customer customer) {
        String sql = "INSERT INTO customers (name, phone, total_playtime, birthday, address) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getPhone());
            pstmt.setDouble(3, customer.getTotalPlaytime());
            pstmt.setDate(4, customer.getBirthday() != null ? new java.sql.Date(customer.getBirthday().getTime()) : null);
            pstmt.setString(5, customer.getAddress());
            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                customer.setCustomerId(generatedKeys.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateCustomer(Customer customer) {
        String sql = "UPDATE customers SET name = ?, phone = ?, birthday = ?, address = ? WHERE customer_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getPhone());
//            pstmt.setDouble(3, customer.getTotalPlaytime());
            pstmt.setDate(3, customer.getBirthday() != null ? new java.sql.Date(customer.getBirthday().getTime()) : null);
            pstmt.setString(4, customer.getAddress());
            pstmt.setInt(5, customer.getCustomerId());
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
            pstmt.executeUpdate();

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
                customer.setTotalPlaytime(rs.getDouble("total_playtime"));
                customer.setBirthday(rs.getDate("birthday"));
                customer.setAddress(rs.getString("address"));
                customers.add(customer);
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

    public static int getCustomerIdByOrderId(int orderId){
        String query = "SELECT customer_id FROM orders WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("customer_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
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
    // Phương thức để lấy tất cả các ID khách hàng
    public List<Integer> getAllCustomerIds() {
        List<Integer> customerIds = new ArrayList<>();
        for (Customer customer : getAllCustomers()) {
            customerIds.add(customer.getCustomerId());
        }
        return customerIds;
    }

    public static Integer getCustomerIdByPhone(String phone) {
        String query = "SELECT customer_id FROM customers WHERE phone = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, phone);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("customer_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Trả về null nếu không tìm thấy khách hàng
    }

    public Customer getCustomerByPhone(String phone) {
        String sql = "SELECT * FROM customers WHERE phone = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, phone);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Customer customer = new Customer();
                    customer.setCustomerId(rs.getInt("customer_id"));
                    customer.setName(rs.getString("name"));
                    customer.setPhone(rs.getString("phone"));
                    // Set other customer properties as needed
                    return customer;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}