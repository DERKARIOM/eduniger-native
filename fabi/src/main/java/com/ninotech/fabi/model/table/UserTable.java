package com.ninotech.fabi.model.table;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ninotech.fabi.R;

public class UserTable extends SQLiteOpenHelper {
    public static final String NAME_TABLE = "User";
    public UserTable(Context context) {
        super(context, context.getString(R.string.database_name), null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE IF NOT EXISTS " + NAME_TABLE + "\n" +
                "(\n" +
                "    idUser VARCHAR(256) PRIMARY KEY,\n" +
                "    nameUser VARCHAR(256) NOT NULL,\n" +
                "    firstNameUser VARCHAR(256) NOT NULL,\n" +
                "    emailUser VARCHAR(256) NOT NULL,\n" +
                "    password VARCHAR(256) NOT NULL,\n" +
                "    professionUser VARCHAR(256) NOT NULL,\n" +
                "    profileUser BLOB,\n" +
                "    isAdminUser VARCHAR(256)\n" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int i, int i1) {
        database.execSQL("DROP TABLE IF EXISTS " + NAME_TABLE + ";");
        onCreate(database);
    }
    public void delete() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + NAME_TABLE);
        onCreate(db);
    }
    public byte[] getPhoto(String number)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor=db.rawQuery("SELECT profileUser FROM User WHERE idUser='"+number+"';",null);
        cursor.moveToFirst();
        return cursor.getBlob(0);
    }
    public boolean update(String idNumber,String column , String newValues)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(column,newValues);
        db.update(NAME_TABLE,contentValues,"idUser = ?",new String[]{idNumber});
        return true;
    }
    public Cursor getData()
    {
        SQLiteDatabase database = this.getReadableDatabase();
        return database.rawQuery("SELECT * FROM " + NAME_TABLE + ";",null);
    }
    public Cursor getData(String idUser)
    {
        SQLiteDatabase database = this.getReadableDatabase();
        return database.rawQuery("SELECT * FROM " + NAME_TABLE + " WHERE idUser=\"" + idUser + "\";",null);
    }
    public boolean isUserExist(String idUser)
    {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + NAME_TABLE + " WHERE idUser=\"" + idUser + "\";",null);
        return cursor.moveToFirst();
    }

    public String getProfession(String idUser)
    {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT professionUser FROM " + NAME_TABLE + " WHERE idUser='" + idUser + "'",null);
        cursor.moveToFirst();
        return cursor.getString(0);
    }
    public String getIsAuthor(String idUser)
    {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT isAdminUser FROM " + NAME_TABLE + " WHERE idUser='" + idUser + "'",null);
        cursor.moveToFirst();
        return cursor.getString(0);
    }
    public void setPhoto(byte[] photo)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("profileUser",photo);
        db.update(NAME_TABLE,contentValues,null,null);
    }
    public boolean insert (String idUser , String name , String firstName , String email , String password, byte[] profile , String profession ,String isAdmin)
    {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("idUser",idUser);
            contentValues.put("nameUser",name);
            contentValues.put("firstNameUser",firstName);
            contentValues.put("professionUser",profession);
            contentValues.put("emailUser",email);
            contentValues.put("password",password);
            contentValues.put("profileUser",profile);
            contentValues.put("isAdminUser",isAdmin);
            db.insert(NAME_TABLE,null,contentValues);
            return  true;
        }catch (Exception e)
        {
            return false;
        }
    }
}

