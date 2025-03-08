package src.billiardsmanagement.controller.users;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import src.billiardsmanagement.dao.RolesPermissionsDAO;
import src.billiardsmanagement.model.Role;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class EditRoleController {
    @FXML
    private ListView<CheckBox> permissionsListView;
    @FXML
    private Button btnSave;

    private Role selectedRole;
    private RolesPermissionsDAO rolesPermissionsDAO = new RolesPermissionsDAO();
    private Map<String, CheckBox> permissionCheckBoxMap = new HashMap<>();

    public void setRole(Role role) {
        this.selectedRole = role;
        loadPermissions();
    }

    private void loadPermissions() {
        ObservableList<String> allPermissions = rolesPermissionsDAO.getAllPermissions();
        ObservableList<String> rolePermissions = rolesPermissionsDAO.getPermissionsByRoleId(selectedRole.getRoleId());

        permissionsListView.getItems().clear();
        permissionCheckBoxMap.clear();

        for (String permission : allPermissions) {
            CheckBox checkBox = new CheckBox(permission);
            checkBox.setSelected(rolePermissions.contains(permission));
            permissionCheckBoxMap.put(permission, checkBox);
            permissionsListView.getItems().add(checkBox);
        }
    }

    @FXML
    private void handleSave() {
        ObservableList<String> selectedPermissions = FXCollections.observableArrayList();

        for (Map.Entry<String, CheckBox> entry : permissionCheckBoxMap.entrySet()) {
            if (entry.getValue().isSelected()) {
                selectedPermissions.add(entry.getKey());
            }
        }

        try {
            rolesPermissionsDAO.updateRolePermissions(selectedRole.getRoleId(), selectedPermissions);
            Stage stage = (Stage) btnSave.getScene().getWindow();
            stage.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

}
