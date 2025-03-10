package src.billiardsmanagement.model;

public class OrderItem {
    private int orderItemId;
    private int orderId;
    private int productId;
    private String productName; // products.name
    private double productPrice;
    private int quantity;
    private double total;
//    private double subTotal;
//    private int promotionId;
//    private String promotionName;
//    private double promotionDiscount;

    public OrderItem(){}

    public OrderItem(int orderId, int productId, int quantity, double total) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.total = total;
//        this.subTotal = subTotal;
//        this.promotionId = promotionId; // nếu = -1 : không có promotion
    }

    public OrderItem(int orderItemId, int orderId, int productId, String productName, int quantity, double total) {
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.total = total;
//        this.subTotal = subTotal;
//        this.promotionId = promotionId;
    } // Full-model constructor


    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
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

    public double getTotal() {
        return total;
    }

    public OrderItem setTotal(double total) {
        this.total = total;
        return this;
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "orderItemId=" + orderItemId +
                ", orderId=" + orderId +
                ", productId=" + productId +
                ", productName='" + productName + '\'' +
                ", productPrice=" + productPrice +
                ", quantity=" + quantity +
                ", total=" + total +
                '}';
    }

//    public double getSubTotal() {
//        return subTotal;
//    }
//
//    public OrderItem setSubTotal(double subTotal) {
//        this.subTotal = subTotal;
//        return this;
//    }
//
//    public int getPromotionId() {
//        return promotionId;
//    }
//
//    public OrderItem setPromotionId(int promotionId) {
//        this.promotionId = promotionId;
//        return this;
//    }
//
//    public String getPromotionName() {
//        return promotionName;
//    }
//
//    public OrderItem setPromotionName(String promotionName) {
//        this.promotionName = promotionName;
//        return this;
//    }
//
//    public double getPromotionDiscount() {
//        return promotionDiscount;
//    }
//
//    public OrderItem setPromotionDiscount(double promotionDiscount) {
//        this.promotionDiscount = promotionDiscount;
//        return this;
//    }
}
