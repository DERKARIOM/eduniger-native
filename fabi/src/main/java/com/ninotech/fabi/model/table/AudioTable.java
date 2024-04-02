package com.ninotech.fabi.model.table;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class AudioTable extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "data.db";
    public static final String NAME_TABLE = "Audio";
    public AudioTable(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + NAME_TABLE + "\n" +
                "(\n" +
                "    idAudio INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n" +
                "    idNumberAudio VARCHAR(100) NOT NULL,\n" +
                "    idBookAudio VARCHAR(100) NOT NULL,\n" +
                "    descriptionAudio VARCHAR(100) NOT NULL,\n" +
                "    authorAudio VARCHAR(100) NOT NULL,\n" +
                "    blanketBookAudio BLOB NOT NULL,\n" +
                "    audio VARCHAR(100),\n" +
                "    categoryAudio VARCHAR(100),\n" +
                "    titleAudio VARCHAR(100),\n" +
                "    blanketCategoryAudio BLOB NOT NULL,\n" +
                "    profileAuthorAudio BLOB,\n" +
                "    durationAudio VARCHAR(100) NOT NULL,\n" +
                "    UNIQUE(idNumberAudio,idBookAudio)\n" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + NAME_TABLE);
        onCreate(db);
    }
    public Cursor getData()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + NAME_TABLE,null);
    }
    public Cursor getData(String idNumber)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + NAME_TABLE + " WHERE idNumberAudio='" + idNumber + "';",null);
    }
    public Cursor getData(String idNumber , String idBook)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + NAME_TABLE + " WHERE idNumberAudio='" + idNumber + "' AND idBookAudio='" + idBook + "';",null);
    }
    public Cursor getCategoryData(String idNumber)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT DISTINCT blanketCategory,category  FROM " + NAME_TABLE + " WHERE idNumberAudio='" + idNumber + "';",null);
    }
    public Cursor getAuthorData(String idNumber)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT DISTINCT profileAuthor,author  FROM " + NAME_TABLE + " WHERE idNumberAudio='" + idNumber + "';",null);
    }

    public int getNbrAudio(String idNumber)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT COUNT(*) FROM " + NAME_TABLE + " WHERE idNumberAudio='"+idNumber+"';",null);
        res.moveToFirst();
        return res.getInt(0);
    }

    public int getNbrAuthor(String idNumber)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT COUNT(DISTINCT authorAudio) FROM " + NAME_TABLE + " WHERE idNumberAudio='"+idNumber+"';",null);
        res.moveToFirst();
        return res.getInt(0);
    }

    public int getNbrCategory(String idNumber)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT COUNT(DISTINCT category) FROM " + NAME_TABLE + " WHERE idNumberAudio='"+idNumber+"';",null);
        res.moveToFirst();
        return res.getInt(0);
    }
    public boolean insert (String idNumber , String idBook , String description , String author ,byte[] blanketBook, String audio , String category , String title ,byte[] blanketCategory,byte[] profileAuthor , String duration)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        try {
            contentValues.put("idNumberAudio",idNumber);
            contentValues.put("idBookAudio",idBook);
            contentValues.put("descriptionAudio",description);
            contentValues.put("authorAudio",author);
            contentValues.put("blanketBookAudio",blanketBook);
            contentValues.put("audio",audio);
            contentValues.put("categoryAudio",category);
            contentValues.put("titleAudio",title);
            contentValues.put("blanketCategoryAudio",blanketCategory);
            contentValues.put("profileAuthorAudio",profileAuthor);
            contentValues.put("durationAudio",duration);
            db.insert(NAME_TABLE,null,contentValues);
            return  true;
        }
        catch (SQLiteException e)
        {
            return false;
        }
    }
}

