package com.ninotech.fabi.controleur.adapter;

import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.fragment.BooksFragment;
import com.ninotech.fabi.model.data.Chat;
import com.ninotech.fabi.model.data.Connection;
import com.ninotech.fabi.model.data.Server;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {
    List<Chat> mListDisscution;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public ChatAdapter(List<Chat> listDisscution) {
        mListDisscution = listDisscution;
    }
    @Override
    public ChatAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_chat,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Chat item = mListDisscution.get(position);
        int i = position;
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mPosition = holder.getAdapterPosition();
                view.showContextMenu();
                return true;
            }
        });
        holder.display(mListDisscution.get(position));

    }
    @Override
    public int getItemCount() {
        return mListDisscution.size();
    }

    public Chat getItem(int position) {
        return mListDisscution.get(position);
    }

    public void Remove(int position){
        mListDisscution.remove(position);
        notifyItemRemoved(position);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        private RelativeLayout mRelativeLayout;
        private ImageView mProfile;
        private TextView mNom;
        private TextView mMessage;
        private ImageView mWaitChatImageView;
        MyViewHolder(View itemView){
            super(itemView);
            mRelativeLayout = itemView.findViewById(R.id.relative_layout_adapter_chat);
            mProfile = itemView.findViewById(R.id.chatProfile);
            mNom = itemView.findViewById(R.id.chatNom);
            mMessage = itemView.findViewById(R.id.chatMessage);
            mWaitChatImageView = itemView.findViewById(R.id.image_view_adapter_wait_chat);
            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
//            menu.add(Menu.NONE,R.id.infoNotif,Menu.NONE,"Information");
//            menu.add(Menu.NONE,R.id.suppNotif,Menu.NONE,"Supprimer");
//            menu.add(Menu.NONE,R.id.inportanteNotif,Menu.NONE,"Message importants");
        }
        void display(Chat chat){
            BroadcastReceiver receiverResponseChat = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if ("RESPONSE_CHAT_OK".equals(intent.getAction())) {
                        Animation pulseAnimImg = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.pulse);
                        // Lancer l'animation automatiquement
                        mWaitChatImageView.startAnimation(pulseAnimImg);
                        mWaitChatImageView.setVisibility(GONE);
                        Intent intentEndLine = new Intent("GO_TO_END_CHAT");
                        itemView.getContext().sendBroadcast(intentEndLine);
                    }
                }
            };
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                itemView.getContext().registerReceiver(receiverResponseChat, new IntentFilter("RESPONSE_CHAT_OK"),Context.RECEIVER_EXPORTED);
            }
            Animation pulseAnimImg = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.pulse);
            // Lancer l'animation automatiquement
            mWaitChatImageView.startAnimation(pulseAnimImg);
            if(chat.isChat())
            {
                mWaitChatImageView.setVisibility(GONE);
                Picasso.get()
                        .load(R.drawable.ia_new)
                        .placeholder(R.drawable.ia_new)
                        .error(R.drawable.ia_new)
                        .into(mProfile);
            }
            else
            {
                mProfile.setVisibility(GONE);
                mNom.setVisibility(GONE);
                mWaitChatImageView.setVisibility(View.VISIBLE);
                mMessage.setTextColor(Color.parseColor("#FFFFFFFF"));
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mRelativeLayout.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_END);
                mRelativeLayout.setLayoutParams(params);
                mRelativeLayout.setBackground(itemView.getResources().getDrawable(R.drawable.forme_black3_radius_10dp));
                Picasso.get()
                        .load(R.drawable.img_wait_profile)
                        .placeholder(R.drawable.img_wait_profile)
                        .error(R.drawable.img_wait_profile)
                        .into(mProfile);
            }
            mNom.setText(chat.getUserName());
            mMessage.setText(chat.getMessage());
        }
    }
}