package src.billiardsmanagement.controller.orders;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import src.billiardsmanagement.dao.CustomerDAO;
import src.billiardsmanagement.dao.OrderDAO;
import src.billiardsmanagement.model.Customer;
import src.billiardsmanagement.model.Order;

public class AddOrderController {
    @FXML
    private TextField nameField;
    @FXML
    private TextField phoneField;

    public void saveOrder(ActionEvent actionEvent) {
        try {
            // Lấy thông tin từ các trường
            String customerName = nameField.getText();
            String customerPhone = phoneField.getText();

            // Kiểm tra dữ liệu đầu vào
            if (customerName == null || customerName.trim().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng nhập tên khách hàng.");
            }
            if (customerPhone == null || customerPhone.trim().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng nhập số điện thoại khách hàng.");
            }

            // Tạo đối tượng Customer
            CustomerDAO customerDAO = new CustomerDAO();
            Customer customer = new Customer();
            customer.setName(customerName);
            customer.setPhone(customerPhone);

            // Thêm khách hàng vào cơ sở dữ liệu
            customerDAO.addCustomer(customer);

            // Lấy ID khách hàng vừa thêm
            Integer customerId = customer.getCustomerId();
            if (customerId == null) {
                throw new IllegalStateException("Không thể lấy ID của khách hàng sau khi thêm.");
            }



            // Hiển thị thông báo thành công
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Khách hàng được thêm thành công");

            // Đóng cửa sổ sau khi lưu
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.close();

        } catch (IllegalArgumentException e) {
            // Hiển thị lỗi đầu vào không hợp lệ
            showAlert(Alert.AlertType.ERROR, "Lỗi", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            // Hiển thị lỗi không xác định
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Đã xảy ra lỗi khi lưu đơn hàng. Vui lòng thử lại.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
