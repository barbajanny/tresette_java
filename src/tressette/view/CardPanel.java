package tressette.view;

import tressette.model.Carta;
import tressette.util.Utility;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CardPanel extends JPanel {
    private List<Carta> mano;
    private final int CARD_WIDTH = 80;  // Larghezza fissa
    private final int CARD_HEIGHT = 120; // Altezza fissa

    public CardPanel(List<Carta> mano) {
        this.mano = mano;
        setPreferredSize(new Dimension(800, 150));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (mano == null || mano.isEmpty()) return;

        int spacing = 20;
        int startX = 50;
        int y = 15;

        for (int i = 0; i < mano.size(); i++) {
            Carta carta = mano.get(i);
            ImageIcon image = carta.getImmagine();
            
            int x = startX + i * (CARD_WIDTH + spacing);
            
            if (image != null) {
                // Scala l'immagine alle dimensioni fisse
                Image scaledImage = image.getImage().getScaledInstance(
                    CARD_WIDTH, CARD_HEIGHT, Image.SCALE_SMOOTH);
                g.drawImage(scaledImage, x, y, this);
            }
        }
    }

    public void setMano(List<Carta> mano) {
        this.mano = mano;
        repaint();
    }
}