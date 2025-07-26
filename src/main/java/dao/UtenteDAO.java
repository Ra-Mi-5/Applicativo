package dao;

import model.Utente;
import java.util.List;

public interface UtenteDAO {
    void aggiungiUtente(Utente utente);
    Utente getUtenteByUsername(String username);
    List<String> getTuttiGliUtenti();
}
