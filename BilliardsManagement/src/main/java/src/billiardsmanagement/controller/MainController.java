package src.billiardsmanagement.controller;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import src.billiardsmanagement.controller.poolTables.*;
import src.billiardsmanagement.controller.products2.ProductController2;
import src.billiardsmanagement.controller.users.RolesPermissionsController;
import src.billiardsmanagement.controller.users.UserController;
import src.billiardsmanagement.model.TestDBConnection;
import src.billiardsmanagement.model.User;

import java.lang.reflect.Method;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;


public class MainController {

    @FXML
    private Label usernameLabel;
    @FXML
    private Label roleLabel;
    @FXML
    private Label logoutLabel;
    @FXML
    private void onLogoutHover() {
        logoutLabel.setStyle("-fx-text-fill: darkblue; -fx-underline: true; -fx-cursor: hand;");
    }

    @FXML
    private void onLogoutExit() {
        logoutLabel.setStyle("-fx-text-fill: blue; -fx-underline: true; -fx-cursor: hand;");
    }

    @FXML
    private ImageView avatarImageView;
    @FXML
    private BorderPane mainContainer;
    @FXML
    private VBox navbarContainer; // VBox chứa Navbar
    @FXML
    private StackPane contentArea;


    //
//    private void loadNavbar() {
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/navbar.fxml"));
//            VBox navbar = loader.load();
//            NavbarController navbarController = loader.getController();
//            navbarContainer.getChildren().setAll(navbar);
//
//            // Lấy controller của Navbar
//
//            // Truyền contentArea vào NavbarController
//            navbarController.setContentArea(contentArea);
//            mainContainer.setLeft(navbar);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
    private User loggedInUser;

    public void setLoggedInUser(User user) {
        if (user == null) {
            System.err.println("❌ Lỗi: User được truyền vào là null!");
            return;
        }

        this.loggedInUser = user;
        System.out.println("✅ loggedInUser đã được cập nhật: " + loggedInUser.getUsername());

        String roleId = user.getRole();
        if (roleId == null || roleId.trim().isEmpty()) {
            System.err.println("⚠️ Lỗi: roleId là null hoặc rỗng!");
            return;
        }

        // Truy vấn role_name từ bảng roles dựa vào role_id
        String roleName = getRoleName(Integer.parseInt(roleId));

        Platform.runLater(() -> {
            usernameLabel.setText("Welcome, " + user.getFullname());
            roleLabel.setText(roleName);

            // Đường dẫn ảnh đại diện từ thư mục resources
            String avatarPath = "/src/billiardsmanagement/images/avatars/" + user.getImagePath();
            URL imageUrl = getClass().getResource(avatarPath);

            if (imageUrl != null) {
                avatarImageView.setImage(new Image(imageUrl.toExternalForm()));
            } else {
                System.out.println("⚠️ Không tìm thấy avatar, dùng ảnh mặc định.");
                URL defaultImageUrl = getClass().getResource("/src/billiardsmanagement/images/avatars/user.png");
                if (defaultImageUrl != null) {
                    avatarImageView.setImage(new Image(defaultImageUrl.toExternalForm()));
                } else {
                    System.err.println("❌ Không tìm thấy ảnh mặc định!");
                }
            }
            setupMenu();

        });
    }


