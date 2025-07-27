package controller;

import gui.*;
import model.*;

import database.DBConnection;
import dao.*;
import postgresDAO.*;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.List;


public class Controller {
    private final JFrame frame;
    private HomePanel homePanel;

    private Map<String, Bacheca> bacheche = new HashMap<>();
    private final Map<String, Utente> utentiRegistrati = new HashMap<>();


    private ToDoPanel todoSelezionato;

    private UtenteDAO utenteDAO;
    private BachecaDAO bachecaDAO;
    private ToDoDAO toDoDAO;
    private VistaToDoDAO vistaToDoDAO;
    private CondivisioneDAO condivisioneDAO;

    private String usernameCorrente;


    public Controller() {
        frame = new JFrame("ToDo App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);

        var conn = DBConnection.getConnection();

        utenteDAO = new PostgresUtenteDAO(conn);
        bachecaDAO = new PostgresBachecaDAO(conn);
        toDoDAO = new PostgresToDoDAO(conn);
        vistaToDoDAO = new PostgresVistaToDoDAO(conn);
        condivisioneDAO = new PostgresCondivisioneDAO(conn);

        inizializzaBachecheDefault();
    }

    public ToDo getToDoFromPanel(ToDoPanel panel) {
        ToDo todo = new ToDo();

        todo.setId(panel.getId());

        todo.setTitolo(panel.getTitolo());
        todo.setDescrizione(panel.getDescrizione());
        todo.setDataScadenza(panel.getDataScadenza());
        todo.setColore(panel.getColoreSfondo());
        todo.setUrl(panel.getUrl());

        // Stato: converte booleano in enum StatoToDo
        todo.setStato(panel.isCompletato() ? StatoToDo.Completato : StatoToDo.Non_Completato);

        // Autore: crea nuovo Utente solo se non null o vuoto
        String autore = panel.getAutore();
        if (autore != null && !autore.isBlank()) {
            todo.setAutore(new Utente(autore));
        } else {
            todo.setAutore(null);
        }

        todo.setCategoria(panel.getCategoria());

        return todo;
    }

    private void inizializzaBachecheDefault() {
        for (TitoloBacheca titolo : TitoloBacheca.values()) {
            bacheche.put(titolo.name(), new Bacheca(titolo.name()));
        }
    }


    // Metodo per registrare un utente nel database
    public boolean registraUtente(String username, String password) {
        // Verifica se l'utente esiste nel DB
        Utente utenteEsistente = utenteDAO.getUtenteByUsername(username);
        if (utenteEsistente != null) {
            return false; // Utente già esistente
        }

        // Crea e salva il nuovo utente
        Utente nuovoUtente = new Utente(username);
        nuovoUtente.setPassword(password);
        utenteDAO.aggiungiUtente(nuovoUtente);

        // Crea bacheche di default per l'utente con descrizioni predefinite
        bachecaDAO.creaBacheca(username, "Università", "Attività Universitarie");
        bachecaDAO.creaBacheca(username, "Lavoro", "Gestione Attività Lavorative");
        bachecaDAO.creaBacheca(username, "Tempo_Libero", "Tempo Libero, Hobby e Altro");

        return true;
    }

    // Metodo per verificare credenziali
    public boolean verificaCredenziali(String username, String password) {
        Utente utente = utenteDAO.getUtenteByUsername(username);

        if (utente != null && password.equals(utente.getPassword())) {
            // Imposta l'utente loggato se le credenziali sono corrette
            this.usernameCorrente = username;
            return true;
        } else {
            return false;
        }
    }

    public void mostraLogin() {
        LoginPanel loginPanel = new LoginPanel(frame, this);
        frame.setContentPane(loginPanel.getMainPanel());
        frame.revalidate();
        frame.repaint();
    }

    public void mostraRegistrazione() {
        RegisterPanel registerPanel = new RegisterPanel(frame, this);
        frame.setContentPane(registerPanel.getMainPanel());
        frame.revalidate();
        frame.repaint();
    }

    public void mostraHome() {
        if (homePanel == null) {
            homePanel = new HomePanel(frame, this);
        }

        Container mainPanel = homePanel.getMainPanel();

        frame.getContentPane().removeAll();
        frame.setContentPane(mainPanel);
        frame.revalidate();
        frame.repaint();
    }

    // Mostra la lista dei ToDo per una categoria specifica
    public void mostraToDoList(String categoria) {
        // Ottieni la lista dei VistaToDo per l'utente e per la bacheca
        List<VistaToDo> vistaToDos = vistaToDoDAO.getByBacheca(usernameCorrente, categoria);

        //  Ordina per posizione
        vistaToDos.sort(Comparator.comparingInt(VistaToDo::getPosizione));

        List<ToDoPanel> pannelliTodo = new ArrayList<>();
        for (VistaToDo vista : vistaToDos) {
            ToDo todo = vista.getTodo();
            List<String> tutteLeCategorie = Arrays.asList("Università", "Lavoro", "Tempo Libero");
            ToDoPanel panel = new ToDoPanel(
                    todo.getTitolo(),
                    todo.getDescrizione(),
                    todo.getDataScadenza(),
                    todo.getColore(),
                    todo.getUrl(),
                    todo.getStato() == StatoToDo.Completato,
                    todo.getAutore() != null ? todo.getAutore().getUsername() : null,
                    categoria,
                    tutteLeCategorie,
                    creaListener(categoria),
                    creaSpostamentoListener(categoria)
            );
            panel.setId(todo.getId());
            pannelliTodo.add(panel);
        }

        ToDoListPanel listaPanel = new ToDoListPanel(categoria, pannelliTodo);

        for (ToDoPanel panel : pannelliTodo) {
            panel.setToDoActionListener(creaListener(categoria));
            panel.spostamentoListener = creaSpostamentoListener(categoria);
        }

        listaPanel.setBackListener(() -> mostraHome());

        frame.setContentPane(listaPanel.getPanel());
        frame.revalidate();
        frame.repaint();
    }

    // Crea un listener per le azioni su ogni ToDo
    public ToDoPanel.ToDoActionListener creaListener(String  categoria) {
        return new ToDoPanel.ToDoActionListener() {
            @Override
            public void onModifica(ToDoPanel panel) {
                apriEditor(categoria, panel);
            }

            @Override
            public void onElimina(ToDoPanel panel) {
                int conferma = JOptionPane.showConfirmDialog(frame,
                        "Sei sicuro di voler eliminare questo ToDo?",
                        "Conferma eliminazione",
                        JOptionPane.YES_NO_OPTION);

                if (conferma == JOptionPane.YES_OPTION) {
                    ToDo todo = getToDoFromPanel(panel);
                    eliminaToDo(panel.getCategoria(), todo);

                    mostraToDoList(panel.getCategoria());
                }
            }

            @Override
            public void onSpostaBacheca(ToDoPanel panel, String nuovaCategoria) {
                String categoriaOrigine = panel.getCategoria();
                ToDo toDo = getToDoFromPanel(panel);

                // Aggiorna il modello spostando il ToDo da una categoria all'altra
                spostaToDo(categoriaOrigine, nuovaCategoria, toDo);

                // Aggiorna il campo categoria del ToDo
                toDo.setCategoria(nuovaCategoria);

                // Aggiorna il pannello per riflettere la nuova categoria
                panel.setCategoria(nuovaCategoria);

                // Aggiorna il DB: aggiorna la relazione nella vista/intermedia
                try {
                    toDoDAO.spostaToDoInBacheca(toDo.getId(), nuovaCategoria, usernameCorrente);
                } catch (Exception e) {

                    e.printStackTrace();
                }

                // Ricarica la lista ToDo per la nuova categoria nella UI
                SwingUtilities.invokeLater(() -> {
                    mostraToDoList(nuovaCategoria);
                });
            }


            @Override
            public void onToggleCompletato(ToDoPanel panel, boolean completato) {
                ToDo toDo = getToDoFromPanel(panel);
                if (completato) {
                    toDo.setStato(StatoToDo.Completato);
                } else {
                    toDo.setStato(StatoToDo.Non_Completato);
                }

                try {
                    // Salvo l'aggiornamento dello stato sul DB
                    toDoDAO.aggiornaStatoToDo(toDo);
                } catch (Exception ex) {
                    System.err.println("Errore durante l'aggiornamento dello stato ToDo: " + ex.getMessage());
                }
            }

            @Override
            public void onApriCondivisioni(ToDoPanel panel) {
                apriFinestraCondivisioni(panel);
            }
        };

    }

    // Crea listener per spostare un ToDo su/giù
    public ToDoPanel.SpostamentoListener creaSpostamentoListener(String categoria) {
        return (panel, direzione) -> {
            List<VistaToDo> listaVista = vistaToDoDAO.getByBacheca(usernameCorrente, categoria);

            VistaToDo corrente = null;
            for (VistaToDo v : listaVista) {
                if (v.getTodo().getId() == getToDoFromPanel(panel).getId()) {
                    corrente = v;
                    break;
                }
            }

            if (corrente == null) return;

            int index = listaVista.indexOf(corrente);
            int nuovoIndex = index + direzione;

            if (index >= 0 && nuovoIndex >= 0 && nuovoIndex < listaVista.size()) {
                // Sposta nella lista in memoria
                Collections.swap(listaVista, index, nuovoIndex);

                // Riassegna le posizioni da 1 a N
                for (int i = 0; i < listaVista.size(); i++) {
                    VistaToDo v = listaVista.get(i);
                    int nuovaPos = i + 1;
                    vistaToDoDAO.aggiornaPosizione(v.getTodo().getId(), usernameCorrente, categoria, nuovaPos);
                }

                mostraToDoList(categoria);
            }
        };
    }


    public void apriEditor(String categoria, ToDoPanel toModificaPanel) {
        ToDoPanel toDoPanelLocale = toModificaPanel;

        JDialog dialog = new JDialog(frame, (toModificaPanel == null ? "Nuovo ToDo - " : "Modifica ToDo - ") + categoria, true);

        final ToDoEditor[] editor = new ToDoEditor[1];

        editor[0] = new ToDoEditor(toDoPanelLocale, () -> {
            if (toModificaPanel == null) {
                // Creazione nuovo ToDo
                ToDo nuovo = new ToDo();
                nuovo.setTitolo(editor[0].getTitolo());
                nuovo.setDescrizione(editor[0].getDescrizione());
                nuovo.setDataScadenza(editor[0].getDataScadenza());
                nuovo.setColore(editor[0].getColoreScelto());
                nuovo.setUrl(editor[0].getUrl());
                nuovo.setStato(StatoToDo.Non_Completato);

                String autoreUsername = SessionManager.getLoggedUsername();
                Utente autore = utentiRegistrati.get(autoreUsername);
                nuovo.setAutore(autore);

                // Inserisci nel DB e aggiorna la UI
                aggiungiToDo(nuovo, categoria, usernameCorrente);

            } else {
                // Modifica ToDo esistente
                ToDo toModifica = getToDoFromPanel(toModificaPanel);


                toModifica.setTitolo(editor[0].getTitolo());
                toModifica.setDescrizione(editor[0].getDescrizione());
                toModifica.setDataScadenza(editor[0].getDataScadenza());
                toModifica.setColore(editor[0].getColoreScelto());
                toModifica.setUrl(editor[0].getUrl());

                // *** AGGIORNAMENTO DB ***
                try {
                    toDoDAO.update(toModifica);
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Errore durante l'aggiornamento del ToDo nel database.", "Errore", JOptionPane.ERROR_MESSAGE);
                }

                // Aggiorna la UI del pannello modificato
                toModificaPanel.revalidate();
                toModificaPanel.repaint();
            }

            mostraToDoList(categoria);
            dialog.dispose();
        }, dialog);

        // Se sto modificando, carico i dati esistenti nel form
        if (toModificaPanel != null) {
            editor[0].setToDoData(toModificaPanel);
        }

        dialog.setContentPane(editor[0].getPanel());
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }


    private void apriFinestraCondivisioni(ToDoPanel todoPanel) {
        ToDo todo = getToDoFromPanel(todoPanel);
        Utente autore = todo.getAutore();
        String autoreUsername = autore != null ? autore.getUsername() : "";

        String utenteCorrente = SessionManager.getLoggedUsername();
        String titolo = todo.getTitolo();

        int todoId = todo.getId();

        //  Recupera dal DB gli utenti con cui è già condiviso
        List<String> condivisi = new ArrayList<>(condivisioneDAO.getUtentiCondivisi(todoId));

        //  Recupera dal DB gli utenti disponibili (non autore e non già condivisi)
        List<String> disponibili = new ArrayList<>(condivisioneDAO.getUtentiDisponibili(todoId, autoreUsername));

        CondivisioniPanel.CondivisioniDialog dialog = new CondivisioniPanel.CondivisioniDialog(
                frame, titolo, autoreUsername, utenteCorrente, condivisi, disponibili
        );

        dialog.setSize(1000, 800);
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);

        Set<String> aggiornati = dialog.getUtentiCondivisiAggiornati();

        //  Aggiorna nel DB le condivisioni
        aggiornaCondivisioni(todoId, aggiornati);

        // Aggiorna la vista
        todoPanel.setCondivisoCon(aggiornati.toArray(new String[0]));
    }

