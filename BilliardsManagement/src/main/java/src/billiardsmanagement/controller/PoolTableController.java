package src.billiardsmanagement.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import src.billiardsmanagement.dao.PoolTableDAO;
import src.billiardsmanagement.model.PoolTable;

public class PoolTableController {

    @FXML
    private TableView<PoolTable> tableView;
    @FXML
    private TableColumn<PoolTable, Integer> tableIdCol; // Cột ID
    @FXML
    private TableColumn<PoolTable, String> tableNameCol;
    @FXML
    private TableColumn<PoolTable, Double> tablePriceCol;
    @FXML
    private TableColumn<PoolTable, String> tableStatusCol;

    @FXML
    private TextField tableNameField;
    @FXML
    private TextField tablePriceField;
    @FXML
    private ComboBox<String> tableStatusCombo;

    private ObservableList<PoolTable> tableList = FXCollections.observableArrayList();
    private PoolTableDAO poolTableDAO = new PoolTableDAO(); // Giả định DAO

    @FXML
    public void initialize() {
        tableIdCol.setCellValueFactory(new PropertyValueFactory<>("tableId"));
        tableNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        tablePriceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        tableStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        tableView.setItems(tableList);

        tableStatusCombo.getItems().addAll("Available", "Occupied", "Under Maintenance");

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                tableNameField.setText(newSelection.getName());
                tablePriceField.setText(String.valueOf(newSelection.getPrice()));
                tableStatusCombo.setValue(newSelection.getStatus());
            }
        });

        handleViewAllTables();
    }

    @FXML
    private void handleAddNewTable() {
        try {
            String name = tableNameField.getText();
            double price = Double.parseDouble(tablePriceField.getText());
            String status = tableStatusCombo.getValue();

            PoolTable newTable = new PoolTable(0, name, price, status); // ID sẽ được tạo tự động
            int newId = poolTableDAO.addTable(newTable); // Gọi phương thức addTable để thêm vào DB
            newTable.setTableId(newId); // Cập nhật ID cho đối tượng
            handleViewAllTables();

            // Xóa thông tin đã nhập
            clearInputFields();
        } catch (NumberFormatException e) {
            showAlert("Invalid price", "Please enter a valid number for price.");
        } catch (IllegalArgumentException e) {
            showAlert("Invalid input", e.getMessage());
        } catch (Exception e) {
            showAlert("Error", "An error occurred while adding the table: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateTable() {
        PoolTable selectedTable = tableView.getSelectionModel().getSelectedItem();
        if (selectedTable != null) {
            try {
                selectedTable.setName(tableNameField.getText());
                selectedTable.setPrice(Double.parseDouble(tablePriceField.getText()));
                selectedTable.setStatus(tableStatusCombo.getValue());
                poolTableDAO.updateTable(selectedTable);
                handleViewAllTables();
                clearInputFields();
            } catch (NumberFormatException e) {
                showAlert("Invalid price", "Please enter a valid number for price.");
            } catch (IllegalArgumentException e) {
                showAlert("Invalid input", e.getMessage());
            } catch (Exception e) {
                showAlert("Error", "An error occurred while updating the table: " + e.getMessage());
            }
        } else {
            showAlert("No selection", "Please select a table to update.");
        }
    }

    @FXML
    private void handleRemoveTable() {
        PoolTable selectedTable = tableView.getSelectionModel().getSelectedItem();
        if (selectedTable != null) {
            poolTableDAO.removeTable(selectedTable.getTableId()); // Xóa theo tableId
            handleViewAllTables();
            clearInputFields();
        } else {
            showAlert("No selection", "Please select a table to remove.");
        }
    }

    @FXML
    private void handleViewAllTables() {
        tableList.clear();
        tableList.addAll(poolTableDAO.getAllTables());
    }

    private void clearInputFields() {
        tableNameField.clear();
        tablePriceField.clear();
        tableStatusCombo.setValue(null); // Đặt lại ComboBox
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING); // Sử dụng loại cảnh báo cho thông báo
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}