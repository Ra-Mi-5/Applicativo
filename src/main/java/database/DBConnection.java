package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/ToDoAppDb";
    private static final String USER = "postgres";
    private static final String PASSWORD = "dBR10";

    private static Connection connection = null;

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Errore connessione DB: " + e.getMessage());
            throw new RuntimeException(e);  // rilancia come unchecked exception
        }
    }
}