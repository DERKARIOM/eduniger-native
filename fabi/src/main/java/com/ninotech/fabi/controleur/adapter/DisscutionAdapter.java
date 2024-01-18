package com.ninotech.fabi.controleur.adapter;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.model.data.Disscution;
import com.squareup.picasso.Picasso;

import java.util.List;

public class DisscutionAdapter extends RecyclerView.Adapter<DisscutionAdapter.MyViewHolder> {
    List<Disscution> mListDisscution;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public DisscutionAdapter(List<Disscution> listDisscution) {
        mListDisscution = listDisscution;
    }
    @Override
    public DisscutionAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_disscusion,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Disscution item = mListDisscution.get(position);
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

    public Disscution getItem(int position) {
        return mListDisscution.get(position);
    }

    public void Remove(int position){
        mListDisscution.remove(position);
        notifyItemRemoved(position);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        private ImageView mPhoto;
        private TextView mUsername;
        private TextView mMessage;
        MyViewHolder(View itemView){
            super(itemView);
            mPhoto = itemView.findViewById(R.id.profile_disscution);
            mUsername = itemView.findViewById(R.id.username);
            mMessage = itemView.findViewById(R.id.disscution);
            itemView.setOnCreateContextMenuListener(this);
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
//            menu.add(Menu.NONE,R.id.infoNotif,Menu.NONE,"Information");
//            menu.add(Menu.NONE,R.id.suppNotif,Menu.NONE,"Supprimer");
//            menu.add(Menu.NONE,R.id.inportanteNotif,Menu.NONE,"Message importants");
        }
        void display(Disscution disscution){
            Picasso.with(itemView.getContext())
                    .load("http://192.168.43.1:2222/fabi/profil/" + disscution.getProfil())
                    .placeholder(R.drawable.img_default_livre)
                    .error(R.drawable.img_default_livre)
                    .into(mPhoto);
            mUsername.setText(disscution.getUsername());
            mMessage.setText(disscution.getMessage());
        }
    }
}