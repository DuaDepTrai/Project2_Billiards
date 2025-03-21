package src.billiardsmanagement.controller.poolTables;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.controlsfx.control.textfield.TextFields;
import src.billiardsmanagement.dao.PoolTableDAO;
import src.billiardsmanagement.dao.CatePooltableDAO;
import src.billiardsmanagement.model.PoolTable;
import src.billiardsmanagement.model.CatePooltable;
import src.billiardsmanagement.service.NotificationService;
import src.billiardsmanagement.model.NotificationStatus;
import java.util.List;

public class PoolTableInfoController {
    @FXML
    private Label notifyLabel;

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
    private PoolTableController poolTableController;
    private List<String> currentTableNameList;
    private List<String> catePooltableShortNameList;

    public void initializePoolInfo() {
        poolTableDAO = new PoolTableDAO();
        setupCategoryComboBox();

        // Setup Validate Inline
//        if(currentTable.getStatus().equalsIgnoreCase("Available")){
//            notifyLabel.setText("Enter a new name for this table");
//            notifyLabel.setStyle("-fx-text-fill: #FF9D23;"); // Red color
//            updateButton.setDisable(true);
//        }
        setupNameField();
    }

    private void setupNameField() {
        List<String> suggestionList = currentTableNameList.stream()
                .map(name -> name.replaceAll("\\s*\\d+\\s*", "")) // Remove numbers
                .map(String::trim)
                .map(str -> str + " ")// Trim any extra spaces
                .distinct() // Ensure unique names
                .toList();
        
        TextFields.bindAutoCompletion(nameField, suggestionList);

        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                // Provide feedback for empty name
                notifyLabel.setText("Please enter a name for the table.");
                notifyLabel.setStyle("-fx-text-fill: #DB2B3D;"); // Red color
                updateButton.setDisable(true);
            } else if (newValue.equalsIgnoreCase(currentTable.getName())){
                notifyLabel.setText("Nothing changed in table name.");
                notifyLabel.setStyle("-fx-text-fill: #FF9D23;"); // Red color
                updateButton.setDisable(true);
            }
            else if (currentTableNameList.contains(newValue)) {
                // Provide feedback for existing name
                notifyLabel.setText("This table name already exists!");
                notifyLabel.setStyle("-fx-text-fill: #DB2B3D;"); // Red color
                updateButton.setDisable(true);
            } else {
                // Name is valid
                notifyLabel.setText("This table name is valid.");
                notifyLabel.setStyle("-fx-text-fill: #28BE8E;"); // Green color
                updateButton.setDisable(false);
            }
        });
    }


    private void setupCategoryComboBox() {
        try {
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
            CatePooltable selectedCategory = null;
            for (CatePooltable category : categoryComboBox.getItems()) {
                System.out.println("Checking category: " + category.getName() + " with ID: " + category.getId() +
                        ", Table Category ID: " + currentTable.getCatePooltableId());
                if (category.getId() == currentTable.getCatePooltableId()) {
                    System.out.println("Found category: " + category.getName());
                    selectedCategory = category;
                    break;
                }
            }

            // Set the selected category in the combo box
            if (selectedCategory != null) {
                categoryComboBox.setValue(selectedCategory);
            } else {
                System.out.println("No matching category found for ID: " + currentTable.getCatePooltableId());
            }

            // Enable/disable buttons based on table availability
            boolean isAvailable = "Available".equals(table.getStatus());
            nameField.setDisable(!isAvailable);
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

            if (poolTableController != null) {
                poolTableController.hidePoolPopup();
                poolTableController.resetTableListAndTableNameList();
                poolTableController.handleViewAllTables();
            }

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
                if (poolTableController != null) {
                    poolTableController.hidePoolPopup();
                    poolTableController.resetTableListAndTableNameList();
                    poolTableController.handleViewAllTables();
                }
            } catch (Exception e) {
                NotificationService.showNotification("Error", "Failed to remove pool table: " + e.getMessage(),
                        NotificationStatus.Error);
            }
        }
    }

    public void setPoolTableController(PoolTableController poolTableController) {
        this.poolTableController = poolTableController;
    }

    public void setTableNameList(List<String> tableNameList) {
        this.currentTableNameList = tableNameList;
    }

    public void setCatePooltableComboBox(List<CatePooltable> catePooltableComboBox){
        this.categoryComboBox.getItems().clear();
        this.categoryComboBox.getItems().addAll(catePooltableComboBox);
    }

}