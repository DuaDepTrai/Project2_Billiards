package src.billiardsmanagement.controller.poolTables.catepooltables;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import src.billiardsmanagement.dao.CatePooltableDAO;
import src.billiardsmanagement.model.CatePooltable;
import src.billiardsmanagement.model.NotificationStatus;
import src.billiardsmanagement.service.NotificationService;

import java.util.List;
import java.util.Optional;

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

    private List<String> currentCategoryNames;
    private List<String> currentShortNames;

    public void initializeUpdateCatePooltable() {
        // Initialize notification labels as empty
        notifyCategoryName.setText("");
        notifyShortName.setText("");
        notifyPrice.setText("");
        confirmUpdateButton.setDisable(true);

        // Add listeners for validation
        cateNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            String nv = newValue.trim();
            if (nv.isEmpty()) {
                setNotification(notifyCategoryName, "Category name cannot be empty", Color.RED);
            } else if (nv.equalsIgnoreCase(catePooltable.getName())) {
                setNotification(notifyCategoryName, "No changes in category pooltable name", Color.ORANGE);
            } else if (currentCategoryNames.stream().map(String::toLowerCase).toList().contains(nv.toLowerCase())) {
                setNotification(notifyCategoryName, "This CatePooltable name has already existed!", Color.RED);
            } else {
                setNotification(notifyCategoryName, "This CatePooltable name is valid", Color.GREEN);
            }
            updateConfirmButtonStatus();
        });

        shortNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            String nv = newValue.trim();
            if (nv.isEmpty()) {
                setNotification(notifyShortName, "Short name cannot be empty", Color.RED);
            } else if (nv.equalsIgnoreCase(catePooltable.getShortName())) {
                setNotification(notifyShortName, "No changes in short name", Color.ORANGE);
            } else if (currentShortNames.stream().map(String::toLowerCase).toList().contains(nv.toLowerCase())) {
                setNotification(notifyShortName, "This short name has already existed!", Color.RED);
            } else {
                setNotification(notifyShortName, "This short name is valid", Color.GREEN);
            }
            updateConfirmButtonStatus();
        });

        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            String nv = newValue.trim();
            if (nv.isEmpty()) {
                setNotification(notifyPrice, "Price cannot be empty", Color.RED);
            } else {
                setNotification(notifyPrice, "Valid price", Color.GREEN);
            }
            updateConfirmButtonStatus();
        });
    }

    private void setNotification(Label label, String message, Color color) {
        label.setText(message);
        label.setTextFill(color);
    }

    private void updateConfirmButtonStatus() {
        try {
            String name = cateNameField.getText();
            String shortName = shortNameField.getText();
            String price = priceField.getText();

            if (name.equalsIgnoreCase(catePooltable.getName()) && shortName.equalsIgnoreCase(catePooltable.getShortName()) && price.equalsIgnoreCase(String.valueOf(catePooltable.getPrice()))) {
                confirmUpdateButton.setDisable(true);
                return;
            }

            boolean isNameValid = name != null && !name.isEmpty() && !currentCategoryNames.stream().filter(c -> !c.equalsIgnoreCase(catePooltable.getName())).map(String::toLowerCase).toList().contains(name.toLowerCase());
            boolean isShortNameValid = shortName != null && !shortName.isEmpty() && !currentShortNames.stream().filter(c -> !c.equalsIgnoreCase(catePooltable.getShortName())).map(String::toLowerCase).toList().contains(shortName.toLowerCase());
            boolean isPriceValid = price != null && !price.isEmpty() && Double.parseDouble(price.trim()) > 0.0;

//            // Output checks for each validity with highlighted text
//            System.out.println("--- Is Name Valid: " + (isNameValid ? "✔️" : "❌") + " (Value: " + name + ")");
//            System.out.println("--- Is Short Name Valid: " + (isShortNameValid ? "✔️" : "❌") + " (Value: " + shortName + ")");
//            System.out.println("--- Is Price Valid: " + (isPriceValid ? "✔️" : "❌") + " (Value: " + price + ")");

            confirmUpdateButton.setDisable(!(isNameValid && isShortNameValid && isPriceValid));
            System.out.println("--- Is all valid? " + (isNameValid && isShortNameValid && isPriceValid));

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void setCatePooltable(CatePooltable catePooltable) {
        this.catePooltable = catePooltable;
        cateNameField.setText(catePooltable.getName());
        shortNameField.setText(catePooltable.getShortName());
        priceField.setText(String.valueOf(catePooltable.getPrice()));

//        // Validate initial values
//        validateCategoryName(catePooltable.getName());
//        validateShortName(catePooltable.getShortName());
//        validatePrice(String.valueOf(catePooltable.getPrice()));
    }

    @FXML
    private void handleConfirm() {
        String name = cateNameField.getText().trim();
        String shortName = shortNameField.getText().trim();
        String priceStr = priceField.getText().trim();

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Please Confirm");
        confirmAlert.setContentText("Are you sure you want to proceed with the category name : " + name);

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK){
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

    public void setCurrentCategoryNames(List<String> currentNames) {
        this.currentCategoryNames = currentNames;
    }

    public void setCurrentShortNames(List<String> shortNames) {
        this.currentShortNames = shortNames;
    }

} 