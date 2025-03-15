package src.billiardsmanagement.controller.poolTables;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import src.billiardsmanagement.dao.CatePooltableDAO;
import src.billiardsmanagement.dao.PoolTableDAO;
import src.billiardsmanagement.model.CatePooltable;
import src.billiardsmanagement.model.PoolTable;
import src.billiardsmanagement.service.NotificationService;
import src.billiardsmanagement.model.NotificationStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AddPoolTableController {
    @FXML private Button saveButton;
    @FXML private Label notifyText;
    @FXML private Label categoryNotify;

    @FXML
    private TextField addNameField;
    @FXML
    private ComboBox<String> addCategoryCombo;


    private PoolTableDAO poolTableDAO;
    private ArrayList<CatePooltable> catePooltablesList;
    private List<String> currentTableNameList;
    private PoolTableController poolTableController;

    public void initializeAddPoolTable() {
        poolTableDAO = new PoolTableDAO();
        catePooltablesList = new ArrayList<>();
        catePooltablesList.addAll(CatePooltableDAO.getAllCategories());

        // Initialize form fields
        addCategoryCombo.getItems().setAll(catePooltablesList.stream().map(CatePooltable::getName).toList());

        notifyText.setText("Please enter a name for this pool table");
        notifyText.setStyle("-fx-text-fill: #DB2B3D;"); // Red color
        saveButton.setDisable(true);
        categoryNotify.setText("Please select a category !");
        categoryNotify.setStyle("-fx-text-fill: #DB2B3D;"); // Red color
        categoryNotify.setVisible(true);

        // ControlFX auto completion text field
        List<String> suggestionList = currentTableNameList.stream()
                .map(name -> name.replaceAll("\\s*\\d+\\s*", "")) // Remove numbers
                .map(String::trim)
                .map(str -> str + " ")// Trim any extra spaces
                .distinct() // Ensure unique names
                .toList();

        TextFields.bindAutoCompletion(addNameField, suggestionList);

        addNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isNameValid = newValue != null && !newValue.isEmpty() && !currentTableNameList.contains(newValue);

            if (!isNameValid) {
                if (newValue == null || newValue.isEmpty()) {
                    notifyText.setText("Please enter a name for this pool table");
                    notifyText.setStyle("-fx-text-fill: #DB2B3D;"); // Red color
                } else {
                    notifyText.setText("This Pool Table name has already exists !");
                    notifyText.setStyle("-fx-text-fill: #DB2B3D;"); // Red color
                }
                saveButton.setDisable(true);
            } else {
                notifyText.setText("This Pool Table name is valid !");
                notifyText.setStyle("-fx-text-fill: #28BE8E;"); // Green color
            }

            // Check category selection and set saveButton state
            updateSaveButtonState();
        });

        addCategoryCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                categoryNotify.setText("Please select a category !");
                categoryNotify.setStyle("-fx-text-fill: #DB2B3D;"); // Red color
                categoryNotify.setVisible(true);
            } else {
                categoryNotify.setVisible(false); // Hide notification
            }

            // Check name validity and update saveButton state
            updateSaveButtonState();
        });
    }

    private void updateSaveButtonState() {
        boolean isNameValid = !addNameField.getText().isEmpty() && !currentTableNameList.contains(addNameField.getText());
        boolean isCategorySelected = addCategoryCombo.getValue() != null;

        saveButton.setDisable(!(isNameValid && isCategorySelected));
    }

    @FXML
    private void handleAddTableSave() {
        try {
            String name = addNameField.getText();
            String category = addCategoryCombo.getValue();

            // Validate input
            if (name == null || name.trim().isEmpty()) {
                NotificationService.showNotification("Warning", "Please enter a table name", NotificationStatus.Warning);
                requestFocusOnPopup();
                return;
            }

            if (currentTableNameList.contains(name)) {
                NotificationService.showNotification("Warning", "This table name already exists. Choose another name!", NotificationStatus.Warning);
                requestFocusOnPopup();
                return;
            }

            if (category == null) {
                NotificationService.showNotification("Warning", "Please select a category", NotificationStatus.Warning);
                requestFocusOnPopup();
                return;
            }

            // Get category ID from selected category name
            int categoryId = catePooltablesList.stream()
                    .filter(cat -> cat.getName().equals(category))
                    .findFirst()
                    .map(CatePooltable::getId)
                    .orElse(1);

            // Create and save new table
            PoolTable newTable = new PoolTable(0, name, "Available", categoryId, category, 0.0);
            int newId = poolTableDAO.addTable(newTable);
            newTable.setTableId(newId);

            // Close dialog
            poolTableController.hidePoolPopup();
            poolTableController.resetTableListAndTableNameList();
            poolTableController.handleViewAllTables();  

            NotificationService.showNotification("Success", "Table added successfully", NotificationStatus.Success);
        } catch (Exception e) {
            NotificationService.showNotification("Error", "Failed to add table: " + e.getMessage(), NotificationStatus.Error);
        }
    }

    private void requestFocusOnPopup() {
        Platform.runLater(() -> {
            if (poolTableController != null && poolTableController.getCurrentPoolPopup().isShowing()) {
                poolTableController.getCurrentPoolPopup().requestFocus();
            }
        });
    }

    public void setTableNameList(List<String> tableNameList) {
        this.currentTableNameList = tableNameList;
    }

    public void setPoolController(PoolTableController poolTableController) {
        this.poolTableController = poolTableController;
    }
}