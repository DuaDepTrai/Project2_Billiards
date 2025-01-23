package src.billiardsmanagement.model;

public class OrderItem {
    private int orderItemId;
    private int orderId;
    private int productId;
    private String productName; // products.name
    private double productPrice;
    private int quantity;
    private double netTotal;
    private double subTotal;
    private int promotionId;
    private double promotionDiscount;

    public OrderItem(int orderId, int productId, int quantity, double netTotal, double subTotal, int promotionId) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.netTotal = netTotal;
        this.subTotal = subTotal;
        this.promotionId = promotionId; // nếu = -1 : không có promotion
    }

    public OrderItem(int orderItemId, int orderId, int productId, String productName, int quantity, double netTotal, double subTotal, int promotionId) {
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.netTotal = netTotal;
        this.subTotal = subTotal;
        this.promotionId = promotionId;
    } // Full-model constructor

    public OrderItem() {
        //TODO Auto-generated constructor stub
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    @Override
    public String toString() {
        return orderItemId + " " +orderId + " "+ productId + " "+ productName;
    }

    public int getOrderItemId() {
        return orderItemId;
    }

    public String getProductName() {
        return productName;
    }

    public OrderItem setProductName(String productName) {
        this.productName = productName;
        return this;
    }

    public OrderItem setOrderItemId(int orderItemId) {
        this.orderItemId = orderItemId;
        return this;
    }

    public int getOrderId() {
        return orderId;
    }

    public OrderItem setOrderId(int orderId) {
        this.orderId = orderId;
        return this;
    }

    public int getProductId() {
        return productId;
    }

    public OrderItem setProductId(int productId) {
        this.productId = productId;
        return this;
    }

    public int getQuantity() {
        return quantity;
    }

    public OrderItem setQuantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    public double getNetTotal() {
        return netTotal;
    }

    public OrderItem setNetTotal(double netTotal) {
        this.netTotal = netTotal;
        return this;
    }

    public double getSubTotal() {
        return subTotal;
    }

    public OrderItem setSubTotal(double subTotal) {
        this.subTotal = subTotal;
        return this;
    }

    public int getPromotionId() {
        return promotionId;
    }

    public OrderItem setPromotionId(int promotionId) {
        this.promotionId = promotionId;
        return this;
    }

    public double getPromotionDiscount() {
        return promotionDiscount;
    }

    public OrderItem setPromotionDiscount(double promotionDiscount) {
        this.promotionDiscount = promotionDiscount;
        return this;
    }
}
