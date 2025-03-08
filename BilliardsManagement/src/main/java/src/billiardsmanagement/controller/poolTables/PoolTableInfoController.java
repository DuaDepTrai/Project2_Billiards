package src.billiardsmanagement.controller.poolTables;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import src.billiardsmanagement.dao.PoolTableDAO;
import src.billiardsmanagement.model.PoolTable;
import src.billiardsmanagement.service.NotificationService;
import src.billiardsmanagement.model.NotificationStatus;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;

public class PoolTableInfoController {
    @FXML
    private TextField nameField;

    @FXML
    private TextField categoryField;

    @FXML
    private Button updateButton;

    @FXML
    private Button removeButton;

    private PoolTable poolTable;
    private PoolTableDAO poolTableDAO = new PoolTableDAO();

    public void setPoolTable(PoolTable table) {
        this.poolTable = table;

        // Populate fields with read-only information
        nameField.setText(table.getName());
        categoryField.setText(table.getCatePooltableName());

        // Enable/disable buttons based on table status
        boolean isAvailable = "available".equalsIgnoreCase(table.getStatus());
        updateButton.setDisable(!isAvailable);
        removeButton.setDisable(!isAvailable);

        // Make fields read-only
        nameField.setEditable(false);
        categoryField.setEditable(false);
    }

    @FXML
    private void handleUpdate() {
        try {
            // Since fields are read-only, we'll just show the update dialog
            Stage currentStage = (Stage) nameField.getScene().getWindow();
            currentStage.close();

            // Show the update dialog
            showUpdateDialog(poolTable);

        } catch (Exception e) {
            NotificationService.showNotification("Error", "Failed to open update dialog: " + e.getMessage(),
                    NotificationStatus.Error);
        }
    }

    @FXML
    private void handleRemove() {
        try {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Deletion");
            confirmation.setHeaderText("Delete Pool Table");
            confirmation
                    .setContentText("Are you sure you want to delete this pool table: " + poolTable.getName() + "?");

            if (confirmation.showAndWait().get() == ButtonType.OK) {
                poolTableDAO.removeTable(poolTable.getTableId());
                NotificationService.showNotification("Success", "Pool table removed successfully",
                        NotificationStatus.Success);
                closeDialog();
            }
        } catch (Exception e) {
            NotificationService.showNotification("Error", "Failed to remove pool table: " + e.getMessage(),
                    NotificationStatus.Error);
        }
    }

    private void showUpdateDialog(PoolTable table) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/src/billiardsmanagement/pooltables/editPoolTable.fxml"));
            Parent root = loader.load();

            UpdatePoolTableController controller = loader.getController();
            controller.setPoolTable(table);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Edit Table");

            Scene scene = new Scene(root);
            dialog.setScene(scene);
            dialog.showAndWait();

        } catch (IOException e) {
            NotificationService.showNotification("Error", "Failed to open update dialog: " + e.getMessage(),
                    NotificationStatus.Error);
        }
    }

    private void closeDialog() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}