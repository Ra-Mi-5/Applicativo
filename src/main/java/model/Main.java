package model;

import java.awt.*;
import java.time.LocalDate;

public class Main {

    public static void main(String[] args) {


        Bacheca bachecaUniversita = new Bacheca("Università");
        bachecaUniversita.setDescrizione("Attività universitarie");

        Bacheca bachecaLavoro = new Bacheca("Lavoro");
        bachecaLavoro.setDescrizione("Progetti di lavoro");

        Bacheca bachecaTempoLibero = new Bacheca("Tempo Libero");
        bachecaTempoLibero.setDescrizione("Tempo libero e hobby");

        bachecaUniversita.setDescrizione("Esami, lezioni e appunti universitari");
        System.out.println("Descrizione aggiornata Bacheca Università: " + bachecaUniversita.getDescrizione());

        Utente utente = new Utente("mario.rossi");
        utente.setPassword("pass223");
        System.out.println("Utente creato: " + utente.getUsername());

        ToDo todo = new ToDo();
        todo.setTitolo("Studiare per l'esame di Analisi");
        todo.setDataScadenza(LocalDate.now());
        todo.setDescrizione("Ripassare il programma di Analisi");
        todo.setUrl("https...");
        todo.setColore(Color.decode("#FFCC00"));
        todo.setStato(StatoToDo.Non_Completato);
        todo.setAutore(utente);

        VistaToDo vista = new VistaToDo();
        vista.setTodo(todo);
        vista.setUtente(utente);
        vista.setBacheca(bachecaUniversita);
        vista.setPosizione(1);

        System.out.println("\n-- Dettagli --");
        System.out.println("Utente: " + vista.getUtente().getUsername());
        System.out.println("Bacheca: " + vista.getBacheca().getTitolo());
        System.out.println("Descrizione bacheca: " + vista.getBacheca().getDescrizione());
        System.out.println("ToDo: " + vista.getTodo().getTitolo());
        System.out.println("Descrizione ToDo: " + vista.getTodo().getDescrizione());
        System.out.println("Scadenza: " + vista.getTodo().getDataScadenza());
        System.out.println("Posizione bacheca: " + vista.getPosizione());
        System.out.println("Autore ToDo: " + todo.getAutore().getUsername());
    }
}