package tressette.model;

import java.util.ArrayList;
import java.util.List;

public class GiocatoreCPU extends Giocatore {

    private Partita partita;

    public GiocatoreCPU(String nickname, String avatar) {
        super(nickname, avatar);
        this.partiteGiocate = 0;
        this.partiteVinte = 0;
        this.partitePerse = 0;
        this.livello = 1;
    }

    public void setPartita(Partita partita) {
        this.partita = partita;
    }

    public Carta scegliCarta(List<Carta> tavolo) {
        List<Carta> mano = getMano();
        if (mano.isEmpty()) return null;
        
        // REGOLA: se c'è già una carta sul tavolo, devi rispondere al seme
        if (!tavolo.isEmpty()) {
            String semeGuidato = tavolo.get(0).getSeme();
            
            // Prima cerca se hai carte dello stesso seme
            List<Carta> carteStessoSeme = new ArrayList<Carta>();
            for (Carta c : mano) {
                if (c.getSeme().equals(semeGuidato)) {
                    carteStessoSeme.add(c);
                }
            }
            
            // Se hai carte dello stesso seme, gioca una di quelle
            if (!carteStessoSeme.isEmpty()) {
                // Scegli la carta più alta o più bassa in base alla strategia
                return scegliCartaMigliore(carteStessoSeme);
            }
            
            // Se non hai carte dello stesso seme, puoi giocare qualsiasi carta
            // Strategia: gioca la carta più bassa per conservare quelle buone
            return scegliCartaPeggiore(mano);
        }
        
        // Se non ci sono carte sul tavolo (primo di mano)
        // Strategia: gioca una carta media
        return scegliCartaMedia(mano);
    }

    // Metodo per scegliere la carta migliore da giocare (più alta)
    private Carta scegliCartaMigliore(List<Carta> carte) {
        Carta cartaMigliore = carte.get(0);
        for (Carta c : carte) {
            if (c.getForza() > cartaMigliore.getForza()) {
                cartaMigliore = c;
            }
        }
        return cartaMigliore;
    }

    // Metodo per scegliere la carta peggiore (più bassa)
    private Carta scegliCartaPeggiore(List<Carta> carte) {
        Carta cartaPeggiore = carte.get(0);
        for (Carta c : carte) {
            if (c.getForza() < cartaPeggiore.getForza()) {
                cartaPeggiore = c;
            }
        }
        return cartaPeggiore;
    }

    // Metodo per scegliere una carta media
    private Carta scegliCartaMedia(List<Carta> carte) {
        // Calcola la forza media
        int sommaForza = 0;
        for (Carta c : carte) {
            sommaForza += c.getForza();
        }
        int forzaMedia = sommaForza / carte.size();
        
        // Trova la carta più vicina alla forza media
        Carta cartaMedia = carte.get(0);
        int differenzaMinima = Math.abs(cartaMedia.getForza() - forzaMedia);
        
        for (Carta c : carte) {
            int differenza = Math.abs(c.getForza() - forzaMedia);
            if (differenza < differenzaMinima) {
                differenzaMinima = differenza;
                cartaMedia = c;
            }
        }
        
        return cartaMedia;
    }

    public Carta gioca() {
        Carta carta = scegliCarta(partita.getTavolo());
        if (carta != null) {
            rimuoviCarta(carta);
        }
        return carta;
    }
}