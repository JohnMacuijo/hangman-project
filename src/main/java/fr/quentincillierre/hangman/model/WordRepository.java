package fr.quentincillierre.hangman.model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class WordRepository {

    public record HangmanQuestion(String text, String hint) {}

    private final List<HangmanQuestion> questions = new ArrayList<>();
    private final Random random = new Random();

    public WordRepository() {
        loadQuestionsFromFile();
    }

    private void loadQuestionsFromFile() {
        InputStream is = getClass().getResourceAsStream("/words.txt");
        
        if (is == null) {
            System.err.println("Error: words.txt not found in resources folder!");
            return;
        }

        try (Scanner scanner = new Scanner(new BufferedReader(new InputStreamReader(is)))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                
                if (line.isEmpty() || !line.contains(":")) {
                    continue;
                }

                String[] parts = line.split(":", 2);
                String wordText = parts[0].toUpperCase().trim();
                String hintText = parts[1].trim();

                questions.add(new HangmanQuestion(wordText, hintText));
            }
        } catch (Exception e) {
            System.err.println("Error reading words.txt: " + e.getMessage());
        }
    }

    public HangmanQuestion getRandomQuestion() {
        if (questions.isEmpty()) {
            return new HangmanQuestion("COMPUTER", "A machine that processes data.");
        }
        return questions.get(random.nextInt(questions.size()));
    }
}