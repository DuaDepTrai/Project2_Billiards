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
        private DatePicker startDatePicker, endDatePicker;
        @FXML
        private ComboBox<Integer> monthComboBox, yearComboBox, yearOnlyComboBox;
        @FXML
        private ComboBox<String> filterTypeComboBox;
        @FXML
        private HBox dateRangeBox, monthBox, yearBox;
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
            List<String> dates = new ArrayList<>(); // Đặt trước khi sử dụng
            // Configure category column
            colCategory.setCellValueFactory(data -> new SimpleStringProperty((String) data.getValue().get("category")));
            revenueTable.setItems(revenueData);

            // Add options to ComboBox
            filterTypeComboBox.setItems(FXCollections.observableArrayList("Date Range", "Month", "Year"));
            filterTypeComboBox.setValue("Date Range"); // Default to Date Range
            filterTypeComboBox.setOnAction(e -> updateFilterType());

            // Set values for month and year ComboBox
            monthComboBox.setItems(FXCollections.observableArrayList(IntStream.rangeClosed(1, 12).boxed().toList()));
            int currentYear = LocalDate.now().getYear();
            yearComboBox.setItems(FXCollections.observableArrayList(IntStream.rangeClosed(2020, currentYear).boxed().toList()));
            yearOnlyComboBox.setItems(yearComboBox.getItems());

// Set default values for DatePicker
            LocalDate today = LocalDate.now();
            startDatePicker.setValue(today);
            endDatePicker.setValue(today);

