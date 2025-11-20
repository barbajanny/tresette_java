package tressette.view;

import tressette.model.*;
import tressette.controller.GameController;
import tressette.util.Utility;
import java.util.ArrayList;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;

public class GUI extends JFrame implements PropertyChangeListener {

    private JPanel manoPanel;
    private JPanel tavoloPanel;
    private JLabel turnoLabel;
    private JLabel puntiLabel;
    private GameController controller;

    public GUI() {
        setTitle("Tressette");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Panel per le informazioni
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        turnoLabel = new JLabel("Turno: -", SwingConstants.CENTER);
        puntiLabel = new JLabel("Punti: -", SwingConstants.CENTER);
        
        turnoLabel.setFont(new Font("Arial", Font.BOLD, 16));
        puntiLabel.setFont(new Font("Arial", Font.BOLD, 14));
        turnoLabel.setForeground(Color.BLUE);
        infoPanel.setBackground(Color.WHITE);
        
        infoPanel.add(turnoLabel);
        infoPanel.add(puntiLabel);

        // Panel per il tavolo
        tavoloPanel = new JPanel();
        tavoloPanel.setBackground(new Color(240, 240, 240));
        tavoloPanel.setPreferredSize(new Dimension(800, 300));

        // Panel per la mano
        manoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        manoPanel.setBackground(new Color(220, 220, 220));
        manoPanel.setPreferredSize(new Dimension(800, 150));

        // Aggiungi tutto al frame
        add(infoPanel, BorderLayout.NORTH);
        add(tavoloPanel, BorderLayout.CENTER);
        add(manoPanel, BorderLayout.SOUTH);

        setSize(1000, 700);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void setController(GameController controller) {
        this.controller = controller;
    }

    public void aggiornaMano(List<Carta> mano, boolean turnoUmano, List<Carta> tavolo) {
        SwingUtilities.invokeLater(() -> {
            try {
                manoPanel.removeAll();
                
                if (mano == null || mano.isEmpty()) {
                    manoPanel.revalidate();
                    manoPanel.repaint();
                    return;
                }
                
                // Controlla se c'è obbligo di risposta al seme
                boolean obbligoSeme = false;
                String semeObbligatorio = null;
                
                if (tavolo != null && !tavolo.isEmpty()) {
                    obbligoSeme = true;
                    semeObbligatorio = tavolo.get(0).getSeme();
                }
                
                List<Carta> manoCopy = new ArrayList<Carta>(mano);
                
                for (Carta carta : manoCopy) {
                    if (carta == null) continue;
                    
                    JButton cardButton = new JButton();
                    ImageIcon originalIcon = carta.getImmagine();
                    if (originalIcon != null) {
                        ImageIcon scaledIcon = Utility.scalaImmagine(originalIcon, 80, 120);
                        cardButton.setIcon(scaledIcon);
                    } else {
                        cardButton.setText(carta.toString());
                    }
                    
                    cardButton.setPreferredSize(new Dimension(80, 120));
                    cardButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                    
                    // DISABILITA SE NON È IL TURNO DEL GIOCATORE UMANO
                    if (!turnoUmano) {
                        cardButton.setEnabled(false);
                        cardButton.setBackground(Color.LIGHT_GRAY);
                    } 
                    // Altrimenti, controlla obbligo di seme
                    else if (obbligoSeme && semeObbligatorio != null && !carta.getSeme().equals(semeObbligatorio)) {
                        // Controlla se il giocatore ha altre carte del seme obbligatorio
                        boolean haSemeObbligatorio = false;
                        for (Carta cartaMano : manoCopy) {
                            if (cartaMano.getSeme().equals(semeObbligatorio)) {
                                haSemeObbligatorio = true;
                                break;
                            }
                        }
                        
                        if (haSemeObbligatorio) {
                            cardButton.setEnabled(false);
                            cardButton.setBackground(Color.LIGHT_GRAY);
                        } else {
                            cardButton.setEnabled(true);
                            cardButton.setBackground(Color.WHITE);
                        }
                    } 
                    // Abilita la carta
                    else {
                        cardButton.setEnabled(true);
                        cardButton.setBackground(Color.WHITE);
                    }
                    
                    cardButton.addActionListener(e -> {
                        if (controller != null && cardButton.isEnabled()) {
                            controller.giocatoreGiocaCarta(carta);
                        }
                    });
                    
                    cardButton.setToolTipText(carta.getSeme() + " " + carta.getValore());
                    manoPanel.add(cardButton);
                }
                
                manoPanel.revalidate();
                manoPanel.repaint();
            } catch (Exception e) {
                System.err.println("Errore nell'aggiornamento della mano: " + e.getMessage());
            }
        });
    }

    public void aggiornaTavolo(List<Carta> tavolo) {
        SwingUtilities.invokeLater(() -> {
            try {
                tavoloPanel.removeAll();
                
                if (tavolo == null || tavolo.isEmpty()) {
                    tavoloPanel.revalidate();
                    tavoloPanel.repaint();
                    return;
                }
                
                // Usa un layout a griglia per organizzare meglio le carte con i nomi
                tavoloPanel.setLayout(new GridLayout(2, tavolo.size(), 10, 5));
                tavoloPanel.setBackground(new Color(240, 240, 240));
                
                // Ottieni l'ordine dei giocatori dalla partita
                Giocatore[] giocatori = controller.getPartita().getGiocatori();
                int primoDiMano = controller.getPartita().getTurno(); // il primo di mano è quello che ha iniziato il turno
                
                for (int i = 0; i < tavolo.size(); i++) {
                    Carta carta = tavolo.get(i);
                    
                    // Calcola quale giocatore ha giocato questa carta
                    int indiceGiocatore = (primoDiMano + i) % giocatori.length;
                    Giocatore giocatore = giocatori[indiceGiocatore];
                    
                    // Panel per carta + nome giocatore
                    JPanel cardPanel = new JPanel(new BorderLayout());
                    cardPanel.setBackground(new Color(240, 240, 240));
                    
                    // Label con nome giocatore
                    JLabel playerLabel = new JLabel(giocatore.getNickname(), SwingConstants.CENTER);
                    playerLabel.setFont(new Font("Arial", Font.BOLD, 12));
                    playerLabel.setForeground(Color.BLUE);
                    
                    // Label con la carta
                    JLabel cardLabel = new JLabel();
                    ImageIcon icona = Utility.scalaImmagine(carta.getImmagine(), 80, 120);
                    
                    if (icona != null) {
                        cardLabel.setIcon(icona);
                    } else {
                        cardLabel.setText(carta.toString());
                        cardLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                    }
                    
                    cardLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    cardLabel.setPreferredSize(new Dimension(80, 120));
                    
                    // Aggiungi al panel
                    cardPanel.add(playerLabel, BorderLayout.NORTH);
                    cardPanel.add(cardLabel, BorderLayout.CENTER);
                    
                    // Evidenzia il giocatore di turno corrente
                    if (indiceGiocatore == controller.getPartita().getTurno()) {
                        cardPanel.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
                    }
                    
                    tavoloPanel.add(cardPanel);
                }
                
                tavoloPanel.revalidate();
                tavoloPanel.repaint();
            } catch (Exception e) {
                System.err.println("Errore nell'aggiornamento del tavolo: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    public void aggiornaPunti(java.util.Map<Giocatore, List<Carta>> prese) {
        SwingUtilities.invokeLater(() -> {
            StringBuilder sb = new StringBuilder("Punti: ");
            
            if (prese == null || prese.isEmpty()) {
                puntiLabel.setText("Punti: -");
                return;
            }
            
            // Usa l'ordine dei giocatori dalla partita per consistenza
            if (controller != null && controller.getPartita() != null) {
                Giocatore[] giocatori = controller.getPartita().getGiocatori();
                
                for (int i = 0; i < giocatori.length; i++) {
                    Giocatore g = giocatori[i];
                    if (!prese.containsKey(g)) continue;
                    
                    // USA IL METODO CENTRALIZZATO Utility.calcolaPunti()
                    String puntiStr = Utility.calcolaPunti(prese.get(g));
                    
                    sb.append(g.getNickname()).append("=").append(puntiStr);
                    if (i < giocatori.length - 1) {
                        sb.append(" | ");
                    }
                }
            } else {
                // Fallback se non c'è il controller
                for (Giocatore g : prese.keySet()) {
                    // USA IL METODO CENTRALIZZATO Utility.calcolaPunti()
                    String puntiStr = Utility.calcolaPunti(prese.get(g));
                    
                    sb.append(g.getNickname()).append("=").append(puntiStr).append(" ");
                }
            }
            
            puntiLabel.setText(sb.toString());
        });
    }
   
    public void aggiornaTurno(int turno) {
        SwingUtilities.invokeLater(() -> {
            if (turnoLabel != null && controller != null && controller.getPartita() != null) {
                Giocatore[] giocatori = controller.getPartita().getGiocatori();
                if (turno >= 0 && turno < giocatori.length) {
                    turnoLabel.setText("Turno di: " + giocatori[turno].getNickname());
                    
                    // AGGIORNA LO STATO DELLE CARTE quando cambia il turno
                    if (controller.getPartita().getGiocatori()[0] != null) {
                        aggiornaMano(controller.getPartita().getGiocatori()[0].getMano(), 
                                    turno == 0, 
                                    controller.getPartita().getTavolo());
                    }
                }
            }
        });
    }

    
 // In GUI.java - modifica propertyChange()
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "turno":
                aggiornaTurno((int) evt.getNewValue());
                break;
            case "mano":
                if (controller != null && controller.getPartita() != null) {
                    aggiornaMano((java.util.List<Carta>) evt.getNewValue(), 
                                controller.getPartita().getTurno() == 0,
                                controller.getPartita().getTavolo());
                }
                break;
            case "cartaPescata":
                // Mostra la carta pescata
                Partita.CartaPescataEvent event = (Partita.CartaPescataEvent) evt.getNewValue();
                mostraCartaPescata(event.giocatore, event.carta);
                break;
        }
    }

    // Aggiungi questo metodo
    private void mostraCartaPescata(Giocatore giocatore, Carta carta) {
        SwingUtilities.invokeLater(() -> {
            String messaggio = "<html><b>" + giocatore.getNickname() + "</b> pesca:<br>" + 
                              carta.getSeme() + " " + carta.getValore() + "</html>";
            
            JOptionPane.showMessageDialog(this, messaggio, "Carta Pescata", 
                                        JOptionPane.INFORMATION_MESSAGE);
            
            // Aggiorna la visualizzazione
            if (controller != null && controller.getPartita() != null) {
                aggiornaMano(controller.getPartita().getGiocatori()[0].getMano(), 
                            controller.getPartita().getTurno() == 0,
                            controller.getPartita().getTavolo());
            }
        });
    }
}