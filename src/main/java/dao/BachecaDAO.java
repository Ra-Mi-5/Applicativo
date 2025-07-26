package dao;


public interface BachecaDAO {
    void aggiornaDescrizioneBacheca(String username, String titolo, String nuovaDescrizione);
    void creaBacheca(String username, String titolo, String descrizione);
}
