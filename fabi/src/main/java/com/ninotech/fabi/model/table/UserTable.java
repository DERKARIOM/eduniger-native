package com.ninotech.fabi.model.table;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ninotech.fabi.R;

public class UserTable extends SQLiteOpenHelper {
    public static final String NAME_TABLE = "Utilisateur";
    public UserTable(Context context) {
        super(context, context.getString(R.string.database_name), null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE IF NOT EXISTS " + NAME_TABLE + "\n" +
                "(\n" +
                "    matriculeUt VARCHAR(10) PRIMARY KEY,\n" +
                "    nomUt VARCHAR(20) NOT NULL,\n" +
                "    prenomUt VARCHAR(20) NOT NULL,\n" +
                "    statusUt VARCHAR(100) NOT NULL,\n" +
                "    email VARCHAR(100) NOT NULL\n" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int i, int i1) {
        database.execSQL("DROP TABLE IF EXISTS " + NAME_TABLE + ";");
        onCreate(database);
    }
    public Cursor getData()
    {
        SQLiteDatabase database = this.getReadableDatabase();
        return database.rawQuery("SELECT * FROM " + NAME_TABLE + ";",null);
    }
    public boolean isUserExist(String idNumber)
    {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + NAME_TABLE + " WHERE matriculeUt=\"" + idNumber + "\";",null);
        return cursor.moveToFirst();
    }
    public boolean insert (String matricule , String nom , String prenom , String status , String email)
    {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("matriculeUt",matricule);
            contentValues.put("nomUt",nom);
            contentValues.put("prenomUt",prenom);
            contentValues.put("statusUt",status);
            contentValues.put("email",email);
            db.insert(NAME_TABLE,null,contentValues);
            return  true;
        }catch (Exception e)
        {
            return false;
        }
    }
}

