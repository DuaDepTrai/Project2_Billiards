module src.billiardsmanagement {
    requires de.jensd.fx.glyphs.fontawesome;
    requires de.jensd.fx.glyphs.commons;
    requires java.sql;
    requires org.controlsfx.controls;

    requires mysql.connector.j;

    requires org.apache.pdfbox;
    requires kernel;
    requires io;
    requires itextpdf;
    requires MaterialFX;

    exports src.billiardsmanagement.model;
    exports src.billiardsmanagement.controller;
    exports src.billiardsmanagement.view;
    exports src.billiardsmanagement.dao;
    exports src.billiardsmanagement.controller.report to javafx.fxml;
    exports src.billiardsmanagement.controller.orders;  // add this line
    exports src.billiardsmanagement.controller.orders.items;
    exports src.billiardsmanagement.controller.orders.bookings;

    opens src.billiardsmanagement.view to javafx.fxml;
    opens src.billiardsmanagement.controller.orders.items to javafx.fxml;
    opens src.billiardsmanagement.controller.orders.bookings to javafx.fxml;

    opens src.billiardsmanagement.model to javafx.base;
    opens src.billiardsmanagement.controller to javafx.fxml;
    opens src.billiardsmanagement.controller.orders to javafx.fxml;

    opens src.billiardsmanagement.controller.products to javafx.fxml;
    opens src.billiardsmanagement.controller.products2 to javafx.fxml;
    opens src.billiardsmanagement.controller.category to javafx.fxml;
    opens src.billiardsmanagement.controller.users to javafx.fxml;
    exports src.billiardsmanagement.service;
    opens src.billiardsmanagement.service to javafx.base;
    opens src.billiardsmanagement.controller.report to javafx.fxml;
    exports src.billiardsmanagement.controller.pooltables;
    opens src.billiardsmanagement.controller.pooltables to javafx.fxml;

    exports src.billiardsmanagement.controller.pooltables.catepooltables;
    opens src.billiardsmanagement.controller.pooltables.catepooltables to javafx.fxml;
}
