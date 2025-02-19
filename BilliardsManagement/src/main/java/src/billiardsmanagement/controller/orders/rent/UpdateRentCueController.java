package src.billiardsmanagement.controller.orders.rent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import src.billiardsmanagement.dao.CategoryDAO;
import src.billiardsmanagement.model.Pair;
import src.billiardsmanagement.model.RentCue;
import src.billiardsmanagement.dao.PromotionDAO;
import src.billiardsmanagement.dao.RentCueDAO;
import src.billiardsmanagement.model.NotificationService;
import src.billiardsmanagement.model.NotificationStatus;

public class UpdateRentCueController {

    private int orderID;
    private int promotionId;
    private String promotionName;

    private int rentCueId;

    @FXML
    protected TextField promotionNameAutoCompleteText;

    @FXML
    private CheckBox promotionCheckBox;

    private RentCue rentCue;
    private AutoCompletionBinding<String> autoBinding;

    protected Map<String, String> productCategoryMap;

    public void initializeRentCue() {
        String rentCueCategory = "Cues-rent";
        productCategoryMap = CategoryDAO.getProductAndCategoryUnitMap();

        ArrayList<String> pList = (ArrayList<String>) PromotionDAO.getAllPromotionsNameByList();
        ArrayList<String> promotionList = new ArrayList<>();
        if (pList != null) {
            for (String s : pList) {
                if (productCategoryMap.get(s) != null && productCategoryMap.get(s).equalsIgnoreCase(rentCueCategory)) {
                    s = s + " ";
                    promotionList.add(s);
                } else {
                    promotionList.add(s);
                }
            }

            AutoCompletionBinding<String> promotionNameAutoBinding = TextFields.bindAutoCompletion(promotionNameAutoCompleteText, promotionList);
            HandleTextFieldClick(promotionNameAutoBinding, promotionList, promotionNameAutoCompleteText, promotionName);
            promotionNameAutoBinding.setHideOnEscape(true);
            promotionNameAutoBinding.setVisibleRowCount(7);
        }
    }

    @FXML
    public void updateRentCue() {
        try {
            String promotionName = promotionNameAutoCompleteText.getText();
            if (promotionName == null || promotionName.isBlank()) {
                NotificationService.showNotification("No Promotion Selected", "Please select a promotion before updating the rent cue.", NotificationStatus.Warning);
                return;
            }

            RentCue rentCue = new RentCue();
            int receivedPromotionId = PromotionDAO.getPromotionIdByName(promotionName);
            rentCue.setPromotionId(receivedPromotionId);
            rentCue.setRentCueId(rentCueId);

            boolean success = RentCueDAO.updateRentCue(rentCue);
            if (success) {
                Stage stage = (Stage) promotionNameAutoCompleteText.getScene().getWindow();
                stage.close();
            } else {
                throw new Exception("Failed to update rent cue promotion");
            }

        } catch (Exception e) {
            NotificationService.showNotification("Error", "An unexpected error occurred. Please try again!", NotificationStatus.Error);
        }
    }

    public void HandleTextFieldClick(AutoCompletionBinding<String> auto, List<String> list, TextField text, String name) {
        text.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                auto.setUserInput(" ");
                return;
            }

            if (!newValue) {
                String inputText = text.getText();
                if (inputText == null || inputText.trim().isEmpty()) {
                    text.setText(name);
                    return;
                }

                boolean check = list.stream().anyMatch(inputText::equals);
                if (check) {
                    text.setText(inputText);
                } else {
                    text.setText(name);
                }
            }
        });
    }

    @FXML
    public void cancelUpdate() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) promotionNameAutoCompleteText.getScene().getWindow();
        stage.close();
    }

    public int getOrderID() {
        return orderID;
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    public int getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(int promotionId) {
        this.promotionId = promotionId;
    }

    public int getRentCueId() {
        return rentCueId;
    }

    public void setRentCueId(int rentCueId) {
        this.rentCueId = rentCueId;
    }

    public String getPromotionName() {
        return promotionName;
    }

    public void setPromotionName(String promotionName) {
        this.promotionName = promotionName;
    }

    public void setRentCue(RentCue rentCue) {
        this.rentCue = rentCue;
    }
}
