package model;

public class Utente {

    private final String username;
    private String password;

    public Utente(String username)
    {
        this.username = username;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }
}