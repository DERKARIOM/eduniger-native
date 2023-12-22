package com.fabi.Model;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.fabi.R;
import com.squareup.picasso.Picasso;

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
    public void onBindViewHolder(MyViewHolder holder, int position) {
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
        private ImageView mProfile;
        private TextView mNom;
        private TextView mMessage;
        MyViewHolder(View itemView){
            super(itemView);
            mProfile = itemView.findViewById(R.id.chatProfile);
            mNom = itemView.findViewById(R.id.chatNom);
            mMessage = itemView.findViewById(R.id.chatMessage);
            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
//            menu.add(Menu.NONE,R.id.infoNotif,Menu.NONE,"Information");
//            menu.add(Menu.NONE,R.id.suppNotif,Menu.NONE,"Supprimer");
//            menu.add(Menu.NONE,R.id.inportanteNotif,Menu.NONE,"Message importants");
        }
        void display(Chat chat){
            Picasso.with(itemView.getContext())
                    .load("http://192.168.43.1:2222/fabi/profil/" + chat.getProfile())
                    .placeholder(R.drawable.item)
                    .error(R.drawable.item)
                    .into(mProfile);
            mNom.setText(chat.getNom());
            mMessage.setText(chat.getMessage());
        }
    }
}