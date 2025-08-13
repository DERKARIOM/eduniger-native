package com.ninotech.fabi.controleur.activity;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.ninotech.fabi.controleur.adapter.StatusBarAdapter;
import com.ninotech.fabi.controleur.adapter.VoidContainerAdapter;
import com.ninotech.fabi.model.data.NotifNumber;
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
        NotifNumber.saveLocation(getApplicationContext(),0);
//        Cursor cursor = mNotificationTable.getData(mSession.getIdNumber());
//        cursor.moveToFirst();
//        try {
//            do {
//                switch (cursor.getString(7))
//                {
//                    case "0":
//                        mNotifications.add(new Notification(cursor.getString(0),cursor.getString(7),cursor.getString(2),cursor.getString(4),cursor.getString(3)));
//                        break;
//                    case "1":
//                        mNotifications.add(new Notification(cursor.getString(0),cursor.getString(7),cursor.getString(2),cursor.getString(4),cursor.getString(3),cursor.getString(5),cursor.getString(6)));
//                        break;
//                }
//            }while(cursor.moveToNext());
            mNotifications.add(new Notification("1","2","Mise a jour disponible","13/08/2025 09:30:01","Félicitation ! vous etes maintenant officiellement un client de telesafe. Merci pour votre achat"));
            mNotificationAdapter = new NotificationAdapter(mNotifications);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getApplicationContext()));
            registerForContextMenu(mRecyclerView);
            mRecyclerView.setAdapter(mNotificationAdapter);
//        }catch (Exception e)
//        {
//            Log.e("ErrGetDataNotification",e.getMessage());
//            voidContainer(R.drawable.img_message_suggestion,getString(R.string.no_notification));
//        }
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