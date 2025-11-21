package tressette.util;

import tressette.model.Giocatore;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserManager {

    private static final String FILE_PROFILO = "profili_tressette.txt";

    /**
     * Salva o Aggiorna un giocatore nella lista e riscrive il file.
     */
    public static void salvaOAggiornaProfilo(Giocatore g) {
        List<Giocatore> tutti = caricaTuttiIProfili();
        boolean trovato = false;

        // Cerca se esiste già e aggiornalo
        for (int i = 0; i < tutti.size(); i++) {
            if (tutti.get(i).getNickname().equals(g.getNickname())) {
                tutti.set(i, g); // Sostituisci con i dati nuovi
                trovato = true;
                break;
            }
        }

        // Se non c'è, aggiungilo
        if (!trovato) {
            tutti.add(g);
        }

        riscriviFile(tutti);
    }

    public static List<Giocatore> caricaTuttiIProfili() {
        List<Giocatore> lista = new ArrayList<>();
        File file = new File(FILE_PROFILO);
        if (!file.exists()) return lista;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String riga;
            while ((riga = reader.readLine()) != null) {
                if (riga.isEmpty()) continue;
                
                String[] parti = riga.split(";");
                if (parti.length >= 6) {
                    try {
                        String nickname = parti[0];
                        String avatarPath = parti[1];
                        int giocate = Integer.parseInt(parti[2]);
                        int vinte = Integer.parseInt(parti[3]);
                        int perse = Integer.parseInt(parti[4]);
                        int livello = Integer.parseInt(parti[5]);

                        Giocatore g = new Giocatore(nickname, avatarPath);
                        g.setPartiteGiocate(giocate);
                        g.setPartiteVinte(vinte);
                        g.setPartitePerse(perse);
                        g.setLivello(livello);
                        
                        lista.add(g);
                    } catch (NumberFormatException e) {
                        System.err.println("Riga corrotta saltata: " + riga);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    private static void riscriviFile(List<Giocatore> lista) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PROFILO))) {
            for (Giocatore g : lista) {
                String riga = String.format("%s;%s;%d;%d;%d;%d",
                    g.getNickname(),
                    g.getAvatar(), 
                    g.getPartiteGiocate(),
                    g.getPartiteVinte(),
                    g.getPartitePerse(),
                    g.getLivello()
                );
                writer.write(riga);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}