    public void aggiungiToDo(ToDo todo, String categoria, String username) {
        // Imposta la categoria (titolo della bacheca)
        todo.setCategoria(categoria);

        // Imposta l'autore del ToDo
        Utente autore = new Utente(username);
        todo.setAutore(autore);

        // Salva il ToDo nel database
        toDoDAO.aggiungiToDo(todo);

        // Crea la vista del ToDo per l'autore (utente proprietario)
        VistaToDo vista = new VistaToDo();
        vista.setTodo(todo);
        vista.setUtente(autore);
        vista.setBacheca(new Bacheca(categoria));
        vista.setPosizione(0);

        vistaToDoDAO.inserisciVista(vista);
    }


    public void eliminaToDo(String categoria, ToDo todo) {
        Bacheca bacheca = bacheche.get(categoria);
        if (bacheca != null) {
            // 1. Rimuovi il ToDo dalla bacheca in memoria
            bacheca.rimuoviToDo(todo);

            try {
                // 2. Elimina tutte le viste associate al ToDo (vista_todo)
                vistaToDoDAO.eliminaVistaByToDo(todo.getId());

                // 3. Elimina il ToDo dal database
                toDoDAO.eliminaToDo(todo.getId());

            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Errore durante l'eliminazione del ToDo dal database", e);
            }

            // 4. Aggiorna la GUI ricaricando o aggiornando la lista ToDo per la categoria
            mostraToDoList(categoria);
        }
    }

