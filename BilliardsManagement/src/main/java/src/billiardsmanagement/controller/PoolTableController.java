package src.billiardsmanagement.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import src.billiardsmanagement.dao.PoolTableDAO;
import src.billiardsmanagement.model.PoolTable;

public class PoolTableController {
    @FXML
    private FlowPane tablesContainer;

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    @FXML
    private Button addNewButton;

    private ObservableList<PoolTable> tableList = FXCollections.observableArrayList();
    private PoolTableDAO poolTableDAO = new PoolTableDAO();

    @FXML
    public void initialize() {
        // Set search icon
        ImageView searchIcon = new ImageView(
                new Image(getClass().getResourceAsStream("/src/billiardsmanagement/images/pooltables/searchIcon.png")));
        searchIcon.setFitHeight(16);
        searchIcon.setFitWidth(16);
        searchButton.setGraphic(searchIcon);

        // Load all tables
        handleViewAllTables();

        // Add search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterTables(newValue);
        });
    }

    private void filterTables(String searchText) {
        tablesContainer.getChildren().clear();
        if (searchText == null || searchText.isEmpty()) {
            tableList.forEach(this::createTableUI);
        } else {
            tableList.stream()
                    .filter(table -> table.getName().toLowerCase().contains(searchText.toLowerCase()))
                    .forEach(this::createTableUI);
        }
    }

    private void createTableUI(PoolTable table) {
        StackPane tableStack = new StackPane();
        tableStack.setPrefSize(76, 105);
        tableStack.getStyleClass().add("table-stack");

        // Background image
        ImageView bgImage = new ImageView(
                new Image(getClass().getResourceAsStream("/src/billiardsmanagement/images/pooltables/Background.png")));
        bgImage.setFitHeight(105);
        bgImage.setFitWidth(76);

        // Create VBox for text elements
        VBox textContainer = new VBox(5);
        textContainer.setAlignment(Pos.CENTER);

        // Table type text (now category name)
        Text typeText = new Text(table.getCatePooltableName());
        typeText.setStyle("-fx-font-weight: bold;");
        typeText.setUnderline(true);
        typeText.setFill(Color.valueOf("#c21e00"));

        // Table number text
        Text numberText = new Text(String.valueOf(table.getTableId()));
        numberText.setStyle("-fx-font-weight: bold; -fx-font-size: 26px;");
        numberText.setFill(Color.valueOf("#c21e00"));

        // Status text
        Text statusText = new Text(table.getStatus());
        statusText.setStyle("-fx-font-weight: bold;");
        statusText.setUnderline(true);

        // Set status color
        switch (table.getStatus()) {
            case "Available":
                statusText.setFill(Color.valueOf("#04ff00"));
                break;
            case "Ordered":
            case "Playing":
                statusText.setFill(Color.valueOf("#c21e00"));
                break;
        }

        textContainer.getChildren().addAll(typeText, numberText, statusText);

        // Create hover buttons container
        VBox hoverButtons = new VBox();
        hoverButtons.getStyleClass().add("hover-buttons");
        hoverButtons.setPrefSize(76, 105);
        hoverButtons.setAlignment(Pos.CENTER);
        hoverButtons.setSpacing(2);

        // Create Update button
        Button updateButton = new Button("Update");
        updateButton.getStyleClass().add("hover-button");
        updateButton.setMaxWidth(Double.MAX_VALUE);
        updateButton.setOnAction(e -> showEditDialog(table));

        // Create divider
        Region divider = new Region();
        divider.getStyleClass().add("divider");
        divider.setPrefHeight(1);
        divider.setMaxWidth(Double.MAX_VALUE);

        // Create Info button
        Button infoButton = new Button("Info");
        infoButton.getStyleClass().add("hover-button");
        infoButton.setMaxWidth(Double.MAX_VALUE);
        infoButton.setOnAction(e -> showInfoDialog(table));

        hoverButtons.getChildren().addAll(updateButton, divider, infoButton);

        // Add elements to stack
        tableStack.getChildren().addAll(bgImage, textContainer, hoverButtons);

        // Add to container with margin
        tablesContainer.getChildren().add(tableStack);
        FlowPane.setMargin(tableStack, new Insets(10));
    }

    private void showInfoDialog(PoolTable table) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Table Information");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER_LEFT);

        Text idText = new Text("Table ID: " + table.getTableId());
        Text nameText = new Text("Name: " + table.getName());
        Text categoryText = new Text("Category: " + table.getCatePooltableName());
        Text priceText = new Text("Price: $" + String.format("%.2f", table.getPrice()));
        Text statusText = new Text("Status: " + table.getStatus());

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> dialog.close());

        content.getChildren().addAll(idText, nameText, categoryText, priceText, statusText, closeButton);

        Scene scene = new Scene(content);
        scene.getStylesheets()
                .add(getClass().getResource("/src/billiardsmanagement/css/poolTable.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    @FXML
    private void showAddTableDialog() {
        // Create dialog
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Add New Table");

        // Create form elements
        TextField nameField = new TextField();
        nameField.setPromptText("Table Name");

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Available", "Ordered", "Playing");
        statusCombo.setPromptText("Status");

        // TODO: Add ComboBox for category selection
        // This should be populated from cate_pooltables table
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.setPromptText("Category");

        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        // Create layout
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        layout.getChildren().addAll(
                new Label("Table Name:"), nameField,
                new Label("Category:"), categoryCombo,
                new Label("Status:"), statusCombo,
                new HBox(10, saveButton, cancelButton));

        // Handle save
        saveButton.setOnAction(e -> {
            try {
                String name = nameField.getText();
                String status = statusCombo.getValue();
                // TODO: Get selected category ID
                int categoryId = 1; // Temporary default value

                if (name.isEmpty() || status == null) {
                    showAlert("Invalid Input", "Please fill in all fields");
                    return;
                }

                // Create and save new table
                PoolTable newTable = new PoolTable(0, name, status, categoryId, categoryCombo.getValue(), 0.0);
                int newId = poolTableDAO.addTable(newTable);
                newTable.setTableId(newId);

                // Refresh UI
                handleViewAllTables();
                dialog.close();

            } catch (Exception ex) {
                showAlert("Error", "Failed to add table: " + ex.getMessage());
            }
        });

        // Handle cancel
        cancelButton.setOnAction(e -> dialog.close());

        // Show dialog
        Scene scene = new Scene(layout);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void showEditDialog(PoolTable table) {
        // Create dialog
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Edit Table");

        // Create form elements
        TextField nameField = new TextField(table.getName());

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Available", "Ordered", "Playing");
        statusCombo.setValue(table.getStatus());

        // Create layout
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        layout.getChildren().addAll(
                new Label("Table Name:"), nameField,
                new Label("Status:"), statusCombo,
                new HBox(10, new Button("Save"), new Button("Cancel")));

        // Get the buttons
        Button saveButton = (Button) ((HBox) layout.getChildren().get(4)).getChildren().get(0);
        Button cancelButton = (Button) ((HBox) layout.getChildren().get(4)).getChildren().get(1);

        // Handle save
        saveButton.setOnAction(e -> {
            try {
                table.setName(nameField.getText());
                table.setStatus(statusCombo.getValue());

                poolTableDAO.updateTable(table);
                handleViewAllTables();
                dialog.close();

            } catch (Exception ex) {
                showAlert("Error", "Failed to update table: " + ex.getMessage());
            }
        });

        // Handle cancel
        cancelButton.setOnAction(e -> dialog.close());

        // Show dialog
        Scene scene = new Scene(layout);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    @FXML
    private void handleViewAllTables() {
        tableList.clear();
        tableList.addAll(poolTableDAO.getAllTables());

        // Clear and rebuild UI
        tablesContainer.getChildren().clear();
        tableList.forEach(this::createTableUI);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}