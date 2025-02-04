package src.billiardsmanagement.dao;

import src.billiardsmanagement.model.Booking;
import src.billiardsmanagement.model.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    public static boolean updateBooking(int bookingId, int orderId, int tableId, String tableStatus) {
        // SQL cố định booking_status là 'playing'
        String sqlBooking = "UPDATE bookings SET booking_status = 'playing', start_time = NOW() WHERE booking_id = ? AND order_id = ?";
        String sqlPoolTable = "UPDATE pooltables SET status = ? WHERE table_id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstBooking = con.prepareStatement(sqlBooking);
             PreparedStatement pstPoolTable = con.prepareStatement(sqlPoolTable)) {

            // Bắt đầu giao dịch
            con.setAutoCommit(false);

            try {
                // Cập nhật trạng thái đặt bàn
                pstBooking.setInt(1, bookingId);
                pstBooking.setInt(2, orderId);
                int bookingRowsAffected = pstBooking.executeUpdate();

                // Kiểm tra cập nhật đặt bàn
                if (bookingRowsAffected <= 0) {
                    System.err.println("Không thể cập nhật trạng thái đặt bàn. Mã đặt bàn: " + bookingId + ", Mã đơn hàng: " + orderId);
                    return false;
                }

                // Cập nhật trạng thái bàn
                pstPoolTable.setString(1, tableStatus);
                pstPoolTable.setInt(2, tableId);
                int tableRowsAffected = pstPoolTable.executeUpdate();

                // Kiểm tra cập nhật bàn
                if (tableRowsAffected <= 0) {
                    System.err.println("Không thể cập nhật trạng thái bàn. Mã bàn: " + tableId);
                    return false;
                }

                // Xác nhận giao dịch nếu cả hai cập nhật thành công
                con.commit();
                return true;

            } catch (SQLException e) {
                // Hoàn tác giao dịch nếu có lỗi
                con.rollback();
                System.err.println("Lỗi cập nhật trạng thái: " + e.getMessage());
                return false;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối cơ sở dữ liệu: " + e.getMessage());
            return false;
        }
    }

    public static List<Booking> getBookingByOrderId(int orderId) {
        List<Booking> bookings = new ArrayList<>();
        String query = "SELECT b.booking_id, b.table_id, b.order_id, pt.name AS table_name, pt.price AS table_price, " +
                "b.start_time, b.end_time, b.timeplay, b.subtotal, b.promotion_id, b.net_total, b.booking_status " +
                "FROM bookings b " +
                "JOIN pooltables pt ON b.table_id = pt.table_id " +
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
                        resultSet.getDouble("table_price"), // New field for table price
                        resultSet.getTimestamp("start_time") != null
                                ? resultSet.getTimestamp("start_time").toLocalDateTime()
                                : null,
                        resultSet.getTimestamp("end_time") != null
                                ? resultSet.getTimestamp("end_time").toLocalDateTime()
                                : null,
                        resultSet.getDouble("timeplay"),
                        resultSet.getDouble("net_total"),
                        resultSet.getDouble("subtotal"),
                        resultSet.getString("booking_status"),
                        resultSet.getInt("promotion_id")
                );

                bookings.add(booking);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }


    public void addBooking(Booking newBooking) {
        String query = "INSERT INTO bookings (order_id, table_id, start_time, booking_status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Set các giá trị vào PreparedStatement
            stmt.setObject(1, newBooking.getOrderId());      // order_id (cho phép null)
            stmt.setObject(2, newBooking.getTableId());      // table_id (cho phép null)
            stmt.setTimestamp(3, newBooking.getStartTime()); // start_time
            stmt.setString(4, newBooking.getBookingStatus()); // booking_status

            // Thực thi câu lệnh INSERT
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
}
