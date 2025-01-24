package src.billiardsmanagement.controller.orders.rent;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import src.billiardsmanagement.dao.ProductDAO;
import src.billiardsmanagement.dao.PromotionDAO;
import src.billiardsmanagement.dao.RentCueDAO;
import src.billiardsmanagement.model.Pair;
import src.billiardsmanagement.model.RentCue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AddRentCueController {
    private int orderID;
    
    @FXML
    private ComboBox<String> productNameComboBox;

    @FXML
    private ComboBox<String> promotionComboBox;

    @FXML
    private TextField quantityTextField;

    private RentCueDAO rentCueDAO = new RentCueDAO();

    @FXML
    private void initialize() {
        // Filter products that contain "Cue" and end with "Rent"
        ArrayList<String> allProducts = ProductDAO.getAllProductsName();
        ArrayList<String> filteredProducts = new ArrayList<>();
        
        if (allProducts != null) {
            for (String product : allProducts) {
                if (product.contains("Cue") && product.endsWith("Rent")) {
                    filteredProducts.add(product);
                }
            }
            productNameComboBox.getItems().setAll(filteredProducts);
        }

        // Get promotion names
        List<Pair<String, Integer>> allPromotions = PromotionDAO.getAllPromotionsName();
        ArrayList<String> promotionNames = new ArrayList<>();
        
        if (allPromotions != null) {
            for (Pair<String, Integer> promotion : allPromotions) {
                promotionNames.add(promotion.getFirstValue());
            }
            promotionComboBox.getItems().setAll(promotionNames);
        }
    }

    @FXML
    private void addRentCue() {
        try {
            // Validate input
            String productName = productNameComboBox.getValue();
            String promotionName = promotionComboBox.getValue();
            String quantityStr = quantityTextField.getText();

            if (productName == null || productName.isEmpty()) {
                showAlert("Lỗi Xác Thực", "Vui lòng chọn sản phẩm.");
                return;
            }

            int requestQuantity;
            try {
                requestQuantity = Integer.parseInt(quantityStr);
                if (requestQuantity <= 0) {
                    showAlert("Lỗi Xác Thực", "Số lượng phải lớn hơn 0.");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Lỗi Xác Thực", "Vui lòng nhập số lượng hợp lệ.");
                return;
            }

            // Get product quantity from database
            Integer availableQuantity = ProductDAO.getProductQuantityByName(productName);
            if (availableQuantity == null) {
                showAlert("Lỗi", "Không thể lấy số lượng sản phẩm.");
                return;
            }

            if (requestQuantity > availableQuantity) {
                showAlert("Lỗi", "Số lượng yêu cầu vượt quá số lượng trong kho.");
                return;
            }

            // Get product and promotion IDs
            Integer productId = ProductDAO.getProductIdByName(productName);
            Integer promotionId = promotionName != null && !promotionName.isEmpty() 
                ? PromotionDAO.getPromotionIdByName(promotionName) 
                : null;

            if (productId == null) {
                showAlert("Lỗi", "Không tìm thấy mã sản phẩm.");
                return;
            }

            // Create RentCue object
            RentCue rentCue = new RentCue();
            rentCue.setRentCueId(rentCueDAO.getNextRentCueId());
            rentCue.setOrderId(orderID);
            rentCue.setProductId(productId);
            rentCue.setStartTime(LocalDateTime.now());
            rentCue.setQuantity(requestQuantity);

            if(promotionId != null) {
                rentCue.setPromotionId(promotionId);
            }
            else{
                rentCue.setPromotionId(-1);
            }
        
            // Add rent cue to database
            boolean success = RentCueDAO.addRentCue(rentCue);
            if (success) {
                showAlert("Thành Công", "Thêm thuê cơ thành công.");
                closeWindow();
            } else {
                showAlert("Lỗi", "Thêm thuê cơ thất bại.");
            }

        } catch (Exception e) {
            showAlert("Lỗi Không Mong Muốn", "Đã xảy ra lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) productNameComboBox.getScene().getWindow();
        stage.close();
    }

    public void setOrderId(int orderID) {
        this.orderID = orderID;
    }
}


