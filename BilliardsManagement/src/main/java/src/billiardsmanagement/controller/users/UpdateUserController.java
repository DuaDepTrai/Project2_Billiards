package src.billiardsmanagement.controller.users;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import src.billiardsmanagement.dao.UserDAO;
import src.billiardsmanagement.model.NotificationStatus;
import src.billiardsmanagement.model.User;
import src.billiardsmanagement.model.TestDBConnection;
import src.billiardsmanagement.service.NotificationService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Optional;

public class UpdateUserController {
    @FXML private TextField txtUsername;
    @FXML private TextField txtPassword;
    @FXML private TextField txtRePassword;
    @FXML private ComboBox<String> comboRole;
    @FXML
    private TextField txtFullname;
    @FXML
    private TextField txtPhone;
    @FXML
    private DatePicker txtBirthday;
    @FXML
    private TextField txtAddress;
    @FXML
    private Label lblImagePath;

    private String imagePath;
    private int userId;  // Biến để lưu trữ user_id
    private String uploadedImagePath = null; // Đường dẫn file đã upload
    private UserDAO userDAO = new UserDAO();

    public void initialize() {
        loadRoles();
        setupDatePickerFormat();

        // Giới hạn ngày sinh: tối đa là ngày hiện tại, tối thiểu là 100 năm trước
        txtBirthday.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                LocalDate minDate = today.minusYears(100);

                // Nếu ngày ngoài khoảng min/max thì disable
                setDisable(date.isAfter(today) || date.isBefore(minDate));

                // Tô màu cho ngày bị vô hiệu hóa
                if (date.isAfter(today) || date.isBefore(minDate)) {
                    setStyle("-fx-background-color: #ffcccc;");
                }
            }
        });

        // Đặt giá trị min/max cho DatePicker
        txtBirthday.setValue(null);
    }

    private void loadRoles() {
        try (Connection connection = TestDBConnection.getConnection()) {
            String sql = "SELECT role_name FROM roles";
            ResultSet resultSet = connection.createStatement().executeQuery(sql);
            while (resultSet.next()) {
                comboRole.getItems().add(resultSet.getString("role_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupDatePickerFormat() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        txtBirthday.setConverter(new javafx.util.StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return (date != null) ? date.format(formatter) : "";
            }

            @Override
            public LocalDate fromString(String text) {
                if (text == null || text.trim().isEmpty()) {
                    return null;
                }
                try {
                    return LocalDate.parse(text, formatter);
                } catch (Exception e) {
                    return null;
                }
            }
        });

        // Lắng nghe khi người dùng nhập vào DatePicker
        txtBirthday.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() == 10) { // Chỉ kiểm tra khi đủ 10 ký tự (dd/MM/yyyy)
                if (isValidDateFormat(newValue)) {
                    try {
                        LocalDate date = LocalDate.parse(newValue, formatter);

                        // Kiểm tra giới hạn min/max
                        LocalDate today = LocalDate.now();
                        LocalDate minDate = today.minusYears(100);

                        if (date.isAfter(today) || date.isBefore(minDate)) {
                            NotificationService.showNotification("Error", "Date must be between " +
                                    minDate.format(formatter) + " and " + today.format(formatter), NotificationStatus.Error);
                            txtBirthday.getEditor().clear();
                        } else {
                            txtBirthday.setValue(date); // Cập nhật giá trị DatePicker
                        }

                    } catch (Exception e) {
                        txtBirthday.getEditor().clear();
                    }
                } else {
                    NotificationService.showNotification("Error", "Invalid date format (DD/MM/YYYY)", NotificationStatus.Error);
                    txtBirthday.getEditor().clear();
                }
            }
        });
    }

    public void setUserData(User user) {
        this.userId = user.getId();
        txtUsername.setText(user.getUsername());
        txtPassword.setText(user.getPlainPassword()); // Lưu ý: cần truyền password chưa hash từ DB
        txtRePassword.setText(user.getPlainPassword());
        comboRole.setValue(user.getRole());
        txtFullname.setText(user.getFullname());
        txtPhone.setText(user.getPhone());

        Date birthday = (Date) user.getBirthday();
        if (birthday != null) {
            LocalDate localDate = birthday.toLocalDate(); // Chuyển java.sql.Date -> LocalDate
            txtBirthday.setValue(localDate);
        } else {
            txtBirthday.setValue(null);
            txtBirthday.getEditor().clear();
        }

        txtAddress.setText(user.getAddress());
    }

