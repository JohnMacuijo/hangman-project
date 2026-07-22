package fr.quentincillierre.hangman.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fr/quentincillierre/hangman/application/game-view.fxml"));
        Parent root = loader.load();

        // 1. Scene setup
        Scene scene = new Scene(root, 950, 850);

        // 2. Set Minimum Dimensions (prevents UI from collapsing too small)
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(750);

        // 3. Enable resizing & standard window controls
        primaryStage.setResizable(true); 

        primaryStage.setTitle("Neon Hangman");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}