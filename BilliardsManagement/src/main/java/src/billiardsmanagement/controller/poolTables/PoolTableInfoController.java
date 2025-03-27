package src.billiardsmanagement.controller.poolTables;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.textfield.TextFields;
import src.billiardsmanagement.dao.PoolTableDAO;
import src.billiardsmanagement.dao.CatePooltableDAO;
import src.billiardsmanagement.model.PoolTable;
import src.billiardsmanagement.model.CatePooltable;
import src.billiardsmanagement.service.NotificationService;
import src.billiardsmanagement.model.NotificationStatus;

import java.util.List;
import java.util.Optional;

public class PoolTableInfoController {
    @FXML
    private Label notifyLabel;

    @FXML
    private TextField nameField;

    @FXML
    private ComboBox<CatePooltable> categoryComboBox;

    @FXML
    private Button updateButton;

    @FXML
    private Button removeButton;

    private PoolTable currentTable;
    private PoolTableDAO poolTableDAO;
    private PoolTableController poolTableController;
    private List<String> currentTableNameList;
    private List<String> catePooltableShortNameList;
    private Popup poolInfoPopup = new Popup();
    private FlowPane tablesContainer;


    public void initializePoolInfo() {
        poolTableDAO = new PoolTableDAO();
        setupCategoryComboBox();

        // Setup Validate Inline
//        if(currentTable.getStatus().equalsIgnoreCase("Available")){
//            notifyLabel.setText("Enter a new name for this table");
//            notifyLabel.setStyle("-fx-text-fill: #FF9D23;"); // Red color
//            updateButton.setDisable(true);
//        }
        setupNameField();
        updateButton.setDisable(true);
    }

