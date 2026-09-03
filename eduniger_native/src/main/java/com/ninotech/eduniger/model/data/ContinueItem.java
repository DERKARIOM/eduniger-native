package com.ninotech.eduniger.model.data;

/**
 * Une entrée d'écoute : sert à la fois au carrousel « Reprendre l'écoute »,
 * à la liste des favoris et à l'historique — les trois vues lisent les mêmes
 * colonnes de PlaybackTable (jointes à la table Audio).
 */
public class ContinueItem {

    private final String idBook;
    private final String cover;
    private final String title;
    private final String author;
    private final String audio;
    private final String durationLabel;
    private final int    positionMs;
    private final int    durationMs;
    private final boolean favorite;
    private final long   updatedAt;

    public ContinueItem(String idBook, String cover, String title, String author, String audio,
                        String durationLabel, int positionMs, int durationMs,
                        boolean favorite, long updatedAt) {
        this.idBook        = idBook;
        this.cover         = cover;
        this.title         = title;
        this.author        = author;
        this.audio         = audio;
        this.durationLabel = durationLabel;
        this.positionMs    = positionMs;
        this.durationMs    = durationMs;
        this.favorite      = favorite;
        this.updatedAt     = updatedAt;
    }

    public String getIdBook()        { return idBook; }
    public String getCover()         { return cover; }
    public String getTitle()         { return title; }
    public String getAuthor()        { return author; }
    public String getAudio()         { return audio; }
    public String getDurationLabel() { return durationLabel; }
    public int    getPositionMs()    { return positionMs; }
    public int    getDurationMs()    { return durationMs; }
    public boolean isFavorite()      { return favorite; }
    public long   getUpdatedAt()     { return updatedAt; }

    /** Progression 0..1000 (échelle de ProgressBar), 0 si la durée est inconnue. */
    public int getProgressPerMille() {
        if (durationMs <= 0) return 0;
        return (int) Math.min(1000L, 1000L * positionMs / durationMs);
    }

    /** Minutes restantes, arrondies vers le haut ; -1 si durée inconnue. */
    public int getRemainingMinutes() {
        if (durationMs <= 0) return -1;
        int remainingMs = Math.max(0, durationMs - positionMs);
        return (int) Math.ceil(remainingMs / 60000.0);
    }
}
