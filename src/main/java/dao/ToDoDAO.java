package dao;

import model.ToDo;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface ToDoDAO {
    void aggiungiToDo(ToDo todo);
    void eliminaToDo(int todoId) throws SQLException;
    List<ToDo> getToDoByBacheca(String username, String titoloBacheca);
    List<ToDo> getToDoInScadenzaEntro(String username, LocalDate data);
    List<ToDo> cercaToDoAvanzato(String username, String testo, LocalDate dataScadenza);
    void aggiornaStatoToDo(ToDo todo) throws SQLException;
    void spostaToDoInBacheca(int todoId, String nuovaBacheca, String username);
    ToDo getById(int id);
    void update(ToDo todo) throws SQLException;
}
