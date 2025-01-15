module src.billiardsmanagement {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    exports src.billiardsmanagement.model; // Xuất khẩu package chứa lớp Order
    opens src.billiardsmanagement.controller to javafx.fxml; // Mở controller cho JavaFX
    opens src.billiardsmanagement to javafx.fxml;
    exports src.billiardsmanagement.controller;
    exports src.billiardsmanagement.view;
    opens src.billiardsmanagement.view to javafx.fxml;
    opens src.billiardsmanagement.model to javafx.base;
    exports src.billiardsmanagement.controller.category;
    opens src.billiardsmanagement.controller.category to javafx.fxml;
    exports src.billiardsmanagement.controller.orders;
    opens src.billiardsmanagement.controller.orders to javafx.fxml;
    exports src.billiardsmanagement.controller.products;
    opens src.billiardsmanagement.controller.products to javafx.fxml;
    exports src.billiardsmanagement.controller.orders.bookings;
    opens src.billiardsmanagement.controller.orders.bookings to javafx.fxml;
    exports src.billiardsmanagement.controller.orders.items;
    opens src.billiardsmanagement.controller.orders.items to javafx.fxml;
    exports src.billiardsmanagement.controller.orders.rent;
    opens src.billiardsmanagement.controller.orders.rent to javafx.fxml;
    exports src.billiardsmanagement.dao;
    opens src.billiardsmanagement.dao to javafx.base;

}

