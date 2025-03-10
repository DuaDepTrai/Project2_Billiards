package src.billiardsmanagement.controller.poolTables;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import src.billiardsmanagement.controller.orders.ForEachOrderController;
import src.billiardsmanagement.controller.orders.OrderController;
import src.billiardsmanagement.dao.BookingDAO;
import src.billiardsmanagement.dao.OrderDAO;
import src.billiardsmanagement.model.NotificationStatus;
import src.billiardsmanagement.model.Order;
import src.billiardsmanagement.service.NotificationService;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AddTableToOrderController implements Initializable {

    @FXML
    private TableView<Order> orderTable;

    @FXML
    private TableColumn<Order, Integer> sttColumn;
    @FXML
    private TableColumn<Order, String> customerNameColumn;
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

    private OrderDAO orderDAO = new OrderDAO();
    private int currentTableId = -1;


    // Initialize the table columns
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

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

        dateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        actionColumn.setCellFactory(column -> new TableCell<Order, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                // Create a button for "Add to Order"
                Button addToOrderButton = new Button("Add to Order");
                addToOrderButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    System.out.println("Order = " + order);

                    // Create a dialog to ask user's choice
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Choose Action");
                    alert.setHeaderText("Add Order");
                    alert.setContentText("Do you want to play on the table or just order?");

                    ButtonType playButton = new ButtonType("Play on Table");
                    ButtonType orderButton = new ButtonType("Order on Table");
                    ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

                    alert.getButtonTypes().setAll(playButton, orderButton, cancelButton);

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == playButton) {
                        // User chose to play
                        addToOrder(order, "Playing");
                    } else if (result.isPresent() && result.get() == orderButton) {
                        // User chose to order
                        addToOrder(order, "Order");
                    } else {
                        NotificationService.showNotification("Info",
                                "Action canceled.",
                                NotificationStatus.Information);
                    }
                });

                // Add the button to an HBox for alignment
                HBox actionBox = new HBox();
                actionBox.setAlignment(Pos.CENTER);
                actionBox.getChildren().add(addToOrderButton);

                setGraphic(actionBox);
            }
        });

        loadOrderList();
    }

    private void addToOrder(Order order, String bookingStatus) {
        if (currentTableId != -1) {
            boolean success = BookingDAO.addBookingToExistedOrder(order.getOrderId(), currentTableId, bookingStatus);

            if (success) {
                NotificationService.showNotification(
                        "Success",
                        "Table added to order successfully!",
                        NotificationStatus.Success
                );
                showForEachOrderView(order);
            } else {
                NotificationService.showNotification(
                        "Error",
                        "Failed to add table to order. Please try again.",
                        NotificationStatus.Error
                );
            }
        } else {
            NotificationService.showNotification(
                    "Error",
                    "No table selected. Please select a table first.",
                    NotificationStatus.Error
            );
        }
    }

    private void showForEachOrderView(Order order) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/orders/forEachOrder.fxml"));
            Parent root = loader.load();

            ForEachOrderController forEachOrderController = loader.getController();
            forEachOrderController.setOrderID(order.getOrderId());
            forEachOrderController.setCustomerID(order.getCustomerId());
            forEachOrderController.setForEachUserID(order.getUserId());
            forEachOrderController.setBillNo(OrderController.getBillNumberCount());
            forEachOrderController.initializeAllTables();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Order Information");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            NotificationService.showNotification("Error",
                    "Failed to open order information: " + e.getMessage(),
                    NotificationStatus.Error);
        }
    }


    private void loadOrderList() {
        List<Order> orders = orderDAO.getAllOrders();
        if (!orders.isEmpty()) {
            orders = orders.stream().filter(order -> order.getOrderStatus().equalsIgnoreCase("Playing")).toList();
            orderTable.setItems(FXCollections.observableArrayList(orders));
        }
    }

    public void setCurrentTableId(int tableId) {
        this.currentTableId = tableId;
    }

    private void closeWindow() {
        Stage stage = (Stage) orderTable.getScene().getWindow();
        stage.close();
    }
}