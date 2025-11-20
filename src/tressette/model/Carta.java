package tressette.model;

import javax.swing.ImageIcon;
import tressette.util.Utility;

public class Carta {
    private String seme;
    private int valore;
    private int forza;
    private ImageIcon immagine;

    public Carta(String seme, int valore) {
        this.seme = seme;
        this.valore = valore;
        this.forza = calcolaForza(valore);
        this.immagine = Utility.caricaCarta(seme, valore);
    }

    private int calcolaForza(int valore) {
        switch (valore) {
            case 3: return 10;
            case 2: return 9;
            case 1: return 8;
            case 10: return 7;
            case 9: return 6;
            case 8: return 5;
            case 7: return 4;
            case 6: return 3;
            case 5: return 2;
            case 4: return 1;
            default: return 0;
        }
    }

    public String getSeme() { return seme; }
    public int getValore() { return valore; }
    public int getForza() { return forza; }
    public ImageIcon getImmagine() { return immagine; }
    
    @Override
    public String toString() {
        return seme + " " + valore;
    }
}