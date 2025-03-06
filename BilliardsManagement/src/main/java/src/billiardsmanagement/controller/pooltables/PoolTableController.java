package src.billiardsmanagement.controller.pooltables;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import src.billiardsmanagement.dao.CatePooltableDAO;
import src.billiardsmanagement.dao.PoolTableDAO;
import src.billiardsmanagement.model.CatePooltable;
import src.billiardsmanagement.model.PoolTable;
import src.billiardsmanagement.service.NotificationService;
import src.billiardsmanagement.model.NotificationStatus;
import src.billiardsmanagement.controller.pooltables.catepooltables.UpdateCategoryPooltableController;

import java.io.IOException;
import java.util.ArrayList;

public class PoolTableController {
    public MFXScrollPane availableTableScrollPane;
    public MFXScrollPane catePooltablesScrollPane;
    public Button addNewTableCategory;

    @FXML
    protected FlowPane tablesContainer;

    @FXML
    protected TextField searchField;

    @FXML
    protected Button searchButton;

    @FXML
    protected Button addNewButton;

    @FXML
    private TextField addNameField;
    @FXML
    private ComboBox<String> addCategoryCombo;
    @FXML
    private ComboBox<String> addStatusCombo;
    @FXML
    private TextField editNameField;
    @FXML
    private ComboBox<String> editStatusCombo;

    protected ObservableList<PoolTable> tableList = FXCollections.observableArrayList();
    protected ObservableList<PoolTable> availableTableList = FXCollections.observableArrayList();
    protected ObservableList<PoolTable> orderedTableList = FXCollections.observableArrayList();
    protected ObservableList<PoolTable> playingTableList = FXCollections.observableArrayList();
    protected PoolTableDAO poolTableDAO = new PoolTableDAO();
    protected ArrayList<CatePooltable> catePooltablesList;

    @FXML
    public void initialize() {
        catePooltablesList = new ArrayList<>();
        catePooltablesList.addAll(CatePooltableDAO.getAllCategories());

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

        // Initialize category list
        initializeCategoryList();

        // Add handler for new category button
        addNewTableCategory.setOnAction(e -> showAddCategoryDialog());
    }

    protected void filterTables(String searchText) {
        tablesContainer.getChildren().clear();
        if (searchText == null || searchText.isEmpty()) {
            tableList.forEach(this::createTableUI);
        } else {
            tableList.stream()
                    .filter(table -> table.getName().toLowerCase().contains(searchText.toLowerCase()))
                    .forEach(this::createTableUI);
        }
    }

