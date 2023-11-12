package com.example.fabi.Controleur.Model;

public class Archiver {
    public String getSemestre() {
        return semestre;
    }

    public void setSemestre(String semestre) {
        this.semestre = semestre;
    }

    public Archiver(String semestre) {
        this.semestre = semestre;
    }

    private String semestre;
}
