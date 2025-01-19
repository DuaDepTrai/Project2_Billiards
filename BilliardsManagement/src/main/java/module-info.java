module src.billiardsmanagement {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires java.sql;
    
    exports src.billiardsmanagement.model;
    exports src.billiardsmanagement.controller;
    exports src.billiardsmanagement.view;
    exports src.billiardsmanagement.dao;

    exports src.billiardsmanagement.controller.orders.items;
    exports src.billiardsmanagement.controller.orders.rent;

    opens src.billiardsmanagement.view to javafx.fxml;
    opens src.billiardsmanagement.model to javafx.base;
    opens src.billiardsmanagement.controller to javafx.fxml;
    opens src.billiardsmanagement.controller.category to javafx.fxml;
    opens src.billiardsmanagement.controller.orders to javafx.fxml;
    opens src.billiardsmanagement.controller.products to javafx.fxml;
    opens src.billiardsmanagement.controller.orders.bookings to javafx.fxml;
}
