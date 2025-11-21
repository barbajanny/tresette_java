package tressette.model;

import tressette.util.Utility;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

public class Partita {

    private Giocatore[] giocatori;
    private int turno;
    private int primoDiMano; // Indice di chi ha iniziato la mano corrente
    private PropertyChangeSupport support;
    private Mazzo mazzo;
    private List<Carta> tavolo;
    private Map<Giocatore, List<Carta>> prese;
    
    // Variabili per 1vs1 (Pizzico)
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

    // --- GETTERS ---
    public Giocatore[] getGiocatori() { return giocatori; }
    public int getTurno() { return turno; }
    public int getPrimoDiMano() { return primoDiMano; } // Fondamentale per la GUI
    public List<Carta> getTavolo() { return tavolo; }
    public Map<Giocatore, List<Carta>> getPrese() { return prese; }
    public boolean isModalitaPescaggio() { return modalitaPescaggio; }
    public int getPuntiAccuse(Giocatore g) { return puntiAccuse.get(g); }

    public void setModalitaPescaggio(boolean modalitaPescaggio) {
        this.modalitaPescaggio = modalitaPescaggio;
        if (modalitaPescaggio) {
            mazzoDiPescaggio = new ArrayList<>();
        }
    }

    public void distribuisciCarte() {
        // 1vs1 = 10 carte, Altrimenti (4 o 3) = carte uguali per tutti
        int cartePerGiocatore = giocatori.length == 2 ? 10 : (40 / giocatori.length);
        
        mazzo.mescola();
        
        for (int i = 0; i < cartePerGiocatore; i++) {
            for (Giocatore g : giocatori) {
                if (!mazzo.isVuoto()) {
                    g.aggiungiCarta(mazzo.pescaCarta());
                }
            }
        }
        
        if (modalitaPescaggio) {
            mazzoDiPescaggio.addAll(mazzo.getCarte());
            mazzo.getCarte().clear();
        }
        
        this.turno = 0;
        this.primoDiMano = 0;
        support.firePropertyChange("turno", -1, turno);
    }

    public void prossimoTurno() {
        int old = turno;
        turno = (turno + 1) % giocatori.length;
        support.firePropertyChange("turno", old, turno);
    }

    /**
     * FASE 1: Esecuzione della mossa singola.
     * Mette la carta sul tavolo e passa il turno, MA NON PULISCE IL TAVOLO.
     */
    public void giocaCarta(Giocatore g, Carta c) {
        // Verifica accuse (solo prima mano)
        if (primaMano && tavolo.isEmpty()) {
            verificaAccuse();
        }
        
        // Controllo Obbligo Seme
        if (!tavolo.isEmpty()) {
            String semeObbligatorio = tavolo.get(0).getSeme();
            // Controllo se il giocatore ha il seme
            boolean haSeme = g.getMano().stream()
                    .anyMatch(carta -> carta.getSeme().equals(semeObbligatorio));
            
            if (!c.getSeme().equals(semeObbligatorio) && haSeme) {
                throw new IllegalStateException("Il giocatore " + g.getNickname() + 
                        " deve rispondere al seme " + semeObbligatorio);
            }
        }

        // Esegui giocata
        System.out.println("GIOCATA: " + g.getNickname() + " gioca " + c);
        tavolo.add(c);
        g.rimuoviCarta(c);
        
        support.firePropertyChange("giocata", null, c);
        support.firePropertyChange("tavolo", null, tavolo);

        // Se la mano NON è finita, tocca al prossimo
        if (tavolo.size() < giocatori.length) {
            prossimoTurno();
        }
        // Se la mano è finita, NON facciamo nulla qui. 
        // Aspettiamo che il Controller chiami 'concludiMano()' dopo il timer.
    }

