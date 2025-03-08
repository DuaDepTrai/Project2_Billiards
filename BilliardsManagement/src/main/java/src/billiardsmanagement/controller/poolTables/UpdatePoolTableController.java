package src.billiardsmanagement.controller.poolTables;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import src.billiardsmanagement.dao.PoolTableDAO;
import src.billiardsmanagement.model.PoolTable;
import src.billiardsmanagement.service.NotificationService;
import src.billiardsmanagement.model.NotificationStatus;

public class UpdatePoolTableController {
    @FXML
    private TextField editNameField;
    @FXML
    private ComboBox<String> editStatusCombo;

    private PoolTableDAO poolTableDAO;
    private PoolTable poolTable;

    @FXML
    public void initialize() {
        poolTableDAO = new PoolTableDAO();
        editStatusCombo.getItems().addAll("Available", "Ordered", "Playing");
    }

    public void setPoolTable(PoolTable table) {
        this.poolTable = table;
        editNameField.setText(table.getName());
        editStatusCombo.setValue(table.getStatus());
    }

    @FXML
    private void handleEditTableSave() {
        try {
            String name = editNameField.getText();
            String status = editStatusCombo.getValue();

            // Validate input
            if (name == null || name.trim().isEmpty()) {
                NotificationService.showNotification("Warning", "Please enter a table name", NotificationStatus.Warning);
                return;
            }
            if (status == null) {
                NotificationService.showNotification("Warning", "Please select a status", NotificationStatus.Warning);
                return;
            }

            // Update table
            poolTable.setName(name);
            poolTable.setStatus(status);

            poolTableDAO.updateTable(poolTable);
            
            // Close dialog
            ((Stage) editNameField.getScene().getWindow()).close();
            
            NotificationService.showNotification("Success", "Table updated successfully", NotificationStatus.Success);
        } catch (Exception e) {
            NotificationService.showNotification("Error", "Failed to update table: " + e.getMessage(), NotificationStatus.Error);
        }
    }

    @FXML
    private void handleEditTableCancel() {
        ((Stage) editNameField.getScene().getWindow()).close();
    }
} 