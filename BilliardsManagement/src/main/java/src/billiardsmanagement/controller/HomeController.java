package src.billiardsmanagement.controller;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;

public class HomeController {
    @FXML
    private ImageView homeImageView;

    @FXML
    public void initialize() {
        String imagePath = "/src/billiardsmanagement/images/logo22.png";
        URL imageUrl = getClass().getResource(imagePath);

        if (imageUrl != null) {
            homeImageView.setImage(new Image(imageUrl.toExternalForm()));
        } else {
            System.err.println("❌ Không tìm thấy ảnh logo: " + imagePath);
        }
    }
}
