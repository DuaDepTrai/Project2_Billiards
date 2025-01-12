package src.billiardsmanagement.model;

public class Order {
    private int orderId;
    private int customerId;
    private String customerName; // Để hiển thị tên khách hàng từ bảng `customers`
    private double totalCost;
    private String orderStatus;

    // Constructor
    public Order(int customerId,double totalCost,String orderStatus){
        this.customerId = customerId;
        this.totalCost = totalCost;
        this.orderStatus = orderStatus;
    }
    public Order(int orderId, int customerId, String customerName, double totalCost, String orderStatus) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.totalCost = totalCost;
        this.orderStatus = orderStatus;
        this.orderId = orderId;
    }

    public Order(double totalCost, String orderStatus) {
        this.totalCost = totalCost;
        this.orderStatus = orderStatus;
    }

    // Getters and Setters
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

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", customerId=" + customerId +
                ", customerName='" + customerName + '\'' +
                ", totalCost=" + totalCost +
                ", orderStatus='" + orderStatus + '\'' +
                '}';
    }
}