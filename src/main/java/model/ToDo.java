package model;

import java.time.LocalDate;
import java.awt.Color;


public class ToDo {

    private int id;
    private String titolo;
    private LocalDate dataScadenza;
    private Color colore;
    private String descrizione;
    private String url;
    private StatoToDo stato;
    private Utente autore;
    private String categoria;



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }



    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }


    public String getTitolo()
    {
        return titolo;
    }

    public void setTitolo(String titolo)
    {
        this.titolo = titolo;
    }

    public LocalDate getDataScadenza() {
        return dataScadenza;
    }

    public void setDataScadenza(LocalDate dataScadenza) {
        this.dataScadenza = dataScadenza;
    }

    public Color getColore()
    {
        return colore;
    }

    public void setColore(Color colore)
    {
        this.colore = colore;
    }

    public String getDescrizione()
    {
        return descrizione;
    }

    public void setDescrizione(String descrizione)
    {
        this.descrizione = descrizione;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public StatoToDo getStato()
    {
        return stato;
    }

    public void setStato(StatoToDo stato)
    {
        this.stato = stato;
    }

    public Utente getAutore()
    {
        return autore;
    }

    public void setAutore(Utente autore)
    {
        this.autore = autore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ToDo)) return false;
        ToDo other = (ToDo) o;
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

}