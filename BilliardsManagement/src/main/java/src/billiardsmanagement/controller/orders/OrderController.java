package src.billiardsmanagement.controller.orders;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
import src.billiardsmanagement.controller.orders.bookings.AddBookingController;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
    private TableColumn<Order, String> dateColumn;
    @FXML
    private TableColumn<Order, String> staffColumn;
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
    @FXML
    private ComboBox<String> filterTypeComboBox;
    @FXML
    private DatePicker datePicker;
    @FXML
    private ComboBox<String> categoryComboBox;
    @FXML
    private ComboBox<String> statusComboBox;
    @FXML
    private HBox filterContainer,tableCategoryContainer,orderStatusContainer;
    @FXML
    private Button filterDateButton;// Chứa bộ lọc
    private ObservableList<Order> orderList = FXCollections.observableArrayList();
    @FXML
    DatePicker startDatePicker,endDatePicker;
    private int orderID;

    private final Connection conn = DatabaseConnection.getConnection();
    private final OrderDAO orderDAO = new OrderDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final Map<String, Integer> customerNameToIdMap = new HashMap<>();
    private final PoolTableDAO poolTableDAO = new PoolTableDAO();
    
    private MainController mainController;
    private ForEachOrderController forEachOrderController;
    private Parent forEachOrderPage;


    // Minute Limit, use for auto-cancel booking and check booking-time
    public static int minutesLimit = 1;


    // without chosen page, of course
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

            // Tạo order mới với customer_id mặc định là 1
            Order newOrder = new Order();
            newOrder.setCustomerId(1);
            newOrder.setUserId(userSession.getUserId());

            // Lưu order vào database
            orderDAO.addOrder(newOrder);

            // Lấy order mới nhất của khách hàng có ID = 1
            Order orderLatest = orderDAO.getLatestOrderByCustomerId(1);
            int orderId = orderLatest.getOrderId();
            int totalRow = orderTable.getItems().size();
            int selectedIndex = orderTable.getItems().indexOf(newOrder);
            int billNo = totalRow - selectedIndex;
            System.out.println(orderId);
            loadOrderList();

//            Stage stage = new Stage();
//            stage.setTitle("Order Detail");
//            stage.setScene(new Scene(forEachOrderPage));
//            stage.show();

            forEachOrderController.setOrderID(orderId);
            forEachOrderController.setMainController(this.mainController, ChosenPage.ORDERS);
            forEachOrderController.setCustomerID(1);
            forEachOrderController.setOrderTable(orderTable);
            forEachOrderController.setBillNo(billNo);
            forEachOrderController.setOrderDate(orderLatest.getOrderDate());
            forEachOrderController.setInitialPhoneText(orderLatest.getCustomerPhone());
            forEachOrderController.setInitialPhoneText(orderLatest.getCustomerPhone());
            forEachOrderController.initializeAllTables();

            if(mainController!=null){
                StackPane contentArea = mainController.getContentArea();
                contentArea.getChildren().clear();
                contentArea.getChildren().setAll(forEachOrderPage);
            }
        } catch (IllegalArgumentException e) {
            NotificationService.showNotification("Validation Error", e.getMessage(), NotificationStatus.Error);
        } catch (Exception e) {
            e.printStackTrace();
            NotificationService.showNotification("Error", "An error occurred while saving the order. Please try again.",
                    NotificationStatus.Error);
        }
    }

    public void loadOrderList() {
        List<Order> orders = orderDAO.getAllOrders();
        orderList = FXCollections.observableArrayList(orders);
        orderTable.getItems().clear();
        orderTable.setItems(FXCollections.observableArrayList(orderList));
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
        setUpSearchField();
        loadOrderList();
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
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setWrapText(true); // Cho phép text xuống dòng
                    getStyleClass().add("table-name-cell");
                }
            }
        });

        orderTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        sttColumn.prefWidthProperty().bind(orderTable.widthProperty().multiply(0.08));
        customerNameColumn.prefWidthProperty().bind(orderTable.widthProperty().multiply(0.15));
        phoneCustomerColumn.prefWidthProperty().bind(orderTable.widthProperty().multiply(0.12));
        nameTableColumn.prefWidthProperty().bind(orderTable.widthProperty().multiply(0.15));
        totalCostColumn.prefWidthProperty().bind(orderTable.widthProperty().multiply(0.1));
        orderStatusColumn.prefWidthProperty().bind(orderTable.widthProperty().multiply(0.1));
        dateColumn.prefWidthProperty().bind(orderTable.widthProperty().multiply(0.15));
        staffColumn.prefWidthProperty().bind(orderTable.widthProperty().multiply(0.10));
        actionColumn.prefWidthProperty().bind(orderTable.widthProperty().multiply(0.05));

        sttColumn.setStyle("-fx-alignment: CENTER;");
        customerNameColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        phoneCustomerColumn.setStyle("-fx-alignment: CENTER;");
        nameTableColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        totalCostColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
        orderStatusColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        dateColumn.setStyle("-fx-alignment: CENTER;");
        staffColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        actionColumn.setStyle("-fx-alignment: CENTER;");

        // Đảm bảo cập nhật kích thước khi cửa sổ thay đổi
        mainPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(() -> {
                    orderTable.refresh(); // Làm mới bảng để áp dụng các thay đổi
                });
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





        staffColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
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
//        orderTable.getColumns().forEach(column -> {
//            column.setMinWidth(50); // Độ rộng tối thiểu
//            column.setPrefWidth(120); // Độ rộng mặc định
//            column.setResizable(true); // Cho phép thay đổi kích thước
//        });

