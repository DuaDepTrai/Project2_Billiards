package src.billiardsmanagement.controller.orders.bookings;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import src.billiardsmanagement.dao.BookingDAO;

public class UpdateBookingController {
    // @FXML
    // private ComboBox<String> tableNameComboBox;

    // @FXML
    // private TextField startTimeField;

    // @FXML
    // private TextField endTimeField;

    // @FXML
    // private Label timeplayLabel;

    @FXML
    private RadioButton preBookedRadio;

    @FXML
    private RadioButton occupiedRadio;

    @FXML
    private RadioButton finishedRadio;

    @FXML
    private ToggleGroup tableStatusGroup;

    private int bookingId;
    private int orderId = 14; // set orderId để test
    private int tableId;
    private String tableStatus;
    private String bookingStatus;

    @FXML
    public void initialize() {
        // Khoi tao ToggleGroup
        tableStatusGroup = new ToggleGroup();

        // Gán ToggleGroup cho các RadioButton
        preBookedRadio.setToggleGroup(tableStatusGroup);
        occupiedRadio.setToggleGroup(tableStatusGroup);
        finishedRadio.setToggleGroup(tableStatusGroup);
    }

    // Phương thức hỗ trợ để lấy các thông tin đã set
    public int getBookingId() {
        return bookingId;
    }

    public int getOrderId() {
        return orderId;
    }

    public int getTableId() {
        return tableId;
    }

    public String getTableStatus() {
        return tableStatus;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    // Phương thức để set chi tiết booking khi mở cửa sổ cập nhật
    public void setBookingDetails(
            int bookingId,
            int orderId,
            int tableId,
            String tableStatus,
            String bookingStatus
    ) {
        // Lưu trữ thông tin booking
        this.bookingId = bookingId;
        this.orderId = orderId;
        this.tableId = tableId;
        this.tableStatus = tableStatus;
        this.bookingStatus = bookingStatus;

        // Đặt trạng thái radio button dựa trên tableStatus
        // switch (tableStatus) {
        //     case "Sẵn sàng để chơi":
        //         readyToPlayRadio.setSelected(true);
        //         break;
        //     case "Đã được đặt trước":
        //         preBookedRadio.setSelected(true);
        //         break;
        //     case "Đã có người chơi":
        //         occupiedRadio.setSelected(true);
        //         break;
        //     default:
        //         // Nếu không khớp, bỏ chọn tất cả
        //         tableStatusGroup.selectToggle(null);
        // }
    }

    // Phương thức cập nhật trạng thái bàn
    @FXML
    public void updateBooking(ActionEvent event) {
        // Lấy trạng thái được chọn
        RadioButton selectedRadio = (RadioButton) tableStatusGroup.getSelectedToggle();

        if (selectedRadio == null) {
            // Hiển thị cảnh báo nếu không chọn trạng thái
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Cảnh Báo");
            alert.setHeaderText("Chưa Chọn Trạng Thái");
            alert.setContentText("Vui lòng chọn trạng thái bàn.");
            alert.showAndWait();
            return;
        }

        // Lấy text của radio button được chọn
        String newTableStatus = "";
        String newBookingStatus = "";
        if (selectedRadio == preBookedRadio) {
            newTableStatus = "Ordered";
            newBookingStatus = "order";
        } else if (selectedRadio == occupiedRadio) {
            newTableStatus = "Playing";
            newBookingStatus = "playing";
        } else if (selectedRadio == finishedRadio) {
            newTableStatus = "Available";
            newBookingStatus = "finish";
        }

        // Gọi phương thức cập nhật trạng thái bàn
        boolean updateSuccess = BookingDAO.updateBooking(
                this.bookingId,
                this.orderId,
                this.tableId,
                newTableStatus,
                newBookingStatus
        );

        // Kiểm tra kết quả cập nhật và hiển thị thông báo
        Alert alert = new Alert(updateSuccess ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        alert.setTitle("Cập Nhật Trạng Thái");

        if (updateSuccess) {
            alert.setHeaderText("Cập Nhật Thành Công");
            alert.setContentText("Đã cập nhật trạng thái bàn thành: " + newTableStatus);
            alert.showAndWait();

            // Đóng cửa sổ sau khi cập nhật
            closeWindow(event);
        } else {
            alert.setHeaderText("Cập Nhật Thất Bại");
            alert.setContentText("Không thể cập nhật trạng thái bàn. Vui lòng kiểm tra lại thông tin.");
            alert.showAndWait();
        }
    }

    // Phương thức hủy
    @FXML
    public void handleCancel(ActionEvent event) {
        // Đóng cửa sổ hiện tại
        closeWindow(event);
    }

    private void closeWindow(ActionEvent event) {
        // Lấy stage (cửa sổ) từ sự kiện và đóng nó
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
