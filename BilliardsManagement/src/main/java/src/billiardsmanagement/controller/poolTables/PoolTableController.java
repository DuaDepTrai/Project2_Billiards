package src.billiardsmanagement.controller.poolTables;

import com.itextpdf.io.font.otf.FontReadingException;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import src.billiardsmanagement.controller.MainController;
import src.billiardsmanagement.controller.orders.ForEachOrderController;
import src.billiardsmanagement.controller.orders.OrderController;
import src.billiardsmanagement.controller.poolTables.catepooltables.UpdateCategoryPooltableController;
import src.billiardsmanagement.dao.*;
import src.billiardsmanagement.model.*;
import src.billiardsmanagement.service.NotificationService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PoolTableController {
    public MFXScrollPane availableTableScrollPane;
    public MFXScrollPane catePooltablesScrollPane;
    public Button addNewTableCategory;
    public ScrollPane poolTableScrollPane;
    private User currentUser; // Lưu user đang đăng nhập
    private List<String> userPermissions = new ArrayList<>();

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

    protected List<PoolTable> tableList;
    protected ObservableList<PoolTable> availableTableList = FXCollections.observableArrayList();
    protected ObservableList<PoolTable> orderedTableList = FXCollections.observableArrayList();
    protected ObservableList<PoolTable> playingTableList = FXCollections.observableArrayList();
    protected PoolTableDAO poolTableDAO = new PoolTableDAO();
    protected ArrayList<CatePooltable> catePooltablesList;
    protected OrderDAO orderDAO = new OrderDAO();

    private Popup poolPopup;
    private Popup oldPopup;
    private boolean isPoolPopupShowing = false;
    private OrderController orderController;
    private List<String> tableNameList;

    private FXMLLoader forEachViewLoader;
    private ForEachOrderController forEachOrderController;
    private Parent forEachRoot;

    private MainController mainController;
    private String poolTablePageChosen = "PoolTablePage";

    @FXML
    public void initialize() {
        System.out.println("Pool Table Controller's Initialize() method is called!");
        catePooltablesList = new ArrayList<>();
        catePooltablesList.addAll(CatePooltableDAO.getAllCategories());

        // Set search icon
        ImageView searchIcon = new ImageView(
                new Image(getClass().getResourceAsStream("/src/billiardsmanagement/images/pooltables/searchIcon.png")));
        searchIcon.setFitHeight(16);
        searchIcon.setFitWidth(16);
//        searchButton.setGraphic(searchIcon);

        // Load all tables
        handleViewAllTables();

        // Add search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue!=null) filterTables(searchField.getText());
        });

        // Initialize category list
        initializeCategoryList();

        // Add handler for new category button
        addNewTableCategory.setOnAction(e -> showAddCategoryDialog());

        tableList = poolTableDAO.getAllTables();
        tableNameList = this.tableList.stream()
                .map(PoolTable::getName)
                .collect(Collectors.toCollection(ArrayList::new));

        // Add handler for add new table button
//        addNewButton.setOnAction(e -> showAddDialog());
    }

    public void initializePoolTableController() {
        System.out.println("Pool Table Controller's Initialize() method is called!");
        catePooltablesList = new ArrayList<>();
        catePooltablesList.addAll(CatePooltableDAO.getAllCategories());

        // Set search icon
        ImageView searchIcon = new ImageView(
                new Image(getClass().getResourceAsStream("/src/billiardsmanagement/images/pooltables/searchIcon.png")));
        searchIcon.setFitHeight(16);
        searchIcon.setFitWidth(16);
//        searchButton.setGraphic(searchIcon);

        // Load all tables
        handleViewAllTables();

        // Add search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue!=null){
                filterTables(searchField.getText());
            }
        });

        // Initialize category list
        initializeCategoryList();

        // Add handler for new category button
        addNewTableCategory.setOnAction(e -> showAddCategoryDialog());

        tableList = poolTableDAO.getAllTables();
        tableNameList = this.tableList.stream()
                .map(PoolTable::getName)
                .collect(Collectors.toCollection(ArrayList::new));

        // Add handler for add new table button
