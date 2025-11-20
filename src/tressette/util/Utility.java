package tressette.util;

import tressette.model.Carta;
import tressette.model.Giocatore;
import javax.swing.ImageIcon;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Utility {
	
	
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

	public static List<Accusa> checkAccuse(List<Carta> mano) {
	    List<Accusa> accuse = new ArrayList<>();
	    
	    if (mano == null || mano.isEmpty()) return accuse;
	    
	    // Conta le carte speciali
	    int contatoreAssi = 0;
	    int contatoreDue = 0;
	    int contatoreTre = 0;
	    
	    for (Carta carta : mano) {
	        switch (carta.getValore()) {
	            case 1: contatoreAssi++; break;
	            case 2: contatoreDue++; break;
	            case 3: contatoreTre++; break;
	        }
	    }
	    
	    // Verifica le accuse possibili
	    if (contatoreTre == 4) {
	        accuse.add(new Accusa("Quattro Tre", 4));
	    } else if (contatoreTre == 3) {
	        accuse.add(new Accusa("Tre Tre", 3));
	    }
	    
	    if (contatoreDue == 4) {
	        accuse.add(new Accusa("Quattro Due", 4));
	    } else if (contatoreDue == 3) {
	        accuse.add(new Accusa("Tre Due", 3));
	    }
	    
	    if (contatoreAssi == 4) {
	        accuse.add(new Accusa("Quattro Assi", 4));
	    } else if (contatoreAssi == 3) {
	        accuse.add(new Accusa("Tre Assi", 3));
	    }
	    
	    return accuse;
	}
	
	public static String calcolaPunti(List<Carta> carte) {
	    int assi = 0;
	    int carteUnTerzo = 0;
	    
	    if (carte == null) return "0";
	    
	    for (Carta c : carte) {
	        switch(c.getValore()) {
	            case 1: 
	                assi++; 
	                break;
	            case 3: case 2: case 8: case 9: case 10: 
	                carteUnTerzo++;
	                break;
	        }
	    }
	    
	    int puntiInteri = assi;
	    
	    if (carteUnTerzo == 0) {
	        return String.valueOf(puntiInteri);
	    } else if (puntiInteri == 0) {
	        return formatFrazione(carteUnTerzo);
	    } else {
	        return puntiInteri + " + " + formatFrazione(carteUnTerzo);
	    }
	}

	public static String formatFrazione(int numerator) {
	    int interi = numerator / 3;
	    int resto = numerator % 3;

	    if (resto == 0) return String.valueOf(interi);
	    if (interi == 0) return resto + "/3";
	    return interi + " + " + resto + "/3";
	}

	// Metodo aggiuntivo utile per confrontare i punti
	public static int puntiTotaliInTerzi(List<Carta> carte) {
	    int assi = 0;
	    int carteUnTerzo = 0;
	    
	    if (carte == null) return 0;
	    
	    for (Carta c : carte) {
	        switch(c.getValore()) {
	            case 1: assi++; break;
	            case 3: case 2: case 8: case 9: case 10: carteUnTerzo++; break;
	        }
	    }
	    
	    return assi * 3 + carteUnTerzo; // Restituisce i punti in terzi (per confronti)
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

    public static List<Giocatore> piuSconfitte(List<Giocatore> giocatori, int minPerse) {
        return giocatori.stream()
                .filter(g -> g.getPartitePerse() >= minPerse)
                .collect(Collectors.toList());
    }

    public static List<Giocatore> ordinaPerVittorie(List<Giocatore> giocatori) {
        return giocatori.stream()
                .sorted((g1, g2) -> Integer.compare(g2.getPartiteVinte(), g1.getPartiteVinte()))
                .collect(Collectors.toList());
    }

    public static List<Giocatore> ordinaPerLivello(List<Giocatore> giocatori) {
        return giocatori.stream()
                .sorted((g1, g2) -> Integer.compare(g2.getLivello(), g1.getLivello()))
                .collect(Collectors.toList());
    }

    public static ImageIcon caricaImmagine(String path) {
        try {
            URL imageUrl = Utility.class.getClassLoader().getResource(path);
            if (imageUrl != null) {
                return new ImageIcon(imageUrl);
            } else {
                System.err.println("Immagine non trovata: " + path);
                return creaPlaceholder(80, 120);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return creaPlaceholder(80, 120);
        }
    }
    
    public static ImageIcon caricaCarta(String seme, int valore) {
        return caricaImmagine("carte/" + seme + valore + ".png");
    }
    
    public static ImageIcon caricaAvatar(String avatarName) {
        return caricaImmagine("avatar/" + avatarName + ".png");
    }
    
    private static ImageIcon creaPlaceholder(int width, int height) {
        return new ImageIcon(new java.awt.image.BufferedImage(
            width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB));
    }
    
    public static ImageIcon scalaImmagine(ImageIcon original, int width, int height) {
        if (original == null) return creaPlaceholder(width, height);
        
        java.awt.Image img = original.getImage();
        java.awt.Image scaledImage = img.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }
    
    public static java.awt.Dimension getDimensioniOriginali(ImageIcon icon) {
        if (icon == null) return new java.awt.Dimension(80, 120);
        return new java.awt.Dimension(icon.getIconWidth(), icon.getIconHeight());
    }
}