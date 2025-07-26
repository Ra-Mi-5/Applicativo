package postgresDAO;

import dao.BachecaDAO;
import database.DBConnection;

import java.sql.*;


public class PostgresBachecaDAO implements BachecaDAO {
    private final Connection connection;

    public PostgresBachecaDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void aggiornaDescrizioneBacheca(String username, String titolo, String nuovaDescrizione) {
        String sql = """
        UPDATE bacheca
        SET descrizione = ?
        WHERE username = ? AND titolo = ?::titolo_bacheca_enum
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nuovaDescrizione);
            stmt.setString(2, username);
            stmt.setString(3, titolo); // deve corrispondere ai valori dell'enum

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'aggiornamento della descrizione della bacheca", e);
        }
    }
    public void creaBacheca(String username, String titolo, String descrizione) {
        String sql = """
        INSERT INTO bacheca (username, titolo, descrizione)
        VALUES (?, ?::titolo_bacheca_enum, ?)
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, titolo); // deve essere uno dei valori validi dell'enum
            stmt.setString(3, descrizione);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la creazione della bacheca per l'utente " + username, e);
        }
    }

}