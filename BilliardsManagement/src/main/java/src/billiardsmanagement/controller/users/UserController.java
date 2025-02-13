package src.billiardsmanagement.controller.users;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import src.billiardsmanagement.controller.users.UpdateUserController;
import src.billiardsmanagement.dao.UserDAO;
import src.billiardsmanagement.model.User;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class UserController {
    @FXML
    private TableView<User> tableUsers;
    @FXML
    private TableColumn<User, Integer> columnId;
    @FXML
    private TableColumn<User, String> columnUsername;
    @FXML
    private TableColumn<User, String> columnPassword;
    @FXML
    private TableColumn<User, String> columnRole;
    @FXML
    private Button btnAddNewUser;
    @FXML
    private Button btnUpdateUser;
    @FXML
    private Button btnRemoveUser;

    private ObservableList<User> userList = FXCollections.observableArrayList();
    private UserDAO userDAO = new UserDAO();

    public void initialize() {
        columnId.setCellValueFactory(new PropertyValueFactory<>("id"));
        columnUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        columnRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        loadUsers();

        btnAddNewUser.setOnAction(event -> handleAddNewUser());
        btnUpdateUser.setOnAction(event -> handleUpdateSelectedUser());
        btnRemoveUser.setOnAction(event -> handleRemoveSelectedUser());
    }

    private void loadUsers() {
        try {
            userList.clear();
            userList.addAll(userDAO.getAllUsers());
            tableUsers.setItems(userList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddNewUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/users/addUser.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Add New USer");
            stage.setScene(new Scene(root));
            stage.show();
            stage.setOnHidden(event -> refreshTable());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshTable() {
        loadUsers();
    }

    private void handleRemoveSelectedUser() {
        User selectedUser = tableUsers.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No User Selected");
            alert.setContentText("Please select a user to remove");
            alert.showAndWait();
            return;
        }
        confirmAndRemoveUser(selectedUser);
    }

    private void confirmAndRemoveUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Remove");
        alert.setHeaderText("Are you sure you want to remove this user?");
        alert.setContentText("User: " + user.getUsername());

        ButtonType buttonYes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType buttonNo = new ButtonType("No", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(buttonYes, buttonNo);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == buttonYes) {
            removeUser(user);
        }
    }

    private void removeUser(User user) {
        try {
            userDAO.removeUser(user.getId());
            userList.remove(user);
            tableUsers.refresh();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleUpdateSelectedUser() {
        User selectedUser = tableUsers.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No User Selected");
            alert.setContentText("Please select a user to update");
            alert.showAndWait();
            return;
        }
        openUpdateWindow(selectedUser);
    }

    private void openUpdateWindow(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/users/updateUser.fxml"));
            Parent root = loader.load();
            UpdateUserController controller = loader.getController();
            controller.setUserData(user);

            Stage stage = new Stage();
            stage.setTitle("Update User");
            stage.setScene(new Scene(root));
            stage.show();
            stage.setOnHidden(event -> refreshTable());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}