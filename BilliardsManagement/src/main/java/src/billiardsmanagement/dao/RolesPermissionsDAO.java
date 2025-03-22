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


    // Phương thức để thêm role mới
    public void addRole(String rolename) throws SQLException {
        String sql = "INSERT INTO roles (role_name) VALUES (?)";
        try (Connection connection = TestDBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, rolename);
            statement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    // Phương thức để xóa role
    public void removeRole(int role_id) throws SQLException {
        String deletePermissionsSQL = "DELETE FROM role_permission WHERE role_id = ?";
        String deleteRoleSQL = "DELETE FROM roles WHERE role_id = ?";

        try (Connection connection = TestDBConnection.getConnection();
             PreparedStatement deletePermissionsStmt = connection.prepareStatement(deletePermissionsSQL);
             PreparedStatement deleteRoleStmt = connection.prepareStatement(deleteRoleSQL)) {

            connection.setAutoCommit(false); // Bắt đầu transaction

            // Xóa tất cả quyền liên kết với role
            deletePermissionsStmt.setInt(1, role_id);
            deletePermissionsStmt.executeUpdate();

            // Xóa role sau khi đã xóa quyền
            deleteRoleStmt.setInt(1, role_id);
            deleteRoleStmt.executeUpdate();

            connection.commit(); // Lưu thay đổi
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public ObservableList<String> getAllPermissions() {
        ObservableList<String> permissions = FXCollections.observableArrayList();
        String query = "SELECT description FROM permissions";

        try (ResultSet rs = connection.createStatement().executeQuery(query)) {
            while (rs.next()) {
                permissions.add(rs.getString("description"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return permissions;
    }

    public void updateRolePermissions(int roleId, ObservableList<String> newPermissions) throws SQLException {
        String deleteQuery = "DELETE FROM role_permission WHERE role_id = ?";
        String insertQuery = "INSERT INTO role_permission (role_id, permission_id) SELECT ?, permission_id FROM permissions WHERE description = ?";

        try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery);
             PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {

            // Xóa tất cả quyền cũ của role
            deleteStmt.setInt(1, roleId);
            deleteStmt.executeUpdate();

            // Thêm quyền mới được chọn
            for (String permission : newPermissions) {
                insertStmt.setInt(1, roleId);
                insertStmt.setString(2, permission);
                insertStmt.executeUpdate();
            }
        }
    }

    public boolean isRoleInUse(int roleId) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE role_id = ?";
        try (Connection connection = TestDBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, roleId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0; // Trả về true nếu có ít nhất 1 user đang sử dụng role này
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
