package com.ninotech.fabi.controleur.adapter;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.model.data.Notification;

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
    public NotificationAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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

    public void Remove(int position){
        mNotifications.remove(position);
        notifyItemRemoved(position);
    }
    public void filterList(ArrayList<Notification> filteredList) {
        mNotifications = filteredList;
        notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        private TextView mTitre;
        private TextView mMessage;
        private TextView mDate;
        MyViewHolder(View itemView){
            super(itemView);
            mTitre = itemView.findViewById(R.id.notifTitre);
            mMessage = itemView.findViewById(R.id.notifMessage);
            mDate = itemView.findViewById(R.id.notifDate);
            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
//            menu.add(Menu.NONE,R.id.infoNotif,Menu.NONE,"Information");
//            menu.add(Menu.NONE,R.id.suppNotif,Menu.NONE,"Supprimer");
//            menu.add(Menu.NONE,R.id.inportanteNotif,Menu.NONE,"Message importants");
        }
        void display(Notification notification){
            mTitre.setText(notification.getTitre());
            mMessage.setText(notification.getMessage());
            mDate.setText(notification.getDate());
        }

    }
}
