package src.billiardsmanagement.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import src.billiardsmanagement.dao.CustomerDAO;
import src.billiardsmanagement.model.Customer;

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
    private Button addCustomerBtn;
    @FXML
    private Button updateCustomerBtn;
    @FXML
    private Button removeCustomerBtn;
    @FXML
    private Button searchBtn;

    @FXML
    private TextField searchField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField playtimeField;

    private ObservableList<Customer> customerList = FXCollections.observableArrayList();
    private CustomerDAO customerDAO = new CustomerDAO();

    @FXML
    public void initialize() {
        // Thiết lập các cột trong TableView
        customerIdCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        totalPlaytimeCol.setCellValueFactory(new PropertyValueFactory<>("totalPlaytime"));

        // Thêm hành động cho các nút
        addCustomerBtn.setOnAction(event -> handleAddCustomer());
        updateCustomerBtn.setOnAction(event -> handleUpdateCustomer());
        removeCustomerBtn.setOnAction(event -> handleRemoveCustomer());
        searchBtn.setOnAction(event -> handleSearchCustomer());

        handleViewAllCustomers();
    }

    @FXML
    private void handleAddCustomer() {
        String name = nameField.getText();
        String phone = phoneField.getText();
        double totalPlaytime;

        try {
            totalPlaytime = Double.parseDouble(playtimeField.getText());
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Total Playtime must be a number.");
            return;
        }

        Customer newCustomer = new Customer(name, phone);
        customerDAO.addCustomer(newCustomer);
        customerList.add(newCustomer);

        clearInputFields();
        handleViewAllCustomers();
    }

    @FXML
    private void handleUpdateCustomer() {
        Customer selectedCustomer = customerTableView.getSelectionModel().getSelectedItem();
        if (selectedCustomer != null) {
            selectedCustomer.setName(nameField.getText());
            selectedCustomer.setPhone(phoneField.getText());
            try {
                selectedCustomer.setTotalPlaytime(Double.parseDouble(playtimeField.getText()));
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Total Playtime must be a number.");
                return;
            }
            customerDAO.updateCustomer(selectedCustomer);
            handleViewAllCustomers();
        }
    }

    @FXML
    private void handleRemoveCustomer() {
        Customer selectedCustomer = customerTableView.getSelectionModel().getSelectedItem();
        if (selectedCustomer != null) {
            customerDAO.removeCustomer(selectedCustomer.getCustomerId());
            customerList.remove(selectedCustomer);
            handleViewAllCustomers();
        }
    }

    @FXML
    private void handleSearchCustomer() {
        String searchTerm = searchField.getText().toLowerCase();
        ObservableList<Customer> filteredList = FXCollections.observableArrayList();

        for (Customer customer : customerDAO.getAllCustomers()) {
            if (customer.getName().toLowerCase().contains(searchTerm)) {
                filteredList.add(customer);
            }
        }

        customerTableView.setItems(filteredList);
    }

    @FXML
    private void handleViewAllCustomers() {
        customerList.clear();
        customerList.addAll(customerDAO.getAllCustomers());
        customerTableView.setItems(customerList);
    }

    private void clearInputFields() {
        nameField.clear();
        phoneField.clear();
        playtimeField.clear();
        searchField.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}