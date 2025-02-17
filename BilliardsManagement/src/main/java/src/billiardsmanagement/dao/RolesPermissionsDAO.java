package src.billiardsmanagement.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import src.billiardsmanagement.model.Role;
import src.billiardsmanagement.model.TestDBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RolesPermissionsDAO {
    private Connection connection;

    public RolesPermissionsDAO() {
        try {
            connection = TestDBConnection.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ObservableList<Role> getAllRoles() {
        ObservableList<Role> roles = FXCollections.observableArrayList();
        String query = "SELECT * FROM roles";
        try (ResultSet rs = connection.createStatement().executeQuery(query)) {
            while (rs.next()) {
                roles.add(new Role(rs.getInt("role_id"), rs.getString("role_name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roles;
    }

    public ObservableList<String> getPermissionsByRoleId(int roleId) {
        ObservableList<String> permissions = FXCollections.observableArrayList();
        String query = "SELECT p.description FROM permissions p " +
                "JOIN role_permission rp ON p.permission_id = rp.permission_id " +
                "WHERE rp.role_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, roleId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                permissions.add(rs.getString("description"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return permissions;
    }
}