    protected void createTableUI(PoolTable table) {
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
        updateButton.setOnAction(e -> showUpdateDialog(table));

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

    protected void showInfoDialog(PoolTable table) {
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

    private void updateStatusLists() {
        // Clear all status lists
        availableTableList.clear();
        orderedTableList.clear();
        playingTableList.clear();

        // Filter tables by status
        tableList.forEach(table -> {
            switch (table.getStatus()) {
                case "Available":
                    availableTableList.add(table);
                    break;
                case "Ordered":
                    orderedTableList.add(table);
                    break;
                case "Playing":
                    playingTableList.add(table);
                    break;
            }
        });


        VBox fullContent = new VBox(5);
        
        // Available
        VBox availableContent = new VBox(5);
        Label availableText = new Label("Available Tables : " + availableTableList.size());
        availableText.setStyle("-fx-font-weight: bold; -fx-font-size: 12.5;"); // Make bold, set font size
        availableText.setPadding(new Insets(0, 0, 0, 5)); // Apply left padding
        availableContent.getChildren().add(availableText);

        catePooltablesList.forEach(cate -> {
            Label text = new Label(cate.getName() + " : " + availableTableList.stream()
                    .filter(i -> i.getCatePooltableName().equalsIgnoreCase(cate.getName()))
                    .count());
            text.setStyle("-fx-font-size: 12.5;"); // Set font size
            text.setPadding(new Insets(0, 0, 0, 20)); // Apply left padding
            availableContent.getChildren().add(text);
        });
        availableContent.getChildren().add(new Region()); // Empty region as spacer

        // Ordered
        VBox orderedContent = new VBox(5);
        Label orderedText = new Label("Ordered Tables : " + orderedTableList.size());
        orderedText.setStyle("-fx-font-weight: bold; -fx-font-size: 12.5;"); // Make bold, set font size
        orderedText.setPadding(new Insets(0, 0, 0, 10)); // Apply left padding
        orderedContent.getChildren().add(orderedText);

        catePooltablesList.forEach(cate -> {
            Label text = new Label(cate.getName() + " : " + orderedTableList.stream()
                    .filter(i -> i.getCatePooltableName().equalsIgnoreCase(cate.getName()))
                    .count());
            text.setStyle("-fx-font-size: 12.5;"); // Set font size
            text.setPadding(new Insets(0, 0, 0, 20)); // Apply left padding
            orderedContent.getChildren().add(text);
        });
        orderedContent.getChildren().add(new Region()); // Empty region as spacer

        // Playing
        VBox playingContent = new VBox(5);
        Label playingText = new Label("Playing Tables : " + playingTableList.size());
        playingText.setStyle("-fx-font-weight: bold; -fx-font-size: 12.5;"); // Make bold, set font size
        playingText.setPadding(new Insets(0, 0, 0, 10)); // Apply left padding
        playingContent.getChildren().add(playingText);

        catePooltablesList.forEach(cate -> {
            Label text = new Label(cate.getName() + " : " + playingTableList.stream()
                    .filter(i -> i.getCatePooltableName().equalsIgnoreCase(cate.getName()))
                    .count());
            text.setStyle("-fx-font-size: 12.5;"); // Set font size
            text.setPadding(new Insets(0, 0, 0, 20)); // Apply left padding
            playingContent.getChildren().add(text);
        });
        playingContent.getChildren().add(new Region()); // Empty region as spacer

        // Add all sections to fullContent
        fullContent.getChildren().addAll(availableContent, orderedContent, playingContent);
        availableTableScrollPane.setContent(fullContent);
    }

    private void initializeCategoryList() {
        VBox categoryContent = new VBox(5);
        categoryContent.setPadding(new Insets(10));

        catePooltablesList.forEach(category -> {
            HBox categoryRow = new HBox(10);
            categoryRow.setAlignment(Pos.CENTER_LEFT);

            Text categoryText = new Text(category.getName() + " - " + String.format("%.2f", category.getPrice()));
            categoryText.setStyle("-fx-line-spacing: 5;"); // Increase line height (spacing)

// Create the edit button
            Button editButton = new Button();
            editButton.getStyleClass().add("edit-button");

// Set button size to 25x25
            editButton.setMinSize(25, 25); // Minimum size
            editButton.setMaxSize(25, 25); // Maximum size

// Add pencil icon from FontAwesome
            FontAwesomeIconView pencilIcon = new FontAwesomeIconView(FontAwesomeIcon.PENCIL);
            pencilIcon.prefWidth(15); // Adjust icon size as needed
            pencilIcon.prefHeight(15);

            editButton.setGraphic(pencilIcon);
            editButton.setGraphicTextGap(0); // Remove gap between icon and text if needed

// Center the icon within the button
            editButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

            editButton.setOnAction(e -> showUpdateCategoryDialog(category));

            categoryRow.getChildren().addAll(editButton, categoryText);
            categoryContent.getChildren().add(categoryRow);
        });
        catePooltablesScrollPane.setContent(categoryContent);
    }

    private void showAddCategoryDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/catepooltables/addCatePooltable.fxml"));
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Add New Category");

            Scene scene = new Scene(root);
            dialog.setScene(scene);
            dialog.showAndWait();

            // Refresh categories after dialog closes
            catePooltablesList.clear();
            catePooltablesList.addAll(CatePooltableDAO.getAllCategories());
            initializeCategoryList();
        } catch (IOException e) {
            e.printStackTrace();
            NotificationService.showNotification("Error", "Failed to open add category dialog: " + e.getMessage(), NotificationStatus.Error);
        }
    }

    private void showUpdateCategoryDialog(CatePooltable category) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/catepooltables/updateCatePooltable.fxml"));
            Parent root = loader.load();

            UpdateCategoryPooltableController controller = loader.getController();
            controller.setCatePooltable(category);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Update Category");

            Scene scene = new Scene(root);
            dialog.setScene(scene);
            dialog.showAndWait();

            // Refresh categories after dialog closes
            catePooltablesList.clear();
            catePooltablesList.addAll(CatePooltableDAO.getAllCategories());
            initializeCategoryList();
        } catch (IOException e) {
            e.printStackTrace();
            NotificationService.showNotification("Error", "Failed to open update category dialog: " + e.getMessage(), NotificationStatus.Error);
        }
    }

    @FXML
    protected void handleViewAllTables() {
        tableList.clear();
        tableList.addAll(poolTableDAO.getAllTables());

        // Clear and rebuild UI
        tablesContainer.getChildren().clear();
        tableList.forEach(this::createTableUI);

        // Update status lists
        updateStatusLists();
    }

    @FXML
    private void showAddDialog() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/pooltables/addPoolTable.fxml"));
        Parent root = loader.load();

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Add New Table");

        Scene scene = new Scene(root);
        dialog.setScene(scene);
        dialog.showAndWait();

        // Refresh the table list after dialog closes
        handleViewAllTables();
    }

    private void showUpdateDialog(PoolTable table) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/pooltables/editPoolTable.fxml"));
            Parent root = loader.load();

            UpdatePoolTableController controller = loader.getController();
            controller.setPoolTable(table);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Edit Table");

            Scene scene = new Scene(root);
            dialog.setScene(scene);
            dialog.showAndWait();

            // Refresh the table list after dialog closes
            handleViewAllTables();
        } catch (IOException e) {
            e.printStackTrace();
            NotificationService.showNotification("Error", "Failed to open update dialog: " + e.getMessage(), NotificationStatus.Error);
        }
    }
}