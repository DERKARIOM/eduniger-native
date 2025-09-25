package com.ninotech.fabi.controleur.adapter;
import android.Manifest;
import android.app.Activity;
import android.app.UiModeManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.activity.BookActivity;
import com.ninotech.fabi.model.data.Notification;
import com.ninotech.fabi.model.data.Themes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.MyViewHolder> {
    List<Notification> mNotifications;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public NotificationAdapter(List<Notification> notifications) {
        mNotifications = notifications;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_notification,parent,false);
        return new MyViewHolder(view);
    }
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Notification item = mNotifications.get(position);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mPosition = holder.getAdapterPosition();
                view.showContextMenu();
                return true;
            }
        });
        holder.display(mNotifications.get(position));

    }
    @Override
    public int getItemCount() {
        return mNotifications.size();
    }

    public Notification getItem(int position) {
        return mNotifications.get(position);
    }

    public void remove(int position){
        mNotifications.remove(position);
        notifyItemRemoved(position);
    }
    public void filterList(ArrayList<Notification> filteredList) {
        mNotifications = filteredList;
        notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        private RelativeLayout mRelativeLayout;
        private ImageView mIconImageView;
        private TextView mTitleTextView;
        private TextView mDate;
        private TextView mMessage;

        MyViewHolder(View itemView) {
            super(itemView);
            mRelativeLayout = itemView.findViewById(R.id.relative_layout_activity_notification);
            mIconImageView = itemView.findViewById(R.id.image_view_adapter_notification_icon);
            mTitleTextView = itemView.findViewById(R.id.text_view_adapter_notification_title);
            mMessage = itemView.findViewById(R.id.text_view_adapter_notification_message);
            mDate = itemView.findViewById(R.id.text_view_adapter_notification_date);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//            menu.add(Menu.NONE,R.id.infoNotif,Menu.NONE,"Information");
//            menu.add(Menu.NONE,R.id.suppNotif,Menu.NONE,"Supprimer");
//            menu.add(Menu.NONE,R.id.inportanteNotif,Menu.NONE,"Message importants");
        }

        void display(Notification notification) {
            mTitleTextView.setText(notification.getTitle());
            mDate.setText(notification.getDate());
            mMessage.setText(notification.getMessage());
            UiModeManager uiModeManager = null;
            switch (Themes.getName(itemView.getContext()))
            {
                case "system":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        uiModeManager = (UiModeManager) itemView.getContext().getSystemService(Context.UI_MODE_SERVICE);
                    }
                    int currentMode = uiModeManager.getNightMode();
                    if (currentMode == UiModeManager.MODE_NIGHT_NO) {
                        // code mode jour
                        themeNoNight(notification.getType());
                    }
                    else
                    {
                        // code mode nuit
                        themeNight(notification.getType());
                    }
                    break;
                case "notNight":
                    // code mode jour
                    themeNoNight(notification.getType());
                    break;
                case "night":
                    themeNight(notification.getType());
                    break;
            }
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (notification.getType())
                    {
                        case "0":
                            Toast.makeText(itemView.getContext(), "Notification Simple", Toast.LENGTH_SHORT).show();
                            break;
                        case "1":
                            openInBrowser(notification.getLink());
                            break;
                        case "2":
                            Intent intentBook = new Intent(itemView.getContext(), BookActivity.class);
                            intentBook.putExtra("intent_adapter_book_id", notification.getIdLink());
                            itemView.getContext().startActivity(intentBook);
                            break;
                    }
                }
            });
        }
        private void themeNoNight(String type)
        {
            switch (type)
            {
                case "0":
                    mRelativeLayout.setBackgroundResource(R.drawable.forme_white_radius_100dp_border_dark);
                    break;
                case "1":
                    mRelativeLayout.setBackgroundResource(R.drawable.forme_white_radius_100dp_border_blue);
                    break;
                case "2":
                    mRelativeLayout.setBackgroundResource(R.drawable.forme_white_radius_100dp_border_vert);
                    break;

            }
        }
        private void themeNight(String type)
        {
            switch (type)
            {
                case "0":
                    mRelativeLayout.setBackgroundResource(R.drawable.forme_black3_radius_100dp_border_dark);
                    break;
                case "1":
                    mRelativeLayout.setBackgroundResource(R.drawable.forme_black3_radius_100dp_border_blue);
                    break;
                case "2":
                    mRelativeLayout.setBackgroundResource(R.drawable.forme_black3_radius_100dp_border_vert);
                    break;

            }
        }
        private void openInBrowser(String url) {
            // S'assure qu'il y a un schéma
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);

            // Facultatif : forcer le choix du navigateur
            Intent chooser = Intent.createChooser(intent, "Ouvrir avec…");
            try {
                itemView.getContext().startActivity(chooser);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(itemView.getContext(), "Aucun navigateur trouvé.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
