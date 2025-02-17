package src.billiardsmanagement.controller.orders.items;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;
import javafx.stage.Stage;

import javafx.stage.Window;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import src.billiardsmanagement.dao.CategoryDAO;
import src.billiardsmanagement.dao.OrderItemDAO;
import src.billiardsmanagement.dao.ProductDAO;
import src.billiardsmanagement.dao.PromotionDAO;
import src.billiardsmanagement.model.DatabaseConnection;
import src.billiardsmanagement.model.OrderItem;
import src.billiardsmanagement.model.Pair;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class AddOrderItemController {
    @FXML
    protected TextField productNameAutoCompleteText;
    protected ArrayList<String> productNameTrimmed;

    @FXML
    protected TextField promotionNameAutoCompleteText;
    protected ArrayList<String> promotionNameTrimmed;

    private int orderId;
    private int promotionId;


    @FXML
    private TextField quantityTextField;

    private Stage stage;
    private Window window;
    protected Map<String,String> productCategoryMap;

    @FXML
    public void initialize() {
        String saleCueCategory = "Cues-sale";
        productCategoryMap = CategoryDAO.getProductAndCategoryUnitMap();

        productNameTrimmed = new ArrayList<>();
        ArrayList<String> productList = new ArrayList<>();
        List<Pair<String, Integer>> list = ProductDAO.getAllProductNameAndQuantity();

        for (Pair<String, Integer> s : list) {
            String str = s.getFirstValue();
            int quant = s.getSecondValue();
            if (productCategoryMap.get(str).equalsIgnoreCase(saleCueCategory) && quant > 0) {
                productNameTrimmed.add(str.trim());
                str = str + "  / "+quant+" in stock";
                productList.add(str);
            }
        }

        AutoCompletionBinding<String> productNameAutoBinding = TextFields.bindAutoCompletion(productNameAutoCompleteText, productList);
        HandleTextFieldClick(productNameAutoBinding, productList, productNameAutoCompleteText,productNameTrimmed);
        productNameAutoBinding.setVisibleRowCount(7);
        productNameAutoBinding.setHideOnEscape(true);
        // productNameAutoBinding.setOnAutoCompleted(event -> {
        //     String input = event.getCompletion().split("/")[0].trim();
        //     productNameAutoCompleteText.setText(input);
        // });

        promotionNameTrimmed = new ArrayList<>();
        ArrayList<String> pList = (ArrayList<String>) PromotionDAO.getAllPromotionsNameByList();
        ArrayList<String> promotionList = new ArrayList<>();
        if (pList != null) {
            for (String s : pList) {
                promotionNameTrimmed.add(s.trim());
                s = s + " ";
                promotionList.add(s);
            }
            AutoCompletionBinding<String> promotionNameAutoBinding = TextFields.bindAutoCompletion(promotionNameAutoCompleteText, promotionList);
            HandleTextFieldClick(promotionNameAutoBinding, promotionList, promotionNameAutoCompleteText,promotionNameTrimmed);
            promotionNameAutoBinding.setHideOnEscape(true);
            promotionNameAutoBinding.setVisibleRowCount(7);
        }

        quantityTextField.setText("1");
    }


    @FXML
    public void saveOrderItem(ActionEvent event) {
        try {

            if (!(orderId > 0))
                throw new Exception("Error: Order not found ! Please try again.");

            String selectedProductName = productNameAutoCompleteText.getText().isBlank() ? "" : productNameAutoCompleteText.getText().trim();

            if(selectedProductName.isBlank()) throw new IllegalArgumentException("Please select a product !");

            if(!productNameTrimmed.contains(selectedProductName)) throw new IllegalArgumentException("The product with name "+selectedProductName+" cannot be found !");

            Pair<Integer, Double> productPair = ProductDAO.getProductIdAndPriceByName(selectedProductName);
            if (productPair == null)
                throw new SQLException("Connection Error: Can't connect to Database. Please try again later.");

            int productId = productPair.getFirstValue();
            double productPrice = productPair.getSecondValue();

            int quantity;
            try {
                quantity = Integer.parseInt(quantityTextField.getText().trim());
                if(quantity>ProductDAO.getProductQuantityByName(selectedProductName)){
                    throw new IllegalArgumentException("The quantity you selected exceeds the available stock; please choose a smaller amount.");
                }
            } catch (NumberFormatException e) {
                throw new RuntimeException(e);
            }
            // Check duplicate Order Item
            for (OrderItem orderItem : OrderItemDAO.getForEachOrderItem(orderId)) {
                if (orderItem.getProductId() == productId) {
                    OrderItem newItem = new OrderItem();
                    newItem.setOrderId(orderId);
                    newItem.setOrderItemId(orderItem.getOrderItemId());
                    newItem.setQuantity(orderItem.getQuantity() + quantity);
                    System.out.println("new Quant = "+newItem.getQuantity());
                    newItem.setProductId(orderItem.getProductId());

                    ProductDAO.dispatchItem(orderItem.getProductName(), quantity);

                    if (orderItem.getPromotionDiscount() > 1)
                        newItem.setNetTotal(orderItem.getNetTotal() + (quantity * productPrice) - ((orderItem.getPromotionDiscount() * quantity * productPrice) / 100));
                    else
                        newItem.setNetTotal(orderItem.getNetTotal() + quantity * productPrice);

                    newItem.setSubTotal(orderItem.getSubTotal() + quantity * productPrice);

                    boolean success = OrderItemDAO.addOrderItemDuplicate(newItem);

                    if(success){
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Notification");
                        alert.setHeaderText(null);
                        alert.setContentText("An Order Item has been added successfully !");
                        alert.showAndWait();
                    }

                    Stage stage = (Stage) promotionNameAutoCompleteText.getScene().getWindow();
                    stage.close();
                    return;
                }
            }

            // promotionId = -1, if promotion not found
            promotionId = PromotionDAO.getPromotionIdByName(promotionNameAutoCompleteText.getText().trim());
            double subTotal;
            double netTotal;
            OrderItem orderItem;
            if (promotionId > 0) {
                double discount = PromotionDAO.getPromotionDiscountById(promotionId);
                if (discount == -1)
                    throw new SQLException("Connection Error: Can't connect to Database. Please try again later.");
                subTotal = quantity * productPrice;
                netTotal = subTotal - (subTotal * (discount / 100));
                orderItem = new OrderItem(orderId, productId, quantity, netTotal, subTotal, promotionId);
            } else {
                subTotal = quantity * productPrice;
                netTotal = subTotal;
                orderItem = new OrderItem(orderId, productId, quantity, netTotal, subTotal, -1);
            }

            ProductDAO.dispatchItem(selectedProductName, quantity);
            OrderItemDAO.addOrderItem(orderItem);

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Notification");
            alert.setHeaderText(null);
            alert.setContentText("An Order Item has been added successfully !");
            alert.showAndWait();

            Stage stage = (Stage) promotionNameAutoCompleteText.getScene().getWindow();
            stage.close();
        } catch (IllegalArgumentException illegal) {
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
            if(!newValue){
                String input = text.getText();
                input = input==null ? "" : input.trim();
                if(input.isBlank() || !trimmedList.contains(input)){
                    text.setText("");
                }
                else text.setText(input);
            }
        });
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

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
