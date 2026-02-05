package com.ninotech.eduniger.model.table;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DigitalPrintTable extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "data.db";
    public static final String NAME_TABLE = "DigitalPrint";
    public DigitalPrintTable(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + NAME_TABLE + "\n" +
                "(\n" +
                "    pass VARCHAR(5) PRIMARY KEY\n" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + NAME_TABLE);
        onCreate(db);
    }
    public void onUpdate(String pass)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("UPDATE " + NAME_TABLE + " SET pass='" + pass + "'");
    }
    public Cursor getData()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = null;
        res = db.rawQuery("SELECT * FROM " + NAME_TABLE,null);
        return res;
    }
    public boolean insert (String pass)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("pass",pass);
        db.insert(NAME_TABLE,null,contentValues);
        return  true;
    }
    public String getPass()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT pass FROM " + NAME_TABLE,null);
        res.moveToFirst();
        return res.getString(0);
    }
}
