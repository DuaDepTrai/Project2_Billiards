package src.billiardsmanagement.controller.poolTables;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import src.billiardsmanagement.dao.PoolTableDAO;
import src.billiardsmanagement.dao.CatePooltableDAO;
import src.billiardsmanagement.model.PoolTable;
import src.billiardsmanagement.model.CatePooltable;
import src.billiardsmanagement.service.NotificationService;
import src.billiardsmanagement.model.NotificationStatus;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.List;

public class PoolTableInfoController {
    @FXML
    private TextField nameField;

    @FXML
    private ComboBox<CatePooltable> categoryComboBox;

    @FXML
    private Button saveButton;

    @FXML
    private Button removeButton;

    private PoolTable poolTable;
    private PoolTableDAO poolTableDAO = new PoolTableDAO();
    private List<CatePooltable> categories;

    @FXML
    public void initialize() {
        // Load categories for combo box
        categories = CatePooltableDAO.getAllCategories();
        categoryComboBox.setItems(FXCollections.observableArrayList(categories));

        // Set custom display for category items
        categoryComboBox.setCellFactory(param -> new javafx.scene.control.ListCell<CatePooltable>() {
            @Override
            protected void updateItem(CatePooltable item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getShortName());
                }
            }
        });

        // Set custom display for selected item
        categoryComboBox.setButtonCell(new javafx.scene.control.ListCell<CatePooltable>() {
            @Override
            protected void updateItem(CatePooltable item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getShortName());
                }
            }
        });
    }

    public void setPoolTable(PoolTable table) {
        this.poolTable = table;

        // Populate fields
        nameField.setText(table.getName());

        // Find and select the current category
        categories.stream()
                .filter(cat -> cat.getName().equals(table.getCatePooltableName()))
                .findFirst()
                .ifPresent(cat -> categoryComboBox.setValue(cat));

        // Enable/disable fields based on table status
        boolean isAvailable = "available".equalsIgnoreCase(table.getStatus());
        nameField.setEditable(isAvailable);
        categoryComboBox.setDisable(!isAvailable);
        saveButton.setDisable(!isAvailable);
        removeButton.setDisable(!isAvailable);

        // Set ComboBox style
        categoryComboBox.setStyle("-fx-font-size: 14px;");
    }

    @FXML
    private void handleSave() {
        try {
            String name = nameField.getText().trim();
            CatePooltable selectedCategory = categoryComboBox.getValue();

            // Validate input
            if (name.isEmpty()) {
                NotificationService.showNotification("Error", "Table name cannot be empty", NotificationStatus.Error);
                return;
            }
            if (selectedCategory == null) {
                NotificationService.showNotification("Error", "Please select a category", NotificationStatus.Error);
                return;
            }

            // Update pool table
            poolTable.setName(name);
            poolTable.setCatePooltableId(selectedCategory.getId());
            poolTable.setCatePooltableName(selectedCategory.getName());
            poolTableDAO.updateTable(poolTable);

            NotificationService.showNotification("Success", "Pool table updated successfully",
                    NotificationStatus.Success);
            closeDialog();

        } catch (Exception e) {
            NotificationService.showNotification("Error", "Failed to update pool table: " + e.getMessage(),
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

    private void closeDialog() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}