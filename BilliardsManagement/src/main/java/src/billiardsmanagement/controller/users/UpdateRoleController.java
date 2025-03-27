package src.billiardsmanagement.controller.users;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import src.billiardsmanagement.dao.RolesPermissionsDAO;
import src.billiardsmanagement.model.Role;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UpdateRoleController {
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

        // Hiển thị hộp thoại xác nhận
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Save Permissions");
        alert.setHeaderText("Are you sure you want to save these permissions?");
        alert.setContentText("Role: " + selectedRole.getRoleName() +
                "\nSelected Permissions: " + String.join(", ", selectedPermissions));

        ButtonType buttonYes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType buttonNo = new ButtonType("No", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(buttonYes, buttonNo);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == buttonYes) {
            try {
                rolesPermissionsDAO.updateRolePermissions(selectedRole.getRoleId(), selectedPermissions);
                Stage stage = (Stage) btnSave.getScene().getWindow();
                stage.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

//        try {
//            rolesPermissionsDAO.updateRolePermissions(selectedRole.getRoleId(), selectedPermissions);
//            Stage stage = (Stage) btnSave.getScene().getWindow();
//            stage.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }

    }

}
