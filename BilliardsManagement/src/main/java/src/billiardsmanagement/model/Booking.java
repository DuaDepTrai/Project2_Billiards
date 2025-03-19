package src.billiardsmanagement.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Booking {
    private int bookingId;
    private int tableId;
    private int orderId;
    private String bookingStatus;
    private String tableName; // [PoolTables.tableName]
    private double priceTable; // [PoolTables.price]
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double timeplay;
    private double total;

    public Booking() {}

    // Constructor for table data display
    public Booking(String tableName, double priceTable, LocalDateTime startTime, LocalDateTime endTime, double timeplay, double total, String bookingStatus) {
        this.tableName = tableName;
        this.priceTable = priceTable;
        this.startTime = startTime;
        this.endTime = endTime;
        this.timeplay = timeplay;
        this.total = total;
        this.bookingStatus = bookingStatus;
    }

    // Constructor for minimal booking details
    public Booking(int orderId, int tableId, Timestamp startTime, String bookingStatus) {
        this.orderId = orderId;
        this.tableId = tableId;
        this.startTime = startTime.toLocalDateTime();
        this.bookingStatus = bookingStatus;
    }

    // Full constructor for DAO usage
    public Booking(int bookingId, int orderId, int tableId, String tableName, double priceTable, LocalDateTime startTime, LocalDateTime endTime, double timeplay, double total, String bookingStatus) {
        this.bookingId = bookingId;
        this.orderId = orderId;
        this.tableId = tableId;
        this.tableName = tableName;
        this.priceTable = priceTable;
        this.startTime = startTime;
        this.endTime = endTime;
        this.timeplay = timeplay;
        this.total = total;
        this.bookingStatus = bookingStatus;
    }

    public Booking(int bookingId, int orderId, int tableId, LocalDateTime startTime, LocalDateTime localDateTime, double timeplay, double total, String bookingStatus, String tableName) {
        this.bookingId = bookingId;
        this.orderId = orderId;
        this.tableId = tableId;
        this.startTime = startTime;
        this.endTime = localDateTime;
        this.timeplay = timeplay;
        this.total = total;
        this.bookingStatus = bookingStatus;
        this.tableName = tableName;
    }

    // Getters and Setters
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

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public double getPriceTable() {
        return priceTable;
    }

    public void setPriceTable(double priceTable) {
        this.priceTable = priceTable;
    }

    public Timestamp getStartTime() {
        return startTime != null ? Timestamp.valueOf(startTime) : null;
    }

    public LocalDateTime getStartTimeBooking(){
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

    public double getTotal() {
        return total;
    }

    public Booking getTotal(double total) {
        this.total = total;
        return this;
    }

//    public int getPromotionId() {
//        return promotionId;
//    }

//    public Booking setPromotionId(int promotionId) {
//        this.promotionId = promotionId;
//        return this;
//    }
//
//    public double getNetTotal() {
//        return total;
//    }
//
//    public Booking setNetTotal(double netTotal) {
//        this.netTotal = netTotal;
//        return this;
//    }

    @Override
    public String toString() {
        return "Booking{" +
                "bookingId=" + bookingId +
                ", tableId=" + tableId +
                ", orderId=" + orderId +
                ", bookingStatus='" + bookingStatus + '\'' +
                ", tableName='" + tableName + '\'' +
                ", priceTable=" + priceTable +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", timeplay=" + timeplay +
                ", total=" + total +
                '}';
    }

    public String getTableStatus() {
        return bookingStatus;
    }

    public String getUnit() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUnit'");
    }


    public Booking setTotal(double total) {
        this.total = total;
        return this;
    }
}
