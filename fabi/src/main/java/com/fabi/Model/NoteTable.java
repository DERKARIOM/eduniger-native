package com.fabi.Model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class NoteTable extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "data.db";
    public static final String NAME_TABLE = "NoteTable";
    public NoteTable(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS Note\n" +
                "(\n" +
                "     id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n" +
                "     matriculeNEt VARCHAR(10) NOT NULL,\n" +
                "     codeNMat VARCHAR(10) NOT NULL,\n" +
                "     nomMat VARCHAR(20) NOT NULL,\n" +
                "     valeurNot VARCHAR(5),\n" +
                "     dateNot VARCHAR(30) NOT NULL,\n" +
                "     nomAg VARCHAR(50) NOT NULL,\n" +
                "     credit CHAR(2) NOT NULL,\n" +
                "     semestreMat CHAR(2) NOT NULL,\n" +
                "     valeurNotS2 VARCHAR(2)\n," +
                "     UNIQUE (matriculeNEt,codeNMat)" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS Note;");
        onCreate(db);
    }
    public Cursor getData()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM Note;",null);
        return res;
    }
    public Cursor getData(String matriculeEt)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM Note WHERE matriculeNEt='" + matriculeEt + "'ORDER BY id DESC;",null);
        return res;
    }

    public Cursor getData(String matriculeEt , String nomMat)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM Note WHERE matriculeNEt='" + matriculeEt + "' AND nomMat='" + nomMat + "';",null);
        return res;
    }

    public boolean estExiste(String matriculeEt , String codeMat)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = null;
        res = db.rawQuery("SELECT * FROM Note WHERE matriculeNEt='" + matriculeEt + "' AND codeNMat='" + codeMat + "';",null);
        if(res.moveToFirst())
            return true;
        else
            return false;
    }

    public Cursor getDataSemestre(String matriculeEt , String semestreMat)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM Note WHERE matriculeNEt='" + matriculeEt + "' AND semestreMat='" + semestreMat + "'ORDER BY id;",null);
        return res;
    }
    public boolean remove(String codeNMat)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("DELETE FROM Note WHERE codeNMat='" + codeNMat + "';");
        return true;
    }
    public boolean insert (String matriculeNEt , String codeNMat , String nomMat , String valeurNot , String dateNot , String nomAg , String credit , String semestreMat , String valeurNotS2)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("matriculeNEt",matriculeNEt);
        contentValues.put("codeNMat",codeNMat);
        contentValues.put("nomMat",nomMat);
        contentValues.put("valeurNot",valeurNot);
        contentValues.put("dateNot",dateNot);
        contentValues.put("nomAg",nomAg);
        contentValues.put("credit",credit);
        contentValues.put("semestreMat",semestreMat);
        contentValues.put("valeurNotS2",valeurNotS2);
        db.insert("Note",null,contentValues);
        return  true;
    }
    public float totale(String matricule , String semestre)
    {
        Cursor cursor = getDataSemestre(matricule,semestre);
        float t=0;
        cursor.moveToFirst();
        do {

            if(cursor.getString(9).equals("null"))
            {
                if(cursor.getString(4).equals("null"))
                    Log.e("tag","ras");
                else
                    t = t + cursor.getFloat(4)*cursor.getInt(7);
            }
            else
                t = t + cursor.getFloat(9)*cursor.getInt(7);
        }while (cursor.moveToNext());
        return t;
    }
}