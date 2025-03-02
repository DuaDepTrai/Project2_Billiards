package src.billiardsmanagement.model;
public class UserSession {
    private static UserSession instance;
    private int userId;
    private String username;
    private String role;
    private String fullname;

    private UserSession() {
        // Private constructor để ngăn việc tạo instance trực tiếp
    }

    public static UserSession getInstance() {
        if(instance == null) {
            instance = new UserSession();
        }
        return instance;
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

    public void clearSession() {
        userId = 0;
        username = null;
        role = null;
    }
}