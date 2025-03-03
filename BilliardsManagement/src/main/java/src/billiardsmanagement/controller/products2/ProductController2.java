package src.billiardsmanagement.controller.products2;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import src.billiardsmanagement.dao.PermissionDAO;
import src.billiardsmanagement.model.Category;
import src.billiardsmanagement.model.Product;
import src.billiardsmanagement.dao.CategoryDAO;
import src.billiardsmanagement.dao.ProductDAO;
import src.billiardsmanagement.model.User;

import java.sql.SQLException;
import java.util.List;

public class ProductController2 {
    @FXML
    private GridPane gridPane;

    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private User currentUser; // Lưu user đang đăng nhập

    @FXML
    public void initialize() throws SQLException {
        List<Category> categories = categoryDAO.getAllCategories();
        int categoryCount = categories.size();

        gridPane.getChildren().clear(); // Xóa bảng cũ trước khi load mới

        int rowCount = (int) Math.ceil(categoryCount / 2.0); // Tính số hàng cần thiết

        for (int i = 0; i < categoryCount; i++) {
            Category category = categories.get(i);
            TableView<Product> tableView = createProductTable(category);

            int row = i / 2; // Chia thành 2 cột
            int col = i % 2;
            gridPane.add(tableView, col, row);
        }
    }

    private TableView<Product> createProductTable(Category category) throws SQLException {
        TableView<Product> tableView = new TableView<>();
        tableView.setPrefSize(400, 300); // Đặt kích thước mỗi bảng

        TableColumn<Product, String> nameColumn = new TableColumn<>("Product Name");
        TableColumn<Product, Integer> quantityColumn = new TableColumn<>("Quantity");
        TableColumn<Product, Double> priceColumn = new TableColumn<>("Price");
        TableColumn<Product, String> unitColumn = new TableColumn<>("Unit");

        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        quantityColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        priceColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getPrice()).asObject());
        unitColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUnit()));

        tableView.getColumns().addAll(nameColumn, quantityColumn, priceColumn, unitColumn);

        List<Product> products = productDAO.getProductsByCategory(category.getId());
        tableView.getItems().addAll(products);

        return tableView;
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
