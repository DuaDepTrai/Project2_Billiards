package src.billiardsmanagement.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import src.billiardsmanagement.model.PoolTable;

public class PoolTableDAO {
    private Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/biamanagement";
        String user = "root";
        String password = "";
        return DriverManager.getConnection(url, user, password);
    }

    public List<PoolTable> getAllTables() {
        List<PoolTable> tables = new ArrayList<>();
        String query = "SELECT p.table_id, p.name, p.status, p.cate_id, " +
                      "c.name as cate_name, c.price " +
                      "FROM pooltables p " +
                      "JOIN cate_pooltables c ON p.cate_id = c.id";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int tableId = rs.getInt("table_id");
                String name = rs.getString("name");
                String status = rs.getString("status");
                int cateId = rs.getInt("cate_id");
                String cateName = rs.getString("cate_name");
                double price = rs.getDouble("price");
                PoolTable newTable = new PoolTable(tableId, name, status, cateId, cateName, price);
                tables.add(newTable);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tables;
    }

    public int addTable(PoolTable table) {
        String query = "INSERT INTO pooltables (name, status, cate_id) VALUES (?, ?, ?)";
        int generatedId = -1;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, table.getName());
            pstmt.setString(2, table.getStatus());
            pstmt.setInt(3, table.getCatePooltableId());
            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                generatedId = generatedKeys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return generatedId;
    }

    public void updateTable(PoolTable table) {
        String query = "UPDATE pooltables SET name = ?, status = ? WHERE table_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, table.getName());
            pstmt.setString(2, table.getStatus());
            pstmt.setInt(3, table.getTableId());
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

    public PoolTable getTableById(int tableId) {
        PoolTable table = null;
        String query = "SELECT p.table_id, p.name, p.status, p.cate_id, " +
                      "c.name as cate_name, c.price " +
                      "FROM pooltables p " +
                      "JOIN cate_pooltables c ON p.cate_id = c.id " +
                      "WHERE p.table_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, tableId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String name = rs.getString("name");
                String status = rs.getString("status");
                int cateId = rs.getInt("cate_id");
                String cateName = rs.getString("cate_name");
                double price = rs.getDouble("price");
                
                table = new PoolTable(tableId, name, status, cateId, cateName, price);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return table;
    }
}