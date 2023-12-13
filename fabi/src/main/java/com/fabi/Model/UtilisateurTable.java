package com.fabi.Model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UtilisateurTable extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "data.db";
    public static final String NAME_TABLE = "Utilisateur";
    public UtilisateurTable(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS Utilisateur\n" +
                "(\n" +
                "    matriculeUt VARCHAR(10) PRIMARY KEY,\n" +
                "    nomUt VARCHAR(20) NOT NULL,\n" +
                "    prenomUt VARCHAR(20) NOT NULL,\n" +
                "    statusUt VARCHAR(100) NOT NULL\n" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS Utilisateur;");
        onCreate(db);
    }
    public Cursor getData()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM Utilisateur;",null);
        return res;
    }
    public Cursor getData(String matricule)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM Utilisateur WHERE matriculeUt='" + matricule + "';",null);
        return res;
    }
    public boolean insert (String matricule , String nom , String prenom , String status)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("matriculeUt",matricule);
        contentValues.put("nomUt",nom);
        contentValues.put("prenomUt",prenom);
        contentValues.put("statusUt",status);
        db.insert("Utilisateur",null,contentValues);
        return  true;
    }
}