    private void setupNameField() {
        List<String> suggestionList = currentTableNameList.stream()
                .map(name -> name.replaceAll("\\s*\\d+\\s*", "")) // Remove numbers
                .map(String::trim)
                .map(str -> str + " ")// Trim any extra spaces
                .distinct() // Ensure unique names
                .toList();

        TextFields.bindAutoCompletion(nameField, suggestionList);

        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                // Provide feedback for empty name
                notifyLabel.setText("Please enter a name for the table.");
                notifyLabel.setStyle("-fx-text-fill: #DB2B3D;"); // Red color
                updateButton.setDisable(true);
            } else if (newValue.equalsIgnoreCase(currentTable.getName())) {
                notifyLabel.setText("Nothing changed in table name.");
                notifyLabel.setStyle("-fx-text-fill: #FF9D23;"); // Red color
                updateButton.setDisable(true);
            } else if (currentTableNameList.contains(newValue)) {
                // Provide feedback for existing name
                notifyLabel.setText("This table name already exists!");
                notifyLabel.setStyle("-fx-text-fill: #DB2B3D;"); // Red color
                updateButton.setDisable(true);
            } else {
                // Name is valid
                notifyLabel.setText("This table name is valid.");
                notifyLabel.setStyle("-fx-text-fill: #28BE8E;"); // Green color
                updateButton.setDisable(false);
            }
        });
    }


    private void setupCategoryComboBox() {
        try {
            // Set cell factory to display only short names
            categoryComboBox.setCellFactory(lv -> new ListCell<CatePooltable>() {
                @Override
                protected void updateItem(CatePooltable item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getShortName());
                    }
                }
            });

            // Set button cell to display only short names
            categoryComboBox.setButtonCell(new ListCell<CatePooltable>() {
                @Override
                protected void updateItem(CatePooltable item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getShortName());
                    }
                }
            });

            categoryComboBox.setStyle("-fx-font-size: 14px;");
        } catch (Exception e) {
            NotificationService.showNotification("Error", "Failed to load categories: " + e.getMessage(),
                    NotificationStatus.Error);
        }
    }

    public void setPoolTable(PoolTable table) {
        this.currentTable = table;
        nameField.setText(table.getName());


        try {
            CatePooltable selectedCategory = null;
            for (CatePooltable category : categoryComboBox.getItems()) {
                System.out.println("Checking category: " + category.getName() + " with ID: " + category.getId() +
                        ", Table Category ID: " + currentTable.getCatePooltableId());
                if (category.getId() == currentTable.getCatePooltableId()) {
                    System.out.println("Found category: " + category.getName());
                    selectedCategory = category;
                    break;
                }
            }

            // Set the selected category in the combo box
            if (selectedCategory != null) {
                categoryComboBox.setValue(selectedCategory);
            } else {
                System.out.println("No matching category found for ID: " + currentTable.getCatePooltableId());
            }

            // Enable/disable buttons based on table availability
            boolean isAvailable = "Available".equals(table.getStatus());
            nameField.setDisable(!isAvailable);
            updateButton.setDisable(!isAvailable);
            removeButton.setDisable(!isAvailable);

            // Make fields read-only if table is not available
            nameField.setEditable(isAvailable);
            categoryComboBox.setDisable(!isAvailable);
        } catch (Exception e) {
            NotificationService.showNotification("Error", "Failed to load table information: " + e.getMessage(),
                    NotificationStatus.Error);
        }
    }

    @FXML
    private void handleUpdate() {
        try {
            String name = nameField.getText().trim();
            CatePooltable selectedCategory = categoryComboBox.getValue();

            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmation");
            confirmAlert.setHeaderText("Please Confirm");
            confirmAlert.setContentText("Are you sure you want to proceed with the name: " + name + " and category: " + selectedCategory + "?");

            Optional<ButtonType> result = confirmAlert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK){
                // Validate input
                if (name.isEmpty()) {
                    NotificationService.showNotification("Error", "Table name cannot be empty", NotificationStatus.Error);
                    return;
                }

                if (selectedCategory == null) {
                    NotificationService.showNotification("Error", "Please select a category", NotificationStatus.Error);
                    return;
                }

                // Update table properties
                currentTable.setName(name);
                currentTable.setCatePooltableId(selectedCategory.getId());
                currentTable.setCatePooltableName(selectedCategory.getName());
                currentTable.setPrice(selectedCategory.getPrice()); // Also update the price based on the new category

                // Update in database
                poolTableDAO.updateTable(currentTable);
                NotificationService.showNotification("Success", "Pool table updated successfully",
                        NotificationStatus.Success);

                if (poolTableController != null) {
                    poolTableController.hidePoolPopup();
                    poolTableController.resetTableListAndTableNameList();
                    poolTableController.handleViewAllTables();
                }
            }


        } catch (Exception e) {
            NotificationService.showNotification("Error", "Failed to update pool table: " + e.getMessage(),
                    NotificationStatus.Error);
        }
    }

    public void showPoolInfoPopup(Pane content) {
        if (poolInfoPopup == null) poolInfoPopup = new Popup();
        if (poolInfoPopup.isShowing()) poolInfoPopup.hide();

        StackPane contentPane = new StackPane();
        contentPane.setStyle("-fx-background-color: white; -fx-padding: 10;");
        contentPane.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, null, null)));
        contentPane.getChildren().add(content);

        poolInfoPopup.getContent().add(contentPane);

        Scene scene = tablesContainer.getScene();
        double sceneWidth = scene.getWidth();
        double sceneHeight = scene.getHeight();

        double popupWidth = content.getPrefWidth();
        double popupHeight = content.getPrefHeight();

        System.out.println("Pool Popup Width : " + popupWidth);
        System.out.println("Pool Popup Height : " + popupHeight);

        double xPos = (sceneWidth - popupWidth) / 2;
        double yPos = (sceneHeight - popupHeight) / 2;

        poolInfoPopup.setX(xPos);
        poolInfoPopup.setY(yPos);

        // Fade-in effect
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.25), contentPane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        poolInfoPopup.setAutoHide(true);
        poolInfoPopup.setAutoFix(true);
        poolInfoPopup.show(tablesContainer.getScene().getWindow());
        fadeIn.play();
    }


    @FXML
    private void handleRemove() {
        VBox popupContent = new VBox(15);
        popupContent.setPadding(new Insets(20));
        popupContent.setAlignment(Pos.CENTER);

        Label confirmationLabel = new Label("Are you sure you want to\ndelete this pool table?");
        confirmationLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(Pos.CENTER);

        Button confirmButton = new Button("Confirm");
        confirmButton.setStyle("-fx-background-color: #ff5555; -fx-text-fill: white;");
        confirmButton.setPrefWidth(130.0);

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #cccccc; -fx-text-fill: black;");

        // Handle Cancel Button
        cancelButton.setOnAction(event -> {
            poolInfoPopup.hide();
            poolTableController.getCurrentPoolPopup().hide();
        });

        // Handle Confirm Button
        confirmButton.setOnAction(event -> {
            try {
                // Check for poolTableController
                if (poolTableController == null) {
                    System.out.println("\033[1;31m" + "🔥 Error: poolTableController is null! 🔥" + "\033[0m" + " 🤦‍♂️😂💥");
                } else {
                    // If not null, show the poolTableController object
                    System.out.println("\033[1;32m" + "✅ poolTableController is initialized: " + poolTableController.toString() + " ✅" + "\033[0m" + " 😄🎉");
                }

                // Check for poolInfoPopup
                if (poolInfoPopup == null) {
                    System.out.println("\033[1;31m" + "🔥 Error: poolInfoPopup is null! 🔥" + "\033[0m" + " 🤷‍♂️🤣💥");
                } else {
                    // If not null, show the poolInfoPopup object
                    System.out.println("\033[1;32m" + "✅ poolInfoPopup is initialized: " + poolInfoPopup.toString() + " ✅" + "\033[0m" + " 😄🎊");
                }

                poolTableDAO.removeTable(currentTable.getTableId());
                NotificationService.showNotification("Success", "Pool table " + currentTable.getName() + " removed successfully",
                        NotificationStatus.Success);
            } catch (Exception e) {
                System.out.println("\033[1;31m" + "From PoolTableInfoController, handleRemove():" + "\033[0m" + " ❌ Error: Something went wrong! 🤦‍♂️😂");
                System.out.println("❌ Error: Failed to DELETE the pool table ‍♂️😂");
            }
        });

        if (poolInfoPopup != null) {
            poolInfoPopup.setOnHidden(e -> {
                poolTableController.handleViewAllTables();
                poolTableController.resetTableNameList();
                System.out.println("\033[1;32m" + "From PoolTableInfoController, handleViewAllTables():" + "\033[0m" + " ✅ Successfully executed handleViewAllTables! 🎊");
                poolInfoPopup.setOnHidden(null);
            });
        }

        buttonContainer.getChildren().addAll(confirmButton, cancelButton);
        popupContent.getChildren().addAll(confirmationLabel, buttonContainer);

        showPoolInfoPopup(popupContent);
    }

    public void setPoolTableController(PoolTableController poolTableController) {
        this.poolTableController = poolTableController;
    }

    public void setTableNameList(List<String> tableNameList) {
        this.currentTableNameList = tableNameList;
    }

    public void setCatePooltableComboBox(List<CatePooltable> catePooltableComboBox) {
        this.categoryComboBox.getItems().clear();
        this.categoryComboBox.getItems().addAll(catePooltableComboBox);
    }

    public void setTablesContainer(FlowPane tablesContainer) {
        this.tablesContainer = tablesContainer;
    }

}