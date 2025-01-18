package src.billiardsmanagement.controller.products;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import src.billiardsmanagement.model.Product;
import src.billiardsmanagement.model.TestDBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StockUpController {
    @FXML
    private ComboBox<Product> comboProduct;
    @FXML
    private TextField txtQuantity;

    private ObservableList<Product> productList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadProducts();

        setupAutoCompleteComboBox(comboProduct);
    }

    private void loadProducts() {
        try (Connection connection = TestDBConnection.getConnection()) {
            String sql = "SELECT p.product_id, p.name, c.category_name, p.price, p.unit, p.quantity " +
                    "FROM products p " +
                    "JOIN category c ON p.category_id = c.category_id";

            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("product_id");
                String name = resultSet.getString("name");
                String category = resultSet.getString("category_name");
                double price = resultSet.getDouble("price");
                String unit = resultSet.getString("unit");
                int quantity = resultSet.getInt("quantity");

                productList.add(new Product(id, name, category, price, unit, quantity));
            }

            comboProduct.setItems(productList);
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        try (Connection connection = TestDBConnection.getConnection()) {
            String sql = "UPDATE products SET quantity = quantity + ? WHERE product_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, quantityToAdd);
            statement.setInt(2, productId);
            statement.executeUpdate();

            showAlert("Success", "Stock Updated", "The stock has been updated successfully.", Alert.AlertType.INFORMATION);

            // Đóng cửa sổ sau khi cập nhật
            txtQuantity.getScene().getWindow().hide();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
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
        comboBox.setEditable(true); // Cho phép người dùng nhập liệu

        TextField editor = comboBox.getEditor();
        ObservableList<Product> items = comboBox.getItems();

        editor.textProperty().addListener((observable, oldValue, newValue) -> {
            // Lọc danh sách sản phẩm dựa trên nội dung nhập vào
            ObservableList<Product> filteredItems = FXCollections.observableArrayList();
            String lowerCaseFilter = newValue.toLowerCase();

            for (Product product : items) {
                if (product.getName().toLowerCase().contains(lowerCaseFilter)) {
                    filteredItems.add(product);
                }
            }

            comboBox.setItems(filteredItems);

            // Nếu danh sách có kết quả, hiển thị menu gợi ý
            if (!filteredItems.isEmpty()) {
                comboBox.show();
            }
        });

        // Khôi phục danh sách đầy đủ khi người dùng xóa nội dung
        editor.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) { // Khi mất focus
                comboBox.setItems(items);
            }
        });
    }

}
