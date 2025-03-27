package src.billiardsmanagement.controller.products2;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import src.billiardsmanagement.dao.ProductDAO;
import src.billiardsmanagement.model.TestDBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AddProductController2 {
    @FXML
    private TextField txtName;
    @FXML
    private Label lblCategory;
    @FXML
    private TextField txtPrice;
    @FXML
    private TextField txtUnit;
    @FXML
    private TextField txtQuantity;

    private ObservableList<String> categoryList = FXCollections.observableArrayList();
    private ProductDAO productDAO = new ProductDAO();

    public void initialize() {

    }


//    private void loadCategories() {
//        try (Connection connection = TestDBConnection.getConnection()) {
//            String sql = "SELECT category_name FROM category";
//            ResultSet resultSet = connection.createStatement().executeQuery(sql);
//
//            while (resultSet.next()) {
//                categoryList.add(resultSet.getString("category_name"));
//            }
//            comboCategory.setItems(categoryList);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public void setCategoryName(String categoryName) {
        lblCategory.setText(categoryName);
    }

    @FXML
    private void handleAdd() {
        String name = txtName.getText();
        String category = lblCategory.getText();
        String price = txtPrice.getText();
        String unit = txtUnit.getText();
        String quantity = txtQuantity.getText();

        if (name.isEmpty() || category == null || price.isEmpty() || unit.isEmpty() || quantity.isEmpty()) {
            System.out.println("Please fill in all fields.");
            return;
        }

        // Hiển thị hộp thoại xác nhận trước khi cập nhật
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Add Product");
        alert.setHeaderText("Are you sure you want to add this product?");
        alert.setContentText("Product: " + name);

        ButtonType buttonYes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType buttonNo = new ButtonType("No", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(buttonYes, buttonNo);

        // Hiển thị Alert và lấy kết quả người dùng chọn
        alert.showAndWait().ifPresent(result -> {
            if (result == buttonYes) {
                try (Connection connection = TestDBConnection.getConnection()) {
                    // Get category_id from category name
                    String getCategorySql = "SELECT category_id FROM category WHERE category_name = ?";
                    PreparedStatement categoryStmt = connection.prepareStatement(getCategorySql);
                    categoryStmt.setString(1, category);
                    ResultSet resultSet = categoryStmt.executeQuery();
                    int categoryId = resultSet.next() ? resultSet.getInt("category_id") : 0;

                    // Thêm sản phẩm mới
                    productDAO.addProduct(name, categoryId, Double.parseDouble(price), unit, Integer.parseInt(quantity));

                    System.out.println("Product added successfully!");

                    // Đóng cửa sổ Add Product
                    Stage stage = (Stage) txtName.getScene().getWindow();
                    stage.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) txtName.getScene().getWindow();
        stage.close();
    }
}