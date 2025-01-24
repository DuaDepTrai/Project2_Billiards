package src.billiardsmanagement.dao;

import src.billiardsmanagement.model.DatabaseConnection;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import src.billiardsmanagement.model.Pair;

public class PromotionDAO {
    
    public static double getPromotionDiscountById(int promotionId){
        try(Connection con = DatabaseConnection.getConnection()){
            if(con==null) throw new SQLException("Lỗi kết nối: Không thể kết nối đến cơ sở dữ liệu!");
            String query = "SELECT discount FROM promotions WHERE promotion_id = "+promotionId;
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(query);
            if(!rs.next()) throw new SQLException("Lỗi truy vấn: Không thể lấy dữ liệu từ cơ sở dữ liệu!");
            return rs.getDouble("discount");
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    

    public static int getPromotionIdByName(String promotionName) {
        try(Connection con = DatabaseConnection.getConnection()) {
            if(con == null) throw new SQLException("Lỗi kết nối: Không thể kết nối đến cơ sở dữ liệu!");
            
            String query = "SELECT promotion_id FROM promotions WHERE name = ?";
            try(PreparedStatement pst = con.prepareStatement(query)) {
                pst.setString(1, promotionName);
                ResultSet rs = pst.executeQuery();
                
                if(!rs.next()) throw new SQLException("Lỗi truy vấn: Không tìm thấy khuyến mãi với tên này!");
                return rs.getInt("promotion_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static List<String> getAllPromotionsNameByList(){
        try(Connection con = DatabaseConnection.getConnection();
            PreparedStatement pstmt = con.prepareStatement("SELECT name FROM promotions");
            ResultSet rs = pstmt.executeQuery()){
            
            List<String> promotionNames = new ArrayList<>();
            while(rs.next()){
                promotionNames.add(rs.getString("name"));
            }
            return promotionNames;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Pair<String, Integer>> getAllPromotionsName() {
        try(Connection con = DatabaseConnection.getConnection()) {
            if(con == null) throw new SQLException("Lỗi kết nối: Không thể kết nối đến cơ sở dữ liệu!");
            
            List<Pair<String, Integer>> promotions = new ArrayList<>();
            String query = "SELECT promotion_id, name FROM promotions";
            
            try(Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(query)) {
                
                while(rs.next()) {
                    Pair<String, Integer> promotion = new Pair<>();
                    promotion.setFirstValue(rs.getString("name"));
                    promotion.setSecondValue(rs.getInt("promotion_id"));
                    promotions.add(promotion);
                }
            }
            
            return promotions;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getPromotionNameById(int promotionId) {
        try(Connection con = DatabaseConnection.getConnection()) {
            if(con == null) throw new SQLException("Error connecting to database");
            
            PreparedStatement pstmt = con.prepareStatement("SELECT name FROM promotions WHERE promotion_id = ?");
            pstmt.setInt(1, promotionId);
            
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()) {
                return rs.getString("name");
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
