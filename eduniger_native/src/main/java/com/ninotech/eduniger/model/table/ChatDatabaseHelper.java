package com.ninotech.eduniger.model.table;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ninotech.eduniger.model.data.ChatSession;
import com.ninotech.eduniger.model.data.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestionnaire SQLite pour les sessions de chat et leurs messages.
 *
 * Tables :
 *   chat_sessions  — une ligne par conversation
 *   messages       — tous les messages, liés à une session
 */
public class ChatDatabaseHelper extends SQLiteOpenHelper {

    // ─── Méta-base ────────────────────────────────────────────────────────────
    private static final String DB_NAME    = "eduniger_chat.db";
    private static final int    DB_VERSION = 1;

    // ─── Table chat_sessions ──────────────────────────────────────────────────
    public static final String TABLE_SESSIONS   = "chat_sessions";
    public static final String COL_S_ID         = "_id";
    public static final String COL_S_UUID       = "session_uuid";
    public static final String COL_S_TITLE      = "title";
    public static final String COL_S_CREATED_AT = "created_at";
    public static final String COL_S_UPDATED_AT = "updated_at";

    private static final String CREATE_SESSIONS =
            "CREATE TABLE " + TABLE_SESSIONS + " (" +
                    COL_S_ID         + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_S_UUID       + " TEXT NOT NULL, "                     +
                    COL_S_TITLE      + " TEXT, "                              +
                    COL_S_CREATED_AT + " INTEGER NOT NULL, "                  +
                    COL_S_UPDATED_AT + " INTEGER NOT NULL"                    +
                    ");";

    // ─── Table messages ───────────────────────────────────────────────────────
    public static final String TABLE_MESSAGES   = "messages";
    public static final String COL_M_ID         = "_id";
    public static final String COL_M_SESSION_ID = "session_id";   // FK
    public static final String COL_M_TEXT       = "text";
    public static final String COL_M_TYPE       = "type";          // 0=USER, 1=BOT
    public static final String COL_M_TIMESTAMP  = "timestamp";

    private static final String CREATE_MESSAGES =
            "CREATE TABLE " + TABLE_MESSAGES + " (" +
                    COL_M_ID         + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_M_SESSION_ID + " INTEGER NOT NULL, "                  +
                    COL_M_TEXT       + " TEXT NOT NULL, "                     +
                    COL_M_TYPE       + " INTEGER NOT NULL, "                  +
                    COL_M_TIMESTAMP  + " INTEGER NOT NULL, "                  +
                    "FOREIGN KEY(" + COL_M_SESSION_ID + ") REFERENCES "      +
                    TABLE_SESSIONS + "(" + COL_S_ID + ") ON DELETE CASCADE" +
                    ");";

    // ─── Singleton ────────────────────────────────────────────────────────────
    private static ChatDatabaseHelper instance;

    public static synchronized ChatDatabaseHelper getInstance(Context ctx) {
        if (instance == null) {
            instance = new ChatDatabaseHelper(ctx.getApplicationContext());
        }
        return instance;
    }

    private ChatDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // ─── Cycle de vie ─────────────────────────────────────────────────────────
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SESSIONS);
        db.execSQL(CREATE_MESSAGES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSIONS);
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true); // active ON DELETE CASCADE
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  SESSIONS
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Insère une nouvelle session et retourne son id auto-généré.
     */
    public long insertSession(ChatSession session) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_S_UUID,       session.getSessionUuid());
        cv.put(COL_S_TITLE,      session.getTitle());
        cv.put(COL_S_CREATED_AT, session.getCreatedAt());
        cv.put(COL_S_UPDATED_AT, session.getUpdatedAt());
        long id = db.insert(TABLE_SESSIONS, null, cv);
        session.setId(id);
        return id;
    }

    /**
     * Met à jour le titre et la date de dernière modification d'une session.
     */
    public void updateSession(long sessionId, String newTitle) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_S_TITLE,      newTitle);
        cv.put(COL_S_UPDATED_AT, System.currentTimeMillis());
        db.update(TABLE_SESSIONS, cv, COL_S_ID + "=?",
                new String[]{ String.valueOf(sessionId) });
    }

    /**
     * Supprime une session ET tous ses messages (CASCADE).
     */
    public void deleteSession(long sessionId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_SESSIONS, COL_S_ID + "=?",
                new String[]{ String.valueOf(sessionId) });
    }

    /**
     * Retourne toutes les sessions triées par date décroissante.
     */
    public List<ChatSession> getAllSessions() {
        List<ChatSession> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(
                TABLE_SESSIONS, null, null, null, null, null,
                COL_S_UPDATED_AT + " DESC"
        );
        while (c.moveToNext()) {
            list.add(sessionFromCursor(c));
        }
        c.close();
        return list;
    }

    /**
     * Retourne une session par son UUID réseau.
     */
    public ChatSession getSessionByUuid(String uuid) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(
                TABLE_SESSIONS, null,
                COL_S_UUID + "=?", new String[]{ uuid },
                null, null, null, "1"
        );
        ChatSession s = null;
        if (c.moveToFirst()) s = sessionFromCursor(c);
        c.close();
        return s;
    }

    private ChatSession sessionFromCursor(Cursor c) {
        return new ChatSession(
                c.getLong  (c.getColumnIndexOrThrow(COL_S_ID)),
                c.getString(c.getColumnIndexOrThrow(COL_S_UUID)),
                c.getString(c.getColumnIndexOrThrow(COL_S_TITLE)),
                c.getLong  (c.getColumnIndexOrThrow(COL_S_CREATED_AT)),
                c.getLong  (c.getColumnIndexOrThrow(COL_S_UPDATED_AT))
        );
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  MESSAGES
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Insère un message et affecte son id auto-généré au modèle.
     */
    public long insertMessage(Message message) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_M_SESSION_ID, message.getSessionDbId());
        cv.put(COL_M_TEXT,       message.getText());
        cv.put(COL_M_TYPE,       message.getType());
        cv.put(COL_M_TIMESTAMP,  message.getTimestamp());
        long id = db.insert(TABLE_MESSAGES, null, cv);
        message.setId(id);
        return id;
    }

    /**
     * Retourne tous les messages d'une session dans l'ordre chronologique.
     */
    public List<Message> getMessagesForSession(long sessionDbId) {
        List<Message> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(
                TABLE_MESSAGES, null,
                COL_M_SESSION_ID + "=?", new String[]{ String.valueOf(sessionDbId) },
                null, null, COL_M_TIMESTAMP + " ASC"
        );
        while (c.moveToNext()) {
            list.add(messageFromCursor(c));
        }
        c.close();
        return list;
    }

    /**
     * Supprime un seul message.
     */
    public void deleteMessage(long messageId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_MESSAGES, COL_M_ID + "=?",
                new String[]{ String.valueOf(messageId) });
    }

    private Message messageFromCursor(Cursor c) {
        return new Message(
                c.getLong  (c.getColumnIndexOrThrow(COL_M_ID)),
                c.getLong  (c.getColumnIndexOrThrow(COL_M_SESSION_ID)),
                c.getString(c.getColumnIndexOrThrow(COL_M_TEXT)),
                c.getInt   (c.getColumnIndexOrThrow(COL_M_TYPE)),
                c.getLong  (c.getColumnIndexOrThrow(COL_M_TIMESTAMP))
        );
    }
}