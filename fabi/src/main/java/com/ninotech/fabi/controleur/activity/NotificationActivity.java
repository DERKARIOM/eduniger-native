package com.ninotech.fabi.controleur.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.ninotech.fabi.controleur.adapter.VoidContainerAdapter;
import com.ninotech.fabi.model.data.Notification;
import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.adapter.NotificationAdapter;
import com.ninotech.fabi.model.data.VoidContainer;
import com.ninotech.fabi.model.table.NotificationTable;
import com.ninotech.fabi.model.table.Session;

import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
        ab.setHomeAsUpIndicator(R.drawable.vector_back);
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setCustomView(R.layout.custom_action_bar);
        ab.setDisplayHomeAsUpEnabled(true);
        TextView actionBarTitle = ab.getCustomView().findViewById(R.id.action_bar_title);
        actionBarTitle.setText(R.string.notification);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_activity_notification);
        mSession = new Session(this);
        mNotificationTable = new NotificationTable(this);
        mNotifications = new ArrayList<Notification>();
        Cursor cursor = mNotificationTable.getData(mSession.getIdNumber());
        cursor.moveToFirst();
        try {
            do {
                mNotifications.add(new Notification(cursor.getString(2),cursor.getString(3),cursor.getString(4)));
            }while(cursor.moveToNext());
            mNotificationAdapter = new NotificationAdapter(mNotifications);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getApplicationContext()));
            mRecyclerView.setAdapter(mNotificationAdapter);
            mRecyclerView.smoothScrollToPosition(mNotificationAdapter.getItemCount()-1);
        }catch (Exception e)
        {
            Log.e("ErrGetDataNotification",e.getMessage());
            voidContainer(R.drawable.img_message_suggestion,getString(R.string.no_notification));
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id)
        {
            case android.R.id.home:
                onBackPressed(); // Appel de la méthode onBackPressed() pour simuler le comportement du bouton retour
                return true;
            case R.id.item_menu_search:
                Intent searchIntent = new Intent(NotificationActivity.this,SearchActivity.class);
                searchIntent.putExtra("search_key","NOTIFICATION");
                startActivity(searchIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void voidContainer(int image , String message)
    {
        ArrayList<VoidContainer> voidContainers = new ArrayList<>();
        voidContainers.add(new VoidContainer(image,message));
        VoidContainerAdapter voidContainerAdapter = new VoidContainerAdapter(voidContainers);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(voidContainerAdapter);
    }
    private RecyclerView mRecyclerView;
    private NotificationAdapter mNotificationAdapter;
    private ArrayList<Notification> mNotifications;
    private NotificationTable mNotificationTable;
    private Session mSession;
}