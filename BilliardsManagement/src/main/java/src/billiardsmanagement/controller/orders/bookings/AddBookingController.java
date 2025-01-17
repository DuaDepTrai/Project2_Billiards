package src.billiardsmanagement.controller.orders.bookings;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
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
    private ComboBox<Integer> minuteComboBox;
    @FXML
    private TextField orderIDField;
    private Order order;
    private int orderID;

    private Map<String,Integer> tableNameToIdMap;
    public void saveBooking(ActionEvent actionEvent) {
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

            String startTime = String.format("%02d:%02d", selectedHour, selectedMinute);
            LocalTime time = LocalTime.parse(startTime);
            LocalDateTime datatime = time.atDate(LocalDate.now());
            Timestamp timeStamp = Timestamp.valueOf(datatime);

            String bookingStatus = bookingStatusComboBox.getValue();
            if (bookingStatus == null || bookingStatus.isEmpty()) {
                throw new IllegalArgumentException("Vui lòng nhập trạng thái bàn ");
            }

            Booking newBooking = new Booking(order_id, tableId, timeStamp, bookingStatus);
            BookingDAO bookingDAO = new BookingDAO();
            bookingDAO.addBooking(newBooking);

            Stage stage = (Stage) tableIdComboBox.getScene().getWindow();
            stage.close();
        } catch (IllegalArgumentException e) {
            // Hiển thị thông báo lỗi cho người dùng
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            // Thêm xử lý lỗi nếu cần
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText("Đã xảy ra lỗi khi lưu dữ liệu.");
            alert.setContentText("Vui lòng thử lại sau.");
            alert.showAndWait();
        }
    }
    private void initializeOrderId(){
        orderIDField.setText(String.valueOf(orderID));
        System.out.println("OrderIdField: " + orderIDField.getText());
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        PoolTableDAO poolTableDAO = new PoolTableDAO();
        Map<Integer,String> tableMap = poolTableDAO.getAvailableTable();
        tableNameToIdMap = tableMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue,Map.Entry::getKey));
        tableIdComboBox.getItems().addAll(tableNameToIdMap.keySet());

        LocalTime now = LocalTime.now();
        int currentHour = now.getHour();
        int currentMinute = now.getMinute();
        // Định dạng thời gian
        // Thêm dữ liệu cho ComboBox giờ (0-23)

        for (int i = 0; i < 24; i++) {
            hourComboBox.getItems().add(i);
        }

        // Thêm dữ liệu cho ComboBox phút (0-59)
        for (int i = 0; i < 60; i++) {
            minuteComboBox.getItems().add(i);
        }

        hourComboBox.setValue(currentHour);  // Đặt giá trị giờ hiện tại
        minuteComboBox.setValue(currentMinute);  // Đặt giá trị phút hiện tại
    }

    public void setOrderId(int orderID) {
        this.orderID = orderID;
        initializeOrderId();
    }
}
