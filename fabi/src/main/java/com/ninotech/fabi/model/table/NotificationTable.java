package com.ninotech.fabi.model.table;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NotificationTable extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "data.db";
    public static final String NAME_TABLE = "Notification";
    public NotificationTable(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS Notification\n" +
                "(\n" +
                "    idNotification INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "    notifMatriculeUt VARCHAR(100) NOT NULL,\n" +
                "    notifTitre VARCHAR(100) NOT NULL,\n" +
                "    notifMessage VARCHAR(100) NOT NULL,\n" +
                "    notifDate VARCHAR(100) NOT NULL\n" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS Notification;");
        onCreate(db);
    }
    public Cursor getData()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM Notification;",null);
        return res;
    }
    public Cursor getData(String matricule)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM Notification WHERE notifMatriculeUt='" + matricule + "';",null);
        return res;
    }
    public boolean insert (String matricule , String titre , String message , String date)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("NotifMatriculeUt",matricule);
        contentValues.put("notifTitre",titre);
        contentValues.put("notifMessage",message);
        contentValues.put("notifDate",date);
        db.insert("Notification",null,contentValues);
        return  true;
    }
}

