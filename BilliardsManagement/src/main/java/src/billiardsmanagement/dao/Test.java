package src.billiardsmanagement.dao;

import java.util.List;

public class Test {
    public static void main(String[] args) {
        String fromDate = "2025-01-01";
        String toDate = "2025-12-31";

        // 1. Kiểm tra tổng doanh thu
        RevenueSummary summary = RevenueDAO.getTotalRevenue(fromDate, toDate);
        System.out.println("Tổng đơn hàng: " + summary.getTotalOrders());
        System.out.println("Tổng doanh thu: " + summary.getTotalRevenue());

        // 2. Kiểm tra doanh thu theo ngày
        List<RevenueByDate> revenueByDate = RevenueDAO.getRevenueByDate("month", fromDate, toDate);
        for (RevenueByDate r : revenueByDate) {
            System.out.println("Tháng: " + r.getPeriod() + " - Doanh thu: " + r.getRevenue());
        }

        // 3. Kiểm tra doanh thu theo nhóm bàn
        List<RevenueByCategory> revenueByTable = RevenueDAO.getRevenueByTableGroup(fromDate, toDate);
        for (RevenueByCategory r : revenueByTable) {
            System.out.println("Bàn: " + r.getCategory() + " - Doanh thu: " + r.getRevenue());
        }
    }
}