    public void spostaToDo(String daCategoria, String aCategoria, ToDo todo) {
        Bacheca daBacheca = bacheche.get(daCategoria);
        Bacheca aBacheca = bacheche.computeIfAbsent(aCategoria, k -> new Bacheca(aCategoria));

        if (daBacheca != null) {
            daBacheca.rimuoviToDo(todo);
            todo.setCategoria(aCategoria);
        }
        if (aBacheca != null) {
            aBacheca.aggiungiToDo(todo);
        }
    }

    public List<ToDoPanel> cercaToDo(String testo, LocalDate data) {
        List<ToDoPanel> risultati = new ArrayList<>();

        List<ToDo> trovati = toDoDAO.cercaToDoAvanzato(usernameCorrente, testo, data);

        for (ToDo todo : trovati) {
            String categoria = todo.getCategoria();
            risultati.add(creaToDoPanelFromToDo(todo, categoria));
        }

        return risultati;
    }

    /* Metodo helper per creare ToDoPanel da ToDo e categoria.
       Si occupa di costruire un pannello grafico (ToDoPanel) che rappresenta visivamente
       un'attività ToDo con tutte le sue proprietà principali, come titolo, data di scadenza,
       descrizione, colore, URL */
    private ToDoPanel creaToDoPanelFromToDo(ToDo todo, String categoria) {
        Color colore;
        try {
            colore = todo.getColore() != null ? todo.getColore() : Color.WHITE;
        } catch (Exception e) {
            colore = Color.WHITE;
        }

        List<String> tutteLeCategorie = Arrays.asList("Università", "Lavoro", "Tempo Libero");

        ToDoPanel panel = new ToDoPanel(
                todo.getTitolo(),
                todo.getDescrizione(),
                todo.getDataScadenza(),
                colore,
                todo.getUrl(),
                todo.getStato() == StatoToDo.Completato,
                todo.getAutore() != null ? todo.getAutore().getUsername() : null,
                categoria,
                tutteLeCategorie,
                creaListener(categoria),
                creaSpostamentoListener(categoria)
        );

        panel.setId(todo.getId());

        return panel;
    }


