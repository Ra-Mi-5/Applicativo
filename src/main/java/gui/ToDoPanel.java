package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.Timer;

public class ToDoPanel extends JPanel {
    private JPanel mainPanel;
    public JLabel titoloLabel;
    private JLabel dataScadenzaLabel;
    public JCheckBox completatoCheckBox;
    private JButton modificaButton;
    private JButton eliminaButton;
    private JComboBox<String> spostaComboBox;
    private JButton suButton;
    private JButton giuButton;
    private JButton condivisioniButton;
    private JTextArea descrizioneTextArea;
    private JLabel urlLabel;

    // CAMPI DATI DEL TODO
    public String titolo;
    public Color coloreSfondo;
    public LocalDate dataScadenza;
    public String descrizione;
    public String url;
    public ImageIcon immagine;
    public String[] condivisoCon;
    private String categoria;
    List<String> tutteLeCategorie;
    private String utenteCorrente;
    private String autoreDelToDo;

    private boolean suppressComboEvent = false;
    private int id;

    /*
      Questa interfaccia è pensata per permettere alla view (`ToDoPanel`) di restare riutilizzabile
      e disaccoppiata dalla logica del Controller.
      Ogni metodo rappresenta un'azione che può essere eseguita su un ToDo.

      È il Controller a implementare questa interfaccia, così può decidere il comportamento concreto
      per ciascuna azione, mantenendo la separazione tra interfaccia grafica e logica applicativa.
     */
    public interface ToDoActionListener {
        void onModifica(ToDoPanel panel);
        void onElimina(ToDoPanel panel);
        void onSpostaBacheca(ToDoPanel panel, String nuovaCategoria);
        void onApriCondivisioni(ToDoPanel panel);
        void onToggleCompletato(ToDoPanel panel, boolean completato);
    }

    /*
     È utile per astrarre il comportamento di riordinamento della lista dal punto di vista del
     singolo `ToDoPanel`, lasciando al Controller il compito di modificare effettivamente la
     struttura dati sottostante (es. `List<ToDoPanel>`).
     In pratica, quando un utente clicca per spostare un ToDo su o giù, il `ToDoPanel` chiama questo
     listener.
   */
    public interface SpostamentoListener {
        void onSposta(ToDoPanel panel, int direzione); // -1 = su, 1 = giù
    }

    public ToDoActionListener toDoActionListener;
    public SpostamentoListener spostamentoListener;

    public ToDoPanel(
            String titolo,
            String descrizione,
            LocalDate dataScadenza,
            Color colore,
            String url,
            boolean completato,
            String autore,
            String categoria,
            List<String> tutteLeCategorie,
            ToDoActionListener actionListener,
            SpostamentoListener spostamentoListener) {

        this.titolo = titolo;
        this.descrizione = descrizione;
        this.dataScadenza = dataScadenza;
        this.coloreSfondo = colore;
        this.url = url;
        this.autoreDelToDo = autore;
        this.categoria = categoria;
        this.tutteLeCategorie = tutteLeCategorie;
        this.toDoActionListener = actionListener;
        this.spostamentoListener = spostamentoListener;

        initComponents();  // PRIMA inizializzi i componenti GUI

        // Ora puoi usarli senza errori
        completatoCheckBox.setSelected(completato);
        setCategoria(categoria);

        setTitolo(titolo);
        setDescrizione(descrizione);
        setUrl(url);
        setColoreSfondo(colore);
        setDataScadenza(dataScadenza);
        setAutore(autore);

        initListeners();
    }



    private void initComponents() {
        this.setLayout(new BorderLayout());
        this.add(mainPanel, BorderLayout.CENTER);

        // Imposta le opzioni della combo box per cambiare bacheca
        spostaComboBox.setModel(new DefaultComboBoxModel<>(tutteLeCategorie.toArray(new String[0])));
        spostaComboBox.setSelectedItem(categoria);

        // Imposta i colori iniziali
        if (mainPanel != null) mainPanel.setBackground(coloreSfondo);
        if (descrizioneTextArea != null) descrizioneTextArea.setBackground(coloreSfondo);
    }

    private void initListeners() {
        modificaButton.addActionListener(e -> {
            if (toDoActionListener != null) toDoActionListener.onModifica(this);
        });

        eliminaButton.addActionListener(e -> {
            if (toDoActionListener != null) toDoActionListener.onElimina(this);
        });

        completatoCheckBox.addActionListener(e -> {
            if (toDoActionListener != null) {
                toDoActionListener.onToggleCompletato(this, completatoCheckBox.isSelected());
            }
        });

        suButton.addActionListener(e -> {
            if (spostamentoListener != null) spostamentoListener.onSposta(this, -1);
        });

        giuButton.addActionListener(e -> {
            if (spostamentoListener != null) spostamentoListener.onSposta(this, 1);
        });

        spostaComboBox.addActionListener(e -> {
            if (suppressComboEvent) return;

            String nuovaCategoria = (String) spostaComboBox.getSelectedItem();

            if (toDoActionListener != null && categoria != null && !categoria.equals(nuovaCategoria)) {
                toDoActionListener.onSpostaBacheca(this, nuovaCategoria);

            }
        });

        condivisioniButton.addActionListener(e -> {
            if (toDoActionListener != null) toDoActionListener.onApriCondivisioni(this);
        });
    }

