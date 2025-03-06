package src.billiardsmanagement.controller.pooltables.catepooltables;

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
    private TextField priceField;

    @FXML
    private void handleConfirm() {
        try {
            String name = nameField.getText();
            String priceText = priceField.getText();

            // Validate input
            if (name == null || name.trim().isEmpty()) {
                NotificationService.showNotification("Warning", "Please enter a category name", NotificationStatus.Warning);
                return;
            }
            if (priceText == null || priceText.trim().isEmpty()) {
                NotificationService.showNotification("Warning", "Please enter a price", NotificationStatus.Warning);
                return;
            }

            // Parse price
            double price;
            try {
                price = Double.parseDouble(priceText);
                if (price < 0) {
                    NotificationService.showNotification("Warning", "Price cannot be negative", NotificationStatus.Warning);
                    return;
                }
            } catch (NumberFormatException e) {
                NotificationService.showNotification("Warning", "Please enter a valid price", NotificationStatus.Warning);
                return;
            }

            // Create and save new category
            CatePooltable newCategory = new CatePooltable(0, name, price);
            int newId = CatePooltableDAO.addCategory(newCategory);

            if (newId > 0) {
                // Close dialog
                ((Stage) nameField.getScene().getWindow()).close();
                NotificationService.showNotification("Success", "Category added successfully", NotificationStatus.Success);
            } else {
                NotificationService.showNotification("Error", "Failed to add category", NotificationStatus.Error);
            }
        } catch (Exception e) {
            NotificationService.showNotification("Error", "Failed to add category: " + e.getMessage(), NotificationStatus.Error);
        }
    }

    @FXML
    private void handleClose() {
        ((Stage) nameField.getScene().getWindow()).close();
    }
} 