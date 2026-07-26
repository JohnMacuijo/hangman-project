package fr.quentincillierre.hangman.model;

import java.util.HashSet;
import java.util.Set;

public class HangmanModel {

    private final String wordToGuess;
    private final int maxWrongs = 10; 

    private int currentWrongs;
    private final Set<Character> guessedLetter;

    public HangmanModel(String wordToGuess) {
        this.wordToGuess = wordToGuess;
        this.currentWrongs = 0;
        this.guessedLetter = new HashSet<>();
    }

    public String getWordToGuess() {
        return this.wordToGuess;
    }

    public int getCurrentWrongs() {
        return this.currentWrongs;
    }

    public Set<Character> getGuessedLetter() {
        return this.guessedLetter;
    }

    public void tryLetter(Character letter) {
        if (letter == null || isWin() || isLose()) {
            return;
        }

        char lowerLetter = Character.toLowerCase(letter);

        if (guessedLetter.contains(lowerLetter)) {
            return;
        }

        guessedLetter.add(lowerLetter);

        if (!wordToGuess.toLowerCase().contains(String.valueOf(lowerLetter))) {
            currentWrongs++;
        }
    }

    public String getHiddenWord() {
        StringBuilder hidden = new StringBuilder();
        for (int i = 0; i < wordToGuess.length(); i++) {
            char originalChar = wordToGuess.charAt(i);
            if (guessedLetter.contains(Character.toLowerCase(originalChar))) {
                hidden.append(originalChar);
            } else {
                hidden.append('_');
            }
        }
        return hidden.toString();
    }

    public boolean isWin() {
        if (isLose()) {
            return false;
        }
        return !getHiddenWord().contains("_");
    }

    public boolean isLose() {
        return this.currentWrongs >= this.maxWrongs;
    }
}