package src.billiardsmanagement.controller.users;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import src.billiardsmanagement.controller.MainController;
import src.billiardsmanagement.controller.users.UpdateUserController;
import src.billiardsmanagement.dao.PermissionDAO;
import src.billiardsmanagement.dao.UserDAO;
import src.billiardsmanagement.model.User;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.text.SimpleDateFormat;


public class UserController {
    @FXML
    private TableColumn<User, Integer> sttColumn;
    @FXML
    private TableView<User> tableUsers;
    @FXML
    private TableColumn<User, ImageView> columnAvatar;
    @FXML
    private TableColumn<User, String> columnUsername;
    @FXML
    private TableColumn<User, String> columnRole;
    @FXML
    private TableColumn<User, String> columnFullname;
    @FXML
    private TableColumn<User, String> columnPhone;
    @FXML
    private TableColumn<User, String> columnBirthday;
    @FXML
    private TableColumn<User, String> columnAddress;
    @FXML
    private TableColumn<User, String> columnHireDate;
    @FXML
    private TableColumn<User, Void> columnAction;
    @FXML
    private Button btnAddNewUser;
    @FXML
    private Button btnRolesPermissions;
    @FXML
    private TextField searchText;
    private ObservableList<User> userList = FXCollections.observableArrayList();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private User currentUser; // Lưu user đang đăng nhập


    private UserDAO userDAO = new UserDAO();

