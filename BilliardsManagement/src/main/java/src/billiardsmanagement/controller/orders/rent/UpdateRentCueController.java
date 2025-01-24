package src.billiardsmanagement.controller.orders.rent;

import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
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
    private ComboBox<String> promotionComboBox;

    @FXML
    private CheckBox promotionCheckBox;

    private RentCue rentCue;

    @FXML
    public void initialize() {
        ArrayList<String> promotionList = (ArrayList<String>) PromotionDAO.getAllPromotionsNameByList();

        // Populate promotion combo box
        promotionComboBox.getItems().clear();
        promotionComboBox.getItems().addAll(promotionList);
    }

    @FXML
    public void updateRentCue() {
        System.out.println("PromotionID = "+promotionId);
        System.out.println("RentCueID = "+rentCueId);
        try {
            String promotionName = promotionComboBox.getValue();
            if(promotionName==null){
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("No Product Selected");
                alert.setHeaderText("Product Selection Required");
                alert.setContentText("Please select a promotion before updating the rent cue.");
                alert.showAndWait();
                return;
            }
            
            RentCue rentCue = new RentCue();
            rentCue.setPromotionId(PromotionDAO.getPromotionIdByName(promotionName));
            rentCue.setRentCueId(rentCueId);


            boolean success = RentCueDAO.updateRentCue(rentCue);
            if(success) {
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Update Successful");
                successAlert.setHeaderText("Promotion Updated");
                successAlert.setContentText("The promotion for the rent cue has been successfully updated.");
                successAlert.showAndWait();
                
                // Close the current window after successful update
                Stage stage = (Stage) promotionComboBox.getScene().getWindow();
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

    @FXML
    public void cancelUpdate() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) promotionComboBox.getScene().getWindow();
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
