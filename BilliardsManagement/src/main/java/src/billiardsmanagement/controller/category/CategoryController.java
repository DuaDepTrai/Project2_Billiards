package src.billiardsmanagement.controller.category;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import src.billiardsmanagement.controller.products.ProductController;
import src.billiardsmanagement.dao.CategoryDAO;
import src.billiardsmanagement.model.Category;
import src.billiardsmanagement.model.TestDBConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class CategoryController {
    @FXML
    private FlowPane flowPaneCategories; // FlowPane chứa các thumbnail
    @FXML
    private Button btnAddNewCategory;
    @FXML
    private Button btnUpdateCategory;
    @FXML
    private Button btnRemoveCategory;

    private ObservableList<Category> categoryList = FXCollections.observableArrayList();
    private CategoryDAO categoryDAO = new CategoryDAO();

    public void initialize() {
        loadCategories();

        // Button Add New Category
        btnAddNewCategory.setOnAction(event -> handleAddNewCategory());
        btnUpdateCategory.setOnAction(event -> handleUpdateCategory());
        btnRemoveCategory.setOnAction(event -> handleRemoveCategory());
    }

    private void loadCategories() {
        try {
            // Xóa danh sách cũ
            categoryList.clear();

            List<Category> categories = categoryDAO.getAllCategories();
            for (Category category : categories) {
                createCategoryThumbnail(category.getId(), category.getName(), category.getImagePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error loading categories: " + e.getMessage());
        }
    }

    private void createCategoryThumbnail(int id, String name, String imagePath) {
        try {
            // Đường dẫn tới hình ảnh
            String fullPath = "/src/billiardsmanagement/images/category/" + imagePath;

            // Kiểm tra nếu hình ảnh tồn tại, nếu không dùng ảnh mặc định
            Image image;
            if (getClass().getResource(fullPath) != null) {
                image = new Image(getClass().getResource(fullPath).toExternalForm());
            } else {
                image = new Image(getClass().getResource("/src/billiardsmanagement/images/category/default.png").toExternalForm());
            }

            // Tạo ImageView
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(100);
            imageView.setFitHeight(100);
            imageView.setPreserveRatio(true);

            // Tạo Text hiển thị tên danh mục
            Text categoryName = new Text(name);
            categoryName.setStyle("-fx-font-size: 14px; -fx-text-alignment: center;");

            // Đặt ImageView và Text vào VBox
            VBox vbox = new VBox(imageView, categoryName);
            vbox.setStyle("-fx-border-color: black; -fx-border-width: 2; -fx-border-radius: 5; -fx-padding: 5;-fx-alignment: center; -fx-padding: 10px;");
            vbox.setSpacing(5);

            // Thêm sự kiện click để mở danh sách sản phẩm
            vbox.setOnMouseClicked(event -> showProductList(id));

            // Thêm VBox vào FlowPane
            flowPaneCategories.getChildren().add(vbox);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error displaying category thumbnail: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showProductList(int categoryId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/products/products.fxml"));
            Parent root = loader.load();

            // Truyền dữ liệu sang ProductController
            ProductController controller = loader.getController();
            controller.loadProductsByCategory(categoryId);

            // Tạo cửa sổ mới
            Stage stage = new Stage();
            stage.setTitle("Products");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
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

    public void refreshTable() {
        flowPaneCategories.getChildren().clear();
        categoryList.clear();
        loadCategories();
    }

    private void handleUpdateCategory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/category/updateCategory.fxml"));
            Parent root = loader.load();
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

    private void handleRemoveCategory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/category/removeCategory.fxml"));
            Parent root = loader.load();
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
}