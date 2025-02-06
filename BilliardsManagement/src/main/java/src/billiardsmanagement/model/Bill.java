package src.billiardsmanagement.model;

public class Bill {
    private String customerName;
    private String customerPhone;
    private double totalCost;

    public Bill(String customerName, String customerPhone, double totalCost) {
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.totalCost = totalCost;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }
}
