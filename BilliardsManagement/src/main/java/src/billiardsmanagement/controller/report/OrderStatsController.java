package src.billiardsmanagement.controller.report;

import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.IntStream;
import javafx.scene.layout.HBox;
import src.billiardsmanagement.model.DatabaseConnection;
import javafx.beans.property.SimpleStringProperty;

public class OrderStatsController {
    @FXML
    private DatePicker startDatePicker, endDatePicker,datePicker;
    @FXML
    private ComboBox<Integer> monthComboBox, yearComboBox, yearOnlyComboBox;
    @FXML
    private ComboBox<String> filterTypeComboBox;
    @FXML
    private HBox dateRangeBox, monthBox, yearBox,dateBox;
    @FXML
    private Label totalRevenueLabel, totalOrdersLabel;
    @FXML
    private TableView<Map<String, Object>> revenueTable;
    @FXML
    private TableColumn<Map<String, Object>, String> colCategory;
    @FXML
    private BarChart<String, Number> revenueBarChart;
    @FXML
    private PieChart tablePieChart, productPieChart;

    private final ObservableList<Map<String, Object>> revenueData = FXCollections.observableArrayList();
    private List<String> dateColumns = new ArrayList<>();

    public void initialize() {
        revenueTable.getColumns().clear();
        // Cấu hình cột danh mục
        colCategory.setCellValueFactory(data -> new SimpleStringProperty((String) data.getValue().get("category")));
        revenueTable.setItems(revenueData);

        // Thêm tùy chọn cho ComboBox lọc
        filterTypeComboBox.setItems(FXCollections.observableArrayList("Date","Date Range", "Month", "Year"));
        filterTypeComboBox.setValue("Date"); // Mặc định là Date Range
        filterTypeComboBox.setOnAction(e -> updateFilterType());

        // Thiết lập giá trị cho ComboBox tháng & năm
        monthComboBox.setItems(FXCollections.observableArrayList(IntStream.rangeClosed(1, 12).boxed().toList()));
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();
        yearComboBox.setItems(FXCollections.observableArrayList(IntStream.rangeClosed(2020, currentYear).boxed().toList()));
        yearOnlyComboBox.setItems(yearComboBox.getItems());

        // Set default values for month and year ComboBoxes
        monthComboBox.setValue(currentMonth);
        yearComboBox.setValue(currentYear);
        yearOnlyComboBox.setValue(currentYear);

        // Thiết lập giá trị mặc định cho DatePicker
        LocalDate today = LocalDate.now();
        datePicker.setValue(today);
        startDatePicker.setValue(today);
        endDatePicker.setValue(today);

        // Tự động lọc doanh thu khi thay đổi ngày
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> filterRevenue());
        startDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> filterRevenue());
        endDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> filterRevenue());

        // Tự động lọc khi thay đổi tháng hoặc năm
        monthComboBox.valueProperty().addListener((obs, oldMonth, newMonth) -> filterRevenue());
        yearComboBox.valueProperty().addListener((obs, oldYear, newYear) -> filterRevenue());
        yearOnlyComboBox.valueProperty().addListener((obs, oldYear, newYear) -> filterRevenue());

        updateFilterType(); // Cập nhật giao diện lọc ban đầu
    }


    @FXML
    private void updateFilterType() {
        String selectedFilter = filterTypeComboBox.getValue();
        dateBox.setVisible("Date".equals(selectedFilter));
        dateBox.setManaged("Date".equals(selectedFilter));

        dateRangeBox.setVisible("Date Range".equals(selectedFilter));
        dateRangeBox.setManaged("Date Range".equals(selectedFilter));

        monthBox.setVisible("Month".equals(selectedFilter));
        monthBox.setManaged("Month".equals(selectedFilter));

        yearBox.setVisible("Year".equals(selectedFilter));
        yearBox.setManaged("Year".equals(selectedFilter));

        // Trigger filter when changing filter type
        filterRevenue();
    }

    @FXML
    private void filterRevenue() {
        // Always completely clear the table data and columns
        revenueData.clear();
        revenueTable.getItems().clear(); // Explicitly clear table items
        revenueTable.getColumns().clear();
        dateColumns.clear();

        // Re-add the category column first
        revenueTable.getColumns().add(colCategory);

        System.out.println("Filtering revenue data...");
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Lấy danh sách các ngày trong khoảng thời gian đã chọn
            List<String> dates = fetchDatesInRange(conn);
            // Định dạng hiển thị ngày
            DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd/MM");
            for (String date : dates) {
                try{
                    dateColumns.add(date);
                    LocalDate localDate = LocalDate.parse(date);
                    String displayDate = localDate.format(displayFormatter);
                    System.out.println("Ngày đã phân tích: " + date + " -> " + displayDate);

                    TableColumn<Map<String, Object>, String> dateColumn = new TableColumn<>(displayDate);
                    System.out.println(dateColumn);

                    dateColumn.setCellValueFactory(data -> {
                        Double value = (Double) data.getValue().get(date);
                        return new SimpleStringProperty(value != null ?
                                NumberFormat.getIntegerInstance(new Locale("vi", "VN")).format(value) : "0");
                    });
                    revenueTable.getColumns().add(dateColumn);
                }catch (Exception e) {
                    System.err.println("Lỗi khi phân tích ngày: " + date);
                    e.printStackTrace();
                }
            }

            // Lấy dữ liệu doanh thu theo danh mục và ngày
            Map<String, Map<String, Double>> categoryRevenueByDate = fetchCategoryRevenueByDate(conn, dates);

            // Tạo dữ liệu mới cho bảng
            ObservableList<Map<String, Object>> newData = FXCollections.observableArrayList();
            for (Map.Entry<String, Map<String, Double>> entry : categoryRevenueByDate.entrySet()) {
                Map<String, Object> row = new HashMap<>();
                row.put("category", entry.getKey());

                // Thêm doanh thu cho mỗi ngày
                for (String date : dates) {
                    Double revenue = entry.getValue().getOrDefault(date, 0.0);
                    row.put(date, revenue);
                }

                newData.add(row);
            }

            // Set the table items with the new data
            revenueTable.setItems(newData);
            revenueData.setAll(newData); // Update the revenueData collection

            // Tính tổng doanh thu và số lượng đơn hàng
            double totalRevenue = 0;
            int totalOrders = 0;

            String sql = buildTotalQueryBasedOnFilter();
            PreparedStatement stmt = prepareStatementWithFilterParams(conn, sql);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                totalRevenue = rs.getDouble("total_revenue");
                totalOrders = rs.getInt("total_orders");
            }

            // Định dạng số không có chữ số thập phân
            NumberFormat numberFormat = NumberFormat.getInstance(new Locale("en", "US"));
            numberFormat.setMaximumFractionDigits(0);
            numberFormat.setGroupingUsed(true);

            totalRevenueLabel.setText(numberFormat.format(totalRevenue) + " VNĐ");
            totalOrdersLabel.setText(String.valueOf(totalOrders));

            // Clear and reload charts
            revenueBarChart.getData().clear();
            tablePieChart.getData().clear();
            productPieChart.getData().clear();

            loadCharts(); // Cập nhật biểu đồ

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<String> fetchDatesInRange(Connection conn) throws SQLException {
        List<String> dates = new ArrayList<>();
        String sql = "";
        PreparedStatement stmt;
        String selectedFilter = filterTypeComboBox.getValue();

        if("Date".equals(selectedFilter)) {
            // For Date filter, only return the selected date
            // No need to query the database for dates
            dates.add(datePicker.getValue().toString());
            System.out.println("Date filter: Adding only selected date: " + datePicker.getValue());
            return dates;
        }
        else if ("Date Range".equals(selectedFilter)) {
            if (startDatePicker.getValue().isEqual(endDatePicker.getValue())) {
                // Single date case
                dates.add(startDatePicker.getValue().toString());
                System.out.println("Date Range (single date): Adding only selected date: " + startDatePicker.getValue());
                return dates;
            } else {
                // Date range case
                sql = "SELECT DISTINCT DATE(order_date) AS date FROM orders " +
                        "WHERE order_status = 'Paid' AND DATE(order_date) >= DATE(?) AND DATE(order_date) <= DATE(?) ORDER BY date";

                stmt = conn.prepareStatement(sql);
                stmt.setString(1, startDatePicker.getValue().toString());
                stmt.setString(2, endDatePicker.getValue().toString());
            }
        } else if ("Month".equals(selectedFilter)) {
            sql = "SELECT DISTINCT DATE(order_date) AS date FROM orders " +
                    "WHERE order_status = 'Paid' AND MONTH(order_date) = ? AND YEAR(order_date) = ? ORDER BY date";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, monthComboBox.getValue());
            stmt.setInt(2, yearComboBox.getValue());
        } else { // Lọc theo Năm
            sql = "SELECT DISTINCT DATE(order_date) AS date FROM orders " +
                    "WHERE order_status = 'Paid' AND YEAR(order_date) = ? ORDER BY date";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, yearOnlyComboBox.getValue());
        }

        // Only execute SQL query for non-single date filters
        if (!"Date".equals(selectedFilter) &&
                !("Date Range".equals(selectedFilter) && startDatePicker.getValue().isEqual(endDatePicker.getValue()))) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                dates.add(rs.getString("date"));
            }
        }

        return dates;
    }

    private Map<String, Map<String, Double>> fetchCategoryRevenueByDate(Connection conn, List<String> dates) throws SQLException {
        Map<String, Map<String, Double>> result = new HashMap<>();

        // Lấy doanh thu theo danh mục sản phẩm
        String productSql = buildProductCategoryQueryBasedOnFilter();
        PreparedStatement productStmt = prepareStatementWithFilterParams(conn, productSql);
        ResultSet productRs = productStmt.executeQuery();

        while (productRs.next()) {
            String category = productRs.getString("category_name");
            String date = productRs.getString("date");
            double revenue = productRs.getDouble("revenue");

            result.computeIfAbsent(category, k -> new HashMap<>())
                    .put(date, revenue);
        }

        // Lấy doanh thu theo loại bàn
        String tableSql = buildTableCategoryQueryBasedOnFilter();
        PreparedStatement tableStmt = prepareStatementWithFilterParams(conn, tableSql);
        ResultSet tableRs = tableStmt.executeQuery();

        while (tableRs.next()) {
            String category = tableRs.getString("table_type");
            String date = tableRs.getString("date");
            double revenue = tableRs.getDouble("revenue");

            result.computeIfAbsent(category, k -> new HashMap<>())
                    .put(date, revenue);
        }

        return result;
    }

    private String buildProductCategoryQueryBasedOnFilter() {
        String selectedFilter = filterTypeComboBox.getValue();

        if ("Date".equals(selectedFilter)) {
            return "SELECT c.category_name, DATE(o.order_date) AS date, SUM(oi.total) AS revenue " +
                    "FROM orders_items oi " +
                    "JOIN products p ON oi.product_id = p.product_id " +
                    "JOIN category c ON p.category_id = c.category_id " +
                    "JOIN orders o ON oi.order_id = o.order_id " +
                    "WHERE o.order_status = 'Paid' AND DATE(o.order_date) = ? " +
                    "GROUP BY c.category_name, DATE(o.order_date)";
        } else if ("Date Range".equals(selectedFilter)) {
            if (startDatePicker.getValue().isEqual(endDatePicker.getValue())) {
                // Single date case
                return "SELECT c.category_name, DATE(o.order_date) AS date, SUM(oi.total) AS revenue " +
                        "FROM orders_items oi " +
                        "JOIN products p ON oi.product_id = p.product_id " +
                        "JOIN category c ON p.category_id = c.category_id " +
                        "JOIN orders o ON oi.order_id = o.order_id " +
                        "WHERE o.order_status = 'Paid' AND DATE(o.order_date) = ? " +
                        "GROUP BY c.category_name, DATE(o.order_date)";
            } else {
                // Date range case
                return "SELECT c.category_name, DATE(o.order_date) AS date, SUM(oi.total) AS revenue " +
                        "FROM orders_items oi " +
                        "JOIN products p ON oi.product_id = p.product_id " +
                        "JOIN category c ON p.category_id = c.category_id " +
                        "JOIN orders o ON oi.order_id = o.order_id " +
                        "WHERE o.order_status = 'Paid' AND o.order_date BETWEEN ? AND ? " +
                        "GROUP BY c.category_name, DATE(o.order_date)";
            }
        } else if ("Month".equals(selectedFilter)) {
            return "SELECT c.category_name, DATE(o.order_date) AS date, SUM(oi.total) AS revenue " +
                    "FROM orders_items oi " +
                    "JOIN products p ON oi.product_id = p.product_id " +
                    "JOIN category c ON p.category_id = c.category_id " +
                    "JOIN orders o ON oi.order_id = o.order_id " +
                    "WHERE o.order_status = 'Paid' AND MONTH(o.order_date) = ? AND YEAR(o.order_date) = ? " +
                    "GROUP BY c.category_name, DATE(o.order_date)";
        } else { // Lọc theo Năm
            return "SELECT c.category_name, DATE(o.order_date) AS date, SUM(oi.total) AS revenue " +
                    "FROM orders_items oi " +
                    "JOIN products p ON oi.product_id = p.product_id " +
                    "JOIN category c ON p.category_id = c.category_id " +
                    "JOIN orders o ON oi.order_id = o.order_id " +
                    "WHERE o.order_status = 'Paid' AND YEAR(o.order_date) = ? " +
                    "GROUP BY c.category_name, DATE(o.order_date)";
        }
    }

    private String buildTableCategoryQueryBasedOnFilter() {
        String selectedFilter = filterTypeComboBox.getValue();

        if ("Date".equals(selectedFilter)) {
            return "SELECT cp.name AS table_type, DATE(o.order_date) AS date, SUM(b.total) AS revenue " +
                    "FROM bookings b " +
                    "JOIN pooltables pt ON b.table_id = pt.table_id " +
                    "JOIN cate_pooltables cp ON pt.cate_id = cp.id " +
                    "JOIN orders o ON b.order_id = o.order_id " +
                    "WHERE o.order_status = 'Paid' AND DATE(o.order_date) = ? " +
                    "GROUP BY cp.name, DATE(o.order_date)";
        } else if ("Date Range".equals(selectedFilter)) {
            if (startDatePicker.getValue().isEqual(endDatePicker.getValue())) {
                // Single date case
                return "SELECT cp.name AS table_type, DATE(o.order_date) AS date, SUM(b.total) AS revenue " +
                        "FROM bookings b " +
                        "JOIN pooltables pt ON b.table_id = pt.table_id " +
                        "JOIN cate_pooltables cp ON pt.cate_id = cp.id " +
                        "JOIN orders o ON b.order_id = o.order_id " +
                        "WHERE o.order_status = 'Paid' AND DATE(o.order_date) = ? " +
                        "GROUP BY cp.name, DATE(o.order_date)";
            } else {
                // Date range case
                return "SELECT cp.name AS table_type, DATE(o.order_date) AS date, SUM(b.total) AS revenue " +
                        "FROM bookings b " +
                        "JOIN pooltables pt ON b.table_id = pt.table_id " +
                        "JOIN cate_pooltables cp ON pt.cate_id = cp.id " +
                        "JOIN orders o ON b.order_id = o.order_id " +
                        "WHERE o.order_status = 'Paid' AND o.order_date BETWEEN ? AND ? " +
                        "GROUP BY cp.name, DATE(o.order_date)";
            }
        } else if ("Month".equals(selectedFilter)) {
            return "SELECT cp.name AS table_type, DATE(o.order_date) AS date, SUM(b.total) AS revenue " +
                    "FROM bookings b " +
                    "JOIN pooltables pt ON b.table_id = pt.table_id " +
                    "JOIN cate_pooltables cp ON pt.cate_id = cp.id " +
                    "JOIN orders o ON b.order_id = o.order_id " +
                    "WHERE o.order_status = 'Paid' AND MONTH(o.order_date) = ? AND YEAR(o.order_date) = ? " +
                    "GROUP BY cp.name";
        } else { // Lọc theo Năm
            return "SELECT cp.name AS table_type, DATE(o.order_date) AS date, SUM(b.total) AS revenue " +
                    "FROM bookings b " +
                    "JOIN pooltables pt ON b.table_id = pt.table_id " +
                    "JOIN cate_pooltables cp ON pt.cate_id = cp.id " +
                    "JOIN orders o ON b.order_id = o.order_id " +
                    "WHERE o.order_status = 'Paid' AND YEAR(o.order_date) = ? " +
                    "GROUP BY cp.name";
        }
    }

    private String buildTotalQueryBasedOnFilter() {
        String selectedFilter = filterTypeComboBox.getValue();

        if ("Date".equals(selectedFilter)) {
            return "SELECT COUNT(*) AS total_orders, SUM(total_cost) AS total_revenue " +
                    "FROM orders WHERE order_status = 'Paid' AND DATE(order_date) = ?";
        } else if ("Date Range".equals(selectedFilter)) {
            if (startDatePicker.getValue().isEqual(endDatePicker.getValue())) {
                // Single date case
                return "SELECT COUNT(*) AS total_orders, SUM(total_cost) AS total_revenue " +
                        "FROM orders WHERE order_status = 'Paid' AND DATE(order_date) = ?";
            } else {
                // Date range case
                return "SELECT COUNT(*) AS total_orders, SUM(total_cost) AS total_revenue " +
                        "FROM orders WHERE order_status = 'Paid' AND order_date BETWEEN ? AND ?";
            }
        } else if ("Month".equals(selectedFilter)) {
            return "SELECT COUNT(*) AS total_orders, SUM(total_cost) AS total_revenue " +
                    "FROM orders WHERE order_status = 'Paid' AND MONTH(order_date) = ? AND YEAR(order_date) = ?";
        } else { // Lọc theo Năm
            return "SELECT COUNT(*) AS total_orders, SUM(total_cost) AS total_revenue " +
                    "FROM orders WHERE order_status = 'Paid' AND YEAR(order_date) = ?";
        }
    }

    private PreparedStatement prepareStatementWithFilterParams(Connection conn, String sql) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(sql);
        String selectedFilter = filterTypeComboBox.getValue();

        if ("Date".equals(selectedFilter)) {
            stmt.setString(1, datePicker.getValue().toString());
        } else if ("Date Range".equals(selectedFilter)) {
            if (startDatePicker.getValue().isEqual(endDatePicker.getValue())) {
                // Single date case
                stmt.setString(1, startDatePicker.getValue().toString());
            } else {
                // Date range case
                stmt.setString(1, startDatePicker.getValue().toString());
                stmt.setString(2, endDatePicker.getValue().toString());
            }
        } else if ("Month".equals(selectedFilter)) {
            stmt.setInt(1, monthComboBox.getValue());
            stmt.setInt(2, yearComboBox.getValue());
        } else { // Lọc theo Năm
            stmt.setInt(1, yearOnlyComboBox.getValue());
        }

        return stmt;
    }

    // Các phương thức loadCharts(), loadTablePieChart(), loadProductPieChart() giữ nguyên
    private void loadCharts() {
        // Xóa dữ liệu cũ của biểu đồ
        revenueBarChart.getData().clear();
        tablePieChart.getData().clear();
        productPieChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        // Lấy kiểu lọc hiện tại
        String selectedFilter = filterTypeComboBox.getValue();
        DateTimeFormatter formatter;
        if ("Date".equals(selectedFilter) || "Date Range".equals(selectedFilter)) {
            formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        } else if ("Month".equals(selectedFilter)) {
            formatter = DateTimeFormatter.ofPattern("dd/MM");
        } else {
            formatter = DateTimeFormatter.ofPattern("MM/yyyy");
        }

        // Tính tổng doanh thu theo ngày từ dữ liệu bảng
        Map<String, Double> dailyTotals = new HashMap<>();

        for (Map<String, Object> row : revenueData) {
            for (String date : dateColumns) {
                Double revenue = (Double) row.get(date);
                if (revenue != null) {
                    dailyTotals.merge(date, revenue, Double::sum);
                }
            }
        }

        // Thêm dữ liệu vào biểu đồ cột
        for (Map.Entry<String, Double> entry : dailyTotals.entrySet()) {
            String formattedDate = formatDate(entry.getKey(), formatter);
            XYChart.Data<String, Number> chartData = new XYChart.Data<>(formattedDate, entry.getValue());
            chartData.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    Tooltip.install(newNode, new Tooltip(String.format("%.2f VND", entry.getValue())));
                }
            });
            series.getData().add(chartData);
        }
        revenueBarChart.getData().add(series);

        // Load dữ liệu cho PieChart nhóm bàn và nhóm sản phẩm
        loadTablePieChart();
        loadProductPieChart();
    }

    // Hàm định dạng ngày dựa trên formatter
    private String formatDate(String dateStr, DateTimeFormatter formatter) {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            return date.format(formatter);
        } catch (Exception e) {
            return dateStr; // Nếu lỗi, trả về ngày gốc
        }
    }
    private void loadTablePieChart() {
        tablePieChart.getData().clear();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "";
            PreparedStatement stmt;
            String selectedFilter = filterTypeComboBox.getValue();

            if ("Date".equals(selectedFilter)) {
                sql = "SELECT cp.name AS table_type, SUM(b.total) AS revenue " +
                        "FROM bookings b " +
                        "JOIN pooltables pt ON b.table_id = pt.table_id " +
                        "JOIN cate_pooltables cp ON pt.cate_id = cp.id " +
                        "JOIN orders o ON b.order_id = o.order_id " +
                        "WHERE o.order_status = 'Paid' AND DATE(o.order_date) = ? " +
                        "GROUP BY cp.name";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, datePicker.getValue().toString());
            } else if ("Date Range".equals(selectedFilter)) {
                if (startDatePicker.getValue().isEqual(endDatePicker.getValue())) {
                    // Single date case
                    sql = "SELECT cp.name AS table_type, SUM(b.total) AS revenue " +
                            "FROM bookings b " +
                            "JOIN pooltables pt ON b.table_id = pt.table_id " +
                            "JOIN cate_pooltables cp ON pt.cate_id = cp.id " +
                            "JOIN orders o ON b.order_id = o.order_id " +
                            "WHERE o.order_status = 'Paid' AND DATE(o.order_date) = ? " +
                            "GROUP BY cp.name";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, startDatePicker.getValue().toString());
                    System.out.println("\033[1;32m" + "🎉 Run on Single Date Case" + " 🎊" + "\033[0m");

                } else {
                    // Date range case
                    sql = "SELECT cp.name AS table_type, SUM(b.total) AS revenue " +
                            "FROM bookings b " +
                            "JOIN pooltables pt ON b.table_id = pt.table_id " +
                            "JOIN cate_pooltables cp ON pt.cate_id = cp.id " +
                            "JOIN orders o ON b.order_id = o.order_id " +
                            "WHERE o.order_status = 'Paid' AND o.order_date BETWEEN ? AND ? " +
                            "GROUP BY cp.name";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, startDatePicker.getValue().toString());
                    stmt.setString(2, endDatePicker.getValue().toString());
                    System.out.println("\033[1;32m" + "🎉 Run on Date Range Case" + " 🎊" + "\033[0m");

                }
            }else if ("Month".equals(selectedFilter)) {
                // Check if month and year values are not null
                if (monthComboBox.getValue() == null || yearComboBox.getValue() == null) {
                    return; // Skip if values are not set
                }
//                sql = "SELECT c.category_name, SUM(oi.total) AS revenue " +
//                        "FROM orders_items oi " +
//                        "JOIN products p ON oi.product_id = p.product_id " +
//                        "JOIN category c ON p.category_id = c.category_id " +
//                        "JOIN orders o ON oi.order_id = o.order_id " +
//                        "WHERE o.order_status = 'Paid' AND MONTH(o.order_date) = ? AND YEAR(o.order_date) = ? " +
//                        "GROUP BY c.category_name";
                sql = "SELECT cp.name AS table_type, SUM(b.total) AS revenue " +
                        "FROM bookings b " +
                        "JOIN pooltables pt ON b.table_id = pt.table_id " +
                        "JOIN cate_pooltables cp ON pt.cate_id = cp.id " +
                        "JOIN orders o ON b.order_id = o.order_id " +
                        "WHERE o.order_status = 'Paid' AND MONTH(o.order_date) = ? AND YEAR(o.order_date) = ? " +
                        "GROUP BY cp.name";

                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, monthComboBox.getValue());
                stmt.setInt(2, yearComboBox.getValue());
                System.out.println("\033[1;32m" + "🎉 Run on Month Filter" + " 🎊" + "\033[0m");

            } else { // Lọc theo Năm
                // Check if year value is not null
                if (yearOnlyComboBox.getValue() == null) {
                    return; // Skip if value is not set
                }
                sql = "SELECT cp.name AS table_type, SUM(b.total) AS revenue " +
                        "FROM bookings b " +
                        "JOIN pooltables pt ON b.table_id = pt.table_id " +
                        "JOIN cate_pooltables cp ON pt.cate_id = cp.id " +
                        "JOIN orders o ON b.order_id = o.order_id " +
                        "WHERE o.order_status = 'Paid' AND YEAR(o.order_date) = ? " +
                        "GROUP BY cp.name";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, yearOnlyComboBox.getValue());
                System.out.println("\033[1;32m" + "🎉 Run on Year Filter" + " 🎊" + "\033[0m");
            }

            ResultSet rs = stmt.executeQuery();
            System.out.println("\033[1;32m" + "🎉 Year Got = " + yearOnlyComboBox.getValue() + " 🎊" + "\033[0m");

            while (rs.next()) {
                String tableType = rs.getString("table_type");
                double revenue = rs.getDouble("revenue");

                if (revenue > 0) {
                    PieChart.Data slice = new PieChart.Data(tableType, revenue);
                    tablePieChart.getData().add(slice);
                }
            }

            // Thêm tooltip cho các phần của biểu đồ
            for (PieChart.Data data : tablePieChart.getData()) {
                Tooltip tooltip = new Tooltip(String.format("%s: %.0f VNĐ", data.getName(), data.getPieValue()));
                Tooltip.install(data.getNode(), tooltip);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadProductPieChart() {
        productPieChart.getData().clear();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "";
            PreparedStatement stmt;
            String selectedFilter = filterTypeComboBox.getValue();

            if ("Date".equals(selectedFilter)) {
                sql = "SELECT c.category_name, SUM(oi.total) AS revenue " +
                        "FROM orders_items oi " +
                        "JOIN products p ON oi.product_id = p.product_id " +
                        "JOIN category c ON p.category_id = c.category_id " +
                        "JOIN orders o ON oi.order_id = o.order_id " +
                        "WHERE o.order_status = 'Paid' AND DATE(o.order_date) = ? " +
                        "GROUP BY c.category_name";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, datePicker.getValue().toString());
            } else if ("Date Range".equals(selectedFilter)) {
                if (startDatePicker.getValue().isEqual(endDatePicker.getValue())) {
                    // Single date case
                    sql = "SELECT c.category_name, SUM(oi.total) AS revenue " +
                            "FROM orders_items oi " +
                            "JOIN products p ON oi.product_id = p.product_id " +
                            "JOIN category c ON p.category_id = c.category_id " +
                            "JOIN orders o ON oi.order_id = o.order_id " +
                            "WHERE o.order_status = 'Paid' AND DATE(o.order_date) = ? " +
                            "GROUP BY c.category_name";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, startDatePicker.getValue().toString());
                } else {
                    // Date range case
                    sql = "SELECT c.category_name, SUM(oi.total) AS revenue " +
                            "FROM orders_items oi " +
                            "JOIN products p ON oi.product_id = p.product_id " +
                            "JOIN category c ON p.category_id = c.category_id " +
                            "JOIN orders o ON oi.order_id = o.order_id " +
                            "WHERE o.order_status = 'Paid' AND o.order_date BETWEEN ? AND ? " +
                            "GROUP BY c.category_name";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, startDatePicker.getValue().toString());
                    stmt.setString(2, endDatePicker.getValue().toString());
                }
            } else if ("Month".equals(selectedFilter)) {
                // Check if month and year values are not null
                if (monthComboBox.getValue() == null || yearComboBox.getValue() == null) {
                    return; // Skip if values are not set
                }
                sql = "SELECT c.category_name, SUM(oi.total) AS revenue " +
                        "FROM orders_items oi " +
                        "JOIN products p ON oi.product_id = p.product_id " +
                        "JOIN category c ON p.category_id = c.category_id " +
                        "JOIN orders o ON oi.order_id = o.order_id " +
                        "WHERE o.order_status = 'Paid' AND MONTH(o.order_date) = ? AND YEAR(o.order_date) = ? " +
                        "GROUP BY c.category_name";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, monthComboBox.getValue());
                stmt.setInt(2, yearComboBox.getValue());
            } else { // Lọc theo Năm
                // Check if year value is not null
                if (yearOnlyComboBox.getValue() == null) {
                    return; // Skip if value is not set
                }
                sql = "SELECT c.category_name, SUM(oi.total) AS revenue " +
                        "FROM orders_items oi " +
                        "JOIN products p ON oi.product_id = p.product_id " +
                        "JOIN category c ON p.category_id = c.category_id " +
                        "JOIN orders o ON oi.order_id = o.order_id " +
                        "WHERE o.order_status = 'Paid' AND YEAR(o.order_date) = ? " +
                        "GROUP BY c.category_name";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, yearOnlyComboBox.getValue());
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String categoryName = rs.getString("category_name");
                double revenue = rs.getDouble("revenue");

                if (revenue > 0) {
                    PieChart.Data slice = new PieChart.Data(categoryName, revenue);
                    productPieChart.getData().add(slice);
                }
            }

            // Thêm tooltip cho các phần của biểu đồ
            for (PieChart.Data data : productPieChart.getData()) {
                Tooltip tooltip = new Tooltip(String.format("%s: %.0f VNĐ", data.getName(), data.getPieValue()));
                Tooltip.install(data.getNode(), tooltip);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

