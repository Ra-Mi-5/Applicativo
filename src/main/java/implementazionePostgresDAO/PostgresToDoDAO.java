package postgresDAO;

import dao.ToDoDAO;
import database.DBConnection;
import model.*;

import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PostgresToDoDAO implements ToDoDAO {

    private final Connection connection;

    public PostgresToDoDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void aggiungiToDo(ToDo todo) {
        String sql = """
        INSERT INTO todo (titolo, descrizione, data_scadenza, colore, url, stato, autore, categoria)
        VALUES (?, ?, ?, ?, ?, ?::stato_todo_enum, ?, ?)
        RETURNING id
    """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, todo.getTitolo());
            stmt.setString(2, todo.getDescrizione());
            stmt.setDate(3, todo.getDataScadenza() != null ? Date.valueOf(todo.getDataScadenza()) : null);
            stmt.setString(4, todo.getColore() != null ? String.format("#%06x", todo.getColore().getRGB() & 0xFFFFFF) : null);
            stmt.setString(5, todo.getUrl());
            stmt.setObject(6, todo.getStato() != null ? todo.getStato().name() : null, java.sql.Types.OTHER);
            stmt.setString(7, todo.getAutore().getUsername());
            stmt.setObject(8, todo.getCategoria(), java.sql.Types.OTHER);


            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                todo.setId(rs.getInt("id"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'aggiunta del ToDo", e);
        }
    }

    public void eliminaToDo(int todoId) throws SQLException {
        String sqlCondivisione = "DELETE FROM todo_condivisione WHERE todo_id = ?";
        String sqlVista = "DELETE FROM vista_todo WHERE todo_id = ?";
        String sqlToDo = "DELETE FROM todo WHERE id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Inizia transazione

            try (
                    PreparedStatement stmtCondivisione = conn.prepareStatement(sqlCondivisione);
                    PreparedStatement stmtVista = conn.prepareStatement(sqlVista);
                    PreparedStatement stmtToDo = conn.prepareStatement(sqlToDo)
            ) {
                // 1. Rimuove condivisioni
                stmtCondivisione.setInt(1, todoId);
                stmtCondivisione.executeUpdate();

                // 2. Rimuove vista_todo
                stmtVista.setInt(1, todoId);
                stmtVista.executeUpdate();

                // 3. Rimuove il ToDo
                stmtToDo.setInt(1, todoId);
                stmtToDo.executeUpdate();

                conn.commit(); // Conferma tutte le operazioni
            } catch (SQLException e) {
                conn.rollback(); // In caso di errore, annulla tutto
                throw new SQLException("Errore durante l'eliminazione del ToDo completo", e);
            } finally {
                conn.setAutoCommit(true); // Ripristina auto-commit
            }
        }
    }



    @Override
    public List<ToDo> getToDoByBacheca(String username, String titoloBacheca) {
        String sql = """
    SELECT t.* FROM todo t
    JOIN vista_todo v ON t.id = v.todo_id
    WHERE v.username = ? AND v.bacheca = ?::titolo_bacheca_enum
    ORDER BY v.posizione
""";

        return recuperaToDoConQuery(sql, username, titoloBacheca, null);
    }


    @Override
    public List<ToDo> getToDoInScadenzaEntro(String username, LocalDate data) {
        String sql = """
        SELECT t.*
        FROM todo t
        JOIN vista_todo v ON t.id = v.todo_id
        WHERE v.username = ? AND t.data_scadenza <= ?
    """;

        List<ToDo> risultati = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setDate(2, Date.valueOf(data));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                risultati.add(mappaToDo(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca dei ToDo in scadenza", e);
        }

        return risultati;
    }

    private List<ToDo> recuperaToDoConQuery(String sql, String username, String bacheca, Object extraParam) {
        List<ToDo> todos = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            if (bacheca != null) stmt.setString(2, bacheca);
            if (extraParam instanceof LocalDate) stmt.setDate(2, Date.valueOf((LocalDate) extraParam));
            if (extraParam instanceof String) stmt.setString(2, (String) extraParam);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ToDo todo = new ToDo();
                    todo.setId(rs.getInt("id"));
                    todo.setTitolo(rs.getString("titolo"));
                    todo.setDescrizione(rs.getString("descrizione"));

                    Date data = rs.getDate("data_scadenza");
                    if (data != null) {
                        todo.setDataScadenza(data.toLocalDate());
                    }

                    String coloreHex = rs.getString("colore");
                    if (coloreHex != null) {
                        todo.setColore(Color.decode(coloreHex));
                    }

                    todo.setUrl(rs.getString("url"));


                    String stato = rs.getString("stato");
                    todo.setStato(stato != null ? StatoToDo.valueOf(stato) : StatoToDo.Non_Completato);

                    String autore = rs.getString("autore");
                    if (autore != null) {
                        todo.setAutore(new Utente(autore));
                    }

                    String categoria = rs.getString("categoria");
                    if (categoria != null) {
                        todo.setCategoria(String.valueOf(categoria));
                    }

                    todos.add(todo);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero dei ToDo", e);
        }
        return todos;
    }
    private ToDo mappaToDo(ResultSet rs) throws SQLException {
        ToDo todo = new ToDo();
        todo.setId(rs.getInt("id"));
        todo.setTitolo(rs.getString("titolo"));
        todo.setDescrizione(rs.getString("descrizione"));
        todo.setDataScadenza(rs.getDate("data_scadenza") != null ? rs.getDate("data_scadenza").toLocalDate() : null);

        String coloreHex = rs.getString("colore");
        if (coloreHex != null) {
            todo.setColore(Color.decode(coloreHex));
        }

        todo.setUrl(rs.getString("url"));

        String statoStr = rs.getString("stato");
        if (statoStr != null) {
            todo.setStato(StatoToDo.valueOf(statoStr));
        }

        String autoreUsername = rs.getString("autore");
        if (autoreUsername != null) {
            todo.setAutore(new Utente(autoreUsername));
        }

        String categoriaStr = rs.getString("categoria");
        if (categoriaStr != null) {
            todo.setCategoria(String.valueOf(categoriaStr));
        }

        return todo;
    }
    public List<ToDo> cercaToDoAvanzato(String username, String testo, LocalDate dataScadenza) {
        StringBuilder sql = new StringBuilder("""
        SELECT t.*
        FROM todo t
        JOIN vista_todo v ON t.id = v.todo_id
        WHERE v.username = ?
    """);

        List<Object> parametri = new ArrayList<>();
        parametri.add(username);

        if (testo != null && !testo.trim().isEmpty()) {
            sql.append(" AND (LOWER(t.titolo) LIKE ? OR LOWER(t.descrizione) LIKE ?)");
            String pattern = "%" + testo.trim().toLowerCase() + "%";
            parametri.add(pattern);
            parametri.add(pattern);
        }

        if (dataScadenza != null) {
            sql.append(" AND t.data_scadenza <= ?");
            parametri.add(Date.valueOf(dataScadenza));
        }

        List<ToDo> risultati = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametri.size(); i++) {
                stmt.setObject(i + 1, parametri.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                risultati.add(mappaToDo(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore nella ricerca avanzata dei ToDo", e);
        }

        return risultati;
    }

    public void aggiornaStatoToDo(ToDo todo) {
        String sql = "UPDATE todo SET stato = ?::stato_todo_enum WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, todo.getStato().name()); // nome dell'enum come stringa
            stmt.setInt(2, todo.getId());

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Nessun ToDo aggiornato, id non trovato: " + todo.getId());
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'aggiornamento dello stato ToDo: " + e.getMessage(), e);
        }
    }
    public void spostaToDoInBacheca(int todoId, String nuovaCategoria, String username) {
        String sqlUpdateVista = """
        UPDATE vista_todo
        SET bacheca = ?::titolo_bacheca_enum
        WHERE todo_id = ? AND username = ?
    """;

        String sqlUpdateTodo = """
        UPDATE todo
        SET categoria = ?::titolo_bacheca_enum
        WHERE id = ?
    """;

        try (Connection conn = DBConnection.getConnection()) {
            // Disabilita auto-commit per garantire atomicità
            conn.setAutoCommit(false);

            try (PreparedStatement stmt1 = conn.prepareStatement(sqlUpdateVista);
                 PreparedStatement stmt2 = conn.prepareStatement(sqlUpdateTodo)) {

                // Aggiorna vista_todo
                stmt1.setString(1, nuovaCategoria);
                stmt1.setInt(2, todoId);
                stmt1.setString(3, username);
                int rows1 = stmt1.executeUpdate();

                // Aggiorna tabella todo
                stmt2.setString(1, nuovaCategoria);
                stmt2.setInt(2, todoId);
                int rows2 = stmt2.executeUpdate();

                if (rows1 == 0 || rows2 == 0) {
                    conn.rollback();
                    throw new RuntimeException("Errore: aggiornamento fallito, record non trovato.");
                }

                // Commit se tutto ok
                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante lo spostamento del ToDo nella bacheca", e);
        }
    }
    @Override
    public ToDo getById(int id) {
        String sql = "SELECT * FROM todo WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ToDo todo = new ToDo();

                    todo.setId(rs.getInt("id"));
                    todo.setTitolo(rs.getString("titolo"));
                    todo.setDescrizione(rs.getString("descrizione"));

                    Date dataScadenzaSql = rs.getDate("data_scadenza");
                    if (dataScadenzaSql != null) {
                        todo.setDataScadenza(dataScadenzaSql.toLocalDate());
                    }

                    String coloreString = rs.getString("colore");
                    if (coloreString != null) {
                        Color colore = Color.decode(coloreString);
                        todo.setColore(colore);
                    }

                    todo.setUrl(rs.getString("url"));

                    // Qui aggiungi la categoria
                    String categoriaStr = rs.getString("categoria");
                    if (categoriaStr != null) {
                        todo.setCategoria(String.valueOf(categoriaStr));
                    } else {
                        todo.setCategoria(null); // o eventualmente un valore di default
                    }

                    return todo;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero del ToDo per ID", e);
        }
        return null;
    }
    public void update(ToDo todo) throws SQLException {
        String sql = "UPDATE todo SET titolo = ?, descrizione = ?, data_scadenza = ?, colore = ?, url = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, todo.getTitolo());
            stmt.setString(2, todo.getDescrizione());
            stmt.setDate(3, java.sql.Date.valueOf(todo.getDataScadenza()));

            // Salva colore in formato stringa esadecimale #RRGGBB
            String coloreString = String.format("#%02x%02x%02x",
                    todo.getColore().getRed(),
                    todo.getColore().getGreen(),
                    todo.getColore().getBlue());
            stmt.setString(4, coloreString);

            stmt.setString(5, todo.getUrl());
            stmt.setInt(6, todo.getId());

            stmt.executeUpdate();
        }
    }
}
