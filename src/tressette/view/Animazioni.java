package tressette.view;

import javax.swing.*;
import java.awt.*;

public class Animazioni {

    public static void mostraEffettoVittoria(JPanel panel) {
        new Thread(() -> {
            for(int i = 0; i < 10; i++) {
                panel.setBackground(new Color((int)(Math.random()*255), (int)(Math.random()*255), (int)(Math.random()*255)));
                panel.repaint();
                try { Thread.sleep(100); } catch (InterruptedException e) {}
            }
        }).start();
    }
}
