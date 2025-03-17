package src.billiardsmanagement.controller.poolTables.catepooltables;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import src.billiardsmanagement.dao.CatePooltableDAO;
import src.billiardsmanagement.model.CatePooltable;
import src.billiardsmanagement.service.NotificationService;
import src.billiardsmanagement.model.NotificationStatus;

import java.util.ArrayList;
import java.util.List;

public class AddCategoryPooltableController {
    public Label notifyCategoryName;
    public Label notifyShortName;
    public Label notifyPrice;

    @FXML
    private TextField nameField;
    @FXML
    private TextField shortNameField;
    @FXML
    private TextField priceField;
    @FXML
    private Button confirmAddButton; // Add reference to the confirm button

    private List<String> existedCateNames;
    private List<String> existedCateShortNames;
    private List<CatePooltable> existedCategoriesList;

    public void initializeCatePooltables() {
        loadCatePooltableList();

        // Initialize notification labels
        confirmAddButton.setDisable(true);
        notifyCategoryName.setText("Please enter a category name.");
        notifyCategoryName.setStyle("-fx-text-fill: #DB2B3D;"); // Red color

        notifyShortName.setText("Please enter a short name.");
        notifyShortName.setStyle("-fx-text-fill: #DB2B3D;"); // Red color

        notifyPrice.setText("Please enter a valid price.");
        notifyPrice.setStyle("-fx-text-fill: #DB2B3D;"); // Red color

        // Add listeners to text fields
        nameField.textProperty().addListener((observable, oldValue, newValue) -> validateFields());
        shortNameField.textProperty().addListener((observable, oldValue, newValue) -> validateFields());
        priceField.textProperty().addListener((observable, oldValue, newValue) -> validateFields());
    }

    public void loadCatePooltableList(){
        existedCategoriesList = CatePooltableDAO.getAllCategories();
        existedCateNames = existedCategoriesList.stream().map(CatePooltable::getName).toList();
        existedCateShortNames = existedCategoriesList.stream().map(CatePooltable::getShortName).toList();
    }

    private void validateFields() {
        boolean isValid = true;

        // Check category name
        String name = nameField.getText().trim();
        if (name == null || name.isEmpty()) {
            notifyCategoryName.setText("Please enter a category name.");
            notifyCategoryName.setStyle("-fx-text-fill: #DB2B3D;"); // Red color
            isValid = false;
        } else if (existedCateNames.stream().anyMatch(existingName -> existingName.equalsIgnoreCase(name))) {
            notifyCategoryName.setText("Category name already exists.");
            notifyCategoryName.setStyle("-fx-text-fill: #DB2B3D;"); // Red color
            isValid = false;
        } else {
            notifyCategoryName.setText("This category name is valid!");
            notifyCategoryName.setStyle("-fx-text-fill: #28BE8E;"); // Green color
        }

        // Check short name
        String shortName = shortNameField.getText().trim();
        if (shortName == null || shortName.isEmpty()) {
            notifyShortName.setText("Please enter a short name.");
            notifyShortName.setStyle("-fx-text-fill: #DB2B3D;"); // Red color
            isValid = false;
        } else if (existedCateShortNames.stream().anyMatch(existingShortName -> existingShortName.equalsIgnoreCase(shortName))) {
            notifyShortName.setText("Short name already exists.");
            notifyShortName.setStyle("-fx-text-fill: #DB2B3D;"); // Red color
            isValid = false;
        } else {
            notifyShortName.setText("This short name is valid!");
            notifyShortName.setStyle("-fx-text-fill: #28BE8E;"); // Green color
        }

        // Check price
        double price = 0;
        try {
            price = Double.parseDouble(priceField.getText().trim());
            if (price <= 0) {
                notifyPrice.setText("Price must be greater than 0.");
                notifyPrice.setStyle("-fx-text-fill: #DB2B3D;"); // Red color
                isValid = false;
            } else {
                notifyPrice.setText("Price is valid!");
                notifyPrice.setStyle("-fx-text-fill: #28BE8E;"); // Green color
            }
        } catch (NumberFormatException e) {
            notifyPrice.setText("Please enter a valid price.");
            notifyPrice.setStyle("-fx-text-fill: #DB2B3D;"); // Red color
            isValid = false;
        }

        // Enable or disable the confirm button based on validation
        confirmAddButton.setDisable(!isValid);
    }

    @FXML
    private void handleConfirm() {
        try {
            // Validate input again before adding
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
                // close popup after using
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

}