package tressette.controller;

import tressette.model.*;
import tressette.view.GUI;
import tressette.util.Utility;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.SwingUtilities;

public class GameController {

    private GUI gui;
    private Partita partita;
    private Timer animationTimer;
    private List<Carta> carteCPUdaGiocare = new ArrayList<>();
    private int currentCPUIndex = 0;
    private final int DELAY_MS = 1500;

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

    private int scegliNumeroCPUGUI() {
        Object[] options = {"1 CPU", "2 CPU", "3 CPU"};
        int scelta = JOptionPane.showOptionDialog(gui,
            "Quanti avversari CPU vuoi?",
            "Scelta numero di CPU",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);
        
        return (scelta == JOptionPane.CLOSED_OPTION) ? 1 : scelta + 1;
    }

    private void mostraAccuseIniziali() {
        // Verifica le accuse per il giocatore umano
        Giocatore umano = partita.getGiocatori()[0];
        List<Utility.Accusa> accuse = Utility.checkAccuse(umano.getMano());
        
        if (!accuse.isEmpty()) {
            StringBuilder sb = new StringBuilder("<html><b>ACCUSE TROVATE!</b><br><br>");
            for (Utility.Accusa accusa : accuse) {
                sb.append("• ").append(accusa).append("<br>");
            }
            sb.append("<br>I punti verranno aggiunti alla fine della partita.</html>");
            
            JOptionPane.showMessageDialog(gui, sb.toString(), 
                                        "Accuse - " + umano.getNickname(), 
                                        JOptionPane.INFORMATION_MESSAGE);
        }
        
        // Verifica e mostra accuse CPU (solo in debug)
        for (int i = 1; i < partita.getGiocatori().length; i++) {
            Giocatore cpu = partita.getGiocatori()[i];
            List<Utility.Accusa> accuseCPU = Utility.checkAccuse(cpu.getMano());
            
            if (!accuseCPU.isEmpty()) {
                StringBuilder sb = new StringBuilder("<html><b>ACCUSE CPU!</b><br><br>");
                for (Utility.Accusa accusa : accuseCPU) {
                    sb.append("• ").append(accusa).append("<br>");
                }
                sb.append("<br>").append(cpu.getNickname()).append(" ha fatto accuse!</html>");
                
                JOptionPane.showMessageDialog(gui, sb.toString(), 
                                            "Accuse - " + cpu.getNickname(), 
                                            JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void creaPartita(int numCpu) {
        Giocatore umano = new Giocatore("Thomas", "avatar1");
        List<Giocatore> giocatori = new ArrayList<>();
        giocatori.add(umano);

        for (int i = 1; i <= numCpu; i++) {
            GiocatoreCPU cpu = new GiocatoreCPU("CPU" + i, "avatarCPU");
            giocatori.add(cpu);
        }

        partita = new Partita(giocatori.toArray(new Giocatore[0]));
        
        // Abilita il pescaggio solo in 1vs1
        if (numCpu == 1) {
            partita.setModalitaPescaggio(true);
            System.out.println("Modalità pescaggio attivata per 1vs1");
        }

        for (Giocatore g : giocatori) {
            if (g instanceof GiocatoreCPU cpu) {
                cpu.setPartita(partita);
            }
            g.addPropertyChangeListener(gui);
        }

        partita.addPropertyChangeListener(gui);
    }

    public void giocatoreGiocaCarta(Carta c) {
        Giocatore umano = partita.getGiocatori()[0];
        if (!umano.getMano().contains(c)) return;

        // CONTROLLO OBBLIGO SEME
        List<Carta> tavolo = partita.getTavolo();
        if (!tavolo.isEmpty()) {
            String semeObbligatorio = tavolo.get(0).getSeme();
            
            if (!c.getSeme().equals(semeObbligatorio)) {
                boolean haSemeObbligatorio = false;
                for (Carta cartaMano : umano.getMano()) {
                    if (cartaMano.getSeme().equals(semeObbligatorio)) {
                        haSemeObbligatorio = true;
                        break;
                    }
                }
                
                if (haSemeObbligatorio) {
                    JOptionPane.showMessageDialog(gui, 
                        "Devi rispondere al seme " + semeObbligatorio + "!", 
                        "Mossa non valida", 
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
        }

        partita.giocaCarta(umano, c);
        SwingUtilities.invokeLater(() -> gui.aggiornaTavolo(partita.getTavolo()));
        aggiornaGUI();
        
        //SwingUtilities.invokeLater(() -> gui.aggiornaTavolo(partita.getTavolo()));
        
        if (partita.getTavolo().size() == partita.getGiocatori().length) {
            Timer handTimer = new Timer(1000, e -> {
                if (partitaFinita()) {
                    mostraRisultato();
                    int choice = JOptionPane.showConfirmDialog(gui, 
                        "Vuoi giocare un'altra partita?", 
                        "Partita Finita", 
                        JOptionPane.YES_NO_OPTION);
                    if (choice == JOptionPane.YES_OPTION) {
                        startGame();
                    }
                } else {
                    controllaTurnoCPU();
                }
            });
            handTimer.setRepeats(false);
            handTimer.start();
        } else {
            controllaTurnoCPU();
        }
    }

    private void controllaTurnoCPU() {
        if (partita != null && !partitaFinita()) {
            int turnoAttuale = partita.getTurno();
            
            if (turnoAttuale > 0 && turnoAttuale < partita.getGiocatori().length) {
                Giocatore cpu = partita.getGiocatori()[turnoAttuale];
                
                if (cpu instanceof GiocatoreCPU && !cpu.getMano().isEmpty()) {
                    Timer cpuTimer = new Timer(1000, e -> {
                        Carta cartaCPU = ((GiocatoreCPU) cpu).scegliCarta(partita.getTavolo());
                        if (cartaCPU != null) {
                            partita.giocaCarta(cpu, cartaCPU);
                            SwingUtilities.invokeLater(() -> gui.aggiornaTavolo(partita.getTavolo()));
                            aggiornaGUI();
                            
                            if (partita.getTavolo().size() == partita.getGiocatori().length) {
                                Timer handTimer = new Timer(1000, e2 -> {
                                    if (partitaFinita()) {
                                        mostraRisultato();
                                        int choice = JOptionPane.showConfirmDialog(gui, 
                                            "Vuoi giocare un'altra partita?", 
                                            "Partita Finita", 
                                            JOptionPane.YES_NO_OPTION);
                                        if (choice == JOptionPane.YES_OPTION) {
                                            startGame();
                                        }
                                    } else {
                                        Timer nextTurnTimer = new Timer(500, e3 -> {
                                            controllaTurnoCPU();
                                        });
                                        nextTurnTimer.setRepeats(false);
                                        nextTurnTimer.start();
                                    }
                                });
                                handTimer.setRepeats(false);
                                handTimer.start();
                            } else {
                                Timer nextTurnTimer = new Timer(500, e2 -> {
                                    controllaTurnoCPU();
                                });
                                nextTurnTimer.setRepeats(false);
                                nextTurnTimer.start();
                            }
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

    private void aggiornaGUI() {
        SwingUtilities.invokeLater(() -> {
            try {
                if (partita != null && partita.getGiocatori() != null && partita.getGiocatori().length > 0) {
                    Giocatore umano = partita.getGiocatori()[0];
                    if (umano != null) {
                        gui.aggiornaMano(umano.getMano(), partita.getTurno() == 0, partita.getTavolo());
                    }
                    if (partita.getTavolo() != null) {
                        gui.aggiornaTavolo(partita.getTavolo());
                    }
                    if (partita.getPrese() != null) {
                        gui.aggiornaPunti(partita.getPrese());
                    }
                    gui.aggiornaTurno(partita.getTurno());
                }
            } catch (Exception e) {
                System.err.println("Errore nell'aggiornamento della GUI: " + e.getMessage());
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
        StringBuilder sb = new StringBuilder("<html><body><h2>Partita finita!</h2>");
        
        // Punti Accuse
        sb.append("<h3>Punti Accuse:</h3><ul>");
        for (Giocatore g : partita.getGiocatori()) {
            int puntiAccuse = partita.getPuntiAccuse(g);
            if (puntiAccuse > 0) {
                sb.append("<li>").append(g.getNickname())
                  .append(": ").append(puntiAccuse).append(" punti accuse</li>");
            }
        }
        sb.append("</ul>");
        
        // Punti Gioco
        sb.append("<h3>Punti Gioco:</h3><ul>");
        for (Giocatore g : partita.getGiocatori()) {
            String punti = Utility.calcolaPunti(partita.getPrese().get(g));
            sb.append("<li>").append(g.getNickname())
              .append(": ").append(punti).append(" punti</li>");
        }
        sb.append("</ul>");
        
        // Totale Punti
        sb.append("<h3>Totale Punti:</h3><ul>");
        for (Giocatore g : partita.getGiocatori()) {
            int puntiAccuse = partita.getPuntiAccuse(g);
            int puntiGioco = Utility.puntiTotaliInTerzi(partita.getPrese().get(g));
            int totale = puntiAccuse * 3 + puntiGioco;
            
            sb.append("<li><b>").append(g.getNickname());
            sb.append(": ").append(Utility.formatFrazione(totale))
              .append(" punti</b></li>");
        }
        sb.append("</ul></body></html>");
        
        JOptionPane.showMessageDialog(gui, sb.toString(), 
                                    "Risultato Finale", 
                                    JOptionPane.INFORMATION_MESSAGE);
    }

    public Partita getPartita() {
        return partita;
    }
}