//        orderTable.widthProperty().addListener((obs, oldWidth, newWidth) -> {
//            for (TableColumn<?, ?> column : orderTable.getColumns()) {
//                column.setPrefWidth(newWidth.doubleValue() / orderTable.getColumns().size());
//            }
//        });

        setupFilters();
    }

    public void initializeOrderController(){
        loadCustomerNameToIdMap();
        setUpSearchField();
        loadOrderList();
        orderTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
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


        totalCostColumn.setCellFactory(param -> new TableCell<Order, Double>() {
            private final DecimalFormat df = new DecimalFormat("#,###");

            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : df.format(item));
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
        nameTableColumn.setCellFactory(tc -> {
            TableCell<Order, String> cell = new TableCell<>() {
                private final Text text = new Text();

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        text.setText(item);
                        text.wrappingWidthProperty().bind(nameTableColumn.widthProperty().subtract(10)); // Tránh sát mép cột
                        setGraphic(text);
                    }
                }
            };
            return cell;
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

        dateColumn.setCellValueFactory(cellData -> {
            LocalDateTime startTime = cellData.getValue().getOrderDate();
            return new SimpleStringProperty(
                    startTime != null ? startTime.format(DateTimeFormatter.ofPattern("HH'h'mm | dd-MM")) : ""
            );
        });

        staffColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
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

        sttColumn.prefWidthProperty().bind(orderTable.widthProperty().multiply(0.05));
        customerNameColumn.prefWidthProperty().bind(orderTable.widthProperty().multiply(0.17));
        phoneCustomerColumn.prefWidthProperty().bind(orderTable.widthProperty().multiply(0.08));
        nameTableColumn.prefWidthProperty().bind(orderTable.widthProperty().multiply(0.20));
        totalCostColumn.prefWidthProperty().bind(orderTable.widthProperty().multiply(0.1));
        orderStatusColumn.prefWidthProperty().bind(orderTable.widthProperty().multiply(0.08));
        dateColumn.prefWidthProperty().bind(orderTable.widthProperty().multiply(0.1));
        staffColumn.prefWidthProperty().bind(orderTable.widthProperty().multiply(0.1));
        actionColumn.prefWidthProperty().bind(orderTable.widthProperty().multiply(0.08));
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
                int totalRow = orderTable.getItems().size();
                int selectedIndex = orderTable.getItems().indexOf(selectedOrder);
                int billNo = totalRow - selectedIndex;

                forEachOrderController.initializeAllTables();

                forEachOrderController.setOrderID(selectedOrder.getOrderId());
                forEachOrderController.setCustomerID(selectedOrder.getCustomerId());

                forEachOrderController.setMainController(mainController, ChosenPage.ORDERS);
                forEachOrderController.setBillNo(billNo);
                forEachOrderController.setOrderDate(selectedOrder.getOrderDate());
                forEachOrderController.setCurrentOrder(selectedOrder);
                forEachOrderController.setInitialPhoneText(selectedOrder.getCustomerPhone());
                forEachOrderController.setOrderController(this);
                forEachOrderController.initializeAllTables();

                // Cập nhật nội dung của contentArea trong MainController
                if (mainController != null) {
                    StackPane contentArea = mainController.getContentArea(); // Phương thức lấy contentArea
                    contentArea.getChildren().clear();
                    contentArea.getChildren().setAll(forEachOrderPage); // Xóa nội dung cũ và thêm trang mới
                }

                Platform.runLater(() -> {
                    forEachOrderPage.requestFocus();
                });

                FXMLLoader loader1 = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/orders/bookings/addBooking.fxml"));
                Parent addBookingPage = loader1.load();
                AddBookingController addBookingController = loader1.getController();

            } catch (IOException e) {
                e.printStackTrace();
                NotificationService.showNotification("Error", "Failed to load order details.", NotificationStatus.Error);
            }
        }
    }

    @FXML
    public void searchOrder(ActionEvent actionEvent) {
//        String phoneNumber = autoCompleteTextField.getText().trim();
//        System.out.println(phoneNumber);
//        if (phoneNumber.isEmpty()) {
//            List<Order> allOrders = orderDAO.getAllOrders();
//            orderTable.getItems().setAll(allOrders);
//            NotificationService.showNotification("All Orders", "Showing all orders in the system.",
//                    NotificationStatus.Information);
//            return;
//        }
//
//        // Fetch orders by phone number
//        List<Order> orders = OrderDAO.getOrdersByPhone(phoneNumber);
//
//        if (orders.isEmpty()) {
//            NotificationService.showNotification("No Orders Found",
//                    "There are no orders associated with this phone number.", NotificationStatus.Information);
//        } else {
//            orderTable.getItems().setAll(orders);
//            NotificationService.showNotification("Search Successful", "Found " + orders.size() + " orders.",
//                    NotificationStatus.Success);
//            autoCompleteTextField.clear(); // Clear the search textfield after successful search
//        }
        String searchItem = autoCompleteTextField.getText().trim().toLowerCase();
        filteredOrders(searchItem);
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
    private void filteredOrders(String searchItem){
        ObservableList<Order> filteredList = FXCollections.observableArrayList();
        for (Order order : orderDAO.getAllOrders()) {
            if (order.getCustomerName().toLowerCase().contains(searchItem) ||
                    order.getCustomerPhone().contains(searchItem)) {
                filteredList.add(order);
            }
        }
        orderTable.setItems(filteredList);
    }

    private void setUpSearchField() {
        autoCompleteTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                loadOrderList();
            } else {
                filteredOrders(newValue.toLowerCase().trim());
            }
        });
    }

    private void setOrderID(int orderId) {
        this.orderID = orderId;
    }

    public ObservableList<Order> getOrderList() {
        if(orderList.isEmpty()) System.out.println("From OrderController - getOrderList() : order List is empty.");
        return this.orderList;
    }
    
    public void setOrderList(ObservableList<Order> orderList) {
        this.orderList = orderList;
    }

    public void setForEachOrderController(ForEachOrderController forEachOrderController) {
        this.forEachOrderController = forEachOrderController;
    }

    public void setForEachOrderPage(Parent forEachOrderPage) {
        this.forEachOrderPage = forEachOrderPage;
    }

    private void setupFilters() {
        // Tạo ComboBox chọn kiểu lọc
        filterTypeComboBox = createComboBox(Arrays.asList("Date Range", "Table Category", "Order Status"));
        filterTypeComboBox.setPromptText("Filter Type");

        // Tạo DatePicker
        startDatePicker = createDatePicker();
        startDatePicker.setPromptText("Start Date");
        startDatePicker.setOnAction(event -> filterByDateRange());
        startDatePicker.getStyleClass().add("hbox-filter");


        endDatePicker = createDatePicker();
        endDatePicker.setPromptText("End Date");
        endDatePicker.setOnAction(event -> filterByDateRange());
        endDatePicker.getStyleClass().add("hbox-filter");


        // Tạo danh sách CheckBox cho Table Category
        tableCategoryContainer = new HBox(10);
        for (String category : OrderDAO.getCatePoolTables()) {
            CheckBox checkBox = new CheckBox(category);
            checkBox.setOnAction(event -> filterByCategory());
            tableCategoryContainer.getChildren().add(checkBox);
            tableCategoryContainer.getStyleClass().add("hbox-filter");

        }

        // Tạo danh sách CheckBox cho Order Status
        orderStatusContainer = new HBox(10);
        for (String status : OrderDAO.getOrderStatuses()) {
            CheckBox checkBox = new CheckBox(status);
            checkBox.setOnAction(event -> filterByStatus());
            orderStatusContainer.getChildren().add(checkBox);
            orderStatusContainer.getStyleClass().add("hbox-filter");

        }

        // Thêm ComboBox chọn kiểu lọc vào `filterContainer`
        filterContainer.getChildren().add(filterTypeComboBox);
        filterContainer.getStyleClass().add("filter-container");


        // Xử lý sự kiện thay đổi bộ lọc
        filterTypeComboBox.setOnAction(event -> updateFilterUI());
    }


    private ComboBox<String> createComboBox(List<String> items) {
        return new ComboBox<>(FXCollections.observableArrayList(items));
    }

    private DatePicker createDatePicker() {
        return new DatePicker();
    }


    private void updateFilterUI() {
        filterContainer.getChildren().clear(); // Xóa các thành phần cũ
        filterContainer.getChildren().add(filterTypeComboBox); // Luôn giữ ComboBox chọn loại lọc

        String selectedFilter = filterTypeComboBox.getValue();

        if ("Date Range".equals(selectedFilter)) {
            filterContainer.getChildren().addAll(startDatePicker, endDatePicker);
        } else if ("Table Category".equals(selectedFilter)) {
            filterContainer.getChildren().add(tableCategoryContainer);
        } else if ("Order Status".equals(selectedFilter)) {
            filterContainer.getChildren().add(orderStatusContainer);
        }
    }


    private void filterByDateRange() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null || endDate == null) {
            // Nếu một trong hai ngày bị rỗng, lấy đơn hàng theo ngày có sẵn
            LocalDate filterDate = (startDate != null) ? startDate : endDate;
            orderTable.setItems(OrderDAO.getOrdersByDate(filterDate));
            NotificationService.showNotification("Lọc đơn hàng", "Hiển thị đơn hàng theo ngày " + filterDate, NotificationStatus.Information);
            return;
        }

        if (startDate.isAfter(endDate)) {
            // Hiển thị thông báo nếu ngày bắt đầu lớn hơn ngày kết thúc
            NotificationService.showNotification("Lỗi chọn ngày", "Ngày bắt đầu không thể lớn hơn ngày kết thúc!", NotificationStatus.Warning);
            return;
        }

        // Nếu cả hai ngày hợp lệ, lấy dữ liệu theo khoảng ngày
        orderTable.setItems(OrderDAO.getOrdersByDateRange(startDate, endDate));
        NotificationService.showNotification("Lọc đơn hàng", "Hiển thị đơn hàng từ " + startDate + " đến " + endDate, NotificationStatus.Success);
    }


    private void filterByCategory() {
        List<String> selectedCategories = new ArrayList<>();

        for (Node node : tableCategoryContainer.getChildren()) {
            if (node instanceof CheckBox checkBox && checkBox.isSelected()) {
                selectedCategories.add(checkBox.getText());
            }
        }

        if (!selectedCategories.isEmpty()) {
            orderTable.setItems(OrderDAO.getOrdersByCatePoolTable(selectedCategories));
        } else {
            loadOrderList();
        }
    }


    private void filterByStatus() {
        List<String> selectedStatuses = new ArrayList<>();

        for (Node node : orderStatusContainer.getChildren()) {
            if (node instanceof CheckBox checkBox && checkBox.isSelected()) {
                selectedStatuses.add(checkBox.getText());
            }
        }

        if (!selectedStatuses.isEmpty()) {
            orderTable.setItems(OrderDAO.getOrdersByStatus(String.valueOf(selectedStatuses)));
        } else {
            loadOrderList();
        }
    }
    @FXML
    private Button refreshButton;

    private boolean isRefreshNotificationShow = true;
    @FXML
    public void refreshPage(ActionEvent event) {
        loadCustomerNameToIdMap(); // Làm mới danh sách tên khách hàng
        loadOrderList(); // Làm mới danh sách đơn hàng
        autoCompleteTextField.clear(); // Xóa trường tìm kiếm

        // Xóa lựa chọn trong ComboBox filter
        filterTypeComboBox.getSelectionModel().clearSelection();

        // Xóa dữ liệu của DatePicker
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);

        // Bỏ chọn tất cả CheckBox trong Table Category
        for (Node node : tableCategoryContainer.getChildren()) {
            if (node instanceof CheckBox checkBox) {
                checkBox.setSelected(false);
            }
        }

        // Bỏ chọn tất cả CheckBox trong Order Status
        for (Node node : orderStatusContainer.getChildren()) {
            if (node instanceof CheckBox checkBox) {
                checkBox.setSelected(false);
            }
        }

        NotificationService.showNotification("Refresh", "Page has been refreshed.", NotificationStatus.Information);
        datePicker.setValue(null);
        categoryComboBox.getSelectionModel().clearSelection();
        statusComboBox.getSelectionModel().clearSelection();
        if(isRefreshNotificationShow){
            NotificationService.showNotification("Refresh", "Page has been refreshed.", NotificationStatus.Information);
        }
    }

    public void setRefreshNotificationShow(boolean refreshNotificationShow) {
        isRefreshNotificationShow = refreshNotificationShow;
    }

}

