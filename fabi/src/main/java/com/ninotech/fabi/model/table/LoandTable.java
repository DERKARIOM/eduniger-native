package com.ninotech.fabi.model.table;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ninotech.fabi.R;

public class LoandTable extends SQLiteOpenHelper {
    public static final String NAME_TABLE = "Loand";
    public LoandTable(Context context) {
        super(context, context.getString(R.string.database_name), null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE IF NOT EXISTS " + NAME_TABLE + "\n" +
                "(\n" +
                "    idLoand VARCHAR(10) PRIMARY KEY,\n" +
                "    idNumberLoand VARCHAR(10),\n" +
                "    blanketLoand VARCHAR(100),\n" +
                "    titleLoand VARCHAR(100),\n" +
                "    dateLoand VARCHAR(100),\n" +
                "    realReturnDate VARCHAR(100)\n" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int i, int i1) {
        database.execSQL("DROP TABLE IF EXISTS " + NAME_TABLE);
        onCreate(database);
    }
    public int getNbrLoand(String idNumber)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT COUNT(*) FROM " + NAME_TABLE + " WHERE idNumberLoand='"+idNumber+"';",null);
        res.moveToFirst();
        return res.getInt(0);
    }
    public Cursor getData()
    {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = null;
        cursor = database.rawQuery("SELECT * FROM " + NAME_TABLE,null);
        return cursor;
    }
    public boolean remove(String idLoand)
    {
        SQLiteDatabase database = this.getReadableDatabase();
        database.execSQL("DELETE FROM " + NAME_TABLE + " WHERE idLoand=\"" + idLoand + "\"");
        return true;
    }
    public boolean insert (String idLoand,String idNumber , String blanket , String title , String dateLoand , String realReturnDate)
    {
        try {
            SQLiteDatabase database = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("idLoand",idLoand);
            contentValues.put("idNumberLoand",idNumber);
            contentValues.put("blanketLoand",blanket);
            contentValues.put("titleLoand",title);
            contentValues.put("dateLoand",dateLoand);
            contentValues.put("realReturnDate",realReturnDate);
            database.insert(NAME_TABLE,null,contentValues);
            return  true;
        }catch (Exception e)
        {
            return false;
        }
    }
}
