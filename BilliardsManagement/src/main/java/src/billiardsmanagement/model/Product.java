package src.billiardsmanagement.model;

public class Product {
    private int id;
    private String name;
    private int quantity;
    private double price;
    private String unit;

    public Product(int id, String name, int quantity, double price, String unit) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.unit = unit;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public String getUnit() { return unit; }
}
