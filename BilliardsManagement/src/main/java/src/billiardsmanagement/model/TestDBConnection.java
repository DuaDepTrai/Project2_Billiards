package src.billiardsmanagement.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestDBConnection {
    private static final String jdbcURL = "jdbc:mysql://127.0.0.1:3306/biamanagement"; //
    private static final String dbUser = "root";
    private static final String dbPassword = "";

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(jdbcURL, dbUser, dbPassword);
    }

    public static void main(String[] args) {
        try {
            Connection connection = TestDBConnection.getConnection();
            System.out.println("Kết nối thành công!");
            connection.close();
        } catch (Exception e) {
            System.out.println("Kết nối thất bại: " + e.getMessage());
        }
    }

}

