package src.billiardsmanagement.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import src.billiardsmanagement.model.PoolTable;

public class PoolTableDAO {
    private Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/biamanagement";
        String user = "root";
        String password = "Qkien@111123";
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

    public int addTable(PoolTable table) {
        String query = "INSERT INTO pooltables (name, price, status) VALUES (?, ?, ?)";
        int generatedId = -1;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, table.getName());
            pstmt.setDouble(2, table.getPrice());
            pstmt.setString(3, table.getStatus());
            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                generatedId = generatedKeys.getInt(1); // Lấy ID mới được tạo
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return generatedId; // Trả về ID mới
    }

    public void updateTable(PoolTable table) {
        String query = "UPDATE pooltables SET name = ?, price = ?, status = ? WHERE table_id = ?";

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

    public void removeTable(int tableId) {
        String query = "DELETE FROM pooltables WHERE table_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, tableId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Phương thức để tìm bàn theo ID
    public PoolTable getTableById(int tableId) {
        PoolTable table = null;
        String query = "SELECT * FROM pooltables WHERE table_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, tableId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                String status = rs.getString("status");
                table = new PoolTable(tableId, name, price, status);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return table;
    }
}