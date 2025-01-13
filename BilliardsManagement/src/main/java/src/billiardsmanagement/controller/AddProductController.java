package src.billiardsmanagement.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import src.billiardsmanagement.model.TestDBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class AddProductController {
    @FXML
    private TextField txtName;
    @FXML
    private ComboBox<String> comboCategory;
    @FXML
    private TextField txtPrice;
    @FXML
    private TextField txtUnit;
    @FXML
    private TextField txtQuantity;

    private ObservableList<String> categoryList = FXCollections.observableArrayList();

    public void initialize() {
        loadCategories();
    }

    private void loadCategories() {
        try (Connection connection = TestDBConnection.getConnection()) {
            String sql = "SELECT category_name FROM category";
            ResultSet resultSet = connection.createStatement().executeQuery(sql);

            while (resultSet.next()) {
                categoryList.add(resultSet.getString("category_name"));
            }
            comboCategory.setItems(categoryList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAdd() {
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

            // Insert new product
            String sql = "INSERT INTO products (name, category_id, price, unit, quantity) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, name);
            statement.setInt(2, categoryId);
            statement.setDouble(3, Double.parseDouble(price));
            statement.setString(4, unit);
            statement.setInt(5, Integer.parseInt(quantity));

            statement.executeUpdate();
            System.out.println("Product added successfully!");

            // Close the Add Product window
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
