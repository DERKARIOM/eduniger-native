package com.ninotech.fabi.controleur.adapter;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.model.data.Notification;

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
        private ImageView mIconImageView;
        private TextView mTitleTextView;
        private TextView mDate;
        private TextView mMessage;

        MyViewHolder(View itemView) {
            super(itemView);
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
//            switch (notification.getType())
//            {
//                case "0":
//                    mIconImageView.setImageResource(R.drawable.img_activate_service);
//                    break;
//                case "1":
//                    mIconImageView.setImageResource(R.drawable.img_carte_sim);
//                    break;
//            }
            mTitleTextView.setText(notification.getTitle());
            mDate.setText(notification.getDate());
            mMessage.setText(notification.getMessage());
//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    switch (notification.getType())
//                    {
//                        case "0":
//                            messageYesOrNo(R.mipmap.safe_round,"Installation requise","Pour utiliser pleinement toutes les fonctionnalités de TeleSafe, vous devez installer le service Safe. Veuillez installer ce service pour garantir la sécurité et le suivi de votre appareil.\n\nVoulez-vous procéder à l'installation maintenant ?");
//                            break;
//                        case "1":
//                            gotoLocalisationMaps(notification.getLatitude(),notification.getLongitude());
//                            break;
//                    }
//
//                }
//            });
        }
    }
}
