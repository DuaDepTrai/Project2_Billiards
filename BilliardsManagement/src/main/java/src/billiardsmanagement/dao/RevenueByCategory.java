package src.billiardsmanagement.dao;

public class RevenueByCategory {
    private String category;
    private double revenue;

    public RevenueByCategory(String category, double revenue) {
        this.category = category;
        this.revenue = revenue;
    }

    public String getCategory() {
        return category;
    }

    public double getRevenue() {
        return revenue;
    }
}
