package src.billiardsmanagement.model;

import src.billiardsmanagement.model.Order;
import src.billiardsmanagement.model.Revenue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RevenueService {
    public static Revenue calculateRevenueByDate(List<Order> orders, List<Booking> bookings, LocalDate date) {
        List<Order> filteredOrders = orders.stream()
                .filter(order -> {
                    List<Booking> orderBookings = bookings.stream()
                            .filter(b -> b.getOrderId() == order.getOrderId())
                            .collect(Collectors.toList());

                    // Kiểm tra nếu có Booking nào có startTime trong ngày được chọn
                    return orderBookings.stream()
                            .anyMatch(b -> b.getStartTimeBooking() != null && b.getStartTimeBooking().toLocalDate().equals(date))
                            && "Paid".equals(order.getOrderStatus());
                })
                .collect(Collectors.toList());

        return generateRevenue(date, filteredOrders);
    }
    public static Revenue calculateRevenueByMonth(List<Order> orders, List<Booking> bookings, YearMonth month) {
        List<Order> filteredOrders = orders.stream()
                .filter(order -> {
                    List<Booking> orderBookings = bookings.stream()
                            .filter(b -> b.getOrderId() == order.getOrderId())
                            .collect(Collectors.toList());

                    // Kiểm tra nếu có Booking nào có startTime trong tháng được chọn
                    return orderBookings.stream()
                            .anyMatch(b -> b.getStartTimeBooking() != null &&
                                    YearMonth.from(b.getStartTimeBooking().toLocalDate()).equals(month))
                            && "Paid".equals(order.getOrderStatus());
                })
                .collect(Collectors.toList());

        return generateRevenue(month.atDay(1), filteredOrders);
    }

    public static Revenue calculateRevenueByYear(List<Order> orders, List<Booking> bookings, int year) {
        List<Order> filteredOrders = orders.stream()
                .filter(order -> {
                    List<Booking> orderBookings = bookings.stream()
                            .filter(b -> b.getOrderId() == order.getOrderId())
                            .collect(Collectors.toList());

                    // Kiểm tra nếu có Booking nào có startTime trong năm được chọn
                    return orderBookings.stream()
                            .anyMatch(b -> b.getStartTimeBooking() != null &&
                                    b.getStartTimeBooking().toLocalDate().getYear() == year)
                            && "Paid".equals(order.getOrderStatus());
                })
                .collect(Collectors.toList());

        return generateRevenue(LocalDate.of(year, 1, 1), filteredOrders);
    }
    private static Revenue generateRevenue(LocalDate date, List<Order> orders) {
        double totalRevenue = orders.stream().mapToDouble(Order::getTotalCost).sum();
        int totalOrders = orders.size();
        int totalCustomers = (int) orders.stream().map(Order::getCustomerId).distinct().count();

        return new Revenue(0, date, totalRevenue, totalOrders, totalCustomers);
    }

}


