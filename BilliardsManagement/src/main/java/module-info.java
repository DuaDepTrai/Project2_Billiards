module src.billiardsmanagement {
    requires javafx.fxml;
    requires java.sql;
    requires org.controlsfx.controls;
    requires itextpdf;
    requires mysql.connector.j;

    exports src.billiardsmanagement.model;
    exports src.billiardsmanagement.controller;
    exports src.billiardsmanagement.view;
    exports src.billiardsmanagement.dao;

    exports src.billiardsmanagement.controller.orders;  // add this line
    exports src.billiardsmanagement.controller.orders.items;
    exports src.billiardsmanagement.controller.orders.rent;
    exports src.billiardsmanagement.controller.orders.bookings;

    opens src.billiardsmanagement.view to javafx.fxml;
    opens src.billiardsmanagement.controller.orders.rent to javafx.fxml;
    opens src.billiardsmanagement.controller.orders.items to javafx.fxml;
    opens src.billiardsmanagement.controller.orders.bookings to javafx.fxml;

    opens src.billiardsmanagement.model to javafx.base;
    opens src.billiardsmanagement.controller to javafx.fxml;
    opens src.billiardsmanagement.controller.orders to javafx.fxml;
    opens src.billiardsmanagement.controller.revenue to javafx.fxml;
    opens src.billiardsmanagement.controller.category to javafx.fxml;
    opens src.billiardsmanagement.controller.products to javafx.fxml;
}
