package com.fabi.Controleur;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.fabi.Model.Notification;
import com.example.fabi.R;
import com.fabi.Model.NotificationAdapter;
import com.fabi.Model.NotificationTable;
import com.fabi.Model.Session;

import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        getSupportActionBar().hide();
        // Activer le bouton de retour de l'action barre
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyler2);
        mSession = new Session(this);
        mNotificationTable = new NotificationTable(this);
        mList = new ArrayList<Notification>();
        Cursor cursor = mNotificationTable.getData(mSession.getMatricule());
        cursor.moveToFirst();
        try {
            do {
                mList.add(new Notification(cursor.getString(2),cursor.getString(3),cursor.getString(4)));
            }while(cursor.moveToNext());
        }catch (Exception e)
        {
            Log.e("ErrGetDataNotification",e.getMessage());
        }
        mNotificationAdapter = new NotificationAdapter(mList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getApplicationContext()));
        mRecyclerView.setAdapter(mNotificationAdapter);
        mRecyclerView.smoothScrollToPosition(mNotificationAdapter.getItemCount()-1);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Gérer les clics sur les éléments de l'action barre
        switch (item.getItemId()) {
            case android.R.id.home:
                // Appeler onBackPressed() lorsque le bouton de retour de l'action barre est pressé
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        // Revenir en arrière tout simplement
        super.onBackPressed();
    }
    private RecyclerView mRecyclerView;
    private NotificationAdapter mNotificationAdapter;
    private ArrayList<Notification> mList;
    private NotificationTable mNotificationTable;
    private Session mSession;
}