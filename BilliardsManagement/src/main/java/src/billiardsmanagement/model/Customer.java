package src.billiardsmanagement.model;

import java.util.Date;

public class Customer {
    private int customerId;
    private String name;
    private String phone;
    private double totalPlaytime;
    private String address;
    private Date birthday;

    // Constructor
    public Customer() {}

    public Customer(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public Customer(String name, String phone, double totalPlaytime, String address, Date birthday) {
        this.name = name;
        this.phone = phone;
        this.totalPlaytime = totalPlaytime;
        this.address = address;
        this.birthday = birthday;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }
}