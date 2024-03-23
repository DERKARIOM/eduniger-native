package com.ninotech.fabi.model.table;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class ElectronicTable extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "data.db";
    public static final String NAME_TABLE = "Electronic";
    public ElectronicTable(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + NAME_TABLE + "\n" +
                "(\n" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n" +
                "    idNumber VARCHAR(100) NOT NULL,\n" +
                "    idBook VARCHAR(100) NOT NULL,\n" +
                "    description VARCHAR(100) NOT NULL,\n" +
                "    author VARCHAR(100) NOT NULL,\n" +
                "    blanketBook BLOB NOT NULL,\n" +
                "    electronic BLOB NOT NULL,\n" +
                "    category VARCHAR(100),\n" +
                "    title VARCHAR(100),\n" +
                "    blanketCategory BLOB NOT NULL,\n" +
                "    profileAuthor BLOB,\n" +
                "    UNIQUE(idNumber,idBook)\n" +
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
        return db.rawQuery("SELECT * FROM " + NAME_TABLE + " WHERE idNumber='" + idNumber + "';",null);
    }

    public int getNbrElectronic(String idNumber)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT COUNT(*) FROM " + NAME_TABLE + " WHERE idNumber='"+idNumber+"';",null);
        res.moveToFirst();
        return res.getInt(0);
    }

    public int getNbrAuthor(String idNumber)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT COUNT(DISTINCT author) FROM " + NAME_TABLE + " WHERE idNumber='"+idNumber+"';",null);
        res.moveToFirst();
        return res.getInt(0);
    }

    public int getNbrCategory(String idNumber)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT COUNT(DISTINCT category) FROM " + NAME_TABLE + " WHERE idNumber='"+idNumber+"';",null);
        res.moveToFirst();
        return res.getInt(0);
    }
    public boolean insert (String idNumber , String idBook , String description , String author ,byte[] blanketBook, byte[] electronic , String category , String title ,byte[] blanketCategory,byte[] profileAuthor)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        try {
            contentValues.put("idNumber",idNumber);
            contentValues.put("idBook",idBook);
            contentValues.put("description",description);
            contentValues.put("author",author);
            contentValues.put("blanketBook",blanketBook);
            contentValues.put("electronic",electronic);
            contentValues.put("category",category);
            contentValues.put("title",title);
            contentValues.put("blanketCategory",blanketCategory);
            contentValues.put("profileAuthor",profileAuthor);
            db.insert(NAME_TABLE,null,contentValues);
            return  true;
        }
        catch (SQLiteException e)
        {
            return false;
        }
    }
}

