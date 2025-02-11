package src.billiardsmanagement.dao;

import src.billiardsmanagement.model.Booking;
import src.billiardsmanagement.model.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

  public static boolean finishOrder(int orderId) {
    Connection conn = DatabaseConnection.getConnection();
    if (conn == null) return false;

    try {
        conn.setAutoCommit(false);

        // Update all bookings: set end_time = NOW(), change status to 'Finish', and calculate timeplay
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

        // Calculate subtotal and net_total for bookings
        String updateBookingCostQuery = """
            UPDATE bookings b 
            JOIN pooltables p ON b.table_id = p.table_id 
            JOIN cate_pooltables c ON p.cate_id = c.id
            SET b.subtotal = (TIMESTAMPDIFF(MINUTE, b.start_time, b.end_time) / 60.0) * c.price, 
                b.net_total = b.subtotal
            WHERE b.order_id = ?""";
        try (PreparedStatement stmt = conn.prepareStatement(updateBookingCostQuery)) {
            stmt.setInt(1, orderId);
            stmt.executeUpdate();
        }
        conn.commit();
        return true;
    } catch (SQLException e) {
        if (conn != null) {
            try {
                conn.rollback();
                e.printStackTrace();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
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
                "b.start_time, b.end_time, b.timeplay, b.subtotal, b.promotion_id, b.net_total, b.booking_status " +
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