    private void aggiornaTitolo() {
        if (dataScadenza != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ITALIAN);
            dataScadenzaLabel.setText("Scadenza: " + formatter.format(dataScadenza));
            boolean scaduto = dataScadenza.isBefore(LocalDate.now());

            Color coloreTesto = isColoreScuro(coloreSfondo) ? Color.WHITE : Color.BLACK;
            Color rossoChiaro = new Color(255, 102, 102);
            Color coloreTitolo = scaduto ? (isColoreScuro(coloreSfondo) ? rossoChiaro : Color.RED) : coloreTesto;

            dataScadenzaLabel.setForeground(coloreTesto);
            titoloLabel.setForeground(coloreTitolo);
        } else {
            dataScadenzaLabel.setText("Nessuna Scadenza");
            Color coloreTesto = isColoreScuro(coloreSfondo) ? Color.WHITE : Color.BLACK;
            dataScadenzaLabel.setForeground(coloreTesto);
            titoloLabel.setForeground(coloreTesto);
        }
    }

    public void startBorderAnimation() {
        Color rosso = Color.RED;

        Timer timer = new Timer(100, null);
        timer.addActionListener(e -> {
            this.setBorder(BorderFactory.createLineBorder(rosso, 3));
            this.repaint();
        });
        timer.start();

        new Timer(3000, ev -> {
            timer.stop();
            this.setBorder(null);
            this.repaint();
            ((Timer) ev.getSource()).stop();
        }).start();
    }

    // --- GETTER e SETTER usati da controller ---

    public String getCategoria() {
        return categoria;
    }

    public String getTitolo() {
        return titoloLabel.getText();
    }

    public LocalDate getDataScadenza() {
        return dataScadenza;
    }

    public String getDescrizione() {
        return descrizione;
    }
    public String getUrl() {
        return url;
    }

    public boolean isCompletato() {
        return completatoCheckBox.isSelected();
    }
    public String[] getCondivisoCon() {
        return condivisoCon;
    }

    public Color getColoreSfondo() {
        return coloreSfondo;
    }

    public String getAutore() {
        return autoreDelToDo;
    }

    public void setAutore(String autore) {
        this.autoreDelToDo = autore;
    }


    public void setCategoria(String categoria) {
        this.categoria = categoria;

        if (spostaComboBox != null) {
            suppressComboEvent = true; // Evita che il cambio scateni l'actionListener
            spostaComboBox.setSelectedItem(categoria);
            suppressComboEvent = false;
        }
    }

    public void setTitolo(String titolo) {
        this.titoloLabel.setText(titolo == null || titolo.isBlank() ? "Nessun titolo" : titolo);
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
        this.descrizioneTextArea.setText(descrizione == null || descrizione.isBlank() ? "Nessuna descrizione" : descrizione);
    }

    public void setDataScadenza(LocalDate data) {
        this.dataScadenza = data;
        aggiornaTitolo();
    }

    //Imposta il colore di sfondo e aggiorna il colore del testo di conseguenza.

    public void setColoreSfondo(Color colore) {
        this.coloreSfondo = colore;

        if (mainPanel != null) mainPanel.setBackground(colore);
        if (descrizioneTextArea != null) descrizioneTextArea.setBackground(colore);

        Color testoColor = isColoreScuro(colore) ? Color.WHITE : Color.BLACK;
        titoloLabel.setForeground(testoColor);
        dataScadenzaLabel.setForeground(testoColor);
        descrizioneTextArea.setForeground(testoColor);

        if (url != null && !url.isBlank()) {
            urlLabel.setForeground(isColoreScuro(colore) ? Color.WHITE : Color.BLUE.darker());
        }

        aggiornaTitolo();
    }

    private boolean isColoreScuro(Color c) {
        double lum = (0.2126 * c.getRed() + 0.7152 * c.getGreen() + 0.0722 * c.getBlue()) / 255;
        return lum < 0.5;
    }

    public void setUrl(String url) {
        this.url = url;
        if (url == null || url.isBlank()) {
            urlLabel.setText("Nessun URL");
            urlLabel.setCursor(Cursor.getDefaultCursor());
            return;
        }

        urlLabel.setText("<html><a href='" + url + "'>" + url + "</a></html>");
        urlLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        for (MouseListener ml : urlLabel.getMouseListeners()) {
            urlLabel.removeMouseListener(ml);
        }

        urlLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new java.net.URI(url));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Errore apertura URL: " + ex.getMessage());
                }
            }
        });
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public void setCondivisoCon(String[] condivisoCon) {
        this.condivisoCon = condivisoCon;
    }

    public void setToDoActionListener(ToDoActionListener listener) {
        this.toDoActionListener = listener;
    }

    public void setSpostamentoListener(SpostamentoListener listener) {
        this.spostamentoListener = listener;
    }

}