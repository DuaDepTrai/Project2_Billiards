package src.billiardsmanagement.controller.orders;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import src.billiardsmanagement.dao.CustomerDAO;
import src.billiardsmanagement.dao.OrderDAO;
import src.billiardsmanagement.model.Order;

import java.util.List;

public class AddOrderController {
    @FXML
    private ComboBox<Integer> customerIdComboBox; // ComboBox để hiển thị customer_id
    @FXML
    private TextField totalCostField;

    @FXML
    private ComboBox<String> statusComboBox;
    @FXML
    private void initialize() {
        // Lấy danh sách customer_id từ CustomerDAO và thêm vào ComboBox
        CustomerDAO customerDAO = new CustomerDAO();
        List<Integer> customerIds = customerDAO.getAllCustomerIds();
        customerIdComboBox.getItems().addAll(customerIds);
    }

    public void saveOrder(ActionEvent actionEvent) {
        try {
            // Lấy dữ liệu từ các trường
            Integer customerId = customerIdComboBox.getValue(); // Lấy giá trị từ ComboBox (nên kiểm tra giá trị null)
            if (customerId == null) {
                throw new IllegalArgumentException("Vui lòng chọn Customer ID.");
            }

            double totalCost;
            try {
                totalCost = Double.parseDouble(totalCostField.getText());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Tổng chi phí phải là một số hợp lệ.");
            }

            String status = statusComboBox.getValue(); // Lấy giá trị được chọn từ ComboBox
            if (status == null || status.isEmpty()) {
                throw new IllegalArgumentException("Vui lòng chọn trạng thái.");
            }

            // Tạo đối tượng Order và lưu vào DB
            Order newOrder = new Order(customerId, totalCost, status);
            OrderDAO orderDAO = new OrderDAO();
            orderDAO.addOrder(newOrder);

            // Đóng form sau khi lưu
            Stage stage = (Stage) customerIdComboBox.getScene().getWindow();
            stage.close();
        } catch (IllegalArgumentException e) {
            // Hiển thị thông báo lỗi cho người dùng
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            // Thêm xử lý lỗi nếu cần
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText("Đã xảy ra lỗi khi lưu dữ liệu.");
            alert.setContentText("Vui lòng thử lại sau.");
            alert.showAndWait();
        }
    }

}
