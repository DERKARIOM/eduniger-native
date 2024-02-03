package com.ninotech.fabi.model.table;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ninotech.fabi.R;

public class StudentTable extends SQLiteOpenHelper {
    public static final String NAME_TABLE = "Student";
    public StudentTable(Context context) {
        super(context, context.getString(R.string.database_name), null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE IF NOT EXISTS " + NAME_TABLE + "\n" +
                "(\n" +
                "    idNumber VARCHAR(10) PRIMARY KEY,\n" +
                "    name VARCHAR(20) NOT NULL,\n" +
                "    firstName VARCHAR(20) NOT NULL,\n" +
                "    department VARCHAR(100) NOT NULL,\n" +
                "    section VARCHAR(100) NOT NULL,\n" +
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
        Cursor cursor = database.rawQuery("SELECT * FROM " + NAME_TABLE + " WHERE idNumber=\"" + idNumber + "\";",null);
        return cursor.moveToFirst();
    }
    public boolean insert (String idNumber , String name , String firstName , String department , String section , String email)
    {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("idNumber",idNumber);
            contentValues.put("name",name);
            contentValues.put("firstName",firstName);
            contentValues.put("department",department);
            contentValues.put("section",section);
            contentValues.put("email",email);
            db.insert(NAME_TABLE,null,contentValues);
            return  true;
        }catch (Exception e)
        {
            return false;
        }
    }
}

