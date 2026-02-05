package com.ninotech.eduniger.model.table;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NotificationTable extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "data.db";
    public static final String NAME_TABLE = "Notification";
    public NotificationTable(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + NAME_TABLE +"\n" +
                "(\n" +
                "    idNotification INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "    idNumberNotif VARCHAR(100) NOT NULL,\n" +
                "    titleNotif VARCHAR(100) NOT NULL,\n" +
                "    messageNotif VARCHAR(1000) NOT NULL,\n" +
                "    dateNotif VARCHAR(1000) NOT NULL," +
                "    link VARCHAR(10000) DEFAULT NULL," +
                "    idBookLink VARCHAR(1000) DEFAULT NULL," +
                "    typeNotif VARCHAR(1000) NOT NULL\n" +
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
        Cursor res = db.rawQuery("SELECT * FROM " + NAME_TABLE,null);
        return res;
    }
    public Cursor getData(String idNumber)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + NAME_TABLE + " WHERE idNumberNotif='" + idNumber + "' ORDER BY idNotification DESC;", null);
        return res;
    }
    public boolean remove(String id)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("DELETE FROM " + NAME_TABLE + " WHERE idNotification=" + id);
        return true;
    }
    public boolean insert (String idNumber , String title , String date , String message , String link , String bookLink, String type)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("idNumberNotif",idNumber);
        contentValues.put("titleNotif",title);
        contentValues.put("dateNotif",date);
        contentValues.put("messageNotif",message);
        contentValues.put("link",link);
        contentValues.put("idBookLink",bookLink);
        contentValues.put("typeNotif",type);
        db.insert(NAME_TABLE,null,contentValues);
        return  true;
    }
}


