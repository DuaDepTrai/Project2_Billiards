package src.billiardsmanagement.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import src.billiardsmanagement.model.TestDBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AddCategoryController {
    @FXML
    private TextField txtName;

    @FXML
    private void handleAdd() {
        String name = txtName.getText();

        if (name.isEmpty()) {
            System.out.println("Please fill name of category.");
            return;
        }

        try (Connection connection = TestDBConnection.getConnection()) {
            // Insert new category
            String sql = "INSERT INTO category (category_name) VALUES (?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, name);

            statement.executeUpdate();
            System.out.println("Category added successfully!");

            // Close the Add Category window
            Stage stage = (Stage) txtName.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) txtName.getScene().getWindow();
        stage.close();
    }
}
