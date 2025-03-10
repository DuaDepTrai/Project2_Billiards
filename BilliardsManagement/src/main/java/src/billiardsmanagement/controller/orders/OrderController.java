package src.billiardsmanagement.controller.orders;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.stage.Stage;
import src.billiardsmanagement.controller.MainController;
import src.billiardsmanagement.dao.BookingDAO;
import src.billiardsmanagement.dao.CustomerDAO;
import src.billiardsmanagement.dao.OrderDAO;
import src.billiardsmanagement.dao.PoolTableDAO;
import src.billiardsmanagement.model.*;
import src.billiardsmanagement.service.NotificationService;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class OrderController implements Initializable {

    @FXML
    private TableColumn<Order, Integer> sttColumn;
    @FXML
    private TableColumn<Order, String> customerNameColumn;
    @FXML
    private TableColumn<Order, Double> totalCostColumn;
    @FXML
    private TableColumn<Order, String> orderStatusColumn;
    @FXML
    private TableColumn<Order, String> phoneCustomerColumn;
    @FXML
    private TableColumn<Order, String> nameTableColumn;
    @FXML
    private TableColumn<Order, Void> actionColumn; // Thêm khai báo này
    @FXML
    private TableColumn<Order, Date> dateColumn;
    @FXML
    private TableColumn<Order, String> managerColumn;
    @FXML
    private BorderPane mainPane; // Thêm khai báo này
    @FXML
    private TextField autoCompleteTextField;
    @FXML
    private TextField phoneTextField;
    @FXML
    private TableView<Order> orderTable;

    private Popup popup;
    private ListView<String> listView;

    private int orderID;
    private static int billNumberCount = OrderDAO.getBillNumberCount();

    private final Connection conn = DatabaseConnection.getConnection();
    private final OrderDAO orderDAO = new OrderDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final Map<String, Integer> customerNameToIdMap = new HashMap<>();
    private final PoolTableDAO poolTableDAO = new PoolTableDAO();
    private MainController mainController;
    public void setMainController(MainController mainController){
        this.mainController = mainController;;
    }

    public TableView<Order> getOrderTable() {
        return orderTable;
    }

    @FXML
    public void addOrder(ActionEvent actionEvent) {
        try {
            // Lấy thông tin user hiện tại từ UserSession
            UserSession userSession = UserSession.getInstance();
            if (userSession.getUserId() == 0) {
                throw new IllegalArgumentException("No user is currently logged in.");
            }

            Order newOrder = new Order();
            newOrder.setCustomerId(1); // Set customer_id mặc định là 1
            newOrder.setUserId(userSession.getUserId());

            orderDAO.addOrder(newOrder);
            Order orderLatest = orderDAO.getLatestOrderByCustomerId(1);

            int orderId = orderLatest.getOrderId();
            int totalRow = orderTable.getItems().size();
            int selectedIndex = orderTable.getItems().indexOf(newOrder);
            int billNo = totalRow - selectedIndex;
            System.out.println(orderId);
            loadOrderList();

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/src/billiardsmanagement/orders/forEachOrder.fxml"));
            Parent root = loader.load();
            StackPane contentArea = mainController.getContentArea();

            Stage stage = new Stage();
            stage.setTitle("Order Detail");
            stage.setScene(new Scene(root));
            stage.show();

            ForEachOrderController controller = loader.getController();
            controller.setOrderID(orderId);
            controller.setCustomerID(1);
            controller.setOrderTable(orderTable);
            controller.setBillNo(billNo);
            controller.initializeAllTables();
        } catch (IllegalArgumentException e) {
            NotificationService.showNotification("Validation Error", e.getMessage(), NotificationStatus.Error);
        } catch (Exception e) {
            e.printStackTrace();
            NotificationService.showNotification("Error", "An error occurred while saving the order. Please try again.",
                    NotificationStatus.Error);
        }
    }

    private void loadOrderList() {

        List<Order> orders = orderDAO.getAllOrders();
        orderTable.setItems(FXCollections.observableArrayList(orders));
    }

    public void loadCustomerNameToIdMap() {
        String query = "SELECT customer_id, name, phone FROM customers";
        try (PreparedStatement statement = DatabaseConnection.getConnection().prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            customerNameToIdMap.clear();
            while (resultSet.next()) {
                int id = resultSet.getInt("customer_id");
                String name = resultSet.getString("name");
                String phone = resultSet.getString("phone");
                String key = name + " - " + phone;
                customerNameToIdMap.put(key, id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadCustomerNameToIdMap();
        loadOrderList();
        orderTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(orderTable, Priority.ALWAYS);

        // Add scene size listener after scene is created
        mainPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obs2, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        mainPane.prefWidthProperty().bind(newScene.widthProperty());
                        mainPane.prefHeightProperty().bind(newScene.heightProperty());
                    }
                });
            }
        });

        totalCostColumn.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        sttColumn.setCellValueFactory(param -> {
            TableView<?> tableView = sttColumn.getTableView();
            int totalRows = tableView.getItems().size();
            int index = tableView.getItems().indexOf(param.getValue());
            return new SimpleIntegerProperty(totalRows - index).asObject();
        });
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        orderStatusColumn.setCellValueFactory(new PropertyValueFactory<>("orderStatus"));
        phoneCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("customerPhone"));
        nameTableColumn.setCellValueFactory(new PropertyValueFactory<>("currentTableName"));
        nameTableColumn.setCellFactory(column -> new TableCell<Order, String>() {
            private final Text text = new Text();

            {
                text.wrappingWidthProperty().bind(nameTableColumn.widthProperty());
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    text.setText(item);
                    setGraphic(text);
                }
            }
        });

        totalCostColumn.setCellFactory(param -> new TableCell<Order, Double>() {
            private final DecimalFormat df = new DecimalFormat("#,###");

            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : df.format(item));
            }
        });

        popup = new Popup();
        listView = new ListView<>();
        popup.getContent().add(listView);

        autoCompleteTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                List<String> filteredCustomers = CustomerDAO.fetchCustomersByPhone(newValue);
                listView.getItems().setAll(filteredCustomers);

                if (!filteredCustomers.isEmpty() && !popup.isShowing()) {
                    popup.show(autoCompleteTextField,
                            autoCompleteTextField.localToScreen(autoCompleteTextField.getBoundsInLocal()).getMinX(),
                            autoCompleteTextField.localToScreen(autoCompleteTextField.getBoundsInLocal()).getMaxY());
                }
            } else {
                popup.hide();
            }
        });

        listView.setOnMouseClicked(event -> {
            if (!listView.getSelectionModel().isEmpty()) {
                String selected = listView.getSelectionModel().getSelectedItem();
                autoCompleteTextField.setText(selected);
                popup.hide();
            }
        });

        autoCompleteTextField.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER:
                    if (!listView.getSelectionModel().isEmpty()) {
                        String selected = listView.getSelectionModel().getSelectedItem();
                        autoCompleteTextField.setText(selected);
                        popup.hide();
                    }
                    break;
                case ESCAPE:
                    popup.hide();
                    break;
                default:
                    break;
            }
        });

        // Bill No column
        sttColumn.setCellFactory(column -> {
            return new TableCell<Order, Integer>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                    } else {
                        setText(String.valueOf(item));
                        getStyleClass().add("bill-number");
                    }
                }
            };
        });

        // Table Name column
        nameTableColumn.setCellFactory(column -> {
            return new TableCell<Order, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                    } else {
                        setText(item);
                        getStyleClass().add("table-name-cell");
                    }
                }
            };
        });

        // Status column
        orderStatusColumn.setCellFactory(column -> {
            return new TableCell<Order, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                    } else {
                        setText(item);
                        if (item.equals("Playing")) {
                            getStyleClass().add("status-playing");
                        }
                    }
                }
            };
        });

        // Cost column formatting
        totalCostColumn.setCellFactory(column -> {
            return new TableCell<Order, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                    } else {
                        setText(String.format("%,.0f", item));
                    }
                }
            };
        });
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        managerColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final HBox container = new HBox(10); // spacing = 10

            private final Button printBtn = new Button();

            {
                // View button với icon EYE
                // Print button với icon PRINT
                FontAwesomeIconView printIcon = new FontAwesomeIconView(FontAwesomeIcon.PRINT);
                printIcon.setSize("16");
                printBtn.setGraphic(printIcon);
                printBtn.getStyleClass().add("action-button");
                // Delete button với icon TRASH
                // Add buttons to container
                container.setAlignment(Pos.CENTER);
                container.getChildren().addAll(printBtn);
                // Add button actions
                printBtn.setOnAction(event -> {
                    Order selectedOrder = getTableView().getItems().get(getIndex());
                    if (selectedOrder == null) {
                        NotificationService.showNotification("Error", "Please select an order to payment.",
                                NotificationStatus.Error);
                        return;
                    }
                    if (!"Finished".equals(selectedOrder.getOrderStatus())
                            && !"Paid".equals(selectedOrder.getOrderStatus())) {
                        NotificationService.showNotification("Access Denied",
                                "Bills can only be accessed when the order is in Finished or Paid status.",
                                NotificationStatus.Error);
                        return;
                    }
                    int totalRow = orderTable.getItems().size();
                    int selectedIndex = getIndex(); // Lấy chỉ số hàng
                    int billNo = totalRow - selectedIndex;
                    int orderId = selectedOrder.getOrderId();
                    FXMLLoader paymentLoader = new FXMLLoader(
                            getClass().getResource("/src/billiardsmanagement/bills/finalBill.fxml"));
                    Parent paymentRoot = null;
                    try {
                        paymentRoot = paymentLoader.load();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }


                    PaymentController paymentController = paymentLoader.getController();
                    paymentController.setOrderID(orderId);
                    paymentController.setBillNo(billNo);
                    paymentController.setBill(createBill(selectedOrder));

                    Stage stage = new Stage();
                    stage.setTitle("Payment Details");
                    stage.setScene(new Scene(paymentRoot));
                    stage.setOnHidden(e -> loadOrderList());
                    stage.show();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });

        orderTable.setOnMouseClicked(event -> {
            showItem(event);
        });
    }

    private Bill createBill(Order selectedOrder) {
        Order currentOrder = selectedOrder;
        if (currentOrder == null)
            return null;

        Bill bill = new Bill();
        bill.setCustomerName(currentOrder.getCustomerName() == null ? "Guest" : currentOrder.getCustomerName());
        bill.setCustomerPhone(currentOrder.getCustomerPhone() == null ? "GuestPhone" : currentOrder.getCustomerPhone());
        bill.setTotalCost(currentOrder.getTotalCost());
        return bill;
    }

    public void addCustomer(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/orders/addCustomer.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle("Add Customer");
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    public void billOrder(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/bills/finalBill.fxml"));
            Parent root = loader.load();

            PaymentController paymentController = loader.getController();

            // Lấy tất cả orders từ TableView
            List<Order> orders = orderTable.getItems();

            // Lọc ra các orders có status là "Finished" hoặc "Paid"
            List<Order> completedOrders = orders.stream()
                    .filter(order -> "Finished".equals(order.getOrderStatus())
                            || "Paid".equals(order.getOrderStatus()))
                    .collect(Collectors.toList());

            if (completedOrders.isEmpty()) {
                NotificationService.showNotification(
                        "No Bills Available",
                        "There are no completed orders to show bills for.",
                        NotificationStatus.Information);
                return;
            }

            // Tạo scene mới để hiển thị danh sách bills
            VBox billsContainer = new VBox(10); // spacing = 10
            billsContainer.setPadding(new Insets(15));
            ScrollPane scrollPane = new ScrollPane(billsContainer);

            // Thêm từng bill vào container
            for (Order order : completedOrders) {
                Bill bill = new Bill();
                bill.setCustomerName(order.getCustomerName());
                bill.setCustomerPhone(order.getCustomerPhone());
                bill.setTotalCost(order.getTotalCost());

                // Tạo một PaymentController mới cho mỗi bill
                FXMLLoader billLoader = new FXMLLoader(
                        getClass().getResource("/src/billiardsmanagement/bills/finalBill.fxml"));
                Parent billRoot = billLoader.load();
                PaymentController billController = billLoader.getController();
                billController.setOrderID(order.getOrderId());
                billController.setBill(bill);

                billsContainer.getChildren().add(billRoot);
            }

            Stage stage = new Stage();
            stage.setTitle("All Bills");
            stage.setScene(new Scene(scrollPane));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            NotificationService.showNotification(
                    "Error",
                    "An error occurred while loading bills.",
                    NotificationStatus.Error);
        }
    }

