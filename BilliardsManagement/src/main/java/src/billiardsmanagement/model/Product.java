package src.billiardsmanagement.model;

public class Product {
    private int id;
    private String name;
    private int quantity;
    private double price;
    private String unit;
    private String category; // ✅ Thêm category


    public Product(int id, String name, String category, int quantity, double price, String unit) {
        this.id = id;
        this.name = name;
        this.category = category; // ✅ Lưu category name
        this.quantity = quantity;
        this.price = price;
        this.unit = unit;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; } // ✅ Thêm getter cho category
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public String getUnit() { return unit; }
}
