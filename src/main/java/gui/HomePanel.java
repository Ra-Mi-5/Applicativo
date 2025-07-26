package gui;

import controller.Controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HomePanel extends JPanel {
    private JPanel mainPanel;
    private JButton InfoUtenteButton;
    private JButton logOutButton;
    private JLabel logoLabel;
    private JPanel univrsitaPanel;
    private JLabel universitaDescrizione;
    private JButton visualizzaToDoButton;
    private JPanel lavoroPanel;
    private JLabel descrizioneLavoro;
    private JButton visualizzaToDoButton1;
    private JPanel tempoLiberoPanel;
    private JLabel descrizioneTempoLibero;
    private JButton visualizzaToDoButton2;
    private JTextField searchField;
    private JButton btnSearch;
    private JTextField dateField;
    private JToolBar tool1;
    private JToolBar tool2;
    private JButton modificaDescrizioneButton;
    private JButton modificaDescrizioneButton1;
    private JButton modificaDescrizioneButton2;
    private JButton nuovoToDoButton;
    private JButton nuovoToDoButton1;
    private JButton nuovoToDoButton2;
    private JButton btnScadenzaOggi;

    private final JFrame mainFrame;         // Riferimento al frame principale dell'app
    private final Controller controller;    // Controller centrale

    private JFrame ricercaFrame;            // Finestra dei risultati di ricerca

    // Costruttore: inizializza componenti e listener
    public HomePanel(JFrame mainFrame, Controller controller) {
        this.mainFrame = mainFrame;
        this.controller = controller;

        visualizzaToDoButton.addActionListener(e -> controller.mostraToDoList("Università"));
        visualizzaToDoButton1.addActionListener(e -> controller.mostraToDoList("Lavoro"));
        visualizzaToDoButton2.addActionListener(e -> controller.mostraToDoList("Tempo_Libero"));

        modificaDescrizioneButton.addActionListener(e -> modificaDescrizione("Università", universitaDescrizione));
        modificaDescrizioneButton1.addActionListener(e -> modificaDescrizione("Lavoro", descrizioneLavoro));
        modificaDescrizioneButton2.addActionListener(e -> modificaDescrizione("Tempo_Libero", descrizioneTempoLibero));

        nuovoToDoButton.addActionListener(e -> controller.apriEditor("Università", null));
        nuovoToDoButton1.addActionListener(e -> controller.apriEditor("Lavoro", null));
        nuovoToDoButton2.addActionListener(e -> controller.apriEditor("Tempo_Libero", null));


        btnSearch.addActionListener(e -> mostraRisultatiRicerca(controller.cercaToDo(searchField.getText(), Data())));
        btnScadenzaOggi.addActionListener(e -> {
            String username = controller.getUsernameCorrente();
            List<ToDoPanel> risultati = controller.getToDoScadenzaOggi(username);
            mostraRisultatiRicerca(risultati);
        });


        InfoUtenteButton.addActionListener(e -> {
            String username = SessionManager.getLoggedUsername();
            JOptionPane.showMessageDialog(mainPanel, username != null ? "Utente Loggato: " + username : "Nessun Utente Loggato");
        });

        // LogOut: svuota la sessione e torna alla schermata di login
        logOutButton.addActionListener(e -> {
            SessionManager.clearSession();
            LoginPanel loginPanel = new LoginPanel(mainFrame, controller);
            mainFrame.setContentPane(loginPanel.getMainPanel());
            mainFrame.revalidate();
            mainFrame.repaint();
        });


    }

    // Metodo per ottenere la data inserita dall'utente
    private LocalDate Data() {
        try {
            if (!dateField.getText().isEmpty()) {
                return LocalDate.parse(dateField.getText(), DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainPanel, "Data non valida. Usa formato dd-MM-yyyy.", "Errore", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    // Mostra i risultati di ricerca in una nuova finestra con layout verticale
    private void mostraRisultatiRicerca(List<ToDoPanel> risultati) {
        if (risultati.isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, "Nessun ToDo trovato.");
            return;
        }

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);

        JLabel messaggio = new JLabel("Click sul Titolo per Visualizzare il ToDo in Bacheca con Maggiori Info");
        messaggio.setHorizontalAlignment(SwingConstants.CENTER);
        messaggio.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        messaggio.setFont(new Font("SansSerif", Font.BOLD, 16));
        messaggio.setForeground(Color.RED);
        container.add(messaggio, BorderLayout.NORTH);

        // Pannello centrale contenente i risultati
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);

        // Per ogni ToDo, crea un'anteprima cliccabile
        for (ToDoPanel todoPanel : risultati) {
            FiltriPanel preview = new FiltriPanel(
                    todoPanel.getTitolo(),
                    todoPanel.getDataScadenza(),
                    todoPanel.getDescrizione(),
                    todoPanel.getColoreSfondo()
            );

            preview.setBackground(Color.WHITE);
            preview.setMaximumSize(new Dimension(850, 200));

            // al click si apre il ToDo selezionato in bacheca
            preview.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (ricercaFrame != null) {
                        ricercaFrame.dispose();
                    }
                    controller.apriBachecaConToDoSelezionato(todoPanel.getCategoria(), todoPanel);
                    controller.getMainFrame().toFront();
                    controller.getMainFrame().requestFocus();
                }
            });

            panel.add(preview);
            panel.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        // Scroll dei risultati
        JScrollPane scrollPane = new JScrollPane(panel);
        container.add(scrollPane, BorderLayout.CENTER);

        // Finestra dei risultati di ricerca
        ricercaFrame = new JFrame("Risultati ricerca");
        ricercaFrame.setContentPane(container);
        ricercaFrame.setSize(900, 600);
        ricercaFrame.setLocationRelativeTo(mainPanel);
        ricercaFrame.setVisible(true);
    }

    // Permette di modificare dinamicamente la descrizione di una categoria
    private void modificaDescrizione(String categoria, JLabel label) {
        String nuova = JOptionPane.showInputDialog(mainPanel, "Inserisci nuova descrizione per " + categoria + ":", label.getText());
        if (nuova != null && !nuova.trim().isEmpty()) {
            controller.modificaDescrizione(controller.getUsernameCorrente(), categoria, nuova);
            label.setText(nuova);
        }
    }

    // Getter per il pannello principale
    public JPanel getMainPanel() {
        return mainPanel;
    }
}