package src.billiardsmanagement.model;

import java.text.NumberFormat;
import java.util.Locale;

public class Product {
    private int id;
    private String name;
    private String category;
    private double price;
    private String unit;
    private int quantity;

    public Product(int id, String name, String category, double price, String unit, int quantity) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.unit = unit;
        this.quantity = quantity;
    }

    // Getter và Setter
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    public String getUnit() {
        return unit;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getFormattedPrice() {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(price) + "đ";
    }

}
