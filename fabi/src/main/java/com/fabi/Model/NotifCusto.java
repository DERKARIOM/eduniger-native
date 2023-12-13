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

public class NotifCusto extends RecyclerView.Adapter<NotifCusto.MyViewHolder> {
    List<Notification> mListNotif;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public NotifCusto(List<Notification> listNotif) {
        mListNotif = listNotif;
    }
    @Override
    public NotifCusto.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.notif_block_layout,parent,false);
        return new MyViewHolder(view);
    }
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Notification item = mListNotif.get(position);
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

    public Notification getItem(int position) {
        return mListNotif.get(position);
    }

    public void Remove(int position){
        mListNotif.remove(position);
        notifyItemRemoved(position);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        private LinearLayout mLinearLayout;
        private LinearLayout mLayout;
        private TextView mMessage;
        private TextView mTime;
        private TextView mUser;
        private ImageView mVue;
        MyViewHolder(View itemView){
            super(itemView);
            mLinearLayout = itemView.findViewById(R.id.notif_block);
            mLayout = itemView.findViewById(R.id.blockP);
            mMessage = itemView.findViewById(R.id.messageNotif);
            mTime = itemView.findViewById(R.id.dateNotif);
            mUser = itemView.findViewById(R.id.userNotif);
            mVue = itemView.findViewById(R.id.vue);
            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
//            menu.add(Menu.NONE,R.id.infoNotif,Menu.NONE,"Information");
//            menu.add(Menu.NONE,R.id.suppNotif,Menu.NONE,"Supprimer");
//            menu.add(Menu.NONE,R.id.inportanteNotif,Menu.NONE,"Message importants");
        }
        void display(Notification notif){
            mMessage.setText(notif.getMessage());
            mTime.setText(notif.getTime());
            if(!notif.getUser().equals(notif.getMoi()))
            {
                mUser.setText(notif.getUser());
                mLinearLayout.setBackgroundResource(R.drawable.block_notif2);
                mUser.setTextColor(Color.parseColor("#E6252525"));
                mTime.setTextColor(Color.parseColor("#E6252525"));
                mMessage.setTextColor(Color.parseColor("#E6252525"));
                mLayout.setHorizontalGravity(200);
            }
            else
            {
                mUser.setText("Vous");
                mLinearLayout.setBackgroundResource(R.drawable.block_notif1);
                mUser.setTextColor(Color.parseColor("#B4EFEFEF"));
                mTime.setTextColor(Color.parseColor("#B4EFEFEF"));
                mMessage.setTextColor(Color.parseColor("#FFFFFF"));
                mLayout.setHorizontalGravity(Gravity.RIGHT);
            }
            if(notif.getVue().equals("true"))
            {
                mVue.setImageResource(R.drawable.check);
            }
            else {
                if(notif.getVue().equals("false"))
                    mVue.setImageResource(R.drawable.en_cour);
                else
                    mVue.setImageResource(R.color.transparente);
            }
        }

    }
}
