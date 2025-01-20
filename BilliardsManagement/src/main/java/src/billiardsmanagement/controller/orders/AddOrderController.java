package src.billiardsmanagement.controller.orders;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import src.billiardsmanagement.dao.CustomerDAO;
import src.billiardsmanagement.dao.OrderDAO;
import src.billiardsmanagement.model.Order;

import java.util.Map;
import java.util.stream.Collectors;

public class AddOrderController {
    @FXML
    private ComboBox<String> customerIdComboBox;
    @FXML
    private TextField totalCostField;

    @FXML
    private ComboBox<String> statusComboBox;

    private Map<String, Integer> customerNameToIdMap; // Lưu trữ bản đồ customer_name -> customer_id

    @FXML
    private void initialize() {
        // Lấy danh sách customer_id và customer_name từ CustomerDAO
        CustomerDAO customerDAO = new CustomerDAO();
        Map<Integer, String> customerMap = customerDAO.getAllCustomerIds();

        // Đảo ngược Map từ customerMap (lấy tên khách hàng làm key và customer_id làm value)
        customerNameToIdMap = customerMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        // Thêm tên khách hàng vào ComboBox (để người dùng chọn tên khách hàng)
        customerIdComboBox.getItems().addAll(customerNameToIdMap.keySet());



    }

    public void saveOrder(ActionEvent actionEvent) {
        try {
            // Lấy dữ liệu từ các trường
            String customerName = customerIdComboBox.getValue();
            if (customerName == null) {
                throw new IllegalArgumentException("Vui lòng chọn Customer.");
            }

            // Lấy customer_id từ customerName (sử dụng customerNameToIdMap)
            Integer customerId = customerNameToIdMap.get(customerName);
            if (customerId == null) {
                throw new IllegalArgumentException("Không tìm thấy Customer ID.");
            }

            // Kiểm tra tổng chi phí
            double totalCost;
            try {
                totalCost = Double.parseDouble(totalCostField.getText());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Tổng chi phí phải là một số hợp lệ.");
            }

            // Kiểm tra trạng thái
            String status = statusComboBox.getValue();
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
