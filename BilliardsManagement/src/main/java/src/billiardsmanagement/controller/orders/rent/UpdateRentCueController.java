package src.billiardsmanagement.controller.orders.rent;

import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import src.billiardsmanagement.model.Pair;
import src.billiardsmanagement.model.RentCue;
import src.billiardsmanagement.dao.PromotionDAO;
import src.billiardsmanagement.dao.RentCueDAO;

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

    public void initializeRentCue() {
        ArrayList<String> pList = (ArrayList<String>) PromotionDAO.getAllPromotionsNameByList();
        ArrayList<String> promotionList = new ArrayList<>();
        if(pList!=null){
            for(String s : pList){
                if(!s.contains(" ")){
                    s = s + " ";
                    promotionList.add(s);
                }
                else promotionList.add(s);
            }
            AutoCompletionBinding<String> promotionNameAutoBinding = TextFields.bindAutoCompletion(promotionNameAutoCompleteText, promotionList);
            HandleTextFieldClick(promotionNameAutoBinding, promotionList, promotionNameAutoCompleteText, promotionName);
            promotionNameAutoBinding.setHideOnEscape(true);
            promotionNameAutoBinding.setVisibleRowCount(7);
        }    }


    @FXML
    public void updateRentCue() {
        try {
            String promotionName = promotionNameAutoCompleteText.getText();
            if(promotionName.isBlank()){
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("No Product Selected");
                alert.setHeaderText("Product Selection Required");
                alert.setContentText("Please select a promotion before updating the rent cue.");
                alert.showAndWait();
                return;
            }

            RentCue rentCue = new RentCue();
            int receivedPromotionId = PromotionDAO.getPromotionIdByName(promotionName);
            rentCue.setPromotionId(receivedPromotionId);
            rentCue.setRentCueId(rentCueId);

            System.out.println("Received PromotionID = "+receivedPromotionId);
            System.out.println("RentCueID = "+rentCueId);

            boolean success = RentCueDAO.updateRentCue(rentCue);
            if(success) {
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Update Successful");
                successAlert.setHeaderText("Promotion Updated");
                successAlert.setContentText("The promotion for the rent cue has been successfully updated.");
                successAlert.showAndWait();

                // Close the current window after successful update
                Stage stage = (Stage) promotionNameAutoCompleteText.getScene().getWindow();
                stage.close();
            } else {
                throw new Exception("Failed to update rent cue promotion");
            }

        }
        catch (Exception e) {
            // Handle unexpected errors
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error !");
            alert.setHeaderText("Unexpected error happens. Please try again !");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    public void HandleTextFieldClick(AutoCompletionBinding<String> auto, ArrayList<String> list, TextField text, String name) {
        text.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue){
                auto.setUserInput(" ");
            }
            if (!newValue) { // Nếu mất focus
                String inputText = text.getText().trim();
                boolean check = false;
                if (inputText.isEmpty()) {
                    text.setText(name);
                    return;
                }

                for(String s : list){
                    if(inputText.equals(s)){
                        check =true;
                        break;
                    }
                }

                if(check) text.setText(inputText);
                else text.setText(name);
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

    public int getOrderID(){
        return orderID;
    }
    public void setOrderID(int orderID){
        this.orderID = orderID;
    }

    public int getPromotionId(){
        return promotionId;
    }

    public void setPromotionId(int promotionId) {
        this.promotionId = promotionId;
    }

    public int getRentCueId(){
        return rentCueId;
    }

    public void setRentCueId(int rentCueId){
        this.rentCueId = rentCueId;
    }

    public String getPromotionName(){
        return promotionName;
    }

    public void setPromotionName(String promotionName){
        this.promotionName = promotionName;
    }

    public void setRentCue(RentCue rentCue) {
        this.rentCue = rentCue;
    }

}
