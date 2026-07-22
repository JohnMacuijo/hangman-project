package fr.quentincillierre.hangman.controller;

import fr.quentincillierre.hangman.model.HangmanModel;
import fr.quentincillierre.hangman.model.WordRepository;
import fr.quentincillierre.hangman.model.WordRepository.HangmanQuestion;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GameController {   

    @FXML
    private HBox wordDisplayBox;

    @FXML
    private Label wordLengthLabel;

    @FXML
    private Label wrongLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private HBox statusContainer;

    @FXML
    private Label hintLabel; 

    @FXML
    private ImageView hangmanImageView;

    @FXML
    private ProgressBar dangerProgressBar;

    @FXML
    private GridPane keyboardGrid;

    @FXML
    private Button restartBtn;

    private HangmanModel model;
    private String currentHint = ""; 

    @FXML
private Label timerLabel;

private Timeline countdownTimer;
private int timeRemaining = 60;

    private final String[][] QWERTY_LAYOUT = {
        {"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"},
        {"A", "S", "D", "F", "G", "H", "J", "K", "L"},
        {"Z", "X", "C", "V", "B", "N", "M"}
    };

    @FXML
    public void initialize() {
        applyButtonAnimations(restartBtn, "#ff7a00", "#0b0c10", "#e06b00", "#0b0c10");
        
        startNewGame();

        

        Platform.runLater(() -> {
            if (keyboardGrid.getScene() != null) {
                keyboardGrid.getScene().setOnKeyPressed(e -> {
                    KeyCode code = e.getCode();
                    if (code != null && code.isLetterKey()) {
                        handleLetter(code.toString().toUpperCase());
                    }
                });
            }
        });
    }

    private void startNewGame() {
        WordRepository wordRepository = new WordRepository();
        HangmanQuestion question = wordRepository.getRandomQuestion();

        model = new HangmanModel(question.text());
        currentHint = question.hint();

        restartBtn.setText("New Word");
        statusContainer.setVisible(false);
        statusContainer.setManaged(false);

        generateKeyboard();
        refreshUI();
        
        keyboardGrid.setDisable(false);
        startTimer();
    }

    private void startTimer() {

    if (countdownTimer != null) {
        countdownTimer.stop();
    }

    timeRemaining = 30;

    timerLabel.setText(String.valueOf(timeRemaining));

    countdownTimer = new Timeline(
        new KeyFrame(Duration.seconds(1), e -> {

            timeRemaining--;

            timerLabel.setText(String.valueOf(timeRemaining));

            if (timeRemaining <= 0) {

                countdownTimer.stop();

                statusContainer.setVisible(true);
                statusContainer.setManaged(true);

                statusContainer.setStyle(
                        "-fx-background-color: rgba(248,81,73,0.15);" +
                        "-fx-border-color:#f85149;" +
                        "-fx-border-radius:6px;" +
                        "-fx-background-radius:6px;" +
                        "-fx-padding:10px 25px;");

                statusLabel.setText("⏰ Time's Up!");
                statusLabel.setStyle(
                        "-fx-text-fill:#f85149;" +
                        "-fx-font-weight:bold;" +
                        "-fx-font-size:15px;");

                keyboardGrid.setDisable(true);
            }

        })
    );

    countdownTimer.setCycleCount(Timeline.INDEFINITE);

    countdownTimer.play();

}

    private void handleLetter(String s) {
        if (s == null || s.isBlank() || model.isWin() || model.isLose()) return;
        model.tryLetter(s.charAt(0));
        refreshUI();
    }

    private void refreshUI() {
        renderWordBoxes();

        int maxAttempts = 10; 
        int currentWrongs = model.getCurrentWrongs();
        int attemptsLeft = Math.max(0, maxAttempts - currentWrongs);
        wrongLabel.setText(attemptsLeft + " left");

        double progress = (double) currentWrongs / maxAttempts;
        dangerProgressBar.setProgress(progress);

        hintLabel.setText(currentHint);
        wordLengthLabel.setText(model.getWordToGuess().length() + " letters");

        if (model.isWin()) {
            if(countdownTimer != null){
                countdownTimer.stop();
            }
            statusContainer.setVisible(true);
            statusContainer.setManaged(true);
            statusContainer.setStyle("-fx-background-color: rgba(46,204,113,0.15); -fx-border-color: #2ecc71; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 10px 25px;");
            statusLabel.setText("🎉 Victory! You correctly guessed the word!");
            statusLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold; -fx-font-size: 15px;");
            restartBtn.setText("Play Again");
        } else if (model.isLose()) {
            if (countdownTimer != null) {
                countdownTimer.stop();
            }
            statusContainer.setVisible(true);
            statusContainer.setManaged(true);
            statusContainer.setStyle("-fx-background-color: rgba(248,81,73,0.15); -fx-border-color: #f85149; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 10px 25px;");
            statusLabel.setText("💀 Game over — it was \"" + model.getWordToGuess() + "\"");
            statusLabel.setStyle("-fx-text-fill: #f85149; -fx-font-weight: bold; -fx-font-size: 15px;");
            restartBtn.setText("Play Again");
        }

        if (hangmanImageView != null) {
            String resourceName = "/pictures/" + currentWrongs + "-hangman.png";
            try {
                Image img = new Image(getClass().getResource(resourceName).toExternalForm());
                hangmanImageView.setImage(img);
            } catch (Exception ignored) {
                hangmanImageView.setImage(null);
            }
        }

        for (Node rowNode : keyboardGrid.getChildren()) {
            if (rowNode instanceof HBox rowContainer) {
                for (Node btnNode : rowContainer.getChildren()) {
                    if (btnNode instanceof Button b) {
                        String letter = b.getText().toLowerCase();
                        char letterChar = letter.charAt(0);
                        boolean alreadyGuessed = model.getGuessedLetter().contains(letterChar);
                        
                        b.setDisable(alreadyGuessed || model.isWin() || model.isLose());
                        
                        if (alreadyGuessed) {
                            b.setScaleX(1.0);
                            b.setScaleY(1.0);
                            if (model.getWordToGuess().toLowerCase().contains(letter)) {
                                b.setStyle("-fx-background-color: #161b22; -fx-text-fill: #ff7a00; -fx-border-color: #ff7a00; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-font-weight: bold; -fx-opacity: 1.0;");
                            } else {
                                b.setStyle("-fx-background-color: #0d1117; -fx-text-fill: #30363d; -fx-border-color: #21262d; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-opacity: 0.4;");
                            }
                        }
                    }
                }
            }
        }
    }

    private void renderWordBoxes() {
        wordDisplayBox.getChildren().clear();
        String targetWord = model.getWordToGuess();
        String hiddenWord = model.getHiddenWord();

        for (int i = 0; i < targetWord.length(); i++) {
            char displayChar = hiddenWord.charAt(i);
            Label letterLabel = new Label(displayChar == '_' ? "" : String.valueOf(displayChar));
            letterLabel.setFont(new Font("System Bold", 20));
            
            VBox charBox = new VBox(letterLabel);
            charBox.setAlignment(Pos.CENTER);
            charBox.setPrefSize(45, 45);
            
            if (displayChar != '_') {
                charBox.setStyle("-fx-border-color: #ff7a00; -fx-border-width: 2px; -fx-border-radius: 8px; -fx-background-color: #21262d; -fx-background-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(255,122,0,0.2), 5, 0, 0, 0);");
                letterLabel.setStyle("-fx-text-fill: #ff7a00;");
            } else {
                charBox.setStyle("-fx-border-color: #30363d; -fx-border-width: 1px; -fx-border-radius: 8px; -fx-background-color: #0d1117; -fx-background-radius: 8px;");
                letterLabel.setStyle("-fx-text-fill: #ffffff;");
            }
            
            VBox baseContainer = new VBox(charBox);
            baseContainer.setSpacing(4);
            baseContainer.setAlignment(Pos.CENTER);
            
            Label underline = new Label();
            underline.setPrefSize(35, 2);
            if (displayChar != '_') {
                underline.setStyle("-fx-background-color: #ff7a00; -fx-background-radius: 1px;");
            } else {
                underline.setStyle("-fx-background-color: #30363d; -fx-background-radius: 1px;");
            }
            baseContainer.getChildren().add(underline);

            wordDisplayBox.getChildren().add(baseContainer);
        }
    }

    private void generateKeyboard() {
        keyboardGrid.getChildren().clear();

        for (int rowIndex = 0; rowIndex < QWERTY_LAYOUT.length; rowIndex++) {
            HBox rowContainer = new HBox(10);
            rowContainer.setAlignment(Pos.CENTER);
            
            for (String key : QWERTY_LAYOUT[rowIndex]) {
                Button btn = new Button(key);
                btn.setPrefSize(42, 42);
                btn.setStyle("-fx-background-color: #161b22; -fx-text-fill: #ffffff; -fx-border-color: #30363d; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-font-weight: bold; -fx-cursor: hand;");
                
                applyButtonAnimations(btn, "#161b22", "#ffffff", "#ff7a00", "#0b0c10");

                btn.setOnAction(e -> handleLetter(btn.getText()));
                rowContainer.getChildren().add(btn);
            }
            keyboardGrid.add(rowContainer, 0, rowIndex);
        }
    }

    private void applyButtonAnimations(Button btn, String baseBg, String baseText, String hoverBg, String hoverText) {
        ScaleTransition hoverTransition = new ScaleTransition(Duration.millis(120), btn);
        btn.setOnMouseEntered(e -> {
            if (!btn.isDisable()) {
                btn.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: %s; -fx-border-color: %s; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-font-weight: bold; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(255,122,0,0.4), 8, 0, 0, 0);", hoverBg, hoverText, hoverBg));
                hoverTransition.setToX(1.08);
                hoverTransition.setToY(1.08);
                hoverTransition.playFromStart();
            }
        });

        btn.setOnMouseExited(e -> {
            if (!btn.isDisable()) {
                btn.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: %s; -fx-border-color: #30363d; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-font-weight: bold;", baseBg, baseText));
                hoverTransition.setToX(1.0);
                hoverTransition.setToY(1.0);
                hoverTransition.playFromStart();
            }
        });

        btn.setOnMousePressed(e -> {
            if (!btn.isDisable()) {
                ScaleTransition pressTransition = new ScaleTransition(Duration.millis(60), btn);
                pressTransition.setToX(0.90);
                pressTransition.setToY(0.90);
                pressTransition.play();
            }
        });

        btn.setOnMouseReleased(e -> {
            if (!btn.isDisable()) {
                ScaleTransition releaseTransition = new ScaleTransition(Duration.millis(80), btn);
                releaseTransition.setToX(1.08);
                releaseTransition.setToY(1.08);
                releaseTransition.play();
            }
        });
    }

    public void exit() {
        Stage stage = (Stage) restartBtn.getScene().getWindow();
        stage.close();
    } 
    
    @FXML
    public void restart() {
        ScaleTransition clickAnim = new ScaleTransition(Duration.millis(100), restartBtn);
        clickAnim.setToX(1.0);
        clickAnim.setToY(1.0);
        clickAnim.setOnFinished(e -> startNewGame());
        clickAnim.play();
    }
}