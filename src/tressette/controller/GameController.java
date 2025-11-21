package tressette.controller;

import tressette.model.*;
import tressette.view.GUI;
import tressette.util.AudioManager;
import tressette.util.UserManager;
import tressette.util.Utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.SwingUtilities;

public class GameController {

    private GUI gui;
    private Partita partita;

    public GameController(GUI gui) {
        this.gui = gui;
        gui.setController(this);
    }

    public void startGame() {
        int numCpu = scegliNumeroCPUGUI();
        creaPartita(numCpu);

        partita.distribuisciCarte();
        aggiornaGUI();
        
        mostraAccuseIniziali();
        
        controllaTurnoCPU();
    }
    
    // --- METODI SETUP ---

    private Giocatore setupGiocatoreUmano() {
        // 1. Mostra il Menu Iniziale (Tabella Profili)
        tressette.view.MenuIniziale menu = new tressette.view.MenuIniziale(gui);
        menu.setVisible(true); // Si ferma qui finché l'utente non clicca qualcosa
        
        // 2. Controlla cosa ha scelto l'utente
        if (menu.isNuovoUtenteSelezionato()) {
            // --- LOGICA CREAZIONE NUOVO (Quella vecchia) ---
            return creaNuovoProfiloWizard();
        } 
        else if (menu.getGiocatoreSelezionato() != null) {
            // --- CARICAMENTO PROFILO ESISTENTE ---
            Giocatore g = menu.getGiocatoreSelezionato();
            // Importante: Ricarica l'immagine avatar perché dal file txt abbiamo solo il path stringa
            g.setAvatar(g.getAvatar()); 
            return g;
        }
        else {
            // Se chiude la finestra con la X, usiamo un default o chiudiamo
            System.exit(0);
            return null;
        }
    }

