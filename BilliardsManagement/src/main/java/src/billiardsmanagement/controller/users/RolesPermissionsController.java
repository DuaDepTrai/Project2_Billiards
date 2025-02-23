package src.billiardsmanagement.controller.users;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import src.billiardsmanagement.controller.MainController;
import src.billiardsmanagement.dao.RolesPermissionsDAO;
import src.billiardsmanagement.model.Role;
import java.sql.SQLException;
import java.util.Optional;

public class RolesPermissionsController {
    @FXML
    private TableView<Role> roleTable;
    @FXML
    private TableColumn<Role, String> roleColumn;
    @FXML
    private TableView<String> permissionTable;

    @FXML
    private Button btnAddNewRole;
    @FXML
    private Button btnEditRole;
    @FXML
    private Button btnRemoveRole;

    @FXML
    private Button btnBack;  // Khai báo nút Back

    @FXML
    private TableColumn<String, String> permissionColumn;

    private ObservableList<Role> roleList = FXCollections.observableArrayList();
    private RolesPermissionsDAO rolesPermissionsDAO = new RolesPermissionsDAO();

    private MainController mainController; // Biến để lưu MainController
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

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


        btnAddNewRole.setOnAction(event -> handleAddNewRole());
        btnEditRole.setOnAction(event -> handleEditSelectedRole());
        btnRemoveRole.setOnAction(event -> handleRemoveSelectedRole());
        btnBack.setOnAction(event -> handleBackAction());

    }

    private void loadRoles() {
        ObservableList<Role> roles = rolesPermissionsDAO.getAllRoles();
        roleList.clear();
        roleList.addAll(rolesPermissionsDAO.getAllRoles());
        roleTable.setItems(roles);
    }

    private void loadPermissionsForRole(int roleId) {
        ObservableList<String> permissions = rolesPermissionsDAO.getPermissionsByRoleId(roleId);
        permissionTable.setItems(permissions);
    }


    @FXML
    private void handleAddNewRole() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/users/addRole.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Add New Role");
            stage.setScene(new Scene(root));
            stage.show();
            stage.setOnHidden(event -> refreshTable());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void refreshTable() {
        loadRoles();
    }


    private void handleRemoveSelectedRole() {
        Role selectedRole = roleTable.getSelectionModel().getSelectedItem();
        if (selectedRole == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Role Selected");
            alert.setContentText("Please select a role to remove");
            alert.showAndWait();
            return;
        }
        confirmAndRemoveRole(selectedRole);
    }


    private void confirmAndRemoveRole(Role role) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Remove");
        alert.setHeaderText("Are you sure you want to remove this role?");
        alert.setContentText("Role: " + role.getRoleName());

        ButtonType buttonYes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType buttonNo = new ButtonType("No", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(buttonYes, buttonNo);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == buttonYes) {
            removeRole(role);
        }
    }

    private void removeRole(Role role) {
        try {
            rolesPermissionsDAO.removeRole(role.getRoleId());
            refreshTable(); // Gọi lại loadRoles() ngay lập tức
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleEditSelectedRole() {
        Role selectedRole = roleTable.getSelectionModel().getSelectedItem();
        if (selectedRole == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Role Selected");
            alert.setContentText("Please select a role to edit.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/users/editRole.fxml"));
            Parent root = loader.load();

            EditRoleController controller = loader.getController();
            controller.setRole(selectedRole);

            Stage stage = new Stage();
            stage.setTitle("Edit Role - " + selectedRole.getRoleName());
            stage.setScene(new Scene(root));
            stage.show();

            // Cập nhật danh sách quyền sau khi cửa sổ đóng
            stage.setOnHidden(event -> loadPermissionsForRole(selectedRole.getRoleId()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackAction() {
        if (mainController != null) {
            try {
                mainController.showUsersPage(); // Gọi phương thức showUsersPage() trong MainController
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
