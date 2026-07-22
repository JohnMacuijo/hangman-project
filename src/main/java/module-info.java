module fr.quentincillierre.hangman {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    exports fr.quentincillierre.hangman.application;
    opens fr.quentincillierre.hangman.application to javafx.fxml;

    exports fr.quentincillierre.hangman.controller;
    opens fr.quentincillierre.hangman.controller to javafx.fxml;

    exports fr.quentincillierre.hangman.model;
}