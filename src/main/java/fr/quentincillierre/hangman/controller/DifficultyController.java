package fr.quentincillierre.hangman.controller;

import fr.quentincillierre.hangman.model.Difficulty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class DifficultyController {

    private Difficulty selectedDifficulty = Difficulty.EASY;

    @FXML
    private Button easyButton;

    @FXML
    private Button mediumButton;

    @FXML
    private Button hardButton;

    @FXML
    public void initialize() {
        highlightSelectedButton();
    }


    @FXML
    private void selectEasy() {
        selectedDifficulty = Difficulty.EASY;
        highlightSelectedButton();
    }


    @FXML
    private void selectMedium() {
        selectedDifficulty = Difficulty.MEDIUM;
        highlightSelectedButton();
    }


    @FXML
    private void selectHard() {
        selectedDifficulty = Difficulty.HARD;
        highlightSelectedButton();
    }


    private void highlightSelectedButton() {

        easyButton.getStyleClass().remove("selected-difficulty");
        mediumButton.getStyleClass().remove("selected-difficulty");
        hardButton.getStyleClass().remove("selected-difficulty");


        switch (selectedDifficulty) {

            case EASY:
                easyButton.getStyleClass().add("selected-difficulty");
                break;

            case MEDIUM:
                mediumButton.getStyleClass().add("selected-difficulty");
                break;

            case HARD:
                hardButton.getStyleClass().add("selected-difficulty");
                break;
        }
    }


    @FXML
    private void startGame() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fr/quentincillierre/hangman/application/game-view.fxml")
            );

            Parent root = loader.load();

            GameController controller = loader.getController();

            controller.setDifficulty(selectedDifficulty);


            Stage stage = (Stage) easyButton.getScene().getWindow();

            stage.setScene(new Scene(root, 950, 850));
            stage.show();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public Difficulty getSelectedDifficulty() {
        return selectedDifficulty;
    }
}
