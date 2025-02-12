package src.billiardsmanagement.model;

import src.billiardsmanagement.model.Order;
import src.billiardsmanagement.model.Revenue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RevenueService {

    // Tính doanh thu theo ngày
    public static Revenue calculateRevenueByDate(List<Order> orders, List<Booking> bookings, LocalDate date) {
        List<Order> filteredOrders = orders.stream()
                .filter(order -> {
                    List<Booking> orderBookings = bookings.stream()
                            .filter(b -> b.getOrderId() == order.getOrderId())
                            .collect(Collectors.toList());

                    // Kiểm tra nếu có Booking nào có startTime trong ngày được chọn
                    return orderBookings.stream()
                            .map(Booking::getStartTimeBooking) // Lấy danh sách startTime của các Booking
                            .filter(Objects::nonNull) // Loại bỏ giá trị null
                            .map(LocalDateTime::toLocalDate) // Chuyển thành LocalDate
                            .anyMatch(d -> d.equals(date)) // Kiểm tra có Booking nào trùng ngày không
                            && "Paid".equals(order.getOrderStatus());


                })
                .collect(Collectors.toList());

        return generateRevenue(date, filteredOrders);
    }

    // Tính doanh thu theo tháng
    public static Revenue calculateRevenueByMonth(List<Order> orders, int year, int month) {
        List<Order> filteredOrders = orders.stream()
                .filter(order -> order.getCreatedAt() != null
                        && order.getCreatedAt().getYear() == year
                        && order.getCreatedAt().getMonthValue() == month
                        && "Paid".equals(order.getOrderStatus()))
                .collect(Collectors.toList());

        return generateRevenue(LocalDate.of(year, month, 1), filteredOrders);
    }

    // Tính doanh thu theo năm
    public static Revenue calculateRevenueByYear(List<Order> orders, int year) {
        List<Order> filteredOrders = orders.stream()
                .filter(order -> order.getCreatedAt() != null
                        && order.getCreatedAt().getYear() == year
                        && "Paid".equals(order.getOrderStatus()))
                .collect(Collectors.toList());

        return generateRevenue(LocalDate.of(year, 1, 1), filteredOrders);
    }

    // Hàm tạo đối tượng Revenue từ danh sách Order
    private static Revenue generateRevenue(LocalDate date, List<Order> orders) {
        double totalRevenue = orders.stream().mapToDouble(Order::getTotalCost).sum();
        int totalOrders = orders.size();
        int totalCustomers = (int) orders.stream().map(Order::getCustomerId).distinct().count();

        return new Revenue(0, date, totalRevenue, totalOrders, totalCustomers);
    }

}
