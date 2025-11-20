package tressette.util;

import tressette.model.Giocatore;
import java.util.List;
import java.util.stream.Collectors;

public class StreamHelper {

    /**
     * Restituisce i giocatori con almeno minVittorie partite vinte.
     */
    public static List<Giocatore> topVincitori(List<Giocatore> giocatori, int minVittorie) {
        return giocatori.stream()
                .filter(g -> g.getPartiteVinte() >= minVittorie)
                .collect(Collectors.toList());
    }

    /**
     * Restituisce i giocatori con livello almeno minLivello.
     */
    public static List<Giocatore> topLivello(List<Giocatore> giocatori, int minLivello) {
        return giocatori.stream()
                .filter(g -> g.getLivello() >= minLivello)
                .collect(Collectors.toList());
    }

    /**
     * Restituisce i giocatori che hanno perso più partite di minPerse.
     */
    public static List<Giocatore> piuSconfitte(List<Giocatore> giocatori, int minPerse) {
        return giocatori.stream()
                .filter(g -> g.getPartitePerse() >= minPerse)
                .collect(Collectors.toList());
    }

    /**
     * Restituisce i giocatori ordinati per numero di vittorie decrescente.
     */
    public static List<Giocatore> ordinaPerVittorie(List<Giocatore> giocatori) {
        return giocatori.stream()
                .sorted((g1, g2) -> Integer.compare(g2.getPartiteVinte(), g1.getPartiteVinte()))
                .collect(Collectors.toList());
    }

    /**
     * Restituisce i giocatori ordinati per livello decrescente.
     */
    public static List<Giocatore> ordinaPerLivello(List<Giocatore> giocatori) {
        return giocatori.stream()
                .sorted((g1, g2) -> Integer.compare(g2.getLivello(), g1.getLivello()))
                .collect(Collectors.toList());
    }
}
