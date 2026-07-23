package fr.quentincillierre.hangman.controller;

import fr.quentincillierre.hangman.application.SoundManager;
import fr.quentincillierre.hangman.model.Difficulty;
import fr.quentincillierre.hangman.model.HangmanModel;
import fr.quentincillierre.hangman.model.WordRepository;
import fr.quentincillierre.hangman.model.WordRepository.HangmanQuestion;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
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
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;

public class GameController {

    @FXML
    private VBox rootLayout;

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

    @FXML
    private Label timerLabel;

    // --- VIDEO / GIF PLAYBACK ---
    private MediaView hangmanMediaView;
    private MediaPlayer victoryVideoPlayer;

    private HangmanModel model;
    private String currentHint = "";
    private Timeline countdownTimer;
    private Timeline criticalPulseAnimation;
    private int timeRemaining = 60;

    private final String[][] QWERTY_LAYOUT = {
        {"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"},
        {"A", "S", "D", "F", "G", "H", "J", "K", "L"},
        {"Z", "X", "C", "V", "B", "N", "M"}
    };

    private Difficulty difficulty = Difficulty.EASY;
    private String wordFile = "easy.txt";

    @FXML
    public void initialize() {
        applyButtonAnimations(restartBtn, "#ff7a00", "#0b0c10", "#e06b00", "#0b0c10");

        // Set up MediaView wrapper programmatically
        setupVideoContainerInCode();

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

    private void setupVideoContainerInCode() {
        if (hangmanImageView == null) return;

        // 1. Create MediaView
        hangmanMediaView = new MediaView();
        hangmanMediaView.setFitWidth(200);
        hangmanMediaView.setFitHeight(200);
        hangmanMediaView.setPreserveRatio(false);
        hangmanMediaView.setVisible(false);

        // 2. Wrap hangmanImageView inside StackPane across any Pane layout
        if (hangmanImageView.getParent() instanceof Pane parentPane) {
            int index = parentPane.getChildren().indexOf(hangmanImageView);

            StackPane imageHolder = new StackPane();
            imageHolder.setPrefSize(200, 200);

            parentPane.getChildren().remove(hangmanImageView);
            imageHolder.getChildren().addAll(hangmanImageView, hangmanMediaView);
            parentPane.getChildren().add(index, imageHolder);
        } else {
            System.err.println("⚠️ Could not wrap hangmanImageView inside parent container.");
        }

        // 3. Pre-load video file with updated resource path inside /pictures/
        try {
            URL videoUrl = getClass().getResource("/pictures/dancing-cat.mp4");

            if (videoUrl == null) {
                videoUrl = getClass().getResource("/dancing-cat.mp4");
            }
            if (videoUrl == null) {
                videoUrl = GameController.class.getClassLoader().getResource("pictures/dancing-cat.mp4");
            }

            if (videoUrl != null) {
                System.out.println("✅ Found video resource at: " + videoUrl.toExternalForm());
                Media media = new Media(videoUrl.toExternalForm());
                victoryVideoPlayer = new MediaPlayer(media);

                victoryVideoPlayer.setOnError(() -> {
                    System.err.println("❌ MediaPlayer Error: " + victoryVideoPlayer.getError().getMessage());
                });

                hangmanMediaView.setMediaPlayer(victoryVideoPlayer);

                victoryVideoPlayer.setMute(false);
                victoryVideoPlayer.setVolume(1.0);
                victoryVideoPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            } else {
                System.err.println("ℹ️ No MP4 video found in /pictures/ or root. (Fallback to GIF if available)");
            }
        } catch (Exception e) {
            System.err.println("❌ Exception during video initialization:");
            e.printStackTrace();
        }
    }

    private void playVictoryVideo() {
        // Priority 1: Check if animated GIF exists in pictures
        URL gifUrl = getClass().getResource("/pictures/dancing-cat.gif");
        if (gifUrl == null) {
            gifUrl = getClass().getResource("/dancing-cat.gif");
        }

        if (gifUrl != null) {
            if (hangmanMediaView != null) hangmanMediaView.setVisible(false);
            if (hangmanImageView != null) {
                hangmanImageView.setVisible(true);
                hangmanImageView.setImage(new Image(gifUrl.toExternalForm()));
            }
            return;
        }

        // Priority 2: Use MediaPlayer if MP4 is loaded successfully
        if (hangmanImageView != null) hangmanImageView.setVisible(false);
        if (hangmanMediaView != null) hangmanMediaView.setVisible(true);

        if (victoryVideoPlayer != null) {
            victoryVideoPlayer.seek(Duration.ZERO);
            victoryVideoPlayer.setMute(false);
            victoryVideoPlayer.play();
        }
    }

    private void resetVictoryVideo() {
        if (victoryVideoPlayer != null) {
            victoryVideoPlayer.stop();
        }
        if (hangmanMediaView != null) hangmanMediaView.setVisible(false);
        if (hangmanImageView != null) hangmanImageView.setVisible(true);
    }

    private void startNewGame() {
        resetVictoryVideo();

        restartBtn.setDisable(false);

        WordRepository wordRepository = new WordRepository(wordFile);
        HangmanQuestion question = wordRepository.getRandomQuestion();

        model = new HangmanModel(question.text());
        currentHint = question.hint();

        restartBtn.setText("New Word");
        statusContainer.setVisible(false);
        statusContainer.setManaged(false);

        setCriticalGlow(timerLabel, false);

        generateKeyboard();
        refreshUI();

        keyboardGrid.setDisable(false);
        startTimer();
    }

    private void startTimer() {
        if (countdownTimer != null) {
            countdownTimer.stop();
        }

        switch (difficulty) {
            case EASY -> timeRemaining = 50;
            case MEDIUM -> timeRemaining = 30;
            case HARD -> timeRemaining = 20;
        }

        timerLabel.setText(String.valueOf(timeRemaining));

        countdownTimer = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> {
                if (model.isWin() || model.isLose()) {
                    countdownTimer.stop();
                    return;
                }

                if (timeRemaining > 0) {
                    timeRemaining--;
                    timerLabel.setText(String.valueOf(timeRemaining));

                    if (timeRemaining <= 10) {
                        setCriticalGlow(timerLabel, true);
                        SoundManager.playClickSound(180, 40);
                    }
                }

                if (timeRemaining <= 0) {
                    countdownTimer.stop();
                    triggerGameOver(true);
                }
            })
        );

