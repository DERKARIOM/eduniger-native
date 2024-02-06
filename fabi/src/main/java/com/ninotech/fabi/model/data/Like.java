package com.ninotech.fabi.model.data;

public class Like {
    public Like(String idNumber, String idBook, boolean isLike, boolean isNoLike) {
        this.idNumber = idNumber;
        this.idBook = idBook;
        this.isLike = isLike;
        this.isNoLike = isNoLike;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getIdBook() {
        return idBook;
    }

    public void setIdBook(String idBook) {
        this.idBook = idBook;
    }

    public boolean isLike() {
        return isLike;
    }

    public void setLike(boolean like) {
        isLike = like;
    }

    public boolean isNoLike() {
        return isNoLike;
    }

    public void setNoLike(boolean noLike) {
        isNoLike = noLike;
    }

    private String idNumber;
    private String idBook;
    private boolean isLike;
    private boolean isNoLike;
}
