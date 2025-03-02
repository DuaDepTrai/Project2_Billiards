package src.billiardsmanagement.model;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.text.NumberFormat;
import java.util.Locale;

public class User {
    private int id;
    private String username;
    private String password;
    private String plainPassword; // Lưu mật khẩu chưa hash
    private String role;
    private String roleName;
    private String fullname;
    private String phone;
    private Date birthday;
    private String address;
    private Date hireDate;
    private String imagePath;

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    private List<String> permissions;

    public User(int userId, String username, String password, String roleId, String roleName, String fullname, String imagePath) {
        this.id= userId;
        this.username = username;
        this.password = password;
        this.role= roleId;
        this.roleName = roleName;
        this.fullname = fullname;
        this.imagePath = imagePath;
    }
    public User(int id, String username, String password, String role, String fullname,
                String phone, Date birthday, String address, Date hireDate, String imagePath) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullname = fullname;
        this.phone = phone;
        this.birthday = birthday;
        this.address = address;
        this.hireDate = hireDate;
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

    public String getFullname() {return fullname;}

    public void setFullname(String fullname) {this.fullname = fullname;}

    public String getPhone() {return phone;}

    public void setPhone(String phone) {this.phone = phone;}

    public Date getBirthday() {return birthday;}

    public void setBirthday(Date birthday) {this.birthday = birthday;}

    public String getAddress() {return address;}

    public void setAddress(String address) {this.address = address;}

    public Date getHireDate() {return hireDate;}

    public void setHireDate(Date hireDate) {this.hireDate = hireDate;}

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public List<String> getPermissionsAsString() {
        return permissions; // Không cần stream/map vì đã là List<String>
    }

    @Override
    public String toString() {
        return username;
    }

}
