package src.billiardsmanagement.controller.users;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import src.billiardsmanagement.dao.RolesPermissionsDAO;
import src.billiardsmanagement.model.TestDBConnection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

public class AddRoleController {
    @FXML
    private TextField txtRolename;

    private RolesPermissionsDAO rolespermissionsDAO = new RolesPermissionsDAO();

    @FXML
    private void handleAdd() {
        String rolename = txtRolename.getText();

        if (!isValidRolename(rolename)) {
            showAlert("Invalid Role", "Role must be at least 5 characters long and contain only letters, numbers, '.', or '_'.");
            return;
        }

        try (Connection connection = TestDBConnection.getConnection()) {
            // Kiểm tra role đã tồn tại hay chưa
            String checkRoleSql = "SELECT COUNT(*) FROM roles WHERE role_name = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkRoleSql);
            checkStmt.setString(1, rolename);
            ResultSet checkResult = checkStmt.executeQuery();

            if (checkResult.next() && checkResult.getInt(1) > 0) {
                showAlert("Duplicate Role", "Role already exists. Please choose a different Role name.");
                return; // Dừng lại nếu role đã tồn tại
            }
            // Hiển thị hộp thoại xác nhận
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Add Role");
            alert.setHeaderText("Are you sure you want to add this role?");
            alert.setContentText("Role Name: " + rolename);

            ButtonType buttonYes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
            ButtonType buttonNo = new ButtonType("No", ButtonBar.ButtonData.NO);
            alert.getButtonTypes().setAll(buttonYes, buttonNo);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == buttonYes) {
                // Thêm role mới
                rolespermissionsDAO.addRole(rolename);
                System.out.println("Role added successfully!");

                // Đóng cửa sổ Add Role
                Stage stage = (Stage) txtRolename.getScene().getWindow();
                stage.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) txtRolename.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean isValidRolename(String rolename) {
        return rolename.matches("^[a-zA-Z0-9_.\\s]{5,}$");
    }

}