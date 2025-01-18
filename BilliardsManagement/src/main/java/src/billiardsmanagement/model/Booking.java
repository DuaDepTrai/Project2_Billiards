package src.billiardsmanagement.model;

import java.sql.Timestamp;
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


    public Booking(int orderId, int tableId, Timestamp timeStamp, String bookingStatus) {
        this.orderId = orderId;
        this.tableId = tableId;
        this.startTime = timeStamp.toLocalDateTime();
        this.bookingStatus = bookingStatus;
    }

    public Booking(int bookingId, int orderId, int tableId, String tableName, LocalDateTime localDateTime, LocalDateTime localDateTime1, double timeplay, double netTotal, double subtotal, String bookingStatus, int promotionId) {
        this.bookingId = bookingId;
        this.orderId = orderId;
        this.tableId = tableId;
        this.tableName = tableName;
        this.startTime = localDateTime;
        this.endTime = localDateTime1;
        this.timeplay = timeplay;
        this.netTotal = netTotal;
        this.subTotal = subtotal;
        this.bookingStatus = bookingStatus;
        this.promotionId = promotionId;

    }


    public String getTableName() { return tableName; }
    public Timestamp getStartTime() { return Timestamp.valueOf(startTime); }
    public LocalDateTime getEndTime() { return endTime; }
    public double getTimeplay() { return timeplay; }
    public double getSubTotal() { return subTotal; }
    public double getNetTotal() { return netTotal; }
    public String getBookingStatus() { return bookingStatus; }

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


    public Booking setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
        return this;
    }

    public Booking setTimeplay(double timeplay) {
        this.timeplay = timeplay;
        return this;
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


    public Booking setNetTotal(double netTotal) {
        this.netTotal = netTotal;
        return this;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "bookingId=" + bookingId +
                ", tableId=" + tableId +
                ", orderId=" + orderId +
                ", bookingStatus='" + bookingStatus + '\'' +
                ", tableName='" + tableName + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", timeplay=" + timeplay +
                ", subTotal=" + subTotal +
                ", promotionId=" + promotionId +
                ", netTotal=" + netTotal +
                '}';
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public String getTableStatus() {
        return bookingStatus;
    }
}
