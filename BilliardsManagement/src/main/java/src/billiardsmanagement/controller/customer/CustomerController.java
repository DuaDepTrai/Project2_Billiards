package src.billiardsmanagement.controller.customer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import src.billiardsmanagement.dao.CustomerDAO;
import src.billiardsmanagement.model.Customer;
import src.billiardsmanagement.service.NotificationService;
import src.billiardsmanagement.model.NotificationStatus;
import javafx.event.ActionEvent;
import java.util.List;

public class CustomerController {

    @FXML
    private TableView<Customer> customerTableView;
    @FXML
    private TableColumn<Customer, Integer> customerIdCol;
    @FXML
    private TableColumn<Customer, String> nameCol;
    @FXML
    private TableColumn<Customer, String> phoneCol;
    @FXML
    private TableColumn<Customer, Double> totalPlaytimeCol;

    @FXML
    private TextField searchField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField phoneField;
    @FXML
    private Label playtimeLabel;

    @FXML
    private Button addCustomerBtn;
    @FXML
    private Button updateCustomerBtn;
    @FXML
    private Button removeCustomerBtn;
    @FXML
    private Button searchBtn;

    @FXML
    private VBox customerForm;

    @FXML
    private VBox updateForm;

    @FXML
    private TextField updateNameField;

    @FXML
    private TextField updatePhoneField;

    @FXML
    private Label updatePlaytimeLabel;

    private ObservableList<Customer> customerList = FXCollections.observableArrayList();
    private CustomerDAO customerDAO = new CustomerDAO();

    @FXML
    public void initialize() {
        setupTableColumns();
        setupSearchField();
        loadCustomers();
        setupTableSelection();

        // Initially hide both forms
        customerForm.setVisible(false);
        updateForm.setVisible(false);
    }

