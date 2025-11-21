package tressette.view;

import tressette.model.*;
import tressette.controller.GameController;
import tressette.util.AudioManager;
import tressette.util.Utility;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class GUI extends JFrame implements PropertyChangeListener {

    private JPanel manoPanel;
    private JPanel tavoloPanel; 
    private JLabel turnoLabel;
    private JLabel puntiLabel;
    private GameController controller;
    private Image backgroundImage;

    // --- COLORI DEL TEMA ---
    // Bordeaux scuro semitrasparente per i pannelli
    private final Color COLORE_BORDEAUX = new Color(70, 5, 20, 220); 
    // Bordeaux un po' più trasparente per la mano
    private final Color COLORE_MANO = new Color(80, 10, 30, 200);
    // Oro per i bordi
    private final Color COLORE_ORO = new Color(218, 165, 32); 

    public GUI() {
        setTitle("Tressette");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 1. CARICAMENTO SFONDO
        try {
            java.net.URL imgUrl = getClass().getResource("/iu/tavolo.jpg");
            if (imgUrl != null) {
                backgroundImage = javax.imageio.ImageIO.read(imgUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. PANEL INFO (In alto) - Stile Bordeaux
        // Usiamo il PannelloTrasparente con il colore Bordeaux
        JPanel infoPanel = new PannelloTrasparente(COLORE_BORDEAUX); 
        infoPanel.setLayout(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, COLORE_ORO), // Bordo Oro sotto
                new EmptyBorder(10, 20, 10, 20) // Margine interno
        ));

        turnoLabel = new JLabel("Turno: -");
        puntiLabel = new JLabel("Punti: -", SwingConstants.RIGHT);

        turnoLabel.setFont(new Font("Georgia", Font.BOLD, 18)); // Font più elegante
        puntiLabel.setFont(new Font("Georgia", Font.PLAIN, 16));
        
        turnoLabel.setForeground(Color.WHITE);
        puntiLabel.setForeground(Color.WHITE);

        infoPanel.add(turnoLabel, BorderLayout.WEST);
        infoPanel.add(puntiLabel, BorderLayout.EAST);

        // 3. PANEL TAVOLO (Centrale)
        if (backgroundImage != null) {
            tavoloPanel = new BackgroundPanel(backgroundImage);
        } else {
            tavoloPanel = new JPanel();
            tavoloPanel.setBackground(new Color(34, 139, 34)); 
        }
        tavoloPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 4. PANEL MANO (In basso) - Stile Bordeaux
        manoPanel = new PannelloTrasparente(COLORE_MANO); 
        manoPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 15));
        manoPanel.setPreferredSize(new Dimension(800, 190)); 
        // Bordo Oro sopra
        manoPanel.setBorder(BorderFactory.createMatteBorder(3, 0, 0, 0, COLORE_ORO));

        // Assemblaggio
        add(infoPanel, BorderLayout.NORTH);
        add(tavoloPanel, BorderLayout.CENTER);
        add(manoPanel, BorderLayout.SOUTH);

        setSize(1024, 768);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void setController(GameController controller) {
        this.controller = controller;
    }

    // =================================================================================
    // METODI GRAFICI
    // =================================================================================

    public void aggiornaTavolo(List<Carta> tavolo) {
        SwingUtilities.invokeLater(() -> {
            try {
                tavoloPanel.removeAll();
                
                if (controller == null || controller.getPartita() == null) return;
                
                int numGiocatori = controller.getPartita().getGiocatori().length;
                tavoloPanel.setLayout(new GridLayout(1, numGiocatori, 30, 0)); 
                
                Giocatore[] giocatori = controller.getPartita().getGiocatori();
                int primoDiMano = controller.getPartita().getPrimoDiMano();
                
                for (int i = 0; i < numGiocatori; i++) {
                    Giocatore giocatore = giocatori[i];
                    int indexCartaNelTavolo = (i - primoDiMano + numGiocatori) % numGiocatori;
                    
                    Carta cartaGiocata = null;
                    if (tavolo != null && indexCartaNelTavolo < tavolo.size()) {
                        cartaGiocata = tavolo.get(indexCartaNelTavolo);
                    }
                    
                    // Slot Visivo
                    JPanel slotPanel = new JPanel(new BorderLayout());
                    slotPanel.setOpaque(false);
                    slotPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

                    // Header
                    JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                    headerPanel.setOpaque(false);
                    
                    ImageIcon avatarIcon = giocatore.getAvatarImage();
                    if (avatarIcon != null) {
                        JLabel iconLabel = new JLabel(Utility.scalaImmagine(avatarIcon, 55, 55));
                        // Bordo Oro intorno all'avatar
                        iconLabel.setBorder(BorderFactory.createLineBorder(COLORE_ORO, 2));
                        headerPanel.add(iconLabel);
                    }
                    
                    JLabel nameLabel = new JLabel(giocatore.getNickname());
                    nameLabel.setFont(new Font("Georgia", Font.BOLD, 14));
                    
                    // Colori: Oro per Umano, Bianco per CPU (più leggibile su verde)
                    if (i == 0) nameLabel.setForeground(new Color(255, 215, 0)); 
                    else nameLabel.setForeground(Color.WHITE); 
                    
                    nameLabel.setText("<html><span style='text-shadow: 2px 2px #000000;'>" + giocatore.getNickname() + "</span></html>");
                    headerPanel.add(nameLabel);

                    // Carta
                    JLabel cardLabel = new JLabel();
                    cardLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    cardLabel.setPreferredSize(new Dimension(100, 150)); 

                    if (cartaGiocata != null) {
                        ImageIcon iconaCarta = Utility.scalaImmagine(cartaGiocata.getImmagine(), 100, 150);
                        if (iconaCarta != null) cardLabel.setIcon(iconaCarta);
                        else cardLabel.setText(cartaGiocata.toString());
                        // Bordo sottile bianco alla carta
                        cardLabel.setBorder(BorderFactory.createLineBorder(new Color(255,255,255,200), 1));
                    } else {
                        // Slot vuoto: bordo tratteggiato oro scuro
                        cardLabel.setBorder(BorderFactory.createDashedBorder(new Color(218, 165, 32, 100), 2, 5, 5, false));
                    }

                    slotPanel.add(headerPanel, BorderLayout.NORTH);
                    slotPanel.add(cardLabel, BorderLayout.CENTER);
                    tavoloPanel.add(slotPanel);
                }
                
                tavoloPanel.revalidate();
                tavoloPanel.repaint();
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    public void aggiornaMano(List<Carta> mano, boolean turnoUmano, List<Carta> tavolo) {
        SwingUtilities.invokeLater(() -> {
            try {
                manoPanel.removeAll();
                if (mano == null || mano.isEmpty()) {
                    manoPanel.revalidate(); manoPanel.repaint(); return;
                }
                
                // 1. ANALISI DEL TAVOLO
                boolean obbligoSeme = false;
                String semeObbligatorio = null;
                boolean hoIlSeme = false; // Indica se il giocatore possiede il seme di risposta
                
                if (tavolo != null && !tavolo.isEmpty()) {
                    obbligoSeme = true;
                    semeObbligatorio = tavolo.get(0).getSeme();
                    
                    // Controlliamo se il giocatore ha ALMENO UNA carta di quel seme
                    String finalSeme = semeObbligatorio;
                    hoIlSeme = mano.stream().anyMatch(c -> c.getSeme().equals(finalSeme));
                    
                    // DEBUG: Stampiamo in console per verifica
                    // System.out.println("Seme guida: " + finalSeme + " | Ho il seme? " + hoIlSeme);
                }
                
                List<Carta> manoCopy = new ArrayList<>(mano);
                for (Carta carta : manoCopy) {
                    if (carta == null) continue;
                    
                    JButton cardButton = new JButton();
                    cardButton.setContentAreaFilled(false);
                    cardButton.setFocusPainted(false);
                    cardButton.setBorder(BorderFactory.createEmptyBorder());
                    
                    ImageIcon originalIcon = carta.getImmagine();
                    if (originalIcon != null) {
                        cardButton.setIcon(Utility.scalaImmagine(originalIcon, 90, 135));
                    } else { cardButton.setText(carta.toString()); }
                    
                    cardButton.setPreferredSize(new Dimension(90, 135));
                    
                    // 2. LOGICA REGOLE TRESSETTE (CORRETTA)
                    boolean cartaGiocabile = false;

                    if (turnoUmano) {
                        if (!obbligoSeme) {
                            // CASO A: Sono il primo di mano (tavolo vuoto)
                            // Posso giocare qualsiasi carta
                            cartaGiocabile = true;
                        } else {
                            // CASO B: Devo rispondere a qualcuno
                            if (hoIlSeme) {
                                // Ho il seme: Posso giocare SOLO le carte di quel seme
                                if (carta.getSeme().equals(semeObbligatorio)) {
                                    cartaGiocabile = true;
                                }
                            } else {
                                // CASO C (PIOMBO): Non ho il seme richiesto
                                // Posso giocare QUALSIASI carta (liberi tutti)
                                cartaGiocabile = true;
                            }
                        }
                    } 
                    // Se non è il mio turno, cartaGiocabile resta false
                    
                    if (cartaGiocabile) {
                        cardButton.setEnabled(true);
                        // Animazione Hover
                        cardButton.addMouseListener(new java.awt.event.MouseAdapter() {
                            public void mouseEntered(java.awt.event.MouseEvent evt) {
                                if (cardButton.isEnabled()) {
                                    cardButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
                                    cardButton.setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 3));
                                    cardButton.setLocation(cardButton.getX(), cardButton.getY() - 20); 
                                }
                            }
                            public void mouseExited(java.awt.event.MouseEvent evt) {
                                cardButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                                cardButton.setBorder(BorderFactory.createEmptyBorder());
                                cardButton.setLocation(cardButton.getX(), cardButton.getY() + 20);
                            }
                        });
                    } else {
                        cardButton.setEnabled(false);
                    }
                    
                    cardButton.addActionListener(e -> {
                        if (controller != null && cardButton.isEnabled()) {
                            controller.giocatoreGiocaCarta(carta);
                        }
                    });
                    cardButton.setToolTipText(carta.toString());
                    manoPanel.add(cardButton);
                }
                manoPanel.revalidate(); manoPanel.repaint();
            } catch (Exception e) { e.printStackTrace(); }
        });
    }
    public void aggiornaPunti(java.util.Map<Giocatore, List<Carta>> prese) {
        SwingUtilities.invokeLater(() -> {
            StringBuilder sb = new StringBuilder("<html><span style='font-size:14px'>");
            
            if (prese == null || prese.isEmpty()) {
                puntiLabel.setText("Punti: -"); return;
            }
            
            if (controller != null && controller.getPartita() != null) {
                Giocatore[] giocatori = controller.getPartita().getGiocatori();
                for (int i = 0; i < giocatori.length; i++) {
                    Giocatore g = giocatori[i];
                    if (!prese.containsKey(g)) continue;
                    
                    String puntiStr = Utility.calcolaPunti(prese.get(g));
                    // Colore Oro per Umano, Rosso Pastello per CPU
                    String color = (i == 0) ? "#FFD700" : "#FF9999";
                    
                    sb.append("<font color='").append(color).append("'>")
                      .append(g.getNickname()).append("</font>: ").append(puntiStr);
                    
                    if (i < giocatori.length - 1) sb.append("  <font color='white'>|</font>  ");
                }
                sb.append("</span></html>");
            }
            puntiLabel.setText(sb.toString());
        });
    }

    public void aggiornaTurno(int turno) {
        SwingUtilities.invokeLater(() -> {
            if (controller != null && controller.getPartita() != null) {
                Giocatore[] giocatori = controller.getPartita().getGiocatori();
                if (turno >= 0 && turno < giocatori.length) {
                    String nick = giocatori[turno].getNickname();
                    String color = (turno == 0) ? "#FFD700" : "#FF9999";
                    turnoLabel.setText("<html>Turno di: <font color='" + color + "'>" + nick + "</font></html>");
                }
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        SwingUtilities.invokeLater(() -> {
            switch (evt.getPropertyName()) {
                case "turno":
                    aggiornaTurno((int) evt.getNewValue());
                    break;
                case "tavolo":
                    aggiornaTavolo((List<Carta>) evt.getNewValue());
                    if (controller != null && controller.getPartita() != null) {
                         Giocatore umano = controller.getPartita().getGiocatori()[0];
                         aggiornaMano(umano.getMano(), controller.getPartita().getTurno() == 0, (List<Carta>) evt.getNewValue());
                    }
                    break;
                case "giocata":
                    AudioManager.play("/audio/flip.wav");
                    if (controller != null && controller.getPartita() != null) {
                        Giocatore umano = controller.getPartita().getGiocatori()[0];
                        aggiornaMano(umano.getMano(), controller.getPartita().getTurno() == 0, controller.getPartita().getTavolo());
                    }
                    break;
                case "punti":
                    AudioManager.play("/audio/punti.wav");
                    aggiornaPunti((java.util.Map<Giocatore, List<Carta>>) evt.getNewValue());
                    break;
                case "cartaPescata":
                    AudioManager.play("/audio/flip.wav");
                    Partita.CartaPescataEvent event = (Partita.CartaPescataEvent) evt.getNewValue();
                    if (event.giocatore == controller.getPartita().getGiocatori()[0]) {
                         JOptionPane.showMessageDialog(this, "Hai pescato: " + event.carta.toString(), 
                                 "Pescaggio", JOptionPane.INFORMATION_MESSAGE, event.carta.getImmagine());
                    }
                    break;
                case "accusa":
                    Partita.AccusaEvent accEvent = (Partita.AccusaEvent) evt.getNewValue();
                    JOptionPane.showMessageDialog(this, 
                            accEvent.giocatore.getNickname() + " dichiara: " + accEvent.accusa, 
                            "Accusa!", JOptionPane.INFORMATION_MESSAGE);
                    break;
            }
        });
    }

    // --- CLASS INTERNA PER LO SFONDO ---
    private class BackgroundPanel extends JPanel {
        private Image backgroundImage;
        public BackgroundPanel(Image image) {
            this.backgroundImage = image;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    // --- CLASSE INTERNA PER LA TRASPARENZA COLORATA ---
    private class PannelloTrasparente extends JPanel {
        private Color coloreSfondo;

        public PannelloTrasparente(Color colore) {
            this.coloreSfondo = colore;
            setOpaque(false); 
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(coloreSfondo);
            g.fillRect(0, 0, getWidth(), getHeight());
            super.paintComponent(g);
        }
    }
}