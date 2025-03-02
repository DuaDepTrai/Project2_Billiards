package src.billiardsmanagement.controller.users;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import src.billiardsmanagement.dao.UserDAO;
import src.billiardsmanagement.model.User;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;
import java.text.SimpleDateFormat;


public class UserController {
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

    private ObservableList<User> userList = FXCollections.observableArrayList();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private UserDAO userDAO = new UserDAO();

    public void initialize() {
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
        btnRolesPermissions.setOnAction(event -> openRolesPermissions());
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
            private final Button deleteButton = new Button();

            {
                FontAwesomeIconView editIcon = new FontAwesomeIconView(FontAwesomeIcon.PENCIL);
                editIcon.setSize("16");
                editButton.setGraphic(editIcon);
                editButton.getStyleClass().add("action-button");

                FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
                deleteIcon.setSize("16");
                deleteButton.setGraphic(deleteIcon);
                deleteButton.getStyleClass().add("action-button");

                container.setAlignment(Pos.CENTER);
                container.getChildren().addAll(editButton, deleteButton);

                editButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    openUpdateWindow(user);
                });

                deleteButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    confirmAndRemoveUser(user);
                });
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

                    // Đường dẫn trong resources (KHÔNG có "/src/")
                    String avatarPath = "/src/billiardsmanagement/images/avatars/" + user.getImagePath();
                    URL imageUrl = getClass().getResource(avatarPath);

                    if (imageUrl != null) {
                        avatarView.setImage(new Image(imageUrl.toExternalForm()));
                    } else {
                        URL defaultImageUrl = getClass().getResource("/src/billiardsmanagement/images/avatars/user.png");
                        if (defaultImageUrl != null) {
                            avatarView.setImage(new Image(defaultImageUrl.toExternalForm()));
                        } else {
                            System.err.println("Default avatar not found!");
                        }
                    }

                    setGraphic(avatarView);
                }
            }
        };
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
        alert.setContentText("User: " + user.getUsername());

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
}