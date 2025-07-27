package gui;

import controller.Controller;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegisterPanel extends JPanel {
    public JLabel labelEmail;
    public JTextField emailField;
    public JLabel labelUsername;
    public JTextField usernameField;
    public JLabel labelPassword;
    public JPasswordField passwordField;
    public JLabel labelConfirmPassword;
    public JPasswordField confirmPasswordField;
    public JButton registerButton;
    public JButton loginButton;
    private JPanel mainPanel;

    private final JFrame mainFrame;      // Riferimento al frame principale dell'applicazione
    private final Controller controller; // Controller che gestisce la logica applicativa

    // il Costruttore riceve frame e controller
    public RegisterPanel(JFrame mainFrame, Controller controller) {
        this.mainFrame = mainFrame;
        this.controller = controller;

        loginButton.addActionListener(e -> controller.mostraLogin());

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Recupero dati dai campi di input
                String email = emailField.getText().trim();
                String username = usernameField.getText().trim();
                String password = String.valueOf(passwordField.getPassword());
                String confirmPassword = String.valueOf(confirmPasswordField.getPassword());

                // Verifica campi obbligatori
                if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    JOptionPane.showMessageDialog(RegisterPanel.this, "Compila tutti i campi.", "Errore", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Verifica corrispondenza password
                if (!password.equals(confirmPassword)) {
                    JOptionPane.showMessageDialog(RegisterPanel.this, "Le password non corrispondono.", "Errore", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Tentativo di registrazione tramite controller
                boolean successo = controller.registraUtente(username, password);
                if (successo) {
                    JOptionPane.showMessageDialog(RegisterPanel.this, "Registrazione completata con successo!");
                    controller.mostraLogin(); // Ritorna alla schermata di login
                } else {
                    JOptionPane.showMessageDialog(RegisterPanel.this, "Nome utente già esistente.", "Errore", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}