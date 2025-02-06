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
import src.billiardsmanagement.model.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class UpdateOrderItemController {
    @FXML
    public TextField quantityTextField;

    @FXML
    protected TextField promotionNameAutoCompleteText;
    @FXML
    protected TextField productNameAutoCompleteText;

    private int orderId;
    private int orderItemId;

    private List<Pair<String, Integer>> promotions;

    private HashMap<String, Integer> productsMap = new HashMap<>();
    
    private String initialProductName;
    private String initialPromotionName;
    private AutoCompletionBinding<String> productNameAutoBinding;
    private AutoCompletionBinding<String> promotionNameAutoBinding;

    @FXML
    public void initialize() {
        ArrayList<String> list = new ArrayList<>();
        for (String s : ProductDAO.getAllProductsName()) {
            if (!s.endsWith("Rent")) {
                list.add(s); // cũ là "Sale"
            }
        }
        productNameAutoBinding = TextFields.bindAutoCompletion(productNameAutoCompleteText,list);
        HandleTextFieldClick(list, productNameAutoCompleteText, initialProductName);

        // Initialize promotion combo box
        promotions = PromotionDAO.getAllPromotionsName();
        ArrayList<String> promotionList = new ArrayList<>();
        if(promotions!=null){
             for(Pair<String,Integer> p : promotions){
                 promotionList.add(p.getFirstValue());
             }
        }
        promotionNameAutoBinding = TextFields.bindAutoCompletion(promotionNameAutoCompleteText,promotionList);
        HandleTextFieldClick(promotionList, promotionNameAutoCompleteText, initialPromotionName);
    }

    public void HandleTextFieldClick(ArrayList<String> list, TextField text, String name) {
        TextFields.bindAutoCompletion(text, list);
        text.setOnMouseClicked(event -> {
            text.setText("");
            if(text.equals(productNameAutoCompleteText)) productNameAutoBinding.setUserInput("");
            else promotionNameAutoBinding.setUserInput("");
        });
        text.focusedProperty().addListener((observable, oldValue, newValue) -> {
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
                System.out.println("initial name = "+name);
                if(check) text.setText(inputText);
                else text.setText(name);
            }
        });
    }

    public void setOrderItemDetails(OrderItem item) {
        if (item != null) {
            initialProductName = item.getProductName();
            
            orderItemId = item.getOrderItemId(); // Store the orderItemId
            productNameAutoCompleteText.setText(initialProductName);
            quantityTextField.setText(String.valueOf(item.getQuantity()));

            // Always set a promotion value, even if no promotion
            if (item.getPromotionId() != -1) {
                // Find promotion name by ID using getFirstBySecondValue method
                initialPromotionName = Pair.getFirstBySecondValue(promotions, item.getPromotionId());
                promotionNameAutoCompleteText.setText(initialPromotionName != null ? initialPromotionName : "No promotion");
            }
        }
    }


    // Thêm : kiểm tra xem thông tin update mới có giống hệt thông tin cũ không.
    // nếu giống hệt thì bỏ qua, không update, đóng update scene
    // Thêm : Kiểm tra nếu edit quantity mới lớn hơn số hàng trong kho (số lượng hàng, trong bảng products) 
    @FXML
    public void updateOrderItem(ActionEvent event) {
        try {
            // Get and validate input values
            String productName = productNameAutoCompleteText.getText();
            if (productName == null || productName.trim().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng chọn sản phẩm!");
            }

            int quantity;
            try {
                quantity = Integer.parseInt(quantityTextField.getText().trim());
                if (quantity <= 0) {
                    throw new IllegalArgumentException("Số lượng phải lớn hơn 0!");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Số lượng phải là số hợp lệ!");
            }

            // Get and validate promotion
            String selectedPromotion = promotionNameAutoCompleteText.getText();
            if (selectedPromotion == null || selectedPromotion.trim().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng chọn khuyến mãi!");
            }

            int promotionId = -1; // Default value if no promotion
            if (!selectedPromotion.equals("Không có khuyến mãi")) {
                promotionId = PromotionDAO.getPromotionIdByName(selectedPromotion);
            }

            // Get product ID and price from product name using ProductDAO
            Pair<Integer, Double> productPair = ProductDAO.getProductIdAndPriceByName(productName);
            if (productPair == null) {
                throw new IllegalArgumentException("Lỗi : Sản phẩm không hợp lệ. Hãy thử lại !");
            }
            int productId = productPair.getFirstValue();
            double productPrice = productPair.getSecondValue();

            // Calculate subTotal and netTotal
            double subTotal = productPrice * quantity;
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
                    orderItemId,  // Add orderItemId here
                    orderId,
                    productId,
                    productName,  // Add product name
                    quantity,
                    netTotal,
                    subTotal,
                    promotionId
            );

            // Update in database
            boolean success = OrderItemDAO.updateOrderItem(orderItem);
            if (!success) {
                throw new Exception("Cập nhật đơn hàng thất bại! Vui lòng thử lại sau!");
            }

            // Close the window after successful update
            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            stage.close();

        } catch (IllegalArgumentException e) {
            // Handle validation errors
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi!");
            alert.setHeaderText("Bạn đã nhập sai thông tin. Vui lòng thử lại!");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        } catch (Exception e) {
            // Handle unexpected errors
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi!");
            alert.setHeaderText("Đã xảy ra lỗi không mong muốn! Vui lòng thử lại sau!");
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

    public UpdateOrderItemController setOrderId(int orderId) {
        this.orderId = orderId;
        return this;
    }
}