    private String getRoleName(int roleId) {
        String roleName = "Unknown"; // Giá trị mặc định nếu không tìm thấy

        String query = "SELECT role_name FROM roles WHERE role_id = ?";
        try (Connection connection = TestDBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, roleId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                roleName = resultSet.getString("role_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return roleName;
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to logout?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            // Đóng cửa sổ hiện tại
            Stage stage = (Stage) logoutLabel.getScene().getWindow();
            stage.close();

            // Mở lại cửa sổ login
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/users/login.fxml"));
                Scene scene = new Scene(loader.load());

                Stage loginStage = new Stage();
                loginStage.setScene(scene);
                loginStage.setTitle("Login");
                loginStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private List<Button> allMenus = new ArrayList<>();

    public void initialize() {
        System.out.println("🔄 MainController đã khởi tạo");
        System.out.println("🔍 Debug: loggedInUser = " + (loggedInUser != null ? loggedInUser.getUsername() : "null"));

//        setupMenu();
    }

    private void setupMenu() {
        // Xóa các button cũ (nếu có)
        navbarContainer.getChildren().clear();

        // Tạo danh sách menu
        Button poolTableButton = createNavButton("Pool Table", "TABLE", "showPoolTablePage");
        Button ordersButton = createNavButton("Order", "SHOPPING_CART", "showOrdersPage");
        Button productsButton = createNavButton("Product", "CUBE", "showProductsPage");
        Button staffButton = createNavButton("Staff", "USERS", "showUsersPage");
        Button customerButton = createNavButton("Customer", "USER", "showCustomerPage");
        Button reportButton = createNavButton("Report", "BAR_CHART", "showReportPage");

        // Lưu tất cả menu vào danh sách để kiểm soát quyền truy cập
        allMenus.addAll(Arrays.asList(poolTableButton, ordersButton, productsButton, staffButton, customerButton, reportButton));

        // Kiểm tra quyền user và thêm menu hợp lệ vào VBox
        for (Button btn : allMenus) {
            if (isAllowed(btn.getText())) {
                navbarContainer.getChildren().add(btn); // Chỉ thêm Button nếu có quyền
            }
        }
    }

    // Hàm tạo Button cho Navbar
    private Button createNavButton(String text, String icon, String actionMethod) {
        Button button = new Button(text);
        button.getStyleClass().add("nav-item");

        FontAwesomeIconView iconView = new FontAwesomeIconView();
        iconView.setGlyphName(icon);
        iconView.setSize("16");
        button.setGraphic(iconView);

        button.setOnAction(event -> handleMenuClick(actionMethod));
        return button;
    }

    // Xử lý sự kiện khi click menu
    private void handleMenuClick(String actionMethod) {
        try {
            System.out.println("🛠 Gọi method: " + actionMethod);

            Method method = getClass().getMethod(actionMethod);
            method.invoke(this);
        } catch (NoSuchMethodException e) {
            System.out.println("⚠ Không tìm thấy phương thức: " + actionMethod);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Kiểm tra user có quyền vào menu không
    private boolean isAllowed(String menuName) {
        if (loggedInUser == null) return false;

        String userRole = loggedInUser.getRoleName(); // Lấy role của user
        List<String> allowedMenus = getAllowedMenusForRole(userRole);

        return allowedMenus.contains(menuName);
    }

    // Hàm trả về danh sách menu mà role này được phép truy cập
    private List<String> getAllowedMenusForRole(String role) {
        switch (role) {
            case "Admin":
                return Arrays.asList("Pool Table", "Order", "Product", "Staff", "Customer", "Report");
            case "Manager":
                return Arrays.asList("Pool Table", "Order", "Product", "Customer", "Report"); // Ẩn Staff
            case "Receptionist":
                return Arrays.asList("Pool Table", "Order", "Customer"); // Ẩn Staff, Report, Product
            default:
                return Arrays.asList("Pool Table", "Order"); // Chỉ thấy mỗi Customer
        }
    }

    @FXML
    public void showOrdersPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/orders/order.fxml"));
        BorderPane orderPage = loader.load();  // Tải FXML thành AnchorPane
        contentArea.getChildren().setAll(orderPage);
    }

    @FXML
    public void showProductsPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/products2/products2.fxml"));
        BorderPane productPage = loader.load();

//        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/products/products.fxml"));
//        AnchorPane productPage = loader.load();

        ProductController2 productController = loader.getController();
        productController.setCurrentUser(loggedInUser);
        System.out.println("🔹 Truyền user vào ProductController: " + (loggedInUser != null ? loggedInUser.getUsername() : "null"));

        if (productController == null) {
            System.out.println("Lỗi: Không lấy được ProductController!");
        } else {
            System.out.println("Debug: ProductController đã load, truyền user...");
            productController.setLoggedInUser(loggedInUser); // ✅ Truyền user đúng cách
        }

        contentArea.getChildren().setAll(productPage);
    }

    @FXML
    public void showUsersPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/users/users.fxml"));
        AnchorPane usersPage = loader.load();

        // Hiển thị giao diện Users trong contentArea
        contentArea.getChildren().setAll(usersPage);

        // Lấy controller của Users và truyền MainController vào
        UserController userController = loader.getController();
        userController.setMainController(this);
    }


    @FXML
    public void showRolesPermissionsPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/users/rolesPermissions.fxml"));
        AnchorPane rolesPermissionsPage = loader.load();

        // Lấy controller của RolesPermissions và truyền MainController vào
        RolesPermissionsController rolesPermissionsController = loader.getController();
        rolesPermissionsController.setMainController(this);

        // Hiển thị trang trong contentArea
        contentArea.getChildren().setAll(rolesPermissionsPage);
    }

    @FXML
    public void showPoolTablePage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/poolTables/poolTable.fxml"));
        AnchorPane poolTablePage = loader.load();

        // Get the controller and pass the logged-in user if needed
        src.billiardsmanagement.controller.poolTables.PoolTableController poolTableController = loader.getController();
        if (poolTableController != null) {
            // poolTableController.setLoggedInUser(loggedInUser); // Assuming you have a method to set the user
        } else {
            System.out.println("Error: Unable to retrieve PoolTableController!");
        }

        contentArea.getChildren().setAll(poolTablePage);
    }

    public void showCustomerPage(ActionEvent actionEvent) {
    }

    public void showReportPage(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/reports/report.fxml"));
        BorderPane reportPage = loader.load();  // Tải FXML thành AnchorPane
        contentArea.getChildren().setAll(reportPage);
    }
}