    // Restituisce i ToDo in scadenza oggi
    public List<ToDoPanel> getToDoScadenzaOggi(String username) {
        LocalDate oggi = LocalDate.now();
        List<ToDoPanel> risultati = new ArrayList<>();

        // Recupera i ToDo scadenza oggi dal DB
        List<ToDo> todos = toDoDAO.getToDoInScadenzaEntro(username, oggi);

        for (ToDo todo : todos) {
            String categoria = todo.getCategoria();

            List<String> tutteLeCategorie = Arrays.asList("Università", "Lavoro", "Tempo Libero");
            ToDoPanel panel = new ToDoPanel(
                    todo.getTitolo(),
                    todo.getDescrizione(),
                    todo.getDataScadenza(),
                    todo.getColore(),
                    todo.getUrl(),
                    todo.getStato() == StatoToDo.Completato,
                    todo.getAutore() != null ? todo.getAutore().getUsername() : null,
                    categoria,
                    tutteLeCategorie,
                    creaListener(categoria),
                    creaSpostamentoListener(categoria)
            );
            panel.setId(todo.getId());
            risultati.add(panel);
        }

        return risultati;
    }


    public void modificaDescrizione(String username, String titolo, String nuovaDescrizione) {
        // Aggiorna la descrizione nel DB
        bachecaDAO.aggiornaDescrizioneBacheca(username, titolo, nuovaDescrizione);
    }

