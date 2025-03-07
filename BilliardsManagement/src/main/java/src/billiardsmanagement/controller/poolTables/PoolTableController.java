package src.billiardsmanagement.controller.poolTables;

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
import src.billiardsmanagement.controller.poolTables.catepooltables.UpdateCategoryPooltableController;
import src.billiardsmanagement.controller.orders.OrderController;
import src.billiardsmanagement.controller.orders.ForEachOrderController;
import src.billiardsmanagement.model.Order;
import src.billiardsmanagement.dao.OrderDAO;
import javafx.scene.control.Separator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

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
    protected OrderDAO orderDAO = new OrderDAO();

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

        // Add handler for add new table button
        addNewButton.setOnAction(e -> showAddDialog());
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
        tableStack.setPrefSize(90, 125);
        tableStack.getStyleClass().add("table-stack");

        // Background image
        ImageView bgImage = new ImageView(
                new Image(getClass().getResourceAsStream("/src/billiardsmanagement/images/pooltables/Background.png")));
        bgImage.setFitHeight(125);
        bgImage.setFitWidth(90);

        // Create VBox for text elements
        VBox textContainer = new VBox(5);
        textContainer.setAlignment(Pos.CENTER);

        // Table name text
        Text nameText = new Text(table.getName());
        nameText.setStyle("-fx-font-weight: bold;");
        nameText.setUnderline(true);
        nameText.setFill(Color.valueOf("#c21e00"));

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

        textContainer.getChildren().addAll(nameText, statusText);

        // Create hover buttons container
        VBox hoverButtons = new VBox();
        hoverButtons.getStyleClass().add("hover-buttons");
        hoverButtons.setPrefSize(90, 125);
        hoverButtons.setAlignment(Pos.CENTER);
        hoverButtons.setSpacing(2);

        // Create Order button
        Button createOrderButton = new Button("Create Order");
        createOrderButton.getStyleClass().add("hover-button");
        createOrderButton.setMaxWidth(Double.MAX_VALUE);
        createOrderButton.setPrefHeight(60);
        createOrderButton.setOnAction(e -> createOrderForTable(table));

        // Order button
        Button orderButton = new Button("Order");
        orderButton.getStyleClass().add("hover-button");
        orderButton.setMaxWidth(Double.MAX_VALUE);
        orderButton.setPrefHeight(60);
        orderButton.setOnAction(e -> showOrderDialog(table));

        hoverButtons.getChildren().addAll(createOrderButton, orderButton);

        // Add elements to stack
        tableStack.getChildren().addAll(bgImage, textContainer, hoverButtons);

        // Add to container with reduced margin
        tablesContainer.getChildren().add(tableStack);
        FlowPane.setMargin(tableStack, new Insets(5));
    }

    protected void createOrderForTable(PoolTable table) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/src/billiardsmanagement/orders/forEachOrder.fxml"));
            Parent root = loader.load();

            // Get the controller
            ForEachOrderController controller = loader.getController();

            // Create a new order first
            Order newOrder = new Order();
            newOrder.setCustomerId(1); // Default customer ID
            orderDAO.addOrder(newOrder);

            // Get the latest order
            Order latestOrder = orderDAO.getLatestOrderByCustomerId(1);

            // Update table status
            table.setStatus("Ordered");
            poolTableDAO.updateTable(table);

            // Initialize the controller with order details
            controller.setOrderID(latestOrder.getOrderId());
            controller.setCustomerID(1);
            controller.initializeAllTables();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Create Order");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Refresh tables after order creation
            handleViewAllTables();
        } catch (IOException e) {
            e.printStackTrace();
            NotificationService.showNotification("Error", "Failed to open order creation dialog: " + e.getMessage(),
                    NotificationStatus.Error);
        }
    }

    private void showOrderDialog(PoolTable table) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/src/billiardsmanagement/orders/forEachOrder.fxml"));
            Parent root = loader.load();

            ForEachOrderController controller = loader.getController();

            // Initialize the controller with existing order for this table
            // You'll need to implement the logic to find the existing order for this table
            // For now, we'll create a new order
            Order newOrder = new Order();
            newOrder.setCustomerId(1);
            orderDAO.addOrder(newOrder);

            Order latestOrder = orderDAO.getLatestOrderByCustomerId(1);
            controller.setOrderID(latestOrder.getOrderId());
            controller.setCustomerID(1);
            controller.initializeAllTables();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Order Details");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Refresh tables after order creation
            handleViewAllTables();
        } catch (IOException e) {
            e.printStackTrace();
            NotificationService.showNotification("Error", "Failed to open order dialog: " + e.getMessage(),
                    NotificationStatus.Error);
        }
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

        VBox fullContent = new VBox(10); // Increased spacing between sections
        fullContent.setPadding(new Insets(10)); // Add padding to the container

        // Available Tables Section
        VBox availableContent = createStatusSection(
            "Available Tables", 
            availableTableList, 
            "-fx-text-fill: #04ff00;" // Green color for available
        );

        // Ordered Tables Section
        VBox orderedContent = createStatusSection(
            "Ordered Tables", 
            orderedTableList,
            "-fx-text-fill: #FFA500;" // Orange color for ordered
        );

        // Playing Tables Section
        VBox playingContent = createStatusSection(
            "Playing Tables", 
            playingTableList,
            "-fx-text-fill: #c21e00;" // Red color for playing
        );

        // Add all sections to fullContent
        fullContent.getChildren().addAll(availableContent, orderedContent, playingContent);
        availableTableScrollPane.setContent(fullContent);
    }

    // Helper method to create a status section
    private VBox createStatusSection(String title, ObservableList<PoolTable> tableList, String textColor) {
        VBox content = new VBox(5);
        content.setPadding(new Insets(5));
        content.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 5;"); // Light gray background with rounded corners

        // Create header with total count
        Label headerLabel = new Label(title + " : " + tableList.size());
        headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;" + textColor);
        headerLabel.setPadding(new Insets(0, 0, 5, 5));
        content.getChildren().add(headerLabel);

        // Add counts for each table category
        for (CatePooltable category : catePooltablesList) {
            long categoryCount = tableList.stream()
                    .filter(table -> table.getCatePooltableName().equals(category.getName()))
                    .count();
            
            Label categoryLabel = new Label(String.format("• %s : %d", category.getName(), categoryCount));
            categoryLabel.setStyle("-fx-font-size: 12;");
            categoryLabel.setPadding(new Insets(0, 0, 0, 15));
            content.getChildren().add(categoryLabel);
        }

        // Add separator
        Separator separator = new Separator();
        separator.setPadding(new Insets(5, 0, 5, 0));
        content.getChildren().add(separator);

        return content;
    }

    private void initializeCategoryList() {
        VBox categoryContent = new VBox(5);
        categoryContent.setPadding(new Insets(10));

        catePooltablesList.forEach(category -> {
            HBox categoryRow = new HBox(10);
            categoryRow.setAlignment(Pos.CENTER_LEFT);

            Text categoryText = new Text(String.format("%s (%s) - %.2f", 
                category.getName(), 
                category.getShortName(), 
                category.getPrice()));
            categoryText.setStyle("-fx-line-spacing: 5;");

            // Create the edit button
            Button editButton = new Button();
            editButton.getStyleClass().add("edit-button");

            // Set button size to 24x24
            editButton.setMinSize(24, 24); // Minimum size
            editButton.setMaxSize(24, 24); // Maximum size

            // Add pencil icon from FontAwesome
            FontAwesomeIconView pencilIcon = new FontAwesomeIconView(FontAwesomeIcon.PENCIL);
            pencilIcon.prefWidth(14); // Adjust icon size
            pencilIcon.prefHeight(14); // Adjust icon size

            editButton.setGraphic(pencilIcon);
            editButton.setGraphicTextGap(0); // Remove gap between icon and text if needed
            editButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

            editButton.setOnAction(e -> showUpdateCategoryDialog(category));

            // Create the delete button
            Button deleteButton = new Button();
            deleteButton.getStyleClass().add("delete-button");

            // Set button size to 24x24
            deleteButton.setMinSize(24, 24); // Minimum size
            deleteButton.setMaxSize(24, 24); // Maximum size

            // Add trash icon from FontAwesome
            FontAwesomeIconView trashIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
            trashIcon.prefWidth(14); // Adjust icon size
            trashIcon.prefHeight(14); // Adjust icon size

            deleteButton.setGraphic(trashIcon);
            deleteButton.setGraphicTextGap(0); // Remove gap between icon and text if needed
            deleteButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

            deleteButton.setOnAction(e -> showDeleteCategoryDialog(category)); // Assuming you have this method

            categoryRow.getChildren().addAll(editButton, deleteButton, categoryText);
            categoryContent.getChildren().add(categoryRow);
        });
        catePooltablesScrollPane.setContent(categoryContent);
    }

    private void showAddCategoryDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/src/billiardsmanagement/catepooltables/addCatePooltable.fxml"));
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
            NotificationService.showNotification("Error", "Failed to open add category dialog: " + e.getMessage(),
                    NotificationStatus.Error);
        }
    }

    private void showUpdateCategoryDialog(CatePooltable category) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/src/billiardsmanagement/catepooltables/updateCatePooltable.fxml"));
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
            NotificationService.showNotification("Error", "Failed to open update category dialog: " + e.getMessage(),
                    NotificationStatus.Error);
        }
    }

    private void showDeleteCategoryDialog(CatePooltable category) {
        // Create a confirmation alert
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Category");
        alert.setHeaderText("Are you sure you want to delete this category?");
        alert.setContentText("Category: " + category.getName());

        // Set button types for the alert
        ButtonType deleteButtonType = new ButtonType("Delete");
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(deleteButtonType, cancelButtonType);

        // Show the alert and wait for a response
        Optional<ButtonType> result = alert.showAndWait();

        // Check if the user clicked the delete button
        if (result.isPresent() && result.get() == deleteButtonType) {
            try {
                // Call the DAO to delete the category
                CatePooltableDAO.deleteCategory(category.getId());

                // Refresh the category list after deletion
                catePooltablesList.clear();
                catePooltablesList.addAll(CatePooltableDAO.getAllCategories());
                initializeCategoryList();
            } catch (Exception e) {
                e.printStackTrace();
                NotificationService.showNotification("Error", "Failed to delete category: " + e.getMessage(),
                        NotificationStatus.Error);
            }
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
    public void showAddDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/src/billiardsmanagement/pooltables/addPoolTable.fxml"));
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Add New Table");

            Scene scene = new Scene(root);
            dialog.setScene(scene);
            dialog.showAndWait();

            // Refresh the table list after dialog closes
            handleViewAllTables();
        } catch (IOException e) {
            e.printStackTrace();
            NotificationService.showNotification("Error", "Failed to open add table dialog: " + e.getMessage(),
                    NotificationStatus.Error);
        }
    }

    private void showUpdateDialog(PoolTable table) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/src/billiardsmanagement/pooltables/editPoolTable.fxml"));
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
            NotificationService.showNotification("Error", "Failed to open update dialog: " + e.getMessage(),
                    NotificationStatus.Error);
        }
    }
}