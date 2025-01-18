package src.billiardsmanagement.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import src.billiardsmanagement.model.PoolTable;

public class PoolTableDAO {
    private Connection getConnection() throws SQLException {
        // Thay đổi thông tin kết nối theo cơ sở dữ liệu của bạn
        String url = "jdbc:mysql://localhost:3306/biamanagement";
        String user = "root";
        String password = "";
        return DriverManager.getConnection(url, user, password);
    }



    public List<PoolTable> getAllTables() {
        List<PoolTable> tables = new ArrayList<>();
        String query = "SELECT * FROM pooltables";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int tableId = rs.getInt("table_id");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                String status = rs.getString("status");
                tables.add(new PoolTable(tableId, name, price, status));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tables;
    }

    public void addTable(PoolTable table) {
        String query = "INSERT INTO your_table_name (name, price, status) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, table.getName());
            pstmt.setDouble(2, table.getPrice());
            pstmt.setString(3, table.getStatus());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateTable(PoolTable table) {
        String query = "UPDATE your_table_name SET name = ?, price = ?, status = ? WHERE table_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, table.getName());
            pstmt.setDouble(2, table.getPrice());
            pstmt.setString(3, table.getStatus());
            pstmt.setInt(4, table.getTableId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeTable(PoolTable table) {
        String query = "DELETE FROM your_table_name WHERE table_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, table.getTableId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<Integer,String> getAvailableTable() {
       Map <Integer,String> availableTablesName = new HashMap<>();
        String query = "SELECT table_id,name FROM pooltables WHERE status = 'Available'"; // Giả sử `status` lưu trữ kiểu boolean

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int tableId = resultSet.getInt("table_id");

                String name = resultSet.getString("name");
                // Tạo đối tượng PoolTable và thêm vào danh sách
                availableTablesName.put(tableId,name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return availableTablesName;
    }

    public Double getPriceTable(String name){
        double price = -1;
        String query = "SELECT price FROM pooltables WHERE name = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Thiết lập tham số table_id
            statement.setString(1, name);

            // Thực thi truy vấn
            ResultSet resultSet = statement.executeQuery();

            // Kiểm tra và lấy giá trị
            if (resultSet.next()) {
                price = resultSet.getDouble("price");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return price;
    }

}