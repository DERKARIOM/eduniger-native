package com.ninotech.eduniger.model.table;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NotificationTable extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "data.db";
    public static final String NAME_TABLE    = "Notification";

    // ← version 2 pour déclencher onUpgrade et ajouter la colonne isRead
    private static final int DB_VERSION = 1;

    public NotificationTable(Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + NAME_TABLE + " (" +
                "idNotification INTEGER PRIMARY KEY AUTOINCREMENT," +
                "idNumberNotif  VARCHAR(100)   NOT NULL," +
                "titleNotif     VARCHAR(100)   NOT NULL," +
                "messageNotif   VARCHAR(1000)  NOT NULL," +
                "dateNotif      VARCHAR(1000)  NOT NULL," +
                "link           VARCHAR(10000) DEFAULT NULL," +
                "idBookLink     VARCHAR(1000)  DEFAULT NULL," +
                "typeNotif      VARCHAR(1000)  NOT NULL," +
                "isRead         INTEGER        NOT NULL DEFAULT 0" +   // ← nouveau
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Ajout non destructif de la colonne isRead
            db.execSQL("ALTER TABLE " + NAME_TABLE + " ADD COLUMN isRead INTEGER NOT NULL DEFAULT 0");
        }
    }

    // ── Lecture ─────────────────────────────────────────────────────

    public Cursor getData() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + NAME_TABLE, null);
    }

    public Cursor getData(String idNumber) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + NAME_TABLE +
                        " WHERE idNumberNotif='" + idNumber + "' ORDER BY idNotification DESC",
                null);
    }

    // ── Comptage des non-lues ────────────────────────────────────────

    public int getUnreadCount(String idNumber) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + NAME_TABLE +
                        " WHERE idNumberNotif='" + idNumber + "' AND isRead=0",
                null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    // ── Marquer toutes les notifs d'un user comme lues ───────────────

    public void markAllAsRead(String idNumber) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("isRead", 1);
        db.update(NAME_TABLE, values, "idNumberNotif=?", new String[]{idNumber});
    }

    // ── Suppression ──────────────────────────────────────────────────

    public boolean remove(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("DELETE FROM " + NAME_TABLE + " WHERE idNotification=" + id);
        return true;
    }

    // ── Insertion (isRead = 0 par défaut) ────────────────────────────

    public boolean insert(String idNumber, String title, String date,
                          String message, String link, String bookLink, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("idNumberNotif", idNumber);
        contentValues.put("titleNotif",    title);
        contentValues.put("dateNotif",     date);
        contentValues.put("messageNotif",  message);
        contentValues.put("link",          link);
        contentValues.put("idBookLink",    bookLink);
        contentValues.put("typeNotif",     type);
        contentValues.put("isRead",        0);          // ← toujours non-lue à l'insertion
        db.insert(NAME_TABLE, null, contentValues);
        return true;
    }
}