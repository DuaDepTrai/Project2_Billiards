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
import src.billiardsmanagement.dao.CategoryDAO;
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
    private CategoryDAO categoryDAO = new CategoryDAO();

    @FXML
    private void initialize() {
        loadCategories();
    }

    private void loadCategories() {
        try {
            categoryList.clear();
            for (Category category : categoryDAO.getAllCategories()) {
                categoryList.add(category);
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
            try {
                Path targetDir = Paths.get("BilliardsManagement/src/main/resources/src/billiardsmanagement/images/category/");
                if (!Files.exists(targetDir)) {
                    Files.createDirectories(targetDir);
                }

                Path targetPath = targetDir.resolve(file.getName());
                Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
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
        Category selectedCategory = comboBoxCategory.getValue();
        String newCategoryName = txtNewCategoryName.getText();

        if (selectedCategory == null) {
            showError("Please select a category to update.");
            return;
        }

        int categoryId = selectedCategory.getId();
        if (categoryId == 0) {
            showError("Invalid category selected.");
            return;
        }

        categoryDAO.updateCategory(categoryId, newCategoryName);
        showInfo("Category updated successfully!");
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