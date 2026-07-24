package fr.quentincillierre.hangman.controller;

import java.io.IOException;
import java.net.URL;
import java.util.Random;

import fr.quentincillierre.hangman.application.SoundManager;
import fr.quentincillierre.hangman.model.HangmanModel;
import fr.quentincillierre.hangman.model.WordRepository;
import fr.quentincillierre.hangman.model.WordRepository.HangmanQuestion;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Challenge Mode -- Hangman King.
 *
 * The player faces the Hangman King, who holds a prisoner captive and issues
 * one word at a time. Each word must be solved within a fixed 15-second
 * window or within 13 total letter guesses.
 */
public class BossBattleController {

    private static final int WORD_TIME_SECONDS = 15;
    private static final int TOTAL_WORDS = 10;
    private static final int MAX_STAGE = 10;
    private static final int MAX_GUESSES_PER_WORD = 13; // Maximum allowed typed letters per word

    @FXML
    private VBox rootLayout;

    @FXML
    private HBox wordDisplayBox;

    @FXML
    private Label wordLengthLabel;

    @FXML
    private Label narrativeLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private HBox statusContainer;

    @FXML
    private ImageView hangmanImageView;

    @FXML
    private ProgressBar dangerProgressBar;

    @FXML
    private Label stageLabel;

    @FXML
    private Label progressLabel;

    @FXML
    private Label timerLabel;

    @FXML
    private GridPane keyboardGrid;

    @FXML
    private Button mainMenuBtn;

    private final Random random = new Random();

    private HangmanModel model;
    private Timeline countdownTimer;
    private Timeline criticalPulseAnimation;
    private int timeRemaining = WORD_TIME_SECONDS;

    private int wordsCompleted = 0;
    private int hangmanStage = 0;
    private boolean runOver = false;

    private final String[][] QWERTY_LAYOUT = {
        {"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"},
        {"A", "S", "D", "F", "G", "H", "J", "K", "L"},
        {"Z", "X", "C", "V", "B", "N", "M"}
    };

   private static final String[] NEW_WORD_TAUNTS = {
        "\"Another word, another failure.\" -- The King",
        "The King sneers and gives a new word.",
        "\"Let's see you squirm.\" Fresh word!",
        "The prisoner watches nervously.",
        "\"15s or 13 guesses. That's it.\""
    };

    private static final String[] TIMEOUT_TAUNTS = {
        "\"Too slow!\" The noose tightens.",
        "Failed! The rope creaks dangerously.",
        "\"Pathetic.\" The King savors your failure.",
        "Out of tries! The drawing grows."
    };

    private static final String[] SOLVED_LINES = {
        "Solved! The prisoner breathes easier.",
        "The King grits his teeth. One step closer!",
        "Correct! The crowd murmurs in surprise.",
        "Cracked just in time!"
    };

