package com.ninotech.fabi.controleur.activity;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ninotech.fabi.controleur.adapter.VoidContainerAdapter;
import com.ninotech.fabi.model.data.Notification;
import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.adapter.NotificationAdapter;
import com.ninotech.fabi.model.data.Themes;
import com.ninotech.fabi.model.data.VoidContainer;
import com.ninotech.fabi.model.table.NotificationTable;
import com.ninotech.fabi.model.table.Session;

import java.io.File;
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
        UiModeManager uiModeManager = null;
        switch (Themes.getName(getApplicationContext()))
        {
            case "system":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    uiModeManager = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);
                }
                int currentMode = uiModeManager.getNightMode();
                if (currentMode == UiModeManager.MODE_NIGHT_YES) {
                    // mode sombre
                    ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black)));
                    actionBarTitle.setTextColor(Color.parseColor("#B4EFEFEF"));
                } else {
                    // mode jours
                    ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
                    ab.setHomeAsUpIndicator(R.drawable.vector_back);
                }
                break;
            case "notNight":
                // mode jours
                ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
                ab.setHomeAsUpIndicator(R.drawable.vector_back);
                break;
            case "night":
                // mode nuit
                ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black)));
                ab.setHomeAsUpIndicator(R.drawable.vector_white_sombre_back);
                actionBarTitle.setTextColor(Color.parseColor("#B4EFEFEF"));
                break;
        }
//        cursor.moveToFirst();
//        try {
//            do {
//                mNotifications.add(new Notification(cursor.getString(0),cursor.getString(2),cursor.getString(3),cursor.getString(4)));
//            }while(cursor.moveToNext());
            mNotifications.add(new Notification("1","Nouveauté sur EduNiger !","Le livre le tambour sacré de Zama est désormais disponible sur notre plateforme.","08/08/2025"));
            mNotifications.add(new Notification("2","Nouveauté sur EduNiger !","Réunion de samedi\n" +
                    "\n" +
                    " Thème : autour des travaux NINOTECH \n" +
                    "1. Rapport des directeurs et orientation des dire tions\n" +
                    "2. Analyse de la modélisation de logiciel anti plagiat\n" +
                    "3. Stratégie d'expansion d'EduNiger \n" +
                    "4.  Prise de décision sur la proposition des pourcentages pour le projet Gida Jari\n" +
                    "\n" +
                    " Lieu : siège NINOTECH\n" +
                    " Date  : 09/08/25\n" +
                    " Heure  : 09h00\n" +
                    "\n" +
                    "Ci-joint le fichier portant la proposition de de la modélisation de logiciel anti-plagiat \uD83D\uDC47\uD83D\uDC47\uD83D\uDC47\n","09/08/2025"));

            mNotificationAdapter = new NotificationAdapter(mNotifications);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getApplicationContext()));
            registerForContextMenu(mRecyclerView);
            mRecyclerView.setAdapter(mNotificationAdapter);
            mRecyclerView.smoothScrollToPosition(mNotificationAdapter.getItemCount()-1);
//        }catch (Exception e)
//        {
//            Log.e("ErrGetDataNotification",e.getMessage());
//            voidContainer(R.drawable.img_message_suggestion,getString(R.string.no_notification));
//        }
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
    private RecyclerView mRecyclerView;
    private NotificationAdapter mNotificationAdapter;
    private ArrayList<Notification> mNotifications;
    private Notification mNotificationSelect;
    private NotificationTable mNotificationTable;
    private Session mSession;
}