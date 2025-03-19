package src.billiardsmanagement.controller.customer;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import src.billiardsmanagement.dao.CustomerDAO;
import src.billiardsmanagement.model.Customer;
import src.billiardsmanagement.model.User;
import src.billiardsmanagement.service.NotificationService;
import src.billiardsmanagement.model.NotificationStatus;
import javafx.event.ActionEvent;

import java.io.Console;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
//import java.util.Date;
import java.util.List;
import java.sql.Date;



public class CustomerController {
    @FXML
    private TextField addressField;
    @FXML
    private DatePicker birthdayPicker;
    @FXML
    private TextField updateAddressField;
    @FXML
    private DatePicker updateBirthdayPicker;

    @FXML
    private TableColumn<User,Integer> sttColumn;
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
    private TableColumn<Customer, String> birthdayCol;
    @FXML
    private TableColumn<Customer, String> addressCol;

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
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    @FXML
    public void initialize() {
        setupTableColumns();
        setupSearchField();
        loadCustomers();
        setupTableSelection();

        // Initially hide both forms
        customerForm.setVisible(false);
        updateForm.setVisible(false);

        setupBirthdayPickerFormat();
        setupUpdateBirthdayPickerFormat();

        // Giới hạn ngày sinh: tối đa là ngày hiện tại, tối thiểu là 100 năm trước
        birthdayPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                LocalDate minDate = today.minusYears(100);

                // Nếu ngày ngoài khoảng min/max thì disable
                setDisable(date.isAfter(today) || date.isBefore(minDate));

                // Tô màu cho ngày bị vô hiệu hóa
                if (date.isAfter(today) || date.isBefore(minDate)) {
                    setStyle("-fx-background-color: #ffcccc;");
                }
            }
        });

        // Đặt giá trị min/max cho DatePicker
        birthdayPicker.setValue(null);

        updateBirthdayPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                LocalDate minDate = today.minusYears(100);

                // Nếu ngày ngoài khoảng min/max thì disable
                setDisable(date.isAfter(today) || date.isBefore(minDate));

                // Tô màu cho ngày bị vô hiệu hóa
                if (date.isAfter(today) || date.isBefore(minDate)) {
                    setStyle("-fx-background-color: #ffcccc;");
                }
            }
        });
        updateBirthdayPicker.setValue(null);
    }

    private void setupBirthdayPickerFormat() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        birthdayPicker.setConverter(new javafx.util.StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return (date != null) ? date.format(formatter) : "";
            }

            @Override
            public LocalDate fromString(String text) {
                if (text == null || text.trim().isEmpty()) {
                    return null;
                }
                try {
                    return LocalDate.parse(text, formatter);
                } catch (Exception e) {
                    return null;
                }
            }
        });

        // Lắng nghe sự kiện khi người dùng nhập vào DatePicker
        birthdayPicker.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (isValidDateFormat(newValue)) {
                birthdayPicker.setValue(LocalDate.parse(newValue, formatter)); // Cập nhật giá trị DatePicker
            }
        });
    }

    private void setupUpdateBirthdayPickerFormat() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        updateBirthdayPicker.setConverter(new javafx.util.StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return (date != null) ? date.format(formatter) : "";
            }

            @Override
            public LocalDate fromString(String text) {
                if (text == null || text.trim().isEmpty()) {
                    return null;
                }
                try {
                    return LocalDate.parse(text, formatter);
                } catch (Exception e) {
                    return null;
                }
            }
        });

        // Lắng nghe sự kiện khi người dùng nhập vào DatePicker
        updateBirthdayPicker.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (isValidDateFormat(newValue)) {
                updateBirthdayPicker.setValue(LocalDate.parse(newValue, formatter)); // Cập nhật giá trị DatePicker
            }
        });
    }

    private void setupTableColumns() {
        sttColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(customerTableView.getItems().indexOf(cellData.getValue()) + 1)
        );
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        totalPlaytimeCol.setCellValueFactory(new PropertyValueFactory<>("totalPlaytime"));
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));


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

        birthdayCol.setCellValueFactory(cellData -> {
            java.util.Date utilDate = cellData.getValue().getBirthday(); // Nếu trả về java.util.Date
            Date sqlDate = (utilDate != null) ? new Date(utilDate.getTime()) : null;
            String formattedDate = (sqlDate != null) ? dateFormat.format(sqlDate) : "";
            return new SimpleStringProperty(formattedDate);
        });

        birthdayCol.setCellFactory(column -> new TableCell<Customer, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
            }
        });

        customerTableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY); // Cho phép cột co giãn tự do

        sttColumn.prefWidthProperty().bind(customerTableView.widthProperty().multiply(0.05)); // 5% tổng chiều rộng
        nameCol.prefWidthProperty().bind(customerTableView.widthProperty().multiply(0.25)); // 5% tổng chiều rộng
        phoneCol.prefWidthProperty().bind(customerTableView.widthProperty().multiply(0.1)); // 5% tổng chiều rộng
        totalPlaytimeCol.prefWidthProperty().bind(customerTableView.widthProperty().multiply(0.1)); // 5% tổng chiều rộng
        birthdayCol.prefWidthProperty().bind(customerTableView.widthProperty().multiply(0.1)); // 5% tổng chiều rộng
        addressCol.prefWidthProperty().bind(customerTableView.widthProperty().multiply(0.38)); // 5% tổng chiều rộng

        // Căn trái
        nameCol.setStyle("-fx-alignment: CENTER-LEFT;");
        addressCol.setStyle("-fx-alignment: CENTER-LEFT;");


        // Căn giữa
        sttColumn.setStyle("-fx-alignment: CENTER;");
        phoneCol.setStyle("-fx-alignment: CENTER;");
        birthdayCol.setStyle("-fx-alignment: CENTER;");

        // Căn phải
        totalPlaytimeCol.setStyle("-fx-alignment: CENTER-RIGHT;");

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
//                updatePlaytimeLabel.setText(String.format("%.2f", newSelection.getTotalPlaytime()));

                // Xử lý birthday
                java.sql.Date birthday = (java.sql.Date) newSelection.getBirthday();
                if (birthday != null) {
                    LocalDate localDate = birthday.toLocalDate(); // Chuyển java.sql.Date -> LocalDate
                    updateBirthdayPicker.setValue(localDate);
                    updateBirthdayPicker.getEditor().setText(localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                } else {
                    updateBirthdayPicker.setValue(null);
                    updateBirthdayPicker.getEditor().clear();
                }
                updateAddressField.setText(newSelection.getAddress());

                updateForm.setVisible(true);
            } else {
                // Hide update form when no selection
                updateForm.setVisible(false);
            }
        });
    }

    @FXML
    private void handleAddCustomer(ActionEvent event) {
        if (customerForm.isVisible()) {
            // If form is visible, hide it
            customerForm.setVisible(false);
        } else {
            // If form is hidden, show it and clear fields
            nameField.clear();
            phoneField.clear();
            birthdayPicker.setValue(null);
            addressField.clear();
//            playtimeLabel.setText("0.00");

            // Hide update form if visible
            updateForm.setVisible(false);

            // Show add customer form
            customerForm.setVisible(true);
        }
    }

    @FXML
    private void handleSaveCustomer(ActionEvent event) {
        try {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            LocalDate birthday = birthdayPicker.getValue();
            String address = addressField.getText().trim();

            // Kiểm tra nếu ngày sinh trống hoặc sai định dạng
            String birthdayStr = birthdayPicker.getEditor().getText().trim();
            if (birthday == null || !isValidDateFormat(birthdayStr)) {
                NotificationService.showNotification("Error", "Date must be in the format DD/MM/YYYY.",
                        NotificationStatus.Error);
                return;
            }

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

            // Chuyển đổi LocalDate sang java.sql.Date để lưu vào database
            Date sqlBirthday = Date.valueOf(birthday);


            // Create and save customer
            Customer customer = new Customer();
            customer.setName(name);
            customer.setPhone(phone);
//            customer.setTotalPlaytime(0.0); // Set initial playtime to 0
            customer.setBirthday(sqlBirthday);
            customer.setAddress(address);

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
//        playtimeLabel.setText("0.00");
        birthdayPicker.setValue(null);
        addressField.clear();

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
            LocalDate birthday = updateBirthdayPicker.getValue();
            String address = updateAddressField.getText().trim();

            // Kiểm tra nếu ngày sinh trống hoặc sai định dạng
            String birthdayStr = updateBirthdayPicker.getEditor().getText().trim();
            if (birthday == null || !isValidDateFormat(birthdayStr)) {
                NotificationService.showNotification("Error", "Date must be in the format DD/MM/YYYY.",
                        NotificationStatus.Error);
                return;
            }

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

            // Chuyển đổi LocalDate sang java.sql.Date để lưu vào database
            Date sqlBirthday = Date.valueOf(birthday);


            // Update customer object
            selectedCustomer.setName(name);
            selectedCustomer.setPhone(phone);
            // Keep the existing playtime value
//            double currentPlaytime = selectedCustomer.getTotalPlaytime();
//            selectedCustomer.setTotalPlaytime(currentPlaytime);
            selectedCustomer.setBirthday(sqlBirthday);
            selectedCustomer.setAddress(address);

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

    private void refreshTable() {
        List<Customer> customers = customerDAO.getAllCustomers();
        customerTableView.setItems(FXCollections.observableArrayList(customers));
    }

    private boolean isValidDateFormat(String date) {
        if (date == null || date.trim().isEmpty()) {
            return false;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try {
            LocalDate.parse(date, formatter);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}