    @FXML
    public void initialize() {
        if (mainMenuBtn != null) {
            mainMenuBtn.setFocusTraversable(false);
            applyButtonAnimations(mainMenuBtn, "transparent", "#c9d1d9", "#1c2530", "#ff7a00");
        }

        // Use Event Filter during the capture phase so focused controls don't steal key input
        Platform.runLater(() -> {
            if (keyboardGrid.getScene() != null) {
                keyboardGrid.getScene().addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    KeyCode code = e.getCode();
                    if (code != null && code.isLetterKey()) {
                        handleLetter(code.toString().toUpperCase());
                        e.consume(); // Prevents UI controls from swallowing key presses
                    }
                });
            }
        });

        startChallenge();
    }

    // ---------------------------------------------------------------------
    // RUN FLOW
    // ---------------------------------------------------------------------

    private void startChallenge() {
        wordsCompleted = 0;
        hangmanStage = 0;
        runOver = false;

        if (mainMenuBtn != null) {
            mainMenuBtn.setVisible(false);
            mainMenuBtn.setManaged(false);
        }

        updateProgressUI();
        updateStageUI();
        loadNextWord();
    }

    private void loadNextWord() {
        WordRepository wordRepository = new WordRepository("boss.txt");
        HangmanQuestion question = wordRepository.getRandomQuestion();

        model = new HangmanModel(question.text());

        // Display taunt with the hint highlighted in bright orange (#ff7a00)
        String taunt = NEW_WORD_TAUNTS[random.nextInt(NEW_WORD_TAUNTS.length)];
        String hintText = (question.hint() != null && !question.hint().isBlank()) 
                ? question.hint() 
                : "No hint available.";

        narrativeLabel.setText(taunt + "\n💡 Hint: " + hintText);
        narrativeLabel.setStyle("-fx-text-fill: #c9d1d9; -fx-font-size: 14px;"); // Base text style

        statusContainer.setVisible(false);
        statusContainer.setManaged(false);

        setCriticalGlow(timerLabel, false);
        keyboardGrid.setDisable(false);

        generateKeyboard();
        refreshUI();
        startWordTimer();
    }

    private void startWordTimer() {
        if (countdownTimer != null) {
            countdownTimer.stop();
        }

        timeRemaining = WORD_TIME_SECONDS;
        timerLabel.setText(String.valueOf(timeRemaining));

        countdownTimer = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> {
                if (runOver || model.isWin()) {
                    countdownTimer.stop();
                    return;
                }

                if (timeRemaining > 0) {
                    timeRemaining--;
                    timerLabel.setText(String.valueOf(timeRemaining));

                    int pitch = 200 + (WORD_TIME_SECONDS - timeRemaining) * 45;
                    SoundManager.playClickSound(pitch, 35);

                    if (timeRemaining <= 5) {
                        setCriticalGlow(timerLabel, true);
                    }
                }

                if (timeRemaining <= 0) {
                    countdownTimer.stop();
                    handleTimeout();
                }
            })
        );

        countdownTimer.setCycleCount(Timeline.INDEFINITE);
        countdownTimer.play();
    }

    private void handleLetter(String s) {
        if (runOver || timeRemaining <= 0 || s == null || s.isBlank() || model.isWin()) {
            return;
        }

        char letterChar = Character.toLowerCase(s.charAt(0));

        // Process guess if not previously guessed
        if (!model.getGuessedLetter().contains(letterChar)) {
            String targetWord = model.getWordToGuess().toLowerCase();
            if (targetWord.contains(String.valueOf(letterChar))) {
                SoundManager.playCorrectSound();
            } else {
                SoundManager.playClickSound(600, 25);
            }

            model.tryLetter(s.charAt(0));
            refreshUI();

            // Check if user reached 13 guesses without solving the word
            if (!model.isWin() && model.getGuessedLetter().size() >= MAX_GUESSES_PER_WORD) {
                if (countdownTimer != null) {
                    countdownTimer.stop();
                }
                handleTimeout();
            }
        }
    }

    /** Word solved before the clock ran out or guess limit reached. */
    private void handleWordSolved() {
        if (countdownTimer != null) countdownTimer.stop();
        setCriticalGlow(timerLabel, false);

        wordsCompleted++;
        updateProgressUI();

        keyboardGrid.setDisable(true);

        statusContainer.setVisible(true);
        statusContainer.setManaged(true);
        statusContainer.setStyle("-fx-background-color: rgba(46,204,113,0.15); -fx-border-color: #2ecc71; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 10px 25px;");
        statusLabel.setText(SOLVED_LINES[random.nextInt(SOLVED_LINES.length)]);
        statusLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold; -fx-font-size: 15px;");

        PauseTransition pause = new PauseTransition(Duration.seconds(1.3));
        pause.setOnFinished(e -> {
            if (wordsCompleted >= TOTAL_WORDS) {
                triggerVictory();
            } else {
                loadNextWord();
            }
        });
        pause.play();
    }

    /** Time ran out OR player exceeded 13 letter attempts. */
    private void handleTimeout() {
        setCriticalGlow(timerLabel, false);

        hangmanStage++;
        updateStageUI();

        keyboardGrid.setDisable(true);

        statusContainer.setVisible(true);
        statusContainer.setManaged(true);
        statusContainer.setStyle("-fx-background-color: rgba(248,81,73,0.15); -fx-border-color: #f85149; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 10px 25px;");
        statusLabel.setText(TIMEOUT_TAUNTS[random.nextInt(TIMEOUT_TAUNTS.length)] + "  (\"" + model.getWordToGuess() + "\")");
        statusLabel.setStyle("-fx-text-fill: #f85149; -fx-font-weight: bold; -fx-font-size: 15px;");

        SoundManager.playClickSound(120, 60);

        PauseTransition pause = new PauseTransition(Duration.seconds(1.6));
        pause.setOnFinished(e -> {
            if (hangmanStage >= MAX_STAGE) {
                triggerDefeat();
            } else {
                loadNextWord();
            }
        });
        pause.play();
    }

    private void triggerVictory() {
        runOver = true;
        if (countdownTimer != null) countdownTimer.stop();
        if (criticalPulseAnimation != null) criticalPulseAnimation.stop();
        setCriticalGlow(timerLabel, false);

        SoundManager.playCorrectSound();

        statusContainer.setVisible(true);
        statusContainer.setManaged(true);
        statusContainer.setStyle("-fx-background-color: rgba(255,209,102,0.15); -fx-border-color: #ffd166; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 10px 25px;");
        statusLabel.setText("👑 You saved the prisoner! The Hangman King retreats in defeat!");
        statusLabel.setStyle("-fx-text-fill: #ffd166; -fx-font-weight: bold; -fx-font-size: 16px;");

        narrativeLabel.setText("The King's reign of terror ends here.");

        keyboardGrid.setDisable(true);

        if (mainMenuBtn != null) {
            mainMenuBtn.setVisible(true);
            mainMenuBtn.setManaged(true);
        }
    }

    private void triggerDefeat() {
        runOver = true;
        if (countdownTimer != null) countdownTimer.stop();
        if (criticalPulseAnimation != null) criticalPulseAnimation.stop();
        setCriticalGlow(timerLabel, false);

        SoundManager.playFailSound();
        loadHangmanImage(MAX_STAGE);

        if (rootLayout != null) {
            rootLayout.setStyle(
                "-fx-background-color: #2b0909;" +
                "-fx-border-color: #f85149;" +
                "-fx-border-width: 3px;" +
                "-fx-effect: innershadow(three-pass-box, rgba(248,81,73,0.8), 30, 0, 0, 0);"
            );
        }

        statusContainer.setVisible(true);
        statusContainer.setManaged(true);
        statusContainer.setStyle("-fx-background-color: rgba(248,81,73,0.15); -fx-border-color: #f85149; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 10px 25px;");
        statusLabel.setText("💀 The prisoner's fate is sealed. The Hangman King reigns victorious.");
        statusLabel.setStyle("-fx-text-fill: #f85149; -fx-font-weight: bold; -fx-font-size: 16px;");

        narrativeLabel.setText("\"As it always ends.\" -- the Hangman King");

        keyboardGrid.setDisable(true);

        if (mainMenuBtn != null) {
            mainMenuBtn.setVisible(true);
            mainMenuBtn.setManaged(true);
        }
    }

    // ---------------------------------------------------------------------
    // UI HELPERS
    // ---------------------------------------------------------------------

    private void updateProgressUI() {
        if (progressLabel != null) {
            progressLabel.setText(Math.min(wordsCompleted, TOTAL_WORDS) + " / " + TOTAL_WORDS);
        }
    }

    private void updateStageUI() {
        if (dangerProgressBar != null) {
            dangerProgressBar.setProgress((double) hangmanStage / MAX_STAGE);
            dangerProgressBar.setStyle(hangmanStage >= MAX_STAGE - 2
                    ? "-fx-accent: #f85149; -fx-effect: dropshadow(three-pass-box, rgba(248,81,73,0.6), 8, 0, 0, 0);"
                    : "-fx-accent: #ff7a00;");
        }
        if (stageLabel != null) {
            stageLabel.setText(hangmanStage + " / " + MAX_STAGE);
        }
        loadHangmanImage(hangmanStage);
    }

    private void loadHangmanImage(int frame) {
        if (hangmanImageView == null) return;

        String resourcePath = "/pictures/" + frame + "-hangman.png";
        try {
            URL imgUrl = getClass().getResource(resourcePath);
            if (imgUrl != null) {
                hangmanImageView.setImage(new Image(imgUrl.toExternalForm()));
            } else {
                System.err.println("Could not find image: " + resourcePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshUI() {
        renderWordBoxes();

        int remainingGuesses = Math.max(0, MAX_GUESSES_PER_WORD - model.getGuessedLetter().size());
        wordLengthLabel.setText(model.getWordToGuess().length() + " letters | Guesses left: " + remainingGuesses);

        if (model.isWin()) {
            handleWordSolved();
        }

        for (Node rowNode : keyboardGrid.getChildren()) {
            if (rowNode instanceof HBox rowContainer) {
                for (Node btnNode : rowContainer.getChildren()) {
                    if (btnNode instanceof Button b) {
                        String letter = b.getText().toLowerCase();
                        char letterChar = letter.charAt(0);
                        boolean alreadyGuessed = model.getGuessedLetter().contains(letterChar);

                        b.setDisable(alreadyGuessed || model.isWin());

                        if (alreadyGuessed) {
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
                btn.setFocusTraversable(false);
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

    private void setCriticalGlow(Node node, boolean enable) {
        if (enable) {
            if (criticalPulseAnimation != null && criticalPulseAnimation.getStatus() == Timeline.Status.RUNNING) {
                return;
            }

            criticalPulseAnimation = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(node.scaleXProperty(), 1.0), new KeyValue(node.scaleYProperty(), 1.0)),
                new KeyFrame(Duration.millis(400), new KeyValue(node.scaleXProperty(), 1.25), new KeyValue(node.scaleYProperty(), 1.25))
            );

            criticalPulseAnimation.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                if (rootLayout == null || criticalPulseAnimation == null) return;
                double progress = newValue.toMillis() / 400.0;
                rootLayout.setStyle(
                    String.format(
                        "-fx-background-color: derive(#2b0909, %f%%);" +
                        "-fx-border-color: #f85149;" +
                        "-fx-border-width: 3px;" +
                        "-fx-effect: innershadow(three-pass-box, rgba(248,81,73,%.2f), 35, 0, 0, 0);",
                        (progress * 20.0),
                        (0.4 + (progress * 0.5))
                    )
                );
            });

            criticalPulseAnimation.setCycleCount(Timeline.INDEFINITE);
            criticalPulseAnimation.setAutoReverse(true);
            criticalPulseAnimation.play();

            node.setStyle("-fx-text-fill: #f85149; -fx-font-weight: bold; -fx-font-size: 15px; -fx-effect: dropshadow(three-pass-box, rgba(248,81,73,0.9), 12, 0, 0, 0);");
        } else {
            if (criticalPulseAnimation != null) {
                criticalPulseAnimation.stop();
            }
            node.setScaleX(1.0);
            node.setScaleY(1.0);
            node.setStyle("-fx-text-fill: #ff4a4a; -fx-font-weight: bold; -fx-font-size: 12px;");

            if (rootLayout != null && !runOver) {
                rootLayout.setStyle(
                    "-fx-background-color: #0b0c10;" +
                    "-fx-border-color: transparent;" +
                    "-fx-effect: none;"
                );
            }
        }
    }

    // ---------------------------------------------------------------------
    // NAVIGATION
    // ---------------------------------------------------------------------

    @FXML
    private void backToMenu() {
        SoundManager.stopFailSound();

        if (countdownTimer != null) countdownTimer.stop();
        if (criticalPulseAnimation != null) criticalPulseAnimation.stop();

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/fr/quentincillierre/hangman/application/difficulty-view.fxml"));

            Parent root = loader.load();

            Stage stage = (Stage) mainMenuBtn.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);

            stage.setFullScreenExitHint("");
            stage.setFullScreen(true);

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}