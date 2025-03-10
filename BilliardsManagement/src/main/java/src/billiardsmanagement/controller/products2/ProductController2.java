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
import src.billiardsmanagement.dao.RolesPermissionsDAO;
import src.billiardsmanagement.model.Category;
import src.billiardsmanagement.model.Product;
import src.billiardsmanagement.dao.CategoryDAO;
import src.billiardsmanagement.dao.ProductDAO;
import src.billiardsmanagement.model.User;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
    private User loggedInUser; // Lưu user đang đăng nhập
    private List<String> userPermissions = new ArrayList<>();


    @FXML
    public void initialize() throws SQLException {
        System.out.println("DEBUG: ProductController2 initialized.");
        setupSearchField(); // Thiết lập tìm kiếm sản phẩm
    }

    public void setUser(User user) throws SQLException {
        this.loggedInUser = user;
        if (user != null) {
            System.out.println("🟢 Gọi getUserPermissions() với user: " + user.getUsername());
            this.userPermissions = user.getPermissionsAsString();
            System.out.println("🔎 Debug: Quyền của user = " + userPermissions);
            loadCategories(); // Gọi phương thức để cập nhật UI
        } else {
            System.err.println("❌ Lỗi: loggedInUser chưa được set trong ProductController2!");
        }
    }

    private void loadCategories() throws SQLException {
        List<Category> categories = categoryDAO.getAllCategories();
        gridPane.getChildren().clear(); // Xóa bảng cũ trước khi load mới

        int categoryCount = categories.size();
        int rowCount = (int) Math.ceil(categoryCount / 2.0);

        for (int i = 0; i < categoryCount; i++) {
            Category category = categories.get(i);
            VBox categoryBox = createCategoryTable(category);
            int row = i / 2;
            int col = i % 2;
            gridPane.add(categoryBox, col, row);
        }

        btnAddNewCategory.setOnAction(event -> handleAddNewCategory());
    }

    private VBox createCategoryTable(Category category) throws SQLException {
        System.out.println("----- DEBUG: createCategoryTable() -----");

        if (category == null) {
            System.out.println("Category is NULL! Không thể tạo bảng.");
            return new VBox();
        } else {
            System.out.println("Category name: " + category.getName());
        }

        System.out.println("Danh sách quyền hiện tại: " + userPermissions);

        Label categoryLabel = new Label(category.getName());
        categoryLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 5px;");

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
        addProductButton.setVisible(userPermissions.contains("add_product"));

        Button updateCategoryButton = new Button();
        updateCategoryButton.setGraphic(updateCategoryIcon);
        updateCategoryButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        updateCategoryButton.setOnAction(event -> handleUpdateCategory(category));
        updateCategoryButton.setVisible(userPermissions.contains("update_product_category"));

        Button removeCategoryButton = new Button();
        removeCategoryButton.setGraphic(removeCategoryIcon);
        removeCategoryButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        removeCategoryButton.setOnAction(event -> handleRemoveCategory(category));
        removeCategoryButton.setVisible(userPermissions.contains("remove_product_category"));

        HBox headerBox = new HBox();
        headerBox.setSpacing(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        headerBox.getChildren().addAll(categoryLabel, spacer, addProductButton, updateCategoryButton, removeCategoryButton);

        TableView<Product> tableView = new TableView<>();
        tableView.setPrefSize(750, 300);

        TableColumn<Product, String> nameColumn = new TableColumn<>("Product Name");
        TableColumn<Product, Integer> quantityColumn = new TableColumn<>("Quantity");
        TableColumn<Product, Double> priceColumn = new TableColumn<>("Price");
        TableColumn<Product, String> unitColumn = new TableColumn<>("Unit");
        TableColumn<Product, Void> actionColumn = new TableColumn<>("Action");

        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        quantityColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        priceColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getPrice()).asObject());
        unitColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUnit()));

        quantityColumn.setSortType(TableColumn.SortType.DESCENDING);
        tableView.getSortOrder().add(quantityColumn);
        actionColumn.setCellFactory(col -> createActionCellFactory(product -> tableView.getItems().remove(product), userPermissions));

        tableView.getColumns().addAll(nameColumn, quantityColumn, priceColumn, unitColumn, actionColumn);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPlaceholder(new Label("No products available in this category."));

        List<Product> products = productDAO.getProductsByCategory(category.getId());
        System.out.println("Lấy sản phẩm cho danh mục: " + category.getName() + " (ID: " + category.getId() + ")");
        tableView.getItems().addAll(products);
        tableView.sort();

        VBox categoryBox = new VBox(headerBox, tableView);
        categoryBox.setSpacing(5);
        categoryBox.setStyle("-fx-border-color: #ccc; -fx-padding: 10px;");

        return categoryBox;
    }

    private TableCell<Product, Void> createActionCellFactory(Consumer<Product> onDelete, List<String> permissions) {
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

                // Kiểm tra quyền và ẩn các nút nếu không có quyền tương ứng
                stockUpButton.setVisible(permissions.contains("stock_up_product"));
                editButton.setVisible(permissions.contains("update_product"));
                deleteButton.setVisible(permissions.contains("remove_product"));

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
    private void setupSearchField() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                refreshTable();// Load lại toàn bộ danh mục sản phẩm nếu ô tìm kiếm rỗng
            } else {
                filterByProductName(newValue.toLowerCase());
            }
        });
    }

    private void filterByProductName(String searchText) {
        try {
            List<Product> allProducts = productDAO.getAllProducts(); // Lấy toàn bộ sản phẩm từ DB
            List<Product> filteredList = allProducts.stream()
                    .filter(product -> product.getName().toLowerCase().contains(searchText))
                    .toList(); // Lọc danh sách sản phẩm theo tên

            if (!filteredList.isEmpty()) {
                gridPane.getChildren().clear(); // Xóa danh mục cũ

                // Lấy danh mục của các sản phẩm tìm thấy
                Map<Integer, List<Product>> categoryMap = filteredList.stream()
                        .collect(Collectors.groupingBy(Product::getCategoryId));

                // Hiển thị từng danh mục trong gridPane
                int rowIndex = 0;
                for (Map.Entry<Integer, List<Product>> entry : categoryMap.entrySet()) {
                    Category productCategory = categoryDAO.getCategoryById(entry.getKey());
                    if (productCategory != null) {
                        VBox categoryBox = createCategoryTable(productCategory);

                        for (javafx.scene.Node child : categoryBox.getChildren()) {
                            if (child instanceof TableView<?> tableView) {
                                TableView<Product> productTableView = (TableView<Product>) tableView;
                                productTableView.getItems().clear();
                                productTableView.getItems().addAll(entry.getValue()); // Chỉ hiển thị sản phẩm tìm thấy
                            }
                        }

                        gridPane.add(categoryBox, 0, rowIndex++);
                    }
                }
            } else {
                gridPane.getChildren().clear(); // Nếu không có kết quả, xóa danh mục
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
        if (loggedInUser != null) {
            PermissionDAO permissionDAO = new PermissionDAO();
            List<String> permissions = permissionDAO.getUserPermissions(loggedInUser.getId());
//            System.out.println("✅ Permissions: " + permissions);

            btnAddNewCategory.setVisible(permissions.contains("add_product"));
//            addProductButton.setVisible(permissions.contains("add_product"));
//            editButton.setVisible(permissions.contains("add_product"));
//            deleteButton.setVisible(permissions.contains("add_product"));
//            stockUpButton.setVisible(permissions.contains("add_product"));
//            updateCategoryButton.setVisible(permissions.contains("add_product"));
//            removeCategoryButton.setVisible(permissions.contains("add_product"));
        } else {
            System.err.println("⚠️ Lỗi: currentUser bị null trong ProductController!");
        }
    }

//    private User loggedInUser;

//    public void setCurrentUser(User user) {
//        this.currentUser = user;
//    }
    public void setLoggedInUser(User user) {
//        System.out.println("🟢 Gọi setCurrentUser() với user: " + (user != null ? user.getUsername() : "null"));

        this.loggedInUser = user;
        if (user != null) {
//            System.out.println("🟢 Gọi setCurrentUser() với user: " + user.getUsername());
//            System.out.println("🎯 Kiểm tra quyền sau khi truyền user...");
            List<String> permissions = user.getPermissionsAsString();
//            System.out.println("🔎 Debug: Quyền sau khi truyền user = " + permissions);
            applyPermissions();
        } else {
            System.err.println("❌ Lỗi: currentUser vẫn null sau khi set!");
        }
    }

//    public List<String> setUserPermissions(User user) {
//        this.loggedInUser = user;
//
//        if (user != null) {
//            System.out.println("🟢 Gọi getUserPermissions() với user: " + loggedInUser.getUsername());
//            List<String> permissions = user.getPermissionsAsString();
//            System.out.println("🔎 Debug: Quyền của user = " + permissions);
//            return permissions;
//        } else {
//            System.err.println("❌ Lỗi: loggedInUser chưa được set trong ProductController2!");
//            return new ArrayList<>();
//        }
//    }
}
