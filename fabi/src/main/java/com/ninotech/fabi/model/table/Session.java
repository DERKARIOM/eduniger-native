package com.ninotech.fabi.model.table;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ninotech.fabi.R;

public class Session extends SQLiteOpenHelper {
    public static final String NAME_TABLE = "Session";
    public Session(Context context) {
        super(context, context.getString(R.string.database_name), null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE IF NOT EXISTS " + NAME_TABLE + "\n" +
                "(\n" +
                "    idNumber VARCHAR(10) PRIMARY KEY,\n" +
                "    password VARCHAR(100)\n" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int i, int i1) {
        database.execSQL("DROP TABLE IF EXISTS " + NAME_TABLE);
        onCreate(database);
    }
    public Cursor getData()
    {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = null;
        cursor = database.rawQuery("SELECT * FROM " + NAME_TABLE,null);
        return cursor;
    }
    public String getIdNumber()
    {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT idNumber FROM " + NAME_TABLE,null);
        cursor.moveToFirst();
        return cursor.getString(0);
    }
    public String getPassword()
    {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT password FROM " + NAME_TABLE,null);
        cursor.moveToFirst();
        return cursor.getString(0);
    }
    public boolean insert (String idNumber,String password)
    {
        try {
            SQLiteDatabase database = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("idNumber",idNumber);
            contentValues.put("password",password);
            database.insert(NAME_TABLE,null,contentValues);
            return  true;
        }catch (Exception e)
        {
            return false;
        }
    }
}
