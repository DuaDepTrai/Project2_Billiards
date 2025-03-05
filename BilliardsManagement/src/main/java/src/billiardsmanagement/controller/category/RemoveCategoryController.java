package src.billiardsmanagement.controller.category;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import src.billiardsmanagement.model.Category;
import src.billiardsmanagement.dao.CategoryDAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RemoveCategoryController {
//    @FXML
//    private ComboBox<Category> comboBoxCategory;
    @FXML
    private Label lblCategory;

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
//            comboBoxCategory.setItems(categoryList);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error loading categories: " + e.getMessage());
        }
    }

    public void setCategoryName(String categoryName) {
        lblCategory.setText(categoryName);
    }


    @FXML
    public void handleRemove() {
        String categoryName = lblCategory.getText();
        Category selectedCategory = categoryList.stream()
                .filter(category -> category.getName().equals(categoryName))
                .findFirst()
                .orElse(null);

        if (selectedCategory == null) {
            showError("Please select a category to delete.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this category?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                removeCategory(selectedCategory);
            }
        });
    }

    private void removeCategory(Category category) {
        if (!categoryDAO.hasProducts(category.getId())) {
            categoryDAO.removeCategory(category.getId());
            showInfo("Category deleted successfully!");
            closeWindow();
            loadCategories();
        } else {
            showError("Cannot delete category because it contains products.");
            closeWindow();
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) lblCategory.getScene().getWindow();
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