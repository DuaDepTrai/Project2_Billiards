package src.billiardsmanagement.model;

import src.billiardsmanagement.dao.BookingDAO;
import src.billiardsmanagement.dao.OrderDAO;

import java.time.LocalDate;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        List<Order> orders = OrderDAO.getOrderPaid();  // Lấy danh sách đơn hàng
        List<Booking> bookings = BookingDAO.getBookingByOrderId(28);  // Lấy danh sách đặt bàn

        LocalDate date = LocalDate.parse("2025-02-08"); // Chuyển đổi String thành LocalDate
        Revenue revenueToday = RevenueService.calculateRevenueByDate(orders, bookings, date);

        Double price = revenueToday.getTotal_revenue();
        System.out.println("Doanh thu hôm nay: " + price);
    }
}
