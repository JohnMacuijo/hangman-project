package fr.quentincillierre.hangman.controller;

import java.io.IOException;

import fr.quentincillierre.hangman.application.SoundManager;
import fr.quentincillierre.hangman.model.Difficulty;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class DifficultyController {

    @FXML
    private MenuButton difficultyMenu;

    @FXML
    private Button startButton;

    @FXML
    private Button challengerButton;

    private Difficulty selectedDifficulty = Difficulty.EASY;

    @FXML
    public void initialize() {
        difficultyMenu.setText("EASY");
    }

    @FXML
    private void selectEasy() {
        SoundManager.playClickSound(440, 20);
        selectedDifficulty = Difficulty.EASY;
        difficultyMenu.setText("EASY");
    }

    @FXML
    private void selectMedium() {
        SoundManager.playClickSound(440, 20);
        selectedDifficulty = Difficulty.MEDIUM;
        difficultyMenu.setText("MEDIUM");
    }

    @FXML
    private void selectHard() {
        SoundManager.playClickSound(440, 20);
        selectedDifficulty = Difficulty.HARD;
        difficultyMenu.setText("HARD");
    }

    @FXML
    private void startGame() {
        SoundManager.playClickSound(440, 20);

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/fr/quentincillierre/hangman/application/game-view.fxml"));

            Parent root = loader.load();

            GameController controller = loader.getController();
            controller.setDifficulty(selectedDifficulty);

            Stage stage = (Stage) startButton.getScene().getWindow();

            stage.setScene(new Scene(root, 950, 850));

            stage.setFullScreenExitHint("");
            stage.setFullScreen(true);

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void challengerMode() {
        SoundManager.playClickSound(440, 20);

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/fr/quentincillierre/hangman/application/boss-battle-view.fxml"));

            Parent root = loader.load();

            Stage stage = (Stage) challengerButton.getScene().getWindow();

            stage.setScene(new Scene(root, 950, 850));

            stage.setFullScreenExitHint("");
            stage.setFullScreen(true);

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

   @FXML
    private void about() {
        SoundManager.playClickSound(440, 20);
 
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
 
        alert.setTitle("About");
        alert.setHeaderText("Hangman Game");
 
        String aboutText = """
                Version 1.0
 
                A classic word-guessing game with a twist: race the clock, \
                dodge the noose, and take on the Hangman King himself.
 
                HOW TO PLAY
                Guess the hidden word one letter at a time before the \
                hangman drawing is complete. Use the on-screen keyboard \
                or just type -- both work.
 
                DIFFICULTY
                • Easy    -- more time per correct letter, gentler pace
                • Medium  -- a balanced challenge
                • Hard    -- less time per letter, tougher words
 
                Each difficulty tracks its own high score, so you can \
                chase a new best on every setting.
 
                CHALLENGE MODE -- THE HANGMAN KING
                Face the King in a boss battle: solve 10 words to save \
                the prisoner. Each word gives you 15 seconds AND 13 \
                letter guesses -- run out of either and the noose \
                tightens one stage. Run out of stages, and the King wins.
 
                FEATURES
                • Multiple difficulties, each with its own high score
                • Live countdown timer with rising tension near the end
                • Unlock-a-letter hint, at the cost of precious seconds
                • Full sound design: clicks, ticks, wins, and losses
                • Challenger Mode boss battle against the Hangman King
 
                Developed using JavaFX.
                """;
 
        // Alert's default setContentText() can visually clip long multi-line
        // text, since the dialog pane doesn't always grow to fit it. A wrapped,
        // read-only TextArea as the dialog's content avoids that entirely.
        TextArea textArea = new TextArea(aboutText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefSize(460, 420);
        textArea.setStyle("-fx-font-size: 13px;");
 
        alert.getDialogPane().setContent(textArea);
        alert.getDialogPane().setPrefSize(500, 480);
        alert.setResizable(true);
 
        alert.showAndWait();
    }

    @FXML
    private void exit() {
        SoundManager.playClickSound(440, 20);
        Platform.exit();
    }

    public Difficulty getSelectedDifficulty() {
        return selectedDifficulty;
    }
}