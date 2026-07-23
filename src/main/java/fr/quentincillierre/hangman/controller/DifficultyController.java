package fr.quentincillierre.hangman.controller;

import java.io.IOException;

import fr.quentincillierre.hangman.model.Difficulty;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.input.KeyCode;
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
        selectedDifficulty = Difficulty.EASY;
        difficultyMenu.setText("EASY");
    }


    @FXML
    private void selectMedium() {
        selectedDifficulty = Difficulty.MEDIUM;
        difficultyMenu.setText("MEDIUM");
    }


    @FXML
    private void selectHard() {
        selectedDifficulty = Difficulty.HARD;
        difficultyMenu.setText("HARD");
    }



    // ==========================
    // START NORMAL GAME
    // ==========================
    @FXML
    private void startGame() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                    "/fr/quentincillierre/hangman/application/game-view.fxml")
            );


            Parent root = loader.load();


            GameController controller = loader.getController();

            controller.setDifficulty(selectedDifficulty);



            Stage stage = (Stage) startButton.getScene().getWindow();


            Scene scene = new Scene(root);


            addFullscreen(scene, stage);


            stage.setScene(scene);


            // Remove ESC fullscreen hint
            stage.setFullScreenExitHint("");


            // Fullscreen game
            stage.setFullScreen(true);


            stage.show();



        } catch (IOException e) {

            e.printStackTrace();

        }
    }




    // ==========================
    // START CHALLENGER MODE
    // ==========================
    @FXML
    private void challengerMode() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                    "/fr/quentincillierre/hangman/application/boss-battle-view.fxml")
            );


            Parent root = loader.load();


            Stage stage = (Stage) challengerButton.getScene().getWindow();


            Scene scene = new Scene(root);


            addFullscreen(scene, stage);


            stage.setScene(scene);


            // Remove ESC fullscreen hint
            stage.setFullScreenExitHint("");


            // Fullscreen challenger mode
            stage.setFullScreen(true);


            stage.show();



        } catch (IOException e) {

            e.printStackTrace();

        }
    }





    // ==========================
    // F11 FULLSCREEN TOGGLE
    // ==========================
    private void addFullscreen(Scene scene, Stage stage) {

        scene.setOnKeyPressed(event -> {


            if (event.getCode() == KeyCode.F11) {


                stage.setFullScreen(
                        !stage.isFullScreen()
                );


            }


        });

    }





    // ==========================
    // ABOUT
    // ==========================
    @FXML
    private void about() {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("About");

        alert.setHeaderText("Hangman Game");

        alert.setContentText("""
                Version 1.0

                Developed using JavaFX.

                Features:
                • Multiple Difficulties
                • Timer
                • Sound Effects
                • Challenger Mode
                """);

        alert.showAndWait();

    }





    // ==========================
    // EXIT
    // ==========================
    @FXML
    private void exit() {

        Platform.exit();

    }





    public Difficulty getSelectedDifficulty() {

        return selectedDifficulty;

    }

}