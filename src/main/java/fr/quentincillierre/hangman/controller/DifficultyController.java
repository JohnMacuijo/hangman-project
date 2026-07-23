package fr.quentincillierre.hangman.controller;

import fr.quentincillierre.hangman.model.Difficulty;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DifficultyController {

    private Difficulty selectedDifficulty = Difficulty.EASY;

    @FXML
    private HBox easyCard;

    @FXML
    private HBox mediumCard;

    @FXML
    private HBox hardCard;

    @FXML
    public void initialize() {
        highlightSelectedCard();
    }

    @FXML
    private void selectEasy() {
        selectedDifficulty = Difficulty.EASY;
        highlightSelectedCard();
    }

    @FXML
    private void selectMedium() {
        selectedDifficulty = Difficulty.MEDIUM;
        highlightSelectedCard();
    }

    @FXML
    private void selectHard() {
        selectedDifficulty = Difficulty.HARD;
        highlightSelectedCard();
    }

    @FXML
private void startGame() {

    try {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fr/quentincillierre/hangman/application/game-view.fxml"));

        Parent root = loader.load();

        // Get the GameController
        GameController controller = loader.getController();

        // Pass the selected difficulty
        controller.setDifficulty(selectedDifficulty);

        Stage stage = (Stage) easyCard.getScene().getWindow();
        stage.setScene(new Scene(root, 950, 850));
        stage.show();

    } catch (IOException e) {
        e.printStackTrace();
    }
}

    private void highlightSelectedCard() {

        String normalStyle =
                "-fx-background-color:#0d1117;" +
                "-fx-background-radius:12;" +
                "-fx-border-color:#30363d;" +
                "-fx-border-radius:12;" +
                "-fx-padding:12;" +
                "-fx-cursor:hand;";

        String selectedStyle =
                "-fx-background-color:#0d1117;" +
                "-fx-background-radius:12;" +
                "-fx-border-color:#ff7a00;" +
                "-fx-border-width:2;" +
                "-fx-border-radius:12;" +
                "-fx-padding:12;" +
                "-fx-cursor:hand;" +
                "-fx-effect:dropshadow(three-pass-box, rgba(255,122,0,0.35),12,0,0,0);";

        easyCard.setStyle(normalStyle);
        mediumCard.setStyle(normalStyle);
        hardCard.setStyle(normalStyle);

        switch (selectedDifficulty) {

            case EASY:
                easyCard.setStyle(selectedStyle);
                break;

            case MEDIUM:
                mediumCard.setStyle(selectedStyle);
                break;

            case HARD:
                hardCard.setStyle(selectedStyle);
                break;
        }
    }

    public Difficulty getSelectedDifficulty() {
        return selectedDifficulty;
    }
}