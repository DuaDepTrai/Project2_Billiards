package src.billiardsmanagement.dao;

public class RevenueByDate {
    private String period;
    private double revenue;

    public RevenueByDate(String period, double revenue) {
        this.period = period;
        this.revenue = revenue;
    }

    public String getPeriod() {
        return period;
    }

    public double getRevenue() {
        return revenue;
    }
}
