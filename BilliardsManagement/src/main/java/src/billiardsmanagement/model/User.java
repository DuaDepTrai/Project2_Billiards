package src.billiardsmanagement.model;

import java.text.NumberFormat;
import java.util.Locale;

public class User {
    private int id;
    private String username;
    private String password;
    private String plainPassword; // Lưu mật khẩu chưa hash
    private String role;
    private String imagePath;

    public User(int id, String username, String password, String role, String imagePath) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.imagePath = imagePath;
    }

    // Getter và Setter
    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {return password;}

    public String getRole() {return role;}

    public String getPlainPassword() {return plainPassword;}

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @Override
    public String toString() {
        return username;
    }
}
