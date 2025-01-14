package src.billiardsmanagement.controller.category;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import src.billiardsmanagement.model.Category;
import src.billiardsmanagement.model.TestDBConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RemoveCategoryController {

    @FXML
    private ComboBox<Category> comboBoxCategory;

    private ObservableList<Category> categoryList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        loadCategories();
    }

    // Tải các danh mục từ cơ sở dữ liệu vào ComboBox
    private void loadCategories() {
        try (Connection connection = TestDBConnection.getConnection()) {
            String sql = "SELECT category_id, category_name FROM category";
            ResultSet resultSet = connection.createStatement().executeQuery(sql);

            while (resultSet.next()) {
                int id = resultSet.getInt("category_id");
                String name = resultSet.getString("category_name");
                categoryList.add(new Category(id, name, null));
            }

            // Cập nhật ComboBox với danh sách danh mục
            comboBoxCategory.setItems(categoryList);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error loading categories: " + e.getMessage());
        }
    }

    @FXML
    public void handleRemove() {
        Category selectedCategory = comboBoxCategory.getValue();
        if (selectedCategory == null) {
            showError("Please select a category to delete.");
            return;
        }

        // Xác nhận trước khi xóa
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this category?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                removeCategory(selectedCategory);
            }
        });
    }

    // Xóa danh mục trong cơ sở dữ liệu
// Xóa danh mục trong cơ sở dữ liệu
    private void removeCategory(Category category) {
        String url = "jdbc:mysql://localhost:3306/biamanagement"; // Thay đổi URL cơ sở dữ liệu của bạn
        String user = "root"; // Thay đổi người dùng cơ sở dữ liệu của bạn
        String password = ""; // Thay đổi mật khẩu của bạn

        // Truy vấn kiểm tra xem danh mục có sản phẩm hay không
        String checkProductsSql = "SELECT COUNT(*) FROM products WHERE category_id = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement checkStmt = conn.prepareStatement(checkProductsSql)) {

            // Kiểm tra xem danh mục có sản phẩm nào không
            checkStmt.setInt(1, category.getId());
            ResultSet resultSet = checkStmt.executeQuery();

            if (resultSet.next()) {
                int productCount = resultSet.getInt(1);
                if (productCount > 0) {
                    // Nếu có sản phẩm, không cho phép xóa và thông báo lỗi
                    showError("Cannot delete category because it contains products.");
                    return; // Dừng lại nếu có sản phẩm
                }
            }

            // Nếu không có sản phẩm, tiếp tục thực hiện xóa danh mục
            String deleteSql = "DELETE FROM category WHERE category_id = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, category.getId());  // Xóa dựa trên ID của danh mục

                int rowsDeleted = deleteStmt.executeUpdate();
                if (rowsDeleted > 0) {
                    showInfo("Category deleted successfully!");
                    loadCategories();  // Tải lại danh mục sau khi xóa
                } else {
                    showError("No category found with the given ID.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error deleting category: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) comboBoxCategory.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.showAndWait();
    }
}
