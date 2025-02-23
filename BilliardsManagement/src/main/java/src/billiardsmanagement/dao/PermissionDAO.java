package src.billiardsmanagement.dao;

import src.billiardsmanagement.model.Permission;
import src.billiardsmanagement.model.TestDBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PermissionDAO {
    private Connection connection;

    public PermissionDAO() {
        try {
            this.connection = TestDBConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getUserPermissions(int userId) {
        List<String> permissions = new ArrayList<>();
        String sql = "SELECT p.permission_name FROM permissions p " +
                "JOIN role_permission rp ON p.permission_id = rp.permission_id " +
                "JOIN roles r ON rp.role_id = r.role_id " +
                "JOIN users u ON r.role_id = u.role_id " +
                "WHERE u.user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                permissions.add(rs.getString("permission_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return permissions;
    }
}