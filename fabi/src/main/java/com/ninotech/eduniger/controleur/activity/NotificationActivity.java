package com.ninotech.eduniger.controleur.activity;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.ninotech.eduniger.controleur.adapter.StatusBarAdapter;
import com.ninotech.eduniger.controleur.adapter.VoidContainerAdapter;
import com.ninotech.eduniger.model.data.NotifNumber;
import com.ninotech.eduniger.model.data.Notification;
import com.ninotech.eduniger.R;
import com.ninotech.eduniger.controleur.adapter.NotificationAdapter;
import com.ninotech.eduniger.model.data.Themes;
import com.ninotech.eduniger.model.data.VoidContainer;
import com.ninotech.eduniger.model.table.NotificationTable;
import com.ninotech.eduniger.model.table.Session;
import java.util.ArrayList;
import java.util.Objects;

public class NotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        ActionBar ab = getSupportActionBar();
        UiModeManager uiModeManager = null;
        switch (Themes.getName(getApplicationContext()))
        {
            case "system":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    uiModeManager = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);
                }
                int currentMode = uiModeManager.getNightMode();
                if (currentMode == UiModeManager.MODE_NIGHT_NO) {
                    StatusBarAdapter statusBarAdapter = new StatusBarAdapter(this,getWindow());
                }
                break;
            case "notNight":
                StatusBarAdapter statusBarAdapter = new StatusBarAdapter(this,getWindow());
                break;
        }
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_activity_notification);
        mSession = new Session(this);
        mNotificationTable = new NotificationTable(this);
        mNotifications = new ArrayList<Notification>();
        NotifNumber.saveLocation(getApplicationContext(),0);
        Cursor cursor = mNotificationTable.getData(mSession.getIdNumber());
        cursor.moveToFirst();
        try {
            do {
                mNotifications.add(new Notification(cursor.getString(0),cursor.getString(7),cursor.getString(2),cursor.getString(4),cursor.getString(3),cursor.getString(5),cursor.getString(6)));
            }while(cursor.moveToNext());
            mNotificationAdapter = new NotificationAdapter(mNotifications);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getApplicationContext()));
            registerForContextMenu(mRecyclerView);
            mRecyclerView.setAdapter(mNotificationAdapter);
        }catch (Exception e)
        {
            Log.e("ErrGetDataNotification",e.getMessage());
            voidContainer(R.drawable.img_message_suggestion,getString(R.string.no_notification));
        }
    }
    public void voidContainer(int image , String message)
    {
        ArrayList<VoidContainer> voidContainers = new ArrayList<>();
        voidContainers.add(new VoidContainer(image,message));
        VoidContainerAdapter voidContainerAdapter = new VoidContainerAdapter(voidContainers);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(voidContainerAdapter);
    }
    private void openGoogleMapsInPlayStore() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps"));
        intent.setPackage("com.android.vending");
        startActivity(intent);
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_item,menu);
        mNotificationSelect = mNotificationAdapter.getItem(mNotificationAdapter.getPosition());
    }
    @Override
    public boolean onContextItemSelected(MenuItem item){
        switch (item.getItemId())
        {
            case R.id.menu_item_delete:
                mNotificationTable.remove(mNotificationSelect.getId());
                mNotificationAdapter.remove(mNotificationAdapter.getPosition());
                break;
            default:
                return super.onContextItemSelected(item);
        }
        return false;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Vérifier si l'item sélectionné est le bouton de retour
        if (item.getItemId() == android.R.id.home) {
            // Appeler la méthode onBackPressed pour simuler un back press
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private RecyclerView mRecyclerView;
    private NotificationAdapter mNotificationAdapter;
    private ArrayList<Notification> mNotifications;
    private Notification mNotificationSelect;
    private Session mSession;
    private NotificationTable mNotificationTable;
}