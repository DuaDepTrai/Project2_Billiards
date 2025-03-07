package src.billiardsmanagement.model;

public class Customer {
    private int customerId;
    private String name;
    private String phone;
    private double totalPlaytime;

    // Constructor
    public Customer() {}

    public Customer(String name, String phone) {
        this.name = name;
        this.phone = phone;
        this.totalPlaytime = totalPlaytime;
    }

    public Customer(String name, String phone, double totalPlaytime) {
        this.name = name;
        this.phone = phone;
        this.totalPlaytime = totalPlaytime;
    }

    // Getters and setters
    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public double getTotalPlaytime() {
        return totalPlaytime;
    }

    public void setTotalPlaytime(double totalPlaytime) {
        this.totalPlaytime = totalPlaytime;
    }
}