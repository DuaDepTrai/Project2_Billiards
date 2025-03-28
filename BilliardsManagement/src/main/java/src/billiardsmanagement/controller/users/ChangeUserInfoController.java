package src.billiardsmanagement.controller.users;

import io.github.palexdev.materialfx.utils.SwingFXUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import src.billiardsmanagement.dao.UserDAO;
import src.billiardsmanagement.model.NotificationStatus;
import src.billiardsmanagement.model.User;
import src.billiardsmanagement.model.TestDBConnection;
import src.billiardsmanagement.model.UserSession;
import src.billiardsmanagement.service.NotificationService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Optional;

public class ChangeUserInfoController {
    @FXML private TextField txtPassword;
    @FXML private TextField txtRePassword;
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
        UserSession userSession = UserSession.getInstance();
        String userName = userSession.getUsername();
        this.userId = userSession.getUserId();
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
//        this.userId = user.getId();
//        if (user == null) {
//            System.out.println("Error: user is null in setUserData!");
//            return;
//        }
//        System.out.println("User ID: " + user.getId());

        txtFullname.setText(user.getFullname());
        txtPassword.setText(user.getPlainPassword()); // Lưu ý: cần truyền password chưa hash từ DB
        txtRePassword.setText(user.getPlainPassword());
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


    @FXML
    private void handleUpdate() {
        String fullname = txtFullname.getText();
        String password = txtPassword.getText() != null ? txtPassword.getText().trim() : "";
        String rePassword = txtRePassword.getText() != null ? txtRePassword.getText().trim() : "";
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
            // Kiểm tra số điện thoại đã tồn tại chưa
            String checkPhoneSql = "SELECT COUNT(*) FROM users WHERE phone = ? AND user_id != ?";
            PreparedStatement checkPhoneStmt = connection.prepareStatement(checkPhoneSql);
            checkPhoneStmt.setString(1, phone);
            checkPhoneStmt.setInt(2, userId); // Loại trừ user hiện tại
            ResultSet checkPhoneResult = checkPhoneStmt.executeQuery();

            if (checkPhoneResult.next() && checkPhoneResult.getInt(1) > 0) {
                showAlert("Duplicate Phone Number", "This phone number already exists.");
                return;
            }

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

            final String imageNameToSave = finalImageName;

            // Hiển thị hộp thoại xác nhận trước khi thêm user
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Update User Information");
            alert.setHeaderText("Are you sure you want to update this user?");
            alert.setContentText("Full Name: " + fullname);

            ButtonType buttonYes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
            ButtonType buttonNo = new ButtonType("No", ButtonBar.ButtonData.NO);
            alert.getButtonTypes().setAll(buttonYes, buttonNo);

            alert.showAndWait().ifPresent(result -> {
                if (result == buttonYes) {
                    try {
                        userDAO.changeUserInfo(userId, hashedPassword, fullname, phone, sqlBirthday, address, imageNameToSave);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

//            userDAO.changeUserInfo(userId, hashedPassword, fullname, phone, sqlBirthday, address, finalImageName);

            System.out.println("User updated successfully!");
            Stage stage = (Stage) txtFullname.getScene().getWindow();
            stage.close();

        } catch (SQLIntegrityConstraintViolationException e) {
            if (e.getMessage().contains("users_unique_1")) { // Kiểm tra lỗi cụ thể từ MySQL
                showAlert("Duplicate Entry", "Phone number already exists. Please use a different one.");
            } else {
                showAlert("Database Error", "An error occurred: " + e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Image File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        File file = fileChooser.showOpenDialog(txtFullname.getScene().getWindow());
        if (file != null) {
            try {
                // Đọc ảnh gốc
                Image originalImage = new Image(file.toURI().toString());

                // Xử lý crop ảnh thành tỷ lệ 1:1
                Image croppedImage = cropToSquare(originalImage);

                // Lưu ảnh sau khi crop
                File savedFile = saveCroppedImage(croppedImage, file.getName());

                // Cập nhật đường dẫn ảnh đã lưu
                uploadedImagePath = savedFile.getAbsolutePath();
                lblImagePath.setText(savedFile.getName());

                System.out.println("Cropped and saved image: " + uploadedImagePath);

                // Transfer the cropped image to the target directory
                copyCroppedImageToTarget(savedFile);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void copyCroppedImageToTarget(File croppedImageFile) {
        try {
            // Define source path (cropped image location)
            Path sourcePath = croppedImageFile.toPath();

            // Define target directory (inside /target)
            Path targetDir = Paths.get("BilliardsManagement/target/classes/src/billiardsmanagement/images/avatars");
            Path targetPath = targetDir.resolve(croppedImageFile.getName());

            // Ensure directory exists before copying
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            // Copy image to target folder (overwrite if exists)
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("✅ Image successfully copied to target: " + targetPath);
        } catch (IOException e) {
            System.out.println("❌ Error copying image to target: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Unexpected error: " + e.getMessage());
        }
    }


    private Image cropToSquare(Image image) {
        double width = image.getWidth();
        double height = image.getHeight();
        double size = Math.min(width, height); // Lấy kích thước nhỏ nhất để tạo hình vuông

        double x = (width - size) / 2;  // Tính toạ độ X bắt đầu crop
        double y = (height - size) / 2; // Tính toạ độ Y bắt đầu crop

        PixelReader pixelReader = image.getPixelReader();
        WritableImage croppedImage = new WritableImage(pixelReader, (int) x, (int) y, (int) size, (int) size);

        return croppedImage;
    }

    private File saveCroppedImage(Image image, String originalFileName) throws IOException {
        File directory = new File("cropped_images"); // Thư mục lưu ảnh crop
        if (!directory.exists()) {
            directory.mkdir();
        }

        String outputFileName = "cropped_" + originalFileName;
        File outputFile = new File(directory, outputFileName);

        // Lưu ảnh dưới dạng PNG
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        ImageIO.write(bufferedImage, "png", outputFile);

        return outputFile;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) txtFullname.getScene().getWindow();
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