    public void initialize() {
        setUpSearchField();
        sttColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(tableUsers.getItems().indexOf(cellData.getValue()) + 1)
        );
        sttColumn.setPrefWidth(50);
        columnAvatar.setCellFactory(createAvatarCellFactory());
        columnUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        columnRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        columnFullname.setCellValueFactory(new PropertyValueFactory<>("fullname"));
        columnPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        columnBirthday.setCellValueFactory(cellData -> {
            Date date = cellData.getValue().getBirthday(); // Lấy Date từ User
            String formattedDate = (date != null) ? dateFormat.format(date) : ""; // Chuyển thành String
            return new SimpleStringProperty(formattedDate);
        });
        columnBirthday.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
            }
        });

        columnAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        columnHireDate.setCellValueFactory(cellData -> {
            Date date = cellData.getValue().getHireDate(); // Lấy Date từ User
            String formattedDate = (date != null) ? dateFormat.format(date) : ""; // Chuyển thành String
            return new SimpleStringProperty(formattedDate);
        });
        columnHireDate.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
            }
        });

        columnAction.setCellFactory(createActionCellFactory());

        loadUsers();

        btnAddNewUser.setOnAction(event -> handleAddNewUser());

        tableUsers.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY); // Cho phép cột co giãn tự do

        sttColumn.prefWidthProperty().bind(tableUsers.widthProperty().multiply(0.03)); // 5% tổng chiều rộng
        columnAvatar.prefWidthProperty().bind(tableUsers.widthProperty().multiply(0.05)); // 10%
        columnUsername.prefWidthProperty().bind(tableUsers.widthProperty().multiply(0.1)); // 15%
        columnRole.prefWidthProperty().bind(tableUsers.widthProperty().multiply(0.1)); // 10%
        columnFullname.prefWidthProperty().bind(tableUsers.widthProperty().multiply(0.15)); // 15%
        columnPhone.prefWidthProperty().bind(tableUsers.widthProperty().multiply(0.08)); // 10%
        columnBirthday.prefWidthProperty().bind(tableUsers.widthProperty().multiply(0.08)); // 10%
        columnAddress.prefWidthProperty().bind(tableUsers.widthProperty().multiply(0.22)); // 15%
        columnHireDate.prefWidthProperty().bind(tableUsers.widthProperty().multiply(0.08)); // 10%
        columnAction.prefWidthProperty().bind(tableUsers.widthProperty().multiply(0.10)); // 10%

        // Căn trái
        columnUsername.setStyle("-fx-alignment: CENTER-LEFT;");
        columnRole.setStyle("-fx-alignment: CENTER-LEFT;");
        columnFullname.setStyle("-fx-alignment: CENTER-LEFT;");
        columnAddress.setStyle("-fx-alignment: CENTER-LEFT;");

        // Căn giữa
        sttColumn.setStyle("-fx-alignment: CENTER;");
        columnAvatar.setStyle("-fx-alignment: CENTER;");
        columnAction.setStyle("-fx-alignment: CENTER;");
        columnPhone.setStyle("-fx-alignment: CENTER;");
        columnBirthday.setStyle("-fx-alignment: CENTER;");
        columnHireDate.setStyle("-fx-alignment: CENTER;");

        // Căn phải

    }

    private void loadUsers() {
        try {
            userList.clear();
            userList.addAll(userDAO.getAllUsers());
            tableUsers.setItems(userList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String formatDate(String dateString) {
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return inputFormatter.parse(dateString, outputFormatter::format);
        } catch (Exception e) {
            return dateString; // Trả về nguyên bản nếu lỗi
        }
    }

    private Callback<TableColumn<User, Void>, TableCell<User, Void>> createActionCellFactory() {
        return column -> new TableCell<>() {
            private final HBox container = new HBox(10);
            private final Button editButton = new Button();
//            private final Button deleteButton = new Button();

            {
                FontAwesomeIconView editIcon = new FontAwesomeIconView(FontAwesomeIcon.PENCIL);
                editIcon.setSize("16");
                editButton.setGraphic(editIcon);
                editButton.getStyleClass().add("action-button");

//                FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
//                deleteIcon.setSize("16");
//                deleteButton.setGraphic(deleteIcon);
//                deleteButton.getStyleClass().add("action-button");

                container.setAlignment(Pos.CENTER);
                container.getChildren().addAll(editButton);

                editButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    openUpdateWindow(user);
                });

//                deleteButton.setOnAction(event -> {
//                    User user = getTableView().getItems().get(getIndex());
//                    confirmAndRemoveUser(user);
//                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        };
    }

    private Callback<TableColumn<User, ImageView>, TableCell<User, ImageView>> createAvatarCellFactory() {
        return column -> new TableCell<>() {
            @Override
            protected void updateItem(ImageView imageView, boolean empty) {
                super.updateItem(imageView, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    User user = getTableRow().getItem();
                    ImageView avatarView = new ImageView();
                    avatarView.setFitWidth(40);
                    avatarView.setFitHeight(40);
                    avatarView.setPreserveRatio(true);
                    avatarView.getStyleClass().add("avatar-image"); // Áp dụng CSS

                    // Đường dẫn trong resources (KHÔNG có "/src/")
                    String avatarPath = "/src/billiardsmanagement/images/avatars/" + user.getImagePath();
                    URL imageUrl = getClass().getResource(avatarPath);

                    if (imageUrl == null) {
                        System.out.println("\u001B[31m" + "🤔 Oops! Looks like we can't find the avatar! Did the user run away? 🏃‍♂️💨" + "\u001B[0m");
                    }

                    if (imageUrl != null) {
                        String trueImageUrl = imageUrl.toExternalForm();

                        Platform.runLater(() -> {
                            avatarView.setImage(null); // Clear cache
                            avatarView.setImage(new Image(trueImageUrl, false)); // Force immediate reload
                        });

                    } else {
                        URL defaultImageUrl = getClass().getResource("/src/billiardsmanagement/images/avatars/user.png");
                        if (defaultImageUrl != null) {
                            avatarView.setImage(new Image(defaultImageUrl.toExternalForm()));
                        } else {
                            System.err.println("Default avatar not found!");
                        }
                    }

                    System.out.println("\u001B[31m" + "🤔 User " + user.getUsername()+ ", image path : "+user.getImagePath() + " 🏃‍♂️💨" + "\u001B[0m");
                    setGraphic(avatarView);
                }
            }
        };
    }

    private void copyImageToTarget(String imageName, URL sourceUrl) {
        try {
            // Define the target directory (inside /target)
            Path targetDir = Paths.get("target/classes/billiardsmanagement/images/avatars/");
            Path targetPath = targetDir.resolve(imageName);

            // Ensure directory exists before copying
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            // Convert URL to Path (requires URI conversion)
            Path sourcePath = Paths.get(sourceUrl.toURI());

            // Copy image to target folder (overwrite if exists)
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("✅ Image successfully copied to target: " + targetPath);
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("❌ Error copying image to target: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Unexpected error: " + e.getMessage());
        }
    }

    private void transferAvatarToTarget(User user, File newImageFile, ImageView avatarView) {
        // 1️⃣ Transfer image in background
        Task<Void> transferTask = new Task<>() {
            protected Void call() throws Exception {
                Path targetDir = Paths.get("target/classes/src/billiardsmanagement/images/avatars");
                Path sourcePath = newImageFile.toPath();

                // Ensure directory exists before copying
                if (!Files.exists(targetDir)) {
                    Files.createDirectories(targetDir);
                }

                // Generate a unique filename if necessary
                String fileName = newImageFile.getName();
                Path targetPath = targetDir.resolve(fileName);
                int counter = 1;

                // Check if file already exists, and rename if needed
                while (Files.exists(targetPath)) {
                    String nameWithoutExt = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
                    String extension = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.')) : "";
                    String newFileName = nameWithoutExt + "_" + counter + extension;
                    targetPath = targetDir.resolve(newFileName);
                    counter++;
                }

                // Print source and target paths with funny icons
                System.out.println("\u001B[34m" + "🔍 Checking source path: " + sourcePath + " 🚀" + "\u001B[0m");
                System.out.println("\u001B[33m" + "🏁 Target path ready: " + targetPath + " 🎯" + "\u001B[0m");

                // Copy image to target folder (ensuring it's a new file)
                Files.copy(sourcePath, targetPath);

                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    // 2️⃣ Update user avatar path in database
                    user.setImagePath(newImageFile.getName());

                    // 3️⃣ Reload avatar immediately
                    refreshAvatar(user, avatarView);
                });
            }

            @Override
            protected void failed() {
                getException().printStackTrace();
            }
        };

        new Thread(transferTask).start();
    }

    private void refreshAvatar(User user, ImageView avatarView) {
        // 4️⃣ Load updated image path from target folder
        String avatarPath = "/src/billiardsmanagement/images/avatars/" + user.getImagePath();
        URL imageUrl = getClass().getResource(avatarPath);

        if (imageUrl == null) {
            System.err.println("❌ Avatar not found in target folder!");
        } else {
            System.out.println("✅ Avatar loaded: " + imageUrl);
        }

        // 5️⃣ Force JavaFX to reload the image
        Platform.runLater(() -> {
            avatarView.setImage(null); // Clear previous image
            if (imageUrl != null) {
                avatarView.setImage(new Image(imageUrl.toExternalForm(), false)); // Disable cache
            }
        });
    }

    @FXML
    private void handleAddNewUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/users/addUser.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Add New USer");
            stage.setScene(new Scene(root));
            stage.show();
            stage.setOnHidden(event -> refreshTable());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshTable() {
        loadUsers();
    }

    private void handleRemoveSelectedUser() {
        User selectedUser = tableUsers.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No User Selected");
            alert.setContentText("Please select a user to remove");
            alert.showAndWait();
            return;
        }
        confirmAndRemoveUser(selectedUser);
    }

    private void confirmAndRemoveUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Remove");
        alert.setHeaderText("Are you sure you want to remove this user?");
        alert.setContentText("User: " + user.getUsername() + "\nFullname: " + user.getFullname() + "\nRole: " + user.getRole());

        ButtonType buttonYes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType buttonNo = new ButtonType("No", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(buttonYes, buttonNo);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == buttonYes) {
            removeUser(user);
        }
    }

    private void removeUser(User user) {
        try {
            userDAO.removeUser(user.getId());
            userList.remove(user);
            tableUsers.refresh();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleUpdateSelectedUser() {
        User selectedUser = tableUsers.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No User Selected");
            alert.setContentText("Please select a user to update");
            alert.showAndWait();
            return;
        }
        openUpdateWindow(selectedUser);
    }

    private void openUpdateWindow(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/src/billiardsmanagement/users/updateUser.fxml"));
            Parent root = loader.load();
            UpdateUserController controller = loader.getController();
            controller.setUserData(user);

            Stage stage = new Stage();
            stage.setTitle("Update User");
            stage.setScene(new Scene(root));
            stage.show();
            stage.setOnHidden(event -> refreshTable());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void openRolesPermissions() {
        if (mainController != null) {
            try {
                mainController.showRolesPermissionsPage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void applyPermissions() {
        if (currentUser != null) {
            PermissionDAO permissionDAO = new PermissionDAO();
            List<String> permissions = permissionDAO.getUserPermissions(currentUser.getId());
            System.out.println("✅ Permissions: " + permissions);
        } else {
            System.err.println("⚠️ Lỗi: currentUser bị null trong ProductController!");
        }
    }

    private User loggedInUser;

    public void setLoggedInUser(User user) {
        System.out.println("🟢 Gọi setCurrentUser() với user: " + (user != null ? user.getUsername() : "null"));

        this.loggedInUser = user;
        if (user != null) {
            System.out.println("🟢 Gọi setCurrentUser() với user: " + user.getUsername());
            System.out.println("🎯 Kiểm tra quyền sau khi truyền user...");
            List<String> permissions = user.getPermissionsAsString();
            System.out.println("🔎 Debug: Quyền sau khi truyền user = " + permissions);
            applyPermissions();
        } else {
            System.err.println("❌ Lỗi: currentUser vẫn null sau khi set!");
        }
    }

    private void filterUsers(String searchItem) throws SQLException {
        ObservableList<User> filteredList = FXCollections.observableArrayList();
        for (User user : userDAO.getAllUsers()) {
            if (user.getUsername().toLowerCase().contains(searchItem) ||
                    user.getFullname().toLowerCase().contains(searchItem) ||
                    user.getPhone().contains(searchItem)) {
                filteredList.add(user);
            }
        }
        tableUsers.setItems(filteredList);
    }

    private void setUpSearchField() {
        searchText.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                loadUsers();
            } else {
                try {
                    filterUsers(newValue);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

}