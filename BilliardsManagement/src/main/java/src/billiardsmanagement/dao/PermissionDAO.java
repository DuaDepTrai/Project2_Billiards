package src.billiardsmanagement.dao;

import src.billiardsmanagement.model.Permission;
import src.billiardsmanagement.model.TestDBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PermissionDAO {
    public List<Permission> getPermissionsByRole(int roleId) {
        List<Permission> permissions = new ArrayList<>();
        String query = "SELECT p.* FROM permissions p " +
                "JOIN role_permissions rp ON p.id = rp.permission_id " +
                "WHERE rp.role_id = ?";

        try (Connection connection = TestDBConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, roleId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                permissions.add(new Permission(rs.getInt("id"), rs.getString("name")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return permissions;
    }
}
