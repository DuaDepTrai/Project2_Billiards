package src.billiardsmanagement.controller.orders.items;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import src.billiardsmanagement.dao.OrderItemDAO;
import src.billiardsmanagement.dao.ProductDAO;
import src.billiardsmanagement.dao.PromotionDAO;
import src.billiardsmanagement.model.OrderItem;
import src.billiardsmanagement.model.Pair;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

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

    private String initialProductName;
    private String initialPromotionName;
    private AutoCompletionBinding<String> productNameAutoBinding;
    private AutoCompletionBinding<String> promotionNameAutoBinding;

    public void initializeOrderItem() {
        ArrayList<String> list = ProductDAO.getAllProductsName();
        productList = new ArrayList<>();
        if (list == null)
            System.out.println("Unexpected error : Product List is null !");
        else {
            for (String s : list) {
                if (!s.contains("Rent"))
                    productList.add(s);
                if (!s.contains(" ")) {
                    s = s + " ";
                    productList.add(s);
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
                    } else
                        promotionList.add(s);
                }

                // Comparator<String> promotionComparator = (s1,s2) -> {
                // if(s1.equals("No Promotion")) return -1;
                // if(s2.equals("No Promotion")) return 1;
                // else return s1.compareTo(s2);
                // };

                promotionList.add(0, "No Promotion");

                AutoCompletionBinding<String> promotionNameAutoBinding = TextFields
                        .bindAutoCompletion(promotionNameAutoCompleteText, promotionList);

                HandleTextFieldClick(promotionNameAutoBinding, promotionList, promotionNameAutoCompleteText,
                        initialPromotionName);
                promotionNameAutoCompleteText.setText(initialPromotionName);
                promotionNameAutoBinding.setHideOnEscape(true);
                promotionNameAutoBinding.setVisibleRowCount(7);
            }

            quantityTextField.setText(String.valueOf(currentQuantity));
        }
    }

    public void HandleTextFieldClick(AutoCompletionBinding<String> auto, List<String> list, TextField text,
                                     String name) {
        text.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                auto.setUserInput(" ");
                return;
            }

            // Only proceed if the field is not focused
            if (!newValue) {
                String inputText = text.getText();
                if (inputText == null || inputText.trim().isEmpty()) {
                    text.setText(name);
                    return;
                }

                boolean check = list.stream()
                        .anyMatch(inputText::equals);

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

            orderItemId = item.getOrderItemId(); // Store the orderItemId
            productNameAutoCompleteText.setText(initialProductName);
            quantityTextField.setText(String.valueOf(item.getQuantity()));

            initialPromotionName = item.getPromotionName() == null ? "No Promotion" : item.getPromotionName();
            currentQuantity = item.getQuantity();
        }
    }

    // Thêm : kiểm tra xem thông tin update mới có giống hệt thông tin cũ không.
    // nếu giống hệt thì bỏ qua, không update, đóng update scene
    // Thêm : Kiểm tra nếu edit quantity mới lớn hơn số hàng trong kho (số lượng
    // hàng, trong bảng products)
    @FXML
    public void updateOrderItem(ActionEvent event) {
        try {
            // Get and validate input values
            String productName = productNameAutoCompleteText.getText();

            if (productName == null)
                throw new IllegalArgumentException("Please select a product!");

            if (productName.trim().isEmpty()) {
                throw new IllegalArgumentException("Please select a product!");
            }

            if (!productList.contains(productName)) {
                throw new IllegalArgumentException(
                        "The product you provided is not found. Check your input and try again !");
            }

            maxQuantity = ProductDAO.getProductQuantityByName(productName);

            int requestQuantity;
            try {
                requestQuantity = Integer.parseInt(quantityTextField.getText().trim());
                // Check if the request quantity is less than current quantity
                if(requestQuantity == 0){
                    throw new IllegalArgumentException("Quantity must be greater than 0 !");
                }
                if (requestQuantity < currentQuantity) {
                    int replenishAmount = currentQuantity - requestQuantity;
                    ProductDAO.replenishItem(productName, replenishAmount);
                }
                // Check if the requested quantity exceeds the available stock
                else if (requestQuantity > currentQuantity){
                    int quant = requestQuantity - currentQuantity;
                    if(quant > maxQuantity){
                        throw new IllegalArgumentException("The quantity you provided exceeds amount in stock. Please reduce the quantity.");
                    } else {
                        ProductDAO.dispatchItem(productName, quant);
                    }
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid quantity ! Please try again.");
            }

            // Get and validate promotion
            String selectedPromotion = promotionNameAutoCompleteText.getText();
            if (selectedPromotion == null) {
                selectedPromotion = "No promotion";
            } else {
                if (selectedPromotion.trim().isEmpty()) {
                    throw new IllegalArgumentException("You haven't choose a promotion !");
                }
            }

            int promotionId; // default value
            if (selectedPromotion.equals("No Promotion")) {
                promotionId = -1;
            } else {
                promotionId = PromotionDAO.getPromotionIdByName(selectedPromotion);
                if (promotionId == -1) {
                    throw new Exception(
                            "The promotion name you provided is not found ! Check your input and try again.");
                }
            }

            // Get product ID and price from product name using ProductDAO
            Pair<Integer, Double> productPair = ProductDAO.getProductIdAndPriceByName(productName);
            if (productPair == null) {
                throw new IllegalArgumentException("Error : Product is invalid. Try again later !");
            }
            int productId = productPair.getFirstValue();
            double productPrice = productPair.getSecondValue();

            // Calculate subTotal and netTotal
            double subTotal = productPrice * requestQuantity;
            double netTotal = subTotal;

            // Apply promotion discount if a promotion is selected
            if (promotionId > 0) {
                double discount = PromotionDAO.getPromotionDiscountById(promotionId);
                if (discount > 0) {
                    netTotal = subTotal * (1 - (discount / 100));
                }
            }

            // Create OrderItem object with orderItemId
            OrderItem orderItem = new OrderItem(
                    orderItemId, // Add orderItemId here
                    orderId,
                    productId,
                    productName, // Add product name
                    requestQuantity,
                    netTotal,
                    subTotal,
                    promotionId);

            // Update in database
            boolean success = OrderItemDAO.updateOrderItem(orderItem);
            if (success) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Updated Successfully:");
                alert.setContentText("An order item has been updated successfully.");
                alert.showAndWait();

            } else {
                throw new Exception("Order update failed! Please try again later!");
            }

            // Close the window after successful update
            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            stage.close();

        } catch (IllegalArgumentException e) {
            // Handle validation errors
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("You have entered incorrect information. Please try again!");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            // Handle unexpected errors
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("An unexpected error occurred! Please try again later!");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
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
