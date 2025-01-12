package src.billiardsmanagement.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Thông tin kết nối cơ sở dữ liệu
    private static final String URL = "jdbc:mysql://localhost:3306/biamanagement"; // Thay đổi URL nếu cần
    private static final String USER = "root"; // Thay đổi tên người dùng nếu cần
    private static final String PASSWORD = ""; // Thay đổi mật khẩu nếu cần

    // Phương thức để lấy kết nối cơ sở dữ liệu
    public static Connection getConnection() {
        try {
            // Tải driver của MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Tạo kết nối và trả về
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return null; // Nếu có lỗi trong việc kết nối, trả về null
        }
    }
}
