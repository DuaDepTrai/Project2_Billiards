package src.billiardsmanagement.dao;

public class RevenueSummary {
    private int totalOrders;
    private double totalRevenue;

    public RevenueSummary(int totalOrders, double totalRevenue) {
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }
}
