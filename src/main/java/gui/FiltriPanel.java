package gui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FiltriPanel extends JPanel {
    private JPanel mainPanel;
    private JTextArea descrizioneTextArea;
    private JLabel titoloLabel;
    private JLabel scadenzaLabel;

    public FiltriPanel(String titolo, LocalDate scadenza, String descrizione, Color sfondo) {
        // Imposta il layout del pannello principale e aggiunge il contenuto interno
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

        // Imposta il colore di sfondo per il pannello principale e i suoi componenti
        setBackground(sfondo);
        mainPanel.setBackground(sfondo);
        descrizioneTextArea.setBackground(sfondo);

        // Imposta il testo del titolo, con fallback se nullo
        titoloLabel.setText(titolo != null ? titolo : "Nessun titolo");

        // Calcola se il colore di sfondo è scuro o chiaro, per scegliere un colore del testo leggibile
        Color testoNormale = isColoreScuro(sfondo) ? Color.WHITE : Color.BLACK;

        // Gestione della descrizione: se nulla o vuota, mostra testo "nessuna descrizione"
        if (descrizione == null || descrizione.isBlank()) {
            descrizioneTextArea.setText("Nessuna descrizione");
            descrizioneTextArea.setFont(descrizioneTextArea.getFont().deriveFont(Font.ITALIC));
            descrizioneTextArea.setForeground(Color.GRAY);
        } else {
            // Se esiste, mostra la descrizione in formato normale
            descrizioneTextArea.setText("Descrizione:\n" + descrizione);
            descrizioneTextArea.setFont(descrizioneTextArea.getFont().deriveFont(Font.PLAIN));
            descrizioneTextArea.setForeground(testoNormale);
        }

        // Gestione della scadenza
        boolean scaduto = false;
        if (scadenza != null) {
            // Formatta e mostra la data di scadenza
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            scadenzaLabel.setText("Scadenza: " + formatter.format(scadenza));

            // Verifica se la scadenza è passata
            scaduto = scadenza.isBefore(LocalDate.now());
        } else {
            scadenzaLabel.setText("Nessuna Scadenza");
        }

        // se scaduto, evidenzia il titolo in rosso
        titoloLabel.setForeground(scaduto ? Color.RED : testoNormale);
        scadenzaLabel.setForeground(testoNormale);
    }

    // Metodo di utilità per determinare se un colore è "scuro" (bassa luminosità)
    private boolean isColoreScuro(Color color) {
        // Calcola la luminosità secondo il modello standard (peso ai colori RGB)
        double lum = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;
        return lum < 0.5;
    }
}
