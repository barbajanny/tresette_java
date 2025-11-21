package tressette.model;

import tressette.util.Utility;
import javax.swing.ImageIcon;
import java.util.ArrayList;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;

public class Giocatore {

    private String nickname;
    private String avatar;
    private ArrayList<Carta> mano;
    private ImageIcon avatarImage;
    protected int partiteGiocate;
    protected int partiteVinte;
    protected int partitePerse;
    protected int livello;
    private PropertyChangeSupport support;

    public Giocatore(String nickname, String avatar) {
        this.nickname = nickname;
        this.avatar = avatar;
        this.mano = new ArrayList<>();
        this.partiteGiocate = 0;
        this.partiteVinte = 0;
        this.partitePerse = 0;
        this.livello = 1;
        this.support = new PropertyChangeSupport(this);
        this.avatarImage = Utility.caricaAvatar(avatar);
    }

    public Giocatore() {
        this("Giocatore", "defaultAvatar");
    }

    public ImageIcon getAvatarImage() {
        return avatarImage;
    }

    public ArrayList<Carta> getMano() {
        if (mano == null) {
            mano = new ArrayList<>(); // Inizializza se null
        }
        return new ArrayList<>(mano); // Restituisce sempre una copia
    }

    public void aggiungiCarta(Carta c) {
        mano.add(c);
        support.firePropertyChange("mano", null, mano);
    }

    public void rimuoviCarta(Carta c) {
        mano.remove(c);
        support.firePropertyChange("mano", null, mano);
    }

    public void svuotaMano() {
        mano.clear();
        support.firePropertyChange("mano", null, mano);
    }

    public int getPartiteGiocate() { return partiteGiocate; }
    public int getPartiteVinte() { return partiteVinte; }
    public int getPartitePerse() { return partitePerse; }
    public int getLivello() { return livello; }
    public String getNickname() { return nickname; }
    public String getAvatar() { return avatar; }

    public void incrementaPartiteGiocate() { partiteGiocate++; }
    public void incrementaPartiteVinte() { partiteVinte++; }
    public void incrementaPartitePerse() { partitePerse++; }
    public void setLivello(int livello) { 
        int old = this.livello;
        this.livello = livello; 
        support.firePropertyChange("livello", old, livello);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    @Override
    public String toString() {
        return nickname + " (Lv " + livello + ")";
    }
    
    public void aggiornaStatistiche(boolean vittoria) {
        this.partiteGiocate++;
        
        if (vittoria) {
            this.partiteVinte++;
            // LOGICA LIVELLO: Ogni 2 vittorie si sale di livello
            // Esempio: 2 vittorie = liv 2, 4 vittorie = liv 3, ecc.
            int nuovoLivello = 1 + (partiteVinte / 2);
            
            if (nuovoLivello > this.livello) {
                setLivello(nuovoLivello);
            }
        } else {
            this.partitePerse++;
        }
        
        // Notifica la GUI che le stats sono cambiate
        support.firePropertyChange("stats", null, this);
    }

    // Assicurati di avere questi setter per quando carichi dal file
    public void setPartiteGiocate(int giocate) { this.partiteGiocate = giocate; }
    public void setPartiteVinte(int vinte) { this.partiteVinte = vinte; }
    public void setPartitePerse(int perse) { this.partitePerse = perse; }
    public void setAvatar(String avatarPath) { 
        this.avatar = avatarPath; 
        this.avatarImage = Utility.caricaAvatar(avatarPath);
    }
}
