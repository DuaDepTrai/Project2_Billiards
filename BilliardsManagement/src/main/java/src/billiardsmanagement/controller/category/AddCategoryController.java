package src.billiardsmanagement.controller.category;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import src.billiardsmanagement.dao.CategoryDAO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class AddCategoryController {
    @FXML
    private TextField txtName;
    @FXML
    private Label lblImagePath;

    private String uploadedImagePath = null; // Đường dẫn file đã upload
    private CategoryDAO categoryDAO = new CategoryDAO();

    @FXML
    private void handleAdd() throws IOException {
        String name = txtName.getText();

        if (name.isEmpty()) {
            System.out.println("Please fill the name of the category.");
            return;
        }

//        String finalImageName = "default.png"; // Sử dụng ảnh mặc định nếu không có ảnh tải lên
//
//        if (uploadedImagePath != null && !uploadedImagePath.isEmpty()) {
//            try {
//                File destinationDir = new File("BilliardsManagement/src/main/resources/src/billiardsmanagement/images/category");
//                if (!destinationDir.exists()) {
//                    destinationDir.mkdirs();
//                }
//
//                Path sourcePath = Paths.get(uploadedImagePath); // File ảnh được chọn
//                finalImageName = sourcePath.getFileName().toString(); // Lấy tên file ảnh
//                Path destinationPath = Paths.get(destinationDir.getAbsolutePath(), finalImageName);
//
//                // Sao chép ảnh
//                Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
//            } catch (IOException e) {
//                e.printStackTrace();
//                System.out.println("Error copying file: " + e.getMessage());
//                return;
//            }
//        }

        // Thêm danh mục mới vào DB
        categoryDAO.addCategory(name);
        System.out.println("Category added successfully!");

        // Tìm controller của CategoryController và gọi refreshTable()
        Stage stage = (Stage) txtName.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/category/category.fxml"));
        Parent root = loader.load();
        CategoryController categoryController = loader.getController();
        categoryController.refreshTable();

        // Đóng cửa sổ Add Category
        stage.close();
    }

    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Image File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

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