        countdownTimer.setCycleCount(Timeline.INDEFINITE);
        countdownTimer.play();
    }

    private void handleLetter(String s) {
        if (timeRemaining <= 0 || s == null || s.isBlank() || model.isWin() || model.isLose()) {
            return;
        }

        char letterChar = Character.toLowerCase(s.charAt(0));

        if (!model.getGuessedLetter().contains(letterChar)) {
            String targetWord = model.getWordToGuess().toLowerCase();

            if (targetWord.contains(String.valueOf(letterChar))) {
                SoundManager.playCorrectSound();
            } else {
                SoundManager.playClickSound(600, 25);
            }
        }

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

        if (attemptsLeft <= 2 && attemptsLeft > 0) {
            dangerProgressBar.setStyle("-fx-accent: #f85149; -fx-effect: dropshadow(three-pass-box, rgba(248,81,73,0.6), 8, 0, 0, 0);");
        } else {
            dangerProgressBar.setStyle("-fx-accent: #ff7a00;");
        }

        hintLabel.setText(currentHint);
        wordLengthLabel.setText(model.getWordToGuess().length() + " letters");

        // 1. ALWAYS update the image first (so 10-hangman.png displays properly on defeat)
        if (hangmanImageView != null && !model.isWin()) {
            String resourcePath = "/pictures/" + currentWrongs + "-hangman.png";
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

        // 2. THEN check for Win or Lose conditions
        if (model.isWin()) {
            if (countdownTimer != null) countdownTimer.stop();
            setCriticalGlow(timerLabel, false);

            playVictoryVideo();

            statusContainer.setVisible(true);
            statusContainer.setManaged(true);
            statusContainer.setStyle("-fx-background-color: rgba(46,204,113,0.15); -fx-border-color: #2ecc71; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 10px 25px;");
            statusLabel.setText("🎉 Victory! You correctly guessed the word!");
            statusLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold; -fx-font-size: 15px;");
            restartBtn.setText("Play Again");
        } else if (model.isLose()) {
            if (countdownTimer != null) countdownTimer.stop();
            triggerGameOver(false);
        }

        // 3. Update keyboard button state
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

    private void triggerGameOver(boolean isTimeOut) {
        SoundManager.playFailSound();

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

        if (isTimeOut) {
            statusLabel.setText("⏰ Time's Up! The word was \"" + model.getWordToGuess() + "\"");
        } else {
            statusLabel.setText("💀 Game over — it was \"" + model.getWordToGuess() + "\"");
        }

        statusLabel.setStyle("-fx-text-fill: #f85149; -fx-font-weight: bold; -fx-font-size: 15px;");
        restartBtn.setText("Play Again");
        keyboardGrid.setDisable(true);
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

    private void setCriticalGlow(Node node, boolean enable) {
        if (enable) {
            if (criticalPulseAnimation != null && criticalPulseAnimation.getStatus() == Timeline.Status.RUNNING) {
                return;
            }

            criticalPulseAnimation = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(node.scaleXProperty(), 1.0), new KeyValue(node.scaleYProperty(), 1.0)),
                new KeyFrame(Duration.millis(500), new KeyValue(node.scaleXProperty(), 1.2), new KeyValue(node.scaleYProperty(), 1.2))
            );

            criticalPulseAnimation.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                if (rootLayout == null || criticalPulseAnimation == null) return;
                double progress = newValue.toMillis() / 500.0;
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

            if (rootLayout != null) {
                rootLayout.setStyle(
                    "-fx-background-color: #0b0c10;" + 
                    "-fx-border-color: transparent;" +
                    "-fx-effect: none;"
                );
            }
        }
    }

    public void exit() {
        Stage stage = (Stage) restartBtn.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void restart() {
        resetVictoryVideo();
        SoundManager.stopFailSound();
        SoundManager.playNewWordSound();

        ScaleTransition clickAnim = new ScaleTransition(Duration.millis(100), restartBtn);
        clickAnim.setToX(1.0);
        clickAnim.setToY(1.0);
        clickAnim.setOnFinished(e -> startNewGame());
        clickAnim.play();
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;

        switch (difficulty) {
            case EASY -> wordFile = "easy.txt";
            case MEDIUM -> wordFile = "medium.txt";
            case HARD -> wordFile = "hard.txt";
        }

        startNewGame();
    }
}