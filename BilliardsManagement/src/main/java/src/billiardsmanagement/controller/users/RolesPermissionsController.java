package src.billiardsmanagement.controller.users;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import src.billiardsmanagement.dao.RolesPermissionsDAO;
import src.billiardsmanagement.model.Role;

public class RolesPermissionsController {
    @FXML
    private TableView<Role> roleTable;
    @FXML
    private TableColumn<Role, String> roleColumn;
    @FXML
    private ListView<String> permissionList;
    @FXML
    private TableView<String> permissionTable;

    @FXML
    private TableColumn<String, String> permissionColumn;

    private RolesPermissionsDAO rolesPermissionsDAO;

    public void initialize() {
        rolesPermissionsDAO = new RolesPermissionsDAO();
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("roleName"));

        loadRoles();

        roleTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                loadPermissionsForRole(newSelection.getRoleId());
            }
        });
        roleTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        permissionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));
        permissionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);


    }

    private void loadRoles() {
        ObservableList<Role> roles = rolesPermissionsDAO.getAllRoles();
        roleTable.setItems(roles);
    }

    private void loadPermissionsForRole(int roleId) {
        ObservableList<String> permissions = rolesPermissionsDAO.getPermissionsByRoleId(roleId);
        permissionTable.setItems(permissions);
    }
}
