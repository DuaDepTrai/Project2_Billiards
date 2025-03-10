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
import javafx.util.Callback;
import src.billiardsmanagement.dao.PermissionDAO;
import src.billiardsmanagement.model.Product;
import src.billiardsmanagement.dao.ProductDAO;
import src.billiardsmanagement.model.User;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
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
    private TableColumn<Product, Void> columnAction;
    @FXML
    private Button btnAddNewProduct;

    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private ProductDAO productDAO = new ProductDAO();
    private User currentUser; // Lưu user đang đăng nhập

    public void initialize() { // Truyền user vào để kiểm tra quyền
        System.out.println("Debug: ProductController đã khởi động!");

        columnId.setCellValueFactory(new PropertyValueFactory<>("id"));
        columnName.setCellValueFactory(new PropertyValueFactory<>("name"));
        columnCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        columnPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        columnUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        columnQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        columnAction.setCellFactory(createActionCellFactory());

//        loadProducts();

        btnAddNewProduct.setOnAction(event -> handleAddNewProduct());
    }

    private void applyPermissions() {
        if (currentUser != null) {
            PermissionDAO permissionDAO = new PermissionDAO();

            List<String> permissions = permissionDAO.getUserPermissions(currentUser.getId());
            System.out.println("✅ Permissions: " + permissions);

            btnAddNewProduct.setVisible(permissions.contains("add_product"));
        } else {
            System.err.println("⚠️ Lỗi: currentUser bị null trong ProductController!");
        }
    }

//    private void loadProducts() {
//        try {
//            productList.clear();
//            List<Product> products = productDAO.getAllProducts();
//            System.out.println("Debug: Số sản phẩm lấy từ DB = " + products.size());
//
//            if (products.isEmpty()) {
//                System.out.println("⚠ Cảnh báo: Không có sản phẩm nào trong DB!");
//            } else {
//                for (Product p : products) {
//                    System.out.println("✅ Sản phẩm: " + p.getName());
//                }
//            }
//
//            productList.addAll(products);
//            tableProducts.setItems(productList);
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }

    private Callback<TableColumn<Product, Void>, TableCell<Product, Void>> createActionCellFactory() {
        return column -> new TableCell<>() {
            private final HBox container = new HBox(10);
            private final Button stockUpButton = new Button();
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();

            {
                FontAwesomeIconView stockUpIcon = new FontAwesomeIconView(FontAwesomeIcon.ARROW_CIRCLE_ALT_DOWN);
                stockUpIcon.setSize("16");
                stockUpButton.setGraphic(stockUpIcon);
                stockUpButton.getStyleClass().add("action-button");

                FontAwesomeIconView editIcon = new FontAwesomeIconView(FontAwesomeIcon.PENCIL);
                editIcon.setSize("16");
                editButton.setGraphic(editIcon);
                editButton.getStyleClass().add("action-button");

                FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
                deleteIcon.setSize("16");
                deleteButton.setGraphic(deleteIcon);
                deleteButton.getStyleClass().add("action-button");

                container.setAlignment(Pos.CENTER);
                container.getChildren().addAll(stockUpButton, editButton, deleteButton);

                stockUpButton.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    handleStockUp(product);
                });

                editButton.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    openUpdateWindow(product);
                });

                deleteButton.setOnAction(event -> {
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
                    setGraphic(container);
                }
            }
        };
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
//            stage.setOnHidden(event -> refreshTable());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private void refreshTable() {
//        loadProducts();
//    }

    private void handleStockUp(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/products/stockUp.fxml"));
            Parent root = loader.load();

            // Lấy controller của StockUpController
            StockUpController controller = loader.getController();
            controller.setSelectedProduct(product); // Truyền sản phẩm được chọn

            Stage stage = new Stage();
            stage.setTitle("Stock Up Product");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
//            stage.setOnHidden(event -> refreshTable());
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
//            stage.setOnHidden(event -> refreshTable());
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

    private User loggedInUser;

    public void setLoggedInUser(User user) {
        System.out.println("🟢 Gọi setCurrentUser() với user: " + (user != null ? user.getUsername() : "null"));

        this.loggedInUser = user;
        if (user != null) {
            System.out.println("🟢 Gọi setCurrentUser() với user: " + user.getUsername());
            System.out.println("🎯 Kiểm tra quyền sau khi truyền user...");
            List<String> permissions = user.getPermissionsAsString();
            System.out.println("🔎 Debug: Quyền sau khi truyền user = " + permissions);
            applyPermissions();
        } else {
            System.err.println("❌ Lỗi: currentUser vẫn null sau khi set!");
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

}