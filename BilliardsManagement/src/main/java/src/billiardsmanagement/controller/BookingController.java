package src.billiardsmanagement.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import src.billiardsmanagement.dao.BookingDAO;
import src.billiardsmanagement.model.Booking;
import src.billiardsmanagement.model.Order;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class BookingController implements Initializable {
    @FXML
    protected TableView bookingPoolTable;
    @FXML private TableColumn<Booking, String> tableNameColumn;
    @FXML private TableColumn<Booking, String> startTimeColumn;
    @FXML private TableColumn<Booking, String> endTimeColumn;
    @FXML private TableColumn<Booking, Double> timeplayColumn;
    @FXML private TableColumn<Booking, Double> priceColumn;
    @FXML private TableColumn<Booking, Double> costColumn;
    @FXML private TableColumn<Booking, String> statusColumn;

    private ObservableList<Booking> bookingList = FXCollections.observableArrayList();
    private BookingDAO bookingDAO = new BookingDAO();
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tableNameColumn.setCellValueFactory(new PropertyValueFactory<>("tableName"));
        startTimeColumn.setCellValueFactory(cellData -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStartTime().format(formatter));
        });

        endTimeColumn.setCellValueFactory(cellData -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEndTime().format(formatter));
        });

        timeplayColumn.setCellValueFactory(new PropertyValueFactory<>("timeplay"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("subTotal")); // Giá trị tương ứng với subtotal
        costColumn.setCellValueFactory(new PropertyValueFactory<>("netTotal")); // Giá trị tương ứng với netTotal
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("bookingStatus"));
        // Gọi phương thức load dữ liệu
        loadBookings();

    }

    private void loadBookings() {
        List<Booking> bookings = bookingDAO.getAllBookings();
        bookingPoolTable.getItems().clear();
        bookingPoolTable.getItems().addAll(bookings);
    }

    public void addBooking(ActionEvent actionEvent) {
        try {
            // Load FXML cho form thêm Order
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/add_booking.fxml"));
            Parent root = loader.load();

            // Tạo một Scene mới
            Stage stage = new Stage();
            stage.setTitle("Add Order");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Sau khi đóng form, refresh bảng Order
            loadBookings();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
