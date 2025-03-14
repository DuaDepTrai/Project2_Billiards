package src.billiardsmanagement.controller.orders.bookings;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Popup;
import javafx.stage.Stage;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import src.billiardsmanagement.dao.BookingDAO;
import src.billiardsmanagement.dao.OrderDAO;
import src.billiardsmanagement.model.Booking;
import src.billiardsmanagement.model.DatabaseConnection;
import src.billiardsmanagement.model.Order;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class AddBookingController implements Initializable {

    @FXML
    private ComboBox<String> bookingStatusComboBox;


    @FXML
    private DatePicker datePicker;

    @FXML
    private TextField hourTextField;

    @FXML
    private TextField minuteTextField;

    @FXML
    private TextField orderIDField;

    @FXML
    private TextField tableNameColumn;

    @FXML
    private Label startTimeLabel;

    @FXML
    private TableView<Order> orderTable;

    private Popup popup;
    private ListView<String> listView;
    private int orderID;

    Connection conn = DatabaseConnection.getConnection();
    private final Map<String, Integer> getTableNameToIdMap = new HashMap<>();
    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadTableNameToIdMap();
//        popup = new Popup();
//        listView = new ListView<>();
//        popup.getContent().add(listView);

        if(!getTableNameToIdMap.isEmpty()){
            ArrayList<String> tableNames = new ArrayList<>(getTableNameToIdMap.keySet());
            AutoCompletionBinding<String> tableNameCompletion = TextFields.bindAutoCompletion(tableNameColumn, tableNames);
            tableNameCompletion.setVisibleRowCount(7);
            tableNameCompletion.setHideOnEscape(true);
            tableNameCompletion.setUserInput(" ");
        }

//        tableNameColumn.textProperty().addListener((observable, oldValue, newValue) -> {
//            if (!newValue.isEmpty()) {
//                List<String> filteredCustomers = fetchTableByName(newValue);
//
//                System.out.println("List view: " + filteredCustomers);
//                listView.getItems().setAll(filteredCustomers);
//
//                if (!filteredCustomers.isEmpty() && !popup.isShowing()) {
//                    popup.show(tableNameColumn,
//                            tableNameColumn.localToScreen(tableNameColumn.getBoundsInLocal()).getMinX(),
//                            tableNameColumn.localToScreen(tableNameColumn.getBoundsInLocal()).getMaxY());
//                }
//            } else {
//                popup.hide();
//            }
//        });
//
//        listView.setOnMouseClicked(event -> {
//            if (!listView.getSelectionModel().isEmpty()) {
//                tableNameColumn.setText(listView.getSelectionModel().getSelectedItem());
//                popup.hide();
//            }
//        });

//        tableNameColumn.setOnKeyPressed(event -> {
//            switch (event.getCode()) {
//                case ENTER:
//                    if (!listView.getSelectionModel().isEmpty()) {
//                        tableNameColumn.setText(listView.getSelectionModel().getSelectedItem());
//                        popup.hide();
//                    }
//                    break;
//                case ESCAPE:
//                    popup.hide();
//                    break;
//                default:
//                    break;
//            }
//        });
        LocalTime now = LocalTime.now();
        int currentHour = now.getHour();
        int currentMinute = now.getMinute();

        hourTextField.setText(String.valueOf(currentHour));
        minuteTextField.setText(String.valueOf(currentMinute));

// Validate khi nhập liệu
        hourTextField.textProperty().addListener((observable, oldValue, newValue) -> validateTimeInput(hourTextField, 23));
        minuteTextField.textProperty().addListener((observable, oldValue, newValue) -> validateTimeInput(minuteTextField, 59));

        datePicker.setValue(LocalDate.now());

        bookingStatusComboBox.setItems(FXCollections.observableArrayList("Order", "Playing"));
        // Lắng nghe thay đổi của bookingStatusComboBox để hiển thị/ẩn các trường giờ và ngày
        bookingStatusComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            toggleDateTimeFields(newValue);
        });
        datePicker.setVisible(false);
        hourTextField.setVisible(false);
        minuteTextField.setVisible(false);
        startTimeLabel.setVisible(false);
    }

    private void validateTimeInput(TextField textField, int maxValue) {
        String input = textField.getText();
        if (!input.matches("\\d*")) {
            textField.setText(input.replaceAll("[^\\d]", "")); // Chỉ giữ lại số
        }

        if (!input.isEmpty()) {
            int value = Integer.parseInt(input);
            if (value > maxValue) {
                textField.setText(String.valueOf(maxValue)); // Giới hạn giá trị tối đa
            }
        }
    }

    public void setOrderId(int orderID) {
        this.orderID = orderID;
        initializeOrderId();
    }

    public void saveBooking(ActionEvent event) {
        try {
            int order_id = Integer.parseInt(orderIDField.getText());
            if (order_id == 0) {
                throw new IllegalArgumentException("Please choose an Order Id !");
            }

            String selectedTableName = tableNameColumn.getText();
            int tableId;
            if (selectedTableName != null) {
                tableId = getTableNameToIdMap.get(selectedTableName);
            } else {
                throw new IllegalArgumentException("You haven't choose any Pool Table yet. Please select one !");
            }

            int selectedHour = Integer.parseInt(hourTextField.getText());
            int selectedMinute = Integer.parseInt(minuteTextField.getText());

            if (selectedHour < 0 || selectedHour > 23 || selectedMinute < 0 || selectedMinute > 59) {
                throw new IllegalArgumentException("Please enter the hour from 0-23 and minute from 0-59");
            }

            LocalDate selectedDate = datePicker.getValue();
            if (selectedDate == null) {
                throw new IllegalArgumentException("Please choose a day !");
            }

            String startTime = String.format("%02d:%02d", selectedHour, selectedMinute);
            LocalTime time = LocalTime.parse(startTime);
            LocalDateTime dateTime = LocalDateTime.of(selectedDate, time);
            Timestamp timeStamp = Timestamp.valueOf(dateTime);

            String bookingStatus = bookingStatusComboBox.getValue();
            if (bookingStatus == null || bookingStatus.isEmpty()) {
                throw new IllegalArgumentException("Please enter the Booking Status");
            }

            Booking newBooking = new Booking(order_id, tableId, timeStamp, bookingStatus);
            BookingDAO bookingDAO = new BookingDAO();
            bookingDAO.addBooking(newBooking);


            // Navigate back to the previous scene
            Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            currentStage.close(); // Close the current stage

        } catch (IllegalArgumentException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("An error occurred while saving the data.");
            alert.setContentText("Please try again later.");
            alert.showAndWait();
        }
    }

    private void initializeOrderId() {
        orderIDField.setText(String.valueOf(orderID));
        System.out.println("OrderIdField: " + orderIDField.getText());
    }

    private void loadTableNameToIdMap() {
        String query = "SELECT table_id,name FROM pooltables WHERE status = 'Available'";
        try (PreparedStatement statement = conn.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            getTableNameToIdMap.clear();
            while (resultSet.next()) {
                int id = resultSet.getInt("table_id");
                String name = resultSet.getString("name");

                getTableNameToIdMap.put(name, id);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load table data.");
        }
    }

    private void toggleDateTimeFields(String bookingStatus) {
        if ("Order".equals(bookingStatus)) {
            // Hiển thị các trường ngày giờ khi trạng thái là "order"
            datePicker.setVisible(true);
            hourTextField.setVisible(true);
            minuteTextField.setVisible(true);
            startTimeLabel.setVisible(true);
        } else {
            // Ẩn các trường ngày giờ nếu trạng thái không phải là "order"
            datePicker.setVisible(false);
            hourTextField.setVisible(false);
            minuteTextField.setVisible(false);
            startTimeLabel.setVisible(false);
        }
    }

    private List<String> fetchTableByName(String name) {
        List<String> tableNames = new ArrayList<>();
        String query = "SELECT name FROM pooltables WHERE name LIKE ? AND status = 'available'";

        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, name + "%"); // Use LIKE with a wildcard for partial matches
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String tableName = resultSet.getString("name");
                tableNames.add(tableName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while fetching table names", e);
        }
        System.out.println("Query executed: " + query + " with name: " + name + "%");
        return tableNames;
    }

    public void setOrderTable(TableView<Order> orderTable) {
        this.orderTable = orderTable;
    }





    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