    /**
     * FASE 2: Risoluzione della mano (Chiamato dal Controller dopo il timer).
     * Calcola vincitore, assegna punti, pulisce tavolo.
     */
    public void concludiMano() {
        if (tavolo.size() != giocatori.length) return; 

        System.out.println("--- RISOLUZIONE MANO ---");

        // 1. Calcola vincitore
        Giocatore vincitore = calcolaVincitoreMano();
        System.out.println("Vincitore mano: " + vincitore.getNickname());

        // 2. Assegna le carte alle PRESE del vincitore (NON al mazzo di pescaggio!)
        prese.get(vincitore).addAll(tavolo);

        // 3. Pescaggio (se attivo e se ci sono carte nel mazzetto)
        if (modalitaPescaggio) {
            // CORREZIONE: Non rimettiamo il tavolo nel mazzo!
            // Peschiamo solo se il mazzo ha ancora carte
            if (!mazzoDiPescaggio.isEmpty()) {
                pescaCarteDopoMano(vincitore);
            } else {
                System.out.println("Mazzo esaurito, si gioca con le ultime carte.");
            }
        }

        // 4. Aggiorna Punti
        support.firePropertyChange("punti", null, prese);

        // 5. Pulisci tavolo
        tavolo.clear();
        support.firePropertyChange("tavolo", null, tavolo);

        // 6. Il vincitore diventa primo di mano
        primoDiMano = java.util.Arrays.asList(giocatori).indexOf(vincitore);
        turno = primoDiMano;
        support.firePropertyChange("turno", null, turno);
    }
    private void verificaAccuse() {
        if (!primaMano) return;
        
        for (Giocatore g : giocatori) {
            List<Utility.Accusa> accuse = Utility.checkAccuse(g.getMano());
            if (!accuse.isEmpty()) {
                for (Utility.Accusa a : accuse) {
                    puntiAccuse.put(g, puntiAccuse.get(g) + a.punti);
                    support.firePropertyChange("accusa", null, new AccusaEvent(g, a));
                }
            }
        }
        primaMano = false;
    }

    private void pescaCarteDopoMano(Giocatore vincitore) {
        // Nel Tressette 1vs1 (Pizzico):
        // Il vincitore pesca per primo, poi il perdente
        int indexVincitore = Arrays.asList(giocatori).indexOf(vincitore);
        int indexPerdente = (indexVincitore + 1) % 2;

        pescaSingola(giocatori[indexVincitore]);
        pescaSingola(giocatori[indexPerdente]);
    }
    
    private void pescaSingola(Giocatore g) {
        if (!mazzoDiPescaggio.isEmpty()) {
            Carta c = mazzoDiPescaggio.remove(0);
            g.aggiungiCarta(c);
            support.firePropertyChange("cartaPescata", null, new CartaPescataEvent(g, c));
        }
    }

    private Giocatore calcolaVincitoreMano() {
        if (tavolo.isEmpty()) return null;

        Carta cartaGuida = tavolo.get(0);
        String semeGuida = cartaGuida.getSeme();
        
        Carta cartaVincente = cartaGuida;
        int indiceVincenteRelativo = 0;

        for (int i = 1; i < tavolo.size(); i++) {
            Carta corrente = tavolo.get(i);
            if (corrente.getSeme().equals(semeGuida)) {
                if (corrente.getForza() > cartaVincente.getForza()) {
                    cartaVincente = corrente;
                    indiceVincenteRelativo = i;
                }
            }
        }
        
        // L'indice nel tavolo è relativo al primo di mano
        int indiceAssoluto = (primoDiMano + indiceVincenteRelativo) % giocatori.length;
        return giocatori[indiceAssoluto];
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    // Classi Evento
    public static class CartaPescataEvent {
        public final Giocatore giocatore;
        public final Carta carta;
        public CartaPescataEvent(Giocatore g, Carta c) { this.giocatore = g; this.carta = c; }
    }
    
    public static class AccusaEvent {
        public final Giocatore giocatore;
        public final Utility.Accusa accusa;
        public AccusaEvent(Giocatore g, Utility.Accusa a) { this.giocatore = g; this.accusa = a; }
    }
}