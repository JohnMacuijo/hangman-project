package fr.quentincillierre.hangman.application;

import javafx.scene.media.AudioClip;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.net.URL;

public class SoundManager {

    public static void playCorrectSound() {
    new Thread(() -> {
        try {
            // First note (E5 - 659 Hz for 50ms)
            playToneBuffer(659, 50, 0.4);
            Thread.sleep(40);
            // Second higher note (A5 - 880 Hz for 80ms)
            playToneBuffer(880, 80, 0.5);
        } catch (Exception ignored) {}
    }).start();
}
    private static AudioClip failSound;

    static {
        try {
            // Load fail.mp3 from root of resources
            URL resource = SoundManager.class.getResource("/fail.mp3");
            if (resource != null) {
                failSound = new AudioClip(resource.toExternalForm());
            } else {
                System.err.println("fail.mp3 not found in resources root!");
            }
        } catch (Exception e) {
            System.err.println("Could not load fail sound: " + e.getMessage());
        }
    }

    public static void playClickSound(int frequencyHz, int durationMs) {
        playSoundTone(frequencyHz, durationMs, 0.3);
    }

    public static void playNewWordSound() {
        new Thread(() -> {
            try {
                playToneBuffer(523, 40, 0.4);
                Thread.sleep(30);
                playToneBuffer(784, 60, 0.5);
            } catch (Exception ignored) {}
        }).start();
    }

    public static void playFailSound() {
        if (failSound != null) {
            failSound.stop();
            failSound.setCycleCount(AudioClip.INDEFINITE); // Loop until explicitly stopped
            failSound.play();
        }
    }

    public static void stopFailSound() {
        if (failSound != null) {
            failSound.stop(); // Cuts off the music completely
        }
    }

    private static void playSoundTone(int frequencyHz, int durationMs, double volume) {
        new Thread(() -> playToneBuffer(frequencyHz, durationMs, volume)).start();
    }

    private static void playToneBuffer(int frequencyHz, int durationMs, double volume) {
        try {
            float sampleRate = 44100f;
            int samples = (int) (sampleRate * (durationMs / 1000.0));
            byte[] buffer = new byte[samples];

            for (int i = 0; i < samples; i++) {
                double angle = i / (sampleRate / frequencyHz) * 2.0 * Math.PI;
                double decay = 1.0 - ((double) i / samples); 
                buffer[i] = (byte) (Math.sin(angle) * 127.0 * decay * volume);
            }

            AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, false);
            SourceDataLine line = AudioSystem.getSourceDataLine(format);
            line.open(format);
            line.start();
            line.write(buffer, 0, buffer.length);
            line.drain();
            line.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}