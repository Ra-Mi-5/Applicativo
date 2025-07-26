package model;

import java.util.ArrayList;
import java.util.List;

public class Bacheca {
    private final String titolo;
    private String descrizione;
    private List<ToDo> toDos;

    public Bacheca(String titolo) {
        this.titolo = titolo;
        this.toDos = new ArrayList<>();
    }

    public String getDescrizione(){
        return descrizione;
    }

    public void setDescrizione(String descrizione)
    {
        this.descrizione = descrizione;
    }

    public String getTitolo()
    {
        return titolo;
    }

    public List<ToDo> getToDos() {
        return toDos;
    }

    public void aggiungiToDo(ToDo todo) {
        toDos.add(todo);
    }

    public void rimuoviToDo(ToDo todo) {
        toDos.remove(todo);
    }
}
