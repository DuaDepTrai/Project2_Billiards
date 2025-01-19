package src.billiardsmanagement.model;

import java.time.LocalDateTime;

/**
 * 
 */
public class RentCue {
    private int rentCueId;
    private int orderId;
    private int productId;
    private String productName;
    private double productPrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double timeplay;
    private double netTotal;
    private double subTotal;
    private int promotionId;
    private String promotionName;
    private double promotionDiscount;
    private int quantity;
    private RentCueStatus status;

    // Default constructor
    public RentCue() {
    }

    // Full constructor
    public RentCue(int rentCueId, int orderId, int productId, String productName, 
                   LocalDateTime startTime, LocalDateTime endTime, double timeplay, 
                   double netTotal, double subTotal, int promotionId) {
        this.rentCueId = rentCueId;
        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.timeplay = timeplay;
        this.netTotal = netTotal;
        this.subTotal = subTotal;
        this.promotionId = promotionId;
    }

    // Getters and Setters
    public int getRentCueId() {
        return rentCueId;
    }

    public void setRentCueId(int rentCueId) {
        this.rentCueId = rentCueId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public double getTimeplay() {
        return timeplay;
    }

    public void setTimeplay(double timeplay) {
        this.timeplay = timeplay;
    }

    public double getNetTotal() {
        return netTotal;
    }

    public void setNetTotal(double netTotal) {
        this.netTotal = netTotal;
    }

    public double getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(double subTotal) {
        this.subTotal = subTotal;
    }

    public int getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(int promotionId) {
        this.promotionId = promotionId;
    }

    public String getPromotionName() {
        return promotionName;
    }

    public void setPromotionName(String promotionName) {
        this.promotionName = promotionName;
    }

    public double getPromotionDiscount() {
        return promotionDiscount;
    }

    public void setPromotionDiscount(double promotionDiscount) {
        this.promotionDiscount = promotionDiscount;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public RentCueStatus getStatus() {
        return status;
    }

    public void setStatus(RentCueStatus status) {
        this.status = status;
    }
}