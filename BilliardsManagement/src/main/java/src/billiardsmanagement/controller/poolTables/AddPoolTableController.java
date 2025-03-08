package src.billiardsmanagement.controller.poolTables;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import src.billiardsmanagement.dao.CatePooltableDAO;
import src.billiardsmanagement.dao.PoolTableDAO;
import src.billiardsmanagement.model.CatePooltable;
import src.billiardsmanagement.model.PoolTable;
import src.billiardsmanagement.service.NotificationService;
import src.billiardsmanagement.model.NotificationStatus;

import java.util.ArrayList;

public class AddPoolTableController {
    @FXML
    private TextField addNameField;
    @FXML
    private ComboBox<String> addCategoryCombo;
    @FXML
    private ComboBox<String> addStatusCombo;

    private PoolTableDAO poolTableDAO;
    private ArrayList<CatePooltable> catePooltablesList;

    @FXML
    public void initialize() {
        poolTableDAO = new PoolTableDAO();
        catePooltablesList = new ArrayList<>();
        catePooltablesList.addAll(CatePooltableDAO.getAllCategories());

        // Initialize form fields
        addStatusCombo.getItems().addAll("Available", "Ordered", "Playing");
        addStatusCombo.setValue("Available"); // Set default value
        addCategoryCombo.getItems().setAll(catePooltablesList.stream().map(CatePooltable::getName).toList());
    }

    @FXML
    private void handleAddTableSave() {
        try {
            String name = addNameField.getText();
            String category = addCategoryCombo.getValue();
            String status = addStatusCombo.getValue();

            // Validate input
            if (name == null || name.trim().isEmpty()) {
                NotificationService.showNotification("Warning", "Please enter a table name", NotificationStatus.Warning);
                return;
            }
            if (category == null) {
                NotificationService.showNotification("Warning", "Please select a category", NotificationStatus.Warning);
                return;
            }

            // Get category ID from selected category name
            int categoryId = catePooltablesList.stream()
                .filter(cat -> cat.getName().equals(category))
                .findFirst()
                .map(CatePooltable::getId)
                .orElse(1);

            // Create and save new table
            PoolTable newTable = new PoolTable(0, name, status, categoryId, category, 0.0);
            int newId = poolTableDAO.addTable(newTable);
            newTable.setTableId(newId);

            // Close dialog
            ((Stage) addNameField.getScene().getWindow()).close();
            
            NotificationService.showNotification("Success", "Table added successfully", NotificationStatus.Success);
        } catch (Exception e) {
            NotificationService.showNotification("Error", "Failed to add table: " + e.getMessage(), NotificationStatus.Error);
        }
    }

    @FXML
    private void handleAddTableCancel() {
        ((Stage) addNameField.getScene().getWindow()).close();
    }
} 