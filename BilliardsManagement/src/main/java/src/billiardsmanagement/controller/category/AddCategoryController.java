package src.billiardsmanagement.controller.category;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import src.billiardsmanagement.model.TestDBConnection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class AddCategoryController {
    @FXML
    private TextField txtName;
    @FXML
    private Label lblImagePath;

    private String uploadedImagePath = null; // Đường dẫn file đã upload

    @FXML
    private void handleAdd() {
        String name = txtName.getText();

        if (name.isEmpty()) {
            System.out.println("Please fill the name of the category.");
            return;
        }

        String finalImageName = "default.png"; // Sử dụng ảnh mặc định nếu không có ảnh tải lên

        if (uploadedImagePath != null && !uploadedImagePath.isEmpty()) {
            try {
                // Tạo thư mục đích nếu chưa tồn tại
                File destinationDir = new File("BilliardsManagement/src/main/resources/src/billiardsmanagement/images/category");
                if (!destinationDir.exists()) {
                    destinationDir.mkdirs();
                }

                // Đường dẫn nguồn và đích
                Path sourcePath = Paths.get(uploadedImagePath); // File ảnh được chọn
                finalImageName = sourcePath.getFileName().toString(); // Lấy tên file ảnh
                Path destinationPath = Paths.get(destinationDir.getAbsolutePath(), finalImageName);

                // Sao chép ảnh
                Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("File copied to: " + destinationPath.toString());
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error copying file: " + e.getMessage());
                return;
            }
        }

        try (Connection connection = TestDBConnection.getConnection()) {
            // Chèn category mới vào cơ sở dữ liệu
            String sql = "INSERT INTO category (category_name, image_path) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, name);
            statement.setString(2, finalImageName);

            statement.executeUpdate();
            System.out.println("Category added successfully!");

            // Đóng cửa sổ Add Category
            Stage stage = (Stage) txtName.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error adding category to database: " + e.getMessage());
        }
    }

    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Image File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(txtName.getScene().getWindow());
        if (file != null) {
            uploadedImagePath = file.getAbsolutePath(); // Lưu đường dẫn file nguồn
            lblImagePath.setText(file.getName()); // Hiển thị tên file trong giao diện
            System.out.println("Selected file: " + uploadedImagePath);
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) txtName.getScene().getWindow();
        stage.close();
    }
}