//        addNewButton.setOnAction(e -> showAddDialog());
    }

    public void setUser(User user) throws SQLException {
        this.currentUser = user;
        if (user != null) {
            System.out.println("🟢 Gọi getUserPermissions() với user: " + user.getUsername());
            this.userPermissions = user.getPermissionsAsString();
            System.out.println("🔎 Debug: Quyền của user = " + userPermissions);

            initializeCategoryList();
        } else {
            System.err.println("❌ Lỗi: loggedInUser chưa được set trong ProductController2!");
        }
    }

    protected void filterTables(String searchText) {
        tablesContainer.getChildren().clear();

        if (searchText != null && !searchText.isEmpty()) {
            List<PoolTable> filteredList = new ArrayList<>(); // Using ArrayList instead of ObservableList

            for (PoolTable table : tableList) {
                if (table.getName().toLowerCase().contains(searchText.toLowerCase())) {
                    filteredList.add(table);
                }
            }
            if(filteredList.isEmpty()) System.out.println("Filter List empty.");
            if(tableList.isEmpty()) System.out.println("Table List empty.");

            if(!filteredList.isEmpty()) updateUIWithTables(filteredList);
        } else {
            // If no search text, show all tables
            updateUIWithTables(tableList);
        }
    }

    protected void createTableUI(PoolTable table) {
        StackPane tableStack = new StackPane();
        tableStack.setPrefSize(100, 135);
        tableStack.getStyleClass().add("table-stack");

        // Background image
        ImageView bgImage = new ImageView(
                new Image(getClass().getResourceAsStream("/src/billiardsmanagement/images/pooltables/Background.png")));
        bgImage.setFitHeight(135);
        bgImage.setFitWidth(100);

        // Create VBox for text elements
        VBox textContainer = new VBox(5);
        textContainer.setAlignment(Pos.CENTER);

        String tableNumberText = table.getName().replaceAll("\\D+", ""); // Extracts the digits

        Label tableShortNameLabel = new Label(table.getPoolTableShortName());
        tableShortNameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        Label tableNumberLabel = new Label(tableNumberText);
        tableNumberLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 30;");

        Label statusLabel = new Label(table.getStatus());
        statusLabel.setStyle("-fx-font-weight: bold; -fx-underline: true;"); // Set bold and underline

        // Set status color based on the table's status
        switch (table.getStatus()) {
            case "Available":
                statusLabel.setStyle(statusLabel.getStyle() + "-fx-text-fill: #04ff00; -fx-font-size: 16"); // Green for
                // Available
                break;
            case "Ordered":
                statusLabel.setStyle(statusLabel.getStyle() + "-fx-text-fill: #FFA500; -fx-font-size: 16"); // Orange
                // for
                // Ordered
                break;
            case "Playing":
                statusLabel.setStyle(statusLabel.getStyle() + "-fx-text-fill: #C21E00; -fx-font-size: 16"); // Red for
                // Playing
                break;
        }

        // Add the label to the container
        textContainer.getChildren().addAll(tableShortNameLabel, tableNumberLabel, statusLabel);

        // Create hover buttons container
        VBox hoverButtons = new VBox();
        hoverButtons.getStyleClass().add("hover-buttons");
        hoverButtons.setPrefSize(100, 135);
        hoverButtons.setAlignment(Pos.CENTER);
        hoverButtons.setSpacing(2);

        // Create Order button
        Button createOrderButton = new Button("Order Info");
        createOrderButton.getStyleClass().add("hover-button");
        createOrderButton.setMaxWidth(Double.MAX_VALUE);
        createOrderButton.setPrefHeight(60);
        createOrderButton.setWrapText(true);
        createOrderButton.setStyle("-fx-font-size: 12px;");
        createOrderButton.setOnAction(e -> {
            if (table.getStatus().equalsIgnoreCase("Available")) {
                try {
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/src/billiardsmanagement/pooltables/poolTableOrderInformation.fxml"));
                    Parent root = loader.load();

                    PoolTableOrderInformationController controller = loader.getController();
                    controller.setPoolTable(table);
                    controller.setPoolTableController(this);
                    controller.setTablesContainer(tableStack);
                    controller.setOrderController(this.orderController);
                    controller.initializeView();

                    showPoolPopup(tableStack, root);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                int orderId = BookingDAO.getTheLatestOrderByTableId(table.getTableId());
                Booking latestBooking = BookingDAO.getBookingByTableIdAndOrderId(orderId, table.getTableId());
                System.out.println("Latest Booking : " + latestBooking);
                System.out.println("Order ID = " + orderId);
                System.out.println("Status : " + latestBooking.getBookingStatus());
                if (latestBooking.getBookingStatus() != null && (latestBooking.getBookingStatus().equalsIgnoreCase("Playing") || latestBooking.getBookingStatus().equalsIgnoreCase("Order"))) {
                    Order currentOrder = OrderDAO.getOrderById(orderId);
                    showForEachOrderView(currentOrder, table);
                } else {
                    NotificationService.showNotification("Error",
                            "This table is not booked yet, so that there's no Order associated with this table.",
                            NotificationStatus.Error);
                }
            }
        });

        // Info button
        Button infoButton = new Button("Pool Info");
        infoButton.getStyleClass().add("hover-button");
        infoButton.setMaxWidth(Double.MAX_VALUE);
        infoButton.setPrefHeight(60);
        infoButton.setWrapText(true);
        infoButton.setStyle("-fx-font-size: 12px;");
//        infoButton.setOnAction(e -> openTableInfoDialog(table));
        infoButton.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/src/billiardsmanagement/pooltables/poolTableInfo.fxml"));
                Parent root = loader.load();

                // Get the controller and set up the table info
                PoolTableInfoController controller = loader.getController();
                controller.setPoolTableController(this);
                // setCatePooltableComboBox must be called before setPoolTable() !
                controller.setCatePooltableComboBox(catePooltablesList);
                controller.setTableNameList(tableNameList);
                controller.setPoolTable(table);
                controller.initializePoolInfo();

                showPoolPopup(tableStack, root);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        hoverButtons.getChildren().addAll(createOrderButton, infoButton);

        // Add elements to stack
        tableStack.getChildren().addAll(bgImage, textContainer, hoverButtons);

        // Add to container with reduced margin
        tablesContainer.getChildren().add(tableStack);
        FlowPane.setMargin(tableStack, new Insets(5));
    }

    public void showPoolPopup(Parent container, Parent content) {
        Platform.runLater(() -> {
            this.poolPopup = new Popup();
            StackPane contentPane = new StackPane();
            contentPane.setStyle("-fx-background-color: white; -fx-padding: 10;");

            contentPane.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, null, null)));
            contentPane.getChildren().add(content);

            poolPopup.getContent().add(contentPane);

            double xPos = container.localToScene(0, 0).getX();
            double yPos = container.localToScene(0, 0).getY();

            poolPopup.setX(xPos);
            poolPopup.setY(yPos);

            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.12), contentPane);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            poolPopup.setAutoHide(true);
            poolPopup.setAutoFix(true);
            poolPopup.show(tablesContainer.getScene().getWindow());

            fadeIn.play();
        });
    }

    public void showPoolPopupInTheMiddle(Parent container, Parent content) {
        Platform.runLater(() -> {
            this.poolPopup = new Popup();
            StackPane contentPane = new StackPane();
            contentPane.setStyle("-fx-background-color: white; -fx-padding: 10;");
            contentPane.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, null, null)));
            contentPane.getChildren().add(content);

            // Set initial opacity to 0
            contentPane.setOpacity(0);

            poolPopup.getContent().add(contentPane);

            // Show the popup first to ensure dimensions are calculated correctly
            poolPopup.show(container.getScene().getWindow());

            // Calculate the center position of the container
            double centerX = container.getScene().getWindow().getX() + (container.getScene().getWindow().getWidth() / 2);
            double centerY = container.getScene().getWindow().getY() + (container.getScene().getWindow().getHeight() / 2);

            // Position the popup in the center of the container
            poolPopup.setAnchorX(centerX - (contentPane.getWidth() / 2));
            poolPopup.setAnchorY(centerY - (contentPane.getHeight() / 2));

            // Fade-in effect
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.25), contentPane);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            // Play the fade-in transition after showing the popup
            fadeIn.play();

            poolPopup.setAutoHide(true);
            poolPopup.setAutoFix(true);
        });
    }

    public void hidePoolPopup() {
        if (poolPopup != null && poolPopup.isShowing()) {
            poolPopup.hide();
        }
//        if (poolPopup != null && poolPopup.isShowing()) {
//            oldPopup = poolPopup; // Keep reference to the current popup
//
//            if (!oldPopup.getContent().isEmpty()) { // Ensure it has content
//                Node popupContent = oldPopup.getContent().get(0);
//
//                // Apply fade-out effect on the popup content
//                FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.07), popupContent);
//                fadeOut.setFromValue(1.0);
//                fadeOut.setToValue(0.0);
//                poolPopup.hide();
//                fadeOut.setOnFinished(event -> {
//                    oldPopup.hide(); // Hide popup after fading out
//                    System.out.println("Popup Hidden with Fade Out!");
//                });
//
//                fadeOut.play();
//            } else {
//                oldPopup.hide(); // Directly hide if no content
//            }
//        } else {
//            System.out.println("poolPopup is null, not showing, or not initialized.");
//        }
    }

    private void showForEachOrderView(Order order, PoolTable currentTable) {
        try {
            forEachViewLoader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/orders/forEachOrder.fxml"));
            forEachRoot = forEachViewLoader.load();

            forEachOrderController = forEachViewLoader.getController();

            forEachOrderController.setOrderID(order.getOrderId());
            forEachOrderController.setCustomerID(order.getCustomerId());
            forEachOrderController.setForEachUserID(order.getUserId());
            forEachOrderController.setOrderDate(order.getOrderDate());
            forEachOrderController.setOrderController(this.orderController);
            forEachOrderController.setMainController(this.mainController, poolTablePageChosen);
            int billNo = OrderDAO.getOrderBillNo(order.getOrderId());
            if (billNo != -1) forEachOrderController.setBillNo(billNo);
            forEachOrderController.setPoolTableController(this);
            if (order.getCustomerPhone() != null) {
                forEachOrderController.setInitialPhoneText(order.getCustomerPhone());
            }
            forEachOrderController.initializeAllTables();

            if(mainController!=null){
                StackPane contentArea = mainController.getContentArea();
                contentArea.getChildren().clear();
                contentArea.getChildren().add(forEachRoot);
            }

            handleViewAllTables();
        } catch (Exception e) {
            e.printStackTrace();
            NotificationService.showNotification("Error",
                    "Failed to open order information: " + e.getMessage(),
                    NotificationStatus.Error);
        }
    }

    protected void createOrderForTable(PoolTable table) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/src/billiardsmanagement/orders/forEachOrder.fxml"));
            Parent root = loader.load();

            // Get the controller
            ForEachOrderController controller = loader.getController();

            // Create a new order first
            UserSession currentStaff = UserSession.getInstance();
            Order newOrder = new Order();
            newOrder.setCustomerId(1); // Default customer ID
            newOrder.setUserId(currentStaff.getUserId());
            newOrder.setOrderStatus("Playing");
            orderDAO.addOrder(newOrder);

            // Table to Playing
            table.setStatus("Playing");
            poolTableDAO.updateTable(table);
            System.out.println("Table = " + table.getName() + ", infor : " + table);

            // Get the newest Order
            Order latestOrder = OrderDAO.getTheLatestOrderCreated();

            // Initialize the controller with order details and selected table
            controller.setOrderID(latestOrder.getOrderId());
            controller.setCustomerID(latestOrder.getCustomerId());
            controller.setForEachUserID(latestOrder.getUserId());
            controller.setSelectedTable(table);
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

            // Firstly, find [orderID]
            int orderId = BookingDAO.getTheLatestOrderByTableId(table.getTableId());
            if (orderId == -1)
                throw new Exception(
                        "This Table has not been booked yet, so that there's no Order associated with this table.");
            // Secondly, use the orderId found above to find [userId] associate with this
            // [Order]
            Pair<Integer, Integer> pair = OrderDAO.getUserIdByOrderId(orderId);
            if (pair.getFirstValue() == null || pair.getSecondValue() == null)
                throw new Exception(
                        "No User / Staff found for this Order. There might be an unexpected error. Please try again.");

            ForEachOrderController controller = loader.getController();
            controller.setOrderID(orderId);
            controller.setForEachUserID(pair.getFirstValue());
            controller.setCustomerID(pair.getSecondValue());
            controller.initializeAllTables();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Order Details");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Refresh tables after order creation
            handleViewAllTables();
        } catch (Exception e) {
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
        content.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 5;");

        // Create header with total count and apply status-specific colors
        Label headerLabel = new Label(title + " : " + tableList.size());
        String statusColor;
        if (title.contains("Available")) {
            statusColor = "-fx-text-fill: #00c853;"; // Green for available
        } else if (title.contains("Ordered")) {
            statusColor = "-fx-text-fill: #ff9800;"; // Orange for order
        } else {
            statusColor = "-fx-text-fill: #ff0000;"; // Red for playing
        }
        headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;" + statusColor);
        headerLabel.setPadding(new Insets(0, 0, 5, 5));
        content.getChildren().add(headerLabel);

        // Add counts for each table category
        for (CatePooltable category : catePooltablesList) {
            long categoryCount = tableList.stream()
                    .filter(table -> table.getCatePooltableName().equals(category.getName()))
                    .count();

            Label categoryLabel = new Label(String.format("• %s : %d", category.getShortName(), categoryCount));
            categoryLabel.setStyle("-fx-font-size: 12;");
            categoryLabel.setPadding(new Insets(0, 0, 0, 7));
            content.getChildren().add(categoryLabel);
        }

        // Add separator
        Separator separator = new Separator();
        separator.setPadding(new Insets(3, 0, 3, 0));
        content.getChildren().add(separator);

        return content;
    }

    private void initializeCategoryList() {
        VBox categoryContent = new VBox(5);
        categoryContent.setPadding(new Insets(10));

        catePooltablesList.forEach(category -> {
            HBox categoryRow = new HBox(10);
            categoryRow.setAlignment(Pos.CENTER_LEFT);

            Text categoryText = new Text(String.format("%s - %.0f",
                    category.getShortName(),
                    category.getPrice()));
            categoryText.setStyle("-fx-line-spacing: 5;");

            // Tạo khoảng trống để đẩy nút về bên phải
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

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

            categoryRow.getChildren().addAll(categoryText, spacer, editButton, deleteButton);
            categoryContent.getChildren().add(categoryRow);

            System.out.println("DEBUG: " + userPermissions);
            editButton.setVisible(userPermissions.contains("update_pool_category"));
            deleteButton.setVisible(userPermissions.contains("remove_pool_category"));
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
            handleViewAllTables();
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
            handleViewAllTables();
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
                handleViewAllTables();
            } catch (Exception e) {
                e.printStackTrace();
                NotificationService.showNotification("Error", "Failed to delete category: " + e.getMessage(),
                        NotificationStatus.Error);
            }
        }
    }

    @FXML
    public void handleViewAllTables() {
        // Create a task for fetching tables
        Task<List<PoolTable>> fetchTablesTask = new Task<>() {
            @Override
            protected List<PoolTable> call() throws Exception {
                // Simulate time-consuming data fetching operation
                return poolTableDAO.getAllTables(); // Fetch tables from DAO
            }

            @Override
            protected void succeeded() {
                // This method runs on the JavaFX Application Thread
                tableList = getValue(); // Get the result from call()
                updateUIWithTables(tableList);
            }

            @Override
            protected void failed() {
                // Handle any exceptions that occurred during the task
                Throwable exception = getException();
                exception.printStackTrace();
                NotificationService.showNotification("Error", "Failed to load tables.", NotificationStatus.Error);
            }
        };

        // Run the task in a new thread
        new Thread(fetchTablesTask).start();
    }

    private void updateUIWithTables(List<PoolTable> tableList) {
        tablesContainer.getChildren().clear();
        tableList.forEach(this::createTableUI);

        poolTableScrollPane.setContent(tablesContainer);
        poolTableScrollPane.setFitToWidth(true);

        // Update status lists
        updateStatusLists();
    }

