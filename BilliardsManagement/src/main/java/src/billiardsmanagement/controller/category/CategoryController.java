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
import src.billiardsmanagement.model.Category;
import src.billiardsmanagement.model.TestDBConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class CategoryController {
    @FXML
    private FlowPane flowPaneCategories; // FlowPane chứa các thumbnail
    @FXML
    private Button btnAddNewCategory;

    private ObservableList<Category> categoryList = FXCollections.observableArrayList();

    public void initialize() {
        loadCategories();

        //button Add New Category
        btnAddNewCategory.setOnAction(event -> handleAddNewCategory());

    }

    private void loadCategories() {
        try {
            // Lấy dữ liệu từ cơ sở dữ liệu
            Connection connection = TestDBConnection.getConnection();
            String sql = "SELECT c.category_id, c.category_name, c.image_path FROM category c";

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            // Xóa danh sách cũ
            categoryList.clear();

            while (resultSet.next()) {
                int id = resultSet.getInt("category_id");
                String name = resultSet.getString("category_name");
                String imagePath = resultSet.getString("image_path");

                // Tạo thumbnail cho mỗi danh mục
                createCategoryThumbnail(id, name, imagePath);
            }

            resultSet.close();
            statement.close();
            connection.close();
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
                // Dùng ảnh mặc định nếu không tìm thấy
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
            stage.initModality(Modality.APPLICATION_MODAL); // Chặn tương tác với cửa sổ khác
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddNewCategory() {
        try {
            // Load the Add Category scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/category/add_category.fxml"));
            Parent root = loader.load();

            // Create a new stage for the Add Category window
            Stage stage = new Stage();
            stage.setTitle("Add New Category");
            stage.setScene(new Scene(root));
            stage.show();

            // Reload the category table after adding a new category
            stage.setOnHidden(event -> refreshTable());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Refresh table
    private void refreshTable() {
        // Xóa các phần tử cũ trong FlowPane
        flowPaneCategories.getChildren().clear();

        // Xóa danh sách cũ và tải lại danh sách mới
        categoryList.clear();
        loadCategories();
    }
}
