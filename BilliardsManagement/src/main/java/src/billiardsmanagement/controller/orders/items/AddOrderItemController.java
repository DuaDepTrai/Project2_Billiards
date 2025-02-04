package src.billiardsmanagement.controller.orders.items;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import src.billiardsmanagement.dao.OrderItemDAO;
import src.billiardsmanagement.dao.ProductDAO;
import src.billiardsmanagement.dao.PromotionDAO;
import src.billiardsmanagement.model.DatabaseConnection;
import src.billiardsmanagement.model.OrderItem;
import src.billiardsmanagement.model.Pair;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class AddOrderItemController  {
    @FXML
    protected TextField productNameAutoCompleteText;
    @FXML
    protected TextField promotionNameAutoCompleteText;

    private int orderId;
    private int promotionId;

    @FXML
    private TextField quantityTextField;

    @FXML
    public void initialize(){
        ArrayList<String> list = new ArrayList<>();
        for (String s : Objects.requireNonNull(ProductDAO.getAllProductsName())) {
            if (!s.endsWith("Rent")) list.add(s);
        }
        TextFields.bindAutoCompletion(productNameAutoCompleteText,list);

        ArrayList<String> promotionList = (ArrayList<String>) PromotionDAO.getAllPromotionsNameByList();
        TextFields.bindAutoCompletion(promotionNameAutoCompleteText,promotionList);
    }

    @FXML
    public void saveOrderItem(ActionEvent event){
        try{
            if(!(orderId>0)) throw new Exception("Error: Order not found ! Please try again.");

            String selectedProductName = productNameAutoCompleteText.getText();
            Pair<Integer, Double> productPair = ProductDAO.getProductIdAndPriceByName(selectedProductName);
            if (productPair==null) throw new SQLException("Connection Error: Can't connect to Database. Please try again later.");

            int productId = productPair.getFirstValue();
            double productPrice = productPair.getSecondValue();

            promotionId = PromotionDAO.getPromotionIdByName(promotionNameAutoCompleteText.getText());

            for(OrderItem orderItem : OrderItemDAO.getForEachOrderItem(orderId)){
                if (orderItem.getProductId()==productId){
                    OrderItem newItem = new OrderItem();
                    newItem.setOrderId(orderId);
                    newItem.setOrderItemId(orderItem.getOrderItemId());
                    newItem.setQuantity(orderItem.getQuantity()+Integer.parseInt(quantityTextField.getText()));
                    newItem.setProductId(orderItem.getProductId());

                    if (orderItem.getPromotionDiscount()>1)
                        newItem.setNetTotal(orderItem.getNetTotal()+(Integer.parseInt(quantityTextField.getText())*productPrice)-((orderItem.getPromotionDiscount()*Integer.parseInt(quantityTextField.getText())*productPrice)/100));
                    else
                        newItem.setNetTotal(orderItem.getNetTotal()+Integer.parseInt(quantityTextField.getText())*productPrice);

                    newItem.setSubTotal(orderItem.getSubTotal()+Integer.parseInt(quantityTextField.getText())*productPrice);
                    OrderItemDAO.addOrderItemDuplicate(newItem);

                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Notification");
                    alert.setHeaderText(null);
                    alert.setContentText("An Order Item has been added successfully !");
                    alert.showAndWait();

                    Stage stage = (Stage) promotionNameAutoCompleteText.getScene().getWindow();
                    stage.close();
                    return;
                 }
             }

            double subTotal;
            double netTotal;
            OrderItem orderItem;
            if(promotionId>0){
                double discount = PromotionDAO.getPromotionDiscountById(promotionId);
                if(discount==-1) throw new SQLException("Connection Error: Can't connect to Database. Please try again later.");
                subTotal = Integer.parseInt(quantityTextField.getText()) * productPrice;
                netTotal = subTotal - (subTotal * (discount/100));
                orderItem = new OrderItem(orderId,productId,Integer.parseInt(quantityTextField.getText()),netTotal,subTotal,promotionId);
             } else {
                subTotal = Integer.parseInt(quantityTextField.getText()) * productPrice;
                netTotal = subTotal;
                orderItem = new OrderItem(orderId,productId,Integer.parseInt(quantityTextField.getText()),netTotal,subTotal,-1);
             }

            OrderItemDAO.addOrderItem(orderItem);

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Notification");
            alert.setHeaderText(null);
            alert.setContentText("An Order Item has been added successfully !");
            alert.showAndWait();

            Stage stage = (Stage) promotionNameAutoCompleteText.getScene().getWindow();
            stage.close();
         } catch (IllegalArgumentException illegal){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error !");
            alert.setHeaderText(illegal.getMessage());
            alert.setContentText(illegal.getMessage());
            alert.showAndWait();
         } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error !");
            alert.setHeaderText(e.getMessage());
            alert.setContentText(e.getMessage());
            alert.showAndWait();
         }
    }

    public int getOrderId(){
        return orderId;
     }

    public AddOrderItemController setOrderId(int orderId){
        this.orderId = orderId;
        return this;
    }

    public int getPromotionId() {
        return promotionId;
    }

    public AddOrderItemController setPromotionId(int promotionId) {
        this.promotionId = promotionId;
        return this;
    }
}
