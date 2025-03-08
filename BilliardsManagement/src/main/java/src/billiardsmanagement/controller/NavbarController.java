package src.billiardsmanagement.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import src.billiardsmanagement.controller.users.UserController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NavbarController {

    @FXML
    private VBox navMenu;

    private StackPane contentArea; // Khu vực hiển thị nội dung

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    private final Map<String, String> menuItems = new HashMap<>() {{
        put("Pool Table", "/src/billiardsmanagement/pooltable/pooltable.fxml");
        put("Order", "/src/billiardsmanagement/orders/order.fxml");
        put("Product", "/src/billiardsmanagement/products2/products2.fxml");
        put("Staff", "/src/billiardsmanagement/users/users.fxml");
        put("Customer", "/billiardsmanagement/customer/customer.fxml");
        put("Report", "/src/billiardsmanagement/reports/report.fxml");
    }};

    public void initialize() {
        loadMenu();
    }

    private void loadMenu() {
        VBox reportContainer = new VBox(); // Chứa Report để thêm sau cùng
        reportContainer.setSpacing(5);

        for (Map.Entry<String, String> entry : menuItems.entrySet()) {
            String title = entry.getKey();
            String fxmlPath = entry.getValue();

            if (title.equals("Report")) {
                // Tạo menu Report với submenu
                VBox reportSubMenu = new VBox();
                reportSubMenu.setVisible(false); // Ẩn ban đầu

                Button reportButton = new Button("Report");
                reportButton.getStyleClass().add("nav-item");

                FontAwesomeIconView reportIcon = new FontAwesomeIconView();
                reportIcon.setGlyphName("BAR_CHART");
                reportIcon.setSize("16");
                reportButton.setGraphic(reportIcon);

                Button dailyReport = new Button("Order Report");
                dailyReport.getStyleClass().add("submenu-item");
                dailyReport.setOnAction(event -> loadPage("/src/billiardsmanagement/reports/orders_stats.fxml"));

                FontAwesomeIconView dailyIcon = new FontAwesomeIconView();
                dailyIcon.setGlyphName("CALENDAR");
                dailyIcon.setSize("14");
                dailyReport.setGraphic(dailyIcon);

                Button monthlyReport = new Button("Product Report");
                monthlyReport.getStyleClass().add("submenu-item");
                monthlyReport.setOnAction(event -> loadPage("/src/billiardsmanagement/reports/products_stats.fxml"));

                FontAwesomeIconView monthlyIcon = new FontAwesomeIconView();
                monthlyIcon.setGlyphName("LINE_CHART");
                monthlyIcon.setSize("14");
                monthlyReport.setGraphic(monthlyIcon);

                Button yearlyReport = new Button("Yearly Report");
                yearlyReport.getStyleClass().add("submenu-item");
                yearlyReport.setOnAction(event -> loadPage("/src/billiardsmanagement/reports/customer_stats.fxml"));

                FontAwesomeIconView yearlyIcon = new FontAwesomeIconView();
                yearlyIcon.setGlyphName("AREA_CHART");
                yearlyIcon.setSize("14");
                yearlyReport.setGraphic(yearlyIcon);

                reportSubMenu.getChildren().addAll(dailyReport, monthlyReport, yearlyReport);
                reportContainer.getChildren().addAll(reportButton, reportSubMenu);

                reportButton.setOnAction(event -> {
                    reportSubMenu.setVisible(!reportSubMenu.isVisible()); // Ẩn/hiện submenu khi bấm
                });

            } else {
                // Tạo các menu bình thường
                Button button = new Button(title);
                button.getStyleClass().add("nav-item");

                FontAwesomeIconView icon = new FontAwesomeIconView();
                icon.setGlyphName(getIconName(title));
                icon.setSize("16");
                button.setGraphic(icon);

                button.setOnAction(event -> loadPage(fxmlPath));
                navMenu.getChildren().add(button);
            }
        }

        // Thêm "Report" vào cuối cùng
        navMenu.getChildren().add(reportContainer);
    }




    private String getIconName(String menuTitle) {
        return switch (menuTitle) {
            case "Pool Table" -> "TABLE";
            case "Order" -> "SHOPPING_CART";
            case "Product" -> "CUBE";
            case "Staff" -> "USERS";
            case "Customer" -> "USER";
            case "Report" -> "BAR_CHART";
            default -> "QUESTION";
        };
    }

    private void loadPage(String fxmlPath) {
        if (contentArea == null) {
            System.out.println("⚠ Lỗi: contentArea chưa được thiết lập!");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            BorderPane page = loader.load();

            contentArea.getChildren().setAll(page); // Sửa lỗi: Thay vì `setCenter()`, dùng `getChildren().setAll()`
            FXMLLoader users = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/users/users.fxml"));
            UserController userController = users.getController();
            FXMLLoader main =  new FXMLLoader(getClass().getResource("/src/billiardsmanagement/main.fxml"));
            MainController mainController = main.getController();
            users.setController(mainController);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
