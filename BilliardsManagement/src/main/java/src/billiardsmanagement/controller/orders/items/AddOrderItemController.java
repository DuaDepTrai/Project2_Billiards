package src.billiardsmanagement.controller.orders.items;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import src.billiardsmanagement.dao.OrderItemDAO;
import src.billiardsmanagement.dao.ProductDAO;
import src.billiardsmanagement.dao.PromotionDAO;
import src.billiardsmanagement.model.DatabaseConnection;
import src.billiardsmanagement.model.OrderItem;
import src.billiardsmanagement.model.Pair;

import java.sql.*;
import java.util.ArrayList;
import java.util.Objects;

public class AddOrderItemController {
    private int orderId;
    private int promotionId;

    @FXML
    private TextField quantityTextField;
    @FXML
    private ComboBox<String> productNameComboBox;

    @FXML
    public void initialize(){
        ArrayList<String> list = new ArrayList<>();
        for (String s : Objects.requireNonNull(ProductDAO.getAllProductsName())) {
            if (!s.endsWith("Rent")) list.add(s);
        }
//        list.forEach(System.out::println);
        productNameComboBox.getItems().setAll(list);
    }
    
    @FXML
    public void saveOrderItem(ActionEvent event) {
        try{
            if(!(orderId>0)) throw new Exception("Lỗi : Không tìm thấy Order ! Vui lòng thử lại.");

            String selectedProductName = productNameComboBox.getSelectionModel().getSelectedItem();
            if(selectedProductName==null) throw new IllegalArgumentException("Bạn chưa chọn sản phẩm !");

            int quantity;
            try{
                quantity = Integer.parseInt(quantityTextField.getText());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Số lượng bạn nhập vào không hợp lệ, hãy thử lại !");
            }

            if(quantity<=0) throw new IllegalArgumentException("Số lượng phải lớn hơn 0, hãy thử lại !");

            Pair<Integer, Double> productPair = ProductDAO.getProductIdAndPriceByName(selectedProductName);
            if(productPair==null) throw new SQLException("Lỗi kết nối : Không thể kết nối với Database. Hãy thử lại sau !");
            
            int productId = productPair.getFirstValue();
            double productPrice = productPair.getSecondValue();
            // divide

            for(OrderItem orderItem : OrderItemDAO.getForEachOrderItem(orderId)){
                if(orderItem.getProductId()==productId){
                    OrderItem newItem = new OrderItem();
                    newItem.setOrderId(orderId);
                    newItem.setOrderItemId(orderItem.getOrderItemId());
                    newItem.setQuantity(orderItem.getQuantity()+quantity);
                    newItem.setProductId(orderItem.getProductId());

                    if(orderItem.getPromotionDiscount()>1) newItem.setNetTotal(orderItem.getNetTotal()+(quantity*productPrice)-((orderItem.getPromotionDiscount()*quantity*productPrice)/100));
                    else newItem.setNetTotal(orderItem.getNetTotal()+quantity*productPrice);

                    newItem.setSubTotal(orderItem.getSubTotal()+quantity*productPrice);
                    OrderItemDAO.addOrderItemDuplicate(newItem);
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Thông báo");
                    alert.setHeaderText(null);
                    alert.setContentText("Một Order Item đã được thêm vào thành công !");
                    alert.showAndWait();

                    Stage stage = (Stage) productNameComboBox.getScene().getWindow();
                    stage.close();
                    return;
                }
            }
            
            double subTotal;
            double netTotal;
            OrderItem orderItem;
            if(promotionId>0){
                double discount = PromotionDAO.getPromotionDiscountById(promotionId);
                if(discount==-1) throw new SQLException("Lỗi kết nối : Không thể kết nối với Database. Hãy thử lại sau !");
                subTotal = quantity * productPrice;
                netTotal = subTotal - (subTotal * (discount/100));
                orderItem = new OrderItem(orderId,productId,quantity,netTotal,subTotal,promotionId);
            }
            else{
                subTotal = quantity * productPrice;
                netTotal = subTotal;
                orderItem = new OrderItem(orderId,productId,quantity,netTotal,subTotal,-1);
            }

            OrderItemDAO.addOrderItem(orderItem);

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Thông báo");
            alert.setHeaderText(null);
            alert.setContentText("Một Order Item đã được thêm vào thành công !");
            alert.showAndWait();

            Stage stage = (Stage) productNameComboBox.getScene().getWindow();
            stage.close();
        }
        catch(IllegalArgumentException illegal){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi !");
            alert.setHeaderText(illegal.getMessage());
            alert.setContentText(illegal.getMessage());
            alert.showAndWait();
        }
        catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi !");
            alert.setHeaderText(e.getMessage());
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    public int getOrderId() {
        return orderId;
    }

    public AddOrderItemController setOrderId(int orderId) {
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


