package src.billiardsmanagement.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class BillItem {
    private SimpleStringProperty itemName;
    private SimpleIntegerProperty quantity;
    private SimpleDoubleProperty unitPrice;
    private SimpleDoubleProperty totalPrice;

    public BillItem(String itemName, int quantity, double unitPrice, double totalPrice) {
        this.itemName = new SimpleStringProperty(itemName);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.unitPrice = new SimpleDoubleProperty(unitPrice);
        this.totalPrice = new SimpleDoubleProperty(totalPrice);
    }

    public String getItemName() {
        return itemName.get();
    }

    public int getQuantity() {
        return quantity.get();
    }

    public double getUnitPrice() {
        return unitPrice.get();
    }

    public double getTotalPrice() {
        return totalPrice.get();
    }
}
