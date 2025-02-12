package src.billiardsmanagement.controller.orders;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import src.billiardsmanagement.dao.CustomerDAO;
import src.billiardsmanagement.model.Customer;

public class AddOrderController {
    @FXML
    private TextField nameField;
    @FXML
    private TextField phoneField;

    public void saveOrder(ActionEvent actionEvent) {
        try {
            // Get input from fields
            String customerName = nameField.getText();
            String customerPhone = phoneField.getText();

            // Validate input data
            if (customerName == null || customerName.trim().isEmpty()) {
                throw new IllegalArgumentException("Please enter the customer's name.");
            }
            if (customerPhone == null || customerPhone.trim().isEmpty()) {
                throw new IllegalArgumentException("Please enter the customer's phone number.");
            }

            // Create a Customer object
            CustomerDAO customerDAO = new CustomerDAO();
            Customer customer = new Customer();
            customer.setName(customerName);
            customer.setPhone(customerPhone);

            // Add customer to the database
            customerDAO.addCustomer(customer);

            // Get the customer ID
            Integer customerId = customer.getCustomerId();
            if (customerId == null) {
                throw new IllegalStateException("Failed to retrieve the customer ID after adding.");
            }

            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Success", "Customer has been added successfully.");

            // Close the window after saving
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.close();

        } catch (IllegalArgumentException e) {
            // Show validation error
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            // Show unexpected error message
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while saving the order. Please try again.");
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
