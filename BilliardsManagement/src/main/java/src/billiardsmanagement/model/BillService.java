package src.billiardsmanagement.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import src.billiardsmanagement.dao.BookingDAO;
import src.billiardsmanagement.dao.OrderItemDAO;
import src.billiardsmanagement.dao.ProductDAO;
import src.billiardsmanagement.dao.RentCueDAO;

import java.util.List;

public class BillService {
    public static ObservableList<BillItem> getBillItems(int orderId) {
        ObservableList<BillItem> billItems = FXCollections.observableArrayList();

        // Lấy danh sách đặt bàn từ BookingDAO
        List<Booking> bookings = BookingDAO.getBookingByOrderId(orderId);
        for (Booking booking : bookings) {
            billItems.add(new BillItem(
                    booking.getTableName(), // Tên bàn
                    "Hour", // Đơn vị tính là giờ
                    booking.getTimeplay(), // Số giờ chơi
                    booking.getPriceTable(), // Giá bàn/giờ
                    booking.getNetTotal() // Tổng tiền bàn
            ));
        }

        // Lấy danh sách sản phẩm từ OrderItemDAO
        List<OrderItem> orderItems = OrderItemDAO.getForEachOrderItem(orderId);
        for (OrderItem item : orderItems) {
            // Lấy đơn vị tính từ bảng product
            String unit = ProductDAO.getProductUnitById(item.getProductId()); // Cần thêm phương thức này trong ProductDAO

            billItems.add(new BillItem(
                    item.getProductName(), // Tên sản phẩm
                    unit, // Đơn vị tính lấy từ product
                    item.getQuantity(), // Số lượng
                    item.getProductPrice(), // Đơn giá sản phẩm
                    item.getNetTotal() // Thành tiền
            ));
        }

        // Lấy danh sách gậy thuê từ RentCueDAO
        List<RentCue> rentCues = RentCueDAO.getAllRentCuesByOrderId(orderId);
        for (RentCue rentCue : rentCues) {
            billItems.add(new BillItem(
                    rentCue.getProductName(),
                    "Hour", // Đơn vị tính là giờ
                    rentCue.getTimeplay(),
                    rentCue.getProductPrice(),
                    rentCue.getNetTotal()
            ));
        }

        return billItems;
    }
}
