package src.billiardsmanagement.model;

import java.time.LocalDate;

public class Revenue {
    private int revenueId;
    private LocalDate date;
    private double total_revenue;
    private int total_customers;
    private int total_orders;
    private String description; // Thêm mô tả

    public Revenue(LocalDate date, int i, int totalRevenue, int totalOrders, String description) {
    }

    // Thêm getter và setter cho description
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Revenue(int revenueId, LocalDate date, double total_revenue, int total_orders, int total_customers) {
        this.revenueId = revenueId;
        this.date = date;
        this.total_revenue = total_revenue;
        this.total_orders = total_orders;
        this.total_customers = total_customers;
        this.description = description;
    }

    public int getRevenueId() {
        return revenueId;
    }

    public void setRevenueId(int revenueId) {
        this.revenueId = revenueId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getTotal_revenue() {
        return total_revenue;
    }

    public void setTotal_revenue(double total_revenue) {
        this.total_revenue = total_revenue;
    }

    public int getTotal_customers() {
        return total_customers;
    }

    public void setTotal_customers(int total_customers) {
        this.total_customers = total_customers;
    }

    public int getTotal_orders() {
        return total_orders;
    }

    public void setTotal_orders(int total_orders) {
        this.total_orders = total_orders;
    }

    @Override
    public String toString() {
        return "Revenue{" +
                "revenueId=" + revenueId +
                ", date=" + date +
                ", total_revenue=" + total_revenue +
                ", total_customers=" + total_customers +
                ", total_orders=" + total_orders +
                '}';
    }
}
