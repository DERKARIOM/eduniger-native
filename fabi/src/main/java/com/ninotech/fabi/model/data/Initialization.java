package com.ninotech.fabi.model.data;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.ninotech.fabi.R;
import com.ninotech.fabi.model.table.ElectroniqueTable;
import com.ninotech.fabi.model.table.NotificationTable;
import com.ninotech.fabi.model.table.StudentTable;

public class Initialization {
    public Initialization(Context context)
    {
        mStudentTable = new StudentTable(context);
        mElectroniqueTable = new ElectroniqueTable(context);
        mNotificationTable = new NotificationTable(context);
    }
    public boolean onCreate(Context context)
    {
        try {
            SQLiteDatabase database = context.openOrCreateDatabase(context.getResources().getString(R.string.database_name),MODE_PRIVATE,null);
            mStudentTable.onCreate(database);
            mElectroniqueTable.onCreate(database);
            mNotificationTable.onCreate(database);
            return true;
        }catch (Exception e)
        {
            return false;
        }
    }
    private StudentTable mStudentTable;
    private ElectroniqueTable mElectroniqueTable;
    private NotificationTable mNotificationTable;
}

