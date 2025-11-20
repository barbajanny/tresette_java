package tressette.model;

import tressette.util.Utility;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import javax.swing.SwingUtilities;
public class Partita {

    private Giocatore[] giocatori;
    private int turno;
    private int primoDiMano;
    private PropertyChangeSupport support;
    private Mazzo mazzo;
    private List<Carta> tavolo;
    private Map<Giocatore, List<Carta>> prese;
    private boolean modalitaPescaggio;
    private List<Carta> mazzoDiPescaggio;
    private boolean primaMano;
    private Map<Giocatore, Integer> puntiAccuse;

    public Partita(Giocatore[] giocatori) {
        this.giocatori = giocatori;
        this.turno = 0;
        this.primoDiMano = 0;
        this.support = new PropertyChangeSupport(this);
        this.mazzo = new Mazzo();
        this.tavolo = new ArrayList<>();
        this.prese = new HashMap<>();
        this.modalitaPescaggio = false;
        this.mazzoDiPescaggio = new ArrayList<>();
        this.primaMano = true;
        this.puntiAccuse = new HashMap<>();
        
        for (Giocatore g : giocatori) {
            prese.put(g, new ArrayList<>());
            puntiAccuse.put(g, 0);
        }
    }

    public Giocatore[] getGiocatori() { return giocatori; }
    public int getTurno() { return turno; }
    public List<Carta> getTavolo() { return tavolo; }
    public Map<Giocatore, List<Carta>> getPrese() { return prese; }
    public boolean isModalitaPescaggio() { return modalitaPescaggio; }
    public int getPuntiAccuse(Giocatore g) { return puntiAccuse.get(g); }
    public Map<Giocatore, Integer> getPuntiAccuse() { return new HashMap<>(puntiAccuse); }

    public void setModalitaPescaggio(boolean modalitaPescaggio) {
        this.modalitaPescaggio = modalitaPescaggio;
        if (modalitaPescaggio) {
            mazzoDiPescaggio = new ArrayList<>();
        }
    }

    public void distribuisciCarte() {
        // Distribuisci 10 carte a testa in 1vs1, 13 in 3+ giocatori
        int cartePerGiocatore = giocatori.length == 2 ? 10 : 13;
        
        for (int i = 0; i < cartePerGiocatore; i++) {
            for (Giocatore g : giocatori) {
                if (!mazzo.isVuoto()) {
                    g.aggiungiCarta(mazzo.pescaCarta());
                }
            }
        }
        
        // Se in modalità pescaggio, salva le carte rimanenti
        if (modalitaPescaggio) {
            mazzoDiPescaggio.addAll(mazzo.getCarte());
            mazzo.getCarte().clear();
            Collections.shuffle(mazzoDiPescaggio);
        }
        
        this.turno = primoDiMano;
        support.firePropertyChange("turno", -1, turno);
    }

    public void prossimoTurno() {
        int old = turno;
        turno = (turno + 1) % giocatori.length;
        support.firePropertyChange("turno", old, turno);
    }

    public void giocaCarta(Giocatore g, Carta c) {
        // Se è la prima giocata della partita, verifica le accuse
        if (primaMano && tavolo.isEmpty()) {
            verificaAccuse();
        }
        
        // Controllo obbligo di seme
        if (!tavolo.isEmpty()) {
            String semeObbligatorio = tavolo.get(0).getSeme();
            if (!c.getSeme().equals(semeObbligatorio)) {
                boolean haSemeObbligatorio = false;
                for (Carta cartaMano : g.getMano()) {
                    if (cartaMano.getSeme().equals(semeObbligatorio)) {
                        haSemeObbligatorio = true;
                        break;
                    }
                }
                if (haSemeObbligatorio) {
                    throw new IllegalStateException("Il giocatore " + g.getNickname() + 
                                                   " deve rispondere al seme " + semeObbligatorio);
                }
            }
        }

        // Gioca la carta
        System.out.println("GIOCATA: " + g.getNickname() + " gioca: " + c + " (turno: " + turno + ")");
        tavolo.add(c);
        g.rimuoviCarta(c);
        support.firePropertyChange("giocata", null, c);

        // Se tutti hanno giocato
        if (tavolo.size() == giocatori.length) {
            System.out.println("MANO COMPLETATA - Ordine carte sul tavolo:");
            for (int i = 0; i < tavolo.size(); i++) {
                System.out.println("  Posizione " + i + ": " + tavolo.get(i) + 
                                 " - giocata da: " + giocatori[(primoDiMano + i) % giocatori.length].getNickname());
            }
            
            // Calcola il vincitore della mano
            Giocatore vincitore = calcolaVincitoreMano();
            System.out.println("Vincitore mano: " + vincitore.getNickname());

            // Assegna le carte al vincitore
            prese.get(vincitore).addAll(tavolo);

            // Se in modalità pescaggio, prepara il mazzo
            if (modalitaPescaggio) {
                mazzoDiPescaggio.addAll(tavolo);
                if (mazzoDiPescaggio.size() >= 2) {
                    pescaCarteDopoMano();
                }
            }

            // Aggiorna punti
            support.firePropertyChange("punti", null, prese);

            // Pulizia tavolo
            tavolo.clear();
            support.firePropertyChange("tavolo", null, tavolo);

            // Il vincitore diventa il primo di mano per il prossimo giro
            primoDiMano = Arrays.asList(giocatori).indexOf(vincitore);
            turno = primoDiMano;
            support.firePropertyChange("turno", null, turno);

            System.out.println("Punti aggiornati:");
            for (Giocatore gioc : giocatori) {
                System.out.println(gioc.getNickname() + ": " + calcolaPunti(gioc));
            }
        } else {
            // Passa al turno successivo
            prossimoTurno();
        }
    }
    
    

