package src.billiardsmanagement.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserSession {
    private static UserSession instance;
    private int userId;
    private String username;
    private String role;
    private String fullname;
    private String image_path;

    private UserSession() {
        // Private constructor để ngăn việc tạo instance trực tiếp
    }

    public static UserSession getInstance() {
        if(instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setUser(int userId, String username, String role, String fullname, String image_path) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.fullname = fullname;
        this.image_path = image_path;
    }

    public void setUser(int userId, String username, String role, String fullname) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.fullname = fullname;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
    public String getFullname() {return fullname;}
    public String getImage_path() {return image_path;}
//    public String getRoleName(int roleId) {
//        String roleName = "Unknown";
//        String query = "SELECT role_name FROM roles WHERE role_id = ?";
//
//        try (Connection conn = DatabaseConnection.getConnection()) {
//            if (conn == null) {
//                System.err.println("❌ Lỗi: Không thể kết nối database!");
//                return "DB Connection Error";
//            }
//
//            try (PreparedStatement stmt = conn.prepareStatement(query)) {
//                stmt.setInt(1, roleId);
//                ResultSet rs = stmt.executeQuery();
//
//                if (rs.next()) {
//                    roleName = rs.getString("role_name");
//                } else {
//                    System.err.println("⚠️ Cảnh báo: Không tìm thấy role với role_id = " + roleId);
//                }
//            }
//        } catch (SQLException e) {
//            System.err.println("❌ Lỗi SQL khi truy vấn role_id = " + roleId + ": " + e.getMessage());
//            e.printStackTrace();
//        }
//        return roleName;
//    }
    public void clearSession() {
        userId = 0;
        username = null;
        role = null;
    }
}