// Format revenue values to integer without decimal places in revenueTable
            DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd/MM");
            for (String date : dates) {
                dateColumns.add(date);
                LocalDate localDate = LocalDate.parse(date);
                String displayDate = localDate.format(displayFormatter);

                TableColumn<Map<String, Object>, String> dateColumn = new TableColumn<>(displayDate);
                dateColumn.setCellValueFactory(data -> {
                    Double value = (Double) data.getValue().get(date);
                    return new SimpleStringProperty(value != null ?
                            NumberFormat.getIntegerInstance(new Locale("en", "US")).format(value) : "0");
                });
                revenueTable.getColumns().add(dateColumn);
            }

            updateFilterType();
        }

        @FXML
        private void updateFilterType() {
            String selectedFilter = filterTypeComboBox.getValue();

            dateRangeBox.setVisible("Date Range".equals(selectedFilter));
            dateRangeBox.setManaged("Date Range".equals(selectedFilter));

            monthBox.setVisible("Month".equals(selectedFilter));
            monthBox.setManaged("Month".equals(selectedFilter));

            yearBox.setVisible("Year".equals(selectedFilter));
            yearBox.setManaged("Year".equals(selectedFilter));
        }

        @FXML
        private void filterRevenue() {
            // Xóa mọi dữ liệu trong bảng chỉ khi có thay đổi bộ lọc
            if (!revenueData.isEmpty()) {
                revenueData.clear();
                revenueTable.getItems().clear();
                revenueTable.getColumns().clear();
                dateColumns.clear();
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                // Lấy danh sách các ngày trong khoảng thời gian đã chọn
                List<String> dates = fetchDatesInRange(conn);

                // Định dạng hiển thị ngày
                DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd/MM");
                for (String date : dates) {
                    dateColumns.add(date);
                    LocalDate localDate = LocalDate.parse(date);
                    String displayDate = localDate.format(displayFormatter);

                    TableColumn<Map<String, Object>, String> dateColumn = new TableColumn<>(displayDate);
                    dateColumn.setCellValueFactory(data -> {
                        Double value = (Double) data.getValue().get(date);
                        return new SimpleStringProperty(value != null ?
                                NumberFormat.getIntegerInstance(new Locale("vi", "VN")).format(value) : "0");
                    });
                    revenueTable.getColumns().add(dateColumn);
                }

                // Lấy dữ liệu doanh thu theo danh mục và ngày
                Map<String, Map<String, Double>> categoryRevenueByDate = fetchCategoryRevenueByDate(conn, dates);

                // Tạo dữ liệu cho bảng
                for (Map.Entry<String, Map<String, Double>> entry : categoryRevenueByDate.entrySet()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("category", entry.getKey());

                    // Thêm doanh thu cho mỗi ngày
                    for (String date : dates) {
                        Double revenue = entry.getValue().getOrDefault(date, 0.0);
                        row.put(date, revenue);
                    }

                    revenueData.add(row);
                }

                // Cập nhật bảng với dữ liệu mới
                revenueTable.setItems(revenueData);

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
            if ("Date Range".equals(selectedFilter)) {
                sql = "SELECT DISTINCT DATE(order_date) AS date FROM orders " +
                        "WHERE order_status = 'Paid' AND order_date BETWEEN ? AND ? ORDER BY date";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, startDatePicker.getValue().toString());
                stmt.setString(2, endDatePicker.getValue().toString());
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
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                dates.add(rs.getString("date"));
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

            if ("Date Range".equals(selectedFilter)) {
                return "SELECT c.category_name, DATE(o.order_date) AS date, SUM(oi.total) AS revenue " +
                        "FROM orders_items oi " +
                        "JOIN products p ON oi.product_id = p.product_id " +
                        "JOIN category c ON p.category_id = c.category_id " +
                        "JOIN orders o ON oi.order_id = o.order_id " +
                        "WHERE o.order_status = 'Paid' AND o.order_date BETWEEN ? AND ? " +
                        "GROUP BY c.category_name, DATE(o.order_date)";
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

            if ("Date Range".equals(selectedFilter)) {
                return "SELECT cp.name AS table_type, DATE(o.order_date) AS date, SUM(b.total) AS revenue " +
                        "FROM bookings b " +
                        "JOIN pooltables pt ON b.table_id = pt.table_id " +
                        "JOIN cate_pooltables cp ON pt.cate_id = cp.id " +
                        "JOIN orders o ON b.order_id = o.order_id " +
                        "WHERE o.order_status = 'Paid' AND o.order_date BETWEEN ? AND ? " +
                        "GROUP BY cp.name";
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

            if ("Date Range".equals(selectedFilter)) {
                return "SELECT COUNT(*) AS total_orders, SUM(total_cost) AS total_revenue " +
                        "FROM orders WHERE order_status = 'Paid' AND order_date BETWEEN ? AND ?";
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

            if ("Date Range".equals(selectedFilter)) {
                stmt.setString(1, startDatePicker.getValue().toString());
                stmt.setString(2, endDatePicker.getValue().toString());
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
            if ("Date Range".equals(selectedFilter)) {
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

                if ("Date Range".equals(selectedFilter)) {
                    sql = "SELECT cp.name AS table_type, SUM(b.total) AS revenue " +
                            "FROM bookings b " +
                            "JOIN pooltables pt ON b.table_id = pt.table_id " +
                            "JOIN cate_pooltables cp ON pt.cate_id = cp.id " +
                            "JOIN orders o ON b.order_id = o.order_id " +
                            "WHERE o.order_date BETWEEN ? AND ? " +
                            "GROUP BY cp.name";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, startDatePicker.getValue().toString());
                    stmt.setString(2, endDatePicker.getValue().toString());
                } else if ("Month".equals(selectedFilter)) {
                    sql = "SELECT cp.name AS table_type, SUM(b.total) AS revenue " +
                            "FROM bookings b " +
                            "JOIN pooltables pt ON b.table_id = pt.table_id " +
                            "JOIN cate_pooltables cp ON pt.cate_id = cp.id " +
                            "JOIN orders o ON b.order_id = o.order_id " +
                            "WHERE MONTH(o.order_date) = ? AND YEAR(o.order_date) = ? " +
                            "GROUP BY cp.name";
                    stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, monthComboBox.getValue());
                    stmt.setInt(2, yearComboBox.getValue());
                } else { // Lọc theo Năm
                    sql = "SELECT cp.name AS table_type, SUM(b.total) AS revenue " +
                            "FROM bookings b " +
                            "JOIN pooltables pt ON b.table_id = pt.table_id " +
                            "JOIN cate_pooltables cp ON pt.cate_id = cp.id " +
                            "JOIN orders o ON b.order_id = o.order_id " +
                            "WHERE YEAR(o.order_date) = ? " +
                            "GROUP BY cp.name";
                    stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, yearOnlyComboBox.getValue());
                }

                ResultSet rs = stmt.executeQuery();

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

                if ("Date Range".equals(selectedFilter)) {
                    sql = "SELECT c.category_name, SUM(oi.total) AS revenue " +
                            "FROM orders_items oi " +
                            "JOIN products p ON oi.product_id = p.product_id " +
                            "JOIN category c ON p.category_id = c.category_id " +
                            "JOIN orders o ON oi.order_id = o.order_id " +
                            "WHERE o.order_date BETWEEN ? AND ? " +
                            "GROUP BY c.category_name";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, startDatePicker.getValue().toString());
                    stmt.setString(2, endDatePicker.getValue().toString());
                } else if ("Month".equals(selectedFilter)) {
                    sql = "SELECT c.category_name, SUM(oi.total) AS revenue " +
                            "FROM orders_items oi " +
                            "JOIN products p ON oi.product_id = p.product_id " +
                            "JOIN category c ON p.category_id = c.category_id " +
                            "JOIN orders o ON oi.order_id = o.order_id " +
                            "WHERE MONTH(o.order_date) = ? AND YEAR(o.order_date) = ? " +
                            "GROUP BY c.category_name";
                    stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, monthComboBox.getValue());
                    stmt.setInt(2, yearComboBox.getValue());
                } else { // Lọc theo Năm
                    sql = "SELECT c.category_name, SUM(oi.total) AS revenue " +
                            "FROM orders_items oi " +
                            "JOIN products p ON oi.product_id = p.product_id " +
                            "JOIN category c ON p.category_id = c.category_id " +
                            "JOIN orders o ON oi.order_id = o.order_id " +
                            "WHERE YEAR(o.order_date) = ? " +
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
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }
