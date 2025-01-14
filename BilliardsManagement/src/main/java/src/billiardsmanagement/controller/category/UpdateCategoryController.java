package src.billiardsmanagement.controller.category;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import src.billiardsmanagement.model.Category;
import src.billiardsmanagement.model.TestDBConnection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UpdateCategoryController {
    @FXML
    private ComboBox<Category> comboBoxCategory;
    @FXML
    private TextField txtNewCategoryName;
    @FXML
    private Label lblImagePath;

    private String imagePath;
    private ObservableList<Category> categoryList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        loadCategories();
    }

    private void loadCategories() {
        try (Connection connection = TestDBConnection.getConnection()) {
            String sql = "SELECT category_id, category_name FROM category";
            ResultSet resultSet = connection.createStatement().executeQuery(sql);

            while (resultSet.next()) {
                int id = resultSet.getInt("category_id");
                String name = resultSet.getString("category_name");
                categoryList.add(new Category(id, name, null));
            }

            comboBoxCategory.setItems(categoryList);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error loading categories: " + e.getMessage());
        }
    }

    @FXML
    public void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(new Stage());

        if (file != null) {
            // Copy the file to the images folder
            try {
                Path targetDir = Paths.get("src/billiardsmanagement/images/category/");
                if (!Files.exists(targetDir)) {
                    Files.createDirectories(targetDir);
                }

                Path targetPath = targetDir.resolve(file.getName());
                Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                // Update imagePath with the relative path
                imagePath = file.getName();
                lblImagePath.setText(imagePath);
            } catch (IOException e) {
                e.printStackTrace();
                showError("Error uploading image: " + e.getMessage());
            }
        } else {
            lblImagePath.setText("No file selected");
        }
    }

    @FXML
    public void handleSave() {
        // Validate input
        Category selectedCategory = comboBoxCategory.getValue();
        String newCategoryName = txtNewCategoryName.getText();

        if (selectedCategory == null) {
            showError("Please select a category to update.");
            return;
        }

        // Lấy ID của danh mục được chọn
        int categoryId = selectedCategory.getId();
        if (categoryId == 0) {
            showError("Invalid category selected.");
            return;
        }

        // Database connection and update logic
        String url = "jdbc:mysql://localhost:3306/biamanagement"; // Replace with your database details
        String user = "root"; // Replace with your database user
        String password = ""; // Replace with your database password

        String sql = "UPDATE category " +
                "SET " +
                "category_name = CASE WHEN ? IS NOT NULL AND ? != '' THEN ? ELSE category_name END, " +
                "image_path = CASE WHEN ? IS NOT NULL AND ? != '' THEN ? ELSE image_path END " +
                "WHERE category_id = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set parameters for SQL
            stmt.setString(1, newCategoryName); // CASE WHEN condition for name
            stmt.setString(2, newCategoryName);
            stmt.setString(3, newCategoryName);

            stmt.setString(4, imagePath); // CASE WHEN condition for image_path
            stmt.setString(5, imagePath);
            stmt.setString(6, imagePath);

            stmt.setInt(7, categoryId); // WHERE condition

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                showInfo("Category updated successfully!");
            } else {
                showError("No category found with the given ID.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error updating category: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.showAndWait();
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) comboBoxCategory.getScene().getWindow();
        stage.close();
    }
}
