package src.billiardsmanagement.model;

public class CatePooltable {
    private int id;
    private String name;
    private double price;
    private String shortName;

    public CatePooltable(int id, String name, String shortName, double price) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.price = price;
    }

    public CatePooltable(int categoryId, String name, double price) {
        this.id = categoryId;
        this.name = name;
        this.price = price; 
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getShortName() { return this.shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }
}