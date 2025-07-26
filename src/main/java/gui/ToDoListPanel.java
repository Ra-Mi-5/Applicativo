package gui;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ToDoListPanel {
    private JPanel mainPanel;
    private JButton backButton;
    private JLabel titoloLabel;
    private JPanel todoContainerPanel;
    private JLabel noToDoLabel;

    // Listener che può essere chiamato per aggiornare la lista
    private Runnable refreshListener;

    public ToDoListPanel(String bachecaTitolo, List<ToDoPanel> todoPanels) {
        todoContainerPanel.setLayout(new BoxLayout(todoContainerPanel, BoxLayout.Y_AXIS));
        titoloLabel.setText("ToDo Bacheca: " + bachecaTitolo);
        aggiornaLista(todoPanels);
    }


    public void aggiornaLista(List<ToDoPanel> nuoviTodo) {
        // Pulisce il contenuto precedente
        todoContainerPanel.removeAll();

        if (nuoviTodo.isEmpty()) {
            // Nessun ToDo: mostra messaggio dedicato
            noToDoLabel.setVisible(true);
            todoContainerPanel.add(noToDoLabel);
        } else {
            // Nasconde il messaggio "nessun ToDo"
            noToDoLabel.setVisible(false);

            // Aggiunge ogni pannello ToDo alla vista
            for (ToDoPanel panel : nuoviTodo) {
                panel.setAlignmentX(Component.LEFT_ALIGNMENT);
                panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
                // Aggiunge il pannello
                todoContainerPanel.add(panel);
                // Aggiunge uno spazio verticale tra i pannelli
                todoContainerPanel.add(Box.createVerticalStrut(10));
            }
        }

        // Aggiorna graficamente il contenitore
        todoContainerPanel.revalidate();
        todoContainerPanel.repaint();
    }

    public void setBackListener(Runnable listener) {
        backButton.addActionListener(e -> listener.run());
    }

    public void setRefreshListener(Runnable listener) {
        this.refreshListener = listener;
    }

    public void selezionaToDo(ToDoPanel todoDaSelezionare) {
        // Cerca l'indice del pannello da selezionare
        int index = -1;
        Component[] comps = todoContainerPanel.getComponents();

        for (int i = 0; i < comps.length; i++) {
            if (comps[i] == todoDaSelezionare) {
                index = i;
                break;
            }
        }

        // Se trovato, effettua scroll e evidenziazione
        if (index >= 0) {
            SwingUtilities.invokeLater(() -> {
                Rectangle rect = todoDaSelezionare.getBounds();
                todoContainerPanel.scrollRectToVisible(rect);

                // Evidenziazione visiva temporanea
                todoDaSelezionare.setBackground(Color.YELLOW);
            });
        }
    }

    public JPanel getPanel() {
        return mainPanel;
    }
}
