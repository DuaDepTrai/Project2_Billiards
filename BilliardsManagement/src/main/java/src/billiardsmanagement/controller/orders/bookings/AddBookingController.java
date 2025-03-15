package src.billiardsmanagement.controller.orders.bookings;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.util.Duration;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import src.billiardsmanagement.controller.poolTables.ChooseOrderTimeController;
import src.billiardsmanagement.dao.BookingDAO;
import src.billiardsmanagement.dao.PoolTableDAO;
import src.billiardsmanagement.model.Booking;
import src.billiardsmanagement.model.NotificationStatus;
import src.billiardsmanagement.model.PoolTable;
import src.billiardsmanagement.service.NotificationService;

import java.awt.print.Book;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class AddBookingController {
    @FXML private Button addBookingButton;
    @FXML private Label notifyLabel;
    @FXML private ComboBox<String> bookingStatusComboBox;
    @FXML private TextField tableNameTextField;
    @FXML private StackPane chooseOrderTimeStackPane;

    private Popup chooseOrderTimePopup;
    private Popup addBookingPopup;
    private StackPane addBookingStackPane;
    private Popup forEachPopup; // use this to hide popup after adding booking

    private List<PoolTable> poolTableAvailableList;
    private List<String> poolTableNameList;

    private ChooseOrderTimeController chooseOrderTimeController;
    private int orderId = -1;

    private BookingDAO bookingDAO = new BookingDAO();
    private Timestamp chooseOrderTimeResult;

    private String playStatus = "Play on Table";
    private String orderStatus = "Order on Table";

    public void initializeAddBooking() {
        bookingStatusComboBox.getItems().addAll(playStatus, orderStatus);
        bookingStatusComboBox.setValue("Play on Table");

        notifyLabel.setText("Please enter a Table Name !");
        notifyLabel.setStyle("-fx-text-fill: red;");
        notifyLabel.setVisible(true);
        addBookingButton.setDisable(true);

        poolTableAvailableList = PoolTableDAO.getAllAvailableTables();
        poolTableNameList = poolTableAvailableList.stream()
                .map(table -> table.getName() + " ") // Add space at the end
                .collect(Collectors.toList());

        // Listener for booking status changes
        bookingStatusComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("New Value Add Booking : "+newValue);
            handleBookingStatusChange(newValue);
        });

        tableNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateTableName(tableNameTextField.getText());
        });

        AutoCompletionBinding<String> autoCompletionBinding = TextFields.bindAutoCompletion(tableNameTextField, poolTableNameList);
        handleTextFieldClick(autoCompletionBinding, poolTableNameList, tableNameTextField);
    }

    private void handleBookingStatusChange(String newValue) {
        System.out.println("New Value : ["+newValue+"]");
        if (orderStatus.equals(newValue)) {
//            double xPos = chooseOrderTimeStackPane.getScene().getWindow().getX() + 100;
//            double yPos = chooseOrderTimeStackPane.getScene().getWindow().getY() + 100;

            showChooseOrderTimePopup((selectedDate, selectedTime) -> {
                LocalDateTime startTime = LocalDateTime.of(selectedDate, selectedTime);
                chooseOrderTimeResult = Timestamp.valueOf(startTime);
                System.out.println("From AddBookingController - Selected Order Time: " + startTime);

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                String formattedDateTime = startTime.format(formatter);

                System.out.println("From AddBookingController - Selected Order Time: " + formattedDateTime);
                System.out.println("From AddBookingController - Timestamp: " + chooseOrderTimeResult);
            });
        } else if (playStatus.equals(newValue)) {
            System.out.println("From AddBookingController - Selected Play on Table");
            chooseOrderTimeController = null;
            chooseOrderTimeStackPane.getChildren().clear();
            chooseOrderTimeResult = null;
        }
        System.out.println("From AddBookingController - handleBookingStatusChange called");
    }

    public void handleTextFieldClick(AutoCompletionBinding<String> auto, List<String> list, TextField text) {
        text.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) { // When the text field gains focus
                auto.setUserInput(" "); // Set user input to a space
            }
        });
    }

    private void validateTableName(String input) {
        // Trim the input for comparison
        String trimmedInput = input == null ? null : input.trim();

        if (trimmedInput == null || trimmedInput.isEmpty()) {
            notifyLabel.setText("Please enter a Table Name !");
            notifyLabel.setStyle("-fx-text-fill: red;");
            notifyLabel.setVisible(true);
            addBookingButton.setDisable(true);
        } else {
            boolean found = false;

            // Check if the trimmed input exists in the list
            for (String tableName : poolTableNameList) {
                if (tableName.trim().equals(trimmedInput)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                notifyLabel.setText("Cannot find this table !");
                notifyLabel.setStyle("-fx-text-fill: red;");
                notifyLabel.setVisible(true);
                addBookingButton.setDisable(true);
            } else {
                notifyLabel.setVisible(false); // Hide the label if the input is valid
                addBookingButton.setDisable(false);
            }
        }
    }

    private void showChooseOrderTimePopup(BiConsumer<LocalDate, LocalTime> onTimeSelected) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/pooltables/chooseOrderTime.fxml"));
            Parent root = loader.load();

            chooseOrderTimeController = loader.getController();
            chooseOrderTimeController.setOnTimeSelected(onTimeSelected);
            chooseOrderTimeController.hideConfirmButton();

            chooseOrderTimeStackPane.getChildren().add(root);

            // Apply fade-in effect
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.12), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void addBooking(ActionEvent actionEvent) {
        String selectedTable = tableNameTextField.getText().trim();
        PoolTable selectedPoolTable = poolTableAvailableList.stream()
                .filter(table -> table.getName().equals(selectedTable))
                .findFirst()
                .orElse(null);
        String selectedStatus = bookingStatusComboBox.getValue().equals("Play on Table") ? "Playing" : "Order";

        boolean bookingSuccess = false;

        if (chooseOrderTimeController == null) {
            if (selectedPoolTable != null && this.orderId > 0) {
                Timestamp startTime = new Timestamp(System.currentTimeMillis());
                Booking booking = new Booking(this.orderId, selectedPoolTable.getTableId(), startTime, selectedStatus);
                bookingSuccess = bookingDAO.addBooking(booking);
            }
        } else {
            chooseOrderTimeController.forceConfirm();
            if (selectedPoolTable != null && this.orderId > 0) {
                Booking booking = new Booking(this.orderId, selectedPoolTable.getTableId(), chooseOrderTimeResult, selectedStatus);
                bookingSuccess = bookingDAO.addBooking(booking);
            }
        }

        if (bookingSuccess) {
            NotificationService.showNotification("Success!", "Booking added successfully!", NotificationStatus.Success);
        } else {
            NotificationService.showNotification("Error!", "Failed to add booking. Please try again.", NotificationStatus.Error);
        }
    }

    private void updateCustomerInformation(ActionEvent actionEvent) {
        // Implementation for updating customer information
    }

    public void setForEachPopup(Popup forEachPopup) {
        this.forEachPopup = forEachPopup;
    }

    public void setAddBookingStackPane(StackPane addBookingStackPane) {
        this.addBookingStackPane = addBookingStackPane;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
}
