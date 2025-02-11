package src.billiardsmanagement.controller.orders.rent;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import src.billiardsmanagement.dao.ProductDAO;
import src.billiardsmanagement.dao.PromotionDAO;
import src.billiardsmanagement.dao.RentCueDAO;
import src.billiardsmanagement.model.Pair;
import src.billiardsmanagement.model.RentCue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AddRentCueController {
    private int orderID;

    @FXML
    protected TextField productNameAutoCompleteText;
    private AutoCompletionBinding<String> productNameAutoBinding;

    @FXML
    protected TextField promotionNameAutoCompleteText;
    private AutoCompletionBinding<String> promotionNameAutoBinding;

    @FXML
    private TextField quantityTextField;
    private RentCueDAO rentCueDAO = new RentCueDAO();

    @FXML
    private void initialize() {
        List<Pair<String, Integer>> list = ProductDAO.getAllProductNameAndQuantity();
        ArrayList<String> productList = new ArrayList<>();

        for (Pair<String, Integer> s : list) {
            String str = s.getFirstValue();
            int quant = s.getSecondValue();
            if(!str.contains("Sale") && str.contains("Rent") && str.contains("Cue") && quant>0){
                str = str + " ";
                productList.add(str);
            }
//            if (!s.contains("Sale") && s.contains("Rent") && s.contains("Cue")) {
//                if (!s.contains(" ")) {
//                    s = s + " ";
//                    productList.add(s);
//                } else
//                    productList.add(s);
//            }
        }

        AutoCompletionBinding<String> productNameAutoBinding = TextFields.bindAutoCompletion(productNameAutoCompleteText, productList);
        HandleTextFieldClick(productNameAutoBinding, productList, productNameAutoCompleteText);
        productNameAutoBinding.setVisibleRowCount(7);

        productNameAutoBinding.setHideOnEscape(true);

        ArrayList<String> pList = (ArrayList<String>) PromotionDAO.getAllPromotionsNameByList();
        ArrayList<String> promotionList = new ArrayList<>();
        if (pList != null) {
            for (String s : pList) {
                s = s + " ";
                promotionList.add(s);
            }
            AutoCompletionBinding<String> promotionNameAutoBinding = TextFields.bindAutoCompletion(promotionNameAutoCompleteText, promotionList);
            HandleTextFieldClick(promotionNameAutoBinding, promotionList, promotionNameAutoCompleteText);
            promotionNameAutoBinding.setHideOnEscape(true);
            promotionNameAutoBinding.setVisibleRowCount(7);
        }

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
                showAlert("Validation Error", "Please select a product.");
                return;
            }

            int quantity;
            if (quantityStr.isBlank())
                quantity = 1;
            else {
                try {
                    quantity = Integer.parseInt(quantityStr);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            // Get product quantity from database
            Integer availableQuantity = ProductDAO.getProductQuantityByName(productName);
            if (availableQuantity == null) {
                showAlert("Error", "Unable to retrieve product quantity.");
                return;
            }

            if (quantity > availableQuantity) {
                showAlert("Error", "Requested quantity exceeds available stock.");
                return;
            }

            // Get product and promotion IDs
            Integer productId = ProductDAO.getProductIdByName(productName);
            Integer promotionId = !promotionName.isEmpty() ? PromotionDAO.getPromotionIdByName(promotionName) : null;

            if (productId == null) {
                showAlert("Error", "Product ID not found.");
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

                // dispatchItem after let people renting
                // Add rent cue to database
                success = RentCueDAO.addRentCue(rentCue) && ProductDAO.dispatchItem(productName, 1);
                if (!success)
                    throw new Exception("Database connection error. Try again later !");
            }

            if (success) {
                showAlert("Success", "Rent cue added successfully.");
                closeWindow();
            } else {
                showAlert("Error", "Failed to add rent cue.");
            }

        } catch (Exception e) {
            showAlert("Unexpected Error", "An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void HandleTextFieldClick(AutoCompletionBinding<String> auto, ArrayList<String> list, TextField text) {
        text.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                auto.setUserInput(" ");
            }
            if (!newValue) { // Nếu mất focus
                String inputText = text.getText().trim();
                boolean check = false;

                for (String s : list) {
                    if (inputText.equals(s)) {
                        check = true;
                        break;
                    }
                }

                if (check)
                    text.setText(inputText);
            }
        });
    }


    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
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

