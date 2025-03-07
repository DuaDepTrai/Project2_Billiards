package src.billiardsmanagement.controller.orders.items;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import src.billiardsmanagement.dao.OrderItemDAO;
import src.billiardsmanagement.dao.ProductDAO;
// import src.billiardsmanagement.dao.PromotionDAO;
import src.billiardsmanagement.model.OrderItem;
import src.billiardsmanagement.model.Pair;
import java.util.ArrayList;
import java.util.List;

import src.billiardsmanagement.service.NotificationService;
import src.billiardsmanagement.model.NotificationStatus;

public class UpdateOrderItemController {
    @FXML
    private TextField quantityTextField;
    private int currentQuantity;
    private int maxQuantity;

    // @FXML
    // protected TextField promotionNameAutoCompleteText;

    @FXML
    protected TextField productNameAutoCompleteText;

    private int orderId;
    private int orderItemId;

    private List<String> productTrimmedList;
    private List<String> currentItemList;

//    protected Map<String, String> productCategoryMap;
    private String initialProductName;
//    private String initialPromotionName;

    private AutoCompletionBinding<String> productNameAutoBinding;
    // private AutoCompletionBinding<String> promotionNameAutoBinding;

    public void initializeOrderItem() {
//        productCategoryMap = CategoryDAO.getProductAndCategoryUnitMap();
        productTrimmedList = new ArrayList<>();
        ArrayList<String> productList = new ArrayList<>();
        List<Pair<String,Integer>> productQuantityList = ProductDAO.getAllProductNameAndQuantity();
        if (productQuantityList.isEmpty()) {
            System.out.println("Unexpected error: Product List is null!");
        } else {
            for (Pair<String,Integer> p : productQuantityList) {
                String prn = p.getFirstValue();
                int quant = p.getSecondValue();
                if (!currentItemList.contains(prn)) {
                    productTrimmedList.add(prn);
                    prn += "  / " + quant + " in stock" ;
                    productList.add(prn);
                }
            }

            AutoCompletionBinding<String> productNameAutoBinding = TextFields
                    .bindAutoCompletion(productNameAutoCompleteText, productList);
            HandleTextFieldClick(productNameAutoBinding, productTrimmedList, productNameAutoCompleteText, initialProductName);
            productNameAutoBinding.setVisibleRowCount(7);
            productNameAutoBinding.setHideOnEscape(true);

            // ArrayList<String> pList = (ArrayList<String>)
            // PromotionDAO.getAllPromotionsNameByList();
            // ArrayList<String> promotionList = new ArrayList<>();

            // if (pList != null) {
            // for (String s : pList) {
            // if (!s.contains(" ")) {
            // s = s + " ";
            // promotionList.add(s);
            // } else {
            // promotionList.add(s);
            // }
            // }

            // promotionList.add(0, "No Promotion");

            // AutoCompletionBinding<String> promotionNameAutoBinding = TextFields
            // .bindAutoCompletion(promotionNameAutoCompleteText, promotionList);

            // HandleTextFieldClick(promotionNameAutoBinding, promotionList,
            // promotionNameAutoCompleteText, initialPromotionName);
            // promotionNameAutoCompleteText.setText(initialPromotionName);
            // promotionNameAutoBinding.setHideOnEscape(true);
            // promotionNameAutoBinding.setVisibleRowCount(7);
            // }

            quantityTextField.setText(String.valueOf(currentQuantity));
        }
    }

