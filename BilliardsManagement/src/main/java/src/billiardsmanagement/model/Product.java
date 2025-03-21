package src.billiardsmanagement.model;

public class Product {
    private int id;
    private String name;
    private int quantity;
    private double price;
    private String unit;
    private int categoryId; // ✅ Thêm categoryId
    private String category; // ✅ Thêm category

    public Product(int id, String name, int categoryId, String category, int quantity, double price, String unit) {
        this.id = id;
        this.name = name;
        this.categoryId = categoryId; // ✅ Lưu categoryId
        this.category = category; // ✅ Lưu category name
        this.quantity = quantity;
        this.price = price;
        this.unit = unit;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getCategoryId() { return categoryId; } // ✅ Getter mới
    public String getCategory() { return category; } // ✅ Thêm getter cho category
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public String getUnit() { return unit; }
}