//    private void showItem(MouseEvent mouseEvent) throws IOException {
//        if (mouseEvent.getClickCount() == 2) {
//            Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
//            if (selectedOrder == null)
//                return;
//
//            int orderId = selectedOrder.getOrderId();
//            int customerId = selectedOrder.getCustomerId();
//            int totalRow = orderTable.getItems().size();
//            int selectedIndex = orderTable.getItems().indexOf(selectedOrder);
//            int billNo = totalRow - selectedIndex;
//
//            FXMLLoader loader = new FXMLLoader(
//                    getClass().getResource("/src/billiardsmanagement/orders/forEachOrder.fxml"));
//            Parent root = null;
//            try {
//                root = loader.load();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//            ForEachOrderController controller = loader.getController();
//            controller.setOrderID(orderId);
//            controller.setCustomerID(customerId);
//            controller.setOrderTable(orderTable);
//            controller.setBillNo(billNo);
//            controller.setInitialPhoneText(selectedOrder.getCustomerPhone());
//            controller.initializeAllTables();
//
//            Stage stage = new Stage();
//            stage.setTitle("Order Details");
//            stage.setScene(new Scene(root));
//            stage.show();
//        }
//    }

    private void showItem(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
            if (selectedOrder == null) {
                return;
            }

            // Tải trang forEachOrder.fxml
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/orders/forEachOrder.fxml"));
                Parent forEachOrderPage = loader.load();

                // Lấy controller của ForEachOrderController
                ForEachOrderController forEachOrderController = loader.getController();
                forEachOrderController.setOrderID(selectedOrder.getOrderId());
                forEachOrderController.setCustomerID(selectedOrder.getCustomerId());
                forEachOrderController.setOrderTable(orderTable);
                forEachOrderController.setMainController(mainController);
                forEachOrderController.initializeAllTables();
                // Cập nhật nội dung của contentArea trong MainController
                if (mainController != null) {
                    StackPane contentArea = mainController.getContentArea(); // Phương thức lấy contentArea
                    contentArea.getChildren().setAll(forEachOrderPage); // Xóa nội dung cũ và thêm trang mới
                }

            } catch (IOException e) {
                e.printStackTrace();
                NotificationService.showNotification("Error", "Failed to load order details.", NotificationStatus.Error);
            }
        }
    }

    @FXML
    public void searchOrder(ActionEvent actionEvent) {
        String phoneNumber = autoCompleteTextField.getText().trim();
        System.out.println(phoneNumber);
        if (phoneNumber.isEmpty()) {
            List<Order> allOrders = orderDAO.getAllOrders();
            orderTable.getItems().setAll(allOrders);
            NotificationService.showNotification("All Orders", "Showing all orders in the system.",
                    NotificationStatus.Information);
            return;
        }

        // Fetch orders by phone number
        List<Order> orders = OrderDAO.getOrdersByPhone(phoneNumber);

        if (orders.isEmpty()) {
            NotificationService.showNotification("No Orders Found",
                    "There are no orders associated with this phone number.", NotificationStatus.Information);
        } else {
            orderTable.getItems().setAll(orders);
            NotificationService.showNotification("Search Successful", "Found " + orders.size() + " orders.",
                    NotificationStatus.Success);
            autoCompleteTextField.clear(); // Clear the search textfield after successful search
        }
    }

    @FXML
    private void addBooking() {
        // Implementation of addBooking
    }

    public void addTableToNewOrder(PoolTable table) {
        // Create a new order
        addBooking();

        // Add the selected table to the booking
        if (table != null) {
            // Update table status to Ordered
            table.setStatus("Ordered");
            poolTableDAO.updateTable(table);

            // Add table to the booking list
            // Note: You'll need to implement the logic to add the table to your booking
            // list
            // This depends on your existing booking implementation
        }
    }

    public void initializeWithTable(PoolTable table) {
        try {
            // Create a new order
            Order newOrder = new Order();
            newOrder.setCustomerId(1); // Default customer ID
            newOrder.setUserId(UserSession.getInstance().getUserId());

            // Add the table to the order
            if (table != null) {
                // Update table status
                table.setStatus("Ordered");
                poolTableDAO.updateTable(table);

                // Add table to order
                orderDAO.addOrder(newOrder);
                Order latestOrder = orderDAO.getLatestOrderByCustomerId(1);

                // Initialize the order with the table
                setOrderID(latestOrder.getOrderId());
                loadOrderList();
            }
        } catch (Exception e) {
            NotificationService.showNotification("Error", "Failed to initialize order with table: " + e.getMessage(),
                    NotificationStatus.Error);
        }
    }

    private void setOrderID(int orderId) {
        this.orderID = orderId;
    }

    public static int getBillNumberCount() {
        return billNumberCount;
    }

    public static void setBillNumberCount(int newBillNumberCount) {
        OrderController.billNumberCount = newBillNumberCount;
    }
}
