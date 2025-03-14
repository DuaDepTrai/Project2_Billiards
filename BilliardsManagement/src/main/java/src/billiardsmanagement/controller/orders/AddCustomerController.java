package src.billiardsmanagement.controller.orders;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import src.billiardsmanagement.dao.CustomerDAO;
import src.billiardsmanagement.model.Customer;
import src.billiardsmanagement.model.NotificationStatus;
import src.billiardsmanagement.service.NotificationService;

public class AddCustomerController {
    @FXML
    private TextField nameField;
    @FXML
    private TextField phoneField;
    @FXML
    private Button saveButton;

    public void saveCustomer(ActionEvent actionEvent) {
        try {
            CustomerDAO customerDAO = new CustomerDAO();
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

            // Validate phone number format (10 digits)
            if (!customerPhone.matches("^0(3[2-9]|5[2689]|7[0-9]|8[1-9]|9[0-9])\\d{7}")) {
                throw new IllegalArgumentException("Phone number must follow the correct format (e.g., 0987654321).");
            }

            // Check if the phone number already exists in the database
            if (customerDAO.isPhoneExists(customerPhone)) {
                throw new IllegalArgumentException("This phone number already exists.");
            }

            // Create a Customer object
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

            // Show success notification
            NotificationService.showNotification("Success", "Customer added successfully!", NotificationStatus.Success);

            // Close the window after saving
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.close();

        } catch (IllegalArgumentException e) {
            // Show validation error notification
            NotificationService.showNotification("Error", e.getMessage(), NotificationStatus.Error);
        } catch (Exception e) {
            e.printStackTrace();
            // Show unexpected error notification
            NotificationService.showNotification("Error", "An error occurred while saving the order. Please try again.", NotificationStatus.Error);
        }
    }
}