    private void verificaAccuse() {
        if (!primaMano) return;
        
        System.out.println("=== VERIFICA ACCUSE PRIMA MANO ===");
        
        for (Giocatore giocatore : giocatori) {
            List<Utility.Accusa> accuse = Utility.checkAccuse(giocatore.getMano());
            
            if (!accuse.isEmpty()) {
                for (Utility.Accusa accusa : accuse) {
                    System.out.println(giocatore.getNickname() + " accusa: " + accusa);
                    puntiAccuse.put(giocatore, puntiAccuse.get(giocatore) + accusa.punti);
                    
                    // Notifica la GUI
                    support.firePropertyChange("accusa", null, 
                        new AccusaEvent(giocatore, accusa));
                }
            }
        }
        
        primaMano = false;
        System.out.println("=== FINE VERIFICA ACCUSE ===");
    }

    private void pescaCarteDopoMano() {
        System.out.println("Pescaggio carte dopo mano...");
        
        // Distribuisci una carta a ogni giocatore
        for (Giocatore g : giocatori) {
            if (!mazzoDiPescaggio.isEmpty()) {
                Carta cartaPescata = mazzoDiPescaggio.remove(0);
                g.aggiungiCarta(cartaPescata);
                System.out.println(g.getNickname() + " pesca: " + cartaPescata);
                
                // Notifica la GUI per mostrare la carta pescata
                support.firePropertyChange("cartaPescata", null, 
                    new CartaPescataEvent(g, cartaPescata));
            }
        }
    }

    private Giocatore calcolaVincitoreMano() {
        if (tavolo.isEmpty()) return null;

        System.out.println("=== CALCOLO VINCITORE ===");
        System.out.println("Primo di mano: " + primoDiMano + " (" + giocatori[primoDiMano].getNickname() + ")");
        
        // La prima carta determina il seme guida
        Carta cartaGuida = tavolo.get(0);
        String semeGuida = cartaGuida.getSeme();
        Carta cartaVincente = cartaGuida;
        int indiceVincitore = 0;

        System.out.println("Seme guida: " + semeGuida);
        System.out.println("Carta guida: " + cartaGuida + " (forza: " + cartaGuida.getForza() + ")");

        // Cerca la carta più forte dello stesso seme
        for (int i = 1; i < tavolo.size(); i++) {
            Carta cartaCorrente = tavolo.get(i);
            
            System.out.println("Analizzo carta " + i + ": " + cartaCorrente + 
                              " (seme: " + cartaCorrente.getSeme() + 
                              ", forza: " + cartaCorrente.getForza() + ")");
            
            // Solo le carte dello stesso seme della guida possono vincere
            if (cartaCorrente.getSeme().equals(semeGuida)) {
                System.out.println("  Stesso seme! Confronto forze: " + cartaCorrente.getForza() + 
                                  " vs " + cartaVincente.getForza());
                
                if (cartaCorrente.getForza() > cartaVincente.getForza()) {
                    cartaVincente = cartaCorrente;
                    indiceVincitore = i;
                    System.out.println("  NUOVA CARTA VINCENTE: " + cartaVincente + " alla posizione " + i);
                }
            } else {
                System.out.println("  Seme diverso, salto");
            }
        }

        // Calcola chi ha giocato la carta vincente
        int indiceGiocatoreVincitore = (primoDiMano + indiceVincitore) % giocatori.length;
        System.out.println("Indice vincitore: " + indiceVincitore);
        System.out.println("Giocatore vincitore: " + giocatori[indiceGiocatoreVincitore].getNickname());
        System.out.println("=== FINE CALCOLO ===");
        
        return giocatori[indiceGiocatoreVincitore];
    }

    public String calcolaPunti(Giocatore g) {
        return Utility.calcolaPunti(prese.get(g));
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    // Classi evento
    public static class CartaPescataEvent {
        public final Giocatore giocatore;
        public final Carta carta;
        
        public CartaPescataEvent(Giocatore giocatore, Carta carta) {
            this.giocatore = giocatore;
            this.carta = carta;
        }
    }
    
 // In Partita.java - aggiungi questo getter
    public int getPrimoDiMano() {
        return primoDiMano;
    }

    public static class AccusaEvent {
        public final Giocatore giocatore;
        public final Utility.Accusa accusa;
        
        public AccusaEvent(Giocatore giocatore, Utility.Accusa accusa) {
            this.giocatore = giocatore;
            this.accusa = accusa;
        }
    }
}