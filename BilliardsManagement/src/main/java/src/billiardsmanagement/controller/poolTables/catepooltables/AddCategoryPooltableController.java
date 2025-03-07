package src.billiardsmanagement.controller.poolTables.catepooltables;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import src.billiardsmanagement.dao.CatePooltableDAO;
import src.billiardsmanagement.model.CatePooltable;
import src.billiardsmanagement.service.NotificationService;
import src.billiardsmanagement.model.NotificationStatus;

public class AddCategoryPooltableController {
    @FXML
    private TextField nameField;
    @FXML
    private TextField shortNameField;
    @FXML
    private TextField priceField;

    @FXML
    private void handleConfirm() {
        try {
            // Validate input
            if (!validateInput()) {
                return;
            }

            String name = nameField.getText().trim();
            String shortName = shortNameField.getText().trim();
            double price = Double.parseDouble(priceField.getText().trim());

            // Create and save new category
            CatePooltable newCategory = new CatePooltable(0, name, shortName, price);
            int newId = CatePooltableDAO.addCategory(newCategory);

            if (newId > 0) {
                NotificationService.showNotification("Success", 
                    "Category added successfully", 
                    NotificationStatus.Success);
                ((Stage) nameField.getScene().getWindow()).close();
            } else {
                NotificationService.showNotification("Error", 
                    "Failed to add category", 
                    NotificationStatus.Error);
            }
        } catch (Exception e) {
            e.printStackTrace();
            NotificationService.showNotification("Error", 
                "Failed to add category: " + e.getMessage(), 
                NotificationStatus.Error);
        }
    }

    private boolean validateInput() {
        // Validate name
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            NotificationService.showNotification("Validation Error", 
                "Please enter a category name", 
                NotificationStatus.Warning);
            return false;
        }

        // Validate short name
        if (shortNameField.getText() == null || shortNameField.getText().trim().isEmpty()) {
            NotificationService.showNotification("Validation Error", 
                "Please enter a short name", 
                NotificationStatus.Warning);
            return false;
        }

        if (shortNameField.getText().trim().length() > 10) {
            NotificationService.showNotification("Validation Error", 
                "Short name must not exceed 10 characters", 
                NotificationStatus.Warning);
            return false;
        }

        // Validate price
        try {
            double price = Double.parseDouble(priceField.getText().trim());
            if (price < 0) {
                NotificationService.showNotification("Validation Error", 
                    "Price cannot be negative", 
                    NotificationStatus.Warning);
                return false;
            }
        } catch (NumberFormatException e) {
            NotificationService.showNotification("Validation Error", 
                "Please enter a valid price", 
                NotificationStatus.Warning);
            return false;
        }

        return true;
    }

    @FXML
    private void handleClose() {
        ((Stage) nameField.getScene().getWindow()).close();
    }
} 