package src.billiardsmanagement.controller.products;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import src.billiardsmanagement.model.Product;
import src.billiardsmanagement.model.TestDBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UpdateProductController {
    @FXML private TextField txtName;
    @FXML private ComboBox<String> comboCategory;
    @FXML private TextField txtPrice;
    @FXML private TextField txtUnit;
    @FXML private TextField txtQuantity;

    public void initialize() {
        loadCategories();
    }

    private void loadCategories() {
        try (Connection connection = TestDBConnection.getConnection()) {
            String sql = "SELECT category_name FROM category";
            ResultSet resultSet = connection.createStatement().executeQuery(sql);
            while (resultSet.next()) {
                comboCategory.getItems().add(resultSet.getString("category_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int productId;  // Thêm biến để lưu trữ product_id
    public void setProductData(Product product) {
        this.productId = product.getId();  // Lưu product_id vào biến
        txtName.setText(product.getName());
        comboCategory.setValue(product.getCategory());
        txtPrice.setText(String.valueOf(product.getPrice()));
        txtUnit.setText(product.getUnit());
        txtQuantity.setText(String.valueOf(product.getQuantity()));
    }

    @FXML
    private void handleUpdate() {
        String name = txtName.getText();
        String category = comboCategory.getValue();
        String price = txtPrice.getText();
        String unit = txtUnit.getText();
        String quantity = txtQuantity.getText();

        if (name.isEmpty() || category == null || price.isEmpty() || unit.isEmpty() || quantity.isEmpty()) {
            System.out.println("Please fill in all fields.");
            return;
        }

        try (Connection connection = TestDBConnection.getConnection()) {
            // Get category_id from category name
            String getCategorySql = "SELECT category_id FROM category WHERE category_name = ?";
            PreparedStatement categoryStmt = connection.prepareStatement(getCategorySql);
            categoryStmt.setString(1, category);
            ResultSet resultSet = categoryStmt.executeQuery();
            int categoryId = resultSet.next() ? resultSet.getInt("category_id") : 0;

            // Update product information
            String sql = "UPDATE products SET name = ?, category_id = ?, price = ?, unit = ?, quantity = ? WHERE product_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, name);
            statement.setInt(2, categoryId);
            statement.setDouble(3, Double.parseDouble(price));
            statement.setString(4, unit);
            statement.setInt(5, Integer.parseInt(quantity));
            statement.setInt(6, productId/* Pass product id here*/);

            statement.executeUpdate();
            System.out.println("Product updated successfully!");

            // Close the Update Product window
            Stage stage = (Stage) txtName.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) txtName.getScene().getWindow();
        stage.close();
    }
}

