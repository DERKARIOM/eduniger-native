package com.fabi.Model;
import android.graphics.Color;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.fabi.R;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.MyViewHolder> {
    List<Notif> mListNotif;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public NotificationAdapter(List<Notif> listNotif) {
        mListNotif = listNotif;
    }
    @Override
    public NotificationAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.notification_adapter,parent,false);
        return new MyViewHolder(view);
    }
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Notif item = mListNotif.get(position);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mPosition = holder.getAdapterPosition();
                view.showContextMenu();
                return true;
            }
        });
        holder.display(mListNotif.get(position));

    }
    @Override
    public int getItemCount() {
        return mListNotif.size();
    }

    public Notif getItem(int position) {
        return mListNotif.get(position);
    }

    public void Remove(int position){
        mListNotif.remove(position);
        notifyItemRemoved(position);
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
        void display(Notif notif){
            mTitre.setText(notif.getTitre());
            mMessage.setText(notif.getMessage());
            mDate.setText(notif.getDate());
        }

    }
}