    // Aggiorna la mappa delle condivisioni con i nuovi utenti selezionati
    public void aggiornaCondivisioni(int todoId, Set<String> nuoviUtenti) {
        // Ottieni la lista attuale degli utenti con cui è condiviso il ToDo
        Set<String> utentiAttuali = condivisioneDAO.getUtentiCondivisi(todoId);

        Set<String> daAggiungere = new HashSet<>(nuoviUtenti);
        daAggiungere.removeAll(utentiAttuali);

        Set<String> daRimuovere = new HashSet<>(utentiAttuali);
        daRimuovere.removeAll(nuoviUtenti);

        // Ottieni il ToDo per conoscerne la categoria
        ToDo todo = toDoDAO.getById(todoId);

        if (todo == null) {
            System.err.println("ToDo con ID " + todoId + " non trovato!");
            return;
        }

        String bacheca = todo.getCategoria();
        if (bacheca == null) {
            System.err.println("Categoria (bacheca) del ToDo con ID " + todoId + " è null!");
            return;
        }

        // Aggiungi nuovi utenti
        for (String utente : daAggiungere) {
            condivisioneDAO.inserisciCondivisione(todoId, utente);

            // Calcola nuova posizione
            int maxPos = vistaToDoDAO.getMaxPosizione(utente, bacheca);
            int nuovaPosizione = maxPos + 1;

            // Inserisci nella vista
            vistaToDoDAO.inserisciVistaToDo(todoId, utente, bacheca, nuovaPosizione);
        }

        // Rimuovi utenti non più presenti
        for (String utente : daRimuovere) {
            condivisioneDAO.rimuoviCondivisione(todoId, utente);
            vistaToDoDAO.rimuoviVistaToDo(todoId, utente);
        }
        // Se l'utente corrente è tra quelli appena aggiunti, aggiorna la sua vista
        if (daAggiungere.contains(usernameCorrente)) {
            mostraToDoList(bacheca);
        }

    }

    // Mostra la bacheca con un ToDo selezionato ed evidenziato
    public void apriBachecaConToDoSelezionato(String categoria, ToDoPanel todoDaSelezionare) {
        this.todoSelezionato = todoDaSelezionare;

        if (todoDaSelezionare.getCategoria() == null) {
            todoDaSelezionare.setCategoria(categoria);
        }

        List<ToDo> todos = toDoDAO.getToDoByBacheca(usernameCorrente, categoria);


        List<ToDoPanel> pannelli = new ArrayList<>();
        List<String> tutteLeCategorie = Arrays.asList("Università", "Lavoro", "Tempo Libero");

        for (ToDo todo : todos) {
            String cat = todo.getCategoria() != null ? todo.getCategoria() : categoria;
            ToDoPanel panel = new ToDoPanel(
                    todo.getTitolo(),
                    todo.getDescrizione(),
                    todo.getDataScadenza(),
                    todo.getColore(),
                    todo.getUrl(),
                    todo.getStato() == StatoToDo.Completato,
                    todo.getAutore() != null ? todo.getAutore().getUsername() : null,
                    cat,
                    tutteLeCategorie,
                    creaListener(cat),
                    creaSpostamentoListener(cat)
            );
            panel.setId(todo.getId());
            panel.setToDoActionListener(creaListener(cat));
            panel.spostamentoListener = creaSpostamentoListener(cat);
            pannelli.add(panel);
        }

        ToDoListPanel listaPanel = new ToDoListPanel(categoria, pannelli);
        listaPanel.setBackListener(this::mostraHome);

        for (ToDoPanel panel : pannelli) {
            ToDo t1 = getToDoFromPanel(panel);
            ToDo t2 = getToDoFromPanel(todoDaSelezionare);
            if (t1.equals(t2)) {
                listaPanel.selezionaToDo(panel);
                panel.startBorderAnimation();
                break;
            }
        }

        frame.setContentPane(listaPanel.getPanel());
        frame.revalidate();
        frame.repaint();
    }

    // imposta l'utente dopo il login
    public void setUsernameCorrente(String username) {
        this.usernameCorrente = username;
    }

    public String getUsernameCorrente() {
        return usernameCorrente;
    }

    public JFrame getMainFrame() {
        return frame;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Controller().mostraLogin());

    }
}