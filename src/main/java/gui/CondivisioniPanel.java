package gui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CondivisioniPanel {
    private JPanel mainPanel;
    private JLabel titoloLabel;
    private JLabel autoreLabel;
    private JList<String> listCondivisi;
    private JButton rimuoviSelezionatiButton;
    private JTextField cercaUtente;
    private JList<String> listDisponibili;
    private JButton aggiungiSelezionatiButton;
    private JButton chiudiButton;
    private JLabel erroreLabel;
    private JScrollPane scrollPaneDisponibili;
    private JScrollPane scrollPaneCondivisi;

    // Modelli per le liste di utenti condivisi e disponibili
    private DefaultListModel<String> modelCondivisi = new DefaultListModel<>();
    private DefaultListModel<String> modelDisponibili = new DefaultListModel<>();

    // Variabili per logica di gestione
    private String autoreToDo;
    private Set<String> condivisi;
    private Set<String> tuttiUtenti;
    private String utenteCorrente;

    public CondivisioniPanel(String titolo, String autore, String utenteCorrente,
                             List<String> condivisiIniziali, List<String> disponibiliIniziali) {
        this.autoreToDo = autore;
        this.utenteCorrente = utenteCorrente;
        this.condivisi = new HashSet<>(condivisiIniziali);

        // Unione di utenti disponibili, condivisi e autore in un unico set
        this.tuttiUtenti = new HashSet<>(disponibiliIniziali);
        this.tuttiUtenti.addAll(condivisiIniziali);
        this.tuttiUtenti.add(autore);

        // Aggiornamento etichette
        titoloLabel.setText("Condivisioni per: " + titolo);
        autoreLabel.setText("Autore: " + autoreToDo);

        // Popola le liste iniziali
        aggiornaListe();

        // Se l'utente NON è l'autore, disabilita le funzionalità di modifica
        if (!utenteCorrente.equals(autoreToDo)) {
            aggiungiSelezionatiButton.setEnabled(false);
            rimuoviSelezionatiButton.setEnabled(false);
            cercaUtente.setEnabled(false);
            erroreLabel.setForeground(Color.RED);
            erroreLabel.setText("Non sei l'autore. Non puoi modificare le Condivisioni.");
        } else {
            erroreLabel.setText("");
        }

        // Listener per aggiungere utenti selezionati alla lista di condivisioni
        aggiungiSelezionatiButton.addActionListener(e -> {
            List<String> selezionati = listDisponibili.getSelectedValuesList();
            condivisi.addAll(selezionati);
            aggiornaListe();
        });

        // Listener per rimuovere utenti selezionati dalla lista di condivisioni
        rimuoviSelezionatiButton.addActionListener(e -> {
            List<String> selezionati = listCondivisi.getSelectedValuesList();
            condivisi.removeAll(selezionati);
            aggiornaListe();
        });

        // Listener per filtrare la lista degli utenti disponibili in base al testo
        cercaUtente.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filtraDisponibili(); }
            @Override public void removeUpdate(DocumentEvent e) { filtraDisponibili(); }
            @Override public void changedUpdate(DocumentEvent e) { filtraDisponibili(); }
        });
        chiudiButton.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(mainPanel);
            if (window != null) window.dispose();
        });
    }

    // Metodo per aggiornare le due liste (condivisi e disponibili)
    private void aggiornaListe() {
        modelCondivisi.clear();
        modelDisponibili.clear();

        // Popola il modello della lista condivisi
        for (String user : condivisi) {
            modelCondivisi.addElement(user);
        }

        // Calcola gli utenti disponibili rimuovendo i già condivisi e l'autore
        Set<String> disponibili = new HashSet<>(tuttiUtenti);
        disponibili.removeAll(condivisi);
        disponibili.remove(autoreToDo);

        // Popola il modello della lista disponibili
        for (String user : disponibili) {
            modelDisponibili.addElement(user);
        }

        // Imposta i modelli alle liste visive
        listCondivisi.setModel(modelCondivisi);
        listDisponibili.setModel(modelDisponibili);

        // Imposta l’altezza visibile dinamicamente in base agli elementi presenti
        int maxVisibleRows = 10;
        scrollPaneCondivisi.setPreferredSize(new Dimension(scrollPaneCondivisi.getWidth(), listCondivisi.getFixedCellHeight() * Math.min(modelCondivisi.size(), maxVisibleRows) + 5));
        scrollPaneDisponibili.setPreferredSize(new Dimension(scrollPaneDisponibili.getWidth(), listDisponibili.getFixedCellHeight() * Math.min(modelDisponibili.size(), maxVisibleRows) + 5));


        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // Metodo per filtrare la lista degli utenti disponibili in base al testo di ricerca
    private void filtraDisponibili() {
        String filtro = cercaUtente.getText().toLowerCase();
        modelDisponibili.clear();

        Set<String> disponibili = new HashSet<>(tuttiUtenti);
        disponibili.removeAll(condivisi);
        disponibili.remove(autoreToDo);

        // Applica filtro
        List<String> filtrati = disponibili.stream()
                .filter(u -> u.toLowerCase().contains(filtro))
                .collect(Collectors.toList());

        for (String user : filtrati) {
            modelDisponibili.addElement(user);
        }
    }

    // Getter per ottenere il pannello principale
    public JPanel getMainPanel() {
        return mainPanel;
    }

    // Getter per ottenere l'insieme aggiornato degli utenti con cui è condiviso il ToDo
    public Set<String> getUtentiCondivisiAggiornati() {
        return new HashSet<>(condivisi);
    }

    // Classe interna: Dialog che mostra il pannello di condivisioni in una finestra modale

    public static class CondivisioniDialog extends JDialog {
        private CondivisioniPanel panel;

        public CondivisioniDialog(Frame owner, String titolo, String autore, String utenteCorrente,
                                  List<String> condivisi, List<String> disponibili) {
            super(owner, "Gestione Condivisioni", true); // true = modale
            panel = new CondivisioniPanel(titolo, autore, utenteCorrente, condivisi, disponibili);
            setContentPane(panel.getMainPanel());
            pack();
            setLocationRelativeTo(owner);
        }

        // Metodo per ottenere le condivisioni aggiornate dopo eventuali modifiche
        public Set<String> getUtentiCondivisiAggiornati() {
            return panel.getUtentiCondivisiAggiornati();
        }
    }
}
