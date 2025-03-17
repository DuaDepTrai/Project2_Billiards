package src.billiardsmanagement.controller;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
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
import org.apache.pdfbox.Loader;
import src.billiardsmanagement.controller.database.BookingAndOrderTableListener;
import src.billiardsmanagement.controller.orders.ForEachOrderController;
import src.billiardsmanagement.controller.orders.OrderController;
import src.billiardsmanagement.controller.poolTables.*;
import src.billiardsmanagement.controller.products2.ProductController2;
import src.billiardsmanagement.controller.users.RolesPermissionsController;
import src.billiardsmanagement.controller.users.UserController;
import src.billiardsmanagement.model.TestDBConnection;
import src.billiardsmanagement.model.User;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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
//    @FXML
//    private void onLogoutHover() {
//        logoutLabel.setStyle("-fx-text-fill: darkblue; -fx-underline: true; -fx-cursor: hand;");
//    }
//
//    @FXML
//    private void onLogoutExit() {
//        logoutLabel.setStyle("-fx-text-fill: white; -fx-underline: true; -fx-cursor: hand;");
//    }

    @FXML
    private ImageView avatarImageView;
    @FXML
    private BorderPane mainContainer;
    @FXML
    private VBox navbarContainer; // VBox chứa Navbar
    @FXML
    private StackPane contentArea;

    private OrderController orderController;
    private PoolTableController poolTableController;

    private FXMLLoader orderLoader;
    private BorderPane orderPane;

    private FXMLLoader poolTableLoader;
    private AnchorPane poolTablePane;

    private ForEachOrderController forEachOrderController;
    private Parent forEachOrderPage;
    private FXMLLoader forEachOrderLoader;

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
//                    avatarImageView.getStyleClass().add("avatar-image"); // Áp dụng CSS

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

    public void initializeAllControllers() {
        try {
            orderLoader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/orders/order.fxml"));
            orderPane = orderLoader.load();
            orderController = orderLoader.getController();

            // pooltables, not fcking poolTables !
            URL poolTableURL = getClass().getResource("/src/billiardsmanagement/pooltables/poolTable.fxml");
            System.out.println("Pool Table FXML Path: " + poolTableURL);

            if (poolTableURL == null) {
                throw new Exception("ERROR: poolTable.fxml not found! Check file path.");
            }

            poolTableLoader = new FXMLLoader(poolTableURL);
            poolTablePane = poolTableLoader.load();
            poolTableController = poolTableLoader.getController();

            // Reload all controller, pane and loader if one of those is null.
//            if (poolTableController == null || poolTablePane==null || poolTableLoader == null) {
//                try {
//                    poolTableController = null;
//                    poolTablePane = null;
//                    poolTableLoader = null;
//
//
//                } catch (IOException e) {
//                    e.printStackTrace(); // Handle the exception as necessary
//                }
//            } else {
//                // Skip loading since the controller is already initialized
//                System.out.println("PoolTableController is already initialized, skipping load.");
//            }

            if(forEachOrderLoader==null) forEachOrderLoader = new FXMLLoader(MainController.class.getResource("/src/billiardsmanagement/orders/forEachOrder.fxml"));
            if(forEachOrderPage==null) forEachOrderPage = forEachOrderLoader.load();
            if(forEachOrderController==null) forEachOrderController = forEachOrderLoader.getController();
            if(forEachOrderController != null) {
                System.out.println("🔹 forEachOrderController đã load!");
            }
            else{
                System.out.println("❌ forEachOrderController không load được!");
            }

            // Inject OrderController's Table View + Order List into Listener
            BookingAndOrderTableListener bookingAndOrderTableListener = new BookingAndOrderTableListener();
            bookingAndOrderTableListener.setOrderTable(orderController.getOrderTable());
            bookingAndOrderTableListener.setOrderList(orderController.getOrderList());
            bookingAndOrderTableListener.setForEachController(this.forEachOrderController);
            bookingAndOrderTableListener.setPoolTableController(this.poolTableController);
            bookingAndOrderTableListener.startListening();
        } catch (Exception e) {
            System.out.println("Loading Pool Table error : "+e.getMessage());
            e.printStackTrace();
        }
    }

    public void initialize() {
        System.out.println("🔄 MainController đã khởi tạo");
        System.out.println("🔍 Debug: loggedInUser = " + (loggedInUser != null ? loggedInUser.getUsername() : "null"));

//        setupMenu();
    }

    private List<Button> allMenus = new ArrayList<>();

    private void setupMenu() {
        // Xóa các button cũ (nếu có)
        navbarContainer.getChildren().clear();

        // Tạo danh sách menu với quyền tương ứng
        Map<String, String> menuPermissions = new HashMap<>();
        menuPermissions.put("Pool Tables", "view_pool");
        menuPermissions.put("Orders", "view_order");
        menuPermissions.put("Products", "view_product");
        menuPermissions.put("Staffs", "view_user");
        menuPermissions.put("Roles & Permissions", "view_role_permission");
        menuPermissions.put("Customers", "view_customer");
        menuPermissions.put("Reports", "view_report");

        FontAwesomeIconView iconView = new FontAwesomeIconView();

        // Tạo danh sách menu button
        List<Button> menuButtons = Arrays.asList(
                createNavButton("Pool Tables", "TABLE", "showPoolTablePage"),
                createNavButton("Orders", "LIST", "showOrdersPage"),
                createNavButton("Products", "CUBE", "showProductsPage"),
                createNavButton("Staffs", "USER", "showUsersPage"),
                createNavButton("Roles & Permissions", "LOCK", "showRolesPermissionsPage"),
                createNavButton("Customers", "USERS", "showCustomerPage"),
                createNavButton("Reports", "BAR_CHART", "showReportPage")
        );

        // Lưu tất cả vào danh sách để kiểm soát quyền truy cập
        allMenus.clear();
        allMenus.addAll(menuButtons);

        // Kiểm tra quyền user và hiển thị menu phù hợp
        for (Button btn : allMenus) {
            String menuName = btn.getText();
            String requiredPermission = menuPermissions.get(menuName);

            if (requiredPermission != null && isAllowed(requiredPermission)) {
                navbarContainer.getChildren().add(btn); // Chỉ thêm button nếu có quyền
            }
        }

        File file = new File("src/main/resources/src/billiardsmanagement/images/bg_navbar.jpg");
        System.out.println("File exists: " + file.exists());

    }

    // 🔹 Kiểm tra user có quyền vào menu không
    private boolean isAllowed(String requiredPermission) {
        if (loggedInUser == null) return false;

        List<String> userPermissions = loggedInUser.getPermissionsAsString();
        return userPermissions.contains(requiredPermission);
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

    @FXML
    public void showOrdersPage() throws IOException {
        if (orderLoader != null && orderPane != null && orderController != null) {
            orderController.setMainController(this); // Truyền MainController vào OrderController

            orderController.setForEachOrderController(this.forEachOrderController);
            orderController.setForEachOrderLoader(this.forEachOrderLoader);
            orderController.setForEachOrderPage(this.forEachOrderPage);

            contentArea.getChildren().setAll(orderPane); // Hiển thị trang order
            orderController.initializeOrderController();
        }
    }

    @FXML
    public void showProductsPage() throws IOException, SQLException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/products2/products2.fxml"));
        BorderPane productPage = loader.load();

//        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/products/products.fxml"));
//        AnchorPane productPage = loader.load();

        ProductController2 productController = loader.getController();
        productController.setLoggedInUser(loggedInUser);
        System.out.println("🔹 Truyền user vào ProductController: " + (loggedInUser != null ? loggedInUser.getUsername() : "null"));

        if (productController == null) {
            System.out.println("Lỗi: Không lấy được ProductController!");
        } else {
            System.out.println("Debug: ProductController đã load, truyền user...");
            productController.setLoggedInUser(loggedInUser); // ✅ Truyền user đúng cách
            productController.setUser(loggedInUser);
        }

        contentArea.getChildren().setAll(productPage);
    }

    @FXML
    public void showUsersPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/users/users.fxml"));
        AnchorPane usersPage = loader.load();

        UserController userController = loader.getController();
        userController.setCurrentUser(loggedInUser);
        System.out.println("🔹 Truyền user vào UserController: " + (loggedInUser != null ? loggedInUser.getUsername() : "null"));

        if (userController == null) {
            System.out.println("Lỗi: Không lấy được UserController!");
        } else {
            System.out.println("Debug: UserController đã load, truyền user...");
            userController.setLoggedInUser(loggedInUser); // ✅ Truyền user đúng cách
        }
        // Hiển thị giao diện Users trong contentArea
        contentArea.getChildren().setAll(usersPage);

        // Lấy controller của Users và truyền MainController vào
