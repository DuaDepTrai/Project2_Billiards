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
import src.billiardsmanagement.controller.products2.StockUpController2;
import src.billiardsmanagement.controller.products2.UpdateProductController2;
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
    }

    private VBox createCategoryTable(Category category) throws SQLException {
        if (category == null) {
            System.out.println("Category is null at index: ");
        } else {
            System.out.println("Category name: " + category.getName());
        }

        Label categoryLabel = new Label(category.getName()); // Tiêu đề là tên danh mục
        categoryLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 5px;");

        // Tạo nút Add Product với icon
        FontAwesomeIconView addIcon = new FontAwesomeIconView(FontAwesomeIcon.PLUS_CIRCLE);
        addIcon.setGlyphSize(18);

        Button addButton = new Button();
        addButton.setGraphic(addIcon);
        addButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

        // Thêm sự kiện cho nút Add Product
        addButton.setOnAction(event -> {
            System.out.println("Thêm sản phẩm vào danh mục: " + category.getName());
            // Gọi method mở form thêm sản phẩm
            handleAddNewProduct(category);
        });

        // Đặt tiêu đề + nút vào HBox
        HBox headerBox = new HBox();
        headerBox.setSpacing(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getChildren().addAll(categoryLabel, new Region(), addButton);
        HBox.setHgrow(headerBox.getChildren().get(1), Priority.ALWAYS); // Đẩy nút về phải


        TableView<Product> tableView = new TableView<>();
        tableView.setPrefSize(500, 350); // Đặt kích thước mỗi bảng

        TableColumn<Product, String> nameColumn = new TableColumn<>("Product Name");
        TableColumn<Product, Integer> quantityColumn = new TableColumn<>("Quantity");
        TableColumn<Product, Double> priceColumn = new TableColumn<>("Price");
        TableColumn<Product, String> unitColumn = new TableColumn<>("Unit");
        TableColumn<Product, Void> actionColumn = new TableColumn<>("Action");

        // Kéo dãn tối đa chiều rộng cột
        nameColumn.setPrefWidth(100);
        quantityColumn.setPrefWidth(100);
        priceColumn.setPrefWidth(100);
        unitColumn.setPrefWidth(100);
        actionColumn.setPrefWidth(150);

        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        quantityColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        priceColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getPrice()).asObject());
        unitColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUnit()));

        quantityColumn.setSortType(TableColumn.SortType.DESCENDING);
        tableView.getSortOrder().add(quantityColumn);

        actionColumn.setCellFactory(col -> createActionCellFactory(product -> {
            tableView.getItems().remove(product); // Xóa khỏi UI sau khi delete
        }));

        tableView.getColumns().addAll(nameColumn, quantityColumn, priceColumn, unitColumn, actionColumn);

        // Kéo dãn cột theo bảng
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        List<Product> products = productDAO.getProductsByCategory(category.getId());
        tableView.getItems().addAll(products);

        tableView.sort(); // Kích hoạt sắp xếp ngay khi load dữ liệu

        VBox categoryBox = new VBox();
        categoryBox.getChildren().addAll(headerBox, tableView);
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
            Product foundProduct = productDAO.getProductByName(productName); // Trả về 1 sản phẩm, không phải danh sách

            if (foundProduct != null) {
                Category productCategory = categoryDAO.getCategoryById(foundProduct.getId());

                gridPane.getChildren().clear(); // Xóa danh mục cũ
                VBox categoryBox = createCategoryTable(productCategory);
                gridPane.add(categoryBox, 0, 0); // Hiển thị sản phẩm ở vị trí đầu tiên
            } else {
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


    private void refreshTable() {
        try {
            initialize(); // Gọi lại initialize() để load lại sản phẩm
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
