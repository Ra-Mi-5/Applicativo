package dao;

import model.TitoloBacheca;
import model.VistaToDo;

import java.sql.SQLException;
import java.util.List;

public interface VistaToDoDAO {

    void inserisciVista(VistaToDo vista);
    List<VistaToDo> getByBacheca(String username, String categoria);
    void eliminaVistaByToDo(int todoId) throws SQLException;
    void aggiornaPosizione(int todoId, String username, String bacheca, int nuovaPosizione);
    void rimuoviVistaToDo(int todoId, String username);
    void inserisciVistaToDo(int todoId, String username, String bacheca, int posizione);
    int getMaxPosizione(String username, String bacheca);
}
