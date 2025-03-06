package src.billiardsmanagement.controller.pooltables.catepooltables;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import src.billiardsmanagement.dao.CatePooltableDAO;
import src.billiardsmanagement.model.CatePooltable;
import src.billiardsmanagement.service.NotificationService;
import src.billiardsmanagement.model.NotificationStatus;

public class UpdateCategoryPooltableController {
    @FXML
    private TextField priceField;

    private CatePooltable category;

    public void setCatePooltable(CatePooltable category) {
        this.category = category;
        priceField.setText(String.format("%.2f", category.getPrice()));
    }

    @FXML
    private void handleConfirm() {
        try {
            String priceText = priceField.getText();

            // Validate input
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

            // Update category
            category.setPrice(price);
            CatePooltableDAO.updateCategory(category);

            // Close dialog
            ((Stage) priceField.getScene().getWindow()).close();
            NotificationService.showNotification("Success", "Category updated successfully", NotificationStatus.Success);
        } catch (Exception e) {
            NotificationService.showNotification("Error", "Failed to update category: " + e.getMessage(), NotificationStatus.Error);
        }
    }

    @FXML
    private void handleClose() {
        ((Stage) priceField.getScene().getWindow()).close();
    }
} 