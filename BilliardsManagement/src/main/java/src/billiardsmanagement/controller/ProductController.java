package src.billiardsmanagement.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import src.billiardsmanagement.model.Product;
import src.billiardsmanagement.model.TestDBConnection;

import javax.security.auth.callback.Callback;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductController {
    //Show list Product
    @FXML
    private TableView<Product> tableProducts;
    @FXML
    private TableColumn<Product, Integer> columnId;
    @FXML
    private TableColumn<Product, String> columnName;
    @FXML
    private TableColumn<Product, String> columnCategory;
    @FXML
    private TableColumn<Product, Double> columnPrice;
    @FXML
    private TableColumn<Product, String> columnUnit;
    @FXML
    private TableColumn<Product, Integer> columnQuantity;
    @FXML
    private Button btnAddNewProduct;

    private ObservableList<Product> productList = FXCollections.observableArrayList();

    public void initialize() {
        // Liên kết các cột với thuộc tính của lớp Product
        columnId.setCellValueFactory(new PropertyValueFactory<>("id"));
        columnName.setCellValueFactory(new PropertyValueFactory<>("name"));
        columnCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        columnPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        columnUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        columnQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        // Thêm cột "Update"
        TableColumn<Product, Void> colUpdate = new TableColumn<>();
        colUpdate.setCellFactory(param -> new TableCell<>() {
            private final Button btnUpdate = new Button("Update");

            {
                btnUpdate.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    openUpdateWindow(product);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnUpdate);
                }
            }
        });

        tableProducts.getColumns().add(colUpdate);

        // Thêm cột "Remove"
        TableColumn<Product, Void> colRemove = new TableColumn<>();
        colRemove.setCellFactory(param -> new TableCell<>() {
            private final Button btnRemove = new Button("Remove");

            {
                btnRemove.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    confirmAndRemoveProduct(product);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnRemove);
                }
            }
        });

        tableProducts.getColumns().add(colRemove);

        // Load dữ liệu từ database
        loadProducts();

        //button Add New Product
        btnAddNewProduct.setOnAction(event -> handleAddNewProduct());
    }

    private void loadProducts() {
        try {
            Connection connection = TestDBConnection.getConnection();
            String sql = "SELECT p.product_id, p.name, c.category_name, p.price, p.unit, p.quantity " +
                    "FROM products p " +
                    "JOIN category c ON p.category_id = c.category_id";

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                int id = resultSet.getInt("product_id");
                String name = resultSet.getString("name");
                String category = resultSet.getString("category_name");
                double price = resultSet.getDouble("price");
                String unit = resultSet.getString("unit");
                int quantity = resultSet.getInt("quantity");

                System.out.println("Sản phẩm: " + name + " - Danh mục: " + category);
                productList.add(new Product(id, name, category, price, unit, quantity));
            }

            tableProducts.setItems(productList);

            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Add new product
    @FXML
    private void handleAddNewProduct() {
        try {
            // Load the Add Product scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/add_product.fxml"));
            Parent root = loader.load();

            // Create a new stage for the Add Product window
            Stage stage = new Stage();
            stage.setTitle("Add New Product");
            stage.setScene(new Scene(root));
            stage.show();

            // Reload the product table after adding a new product
            stage.setOnHidden(event -> refreshTable());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Refresh table
    private void refreshTable() {
        productList.clear();
        loadProducts();
    }

    //Confirm remove product
    private void confirmAndRemoveProduct(Product product) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Remove");
        alert.setHeaderText("Are you sure you want to remove this product?");
        alert.setContentText("Product: " + product.getName());

        ButtonType buttonYes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType buttonNo = new ButtonType("No", ButtonBar.ButtonData.NO);

        alert.getButtonTypes().setAll(buttonYes, buttonNo);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == buttonYes) {
            removeProduct(product);
        }
    }

    //Remove Product
    private void removeProduct(Product product) {
        try (Connection connection = TestDBConnection.getConnection()) {
            String query = "DELETE FROM products WHERE product_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, product.getId());
            statement.executeUpdate();

            // Xóa sản phẩm khỏi danh sách
            productList.remove(product);

            // Làm mới bảng
            tableProducts.refresh();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            // Xử lý lỗi từ getConnection()
            e.printStackTrace();
        }
    }

    //Update product
    private void openUpdateWindow(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/update_product.fxml"));
            Parent root = loader.load();

            UpdateProductController controller = loader.getController();
            controller.setProductData(product);  // Truyền dữ liệu sản phẩm vào cửa sổ update

            Stage stage = new Stage();
            stage.setTitle("Update Product");
            stage.setScene(new Scene(root));
            stage.show();

            // Reload lại bảng sau khi cập nhật
            stage.setOnHidden(event -> refreshTable());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //loadProductsByCategory
    public void loadProductsByCategory(int categoryId) {
        productList.clear(); // Xóa danh sách hiện tại để nạp dữ liệu mới
        try (Connection connection = TestDBConnection.getConnection()) {
            String sql = "SELECT p.product_id, p.name, c.category_name, p.price, p.unit, p.quantity " +
                    "FROM products p " +
                    "JOIN category c ON p.category_id = c.category_id " +
                    "WHERE c.category_id = ?";

            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, categoryId);
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

            tableProducts.setItems(productList); // Hiển thị dữ liệu trên bảng

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
