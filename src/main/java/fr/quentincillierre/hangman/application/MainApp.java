package fr.quentincillierre.hangman.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource("/fr/quentincillierre/hangman/application/difficulty-view.fxml"));

        Parent root = loader.load();

        // Create the scene
        Scene scene = new Scene(root);

        // Allow resizing
        primaryStage.setResizable(true);

        // Minimum window size
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(700);

        // Start maximized (optional)
        primaryStage.setMaximized(true);

        // F11 toggles fullscreen
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F11) {
                primaryStage.setFullScreen(!primaryStage.isFullScreen());
            }
        });

        // Window title
        primaryStage.setTitle("Neon Hangman");

        // Show the scene
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setMaximized(true);
    }

    public static void main(String[] args) {

        System.setProperty("javafx.fullscreenExitHint", "");
        
        launch(args);

    }
}