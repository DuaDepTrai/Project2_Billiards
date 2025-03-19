package src.billiardsmanagement.dao;

import src.billiardsmanagement.controller.orders.OrderController;
import src.billiardsmanagement.model.Booking;
import src.billiardsmanagement.model.DatabaseConnection;
import src.billiardsmanagement.model.NotificationStatus;
import src.billiardsmanagement.service.NotificationService;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    public static List<Booking> getBookingsInTimeRange() {
        List<Booking> bookings = new ArrayList<>();
        String query = "SELECT b.*, p.name AS table_name " +
                "FROM bookings b " +
                "JOIN pooltables p ON b.table_id = p.table_id " +
                "WHERE b.start_time < NOW() - INTERVAL ? MINUTE " +
                "AND b.booking_status = 'Ordered'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, OrderController.minutesLimit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Booking booking = new Booking(
                        rs.getInt("booking_id"),
                        rs.getInt("order_id"),
                        rs.getInt("table_id"),
                        rs.getTimestamp("start_time").toLocalDateTime(),
                        rs.getTimestamp("end_time") != null ? rs.getTimestamp("end_time").toLocalDateTime() : null,
                        rs.getDouble("timeplay"),
                        rs.getDouble("total"),
                        rs.getString("booking_status"),
                        rs.getString("table_name") // Fetch pool table name
                );
                bookings.add(booking);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle exception properly in production
        }
        return bookings;
    }



    public static Booking getBookingByTableIdAndOrderId(int orderId, int tableId) {
        String query = "SELECT * FROM bookings WHERE order_id = ? AND table_id = ? ORDER BY start_time DESC LIMIT 1";
        Booking booking = new Booking(); // Create an empty Booking object
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement pr = con.prepareStatement(query);
            pr.setInt(1, orderId);
            pr.setInt(2, tableId);
            ResultSet rs = pr.executeQuery();

            if (!rs.next()) {
                throw new Exception("No booking found for this orderId = " + orderId + " and tableId = " + tableId);
            }

            // Set properties using setter methods
            booking.setBookingId(rs.getInt("booking_id"))
                    .setOrderId(rs.getInt("order_id"))
                    .setTableId(rs.getInt("table_id"))
                    .setStartTime(rs.getTimestamp("start_time").toLocalDateTime());

            // Check for null before setting values
            if (rs.getTimestamp("end_time") != null) {
                booking.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
            }

            if (rs.getDouble("timeplay") != 0) { // Assuming 0 means not set
                booking.setTimeplay(rs.getDouble("timeplay"));
            }

            if (rs.getDouble("total") != 0) { // Assuming 0 means not set
                booking.setTotal(rs.getDouble("total"));
            }

            booking.setBookingStatus(rs.getString("booking_status"));

            return booking;
        } catch (Exception e) {
            e.printStackTrace(); // Print the stack trace for debugging
            return new Booking();
        }
    }

    public static int getTheLatestOrderByTableId(int tableId) {
        String query = "SELECT * FROM bookings WHERE table_id = ? AND booking_status IN ('Playing', 'Ordered') ORDER BY start_time DESC LIMIT 1";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pr = con.prepareStatement(query)) {

            pr.setInt(1, tableId);
            ResultSet rs = pr.executeQuery();

            if (!rs.next()) {
                throw new Exception("No active order found for tableId = " + tableId + ". This table might not be in use.");
            }

            int orderId = rs.getInt("order_id");
            System.out.println("Got Order ID = " + orderId);
            return orderId;

        } catch (Exception e) {
            System.out.println("Ah shit, error happens in getTheLatestOrderByTableId() in BookingDAO!");
            e.printStackTrace();
        }
        return -1;
    }


    public static void updateBookingStatus(int bookingId) {
        String query = "UPDATE bookings SET booking_status = 'canceled' WHERE booking_id= ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, bookingId);
            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Booking ID " + bookingId + " đã bị hủy thành công.");
            } else {
                System.out.println("Không tìm thấy Booking ID: " + bookingId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean finishAllBookings(int orderId) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try {
            conn.setAutoCommit(false);

            // 1. Update all bookings
            String updateBookingsQuery = """
                    UPDATE bookings
                    SET
                        end_time = CASE
                            WHEN booking_status = 'Playing' THEN NOW()
                            ELSE end_time
                        END,
                        booking_status = CASE
                            WHEN booking_status = 'Playing' THEN 'Finish'
                            WHEN booking_status = 'Ordered' THEN 'Canceled'
                            ELSE booking_status
                        END,
                        timeplay = CASE
                            WHEN booking_status = 'Playing' THEN TIMESTAMPDIFF(MINUTE, start_time, NOW()) / 60.0
                            ELSE timeplay
                        END
                    WHERE order_id = ?""";

            try (PreparedStatement stmt = conn.prepareStatement(updateBookingsQuery)) {
                stmt.setInt(1, orderId);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected <= 0) {
                    throw new SQLException("No bookings were updated. Check the order ID.");
                }

                // 2. Calculate total for bookings
                String updateBookingCostQuery = """
                        UPDATE bookings b
                        JOIN pooltables p ON b.table_id = p.table_id
                        JOIN cate_pooltables c ON p.cate_id = c.id
                        SET b.total = CASE
                            WHEN b.booking_status = 'Finish' THEN (TIMESTAMPDIFF(MINUTE, b.start_time, b.end_time) / 60.0) * c.price
                            ELSE b.total
                        END
                        WHERE b.order_id = ?""";

                try (PreparedStatement stmt2 = conn.prepareStatement(updateBookingCostQuery)) {
                    stmt2.setInt(1, orderId);
                    int rowsAffectedCost = stmt2.executeUpdate();
                    if (rowsAffectedCost <= 0) {
                        throw new SQLException("No total cost was calculated. Check the order ID.");
                    }

                    // 3. Update pool table status to 'available'
                    String updatePooltablesQuery = """
                            UPDATE pooltables
                            SET status = 'available'
                            WHERE table_id IN (
                                SELECT table_id FROM bookings
                                WHERE order_id = ?
                            )""";

                    try (PreparedStatement stmt3 = conn.prepareStatement(updatePooltablesQuery)) {
                        stmt3.setInt(1, orderId);
                        int rowsAffectedTables = stmt3.executeUpdate();
                        if (rowsAffectedTables <= 0) {
                            throw new SQLException("No pool tables were updated to available. Check the order ID.");
                        }
                    }
                }
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean finishOrder(int orderId) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try {
            conn.setAutoCommit(false);

            // 1. Cập nhật tất cả các bookings: đặt end_time = NOW(), đổi booking_status thành 'Finish', tính timeplay
            String updateBookingsQuery = """
                    UPDATE bookings 
                    SET end_time = NOW(), 
                        booking_status = 'Finish', 
                        timeplay = TIMESTAMPDIFF(MINUTE, start_time, NOW()) / 60.0
                    WHERE order_id = ?""";
            try (PreparedStatement stmt = conn.prepareStatement(updateBookingsQuery)) {
                stmt.setInt(1, orderId);
                stmt.executeUpdate();
            }

            // 2. Tính toán total cho các booking
            String updateBookingCostQuery = """
                    UPDATE bookings b 
                    JOIN pooltables p ON b.table_id = p.table_id 
                    JOIN cate_pooltables c ON p.cate_id = c.id
                    SET b.total = (TIMESTAMPDIFF(MINUTE, b.start_time, b.end_time) / 60.0) * c.price
                    WHERE b.order_id = ?""";
            try (PreparedStatement stmt = conn.prepareStatement(updateBookingCostQuery)) {
                stmt.setInt(1, orderId);
                stmt.executeUpdate();
            }

            // 3. Cập nhật trạng thái pooltables thành 'available' cho các booking thuộc orderId này
            String updatePooltablesQuery = """
                    UPDATE pooltables 
                    SET status = 'available'
                    WHERE table_id IN (
                        SELECT table_id FROM bookings WHERE order_id = ?
                    )""";
            try (PreparedStatement stmt = conn.prepareStatement(updatePooltablesQuery)) {
                stmt.setInt(1, orderId);
                stmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateBooking(int bookingId, int orderId, int tableId, String tableStatus) {
        // SQL fixed booking_status as 'playing'
        String sqlBooking = "UPDATE bookings SET booking_status = 'playing', start_time = NOW() WHERE booking_id = ? AND order_id = ?";
        String sqlPoolTable = "UPDATE pooltables SET status = ? WHERE table_id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstBooking = con.prepareStatement(sqlBooking);
             PreparedStatement pstPoolTable = con.prepareStatement(sqlPoolTable)) {

            // Start transaction
            con.setAutoCommit(false);

            try {
                // Update booking status
                pstBooking.setInt(1, bookingId);
                pstBooking.setInt(2, orderId);
                int bookingRowsAffected = pstBooking.executeUpdate();

                // Check update booking status
                if (bookingRowsAffected <= 0) {
                    System.err.println("Cannot update booking status. Booking ID: " + bookingId + ", Order ID: " + orderId);
                    return false;
                }

                // Update table status
                pstPoolTable.setString(1, tableStatus);
                pstPoolTable.setInt(2, tableId);
                int tableRowsAffected = pstPoolTable.executeUpdate();

                // Check update table
                if (tableRowsAffected <= 0) {
                    System.err.println("Cannot update table status. Table ID: " + tableId);
                    return false;
                }

                // Commit transaction if both updates are successful
                con.commit();
                return true;

            } catch (SQLException e) {
                // Rollback transaction if error occurs
                con.rollback();
                System.err.println("Error updating status: " + e.getMessage());
                return false;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            return false;
        }
    }

    public static List<Booking> getBookingByOrderId(int orderId) {
        List<Booking> bookings = new ArrayList<>();
        String query = "SELECT b.booking_id, b.table_id, b.order_id, pt.name AS table_name, cp.price AS table_price, " +
                "b.start_time, b.end_time, b.timeplay, b.total, b.booking_status " +
                "FROM bookings b " +
                "JOIN pooltables pt ON b.table_id = pt.table_id " +
                "JOIN cate_pooltables cp ON pt.cate_id = cp.id " + // Add JOIN with cate_pooltables
                "WHERE b.order_id = ? " +
                "ORDER BY b.booking_id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, orderId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Booking booking = new Booking(
                        resultSet.getInt("booking_id"),
                        resultSet.getInt("order_id"),
                        resultSet.getInt("table_id"),
                        resultSet.getString("table_name"),
                        resultSet.getDouble("table_price"), // Field price is fetched from cate_pooltables
                        resultSet.getTimestamp("start_time") != null
                                ? resultSet.getTimestamp("start_time").toLocalDateTime()
                                : null,
                        resultSet.getTimestamp("end_time") != null
                                ? resultSet.getTimestamp("end_time").toLocalDateTime()
                                : null,
                        resultSet.getDouble("timeplay"),
                        resultSet.getDouble("total"),
                        resultSet.getString("booking_status")
                );

                bookings.add(booking);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    public static boolean stopBooking(int bookingId, Timestamp startTime, int poolTableId) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try {
            conn.setAutoCommit(false);

            // Lấy thời gian hiện tại từ database
            String currentTimeQuery = "SELECT NOW()";
            Timestamp currentTime;
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(currentTimeQuery)) {
                if (!rs.next()) return false;
                currentTime = rs.getTimestamp(1);
            }

            // Kiểm tra thời gian hợp lệ
            if (currentTime.before(startTime)) {
                return false;
            }

            // Tính toán thời gian chơi (phút)
            String timeplayQuery = "SELECT TIMESTAMPDIFF(MINUTE, ?, ?) AS timeplay";
            int timeplayInMinutes;
            try (PreparedStatement timeplayStmt = conn.prepareStatement(timeplayQuery)) {
                timeplayStmt.setTimestamp(1, startTime);
                timeplayStmt.setTimestamp(2, currentTime);
                try (ResultSet timeplayRs = timeplayStmt.executeQuery()) {
                    if (!timeplayRs.next()) return false;
                    timeplayInMinutes = timeplayRs.getInt("timeplay");
                }
            }

            // Chuyển đổi thời gian chơi sang giờ, làm tròn 1 chữ số thập phân
            double timeplayInHours = Math.round((timeplayInMinutes / 60.0) * 10.0) / 10.0;

            // Lấy giá bàn bida
            String priceQuery = "SELECT c.price FROM cate_pooltables c JOIN pooltables p ON p.cate_id = c.id WHERE p.table_id = ?";
            double price;
            try (PreparedStatement priceStmt = conn.prepareStatement(priceQuery)) {
                priceStmt.setInt(1, poolTableId);
                try (ResultSet priceRs = priceStmt.executeQuery()) {
                    if (!priceRs.next()) return false;
                    price = priceRs.getDouble("price");
                }
            }

            // Tính toán tổng tiền
            double total = timeplayInHours * price;

            // Cập nhật trạng thái booking
            String updateQuery = "UPDATE bookings SET end_time = ?, timeplay = ?, total = ?, booking_status = 'finish' WHERE booking_id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                updateStmt.setTimestamp(1, currentTime);
                updateStmt.setDouble(2, timeplayInHours);
                updateStmt.setDouble(3, total);
                updateStmt.setInt(4, bookingId);
                updateStmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        }
    }

    // Used for canceling multiple bookings at once. Returns true if all bookings were canceled successfully.
    public static boolean cancelMultipleBookings(List<Booking> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            return false; // No bookings to cancel
        }

        String updateBookingQuery = "UPDATE bookings SET booking_status = 'Canceled' WHERE booking_id = ?";
        String updateTableStatusQuery = "UPDATE pooltables SET status = 'Available' WHERE table_id = (SELECT table_id FROM bookings WHERE booking_id = ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            try (PreparedStatement stmt1 = conn.prepareStatement(updateBookingQuery);
                 PreparedStatement stmt2 = conn.prepareStatement(updateTableStatusQuery)) {

                for (Booking booking : bookings) {
                    int bookingId = booking.getBookingId();

                    // Update booking status to 'Canceled'
                    stmt1.setInt(1, bookingId);
                    int rowsUpdated = stmt1.executeUpdate();

                    if (rowsUpdated == 0) {
                        conn.rollback(); // Rollback transaction if any booking update fails
                        return false;
                    }

                    // Update table status to 'Available'
                    stmt2.setInt(1, bookingId);
                    stmt2.executeUpdate();
                }

                conn.commit(); // Commit all updates
                return true;
            } catch (SQLException e) {
                conn.rollback(); // Rollback if any error occurs
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static boolean cancelBooking(int bookingId) {
        String updateBookingQuery = "UPDATE bookings SET booking_status = 'Canceled' WHERE booking_id = ?";
        String updateTableStatusQuery = "UPDATE pooltables SET status = 'Available' WHERE table_id = (SELECT table_id FROM bookings WHERE booking_id = ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Bắt đầu transaction

            // Cập nhật trạng thái booking thành 'Canceled'
            try (PreparedStatement stmt1 = conn.prepareStatement(updateBookingQuery)) {
                stmt1.setInt(1, bookingId);
                int rowsUpdated = stmt1.executeUpdate();

                if (rowsUpdated == 0) {
                    conn.rollback(); // Không có booking nào được cập nhật
                    return false;
                }
            }

            // Cập nhật trạng thái bàn thành 'available'
            try (PreparedStatement stmt2 = conn.prepareStatement(updateTableStatusQuery)) {
                stmt2.setInt(1, bookingId);
                stmt2.executeUpdate();
            }

            conn.commit(); // Xác nhận thay đổi
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static boolean handleFinishBooking(int bookingId, int tableId) {
        System.out.println("Booking ID : " + bookingId + " Table ID : " + tableId);
        String updateBookingQuery = """
                UPDATE bookings
                SET end_time = NOW(),
                    booking_status = 'Finish',
                    timeplay = TIMESTAMPDIFF(MINUTE, start_time, NOW()) / 60.0
                WHERE booking_id = ?""";

        String updateBookingTotalQuery = """
                UPDATE bookings b
                JOIN pooltables p ON b.table_id = p.table_id
                JOIN cate_pooltables c ON p.cate_id = c.id
                SET b.total = (TIMESTAMPDIFF(MINUTE, b.start_time, b.end_time) / 60.0) * c.price
                WHERE b.booking_id = ?""";

        String updateTableQuery = "UPDATE pooltables SET status = 'Available' WHERE table_id = ?";

        Connection conn = DatabaseConnection.getConnection();

        try (
                PreparedStatement stmtBooking = conn.prepareStatement(updateBookingQuery);
                PreparedStatement stmtTable = conn.prepareStatement(updateTableQuery);
                PreparedStatement stmtTotal = conn.prepareStatement(updateBookingTotalQuery)) {

            conn.setAutoCommit(false);

            // Update Booking status + timeplay + end time
            System.out.println("Booking ID passed = " + bookingId);
            stmtBooking.setInt(1, bookingId);
            int rowsUpdatedBooking = stmtBooking.executeUpdate();

            // Update table status to 'Available'
            stmtTable.setInt(1, tableId);
            int rowsUpdatedTable = stmtTable.executeUpdate();

            // Commit transaction if both updates are successful
            if (rowsUpdatedBooking > 0 && rowsUpdatedTable > 0) {
                // Update Booking total cost
                stmtTotal.setInt(1, bookingId);
                int rowsUpdatedTotal = stmtTotal.executeUpdate(); // Fixed this line

                if (rowsUpdatedTotal > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    throw new SQLException("Failed to update booking total.");
                }
            } else {
                conn.rollback();
                throw new SQLException("Failed to update booking or table status.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                // Rollback in case of exception
                conn.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handleCancelBooking(int bookingId, int tableId) {
        String updateBookingQuery = "UPDATE bookings SET booking_status = 'Canceled' WHERE booking_id = ?";
        String updateTableQuery = "UPDATE pooltables SET status = 'Available' WHERE table_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmtBooking = conn.prepareStatement(updateBookingQuery);
             PreparedStatement stmtTable = conn.prepareStatement(updateTableQuery)) {

            conn.setAutoCommit(false);

            // Update Booking status
            stmtBooking.setInt(1, bookingId);
            int rowsUpdatedBooking = stmtBooking.executeUpdate();

            // Update Table status
            stmtTable.setInt(1, tableId);
            int rowsUpdatedTable = stmtTable.executeUpdate();

            // Commit transaction if both updates are successful
            if (rowsUpdatedBooking > 0 && rowsUpdatedTable > 0) {
                conn.commit();
                return true;
            } else {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int getBookingIdByOrderIdAndTableId(int orderId, int tableId) {
        int bookingId = -1; // Default value if no booking is found

        // SQL query to fetch the booking_id based on order_id and table_id
        String sql = "SELECT booking_id FROM bookings WHERE order_id = ? AND table_id = ?";

        try (Connection connection = DatabaseConnection.getConnection(); // Replace with your method to get a DB connection
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, orderId);
            preparedStatement.setInt(2, tableId);

            // Execute the query
            ResultSet resultSet = preparedStatement.executeQuery();

            // If a result is found, retrieve the booking_id
            if (resultSet.next()) {
                bookingId = resultSet.getInt("booking_id");
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle exceptions appropriately
        }
        System.out.println("Booking ID: " + bookingId);
        return bookingId; // Return the found booking_id or -1 if not found
    }

    public static void cancelAllBookings(int orderID) {
        String updateBookingsSQL = "UPDATE bookings SET booking_status = 'Canceled' WHERE order_id = ? AND booking_status IN ('Ordered')";
        String updateOrderSQL = "UPDATE orders SET order_status = 'Canceled' WHERE order_id = ?";

        Connection conn = null;
        PreparedStatement updateBookingsStmt = null;
        PreparedStatement updateOrderStmt = null;

        try {
            conn = DatabaseConnection.getConnection(); // Get DB connection
            conn.setAutoCommit(false); // Start transaction

            // Cancel all bookings under the given order
            updateBookingsStmt = conn.prepareStatement(updateBookingsSQL);
            updateBookingsStmt.setInt(1, orderID);
            int affectedRows = updateBookingsStmt.executeUpdate();

            // If at least one booking was canceled, update the order status
            if (affectedRows > 0) {
                updateOrderStmt = conn.prepareStatement(updateOrderSQL);
                updateOrderStmt.setInt(1, orderID);
                updateOrderStmt.executeUpdate();
            }

            conn.commit(); // Commit transaction
            System.out.println("All bookings for order " + orderID + " have been canceled.");
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on failure
                    System.out.println("Transaction rolled back due to an error: " + e.getMessage());
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
        } finally {
            try {
                if (updateBookingsStmt != null) updateBookingsStmt.close();
                if (updateOrderStmt != null) updateOrderStmt.close();
                if (conn != null) conn.setAutoCommit(true); // Restore auto-commit
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public boolean addBooking(Booking newBooking) {
        String query = "INSERT INTO bookings (order_id, table_id, start_time, booking_status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Set values into PreparedStatement
            stmt.setObject(1, newBooking.getOrderId());      // order_id (allows null)
            stmt.setObject(2, newBooking.getTableId());      // table_id (allows null)
            stmt.setTimestamp(3, newBooking.getStartTime()); // start_time
            stmt.setString(4, newBooking.getBookingStatus()); // booking_status

            // Execute INSERT statement
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Booking added successfully!");
                return true;
            } else {
                System.out.println("Failed to add booking.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void handleAddBooking(Booking newBooking) {
        String query = "INSERT INTO bookings (order_id, table_id, start_time, booking_status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Set values into PreparedStatement
            stmt.setObject(1, newBooking.getOrderId());      // order_id (allows null)
            stmt.setObject(2, newBooking.getTableId());      // table_id (allows null)
            stmt.setTimestamp(3, newBooking.getStartTime()); // start_time
            stmt.setString(4, newBooking.getBookingStatus()); // booking_status

            // Execute INSERT statement
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Booking added successfully!");
            } else {
                System.out.println("Failed to add booking.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean addBookingToExistedOrder(int orderId, int tableId, String status) {
        String query = "INSERT INTO bookings (order_id, table_id, start_time, booking_status) VALUES (?, ?, NOW(), ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderId);
            stmt.setInt(2, tableId);
            stmt.setString(3, status);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Booking added successfully!");
                return true;
            } else {
                System.out.println("Failed to add booking.");
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean addOrderedBookingToExistedOrder(int orderId, int tableId, String status, LocalDateTime startTime) {
        String query = "INSERT INTO bookings (order_id, table_id, start_time, booking_status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderId);
            stmt.setInt(2, tableId);
            stmt.setTimestamp(3, Timestamp.valueOf(startTime));
            stmt.setString(4, status);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Booking added successfully!");
                return true;
            } else {
                System.out.println("Failed to add booking.");
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteBooking(int bookingId) {
        String deleteQuery = "DELETE FROM bookings WHERE booking_id = ?"; // Adjust table and column names if needed

        try (Connection connection = DatabaseConnection.getConnection(); // Replace with your actual connection method
             PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {

            preparedStatement.setInt(1, bookingId);

            int rowsAffected = preparedStatement.executeUpdate();

            // Return true if at least one row was deleted
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting booking with ID " + bookingId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static void updateTableStatusAfterBooking(int bookingId) {
        String query = """
                    UPDATE pooltables 
                    SET status = 'available' 
                    WHERE table_id = (
                        SELECT table_id FROM bookings WHERE booking_id = ? 
                        AND booking_status IN ('Finish', 'Canceled')
                    )
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, bookingId);
            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                NotificationService.showNotification("Table Updated",
                        "The pool table is now available.", NotificationStatus.Success);
            } else {
                NotificationService.showNotification("No Update",
                        "No changes were made. The booking might not be finished or canceled.", NotificationStatus.Warning);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            NotificationService.showNotification("Error",
                    "An error occurred while updating the table status.", NotificationStatus.Error);
        }
    }

}
