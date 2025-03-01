public class Order {
    // Giữ nguyên các thuộc tính hiện có
    private int orderId;
    private int customerId;
    private int userId;
    private double totalCost;
    private Date orderDate;
    private String orderStatus;
    
    // Thêm các thuộc tính mới để hiển thị (không lưu trong database)
    private String customerName;    // Tên khách hàng
    private String customerPhone;   // Số điện thoại
    private String tableName;       // Tên bàn

    // Constructor mặc định
    public Order() {
    }

    // Constructor với các thuộc tính cơ bản
    public Order(int orderId, int customerId, int userId, double totalCost, Date orderDate, String orderStatus) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.userId = userId;
        this.totalCost = totalCost;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
    }

    // Getters và Setters cho các thuộc tính cơ bản
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    // Getters và Setters cho các thuộc tính hiển thị bổ sung
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }
} 