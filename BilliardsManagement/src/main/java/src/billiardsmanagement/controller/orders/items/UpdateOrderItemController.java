package src.billiardsmanagement.controller.orders.items;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.Node;
import javafx.stage.Stage;
import src.billiardsmanagement.dao.CategoryDAO;
import src.billiardsmanagement.dao.OrderItemDAO;
import src.billiardsmanagement.dao.ProductDAO;
import src.billiardsmanagement.dao.PromotionDAO;
import src.billiardsmanagement.model.OrderItem;
import src.billiardsmanagement.model.Pair;
import src.billiardsmanagement.model.NotificationService;
import src.billiardsmanagement.model.NotificationStatus;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.util.*;

public class UpdateOrderItemController {
    @FXML
    private TextField quantityTextField;
    private int currentQuantity;
    private int maxQuantity;

    @FXML
    protected TextField promotionNameAutoCompleteText;
    @FXML
    protected TextField productNameAutoCompleteText;

    private int orderId;
    private int orderItemId;

    private List<String> productList;
    protected Map<String,String> productCategoryMap;

    private String initialProductName;
    private String initialPromotionName;
    private AutoCompletionBinding<String> productNameAutoBinding;
    private AutoCompletionBinding<String> promotionNameAutoBinding;

    public void initializeOrderItem() {
        String saleCueCategory = "Cues-sale";
        String rentCueCategory = "Cues-rent";
        productCategoryMap = CategoryDAO.getProductAndCategoryUnitMap();

        ArrayList<String> list = ProductDAO.getAllProductsName();
        productList = new ArrayList<>();
        if (list == null) {
            System.out.println("Unexpected error: Product List is null!");
        } else {
            for (String s : list) {
                if (!productCategoryMap.get(s).equalsIgnoreCase(rentCueCategory)) {
                    productList.add(s);
                    if (!s.contains(" ")) {
                        s = s + " ";
                        productList.add(s);
                    }
                }
            }

            AutoCompletionBinding<String> productNameAutoBinding = TextFields
                    .bindAutoCompletion(productNameAutoCompleteText, productList);
            HandleTextFieldClick(productNameAutoBinding, productList, productNameAutoCompleteText, initialProductName);
            productNameAutoBinding.setVisibleRowCount(7);
            productNameAutoBinding.setHideOnEscape(true);

            ArrayList<String> pList = (ArrayList<String>) PromotionDAO.getAllPromotionsNameByList();
            ArrayList<String> promotionList = new ArrayList<>();

            if (pList != null) {
                for (String s : pList) {
                    if (!s.contains(" ")) {
                        s = s + " ";
                        promotionList.add(s);
                    } else {
                        promotionList.add(s);
                    }
                }

                promotionList.add(0, "No Promotion");

                AutoCompletionBinding<String> promotionNameAutoBinding = TextFields
                        .bindAutoCompletion(promotionNameAutoCompleteText, promotionList);

                HandleTextFieldClick(promotionNameAutoBinding, promotionList, promotionNameAutoCompleteText, initialPromotionName);
                promotionNameAutoCompleteText.setText(initialPromotionName);
                promotionNameAutoBinding.setHideOnEscape(true);
                promotionNameAutoBinding.setVisibleRowCount(7);
            }

            quantityTextField.setText(String.valueOf(currentQuantity));
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

    public void setOrderItemDetails(OrderItem item) {
        if (item != null) {
            initialProductName = item.getProductName();
            orderItemId = item.getOrderItemId();
            productNameAutoCompleteText.setText(initialProductName);
            quantityTextField.setText(String.valueOf(item.getQuantity()));
            initialPromotionName = item.getPromotionName() == null ? "No Promotion" : item.getPromotionName();
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
            if (!productList.contains(productName)) {
                throw new IllegalArgumentException("The product you provided is not found. Check your input and try again!");
            }

            maxQuantity = ProductDAO.getProductQuantityByName(productName);
            int requestQuantity;

            try {
                requestQuantity = Integer.parseInt(quantityTextField.getText().trim());
                if (requestQuantity == 0) {
                    throw new IllegalArgumentException("Quantity must be greater than 0!");
                }
                if (requestQuantity < currentQuantity) {
                    int replenishAmount = currentQuantity - requestQuantity;
                    ProductDAO.replenishItem(productName, replenishAmount);
                } else if (requestQuantity > currentQuantity) {
                    int quant = requestQuantity - currentQuantity;
                    if (quant > maxQuantity) {
                        throw new IllegalArgumentException("The quantity you provided exceeds amount in stock. Please reduce the quantity.");
                    } else {
                        ProductDAO.dispatchItem(productName, quant);
                    }
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid quantity! Please try again.");
            }

            String selectedPromotion = promotionNameAutoCompleteText.getText();
            if (selectedPromotion == null) {
                selectedPromotion = "No promotion";
            } else if (selectedPromotion.trim().isEmpty()) {
                throw new IllegalArgumentException("You haven't chosen a promotion!");
            }

            int promotionId = selectedPromotion.equals("No Promotion") ? -1 : PromotionDAO.getPromotionIdByName(selectedPromotion);
            if (promotionId == -1 && !selectedPromotion.equals("No Promotion")) {
                throw new Exception("The promotion name you provided is not found! Check your input and try again.");
            }

            Pair<Integer, Double> productPair = ProductDAO.getProductIdAndPriceByName(productName);
            if (productPair == null) {
                throw new IllegalArgumentException("Error: Product is invalid. Try again later!");
            }
            int productId = productPair.getFirstValue();
            double productPrice = productPair.getSecondValue();

            double subTotal = productPrice * requestQuantity;
            double netTotal = subTotal;

            if (promotionId > 0) {
                double discount = PromotionDAO.getPromotionDiscountById(promotionId);
                if (discount > 0) {
                    netTotal = subTotal * (1 - (discount / 100));
                }
            }

            OrderItem orderItem = new OrderItem(
                    orderItemId,
                    orderId,
                    productId,
                    productName,
                    requestQuantity,
                    netTotal,
                    subTotal,
                    promotionId);

            boolean success = OrderItemDAO.updateOrderItem(orderItem);
            if (!success) {
                throw new Exception("Order update failed! Please try again later!");
            }

            NotificationService.showNotification("Success", "Order item updated successfully!", NotificationStatus.Success);

            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            stage.close();

        } catch (IllegalArgumentException e) {
            NotificationService.showNotification("Error", e.getMessage(), NotificationStatus.Error);
        } catch (Exception e) {
            e.printStackTrace();
            NotificationService.showNotification("Error", "An unexpected error occurred! Please try again later!", NotificationStatus.Error);
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
}
