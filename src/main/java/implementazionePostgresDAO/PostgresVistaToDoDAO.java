package postgresDAO;

import dao.VistaToDoDAO;
import database.DBConnection;
import model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostgresVistaToDoDAO implements VistaToDoDAO {
    private final Connection connection;

    public PostgresVistaToDoDAO(Connection connection) {
        this.connection = connection;
    }

    public void inserisciVista(VistaToDo vista) {
        String sql = """
        INSERT INTO vista_todo (todo_id, username, bacheca, posizione)
        VALUES (?, ?, ?::titolo_bacheca_enum, ?)
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, vista.getTodo().getId());
            stmt.setString(2, vista.getUtente().getUsername());
            stmt.setString(3, vista.getBacheca().getTitolo());
            stmt.setInt(4, vista.getPosizione());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'inserimento nella vista_todo", e);
        }

    }

    public List<VistaToDo> getByBacheca(String username, String categoria) {
        String sql = """
        SELECT v.todo_id, v.username, v.bacheca, v.posizione,
               t.titolo, t.descrizione, t.data_scadenza, t.colore, t.url,
               t.stato, t.autore, t.categoria
        FROM vista_todo v
        JOIN todo t ON v.todo_id = t.id
        WHERE v.username = ? AND v.bacheca = ?::titolo_bacheca_enum
        ORDER BY v.posizione
    """;

        List<VistaToDo> risultati = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, categoria);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ToDo todo = new ToDo();
                todo.setId(rs.getInt("todo_id"));
                todo.setTitolo(rs.getString("titolo"));
                todo.setDescrizione(rs.getString("descrizione"));
                todo.setDataScadenza(
                        rs.getDate("data_scadenza") != null ? rs.getDate("data_scadenza").toLocalDate() : null
                );

                // Colore (assumendo che sia in formato #RRGGBB stringa)
                String coloreHex = rs.getString("colore");
                if (coloreHex != null) {
                    java.awt.Color colore = java.awt.Color.decode(coloreHex);
                    todo.setColore(colore);
                }

                todo.setUrl(rs.getString("url"));

                // Enum stato
                String statoStr = rs.getString("stato");
                if (statoStr != null) {
                    todo.setStato(StatoToDo.valueOf(statoStr));
                }

                // Autore
                todo.setAutore(new Utente(rs.getString("autore")));

                // Categoria (titolo bacheca)
                todo.setCategoria(String.valueOf(rs.getString("categoria")));

                // VistaToDo
                VistaToDo vista = new VistaToDo();
                vista.setTodo(todo);
                vista.setUtente(new Utente(rs.getString("username")));
                vista.setBacheca(new Bacheca(String.valueOf(rs.getString("bacheca"))));
                vista.setPosizione(rs.getInt("posizione"));

                risultati.add(vista);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero dei VistaToDo per bacheca", e);
        }

        return risultati;
    }
    public void eliminaVistaByToDo(int todoId) throws SQLException {
        String sql = "DELETE FROM vista_todo WHERE todo_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, todoId);
            stmt.executeUpdate();
        }
    }

    public void aggiornaPosizione(int todoId, String username, String bacheca, int nuovaPosizione) {
        String sql = "UPDATE vista_todo SET posizione = ? WHERE todo_id = ? AND username = ? AND bacheca = ?::titolo_bacheca_enum";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, nuovaPosizione);
            stmt.setInt(2, todoId);
            stmt.setString(3, username);
            stmt.setString(4, bacheca);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore aggiornando la posizione in vista_todo", e);
        }
    }

    public void rimuoviVistaToDo(int todoId, String username) {
        String sql = "DELETE FROM vista_todo WHERE todo_id = ? AND username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, todoId);
            stmt.setString(2, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore nella rimozione della vista ToDo", e);
        }
    }
    public void inserisciVistaToDo(int todoId, String username, String bacheca, int posizione) {
        String sql = "INSERT INTO vista_todo (todo_id, username, bacheca, posizione) VALUES (?, ?, ?::titolo_bacheca_enum, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, todoId);
            stmt.setString(2, username);
            stmt.setString(3, bacheca);
            stmt.setInt(4, posizione);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante inserimento vista_todo", e);
        }
    }

    public int getMaxPosizione(String username, String bacheca) {
        String sql = "SELECT COALESCE(MAX(posizione), -1) FROM vista_todo WHERE username = ? AND bacheca = ?::titolo_bacheca_enum";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, bacheca);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int max = rs.getInt(1);

                    return max;
                }
            }
        } catch (SQLException e) {

            e.printStackTrace();
        }
        return -1;
    }
}