package src.billiardsmanagement.model;

public class Customer {
    private int customerId; // customer_id
    private String name;     // name
    private String phone;    // phone
    private double totalPlaytime; // total_playtime

    public Customer(){

    }
    // Constructor
    public Customer(int customerId, String name, String phone, double totalPlaytime) {
        this.customerId = customerId;
        this.name = name;
        this.phone = phone;
        this.totalPlaytime = totalPlaytime;
    }
    public Customer (String name, String phone){
        this.name = name;
        this.phone = phone;
    }
    // Getter and Setter methods
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

    // Override toString() method if you want to display Customer's information as a string
    @Override
    public String toString() {
        return customerId + " - " + name; // Display ID and Name in ComboBox or list
    }
}
