package src.billiardsmanagement.controller.orders.bookings;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import src.billiardsmanagement.dao.BookingDAO;
import src.billiardsmanagement.dao.PoolTableDAO;
import src.billiardsmanagement.model.Booking;
import src.billiardsmanagement.model.Order;

import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
public class AddBookingController implements Initializable {
    @FXML
    private ComboBox<String> tableIdComboBox;

    @FXML
    private ComboBox<String> bookingStatusComboBox;

    @FXML
    private ComboBox<Integer> hourComboBox;

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<Integer> minuteComboBox;
    @FXML
    private TextField orderIDField;

    private Order order;
    private int orderID;

    private Map<String,Integer> tableNameToIdMap;

    @FXML
    private void saveBooking(ActionEvent actionEvent) {
        try {
            int order_id = Integer.parseInt(orderIDField.getText());
            if (order_id == 0) {
                throw new IllegalArgumentException("Vui lòng chọn order_id");
            }

            String selectedTableName = tableIdComboBox.getValue();
            int tableId;
            if (selectedTableName != null) {
                tableId = tableNameToIdMap.get(selectedTableName);
            } else {
                throw new IllegalArgumentException("Vui lòng chọn table_id");
            }

            Integer selectedHour = hourComboBox.getValue();
            Integer selectedMinute = minuteComboBox.getValue();
            if (selectedHour == null || selectedMinute == null) {
                throw new IllegalArgumentException("Vui lòng nhập thời gian chơi");
            }

            LocalDate selectedDate = datePicker.getValue();
            if (selectedDate == null) {
                throw new IllegalArgumentException("Vui lòng chọn ngày");
            }

            String startTime = String.format("%02d:%02d", selectedHour, selectedMinute);
            LocalTime time = LocalTime.parse(startTime);
            LocalDateTime dateTime = LocalDateTime.of(selectedDate, time);
            Timestamp timeStamp = Timestamp.valueOf(dateTime);

            String bookingStatus = bookingStatusComboBox.getValue();
            if (bookingStatus == null || bookingStatus.isEmpty()) {
                throw new IllegalArgumentException("Vui lòng nhập trạng thái bàn");
            }

            Booking newBooking = new Booking(order_id, tableId, timeStamp, bookingStatus);
            BookingDAO bookingDAO = new BookingDAO();
            bookingDAO.addBooking(newBooking);

            Stage stage = (Stage) tableIdComboBox.getScene().getWindow();
            stage.close();
        } catch (IllegalArgumentException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText("Đã xảy ra lỗi khi lưu dữ liệu.");
            alert.setContentText("Vui lòng thử lại sau.");
            alert.showAndWait();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        PoolTableDAO poolTableDAO = new PoolTableDAO();
        Map<Integer, String> tableMap = poolTableDAO.getAvailableTable();
        tableNameToIdMap = tableMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        tableIdComboBox.getItems().addAll(tableNameToIdMap.keySet());
        
        LocalTime now = LocalTime.now();
        int currentHour = now.getHour();
        int currentMinute = now.getMinute();

        // Thêm dữ liệu cho ComboBox giờ (0-23)
        for (int i = 0; i < 24; i++) {
            hourComboBox.getItems().add(i);
        }

        // Thêm dữ liệu cho ComboBox phút (0-59)
        for (int i = 0; i < 60; i++) {
            minuteComboBox.getItems().add(i);
        }

        // Đặt giá trị mặc định
        hourComboBox.setValue(currentHour);
        minuteComboBox.setValue(currentMinute);
        datePicker.setValue(LocalDate.now());

        // Lắng nghe thay đổi của bookingStatusComboBox để hiển thị/ẩn các trường giờ và ngày
        bookingStatusComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            toggleDateTimeFields(newValue);
        });
        datePicker.setDisable(true);
        hourComboBox.setDisable(true);
        minuteComboBox.setDisable(true);
    }

    private void toggleDateTimeFields(String bookingStatus) {
        if ("Order".equals(bookingStatus)) {
            // Hiển thị các trường ngày giờ khi trạng thái là "order"
            datePicker.setDisable(false);
            hourComboBox.setDisable(false);
            minuteComboBox.setDisable(false);
        } else {
            // Ẩn các trường ngày giờ nếu trạng thái không phải là "order"
            datePicker.setDisable(true);
            hourComboBox.setDisable(true);
            minuteComboBox.setDisable(true);
        }
    }

    public void setOrderId(int orderID) {
        this.orderID = orderID;
        initializeOrderId();
    }

    private void initializeOrderId(){
        orderIDField.setText(String.valueOf(orderID));
        System.out.println("OrderIdField: " + orderIDField.getText());
    }
}
