package src.billiardsmanagement.controller.poolTables;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import src.billiardsmanagement.dao.PoolTableDAO;
import src.billiardsmanagement.dao.CatePooltableDAO;
import src.billiardsmanagement.model.PoolTable;
import src.billiardsmanagement.model.CatePooltable;
import src.billiardsmanagement.service.NotificationService;
import src.billiardsmanagement.model.NotificationStatus;
import java.util.List;

public class PoolTableInfoController {

    @FXML
    private TextField nameField;

    @FXML
    private ComboBox<CatePooltable> categoryComboBox;

    @FXML
    private Button updateButton;

    @FXML
    private Button removeButton;

    private PoolTable currentTable;
    private PoolTableDAO poolTableDAO;
    private CatePooltableDAO catePooltableDAO;
    private PoolTableController mainController;
    private PoolTableController poolTableController;

    public void initialize() {
        poolTableDAO = new PoolTableDAO();
        catePooltableDAO = new CatePooltableDAO();
        setupCategoryComboBox();
    }

    public void setMainController(PoolTableController controller) {
        this.mainController = controller;
    }

    private void setupCategoryComboBox() {
        try {
            // Load categories
            List<CatePooltable> categories = CatePooltableDAO.getAllCategories();
            categoryComboBox.getItems().clear(); // Clear existing items first
            categoryComboBox.getItems().addAll(categories);

            // Set cell factory to display only short names
            categoryComboBox.setCellFactory(lv -> new ListCell<CatePooltable>() {
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

            // Set button cell to display only short names
            categoryComboBox.setButtonCell(new ListCell<CatePooltable>() {
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

            categoryComboBox.setStyle("-fx-font-size: 14px;");
        } catch (Exception e) {
            NotificationService.showNotification("Error", "Failed to load categories: " + e.getMessage(),
                    NotificationStatus.Error);
        }
    }

    public void setPoolTable(PoolTable table) {
        this.currentTable = table;
        nameField.setText(table.getName());

        try {
            // Find and select the current category
            for (CatePooltable category : categoryComboBox.getItems()) {
                if (category.getId() == table.getCatePooltableId()) {
                    categoryComboBox.setValue(category);
                    break;
                }
            }

            // Enable/disable buttons based on table availability
            boolean isAvailable = "Available".equals(table.getStatus());
            updateButton.setDisable(!isAvailable);
            removeButton.setDisable(!isAvailable);

            // Make fields read-only if table is not available
            nameField.setEditable(isAvailable);
            categoryComboBox.setDisable(!isAvailable);
        } catch (Exception e) {
            NotificationService.showNotification("Error", "Failed to load table information: " + e.getMessage(),
                    NotificationStatus.Error);
        }
    }

    @FXML
    private void handleUpdate() {
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

            // Update table properties
            currentTable.setName(name);
            currentTable.setCatePooltableId(selectedCategory.getId());
            currentTable.setCatePooltableName(selectedCategory.getName());
            currentTable.setPrice(selectedCategory.getPrice()); // Also update the price based on the new category

            // Update in database
            poolTableDAO.updateTable(currentTable);
            NotificationService.showNotification("Success", "Pool table updated successfully",
                    NotificationStatus.Success);

            if (mainController != null) {
                mainController.handleViewAllTables();
                mainController.hidePoolPopup();
            }
//            closeDialog();
        } catch (Exception e) {
            NotificationService.showNotification("Error", "Failed to update pool table: " + e.getMessage(),
                    NotificationStatus.Error);
        }
    }

    @FXML
    private void handleRemove() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText("Delete Pool Table");
        confirmation.setContentText("Are you sure you want to delete this pool table?");

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            try {
                poolTableDAO.removeTable(currentTable.getTableId());
                NotificationService.showNotification("Success", "Pool table removed successfully",
                        NotificationStatus.Success);
                if (mainController != null) {
                    mainController.handleViewAllTables();
                    mainController.hidePoolPopup();
                }
                closeDialog();
            } catch (Exception e) {
                NotificationService.showNotification("Error", "Failed to remove pool table: " + e.getMessage(),
                        NotificationStatus.Error);
            }
        }
    }

    private void closeDialog() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    public void setPoolTableController(PoolTableController poolTableController) {
        this.poolTableController = poolTableController;
    }
}