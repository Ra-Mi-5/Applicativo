package model;

public enum TitoloBacheca
{
    Università,
    Lavoro,
    Tempo_Libero;

    @Override
    public String toString() {
        return name().replace("_", " ");
    }

}