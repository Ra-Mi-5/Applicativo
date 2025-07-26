package model;

public class VistaToDo {

    private ToDo todo;
    private int posizione;
    private Utente utente;
    private Bacheca bacheca;


    public ToDo getTodo()
    {
        return todo;
    }

    public void setTodo(ToDo todo)
    {
        this.todo = todo;
    }

    public int getPosizione()
    {
        return posizione;
    }

    public void setPosizione(int posizione)
    {
        this.posizione = posizione;
    }

    public Utente getUtente()
    {
        return utente;
    }

    public void setUtente(Utente utente)
    {
        this.utente = utente;
    }

    public Bacheca getBacheca()
    {
        return bacheca;
    }

    public void setBacheca(Bacheca bacheca)
    {
        this.bacheca = bacheca;
    }
}