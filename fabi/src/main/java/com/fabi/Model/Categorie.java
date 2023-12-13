package com.fabi.Model;

public class Categorie {
    public Categorie(String ico, String titre) {
        this.ico = ico;
        this.titre = titre;
    }

    public String getIco() {
        return ico;
    }

    public void setIco(String ico) {
        this.ico = ico;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    private String ico;
    private String titre;
}
