package com.ninotech.eduniger.model.data;

public class Track {
    public Track(String idBook, String title, String artist, int image) {
        this.idBook = idBook;
        this.title = title;
        this.artist = artist;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Track(String idBook ,String cover , String title, String artist, String audio ,  String time, int image) {
        this.idBook = idBook;
        this.cover = cover;
        this.title = title;
        this.artist = artist;
        this.audio = audio;
        this.time = time;
        this.image = image;
    }
    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }
    public String getIdBook() {
        return idBook;
    }

    public void setIdBook(String idBook) {
        this.idBook = idBook;
    }

    private String cover;
    private String title;
    private String artist;
    private String audio;
    private String time;
    private int image;
    private String idBook;
}
