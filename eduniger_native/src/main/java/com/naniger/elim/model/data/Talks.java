package com.naniger.elim.model.data;

public class Talks {
    public Talks(String profil, String username, String message) {
        this.profil = profil;
        mUsername = username;
        mMessage = message;
    }

    /** Commentaire venant du serveur : identifiants necessaires au signalement / blocage. */
    public Talks(String profil, String username, String message, String idComment, String idUser) {
        this(profil, username, message);
        mIdComment = idComment;
        mIdUser = idUser;
    }

    public String getIdComment() { return mIdComment; }
    public String getIdUser() { return mIdUser; }

    public String getProfil() {
        return profil;
    }

    public void setProfil(String profil) {
        this.profil = profil;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    private String profil;
    private String mUsername;
    private String mMessage;
    private String mIdComment;
    private String mIdUser;
}
