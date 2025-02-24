module src.billiardsmanagement {
    requires javafx.controls;
    requires javafx.fxml;
    requires de.jensd.fx.glyphs.fontawesome;
    requires de.jensd.fx.glyphs.commons;
    requires java.sql;
    requires org.controlsfx.controls;
    requires itextpdf;
    requires mysql.connector.j;
    requires java.desktop;
    requires org.apache.pdfbox;
    requires kernel;

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

    opens src.billiardsmanagement.controller.products to javafx.fxml;
    opens src.billiardsmanagement.controller.category to javafx.fxml;
    opens src.billiardsmanagement.controller.users to javafx.fxml;

}
