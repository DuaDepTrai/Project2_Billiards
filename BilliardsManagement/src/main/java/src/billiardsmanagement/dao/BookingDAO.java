package src.billiardsmanagement.dao;

import src.billiardsmanagement.model.Booking;
import src.billiardsmanagement.model.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    public List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        String query = """
            SELECT b.booking_id, b.table_id, pt.name AS table_name, b.start_time, b.end_time, 
                   b.timeplay, b.subtotal, b.promotion_id, b.net_total,b.booking_status
            FROM bookings b
            JOIN pooltables pt ON b.table_id = pt.table_id
            ORDER BY b.booking_id
        """; // Câu truy vấn SQL

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Booking booking = new Booking(
                        rs.getString("table_name"),
                        rs.getTimestamp("start_time").toLocalDateTime(),
                        rs.getTimestamp("end_time").toLocalDateTime(),
                        rs.getDouble("timeplay"),
                        rs.getDouble("subtotal"),
                        rs.getString("booking_status")
                );
                booking.setBookingId(rs.getInt("booking_id"))
                        .setTableId(rs.getInt("table_id"))
                        .setPromotionId(rs.getInt("promotion_id"))
                        .setNetTotal(rs.getDouble("net_total"));
                bookings.add(booking);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return bookings;
    }
}