    private void setupTableColumns() {
        customerIdCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        totalPlaytimeCol.setCellValueFactory(new PropertyValueFactory<>("totalPlaytime"));

        // Format total playtime to show 2 decimal places
        totalPlaytimeCol.setCellFactory(column -> new TableCell<Customer, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });
    }

    private void setupSearchField() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                loadCustomers();
            } else {
                filterCustomers(newValue);
            }
        });
    }

    private void setupTableSelection() {
        customerTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Hide add form if visible
                customerForm.setVisible(false);

                // Show update form with current values
                updateNameField.setText(newSelection.getName());
                updatePhoneField.setText(newSelection.getPhone());
                updatePlaytimeLabel.setText(String.format("%.2f", newSelection.getTotalPlaytime()));
                updateForm.setVisible(true);
            } else {
                // Hide update form when no selection
                updateForm.setVisible(false);
            }
        });
    }

    @FXML
    private void handleAddCustomer(ActionEvent event) {
        // Clear form fields
        nameField.clear();
        phoneField.clear();
        playtimeLabel.setText("0.00");

        // Hide update form if visible
        updateForm.setVisible(false);

        // Show add customer form
        customerForm.setVisible(true);
    }

    @FXML
    private void handleSaveCustomer(ActionEvent event) {
        try {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();

            // Validate input
            if (name.isEmpty() || phone.isEmpty()) {
                NotificationService.showNotification("Error", "Please fill in all required fields",
                        NotificationStatus.Error);
                return;
            }

            // Validate phone number format
            if (!phone.matches("^0(3[2-9]|5[2689]|7[0-9]|8[1-9]|9[0-9])\\d{7}")) {
                NotificationService.showNotification("Error", "Invalid phone number format", NotificationStatus.Error);
                return;
            }

            // Check if phone exists
            if (customerDAO.isPhoneExists(phone)) {
                NotificationService.showNotification("Error", "This phone number already exists",
                        NotificationStatus.Error);
                return;
            }

            // Create and save customer
            Customer customer = new Customer();
            customer.setName(name);
            customer.setPhone(phone);
            customer.setTotalPlaytime(0.0); // Set initial playtime to 0

            customerDAO.addCustomer(customer);
            NotificationService.showNotification("Success", "Customer added successfully",
                    NotificationStatus.Success);

            // Hide form and refresh table
            customerForm.setVisible(false);
            refreshTable();

        } catch (Exception e) {
            NotificationService.showNotification("Error", "An error occurred: " + e.getMessage(),
                    NotificationStatus.Error);
        }
    }

    @FXML
    private void handleCancelAdd(ActionEvent event) {
        // Clear form fields
        nameField.clear();
        phoneField.clear();
        playtimeLabel.setText("0.00");

        // Hide the form
        customerForm.setVisible(false);

        // Clear table selection
        customerTableView.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleUpdateCustomer() {
        try {
            Customer selectedCustomer = customerTableView.getSelectionModel().getSelectedItem();
            if (selectedCustomer == null) {
                NotificationService.showNotification("Error", "Please select a customer to update",
                        NotificationStatus.Error);
                return;
            }

            String name = updateNameField.getText().trim();
            String phone = updatePhoneField.getText().trim();

            // Validate input
            if (name.isEmpty() || phone.isEmpty()) {
                NotificationService.showNotification("Error", "Name and phone number are required",
                        NotificationStatus.Error);
                return;
            }

            // Validate phone number format
            if (!phone.matches("^0(3[2-9]|5[2689]|7[0-9]|8[1-9]|9[0-9])\\d{7}")) {
                NotificationService.showNotification("Error", "Invalid phone number format", NotificationStatus.Error);
                return;
            }

            // Check if phone exists and it's not the current customer's phone
            if (!phone.equals(selectedCustomer.getPhone()) && customerDAO.isPhoneExists(phone)) {
                NotificationService.showNotification("Error", "This phone number already exists",
                        NotificationStatus.Error);
                return;
            }

            // Update customer object
            selectedCustomer.setName(name);
            selectedCustomer.setPhone(phone);
            // Keep the existing playtime value
            double currentPlaytime = selectedCustomer.getTotalPlaytime();
            selectedCustomer.setTotalPlaytime(currentPlaytime);

            // Save to database
            customerDAO.updateCustomer(selectedCustomer);

            // Hide update form
            updateForm.setVisible(false);

            // Refresh table and show success message
            refreshTable();
            NotificationService.showNotification("Success", "Customer updated successfully",
                    NotificationStatus.Success);

        } catch (Exception e) {
            NotificationService.showNotification("Error", "Failed to update customer: " + e.getMessage(),
                    NotificationStatus.Error);
        }
    }

    @FXML
    private void handleRemoveCustomer() {
        try {
            Customer selectedCustomer = customerTableView.getSelectionModel().getSelectedItem();
            if (selectedCustomer == null) {
                NotificationService.showNotification("Error", "Please select a customer to remove",
                        NotificationStatus.Error);
                return;
            }

            // Show confirmation dialog
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Deletion");
            confirmation.setHeaderText("Delete Customer");
            confirmation
                    .setContentText("Are you sure you want to delete customer: " + selectedCustomer.getName() + "?");

            if (confirmation.showAndWait().get() == ButtonType.OK) {
                // Remove from database
                customerDAO.removeCustomer(selectedCustomer.getCustomerId());

                // Hide update form
                updateForm.setVisible(false);

                // Refresh table and show success message
                refreshTable();
                NotificationService.showNotification("Success", "Customer removed successfully",
                        NotificationStatus.Success);
            }
        } catch (Exception e) {
            NotificationService.showNotification("Error", "Failed to remove customer: " + e.getMessage(),
                    NotificationStatus.Error);
        }
    }

    @FXML
    private void handleSearchCustomer() {
        String searchTerm = searchField.getText().trim().toLowerCase();
        filterCustomers(searchTerm);
    }

    private void filterCustomers(String searchTerm) {
        ObservableList<Customer> filteredList = FXCollections.observableArrayList();
        for (Customer customer : customerDAO.getAllCustomers()) {
            if (customer.getName().toLowerCase().contains(searchTerm) ||
                    customer.getPhone().contains(searchTerm)) {
                filteredList.add(customer);
            }
        }
        customerTableView.setItems(filteredList);
    }

    private void loadCustomers() {
        customerList.clear();
        customerList.addAll(customerDAO.getAllCustomers());
        customerTableView.setItems(customerList);
    }

    private void clearInputFields() {
        nameField.clear();
        phoneField.clear();
        playtimeLabel.setText("0.00");
        searchField.clear();
        customerTableView.getSelectionModel().clearSelection();
    }

    private void refreshTable() {
        List<Customer> customers = customerDAO.getAllCustomers();
        customerTableView.setItems(FXCollections.observableArrayList(customers));
    }
}