// Classe di utilità per la gestione della sessione utente nella GUI.
// Tiene traccia dell'username dell'utente attualmente loggato.
package gui;

public class SessionManager {
    // Variabile statica che memorizza il nome utente dell'utente loggato
    private static String loggedUsername;

    // Metodo per impostare il nome utente dell'utente loggato
    public static void setLoggedUsername(String username) {
        loggedUsername = username;
    }

    // Metodo per ottenere il nome utente dell'utente attualmente loggato
    public static String getLoggedUsername() {
        return loggedUsername;
    }

    // Metodo per cancellare la sessione corrente (logout)
    public static void clearSession() {
        loggedUsername = null;
    }
}
