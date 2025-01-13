module src.billiardsmanagement {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens src.billiardsmanagement to javafx.fxml;
    exports src.billiardsmanagement.controller;
    opens src.billiardsmanagement.controller to javafx.fxml;
    exports src.billiardsmanagement.view;
    opens src.billiardsmanagement.view to javafx.fxml;
    opens src.billiardsmanagement.model to javafx.base;

}

