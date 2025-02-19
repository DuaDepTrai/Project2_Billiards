package src.billiardsmanagement.controller.orders.rent;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import src.billiardsmanagement.dao.CategoryDAO;
import src.billiardsmanagement.dao.ProductDAO;
import src.billiardsmanagement.dao.PromotionDAO;
import src.billiardsmanagement.dao.RentCueDAO;
import src.billiardsmanagement.model.Pair;
import src.billiardsmanagement.model.RentCue;
import src.billiardsmanagement.model.NotificationService;
import src.billiardsmanagement.model.NotificationStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AddRentCueController {
    private int orderID;

    @FXML
    protected TextField productNameAutoCompleteText;
    private AutoCompletionBinding<String> productNameAutoBinding;
    protected ArrayList<String> productNameTrimmed;

    @FXML
    protected TextField promotionNameAutoCompleteText;
    private AutoCompletionBinding<String> promotionNameAutoBinding;
    protected ArrayList<String> promotionNameTrimmed;
    protected Map<String,String> productCategoryMap;

    @FXML
    private TextField quantityTextField;
    private RentCueDAO rentCueDAO = new RentCueDAO();

    @FXML
    private void initialize() {
        String rentCueCategory = "Cues-rent";
        productCategoryMap = CategoryDAO.getProductAndCategoryUnitMap();

        productNameTrimmed = new ArrayList<>();
        List<Pair<String, Integer>> list = ProductDAO.getAllProductNameAndQuantity();
        ArrayList<String> productList = new ArrayList<>();

        for (Pair<String, Integer> s : list) {
            String str = s.getFirstValue();
            int quant = s.getSecondValue();
            if (productCategoryMap.get(str).equalsIgnoreCase(rentCueCategory) && quant > 0) {
                productNameTrimmed.add(str);
                str = str + "  / " + quant + " in stock";
                productList.add(str);
            }
        }

        AutoCompletionBinding<String> productNameAutoBinding = TextFields.bindAutoCompletion(productNameAutoCompleteText, productList);
        HandleTextFieldClick(productNameAutoBinding, productList, productNameAutoCompleteText, productNameTrimmed);
        productNameAutoBinding.setVisibleRowCount(7);
        productNameAutoBinding.setHideOnEscape(true);

        promotionNameTrimmed = new ArrayList<>();
        ArrayList<String> pList = (ArrayList<String>) PromotionDAO.getAllPromotionsNameByList();
        ArrayList<String> promotionList = new ArrayList<>();
        if (pList != null) {
            for (String s : pList) {
                promotionList.add(s);
                promotionNameTrimmed.add(s);
            }
        }

        AutoCompletionBinding<String> promotionNameAutoBinding = TextFields.bindAutoCompletion(promotionNameAutoCompleteText, promotionList);
        HandleTextFieldClick(promotionNameAutoBinding, promotionList, promotionNameAutoCompleteText, promotionNameTrimmed);
        promotionNameAutoBinding.setHideOnEscape(true);
        promotionNameAutoBinding.setVisibleRowCount(7);

        quantityTextField.setText("1");
    }

    @FXML
    private void addRentCue() {
        try {
            // Validate input
            String productName = productNameAutoCompleteText.getText().trim();
            String promotionName = promotionNameAutoCompleteText.getText().trim();
            String quantityStr = quantityTextField.getText().trim();

            if (productName.isBlank()) {
                NotificationService.showNotification("Validation Error", "Please select a product.", NotificationStatus.Error);
                return;
            }
            if (!productNameTrimmed.contains(productName)) {
                NotificationService.showNotification("Product Error", "The product name you provided is not found!", NotificationStatus.Error);
                return;
            }

            int quantity;
            if (quantityStr.isBlank()) {
                quantity = 1;
            } else {
                try {
                    quantity = Integer.parseInt(quantityStr);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            // Get product quantity from database
            Integer availableQuantity = ProductDAO.getProductQuantityByName(productName);
            if (availableQuantity == null) {
                NotificationService.showNotification("Error", "Unable to retrieve product quantity.", NotificationStatus.Error);
                return;
            }

            if (quantity > availableQuantity) {
                NotificationService.showNotification("Error", "Requested quantity exceeds available stock.", NotificationStatus.Error);
                return;
            }

            // Get product and promotion IDs
            Integer productId = ProductDAO.getProductIdByName(productName);
            Integer promotionId = !promotionName.isEmpty() ? PromotionDAO.getPromotionIdByName(promotionName) : null;

            if (productId == null) {
                NotificationService.showNotification("Error", "Product ID not found.", NotificationStatus.Error);
                return;
            }

            boolean success = false;
            for (int i = 1; i <= quantity; i++) {
                RentCue rentCue = new RentCue();
                rentCue.setRentCueId(rentCueDAO.getNextRentCueId());
                rentCue.setOrderId(orderID);
                rentCue.setProductId(productId);
                rentCue.setStartTime(LocalDateTime.now());

                if (promotionId != null) {
                    rentCue.setPromotionId(promotionId);
                } else {
                    rentCue.setPromotionId(-1);
                }

                success = RentCueDAO.addRentCue(rentCue) && ProductDAO.dispatchItem(productName, 1);
                if (!success) {
                    throw new Exception("Database connection error. Try again later!");
                }
            }

            if (success) {
                NotificationService.showNotification("Success", "Rent cue added successfully!", NotificationStatus.Success);
                closeWindow();
            } else {
                NotificationService.showNotification("Error", "Failed to add rent cue.", NotificationStatus.Error);
            }

        } catch (IllegalArgumentException illegal) {
            NotificationService.showNotification("Error", illegal.getMessage(), NotificationStatus.Error);
        } catch (Exception e) {
            NotificationService.showNotification("Error", e.getMessage(), NotificationStatus.Error);
        }
    }

    public void HandleTextFieldClick(AutoCompletionBinding<String> auto, ArrayList<String> list, TextField text, ArrayList<String> trimmedList) {
        auto.setOnAutoCompleted(autoCompletionEvent -> {
            String finalText = autoCompletionEvent.getCompletion();
            text.setText(finalText.trim().split("/")[0].trim());
        });

        text.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                auto.setUserInput(" ");
                return;
            }
            if (!newValue) {
                String input = text.getText();
                input = input == null ? "" : input.trim();
                if (input.isBlank() || !trimmedList.contains(input)) {
                    text.setText("");
                } else {
                    text.setText(input);
                }
            }
        });
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) productNameAutoCompleteText.getScene().getWindow();
        stage.close();
    }

    public void setOrderId(int orderID) {
        this.orderID = orderID;
    }
}
