package src.billiardsmanagement.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import src.billiardsmanagement.dao.CustomerDAO;
import src.billiardsmanagement.model.Customer;
import src.billiardsmanagement.service.NotificationService;
import src.billiardsmanagement.model.NotificationStatus;

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
    private TextField playtimeField;

    @FXML
    private Button addCustomerBtn;
    @FXML
    private Button updateCustomerBtn;
    @FXML
    private Button removeCustomerBtn;
    @FXML
    private Button searchBtn;

    private ObservableList<Customer> customerList = FXCollections.observableArrayList();
    private CustomerDAO customerDAO = new CustomerDAO();

    @FXML
    public void initialize() {
        setupTableColumns();
        setupSearchField();
        loadCustomers();
        setupTableSelection();
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
                nameField.setText(newSelection.getName());
                phoneField.setText(newSelection.getPhone());
                playtimeField.setText(String.format("%.2f", newSelection.getTotalPlaytime()));
            }
        });
    }

    @FXML
    private void handleAddCustomer() {
        try {
            // Validate input fields
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String playtimeText = playtimeField.getText().trim();

            if (name.isEmpty() || phone.isEmpty() || playtimeText.isEmpty()) {
                NotificationService.showNotification("Validation Error", "All fields are required.",
                        NotificationStatus.Error);
                return;
            }

            // Validate phone number format
            if (!phone.matches("\\d{10}")) {
                NotificationService.showNotification("Validation Error", "Phone number must be 10 digits.",
                        NotificationStatus.Error);
                return;
            }

            // Check if phone number already exists
            if (customerDAO.isPhoneExists(phone)) {
                NotificationService.showNotification("Validation Error", "Phone number already exists.",
                        NotificationStatus.Error);
                return;
            }

            double playtime = Double.parseDouble(playtimeText);
            if (playtime < 0) {
                NotificationService.showNotification("Validation Error", "Playtime cannot be negative.",
                        NotificationStatus.Error);
                return;
            }

            Customer newCustomer = new Customer(name, phone, playtime);
            customerDAO.addCustomer(newCustomer);

            NotificationService.showNotification("Success", "Customer added successfully.", NotificationStatus.Success);
            clearInputFields();
            loadCustomers();

        } catch (NumberFormatException e) {
            NotificationService.showNotification("Validation Error", "Invalid playtime format.",
                    NotificationStatus.Error);
        } catch (Exception e) {
            NotificationService.showNotification("Error", "Failed to add customer: " + e.getMessage(),
                    NotificationStatus.Error);
        }
    }

    @FXML
    private void handleUpdateCustomer() {
        Customer selectedCustomer = customerTableView.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null) {
            NotificationService.showNotification("Error", "Please select a customer to update.",
                    NotificationStatus.Error);
            return;
        }

        try {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String playtimeText = playtimeField.getText().trim();

            if (name.isEmpty() || phone.isEmpty() || playtimeText.isEmpty()) {
                NotificationService.showNotification("Validation Error", "All fields are required.",
                        NotificationStatus.Error);
                return;
            }

            if (!phone.matches("\\d{10}")) {
                NotificationService.showNotification("Validation Error", "Phone number must be 10 digits.",
                        NotificationStatus.Error);
                return;
            }

            double playtime = Double.parseDouble(playtimeText);
            if (playtime < 0) {
                NotificationService.showNotification("Validation Error", "Playtime cannot be negative.",
                        NotificationStatus.Error);
                return;
            }

            selectedCustomer.setName(name);
            selectedCustomer.setPhone(phone);
            selectedCustomer.setTotalPlaytime(playtime);

            customerDAO.updateCustomer(selectedCustomer);
            NotificationService.showNotification("Success", "Customer updated successfully.",
                    NotificationStatus.Success);
            loadCustomers();

        } catch (NumberFormatException e) {
            NotificationService.showNotification("Validation Error", "Invalid playtime format.",
                    NotificationStatus.Error);
        } catch (Exception e) {
            NotificationService.showNotification("Error", "Failed to update customer: " + e.getMessage(),
                    NotificationStatus.Error);
        }
    }

    @FXML
    private void handleRemoveCustomer() {
        Customer selectedCustomer = customerTableView.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null) {
            NotificationService.showNotification("Error", "Please select a customer to remove.",
                    NotificationStatus.Error);
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Are you sure you want to remove this customer?");

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            try {
                customerDAO.removeCustomer(selectedCustomer.getCustomerId());
                NotificationService.showNotification("Success", "Customer removed successfully.",
                        NotificationStatus.Success);
                clearInputFields();
                loadCustomers();
            } catch (Exception e) {
                NotificationService.showNotification("Error", "Failed to remove customer: " + e.getMessage(),
                        NotificationStatus.Error);
            }
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
        playtimeField.clear();
        searchField.clear();
        customerTableView.getSelectionModel().clearSelection();
    }
}