//        UserController userController = loader.getController();
//        userController.setMainController(this);
    }


    @FXML
    public void showRolesPermissionsPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/users/rolesPermissions.fxml"));
        AnchorPane rolesPermissionsPage = loader.load();

        // Lấy controller của RolesPermissions và truyền MainController vào
//        RolesPermissionsController rolesPermissionsController = loader.getController();
//        rolesPermissionsController.setMainController(this);

        RolesPermissionsController roleperController = loader.getController();
        roleperController.setCurrentUser(loggedInUser);
        System.out.println("🔹 Truyền user vào RolesPermissionsController: " + (loggedInUser != null ? loggedInUser.getUsername() : "null"));

        if (roleperController == null) {
            System.out.println("Lỗi: Không lấy được RolesPermissionsController!");
        } else {
            System.out.println("Debug: RolesPermissionsController đã load, truyền user...");
            roleperController.setLoggedInUser(loggedInUser); // ✅ Truyền user đúng cách
        }

        // Hiển thị trang trong contentArea
        contentArea.getChildren().setAll(rolesPermissionsPage);
    }

    @FXML
    public void showPoolTablePage() throws IOException, SQLException {

        // Get the controller and pass the logged-in user if needed
//        src.billiardsmanagement.controller.poolTables.PoolTableController poolTableController = loader.getController();
//        if (poolTableController != null) {
//            // poolTableController.setLoggedInUser(loggedInUser); // Assuming you have a method to set the user
//        } else {
//            System.out.println("Error: Unable to retrieve PoolTableController!");
//        }

        if (poolTableLoader != null && poolTablePane != null && poolTableController != null) {
            poolTableController.setCurrentUser(loggedInUser);
            poolTableController.setUser(loggedInUser);
            poolTableController.setMainController(this);
            poolTableController.setOrderController(orderController);

            System.out.println("🔹 Truyền user vào poolTableController: " + (loggedInUser != null ? loggedInUser.getUsername() : "null"));

            if (poolTableController == null) {
                System.out.println("Lỗi: Không lấy được ProductController!");
            } else {
                System.out.println("Debug: poolTableController đã load, truyền user...");
                poolTableController.setLoggedInUser(loggedInUser); // ✅ Truyền user đúng cách
            }

            contentArea.getChildren().setAll(poolTablePane);
            poolTableController.initializePoolTableController();
        }
    }

    public void showCustomerPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/customer/customer.fxml"));
        AnchorPane customerPage = loader.load();  // Tải FXML thành AnchorPane

        contentArea.getChildren().setAll(customerPage);
    }

    public void showReportPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/reports/report.fxml"));
        BorderPane reportPage = loader.load();  // Tải FXML thành AnchorPane

        contentArea.getChildren().setAll(reportPage);
    }

    @FXML
    public void showHomePage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/home.fxml"));
        StackPane homePage = loader.load();
        contentArea.getChildren().setAll(homePage);
    }

    // Phương thức để lấy contentArea
    public StackPane getContentArea() {
        return contentArea;
    }


}
