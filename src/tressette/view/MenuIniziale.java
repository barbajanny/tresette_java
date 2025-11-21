package tressette.view;

import tressette.model.Giocatore;
import tressette.util.UserManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MenuIniziale extends JDialog {

    private Giocatore giocatoreSelezionato = null;
    private boolean nuovoUtenteSelezionato = false;
    private JTable tabellaProfili;
    private DefaultTableModel tableModel;
    private List<Giocatore> profiliCaricati;

    public MenuIniziale(JFrame parent) {
        super(parent, "Benvenuto a Tressette", true); // true = modale (blocca il resto)
        setSize(600, 450);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- HEADER ---
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(70, 5, 20)); // Bordeaux tema
        JLabel titolo = new JLabel("Scegli il tuo Profilo");
        titolo.setFont(new Font("Georgia", Font.BOLD, 24));
        titolo.setForeground(new Color(218, 165, 32)); // Oro
        headerPanel.add(titolo);
        add(headerPanel, BorderLayout.NORTH);

        // --- TABELLA ---
        String[] colonne = {"Nickname", "Livello", "Vinte", "Perse", "Totali"};
        tableModel = new DefaultTableModel(colonne, 0) {
            @Override // Rende la tabella non modificabile
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tabellaProfili = new JTable(tableModel);
        tabellaProfili.setRowHeight(25);
        tabellaProfili.setFont(new Font("Arial", Font.PLAIN, 14));
        tabellaProfili.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Carica i dati
        caricaDatiInTabella();

        JScrollPane scrollPane = new JScrollPane(tabellaProfili);
        add(scrollPane, BorderLayout.CENTER);

        // --- BOTTONI ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBackground(Color.LIGHT_GRAY);

        JButton btnGioca = new JButton("Carica e Gioca");
        JButton btnNuovo = new JButton("Nuovo Utente");
        JButton btnEsci = new JButton("Esci");

        styleButton(btnGioca, new Color(34, 139, 34)); // Verde
        styleButton(btnNuovo, new Color(70, 130, 180)); // Blu
        styleButton(btnEsci, Color.RED);

        // AZIONE GIOCA
        btnGioca.addActionListener(e -> {
            int row = tabellaProfili.getSelectedRow();
            if (row >= 0) {
                giocatoreSelezionato = profiliCaricati.get(row);
                dispose(); // Chiude la finestra
            } else {
                JOptionPane.showMessageDialog(this, "Seleziona un profilo dalla tabella!", "Attenzione", JOptionPane.WARNING_MESSAGE);
            }
        });

        // AZIONE NUOVO
        btnNuovo.addActionListener(e -> {
            nuovoUtenteSelezionato = true;
            dispose();
        });

        // AZIONE ESCI
        btnEsci.addActionListener(e -> System.exit(0));

        btnPanel.add(btnGioca);
        btnPanel.add(btnNuovo);
        btnPanel.add(btnEsci);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void caricaDatiInTabella() {
        profiliCaricati = UserManager.caricaTuttiIProfili();
        tableModel.setRowCount(0); // Pulisce tabella
        
        for (Giocatore g : profiliCaricati) {
            Object[] riga = {
                g.getNickname(),
                "Lv " + g.getLivello(),
                g.getPartiteVinte(),
                g.getPartitePerse(),
                g.getPartiteGiocate()
            };
            tableModel.addRow(riga);
        }
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
    }

    public Giocatore getGiocatoreSelezionato() {
        return giocatoreSelezionato;
    }

    public boolean isNuovoUtenteSelezionato() {
        return nuovoUtenteSelezionato;
    }
}