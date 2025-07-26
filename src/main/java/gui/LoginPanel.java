package gui;

import controller.Controller;
import javax.swing.*;

public class LoginPanel extends JPanel {
    public JLabel labelUsername;
    public JTextField usernameField;
    public JLabel labelPassword;
    public JPasswordField passwordField;
    public JButton loginBotton;
    public JButton registratiBotton;
    private JPanel mainPanel;

    private final JFrame mainFrame;      // Frame principale dell'app
    private final Controller controller; // Controller che gestisce le azioni utente

    public LoginPanel(JFrame mainFrame, Controller controller) {
        this.mainFrame = mainFrame;
        this.controller = controller;

        registratiBotton.addActionListener(e -> controller.mostraRegistrazione());

        loginBotton.addActionListener(e -> {
            String username = usernameField.getText().trim();                  // Recupera username
            String password = String.valueOf(passwordField.getPassword());     // Recupera password

            // Controllo campi vuoti
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Inserisci username e password.", "Errore", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Verifica credenziali tramite controller
            if (controller.verificaCredenziali(username, password)) {
                SessionManager.setLoggedUsername(username); // Salva l'utente nella sessione (login riuscito)
                controller.mostraHome();                   // Passa alla schermata principale
            } else {
                JOptionPane.showMessageDialog(this, "Credenziali errate.", "Errore", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
