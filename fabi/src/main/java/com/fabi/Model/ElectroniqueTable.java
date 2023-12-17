package com.fabi.Model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class ElectroniqueTable extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "data.db";
    public static final String NAME_TABLE = "Electronique";
    public ElectroniqueTable(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS Electronique\n" +
                "(\n" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n" +
                "    matriculeUt VARCHAR(100) NOT NULL,\n" +
                "    idLivre VARCHAR(100) NOT NULL,\n" +
                "    descLivre VARCHAR(100) NOT NULL,\n" +
                "    auteur VARCHAR(100) NOT NULL,\n" +
                "    couverture VARCHAR(100),\n" +
                "    documentElec VARCHAR(100),\n" +
                "    categorie VARCHAR(100),\n" +
                "    titre VARCHAR(100),\n" +
                "    UNIQUE(matriculeUt,idLivre)\n" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS Electronique;");
        onCreate(db);
    }
    public Cursor getData()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM Electronique;",null);
        return res;
    }
    public Cursor getData(String matricule)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM Electronique WHERE matriculeUt='" + matricule + "';",null);
        return res;
    }

    public int getNbrElectronique(String matricule)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT COUNT(*) FROM Electronique WHERE matriculeUt='"+matricule+"';",null);
        res.moveToFirst();
        return res.getInt(0);
    }

    public int getNbrAuteur(String matricule)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT COUNT(DISTINCT auteur) FROM Electronique WHERE matriculeUt='"+matricule+"';",null);
        res.moveToFirst();
        return res.getInt(0);
    }

    public int getNbrCategorie(String matricule)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT COUNT(DISTINCT categorie) FROM Electronique WHERE matriculeUt='"+matricule+"';",null);
        res.moveToFirst();
        return res.getInt(0);
    }
    public boolean insert (String matriculeUt , String idLivre , String descLivre , String auteur ,String couverture, String documentElec , String categorie , String titre)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        try {
            contentValues.put("matriculeUt",matriculeUt);
            contentValues.put("idLivre",idLivre);
            contentValues.put("descLivre",descLivre);
            contentValues.put("auteur",auteur);
            contentValues.put("couverture",couverture);
            contentValues.put("documentElec",documentElec);
            contentValues.put("categorie",categorie);
            contentValues.put("titre",titre);
            db.insert("Electronique",null,contentValues);
            return  true;
        }
        catch (SQLiteException e)
        {
            return false;
        }
    }
}

