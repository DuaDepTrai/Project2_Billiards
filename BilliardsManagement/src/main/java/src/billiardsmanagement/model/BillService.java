package src.billiardsmanagement.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import src.billiardsmanagement.dao.BookingDAO;
import src.billiardsmanagement.dao.OrderItemDAO;
import src.billiardsmanagement.dao.RentCueDAO;

import java.util.List;

public class BillService {
    public static ObservableList<BillItem> getBillItems(int orderId) {
        ObservableList<BillItem> billItems = FXCollections.observableArrayList();

        // Lấy danh sách đặt bàn từ BookingDAO
        List<Booking> bookings = BookingDAO.getBookingByOrderId(orderId);

        System.out.println(bookings);
        for (Booking booking : bookings) {
            billItems.add(new BillItem(
                    booking.getTableName(), // Tên bàn
                    booking.getTimeplay(), // Số giờ chơi
                    booking.getPriceTable(), // Giá bàn/giờ
                    booking.getSubTotal() // Tổng tiền bàn
            ));
        }

        // Lấy danh sách sản phẩm từ OrderItemDAO
        List<OrderItem> orderItems = OrderItemDAO.getForEachOrderItem(orderId);
        for (OrderItem item : orderItems) {
            billItems.add(new BillItem(
                    item.getProductName(), // Tên sản phẩm
                    item.getQuantity(), // Số lượng
                    item.getProductPrice(), // Đơn giá sản phẩm
                    item.getSubTotal() // Thành tiền
            ));
        }

        // Lấy danh sách gậy thuê từ RentCueDAO
        List<RentCue> rentCues = RentCueDAO.getAllRentCuesByOrderId(orderId);

        for (RentCue rentCue : rentCues) {
            billItems.add(new BillItem(
                    rentCue.getProductName(),
                     rentCue.getTimeplay(),
                    rentCue.getProductPrice(),
                    rentCue.getSubTotal()
            ));
        }

        return billItems;

    }
}
