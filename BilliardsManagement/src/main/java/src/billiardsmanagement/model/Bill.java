package src.billiardsmanagement.model;

import src.billiardsmanagement.dao.BookingDAO;
import src.billiardsmanagement.dao.OrderItemDAO;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Bill {
    private int billId;
    private int orderId;
    private int customerId;
    private String customerName;
    private String customerPhone;
    private double totalCost;
    private String orderStatus;

    private List<OrderItem> orderItems;
    private List<Booking> bookings;
//    private List<RentCue> rentCues;

    private LocalTime billTime;

    // Default constructor
    public Bill() {
    }

    public Bill(int billId, int orderId, int customerId, String customerName, double totalCost, String orderStatus, String customerPhone, List<OrderItem> orderItems, LocalTime billTime, List<Booking> bookings) {
        this.billId = billId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.totalCost = totalCost;
        this.orderStatus = orderStatus;
        this.customerPhone = customerPhone;
        this.orderItems = orderItems;
        this.billTime = billTime;
        this.bookings = bookings;
    }

    public Bill(int orderId, int customerId, String customerName, String customerPhone, double totalCost,
                String orderStatus, ArrayList<OrderItem> orderItems,
                ArrayList<Booking> bookings, LocalDateTime createdTime) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.totalCost = totalCost;
        this.orderStatus = orderStatus;
        this.orderItems = orderItems != null ? orderItems : new ArrayList<>();
        this.bookings = bookings != null ? bookings : new ArrayList<>();
        this.billTime = LocalTime.from(createdTime);
    }

    public int getBillId() {
        return billId;
    }

    public void setBillId(int billId) {
        this.billId = billId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }

    public LocalTime getBillTime() {
        return billTime;
    }

    public void setBillTime(LocalTime billTime) {
        this.billTime = billTime;
    }

    public List<OrderItem> getOrderItemsFromDB() {
        return OrderItemDAO.getForEachOrderItem(this.orderId);
    }

    public List<Booking> getBookingsFromDB(){
        return BookingDAO.getBookingByOrderId(this.orderId);
    }

}
