package src.billiardsmanagement.dao;

import src.billiardsmanagement.model.Booking;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class BookingDAO {
//    public static void main(String[] args) {
//        getBookingData();
//    }

    public static List<Booking> getBookingData(){
        try(Connection con = HikariPool.getHikariConnection()){
            String query = "SELECT * FROM products";
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery(query);
            if(rs!=null){
                System.out.println(rs);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
