package src.billiardsmanagement.controller.products2;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import de.jensd.fx.glyphs.fontawesome.utils.FontAwesomeIconFactory;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.textfield.TextFields;
import src.billiardsmanagement.controller.category.RemoveCategoryController;
import src.billiardsmanagement.controller.category.UpdateCategoryController;
import src.billiardsmanagement.controller.products2.StockUpController2;
import src.billiardsmanagement.controller.products2.UpdateProductController2;
import src.billiardsmanagement.controller.category.CategoryController;
import src.billiardsmanagement.dao.PermissionDAO;
import src.billiardsmanagement.model.Category;
import src.billiardsmanagement.model.Product;
import src.billiardsmanagement.dao.CategoryDAO;
import src.billiardsmanagement.dao.ProductDAO;
import src.billiardsmanagement.model.User;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ProductController2 {
    @FXML
    private GridPane gridPane;
    @FXML
    private TableView<Product> tableProducts;
    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
    @FXML
    private Button btnAddNewCategory;


    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private User currentUser; // Lưu user đang đăng nhập

    @FXML
    public void initialize() throws SQLException {
        List<Category> categories = categoryDAO.getAllCategories();
        FontAwesomeIconFactory icons = FontAwesomeIconFactory.get();

        // Thêm tìm kiếm sản phẩm
        setupSearchField();

        int categoryCount = categories.size();

        gridPane.getChildren().clear(); // Xóa bảng cũ trước khi load mới

        int rowCount = (int) Math.ceil(categoryCount / 2.0); // Tính số hàng cần thiết

        for (int i = 0; i < categoryCount; i++) {
            Category category = categories.get(i);
            VBox categoryBox = createCategoryTable(category);

            int row = i / 2; // Chia thành 2 cột
            int col = i % 2;
            gridPane.add(categoryBox, col, row);
        }

        btnAddNewCategory.setOnAction(event -> handleAddNewCategory());

    }

    private VBox createCategoryTable(Category category) throws SQLException {
        System.out.println("----- DEBUG: createCategoryTable() -----");

        if (category == null) {
            System.out.println("Category is NULL! Không thể tạo bảng.");
            return new VBox(); // Trả về VBox rỗng để tránh lỗi
        } else {
            System.out.println("Category name: " + category.getName());
        }

        // Tiêu đề danh mục
        Label categoryLabel = new Label(category.getName());
        categoryLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 5px;");

        // Nút Add Product
        FontAwesomeIconView addProductIcon = new FontAwesomeIconView(FontAwesomeIcon.PLUS_CIRCLE);
        addProductIcon.setGlyphSize(18);

        FontAwesomeIconView updateCategoryIcon = new FontAwesomeIconView(FontAwesomeIcon.PENCIL);
        updateCategoryIcon.setGlyphSize(18);

        FontAwesomeIconView removeCategoryIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
        removeCategoryIcon.setGlyphSize(18);

        Button addProductButton = new Button();
        addProductButton.setGraphic(addProductIcon);
        addProductButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        addProductButton.setOnAction(event -> handleAddNewProduct(category));

        Button updateCategoryButton = new Button();
        updateCategoryButton.setGraphic(updateCategoryIcon);
        updateCategoryButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        updateCategoryButton.setOnAction(event -> handleUpdateCategory(category));

        Button removeCategoryButton = new Button();
        removeCategoryButton.setGraphic(removeCategoryIcon);
        removeCategoryButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        removeCategoryButton.setOnAction(event -> handleRemoveCategory(category));

        // Header chứa tiêu đề + nút Add
        HBox headerBox = new HBox();
        headerBox.setSpacing(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

// Tạo một Region để đẩy các nút về bên phải
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerBox.getChildren().addAll(categoryLabel, spacer, addProductButton, updateCategoryButton, removeCategoryButton);

        // Tạo TableView
        TableView<Product> tableView = new TableView<>();
//        tableView.setPrefHeight(200); // Hiển thị tối đa 5 sản phẩm (~40px mỗi dòng)
        tableView.setPrefSize(750, 300);
//        tableView.setMaxHeight(300);

        // Định nghĩa các cột
        TableColumn<Product, String> nameColumn = new TableColumn<>("Product Name");
        TableColumn<Product, Integer> quantityColumn = new TableColumn<>("Quantity");
        TableColumn<Product, Double> priceColumn = new TableColumn<>("Price");
        TableColumn<Product, String> unitColumn = new TableColumn<>("Unit");
        TableColumn<Product, Void> actionColumn = new TableColumn<>("Action");

        // Gán giá trị cho cột
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        quantityColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        priceColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getPrice()).asObject());
        unitColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUnit()));

        // Sắp xếp theo số lượng giảm dần
        quantityColumn.setSortType(TableColumn.SortType.DESCENDING);
        tableView.getSortOrder().add(quantityColumn);

        // Cột hành động (Xóa sản phẩm)
        actionColumn.setCellFactory(col -> createActionCellFactory(product -> tableView.getItems().remove(product)));

        // Gộp các cột vào bảng
        tableView.getColumns().addAll(nameColumn, quantityColumn, priceColumn, unitColumn, actionColumn);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Hiển thị thông báo nếu danh mục không có sản phẩm
        tableView.setPlaceholder(new Label("No products available in this category."));

        // Load dữ liệu từ database
        List<Product> products = productDAO.getProductsByCategory(category.getId());
        System.out.println("Lấy sản phẩm cho danh mục: " + category.getName() + " (ID: " + category.getId() + ")");
        tableView.getItems().addAll(products);
        tableView.sort();

        // Tạo VBox chứa bảng
        VBox categoryBox = new VBox(headerBox, tableView);
        categoryBox.setSpacing(5);
        categoryBox.setStyle("-fx-border-color: #ccc; -fx-padding: 10px;");

        return categoryBox;
    }

    private TableCell<Product, Void> createActionCellFactory(Consumer<Product> onDelete) {
        return new TableCell<>() {
            private final HBox container = new HBox(10);
            private final Button stockUpButton = new Button();
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();

            {
                FontAwesomeIconView stockUpIcon = new FontAwesomeIconView(FontAwesomeIcon.ARROW_CIRCLE_UP);
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
                    confirmAndRemoveProduct(product, getTableView());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, stockUpButton, editButton, deleteButton);
                    setGraphic(buttons);
                }
            }
        };
    }

    private void handleAddNewProduct(Category category) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/products2/addProduct2.fxml"));
            Parent root = loader.load();

            AddProductController2 controller = loader.getController();
            controller.setCategoryName(category.getName()); // Hiển thị category cố định

            Stage stage = new Stage();
            stage.setTitle("Add New Product");
            stage.setScene(new Scene(root));
            stage.show();
            stage.setOnHidden(event -> refreshTable());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openUpdateWindow(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/products2/updateProduct2.fxml"));
            Parent root = loader.load();
            UpdateProductController2 controller = loader.getController();
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

    private void confirmAndRemoveProduct(Product product, TableView<Product> tableView) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Remove");
        alert.setHeaderText("Are you sure you want to remove this product?");
        alert.setContentText("Product: " + product.getName());

        ButtonType buttonYes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType buttonNo = new ButtonType("No", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(buttonYes, buttonNo);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == buttonYes) {
            removeProduct(product, tableView);
        }
    }

    private void removeProduct(Product product, TableView<Product> tableView) {
        try {
            productDAO.removeProduct(product.getId());
            tableView.getItems().remove(product);
            tableView.refresh();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleStockUp(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/products2/stockUp2.fxml"));
            Parent root = loader.load();

            // Lấy controller của StockUpController
            StockUpController2 controller = loader.getController();
            controller.setSelectedProduct(product); // Truyền sản phẩm được chọn

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

    private void setupSearchField() throws SQLException {
        List<Product> allProducts = productDAO.getAllProducts(); // Lấy toàn bộ sản phẩm từ DB
        List<String> productNames = allProducts.stream()
                .map(Product::getName)
                .toList(); // Lấy danh sách tên sản phẩm

        // AutoComplete sử dụng ControlsFX
        TextFields.bindAutoCompletion(searchField, productNames);

        searchButton.setOnAction(event -> {
            String searchText = searchField.getText().trim();
            if (!searchText.isEmpty()) {
                filterByProductName(searchText);
            } else {
                try {
                    initialize(); // Load lại danh mục nếu tìm kiếm rỗng
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void filterByProductName(String productName) {
        try {
            // Tìm sản phẩm theo tên
            Product foundProduct = productDAO.getProductByName(productName);

            if (foundProduct != null) {
                // Lấy danh mục của sản phẩm
                Category productCategory = categoryDAO.getCategoryById(foundProduct.getCategoryId());

                if (productCategory != null) {
                    // Xóa tất cả danh mục hiện có trong gridPane
                    gridPane.getChildren().clear();

                    // Tạo VBox chứa danh mục với TableView chỉ chứa sản phẩm được tìm thấy
                    VBox categoryBox = createCategoryTable(productCategory);
                    for (javafx.scene.Node child : categoryBox.getChildren()) {
                        if (child instanceof TableView<?> tableView) {
                            TableView<Product> productTableView = (TableView<Product>) tableView;

                            // Xóa danh sách sản phẩm cũ và chỉ hiển thị sản phẩm được tìm thấy
                            productTableView.getItems().clear();
                            productTableView.getItems().add(foundProduct);
                        }
                    }

                    // Thêm danh mục vào gridPane
                    gridPane.add(categoryBox, 0, 0);
                }
            } else {
                // Hiển thị thông báo nếu không tìm thấy sản phẩm
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Not Found");
                alert.setHeaderText(null);
                alert.setContentText("No products found with name: " + productName);
                alert.showAndWait();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddNewCategory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/category/addCategory.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Add New Category");
            stage.setScene(new Scene(root));
            stage.show();

            stage.setOnHidden(event -> refreshTable());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleUpdateCategory(Category category) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/category/updateCategory.fxml"));
            Parent root = loader.load();

            UpdateCategoryController controller = loader.getController();
            controller.setCategoryName(category.getName()); // Hiển thị category cố định

            Stage stage = new Stage();
            stage.setTitle("Update Category");
            stage.setScene(new Scene(root));
            stage.show();

            stage.setOnHidden(event -> refreshTable());
        } catch (IOException e) {
            e.printStackTrace();
            showError("Could not load the Update Category interface: " + e.getMessage());
        }
    }

    private void handleRemoveCategory(Category category) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/category/removeCategory.fxml"));
            Parent root = loader.load();

            RemoveCategoryController controller = loader.getController();
            controller.setCategoryName(category.getName()); // Hiển thị category cố định

            Stage stage = new Stage();
            stage.setTitle("Remove Category");
            stage.setScene(new Scene(root));
            stage.show();

            stage.setOnHidden(event -> refreshTable());
        } catch (IOException e) {
            e.printStackTrace();
            showError("Could not load the Remove Category interface: " + e.getMessage());
        }
    }

    private void refreshTable() {
        try {
            initialize(); // Gọi lại initialize() để load lại sản phẩm
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void applyPermissions() {
        if (currentUser != null) {
            PermissionDAO permissionDAO = new PermissionDAO();
            List<String> permissions = permissionDAO.getUserPermissions(currentUser.getId());
            System.out.println("✅ Permissions: " + permissions);
        } else {
            System.err.println("⚠️ Lỗi: currentUser bị null trong ProductController!");
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
