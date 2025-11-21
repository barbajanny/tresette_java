package tressette.util;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;

public class AudioManager {

    // Usa questo metodo statico
    public static void play(String resourcePath) {
        new Thread(() -> {
            try {
                // 1. Carichiamo il file come "risorsa" interna al progetto (indipendente dal disco)
                // Nota: il path deve iniziare con / (es: "/risorse/audio/click.wav")
                InputStream audioSrc = AudioManager.class.getResourceAsStream(resourcePath);
                
                if (audioSrc == null) {
                    System.err.println("File audio non trovato: " + resourcePath);
                    return;
                }

                // 2. BufferedInputStream è necessario per alcuni formati audio in Java per supportare mark/reset
                InputStream bufferedIn = new BufferedInputStream(audioSrc);
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(bufferedIn);
                
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
                
            } catch (Exception e) {
                System.err.println("Errore riproduzione audio: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
}