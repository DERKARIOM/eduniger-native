package com.ninotech.fabi.model.data;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.ninotech.fabi.R;
import com.ninotech.fabi.model.table.AudioTable;
import com.ninotech.fabi.model.table.ElectronicTable;
import com.ninotech.fabi.model.table.LoandTable;
import com.ninotech.fabi.model.table.NotificationTable;
import com.ninotech.fabi.model.table.StudentTable;
import com.ninotech.fabi.model.table.UserTable;

public class Initialization {
    public Initialization(Context context)
    {
        mStudentTable = new StudentTable(context);
        mUserTable = new UserTable(context);
        mElectronicTable = new ElectronicTable(context);
        mNotificationTable = new NotificationTable(context);
        mLoandTable = new LoandTable(context);
        mAudioTable = new AudioTable(context);
    }
    public boolean onCreate(Context context)
    {
        try {
            SQLiteDatabase database = context.openOrCreateDatabase(context.getResources().getString(R.string.database_name),MODE_PRIVATE,null);
            mStudentTable.onCreate(database);
            mUserTable.onCreate(database);
            mElectronicTable.onCreate(database);
            mNotificationTable.onCreate(database);
            mLoandTable.onCreate(database);
            mAudioTable.onCreate(database);
            return true;
        }catch (Exception e)
        {
            return false;
        }
    }
    private final StudentTable mStudentTable;
    private final UserTable mUserTable;
    private final ElectronicTable mElectronicTable;
    private final NotificationTable mNotificationTable;
    private final LoandTable mLoandTable;
    private final AudioTable mAudioTable;
}

