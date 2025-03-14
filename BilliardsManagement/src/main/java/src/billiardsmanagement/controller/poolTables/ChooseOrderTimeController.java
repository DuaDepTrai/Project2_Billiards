package src.billiardsmanagement.controller.poolTables;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;

public class ChooseOrderTimeController implements Initializable {

    @FXML private VBox popupContent;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<Integer> hourPicker;
    @FXML private ComboBox<Integer> minutePicker;
    @FXML private Button confirmButton;

    private BiConsumer<LocalDate, LocalTime> onTimeSelected;
    private Runnable onClosePopup;  // Used to hide the popup from parent

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        LocalDate currentDate = LocalDate.now();
        datePicker.setValue(currentDate);

        datePicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item.isBefore(currentDate) || item.isAfter(currentDate.plusDays(1))) {
                    setDisable(true);
                }
                setText(item.format(DateTimeFormatter.ofPattern(
                        (item.getYear() == currentDate.getYear()) ? "dd/MM" : "dd/MM/yyyy"
                )));
            }
        });

        updateTimePickers();
        datePicker.setOnAction(e -> updateTimePickers());

        Timeline autoRefresh = new Timeline(new KeyFrame(Duration.minutes(1), e -> updateTimePickers()));
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();
    }

    private void updateTimePickers() {
        LocalDate selectedDate = datePicker.getValue();
        LocalTime now = LocalTime.now();

        hourPicker.getItems().clear();
        minutePicker.getItems().clear();

        if (selectedDate.equals(LocalDate.now())) {
            for (int h = now.getHour(); h < 24; h++) hourPicker.getItems().add(h);
        } else {
            for (int h = 0; h < 24; h++) hourPicker.getItems().add(h);
        }

        hourPicker.setValue(hourPicker.getItems().get(0));
        hourPicker.setOnAction(e -> updateMinutePicker());
        updateMinutePicker();
    }

    private void updateMinutePicker() {
        LocalTime now = LocalTime.now();
        LocalDate selectedDate = datePicker.getValue();
        Integer selectedHour = hourPicker.getValue();

        if (selectedDate == null || selectedHour == null) {
            selectedDate = LocalDate.now();
            selectedHour = now.getHour();
        }

        if (selectedDate.equals(LocalDate.now()) && selectedHour == now.getHour()) {
            int startMinute = (now.getMinute() / 5) * 5 + 5;
            for (int m = startMinute; m < 60; m += 5) minutePicker.getItems().add(m);
        } else {
            for (int m = 0; m < 60; m += 5) minutePicker.getItems().add(m);
        }

        minutePicker.setValue(minutePicker.getItems().get(0));
    }

    @FXML
    private void handleConfirm() {
        LocalDate selectedDate = datePicker.getValue();
        LocalTime selectedTime = LocalTime.of(hourPicker.getValue(), minutePicker.getValue());

        if (onTimeSelected != null) {
            onTimeSelected.accept(selectedDate, selectedTime);
        }

        if (onClosePopup != null) {
            onClosePopup.run(); // Hide the popup
        }
    }

    public void setOnTimeSelected(BiConsumer<LocalDate, LocalTime> onTimeSelected) {
        this.onTimeSelected = onTimeSelected;
    }

    public void setOnClosePopup(Runnable onClosePopup) {
        this.onClosePopup = onClosePopup;
    }
}

