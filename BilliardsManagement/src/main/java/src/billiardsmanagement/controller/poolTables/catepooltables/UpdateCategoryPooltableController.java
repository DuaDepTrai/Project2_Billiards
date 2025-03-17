package src.billiardsmanagement.controller.poolTables.catepooltables;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import src.billiardsmanagement.dao.CatePooltableDAO;
import src.billiardsmanagement.model.CatePooltable;
import src.billiardsmanagement.model.NotificationStatus;
import src.billiardsmanagement.service.NotificationService;

public class UpdateCategoryPooltableController {
    @FXML
    private TextField cateNameField;
    @FXML
    private TextField shortNameField;
    @FXML
    private TextField priceField;
    @FXML
    private Label notifyCategoryName;
    @FXML
    private Label notifyShortName;
    @FXML
    private Label notifyPrice;
    @FXML
    private Button confirmUpdateButton;

    private CatePooltable catePooltable;

    public void initialize() {
        // Add listeners for validation
        cateNameField.textProperty().addListener((observable, oldValue, newValue) -> validateCategoryName(newValue));
        shortNameField.textProperty().addListener((observable, oldValue, newValue) -> validateShortName(newValue));
        priceField.textProperty().addListener((observable, oldValue, newValue) -> validatePrice(newValue));

        // Initially disable confirm button
        updateConfirmButton();
    }

    private void validateCategoryName(String name) {
        if (name == null || name.trim().isEmpty()) {
            setNotification(notifyCategoryName, "Category name cannot be empty", Color.RED);
        } else if (name.length() > 50) {
            setNotification(notifyCategoryName, "Category name must be less than 50 characters", Color.RED);
        } else {
            setNotification(notifyCategoryName, "Valid category name", Color.GREEN);
        }
        updateConfirmButton();
    }

    private void validateShortName(String shortName) {
        if (shortName == null || shortName.trim().isEmpty()) {
            setNotification(notifyShortName, "Short name cannot be empty", Color.RED);
        } else if (shortName.length() > 10) {
            setNotification(notifyShortName, "Short name must be less than 10 characters", Color.RED);
        } else {
            setNotification(notifyShortName, "Valid short name", Color.GREEN);
        }
        updateConfirmButton();
    }

    private void validatePrice(String price) {
        if (price == null || price.trim().isEmpty()) {
            setNotification(notifyPrice, "Price cannot be empty", Color.RED);
        } else {
            try {
                double priceValue = Double.parseDouble(price);
                if (priceValue <= 0) {
                    setNotification(notifyPrice, "Price must be greater than 0", Color.RED);
                } else {
                    setNotification(notifyPrice, "Valid price", Color.GREEN);
                }
            } catch (NumberFormatException e) {
                setNotification(notifyPrice, "Price must be a valid number", Color.RED);
            }
        }
        updateConfirmButton();
    }

    private void setNotification(Label label, String message, Color color) {
        label.setText(message);
        label.setTextFill(color);
    }

    private void updateConfirmButton() {
        boolean isValid =
                notifyCategoryName.getTextFill().equals(Color.GREEN) &&
                        notifyShortName.getTextFill().equals(Color.GREEN) &&
                        notifyPrice.getTextFill().equals(Color.GREEN);
        confirmUpdateButton.setDisable(!isValid);
    }

    public void setCatePooltable(CatePooltable catePooltable) {
        this.catePooltable = catePooltable;
        cateNameField.setText(catePooltable.getName());
        shortNameField.setText(catePooltable.getShortName());
        priceField.setText(String.valueOf(catePooltable.getPrice()));
        
        // Validate initial values
        validateCategoryName(catePooltable.getName());
        validateShortName(catePooltable.getShortName());
        validatePrice(String.valueOf(catePooltable.getPrice()));
    }

    @FXML
    private void handleConfirm() {
        String name = cateNameField.getText().trim();
        String shortName = shortNameField.getText().trim();
        String priceStr = priceField.getText().trim();

        try {
            double price = Double.parseDouble(priceStr);
            
            // Update the category
            catePooltable.setName(name);
            catePooltable.setShortName(shortName);
            catePooltable.setPrice(price);
            CatePooltableDAO.updateCategory(catePooltable);
            
            NotificationService.showNotification("Success", 
                "Category updated successfully!", 
                NotificationStatus.Success);
            // Close the popup

        } catch (NumberFormatException e) {
            NotificationService.showNotification("Error", 
                "Invalid price format", 
                NotificationStatus.Error);
        } catch (Exception e) {
            NotificationService.showNotification("Error", 
                "Failed to update category: " + e.getMessage(), 
                NotificationStatus.Error);
        }
    }

} 