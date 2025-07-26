package postgresDAO;

import dao.UtenteDAO;
import database.DBConnection;
import model.Utente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostgresUtenteDAO implements UtenteDAO {
    private final Connection connection;

    public PostgresUtenteDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void aggiungiUtente(Utente utente) {
        String sql = "INSERT INTO utente(username, password) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, utente.getUsername());
            ps.setString(2, utente.getPassword());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore inserimento utente", e);
        }
    }

    @Override
    public Utente getUtenteByUsername(String username) {
        String sql = "SELECT username, password FROM utente WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Utente u = new Utente(rs.getString("username"));
                    u.setPassword(rs.getString("password"));
                    return u;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore recupero utente", e);
        }
        return null;
    }

    public List<String> getTuttiGliUtenti() {
        List<String> utenti = new ArrayList<>();
        String sql = "SELECT username FROM utente";  // Assumendo la tabella si chiami 'utenti' e la colonna 'username'

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                utenti.add(rs.getString("username"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero degli utenti", e);
        }

        return utenti;
    }
}