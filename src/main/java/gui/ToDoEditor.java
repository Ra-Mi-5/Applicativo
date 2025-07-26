package gui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.Date;
import java.util.Properties;
import java.time.ZoneId;

import org.jdatepicker.impl.*;

public class ToDoEditor extends JPanel {
    private JTextField titoloField;
    private JTextArea descrizioneArea;
    private JButton coloreButton;
    private JButton bottoneSalva;
    private JPanel colorePanel;
    private JPanel editorPanel;
    private JTextField urlField;
    private JButton annullaButton;
    private JPanel dataPanelPlaceholder;
    private JLabel titoloFinestraLabel;
    private JDialog dialog;
    private JDatePickerImpl datePicker;

    private Color coloreScelto = Color.WHITE;
    private Runnable onSaveCallback;            // Callback da eseguire al salvataggio
    private ToDoPanel toDoInModifica = null;    // Riferimento al ToDo da modificare (se esiste)

    // il  Costruttore configura il pannello in base a modalità modifica o nuovo
    public ToDoEditor(ToDoPanel toDoInModifica, Runnable onSaveCallback, JDialog dialog) {
        this.toDoInModifica = toDoInModifica;
        this.onSaveCallback = onSaveCallback;
        this.dialog = dialog;

        // Imposta titolo della finestra e dell'etichetta in alto
        if (toDoInModifica == null) {
            dialog.setTitle("Nuovo ToDo");
            titoloFinestraLabel.setText("Nuovo ToDo");
        } else {
            dialog.setTitle("Modifica ToDo");
            titoloFinestraLabel.setText("Modifica ToDo");
        }

        // Inizializza DatePicker con opzioni localizzate
        UtilDateModel model = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "Oggi");
        p.put("text.month", "Mese");
        p.put("text.year", "Anno");

        JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
        datePicker = new JDatePickerImpl(datePanel, new DateComponentFormatter());

        // Inserisce il DatePicker nel pannello placeholder
        if (dataPanelPlaceholder != null) {
            dataPanelPlaceholder.setLayout(new BorderLayout());
            dataPanelPlaceholder.add(datePicker, BorderLayout.CENTER);
        }

        coloreButton.addActionListener(e -> {
            Color nuovoColore = JColorChooser.showDialog(this, "Scegli un colore", coloreScelto);
            if (nuovoColore != null) {
                coloreScelto = nuovoColore;
                colorePanel.setBackground(coloreScelto); // aggiorna preview del colore
            }
        });

        bottoneSalva.addActionListener(e -> {
            if (onSaveCallback != null) {
                onSaveCallback.run();
            }
            dialog.dispose();
        });

        annullaButton.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(annullaButton);
            if (window != null) {
                window.dispose();
            }
        });
    }

    public JPanel getPanel() {
        return editorPanel;
    }

    // Getter dei dati inseriti nel form:

    public String getTitolo() {
        return titoloField.getText().trim();
    }

    public String getDescrizione() {
        return descrizioneArea.getText().trim();
    }

    public String getUrl() {
        return urlField.getText().trim();
    }

    public Color getColoreScelto() {
        return coloreScelto;
    }

    public LocalDate getDataScadenza() {
        Date selectedDate = (Date) datePicker.getModel().getValue();
        if (selectedDate != null) {
            return selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        return null;
    }

    // Carica i dati di un ToDo esistente all'interno del form

    public void setToDoData(ToDoPanel todo) {
        this.toDoInModifica = todo;

        // Titolo (se "Nessun titolo", pulisce il campo)
        String titolo = todo.getTitolo();
        if ("Nessun titolo".equals(titolo)) {
            titolo = "";
        }
        titoloField.setText(titolo);

        // Descrizione
        descrizioneArea.setText(todo.getDescrizione());

        // Colore (usa un default se null)
        Color colore = todo.getColoreSfondo();
        if (colore == null) {
            colore = Color.WHITE;
        }
        coloreScelto = colore;
        colorePanel.setBackground(coloreScelto);

        urlField.setText(todo.getUrl());

        // Data di scadenza (se presente, la imposta nel date picker)
        LocalDate data = todo.getDataScadenza();
        UtilDateModel model = (UtilDateModel) datePicker.getModel();
        if (data != null) {
            model.setDate(data.getYear(), data.getMonthValue() - 1, data.getDayOfMonth());
            model.setSelected(true);
        } else {
            model.setSelected(false);
        }
    }
}