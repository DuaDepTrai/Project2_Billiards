package src.billiardsmanagement.dao;

import src.billiardsmanagement.model.DatabaseConnection;
import src.billiardsmanagement.model.PoolTable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PoolTableDAO {
    public static List<PoolTable> getAllAvailableTables() {
        List<PoolTable> tables = new ArrayList<>();
        String query = "SELECT p.table_id, p.name, p.status, p.cate_id, " +
                "c.name as cate_name, c.price, c.shortName " +
                "FROM pooltables p " +
                "JOIN cate_pooltables c ON p.cate_id = c.id " +
                "WHERE p.status = 'Available'" + // Filter for available tables
                "AND c.name <> 'Inactive'";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int tableId = rs.getInt("table_id");
                String name = rs.getString("name");
                String status = rs.getString("status");
                int cateId = rs.getInt("cate_id");
                String cateName = rs.getString("cate_name");
                String shortName = rs.getString("shortName");
                double price = rs.getDouble("price");
                PoolTable newTable = new PoolTable(tableId, name, status, cateId, cateName, shortName, price);
                tables.add(newTable);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tables;
    }

    public static PoolTable getSpecificTable(int tableId) {
        PoolTable table = null;
        String query = "SELECT p.table_id, p.name, p.status, p.cate_id, " +
                "c.name as cate_name, c.price, c.shortName " +
                "FROM pooltables p " +
                "JOIN cate_pooltables c ON p.cate_id = c.id " +
                "WHERE p.table_id = ? " +
                "AND c.name <> 'Inactive'"; // Exclude inactive categories

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pr = con.prepareStatement(query)) {

            pr.setInt(1, tableId);
            ResultSet rs = pr.executeQuery();

            if (rs.next()) {
                table = new PoolTable(
                        rs.getInt("table_id"),
                        rs.getString("name"),
                        rs.getString("status"),
                        rs.getInt("cate_id"),
                        rs.getString("cate_name"),
                        rs.getString("shortName"),
                        rs.getDouble("price")
                );
            } else {
                throw new Exception("No active table found with tableId = " + tableId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return table;
    }

    public static List<PoolTable> getFullTableList() {
        List<PoolTable> tables = new ArrayList<>();
        String query = "SELECT p.table_id, p.name, p.status, p.cate_id, " +
                "c.name as cate_name, c.price, c.shortName " +
                "FROM pooltables p " +
                "JOIN cate_pooltables c ON p.cate_id = c.id ";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int tableId = rs.getInt("table_id");
                String name = rs.getString("name");
                String status = rs.getString("status");
                int cateId = rs.getInt("cate_id");
                String cateName = rs.getString("cate_name");
                String shortName = rs.getString("shortName");
                double price = rs.getDouble("price");

                PoolTable newTable = new PoolTable(tableId, name, status, cateId, cateName, shortName, price);
                tables.add(newTable);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tables;
    }

    public List<PoolTable> getAllTables() {
        List<PoolTable> tables = new ArrayList<>();
        String query = "SELECT p.table_id, p.name, p.status, p.cate_id, " +
                "c.name as cate_name, c.price, c.shortName " +
                "FROM pooltables p " +
                "JOIN cate_pooltables c ON p.cate_id = c.id " +
                "WHERE c.name <> 'Inactive'"; // Exclude inactive categories

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int tableId = rs.getInt("table_id");
                String name = rs.getString("name");
                String status = rs.getString("status");
                int cateId = rs.getInt("cate_id");
                String cateName = rs.getString("cate_name");
                String shortName = rs.getString("shortName");
                double price = rs.getDouble("price");

                PoolTable newTable = new PoolTable(tableId, name, status, cateId, cateName, shortName, price);
                tables.add(newTable);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tables;
    }

    public List<PoolTable> getTablesByCategoryName(String cateName) {
        List<PoolTable> tables = new ArrayList<>();
        String query = "SELECT p.table_id, p.name, p.status, p.cate_id, " +
                "c.name as cate_name, c.price, c.shortName " +
                "FROM pooltables p " +
                "JOIN cate_pooltables c ON p.cate_id = c.id " +
                "WHERE c.name = ? " +
                "AND c.name <> 'Inactive' "; // Exclude inactive categories

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, cateName);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int tableId = rs.getInt("table_id");
                    String name = rs.getString("name");
                    String status = rs.getString("status");
                    int cateId = rs.getInt("cate_id");
                    String shortName = rs.getString("shortName");
                    double price = rs.getDouble("price");

                    PoolTable newTable = new PoolTable(tableId, name, status, cateId, cateName, shortName, price);
                    tables.add(newTable);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tables;
    }


    public int addTable(PoolTable table) {
        String query = "INSERT INTO pooltables (name, status, cate_id) VALUES (?, ?, ?)";
        int generatedId = -1;

        try (Connection conn = DatabaseConnection.getConnection();
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
        String query = "UPDATE pooltables SET name = ?, status = ?, cate_id = ? WHERE table_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, table.getName());
            pstmt.setString(2, table.getStatus());
            pstmt.setInt(3, table.getCatePooltableId());
            pstmt.setInt(4, table.getTableId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeTable(int tableId) {
        String getInactiveCateIdQuery = "SELECT id FROM cate_pooltables WHERE name = 'Inactive' LIMIT 1";
        String updateTableQuery = "UPDATE pooltables SET cate_id = ? WHERE table_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement getCateStmt = conn.prepareStatement(getInactiveCateIdQuery);
             ResultSet rs = getCateStmt.executeQuery()) {

            if (rs.next()) {
                int inactiveCateId = rs.getInt("id");

                try (PreparedStatement updateStmt = conn.prepareStatement(updateTableQuery)) {
                    updateStmt.setInt(1, inactiveCateId);
                    updateStmt.setInt(2, tableId);
                    updateStmt.executeUpdate();
                }
            } else {
                System.err.println("Error: No 'Inactive' category found in cate_pooltables.");
            }
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

        try (Connection conn = DatabaseConnection.getConnection();
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

    public static String[] getPoolStatuses() {
        List<String> statuses = new ArrayList<>();
        String query = "SELECT DISTINCT status FROM pooltables"; // Lấy các trạng thái duy nhất

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                statuses.add(rs.getString("status")); // Lấy từng trạng thái duy nhất
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return statuses.toArray(new String[0]); // Chuyển danh sách thành mảng String[]
    }

}