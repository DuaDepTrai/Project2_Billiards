package src.billiardsmanagement.controller.products;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import src.billiardsmanagement.model.Product;
import src.billiardsmanagement.dao.ProductDAO;

import java.sql.SQLException;

public class StockUpController {
    @FXML
    private ComboBox<Product> comboProduct;
    @FXML
    private TextField txtQuantity;

    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private ProductDAO productDAO = new ProductDAO();

    @FXML
    public void initialize() throws SQLException {
        loadProducts();
        setupAutoCompleteComboBox(comboProduct);
    }

    private void loadProducts() throws SQLException {
        productList.addAll(productDAO.getAllProducts());
        comboProduct.setItems(productList);
    }

    @FXML
    private void handleConfirm() {
        Product selectedProduct = comboProduct.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showAlert("Error", "No Product Selected", "Please select a product to stock up.", Alert.AlertType.ERROR);
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
            showAlert("Error", "Invalid Quantity", "Please enter a valid quantity.", Alert.AlertType.ERROR);
            return;
        }

        updateStock(selectedProduct.getId(), quantityToAdd);
    }
    private void updateStock(int productId, int quantityToAdd) {
        try {
            productDAO.stockUp(productId, quantityToAdd);
            showAlert("Success", "Stock Updated", "The stock has been updated successfully.", Alert.AlertType.INFORMATION);
            txtQuantity.getScene().getWindow().hide();
        } catch (SQLException e) {
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

    private void setupAutoCompleteComboBox(ComboBox<Product> comboBox) {
        comboBox.setEditable(true);

        TextField editor = comboBox.getEditor();
        ObservableList<Product> items = comboBox.getItems();

        editor.textProperty().addListener((observable, oldValue, newValue) -> {
            ObservableList<Product> filteredItems = FXCollections.observableArrayList();
            String lowerCaseFilter = newValue.toLowerCase();

            for (Product product : items) {
                if (product.getName().toLowerCase().contains(lowerCaseFilter)) {
                    filteredItems.add(product);
                }
            }

            comboBox.setItems(filteredItems);

            if (!filteredItems.isEmpty()) {
                comboBox.show();
            }
        });

        editor.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                // Khi người dùng rời khỏi ô nhập liệu, đặt lại danh sách đầy đủ
                comboBox.setItems(items);

                // Xác nhận giá trị nhập vào
                String text = editor.getText();
                Product matchedProduct = items.stream()
                        .filter(product -> product.getName().equals(text))
                        .findFirst()
                        .orElse(null);

                if (matchedProduct != null) {
                    comboBox.getSelectionModel().select(matchedProduct);
                } else {
                    comboBox.getSelectionModel().clearSelection();
                }
            }
        });
    }
}