    public void HandleTextFieldClick(AutoCompletionBinding<String> auto, List<String> list, TextField text,
            String name) {
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
                String inputText = text.getText();
                if (inputText == null || inputText.trim().isEmpty()) {
                    text.setText(name);
                    return;
                }

                boolean check = productTrimmedList.stream().anyMatch(inputText::equals);
                if (check) {
                    text.setText(inputText);
                } else {
                    text.setText(name);
                }
            }
        });
    }

    public void setOrderItemDetails(OrderItem item) {
        if (item != null) {
            initialProductName = item.getProductName();
            orderItemId = item.getOrderItemId();
            productNameAutoCompleteText.setText(initialProductName);
            quantityTextField.setText(String.valueOf(item.getQuantity()));
//            initialPromotionName = item.getPromotionName() == null ? "No Promotion" : item.getPromotionName();
            currentQuantity = item.getQuantity();
        }
    }

    @FXML
    public void updateOrderItem(ActionEvent event) {
        try {
            String productName = productNameAutoCompleteText.getText();

            if (productName == null || productName.trim().isEmpty()) {
                throw new IllegalArgumentException("Please select a product!");
            }
            
            if (!productTrimmedList.contains(productName)) {
                throw new IllegalArgumentException(
                        "The product you provided is not found. Check your input and try again!");
            }

            maxQuantity = ProductDAO.getProductQuantityByName(productName);
            int requestQuantity = 0;

            try {
                requestQuantity = Integer.parseInt(quantityTextField.getText().trim());
                if (requestQuantity == 0) {
                    throw new IllegalArgumentException("Quantity must be greater than 0!");
                }

                // update the old item
                if(productName.equalsIgnoreCase(initialProductName)){
                    if (requestQuantity < currentQuantity) {
                        int replenishAmount = currentQuantity - requestQuantity;
                        ProductDAO.replenishItem(productName, replenishAmount);
                    } else if (requestQuantity > currentQuantity) {
                        int quant = requestQuantity - currentQuantity;
                        if (quant > maxQuantity) {
                            throw new IllegalArgumentException(
                                    "The quantity you provided exceeds amount in stock. Please reduce the quantity.");
                        } else {
                            ProductDAO.dispatchItem(productName, quant);
                        }
                    }
                }
                // trying to change item ?
                else{
                    ProductDAO.replenishItem(initialProductName, currentQuantity);
                    ProductDAO.dispatchItem(productName, requestQuantity);
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid quantity! Please try again.");
            }

            // Promotion
            // String selectedPromotion = promotionNameAutoCompleteText.getText();
            // if (selectedPromotion == null) {
            // selectedPromotion = "No promotion";
            // } else if (selectedPromotion.trim().isEmpty()) {
            // throw new IllegalArgumentException("You haven't chosen a promotion!");
            // }

            // int promotionId = selectedPromotion.equals("No Promotion") ? -1 :
            // PromotionDAO.getPromotionIdByName(selectedPromotion);
            // if (promotionId == -1 && !selectedPromotion.equals("No Promotion")) {
            // throw new Exception("The promotion name you provided is not found! Check your
            // input and try again.");
            // }

            Pair<Integer, Double> productPair = ProductDAO.getProductIdAndPriceByName(productName);
            if (productPair == null) {
                throw new IllegalArgumentException("Error: Product is invalid. Try again later!");
            }
            int productId = productPair.getFirstValue();
            double productPrice = productPair.getSecondValue();

            double total = productPrice * requestQuantity;
//            double netTotal = subTotal;

            // if (promotionId > 0) {
            // double discount = PromotionDAO.getPromotionDiscountById(promotionId);
            // if (discount > 0) {
            // netTotal = subTotal * (1 - (discount / 100));
            // }
            // }

            OrderItem orderItem = new OrderItem(
                    orderItemId,
                    orderId,
                    productId,
                    productName,
                    requestQuantity,
//                    netTotal,
                    total
            );

            boolean success = OrderItemDAO.updateOrderItem(orderItem);
            if (!success) {
                throw new Exception("Order update failed! Please try again later!");
            }

            NotificationService.showNotification("Success", "Order item updated successfully!",
                    NotificationStatus.Success);

            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            stage.close();

        } catch (IllegalArgumentException e) {
            NotificationService.showNotification("Error", e.getMessage(), NotificationStatus.Error);
        } catch (Exception e) {
            e.printStackTrace();
            NotificationService.showNotification("Error", "An unexpected error occurred! Please try again later!",
                    NotificationStatus.Error);
        }
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getCurrentQuantity() {
        return currentQuantity;
    }

    public void setCurrentQuantity(int currentQuantity) {
        this.currentQuantity = currentQuantity;
    }

    public void setOrderItemList(List<String> list){
        this.currentItemList = new ArrayList<>();
        currentItemList.addAll(list);
    }
}
