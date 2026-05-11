package com.ninotech.eduniger.model.data;

public class Message {

    public static final int TYPE_USER = 0;
    public static final int TYPE_BOT  = 1;

    private long   id;
    private long   sessionDbId;
    private String text;
    private int    type;
    private long   timestamp;
    private String coverUrl; // ← AJOUT : URL complète de la couverture (null si absent)

    // ── Constructeur utilisé à l'affichage (en mémoire) ──────────────────────
    public Message(String text, int type) {
        this.text      = text;
        this.type      = type;
        this.timestamp = System.currentTimeMillis();
    }

    // ── Constructeur complet (chargement depuis SQLite) ───────────────────────
    public Message(long id, long sessionDbId, String text, int type, long timestamp) {
        this.id          = id;
        this.sessionDbId = sessionDbId;
        this.text        = text;
        this.type        = type;
        this.timestamp   = timestamp;
    }

    // ── Getters / Setters existants ───────────────────────────────────────────
    public long   getId()                        { return id; }
    public void   setId(long id)                 { this.id = id; }
    public long   getSessionDbId()               { return sessionDbId; }
    public void   setSessionDbId(long v)         { this.sessionDbId = v; }
    public String getText()                      { return text; }
    public void   setText(String text)           { this.text = text; }
    public int    getType()                      { return type; }
    public void   setType(int type)              { this.type = type; }
    public long   getTimestamp()                 { return timestamp; }
    public void   setTimestamp(long ts)          { this.timestamp = ts; }

    // ── AJOUT ─────────────────────────────────────────────────────────────────
    public String getCoverUrl()                  { return coverUrl; }
    public void   setCoverUrl(String coverUrl)   { this.coverUrl = coverUrl; }
}