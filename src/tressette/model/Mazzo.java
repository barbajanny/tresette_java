package tressette.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Mazzo {
    private List<Carta> carte;

    public Mazzo() {
        carte = new ArrayList<>();
        creaMazzoCompleto();
        mescola();
    }

    private void creaMazzoCompleto() {
        String[] semi = {"bastoni", "coppe", "denara", "spade"};
        int[] valori = {1,2,3,4,5,6,7,8,9,10};

        for (String seme : semi) {
            for (int valore : valori) {
                carte.add(new Carta(seme, valore));
            }
        }
    }

    public void mescola() {
        Collections.shuffle(carte);
    }

    public Carta pescaCarta() {
        if (carte.isEmpty()) return null;
        return carte.remove(0);
    }

    public boolean isVuoto() {
        return carte.isEmpty();
    }

    public int carteRimaste() {
        return carte.size();
    }

    public List<Carta> getCarte() {
        return new ArrayList<>(carte);
    }


    public void ricostruisciMazzoDaPrese(List<Carta> carte) {
        this.carte.addAll(carte);
        mescola();
    }
}