    // Ho spostato la vecchia logica di creazione in un metodo separato per pulizia
    private Giocatore creaNuovoProfiloWizard() {
        String[] nomiFile = {"hacker.png", "dog.png", "robot.jpg", "dinosaur.png"};
        Object[] opzioniVisuali = new Object[nomiFile.length];
        
        for (int i = 0; i < nomiFile.length; i++) {
            String path = "/avatar/" + nomiFile[i];
            java.net.URL imgUrl = getClass().getResource(path);
            if (imgUrl != null) {
                ImageIcon original = new ImageIcon(imgUrl);
                opzioniVisuali[i] = Utility.scalaImmagine(original, 80, 80);
            } else {
                opzioniVisuali[i] = "Avatar " + (i + 1); 
            }
        }

        String nickname = JOptionPane.showInputDialog(gui, "Inserisci Nickname:", "Nuovo Profilo", JOptionPane.QUESTION_MESSAGE);
        if (nickname == null || nickname.trim().isEmpty()) nickname = "Giocatore 1";

        int scelta = JOptionPane.showOptionDialog(gui, "Scegli Avatar:", "Nuovo Profilo",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, opzioniVisuali, opzioniVisuali[0]); 

        if (scelta == JOptionPane.CLOSED_OPTION) scelta = 0;
        String avatarSceltoPath = "/avatar/" + nomiFile[scelta];
        
        return new Giocatore(nickname, avatarSceltoPath);
    }
    private int scegliNumeroCPUGUI() {
        Object[] options = {"1 CPU", "2 CPU", "3 CPU"};
        int scelta = JOptionPane.showOptionDialog(gui, "Quanti avversari CPU vuoi?", "Scelta numero di CPU",
            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        return (scelta == JOptionPane.CLOSED_OPTION) ? 1 : scelta + 1;
    }

    private void mostraAccuseIniziali() {
        Giocatore umano = partita.getGiocatori()[0];
        List<Utility.Accusa> accuse = Utility.checkAccuse(umano.getMano());
        
        if (!accuse.isEmpty()) {
            StringBuilder sb = new StringBuilder("<html><b>ACCUSE TROVATE!</b><br><br>");
            for (Utility.Accusa accusa : accuse) sb.append("• ").append(accusa).append("<br>");
            sb.append("<br>I punti verranno aggiunti alla fine della partita.</html>");
            
            JOptionPane.showMessageDialog(gui, sb.toString(), "Accuse - " + umano.getNickname(), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void creaPartita(int numCpu) {
        Giocatore umano = setupGiocatoreUmano();
        List<Giocatore> giocatori = new ArrayList<>();
        giocatori.add(umano);

        for (int i = 1; i <= numCpu; i++) {
            String avatarCpu = "/avatar/robot.jpg"; 
            GiocatoreCPU cpu = new GiocatoreCPU("CPU " + i, avatarCpu);
            giocatori.add(cpu);
        }

        partita = new Partita(giocatori.toArray(new Giocatore[0]));
        if (numCpu == 1) partita.setModalitaPescaggio(true);

        for (Giocatore g : giocatori) {
            if (g instanceof GiocatoreCPU cpu) cpu.setPartita(partita);
            g.addPropertyChangeListener(gui);
        }
        partita.addPropertyChangeListener(gui);
    }

    // --- LOGICA DI GIOCO ---

    public void giocatoreGiocaCarta(Carta c) {
        Giocatore umano = partita.getGiocatori()[0];
        if (!umano.getMano().contains(c)) return;

        try {
            // FASE 1: La carta va sul tavolo (visualizzazione immediata)
            partita.giocaCarta(umano, c);
            
            // FASE 2: Gestione del flusso (attesa o prossimo turno)
            gestisciFlussoGioco();

        } catch (IllegalStateException e) {
            AudioManager.play("/audio/errore.wav");
            JOptionPane.showMessageDialog(gui, e.getMessage(), "Mossa non valida", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Gestisce la logica temporale:
     * - Se la mano è piena -> Aspetta -> Concludi Mano
     * - Se la mano non è piena -> Passa al prossimo
     */
    private void gestisciFlussoGioco() {
        // Se tutti hanno giocato (tavolo pieno)
        if (partita.getTavolo().size() == partita.getGiocatori().length) {
            
            // TIMER DI ATTESA (1.5 secondi) per far vedere le carte
            Timer handTimer = new Timer(1500, e -> {
                
                // Ora calcoliamo chi ha vinto e puliamo il tavolo
                partita.concludiMano(); 
                
                if (partitaFinita()) {
                    mostraRisultato();
                    chiediNuovaPartita();
                } else {
                    controllaTurnoCPU(); // Tocca al vincitore
                }
            });
            handTimer.setRepeats(false);
            handTimer.start();
            
        } else {
            // La mano continua
            controllaTurnoCPU();
        }
    }

    private void controllaTurnoCPU() {
        if (partita != null && !partitaFinita()) {
            int turnoAttuale = partita.getTurno();
            
            if (turnoAttuale > 0 && turnoAttuale < partita.getGiocatori().length) {
                Giocatore cpu = partita.getGiocatori()[turnoAttuale];
                
                if (cpu instanceof GiocatoreCPU && !cpu.getMano().isEmpty()) {
                    // Ritardo "pensiero" CPU
                    Timer cpuTimer = new Timer(1000, e -> {
                        Carta cartaCPU = ((GiocatoreCPU) cpu).scegliCarta(partita.getTavolo());
                        if (cartaCPU != null) {
                            // FASE 1 CPU
                            partita.giocaCarta(cpu, cartaCPU);
                            
                            // Aggiorniamo la GUI per vedere subito la carta
                            aggiornaGUI();
                            
                            // FASE 2 Gestione
                            gestisciFlussoGioco();
                        }
                    });
                    cpuTimer.setRepeats(false);
                    cpuTimer.start();
                }
            }
        } else if (partitaFinita()) {
            mostraRisultato();
        }
    }

    private void chiediNuovaPartita() {
        int choice = JOptionPane.showConfirmDialog(gui, "Vuoi giocare un'altra partita?", "Partita Finita", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) startGame();
    }

    private void aggiornaGUI() {
        SwingUtilities.invokeLater(() -> {
            try {
                if (partita != null && partita.getGiocatori() != null && partita.getGiocatori().length > 0) {
                    Giocatore umano = partita.getGiocatori()[0];
                    if (umano != null) gui.aggiornaMano(umano.getMano(), partita.getTurno() == 0, partita.getTavolo());
                    if (partita.getTavolo() != null) gui.aggiornaTavolo(partita.getTavolo());
                    if (partita.getPrese() != null) gui.aggiornaPunti(partita.getPrese());
                    gui.aggiornaTurno(partita.getTurno());
                }
            } catch (Exception e) {
                System.err.println("Errore GUI: " + e.getMessage());
            }
        });
    }

    private boolean partitaFinita() {
        for (Giocatore g : partita.getGiocatori()) {
            if (!g.getMano().isEmpty()) return false;
        }
        return true;
    }

    private void mostraRisultato() {
        // Iniziamo l'HTML
        StringBuilder sb = new StringBuilder("<html><body style='width: 300px'>");
        sb.append("<h2>Partita finita!</h2>");

        // 1. Punti Accuse
        sb.append("<h3>Punti Accuse:</h3><ul>");
        String listaAccuse = Arrays.stream(partita.getGiocatori())
            .filter(g -> partita.getPuntiAccuse(g) > 0)
            .map(g -> "<li>" + g.getNickname() + ": " + partita.getPuntiAccuse(g) + " punti accuse</li>")
            .collect(Collectors.joining());
        
        if (listaAccuse.isEmpty()) sb.append("<li>Nessuna accusa</li>");
        else sb.append(listaAccuse);
        sb.append("</ul>");

        // 2. Punti Gioco
        sb.append("<h3>Punti Gioco:</h3><ul>");
        String listaPunti = Arrays.stream(partita.getGiocatori())
            .map(g -> {
                String punti = Utility.calcolaPunti(partita.getPrese().get(g));
                return "<li>" + g.getNickname() + ": " + punti + " punti</li>";
            })
            .collect(Collectors.joining());
        sb.append(listaPunti).append("</ul>");

        // 3. Totale Punti e Calcolo Vincitore
        sb.append("<h3>Totale Punti:</h3><ul>");
        
        Giocatore umano = partita.getGiocatori()[0];
        // Calcoliamo i punti totali dell'umano per confrontarli dopo
        int puntiUmanoTotali = Utility.puntiTotaliInTerzi(partita.getPrese().get(umano)) + (partita.getPuntiAccuse(umano) * 3);
        boolean hoVinto = true;

        String listaTotale = Arrays.stream(partita.getGiocatori())
            .map(g -> {
                int puntiAccuse = partita.getPuntiAccuse(g);
                int puntiGioco = Utility.puntiTotaliInTerzi(partita.getPrese().get(g));
                int totale = puntiAccuse * 3 + puntiGioco;
                
                // Controllo vittoria per la logica (se una CPU ha più punti, ho perso)
                if (g != umano && totale > puntiUmanoTotali) {
                   // Nota: questa variabile boolean locale nella lambda non modifica quella esterna facilmente,
                   // quindi facciamo il calcolo del vincitore separatamente sotto per sicurezza.
                }
                
                return "<li><b>" + g.getNickname() + ": " + Utility.formatFrazione(totale) + " punti</b></li>";
            })
            .collect(Collectors.joining());
        sb.append(listaTotale).append("</ul>");

        // --- LOGICA AGGIORNAMENTO PROFILO ---
        
        // Ricalcoliamo chi ha vinto per sicurezza
        for (int i = 1; i < partita.getGiocatori().length; i++) {
            Giocatore cpu = partita.getGiocatori()[i];
            int puntiCpu = Utility.puntiTotaliInTerzi(partita.getPrese().get(cpu)) + (partita.getPuntiAccuse(cpu) * 3);
            if (puntiCpu > puntiUmanoTotali) {
                hoVinto = false;
                break;
            }
        }

        int livelloVecchio = umano.getLivello();
        umano.aggiornaStatistiche(hoVinto);
        int livelloNuovo = umano.getLivello();

        // Salvataggio
        UserManager.salvaOAggiornaProfilo(umano);

        // Messaggio Level Up (se c'è stato)
        if (livelloNuovo > livelloVecchio) {
            sb.append("<br><hr><h2 style='color:green; text-align:center'>LEVEL UP!</h2>");
            sb.append("<p style='text-align:center'>Sei passato al livello <b>").append(livelloNuovo).append("</b>!</p>");
            AudioManager.play("/audio/vittoria.wav");
        } else if (hoVinto) {
            sb.append("<br><h3 style='color:blue; text-align:center'>HAI VINTO!</h3>");
            AudioManager.play("/audio/vittoria.wav");
        } else {
            sb.append("<br><h3 style='color:red; text-align:center'>HAI PERSO...</h3>");
        }

        // CHIUSURA HTML (Solo alla fine!)
        sb.append("</body></html>");

        JOptionPane.showMessageDialog(gui, sb.toString(), 
            "Risultato Finale", JOptionPane.INFORMATION_MESSAGE);
    }
    public Partita getPartita() {
        return partita;
    }
}