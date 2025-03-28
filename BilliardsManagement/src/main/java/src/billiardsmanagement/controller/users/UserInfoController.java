package src.billiardsmanagement.controller.users;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import src.billiardsmanagement.dao.UserDAO;
import src.billiardsmanagement.model.User;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import src.billiardsmanagement.model.UserSession;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class UserInfoController implements Initializable {

    @FXML
    private Label lblUsername, lblFullname, lblPhone, lblBirthday, lblAddress, lblHireDate, lblRole;
    @FXML
    private ImageView imgAvatar;
    @FXML
    private Button btnChangeUserInfo;

    private UserDAO userDAO = new UserDAO();
    private User user;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnChangeUserInfo.setOnAction(event -> handleChangeUserInfo());

        // Cho phép xuống dòng khi nội dung quá dài
        lblUsername.setWrapText(true);
        lblFullname.setWrapText(true);
        lblPhone.setWrapText(true);
        lblBirthday.setWrapText(true);
        lblAddress.setWrapText(true);
        lblHireDate.setWrapText(true);
        lblRole.setWrapText(true);
    }

    public void setUser(User user)  {
        UserSession userSession = UserSession.getInstance();
        String userName = userSession.getUsername();
        String roleName = userSession.getRole();
        try {
            user = userDAO.getUserByUsername(userName);

            if (user == null) {
                System.err.println("❌ Lỗi: Không tìm thấy user trong database!");
                return;
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi truy vấn user: " + e.getMessage());
            e.printStackTrace();
        }

        if (user == null) {
            System.err.println("❌ Lỗi: User truyền vào là null!");
            return;
        }

        lblUsername.setText("Username: " + user.getUsername());
        lblFullname.setText("Full Name: " + formatString(user.getFullname()));
        lblRole.setText("Role: " + roleName);
        lblPhone.setText("Phone: " + formatString(user.getPhone()));
        lblBirthday.setText("Birthday: " + formatDate(user.getBirthday()));
        lblAddress.setText("Address: " + formatString(user.getAddress()));
        lblHireDate.setText("Start working: " + formatDate(user.getHireDate()));
        setAvatar(user.getImagePath());

    }

    private String formatDate(Date date) {
        return (date != null) ? dateFormat.format(date) : "N/A";
    }

    private String formatString(String value) {
        return (value != null && !value.trim().isEmpty()) ? value : "N/A";
    }

    private void setAvatar(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            System.out.println("⚠️ Không có ảnh avatar, sử dụng ảnh mặc định.");
            imgAvatar.setImage(new Image(getClass().getResourceAsStream("/src/billiardsmanagement/images/avatars/user.png")));
        } else {
            File file = new File("BilliardsManagement/src/main/resources/src/billiardsmanagement/images/avatars/" + imagePath);
            if (file.exists()) {
                imgAvatar.setImage(new Image(file.toURI().toString()));
            } else {
                System.out.println("⚠️ Không tìm thấy ảnh avatar, sử dụng ảnh mặc định.");
                imgAvatar.setImage(new Image(getClass().getResourceAsStream("/src/billiardsmanagement/images/avatars/user.png")));
            }
        }
    }

    @FXML
    private void handleChangeUserInfo() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/users/changeUserInfo.fxml"));
            Parent root = loader.load();
            ChangeUserInfoController controller = loader.getController();
            controller.setUserData(user);

            Stage stage = new Stage();
            stage.setTitle("Change User Info");
            stage.setScene(new Scene(root));
            stage.show();
//            stage.setOnHidden(event -> refreshTable());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
