package tressette.util;

import tressette.model.Carta;
import tressette.model.Giocatore;

import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Utility {

    // -----------------------------------------------------------
    // CLASSE INTERNA PER LE ACCUSE
    // -----------------------------------------------------------
    public static class Accusa {
        public final String tipo;
        public final int punti;

        public Accusa(String tipo, int punti) {
            this.tipo = tipo;
            this.punti = punti;
        }

        @Override
        public String toString() {
            return tipo + " (" + punti + " punti)";
        }
    }

    // -----------------------------------------------------------
    // LOGICA DI GIOCO (ACCUSE E PUNTI)
    // -----------------------------------------------------------

    public static List<Accusa> checkAccuse(List<Carta> mano) {
        List<Accusa> accuse = new ArrayList<>();

        if (mano == null || mano.isEmpty()) return accuse;

        // Conta le carte speciali usando gli stream o ciclo foreach
        long contatoreAssi = mano.stream().filter(c -> c.getValore() == 1).count();
        long contatoreDue = mano.stream().filter(c -> c.getValore() == 2).count();
        long contatoreTre = mano.stream().filter(c -> c.getValore() == 3).count();

        // Verifica le accuse possibili (Napola / Buongioco)
        if (contatoreTre == 4) accuse.add(new Accusa("Quattro Tre", 4));
        else if (contatoreTre == 3) accuse.add(new Accusa("Tre Tre", 3));

        if (contatoreDue == 4) accuse.add(new Accusa("Quattro Due", 4));
        else if (contatoreDue == 3) accuse.add(new Accusa("Tre Due", 3));

        if (contatoreAssi == 4) accuse.add(new Accusa("Quattro Assi", 4));
        else if (contatoreAssi == 3) accuse.add(new Accusa("Tre Assi", 3));

        return accuse;
    }

    public static String calcolaPunti(List<Carta> carte) {
        // Calcoliamo il valore totale in "terzi di punto"
        // (es. un Asso vale 3 terzi, una figura vale 1 terzo)
        int totaleTerzi = puntiTotaliInTerzi(carte);
        
        int interi = totaleTerzi / 3;
        int resto = totaleTerzi % 3;
        
        // Se non c'è resto (es. 2 punti tondi), restituisce "2"
        if (resto == 0) {
            return String.valueOf(interi);
        } 
        // Se sono 0 punti e 1/3
        else if (interi == 0) {
            return resto + "/3";
        } 
        // Se sono punti misti (es. "1 + 1/3")
        else {
            return interi + " + " + resto + "/3";
        }
    }

    public static String formatFrazione(int numerator) {
        int interi = numerator / 3;
        int resto = numerator % 3;

        if (resto == 0) return String.valueOf(interi);
        if (interi == 0) return resto + "/3";
        return interi + " + " + resto + "/3";
    }

    // Metodo utile per confrontare i punti numericamente
    public static int puntiTotaliInTerzi(List<Carta> carte) {
        if (carte == null) return 0;
        
        int assi = 0;
        int carteUnTerzo = 0;

        for (Carta c : carte) {
            switch (c.getValore()) {
                case 1: assi++; break;
                case 3: case 2: case 8: case 9: case 10: carteUnTerzo++; break;
            }
        }
        return assi * 3 + carteUnTerzo; 
    }

    // -----------------------------------------------------------
    // STREAM E STATISTICHE (Richiesto dal Prof)
    // -----------------------------------------------------------
    public static ImageIcon caricaCarta(String seme, int valore) {
        // Costruiamo il nome del file (es. "bastoni1.png")
        // .toLowerCase() è importante perché i tuoi file sono minuscoli (es. bastoni1.png)
        String nomeFile = seme.toLowerCase() + valore + ".png";
        
        // Usiamo il metodo generico caricaImmagine con il percorso corretto
        return caricaImmagine("/carte/" + nomeFile);
    } 
    
    public static List<Giocatore> topVincitori(List<Giocatore> giocatori, int minVittorie) {
        return giocatori.stream()
                .filter(g -> g.getPartiteVinte() >= minVittorie)
                .collect(Collectors.toList());
    }

    public static List<Giocatore> topLivello(List<Giocatore> giocatori, int minLivello) {
        return giocatori.stream()
                .filter(g -> g.getLivello() >= minLivello)
                .collect(Collectors.toList());
    }

    public static List<Giocatore> ordinaPerVittorie(List<Giocatore> giocatori) {
        return giocatori.stream()
                .sorted((g1, g2) -> Integer.compare(g2.getPartiteVinte(), g1.getPartiteVinte()))
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------
    // GESTIONE IMMAGINI (Core Aggiornato)
    // -----------------------------------------------------------

    /**
     * Metodo universale per caricare immagini.
     * Gestisce i percorsi sia con '/' iniziale che senza.
     */
    public static ImageIcon caricaImmagine(String path) {
        if (path == null || path.isEmpty()) return creaPlaceholder(80, 120);

        // Assicuriamoci che il path inizi con / per cercare dalla root del classpath
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        try {
            // Usa Utility.class.getResource che è più robusto per le risorse statiche
            URL imageUrl = Utility.class.getResource(path);
            if (imageUrl != null) {
                return new ImageIcon(imageUrl);
            } else {
                System.err.println("Immagine non trovata: " + path);
                return creaPlaceholder(80, 120);
            }
        } catch (Exception e) {
            System.err.println("Eccezione caricamento img: " + e.getMessage());
            return creaPlaceholder(80, 120);
        }
    }

    /**
     * Ridimensiona un'icona (Fondamentale per gli Avatar nel menu)
     */
    public static ImageIcon scalaImmagine(ImageIcon original, int width, int height) {
        if (original == null) return creaPlaceholder(width, height);

        Image img = original.getImage();
        Image scaledImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }

    /**
     * Metodo helper per caricare avatar.
     * Nota: Ora accetta il PATH COMPLETO se glielo passi, oppure costruisce il path.
     */
    public static ImageIcon caricaAvatar(String pathOrName) {
        // Se contiene già "/", assumiamo sia un path completo (es: "/avatar/robot.png")
        if (pathOrName.contains("/")) {
            return caricaImmagine(pathOrName);
        }
        // Altrimenti proviamo a costruirlo (compatibilità)
        return caricaImmagine("/avatar/" + pathOrName);
    }

    // Crea un quadrato colorato se l'immagine manca (evita crash)
    private static ImageIcon creaPlaceholder(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2d = img.createGraphics();
        g2d.setColor(java.awt.Color.GRAY);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(java.awt.Color.RED);
        g2d.drawRect(0, 0, width-1, height-1);
        g2d.drawString("IMG?", 10, height/2);
        g2d.dispose();
        return new ImageIcon(img);
    }
}