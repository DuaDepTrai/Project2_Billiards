package src.billiardsmanagement.model;

import java.time.LocalDateTime;

public class Booking {
    private int bookingId;
    private int tableId;
    private int orderId;
    private String bookingStatus;
    private String tableName; // [PoolTables.tableName]
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double timeplay;
    private double subTotal;
    private int promotionId;
    private double netTotal;

    public Booking(){}


    public Booking(String tableName, LocalDateTime startTime, LocalDateTime endTime, double timeplay, double subTotal, String bookingStatus) {
        this.tableName = tableName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.timeplay = timeplay;
        this.subTotal = subTotal;
        this.bookingStatus = bookingStatus;
    } // Constructor dùng để "đổ" data vào table

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }
    public int getBookingId() {
        return bookingId;
    }

    public Booking setBookingId(int bookingId) {
        this.bookingId = bookingId;
        return this;
    }

    public int getTableId() {
        return tableId;
    }

    public Booking setTableId(int tableId) {
        this.tableId = tableId;
        return this;
    }

    public int getOrderId() {
        return orderId;
    }

    public Booking setOrderId(int orderId) {
        this.orderId = orderId;
        return this;
    }

    public String getTableName() {
        return tableName;
    }

    public Booking setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public Booking setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public Booking setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
        return this;
    }

    public double getTimeplay() {
        return timeplay;
    }

    public Booking setTimeplay(double timeplay) {
        this.timeplay = timeplay;
        return this;
    }

    public double getSubTotal() {
        return subTotal;
    }

    public Booking setSubTotal(double subTotal) {
        this.subTotal = subTotal;
        return this;
    }

    public int getPromotionId() {
        return promotionId;
    }

    public Booking setPromotionId(int promotionId) {
        this.promotionId = promotionId;
        return this;
    }

    public double getNetTotal() {
        return netTotal;
    }

    public Booking setNetTotal(double netTotal) {
        this.netTotal = netTotal;
        return this;
    }
}
