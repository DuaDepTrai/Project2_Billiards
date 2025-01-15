package src.billiardsmanagement.controller.products;

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
import src.billiardsmanagement.dao.ProductDAO;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class ProductController {
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
    @FXML
    private Button btnStockUp;
    @FXML
    private Button btnUpdateProduct;
    @FXML
    private Button btnRemoveProduct;

    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private ProductDAO productDAO = new ProductDAO();

    public void initialize() {
        columnId.setCellValueFactory(new PropertyValueFactory<>("id"));
        columnName.setCellValueFactory(new PropertyValueFactory<>("name"));
        columnCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        columnPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        columnUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        columnQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        loadProducts();

        btnAddNewProduct.setOnAction(event -> handleAddNewProduct());
        btnStockUp.setOnAction(event -> handleStockUp());
        btnUpdateProduct.setOnAction(event -> handleUpdateSelectedProduct());
        btnRemoveProduct.setOnAction(event -> handleRemoveSelectedProduct());
    }

    private void loadProducts() {
        try {
            productList.clear();
            productList.addAll(productDAO.getAllProducts());
            tableProducts.setItems(productList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddNewProduct() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/products/addProduct.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Add New Product");
            stage.setScene(new Scene(root));
            stage.show();
            stage.setOnHidden(event -> refreshTable());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshTable() {
        loadProducts();
    }

    private void handleStockUp() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/products/stockUp.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Stock Up Product");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
            stage.setOnHidden(event -> refreshTable());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRemoveSelectedProduct() {
        Product selectedProduct = tableProducts.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Product Selected");
            alert.setContentText("Please select a product to remove");
            alert.showAndWait();
            return;
        }
        confirmAndRemoveProduct(selectedProduct);
    }

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

    private void removeProduct(Product product) {
        try {
            productDAO.removeProduct(product.getId());
            productList.remove(product);
            tableProducts.refresh();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleUpdateSelectedProduct() {
        Product selectedProduct = tableProducts.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Product Selected");
            alert.setContentText("Please select a product to update");
            alert.showAndWait();
            return;
        }
        openUpdateWindow(selectedProduct);
    }

    private void openUpdateWindow(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/products/updateProduct.fxml"));
            Parent root = loader.load();
            UpdateProductController controller = loader.getController();
            controller.setProductData(product);

            Stage stage = new Stage();
            stage.setTitle("Update Product");
            stage.setScene(new Scene(root));
            stage.show();
            stage.setOnHidden(event -> refreshTable());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadProductsByCategory(int categoryId) {
        try {
            productList.clear();
            productList.addAll(productDAO.getProductsByCategory(categoryId));
            tableProducts.setItems(productList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}