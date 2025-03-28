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
import java.util.List;
import java.sql.Date;
import java.util.Optional;

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
    private TableColumn<User, Integer> sttColumn;
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

        setupBirthdayPickerFormat();
        setupUpdateBirthdayPickerFormat();
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

        // Lắng nghe khi người dùng nhập vào DatePicker
        birthdayPicker.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() == 10) { // Chỉ kiểm tra khi đủ 10 ký tự (dd/MM/yyyy)
                if (isValidDateFormat(newValue)) {
                    try {
                        LocalDate date = LocalDate.parse(newValue, formatter);

                        // Kiểm tra giới hạn min/max
                        LocalDate today = LocalDate.now();
                        LocalDate minDate = today.minusYears(100);

                        if (date.isAfter(today) || date.isBefore(minDate)) {
                            NotificationService.showNotification("Error", "Date must be between " +
                                            minDate.format(formatter) + " and " + today.format(formatter),
                                    NotificationStatus.Error);
                            birthdayPicker.getEditor().clear();
                        } else {
                            birthdayPicker.setValue(date); // Cập nhật giá trị DatePicker
                        }

                    } catch (Exception e) {
                        birthdayPicker.getEditor().clear();
                    }
                } else {
                    NotificationService.showNotification("Error", "Invalid date format (DD/MM/YYYY)",
                            NotificationStatus.Error);
                    birthdayPicker.getEditor().clear();
                }
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

        // Lắng nghe khi người dùng nhập vào DatePicker
        updateBirthdayPicker.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() == 10) { // Chỉ kiểm tra khi đủ 10 ký tự (dd/MM/yyyy)
                if (isValidDateFormat(newValue)) {
                    try {
                        LocalDate date = LocalDate.parse(newValue, formatter);

                        // Kiểm tra giới hạn min/max
                        LocalDate today = LocalDate.now();
                        LocalDate minDate = today.minusYears(100);

                        if (date.isAfter(today) || date.isBefore(minDate)) {
                            NotificationService.showNotification("Error", "Date must be between " +
                                            minDate.format(formatter) + " and " + today.format(formatter),
                                    NotificationStatus.Error);
                            updateBirthdayPicker.getEditor().clear();
                        } else {
                            updateBirthdayPicker.setValue(date); // Cập nhật giá trị DatePicker
                        }

                    } catch (Exception e) {
                        updateBirthdayPicker.getEditor().clear();
                    }
                } else {
                    NotificationService.showNotification("Error", "Invalid date format (DD/MM/YYYY)",
                            NotificationStatus.Error);
                    updateBirthdayPicker.getEditor().clear();
                }
            }
        });
    }

    private void setupTableColumns() {
        sttColumn.setCellValueFactory(
                cellData -> new ReadOnlyObjectWrapper<>(customerTableView.getItems().indexOf(cellData.getValue()) + 1));
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
        totalPlaytimeCol.prefWidthProperty().bind(customerTableView.widthProperty().multiply(0.1)); // 5% tổng chiều
        // rộng
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
                // updatePlaytimeLabel.setText(String.format("%.2f",
                // newSelection.getTotalPlaytime()));

                // Xử lý birthday
                java.sql.Date birthday = (java.sql.Date) newSelection.getBirthday();
                if (birthday != null) {
                    LocalDate localDate = birthday.toLocalDate(); // Chuyển java.sql.Date -> LocalDate
                    updateBirthdayPicker.setValue(localDate);
                    updateBirthdayPicker.getEditor()
                            .setText(localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
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
            // playtimeLabel.setText("0.00");

            // Hide update form if visible
            updateForm.setVisible(false);

            // Show add customer form
            customerForm.setVisible(true);
        }
    }

    @FXML
    private void handleSaveCustomer(ActionEvent event) {
        try {
            String name = nameField.getText();
            String phone = phoneField.getText();
            LocalDate birthday = birthdayPicker.getValue();
            String address = addressField.getText();

            // Ensure null values don't cause errors with trim()
            name = (name != null) ? name.trim() : "";
            phone = (phone != null) ? phone.trim() : "";
            address = (address != null && !address.trim().isEmpty()) ? address.trim() : null;

            // Check if the birthday is empty or invalid
            String birthdayStr = birthdayPicker.getEditor().getText();
            if (birthdayStr != null && !birthdayStr.trim().isEmpty()
                    && (birthday == null || !isValidDateFormat(birthdayStr.trim()))) {
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

            // Show confirmation dialog
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Add Customer");
            confirmAlert.setHeaderText("Are you sure you want to add this customer?");
            confirmAlert.setContentText("Name: " + name + "\n" +
                    "Phone: " + phone + "\n" +
                    "Birthday: "
                    + (birthday != null ? birthday.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Not specified")
                    + "\n" +
                    "Address: " + (address != null ? address : "Not specified"));

            ButtonType buttonConfirm = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
            ButtonType buttonCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirmAlert.getButtonTypes().setAll(buttonConfirm, buttonCancel);

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == buttonConfirm) {
                // Convert birthday if valid
                Date sqlBirthday = (birthdayStr != null && !birthdayStr.trim().isEmpty() && birthday != null)
                        ? Date.valueOf(birthday)
                        : null;

                // Create and save customer
                Customer customer = new Customer();
                customer.setName(name);
                customer.setPhone(phone);
                customer.setBirthday(sqlBirthday);
                if (address != null) {
                    customer.setAddress(address);
                }

                customerDAO.addCustomer(customer);
                NotificationService.showNotification("Success", "Customer added successfully",
                        NotificationStatus.Success);

                // Hide form and refresh table
                customerForm.setVisible(false);
                refreshTable();
            }
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
        // playtimeLabel.setText("0.00");
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

            String name = updateNameField.getText();
            String phone = updatePhoneField.getText();
            LocalDate birthday = updateBirthdayPicker.getValue();
            String address = updateAddressField.getText();

            // Ensure null values don't cause errors with trim()
            name = (name != null) ? name.trim() : "";
            phone = (phone != null) ? phone.trim() : "";
            address = (address != null && !address.trim().isEmpty()) ? address.trim() : null;

            // Check if the birthday is empty or invalid
            String birthdayStr = updateBirthdayPicker.getEditor().getText();
            if (birthdayStr != null && !birthdayStr.trim().isEmpty()
                    && (birthday == null || !isValidDateFormat(birthdayStr.trim()))) {
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

            // Show confirmation dialog
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Update Customer");
            confirmAlert.setHeaderText("Are you sure you want to update this customer?");
            confirmAlert.setContentText("Current Information:\n" +
                    "Name: " + selectedCustomer.getName() + "\n" +
                    "Phone: " + selectedCustomer.getPhone() + "\n" +
                    "Birthday: "
                    + (selectedCustomer.getBirthday() != null
                    ? new SimpleDateFormat("dd/MM/yyyy").format(selectedCustomer.getBirthday())
                    : "Not specified")
                    + "\n" +
                    "Address: "
                    + (selectedCustomer.getAddress() != null ? selectedCustomer.getAddress() : "Not specified") + "\n\n"
                    +
                    "New Information:\n" +
                    "Name: " + name + "\n" +
                    "Phone: " + phone + "\n" +
                    "Birthday: "
                    + (birthday != null ? birthday.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Not specified")
                    + "\n" +
                    "Address: " + (address != null ? address : "Not specified"));

            ButtonType buttonConfirm = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
            ButtonType buttonCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirmAlert.getButtonTypes().setAll(buttonConfirm, buttonCancel);

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == buttonConfirm) {
                // Update customer object
                selectedCustomer.setName(name);
                selectedCustomer.setPhone(phone);

                // Only set birthday if it's valid and not empty
                if (birthdayStr != null && !birthdayStr.trim().isEmpty() && birthday != null) {
                    selectedCustomer.setBirthday(Date.valueOf(birthday));
                }

                // Only set address if it's not empty
                if (address != null) {
                    selectedCustomer.setAddress(address);
                }

                // Save to database
                customerDAO.updateCustomer(selectedCustomer);

                // Hide update form
                updateForm.setVisible(false);

                // Refresh table and show success message
                refreshTable();
                NotificationService.showNotification("Success", "Customer updated successfully",
                        NotificationStatus.Success);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate.parse(date, formatter);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}