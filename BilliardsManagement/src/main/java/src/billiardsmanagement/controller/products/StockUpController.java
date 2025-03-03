package src.billiardsmanagement.controller.products;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import src.billiardsmanagement.model.Product;
import src.billiardsmanagement.dao.ProductDAO;

import java.sql.SQLException;

public class StockUpController {
//    @FXML
//    private ComboBox<Product> comboProduct;
    @FXML
    private TextField txtQuantity;
    @FXML
    private Label lblProductName;


    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private ProductDAO productDAO = new ProductDAO();
    private Product selectedProduct; // Lưu sản phẩm được chọn

    @FXML
    public void initialize() throws SQLException {
//        loadProducts();
    }

//    private void loadProducts() throws SQLException {
//        productList.addAll(productDAO.getAllProducts());
//        comboProduct.setItems(productList);
//    }


    // Hàm mới để đặt sản phẩm được chọn từ `ProductController`
    public void setSelectedProduct(Product product) {
        this.selectedProduct = product;
        lblProductName.setText(product.getName()); // Hiển thị tên sản phẩm
//        comboProduct.setDisable(true); // Không cho người dùng thay đổi sản phẩm
    }

    @FXML
    private void handleConfirm() {
        if (selectedProduct == null) {
            showAlert("Error", "No Product Selected", "Product selection failed.", Alert.AlertType.ERROR);
            return;
        }

        String quantityText = txtQuantity.getText();
        int quantityToAdd;

        try {
            quantityToAdd = Integer.parseInt(quantityText);
            if (quantityToAdd <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid Quantity", "Quantity must be greater than 0\n.", Alert.AlertType.ERROR);
            return;
        }

        updateStock(selectedProduct.getId(), quantityToAdd);
    }

    private void updateStock(int productId, int quantityToAdd) {
        try {
            productDAO.stockUp(productId, quantityToAdd);
            showAlert("Success", "Stock Updated", "Stock has been successfully updated.", Alert.AlertType.INFORMATION);
            txtQuantity.getScene().getWindow().hide();
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to update stock.", e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        txtQuantity.getScene().getWindow().hide();
    }

    private void showAlert(String title, String header, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