//    @FXML
//    public void handleUploadImage() {
//        FileChooser fileChooser = new FileChooser();
//        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
//        File file = fileChooser.showOpenDialog(new Stage());
//
//        if (file != null) {
//            try {
//                Path targetDir = Paths.get("BilliardsManagement/src/main/resources/src/billiardsmanagement/images/avatars/");
//                if (!Files.exists(targetDir)) {
//                    Files.createDirectories(targetDir);
//                }
//
//                Path targetPath = targetDir.resolve(file.getName());
//                Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
//                imagePath = file.getName();
//                lblImagePath.setText(imagePath);
//            } catch (IOException e) {
//                e.printStackTrace();
//                showError("Error uploading image: " + e.getMessage());
//            }
//        } else {
//            lblImagePath.setText("No file selected");
//        }
//    }

    @FXML
    private void handleUpdate() {
        String username = txtUsername.getText();
        String password = txtPassword.getText() != null ? txtPassword.getText().trim() : "";
        String rePassword = txtRePassword.getText() != null ? txtRePassword.getText().trim() : "";
        String role = comboRole.getValue();
        String fullname = txtFullname.getText();
        String phone = txtPhone.getText();
        LocalDate birthday = txtBirthday.getValue();

        // Kiểm tra nếu DatePicker trống hoặc có dữ liệu không hợp lệ
        if (birthday == null) {
            showAlert("Invalid Date", "Please select a valid date.");
            return;
        }

        // Định dạng ngày thành chuỗi dd/MM/yyyy
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String birthdayStr = txtBirthday.getEditor().getText().trim();

        // Kiểm tra nếu ngày nhập vào sai định dạng
        if (!isValidDateFormat(birthdayStr)) {
            showAlert("Invalid Date Format", "Date must be in the format DD/MM/YYYY.");
            return;
        }

        // Chuyển sang java.sql.Date để lưu vào database
        Date sqlBirthday = Date.valueOf(birthday);

        String address = txtAddress.getText();

        if (!isValidUsername(username)) {
            showAlert("Invalid Username", "Username must be at least 6 characters long and contain only letters, numbers, '.', or '_'.");
            return;
        }

        if (password != null && !password.isEmpty() && !isValidPassword(password)) {
            showAlert("Invalid Password", "Password must be at least 6 characters long and contain letters, numbers, or special characters.");
            return;
        }

        if (!password.equals(rePassword)) {
            showAlert("Error", "Passwords do not match");
            return;
        }

        if (!isValidPhone(phone)) {
            showAlert("Invalid Phone", "Phone must be 10 numbers only, start with 09 | 08 | 07 | 05 | 03.");
            return;
        }

        try (Connection connection = TestDBConnection.getConnection()) {
            // Kiểm tra username đã tồn tại hay chưa
            String checkUsernameSql = "SELECT COUNT(*) FROM users WHERE username = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkUsernameSql);
            checkStmt.setString(1, username);
            ResultSet checkResult = checkStmt.executeQuery();

            if (checkResult.next() && checkResult.getInt(1) > 1) {
                showAlert("Duplicate Username", "Username already exists. Please choose a different username.");
                return; // Dừng lại nếu username đã tồn tại
            }

            String getRoleSql = "SELECT role_id FROM roles WHERE role_name = ?";
            PreparedStatement roleStmt = connection.prepareStatement(getRoleSql);
            roleStmt.setString(1, role);
            ResultSet resultSet = roleStmt.executeQuery();
            int role_id = resultSet.next() ? resultSet.getInt("role_id") : 0;


            String hashedPassword;
            // Nếu password trống, giữ nguyên mật khẩu hash cũ từ DB
            if (password == null || password.isEmpty()) {
                hashedPassword = userDAO.getHashedPassword(userId);
            } else {
                hashedPassword = hashPassword(password);
            }

            String finalImageName = "user.png"; // Sử dụng ảnh mặc định nếu không có ảnh tải lên

            if (uploadedImagePath != null && !uploadedImagePath.isEmpty()) {
                try {
                    File destinationDir = new File("BilliardsManagement/src/main/resources/src/billiardsmanagement/images/avatars");
                    if (!destinationDir.exists()) {
                        destinationDir.mkdirs();
                    }

                    Path sourcePath = Paths.get(uploadedImagePath); // File ảnh được chọn
                    finalImageName = sourcePath.getFileName().toString(); // Lấy tên file ảnh
                    Path destinationPath = Paths.get(destinationDir.getAbsolutePath(), finalImageName);

                    // Sao chép ảnh
                    Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Error copying file: " + e.getMessage());
                    return;
                }
            }


            userDAO.updateUser(userId, username, hashedPassword, role_id, fullname, phone, sqlBirthday, address, finalImageName);

            System.out.println("User updated successfully!");
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Image File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        File file = fileChooser.showOpenDialog(txtUsername.getScene().getWindow());
        if (file != null) {
            uploadedImagePath = file.getAbsolutePath(); // Lưu đường dẫn file nguồn
            lblImagePath.setText(file.getName()); // Hiển thị tên file trong giao diện
            System.out.println("Selected file: " + uploadedImagePath);
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) txtUsername.getScene().getWindow();
        stage.close();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean isValidUsername(String username) {
        return username.matches("^[a-zA-Z0-9_.]{6,}$");
    }

    private boolean isValidPassword(String password) {
        return password != null && password.matches("^.{6,}$");
    }

    private boolean isValidPhone(String phone) {
        return phone.matches("^(0[35789])\\d{8}$");
    }

    private boolean isValidDateFormat(String date) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate.parse(date, formatter);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}