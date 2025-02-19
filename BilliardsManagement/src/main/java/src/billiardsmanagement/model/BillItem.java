package src.billiardsmanagement.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class BillItem {
    private SimpleStringProperty itemName;
    private SimpleStringProperty unit; // Đơn vị tính
    private SimpleDoubleProperty quantity;
    private SimpleDoubleProperty unitPrice;
    private SimpleDoubleProperty totalPrice;

    public BillItem(String itemName, String unit, double quantity, double unitPrice, double totalPrice) {
        this.itemName = new SimpleStringProperty(itemName);
        this.unit = new SimpleStringProperty(unit);
        this.quantity = new SimpleDoubleProperty(quantity);
        this.unitPrice = new SimpleDoubleProperty(unitPrice);
        this.totalPrice = new SimpleDoubleProperty(totalPrice);
    }

    public String getItemName() {
        return itemName.get();
    }

    public String getUnit() {
        return unit.get();
    }

    public double getQuantity() {
        return quantity.get();
    }

    public double getUnitPrice() {
        return unitPrice.get();
    }

    public double getTotalPrice() {
        return totalPrice.get();
    }
}
