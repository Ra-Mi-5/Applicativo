package postgresDAO;

import dao.CondivisioneDAO;
import database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class PostgresCondivisioneDAO implements CondivisioneDAO {
    private final Connection connection;

    public PostgresCondivisioneDAO(Connection connection) {
        this.connection = connection;
    }

    public Set<String> getUtentiCondivisi(int todoId) {
        String sql = "SELECT username FROM todo_condivisione WHERE todo_id = ?";
        Set<String> utenti = new HashSet<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, todoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    utenti.add(rs.getString("username"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero utenti condivisi per ToDo", e);
        }

        return utenti;
    }

    @Override
    public void rimuoviCondivisione(int todoId, String username) {
        String sql = "DELETE FROM todo_condivisione WHERE todo_id = ? AND username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, todoId);
            stmt.setString(2, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la rimozione della condivisione", e);
        }
    }

    @Override
    public Set<String> getUtentiDisponibili(int todoId, String autoreUsername) {
        Set<String> disponibili = new HashSet<>();

        String sql = """
            SELECT username FROM utente 
            WHERE username != ? 
              AND username NOT IN (
                  SELECT username FROM todo_condivisione WHERE todo_id = ?
              )
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, autoreUsername);
            stmt.setInt(2, todoId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    disponibili.add(rs.getString("username"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero utenti disponibili alla condivisione", e);
        }

        return disponibili;
    }
    @Override
    public void inserisciCondivisione(int todoId, String username) {
        String sql = "INSERT INTO todo_condivisione (todo_id, username) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, todoId);
            stmt.setString(2, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore nell'inserimento della condivisione", e);
        }
    }
}
