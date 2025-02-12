package src.billiardsmanagement.controller.orders.items;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Popup;
import javafx.stage.Stage;


import org.controlsfx.control.textfield.TextFields;
import src.billiardsmanagement.dao.OrderItemDAO;
import src.billiardsmanagement.dao.ProductDAO;
import src.billiardsmanagement.dao.PromotionDAO;
import src.billiardsmanagement.model.DatabaseConnection;
import src.billiardsmanagement.model.OrderItem;
import src.billiardsmanagement.model.Pair;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class AddOrderItemController  {
    @FXML
   private TextField productNameAutoCompleteText;
    @FXML
 private TextField promotionNameAutoCompleteText;

    private int orderId;
    private int promotionId;

    @FXML
    private TextField quantityTextField;
    private Popup popup;
    private ListView<String> listView;
    @FXML
    public void initialize() {
        // Tạo danh sách các tên sản phẩm mà không kết thúc bằng "Rent"
        ArrayList<String> list = new ArrayList<>();
        for (String s : Objects.requireNonNull(ProductDAO.getAllProductsName())) {
            if (!s.endsWith("Rent")) list.add(s);
        }

        // Tạo ListView để hiển thị các gợi ý
        listView = new ListView<>();
        listView.getItems().setAll(list);

        // Tạo Popup để hiển thị ListView
        popup = new Popup();
        popup.getContent().add(listView);

        // Bind AutoCompletion cho TextField với danh sách sản phẩm
        productNameAutoCompleteText.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                // Lọc danh sách sản phẩm theo giá trị người dùng nhập
                List<String> filteredProducts = list.stream()
                        .filter(product -> product.toLowerCase().contains(newValue.toLowerCase()))
                        .collect(Collectors.toList());

                listView.getItems().setAll(filteredProducts);

                // Nếu có sản phẩm phù hợp, hiển thị popup
                if (!filteredProducts.isEmpty() && !popup.isShowing()) {
                    popup.show(productNameAutoCompleteText,
                            productNameAutoCompleteText.localToScreen(productNameAutoCompleteText.getBoundsInLocal()).getMinX(),
                            productNameAutoCompleteText.localToScreen(productNameAutoCompleteText.getBoundsInLocal()).getMaxY());
                }
            } else {
                popup.hide();
            }
        });

        // Khi người dùng chọn một item trong ListView
        listView.setOnMouseClicked(event -> {
            if (!listView.getSelectionModel().isEmpty()) {
                productNameAutoCompleteText.setText(listView.getSelectionModel().getSelectedItem());
                popup.hide();
            }
        });

        // Xử lý khi người dùng nhấn phím Enter hoặc Escape
        productNameAutoCompleteText.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER:
                    if (!listView.getSelectionModel().isEmpty()) {
                        productNameAutoCompleteText.setText(listView.getSelectionModel().getSelectedItem());
                        popup.hide();
                    }
                    break;
                case ESCAPE:
                    popup.hide();
                    break;
                default:
                    break;
            }
        });

        ArrayList<String> promotionList = new ArrayList<>();
        for (String s : Objects.requireNonNull(PromotionDAO.getAllPromotionsNameByList())) {
            promotionList.add(s);
        }

        // Tạo ListView và Popup cho danh sách khuyến mãi
        ListView<String> promotionListView = new ListView<>();
        promotionListView.getItems().setAll(promotionList);

        Popup promotionPopup = new Popup();
        promotionPopup.getContent().add(promotionListView);

        // Bind AutoCompletion cho trường khuyến mãi với Popup và ListView
        promotionNameAutoCompleteText.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                List<String> filteredPromotions = promotionList.stream()
                        .filter(promotion -> promotion.toLowerCase().contains(newValue.toLowerCase()))
                        .collect(Collectors.toList());

                promotionListView.getItems().setAll(filteredPromotions);

                if (!filteredPromotions.isEmpty() && !promotionPopup.isShowing()) {
                    promotionPopup.show(promotionNameAutoCompleteText,
                            promotionNameAutoCompleteText.localToScreen(promotionNameAutoCompleteText.getBoundsInLocal()).getMinX(),
                            promotionNameAutoCompleteText.localToScreen(promotionNameAutoCompleteText.getBoundsInLocal()).getMaxY());
                }
            } else {
                promotionPopup.hide();
            }
        });

        // Khi người dùng chọn một item trong ListView khuyến mãi
        promotionListView.setOnMouseClicked(event -> {
            if (!promotionListView.getSelectionModel().isEmpty()) {
                promotionNameAutoCompleteText.setText(promotionListView.getSelectionModel().getSelectedItem());
                promotionPopup.hide();
            }
        });

        // Xử lý khi người dùng nhấn phím Enter hoặc Escape cho trường khuyến mãi
        promotionNameAutoCompleteText.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER:
                    if (!promotionListView.getSelectionModel().isEmpty()) {
                        promotionNameAutoCompleteText.setText(promotionListView.getSelectionModel().getSelectedItem());
                        promotionPopup.hide();
                    }
                    break;
                case ESCAPE:
                    promotionPopup.hide();
                    break;
                default:
                    break;
            }
        });
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
