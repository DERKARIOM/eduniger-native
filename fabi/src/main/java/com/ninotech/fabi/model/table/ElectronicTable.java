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
                "    idElectronic INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n" +
                "    idNumberElectronic VARCHAR(100) NOT NULL,\n" +
                "    idBookElectronic VARCHAR(100) NOT NULL,\n" +
                "    descriptionElectronic VARCHAR(100) NOT NULL,\n" +
                "    authorElectronic VARCHAR(100) NOT NULL,\n" +
                "    coverElectronic VARCHAR(100) NOT NULL,\n" +
                "    electronic VARCHAR(100) NOT NULL,\n" +
                "    categoryElectronic VARCHAR(100),\n" +
                "    titleElectronic VARCHAR(100),\n" +
                "    coverCategoryElectronic VARCHAR(100) NOT NULL,\n" +
                "    profileAuthorElectronic VARCHAR(100),\n" +
                "    UNIQUE(idNumberElectronic,idBookElectronic)\n" +
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
        return db.rawQuery("SELECT * FROM " + NAME_TABLE + " WHERE idNumberElectronic='" + idNumber + "';",null);
    }
    public Cursor getCategoryData(String idNumber)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT DISTINCT coverCategoryElectronic,categoryElectronic  FROM " + NAME_TABLE + " WHERE idNumberElectronic='" + idNumber + "';",null);
    }
    public Cursor getAuthorData(String idNumber)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT DISTINCT profileAuthorElectronic,authorElectronic  FROM " + NAME_TABLE + " WHERE idNumberElectronic='" + idNumber + "';",null);
    }
    public String getPdf(String idNumber)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT electronic FROM " + NAME_TABLE + " WHERE idBookElectronic='"+idNumber+"';",null);
        res.moveToFirst();
        return res.getString(0);
    }

    public int getNbrElectronic(String idNumber)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT COUNT(*) FROM " + NAME_TABLE + " WHERE idNumberElectronic='"+idNumber+"';",null);
        res.moveToFirst();
        return res.getInt(0);
    }

    public int getNbrAuthor(String idNumber)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT COUNT(DISTINCT authorElectronic) FROM " + NAME_TABLE + " WHERE idNumberElectronic='"+idNumber+"';",null);
        res.moveToFirst();
        return res.getInt(0);
    }

    public int getNbrCategory(String idNumber)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT COUNT(DISTINCT categoryElectronic) FROM " + NAME_TABLE + " WHERE idNumberElectronic='"+idNumber+"';",null);
        res.moveToFirst();
        return res.getInt(0);
    }
    public boolean remove(String idNumber , String idBook)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("DELETE FROM " + NAME_TABLE + " WHERE idNumberElectronic='" + idNumber + "' AND idBookElectronic='" + idBook + "';");
        return true;
    }
    public boolean insert (String idNumber , String idBook , String description , String author ,String cover, String electronic , String category , String title ,String coverCategory,String profileAuthor)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        try {
            contentValues.put("idNumberElectronic",idNumber);
            contentValues.put("idBookElectronic",idBook);
            contentValues.put("descriptionElectronic",description);
            contentValues.put("authorElectronic",author);
            contentValues.put("coverElectronic",cover);
            contentValues.put("electronic",electronic);
            contentValues.put("categoryElectronic",category);
            contentValues.put("titleElectronic",title);
            contentValues.put("coverCategoryElectronic",coverCategory);
            contentValues.put("profileAuthorElectronic",profileAuthor);
            db.insert(NAME_TABLE,null,contentValues);
            return  true;
        }
        catch (SQLiteException e)
        {
            return false;
        }
    }
}

