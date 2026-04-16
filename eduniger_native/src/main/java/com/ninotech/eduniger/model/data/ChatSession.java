package com.ninotech.eduniger.model.data;

/**
 * Représente une conversation sauvegardée en base locale.
 */
public class ChatSession {

    private long   id;
    private String sessionUuid;   // UUID envoyé à l'API
    private String title;         // Premier message utilisateur (tronqué)
    private long   createdAt;
    private long   updatedAt;

    // ── Constructeur création ─────────────────────────────────────────────────
    public ChatSession(String sessionUuid, String title) {
        this.sessionUuid = sessionUuid;
        this.title       = title;
        this.createdAt   = System.currentTimeMillis();
        this.updatedAt   = this.createdAt;
    }

    // ── Constructeur chargement ───────────────────────────────────────────────
    public ChatSession(long id, String sessionUuid, String title,
                       long createdAt, long updatedAt) {
        this.id          = id;
        this.sessionUuid = sessionUuid;
        this.title       = title;
        this.createdAt   = createdAt;
        this.updatedAt   = updatedAt;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────
    public long   getId()                     { return id; }
    public void   setId(long id)              { this.id = id; }

    public String getSessionUuid()            { return sessionUuid; }
    public void   setSessionUuid(String uuid) { this.sessionUuid = uuid; }

    public String getTitle()                  { return title; }
    public void   setTitle(String title)      { this.title = title; }

    public long   getCreatedAt()              { return createdAt; }
    public void   setCreatedAt(long ts)       { this.createdAt = ts; }

    public long   getUpdatedAt()              { return updatedAt; }
    public void   setUpdatedAt(long ts)       { this.updatedAt = ts; }
}