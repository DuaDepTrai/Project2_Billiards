package src.billiardsmanagement.model;

import src.billiardsmanagement.dao.BookingDAO;
import src.billiardsmanagement.dao.OrderDAO;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class Test {
    public static void main(String[] args) {
        List<Order> orders = OrderDAO.getOrderPaid();  // Lấy danh sách đơn hàng đã thanh toán

// Lấy tất cả Booking từ danh sách Order đã thanh toán

    }
}
