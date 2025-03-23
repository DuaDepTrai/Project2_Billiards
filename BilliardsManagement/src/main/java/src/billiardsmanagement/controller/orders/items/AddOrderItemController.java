package src.billiardsmanagement.controller.orders.items;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Popup;
import javafx.stage.Stage;

import javafx.stage.Window;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import src.billiardsmanagement.dao.CategoryDAO;
import src.billiardsmanagement.dao.OrderItemDAO;
import src.billiardsmanagement.dao.ProductDAO;
// import src.billiardsmanagement.dao.PromotionDAO;
import src.billiardsmanagement.model.OrderItem;
import src.billiardsmanagement.model.Pair;
import src.billiardsmanagement.service.NotificationService;
import src.billiardsmanagement.model.NotificationStatus;

import java.sql.*;
import java.util.*;

public class AddOrderItemController {
    @FXML
    protected TextField productNameAutoCompleteText;
    protected ArrayList<String> productNameTrimmed;

    // @FXML
    // protected TextField promotionNameAutoCompleteText;
    // protected ArrayList<String> promotionNameTrimmed;

    private int orderId;
    // private int promotionId;

    @FXML
    private TextField quantityTextField;

    private Stage stage;
    private Window window;
    protected Map<String, String> productCategoryMap;
    public Popup forEachPopup;

    @FXML
    public void initialize() {
        productCategoryMap = CategoryDAO.getProductAndCategoryUnitMap();

        productNameTrimmed = new ArrayList<>();
        ArrayList<String> productList = new ArrayList<>();
        List<Pair<String, Integer>> list = ProductDAO.getAllProductNameAndQuantity();

        for (Pair<String, Integer> s : list) {
            String str = s.getFirstValue();
            int quant = s.getSecondValue();

            productNameTrimmed.add(str.trim());
            str = str + "  / " + quant + " in stock";
            productList.add(str);
        }

        AutoCompletionBinding<String> productNameAutoBinding = TextFields
                .bindAutoCompletion(productNameAutoCompleteText, productList);
        HandleTextFieldClick(productNameAutoBinding, productList, productNameAutoCompleteText, productNameTrimmed);
        productNameAutoBinding.setVisibleRowCount(7);
        productNameAutoBinding.setHideOnEscape(true);

        // promotionNameTrimmed = new ArrayList<>();
        // ArrayList<String> pList = (ArrayList<String>)
        // PromotionDAO.getAllPromotionsNameByList();
        // ArrayList<String> promotionList = new ArrayList<>();
        // if (pList != null) {
        // for (String s : pList) {
        // promotionNameTrimmed.add(s.trim());
        // s = s + " ";
        // promotionList.add(s);
        // }
        // AutoCompletionBinding<String> promotionNameAutoBinding =
        // TextFields.bindAutoCompletion(promotionNameAutoCompleteText, promotionList);
        // HandleTextFieldClick(promotionNameAutoBinding, promotionList,
        // promotionNameAutoCompleteText, promotionNameTrimmed);
        // promotionNameAutoBinding.setHideOnEscape(true);
        // promotionNameAutoBinding.setVisibleRowCount(7);
        // }

        quantityTextField.setText("1");
    }

    @FXML
    public void saveOrderItem(ActionEvent event) {
        try {
            if (!(orderId > 0)) {
                throw new Exception("Error: Order not found! Please try again.");
            }

            // Validate product selection
            String selectedProductName = productNameAutoCompleteText.getText().isBlank() ? ""
                    : productNameAutoCompleteText.getText().trim();
            if (selectedProductName.isBlank()) {
                throw new IllegalArgumentException("Please select a product!");
            }
            if (!productNameTrimmed.contains(selectedProductName)) {
                throw new IllegalArgumentException(
                        "The product with name " + selectedProductName + " cannot be found!");
            }

            // Get product details
            Pair<Integer, Double> productPair = ProductDAO.getProductIdAndPriceByName(selectedProductName);
            if (productPair == null) {
                throw new SQLException("Connection Error: Can't connect to Database. Please try again later.");
            }

            int productId = productPair.getFirstValue();
            double productPrice = productPair.getSecondValue();

            // Validate quantity
            int quantity;
            try {
                quantity = Integer.parseInt(quantityTextField.getText().trim());
                int availableQuantity = ProductDAO.getProductQuantityByName(selectedProductName);
                if (quantity <= 0) {
                    throw new IllegalArgumentException("Quantity must be greater than 0.");
                }
                if (quantity > availableQuantity) {
                    throw new IllegalArgumentException("The quantity you selected exceeds the available stock ("
                            + availableQuantity + "); please choose a smaller amount.");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Please enter a valid quantity.");
            }

            // Check for duplicate items and update if found
            List<OrderItem> existingItems = OrderItemDAO.getForEachOrderItem(orderId);
            for (OrderItem existingItem : existingItems) {
                if (existingItem.getProductId() == productId) {
                    OrderItem updatedItem = new OrderItem();
                    updatedItem.setOrderId(orderId);
                    updatedItem.setOrderItemId(existingItem.getOrderItemId());
                    updatedItem.setProductId(productId);
                    updatedItem.setQuantity(existingItem.getQuantity() + quantity);

                    double newTotal = (existingItem.getQuantity() + quantity) * productPrice;
                    updatedItem.setTotal(newTotal);
//                    updatedItem.setNetTotal(newSubTotal); // Since promotions are removed, netTotal equals subTotal

                    if (OrderItemDAO.addOrderItemDuplicate(updatedItem)) {
                        ProductDAO.dispatchItem(selectedProductName, quantity);
                        NotificationService.showNotification("Success", "Order item updated successfully!",
                                NotificationStatus.Success);
                        if (this.forEachPopup != null) forEachPopup.hide();
                        return;
                    }
                }
            }

            // Create new order item
            OrderItem newOrderItem = new OrderItem();
            newOrderItem.setOrderId(orderId);
            newOrderItem.setProductId(productId);
            newOrderItem.setQuantity(quantity);

            double total = quantity * productPrice;
            newOrderItem.setTotal(total);
//            newOrderItem.setNetTotal(subTotal); // Since promotions are removed, netTotal equals subTotal

            // Add the new order item
            if (OrderItemDAO.addOrderItem(newOrderItem)) {
                ProductDAO.dispatchItem(selectedProductName, quantity);
                NotificationService.showNotification("Success", "New order item added successfully!",
                        NotificationStatus.Success);
                if (this.forEachPopup != null) forEachPopup.hide();

            } else {
                throw new Exception("Failed to add order item. Please try again.");
            }

        } catch (IllegalArgumentException e) {
            NotificationService.showNotification("Input Error", e.getMessage(), NotificationStatus.Error);
        } catch (Exception e) {
            NotificationService.showNotification("Error", e.getMessage(), NotificationStatus.Error);
            e.printStackTrace();
        }
    }

    public void HandleTextFieldClick(AutoCompletionBinding<String> auto, ArrayList<String> list, TextField text,
                                     ArrayList<String> trimmedList) {
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
                } else
                    text.setText(input);
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

    // public int getPromotionId() {
    // return promotionId;
    // }

    // public AddOrderItemController setPromotionId(int promotionId) {
    // this.promotionId = promotionId;
    // return this;
    // }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setForEachPopup(Popup forEachPopup) {
        this.forEachPopup = forEachPopup;
    }
}