//    private void loadOneTable(PoolTable table) {
//        Node currentTable = findTableNodeById(tablesContainer, table);
//        if(currentTable!=null){
//            int index = tablesContainer.getChildren().indexOf(currentTable);
//            tablesContainer.getChildren().remove(currentTable);
//            PoolTable tableUpdated = PoolTableDAO.getSpecificTable(table.getTableId());
//            tablesContainer.getChildren().add(index, tableUpdated);
//        }
//    }
//
//    private Node findTableNodeById(FlowPane flowPane, PoolTable table) {
//        for (Node node : flowPane.getChildren()) {
//            if (node.getUserData() != null && node.getUserData().equals(table)) {
//                return node; // Return the node if it matches the table ID
//            }
//        }
//        return null; // Return null if no matching node is found
//    }

    @FXML
    public void showAddDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/src/billiardsmanagement/pooltables/addPoolTable.fxml"));
            Parent root = loader.load();

            AddPoolTableController controller = loader.getController();

            controller.setTableNameList(tableNameList);
            controller.setPoolController(this);
            controller.initializeAddPoolTable();

            showPoolPopupInTheMiddle(poolTableScrollPane, root);
            
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

    private void showPoolTableOrderInformation(PoolTable table) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/src/billiardsmanagement/pooltables/poolTableOrderInformation.fxml"));
            Parent root = loader.load();

            PoolTableOrderInformationController controller = loader.getController();
            controller.setPoolTable(table);
            controller.setPoolTableController(this);
            controller.initializeView();

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Table Order Information");

            Scene scene = new Scene(root);
            dialog.setScene(scene);
            dialog.showAndWait();

            // Refresh the table list after dialog closes
            handleViewAllTables();
        } catch (IOException e) {
            e.printStackTrace();
            NotificationService.showNotification("Error",
                    "Failed to open order information dialog: " + e.getMessage(),
                    NotificationStatus.Error);
        }
    }

    private void createTableCard(PoolTable table, FlowPane tablesContainer) {
        StackPane tableStack = new StackPane();
        tableStack.getStyleClass().add("table-card");
        tableStack.setPrefSize(150, 150);

        // Background image
//        ImageView bgImage = new ImageView(
//                new Image(getClass().getResourceAsStream("/src/billiardsmanagement/images/pool_table.png")));
//        bgImage.setFitWidth(150);
//        bgImage.setFitHeight(150);

        VBox background = new VBox();
        background.setPrefSize(150, 150);
        background.setStyle("-fx-background-color: #2A2A2A; -fx-background-radius: 5;");

        // Text container
        VBox textContainer = new VBox(5);
        textContainer.setAlignment(Pos.CENTER);
        Label nameLabel = new Label(table.getName());
        Label statusLabel = new Label(table.getStatus());
        textContainer.getChildren().addAll(nameLabel, statusLabel);

        // Hover buttons container
        VBox hoverButtons = new VBox(5);
        hoverButtons.setAlignment(Pos.CENTER);
        hoverButtons.getStyleClass().add("hover-buttons");
        hoverButtons.setVisible(false);

        // Create Order button
        Button createOrderButton = new Button("Create Order");
        createOrderButton.getStyleClass().add("hover-button");
        createOrderButton.setMaxWidth(Double.MAX_VALUE);
        createOrderButton.setPrefHeight(60);
        createOrderButton.setWrapText(true);
        createOrderButton.setStyle("-fx-font-size: 12px;");
        createOrderButton.setOnAction(e -> createOrderForTable(table));

        // Info button
        Button infoButton = new Button("Info");
        infoButton.getStyleClass().add("hover-button");
        infoButton.setMaxWidth(Double.MAX_VALUE);
        infoButton.setPrefHeight(60);
        infoButton.setWrapText(true);
        infoButton.setStyle("-fx-font-size: 12px;");
        infoButton.setOnAction(e -> showTableInfo(table));

        hoverButtons.getChildren().addAll(createOrderButton, infoButton);

        // Mouse hover effect
        tableStack.setOnMouseEntered(e -> hoverButtons.setVisible(true));
        tableStack.setOnMouseExited(e -> hoverButtons.setVisible(false));

        // Add elements to stack
        tableStack.getChildren().addAll(textContainer, hoverButtons);

        // Add to container with reduced margin
        tablesContainer.getChildren().add(tableStack);
        FlowPane.setMargin(tableStack, new Insets(5));
    }

    @FXML
    public void showTableInfo(PoolTable table) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/src/billiardsmanagement/poolTables/poolTableInfo.fxml"));
            Parent root = loader.load();

            PoolTableInfoController controller = loader.getController();
            controller.setPoolTableController(this);
            controller.setPoolTable(table);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Pool Table Information");
            stage.setScene(new Scene(root));

            // Add event handler for when dialog closes
            stage.setOnHidden(e -> {
                // Refresh the pool tables view
                handleViewAllTables();
            });

            stage.showAndWait();
        } catch (IOException e) {
            NotificationService.showNotification("Error", "Failed to open table information: " + e.getMessage(),
                    NotificationStatus.Error);
        }
    }

    @FXML
    private void openTableInfoDialog(PoolTable table) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/src/billiardsmanagement/pooltables/poolTableInfo.fxml"));
            Parent root = loader.load();

            // Get the controller and set up the table info
            PoolTableInfoController controller = loader.getController();
            controller.setPoolTableController(this);
            controller.setPoolTable(table);

            Stage stage = new Stage();
            stage.setTitle("Pool Table Information");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            NotificationService.showNotification("Error", "Could not open table info: " + e.getMessage(),
                    NotificationStatus.Error);
        }
    }

    // Remove duplicate methods
    @FXML
    private void showTableInfo(ActionEvent event) {
        Button button = (Button) event.getSource();
        PoolTable table = (PoolTable) button.getUserData();
        openTableInfoDialog(table);
    }

    private void applyPermissions() {
        if (currentUser != null) {
            PermissionDAO permissionDAO = new PermissionDAO();
            List<String> permissions = permissionDAO.getUserPermissions(currentUser.getId());
            System.out.println("✅ Permissions: " + permissions);

            addNewButton.setVisible(permissions.contains("add_pool"));
            addNewTableCategory.setVisible(permissions.contains("add_pool_category"));
//            addProductButton.setVisible(permissions.contains("add_product"));
//            editButton.setVisible(permissions.contains("add_product"));
//            deleteButton.setVisible(permissions.contains("add_product"));
//            stockUpButton.setVisible(permissions.contains("add_product"));
//            btnAddNewCategory.setVisible(permissions.contains("add_product"));
//            updateCategoryButton.setVisible(permissions.contains("add_product"));
//            removeCategoryButton.setVisible(permissions.contains("add_product"));
        } else {
            System.err.println("⚠️ Lỗi: currentUser bị null trong ProductController!");
        }
    }

    private User loggedInUser;

    public void setLoggedInUser(User user) {
        System.out.println("🟢 Gọi setCurrentUser() với user: " + (user != null ? user.getUsername() : "null"));

        this.loggedInUser = user;
        if (user != null) {
            System.out.println("🟢 Gọi setCurrentUser() với user: " + user.getUsername());
            System.out.println("🎯 Kiểm tra quyền sau khi truyền user...");
            List<String> permissions = user.getPermissionsAsString();
            System.out.println("🔎 Debug: Quyền sau khi truyền user = " + permissions);
            applyPermissions();
        } else {
            System.err.println("❌ Lỗi: currentUser vẫn null sau khi set!");
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public Popup getCurrentPoolPopup() {
        return poolPopup;
    }

    public void setPoolPopupShowing(boolean isPoolPopupShowing) {
        this.isPoolPopupShowing = isPoolPopupShowing;
    }

    public boolean isPoolPopupShowing() {
        return this.isPoolPopupShowing;
    }

    public void setOrderController(OrderController orderController) {
        this.orderController = orderController;
    }

    public OrderController getOrderController() {
        return this.orderController;
    }

    // Used after every operation executed on PoolTable
    public void resetTableListAndTableNameList(){
        tableList = poolTableDAO.getAllTables();
        tableNameList = this.tableList.stream()
                .map(PoolTable::getName)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    // Used after every operation executed on CatePooltable
    public void resetCatePooltableList(){
        catePooltablesList.clear();
        catePooltablesList.addAll(CatePooltableDAO.getAllCategories());
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
}