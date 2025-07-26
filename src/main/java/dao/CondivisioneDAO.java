package dao;

import java.util.Set;

public interface CondivisioneDAO {
    Set<String> getUtentiCondivisi(int todoId);
    void rimuoviCondivisione(int todoId, String username);
    Set<String> getUtentiDisponibili(int todoId, String autoreUsername);
    void inserisciCondivisione(int todoId, String username);
}
