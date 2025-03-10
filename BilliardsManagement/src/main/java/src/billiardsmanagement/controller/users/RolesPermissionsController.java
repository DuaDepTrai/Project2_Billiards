package src.billiardsmanagement.controller.users;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import src.billiardsmanagement.controller.MainController;
import src.billiardsmanagement.dao.PermissionDAO;
import src.billiardsmanagement.dao.RolesPermissionsDAO;
import src.billiardsmanagement.model.Role;
import src.billiardsmanagement.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class RolesPermissionsController {
    @FXML
    private TableView<Role> roleTable;
    @FXML
    private TableColumn<Role, String> roleColumn;
    @FXML
    private TableView<String> permissionTable;
    @FXML
    private TableColumn<Role, Void> columnAction;

    @FXML
    private Button btnAddNewRole;

    @FXML
    private Button btnBack;  // Khai báo nút Back

    @FXML
    private TableColumn<String, String> permissionColumn;

    private ObservableList<Role> roleList = FXCollections.observableArrayList();
    private RolesPermissionsDAO rolesPermissionsDAO = new RolesPermissionsDAO();
    private User currentUser; // Lưu user đang đăng nhập

    private MainController mainController; // Biến để lưu MainController
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void initialize() {
        rolesPermissionsDAO = new RolesPermissionsDAO();
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("roleName"));

        columnAction.setCellFactory(createActionCellFactory());

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
//        btnBack.setOnAction(event -> handleBackAction());

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

    private Callback<TableColumn<Role, Void>, TableCell<Role, Void>> createActionCellFactory() {
        return column -> new TableCell<>() {
            private final HBox container = new HBox(10);
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();

            {
                FontAwesomeIconView editIcon = new FontAwesomeIconView(FontAwesomeIcon.PENCIL);
                editIcon.setSize("16");
                editButton.setGraphic(editIcon);
                editButton.getStyleClass().add("action-button");

                FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
                deleteIcon.setSize("16");
                deleteButton.setGraphic(deleteIcon);
                deleteButton.getStyleClass().add("action-button");

                container.setAlignment(Pos.CENTER);
                container.getChildren().addAll(editButton, deleteButton);

                editButton.setOnAction(event -> {
                    Role role = getTableView().getItems().get(getIndex());
                    handleEditSelectedRole(role);
                });

                deleteButton.setOnAction(event -> {
                    Role role = getTableView().getItems().get(getIndex());
                    confirmAndRemoveRole(role);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        };
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

    private void handleEditSelectedRole(Role role) {
        Role selectedRole = role;
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

    private void applyPermissions() {
        if (currentUser != null) {
            PermissionDAO permissionDAO = new PermissionDAO();
            List<String> permissions = permissionDAO.getUserPermissions(currentUser.getId());
            System.out.println("✅ Permissions: " + permissions);

//            addProductButton.setVisible(permissions.contains("add_product"));
//            editButton.setVisible(permissions.contains("add_product"));
//            deleteButton.setVisible(permissions.contains("add_product"));
//            stockUpButton.setVisible(permissions.contains("add_product"));
//            btnAddNewCategory.setVisible(permissions.contains("add_product"));
//            updateCategoryButton.setVisible(permissions.contains("add_product"));
//            removeCategoryButton.setVisible(permissions.contains("add_product"));
        } else {
            System.err.println("⚠️ Lỗi: currentUser bị null trong ProductController!");
        }
    }

    private User loggedInUser;

    public void setLoggedInUser(User user) {
        System.out.println("🟢 Gọi setCurrentUser() với user: " + (user != null ? user.getUsername() : "null"));

        this.loggedInUser = user;
        if (user != null) {
            System.out.println("🟢 Gọi setCurrentUser() với user: " + user.getUsername());
            System.out.println("🎯 Kiểm tra quyền sau khi truyền user...");
            List<String> permissions = user.getPermissionsAsString();
            System.out.println("🔎 Debug: Quyền sau khi truyền user = " + permissions);
            applyPermissions();
        } else {
            System.err.println("❌ Lỗi: currentUser vẫn null sau khi set!");
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
}
