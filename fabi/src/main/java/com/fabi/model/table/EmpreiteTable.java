package com.fabi.model.table;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class EmpreiteTable extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "data.db";
    public static final String NAME_TABLE = "Empreinte";
    public EmpreiteTable(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS Empreinte\n" +
                "(\n" +
                "    passe VARCHAR(5) PRIMARY KEY\n" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS Empreinte");
        onCreate(db);
    }
    public void onUpdate(String passe)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("UPDATE Empreinte SET passe='" + passe + "'");
    }
    public Cursor getData()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = null;
        res = db.rawQuery("SELECT * FROM Empreinte",null);
        return res;
    }
    public boolean insert (String passe)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("passe",passe);
        db.insert("Empreinte",null,contentValues);
        return  true;
    }
    public String getPasse()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM Empreinte",null);
        res.moveToFirst();
        return res.getString(0);
    }
}
