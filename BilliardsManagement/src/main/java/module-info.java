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

}

