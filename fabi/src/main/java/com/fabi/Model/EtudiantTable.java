package com.fabi.Model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class EtudiantTable extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "data.db";
    public static final String NAME_TABLE = "Etudiant";
    public EtudiantTable(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS Etudiant\n" +
                "(\n" +
                "    matriculeEt VARCHAR(10) PRIMARY KEY,\n" +
                "    nomEt VARCHAR(20) NOT NULL,\n" +
                "    prenomEt VARCHAR(20) NOT NULL,\n" +
                "    nomSect VARCHAR(30) NOT NULL,\n" +
                "    nomSemestreI CHAR(2) NOT NULL,\n" +
                "    nomSemestreP CHAR(2),\n" +
                "    delegue CHAR(2)\n" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS Etudiant;");
        onCreate(db);
    }
    public Cursor getData()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM Etudiant;",null);
        return res;
    }
    public Cursor getData(String matricule)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM Etudiant WHERE matriculeEt='" + matricule + "';",null);
        return res;
    }
    public String getSI(String matricule)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT nomSemestreI FROM Etudiant WHERE matriculeEt='" + matricule + "';",null);
        res.moveToFirst();
        return res.getString(0);

    }

    public String getSP(String matricule)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT nomSemestreP FROM Etudiant WHERE matriculeEt='" + matricule + "';",null);
        res.moveToFirst();
        return res.getString(0);

    }
    public String getSection(String matricule)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT nomSect FROM Etudiant WHERE matriculeEt='" + matricule + "';",null);
        res.moveToFirst();
        return res.getString(0);

    }
    public boolean insert (String matricule , String nomEt , String prenomEt , String nomSect ,String nomSemestreI, String nomSemestreP,String delegue)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("matriculeEt",matricule);
        contentValues.put("nomEt",nomEt);
        contentValues.put("prenomEt",prenomEt);
        contentValues.put("nomSect",nomSect);
        contentValues.put("nomSemestreI",nomSemestreI);
        contentValues.put("nomSemestreP",nomSemestreP);
        contentValues.put("delegue",delegue);
        db.insert("Etudiant",null,contentValues);
        return  true;
    }
}

