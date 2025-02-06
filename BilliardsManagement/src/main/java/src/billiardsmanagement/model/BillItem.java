package src.billiardsmanagement.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalTime;

public class BillItem {
    @Override
    public String toString() {
        return "BillItem{" +

                ", itemName=" + itemName +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + totalPrice +
                '}';
    }


    private SimpleStringProperty itemName;
    private SimpleDoubleProperty quantity;
    private SimpleDoubleProperty unitPrice;
    private SimpleDoubleProperty totalPrice;

    public BillItem(String itemName, double quantity, double unitPrice, double totalPrice) {
        this.itemName = new SimpleStringProperty(itemName);
        this.quantity = new SimpleDoubleProperty(quantity);
        this.unitPrice = new SimpleDoubleProperty(unitPrice);
        this.totalPrice = new SimpleDoubleProperty(totalPrice);
    }

    public String getItemName() {
        return itemName.get();
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
